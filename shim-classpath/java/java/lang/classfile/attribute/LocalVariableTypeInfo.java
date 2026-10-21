/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile.attribute;

import java.lang.classfile.constantpool.Utf8Entry;

public interface LocalVariableTypeInfo {
    int startPc();

    int length();

    Utf8Entry name();

    Utf8Entry signature();

    int slot();
}
