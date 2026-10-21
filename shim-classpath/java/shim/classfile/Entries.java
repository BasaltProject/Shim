/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.BootstrapMethodEntry;
import java.lang.classfile.TypeKind;
import java.lang.classfile.constantpool.*;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.DynamicCallSiteDesc;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.constant.ModuleDesc;
import java.lang.constant.PackageDesc;
import java.util.ArrayList;
import java.util.List;

/** Constant pool entry implementations shared by the reader pool and the builder pool. */
final class Entries {
    private Entries() { }

    abstract static class Entry implements PoolEntry {
        final ConstantPool pool;
        int index;
        private final int tag;

        Entry(ConstantPool pool, int index, int tag) {
            this.pool = pool;
            this.index = index;
            this.tag = tag;
        }

        @Override public final int index() { return index; }
        @Override public final int tag() { return tag; }
        @Override public final ConstantPool constantPool() { return pool; }
        @Override public int width() { return 1; }

        abstract void write(BufWriterImpl out);
        abstract Object key();

        @Override
        public final boolean equals(Object o) {
            return o == this || (o instanceof Entry && ((Entry) o).tag == tag && key().equals(((Entry) o).key()));
        }

        @Override
        public final int hashCode() { return tag * 31 + key().hashCode(); }
    }

    static final class Utf8E extends Entry implements Utf8Entry {
        final String value;

        Utf8E(ConstantPool pool, int index, String value) {
            super(pool, index, TAG_UTF8);
            this.value = value;
        }

        @Override public String stringValue() { return value; }
        @Override public boolean equalsString(String text) { return value.equals(text); }
        @Override public boolean isFieldType(ClassDesc desc) { return value.equals(desc.descriptorString()); }
        @Override public boolean isMethodType(MethodTypeDesc desc) { return value.equals(desc.descriptorString()); }
        @Override public ConstantDesc constantValue() { return value; }
        @Override public int length() { return value.length(); }
        @Override public char charAt(int i) { return value.charAt(i); }
        @Override public CharSequence subSequence(int s, int e) { return value.subSequence(s, e); }
        @Override public String toString() { return value; }
        @Override Object key() { return value; }
        @Override void write(BufWriterImpl out) { out.writeU1(TAG_UTF8); out.writeUtf8(value); }
    }

    static final class ClassE extends Entry implements ClassEntry {
        final Utf8E name;
        private ClassDesc symbol;

        ClassE(ConstantPool pool, int index, Utf8E name) {
            super(pool, index, TAG_CLASS);
            this.name = name;
        }

        @Override public Utf8Entry name() { return name; }
        @Override public String asInternalName() { return name.value; }

        @Override
        public ClassDesc asSymbol() {
            if (symbol == null) {
                String n = name.value;
                symbol = n.startsWith("[") ? ClassDesc.ofDescriptor(n) : ClassDesc.ofInternalName(n);
            }
            return symbol;
        }

        @Override
        public boolean matches(ClassDesc desc) {
            String d = desc.descriptorString();
            return d.startsWith("[") ? name.value.equals(d)
                : d.length() == name.value.length() + 2 && d.startsWith(name.value, 1)
                    && d.charAt(0) == 'L' && d.charAt(d.length() - 1) == ';';
        }

        @Override public ConstantDesc constantValue() { return asSymbol(); }
        @Override public String toString() { return TAG_CLASS + " " + name.value; }
        @Override Object key() { return name.value; }
        @Override void write(BufWriterImpl out) { out.writeU1(TAG_CLASS); out.writeU2(name.index); }
    }

    static final class StringE extends Entry implements StringEntry {
        final Utf8E utf8;

        StringE(ConstantPool pool, int index, Utf8E utf8) {
            super(pool, index, TAG_STRING);
            this.utf8 = utf8;
        }

        @Override public Utf8Entry utf8() { return utf8; }
        @Override public String stringValue() { return utf8.value; }
        @Override public boolean equalsString(String text) { return utf8.value.equals(text); }
        @Override public ConstantDesc constantValue() { return utf8.value; }
        @Override public String toString() { return TAG_STRING + " \"" + utf8.value + "\""; }
        @Override Object key() { return utf8.value; }
        @Override void write(BufWriterImpl out) { out.writeU1(TAG_STRING); out.writeU2(utf8.index); }
    }

    static final class IntE extends Entry implements IntegerEntry {
        final int value;

