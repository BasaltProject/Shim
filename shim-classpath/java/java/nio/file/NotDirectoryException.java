/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.file;

public class NotDirectoryException extends FileSystemException {
  private static final long serialVersionUID = -9011457427178200199L;

  public NotDirectoryException(String file) {
    super(file);
  }
}
