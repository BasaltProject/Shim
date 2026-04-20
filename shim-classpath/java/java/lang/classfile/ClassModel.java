/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile;

import java.lang.classfile.constantpool.ClassEntry;
import java.lang.classfile.constantpool.ConstantPool;
import java.util.List;
import java.util.Optional;

public interface ClassModel extends CompoundElement<ClassElement>, AttributedElement {
    AccessFlags flags();
    List<ClassEntry> interfaces();
    Optional<ClassEntry> superclass();
    List<MethodModel> methods();
    List<FieldModel> fields();
    ClassEntry thisClass();
    ConstantPool constantPool();
    int majorVersion();
    int minorVersion();
    boolean isModuleInfo();
}
