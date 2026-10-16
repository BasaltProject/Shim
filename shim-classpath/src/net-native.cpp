/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

#include "jni.h"
#include "jni-util.h"

#ifdef PLATFORM_WINDOWS
#include <winsock2.h>
#include <ws2tcpip.h>
#include <iphlpapi.h>
typedef SOCKET NetHandle;
#define NET_INVALID INVALID_SOCKET
#define NET_ERRNO() WSAGetLastError()
#define NET_EINTR WSAEINTR
#define NET_EWOULDBLOCK WSAEWOULDBLOCK
#define NET_EINPROGRESS WSAEWOULDBLOCK
#define NET_ECONNREFUSED WSAECONNREFUSED
#define NET_ECONNRESET WSAECONNRESET
#define NET_ETIMEDOUT WSAETIMEDOUT
#define NET_ENETUNREACH WSAENETUNREACH
#define NET_EHOSTUNREACH WSAEHOSTUNREACH
#define NET_EADDRINUSE WSAEADDRINUSE
#define NET_EADDRNOTAVAIL WSAEADDRNOTAVAIL
#define NET_EACCES WSAEACCES
#define NET_ENOTCONN WSAENOTCONN
#define NET_ENOTSOCK WSAENOTSOCK
#define NET_EPIPE WSAECONNABORTED
#define NET_CLOSE(s) closesocket(s)
#ifndef EAGAIN
#define EAGAIN WSAEWOULDBLOCK
#endif
typedef int NetLen;
typedef char NetOptVal;
#else
#include <arpa/inet.h>
#include <errno.h>
#include <fcntl.h>
#include <ifaddrs.h>
#include <net/if.h>
#include <netdb.h>
#include <netinet/in.h>
#include <netinet/tcp.h>
#include <poll.h>
#include <signal.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <sys/ioctl.h>
#include <sys/uio.h>
#include <unistd.h>
#ifdef __linux__
#include <netpacket/packet.h>
#endif
typedef int NetHandle;
#define NET_INVALID (-1)
#define NET_ERRNO() errno
#define NET_EINTR EINTR
#define NET_EWOULDBLOCK EWOULDBLOCK
#define NET_EINPROGRESS EINPROGRESS
#define NET_ECONNREFUSED ECONNREFUSED
#define NET_ECONNRESET ECONNRESET
#define NET_ETIMEDOUT ETIMEDOUT
#define NET_ENETUNREACH ENETUNREACH
#define NET_EHOSTUNREACH EHOSTUNREACH
#define NET_EADDRINUSE EADDRINUSE
#define NET_EADDRNOTAVAIL EADDRNOTAVAIL
#define NET_EACCES EACCES
#define NET_ENOTCONN ENOTCONN
#define NET_ENOTSOCK ENOTSOCK
#define NET_EPIPE EPIPE
#define NET_CLOSE(s) close(s)
typedef socklen_t NetLen;
typedef int NetOptVal;
#endif

#include <stdlib.h>
#include <string.h>

namespace {

const int kOptReuseAddr = 1;
const int kOptKeepAlive = 2;
const int kOptTcpNoDelay = 3;
const int kOptRcvBuf = 4;
const int kOptSndBuf = 5;
const int kOptLinger = 6;
const int kOptBroadcast = 7;
const int kOptTos = 8;
const int kOptOobInline = 9;
const int kOptReusePort = 10;
const int kOptMulticastTtl = 11;
const int kOptMulticastLoop = 12;
const int kOptMulticastIf = 13;

const jlong kV6Flag = static_cast<jlong>(1) << 40;
const jlong kHandleMask = (static_cast<jlong>(1) << 40) - 1;

NetHandle handleFd(jlong handle)
{
  return static_cast<NetHandle>(handle & kHandleMask);
}

bool handleV6(jlong handle)
{
  return (handle & kV6Flag) != 0;
}

#ifdef PLATFORM_WINDOWS
bool gWsaStarted = false;

void ensureStarted()
{
  if (!gWsaStarted) {
    WSADATA data;
    if (WSAStartup(MAKEWORD(2, 2), &data) == 0) {
      gWsaStarted = true;
    }
  }
}
#else
void ensureStarted()
{
  static bool ignored = false;
  if (!ignored) {
    signal(SIGPIPE, SIG_IGN);
    ignored = true;
  }
}
#endif

const char* errorText(int err)
{
#ifdef PLATFORM_WINDOWS
  static thread_local char buffer[128];
  snprintf(buffer, sizeof(buffer), "Socket error %d", err);
  return buffer;
#else
  return strerror(err);
#endif
}

void throwForError(JNIEnv* e, int err, const char* what)
{
  const char* cls = "java/net/SocketException";
  const char* message = 0;
  if (err == NET_ECONNREFUSED) {
    cls = "java/net/ConnectException";
    message = "Connection refused";
  } else if (err == NET_ETIMEDOUT) {
    cls = "java/net/ConnectException";
    message = "Connection timed out";
  } else if (err == NET_ENETUNREACH || err == NET_EHOSTUNREACH) {
    cls = "java/net/NoRouteToHostException";
    message = err == NET_ENETUNREACH ? "Network is unreachable" : "No route to host";
  } else if (err == NET_EADDRINUSE) {
    cls = "java/net/BindException";
    message = "Address already in use";
  } else if (err == NET_EADDRNOTAVAIL) {
    cls = "java/net/BindException";
    message = "Cannot assign requested address";
  } else if (err == NET_EACCES) {
    cls = "java/net/BindException";
    message = "Permission denied";
  } else if (err == NET_ECONNRESET) {
    message = "Connection reset";
  } else if (err == NET_EPIPE) {
    message = "Broken pipe";
  } else if (err == NET_ENOTSOCK || err == NET_ENOTCONN) {
    message = err == NET_ENOTCONN ? "Socket is not connected" : "Socket closed";
  }
  if (message == 0) {
    message = errorText(err);
  }
  (void)what;
  throwNew(e, cls, "%s", message);
}

bool waitFor(NetHandle fd, bool forWrite, int timeoutMs, int* err)
{
#ifdef PLATFORM_WINDOWS
  WSAPOLLFD p;
  p.fd = fd;
  p.events = forWrite ? POLLWRNORM : POLLRDNORM;
  p.revents = 0;
  for (;;) {
    int r = WSAPoll(&p, 1, timeoutMs);
    if (r < 0) {
      *err = NET_ERRNO();
      return false;
    }
    return r > 0;
  }
#else
  struct pollfd p;
  p.fd = fd;
  p.events = forWrite ? POLLOUT : POLLIN;
  p.revents = 0;
  for (;;) {
    int r = poll(&p, 1, timeoutMs);
    if (r < 0) {
      if (errno == EINTR) {
        continue;
      }
      *err = errno;
      return false;
    }
    return r > 0;
  }
#endif
}

NetLen makeAddress(JNIEnv* e, jbyteArray addr, int port, bool v6, struct sockaddr_storage* ss)
{
  memset(ss, 0, sizeof(*ss));
  jbyte bytes[16];
  int len = 0;
  if (addr != 0) {
    len = e->GetArrayLength(addr);
    if (len != 4 && len != 16) {
      throwNew(e, "java/net/SocketException", "Invalid address length");
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
    bool mapped = true;
    for (int i = 0; i < 10; i++) {
      mapped = mapped && b[i] == 0;
    }
    if (!mapped || b[10] != 0xff || b[11] != 0xff) {
      throwNew(e, "java/net/SocketException", "Protocol family unavailable");
      return 0;
    }
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
      jbyteArray result = e->NewByteArray(4);
      e->SetByteArrayRegion(result, 0, 4, reinterpret_cast<const jbyte*>(b + 12));
      return result;
    }
    jbyteArray result = e->NewByteArray(16);
    e->SetByteArrayRegion(result, 0, 16, reinterpret_cast<const jbyte*>(b));
    return result;
  }
  const struct sockaddr_in* a4 = reinterpret_cast<const struct sockaddr_in*>(ss);
  if (port) {
    *port = ntohs(a4->sin_port);
  }
  jbyteArray result = e->NewByteArray(4);
  e->SetByteArrayRegion(result, 0, 4, reinterpret_cast<const jbyte*>(&a4->sin_addr));
  return result;
}

void setBlocking(NetHandle fd, bool blocking)
{
#ifdef PLATFORM_WINDOWS
  u_long mode = blocking ? 0 : 1;
  ioctlsocket(fd, FIONBIO, &mode);
#else
  int flags = fcntl(fd, F_GETFL, 0);
  if (flags >= 0) {
    fcntl(fd, F_SETFL, blocking ? (flags & ~O_NONBLOCK) : (flags | O_NONBLOCK));
  }
#endif
}

}  // namespace

