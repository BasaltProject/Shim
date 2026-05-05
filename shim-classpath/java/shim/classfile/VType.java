/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

/** A verification type of one local or stack slot while stack maps are computed. */
final class VType {
    static final int TOP = 0;
    static final int INTEGER = 1;
    static final int FLOAT = 2;
    static final int DOUBLE = 3;
    static final int LONG = 4;
    static final int NULL = 5;
    static final int UNINIT_THIS = 6;
    static final int OBJECT = 7;
    static final int UNINIT = 8;
    static final int LONG2 = 9;
    static final int DOUBLE2 = 10;

    static final VType T_TOP = new VType(TOP, null, -1);
    static final VType T_INTEGER = new VType(INTEGER, null, -1);
    static final VType T_FLOAT = new VType(FLOAT, null, -1);
    static final VType T_DOUBLE = new VType(DOUBLE, null, -1);
    static final VType T_DOUBLE2 = new VType(DOUBLE2, null, -1);
    static final VType T_LONG = new VType(LONG, null, -1);
    static final VType T_LONG2 = new VType(LONG2, null, -1);
    static final VType T_NULL = new VType(NULL, null, -1);
    static final VType T_UNINIT_THIS = new VType(UNINIT_THIS, null, -1);

    static final String OBJECT_NAME = "java/lang/Object";
    static final VType T_OBJECT = new VType(OBJECT, OBJECT_NAME, -1);
    static final VType T_STRING = new VType(OBJECT, "java/lang/String", -1);
    static final VType T_CLASS = new VType(OBJECT, "java/lang/Class", -1);
    static final VType T_THROWABLE = new VType(OBJECT, "java/lang/Throwable", -1);
    static final VType T_METHOD_TYPE = new VType(OBJECT, "java/lang/invoke/MethodType", -1);
    static final VType T_METHOD_HANDLE = new VType(OBJECT, "java/lang/invoke/MethodHandle", -1);

    final int tag;
    /** Internal name of a class, or the descriptor of an array type. */
    final String name;
    /** Offset of the {@code new} instruction of an uninitialized type. */
    final int bci;

    private VType(int tag, String name, int bci) {
        this.tag = tag;
        this.name = name;
        this.bci = bci;
    }

    static VType ref(String name) {
        return OBJECT_NAME.equals(name) ? T_OBJECT : new VType(OBJECT, name, -1);
    }

    static VType uninit(int bci) {
        return new VType(UNINIT, null, bci);
    }

    boolean isCategory2() {
        return tag == LONG || tag == DOUBLE;
    }

    boolean isReference() {
        return tag == OBJECT || tag == NULL;
    }

    boolean isArray() {
        return tag == OBJECT && name.charAt(0) == '[';
    }

    boolean isObjectClass() {
        return tag == OBJECT && name.equals(OBJECT_NAME);
    }

    /** The component of an array type, as a verification type. */
    VType component() {
        char c = name.charAt(1);
        switch (c) {
            case '[': return ref(name.substring(1));
            case 'L': return ref(name.substring(2, name.length() - 1));
            case 'J': return T_LONG;
            case 'D': return T_DOUBLE;
            case 'F': return T_FLOAT;
            default: return T_INTEGER;
        }
    }

    /** The array type whose component is this type. */
    VType arrayOf() {
        return ref(name.charAt(0) == '[' ? "[" + name : "[L" + name + ";");
    }

    /** The verification type of a field descriptor, or null for {@code V}. */
    static VType ofDescriptor(String d, int start) {
        switch (d.charAt(start)) {
            case 'J': return T_LONG;
            case 'D': return T_DOUBLE;
            case 'F': return T_FLOAT;
            case 'V': return null;
            case 'L': return ref(d.substring(start + 1, d.indexOf(';', start)));
            case '[': {
                int end = start;
                while (d.charAt(end) == '[') {
                    end++;
                }
                if (d.charAt(end) == 'L') {
                    end = d.indexOf(';', end);
                }
                return ref(d.substring(start, end + 1));
            }
            default: return T_INTEGER;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof VType)) {
            return false;
        }
        VType t = (VType) o;
        return tag == t.tag && bci == t.bci && (name == null ? t.name == null : name.equals(t.name));
    }

    @Override
    public int hashCode() {
        return tag * 31 + bci + (name == null ? 0 : name.hashCode());
    }

    @Override
    public String toString() {
        switch (tag) {
            case OBJECT: return name;
            case UNINIT: return "uninit@" + bci;
            default: return String.valueOf(tag);
        }
    }
}
