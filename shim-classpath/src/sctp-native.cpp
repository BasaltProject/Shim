/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

#include "jni.h"
#include "jni-util.h"

#include <stdint.h>
#include <string.h>

#if defined(__linux__)
#include <arpa/inet.h>
#include <errno.h>
#include <netinet/in.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <unistd.h>

namespace {

const int kSolSctp = 132;
const jlong kV6Flag = static_cast<jlong>(1) << 40;
const jlong kHandleMask = (static_cast<jlong>(1) << 40) - 1;

const int kOptNodelay = 3;
const int kOptInitmsg = 2;
const int kOptDisableFragments = 8;
const int kOptEvents = 11;
const int kOptFragmentInterleave = 18;
const int kOptExplicitEor = 27;
const int kOptStatus = 14;
const int kOptPrimaryAddr = 6;
const int kOptSetPeerPrimaryAddr = 5;
const int kOptGetPeerAddrs = 108;
const int kOptGetLocalAddrs = 109;
const int kOptBindxAdd = 100;
const int kOptBindxRem = 101;
const int kOptPeeloff = 102;

const int kCmsgSndrcv = 1;
const int kFlagUnordered = 1;
const int kFlagAbort = 4;
const int kFlagEof = 0x200;
const int kMsgNotification = 0x8000;
const int kMsgEor = 0x80;

const uint16_t kSnBase = 1 << 15;

struct SndRcvInfo {
  uint16_t stream;
  uint16_t ssn;
  uint16_t flags;
  uint32_t ppid;
  uint32_t context;
  uint32_t timetolive;
  uint32_t tsn;
  uint32_t cumtsn;
  int32_t assocId;
};

struct InitMsg {
  uint16_t numOstreams;
  uint16_t maxInstreams;
  uint16_t maxAttempts;
  uint16_t maxInitTimeo;
};

struct EventSubscribe {
  uint8_t dataIo;
  uint8_t association;
  uint8_t address;
  uint8_t sendFailure;
  uint8_t peerError;
  uint8_t shutdown;
};

struct NotifHeader {
  uint16_t type;
  uint16_t flags;
  uint32_t length;
};

struct AssocChange {
  NotifHeader h;
  uint16_t state;
  uint16_t error;
  uint16_t outStreams;
  uint16_t inStreams;
  int32_t assocId;
};

struct PaddrChange {
  NotifHeader h;
  struct sockaddr_storage addr;
  int32_t state;
  int32_t error;
  int32_t assocId;
};

struct SendFailed {
  NotifHeader h;
  uint32_t error;
  SndRcvInfo info;
  int32_t assocId;
};

struct ShutdownEvent {
  NotifHeader h;
  int32_t assocId;
};

struct Status {
  int32_t assocId;
  int32_t state;
  uint32_t rwnd;
  uint16_t unackdata;
  uint16_t penddata;
  uint16_t instrms;
  uint16_t outstrms;
  uint8_t rest[256];
};

struct Prim {
  int32_t assocId;
  struct sockaddr_storage addr;
};

struct GetAddrs {
  int32_t assocId;
  uint32_t num;
  uint8_t addrs[4096];
};

struct PeeloffArg {
  int32_t assocId;
  int32_t sd;
};

int handleFd(jlong h)
{
  return static_cast<int>(h & kHandleMask);
}

bool handleV6(jlong h)
{
  return (h & kV6Flag) != 0;
}

void throwErrno(JNIEnv* e, int err)
{
  const char* cls = "java/io/IOException";
  if (err == ECONNREFUSED) {
    cls = "java/net/ConnectException";
  } else if (err == EADDRINUSE || err == EADDRNOTAVAIL || err == EACCES) {
    cls = "java/net/BindException";
  }
  throwNew(e, cls, "%s", strerror(err));
}

int makeAddress(JNIEnv* e, jbyteArray addr, int port, bool v6, struct sockaddr_storage* ss)
{
  memset(ss, 0, sizeof(*ss));
  jbyte bytes[16];
  int len = 0;
  if (addr != 0) {
    len = e->GetArrayLength(addr);
    if (len != 4 && len != 16) {
      throwNew(e, "java/io/IOException", "Invalid address length");
      return 0;
    }
    e->GetByteArrayRegion(addr, 0, len, bytes);
  }
  if (v6) {
    struct sockaddr_in6* a6 = reinterpret_cast<struct sockaddr_in6*>(ss);
    a6->sin6_family = AF_INET6;
    a6->sin6_port = htons(static_cast<unsigned short>(port));
    bool zero4 = len == 4 && bytes[0] == 0 && bytes[1] == 0 && bytes[2] == 0 && bytes[3] == 0;
    if (len == 0 || zero4) {
      a6->sin6_addr = in6addr_any;
    } else if (len == 4) {
      unsigned char* b = reinterpret_cast<unsigned char*>(&a6->sin6_addr);
      memset(b, 0, 10);
      b[10] = 0xff;
      b[11] = 0xff;
      memcpy(b + 12, bytes, 4);
    } else {
      memcpy(&a6->sin6_addr, bytes, 16);
    }
    return sizeof(struct sockaddr_in6);
  }
  struct sockaddr_in* a4 = reinterpret_cast<struct sockaddr_in*>(ss);
  a4->sin_family = AF_INET;
  a4->sin_port = htons(static_cast<unsigned short>(port));
  if (len == 0) {
    a4->sin_addr.s_addr = htonl(INADDR_ANY);
  } else if (len == 4) {
    memcpy(&a4->sin_addr, bytes, 4);
  } else {
    unsigned char* b = reinterpret_cast<unsigned char*>(bytes);
    memcpy(&a4->sin_addr, b + 12, 4);
  }
  return sizeof(struct sockaddr_in);
}

jbyteArray addressBytes(JNIEnv* e, const struct sockaddr_storage* ss, int* port)
{
  if (ss->ss_family == AF_INET6) {
    const struct sockaddr_in6* a6 = reinterpret_cast<const struct sockaddr_in6*>(ss);
    if (port) {
      *port = ntohs(a6->sin6_port);
    }
    const unsigned char* b = reinterpret_cast<const unsigned char*>(&a6->sin6_addr);
    bool mapped = true;
    for (int i = 0; i < 10; i++) {
      mapped = mapped && b[i] == 0;
    }
    if (mapped && b[10] == 0xff && b[11] == 0xff) {
      jbyteArray r = e->NewByteArray(4);
      e->SetByteArrayRegion(r, 0, 4, reinterpret_cast<const jbyte*>(b + 12));
      return r;
    }
    jbyteArray r = e->NewByteArray(16);
    e->SetByteArrayRegion(r, 0, 16, reinterpret_cast<const jbyte*>(b));
    return r;
  }
  const struct sockaddr_in* a4 = reinterpret_cast<const struct sockaddr_in*>(ss);
  if (port) {
    *port = ntohs(a4->sin_port);
  }
  jbyteArray r = e->NewByteArray(4);
  e->SetByteArrayRegion(r, 0, 4, reinterpret_cast<const jbyte*>(&a4->sin_addr));
  return r;
}

int sockaddrLength(const struct sockaddr* sa)
{
  return sa->sa_family == AF_INET6 ? sizeof(struct sockaddr_in6) : sizeof(struct sockaddr_in);
}

int openSocket(bool oneToMany, bool* v6)
{
  int type = oneToMany ? SOCK_SEQPACKET : SOCK_STREAM;
  int fd = ::socket(AF_INET6, type, IPPROTO_SCTP);
  *v6 = fd >= 0;
  if (fd >= 0) {
    int off = 0;
    setsockopt(fd, IPPROTO_IPV6, IPV6_V6ONLY, &off, sizeof(off));
  } else {
    fd = ::socket(AF_INET, type, IPPROTO_SCTP);
  }
  return fd;
}

}  // namespace

