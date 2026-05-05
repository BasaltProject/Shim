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

/** Stack manipulation, arithmetic, control flow and object instructions of the verifier. */
final class VerifyStack {
    private static final VerifyType INT = VerifyType.T_INTEGER;
    private static final VerifyType FLOAT = VerifyType.T_FLOAT;
    private static final VerifyType LONG = VerifyType.T_LONG;
    private static final VerifyType DOUBLE = VerifyType.T_DOUBLE;
    private static final VerifyType CAT1 = VerifyType.T_CAT1_CHECK;
    private static final VerifyType CAT2 = VerifyType.T_CAT2_CHECK;
    private static final VerifyType REF = VerifyType.T_REF_CHECK;

    private VerifyStack() { }

    private static int u1(byte[] c, int p) { return c[p] & 0xff; }
    private static int u2(byte[] c, int p) { return ((c[p] & 0xff) << 8) | (c[p + 1] & 0xff); }
    private static int s2(byte[] c, int p) { return (short) u2(c, p); }

    static boolean execute(VerifyInsns insns, MethodVerifier m, VerifyFrame f, int op, int bci, int index) {
        byte[] c = m.bytes;
        switch (op) {
            case 87:
                f.pop(CAT1);
                return false;
            case 88: {
                VerifyType t = f.pop(VerifyType.T_BOGUS);
                if (t.isCategory1()) {
                    f.pop(CAT1);
                } else if (isSecondHalf(t)) {
                    f.pop(CAT2);
                } else {
                    throw VerifyFrame.fail("Bad type");
                }
                return false;
            }
            case 89: {
                VerifyType t = f.pop(CAT1);
                f.push(t);
                f.push(t);
                return false;
            }
            case 90: {
                VerifyType t = f.pop(CAT1);
                VerifyType t2 = f.pop(CAT1);
                f.push(t);
                f.push(t2);
                f.push(t);
                return false;
            }
            case 91: {
                VerifyType t = f.pop(CAT1);
                VerifyType t2 = f.pop(VerifyType.T_BOGUS);
                VerifyType t3;
                if (t2.isCategory1()) {
                    t3 = f.pop(CAT1);
                } else if (isSecondHalf(t2)) {
                    t3 = f.pop(CAT2);
                } else {
                    throw VerifyFrame.fail("Bad type");
                }
                f.push(t);
                f.push(t3);
                f.push(t2);
                f.push(t);
                return false;
            }
            case 92: {
                VerifyType t = f.pop(VerifyType.T_BOGUS);
                VerifyType t2 = popPair(f, t);
                f.push(t2);
                f.push(t);
                f.push(t2);
                f.push(t);
                return false;
            }
            case 93: {
                VerifyType t = f.pop(VerifyType.T_BOGUS);
                VerifyType t2 = popPair(f, t);
                VerifyType t3 = f.pop(CAT1);
                f.push(t2);
                f.push(t);
                f.push(t3);
                f.push(t2);
                f.push(t);
                return false;
            }
            case 94: {
                VerifyType t = f.pop(VerifyType.T_BOGUS);
                VerifyType t2 = popPair(f, t);
                VerifyType t3 = f.pop(VerifyType.T_BOGUS);
                VerifyType t4 = popPair(f, t3);
                f.push(t2);
                f.push(t);
                f.push(t4);
                f.push(t3);
                f.push(t2);
                f.push(t);
                return false;
            }
            case 95: {
                VerifyType t = f.pop(CAT1);
                VerifyType t2 = f.pop(CAT1);
                f.push(t);
                f.push(t2);
                return false;
            }
            case 96: case 100: case 104: case 108: case 112: case 120: case 122: case 124: case 126: case 128:
            case 130:
                f.pop(INT);
                f.pop(INT);
                f.push(INT);
                return false;
            case 97: case 101: case 105: case 109: case 113: case 127: case 129: case 131:
                f.pop2(LONG);
                f.pop2(LONG);
                f.push2(LONG);
                return false;
            case 98: case 102: case 106: case 110: case 114:
                f.pop(FLOAT);
                f.pop(FLOAT);
                f.push(FLOAT);
                return false;
            case 99: case 103: case 107: case 111: case 115:
                f.pop2(DOUBLE);
                f.pop2(DOUBLE);
                f.push2(DOUBLE);
                return false;
            case 116: case 145: case 146: case 147:
                f.pop(INT);
                f.push(INT);
                return false;
            case 117:
                f.pop2(LONG);
                f.push2(LONG);
                return false;
            case 118:
                f.pop(FLOAT);
                f.push(FLOAT);
                return false;
            case 119:
                f.pop2(DOUBLE);
                f.push2(DOUBLE);
                return false;
            case 121: case 123: case 125:
                f.pop(INT);
                f.pop2(LONG);
                f.push2(LONG);
                return false;
            case 132:
                f.getLocal(index, INT);
                f.setLocal(index, INT);
                return false;
            case 133:
                f.pop(INT);
                f.push2(LONG);
                return false;
            case 134:
                f.pop(INT);
                f.push(FLOAT);
                return false;
            case 135:
                f.pop(INT);
                f.push2(DOUBLE);
                return false;
            case 136:
                f.pop2(LONG);
                f.push(INT);
                return false;
            case 137:
                f.pop2(LONG);
                f.push(FLOAT);
                return false;
            case 138:
                f.pop2(LONG);
                f.push2(DOUBLE);
                return false;
            case 139:
                f.pop(FLOAT);
                f.push(INT);
                return false;
            case 140:
                f.pop(FLOAT);
                f.push2(LONG);
                return false;
            case 141:
                f.pop(FLOAT);
                f.push2(DOUBLE);
                return false;
            case 142:
                f.pop2(DOUBLE);
                f.push(INT);
                return false;
            case 143:
                f.pop2(DOUBLE);
                f.push2(LONG);
                return false;
            case 144:
                f.pop2(DOUBLE);
                f.push(FLOAT);
                return false;
            case 148:
                f.pop2(LONG);
                f.pop2(LONG);
                f.push(INT);
                return false;
            case 149: case 150:
                f.pop(FLOAT);
                f.pop(FLOAT);
                f.push(INT);
                return false;
            case 151: case 152:
                f.pop2(DOUBLE);
                f.pop2(DOUBLE);
                f.push(INT);
                return false;
            case 153: case 154: case 155: case 156: case 157: case 158:
                f.pop(INT);
                m.checkJumpTarget(bci + s2(c, bci + 1));
                return false;
            case 159: case 160: case 161: case 162: case 163: case 164:
                f.pop(INT);
                f.pop(INT);
                m.checkJumpTarget(bci + s2(c, bci + 1));
                return false;
            case 165: case 166:
                f.pop(REF);
                f.pop(REF);
                m.checkJumpTarget(bci + s2(c, bci + 1));
                return false;
            case 167:
                m.checkJumpTarget(bci + s2(c, bci + 1));
                return true;
            case 200:
                m.checkJumpTarget(bci + StackMapGenerator.readInt(c, bci + 1));
                return true;
            case 198: case 199:
                f.pop(REF);
                m.checkJumpTarget(bci + s2(c, bci + 1));
                return false;
            case 170: case 171:
                switchInsn(m, f, op, bci);
                return true;
            case 172:
                returnValue(m, f, f.pop(INT));
                return true;
            case 173:
                f.pop2(LONG);
                returnValue(m, f, LONG);
                return true;
            case 174:
                returnValue(m, f, f.pop(FLOAT));
                return true;
            case 175:
                f.pop2(DOUBLE);
                returnValue(m, f, DOUBLE);
                return true;
            case 176:
                returnValue(m, f, f.pop(REF));
                return true;
            case 177:
                if (m.returnType.kind != VerifyType.BOGUS) {
                    throw VerifyFrame.fail("Method expects a return value");
                }
                if (m.methodName.equals("<init>") && f.thisUninit) {
                    throw VerifyFrame.fail("Constructor must call super() or this() before return");
                }
                return true;
            case 178: case 179: case 180: case 181:
                insns.fieldAccess(op, bci);
                return false;
            case 182: case 183: case 184: case 185: case 186:
                insns.invoke(op, bci);
                return false;
            case 187: {
                ClassEntry type = (ClassEntry) insns.cp(u2(c, bci + 1), ClassEntry.class);
                if (type.asInternalName().charAt(0) == '[') {
                    throw VerifyFrame.fail("Illegal new instruction");
                }
                f.push(VerifyType.uninit(bci));
                return false;
            }
            case 188: {
                VerifyType t = primitiveArray(u1(c, bci + 1));
                f.pop(INT);
                f.push(t);
                return false;
            }
            case 189: {
                VerifyType component = VerifyType.ofDescriptor(classDescriptor(insns, u2(c, bci + 1)));
                f.pop(INT);
                if (component.kind == VerifyType.REF && component.isArray() && component.dimensions() >= 255) {
                    throw VerifyFrame.fail("Illegal anewarray instruction, array has more than 255 dimensions");
                }
                f.push(component.arrayOf());
                return false;
            }
            case 190: {
                VerifyType t = f.pop(REF);
                if (t.kind != VerifyType.NULL && !t.isArray()) {
                    throw VerifyFrame.fail("Bad type");
                }
                f.push(INT);
                return false;
            }
            case 191:
                f.pop(VerifyType.T_THROWABLE);
                return true;
            case 192: {
                VerifyType t = VerifyType.ofDescriptor(classDescriptor(insns, u2(c, bci + 1)));
                f.pop(VerifyType.T_OBJECT);
                f.push(t);
                return false;
            }
            case 193:
                insns.cp(u2(c, bci + 1), ClassEntry.class);
                f.pop(VerifyType.T_OBJECT);
                f.push(INT);
                return false;
            case 194: case 195:
                f.pop(REF);
                return false;
            case 197: {
                int dims = u1(c, bci + 3);
                VerifyType t = VerifyType.ofDescriptor(classDescriptor(insns, u2(c, bci + 1)));
                if (dims < 1 || !t.isArray() || t.dimensions() < dims) {
                    throw VerifyFrame.fail("Illegal dimension in multianewarray instruction: " + dims);
                }
                for (int i = 0; i < dims; i++) {
                    f.pop(INT);
                }
                f.push(t);
                return false;
            }
            default:
                throw VerifyFrame.fail(String.format("Bad instruction: %02x", op));
        }
    }

