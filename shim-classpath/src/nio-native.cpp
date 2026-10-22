/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

#ifndef _GNU_SOURCE
#define _GNU_SOURCE
#endif

#include "jni.h"
#include "jni-util.h"
#include "utf8-util.h"

#include <errno.h>
#include <limits.h>
#include <stdint.h>
#include <fcntl.h>
#include <sys/stat.h>
#include <sys/types.h>

#ifdef PLATFORM_WINDOWS
#include <direct.h>
#include <io.h>
#include <windows.h>
#else
#include <grp.h>
#include <pwd.h>
#include <sys/mman.h>
#include <sys/statvfs.h>
#include <sys/time.h>
#include <unistd.h>
#if defined(__linux__) || defined(__APPLE__)
#include <sys/xattr.h>
#endif
#if defined(__linux__)
#include <sys/sysmacros.h>
#include <poll.h>
#include <sys/inotify.h>
#endif
#include <dirent.h>
#endif

namespace {

using shimutf8::Utf8;
using shimutf8::fromUtf8;

jint err()
{
  return -errno;
}

#ifndef PLATFORM_WINDOWS

jint publish(JNIEnv* e, jlongArray out, const struct stat& st, jlong birthSec, jlong birthNsec)
{
  jlong v[16];
  v[0] = st.st_mode;
  v[1] = st.st_size;
  v[2] = static_cast<jlong>(st.st_dev);
  v[3] = static_cast<jlong>(st.st_ino);
  v[4] = static_cast<jlong>(st.st_nlink);
  v[5] = st.st_uid;
  v[6] = st.st_gid;
  v[7] = static_cast<jlong>(st.st_rdev);
#if defined(__APPLE__)
  v[8] = st.st_atimespec.tv_sec;
  v[9] = st.st_atimespec.tv_nsec;
  v[10] = st.st_mtimespec.tv_sec;
  v[11] = st.st_mtimespec.tv_nsec;
  v[12] = st.st_ctimespec.tv_sec;
  v[13] = st.st_ctimespec.tv_nsec;
  v[14] = st.st_birthtimespec.tv_sec;
  v[15] = st.st_birthtimespec.tv_nsec;
#else
  v[8] = st.st_atim.tv_sec;
  v[9] = st.st_atim.tv_nsec;
  v[10] = st.st_mtim.tv_sec;
  v[11] = st.st_mtim.tv_nsec;
  v[12] = st.st_ctim.tv_sec;
  v[13] = st.st_ctim.tv_nsec;
  v[14] = birthSec;
  v[15] = birthNsec;
#endif
  e->SetLongArrayRegion(out, 0, 16, v);
  return 0;
}

#if defined(__linux__) && defined(STATX_BTIME)
jint statxFill(JNIEnv* e, jlongArray out, int dirfd, const char* path, int flags)
{
  struct statx sx;
  if (statx(dirfd, path, flags, STATX_BASIC_STATS | STATX_BTIME, &sx) != 0) {
    return err();
  }
  jlong v[16];
  v[0] = sx.stx_mode;
  v[1] = static_cast<jlong>(sx.stx_size);
  v[2] = static_cast<jlong>(makedev(sx.stx_dev_major, sx.stx_dev_minor));
  v[3] = static_cast<jlong>(sx.stx_ino);
  v[4] = sx.stx_nlink;
  v[5] = sx.stx_uid;
  v[6] = sx.stx_gid;
  v[7] = static_cast<jlong>(makedev(sx.stx_rdev_major, sx.stx_rdev_minor));
  v[8] = sx.stx_atime.tv_sec;
  v[9] = sx.stx_atime.tv_nsec;
  v[10] = sx.stx_mtime.tv_sec;
  v[11] = sx.stx_mtime.tv_nsec;
  v[12] = sx.stx_ctime.tv_sec;
  v[13] = sx.stx_ctime.tv_nsec;
  if (sx.stx_mask & STATX_BTIME) {
    v[14] = sx.stx_btime.tv_sec;
    v[15] = sx.stx_btime.tv_nsec;
  } else {
    v[14] = 0;
    v[15] = -1;
  }
  e->SetLongArrayRegion(out, 0, 16, v);
  return 0;
}
#endif

#endif

}  // namespace

