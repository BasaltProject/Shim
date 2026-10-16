/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.file;

public class NoSuchFileException extends FileSystemException {
  private static final long serialVersionUID = -1390291775875351931L;

  public NoSuchFileException(String file) {
    super(file);
  }

  public NoSuchFileException(String file, String other, String reason) {
    super(file, other, reason);
  }
}
