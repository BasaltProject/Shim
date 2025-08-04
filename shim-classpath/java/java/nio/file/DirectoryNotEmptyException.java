/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.file;

public class DirectoryNotEmptyException extends FileSystemException {
  public DirectoryNotEmptyException(String dir) {
    super(dir);
  }
}
