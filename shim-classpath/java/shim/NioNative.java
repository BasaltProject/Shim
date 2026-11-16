/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim;

public final class NioNative {
  public static final int kMode = 0;
  public static final int kSize = 1;
  public static final int kDev = 2;
  public static final int kIno = 3;
  public static final int kNlink = 4;
  public static final int kUid = 5;
  public static final int kGid = 6;
  public static final int kRdev = 7;
  public static final int kAtimeSec = 8;
  public static final int kAtimeNsec = 9;
  public static final int kMtimeSec = 10;
  public static final int kMtimeNsec = 11;
  public static final int kCtimeSec = 12;
  public static final int kCtimeNsec = 13;
  public static final int kBirthSec = 14;
  public static final int kBirthNsec = 15;

  private NioNative() { }

  private static native int statx0(String path, long[] out, boolean follow);

  public static int statx(String path, long[] out, boolean follow) {
    String m = FileNative.mapPath(path, FileNative.OP_READ);
    return m == null ? -FileNative.EACCES : statx0(m, out, follow);
  }

  public static native int fstatx(int fd, long[] out);

  private static native int symlink0(String target, String link);

  public static int symlink(String target, String link) {
    String m = FileNative.mapPath(link, FileNative.OP_WRITE);
    return m == null ? -FileNative.EACCES : symlink0(target, m);
  }

  private static native int readlink0(String path, String[] out);

  public static int readlink(String path, String[] out) {
    String m = FileNative.mapPath(path, FileNative.OP_READ);
    return m == null ? -FileNative.EACCES : readlink0(m, out);
  }

  private static native int link0(String existing, String newLink);

  public static int link(String existing, String newLink) {
    String a = FileNative.mapPath(existing, FileNative.OP_READ);
    String b = a == null ? null : FileNative.mapPath(newLink, FileNative.OP_WRITE);
    return b == null ? -FileNative.EACCES : link0(a, b);
  }

  private static native int chown0(String path, int uid, int gid, boolean follow);

  public static int chown(String path, int uid, int gid, boolean follow) {
    String m = FileNative.mapPath(path, FileNative.OP_WRITE);
    return m == null ? -FileNative.EACCES : chown0(m, uid, gid, follow);
  }

  private static native int chmod0(String path, int mode);

  public static int chmod(String path, int mode) {
    String m = FileNative.mapPath(path, FileNative.OP_WRITE);
    return m == null ? -FileNative.EACCES : chmod0(m, mode);
  }

  private static native int utimens0(String path, long atimeSec, long atimeNsec, long mtimeSec, long mtimeNsec,
      boolean follow);

  public static int utimens(String path, long atimeSec, long atimeNsec, long mtimeSec, long mtimeNsec,
      boolean follow) {
    String m = FileNative.mapPath(path, FileNative.OP_WRITE);
    return m == null ? -FileNative.EACCES : utimens0(m, atimeSec, atimeNsec, mtimeSec, mtimeNsec, follow);
  }

  private static native int fsinfo0(String path, long[] out);

  public static int fsinfo(String path, long[] out) {
    String m = FileNative.mapPath(path, FileNative.OP_READ);
    return m == null ? -FileNative.EACCES : fsinfo0(m, out);
  }

  public static native String userName(int uid);

  public static native String groupName(int gid);

  public static native long userId(String name);

  public static native long groupId(String name);

  private static native int listxattr0(String path, byte[] buf, boolean follow);

  public static int listxattr(String path, byte[] buf, boolean follow) {
    String m = FileNative.mapPath(path, FileNative.OP_READ);
    return m == null ? -FileNative.EACCES : listxattr0(m, buf, follow);
  }

  private static native int getxattr0(String path, String name, byte[] buf, int off, int len, boolean follow);

  public static int getxattr(String path, String name, byte[] buf, int off, int len, boolean follow) {
    String m = FileNative.mapPath(path, FileNative.OP_READ);
    return m == null ? -FileNative.EACCES : getxattr0(m, name, buf, off, len, follow);
  }

  private static native int setxattr0(String path, String name, byte[] buf, int off, int len);

  public static int setxattr(String path, String name, byte[] buf, int off, int len) {
    String m = FileNative.mapPath(path, FileNative.OP_WRITE);
    return m == null ? -FileNative.EACCES : setxattr0(m, name, buf, off, len);
  }

  private static native int removexattr0(String path, String name);

  public static int removexattr(String path, String name) {
    String m = FileNative.mapPath(path, FileNative.OP_WRITE);
    return m == null ? -FileNative.EACCES : removexattr0(m, name);
  }

  public static native long mmap(int fd, int prot, int flags, long offset, long size);

  public static native int munmap(long address, long size);

  public static native int msync(long address, long size);

  public static native int lock(int fd, long position, long size, boolean shared, boolean block);

  public static native int unlock(int fd, long position, long size);

  public static native int errnoValue(int which);

  private static native int realpath0(String path, String[] out);

  public static int realpath(String path, String[] out) {
    String m = FileNative.mapPath(path, FileNative.OP_READ);
    return m == null ? -FileNative.EACCES : realpath0(m, out);
  }

  private static native String[] listdir0(String path, int[] errOut);

  public static String[] listdir(String path, int[] errOut) {
    String m = FileNative.mapPath(path, FileNative.OP_READ);
    return m == null ? null : listdir0(m, errOut);
  }

  public static native int inotifyInit();

  public static native int inotifyAddWatch(int fd, String path, int mask);

  public static native int inotifyRmWatch(int fd, int wd);

  public static native int inotifyRead(int fd, int wakeFd, byte[] buf);

  public static native int pipe(int[] out);
}
