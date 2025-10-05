/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.fs;

import shim.NioNative;

final class UnixConstants {
  static final int EPERM = NioNative.errnoValue(0);
  static final int ENOENT = NioNative.errnoValue(1);
  static final int EACCES = NioNative.errnoValue(2);
  static final int EEXIST = NioNative.errnoValue(3);
  static final int ENOTDIR = NioNative.errnoValue(4);
  static final int EISDIR = NioNative.errnoValue(5);
  static final int EINVAL = NioNative.errnoValue(6);
  static final int ELOOP = NioNative.errnoValue(7);
  static final int ENOTEMPTY = NioNative.errnoValue(8);
  static final int EXDEV = NioNative.errnoValue(9);
  static final int ENOSPC = NioNative.errnoValue(10);
  static final int EROFS = NioNative.errnoValue(11);
  static final int ENAMETOOLONG = NioNative.errnoValue(12);
  static final int ENODATA = NioNative.errnoValue(13);
  static final int ERANGE = NioNative.errnoValue(14);
  static final int EBUSY = NioNative.errnoValue(15);
  static final int EMLINK = NioNative.errnoValue(16);
  static final int ENOSYS = NioNative.errnoValue(17);
  static final int ENOTSUP = NioNative.errnoValue(18);
  static final int EIO = NioNative.errnoValue(19);

  static final int S_IFMT = 0170000;
  static final int S_IFREG = 0100000;
  static final int S_IFDIR = 0040000;
  static final int S_IFLNK = 0120000;
  static final int S_IFCHR = 0020000;
  static final int S_IFBLK = 0060000;
  static final int S_IFIFO = 0010000;
  static final int S_IFSOCK = 0140000;

  static final int S_IRUSR = 0400;
  static final int S_IWUSR = 0200;
  static final int S_IXUSR = 0100;
  static final int S_IRGRP = 0040;
  static final int S_IWGRP = 0020;
  static final int S_IXGRP = 0010;
  static final int S_IROTH = 0004;
  static final int S_IWOTH = 0002;
  static final int S_IXOTH = 0001;

  private UnixConstants() { }
}
