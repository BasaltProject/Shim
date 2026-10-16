/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.file;

public class NotLinkException extends FileSystemException {
  private static final long serialVersionUID = 1L;

  public NotLinkException(String file) {
    super(file);
  }

  public NotLinkException(String file, String other, String reason) {
    super(file, other, reason);
  }
}