        IntE(ConstantPool pool, int index, int value) {
            super(pool, index, TAG_INTEGER);
            this.value = value;
        }

        @Override public int intValue() { return value; }
        @Override public Integer constantValue() { return value; }
        @Override public TypeKind typeKind() { return TypeKind.INT; }
        @Override Object key() { return value; }
        @Override void write(BufWriterImpl out) { out.writeU1(TAG_INTEGER); out.writeInt(value); }
    }

    static final class FloatE extends Entry implements FloatEntry {
        final float value;

        FloatE(ConstantPool pool, int index, float value) {
            super(pool, index, TAG_FLOAT);
            this.value = value;
        }

        @Override public float floatValue() { return value; }
        @Override public Float constantValue() { return value; }
        @Override public TypeKind typeKind() { return TypeKind.FLOAT; }
        @Override Object key() { return Float.floatToRawIntBits(value); }
        @Override void write(BufWriterImpl out) { out.writeU1(TAG_FLOAT); out.writeFloat(value); }
    }

    static final class LongE extends Entry implements LongEntry {
        final long value;

        LongE(ConstantPool pool, int index, long value) {
            super(pool, index, TAG_LONG);
            this.value = value;
        }

        @Override public long longValue() { return value; }
        @Override public Long constantValue() { return value; }
        @Override public TypeKind typeKind() { return TypeKind.LONG; }
        @Override public int width() { return 2; }
        @Override Object key() { return value; }
        @Override void write(BufWriterImpl out) { out.writeU1(TAG_LONG); out.writeLong(value); }
    }

    static final class DoubleE extends Entry implements DoubleEntry {
        final double value;

        DoubleE(ConstantPool pool, int index, double value) {
            super(pool, index, TAG_DOUBLE);
            this.value = value;
        }

        @Override public double doubleValue() { return value; }
        @Override public Double constantValue() { return value; }
        @Override public TypeKind typeKind() { return TypeKind.DOUBLE; }
        @Override public int width() { return 2; }
        @Override Object key() { return Double.doubleToRawLongBits(value); }
        @Override void write(BufWriterImpl out) { out.writeU1(TAG_DOUBLE); out.writeDouble(value); }
    }

    static final class NameTypeE extends Entry implements NameAndTypeEntry {
        final Utf8E name;
        final Utf8E type;

        NameTypeE(ConstantPool pool, int index, Utf8E name, Utf8E type) {
            super(pool, index, TAG_NAME_AND_TYPE);
            this.name = name;
            this.type = type;
        }

        @Override public Utf8Entry name() { return name; }
        @Override public Utf8Entry type() { return type; }
        @Override public String toString() { return TAG_NAME_AND_TYPE + " " + name.value + "-" + type.value; }
        @Override Object key() { return List.of(name.value, type.value); }

        @Override
        void write(BufWriterImpl out) {
            out.writeU1(TAG_NAME_AND_TYPE);
            out.writeU2(name.index);
            out.writeU2(type.index);
        }
    }

    abstract static class RefE extends Entry implements MemberRefEntry {
        final ClassE owner;
        final NameTypeE nameAndType;

        RefE(ConstantPool pool, int index, int tag, ClassE owner, NameTypeE nameAndType) {
            super(pool, index, tag);
            this.owner = owner;
            this.nameAndType = nameAndType;
        }

        @Override public ClassEntry owner() { return owner; }
        @Override public NameAndTypeEntry nameAndType() { return nameAndType; }
        @Override public Utf8Entry name() { return nameAndType.name; }
        @Override public Utf8Entry type() { return nameAndType.type; }

        @Override
        public String toString() {
            return tag() + " " + owner.name.value + "." + nameAndType.name.value + "-" + nameAndType.type.value;
        }

        @Override Object key() { return List.of(owner.name.value, nameAndType.name.value, nameAndType.type.value); }

        @Override
        void write(BufWriterImpl out) {
            out.writeU1(tag());
            out.writeU2(owner.index);
            out.writeU2(nameAndType.index);
        }
    }

    static final class FieldRefE extends RefE implements FieldRefEntry {
        FieldRefE(ConstantPool pool, int index, ClassE owner, NameTypeE nt) {
            super(pool, index, TAG_FIELDREF, owner, nt);
        }

        @Override public ClassDesc typeSymbol() { return ClassDesc.ofDescriptor(nameAndType.type.value); }
    }

