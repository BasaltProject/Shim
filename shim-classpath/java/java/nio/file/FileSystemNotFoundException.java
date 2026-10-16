/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.file;

public class FileSystemNotFoundException extends RuntimeException {
  private static final long serialVersionUID = 7999581764446402397L;

  public FileSystemNotFoundException() { }

  public FileSystemNotFoundException(String msg) {
    super(msg);
  }
}
