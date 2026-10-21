/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.constantpool.ClassEntry;
import java.lang.classfile.constantpool.ConstantDynamicEntry;
import java.lang.classfile.constantpool.DoubleEntry;
import java.lang.classfile.constantpool.FieldRefEntry;
import java.lang.classfile.constantpool.FloatEntry;
import java.lang.classfile.constantpool.IntegerEntry;
import java.lang.classfile.constantpool.InterfaceMethodRefEntry;
import java.lang.classfile.constantpool.InvokeDynamicEntry;
import java.lang.classfile.constantpool.LongEntry;
import java.lang.classfile.constantpool.MemberRefEntry;
import java.lang.classfile.constantpool.MethodHandleEntry;
import java.lang.classfile.constantpool.MethodRefEntry;
import java.lang.classfile.constantpool.MethodTypeEntry;
import java.lang.classfile.constantpool.PoolEntry;
import java.lang.classfile.constantpool.StringEntry;
import java.util.ArrayList;
import java.util.List;

/** The effect of each instruction on the verifier's frame. */
final class VerifyInsns {
    private static final VerifyType INT = VerifyType.T_INTEGER;
    private static final VerifyType FLOAT = VerifyType.T_FLOAT;
    private static final VerifyType LONG = VerifyType.T_LONG;
    private static final VerifyType DOUBLE = VerifyType.T_DOUBLE;
    private static final VerifyType REF = VerifyType.T_REF_CHECK;

    private final MethodVerifier m;
    private final byte[] code;

    VerifyInsns(MethodVerifier m) {
        this.m = m;
        this.code = m.bytes;
    }

    private int u1(int p) { return code[p] & 0xff; }
    private int u2(int p) { return ((code[p] & 0xff) << 8) | (code[p + 1] & 0xff); }

    /** Applies the instruction at the current offset; returns true when control does not fall through. */
    boolean execute(int op, int len) {
        VerifyFrame f = m.current;
        int bci = m.bci;
        boolean wide = op == 196;
        if (wide) {
            op = u1(bci + 1);
            if (op == 169) {
                throw VerifyFrame.fail("Bad wide instruction");
            }
        }
        int index = wide ? u2(bci + 2) : (len > 1 ? u1(bci + 1) : 0);
        switch (op) {
            case 0:
                return false;
            case 1:
                f.push(VerifyType.T_NULL);
                return false;
            case 2: case 3: case 4: case 5: case 6: case 7: case 8: case 16: case 17:
                f.push(INT);
                return false;
            case 9: case 10:
                f.push2(LONG);
                return false;
            case 11: case 12: case 13:
                f.push(FLOAT);
                return false;
            case 14: case 15:
                f.push2(DOUBLE);
                return false;
            case 18:
                ldc(u1(bci + 1), false);
                return false;
            case 19:
                ldc(u2(bci + 1), false);
                return false;
            case 20:
                ldc(u2(bci + 1), true);
                return false;
            case 21:
                f.getLocal(index, INT);
                f.push(INT);
                return false;
            case 22:
                f.getLocal2(index, LONG);
                f.push2(LONG);
                return false;
            case 23:
                f.getLocal(index, FLOAT);
                f.push(FLOAT);
                return false;
            case 24:
                f.getLocal2(index, DOUBLE);
                f.push2(DOUBLE);
                return false;
            case 25:
                f.push(f.getLocal(index, REF));
                return false;
            case 26: case 27: case 28: case 29:
                f.getLocal(op - 26, INT);
                f.push(INT);
                return false;
            case 30: case 31: case 32: case 33:
                f.getLocal2(op - 30, LONG);
                f.push2(LONG);
                return false;
            case 34: case 35: case 36: case 37:
                f.getLocal(op - 34, FLOAT);
                f.push(FLOAT);
                return false;
            case 38: case 39: case 40: case 41:
                f.getLocal2(op - 38, DOUBLE);
                f.push2(DOUBLE);
                return false;
            case 42: case 43: case 44: case 45:
                f.push(f.getLocal(op - 42, REF));
                return false;
            case 46: case 47: case 48: case 49: case 50: case 51: case 52: case 53:
                arrayLoad(op);
                return false;
            case 54:
                f.setLocal(index, f.pop(INT));
                return false;
            case 55:
                f.pop2(LONG);
                f.setLocal2(index, LONG);
                return false;
            case 56:
                f.setLocal(index, f.pop(FLOAT));
                return false;
            case 57:
                f.pop2(DOUBLE);
                f.setLocal2(index, DOUBLE);
                return false;
            case 58:
                f.setLocal(index, f.pop(REF));
                return false;
            case 59: case 60: case 61: case 62:
                f.setLocal(op - 59, f.pop(INT));
                return false;
            case 63: case 64: case 65: case 66:
                f.pop2(LONG);
                f.setLocal2(op - 63, LONG);
                return false;
            case 67: case 68: case 69: case 70:
                f.setLocal(op - 67, f.pop(FLOAT));
                return false;
            case 71: case 72: case 73: case 74:
                f.pop2(DOUBLE);
                f.setLocal2(op - 71, DOUBLE);
                return false;
            case 75: case 76: case 77: case 78:
                f.setLocal(op - 75, f.pop(REF));
                return false;
            case 79: case 80: case 81: case 82: case 83: case 84: case 85: case 86:
                arrayStore(op);
                return false;
            default:
                return VerifyStack.execute(this, m, f, op, bci, index);
        }
    }