    static final class MethodRefE extends RefE implements MethodRefEntry {
        MethodRefE(ConstantPool pool, int index, ClassE owner, NameTypeE nt) {
            super(pool, index, TAG_METHODREF, owner, nt);
        }

        @Override public MethodTypeDesc typeSymbol() { return MethodTypeDesc.ofDescriptor(nameAndType.type.value); }
    }

    static final class InterfaceMethodRefE extends RefE implements InterfaceMethodRefEntry {
        InterfaceMethodRefE(ConstantPool pool, int index, ClassE owner, NameTypeE nt) {
            super(pool, index, TAG_INTERFACE_METHODREF, owner, nt);
        }

        @Override public MethodTypeDesc typeSymbol() { return MethodTypeDesc.ofDescriptor(nameAndType.type.value); }
    }

    static final class MethodTypeE extends Entry implements MethodTypeEntry {
        final Utf8E descriptor;

        MethodTypeE(ConstantPool pool, int index, Utf8E descriptor) {
            super(pool, index, TAG_METHOD_TYPE);
            this.descriptor = descriptor;
        }

        @Override public Utf8Entry descriptor() { return descriptor; }
        @Override public MethodTypeDesc asSymbol() { return MethodTypeDesc.ofDescriptor(descriptor.value); }
        @Override public boolean matches(MethodTypeDesc desc) { return descriptor.value.equals(desc.descriptorString()); }
        @Override public ConstantDesc constantValue() { return asSymbol(); }
        @Override public String toString() { return TAG_METHOD_TYPE + " " + descriptor.value; }
        @Override Object key() { return descriptor.value; }
        @Override void write(BufWriterImpl out) { out.writeU1(TAG_METHOD_TYPE); out.writeU2(descriptor.index); }
    }

    static final class MethodHandleE extends Entry implements MethodHandleEntry {
        final int kind;
        final RefE reference;

        MethodHandleE(ConstantPool pool, int index, int kind, RefE reference) {
            super(pool, index, TAG_METHOD_HANDLE);
            this.kind = kind;
            this.reference = reference;
        }

        @Override public int kind() { return kind; }
        @Override public MemberRefEntry reference() { return reference; }

        @Override
        public DirectMethodHandleDesc asSymbol() {
            ClassDesc owner = reference.owner.asSymbol();
            String name = reference.nameAndType.name.value;
            String type = reference.nameAndType.type.value;
            boolean iface = reference instanceof InterfaceMethodRefEntry;
            DirectMethodHandleDesc.Kind k = DirectMethodHandleDesc.Kind.valueOf(kind, iface);
            return MethodHandleDesc.of(k, owner, name, type);
        }

        @Override public ConstantDesc constantValue() { return asSymbol(); }
        @Override public String toString() { return TAG_METHOD_HANDLE + " " + kind + ":" + reference.toString().substring(reference.toString().indexOf(' ') + 1); }
        @Override Object key() { return List.of(kind, reference.key(), reference.tag()); }

        @Override
        void write(BufWriterImpl out) {
            out.writeU1(TAG_METHOD_HANDLE);
            out.writeU1(kind);
            out.writeU2(reference.index);
        }
    }

    static final class ModuleE extends Entry implements ModuleEntry {
        final Utf8E name;

        ModuleE(ConstantPool pool, int index, Utf8E name) {
            super(pool, index, TAG_MODULE);
            this.name = name;
        }

        @Override public Utf8Entry name() { return name; }
        @Override public ModuleDesc asSymbol() { return ModuleDesc.of(name.value); }
        @Override public boolean matches(ModuleDesc desc) { return name.value.equals(desc.name()); }
        @Override public String toString() { return TAG_MODULE + " " + name.value; }
        @Override Object key() { return name.value; }
        @Override void write(BufWriterImpl out) { out.writeU1(TAG_MODULE); out.writeU2(name.index); }
    }

    static final class PackageE extends Entry implements PackageEntry {
        final Utf8E name;

        PackageE(ConstantPool pool, int index, Utf8E name) {
            super(pool, index, TAG_PACKAGE);
            this.name = name;
        }

        @Override public Utf8Entry name() { return name; }
        @Override public PackageDesc asSymbol() { return PackageDesc.ofInternalName(name.value); }
        @Override public boolean matches(PackageDesc desc) { return name.value.equals(desc.internalName()); }
        @Override public String toString() { return TAG_PACKAGE + " " + name.value; }
        @Override Object key() { return name.value; }
        @Override void write(BufWriterImpl out) { out.writeU1(TAG_PACKAGE); out.writeU2(name.index); }
    }

