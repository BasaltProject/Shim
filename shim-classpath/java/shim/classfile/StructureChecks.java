/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.Attributes;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.FieldModel;
import java.lang.classfile.MethodModel;
import java.lang.classfile.Attribute;
import java.lang.classfile.attribute.CodeAttribute;
import java.lang.classfile.attribute.ConstantValueAttribute;
import java.lang.classfile.constantpool.Utf8Entry;
import java.lang.classfile.constantpool.ConstantValueEntry;
import java.lang.classfile.constantpool.DoubleEntry;
import java.lang.classfile.constantpool.FloatEntry;
import java.lang.classfile.constantpool.IntegerEntry;
import java.lang.classfile.constantpool.LongEntry;
import java.lang.classfile.constantpool.StringEntry;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Format checks of the class structure that do not need the bytecode. */
final class StructureChecks {
    private StructureChecks() { }

    static void check(ClassModel model, List<VerifyError> errors) {
        String cls = model.thisClass().asSymbol().displayName();
        Set<String> seen = new HashSet<>();
        for (FieldModel f : model.fields()) {
            String name = f.fieldName().stringValue();
            String type = f.fieldType().stringValue();
            if (!isUnqualifiedName(name, false)) {
                errors.add(new VerifyError("Illegal field name " + name + " in class " + cls));
            }
            if (!seen.add(name + ":" + type)) {
                errors.add(new VerifyError("Duplicate field name " + name + " with signature " + type
                    + " in class " + cls));
            }
        }
        seen.clear();
        for (MethodModel m : model.methods()) {
            String name = m.methodName().stringValue();
            String type = m.methodType().stringValue();
            int flags = m.flags().flagsMask();
            if (name.equals("<clinit>") && (flags & ClassFile.ACC_STATIC) == 0) {
                errors.add(new VerifyError("Method <clinit> is not static in class " + cls));
            }
            if (!name.equals("<init>") && !name.equals("<clinit>") && !isUnqualifiedName(name, true)) {
                errors.add(new VerifyError("Illegal method name " + name + " in class " + cls));
            }
            if (!seen.add(name + ":" + type)) {
                errors.add(new VerifyError("Duplicate method name " + name + " with signature " + type
                    + " in class " + cls));
            }
        }
        if (!(model.constantPool() instanceof ClassReaderImpl)) {
            return;
        }
        ClassReaderImpl r = (ClassReaderImpl) model.constantPool();
        checkTable(r, r.classAttributesOffset(), model.attributes(), "class " + cls, errors);
        int p = r.fieldsOffset() + 2;
        for (FieldModel f : model.fields()) {
            String where = "field " + cls + "." + f.fieldName().stringValue();
            checkTable(r, p + 6, f.attributes(), where, errors);
            Optional<ConstantValueAttribute> cv = f.findAttribute(Attributes.constantValue());
            if (cv.isPresent() && !constantMatches(cv.get().constant(), f.fieldType().stringValue())) {
                errors.add(new VerifyError("Bad constant value type in " + where));
            }
            p = r.skipAttributes(p + 6);
        }
        p = r.methodsOffset() + 2;
        for (MethodModel m : model.methods()) {
            String name = m.methodName().stringValue();
            String display = cls + "::" + name + displayParameters(m.methodType().stringValue());
            int table = p + 6;
            checkTable(r, table, m.attributes(), "method " + display, errors);
            int flags = m.flags().flagsMask();
            int n = r.readU2(table);
            int q = table + 2;
            for (int i = 0; i < n; i++) {
                int length = r.readInt(q + 2);
                if (attributeName(r, q).equals("Code")) {
                    if ((flags & (ClassFile.ACC_ABSTRACT | ClassFile.ACC_NATIVE)) != 0) {
                        errors.add(new VerifyError("Code attribute in native or abstract method " + display));
                    }
                    checkCode(r, q + 6, length, m, display, errors);
                }
                q += 6 + length;
            }
            p = r.skipAttributes(table);
        }
    }

    private static void checkCode(ClassReaderImpl r, int body, int length, MethodModel m, String display,
                                  List<VerifyError> errors) {
        int codeLength = r.readInt(body + 4);
        int handlers = body + 8 + codeLength;
        int table = handlers + 2 + 8 * r.readU2(handlers);
        int expected = table + 2 - body;
        int n = r.readU2(table);
        int q = table + 2;
        for (int i = 0; i < n; i++) {
            int l = r.readInt(q + 2);
            expected += 6 + l;
            q += 6 + l;
        }
        if (expected != length) {
            errors.add(new VerifyError("Wrong Code attribute length in method " + display));
        }
        Optional<CodeAttribute> code = m.findAttribute(Attributes.code());
        if (code.isPresent()) {
            checkTable(r, table, code.get().attributes(), "Code attribute for method " + display, errors);
        }
    }

    private static String attributeName(ClassReaderImpl r, int p) {
        return ((Utf8Entry) r.entryByIndex(r.readU2(p))).stringValue();
    }

    /** Compares the declared length of each attribute of the table at {@code p} with what its contents need. */
    private static void checkTable(ClassReaderImpl r, int p, List<Attribute<?>> attributes, String where,
                                   List<VerifyError> errors) {
        int n = r.readU2(p);
        int q = p + 2;
        int next = 0;
        for (int i = 0; i < n; i++) {
            String name = attributeName(r, q);
            int length = r.readInt(q + 2);
            for (int k = next; k < attributes.size(); k++) {
                Attribute<?> a = attributes.get(k);
                if (a.attributeName().equalsString(name)) {
                    next = k + 1;
                    int expected = AttributeSizes.of(a);
                    if (expected >= 0 && expected != length) {
                        errors.add(new VerifyError("Wrong " + name + " attribute length in " + where));
                    }
                    break;
                }
            }
            q += 6 + length;
        }
    }

    static String displayParameters(String type) {
        java.lang.constant.MethodTypeDesc mt = java.lang.constant.MethodTypeDesc.ofDescriptor(type);
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < mt.parameterCount(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(mt.parameterType(i).displayName());
        }
        return sb.append(')').toString();
    }

    private static boolean constantMatches(ConstantValueEntry e, String type) {
        switch (type) {
            case "J": return e instanceof LongEntry;
            case "F": return e instanceof FloatEntry;
            case "D": return e instanceof DoubleEntry;
            case "I": case "S": case "C": case "B": case "Z": return e instanceof IntegerEntry;
            case "Ljava/lang/String;": return e instanceof StringEntry;
            default: return false;
        }
    }

    private static boolean isUnqualifiedName(String name, boolean method) {
        if (name.isEmpty()) {
            return false;
        }
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c == '.' || c == ';' || c == '[' || c == '/' || method && (c == '<' || c == '>')) {
                return false;
            }
        }
        return true;
    }
}
