/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile.constantpool;

import java.lang.classfile.BootstrapMethodEntry;
import java.lang.classfile.ClassModel;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.DynamicCallSiteDesc;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.constant.ModuleDesc;
import java.lang.constant.PackageDesc;
import java.util.List;

public interface ConstantPoolBuilder extends ConstantPool {
    public static ConstantPoolBuilder of(ClassModel classModel) { return new shim.classfile.PoolImpl((shim.classfile.ClassReaderImpl) classModel.constantPool()); }
    public static ConstantPoolBuilder of() { return new shim.classfile.PoolImpl(); }
    ClassEntry classEntry(Utf8Entry utf8Entry);
    default ClassEntry classEntry(ClassDesc classDesc) { return classEntry(utf8Entry(classDesc.isArray() ? classDesc.descriptorString() : shim.classfile.Names.internalName(classDesc))); }
    default NameAndTypeEntry nameAndTypeEntry(String text, MethodTypeDesc methodTypeDesc) { return nameAndTypeEntry(utf8Entry(text), utf8Entry(methodTypeDesc)); }
    default NameAndTypeEntry nameAndTypeEntry(String text, ClassDesc classDesc) { return nameAndTypeEntry(utf8Entry(text), utf8Entry(classDesc)); }
    NameAndTypeEntry nameAndTypeEntry(Utf8Entry utf8Entry, Utf8Entry utf8Entry2);
    FieldRefEntry fieldRefEntry(ClassEntry classEntry, NameAndTypeEntry nameAndTypeEntry);
    default FieldRefEntry fieldRefEntry(ClassDesc classDesc, String text, ClassDesc classDesc2) { return fieldRefEntry(classEntry(classDesc), nameAndTypeEntry(text, classDesc2)); }
    default Utf8Entry utf8Entry(ClassDesc classDesc) { return utf8Entry(classDesc.descriptorString()); }
    default Utf8Entry utf8Entry(MethodTypeDesc methodTypeDesc) { return utf8Entry(methodTypeDesc.descriptorString()); }
    Utf8Entry utf8Entry(String text);
    IntegerEntry intEntry(int i);
    FloatEntry floatEntry(float f);
    LongEntry longEntry(long l);
    DoubleEntry doubleEntry(double d);
    default PackageEntry packageEntry(PackageDesc packageDesc) { return packageEntry(utf8Entry(packageDesc.internalName())); }
    PackageEntry packageEntry(Utf8Entry utf8Entry);
    ModuleEntry moduleEntry(Utf8Entry utf8Entry);
    default ModuleEntry moduleEntry(ModuleDesc moduleDesc) { return moduleEntry(utf8Entry(moduleDesc.name())); }
    MethodRefEntry methodRefEntry(ClassEntry classEntry, NameAndTypeEntry nameAndTypeEntry);
    default MethodRefEntry methodRefEntry(ClassDesc classDesc, String text, MethodTypeDesc methodTypeDesc) { return methodRefEntry(classEntry(classDesc), nameAndTypeEntry(text, methodTypeDesc)); }
    default InterfaceMethodRefEntry interfaceMethodRefEntry(ClassDesc classDesc, String text, MethodTypeDesc methodTypeDesc) { return interfaceMethodRefEntry(classEntry(classDesc), nameAndTypeEntry(text, methodTypeDesc)); }
    InterfaceMethodRefEntry interfaceMethodRefEntry(ClassEntry classEntry, NameAndTypeEntry nameAndTypeEntry);
    MethodTypeEntry methodTypeEntry(MethodTypeDesc methodTypeDesc);
    MethodTypeEntry methodTypeEntry(Utf8Entry utf8Entry);
    MethodHandleEntry methodHandleEntry(int i, MemberRefEntry memberRefEntry);
    default MethodHandleEntry methodHandleEntry(DirectMethodHandleDesc directMethodHandleDesc) {
        ClassEntry owner = classEntry(directMethodHandleDesc.owner());
        MemberRefEntry ref;
        switch (directMethodHandleDesc.refKind()) {
            case 1: case 2: case 3: case 4:
                ref = fieldRefEntry(owner, nameAndTypeEntry(directMethodHandleDesc.methodName(),
                    ClassDesc.ofDescriptor(directMethodHandleDesc.lookupDescriptor())));
                break;
            default:
                NameAndTypeEntry nt = nameAndTypeEntry(directMethodHandleDesc.methodName(),
                    MethodTypeDesc.ofDescriptor(directMethodHandleDesc.lookupDescriptor()));
                ref = directMethodHandleDesc.isOwnerInterface() ? interfaceMethodRefEntry(owner, nt)
                    : methodRefEntry(owner, nt);
        }
        return methodHandleEntry(directMethodHandleDesc.refKind(), ref);
    }
    default InvokeDynamicEntry invokeDynamicEntry(DynamicCallSiteDesc dynamicCallSiteDesc) {
        return invokeDynamicEntry(bsmEntry((DirectMethodHandleDesc) dynamicCallSiteDesc.bootstrapMethod(),
            List.of(dynamicCallSiteDesc.bootstrapArgs())),
            nameAndTypeEntry(dynamicCallSiteDesc.invocationName(), dynamicCallSiteDesc.invocationType()));
    }
    InvokeDynamicEntry invokeDynamicEntry(BootstrapMethodEntry bootstrapMethodEntry, NameAndTypeEntry nameAndTypeEntry);
    ConstantDynamicEntry constantDynamicEntry(BootstrapMethodEntry bootstrapMethodEntry, NameAndTypeEntry nameAndTypeEntry);
    default ConstantDynamicEntry constantDynamicEntry(DynamicConstantDesc<?> dynamicConstantDesc) {
        return constantDynamicEntry(bsmEntry(dynamicConstantDesc.bootstrapMethod(),
            List.of(dynamicConstantDesc.bootstrapArgs())),
            nameAndTypeEntry(dynamicConstantDesc.constantName(), dynamicConstantDesc.constantType()));
    }
    StringEntry stringEntry(Utf8Entry utf8Entry);
    default StringEntry stringEntry(String text) { return stringEntry(utf8Entry(text)); }
    default BootstrapMethodEntry bsmEntry(DirectMethodHandleDesc directMethodHandleDesc, List<ConstantDesc> list) {
        MethodHandleEntry handle = methodHandleEntry(directMethodHandleDesc);
        List<LoadableConstantEntry> args = new java.util.ArrayList<>(list.size());
        for (ConstantDesc d : list) {
            args.add(loadableConstantEntry(d));
        }
        return bsmEntry(handle, args);
    }
    BootstrapMethodEntry bsmEntry(MethodHandleEntry methodHandleEntry, List<LoadableConstantEntry> list);
    boolean canWriteDirect(ConstantPool constantPool);
    default ConstantValueEntry constantValueEntry(ConstantDesc constantDesc) {
        if (constantDesc instanceof Integer) {
            return intEntry((Integer) constantDesc);
        } else if (constantDesc instanceof String) {
            return stringEntry((String) constantDesc);
        } else if (constantDesc instanceof Long) {
            return longEntry((Long) constantDesc);
        } else if (constantDesc instanceof Float) {
            return floatEntry((Float) constantDesc);
        } else if (constantDesc instanceof Double) {
            return doubleEntry((Double) constantDesc);
        }
        throw new IllegalArgumentException("Illegal type: " + (constantDesc == null ? null : constantDesc.getClass()));
    }

    default LoadableConstantEntry loadableConstantEntry(ConstantDesc constantDesc) {
        if (constantDesc instanceof ClassDesc) {
            return classEntry((ClassDesc) constantDesc);
        } else if (constantDesc instanceof MethodTypeDesc) {
            return methodTypeEntry((MethodTypeDesc) constantDesc);
        } else if (constantDesc instanceof DirectMethodHandleDesc) {
            return methodHandleEntry((DirectMethodHandleDesc) constantDesc);
        } else if (constantDesc instanceof DynamicConstantDesc<?>) {
            return constantDynamicEntry((DynamicConstantDesc<?>) constantDesc);
        }
        return constantValueEntry(constantDesc);
    }
}
