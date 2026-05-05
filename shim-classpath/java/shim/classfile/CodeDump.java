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
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.classfile.constantpool.ConstantPool;
import java.lang.classfile.constantpool.InvokeDynamicEntry;
import java.lang.classfile.constantpool.LoadableConstantEntry;
import java.lang.classfile.constantpool.MemberRefEntry;
import java.lang.classfile.constantpool.InterfaceMethodRefEntry;
import java.lang.classfile.constantpool.MethodHandleEntry;
import java.lang.constant.ConstantDescs;
import java.lang.constant.DirectMethodHandleDesc;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Prints a method body the way stack map generation errors show it. */
final class CodeDump {
    private static final Map<Integer, String> NAMES = new HashMap<>();

    static {
        for (Opcode o : Opcode.values()) {
            NAMES.put(o.bytecode(), o.name());
        }
    }

    private CodeDump() { }

    static String dump(byte[] code, int codeLength, ConstantPool pool, MethodContext method) {
        if (!targetsInRange(code, codeLength)) {
            return hexDump(code, codeLength);
        }
        StringBuilder sb = new StringBuilder("\n  - max stack: 65535\n    max locals: 65535\n    attributes: []\n");
        sb.append("    //stack map frame @0: {locals: [");
        try {
            initialLocals(sb, method);
            sb.append("], stack: []}\n");
            int bci = 0;
            while (bci < codeLength) {
                int len = StackMapGenerator.instructionLength(code, bci);
                if (len <= 0 || bci + len > codeLength) {
                    break;
                }
                sb.append("    ").append(bci).append(": {opcode: ");
                instruction(sb, code, bci, pool);
                sb.append("}\n");
                bci += len;
            }
        } catch (RuntimeException e) {
            sb.append('\n');
        }
        return sb.toString();
    }