    private void arrayLoad(int op) {
        VerifyFrame f = m.current;
        f.pop(INT);
        VerifyType array = f.pop(REF);
        if (!isArrayOf(array, op - 46)) {
            throw VerifyFrame.fail("Bad type");
        }
        switch (op) {
            case 47:
                f.push2(LONG);
                break;
            case 48:
                f.push(FLOAT);
                break;
            case 49:
                f.push2(DOUBLE);
                break;
            case 50:
                f.push(array.kind == VerifyType.NULL ? VerifyType.T_NULL : array.component());
                break;
            default:
                f.push(INT);
                break;
        }
    }

    private void arrayStore(int op) {
        VerifyFrame f = m.current;
        switch (op) {
            case 80:
                f.pop2(LONG);
                break;
            case 81:
                f.pop(FLOAT);
                break;
            case 82:
                f.pop2(DOUBLE);
                break;
            case 83:
                f.pop(VerifyType.T_OBJECT);
                break;
            default:
                f.pop(INT);
                break;
        }
        f.pop(INT);
        VerifyType array = f.pop(REF);
        if (!isArrayOf(array, op - 79)) {
            throw VerifyFrame.fail("Bad type");
        }
    }

    /** Whether {@code t} is null or an array of the component kind of array load/store number {@code k}. */
    private static boolean isArrayOf(VerifyType t, int k) {
        if (t.kind == VerifyType.NULL) {
            return true;
        }
        if (!t.isArray()) {
            return false;
        }
        char c = t.name.charAt(1);
        switch (k) {
            case 0: return c == 'I';
            case 1: return c == 'J';
            case 2: return c == 'F';
            case 3: return c == 'D';
            case 4: return c == 'L' || c == '[';
            case 5: return c == 'B' || c == 'Z';
            case 6: return c == 'C';
            default: return c == 'S';
        }
    }

    /** The pool entry at {@code index}, failing unless it has one of the {@code allowed} types. */
    PoolEntry cp(int index, Class<?>... allowed) {
        if (index <= 0 || index >= m.pool.size()) {
            throw VerifyFrame.fail("Illegal constant pool index " + index);
        }
        PoolEntry e = m.pool.entryByIndex(index);
        for (Class<?> c : allowed) {
            if (c.isInstance(e)) {
                return e;
            }
        }
        throw VerifyFrame.fail("Illegal type at constant pool entry " + index);
    }

