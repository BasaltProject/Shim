/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

/** A verification type of the type checking verifier, with the names its messages use. */
final class VerifyType {
    static final int BOGUS = 0;
    static final int INTEGER = 1;
    static final int FLOAT = 2;
    static final int LONG = 3;
    static final int LONG2 = 4;
    static final int DOUBLE = 5;
    static final int DOUBLE2 = 6;
    static final int NULL = 7;
    static final int UNINIT_THIS = 8;
    static final int UNINIT = 9;
    static final int REF = 10;
    static final int BOOLEAN = 11;
    static final int BYTE = 12;
    static final int CHAR = 13;
    static final int SHORT = 14;
    static final int CAT1_CHECK = 15;
    static final int CAT2_CHECK = 16;
    static final int CAT2_2ND_CHECK = 17;
    static final int REF_CHECK = 18;

    static final VerifyType T_BOGUS = new VerifyType(BOGUS, null, 0);
    static final VerifyType T_INTEGER = new VerifyType(INTEGER, null, 0);
    static final VerifyType T_FLOAT = new VerifyType(FLOAT, null, 0);
    static final VerifyType T_LONG = new VerifyType(LONG, null, 0);
    static final VerifyType T_LONG2 = new VerifyType(LONG2, null, 0);
    static final VerifyType T_DOUBLE = new VerifyType(DOUBLE, null, 0);
    static final VerifyType T_DOUBLE2 = new VerifyType(DOUBLE2, null, 0);
    static final VerifyType T_NULL = new VerifyType(NULL, null, 0);
    static final VerifyType T_UNINIT_THIS = new VerifyType(UNINIT_THIS, null, 0);
    static final VerifyType T_BOOLEAN = new VerifyType(BOOLEAN, null, 0);
    static final VerifyType T_BYTE = new VerifyType(BYTE, null, 0);
    static final VerifyType T_CHAR = new VerifyType(CHAR, null, 0);
    static final VerifyType T_SHORT = new VerifyType(SHORT, null, 0);
    static final VerifyType T_CAT1_CHECK = new VerifyType(CAT1_CHECK, null, 0);
    static final VerifyType T_CAT2_CHECK = new VerifyType(CAT2_CHECK, null, 0);
    static final VerifyType T_CAT2_2ND_CHECK = new VerifyType(CAT2_2ND_CHECK, null, 0);
    static final VerifyType T_REF_CHECK = new VerifyType(REF_CHECK, null, 0);
    static final VerifyType T_OBJECT = ref("java/lang/Object");
    static final VerifyType T_STRING = ref("java/lang/String");
    static final VerifyType T_CLASS = ref("java/lang/Class");
    static final VerifyType T_THROWABLE = ref("java/lang/Throwable");

    final int kind;
    final String name;
    final int bci;

    private VerifyType(int kind, String name, int bci) {
        this.kind = kind;
        this.name = name;
        this.bci = bci;
    }

    static VerifyType ref(String name) {
        return new VerifyType(REF, name, 0);
    }

    static VerifyType uninit(int bci) {
        return new VerifyType(UNINIT, null, bci);
    }

    /** The type of a value of the given field descriptor once on the stack or in a local. */
    static VerifyType ofDescriptor(String d) {
        switch (d.charAt(0)) {
            case 'Z':
            case 'B':
            case 'C':
            case 'S':
            case 'I':
                return T_INTEGER;
            case 'F':
                return T_FLOAT;
            case 'J':
                return T_LONG;
            case 'D':
                return T_DOUBLE;
            case 'L':
                return ref(d.substring(1, d.length() - 1));
            default:
                return ref(d);
        }
    }

    /** The type of a method's return descriptor, keeping the small integral types apart. */
    static VerifyType ofReturn(String d) {
        switch (d.charAt(0)) {
            case 'Z': return T_BOOLEAN;
            case 'B': return T_BYTE;
            case 'C': return T_CHAR;
            case 'S': return T_SHORT;
            default: return ofDescriptor(d);
        }
    }