    /** A method whose jumps leave the code cannot be printed as a model, so it is shown as raw bytes. */
    private static String hexDump(byte[] code, int codeLength) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < codeLength; i++) {
            if (i % 16 == 0) {
                sb.append('\n').append(String.format("%04x:", i));
            }
            sb.append(' ').append(String.format("%02x", code[i] & 0xff));
        }
        return sb.toString();
    }

    private static boolean targetsInRange(byte[] code, int codeLength) {
        int bci = 0;
        while (bci < codeLength) {
            int op = code[bci] & 0xff;
            if (op == 170 && StackMapGenerator.invertedTableSwitch(code, bci, codeLength)) {
                return false;
            }
            int len = StackMapGenerator.instructionLength(code, bci);
            if (len <= 0 || bci + len > codeLength) {
                return true;
            }
            if ((op >= 153 && op <= 168) || op == 198 || op == 199) {
                if (!inRange(bci + (short) (((code[bci + 1] & 0xff) << 8) | (code[bci + 2] & 0xff)), codeLength)) {
                    return false;
                }
            } else if (op == 200 || op == 201) {
                if (!inRange(bci + StackMapGenerator.readInt(code, bci + 1), codeLength)) {
                    return false;
                }
            } else if (op == 170 || op == 171) {
                int p = (bci + 4) & ~3;
                if (op == 170 && StackMapGenerator.invertedTableSwitch(code, bci, codeLength)) {
                    return false;
                }
                if (!inRange(bci + StackMapGenerator.readInt(code, p), codeLength)) {
                    return false;
                }
                int n = op == 170 ? StackMapGenerator.readInt(code, p + 8) - StackMapGenerator.readInt(code, p + 4) + 1
                    : StackMapGenerator.readInt(code, p + 4);
                for (int k = 0; k < n; k++) {
                    int off = StackMapGenerator.readInt(code, op == 170 ? p + 12 + 4 * k : p + 12 + 8 * k);
                    if (!inRange(bci + off, codeLength)) {
                        return false;
                    }
                }
            }
            bci += len;
        }
        return true;
    }

    private static boolean inRange(int target, int codeLength) {
        return target >= 0 && target <= codeLength;
    }

    private static void initialLocals(StringBuilder sb, MethodContext m) {
        boolean first = true;
        if (!m.isStatic()) {
            sb.append(m.name.equalsString("<init>") ? "THIS" : quote(m.thisClass.asInternalName()));
            first = false;
        }
        String d = m.type.stringValue();
        int i = 1;
        while (d.charAt(i) != ')') {
            int end = FrameAnalyzer.skipType(d, i);
            if (!first) {
                sb.append(", ");
            }
            first = false;
            switch (d.charAt(i)) {
                case 'J':
                    sb.append("long, long2");
                    break;
                case 'D':
                    sb.append("double, double2");
                    break;
                case 'F':
                    sb.append("float");
                    break;
                case 'L':
                    sb.append(quote(d.substring(i + 1, end - 1)));
                    break;
                case '[':
                    sb.append(quote(d.substring(i, end)));
                    break;
                default:
                    sb.append("int");
                    break;
            }
            i = end;
        }
    }

    private static int u1(byte[] c, int p) { return c[p] & 0xff; }
    private static int u2(byte[] c, int p) { return ((c[p] & 0xff) << 8) | (c[p + 1] & 0xff); }

    private static void instruction(StringBuilder sb, byte[] code, int bci, ConstantPool pool) {
        int op = u1(code, bci);
        if (op == 196) {
            int wop = u1(code, bci + 1);
            sb.append(NAMES.get(0xc400 | wop)).append(", slot: ").append(u2(code, bci + 2));
            if (wop == 132) {
                sb.append(", const: ").append((short) u2(code, bci + 4));
            }
            return;
        }
        sb.append(NAMES.get(op));
        if (op == 1) {
            sb.append(", constant value: ").append(quote(ConstantDescs.NULL.toString()));
        } else if (op >= 2 && op <= 8) {
            sb.append(", constant value: ").append(op - 3);
        } else if (op == 9 || op == 10) {
            sb.append(", constant value: ").append(op - 9);
        } else if (op >= 11 && op <= 13) {
            sb.append(", constant value: ").append((float) (op - 11));
        } else if (op == 14 || op == 15) {
            sb.append(", constant value: ").append((double) (op - 14));
        } else if (op == 16) {
            sb.append(", constant value: ").append((byte) code[bci + 1]);
        } else if (op == 17) {
            sb.append(", constant value: ").append((short) u2(code, bci + 1));
        } else if (op >= 18 && op <= 20) {
            int idx = op == 18 ? u1(code, bci + 1) : u2(code, bci + 1);
            sb.append(", constant value: ").append(value(pool.entryByIndex(idx, LoadableConstantEntry.class)
                .constantValue()));
        } else if ((op >= 21 && op <= 25) || (op >= 54 && op <= 58) || op == 169) {
            sb.append(", slot: ").append(u1(code, bci + 1));
        } else if (op >= 26 && op <= 45) {
            sb.append(", slot: ").append((op - 26) % 4);
        } else if (op >= 59 && op <= 78) {
            sb.append(", slot: ").append((op - 59) % 4);
        } else if (op == 132) {
            sb.append(", slot: ").append(u1(code, bci + 1)).append(", const: ").append((byte) code[bci + 2]);
        } else if ((op >= 153 && op <= 168) || op == 198 || op == 199) {
            sb.append(", target: ").append(bci + (short) u2(code, bci + 1));
        } else if (op == 200 || op == 201) {
            sb.append(", target: ").append(bci + StackMapGenerator.readInt(code, bci + 1));
        } else if (op == 170 || op == 171) {
            switchTargets(sb, code, bci, op);
        } else if (op >= 178 && op <= 185) {
            MemberRefEntry ref = pool.entryByIndex(u2(code, bci + 1), MemberRefEntry.class);
            String kind = op <= 181 ? "field" : "method";
            sb.append(", owner: ").append(quote(ref.owner().asInternalName())).append(", ").append(kind)
                .append(" name: ").append(quote(ref.name().stringValue())).append(", ").append(kind)
                .append(" type: ").append(quote(ref.type().stringValue()));
        } else if (op == 186) {
            invokeDynamic(sb, pool.entryByIndex(u2(code, bci + 1), InvokeDynamicEntry.class));
        } else if (op == 187 || op == 192 || op == 193) {
            sb.append(", type: ").append(quote(pool.entryByIndex(u2(code, bci + 1), ClassEntry.class)
                .asInternalName()));
        } else if (op == 188) {
            sb.append(", dimensions: 1, descriptor: ").append(primitiveName(u1(code, bci + 1)));
        } else if (op == 189) {
            sb.append(", dimensions: 1, descriptor: ").append(quote(pool.entryByIndex(u2(code, bci + 1),
                ClassEntry.class).asInternalName()));
        } else if (op == 197) {
            sb.append(", dimensions: ").append(u1(code, bci + 3)).append(", descriptor: ").append(quote(
                pool.entryByIndex(u2(code, bci + 1), ClassEntry.class).asInternalName()));
        }
    }

    private static void switchTargets(StringBuilder sb, byte[] code, int bci, int op) {
        int p = (bci + 4) & ~3;
        int def = bci + StackMapGenerator.readInt(code, p);
        sb.append(", targets: [").append(def);
        if (op == 170) {
            int n = StackMapGenerator.readInt(code, p + 8) - StackMapGenerator.readInt(code, p + 4) + 1;
            for (int k = 0; k < n; k++) {
                int t = bci + StackMapGenerator.readInt(code, p + 12 + 4 * k);
                if (t != def) {
                    sb.append(", ").append(t);
                }
            }
        } else {
            int n = StackMapGenerator.readInt(code, p + 4);
            for (int k = 0; k < n; k++) {
                sb.append(", ").append(bci + StackMapGenerator.readInt(code, p + 12 + 8 * k));
            }
        }
        sb.append(']');
    }

    private static void invokeDynamic(StringBuilder sb, InvokeDynamicEntry e) {
        MethodHandleEntry bsm = e.bootstrap().bootstrapMethod();
        MemberRefEntry ref = bsm.reference();
        DirectMethodHandleDesc.Kind kind =
            DirectMethodHandleDesc.Kind.valueOf(bsm.kind(), ref instanceof InterfaceMethodRefEntry);
        sb.append(", name: ").append(quote(e.name().stringValue())).append(", descriptor: ")
            .append(quote(e.type().stringValue())).append(", bootstrap method: ").append(kind.name()).append(' ')
            .append(quote(ref.owner().asInternalName() + "::" + ref.name().stringValue())).append(", arguments: [");
        List<LoadableConstantEntry> args = e.bootstrap().arguments();
        for (int i = 0; i < args.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(value(args.get(i).constantValue()));
        }
        sb.append(']');
    }

    private static String primitiveName(int atype) {
        switch (atype) {
            case 4: return "boolean";
            case 5: return "char";
            case 6: return "float";
            case 7: return "double";
            case 8: return "byte";
            case 9: return "short";
            case 10: return "int";
            case 11: return "long";
            default: return String.valueOf(atype);
        }
    }

    private static String value(Object v) {
        return v instanceof Number ? v.toString() : quote(String.valueOf(v));
    }

    static String quote(String s) {
        boolean q = s.isEmpty() || s.equalsIgnoreCase("null") || "#-*&!%@`?|>:".indexOf(s.charAt(0)) >= 0;
        for (int i = 0; i < s.length() && !q; i++) {
            char c = s.charAt(i);
            q = c < 0x20 || c >= 0x7f || ",{}[]'\"\\".indexOf(c) >= 0;
        }
        if (!q) {
            return s;
        }
        StringBuilder sb = new StringBuilder(s.length() + 2).append('\'');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\'': sb.append("''"); break;
                case '\\': sb.append("\\\\"); break;
                case '"': sb.append("\\\""); break;
                case '\n': sb.append("\\n"); break;
                case '\t': sb.append("\\t"); break;
                case '\r': sb.append("\\r"); break;
                case '\f': sb.append("\\f"); break;
                case '\b': sb.append("\\b"); break;
                default:
                    if (c < 0x20 || c >= 0x7f) {
                        sb.append(String.format("\\u%04X", (int) c));
                    } else {
                        sb.append(c);
                    }
                    break;
            }
        }
        return sb.append('\'').toString();
    }
}
