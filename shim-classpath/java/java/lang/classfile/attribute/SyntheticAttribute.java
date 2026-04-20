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
import java.lang.classfile.ClassElement;
import java.lang.classfile.FieldElement;
import java.lang.classfile.MethodElement;

public interface SyntheticAttribute extends Attribute<SyntheticAttribute>, ClassElement, MethodElement, FieldElement {
    static SyntheticAttribute of() {
        return new shim.classfile.SyntheticAttr();
    }
}
