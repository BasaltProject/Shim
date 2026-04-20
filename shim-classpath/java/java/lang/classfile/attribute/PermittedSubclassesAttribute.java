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
import java.util.Arrays;
import java.util.List;

public interface PermittedSubclassesAttribute extends Attribute<PermittedSubclassesAttribute>, ClassElement {
    List<ClassEntry> permittedSubclasses();

    static PermittedSubclassesAttribute of(List<ClassEntry> entries) {
        return new shim.classfile.PermittedSubclassesAttr(entries);
    }

    static PermittedSubclassesAttribute of(ClassEntry... entries) {
        return of(Arrays.asList(entries));
    }

    static PermittedSubclassesAttribute ofSymbols(List<ClassDesc> symbols) {
        return of(shim.classfile.AttrSupport.classEntries(symbols));
    }

    static PermittedSubclassesAttribute ofSymbols(ClassDesc... symbols) {
        return ofSymbols(Arrays.asList(symbols));
    }
}