extern "C" JNIEXPORT jint JNICALL Java_shim_NioNative_statx(JNIEnv* e, jclass, jstring path, jlongArray out,
                                                            jboolean follow)
{
  Utf8 p(e, path);
  if (p.chars == 0) {
    return -EINVAL;
  }
#ifdef PLATFORM_WINDOWS
  struct _stat64 st;
  wchar_t wide[4096];
  int n = MultiByteToWideChar(CP_UTF8, 0, p.chars, -1, wide, 4096);
  if (n == 0 || _wstat64(wide, &st) != 0) {
    return err();
  }
  jlong v[16];
  v[0] = st.st_mode;
  v[1] = st.st_size;
  v[2] = st.st_dev;
  v[3] = 0;
  v[4] = st.st_nlink;
  v[5] = 0;
  v[6] = 0;
  v[7] = st.st_rdev;
  v[8] = st.st_atime;
  v[9] = 0;
  v[10] = st.st_mtime;
  v[11] = 0;
  v[12] = st.st_ctime;
  v[13] = 0;
  v[14] = st.st_ctime;
  v[15] = 0;
  e->SetLongArrayRegion(out, 0, 16, v);
  return 0;
#else
#if defined(__linux__) && defined(STATX_BTIME)
  jint r = statxFill(e, out, AT_FDCWD, p.chars, (follow ? 0 : AT_SYMLINK_NOFOLLOW) | AT_STATX_SYNC_AS_STAT);
  if (r != -ENOSYS) {
    return r;
  }
#endif
  struct stat st;
  int rc = follow ? stat(p.chars, &st) : lstat(p.chars, &st);
  if (rc != 0) {
    return err();
  }
  return publish(e, out, st, 0, -1);
#endif
}

extern "C" JNIEXPORT jint JNICALL Java_shim_NioNative_fstatx(JNIEnv* e, jclass, jint fd, jlongArray out)
{
#ifdef PLATFORM_WINDOWS
  return -ENOSYS;
#else
#if defined(__linux__) && defined(STATX_BTIME)
  jint r = statxFill(e, out, fd, "", AT_EMPTY_PATH | AT_STATX_SYNC_AS_STAT);
  if (r != -ENOSYS) {
    return r;
  }
#endif
  struct stat st;
  if (fstat(fd, &st) != 0) {
    return err();
  }
  return publish(e, out, st, 0, -1);
#endif
}

extern "C" JNIEXPORT jint JNICALL Java_shim_NioNative_symlink(JNIEnv* e, jclass, jstring target, jstring link)
{
#ifdef PLATFORM_WINDOWS
  return -ENOSYS;
#else
  Utf8 t(e, target);
  Utf8 l(e, link);
  if (t.chars == 0 || l.chars == 0) {
    return -EINVAL;
  }
  return symlink(t.chars, l.chars) == 0 ? 0 : err();
#endif
}

extern "C" JNIEXPORT jint JNICALL Java_shim_NioNative_readlink(JNIEnv* e, jclass, jstring path, jobjectArray out)
{
#ifdef PLATFORM_WINDOWS
  return -ENOSYS;
#else
  Utf8 p(e, path);
  if (p.chars == 0) {
    return -EINVAL;
  }
  char buffer[4097];
  ssize_t n = readlink(p.chars, buffer, 4096);
  if (n < 0) {
    return err();
  }
  buffer[n] = 0;
  e->SetObjectArrayElement(out, 0, fromUtf8(e, buffer));
  return 0;
#endif
}

extern "C" JNIEXPORT jint JNICALL Java_shim_NioNative_link(JNIEnv* e, jclass, jstring existing, jstring newLink)
{
#ifdef PLATFORM_WINDOWS
  return -ENOSYS;
#else
  Utf8 a(e, existing);
  Utf8 b(e, newLink);
  if (a.chars == 0 || b.chars == 0) {
    return -EINVAL;
  }
  return link(a.chars, b.chars) == 0 ? 0 : err();
#endif
}

extern "C" JNIEXPORT jint JNICALL Java_shim_NioNative_chown(JNIEnv* e, jclass, jstring path, jint uid, jint gid,
                                                            jboolean follow)
{
#ifdef PLATFORM_WINDOWS
  return -ENOSYS;
#else
  Utf8 p(e, path);
  if (p.chars == 0) {
    return -EINVAL;
  }
  int rc = follow ? chown(p.chars, static_cast<uid_t>(uid), static_cast<gid_t>(gid))
                  : lchown(p.chars, static_cast<uid_t>(uid), static_cast<gid_t>(gid));
  return rc == 0 ? 0 : err();
#endif
}

