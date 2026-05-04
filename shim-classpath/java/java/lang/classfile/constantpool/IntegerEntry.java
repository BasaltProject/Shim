/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile.constantpool;

import java.lang.classfile.TypeKind;

public interface IntegerEntry extends AnnotationConstantValueEntry, ConstantValueEntry {
    int intValue();
    default TypeKind typeKind() { return TypeKind.INT; }
}
