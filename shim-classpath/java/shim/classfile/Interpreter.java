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
import java.lang.classfile.constantpool.ConstantPoolBuilder;
import java.lang.classfile.constantpool.DoubleEntry;
import java.lang.classfile.constantpool.FloatEntry;
import java.lang.classfile.constantpool.IntegerEntry;
import java.lang.classfile.constantpool.InvokeDynamicEntry;
import java.lang.classfile.constantpool.LongEntry;
import java.lang.classfile.constantpool.MemberRefEntry;
import java.lang.classfile.constantpool.MethodHandleEntry;
import java.lang.classfile.constantpool.MethodTypeEntry;
import java.lang.classfile.constantpool.PoolEntry;
import java.lang.classfile.constantpool.StringEntry;

/** The effect of each instruction on the current frame of a {@link FrameAnalyzer}. */
final class Interpreter {
    private final FrameAnalyzer a;
    private final byte[] code;
    private final ConstantPoolBuilder pool;

    Interpreter(FrameAnalyzer a, byte[] code, ConstantPoolBuilder pool) {
        this.a = a;
        this.code = code;
        this.pool = pool;
    }

    private int u1(int p) { return code[p] & 0xff; }
    private int u2(int p) { return ((code[p] & 0xff) << 8) | (code[p + 1] & 0xff); }
    private int s2(int p) { return (short) u2(p); }