extern "C" JNIEXPORT void JNICALL Java_shim_NetNative_init(JNIEnv*, jclass)
{
  ensureStarted();
}

extern "C" JNIEXPORT jlong JNICALL Java_shim_NetNative_socket(JNIEnv* e, jclass, jboolean preferV6, jboolean datagram)
{
  ensureStarted();
  int type = datagram ? SOCK_DGRAM : SOCK_STREAM;
  bool v6 = false;
  NetHandle fd = NET_INVALID;
  if (preferV6) {
    fd = ::socket(AF_INET6, type, 0);
    if (fd != NET_INVALID) {
      NetOptVal off = 0;
      setsockopt(fd, IPPROTO_IPV6, IPV6_V6ONLY, reinterpret_cast<const char*>(&off), sizeof(off));
      v6 = true;
    }
  }
  if (fd == NET_INVALID) {
    fd = ::socket(AF_INET, type, 0);
  }
  if (fd == NET_INVALID) {
    throwForError(e, NET_ERRNO(), "socket");
    return -1;
  }
  if (datagram) {
    NetOptVal on = 1;
    setsockopt(fd, SOL_SOCKET, SO_BROADCAST, reinterpret_cast<const char*>(&on), sizeof(on));
  }
  return static_cast<jlong>(fd) | (v6 ? kV6Flag : 0);
}

extern "C" JNIEXPORT void JNICALL Java_shim_NetNative_bind(JNIEnv* e, jclass, jlong handle, jbyteArray addr, jint port)
{
  struct sockaddr_storage ss;
  NetLen len = makeAddress(e, addr, port, handleV6(handle), &ss);
  if (len == 0) {
    return;
  }
  if (::bind(handleFd(handle), reinterpret_cast<struct sockaddr*>(&ss), len) != 0) {
    throwForError(e, NET_ERRNO(), "bind");
  }
}

extern "C" JNIEXPORT void JNICALL Java_shim_NetNative_listen(JNIEnv* e, jclass, jlong handle, jint backlog)
{
  if (::listen(handleFd(handle), backlog) != 0) {
    throwForError(e, NET_ERRNO(), "listen");
  }
}

extern "C" JNIEXPORT jlong JNICALL Java_shim_NetNative_accept(JNIEnv* e, jclass, jlong handle, jint timeoutMs,
                                                              jobjectArray peerAddr, jintArray peerPort)
{
  NetHandle fd = handleFd(handle);
  for (;;) {
    if (timeoutMs > 0) {
      int err = 0;
      bool ready = waitFor(fd, false, timeoutMs, &err);
      if (err != 0) {
        throwForError(e, err, "accept");
        return -1;
      }
      if (!ready) {
        throwNew(e, "java/net/SocketTimeoutException", "Accept timed out");
        return -1;
      }
    }
    struct sockaddr_storage ss;
    NetLen len = sizeof(ss);
    NetHandle client = ::accept(fd, reinterpret_cast<struct sockaddr*>(&ss), &len);
    if (client == NET_INVALID) {
      int err = NET_ERRNO();
      if (err == NET_EINTR) {
        continue;
      }
      throwForError(e, err, "accept");
      return -1;
    }
    int port = 0;
    jbyteArray bytes = addressBytes(e, &ss, &port);
    if (peerAddr != 0) {
      e->SetObjectArrayElement(peerAddr, 0, bytes);
    }
    if (peerPort != 0) {
      jint p = port;
      e->SetIntArrayRegion(peerPort, 0, 1, &p);
    }
    return static_cast<jlong>(client) | (handleV6(handle) ? kV6Flag : 0);
  }
}