extern "C" JNIEXPORT jboolean JNICALL Java_shim_SctpNative_supported(JNIEnv*, jclass)
{
  bool v6 = false;
  int fd = openSocket(false, &v6);
  if (fd < 0) {
    return JNI_FALSE;
  }
  ::close(fd);
  return JNI_TRUE;
}

extern "C" JNIEXPORT jlong JNICALL Java_shim_SctpNative_socket(JNIEnv* e, jclass, jboolean oneToMany)
{
  bool v6 = false;
  int fd = openSocket(oneToMany, &v6);
  if (fd < 0) {
    int err = errno;
    if (err == EPROTONOSUPPORT || err == ESOCKTNOSUPPORT || err == EAFNOSUPPORT || err == EPERM) {
      throwNew(e, "java/lang/UnsupportedOperationException", "SCTP is not supported on this system");
    } else {
      throwErrno(e, err);
    }
    return -1;
  }
  EventSubscribe ev;
  memset(&ev, 0, sizeof(ev));
  ev.dataIo = 1;
  ev.association = 1;
  ev.address = 1;
  ev.sendFailure = 1;
  ev.shutdown = 1;
  setsockopt(fd, kSolSctp, kOptEvents, &ev, sizeof(ev));
  return static_cast<jlong>(fd) | (v6 ? kV6Flag : 0);
}