extern "C" JNIEXPORT jint JNICALL Java_shim_NioNative_chmod(JNIEnv* e, jclass, jstring path, jint mode)
{
  Utf8 p(e, path);
  if (p.chars == 0) {
    return -EINVAL;
  }
#ifdef PLATFORM_WINDOWS
  wchar_t wide[4096];
  if (MultiByteToWideChar(CP_UTF8, 0, p.chars, -1, wide, 4096) == 0) {
    return -EINVAL;
  }
  return _wchmod(wide, mode & 0600) == 0 ? 0 : err();
#else
  return chmod(p.chars, static_cast<mode_t>(mode)) == 0 ? 0 : err();
#endif
}

extern "C" JNIEXPORT jint JNICALL Java_shim_NioNative_utimens(JNIEnv* e, jclass, jstring path, jlong atimeSec,
                                                              jlong atimeNsec, jlong mtimeSec, jlong mtimeNsec,
                                                              jboolean follow)
{
#ifdef PLATFORM_WINDOWS
  return -ENOSYS;
#else
  Utf8 p(e, path);
  if (p.chars == 0) {
    return -EINVAL;
  }
  struct timespec ts[2];
  ts[0].tv_sec = static_cast<time_t>(atimeSec);
  ts[0].tv_nsec = atimeNsec < 0 ? UTIME_OMIT : static_cast<long>(atimeNsec);
  ts[1].tv_sec = static_cast<time_t>(mtimeSec);
  ts[1].tv_nsec = mtimeNsec < 0 ? UTIME_OMIT : static_cast<long>(mtimeNsec);
  return utimensat(AT_FDCWD, p.chars, ts, follow ? 0 : AT_SYMLINK_NOFOLLOW) == 0 ? 0 : err();
#endif
}

extern "C" JNIEXPORT jint JNICALL Java_shim_NioNative_fsinfo(JNIEnv* e, jclass, jstring path, jlongArray out)
{
#ifdef PLATFORM_WINDOWS
  return -ENOSYS;
#else
  Utf8 p(e, path);
  if (p.chars == 0) {
    return -EINVAL;
  }
  struct statvfs sv;
  if (statvfs(p.chars, &sv) != 0) {
    return err();
  }
  jlong v[5];
  v[0] = static_cast<jlong>(sv.f_frsize);
  v[1] = static_cast<jlong>(sv.f_frsize) * static_cast<jlong>(sv.f_blocks);
  v[2] = static_cast<jlong>(sv.f_frsize) * static_cast<jlong>(sv.f_bfree);
  v[3] = static_cast<jlong>(sv.f_frsize) * static_cast<jlong>(sv.f_bavail);
  v[4] = (sv.f_flag & ST_RDONLY) ? 1 : 0;
  e->SetLongArrayRegion(out, 0, 5, v);
  return 0;
#endif
}

extern "C" JNIEXPORT jstring JNICALL Java_shim_NioNative_userName(JNIEnv* e, jclass, jint uid)
{
#ifdef PLATFORM_WINDOWS
  return 0;
#else
  struct passwd pwd;
  struct passwd* result = 0;
  char buf[4096];
  if (getpwuid_r(static_cast<uid_t>(uid), &pwd, buf, sizeof(buf), &result) != 0 || result == 0) {
    return 0;
  }
  return fromUtf8(e, result->pw_name);
#endif
}

extern "C" JNIEXPORT jstring JNICALL Java_shim_NioNative_groupName(JNIEnv* e, jclass, jint gid)
{
#ifdef PLATFORM_WINDOWS
  return 0;
#else
  struct group grp;
  struct group* result = 0;
  char buf[4096];
  if (getgrgid_r(static_cast<gid_t>(gid), &grp, buf, sizeof(buf), &result) != 0 || result == 0) {
    return 0;
  }
  return fromUtf8(e, result->gr_name);
#endif
}

extern "C" JNIEXPORT jlong JNICALL Java_shim_NioNative_userId(JNIEnv* e, jclass, jstring name)
{
#ifdef PLATFORM_WINDOWS
  return -1;
#else
  Utf8 n(e, name);
  if (n.chars == 0) {
    return -1;
  }
  struct passwd pwd;
  struct passwd* result = 0;
  char buf[4096];
  if (getpwnam_r(n.chars, &pwd, buf, sizeof(buf), &result) != 0 || result == 0) {
    return -1;
  }
  return static_cast<jlong>(result->pw_uid);
#endif
}

