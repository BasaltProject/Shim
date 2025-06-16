/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.lang.invoke;

public class StringConcatException extends Exception {
    public StringConcatException(String message) {
        super(message);
    }

    public StringConcatException(String message, Throwable cause) {
        super(message, cause);
    }
}
