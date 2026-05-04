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
import java.lang.constant.ConstantDesc;

public interface LoadableConstantEntry extends PoolEntry {
    default TypeKind typeKind() { return TypeKind.REFERENCE; }
    ConstantDesc constantValue();
}