extern "C" JNIEXPORT void JNICALL Java_shim_NetNative_connect(JNIEnv* e, jclass, jlong handle, jbyteArray addr, jint port,
                                                              jint timeoutMs)
{
  NetHandle fd = handleFd(handle);
  struct sockaddr_storage ss;
  NetLen len = makeAddress(e, addr, port, handleV6(handle), &ss);
  if (len == 0) {
    return;
  }
  if (timeoutMs > 0) {
    setBlocking(fd, false);
    int r = ::connect(fd, reinterpret_cast<struct sockaddr*>(&ss), len);
    if (r != 0) {
      int err = NET_ERRNO();
      if (err != NET_EINPROGRESS && err != NET_EWOULDBLOCK) {
        setBlocking(fd, true);
        throwForError(e, err, "connect");
        return;
      }
      int waitErr = 0;
      bool ready = waitFor(fd, true, timeoutMs, &waitErr);
      if (!ready) {
        setBlocking(fd, true);
        if (waitErr != 0) {
          throwForError(e, waitErr, "connect");
        } else {
          throwNew(e, "java/net/SocketTimeoutException", "Connect timed out");
        }
        return;
      }
      NetOptVal soError = 0;
      NetLen sl = sizeof(soError);
      getsockopt(fd, SOL_SOCKET, SO_ERROR, reinterpret_cast<char*>(&soError), &sl);
      if (soError != 0) {
        setBlocking(fd, true);
        throwForError(e, soError, "connect");
        return;
      }
    }
    setBlocking(fd, true);
    return;
  }
  for (;;) {
    if (::connect(fd, reinterpret_cast<struct sockaddr*>(&ss), len) == 0) {
      return;
    }
    int err = NET_ERRNO();
    if (err == NET_EINTR) {
      continue;
    }
    throwForError(e, err, "connect");
    return;
  }
}

extern "C" JNIEXPORT jint JNICALL Java_shim_NetNative_read(JNIEnv* e, jclass, jlong handle, jbyteArray buffer, jint off,
                                                           jint len, jint timeoutMs)
{
  NetHandle fd = handleFd(handle);
  if (len <= 0) {
    return 0;
  }
  if (timeoutMs > 0) {
    int err = 0;
    bool ready = waitFor(fd, false, timeoutMs, &err);
    if (err != 0) {
      throwForError(e, err, "read");
      return -1;
    }
    if (!ready) {
      throwNew(e, "java/net/SocketTimeoutException", "Read timed out");
      return -1;
    }
  }
  char* tmp = static_cast<char*>(malloc(len));
  if (tmp == 0) {
    throwNew(e, "java/lang/OutOfMemoryError", "native buffer");
    return -1;
  }
  int n;
  for (;;) {
    n = static_cast<int>(::recv(fd, tmp, len, 0));
    if (n < 0 && NET_ERRNO() == NET_EINTR) {
      continue;
    }
    break;
  }
  if (n < 0) {
    int err = NET_ERRNO();
    free(tmp);
    throwForError(e, err, "read");
    return -1;
  }
  if (n > 0) {
    e->SetByteArrayRegion(buffer, off, n, reinterpret_cast<jbyte*>(tmp));
  }
  free(tmp);
  return n == 0 ? -1 : n;
}

extern "C" JNIEXPORT void JNICALL Java_shim_NetNative_write(JNIEnv* e, jclass, jlong handle, jbyteArray buffer, jint off,
                                                            jint len)
{
  NetHandle fd = handleFd(handle);
  if (len <= 0) {
    return;
  }
  char* tmp = static_cast<char*>(malloc(len));
  if (tmp == 0) {
    throwNew(e, "java/lang/OutOfMemoryError", "native buffer");
    return;
  }
  e->GetByteArrayRegion(buffer, off, len, reinterpret_cast<jbyte*>(tmp));
  int sent = 0;
  while (sent < len) {
#ifdef MSG_NOSIGNAL
    int flags = MSG_NOSIGNAL;
#else
    int flags = 0;
#endif
    int n = static_cast<int>(::send(fd, tmp + sent, len - sent, flags));
    if (n < 0) {
      int err = NET_ERRNO();
      if (err == NET_EINTR) {
        continue;
      }
      free(tmp);
      throwForError(e, err, "write");
      return;
    }
    sent += n;
  }
  free(tmp);
}

extern "C" JNIEXPORT jint JNICALL Java_shim_NetNative_sendTo(JNIEnv* e, jclass, jlong handle, jbyteArray buffer,
                                                             jint off, jint len, jbyteArray addr, jint port)
{
  NetHandle fd = handleFd(handle);
  char* tmp = static_cast<char*>(malloc(len > 0 ? len : 1));
  if (tmp == 0) {
    throwNew(e, "java/lang/OutOfMemoryError", "native buffer");
    return 0;
  }
  if (len > 0) {
    e->GetByteArrayRegion(buffer, off, len, reinterpret_cast<jbyte*>(tmp));
  }
  int n;
  if (addr != 0) {
    struct sockaddr_storage ss;
    NetLen alen = makeAddress(e, addr, port, handleV6(handle), &ss);
    if (alen == 0) {
      free(tmp);
      return 0;
    }
    n = static_cast<int>(::sendto(fd, tmp, len, 0, reinterpret_cast<struct sockaddr*>(&ss), alen));
  } else {
    n = static_cast<int>(::send(fd, tmp, len, 0));
  }
  int err = NET_ERRNO();
  free(tmp);
  if (n < 0) {
    if (err == NET_ECONNREFUSED) {
      throwNew(e, "java/net/PortUnreachableException", "ICMP Port Unreachable");
    } else {
      throwForError(e, err, "send");
    }
    return 0;
  }
  return n;
}

extern "C" JNIEXPORT jint JNICALL Java_shim_NetNative_recvFrom(JNIEnv* e, jclass, jlong handle, jbyteArray buffer,
                                                               jint off, jint len, jint timeoutMs, jobjectArray srcAddr,
                                                               jintArray srcPort, jboolean peek)
{
  NetHandle fd = handleFd(handle);
  if (timeoutMs > 0) {
    int err = 0;
    bool ready = waitFor(fd, false, timeoutMs, &err);
    if (err != 0) {
      throwForError(e, err, "receive");
      return -1;
    }
    if (!ready) {
      throwNew(e, "java/net/SocketTimeoutException", "Receive timed out");
      return -1;
    }
  }
  int cap = len > 0 ? len : 1;
  char* tmp = static_cast<char*>(malloc(cap));
  if (tmp == 0) {
    throwNew(e, "java/lang/OutOfMemoryError", "native buffer");
    return -1;
  }
  struct sockaddr_storage ss;
  int n;
  for (;;) {
    NetLen slen = sizeof(ss);
    memset(&ss, 0, sizeof(ss));
    n = static_cast<int>(::recvfrom(fd, tmp, len, peek ? MSG_PEEK : 0, reinterpret_cast<struct sockaddr*>(&ss), &slen));
    if (n < 0 && NET_ERRNO() == NET_EINTR) {
      continue;
    }
    break;
  }
  if (n < 0) {
#ifdef PLATFORM_WINDOWS
    if (NET_ERRNO() == WSAEMSGSIZE) {
      n = len;
    } else
#endif
    {
      int err = NET_ERRNO();
      free(tmp);
      if (err == NET_ECONNREFUSED) {
        throwNew(e, "java/net/PortUnreachableException", "ICMP Port Unreachable");
      } else {
        throwForError(e, err, "receive");
      }
      return -1;
    }
  }
  if (n > 0) {
    e->SetByteArrayRegion(buffer, off, n, reinterpret_cast<jbyte*>(tmp));
  }
  free(tmp);
  int port = 0;
  if (ss.ss_family == AF_INET || ss.ss_family == AF_INET6) {
    jbyteArray bytes = addressBytes(e, &ss, &port);
    if (srcAddr != 0) {
      e->SetObjectArrayElement(srcAddr, 0, bytes);
    }
    if (srcPort != 0) {
      jint p = port;
      e->SetIntArrayRegion(srcPort, 0, 1, &p);
    }
  }
  return n;
}