    private static boolean isSecondHalf(VerifyType t) {
        return t.kind == VerifyType.LONG2 || t.kind == VerifyType.DOUBLE2;
    }

    private static VerifyType popPair(VerifyFrame f, VerifyType top) {
        if (top.isCategory1()) {
            return f.pop(CAT1);
        }
        if (isSecondHalf(top)) {
            return f.pop(CAT2);
        }
        throw VerifyFrame.fail("Bad type");
    }

    private static String classDescriptor(VerifyInsns insns, int index) {
        String n = ((ClassEntry) insns.cp(index, ClassEntry.class)).asInternalName();
        return n.charAt(0) == '[' ? n : "L" + n + ";";
    }

    private static VerifyType primitiveArray(int code) {
        switch (code) {
            case 4: return VerifyType.ref("[Z");
            case 5: return VerifyType.ref("[C");
            case 6: return VerifyType.ref("[F");
            case 7: return VerifyType.ref("[D");
            case 8: return VerifyType.ref("[B");
            case 9: return VerifyType.ref("[S");
            case 10: return VerifyType.ref("[I");
            case 11: return VerifyType.ref("[J");
            default: throw VerifyFrame.fail("Illegal newarray instruction");
        }
    }

    private static void returnValue(MethodVerifier m, VerifyFrame f, VerifyType value) {
        if (m.returnType.kind == VerifyType.BOGUS) {
            throw VerifyFrame.fail("Method expects a return value");
        }
        if (!f.assignable(m.returnType, value)) {
            throw VerifyFrame.fail("Bad return type", m.returnType, value);
        }
    }

    private static void switchInsn(MethodVerifier m, VerifyFrame f, int op, int bci) {
        byte[] c = m.bytes;
        int p = (bci + 4) & ~3;
        int dflt = bci + StackMapGenerator.readInt(c, p);
        int[] targets;
        if (op == 170) {
            int low = StackMapGenerator.readInt(c, p + 4);
            int high = StackMapGenerator.readInt(c, p + 8);
            if (low > high) {
                throw VerifyFrame.fail("low must be less than or equal to high in tableswitch");
            }
            int n = high - low + 1;
            targets = new int[n];
            for (int k = 0; k < n; k++) {
                targets[k] = bci + StackMapGenerator.readInt(c, p + 12 + 4 * k);
            }
        } else {
            int n = StackMapGenerator.readInt(c, p + 4);
            targets = new int[n];
            for (int k = 0; k < n; k++) {
                targets[k] = bci + StackMapGenerator.readInt(c, p + 12 + 8 * k);
            }
        }
        f.pop(INT);
        m.checkJumpTarget(dflt);
        for (int t : targets) {
            m.checkJumpTarget(t);
        }
    }
}
