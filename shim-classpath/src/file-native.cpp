/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

#include "jni.h"
#include "jni-util.h"
#include "utf8-util.h"

#include <errno.h>
#include <fcntl.h>
#include <locale.h>
#include <sys/stat.h>
#include <sys/types.h>

#ifdef PLATFORM_WINDOWS
#include <direct.h>
#include <io.h>
#include <sys/utime.h>
#include <windows.h>
typedef wchar_t FsChar;
#define FS_OPEN _wopen
#define FS_READ _read
#define FS_WRITE _write
#define FS_CLOSE _close
#define FS_LSEEK _lseeki64
#define FS_STAT _wstat64
typedef struct _stat64 FsStat;
#else
#include <dirent.h>
#include <sys/ioctl.h>
#include <sys/time.h>
#include <sys/statvfs.h>
#include <unistd.h>
#include <utime.h>
typedef char FsChar;
#define FS_OPEN open
#define FS_READ read
#define FS_WRITE write
#define FS_CLOSE close
#define FS_LSEEK lseek
#define FS_STAT stat
typedef struct stat FsStat;
#endif


namespace {

struct FsString {
  FsChar* data;
  size_t length;

  FsString() : data(0), length(0) { }
  ~FsString() { free(data); }

  const FsChar* c_str() const { return data ? data : empty(); }

  static const FsChar* empty() {
    static const FsChar zero = 0;
    return &zero;
  }

  bool assign(const FsChar* chars, size_t n) {
    free(data);
    data = static_cast<FsChar*>(malloc((n + 1) * sizeof(FsChar)));
    if (data == 0) {
      length = 0;
      return false;
    }
    memcpy(data, chars, n * sizeof(FsChar));
    data[n] = 0;
    length = n;
    return true;
  }

 private:
  FsString(const FsString&);
  FsString& operator=(const FsString&);
};

struct ByteBuffer {
  jbyte* data;

  explicit ByteBuffer(size_t n) : data(static_cast<jbyte*>(malloc(n ? n : 1))) { }
  ~ByteBuffer() { free(data); }

 private:
  ByteBuffer(const ByteBuffer&);
  ByteBuffer& operator=(const ByteBuffer&);
};

}  // namespace

namespace {

enum {
  kRead = 1,
  kWrite = 2,
  kAppend = 4,
  kCreate = 8,
  kCreateNew = 16,
  kTruncate = 32,
  kSync = 64,
  kDsync = 128,
  kNoFollow = 256,
  kAllowDir = 512
};

bool toPath(JNIEnv* e, jstring s, FsString& out)
{
  if (s == 0) {
    return false;
  }
#ifdef PLATFORM_WINDOWS
  const jchar* chars = e->GetStringChars(s, 0);
  if (chars == 0) {
    return false;
  }
  bool ok = out.assign(reinterpret_cast<const wchar_t*>(chars), e->GetStringLength(s));
  e->ReleaseStringChars(s, chars);
  return ok;
#else
  shimutf8::Utf8 chars(e, s);
  if (chars.chars == 0) {
    return false;
  }
  return out.assign(chars.chars, strlen(chars.chars));
#endif
}

jstring fromPath(JNIEnv* e, const FsString& s)
{
#ifdef PLATFORM_WINDOWS
  return e->NewString(reinterpret_cast<const jchar*>(s.c_str()), static_cast<jsize>(s.length));
#else
  return shimutf8::fromUtf8(e, s.c_str());
#endif
}

jint err()
{
  return -errno;
}

}  // namespace

extern "C" JNIEXPORT jint JNICALL
    Java_shim_FileNative_open(JNIEnv* e, jclass, jstring path, jint flags, jint mode)
{
  FsString p;
  if (!toPath(e, path, p)) {
    return -EINVAL;
  }
  int oflags = 0;
  bool read = (flags & kRead) != 0;
  bool write = (flags & (kWrite | kAppend)) != 0;
  if (read && write) {
    oflags = O_RDWR;
  } else if (write) {
    oflags = O_WRONLY;
  } else {
    oflags = O_RDONLY;
  }
  if (flags & kAppend) {
#ifdef O_APPEND
    oflags |= O_APPEND;
#endif
  }
  if (flags & kCreateNew) {
    oflags |= O_CREAT | O_EXCL;
  } else if (flags & kCreate) {
    oflags |= O_CREAT;
  }
  if (flags & kTruncate) {
    oflags |= O_TRUNC;
  }
#ifdef O_SYNC
  if (flags & kSync) {
    oflags |= O_SYNC;
  }
#endif
#ifdef O_DSYNC
  if (flags & kDsync) {
    oflags |= O_DSYNC;
  }
#endif
#ifdef O_CLOEXEC
  oflags |= O_CLOEXEC;
#endif
#ifdef O_NOFOLLOW
  if (flags & kNoFollow) {
    oflags |= O_NOFOLLOW;
  }
#endif
#ifdef PLATFORM_WINDOWS
  oflags |= _O_BINARY;
#endif
  int fd;
  do {
    fd = FS_OPEN(p.c_str(), oflags, mode);
  } while (fd < 0 && errno == EINTR);
  if (fd < 0) {
    return err();
  }
#ifndef PLATFORM_WINDOWS
  if ((flags & (kWrite | kAppend | kAllowDir)) == 0) {
    struct stat st;
    if (fstat(fd, &st) == 0 && S_ISDIR(st.st_mode)) {
      close(fd);
      return -EISDIR;
    }
  }
#endif
  return fd;
}