extern "C" JNIEXPORT jint JNICALL Java_shim_NetNative_available(JNIEnv*, jclass, jlong handle)
{
#ifdef PLATFORM_WINDOWS
  u_long n = 0;
  if (ioctlsocket(handleFd(handle), FIONREAD, &n) != 0) {
    return 0;
  }
  return static_cast<jint>(n);
#else
  int n = 0;
  if (ioctl(handleFd(handle), FIONREAD, &n) != 0) {
    return 0;
  }
  return n;
#endif
}

extern "C" JNIEXPORT void JNICALL Java_shim_NetNative_sendUrgent(JNIEnv* e, jclass, jlong handle, jint data)
{
  char byte = static_cast<char>(data);
  if (::send(handleFd(handle), &byte, 1, MSG_OOB) < 0) {
    throwForError(e, NET_ERRNO(), "send");
  }
}

extern "C" JNIEXPORT void JNICALL Java_shim_NetNative_shutdown(JNIEnv*, jclass, jlong handle, jint how)
{
#ifdef PLATFORM_WINDOWS
  int mode = how == 0 ? SD_RECEIVE : how == 1 ? SD_SEND : SD_BOTH;
#else
  int mode = how == 0 ? SHUT_RD : how == 1 ? SHUT_WR : SHUT_RDWR;
#endif
  ::shutdown(handleFd(handle), mode);
}

extern "C" JNIEXPORT void JNICALL Java_shim_NetNative_close(JNIEnv*, jclass, jlong handle)
{
  NET_CLOSE(handleFd(handle));
}

extern "C" JNIEXPORT jbyteArray JNICALL Java_shim_NetNative_sockName(JNIEnv* e, jclass, jlong handle, jboolean peer,
                                                                     jintArray portOut)
{
  struct sockaddr_storage ss;
  NetLen len = sizeof(ss);
  memset(&ss, 0, sizeof(ss));
  int r = peer ? getpeername(handleFd(handle), reinterpret_cast<struct sockaddr*>(&ss), &len)
               : getsockname(handleFd(handle), reinterpret_cast<struct sockaddr*>(&ss), &len);
  if (r != 0) {
    return 0;
  }
  int port = 0;
  jbyteArray bytes = addressBytes(e, &ss, &port);
  if (portOut != 0) {
    jint p = port;
    e->SetIntArrayRegion(portOut, 0, 1, &p);
  }
  return bytes;
}

extern "C" JNIEXPORT void JNICALL Java_shim_NetNative_setOption(JNIEnv* e, jclass, jlong handle, jint option, jint value)
{
  NetHandle fd = handleFd(handle);
  int level = SOL_SOCKET;
  int name = 0;
  NetOptVal v = value;
  switch (option) {
  case kOptReuseAddr: name = SO_REUSEADDR; break;
  case kOptKeepAlive: name = SO_KEEPALIVE; break;
  case kOptTcpNoDelay: level = IPPROTO_TCP; name = TCP_NODELAY; break;
  case kOptRcvBuf: name = SO_RCVBUF; break;
  case kOptSndBuf: name = SO_SNDBUF; break;
  case kOptBroadcast: name = SO_BROADCAST; break;
  case kOptOobInline: name = SO_OOBINLINE; break;
  case kOptReusePort:
#ifdef SO_REUSEPORT
    name = SO_REUSEPORT;
    break;
#else
    return;
#endif
  case kOptTos:
    if (handleV6(handle)) {
      level = IPPROTO_IPV6;
      name = IPV6_TCLASS;
      setsockopt(fd, IPPROTO_IP, IP_TOS, reinterpret_cast<const char*>(&v), sizeof(v));
    } else {
      level = IPPROTO_IP;
      name = IP_TOS;
    }
    break;
  case kOptMulticastTtl:
    level = handleV6(handle) ? IPPROTO_IPV6 : IPPROTO_IP;
    name = handleV6(handle) ? IPV6_MULTICAST_HOPS : IP_MULTICAST_TTL;
    break;
  case kOptMulticastLoop:
    level = handleV6(handle) ? IPPROTO_IPV6 : IPPROTO_IP;
    name = handleV6(handle) ? IPV6_MULTICAST_LOOP : IP_MULTICAST_LOOP;
    break;
  case kOptMulticastIf:
    if (handleV6(handle)) {
      level = IPPROTO_IPV6;
      name = IPV6_MULTICAST_IF;
    } else {
#ifdef PLATFORM_WINDOWS
      return;
#else
      struct ip_mreqn m;
      memset(&m, 0, sizeof(m));
      m.imr_ifindex = value;
      if (setsockopt(fd, IPPROTO_IP, IP_MULTICAST_IF, &m, sizeof(m)) != 0) {
        throwForError(e, NET_ERRNO(), "setsockopt");
      }
      return;
#endif
    }
    break;
  case kOptLinger: {
    struct linger l;
    l.l_onoff = value >= 0 ? 1 : 0;
    l.l_linger = value >= 0 ? value : 0;
    if (setsockopt(fd, SOL_SOCKET, SO_LINGER, reinterpret_cast<const char*>(&l), sizeof(l)) != 0) {
      throwForError(e, NET_ERRNO(), "setsockopt");
    }
    return;
  }
  default:
    return;
  }
  if (setsockopt(fd, level, name, reinterpret_cast<const char*>(&v), sizeof(v)) != 0) {
    throwForError(e, NET_ERRNO(), "setsockopt");
  }
}

