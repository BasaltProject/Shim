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
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.constant.ClassDesc;

public interface ModuleMainClassAttribute extends Attribute<ModuleMainClassAttribute>, ClassElement {
    ClassEntry mainClass();

    static ModuleMainClassAttribute of(ClassEntry mainClass) {
        return new shim.classfile.ModuleMainClassAttr(mainClass);
    }

    static ModuleMainClassAttribute of(ClassDesc mainClass) {
        return of(shim.classfile.AttrSupport.pool().classEntry(mainClass));
    }
}
