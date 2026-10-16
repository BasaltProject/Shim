/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.lang;

public class ClassFormatError extends LinkageError {
  private static final long serialVersionUID = -8420114879011949195L;

    public ClassFormatError() {
    }

    public ClassFormatError(String message) {
        super(message);
    }
}