    private void ldc(int index, boolean wide) {
        VerifyFrame f = m.current;
        PoolEntry e = wide ? cp(index, LongEntry.class, DoubleEntry.class, ConstantDynamicEntry.class)
            : cp(index, IntegerEntry.class, FloatEntry.class, StringEntry.class, ClassEntry.class,
                MethodHandleEntry.class, MethodTypeEntry.class, ConstantDynamicEntry.class);
        if (wide) {
            if (e instanceof LongEntry) {
                f.push2(LONG);
            } else if (e instanceof DoubleEntry) {
                f.push2(DOUBLE);
            } else if (((ConstantDynamicEntry) e).typeKind().slotSize() == 2) {
                f.pushValue(VerifyType.ofDescriptor(((ConstantDynamicEntry) e).type().stringValue()));
            } else {
                throw VerifyFrame.fail("Illegal type at constant pool entry " + index);
            }
            return;
        }
        if (e instanceof IntegerEntry) {
            f.push(INT);
        } else if (e instanceof FloatEntry) {
            f.push(FLOAT);
        } else if (e instanceof StringEntry) {
            f.push(VerifyType.T_STRING);
        } else if (e instanceof ClassEntry) {
            f.push(VerifyType.T_CLASS);
        } else if (e instanceof MethodTypeEntry) {
            f.push(VerifyType.ref("java/lang/invoke/MethodType"));
        } else if (e instanceof MethodHandleEntry) {
            f.push(VerifyType.ref("java/lang/invoke/MethodHandle"));
        } else if (((ConstantDynamicEntry) e).typeKind().slotSize() == 1) {
            f.push(VerifyType.ofDescriptor(((ConstantDynamicEntry) e).type().stringValue()));
        } else {
            throw VerifyFrame.fail("Illegal type at constant pool entry " + index);
        }
    }

    /** Parameter types of a method descriptor, each long or double followed by its second half. */
    static List<VerifyType> argumentTypes(String d) {
        List<VerifyType> out = new ArrayList<>();
        int i = 1;
        while (d.charAt(i) != ')') {
            int end = FrameAnalyzer.skipType(d, i);
            VerifyType t = VerifyType.ofDescriptor(d.substring(i, end));
            out.add(t);
            if (t.isCategory2()) {
                out.add(t.secondHalf());
            }
            i = end;
        }
        return out;
    }

    static String returnDescriptor(String d) {
        return d.substring(d.indexOf(')') + 1);
    }

    void fieldAccess(int op, int bci) {
        VerifyFrame f = m.current;
        FieldRefEntry ref = (FieldRefEntry) cp(u2(bci + 1), FieldRefEntry.class);
        VerifyType owner = VerifyType.ref(ref.owner().asInternalName());
        VerifyType type = VerifyType.ofDescriptor(ref.type().stringValue());
        switch (op) {
            case 178:
                f.pushValue(type);
                break;
            case 179:
                f.popValue(type);
                break;
            case 180:
                f.pop(owner);
                f.pushValue(type);
                break;
            default: {
                f.popValue(type);
                VerifyType obj = f.pop(VerifyType.T_BOGUS);
                if (obj.kind == VerifyType.UNINIT_THIS && owner.equals(m.thisType) && declaresField(ref)) {
                    obj = m.thisType;
                }
                if (!f.assignable(owner, obj)) {
                    throw VerifyFrame.fail("Bad type on operand stack in putfield", owner, obj);
                }
                break;
            }
        }
    }

    private boolean declaresField(FieldRefEntry ref) {
        for (java.lang.classfile.FieldModel fm : m.cls.fields()) {
            if (fm.fieldName().equalsString(ref.name().stringValue())
                    && fm.fieldType().equalsString(ref.type().stringValue())) {
                return true;
            }
        }
        return false;
    }

