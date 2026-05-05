/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.constantpool.ConstantDynamicEntry;
import java.lang.classfile.constantpool.ConstantPool;
import java.lang.classfile.constantpool.DoubleEntry;
import java.lang.classfile.constantpool.InvokeDynamicEntry;
import java.lang.classfile.constantpool.LongEntry;
import java.lang.classfile.constantpool.MemberRefEntry;
import java.lang.classfile.constantpool.PoolEntry;
import java.util.ArrayDeque;
import java.util.List;

/** Max stack and max locals of a method body that gets no stack maps (class files older than version 50). */
final class StackCounter {
    private static final byte VARIABLE = 99;
    private static final byte[] DELTA = new byte[256];

    static {
        for (int op = 0; op < 256; op++) {
            DELTA[op] = (byte) fixedDelta(op);
        }
    }

    private static int fixedDelta(int op) {
        if (op == 1 || (op >= 2 && op <= 8) || op == 11 || op == 12 || op == 13 || op == 16 || op == 17 || op == 21
                || op == 23 || op == 25 || (op >= 26 && op <= 29) || (op >= 34 && op <= 37) || (op >= 42 && op <= 45)
                || op == 89 || op == 90 || op == 91 || op == 133 || op == 135 || op == 140 || op == 141 || op == 187) {
            return 1;
        }
        if (op == 9 || op == 10 || op == 14 || op == 15 || op == 22 || op == 24 || (op >= 30 && op <= 33)
                || (op >= 38 && op <= 41) || op == 92 || op == 93 || op == 94) {
            return 2;
        }
        if (op >= 96 && op <= 115) {
            return (op - 96) % 2 == 0 ? -1 : -2;
        }
        if (op >= 126 && op <= 131) {
            return (op & 1) == 0 ? -1 : -2;
        }
        switch (op) {
            case 18: case 19: case 20: case 178: case 179: case 180: case 181: case 182: case 183: case 184:
            case 185: case 186: case 196: case 197:
                return VARIABLE;
            case 46: case 48: case 50: case 51: case 52: case 53: case 54: case 56: case 58: case 87:
            case 120: case 121: case 122: case 123: case 124: case 125: case 136: case 137: case 142: case 144:
            case 149: case 150: case 153: case 154: case 155: case 156: case 157: case 158: case 170: case 171:
            case 172: case 174: case 176: case 191: case 194: case 195: case 198: case 199:
                return -1;
            case 55: case 57: case 88: case 159: case 160: case 161: case 162: case 163: case 164: case 165:
            case 166: case 173: case 175:
                return -2;
            case 79: case 81: case 83: case 84: case 85: case 86: case 148: case 151: case 152:
                return -3;
            case 80: case 82:
                return -4;
            default:
                if ((op >= 59 && op <= 62) || (op >= 67 && op <= 70) || (op >= 75 && op <= 78)) {
                    return -1;
                }
                if ((op >= 63 && op <= 66) || (op >= 71 && op <= 74)) {
                    return -2;
                }
                return 0;
        }
    }

    private StackCounter() { }

    static StackMapGenerator.Result count(byte[] code, int codeLength, List<int[]> handlers, ConstantPool pool,
                                          MethodContext method) {
        int maxLocals = (method.isStatic() ? 0 : 1) + Interpreter.argSlots(method.type.stringValue());
        int maxStack = 0;
        boolean ret = false;
        boolean[] visited = new boolean[codeLength];
        ArrayDeque<int[]> work = new ArrayDeque<>();
        for (int[] h : handlers) {
            work.add(new int[] {h[2], 1});
        }
        work.add(new int[] {0, 0});
        while (!work.isEmpty()) {
            int[] w = work.poll();
            int bci = w[0];
            int stack = w[1];
            if (bci < 0) {
                throw new IndexOutOfBoundsException("bitIndex < 0: " + bci);
            }
            if (!visited[bci] && stack > maxStack) {
                maxStack = stack;
            }
            while (bci < codeLength && !visited[bci]) {
                visited[bci] = true;
                int op = code[bci] & 0xff;
                if (op == 171 && !StackMapGenerator.sortedLookupSwitch(code, bci, codeLength)) {
                    throw new IllegalArgumentException("Bad lookupswitch instruction at bytecode offset " + bci
                        + " of method " + FrameAnalyzer.methodDisplay(method) + CodeDump.dump(code, codeLength, pool,
                        method));
                }
                if (op == 170 && StackMapGenerator.invertedTableSwitch(code, bci, codeLength)) {
                    throw new IllegalArgumentException("low must be less than or equal to high in tableswitch at "
                        + "bytecode offset " + bci + " of method " + FrameAnalyzer.methodDisplay(method)
                        + CodeDump.dump(code, codeLength, pool, method));
                }
                int len = StackMapGenerator.instructionLength(code, bci);
                if (len <= 0) {
                    throw new IllegalArgumentException("unknown instruction: " + op);
                }
                int delta = DELTA[op];
                if (delta == VARIABLE) {
                    delta = variableDelta(code, bci, op, pool);
                }
                stack += delta;
                if (stack > maxStack) {
                    maxStack = stack;
                }
                maxLocals = Math.max(maxLocals, localsUsed(code, bci, op));
                boolean stop = false;
                if ((op >= 153 && op <= 166) || op == 198 || op == 199) {
                    work.add(new int[] {bci + (short) (((code[bci + 1] & 0xff) << 8) | (code[bci + 2] & 0xff)), stack});
                } else if (op == 167 || op == 200) {
                    int off = op == 167 ? (short) (((code[bci + 1] & 0xff) << 8) | (code[bci + 2] & 0xff))
                        : StackMapGenerator.readInt(code, bci + 1);
                    work.add(new int[] {bci + off, stack});
                    stop = true;
                } else if (op == 168 || op == 201) {
                    int off = op == 168 ? (short) (((code[bci + 1] & 0xff) << 8) | (code[bci + 2] & 0xff))
                        : StackMapGenerator.readInt(code, bci + 1);
                    work.add(new int[] {bci + off, stack + 1});
                } else if (op == 170 || op == 171) {
                    int p = (bci + 4) & ~3;
                    work.add(new int[] {bci + StackMapGenerator.readInt(code, p), stack});
                    if (op == 170) {
                        int n = StackMapGenerator.readInt(code, p + 8) - StackMapGenerator.readInt(code, p + 4) + 1;
                        for (int k = 0; k < n; k++) {
                            work.add(new int[] {bci + StackMapGenerator.readInt(code, p + 12 + 4 * k), stack});
                        }
                    } else {
                        int n = StackMapGenerator.readInt(code, p + 4);
                        for (int k = 0; k < n; k++) {
                            work.add(new int[] {bci + StackMapGenerator.readInt(code, p + 12 + 8 * k), stack});
                        }
                    }
                    stop = true;
                } else if (op == 169 || (op == 196 && (code[bci + 1] & 0xff) == 169)) {
                    ret = true;
                    stop = true;
                } else if ((op >= 172 && op <= 177) || op == 191) {
                    stop = true;
                }
                if (stop) {
                    break;
                }
                bci += len;
            }
        }
        return new StackMapGenerator.Result(ret ? maxStack * 2 : maxStack, maxLocals,
            java.util.Collections.<java.lang.classfile.attribute.StackMapFrameInfo>emptyList(), handlers, false);
    }

