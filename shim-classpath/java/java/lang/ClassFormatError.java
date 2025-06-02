/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.lang;

public class ClassFormatError extends LinkageError {
    public ClassFormatError() {
    }

    public ClassFormatError(String message) {
        super(message);
    }
}
