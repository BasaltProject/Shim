/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile.constantpool;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;

public interface ClassEntry extends LoadableConstantEntry {
    Utf8Entry name();
    boolean matches(ClassDesc classDesc);
    String asInternalName();
    ClassDesc asSymbol();
    default ConstantDesc constantValue() { return asSymbol(); }
}
