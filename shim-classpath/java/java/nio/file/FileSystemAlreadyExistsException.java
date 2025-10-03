/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.file;

public class FileSystemAlreadyExistsException extends RuntimeException {
  private static final long serialVersionUID = -5438419127181131148L;

  public FileSystemAlreadyExistsException() { }

  public FileSystemAlreadyExistsException(String msg) {
    super(msg);
  }
}
