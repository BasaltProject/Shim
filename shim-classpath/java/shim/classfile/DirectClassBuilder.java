/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.AccessFlags;
import java.lang.classfile.Attribute;
import java.lang.classfile.Attributes;
import java.lang.classfile.ClassBuilder;
import java.lang.classfile.ClassElement;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassFileVersion;
import java.lang.classfile.CodeModel;
import java.lang.classfile.FieldModel;
import java.lang.classfile.Interfaces;
import java.lang.classfile.MethodModel;
import java.lang.classfile.Superclass;
import java.lang.classfile.attribute.BootstrapMethodsAttribute;
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.classfile.constantpool.ConstantPoolBuilder;
import java.lang.constant.ConstantDescs;
import java.util.ArrayList;
import java.util.List;
import shim.classfile.Entries.BsmE;

/** The class builder that serializes directly: members are written as they arrive, class attributes at the end. */
public final class DirectClassBuilder extends AbstractClassBuilder {
    private final ClassFileImpl classFile;
    private final PoolImpl pool;
    private final ClassEntry thisClass;
    private int flags = ClassFile.ACC_PUBLIC | ClassFile.ACC_SUPER;
    private int major = ClassFile.latestMajorVersion();
    private int minor = ClassFile.latestMinorVersion();
    private ClassEntry superclass;
    private List<ClassEntry> interfaces = List.of();
    private final BufWriterImpl fieldBytes;
    private final BufWriterImpl methodBytes;
    private final List<FieldModel> fields = new ArrayList<>();
    private final List<MethodModel> methods = new ArrayList<>();
    private final List<Attribute<?>> attributes = new ArrayList<>();

    DirectClassBuilder(ClassFileImpl classFile, PoolImpl pool, ClassEntry thisClass) {
        this.classFile = classFile;
        this.pool = pool;
        this.thisClass = pool.classEntry(thisClass.name());
        this.fieldBytes = new BufWriterImpl(pool);
        this.methodBytes = new BufWriterImpl(pool);
    }

    @Override ClassFileImpl classFile() { return classFile; }
    @Override ClassEntry thisClass() { return thisClass; }
    @Override public ConstantPoolBuilder constantPool() { return pool; }

    @Override
    public ClassBuilder with(ClassElement element) {
        if (element instanceof AccessFlags) {
            flags = ((AccessFlags) element).flagsMask();
        } else if (element instanceof ClassFileVersion) {
            major = ((ClassFileVersion) element).majorVersion();
            minor = ((ClassFileVersion) element).minorVersion();
        } else if (element instanceof Superclass) {
            superclass = ((Superclass) element).superclassEntry();
        } else if (element instanceof Interfaces) {
            interfaces = ((Interfaces) element).interfaces();
        } else if (element instanceof FieldModel) {
            fields.add((FieldModel) element);
        } else if (element instanceof MethodModel) {
            methods.add((MethodModel) element);
        } else if (element instanceof Attribute<?>) {
            Attribute<?> a = (Attribute<?>) element;
            if (!a.attributeMapper().allowMultiple()) {
                attributes.removeIf(o -> o.attributeMapper() == a.attributeMapper());
            }
            attributes.add(a);
        } else {
            throw new IllegalArgumentException("Unsupported class element: " + element);
        }
        return this;
    }

    private void writeField(FieldModel f) {
        BufWriterImpl out = fieldBytes;
        out.writeU2(f.flags().flagsMask());
        out.writeIndex(f.fieldName());
        out.writeIndex(f.fieldType());
        List<Attribute<?>> attrs = new ArrayList<>();
        f.forEach(e -> {
            if (e instanceof Attribute<?>) {
                attrs.add((Attribute<?>) e);
            }
        });
        writeAttributes(out, attrs, null);
    }

    private void writeMethod(MethodModel m) {
        BufWriterImpl out = methodBytes;
        out.writeU2(m.flags().flagsMask());
        out.writeIndex(m.methodName());
        out.writeIndex(m.methodType());
        List<Object> attrs = new ArrayList<>();
        m.forEach(e -> {
            if (e instanceof Attribute<?> || e instanceof CodeModel) {
                attrs.add(e);
            }
        });
        MethodContext ctx = new MethodContext(classFile, pool, thisClass, m.methodName(), m.methodType(),
            m.flags().flagsMask());
        ctx.setMajorVersion(major);
        writeAttributes(out, attrs, ctx);
    }

    private void writeAttributes(BufWriterImpl out, List<?> attrs, MethodContext ctx) {
        int countPos = out.size();
        out.writeU2(0);
        int count = 0;
        for (Object o : attrs) {
            if (AttrWriter.write(out, classFile, o, ctx)) {
                count++;
            }
        }
        out.patchInt(countPos, 2, count);
    }

    byte[] finish() {
        BufWriterImpl body = new BufWriterImpl(pool);
        ClassEntry sup = superclass;
        if (sup != null) {
            sup = (ClassEntry) pool.adopt(sup);
        } else if ((flags & ClassFile.ACC_MODULE) == 0 && !thisClass.asInternalName().equals("java/lang/Object")) {
            sup = pool.classEntry(ConstantDescs.CD_Object);
        }
        List<ClassEntry> ifaces = new ArrayList<>(interfaces.size());
        for (ClassEntry c : interfaces) {
            ifaces.add((ClassEntry) pool.adopt(c));
        }
        for (FieldModel f : fields) {
            writeField(f);
        }
        for (MethodModel m : methods) {
            writeMethod(m);
        }
        body.writeU2(flags);
        body.writeIndex(thisClass);
        body.writeIndexOrZero(sup);
        body.writeU2(ifaces.size());
        for (ClassEntry i : ifaces) {
            body.writeIndex(i);
        }
        body.writeU2(fields.size());
        body.writeBuf(fieldBytes);
        body.writeU2(methods.size());
        body.writeBuf(methodBytes);
        List<Object> all = new ArrayList<>(attributes);
        int countPos = body.size();
        body.writeU2(0);
        int count = 0;
        for (Object o : all) {
            if (AttrWriter.write(body, classFile, o, null)) {
                count++;
            }
        }
        if (!pool.bootstrapMethods().isEmpty()) {
            AttrWriter.writeBootstrapMethods(body, pool);
            count++;
        }
        body.patchInt(countPos, 2, count);

        BufWriterImpl out = new BufWriterImpl(pool, body.size() + 1024);
        out.writeInt(ClassFile.MAGIC_NUMBER);
        out.writeU2(minor);
        out.writeU2(major);
        pool.writeTo(out);
        out.writeBuf(body);
        return out.toByteArray();
    }
}
