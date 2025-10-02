/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.lang;

public class UnsupportedClassVersionError extends ClassFormatError {
  private static final long serialVersionUID = -7123279212883497373L;

    public UnsupportedClassVersionError() {
    }

    public UnsupportedClassVersionError(String message) {
        super(message);
    }
}
