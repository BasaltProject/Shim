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
import java.lang.constant.MethodTypeDesc;
import java.util.Optional;

public interface MethodModel extends CompoundElement<MethodElement>, AttributedElement, ClassElement {
    Optional<ClassModel> parent();
    AccessFlags flags();
    Utf8Entry methodName();
    Optional<CodeModel> code();
    Utf8Entry methodType();
    default MethodTypeDesc methodTypeSymbol() { return MethodTypeDesc.ofDescriptor(methodType().stringValue()); }
}