    /** Applies the instruction at {@code bci}; true when control does not fall through to the next one. */
    boolean step(int bci) {
        a.mergeHandlers(bci);
        int op = u1(bci);
        switch (op) {
            case 0:
                return false;
            case 1:
                a.push(VType.T_NULL);
                return false;
            case 2: case 3: case 4: case 5: case 6: case 7: case 8: case 16: case 17:
                a.push(VType.T_INTEGER);
                return false;
            case 9: case 10:
                a.push2(VType.T_LONG);
                return false;
            case 11: case 12: case 13:
                a.push(VType.T_FLOAT);
                return false;
            case 14: case 15:
                a.push2(VType.T_DOUBLE);
                return false;
            case 18:
                ldc(u1(bci + 1));
                return false;
            case 19: case 20:
                ldc(u2(bci + 1));
                return false;
            case 21: case 22: case 23: case 24: case 25:
                load(op - 21, u1(bci + 1));
                return false;
            case 46: case 51: case 52: case 53:
                a.pop(2, bci);
                a.push(VType.T_INTEGER);
                return false;
            case 47:
                a.pop(2, bci);
                a.push2(VType.T_LONG);
                return false;
            case 48:
                a.pop(2, bci);
                a.push(VType.T_FLOAT);
                return false;
            case 49:
                a.pop(2, bci);
                a.push2(VType.T_DOUBLE);
                return false;
            case 50: {
                a.pop(bci);
                VType arr = a.pop(bci);
                a.push(arr.tag == VType.NULL ? VType.T_NULL : arr.isArray() ? arr.component() : VType.T_TOP);
                return false;
            }
            case 54: case 55: case 56: case 57: case 58:
                store(op - 54, u1(bci + 1), bci);
                return false;
            case 79: case 81: case 83: case 84: case 85: case 86:
                a.pop(3, bci);
                return false;
            case 80: case 82:
                a.pop(4, bci);
                return false;
            case 87:
                a.pop(1, bci);
                return false;
            case 88:
                a.pop(2, bci);
                return false;
            case 89: {
                VType v = a.pop(bci);
                a.push(v);
                a.push(v);
                return false;
            }
            case 90: {
                VType v1 = a.pop(bci);
                VType v2 = a.pop(bci);
                a.push(v1);
                a.push(v2);
                a.push(v1);
                return false;
            }
            case 91: {
                VType v1 = a.pop(bci);
                VType v2 = a.pop(bci);
                VType v3 = a.pop(bci);
                a.push(v1);
                a.push(v3);
                a.push(v2);
                a.push(v1);
                return false;
            }
            case 92: {
                VType v1 = a.pop(bci);
                VType v2 = a.pop(bci);
                a.push(v2);
                a.push(v1);
                a.push(v2);
                a.push(v1);
                return false;
            }
            case 93: {
                VType v1 = a.pop(bci);
                VType v2 = a.pop(bci);
                VType v3 = a.pop(bci);
                a.push(v2);
                a.push(v1);
                a.push(v3);
                a.push(v2);
                a.push(v1);
                return false;
            }
            case 94: {
                VType v1 = a.pop(bci);
                VType v2 = a.pop(bci);
                VType v3 = a.pop(bci);
                VType v4 = a.pop(bci);
                a.push(v2);
                a.push(v1);
                a.push(v4);
                a.push(v3);
                a.push(v2);
                a.push(v1);
                return false;
            }
            case 95: {
                VType v1 = a.pop(bci);
                VType v2 = a.pop(bci);
                a.push(v1);
                a.push(v2);
                return false;
            }
            case 132:
                a.touchLocal(u1(bci + 1) + 1);
                return false;
            case 148: case 151: case 152:
                a.pop(4, bci);
                a.push(VType.T_INTEGER);
                return false;
            case 149: case 150:
                a.pop(2, bci);
                a.push(VType.T_INTEGER);
                return false;
            case 153: case 154: case 155: case 156: case 157: case 158: case 198: case 199:
                a.pop(1, bci);
                a.branch(bci + s2(bci + 1), bci);
                return false;
            case 159: case 160: case 161: case 162: case 163: case 164: case 165: case 166:
                a.pop(2, bci);
                a.branch(bci + s2(bci + 1), bci);
                return false;
            case 167:
                a.branch(bci + s2(bci + 1), bci);
                return true;
            case 200:
                a.branch(bci + StackMapGenerator.readInt(code, bci + 1), bci);
                return true;
            case 168: case 169: case 201:
                throw a.error("Instructions jsr, jsr_w, or ret must not appear in the class file version >= 51.0",
                    bci);
            case 170: case 171:
                a.pop(1, bci);
                switchTargets(op, bci);
                return true;
            case 172: case 174: case 176: case 191:
                a.pop(1, bci);
                return true;
            case 173: case 175:
                a.pop(2, bci);
                return true;
            case 177:
                return true;
            case 178: case 179: case 180: case 181:
                field(op, u2(bci + 1), bci);
                return false;
            case 182: case 183: case 184: case 185:
                invoke(op, u2(bci + 1), bci);
                return false;
            case 186: {
                String d = pool.entryByIndex(u2(bci + 1), InvokeDynamicEntry.class).type().stringValue();
                a.pop(argSlots(d), bci);
                a.pushType(VType.ofDescriptor(d, d.indexOf(')') + 1));
                return false;
            }
            case 187:
                a.push(VType.uninit(bci));
                return false;
            case 188:
                a.pop(1, bci);
                a.push(VType.ref(primitiveArray(u1(bci + 1), bci)));
                return false;
            case 189:
                a.pop(1, bci);
                a.push(classType(u2(bci + 1)).arrayOf());
                return false;
            case 190: case 193:
                a.pop(1, bci);
                a.push(VType.T_INTEGER);
                return false;
            case 192:
                a.pop(1, bci);
                a.push(classType(u2(bci + 1)));
                return false;
            case 194: case 195:
                a.pop(1, bci);
                return false;
            case 196:
                return wide(bci);
            case 197:
                a.pop(u1(bci + 3), bci);
                a.push(classType(u2(bci + 1)));
                return false;
            default:
                break;
        }
        if (op >= 26 && op <= 45) {
            load((op - 26) / 4, (op - 26) % 4);
        } else if (op >= 59 && op <= 78) {
            store((op - 59) / 4, (op - 59) % 4, bci);
        } else if (op >= 96 && op <= 115) {
            int k = (op - 96) % 4;
            a.pop(k == 1 || k == 3 ? 4 : 2, bci);
            pushKind(k);
        } else if (op >= 116 && op <= 119) {
            int k = op - 116;
            a.pop(k == 1 || k == 3 ? 2 : 1, bci);
            pushKind(k);
        } else if (op >= 120 && op <= 125) {
            boolean isLong = (op & 1) == 1;
            a.pop(isLong ? 3 : 2, bci);
            pushKind(isLong ? 1 : 0);
        } else if (op >= 126 && op <= 131) {
            boolean isLong = (op & 1) == 1;
            a.pop(isLong ? 4 : 2, bci);
            pushKind(isLong ? 1 : 0);
        } else if (op >= 133 && op <= 147) {
            convert(op, bci);
        } else {
            throw new IllegalArgumentException("unknown instruction: " + op);
        }
        return false;
    }

