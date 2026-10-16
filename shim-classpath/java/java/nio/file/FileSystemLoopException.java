/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.file;

public class FileSystemLoopException extends FileSystemException {
  private static final long serialVersionUID = 4843039591949217617L;

  public FileSystemLoopException(String file) {
    super(file);
  }
}