extern "C" JNIEXPORT void JNICALL Java_shim_SctpNative_bindx(JNIEnv* e, jclass, jlong h, jobjectArray addrs, jint port,
                                                             jboolean add)
{
  int n = e->GetArrayLength(addrs);
  unsigned char packed[4096];
  int used = 0;
  for (int i = 0; i < n; i++) {
    jbyteArray a = static_cast<jbyteArray>(e->GetObjectArrayElement(addrs, i));
    struct sockaddr_storage ss;
    int len = makeAddress(e, a, port, handleV6(h), &ss);
    if (len == 0) {
      return;
    }
    if (used + len > static_cast<int>(sizeof(packed))) {
      throwNew(e, "java/io/IOException", "Too many addresses");
      return;
    }
    memcpy(packed + used, &ss, len);
    used += len;
  }
  if (setsockopt(handleFd(h), kSolSctp, add ? kOptBindxAdd : kOptBindxRem, packed, used) != 0) {
    throwErrno(e, errno);
  }
}

extern "C" JNIEXPORT jint JNICALL Java_shim_SctpNative_send(JNIEnv* e, jclass, jlong h, jbyteArray buf, jint off, jint len,
                                                            jint assocId, jint stream, jint ppid, jint flags, jlong ttl,
                                                            jbyteArray addr, jint port, jboolean nonblock)
{
  jbyte* data = static_cast<jbyte*>(malloc(len > 0 ? len : 1));
  e->GetByteArrayRegion(buf, off, len, data);
  struct iovec iov;
  iov.iov_base = data;
  iov.iov_len = len;
  struct msghdr msg;
  memset(&msg, 0, sizeof(msg));
  msg.msg_iov = &iov;
  msg.msg_iovlen = 1;
  struct sockaddr_storage ss;
  if (addr != 0) {
    int al = makeAddress(e, addr, port, handleV6(h), &ss);
    if (al == 0) {
      free(data);
      return -1;
    }
    msg.msg_name = &ss;
    msg.msg_namelen = al;
  }
  char control[CMSG_SPACE(sizeof(SndRcvInfo))];
  memset(control, 0, sizeof(control));
  msg.msg_control = control;
  msg.msg_controllen = sizeof(control);
  struct cmsghdr* c = CMSG_FIRSTHDR(&msg);
  c->cmsg_level = kSolSctp;
  c->cmsg_type = kCmsgSndrcv;
  c->cmsg_len = CMSG_LEN(sizeof(SndRcvInfo));
  SndRcvInfo info;
  memset(&info, 0, sizeof(info));
  info.stream = static_cast<uint16_t>(stream);
  info.ppid = htonl(static_cast<uint32_t>(ppid));
  info.flags = static_cast<uint16_t>(flags);
  info.timetolive = static_cast<uint32_t>(ttl);
  info.assocId = assocId;
  memcpy(CMSG_DATA(c), &info, sizeof(info));
  int mflags = nonblock ? MSG_DONTWAIT : 0;
  for (;;) {
    ssize_t n = sendmsg(handleFd(h), &msg, mflags);
    if (n < 0) {
      int err = errno;
      if (err == EINTR) {
        continue;
      }
      free(data);
      if (err == EAGAIN || err == EWOULDBLOCK) {
        return -2;
      }
      throwErrno(e, err);
      return -1;
    }
    free(data);
    return static_cast<jint>(n);
  }
}