    private static int localsUsed(byte[] code, int bci, int op) {
        if ((op >= 21 && op <= 25) || (op >= 54 && op <= 58)) {
            int k = op <= 25 ? op - 21 : op - 54;
            return (code[bci + 1] & 0xff) + (k == 1 || k == 3 ? 2 : 1);
        }
        if (op >= 26 && op <= 45) {
            int k = (op - 26) / 4;
            return (op - 26) % 4 + (k == 1 || k == 3 ? 2 : 1);
        }
        if (op >= 59 && op <= 78) {
            int k = (op - 59) / 4;
            return (op - 59) % 4 + (k == 1 || k == 3 ? 2 : 1);
        }
        if (op == 132 || op == 169) {
            return (code[bci + 1] & 0xff) + 1;
        }
        if (op == 196) {
            int wop = code[bci + 1] & 0xff;
            int slot = ((code[bci + 2] & 0xff) << 8) | (code[bci + 3] & 0xff);
            boolean wide2 = wop == 22 || wop == 24 || wop == 55 || wop == 57;
            return slot + (wide2 ? 2 : 1);
        }
        return 0;
    }

    private static int variableDelta(byte[] code, int bci, int op, ConstantPool pool) {
        switch (op) {
            case 18:
            case 19:
            case 20: {
                int idx = op == 18 ? code[bci + 1] & 0xff : ((code[bci + 1] & 0xff) << 8) | (code[bci + 2] & 0xff);
                PoolEntry e = pool.entryByIndex(idx);
                if (e instanceof LongEntry || e instanceof DoubleEntry) {
                    return 2;
                }
                if (e instanceof ConstantDynamicEntry) {
                    char c = ((ConstantDynamicEntry) e).type().stringValue().charAt(0);
                    return c == 'J' || c == 'D' ? 2 : 1;
                }
                return 1;
            }
            case 178:
            case 179:
            case 180:
            case 181: {
                char c = pool.entryByIndex(u2(code, bci + 1), MemberRefEntry.class).type().stringValue().charAt(0);
                int size = c == 'J' || c == 'D' ? 2 : 1;
                switch (op) {
                    case 178: return size;
                    case 179: return -size;
                    case 180: return size - 1;
                    default: return -size - 1;
                }
            }
            case 186: {
                String d = pool.entryByIndex(u2(code, bci + 1), InvokeDynamicEntry.class).type().stringValue();
                return returnSlots(d) - Interpreter.argSlots(d);
            }
            case 196: {
                int wop = code[bci + 1] & 0xff;
                if (wop >= 21 && wop <= 25) {
                    return wop == 22 || wop == 24 ? 2 : 1;
                }
                if (wop >= 54 && wop <= 58) {
                    return wop == 55 || wop == 57 ? -2 : -1;
                }
                return 0;
            }
            case 197:
                return 1 - (code[bci + 3] & 0xff);
            default: {
                String d = pool.entryByIndex(u2(code, bci + 1), MemberRefEntry.class).type().stringValue();
                return returnSlots(d) - Interpreter.argSlots(d) - (op == 184 ? 0 : 1);
            }
        }
    }

    private static int u2(byte[] code, int p) {
        return ((code[p] & 0xff) << 8) | (code[p + 1] & 0xff);
    }

    private static int returnSlots(String d) {
        char c = d.charAt(d.indexOf(')') + 1);
        return c == 'V' ? 0 : c == 'J' || c == 'D' ? 2 : 1;
    }
}