extern "C" JNIEXPORT jint JNICALL Java_shim_NetNative_getOption(JNIEnv* e, jclass, jlong handle, jint option)
{
  NetHandle fd = handleFd(handle);
  int level = SOL_SOCKET;
  int name = 0;
  switch (option) {
  case kOptReuseAddr: name = SO_REUSEADDR; break;
  case kOptKeepAlive: name = SO_KEEPALIVE; break;
  case kOptTcpNoDelay: level = IPPROTO_TCP; name = TCP_NODELAY; break;
  case kOptRcvBuf: name = SO_RCVBUF; break;
  case kOptSndBuf: name = SO_SNDBUF; break;
  case kOptBroadcast: name = SO_BROADCAST; break;
  case kOptOobInline: name = SO_OOBINLINE; break;
  case kOptReusePort:
#ifdef SO_REUSEPORT
    name = SO_REUSEPORT;
    break;
#else
    return 0;
#endif
  case kOptTos:
    level = handleV6(handle) ? IPPROTO_IPV6 : IPPROTO_IP;
    name = handleV6(handle) ? IPV6_TCLASS : IP_TOS;
    break;
  case kOptMulticastTtl:
    level = handleV6(handle) ? IPPROTO_IPV6 : IPPROTO_IP;
    name = handleV6(handle) ? IPV6_MULTICAST_HOPS : IP_MULTICAST_TTL;
    break;
  case kOptMulticastLoop:
    level = handleV6(handle) ? IPPROTO_IPV6 : IPPROTO_IP;
    name = handleV6(handle) ? IPV6_MULTICAST_LOOP : IP_MULTICAST_LOOP;
    break;
  case kOptMulticastIf:
    if (handleV6(handle)) {
      level = IPPROTO_IPV6;
      name = IPV6_MULTICAST_IF;
      break;
    }
    return 0;
  case kOptLinger: {
    struct linger l;
    NetLen ll = sizeof(l);
    if (getsockopt(fd, SOL_SOCKET, SO_LINGER, reinterpret_cast<char*>(&l), &ll) != 0) {
      throwForError(e, NET_ERRNO(), "getsockopt");
      return -1;
    }
    return l.l_onoff ? l.l_linger : -1;
  }
  default:
    return 0;
  }
  NetOptVal v = 0;
  NetLen vl = sizeof(v);
  if (getsockopt(fd, level, name, reinterpret_cast<char*>(&v), &vl) != 0) {
    throwForError(e, NET_ERRNO(), "getsockopt");
    return 0;
  }
  return v;
}

extern "C" JNIEXPORT void JNICALL Java_shim_NetNative_membership(JNIEnv* e, jclass, jlong handle, jbyteArray group,
                                                                 jint ifIndex, jboolean join)
{
  NetHandle fd = handleFd(handle);
  jbyte bytes[16];
  int len = e->GetArrayLength(group);
  e->GetByteArrayRegion(group, 0, len, bytes);
  if (len == 4) {
#ifdef PLATFORM_WINDOWS
    struct ip_mreq m;
    memcpy(&m.imr_multiaddr, bytes, 4);
    m.imr_interface.s_addr = htonl(INADDR_ANY);
    int r = setsockopt(fd, IPPROTO_IP, join ? IP_ADD_MEMBERSHIP : IP_DROP_MEMBERSHIP, reinterpret_cast<const char*>(&m),
                       sizeof(m));
#else
    struct ip_mreqn m;
    memset(&m, 0, sizeof(m));
    memcpy(&m.imr_multiaddr, bytes, 4);
    m.imr_ifindex = ifIndex;
    int r = setsockopt(fd, IPPROTO_IP, join ? IP_ADD_MEMBERSHIP : IP_DROP_MEMBERSHIP, &m, sizeof(m));
#endif
    if (r != 0) {
      throwForError(e, NET_ERRNO(), "membership");
    }
  } else {
    struct ipv6_mreq m;
    memset(&m, 0, sizeof(m));
    memcpy(&m.ipv6mr_multiaddr, bytes, 16);
    m.ipv6mr_interface = ifIndex;
    if (setsockopt(fd, IPPROTO_IPV6, join ? IPV6_JOIN_GROUP : IPV6_LEAVE_GROUP, reinterpret_cast<const char*>(&m),
                   sizeof(m)) != 0) {
      throwForError(e, NET_ERRNO(), "membership");
    }
  }
}

extern "C" JNIEXPORT jobjectArray JNICALL Java_shim_NetNative_lookup(JNIEnv* e, jclass, jstring host)
{
  ensureStarted();
  const char* name = e->GetStringUTFChars(host, 0);
  if (name == 0) {
    return 0;
  }
  struct addrinfo hints;
  memset(&hints, 0, sizeof(hints));
  hints.ai_family = AF_UNSPEC;
  hints.ai_socktype = SOCK_STREAM;
  struct addrinfo* result = 0;
  int rc = getaddrinfo(name, 0, &hints, &result);
  e->ReleaseStringUTFChars(host, name);
  if (rc != 0 || result == 0) {
    return 0;
  }
  jbyte seen[64][16];
  int seenLen[64];
  int count = 0;
  for (int pass = 0; pass < 2; pass++) {
    for (struct addrinfo* a = result; a != 0 && count < 64; a = a->ai_next) {
      int family = pass == 0 ? AF_INET : AF_INET6;
      if (a->ai_family != family) {
        continue;
      }
      const unsigned char* raw;
      int rawLen;
      if (family == AF_INET) {
        raw = reinterpret_cast<const unsigned char*>(&reinterpret_cast<struct sockaddr_in*>(a->ai_addr)->sin_addr);
        rawLen = 4;
      } else {
        raw = reinterpret_cast<const unsigned char*>(&reinterpret_cast<struct sockaddr_in6*>(a->ai_addr)->sin6_addr);
        rawLen = 16;
      }
      bool dup = false;
      for (int i = 0; i < count; i++) {
        if (seenLen[i] == rawLen && memcmp(seen[i], raw, rawLen) == 0) {
          dup = true;
        }
      }
      if (!dup) {
        memcpy(seen[count], raw, rawLen);
        seenLen[count] = rawLen;
        count++;
      }
    }
  }
  freeaddrinfo(result);
  jclass byteArrayClass = e->FindClass("[B");
  jobjectArray out = e->NewObjectArray(count, byteArrayClass, 0);
  for (int i = 0; i < count; i++) {
    jbyteArray b = e->NewByteArray(seenLen[i]);
    e->SetByteArrayRegion(b, 0, seenLen[i], seen[i]);
    e->SetObjectArrayElement(out, i, b);
    e->DeleteLocalRef(b);
  }
  return out;
}

