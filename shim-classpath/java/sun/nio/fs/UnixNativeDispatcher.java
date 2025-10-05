/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.fs;

import shim.FileNative;
import shim.NioNative;

final class UnixNativeDispatcher {
  private UnixNativeDispatcher() { }

  static void check(int r) throws UnixException {
    if (r < 0) {
      throw new UnixException(-r);
    }
  }

  static int open(UnixPath path, int flags, int mode) throws UnixException {
    int r = FileNative.open(path.getPathForSysCalls(), flags, mode);
    check(r);
    return r;
  }

  static void close(int fd) {
    FileNative.close(fd);
  }

  static void unlink(UnixPath path) throws UnixException {
    check(FileNative.unlink(path.getPathForSysCalls()));
  }

  static void rmdir(UnixPath path) throws UnixException {
    check(FileNative.rmdir(path.getPathForSysCalls()));
  }

  static void mkdir(UnixPath path, int mode) throws UnixException {
    check(FileNative.mkdir(path.getPathForSysCalls(), mode));
  }

  static void rename(UnixPath from, UnixPath to) throws UnixException {
    check(FileNative.rename(from.getPathForSysCalls(), to.getPathForSysCalls()));
  }

  static void access(UnixPath path, int amode) throws UnixException {
    check(FileNative.access(path.getPathForSysCalls(), amode));
  }

  static void symlink(String target, UnixPath link) throws UnixException {
    check(NioNative.symlink(target, link.getPathForSysCalls()));
  }

  static String readlink(UnixPath link) throws UnixException {
    String[] out = new String[1];
    check(NioNative.readlink(link.getPathForSysCalls(), out));
    return out[0];
  }

  static void link(UnixPath existing, UnixPath newLink) throws UnixException {
    check(NioNative.link(existing.getPathForSysCalls(), newLink.getPathForSysCalls()));
  }

  static void chmod(UnixPath path, int mode) throws UnixException {
    check(NioNative.chmod(path.getPathForSysCalls(), mode));
  }

  static void chown(UnixPath path, int uid, int gid, boolean follow) throws UnixException {
    check(NioNative.chown(path.getPathForSysCalls(), uid, gid, follow));
  }

  static void utimens(UnixPath path, long atimeSec, long atimeNsec, long mtimeSec, long mtimeNsec, boolean follow)
      throws UnixException {
    check(NioNative.utimens(path.getPathForSysCalls(), atimeSec, atimeNsec, mtimeSec, mtimeNsec, follow));
  }
}