    void invoke(int op, int bci) {
        VerifyFrame f = m.current;
        int index = u2(bci + 1);
        PoolEntry entry;
        if (op == 186) {
            entry = cp(index, InvokeDynamicEntry.class);
        } else if (op == 185) {
            entry = cp(index, InterfaceMethodRefEntry.class);
        } else if (op == 182 || m.cls.majorVersion() < 52) {
            entry = cp(index, MethodRefEntry.class);
        } else {
            entry = cp(index, MethodRefEntry.class, InterfaceMethodRefEntry.class);
        }
        String name;
        String type;
        String owner = null;
        if (op == 186) {
            InvokeDynamicEntry indy = (InvokeDynamicEntry) entry;
            name = indy.name().stringValue();
            type = indy.type().stringValue();
            if (u1(bci + 3) != 0 || u1(bci + 4) != 0) {
                throw VerifyFrame.fail("Third and fourth operand bytes of invokedynamic must be zero");
            }
        } else {
            MemberRefEntry ref = (MemberRefEntry) entry;
            name = ref.name().stringValue();
            type = ref.type().stringValue();
            owner = ref.owner().asInternalName();
        }
        List<VerifyType> args = argumentTypes(type);
        if (op == 185) {
            if (args.size() + 1 != u1(bci + 3)) {
                throw VerifyFrame.fail("Inconsistent args count operand in invokeinterface");
            }
            if (u1(bci + 4) != 0) {
                throw VerifyFrame.fail("Fourth operand byte of invokeinterface must be zero");
            }
        }
        if (name.charAt(0) == '<') {
            if (op != 183 || !name.equals("<init>")) {
                throw VerifyFrame.fail("Illegal call to internal method");
            }
        } else if (op == 183 && !isSameOrDirectInterface(owner) && !owner.equals(superName())) {
            VerifyType ref = VerifyType.ref(owner);
            if (!f.assignable(ref, m.thisType)) {
                throw VerifyFrame.fail("Bad invokespecial instruction: current class isn't assignable to reference "
                    + "class.", ref, m.thisType);
            }
            if (entry instanceof InterfaceMethodRefEntry) {
                throw VerifyFrame.fail("Bad invokespecial instruction: interface method reference is in an indirect "
                    + "superinterface.");
            }
        }
        for (int i = args.size() - 1; i >= 0; i--) {
            f.pop(args.get(i));
        }
        if (op == 183 && name.equals("<init>")) {
            invokeInit(owner);
        } else if (op == 183) {
            f.pop(m.thisType);
        } else if (op == 182 || op == 185) {
            f.pop(VerifyType.ref(owner));
        }
        String ret = returnDescriptor(type);
        if (!ret.equals("V")) {
            f.pushValue(VerifyType.ofDescriptor(ret));
        }
    }

    private String superName() {
        return m.cls.superclass().isPresent() ? m.cls.superclass().get().asInternalName() : null;
    }

    private boolean isSameOrDirectInterface(String owner) {
        if (owner.equals(m.thisName)) {
            return true;
        }
        for (ClassEntry i : m.cls.interfaces()) {
            if (i.asInternalName().equals(owner)) {
                return true;
            }
        }
        return false;
    }

    private void invokeInit(String owner) {
        VerifyFrame f = m.current;
        VerifyType type = f.pop(REF);
        if (type.kind == VerifyType.UNINIT_THIS) {
            String sup = m.cls.superclass().isPresent() ? m.cls.superclass().get().asInternalName() : null;
            if (!owner.equals(m.thisName) && !owner.equals(sup)) {
                throw VerifyFrame.fail("Bad <init> method call");
            }
            f.initialize(type, m.thisType);
        } else if (type.kind == VerifyType.UNINIT) {
            int at = type.bci;
            if (at < 0 || at + 2 >= m.codeLength || (code[at] & 0xff) != 187) {
                throw VerifyFrame.fail("Expecting new instruction");
            }
            ClassEntry created = (ClassEntry) m.pool.entryByIndex(u2(at + 1));
            if (!created.asInternalName().equals(owner)) {
                throw VerifyFrame.fail("Call to wrong <init> method");
            }
            f.initialize(type, VerifyType.ref(owner));
        } else {
            throw VerifyFrame.fail("Bad operand type when invoking <init>");
        }
    }

}