extern "C" JNIEXPORT jint JNICALL Java_shim_FileNative_close(JNIEnv*, jclass, jint fd)
{
  return FS_CLOSE(fd) < 0 ? err() : 0;
}

extern "C" JNIEXPORT jint JNICALL
    Java_shim_FileNative_read(JNIEnv* e, jclass, jint fd, jbyteArray b, jint off, jint len)
{
  if (len == 0) {
    return 0;
  }
  ByteBuffer buffer(len);
  int r;
  do {
    r = static_cast<int>(FS_READ(fd, buffer.data, len));
  } while (r < 0 && errno == EINTR);
  if (r < 0) {
    return err();
  }
  if (r > 0) {
    e->SetByteArrayRegion(b, off, r, buffer.data);
  }
  return r;
}

extern "C" JNIEXPORT jint JNICALL Java_shim_FileNative_readByte(JNIEnv*, jclass, jint fd)
{
  unsigned char c;
  int r;
  do {
    r = static_cast<int>(FS_READ(fd, &c, 1));
  } while (r < 0 && errno == EINTR);
  if (r < 0) {
    return -2 - errno;
  }
  return r == 0 ? -1 : c;
}

extern "C" JNIEXPORT jint JNICALL
    Java_shim_FileNative_write(JNIEnv* e, jclass, jint fd, jbyteArray b, jint off, jint len)
{
  if (len == 0) {
    return 0;
  }
  ByteBuffer buffer(len);
  e->GetByteArrayRegion(b, off, len, buffer.data);
  int done = 0;
  while (done < len) {
    int r = static_cast<int>(FS_WRITE(fd, buffer.data + done, len - done));
    if (r < 0) {
      if (errno == EINTR) {
        continue;
      }
      return err();
    }
    done += r;
  }
  return done;
}

extern "C" JNIEXPORT jint JNICALL Java_shim_FileNative_writeByte(JNIEnv*, jclass, jint fd, jint b)
{
  unsigned char c = static_cast<unsigned char>(b);
  int r;
  do {
    r = static_cast<int>(FS_WRITE(fd, &c, 1));
  } while (r < 0 && errno == EINTR);
  return r < 0 ? err() : 0;
}

extern "C" JNIEXPORT jint JNICALL
    Java_shim_FileNative_pread(JNIEnv* e, jclass, jint fd, jbyteArray b, jint off, jint len, jlong pos)
{
  if (len == 0) {
    return 0;
  }
  ByteBuffer buffer(len);
#ifdef PLATFORM_WINDOWS
  jlong saved = FS_LSEEK(fd, 0, SEEK_CUR);
  if (saved < 0 || FS_LSEEK(fd, pos, SEEK_SET) < 0) {
    return err();
  }
  int r = static_cast<int>(FS_READ(fd, buffer.data, len));
  int saveErrno = errno;
  FS_LSEEK(fd, saved, SEEK_SET);
  errno = saveErrno;
#else
  int r;
  do {
    r = static_cast<int>(pread(fd, buffer.data, len, pos));
  } while (r < 0 && errno == EINTR);
#endif
  if (r < 0) {
    return err();
  }
  if (r > 0) {
    e->SetByteArrayRegion(b, off, r, buffer.data);
  }
  return r;
}

extern "C" JNIEXPORT jint JNICALL
    Java_shim_FileNative_pwrite(JNIEnv* e, jclass, jint fd, jbyteArray b, jint off, jint len, jlong pos)
{
  if (len == 0) {
    return 0;
  }
  ByteBuffer buffer(len);
  e->GetByteArrayRegion(b, off, len, buffer.data);
#ifdef PLATFORM_WINDOWS
  jlong saved = FS_LSEEK(fd, 0, SEEK_CUR);
  if (saved < 0 || FS_LSEEK(fd, pos, SEEK_SET) < 0) {
    return err();
  }
  int r = static_cast<int>(FS_WRITE(fd, buffer.data, len));
  int saveErrno = errno;
  FS_LSEEK(fd, saved, SEEK_SET);
  errno = saveErrno;
#else
  int r;
  do {
    r = static_cast<int>(pwrite(fd, buffer.data, len, pos));
  } while (r < 0 && errno == EINTR);
#endif
  return r < 0 ? err() : r;
}

