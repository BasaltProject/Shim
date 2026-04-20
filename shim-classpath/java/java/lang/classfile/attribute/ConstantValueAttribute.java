/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile.attribute;

import java.lang.classfile.Attribute;
import java.lang.classfile.FieldElement;
import java.lang.classfile.constantpool.ConstantValueEntry;
import java.lang.constant.ConstantDesc;

public interface ConstantValueAttribute extends Attribute<ConstantValueAttribute>, FieldElement {
    ConstantValueEntry constant();

    static ConstantValueAttribute of(ConstantValueEntry value) {
        return new shim.classfile.ConstantValueAttr(value);
    }

    static ConstantValueAttribute of(ConstantDesc value) {
        return shim.classfile.ConstantValueAttr.of(value);
    }
}
