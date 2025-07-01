/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.lang;

public class VerifyError extends LinkageError {
    public VerifyError() {
    }

    public VerifyError(String message) {
        super(message);
    }
}