extern "C" JNIEXPORT jstring JNICALL Java_shim_NetNative_reverse(JNIEnv* e, jclass, jbyteArray addr)
{
  ensureStarted();
  struct sockaddr_storage ss;
  NetLen len = makeAddress(e, addr, 0, e->GetArrayLength(addr) == 16, &ss);
  if (len == 0) {
    e->ExceptionClear();
    return 0;
  }
  char host[1025];
  if (getnameinfo(reinterpret_cast<struct sockaddr*>(&ss), len, host, sizeof(host), 0, 0, NI_NAMEREQD) != 0) {
    return 0;
  }
  return e->NewStringUTF(host);
}

extern "C" JNIEXPORT jstring JNICALL Java_shim_NetNative_localHostName(JNIEnv* e, jclass)
{
  ensureStarted();
  char buffer[256];
  if (gethostname(buffer, sizeof(buffer)) != 0) {
    return 0;
  }
  buffer[sizeof(buffer) - 1] = 0;
  return e->NewStringUTF(buffer);
}

extern "C" JNIEXPORT jobjectArray JNICALL Java_shim_NetNative_interfaces(JNIEnv* e, jclass)
{
  jclass objectClass = e->FindClass("java/lang/Object");
#ifdef PLATFORM_WINDOWS
  return e->NewObjectArray(0, objectClass, 0);
#else
  struct ifaddrs* list = 0;
  if (getifaddrs(&list) != 0) {
    return e->NewObjectArray(0, objectClass, 0);
  }
  const int kMax = 128;
  const char* names[kMax];
  int flagsOf[kMax];
  int indexOf[kMax];
  int ifCount = 0;
  for (struct ifaddrs* a = list; a != 0; a = a->ifa_next) {
    bool known = false;
    for (int i = 0; i < ifCount; i++) {
      if (strcmp(names[i], a->ifa_name) == 0) {
        known = true;
      }
    }
    if (!known && ifCount < kMax) {
      names[ifCount] = a->ifa_name;
      flagsOf[ifCount] = static_cast<int>(a->ifa_flags);
      indexOf[ifCount] = static_cast<int>(if_nametoindex(a->ifa_name));
      ifCount++;
    }
  }
  jclass stringClass = e->FindClass("java/lang/String");
  jclass byteArrayClass = e->FindClass("[B");
  jobjectArray nameArray = e->NewObjectArray(ifCount, stringClass, 0);
  jintArray info = e->NewIntArray(ifCount * 2);
  jobjectArray hw = e->NewObjectArray(ifCount, byteArrayClass, 0);
  for (int i = 0; i < ifCount; i++) {
    jstring s = e->NewStringUTF(names[i]);
    e->SetObjectArrayElement(nameArray, i, s);
    e->DeleteLocalRef(s);
    jint pair[2] = {indexOf[i], flagsOf[i]};
    e->SetIntArrayRegion(info, i * 2, 2, pair);
#ifdef __linux__
    for (struct ifaddrs* a = list; a != 0; a = a->ifa_next) {
      if (a->ifa_addr != 0 && a->ifa_addr->sa_family == AF_PACKET && strcmp(a->ifa_name, names[i]) == 0) {
        struct sockaddr_ll* ll = reinterpret_cast<struct sockaddr_ll*>(a->ifa_addr);
        if (ll->sll_halen == 6) {
          jbyteArray mac = e->NewByteArray(6);
          e->SetByteArrayRegion(mac, 0, 6, reinterpret_cast<jbyte*>(ll->sll_addr));
          e->SetObjectArrayElement(hw, i, mac);
          e->DeleteLocalRef(mac);
        }
      }
    }
#endif
  }
  int addrCount = 0;
  for (struct ifaddrs* a = list; a != 0; a = a->ifa_next) {
    if (a->ifa_addr != 0 && (a->ifa_addr->sa_family == AF_INET || a->ifa_addr->sa_family == AF_INET6)) {
      addrCount++;
    }
  }
  jobjectArray addrs = e->NewObjectArray(addrCount, byteArrayClass, 0);
  jobjectArray bcast = e->NewObjectArray(addrCount, byteArrayClass, 0);
  jintArray owner = e->NewIntArray(addrCount);
  jintArray prefix = e->NewIntArray(addrCount);
  int k = 0;
  for (struct ifaddrs* a = list; a != 0; a = a->ifa_next) {
    if (a->ifa_addr == 0 || (a->ifa_addr->sa_family != AF_INET && a->ifa_addr->sa_family != AF_INET6)) {
      continue;
    }
    int ownerIndex = 0;
    for (int i = 0; i < ifCount; i++) {
      if (strcmp(names[i], a->ifa_name) == 0) {
        ownerIndex = i;
      }
    }
    struct sockaddr_storage ss;
    memset(&ss, 0, sizeof(ss));
    memcpy(&ss, a->ifa_addr, a->ifa_addr->sa_family == AF_INET ? sizeof(struct sockaddr_in) : sizeof(struct sockaddr_in6));
    int dummy = 0;
    jbyteArray bytes = e->NewByteArray(a->ifa_addr->sa_family == AF_INET ? 4 : 16);
    if (a->ifa_addr->sa_family == AF_INET) {
      e->SetByteArrayRegion(bytes, 0, 4, reinterpret_cast<const jbyte*>(&reinterpret_cast<struct sockaddr_in*>(&ss)->sin_addr));
    } else {
      e->SetByteArrayRegion(bytes, 0, 16,
                            reinterpret_cast<const jbyte*>(&reinterpret_cast<struct sockaddr_in6*>(&ss)->sin6_addr));
    }
    (void)dummy;
    e->SetObjectArrayElement(addrs, k, bytes);
    e->DeleteLocalRef(bytes);
    int bits = 0;
    if (a->ifa_netmask != 0) {
      const unsigned char* m;
      int mlen;
      if (a->ifa_addr->sa_family == AF_INET) {
        m = reinterpret_cast<const unsigned char*>(&reinterpret_cast<struct sockaddr_in*>(a->ifa_netmask)->sin_addr);
        mlen = 4;
      } else {
        m = reinterpret_cast<const unsigned char*>(&reinterpret_cast<struct sockaddr_in6*>(a->ifa_netmask)->sin6_addr);
        mlen = 16;
      }
      for (int i = 0; i < mlen; i++) {
        for (int b = 7; b >= 0; b--) {
          if (m[i] & (1 << b)) {
            bits++;
          }
        }
      }
    }
    jint o = ownerIndex;
    jint p = bits;
    e->SetIntArrayRegion(owner, k, 1, &o);
    e->SetIntArrayRegion(prefix, k, 1, &p);
    if (a->ifa_addr->sa_family == AF_INET && (a->ifa_flags & IFF_BROADCAST) && a->ifa_broadaddr != 0) {
      jbyteArray b = e->NewByteArray(4);
      e->SetByteArrayRegion(b, 0, 4, reinterpret_cast<const jbyte*>(&reinterpret_cast<struct sockaddr_in*>(a->ifa_broadaddr)->sin_addr));
      e->SetObjectArrayElement(bcast, k, b);
      e->DeleteLocalRef(b);
    }
    k++;
  }
  freeifaddrs(list);
  jobjectArray result = e->NewObjectArray(7, objectClass, 0);
  e->SetObjectArrayElement(result, 0, nameArray);
  e->SetObjectArrayElement(result, 1, info);
  e->SetObjectArrayElement(result, 2, hw);
  e->SetObjectArrayElement(result, 3, addrs);
  e->SetObjectArrayElement(result, 4, owner);
  e->SetObjectArrayElement(result, 5, prefix);
  e->SetObjectArrayElement(result, 6, bcast);
  return result;
#endif
}

