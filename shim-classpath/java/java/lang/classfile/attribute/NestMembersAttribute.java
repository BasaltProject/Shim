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

public interface NestMembersAttribute extends Attribute<NestMembersAttribute>, ClassElement {
    List<ClassEntry> nestMembers();

    static NestMembersAttribute of(List<ClassEntry> entries) {
        return new shim.classfile.NestMembersAttr(entries);
    }

    static NestMembersAttribute of(ClassEntry... entries) {
        return of(Arrays.asList(entries));
    }

    static NestMembersAttribute ofSymbols(List<ClassDesc> symbols) {
        return of(shim.classfile.AttrSupport.classEntries(symbols));
    }

    static NestMembersAttribute ofSymbols(ClassDesc... symbols) {
        return ofSymbols(Arrays.asList(symbols));
    }
}
