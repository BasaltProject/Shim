/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.file.attribute;

public interface BasicFileAttributes {
  FileTime lastModifiedTime();

  FileTime lastAccessTime();

  FileTime creationTime();

  boolean isRegularFile();

  boolean isDirectory();

  boolean isSymbolicLink();

  boolean isOther();

  long size();

  Object fileKey();
}
