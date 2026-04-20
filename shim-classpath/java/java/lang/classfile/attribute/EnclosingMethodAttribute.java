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
import java.lang.classfile.constantpool.NameAndTypeEntry;
import java.lang.classfile.constantpool.Utf8Entry;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.Optional;

public interface EnclosingMethodAttribute extends Attribute<EnclosingMethodAttribute>, ClassElement {
    ClassEntry enclosingClass();

    Optional<NameAndTypeEntry> enclosingMethod();

    default Optional<Utf8Entry> enclosingMethodName() {
        return enclosingMethod().map(NameAndTypeEntry::name);
    }

    default Optional<Utf8Entry> enclosingMethodType() {
        return enclosingMethod().map(NameAndTypeEntry::type);
    }

    default Optional<MethodTypeDesc> enclosingMethodTypeSymbol() {
        return enclosingMethodType().map(t -> MethodTypeDesc.ofDescriptor(t.stringValue()));
    }

    static EnclosingMethodAttribute of(ClassEntry className, Optional<NameAndTypeEntry> method) {
        return new shim.classfile.EnclosingMethodAttr(className, method);
    }

    static EnclosingMethodAttribute of(ClassDesc className, Optional<String> methodName,
                                       Optional<MethodTypeDesc> methodType) {
        shim.classfile.PoolImpl pool = shim.classfile.AttrSupport.pool();
        NameAndTypeEntry nat = methodName.isPresent() && methodType.isPresent()
            ? pool.nameAndTypeEntry(methodName.get(), methodType.get()) : null;
        return new shim.classfile.EnclosingMethodAttr(pool.classEntry(className), Optional.ofNullable(nat));
    }
}
