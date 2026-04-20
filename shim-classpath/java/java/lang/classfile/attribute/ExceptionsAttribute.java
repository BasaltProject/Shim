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
import java.lang.classfile.MethodElement;
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.constant.ClassDesc;
import java.util.Arrays;
import java.util.List;

public interface ExceptionsAttribute extends Attribute<ExceptionsAttribute>, MethodElement {
    List<ClassEntry> exceptions();

    static ExceptionsAttribute of(List<ClassEntry> entries) {
        return new shim.classfile.ExceptionsAttr(entries);
    }

    static ExceptionsAttribute of(ClassEntry... entries) {
        return of(Arrays.asList(entries));
    }

    static ExceptionsAttribute ofSymbols(List<ClassDesc> symbols) {
        return of(shim.classfile.AttrSupport.classEntries(symbols));
    }

    static ExceptionsAttribute ofSymbols(ClassDesc... symbols) {
        return ofSymbols(Arrays.asList(symbols));
    }
}