extern "C" JNIEXPORT jint JNICALL Java_shim_SctpNative_recv(JNIEnv* e, jclass, jlong h, jbyteArray buf, jint off, jint len,
                                                            jboolean nonblock, jlongArray info, jobjectArray addrOut)
{
  int fd = handleFd(h);
  int mflags = nonblock ? MSG_DONTWAIT : 0;
  int cap = len < 4096 ? 4096 : len;
  unsigned char* data = static_cast<unsigned char*>(malloc(cap));
  struct sockaddr_storage ss;
  char control[CMSG_SPACE(sizeof(SndRcvInfo)) + 64];
  struct msghdr msg;
  struct iovec iov;
  ssize_t n;
  bool notification = false;
  if (len < 4096) {
    iov.iov_base = data;
    iov.iov_len = cap;
    memset(&msg, 0, sizeof(msg));
    msg.msg_iov = &iov;
    msg.msg_iovlen = 1;
    for (;;) {
      n = recvmsg(fd, &msg, mflags | MSG_PEEK);
      if (n < 0 && errno == EINTR) {
        continue;
      }
      break;
    }
    if (n < 0) {
      int err = errno;
      free(data);
      if (err == EAGAIN || err == EWOULDBLOCK) {
        return -2;
      }
      if (err == ENOTCONN) {
        return -1;
      }
      throwErrno(e, err);
      return -1;
    }
    notification = (msg.msg_flags & kMsgNotification) != 0;
  }
  memset(&msg, 0, sizeof(msg));
  memset(&ss, 0, sizeof(ss));
  iov.iov_base = data;
  iov.iov_len = (len < 4096 && !notification) ? len : cap;
  msg.msg_iov = &iov;
  msg.msg_iovlen = 1;
  msg.msg_name = &ss;
  msg.msg_namelen = sizeof(ss);
  msg.msg_control = control;
  msg.msg_controllen = sizeof(control);
  for (;;) {
    n = recvmsg(fd, &msg, mflags);
    if (n < 0 && errno == EINTR) {
      continue;
    }
    break;
  }
  if (n < 0) {
    int err = errno;
    free(data);
    if (err == EAGAIN || err == EWOULDBLOCK) {
      return -2;
    }
    if (err == ENOTCONN) {
      return -1;
    }
    throwErrno(e, err);
    return -1;
  }
  jlong out[12];
  memset(out, 0, sizeof(out));
  notification = (msg.msg_flags & kMsgNotification) != 0;
  int port = 0;
  jbyteArray peer = 0;
  if (msg.msg_namelen > 0) {
    peer = addressBytes(e, &ss, &port);
  }
  out[4] = port;
  if (!notification) {
    for (struct cmsghdr* c = CMSG_FIRSTHDR(&msg); c != 0; c = CMSG_NXTHDR(&msg, c)) {
      if (c->cmsg_level == kSolSctp && c->cmsg_type == kCmsgSndrcv) {
        SndRcvInfo si;
        memcpy(&si, CMSG_DATA(c), sizeof(si));
        out[0] = si.assocId;
        out[1] = si.stream;
        out[2] = static_cast<jlong>(ntohl(si.ppid));
        if (si.flags & kFlagUnordered) {
          out[3] |= 2;
        }
      }
    }
    if (msg.msg_flags & kMsgEor) {
      out[3] |= 1;
    }
    if (n == 0 && len > 0) {
      free(data);
      e->SetLongArrayRegion(info, 0, 12, out);
      return -1;
    }
    int copy = static_cast<int>(n) > len ? len : static_cast<int>(n);
    e->SetByteArrayRegion(buf, off, copy, reinterpret_cast<jbyte*>(data));
    n = copy;
  } else {
    out[3] |= 4;
    const NotifHeader* nh = reinterpret_cast<const NotifHeader*>(data);
    if (n >= static_cast<ssize_t>(sizeof(NotifHeader))) {
      if (nh->type == kSnBase + 1 && n >= static_cast<ssize_t>(sizeof(AssocChange))) {
        const AssocChange* a = reinterpret_cast<const AssocChange*>(data);
        out[5] = 1;
        out[0] = a->assocId;
        out[6] = a->state;
        out[7] = a->outStreams;
        out[8] = a->inStreams;
      } else if (nh->type == kSnBase + 2 && n >= static_cast<ssize_t>(sizeof(PaddrChange))) {
        const PaddrChange* p = reinterpret_cast<const PaddrChange*>(data);
        out[5] = 2;
        out[0] = p->assocId;
        out[6] = p->state;
        int pp = 0;
        peer = addressBytes(e, &p->addr, &pp);
        out[4] = pp;
      } else if (nh->type == kSnBase + 3 && n >= static_cast<ssize_t>(sizeof(SendFailed))) {
        const SendFailed* s = reinterpret_cast<const SendFailed*>(data);
        out[5] = 3;
        out[0] = s->assocId;
        out[6] = s->error;
        out[7] = s->info.stream;
        int dl = static_cast<int>(n) - static_cast<int>(sizeof(SendFailed));
        if (dl > 0) {
          int copy = dl > len ? len : dl;
          e->SetByteArrayRegion(buf, off, copy, reinterpret_cast<jbyte*>(data + sizeof(SendFailed)));
          out[8] = copy;
        }
      } else if (nh->type == kSnBase + 5 && n >= static_cast<ssize_t>(sizeof(ShutdownEvent))) {
        const ShutdownEvent* s = reinterpret_cast<const ShutdownEvent*>(data);
        out[5] = 4;
        out[0] = s->assocId;
      }
    }
    n = 0;
  }
  free(data);
  e->SetLongArrayRegion(info, 0, 12, out);
  if (addrOut != 0 && peer != 0) {
    e->SetObjectArrayElement(addrOut, 0, peer);
  }
  return static_cast<jint>(n);
}

