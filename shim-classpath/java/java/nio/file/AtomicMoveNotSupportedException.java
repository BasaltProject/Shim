/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.file;

public class AtomicMoveNotSupportedException extends FileSystemException {
  private static final long serialVersionUID = 1L;

  public AtomicMoveNotSupportedException(String source, String target, String reason) {
    super(source, target, reason);
  }
}
