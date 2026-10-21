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
import java.lang.classfile.constantpool.Utf8Entry;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.AccessFlag;
import java.util.List;
import java.util.function.Consumer;

public interface ClassBuilder extends ClassFileBuilder<ClassElement, ClassBuilder> {
    default ClassBuilder withFlags(int i) { return with(new shim.classfile.Elements.AccessFlagsImpl(AccessFlag.Location.CLASS, i)); }
    default ClassBuilder withFlags(AccessFlag... accessFlags) {
        int mask = 0;
        for (AccessFlag f : accessFlags) {
            mask |= f.mask();
        }
        return withFlags(mask);
    }
    ClassBuilder withMethod(Utf8Entry utf8Entry, Utf8Entry utf8Entry2, int i, Consumer<? super MethodBuilder> consumer);
    default ClassBuilder withMethod(String text, MethodTypeDesc methodTypeDesc, int i, Consumer<? super MethodBuilder> consumer) { return withMethod(constantPool().utf8Entry(text), constantPool().utf8Entry(methodTypeDesc), i, consumer); }
    default ClassBuilder withMethodBody(Utf8Entry utf8Entry, Utf8Entry utf8Entry2, int i, Consumer<? super CodeBuilder> consumer) { return withMethod(utf8Entry, utf8Entry2, i, mb -> mb.withCode(consumer)); }
    default ClassBuilder withMethodBody(String text, MethodTypeDesc methodTypeDesc, int i, Consumer<? super CodeBuilder> consumer) { return withMethodBody(constantPool().utf8Entry(text), constantPool().utf8Entry(methodTypeDesc), i, consumer); }
    default ClassBuilder withSuperclass(ClassEntry classEntry) { return with(Superclass.of(classEntry)); }
    default ClassBuilder withSuperclass(ClassDesc classDesc) { return withSuperclass(constantPool().classEntry(classDesc)); }
    default ClassBuilder withField(String text, ClassDesc classDesc, Consumer<? super FieldBuilder> consumer) { return withField(constantPool().utf8Entry(text), constantPool().utf8Entry(classDesc), consumer); }
    default ClassBuilder withField(Utf8Entry utf8Entry, Utf8Entry utf8Entry2, int i) { return withField(utf8Entry, utf8Entry2, fb -> fb.withFlags(i)); }
    default ClassBuilder withField(String text, ClassDesc classDesc, int i) { return withField(constantPool().utf8Entry(text), constantPool().utf8Entry(classDesc), i); }
    ClassBuilder withField(Utf8Entry utf8Entry, Utf8Entry utf8Entry2, Consumer<? super FieldBuilder> consumer);
    ClassBuilder transformField(FieldModel fieldModel, FieldTransform fieldTransform);
    ClassBuilder transformMethod(MethodModel methodModel, MethodTransform methodTransform);
    default ClassBuilder withInterfaces(List<ClassEntry> list) { return with(Interfaces.of(list)); }
    default ClassBuilder withInterfaces(ClassEntry... classEntrys) { return with(Interfaces.of(classEntrys)); }
    default ClassBuilder withInterfaceSymbols(List<ClassDesc> list) { return with(Interfaces.ofSymbols(list)); }
    default ClassBuilder withInterfaceSymbols(ClassDesc... classDescs) { return withInterfaceSymbols(List.of(classDescs)); }
    default ClassBuilder withVersion(int i, int i2) { return with(ClassFileVersion.of(i, i2)); }
}
