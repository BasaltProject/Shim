/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile.constantpool;

public interface StringEntry extends ConstantValueEntry {
    Utf8Entry utf8();
    String stringValue();
    boolean equalsString(String text);
}
