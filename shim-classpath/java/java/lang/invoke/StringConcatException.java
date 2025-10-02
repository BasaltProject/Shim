/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.lang.invoke;

public class StringConcatException extends Exception {
  private static final long serialVersionUID = 301L;

    public StringConcatException(String message) {
        super(message);
    }

    public StringConcatException(String message, Throwable cause) {
        super(message, cause);
    }
}