extern "C" JNIEXPORT void JNICALL Java_shim_NetNative_setBlocking(JNIEnv*, jclass, jlong handle, jboolean blocking)
{
  setBlocking(handleFd(handle), blocking);
}

extern "C" JNIEXPORT jint JNICALL Java_shim_NetNative_readNb(JNIEnv* e, jclass, jlong handle, jbyteArray buffer,
                                                             jint off, jint len)
{
  if (len <= 0) {
    return 0;
  }
  char* tmp = static_cast<char*>(malloc(len));
  if (tmp == 0) {
    throwNew(e, "java/lang/OutOfMemoryError", "native buffer");
    return -1;
  }
  int n;
  for (;;) {
    n = static_cast<int>(::recv(handleFd(handle), tmp, len, 0));
    if (n < 0 && NET_ERRNO() == NET_EINTR) {
      continue;
    }
    break;
  }
  if (n < 0) {
    int err = NET_ERRNO();
    free(tmp);
    if (err == NET_EWOULDBLOCK || err == EAGAIN) {
      return 0;
    }
    throwForError(e, err, "read");
    return -1;
  }
  if (n > 0) {
    e->SetByteArrayRegion(buffer, off, n, reinterpret_cast<jbyte*>(tmp));
  }
  free(tmp);
  return n == 0 ? -1 : n;
}

extern "C" JNIEXPORT jint JNICALL Java_shim_NetNative_writeNb(JNIEnv* e, jclass, jlong handle, jbyteArray buffer,
                                                              jint off, jint len)
{
  if (len <= 0) {
    return 0;
  }
  char* tmp = static_cast<char*>(malloc(len));
  if (tmp == 0) {
    throwNew(e, "java/lang/OutOfMemoryError", "native buffer");
    return 0;
  }
  e->GetByteArrayRegion(buffer, off, len, reinterpret_cast<jbyte*>(tmp));
#ifdef MSG_NOSIGNAL
  int flags = MSG_NOSIGNAL;
#else
  int flags = 0;
#endif
  int n;
  for (;;) {
    n = static_cast<int>(::send(handleFd(handle), tmp, len, flags));
    if (n < 0 && NET_ERRNO() == NET_EINTR) {
      continue;
    }
    break;
  }
  int err = NET_ERRNO();
  free(tmp);
  if (n < 0) {
    if (err == NET_EWOULDBLOCK || err == EAGAIN) {
      return 0;
    }
    throwForError(e, err, "write");
    return 0;
  }
  return n;
}

extern "C" JNIEXPORT jlong JNICALL Java_shim_NetNative_acceptNb(JNIEnv* e, jclass, jlong handle, jobjectArray peerAddr,
                                                                jintArray peerPort)
{
  struct sockaddr_storage ss;
  NetLen len = sizeof(ss);
  NetHandle client;
  for (;;) {
    len = sizeof(ss);
    client = ::accept(handleFd(handle), reinterpret_cast<struct sockaddr*>(&ss), &len);
    if (client == NET_INVALID && NET_ERRNO() == NET_EINTR) {
      continue;
    }
    break;
  }
  if (client == NET_INVALID) {
    int err = NET_ERRNO();
    if (err == NET_EWOULDBLOCK || err == EAGAIN) {
      return -1;
    }
    throwForError(e, err, "accept");
    return -1;
  }
  int port = 0;
  jbyteArray bytes = addressBytes(e, &ss, &port);
  if (peerAddr != 0) {
    e->SetObjectArrayElement(peerAddr, 0, bytes);
  }
  if (peerPort != 0) {
    jint p = port;
    e->SetIntArrayRegion(peerPort, 0, 1, &p);
  }
  return static_cast<jlong>(client) | (handleV6(handle) ? kV6Flag : 0);
}

extern "C" JNIEXPORT jboolean JNICALL Java_shim_NetNative_connectNb(JNIEnv* e, jclass, jlong handle, jbyteArray addr,
                                                                    jint port)
{
  struct sockaddr_storage ss;
  NetLen len = makeAddress(e, addr, port, handleV6(handle), &ss);
  if (len == 0) {
    return JNI_FALSE;
  }
  for (;;) {
    if (::connect(handleFd(handle), reinterpret_cast<struct sockaddr*>(&ss), len) == 0) {
      return JNI_TRUE;
    }
    int err = NET_ERRNO();
    if (err == NET_EINTR) {
      continue;
    }
    if (err == NET_EINPROGRESS || err == NET_EWOULDBLOCK) {
      return JNI_FALSE;
    }
    throwForError(e, err, "connect");
    return JNI_FALSE;
  }
}

extern "C" JNIEXPORT jboolean JNICALL Java_shim_NetNative_finishConnect(JNIEnv* e, jclass, jlong handle, jint timeoutMs)
{
  NetHandle fd = handleFd(handle);
  int err = 0;
  if (!waitFor(fd, true, timeoutMs, &err)) {
    if (err != 0) {
      throwForError(e, err, "connect");
    }
    return JNI_FALSE;
  }
  NetOptVal soError = 0;
  NetLen sl = sizeof(soError);
  getsockopt(fd, SOL_SOCKET, SO_ERROR, reinterpret_cast<char*>(&soError), &sl);
  if (soError != 0) {
    throwForError(e, soError, "connect");
    return JNI_FALSE;
  }
  return JNI_TRUE;
}