    boolean isArray() {
        return kind == REF && name.charAt(0) == '[';
    }

    boolean isReference() {
        return kind == REF || kind == NULL || kind == UNINIT || kind == UNINIT_THIS;
    }

    boolean isCategory2() {
        return kind == LONG || kind == DOUBLE;
    }

    boolean isCategory1() {
        switch (kind) {
            case INTEGER:
            case FLOAT:
            case NULL:
            case UNINIT_THIS:
            case UNINIT:
            case REF:
            case BOOLEAN:
            case BYTE:
            case CHAR:
            case SHORT:
            case CAT1_CHECK:
                return true;
            default:
                return false;
        }
    }

    boolean isIntegral() {
        return kind == INTEGER || kind == BOOLEAN || kind == BYTE || kind == CHAR || kind == SHORT;
    }

    /** The second slot type of a category 2 type. */
    VerifyType secondHalf() {
        return kind == LONG ? T_LONG2 : T_DOUBLE2;
    }

    /** The component type of an array type, with small integral components kept apart. */
    VerifyType component() {
        char c = name.charAt(1);
        switch (c) {
            case 'Z': return T_BOOLEAN;
            case 'B': return T_BYTE;
            case 'C': return T_CHAR;
            case 'S': return T_SHORT;
            case 'I': return T_INTEGER;
            case 'F': return T_FLOAT;
            case 'J': return T_LONG;
            case 'D': return T_DOUBLE;
            case 'L': return ref(name.substring(2, name.length() - 1));
            default: return ref(name.substring(1));
        }
    }

    /** The array type whose components are of this reference type. */
    VerifyType arrayOf() {
        return ref(name.charAt(0) == '[' ? "[" + name : "[L" + name + ";");
    }

    int dimensions() {
        int n = 0;
        while (n < name.length() && name.charAt(n) == '[') {
            n++;
        }
        return n;
    }

    /** Whether a value of type {@code from} may be used where this type is expected. */
    boolean isAssignableFrom(VerifyType from, VerifyHierarchy h) {
        if (equals(from) || kind == BOGUS) {
            return true;
        }
        switch (kind) {
            case CAT1_CHECK:
                return from.isCategory1();
            case CAT2_CHECK:
                return from.kind == LONG || from.kind == DOUBLE;
            case CAT2_2ND_CHECK:
                return from.kind == LONG2 || from.kind == DOUBLE2;
            case REF_CHECK:
                return from.isReference();
            case INTEGER:
            case BOOLEAN:
            case BYTE:
            case CHAR:
            case SHORT:
                return from.isIntegral();
            case REF:
                if (from.kind == NULL) {
                    return true;
                }
                return from.kind == REF && h.isReferenceAssignable(this, from);
            default:
                return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof VerifyType)) {
            return false;
        }
        VerifyType t = (VerifyType) o;
        return t.kind == kind && t.bci == bci && (name == null ? t.name == null : name.equals(t.name));
    }

    @Override
    public int hashCode() {
        return kind * 31 + bci + (name == null ? 0 : name.hashCode());
    }

    @Override
    public String toString() {
        switch (kind) {
            case BOGUS: return "bogus_type";
            case INTEGER: return "integer_type";
            case FLOAT: return "float_type";
            case LONG: return "long_type";
            case LONG2: return "long2_type";
            case DOUBLE: return "double_type";
            case DOUBLE2: return "double2_type";
            case NULL: return "null_type";
            case UNINIT_THIS: return "uninit@65535";
            case UNINIT: return "uninit@" + bci;
            case BOOLEAN: return "boolean_type";
            case BYTE: return "byte_type";
            case CHAR: return "char_type";
            case SHORT: return "short_type";
            case CAT1_CHECK: return "category1_check";
            case CAT2_CHECK: return "category2_check";
            case CAT2_2ND_CHECK: return "category2_2nd_check";
            case REF_CHECK: return "reference_check";
            default: return name;
        }
    }
}