    private static final int[] CONVERSIONS = {
        1, 1, 1, 2, 1, 3, 2, 0, 2, 2, 2, 3, 1, 0, 1, 1, 1, 3, 2, 0, 2, 1, 2, 2, 1, 0, 1, 0, 1, 0
    };

    private void convert(int op, int bci) {
        int i = (op - 133) * 2;
        a.pop(CONVERSIONS[i], bci);
        pushKind(CONVERSIONS[i + 1]);
    }

    private void pushKind(int k) {
        switch (k) {
            case 0: a.push(VType.T_INTEGER); break;
            case 1: a.push2(VType.T_LONG); break;
            case 2: a.push(VType.T_FLOAT); break;
            default: a.push2(VType.T_DOUBLE); break;
        }
    }

    private void load(int kind, int slot) {
        Frame f = a.current();
        switch (kind) {
            case 0:
                a.touchLocal(slot + 1);
                a.push(VType.T_INTEGER);
                break;
            case 1:
                a.touchLocal(slot + 2);
                a.push2(VType.T_LONG);
                break;
            case 2:
                a.touchLocal(slot + 1);
                a.push(VType.T_FLOAT);
                break;
            case 3:
                a.touchLocal(slot + 2);
                a.push2(VType.T_DOUBLE);
                break;
            default:
                a.touchLocal(slot + 1);
                a.push(f.local(slot));
                break;
        }
    }

    private void store(int kind, int slot, int bci) {
        Frame f = a.current();
        switch (kind) {
            case 0:
                a.pop(1, bci);
                a.touchLocal(slot + 1);
                f.setLocal(slot, VType.T_INTEGER);
                break;
            case 1:
                a.pop(2, bci);
                a.touchLocal(slot + 2);
                f.setLocal2(slot, VType.T_LONG);
                break;
            case 2:
                a.pop(1, bci);
                a.touchLocal(slot + 1);
                f.setLocal(slot, VType.T_FLOAT);
                break;
            case 3:
                a.pop(2, bci);
                a.touchLocal(slot + 2);
                f.setLocal2(slot, VType.T_DOUBLE);
                break;
            default: {
                VType v = a.pop(bci);
                a.touchLocal(slot + 1);
                f.setLocal(slot, v);
                break;
            }
        }
    }

    private boolean wide(int bci) {
        int op = u1(bci + 1);
        int slot = u2(bci + 2);
        if (op >= 21 && op <= 25) {
            load(op - 21, slot);
        } else if (op >= 54 && op <= 58) {
            store(op - 54, slot, bci);
        } else if (op == 132) {
            a.touchLocal(slot + 1);
        } else if (op == 169) {
            throw a.error("Instructions jsr, jsr_w, or ret must not appear in the class file version >= 51.0", bci);
        } else {
            throw new IllegalArgumentException("unknown instruction: " + op);
        }
        return false;
    }