extern "C" JNIEXPORT jint JNICALL Java_shim_NetNative_sendToNb(JNIEnv* e, jclass, jlong handle, jbyteArray buffer,
                                                               jint off, jint len, jbyteArray addr, jint port)
{
  NetHandle fd = handleFd(handle);
  char* tmp = static_cast<char*>(malloc(len > 0 ? len : 1));
  if (tmp == 0) {
    throwNew(e, "java/lang/OutOfMemoryError", "native buffer");
    return 0;
  }
  if (len > 0) {
    e->GetByteArrayRegion(buffer, off, len, reinterpret_cast<jbyte*>(tmp));
  }
  int n;
  if (addr != 0) {
    struct sockaddr_storage ss;
    NetLen alen = makeAddress(e, addr, port, handleV6(handle), &ss);
    if (alen == 0) {
      free(tmp);
      return 0;
    }
    n = static_cast<int>(::sendto(fd, tmp, len, 0, reinterpret_cast<struct sockaddr*>(&ss), alen));
  } else {
    n = static_cast<int>(::send(fd, tmp, len, 0));
  }
  int err = NET_ERRNO();
  free(tmp);
  if (n < 0) {
    if (err == NET_EWOULDBLOCK || err == EAGAIN) {
      return -1;
    }
    throwForError(e, err, "send");
    return 0;
  }
  return n;
}

extern "C" JNIEXPORT jint JNICALL Java_shim_NetNative_recvFromNb(JNIEnv* e, jclass, jlong handle, jbyteArray buffer,
                                                                 jint off, jint len, jobjectArray srcAddr,
                                                                 jintArray srcPort)
{
  NetHandle fd = handleFd(handle);
  int cap = len > 0 ? len : 1;
  char* tmp = static_cast<char*>(malloc(cap));
  if (tmp == 0) {
    throwNew(e, "java/lang/OutOfMemoryError", "native buffer");
    return -1;
  }
  struct sockaddr_storage ss;
  NetLen slen = sizeof(ss);
  memset(&ss, 0, sizeof(ss));
  int n = static_cast<int>(::recvfrom(fd, tmp, len, 0, reinterpret_cast<struct sockaddr*>(&ss), &slen));
  if (n < 0) {
    int err = NET_ERRNO();
    free(tmp);
    if (err == NET_EWOULDBLOCK || err == EAGAIN) {
      return -1;
    }
    throwForError(e, err, "receive");
    return -1;
  }
  if (n > 0) {
    e->SetByteArrayRegion(buffer, off, n, reinterpret_cast<jbyte*>(tmp));
  }
  free(tmp);
  int port = 0;
  if (ss.ss_family == AF_INET || ss.ss_family == AF_INET6) {
    jbyteArray bytes = addressBytes(e, &ss, &port);
    if (srcAddr != 0) {
      e->SetObjectArrayElement(srcAddr, 0, bytes);
    }
    if (srcPort != 0) {
      jint p = port;
      e->SetIntArrayRegion(srcPort, 0, 1, &p);
    }
  }
  return n;
}

extern "C" JNIEXPORT jint JNICALL Java_shim_NetNative_poll(JNIEnv* e, jclass, jlongArray handles, jintArray events,
                                                           jintArray revents, jint count, jint timeoutMs)
{
  if (count <= 0) {
    return 0;
  }
  jlong* hs = static_cast<jlong*>(malloc(sizeof(jlong) * count));
  jint* ev = static_cast<jint*>(malloc(sizeof(jint) * count));
  jint* rv = static_cast<jint*>(malloc(sizeof(jint) * count));
#ifdef PLATFORM_WINDOWS
  WSAPOLLFD* fds = static_cast<WSAPOLLFD*>(malloc(sizeof(WSAPOLLFD) * count));
#else
  struct pollfd* fds = static_cast<struct pollfd*>(malloc(sizeof(struct pollfd) * count));
#endif
  e->GetLongArrayRegion(handles, 0, count, hs);
  e->GetIntArrayRegion(events, 0, count, ev);
  for (int i = 0; i < count; i++) {
    fds[i].fd = handleFd(hs[i]);
    short mask = 0;
#ifdef PLATFORM_WINDOWS
    if (ev[i] & 1) mask |= POLLRDNORM;
    if (ev[i] & 4) mask |= POLLWRNORM;
#else
    if (ev[i] & 1) mask |= POLLIN;
    if (ev[i] & 4) mask |= POLLOUT;
#endif
    fds[i].events = mask;
    fds[i].revents = 0;
  }
  int r;
  for (;;) {
#ifdef PLATFORM_WINDOWS
    r = WSAPoll(fds, count, timeoutMs);
#else
    r = ::poll(fds, count, timeoutMs);
    if (r < 0 && errno == EINTR) {
      continue;
    }
#endif
    break;
  }
  if (r < 0) {
    int err = NET_ERRNO();
    free(hs); free(ev); free(rv); free(fds);
    throwForError(e, err, "poll");
    return 0;
  }
  for (int i = 0; i < count; i++) {
    int out = 0;
#ifdef PLATFORM_WINDOWS
    if (fds[i].revents & POLLRDNORM) out |= 1;
    if (fds[i].revents & POLLWRNORM) out |= 4;
#else
    if (fds[i].revents & POLLIN) out |= 1;
    if (fds[i].revents & POLLOUT) out |= 4;
#endif
    if (fds[i].revents & (POLLERR | POLLHUP | POLLNVAL)) out |= 8;
    rv[i] = out;
  }
  e->SetIntArrayRegion(revents, 0, count, rv);
  free(hs); free(ev); free(rv); free(fds);
  return r;
}

extern "C" JNIEXPORT void JNICALL Java_shim_NetNative_disconnect(JNIEnv* e, jclass, jlong handle)
{
  struct sockaddr_storage ss;
  memset(&ss, 0, sizeof(ss));
  ss.ss_family = AF_UNSPEC;
  if (::connect(handleFd(handle), reinterpret_cast<struct sockaddr*>(&ss), sizeof(ss)) != 0) {
    int err = NET_ERRNO();
#ifndef PLATFORM_WINDOWS
    if (err == EAFNOSUPPORT || err == EINVAL) {
      return;
    }
#endif
    throwForError(e, err, "disconnect");
  }
}

extern "C" JNIEXPORT void JNICALL Java_shim_NetNative_socketPair(JNIEnv* e, jclass, jlongArray out)
{
#ifdef PLATFORM_WINDOWS
  throwNew(e, "java/io/IOException", "Pipes are not supported on this platform");
#else
  int fds[2];
  if (socketpair(AF_UNIX, SOCK_STREAM, 0, fds) != 0) {
    throwForError(e, errno, "socketpair");
    return;
  }
  jlong pair[2] = {fds[0], fds[1]};
  e->SetLongArrayRegion(out, 0, 2, pair);
#endif
}
