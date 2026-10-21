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

public interface FieldRefEntry extends MemberRefEntry {
    default ClassDesc typeSymbol() { return ClassDesc.ofDescriptor(type().stringValue()); }
}
