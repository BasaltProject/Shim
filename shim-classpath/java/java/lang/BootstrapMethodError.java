/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.lang;

public class BootstrapMethodError extends LinkageError {
    public BootstrapMethodError() {
    }

    public BootstrapMethodError(String message) {
        super(message);
    }

    public BootstrapMethodError(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }

    public BootstrapMethodError(Throwable cause) {
        this(cause == null ? null : cause.toString(), cause);
    }
}