extern "C" JNIEXPORT jlong JNICALL Java_shim_NioNative_groupId(JNIEnv* e, jclass, jstring name)
{
#ifdef PLATFORM_WINDOWS
  return -1;
#else
  Utf8 n(e, name);
  if (n.chars == 0) {
    return -1;
  }
  struct group grp;
  struct group* result = 0;
  char buf[4096];
  if (getgrnam_r(n.chars, &grp, buf, sizeof(buf), &result) != 0 || result == 0) {
    return -1;
  }
  return static_cast<jlong>(result->gr_gid);
#endif
}

extern "C" JNIEXPORT jint JNICALL Java_shim_NioNative_listxattr(JNIEnv* e, jclass, jstring path, jbyteArray buf,
                                                                jboolean follow)
{
#if defined(__linux__)
  Utf8 p(e, path);
  if (p.chars == 0) {
    return -EINVAL;
  }
  jsize cap = buf ? e->GetArrayLength(buf) : 0;
  char* tmp = static_cast<char*>(malloc(cap ? cap : 1));
  ssize_t n = follow ? listxattr(p.chars, cap ? tmp : 0, cap) : llistxattr(p.chars, cap ? tmp : 0, cap);
  if (n < 0) {
    free(tmp);
    return err();
  }
  if (cap && n <= cap) {
    e->SetByteArrayRegion(buf, 0, static_cast<jsize>(n), reinterpret_cast<jbyte*>(tmp));
  }
  free(tmp);
  return static_cast<jint>(n);
#else
  return -ENOTSUP;
#endif
}

extern "C" JNIEXPORT jint JNICALL Java_shim_NioNative_getxattr(JNIEnv* e, jclass, jstring path, jstring name,
                                                               jbyteArray buf, jint off, jint len, jboolean follow)
{
#if defined(__linux__)
  Utf8 p(e, path);
  Utf8 n(e, name);
  if (p.chars == 0 || n.chars == 0) {
    return -EINVAL;
  }
  char* tmp = static_cast<char*>(malloc(len > 0 ? len : 1));
  ssize_t r = follow ? getxattr(p.chars, n.chars, len > 0 ? tmp : 0, len) : lgetxattr(p.chars, n.chars,
                                                                                       len > 0 ? tmp : 0, len);
  if (r < 0) {
    free(tmp);
    return err();
  }
  if (len > 0 && r <= len) {
    e->SetByteArrayRegion(buf, off, static_cast<jsize>(r), reinterpret_cast<jbyte*>(tmp));
  }
  free(tmp);
  return static_cast<jint>(r);
#else
  return -ENOTSUP;
#endif
}

extern "C" JNIEXPORT jint JNICALL Java_shim_NioNative_setxattr(JNIEnv* e, jclass, jstring path, jstring name,
                                                               jbyteArray buf, jint off, jint len)
{
#if defined(__linux__)
  Utf8 p(e, path);
  Utf8 n(e, name);
  if (p.chars == 0 || n.chars == 0) {
    return -EINVAL;
  }
  char* tmp = static_cast<char*>(malloc(len > 0 ? len : 1));
  if (len > 0) {
    e->GetByteArrayRegion(buf, off, len, reinterpret_cast<jbyte*>(tmp));
  }
  int r = setxattr(p.chars, n.chars, tmp, len, 0);
  free(tmp);
  return r == 0 ? 0 : err();
#else
  return -ENOTSUP;
#endif
}

extern "C" JNIEXPORT jint JNICALL Java_shim_NioNative_removexattr(JNIEnv* e, jclass, jstring path, jstring name)
{
#if defined(__linux__)
  Utf8 p(e, path);
  Utf8 n(e, name);
  if (p.chars == 0 || n.chars == 0) {
    return -EINVAL;
  }
  return removexattr(p.chars, n.chars) == 0 ? 0 : err();
#else
  return -ENOTSUP;
#endif
}

extern "C" JNIEXPORT jlong JNICALL Java_shim_NioNative_mmap(JNIEnv*, jclass, jint fd, jint prot, jint flags,
                                                            jlong offset, jlong size)
{
#ifdef PLATFORM_WINDOWS
  return -ENOSYS;
#else
  int p = 0;
  if (prot & 1) {
    p |= PROT_READ;
  }
  if (prot & 2) {
    p |= PROT_WRITE;
  }
  void* addr = mmap(0, static_cast<size_t>(size), p, (flags == 1) ? MAP_PRIVATE : MAP_SHARED, fd,
                    static_cast<off_t>(offset));
  if (addr == MAP_FAILED) {
    return err();
  }
  return static_cast<jlong>(reinterpret_cast<uintptr_t>(addr));
#endif
}

