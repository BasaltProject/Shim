/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.Attribute;
import java.lang.classfile.Attributes;
import java.lang.classfile.ClassElement;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.CodeModel;
import java.lang.classfile.FieldElement;
import java.lang.classfile.FieldModel;
import java.lang.classfile.MethodElement;
import java.lang.classfile.MethodModel;
import java.lang.classfile.AccessFlags;
import java.lang.classfile.attribute.CodeAttribute;
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.classfile.constantpool.ConstantPool;
import java.lang.classfile.constantpool.Utf8Entry;
import java.lang.reflect.AccessFlag;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import shim.classfile.Elements.*;

/** A class parsed from bytes; members and attributes are decoded on first use. */
public final class ClassModelImpl implements ClassModel {
    private final ClassReaderImpl reader;
    private List<FieldModel> fields;
    private List<MethodModel> methods;
    private List<Attribute<?>> attributes;
    private List<ClassEntry> interfaces;

    public ClassModelImpl(ClassReaderImpl reader) {
        this.reader = reader;
    }

    @Override public AccessFlags flags() { return new AccessFlagsImpl(AccessFlag.Location.CLASS, reader.flags()); }
    @Override public ClassEntry thisClass() { return reader.thisClassEntry(); }
    @Override public Optional<ClassEntry> superclass() { return reader.superclassEntry(); }
    @Override public ConstantPool constantPool() { return reader; }
    @Override public int majorVersion() { return reader.readU2(6); }
    @Override public int minorVersion() { return reader.readU2(4); }
    @Override public boolean isModuleInfo() { return (reader.flags() & ClassFile.ACC_MODULE) != 0; }

    @Override
    public List<ClassEntry> interfaces() {
        if (interfaces == null) {
            int p = reader.interfacesOffset();
            int n = reader.readU2(p);
            List<ClassEntry> out = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                out.add(reader.readEntry(p + 2 + 2 * i, ClassEntry.class));
            }
            interfaces = Collections.unmodifiableList(out);
        }
        return interfaces;
    }

    @Override
    public List<FieldModel> fields() {
        if (fields == null) {
            int p = reader.fieldsOffset();
            int n = reader.readU2(p);
            p += 2;
            List<FieldModel> out = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                out.add(new FieldImpl(this, reader, p));
                p = reader.skipAttributes(p + 6);
            }
            fields = Collections.unmodifiableList(out);
        }
        return fields;
    }

    @Override
    public List<MethodModel> methods() {
        if (methods == null) {
            int p = reader.methodsOffset();
            int n = reader.readU2(p);
            p += 2;
            List<MethodModel> out = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                out.add(new MethodImpl(this, reader, p));
                p = reader.skipAttributes(p + 6);
            }
            methods = Collections.unmodifiableList(out);
        }
        return methods;
    }

    @Override
    public List<Attribute<?>> attributes() {
        if (attributes == null) {
            attributes = Collections.unmodifiableList(reader.readAttributes(this, reader.classAttributesOffset()));
        }
        return attributes;
    }

    @Override
    public void forEach(Consumer<? super ClassElement> consumer) {
        consumer.accept(flags());
        consumer.accept(new ClassFileVersionImpl(majorVersion(), minorVersion()));
        thisClassSuper(consumer);
        consumer.accept(new InterfacesImpl(interfaces()));
        for (FieldModel f : fields()) {
            consumer.accept(f);
        }
        for (MethodModel m : methods()) {
            consumer.accept(m);
        }
        for (Attribute<?> a : attributes()) {
            if (a instanceof ClassElement) {
                consumer.accept((ClassElement) a);
            }
        }
    }

    private void thisClassSuper(Consumer<? super ClassElement> consumer) {
        ClassEntry sup = reader.superclassEntry().orElse(null);
        if (sup != null) {
            consumer.accept(new SuperclassImpl(sup));
        }
    }

    @Override
    public String toString() {
        return "ClassModel[thisClass=" + thisClass().name().stringValue() + ", flags=" + reader.flags() + "]";
    }

    static final class FieldImpl implements FieldModel {
        private final ClassModelImpl parent;
        private final ClassReaderImpl reader;
        private final int pos;
        private List<Attribute<?>> attributes;

        FieldImpl(ClassModelImpl parent, ClassReaderImpl reader, int pos) {
            this.parent = parent;
            this.reader = reader;
            this.pos = pos;
        }

        @Override public Optional<ClassModel> parent() { return Optional.ofNullable(parent); }
        @Override public AccessFlags flags() { return new AccessFlagsImpl(AccessFlag.Location.FIELD, reader.readU2(pos)); }
        @Override public Utf8Entry fieldName() { return reader.readEntry(pos + 2, Utf8Entry.class); }
        @Override public Utf8Entry fieldType() { return reader.readEntry(pos + 4, Utf8Entry.class); }

        @Override
        public List<Attribute<?>> attributes() {
            if (attributes == null) {
                attributes = Collections.unmodifiableList(reader.readAttributes(this, pos + 6));
            }
            return attributes;
        }

        @Override
        public void forEach(Consumer<? super FieldElement> consumer) {
            consumer.accept(flags());
            for (Attribute<?> a : attributes()) {
                if (a instanceof FieldElement) {
                    consumer.accept((FieldElement) a);
                }
            }
        }

        @Override
        public String toString() {
            return "FieldModel[fieldName=" + fieldName().stringValue() + ", fieldType=" + fieldType().stringValue()
                + ", flags=" + reader.readU2(pos) + "]";
        }
    }

    static final class MethodImpl implements MethodModel {
        private final ClassModelImpl parent;
        private final ClassReaderImpl reader;
        private final int pos;
        private List<Attribute<?>> attributes;

        MethodImpl(ClassModelImpl parent, ClassReaderImpl reader, int pos) {
            this.parent = parent;
            this.reader = reader;
            this.pos = pos;
        }

        @Override public Optional<ClassModel> parent() { return Optional.ofNullable(parent); }
        @Override public AccessFlags flags() { return new AccessFlagsImpl(AccessFlag.Location.METHOD, reader.readU2(pos)); }
        @Override public Utf8Entry methodName() { return reader.readEntry(pos + 2, Utf8Entry.class); }
        @Override public Utf8Entry methodType() { return reader.readEntry(pos + 4, Utf8Entry.class); }

        @Override
        public Optional<CodeModel> code() {
            return findAttribute(Attributes.code()).map(c -> (CodeModel) c);
        }

        @Override
        public List<Attribute<?>> attributes() {
            if (attributes == null) {
                attributes = Collections.unmodifiableList(reader.readAttributes(this, pos + 6));
            }
            return attributes;
        }

        @Override
        public void forEach(Consumer<? super MethodElement> consumer) {
            consumer.accept(flags());
            for (Attribute<?> a : attributes()) {
                if (a instanceof MethodElement) {
                    consumer.accept((MethodElement) a);
                }
            }
        }

        @Override
        public String toString() {
            return "MethodModel[methodName=" + methodName().stringValue() + ", methodType="
                + methodType().stringValue() + ", flags=" + reader.readU2(pos) + "]";
        }
    }
}