extern "C" JNIEXPORT jlong JNICALL
    Java_shim_FileNative_seek(JNIEnv*, jclass, jint fd, jlong offset, jint whence)
{
  jlong r = FS_LSEEK(fd, offset, whence);
  return r < 0 ? err() : r;
}

extern "C" JNIEXPORT jlong JNICALL Java_shim_FileNative_size(JNIEnv*, jclass, jint fd)
{
#ifdef PLATFORM_WINDOWS
  struct _stat64 st;
  if (_fstat64(fd, &st) < 0) {
    return err();
  }
  return st.st_size;
#else
  struct stat st;
  if (fstat(fd, &st) < 0) {
    return err();
  }
  return st.st_size;
#endif
}

extern "C" JNIEXPORT jint JNICALL Java_shim_FileNative_truncate(JNIEnv*, jclass, jint fd, jlong length)
{
#ifdef PLATFORM_WINDOWS
  return _chsize_s(fd, length) == 0 ? 0 : err();
#else
  return ftruncate(fd, length) == 0 ? 0 : err();
#endif
}

extern "C" JNIEXPORT jint JNICALL Java_shim_FileNative_sync(JNIEnv*, jclass, jint fd, jboolean metadata)
{
#ifdef PLATFORM_WINDOWS
  return _commit(fd) == 0 ? 0 : err();
#else
#if defined(__linux__)
  int r = metadata ? fsync(fd) : fdatasync(fd);
#else
  int r = fsync(fd);
#endif
  return r == 0 ? 0 : err();
#endif
}

extern "C" JNIEXPORT jlong JNICALL Java_shim_FileNative_available(JNIEnv*, jclass, jint fd)
{
#ifdef PLATFORM_WINDOWS
  jlong cur = _lseeki64(fd, 0, SEEK_CUR);
  jlong end = _lseeki64(fd, 0, SEEK_END);
  if (cur < 0 || end < 0) {
    return 0;
  }
  _lseeki64(fd, cur, SEEK_SET);
  return end > cur ? end - cur : 0;
#else
  struct stat st;
  if (fstat(fd, &st) < 0) {
    return err();
  }
  if (S_ISCHR(st.st_mode) || S_ISFIFO(st.st_mode) || S_ISSOCK(st.st_mode)) {
    int n = 0;
    if (ioctl(fd, FIONREAD, &n) >= 0) {
      return n;
    }
    return 0;
  }
  off_t cur = lseek(fd, 0, SEEK_CUR);
  if (cur < 0) {
    return err();
  }
  return st.st_size > cur ? st.st_size - cur : 0;
#endif
}

extern "C" JNIEXPORT jint JNICALL
    Java_shim_FileNative_stat(JNIEnv* e, jclass, jstring path, jlongArray out, jboolean follow)
{
  FsString p;
  if (!toPath(e, path, p)) {
    return -EINVAL;
  }
  FsStat st;
#ifdef PLATFORM_WINDOWS
  int r = FS_STAT(p.c_str(), &st);
#else
  int r = follow ? stat(p.c_str(), &st) : lstat(p.c_str(), &st);
#endif
  if (r < 0) {
    return err();
  }
  jlong values[4];
  values[0] = st.st_mode;
  values[1] = st.st_size;
#if defined(__APPLE__)
  values[2] = static_cast<jlong>(st.st_mtimespec.tv_sec) * 1000 + st.st_mtimespec.tv_nsec / 1000000;
#elif defined(PLATFORM_WINDOWS)
  values[2] = static_cast<jlong>(st.st_mtime) * 1000;
#else
  values[2] = static_cast<jlong>(st.st_mtim.tv_sec) * 1000 + st.st_mtim.tv_nsec / 1000000;
#endif
  values[3] = 0;
  e->SetLongArrayRegion(out, 0, 4, values);
  return 0;
}

extern "C" JNIEXPORT jint JNICALL Java_shim_FileNative_access(JNIEnv* e, jclass, jstring path, jint mode)
{
  FsString p;
  if (!toPath(e, path, p)) {
    return -EINVAL;
  }
#ifdef PLATFORM_WINDOWS
  return _waccess(p.c_str(), mode & 6) == 0 ? 0 : err();
#else
  return access(p.c_str(), mode) == 0 ? 0 : err();
#endif
}