extern "C" JNIEXPORT jint JNICALL Java_shim_NioNative_munmap(JNIEnv*, jclass, jlong address, jlong size)
{
#ifdef PLATFORM_WINDOWS
  return -ENOSYS;
#else
  return munmap(reinterpret_cast<void*>(static_cast<uintptr_t>(address)), static_cast<size_t>(size)) == 0 ? 0
                                                                                                          : err();
#endif
}

extern "C" JNIEXPORT jint JNICALL Java_shim_NioNative_msync(JNIEnv*, jclass, jlong address, jlong size)
{
#ifdef PLATFORM_WINDOWS
  return -ENOSYS;
#else
  return msync(reinterpret_cast<void*>(static_cast<uintptr_t>(address)), static_cast<size_t>(size), MS_SYNC) == 0
      ? 0
      : err();
#endif
}

extern "C" JNIEXPORT jint JNICALL Java_shim_NioNative_lock(JNIEnv*, jclass, jint fd, jlong position, jlong size,
                                                           jboolean shared, jboolean block)
{
#ifdef PLATFORM_WINDOWS
  return -ENOSYS;
#else
  struct flock fl;
  memset(&fl, 0, sizeof(fl));
  fl.l_whence = SEEK_SET;
  fl.l_start = static_cast<off_t>(position);
  fl.l_len = (size == INT64_MAX) ? 0 : static_cast<off_t>(size);
  fl.l_type = shared ? F_RDLCK : F_WRLCK;
  int rc;
  do {
    rc = fcntl(fd, block ? F_SETLKW : F_SETLK, &fl);
  } while (rc < 0 && errno == EINTR && block);
  if (rc == 0) {
    return 0;
  }
  if (errno == EAGAIN || errno == EACCES) {
    return 1;
  }
  return err();
#endif
}

extern "C" JNIEXPORT jint JNICALL Java_shim_NioNative_unlock(JNIEnv*, jclass, jint fd, jlong position, jlong size)
{
#ifdef PLATFORM_WINDOWS
  return -ENOSYS;
#else
  struct flock fl;
  memset(&fl, 0, sizeof(fl));
  fl.l_whence = SEEK_SET;
  fl.l_start = static_cast<off_t>(position);
  fl.l_len = (size == INT64_MAX) ? 0 : static_cast<off_t>(size);
  fl.l_type = F_UNLCK;
  return fcntl(fd, F_SETLK, &fl) == 0 ? 0 : err();
#endif
}

extern "C" JNIEXPORT jint JNICALL Java_shim_NioNative_errnoValue(JNIEnv*, jclass, jint which)
{
  switch (which) {
    case 0: return EPERM;
    case 1: return ENOENT;
    case 2: return EACCES;
    case 3: return EEXIST;
    case 4: return ENOTDIR;
    case 5: return EISDIR;
    case 6: return EINVAL;
    case 7: return ELOOP;
    case 8: return ENOTEMPTY;
    case 9: return EXDEV;
    case 10: return ENOSPC;
    case 11: return EROFS;
    case 12: return ENAMETOOLONG;
#ifdef ENODATA
    case 13: return ENODATA;
#elif defined(ENOATTR)
    case 13: return ENOATTR;
#endif
    case 14: return ERANGE;
    case 15: return EBUSY;
    case 16: return EMLINK;
    case 17: return ENOSYS;
    case 18: return ENOTSUP;
    case 19: return EIO;
    case 20: return EAGAIN;
    default: return -1;
  }
}

extern "C" JNIEXPORT jint JNICALL Java_shim_NioNative_realpath(JNIEnv* e, jclass, jstring path, jobjectArray out)
{
#ifdef PLATFORM_WINDOWS
  return -ENOSYS;
#else
  Utf8 p(e, path);
  if (p.chars == 0) {
    return -EINVAL;
  }
  char buffer[PATH_MAX + 1];
  if (realpath(p.chars, buffer) == 0) {
    return err();
  }
  e->SetObjectArrayElement(out, 0, fromUtf8(e, buffer));
  return 0;
#endif
}