    static final class BsmE implements BootstrapMethodEntry {
        final ConstantPool pool;
        final int index;
        final MethodHandleE handle;
        final List<LoadableConstantEntry> arguments;

        BsmE(ConstantPool pool, int index, MethodHandleE handle, List<LoadableConstantEntry> arguments) {
            this.pool = pool;
            this.index = index;
            this.handle = handle;
            this.arguments = List.copyOf(arguments);
        }

        @Override public List<LoadableConstantEntry> arguments() { return arguments; }
        @Override public MethodHandleEntry bootstrapMethod() { return handle; }
        @Override public ConstantPool constantPool() { return pool; }
        @Override public int bsmIndex() { return index; }

        Object key() {
            List<Object> k = new ArrayList<>();
            k.add(handle);
            k.addAll(arguments);
            return k;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof BsmE && handle.equals(((BsmE) o).handle) && arguments.equals(((BsmE) o).arguments);
        }

        @Override public int hashCode() { return handle.hashCode() * 31 + arguments.hashCode(); }
    }

    abstract static class DynE extends Entry implements DynamicConstantPoolEntry {
        final BsmE bsm;
        final NameTypeE nameAndType;

        DynE(ConstantPool pool, int index, int tag, BsmE bsm, NameTypeE nameAndType) {
            super(pool, index, tag);
            this.bsm = bsm;
            this.nameAndType = nameAndType;
        }

        @Override public Utf8Entry name() { return nameAndType.name; }
        @Override public Utf8Entry type() { return nameAndType.type; }
        @Override public BootstrapMethodEntry bootstrap() { return bsm; }
        @Override public NameAndTypeEntry nameAndType() { return nameAndType; }
        @Override public int bootstrapMethodIndex() { return bsm.index; }

        @Override
        public String toString() {
            return tag() + " " + bsm + "." + nameAndType.name.value + "-" + nameAndType.type.value;
        }

        @Override Object key() { return List.of(bsm.key(), nameAndType.name.value, nameAndType.type.value); }

        @Override
        void write(BufWriterImpl out) {
            out.writeU1(tag());
            out.writeU2(bsm.index);
            out.writeU2(nameAndType.index);
        }

        Object[] bootstrapArguments() {
            Object[] args = new Object[bsm.arguments.size()];
            for (int i = 0; i < args.length; i++) {
                args[i] = bsm.arguments.get(i).constantValue();
            }
            return args;
        }
    }

    static final class InvokeDynamicE extends DynE implements InvokeDynamicEntry {
        InvokeDynamicE(ConstantPool pool, int index, BsmE bsm, NameTypeE nt) {
            super(pool, index, TAG_INVOKE_DYNAMIC, bsm, nt);
        }

        @Override
        public DynamicCallSiteDesc asSymbol() {
            Object[] raw = bootstrapArguments();
            ConstantDesc[] args = new ConstantDesc[raw.length];
            for (int i = 0; i < raw.length; i++) {
                args[i] = (ConstantDesc) raw[i];
            }
            return DynamicCallSiteDesc.of(bsm.handle.asSymbol(), nameAndType.name.value, typeSymbol(), args);
        }

        @Override public MethodTypeDesc typeSymbol() { return MethodTypeDesc.ofDescriptor(nameAndType.type.value); }
    }

    static final class ConstantDynE extends DynE implements ConstantDynamicEntry {
        ConstantDynE(ConstantPool pool, int index, BsmE bsm, NameTypeE nt) {
            super(pool, index, TAG_DYNAMIC, bsm, nt);
        }

        @Override
        public DynamicConstantDesc<?> asSymbol() {
            Object[] raw = bootstrapArguments();
            ConstantDesc[] args = new ConstantDesc[raw.length];
            for (int i = 0; i < raw.length; i++) {
                args[i] = (ConstantDesc) raw[i];
            }
            return DynamicConstantDesc.ofNamed(bsm.handle.asSymbol(), nameAndType.name.value, typeSymbol(), args);
        }

        @Override public TypeKind typeKind() { return TypeKind.fromDescriptor(nameAndType.type.value); }
        @Override public ConstantDesc constantValue() { return asSymbol(); }
        @Override public ClassDesc typeSymbol() { return ClassDesc.ofDescriptor(nameAndType.type.value); }
    }
}