    private void ldc(int index) {
        PoolEntry e = pool.entryByIndex(index);
        if (e instanceof IntegerEntry) {
            a.push(VType.T_INTEGER);
        } else if (e instanceof FloatEntry) {
            a.push(VType.T_FLOAT);
        } else if (e instanceof LongEntry) {
            a.push2(VType.T_LONG);
        } else if (e instanceof DoubleEntry) {
            a.push2(VType.T_DOUBLE);
        } else if (e instanceof StringEntry) {
            a.push(VType.T_STRING);
        } else if (e instanceof ClassEntry) {
            a.push(VType.T_CLASS);
        } else if (e instanceof MethodTypeEntry) {
            a.push(VType.T_METHOD_TYPE);
        } else if (e instanceof MethodHandleEntry) {
            a.push(VType.T_METHOD_HANDLE);
        } else if (e instanceof ConstantDynamicEntry) {
            String d = ((ConstantDynamicEntry) e).type().stringValue();
            a.pushType(VType.ofDescriptor(d, 0));
        } else {
            throw new IllegalArgumentException("Invalid ldc constant: " + e);
        }
    }

    private void field(int op, int index, int bci) {
        String d = pool.entryByIndex(index, MemberRefEntry.class).type().stringValue();
        VType t = VType.ofDescriptor(d, 0);
        int size = t.isCategory2() ? 2 : 1;
        switch (op) {
            case 178:
                a.pushType(t);
                break;
            case 179:
                a.pop(size, bci);
                break;
            case 180:
                a.pop(1, bci);
                a.pushType(t);
                break;
            default:
                a.pop(size + 1, bci);
                break;
        }
    }

    private void invoke(int op, int index, int bci) {
        MemberRefEntry ref = pool.entryByIndex(index, MemberRefEntry.class);
        String d = ref.type().stringValue();
        a.pop(argSlots(d), bci);
        if (op != 184) {
            VType receiver = a.pop(bci);
            if (op == 183 && ref.name().equalsString("<init>")) {
                Frame f = a.current();
                if (receiver.tag == VType.UNINIT_THIS) {
                    f.replace(receiver, VType.ref(a.thisName()));
                } else if (receiver.tag == VType.UNINIT) {
                    int ci = a.newInstructionClassIndex(receiver.bci);
                    f.replace(receiver, classType(ci));
                } else {
                    throw a.error("Bad operand type when invoking <init>", bci);
                }
                a.mergeHandlers(bci);
            }
        }
        a.pushType(VType.ofDescriptor(d, d.indexOf(')') + 1));
    }

    private VType classType(int index) {
        return VType.ref(pool.entryByIndex(index, ClassEntry.class).asInternalName());
    }

    private String primitiveArray(int atype, int bci) {
        switch (atype) {
            case 4: return "[Z";
            case 5: return "[C";
            case 6: return "[F";
            case 7: return "[D";
            case 8: return "[B";
            case 9: return "[S";
            case 10: return "[I";
            case 11: return "[J";
            default: throw a.error("Illegal newarray instruction type " + atype, bci);
        }
    }

    private void switchTargets(int op, int bci) {
        int p = (bci + 4) & ~3;
        a.branch(bci + StackMapGenerator.readInt(code, p), bci);
        if (op == 170) {
            int n = StackMapGenerator.readInt(code, p + 8) - StackMapGenerator.readInt(code, p + 4) + 1;
            for (int k = 0; k < n; k++) {
                a.branch(bci + StackMapGenerator.readInt(code, p + 12 + 4 * k), bci);
            }
        } else {
            int n = StackMapGenerator.readInt(code, p + 4);
            for (int k = 0; k < n; k++) {
                a.branch(bci + StackMapGenerator.readInt(code, p + 12 + 8 * k), bci);
            }
        }
    }

    static int argSlots(String d) {
        int n = 0;
        int i = 1;
        while (d.charAt(i) != ')') {
            char c = d.charAt(i);
            n += c == 'J' || c == 'D' ? 2 : 1;
            i = FrameAnalyzer.skipType(d, i);
        }
        return n;
    }
}
