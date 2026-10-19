/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim;

public final class FileNative {
  private FileNative() { }

  public static final int READ = 1;
  public static final int WRITE = 2;
  public static final int APPEND = 4;
  public static final int CREATE = 8;
  public static final int CREATE_NEW = 16;
  public static final int TRUNCATE = 32;
  public static final int SYNC = 64;
  public static final int DSYNC = 128;
  public static final int NOFOLLOW = 256;
  public static final int ALLOW_DIR = 512;

  public static final int ENOENT = 2;
  public static final int EACCES = 13;
  public static final int EEXIST = 17;
  public static final int EISDIR = 21;
  public static final int ENOTDIR = 20;
  public static final int ENOTEMPTY = 39;

  public static final int S_IFMT = 0170000;
  public static final int S_IFDIR = 0040000;
  public static final int S_IFREG = 0100000;
  public static final int S_IFLNK = 0120000;

  public static final int F_OK = 0;
  public static final int X_OK = 1;
  public static final int W_OK = 2;
  public static final int R_OK = 4;

  public static native int open(String path, int flags, int mode);

  public static native int close(int fd);

  public static native int read(int fd, byte[] b, int off, int len);

  public static native int readByte(int fd);

  public static native int write(int fd, byte[] b, int off, int len);

  public static native int writeByte(int fd, int b);

  public static native int pread(int fd, byte[] b, int off, int len, long pos);

  public static native int pwrite(int fd, byte[] b, int off, int len, long pos);

  public static native long seek(int fd, long offset, int whence);

  public static native long size(int fd);

  public static native int truncate(int fd, long length);

  public static native int sync(int fd, boolean metadata);

  public static native long available(int fd);

  public static native int stat(String path, long[] out, boolean follow);

  public static native int access(String path, int mode);

  public static native int mkdir(String path, int mode);

  public static native int unlink(String path);

  public static native int rmdir(String path);

  public static native int rename(String from, String to);

  public static native int chmod(String path, int mode);

  public static native int setModified(String path, long millis);

  public static native String realpath(String path);

  public static native String[] list(String path);

  public static native int space(String path, long[] out);

  public static native String strerror(int errno);

  public static native String cwd();

  public static native boolean isTerminal(int fd);

  public static native boolean setEcho(boolean on);

  public static String message(int negativeErrno) {
    return strerror(-negativeErrno);
  }
}
