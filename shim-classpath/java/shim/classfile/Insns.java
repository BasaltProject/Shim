/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.Opcode;
import java.lang.classfile.TypeKind;
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.classfile.constantpool.LoadableConstantEntry;
import java.lang.classfile.constantpool.ConstantPoolBuilder;
import java.lang.classfile.constantpool.MemberRefEntry;
import java.lang.classfile.constantpool.Utf8Entry;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.constant.ConstantDescs;

/** Opcode tables and the argument checks shared by the instruction factories and the code builder. */
public final class Insns {
    private Insns() { }

    private static final Opcode[] BY_CODE = new Opcode[256];
    private static final Opcode[] WIDE_BY_CODE = new Opcode[256];

    static {
        for (Opcode op : Opcode.values()) {
            if (op.isWide()) {
                WIDE_BY_CODE[op.bytecode() & 0xff] = op;
            } else {
                BY_CODE[op.bytecode()] = op;
            }
        }
    }

    static Opcode ofCode(int code) { return BY_CODE[code]; }

    public static PoolImpl tempPool() { return new PoolImpl(); }

    public static ConstantPoolBuilder pool(ClassEntry owner) {
        java.util.Objects.requireNonNull(owner);
        return new PoolImpl();
    }

    public static ClassDesc fieldType(Utf8Entry descriptor) {
        return ClassDesc.ofDescriptor(descriptor.stringValue());
    }

    public static TypeKind constantType(Opcode op) { return typeOf(op); }

