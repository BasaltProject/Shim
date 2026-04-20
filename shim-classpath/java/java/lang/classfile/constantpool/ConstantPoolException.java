/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile.constantpool;

public class ConstantPoolException extends IllegalArgumentException {
    public ConstantPoolException(String text, Throwable throwable) { super(text, throwable); }
    public ConstantPoolException(Throwable throwable) { super(throwable); }
    public ConstantPoolException(String text) { super(text); }
    public ConstantPoolException() { super(); }
}