extern "C" JNIEXPORT jint JNICALL Java_shim_SctpNative_addresses(JNIEnv* e, jclass, jlong h, jint assocId, jboolean peer,
                                                                 jobjectArray addrs, jintArray ports)
{
  GetAddrs ga;
  memset(&ga, 0, sizeof(ga));
  ga.assocId = assocId;
  socklen_t len = sizeof(ga);
  if (getsockopt(handleFd(h), kSolSctp, peer ? kOptGetPeerAddrs : kOptGetLocalAddrs, &ga, &len) != 0) {
    throwErrno(e, errno);
    return 0;
  }
  int max = e->GetArrayLength(addrs);
  unsigned char* p = ga.addrs;
  int count = 0;
  for (uint32_t i = 0; i < ga.num && count < max; i++) {
    const struct sockaddr* sa = reinterpret_cast<const struct sockaddr*>(p);
    struct sockaddr_storage ss;
    memset(&ss, 0, sizeof(ss));
    int sl = sockaddrLength(sa);
    memcpy(&ss, sa, sl);
    int port = 0;
    jbyteArray b = addressBytes(e, &ss, &port);
    e->SetObjectArrayElement(addrs, count, b);
    jint pv = port;
    e->SetIntArrayRegion(ports, count, 1, &pv);
    count++;
    p += sl;
  }
  return count;
}

extern "C" JNIEXPORT void JNICALL Java_shim_SctpNative_status(JNIEnv* e, jclass, jlong h, jint assocId, jintArray out)
{
  Status st;
  memset(&st, 0, sizeof(st));
  st.assocId = assocId;
  socklen_t len = sizeof(st);
  if (getsockopt(handleFd(h), kSolSctp, kOptStatus, &st, &len) != 0) {
    throwErrno(e, errno);
    return;
  }
  jint v[4] = {st.assocId, st.instrms, st.outstrms, st.state};
  e->SetIntArrayRegion(out, 0, 4, v);
}

