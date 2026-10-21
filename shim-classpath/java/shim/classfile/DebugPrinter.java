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
import java.lang.classfile.AttributedElement;
import java.lang.classfile.ClassModel;
import java.lang.classfile.CodeModel;
import java.lang.classfile.CompoundElement;
import java.lang.classfile.FieldModel;
import java.lang.classfile.MethodModel;
import java.lang.classfile.attribute.CodeAttribute;
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.classfile.constantpool.ConstantPool;
import java.lang.classfile.constantpool.DoubleEntry;
import java.lang.classfile.constantpool.DynamicConstantPoolEntry;
import java.lang.classfile.constantpool.FloatEntry;
import java.lang.classfile.constantpool.IntegerEntry;
import java.lang.classfile.constantpool.LoadableConstantEntry;
import java.lang.classfile.constantpool.LongEntry;
import java.lang.classfile.constantpool.MemberRefEntry;
import java.lang.classfile.constantpool.MethodHandleEntry;
import java.lang.classfile.constantpool.MethodTypeEntry;
import java.lang.classfile.constantpool.ModuleEntry;
import java.lang.classfile.constantpool.NameAndTypeEntry;
import java.lang.classfile.constantpool.PackageEntry;
import java.lang.classfile.constantpool.PoolEntry;
import java.lang.classfile.constantpool.StringEntry;
import java.lang.classfile.constantpool.Utf8Entry;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.reflect.AccessFlag;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Renders class file models as the YAML like text of {@code CompoundElement.toDebugString}. */
public final class DebugPrinter {
    private DebugPrinter() { }

    public static String toDebugString(CompoundElement<?> element) {
        DebugNode root;
        if (element instanceof ClassModel) {
            root = classTree((ClassModel) element);
        } else if (element instanceof FieldModel) {
            root = fieldTree((FieldModel) element);
        } else if (element instanceof MethodModel) {
            root = methodTree((MethodModel) element);
        } else if (element instanceof CodeModel) {
            root = DebugCode.codeTree(asCode((CodeModel) element));
        } else {
            throw new IllegalStateException();
        }
        return DebugNode.render(root);
    }

    static CodeAttribute asCode(CodeModel code) {
        if (code == null || code instanceof CodeAttribute) {
            return (CodeAttribute) code;
        }
        throw new ClassCastException("class jdk.internal.classfile.impl.BufferedCodeBuilder$Model cannot be cast to"
                + " class " + CodeAttribute.class.getName() + " (jdk.internal.classfile.impl.BufferedCodeBuilder$Model"
                + " and " + CodeAttribute.class.getName() + " are in module java.base of loader 'bootstrap')");
    }

    static DebugNode flags(Set<AccessFlag> flags) {
        List<Object> names = new ArrayList<>();
        for (AccessFlag f : flags) {
            names.add(f.name());
        }
        return DebugNode.flowList("flags", names);
    }

    static DebugNode attributeNames(AttributedElement e) {
        List<Object> names = new ArrayList<>();
        for (Attribute<?> a : e.attributes()) {
            names.add(a.attributeName().stringValue());
        }
        return DebugNode.flowList("attributes", names);
    }

    static DebugNode classTree(ClassModel cm) {
        DebugNode n = DebugNode.blockMap("class");
        n.with("class name", cm.thisClass().asInternalName());
        n.with("version", cm.majorVersion() + "." + cm.minorVersion());
        n.with(flags(cm.flags().flags()));
        Optional<ClassEntry> sup = cm.superclass();
        n.with("superclass", sup.isPresent() ? sup.get().asInternalName() : "");
        List<Object> itfs = new ArrayList<>();
        for (ClassEntry ce : cm.interfaces()) {
            itfs.add(ce.asInternalName());
        }
        n.with(DebugNode.flowList("interfaces", itfs));
        n.with(attributeNames(cm));
        n.with(constantPoolTree(cm.constantPool()));
        n.withAll(DebugAttributes.attributes(cm.attributes()));
        DebugNode fields = DebugNode.blockList("fields");
        for (FieldModel f : cm.fields()) {
            fields.add(fieldTree(f));
        }
        n.with(fields);
        DebugNode methods = DebugNode.blockList("methods");
        for (MethodModel m : cm.methods()) {
            methods.add(methodTree(m));
        }
        n.with(methods);
        return n;
    }

    static DebugNode fieldTree(FieldModel f) {
        DebugNode n = DebugNode.blockMap("field");
        n.with("field name", f.fieldName().stringValue());
        n.with(flags(f.flags().flags()));
        n.with("field type", f.fieldType().stringValue());
        n.with(attributeNames(f));
        n.withAll(DebugAttributes.attributes(f.attributes()));
        return n;
    }

    static DebugNode methodTree(MethodModel m) {
        DebugNode n = DebugNode.blockMap("method");
        n.with("method name", m.methodName().stringValue());
        n.with(flags(m.flags().flags()));
        n.with("method type", m.methodType().stringValue());
        n.with(attributeNames(m));
        n.withAll(DebugAttributes.attributes(m.attributes()));
        Optional<CodeModel> code = m.code();
        CodeAttribute ca = asCode(code.isPresent() ? code.get() : null);
        if (ca != null) {
            n.with(DebugCode.codeTree(ca));
        }
        return n;
    }

