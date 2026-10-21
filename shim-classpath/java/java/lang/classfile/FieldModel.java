/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile;

import java.lang.classfile.constantpool.Utf8Entry;
import java.lang.constant.ClassDesc;
import java.util.Optional;

public interface FieldModel extends CompoundElement<FieldElement>, AttributedElement, ClassElement {
    Optional<ClassModel> parent();
    AccessFlags flags();
    Utf8Entry fieldType();
    Utf8Entry fieldName();
    default ClassDesc fieldTypeSymbol() { return ClassDesc.ofDescriptor(fieldType().stringValue()); }
}