extern "C" JNIEXPORT jlong JNICALL Java_shim_SctpNative_peeloff(JNIEnv* e, jclass, jlong h, jint assocId)
{
  PeeloffArg a;
  a.assocId = assocId;
  a.sd = -1;
  socklen_t len = sizeof(a);
  int fd = getsockopt(handleFd(h), kSolSctp, kOptPeeloff, &a, &len);
  if (fd < 0) {
    throwErrno(e, errno);
    return -1;
  }
  return static_cast<jlong>(fd) | (handleV6(h) ? kV6Flag : 0);
}

extern "C" JNIEXPORT void JNICALL Java_shim_SctpNative_setOpt(JNIEnv* e, jclass, jlong h, jint id, jint a, jint b)
{
  int fd = handleFd(h);
  int r = 0;
  if (id == 2) {
    InitMsg im;
    memset(&im, 0, sizeof(im));
    im.numOstreams = static_cast<uint16_t>(a);
    im.maxInstreams = static_cast<uint16_t>(b);
    r = setsockopt(fd, kSolSctp, kOptInitmsg, &im, sizeof(im));
  } else {
    int name = id == 1 ? kOptNodelay : id == 3 ? kOptDisableFragments : id == 4 ? kOptFragmentInterleave : kOptExplicitEor;
    int v = a;
    r = setsockopt(fd, kSolSctp, name, &v, sizeof(v));
  }
  if (r != 0) {
    throwErrno(e, errno);
  }
}

extern "C" JNIEXPORT void JNICALL Java_shim_SctpNative_getOpt(JNIEnv* e, jclass, jlong h, jint id, jintArray out)
{
  int fd = handleFd(h);
  if (id == 2) {
    InitMsg im;
    socklen_t len = sizeof(im);
    if (getsockopt(fd, kSolSctp, kOptInitmsg, &im, &len) != 0) {
      throwErrno(e, errno);
      return;
    }
    jint v[2] = {im.numOstreams, im.maxInstreams};
    e->SetIntArrayRegion(out, 0, 2, v);
    return;
  }
  int name = id == 1 ? kOptNodelay : id == 3 ? kOptDisableFragments : id == 4 ? kOptFragmentInterleave : kOptExplicitEor;
  int v = 0;
  socklen_t len = sizeof(v);
  if (getsockopt(fd, kSolSctp, name, &v, &len) != 0) {
    throwErrno(e, errno);
    return;
  }
  jint j = v;
  e->SetIntArrayRegion(out, 0, 1, &j);
}

extern "C" JNIEXPORT void JNICALL Java_shim_SctpNative_setPrimary(JNIEnv* e, jclass, jlong h, jint assocId, jbyteArray addr,
                                                                  jint port, jboolean peerPrimary)
{
  Prim p;
  memset(&p, 0, sizeof(p));
  p.assocId = assocId;
  if (makeAddress(e, addr, port, handleV6(h), &p.addr) == 0) {
    return;
  }
  if (setsockopt(handleFd(h), kSolSctp, peerPrimary ? kOptSetPeerPrimaryAddr : kOptPrimaryAddr, &p, sizeof(p)) != 0) {
    throwErrno(e, errno);
  }
}

extern "C" JNIEXPORT jbyteArray JNICALL Java_shim_SctpNative_getPrimary(JNIEnv* e, jclass, jlong h, jint assocId,
                                                                        jintArray portOut)
{
  Prim p;
  memset(&p, 0, sizeof(p));
  p.assocId = assocId;
  socklen_t len = sizeof(p);
  if (getsockopt(handleFd(h), kSolSctp, kOptPrimaryAddr, &p, &len) != 0) {
    throwErrno(e, errno);
    return 0;
  }
  int port = 0;
  jbyteArray b = addressBytes(e, &p.addr, &port);
  jint pv = port;
  e->SetIntArrayRegion(portOut, 0, 1, &pv);
  return b;
}

#else

extern "C" JNIEXPORT jboolean JNICALL Java_shim_SctpNative_supported(JNIEnv*, jclass)
{
  return JNI_FALSE;
}

extern "C" JNIEXPORT jlong JNICALL Java_shim_SctpNative_socket(JNIEnv* e, jclass, jboolean)
{
  jclass c = e->FindClass("java/lang/UnsupportedOperationException");
  if (c != 0) {
    e->ThrowNew(c, "SCTP is not supported on this system");
  }
  return -1;
}

#endif