extern "C" JNIEXPORT jint JNICALL Java_shim_FileNative_mkdir(JNIEnv* e, jclass, jstring path, jint mode)
{
  FsString p;
  if (!toPath(e, path, p)) {
    return -EINVAL;
  }
#ifdef PLATFORM_WINDOWS
  return _wmkdir(p.c_str()) == 0 ? 0 : err();
#else
  return mkdir(p.c_str(), mode) == 0 ? 0 : err();
#endif
}

extern "C" JNIEXPORT jint JNICALL Java_shim_FileNative_unlink(JNIEnv* e, jclass, jstring path)
{
  FsString p;
  if (!toPath(e, path, p)) {
    return -EINVAL;
  }
#ifdef PLATFORM_WINDOWS
  return _wunlink(p.c_str()) == 0 ? 0 : err();
#else
  return unlink(p.c_str()) == 0 ? 0 : err();
#endif
}

extern "C" JNIEXPORT jint JNICALL Java_shim_FileNative_rmdir(JNIEnv* e, jclass, jstring path)
{
  FsString p;
  if (!toPath(e, path, p)) {
    return -EINVAL;
  }
#ifdef PLATFORM_WINDOWS
  return _wrmdir(p.c_str()) == 0 ? 0 : err();
#else
  return rmdir(p.c_str()) == 0 ? 0 : err();
#endif
}

extern "C" JNIEXPORT jint JNICALL
    Java_shim_FileNative_rename(JNIEnv* e, jclass, jstring from, jstring to)
{
  FsString a;
  FsString b;
  if (!toPath(e, from, a) || !toPath(e, to, b)) {
    return -EINVAL;
  }
#ifdef PLATFORM_WINDOWS
  _wunlink(b.c_str());
  return _wrename(a.c_str(), b.c_str()) == 0 ? 0 : err();
#else
  return rename(a.c_str(), b.c_str()) == 0 ? 0 : err();
#endif
}

extern "C" JNIEXPORT jint JNICALL Java_shim_FileNative_chmod(JNIEnv* e, jclass, jstring path, jint mode)
{
  FsString p;
  if (!toPath(e, path, p)) {
    return -EINVAL;
  }
#ifdef PLATFORM_WINDOWS
  return _wchmod(p.c_str(), mode & (_S_IREAD | _S_IWRITE)) == 0 ? 0 : err();
#else
  return chmod(p.c_str(), mode) == 0 ? 0 : err();
#endif
}

extern "C" JNIEXPORT jint JNICALL
    Java_shim_FileNative_setModified(JNIEnv* e, jclass, jstring path, jlong millis)
{
  FsString p;
  if (!toPath(e, path, p)) {
    return -EINVAL;
  }
#ifdef PLATFORM_WINDOWS
  struct _utimbuf times;
  times.actime = static_cast<time_t>(millis / 1000);
  times.modtime = static_cast<time_t>(millis / 1000);
  return _wutime(p.c_str(), &times) == 0 ? 0 : err();
#else
  struct stat st;
  if (stat(p.c_str(), &st) < 0) {
    return err();
  }
  struct timeval tv[2];
  tv[0].tv_sec = st.st_atime;
  tv[0].tv_usec = 0;
  tv[1].tv_sec = static_cast<time_t>(millis / 1000);
  tv[1].tv_usec = static_cast<suseconds_t>((millis % 1000) * 1000);
  return utimes(p.c_str(), tv) == 0 ? 0 : err();
#endif
}

extern "C" JNIEXPORT jstring JNICALL Java_shim_FileNative_realpath(JNIEnv* e, jclass, jstring path)
{
  FsString p;
  if (!toPath(e, path, p)) {
    return 0;
  }
#ifdef PLATFORM_WINDOWS
  wchar_t buffer[MAX_PATH * 4];
  if (_wfullpath(buffer, p.c_str(), MAX_PATH * 4) == 0) {
    return 0;
  }
  return e->NewString(reinterpret_cast<const jchar*>(buffer), static_cast<jsize>(wcslen(buffer)));
#else
  char buffer[PATH_MAX + 1];
  if (realpath(p.c_str(), buffer) == 0) {
    return 0;
  }
  return shimutf8::fromUtf8(e, buffer);
#endif
}

#ifdef PLATFORM_WINDOWS
static bool isDotEntry(const wchar_t* n)
{
  return wcscmp(n, L".") == 0 || wcscmp(n, L"..") == 0;
}
#else
static bool isDotEntry(const char* n)
{
  return strcmp(n, ".") == 0 || strcmp(n, "..") == 0;
}
#endif

