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

  public static native int statx(String path, long[] out, boolean follow);

  public static native int fstatx(int fd, long[] out);

  public static native int symlink(String target, String link);

  public static native int readlink(String path, String[] out);

  public static native int link(String existing, String newLink);

  public static native int chown(String path, int uid, int gid, boolean follow);

  public static native int chmod(String path, int mode);

  public static native int utimens(String path, long atimeSec, long atimeNsec, long mtimeSec, long mtimeNsec,
      boolean follow);

  public static native int fsinfo(String path, long[] out);

  public static native String userName(int uid);

  public static native String groupName(int gid);

  public static native long userId(String name);

  public static native long groupId(String name);

  public static native int listxattr(String path, byte[] buf, boolean follow);

  public static native int getxattr(String path, String name, byte[] buf, int off, int len, boolean follow);

  public static native int setxattr(String path, String name, byte[] buf, int off, int len);

  public static native int removexattr(String path, String name);

  public static native long mmap(int fd, int prot, int flags, long offset, long size);

  public static native int munmap(long address, long size);

  public static native int msync(long address, long size);

  public static native int lock(int fd, long position, long size, boolean shared, boolean block);

  public static native int unlock(int fd, long position, long size);

  public static native int errnoValue(int which);

  public static native int realpath(String path, String[] out);

  public static native String[] listdir(String path, int[] errOut);

  public static native int inotifyInit();

  public static native int inotifyAddWatch(int fd, String path, int mask);

  public static native int inotifyRmWatch(int fd, int wd);

  public static native int inotifyRead(int fd, int wakeFd, byte[] buf);

  public static native int pipe(int[] out);
}