    public static void checkArgument(Opcode op, int value) {
        if (op == Opcode.BIPUSH) {
            if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
                throw new IllegalArgumentException("BIPUSH: value must be within: Byte.MIN_VALUE <= value <= "
                    + "Byte.MAX_VALUE, found: " + value);
            }
        } else if (op == Opcode.SIPUSH) {
            if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
                throw new IllegalArgumentException("SIPUSH: value must be within: Short.MIN_VALUE <= value <= "
                    + "Short.MAX_VALUE, found: " + value);
            }
        } else {
            throw new IllegalArgumentException("Wrong opcode specified; found " + op
                + ", expected BIPUSH or SIPUSH");
        }
    }

    public static void checkRetSlot(Opcode op, int slot) {
        boolean ok = op == Opcode.RET ? (slot & ~0xff) == 0 : op == Opcode.RET_W && (slot & ~0xffff) == 0;
        if (!ok) {
            throw new IllegalArgumentException("Invalid slot index " + slot + " for " + op);
        }
    }

    public static void checkS2(int value) {
        if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
            throw new IllegalArgumentException("cannot encode as S2: " + value);
        }
    }

    static Opcode ofWideCode(int code) { return WIDE_BY_CODE[code]; }

    public static void checkKind(Opcode op, Opcode.Kind kind) {
        if (op.kind() != kind) {
            throw new IllegalArgumentException("Wrong opcode kind specified; found " + op + "(" + op.kind()
                + "), expected " + kind);
        }
    }

    public static IllegalArgumentException badSlot(int slot) {
        return new IllegalArgumentException("Invalid slot index :" + slot);
    }

    public static int checkSlot(int slot) {
        if ((slot & ~0xffff) != 0) {
            throw badSlot(slot);
        }
        return slot;
    }

    /** Validates a slot against an explicit load, store or ret opcode. */
    public static void checkSlot(Opcode op, int slot) {
        int size = op.sizeIfFixed();
        boolean ok;
        if (size == 1) {
            ok = slot == impliedSlot(op);
        } else if (size == 2) {
            ok = (slot & ~0xff) == 0;
        } else if (size == 4) {
            ok = (slot & ~0xffff) == 0;
        } else {
            ok = false;
        }
        if (!ok) {
            throw new IllegalArgumentException("Invalid slot index " + slot + " for " + op);
        }
    }

    static int impliedSlot(Opcode op) {
        int c = op.bytecode();
        if (c >= 26 && c <= 45) {
            return (c - 26) & 3;
        }
        if (c >= 59 && c <= 78) {
            return (c - 59) & 3;
        }
        return -1;
    }

    private static int typeIndex(TypeKind tk) {
        switch (tk) {
            case BOOLEAN:
            case BYTE:
            case CHAR:
            case SHORT:
            case INT:
                return 0;
            case LONG:
                return 1;
            case FLOAT:
                return 2;
            case DOUBLE:
                return 3;
            case REFERENCE:
                return 4;
            default:
                throw new IllegalArgumentException("void");
        }
    }

    public static Opcode loadOpcode(TypeKind tk, int slot) {
        return localOpcode(tk, slot, 21, 26, 0xc415);
    }

    public static Opcode storeOpcode(TypeKind tk, int slot) {
        return localOpcode(tk, slot, 54, 59, 0xc436);
    }

    private static Opcode localOpcode(TypeKind tk, int slot, int base, int shortBase, int wideBase) {
        int t = typeIndex(tk);
        if (slot >= 0 && slot < 4) {
            return BY_CODE[shortBase + 4 * t + slot];
        }
        if ((slot & ~0xff) == 0) {
            return BY_CODE[base + t];
        }
        if ((slot & ~0xffff) == 0) {
            return WIDE_BY_CODE[(wideBase + t) & 0xff];
        }
        throw badSlot(slot);
    }

    /** The type operated on by load, store, return, array, operator and constant opcodes. */
    static TypeKind typeOf(Opcode op) {
        int c = op.bytecode();
        switch (op.kind()) {
            case LOAD:
                return localType(op.isWide() ? (c & 0xff) - 21 : c <= 25 ? c - 21 : (c - 26) / 4);
            case STORE:
                return localType(op.isWide() ? (c & 0xff) - 54 : c <= 58 ? c - 54 : (c - 59) / 4);
            case RETURN:
                return c == 177 ? TypeKind.VOID : localType(c - 172);
            case ARRAY_LOAD:
                return arrayType(c - 46);
            case ARRAY_STORE:
                return arrayType(c - 79);
            default:
                break;
        }
        switch (op) {
            case ACONST_NULL:
                return TypeKind.REFERENCE;
            case LCONST_0:
            case LCONST_1:
            case LCMP:
                return TypeKind.LONG;
            case FCONST_0:
            case FCONST_1:
            case FCONST_2:
            case FCMPL:
            case FCMPG:
                return TypeKind.FLOAT;
            case DCONST_0:
            case DCONST_1:
            case DCMPL:
            case DCMPG:
                return TypeKind.DOUBLE;
            case ARRAYLENGTH:
                return TypeKind.INT;
            default:
                break;
        }
        if (op.kind() == Opcode.Kind.OPERATOR && c >= 96 && c <= 131) {
            int k = c < 120 ? (c - 96) % 4 : (c - 120) % 2;
            return localType(k);
        }
        return TypeKind.INT;
    }

    private static TypeKind localType(int i) {
        switch (i) {
            case 0: return TypeKind.INT;
            case 1: return TypeKind.LONG;
            case 2: return TypeKind.FLOAT;
            case 3: return TypeKind.DOUBLE;
            default: return TypeKind.REFERENCE;
        }
    }

    private static TypeKind arrayType(int i) {
        switch (i) {
            case 0: return TypeKind.INT;
            case 1: return TypeKind.LONG;
            case 2: return TypeKind.FLOAT;
            case 3: return TypeKind.DOUBLE;
            case 4: return TypeKind.REFERENCE;
            case 5: return TypeKind.BYTE;
            case 6: return TypeKind.CHAR;
            default: return TypeKind.SHORT;
        }
    }

    static TypeKind convertFrom(Opcode op) {
        int c = op.bytecode();
        return c >= 145 ? TypeKind.INT : localType((c - 133) / 3);
    }

    static TypeKind convertTo(Opcode op) {
        switch (op) {
            case I2B: return TypeKind.BYTE;
            case I2C: return TypeKind.CHAR;
            case I2S: return TypeKind.SHORT;
            case L2I: case F2I: case D2I: return TypeKind.INT;
            case I2L: case F2L: case D2L: return TypeKind.LONG;
            case I2F: case L2F: case D2F: return TypeKind.FLOAT;
            default: return TypeKind.DOUBLE;
        }
    }

    public static Opcode convertOpcode(TypeKind from, TypeKind to) {
        Opcode op = null;
        switch (from) {
            case INT:
                switch (to) {
                    case LONG: op = Opcode.I2L; break;
                    case FLOAT: op = Opcode.I2F; break;
                    case DOUBLE: op = Opcode.I2D; break;
                    case BYTE: op = Opcode.I2B; break;
                    case CHAR: op = Opcode.I2C; break;
                    case SHORT: op = Opcode.I2S; break;
                    default: break;
                }
                break;
            case LONG:
                op = to == TypeKind.INT ? Opcode.L2I : to == TypeKind.FLOAT ? Opcode.L2F
                    : to == TypeKind.DOUBLE ? Opcode.L2D : null;
                break;
            case FLOAT:
                op = to == TypeKind.INT ? Opcode.F2I : to == TypeKind.LONG ? Opcode.F2L
                    : to == TypeKind.DOUBLE ? Opcode.F2D : null;
                break;
            case DOUBLE:
                op = to == TypeKind.INT ? Opcode.D2I : to == TypeKind.LONG ? Opcode.D2L
                    : to == TypeKind.FLOAT ? Opcode.D2F : null;
                break;
            default:
                break;
        }
        if (op == null) {
            throw new IllegalArgumentException("convert " + from + " -> " + to);
        }
        return op;
    }

    /** Emits the conversion between two types, narrowing through int where the bytecode needs it. */
    public static void conversion(java.lang.classfile.CodeBuilder b, TypeKind from, TypeKind to) {
        if (to == TypeKind.REFERENCE || to == TypeKind.VOID) {
            return;
        }
        TypeKind source = from.asLoadable();
        if (source == TypeKind.REFERENCE || source == TypeKind.VOID) {
            throw new IllegalArgumentException("convert " + from + " -> " + to);
        }
        TypeKind target = to.asLoadable();
        if (source != target) {
            b.with(new UnboundInsns.UnboundConvertInstruction(convertOpcode(source, target)));
        }
        switch (to) {
            case BOOLEAN:
                b.iconst_1().iand();
                break;
            case BYTE:
                b.i2b();
                break;
            case CHAR:
                b.i2c();
                break;
            case SHORT:
                b.i2s();
                break;
            default:
                break;
        }
    }

    public static Opcode returnOpcode(TypeKind tk) {
        switch (tk) {
            case BOOLEAN:
            case BYTE:
            case CHAR:
            case SHORT:
            case INT:
                return Opcode.IRETURN;
            case LONG: return Opcode.LRETURN;
            case FLOAT: return Opcode.FRETURN;
            case DOUBLE: return Opcode.DRETURN;
            case REFERENCE: return Opcode.ARETURN;
            default: return Opcode.RETURN;
        }
    }

    public static Opcode arrayLoadOpcode(TypeKind tk) {
        switch (tk) {
            case BOOLEAN:
            case BYTE:
                return Opcode.BALOAD;
            case CHAR: return Opcode.CALOAD;
            case SHORT: return Opcode.SALOAD;
            case INT: return Opcode.IALOAD;
            case LONG: return Opcode.LALOAD;
            case FLOAT: return Opcode.FALOAD;
            case DOUBLE: return Opcode.DALOAD;
            case REFERENCE: return Opcode.AALOAD;
            default: throw new IllegalArgumentException("void not an allowable array type");
        }
    }

    public static Opcode arrayStoreOpcode(TypeKind tk) {
        switch (tk) {
            case BOOLEAN:
            case BYTE:
                return Opcode.BASTORE;
            case CHAR: return Opcode.CASTORE;
            case SHORT: return Opcode.SASTORE;
            case INT: return Opcode.IASTORE;
            case LONG: return Opcode.LASTORE;
            case FLOAT: return Opcode.FASTORE;
            case DOUBLE: return Opcode.DASTORE;
            case REFERENCE: return Opcode.AASTORE;
            default: throw new IllegalArgumentException("void not an allowable array type");
        }
    }

    public static Opcode reverseBranch(Opcode op) {
        switch (op) {
            case IFEQ: return Opcode.IFNE;
            case IFNE: return Opcode.IFEQ;
            case IFLT: return Opcode.IFGE;
            case IFGE: return Opcode.IFLT;
            case IFGT: return Opcode.IFLE;
            case IFLE: return Opcode.IFGT;
            case IF_ICMPEQ: return Opcode.IF_ICMPNE;
            case IF_ICMPNE: return Opcode.IF_ICMPEQ;
            case IF_ICMPLT: return Opcode.IF_ICMPGE;
            case IF_ICMPGE: return Opcode.IF_ICMPLT;
            case IF_ICMPGT: return Opcode.IF_ICMPLE;
            case IF_ICMPLE: return Opcode.IF_ICMPGT;
            case IF_ACMPEQ: return Opcode.IF_ACMPNE;
            case IF_ACMPNE: return Opcode.IF_ACMPEQ;
            case IFNULL: return Opcode.IFNONNULL;
            case IFNONNULL: return Opcode.IFNULL;
            default: throw new IllegalArgumentException("Unknown branch instruction: " + op);
        }
    }

    static boolean isUnconditional(Opcode op) {
        switch (op) {
            case GOTO:
            case GOTO_W:
            case ATHROW:
            case IRETURN:
            case LRETURN:
            case FRETURN:
            case DRETURN:
            case ARETURN:
            case RETURN:
            case TABLESWITCH:
            case LOOKUPSWITCH:
                return true;
            default:
                return false;
        }
    }

    static ConstantDesc intrinsicValue(Opcode op) {
        switch (op) {
            case ACONST_NULL: return ConstantDescs.NULL;
            case ICONST_M1: return -1;
            case ICONST_0: return 0;
            case ICONST_1: return 1;
            case ICONST_2: return 2;
            case ICONST_3: return 3;
            case ICONST_4: return 4;
            case ICONST_5: return 5;
            case LCONST_0: return 0L;
            case LCONST_1: return 1L;
            case FCONST_0: return 0.0f;
            case FCONST_1: return 1.0f;
            case FCONST_2: return 2.0f;
            case DCONST_0: return 0.0d;
            case DCONST_1: return 1.0d;
            default: return null;
        }
    }

    /** Parameter slots of a method descriptor, plus one for invokeinterface's count operand. */
    static int argSlots(String descriptor) {
        int slots = 0;
        int i = 1;
        while (descriptor.charAt(i) != ')') {
            char c = descriptor.charAt(i);
            if (c == 'J' || c == 'D') {
                slots += 2;
                i++;
            } else {
                slots++;
                while (descriptor.charAt(i) == '[') {
                    i++;
                }
                if (descriptor.charAt(i) == 'L') {
                    i = descriptor.indexOf(';', i);
                }
                i++;
            }
        }
        return slots;
    }

    static String member(MemberRefEntry m) {
        return m.owner().asInternalName() + "." + m.name().stringValue();
    }

    /** The load constant opcode for an entry, chosen by its index once adopted into {@code pool}. */
    public static Opcode ldcOpcode(ConstantPoolBuilder pool, LoadableConstantEntry entry) {
        if (entry.typeKind().slotSize() == 2) {
            return Opcode.LDC2_W;
        }
        int index = pool instanceof PoolImpl ? ((PoolImpl) pool).indexOf(entry) : entry.index();
        return index > 0xff ? Opcode.LDC_W : Opcode.LDC;
    }

    /** Size of a switch instruction placed at {@code bci}. */
    static int switchSize(Opcode op, int bci, int entries) {
        int pad = (4 - ((bci + 1) & 3)) & 3;
        return op == Opcode.TABLESWITCH ? 1 + pad + 12 + 4 * entries : 1 + pad + 8 + 8 * entries;
    }
}