extern "C" JNIEXPORT jobjectArray JNICALL Java_shim_FileNative_list(JNIEnv* e, jclass, jstring path)
{
  FsString p;
  if (!toPath(e, path, p)) {
    return 0;
  }
  jclass stringClass = e->FindClass("java/lang/String");
  jsize count = 0;
  jobjectArray result = 0;
  for (int pass = 0; pass < 2; ++pass) {
    jsize index = 0;
#ifdef PLATFORM_WINDOWS
    WIN32_FIND_DATAW data;
    FsString pattern;
    size_t n = p.length;
    wchar_t* buffer = static_cast<wchar_t*>(malloc((n + 3) * sizeof(wchar_t)));
    if (buffer == 0) {
      return 0;
    }
    memcpy(buffer, p.c_str(), n * sizeof(wchar_t));
    buffer[n] = L'\\';
    buffer[n + 1] = L'*';
    buffer[n + 2] = 0;
    HANDLE h = FindFirstFileW(buffer, &data);
    free(buffer);
    if (h == INVALID_HANDLE_VALUE) {
      return 0;
    }
    do {
      if (!isDotEntry(data.cFileName)) {
        if (pass == 1 && index < count) {
          e->SetObjectArrayElement(result, index, e->NewString(reinterpret_cast<const jchar*>(data.cFileName),
                                                                static_cast<jsize>(wcslen(data.cFileName))));
        }
        ++index;
      }
    } while (FindNextFileW(h, &data));
    FindClose(h);
#else
    DIR* dir = opendir(p.c_str());
    if (dir == 0) {
      return 0;
    }
    while (struct dirent* entry = readdir(dir)) {
      if (!isDotEntry(entry->d_name)) {
        if (pass == 1 && index < count) {
          e->SetObjectArrayElement(result, index, shimutf8::fromUtf8(e, entry->d_name));
        }
        ++index;
      }
    }
    closedir(dir);
#endif
    if (pass == 0) {
      count = index;
      result = e->NewObjectArray(count, stringClass, 0);
      if (result == 0) {
        return 0;
      }
    }
  }
  return result;
}

extern "C" JNIEXPORT jint JNICALL
    Java_shim_FileNative_space(JNIEnv* e, jclass, jstring path, jlongArray out)
{
  FsString p;
  if (!toPath(e, path, p)) {
    return -EINVAL;
  }
  jlong values[3];
#ifdef PLATFORM_WINDOWS
  ULARGE_INTEGER avail;
  ULARGE_INTEGER total;
  ULARGE_INTEGER free;
  if (!GetDiskFreeSpaceExW(p.c_str(), &avail, &total, &free)) {
    return -ENOENT;
  }
  values[0] = static_cast<jlong>(total.QuadPart);
  values[1] = static_cast<jlong>(free.QuadPart);
  values[2] = static_cast<jlong>(avail.QuadPart);
#else
  struct statvfs sv;
  if (statvfs(p.c_str(), &sv) < 0) {
    return err();
  }
  values[0] = static_cast<jlong>(sv.f_frsize) * static_cast<jlong>(sv.f_blocks);
  values[1] = static_cast<jlong>(sv.f_frsize) * static_cast<jlong>(sv.f_bfree);
  values[2] = static_cast<jlong>(sv.f_frsize) * static_cast<jlong>(sv.f_bavail);
#endif
  e->SetLongArrayRegion(out, 0, 3, values);
  return 0;
}

extern "C" JNIEXPORT jstring JNICALL Java_shim_FileNative_strerror(JNIEnv* e, jclass, jint number)
{
#ifndef PLATFORM_WINDOWS
  static bool localeSet = false;
  if (!localeSet) {
    localeSet = true;
    setlocale(LC_MESSAGES, "");
    setlocale(LC_CTYPE, "");
  }
#endif
  return shimutf8::fromUtf8(e, strerror(number));
}

extern "C" JNIEXPORT jstring JNICALL Java_shim_FileNative_cwd(JNIEnv* e, jclass)
{
#ifdef PLATFORM_WINDOWS
  wchar_t buffer[MAX_PATH * 4];
  if (_wgetcwd(buffer, MAX_PATH * 4) == 0) {
    return 0;
  }
  return e->NewString(reinterpret_cast<const jchar*>(buffer), static_cast<jsize>(wcslen(buffer)));
#else
  char buffer[PATH_MAX + 1];
  if (getcwd(buffer, sizeof(buffer)) == 0) {
    return 0;
  }
  return shimutf8::fromUtf8(e, buffer);
#endif
}
