/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.invoke.TypeDescriptor;

public enum TypeKind {
    BOOLEAN(4, 1, "Z"),
    BYTE(8, 1, "B"),
    CHAR(5, 1, "C"),
    SHORT(9, 1, "S"),
    INT(10, 1, "I"),
    LONG(11, 2, "J"),
    FLOAT(6, 1, "F"),
    DOUBLE(7, 2, "D"),
    REFERENCE(-1, 1, "L"),
    VOID(-1, 0, "V");

    private final int newarrayCode;
    private final int slotSize;
    private final String descriptor;

    TypeKind(int newarrayCode, int slotSize, String descriptor) {
        this.newarrayCode = newarrayCode;
        this.slotSize = slotSize;
        this.descriptor = descriptor;
    }

    public static TypeKind from(TypeDescriptor.OfField<?> descriptor) {
        return descriptor.isPrimitive() ? fromDescriptor(descriptor.descriptorString()) : REFERENCE;
    }

    public static TypeKind fromDescriptor(CharSequence descriptor) {
        if (descriptor.length() == 0) {
            throw new IllegalArgumentException("Empty descriptor");
        }
        switch (descriptor.charAt(0)) {
            case '[':
            case 'L':
                return REFERENCE;
            case 'B':
                return BYTE;
            case 'C':
                return CHAR;
            case 'Z':
                return BOOLEAN;
            case 'S':
                return SHORT;
            case 'I':
                return INT;
            case 'F':
                return FLOAT;
            case 'J':
                return LONG;
            case 'D':
                return DOUBLE;
            case 'V':
                return VOID;
            default:
                throw new IllegalArgumentException("Bad type: " + descriptor);
        }
    }

    public int newarrayCode() { return newarrayCode; }

    public ClassDesc upperBound() {
        switch (this) {
            case BOOLEAN: return ConstantDescs.CD_boolean;
            case BYTE: return ConstantDescs.CD_byte;
            case CHAR: return ConstantDescs.CD_char;
            case SHORT: return ConstantDescs.CD_short;
            case INT: return ConstantDescs.CD_int;
            case LONG: return ConstantDescs.CD_long;
            case FLOAT: return ConstantDescs.CD_float;
            case DOUBLE: return ConstantDescs.CD_double;
            case VOID: return ConstantDescs.CD_void;
            default: return ConstantDescs.CD_Object;
        }
    }

    public int slotSize() { return slotSize; }

    public TypeKind asLoadable() {
        switch (this) {
            case BOOLEAN:
            case BYTE:
            case CHAR:
            case SHORT:
                return INT;
            default:
                return this;
        }
    }

    public static TypeKind fromNewarrayCode(int code) {
        switch (code) {
            case 4: return BOOLEAN;
            case 5: return CHAR;
            case 6: return FLOAT;
            case 7: return DOUBLE;
            case 8: return BYTE;
            case 9: return SHORT;
            case 10: return INT;
            case 11: return LONG;
            default: throw new IllegalArgumentException("Bad newarray code: " + code);
        }
    }
}
