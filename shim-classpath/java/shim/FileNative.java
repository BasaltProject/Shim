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

  private static native int open0(String path, int flags, int mode);

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

  private static native int stat0(String path, long[] out, boolean follow);

  private static native int access0(String path, int mode);

  private static native int mkdir0(String path, int mode);

  private static native int unlink0(String path);

  private static native int rmdir0(String path);

  private static native int rename0(String from, String to);

  private static native int chmod0(String path, int mode);

  private static native int setModified0(String path, long millis);

  private static native String realpath0(String path);

  private static native String[] list0(String path);

  private static native int space0(String path, long[] out);

  public static native String strerror(int errno);

  public static native String cwd();

  public static native boolean isTerminal(int fd);

  public static native boolean setEcho(boolean on);

  public static final int OP_READ = 0;
  public static final int OP_WRITE = 1;

  private static volatile java.util.function.BiFunction<String, Integer, String> mapper;
  private static final ThreadLocal<Boolean> MAPPING = new ThreadLocal<>();

  /**
   * Installs a function that sees every path before the file system does. It receives the path as
   * the program wrote it and OP_READ or OP_WRITE, and returns the path to use instead, or null to
   * refuse the access. File I/O done by the mapper itself is not mapped. Pass null to remove it.
   */
  public static void setPathMapper(java.util.function.BiFunction<String, Integer, String> function) {
    mapper = function;
  }

  private static String map(String path, int operation) {
    java.util.function.BiFunction<String, Integer, String> function = mapper;
    if (function == null || path == null || MAPPING.get() != null) {
      return path;
    }
    MAPPING.set(Boolean.TRUE);
    try {
      return function.apply(path, operation);
    } finally {
      MAPPING.remove();
    }
  }

  public static int open(String path, int flags, int mode) {
    String mapped = map(path, (flags & (WRITE | APPEND | CREATE | CREATE_NEW | TRUNCATE)) != 0 ? OP_WRITE : OP_READ);
    return mapped == null ? -EACCES : open0(mapped, flags, mode);
  }

  public static int stat(String path, long[] out, boolean follow) {
    String mapped = map(path, OP_READ);
    return mapped == null ? -EACCES : stat0(mapped, out, follow);
  }

  public static int access(String path, int mode) {
    String mapped = map(path, (mode & W_OK) != 0 ? OP_WRITE : OP_READ);
    return mapped == null ? -EACCES : access0(mapped, mode);
  }

  public static int mkdir(String path, int mode) {
    String mapped = map(path, OP_WRITE);
    return mapped == null ? -EACCES : mkdir0(mapped, mode);
  }

  public static int unlink(String path) {
    String mapped = map(path, OP_WRITE);
    return mapped == null ? -EACCES : unlink0(mapped);
  }

  public static int rmdir(String path) {
    String mapped = map(path, OP_WRITE);
    return mapped == null ? -EACCES : rmdir0(mapped);
  }

  public static int rename(String from, String to) {
    String source = map(from, OP_WRITE);
    String target = source == null ? null : map(to, OP_WRITE);
    return target == null ? -EACCES : rename0(source, target);
  }

  public static int chmod(String path, int mode) {
    String mapped = map(path, OP_WRITE);
    return mapped == null ? -EACCES : chmod0(mapped, mode);
  }

  public static int setModified(String path, long millis) {
    String mapped = map(path, OP_WRITE);
    return mapped == null ? -EACCES : setModified0(mapped, millis);
  }

  public static String realpath(String path) {
    String mapped = map(path, OP_READ);
    return mapped == null ? null : realpath0(mapped);
  }

  public static String[] list(String path) {
    String mapped = map(path, OP_READ);
    return mapped == null ? null : list0(mapped);
  }

  public static int space(String path, long[] out) {
    String mapped = map(path, OP_READ);
    return mapped == null ? -EACCES : space0(mapped, out);
  }

  public static String message(int negativeErrno) {
    return strerror(-negativeErrno);
  }
}