extern "C" JNIEXPORT jobjectArray JNICALL Java_shim_NioNative_listdir(JNIEnv* e, jclass, jstring path, jintArray errOut)
{
#ifdef PLATFORM_WINDOWS
  jint code = -ENOSYS;
  e->SetIntArrayRegion(errOut, 0, 1, &code);
  return 0;
#else
  Utf8 p(e, path);
  jint code = 0;
  if (p.chars == 0) {
    code = -EINVAL;
    e->SetIntArrayRegion(errOut, 0, 1, &code);
    return 0;
  }
  jclass stringClass = e->FindClass("java/lang/String");
  jsize count = 0;
  jobjectArray result = 0;
  for (int pass = 0; pass < 2; ++pass) {
    DIR* dir = opendir(p.chars);
    if (dir == 0) {
      code = err();
      e->SetIntArrayRegion(errOut, 0, 1, &code);
      return 0;
    }
    jsize index = 0;
    while (struct dirent* entry = readdir(dir)) {
      if (strcmp(entry->d_name, ".") == 0 || strcmp(entry->d_name, "..") == 0) {
        continue;
      }
      if (pass == 1 && index < count) {
        e->SetObjectArrayElement(result, index, fromUtf8(e, entry->d_name));
      }
      ++index;
    }
    closedir(dir);
    if (pass == 0) {
      count = index;
      result = e->NewObjectArray(count, stringClass, 0);
      if (result == 0) {
        return 0;
      }
    }
  }
  e->SetIntArrayRegion(errOut, 0, 1, &code);
  return result;
#endif
}

extern "C" JNIEXPORT jint JNICALL Java_shim_NioNative_inotifyInit(JNIEnv*, jclass)
{
#if defined(__linux__)
  int fd = inotify_init1(IN_CLOEXEC);
  return fd < 0 ? err() : fd;
#else
  return -ENOSYS;
#endif
}

extern "C" JNIEXPORT jint JNICALL Java_shim_NioNative_inotifyAddWatch(JNIEnv* e, jclass, jint fd, jstring path,
                                                                      jint mask)
{
#if defined(__linux__)
  Utf8 p(e, path);
  if (p.chars == 0) {
    return -EINVAL;
  }
  int wd = inotify_add_watch(fd, p.chars, static_cast<uint32_t>(mask));
  return wd < 0 ? err() : wd;
#else
  return -ENOSYS;
#endif
}

extern "C" JNIEXPORT jint JNICALL Java_shim_NioNative_inotifyRmWatch(JNIEnv*, jclass, jint fd, jint wd)
{
#if defined(__linux__)
  return inotify_rm_watch(fd, wd) == 0 ? 0 : err();
#else
  return -ENOSYS;
#endif
}

extern "C" JNIEXPORT jint JNICALL Java_shim_NioNative_inotifyRead(JNIEnv* e, jclass, jint fd, jint wakeFd,
                                                                  jbyteArray buf)
{
#if defined(__linux__)
  struct pollfd fds[2];
  fds[0].fd = fd;
  fds[0].events = POLLIN;
  fds[1].fd = wakeFd;
  fds[1].events = POLLIN;
  int rc;
  do {
    rc = poll(fds, 2, -1);
  } while (rc < 0 && errno == EINTR);
  if (rc < 0) {
    return err();
  }
  if (fds[1].revents & POLLIN) {
    return 0;
  }
  jsize cap = e->GetArrayLength(buf);
  char* tmp = static_cast<char*>(malloc(cap));
  ssize_t n;
  do {
    n = read(fd, tmp, cap);
  } while (n < 0 && errno == EINTR);
  if (n < 0) {
    free(tmp);
    return err();
  }
  e->SetByteArrayRegion(buf, 0, static_cast<jsize>(n), reinterpret_cast<jbyte*>(tmp));
  free(tmp);
  return static_cast<jint>(n);
#else
  return -ENOSYS;
#endif
}

extern "C" JNIEXPORT jint JNICALL Java_shim_NioNative_pipe(JNIEnv* e, jclass, jintArray out)
{
#ifdef PLATFORM_WINDOWS
  return -ENOSYS;
#else
  int fds[2];
  if (pipe(fds) != 0) {
    return err();
  }
  jint v[2] = {fds[0], fds[1]};
  e->SetIntArrayRegion(out, 0, 2, v);
  return 0;
#endif
}
