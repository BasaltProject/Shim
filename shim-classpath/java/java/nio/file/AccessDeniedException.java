/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.file;

public class AccessDeniedException extends FileSystemException {
  public AccessDeniedException(String file) {
    super(file);
  }

  public AccessDeniedException(String file, String other, String reason) {
    super(file, other, reason);
  }
}
