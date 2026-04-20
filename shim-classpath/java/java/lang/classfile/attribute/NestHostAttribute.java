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

public interface NestHostAttribute extends Attribute<NestHostAttribute>, ClassElement {
    ClassEntry nestHost();

    static NestHostAttribute of(ClassEntry nestHost) {
        return new shim.classfile.NestHostAttr(nestHost);
    }

    static NestHostAttribute of(ClassDesc nestHost) {
        return of(shim.classfile.AttrSupport.pool().classEntry(nestHost));
    }
}
