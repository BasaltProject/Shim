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
import java.lang.constant.ClassDesc;
import java.util.List;

public interface Interfaces extends ClassElement {
    public static Interfaces of(ClassEntry... classEntrys) { return of(List.of(classEntrys)); }
    public static Interfaces of(List<ClassEntry> list) { return new shim.classfile.Elements.InterfacesImpl(list); }
    List<ClassEntry> interfaces();
    public static Interfaces ofSymbols(List<ClassDesc> list) {
        java.lang.classfile.constantpool.ConstantPoolBuilder pool = java.lang.classfile.constantpool.ConstantPoolBuilder.of();
        List<ClassEntry> entries = new java.util.ArrayList<>(list.size());
        for (ClassDesc d : list) {
            entries.add(pool.classEntry(d));
        }
        return of(entries);
    }
    public static Interfaces ofSymbols(ClassDesc... classDescs) { return ofSymbols(List.of(classDescs)); }
}