    private static DebugNode constantPoolTree(ConstantPool cp) {
        DebugNode pool = DebugNode.blockMap("constant pool");
        for (PoolEntry e : cp) {
            DebugNode n = DebugNode.flowMap(e.index());
            n.with("tag", tagName(e.tag()));
            entryDetails(n, e);
            pool.with(n);
        }
        return pool;
    }

    private static String tagName(int tag) {
        switch (tag) {
            case PoolEntry.TAG_UTF8: return "Utf8";
            case PoolEntry.TAG_INTEGER: return "Integer";
            case PoolEntry.TAG_FLOAT: return "Float";
            case PoolEntry.TAG_LONG: return "Long";
            case PoolEntry.TAG_DOUBLE: return "Double";
            case PoolEntry.TAG_CLASS: return "Class";
            case PoolEntry.TAG_STRING: return "String";
            case PoolEntry.TAG_FIELDREF: return "Fieldref";
            case PoolEntry.TAG_METHODREF: return "Methodref";
            case PoolEntry.TAG_INTERFACE_METHODREF: return "InterfaceMethodref";
            case PoolEntry.TAG_NAME_AND_TYPE: return "NameAndType";
            case PoolEntry.TAG_METHOD_HANDLE: return "MethodHandle";
            case PoolEntry.TAG_METHOD_TYPE: return "MethodType";
            case PoolEntry.TAG_DYNAMIC: return "Dynamic";
            case PoolEntry.TAG_INVOKE_DYNAMIC: return "InvokeDynamic";
            case PoolEntry.TAG_MODULE: return "Module";
            case PoolEntry.TAG_PACKAGE: return "Package";
            default: throw new IllegalStateException();
        }
    }

    private static void entryDetails(DebugNode n, PoolEntry e) {
        if (e instanceof Utf8Entry) {
            n.with("value", ((Utf8Entry) e).stringValue());
        } else if (e instanceof ClassEntry) {
            ClassEntry ce = (ClassEntry) e;
            n.with("class name index", ce.name().index());
            n.with("class internal name", ce.asInternalName());
        } else if (e instanceof ModuleEntry) {
            ModuleEntry me = (ModuleEntry) e;
            n.with("module name index", me.name().index());
            n.with("module name", me.name().stringValue());
        } else if (e instanceof PackageEntry) {
            PackageEntry pe = (PackageEntry) e;
            n.with("package name index", pe.name().index());
            n.with("package name", pe.name().stringValue());
        } else if (e instanceof StringEntry) {
            StringEntry se = (StringEntry) e;
            n.with("value index", se.utf8().index());
            n.with("value", se.stringValue());
        } else if (e instanceof MemberRefEntry) {
            MemberRefEntry mre = (MemberRefEntry) e;
            n.with("owner index", mre.owner().index());
            n.with("name and type index", mre.nameAndType().index());
            n.with("owner", mre.owner().asInternalName());
            n.with("name", mre.name().stringValue());
            n.with("type", mre.type().stringValue());
        } else if (e instanceof NameAndTypeEntry) {
            NameAndTypeEntry nte = (NameAndTypeEntry) e;
            n.with("name index", nte.name().index());
            n.with("type index", nte.type().index());
            n.with("name", nte.name().stringValue());
            n.with("type", nte.type().stringValue());
        } else if (e instanceof MethodHandleEntry) {
            MethodHandleEntry mhe = (MethodHandleEntry) e;
            n.with("reference kind", DirectMethodHandleDesc.Kind.valueOf(mhe.kind()).name());
            n.with("reference index", mhe.reference().index());
            n.with("owner", mhe.reference().owner().asInternalName());
            n.with("name", mhe.reference().name().stringValue());
            n.with("type", mhe.reference().type().stringValue());
        } else if (e instanceof MethodTypeEntry) {
            MethodTypeEntry mte = (MethodTypeEntry) e;
            n.with("descriptor index", mte.descriptor().index());
            n.with("descriptor", mte.descriptor().stringValue());
        } else if (e instanceof DynamicConstantPoolEntry) {
            DynamicConstantPoolEntry de = (DynamicConstantPoolEntry) e;
            n.with("bootstrap method handle index", de.bootstrap().bootstrapMethod().index());
            List<Object> args = new ArrayList<>();
            for (LoadableConstantEntry a : de.bootstrap().arguments()) {
                args.add(a.index());
            }
            n.with(DebugNode.flowList("bootstrap method arguments indexes", args));
            n.with("name and type index", de.nameAndType().index());
            n.with("name", de.name().stringValue());
            n.with("type", de.type().stringValue());
        } else if (e instanceof IntegerEntry) {
            n.with("value", String.valueOf(((IntegerEntry) e).intValue()));
        } else if (e instanceof FloatEntry) {
            n.with("value", String.valueOf(((FloatEntry) e).floatValue()));
        } else if (e instanceof LongEntry) {
            n.with("value", String.valueOf(((LongEntry) e).longValue()));
        } else if (e instanceof DoubleEntry) {
            n.with("value", String.valueOf(((DoubleEntry) e).doubleValue()));
        }
    }
}
