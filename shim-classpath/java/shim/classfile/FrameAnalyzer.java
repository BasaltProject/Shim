/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.ClassHierarchyResolver;
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.classfile.constantpool.ConstantPoolBuilder;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** The data flow analysis behind {@link StackMapGenerator}: frames at every branch target and handler. */
final class FrameAnalyzer {
    private final byte[] code;
    private final int codeLength;
    private final int[][] handlers;
    private final ConstantPoolBuilder pool;
    private final MethodContext method;
    private final TypeHierarchy hierarchy;
    private final String thisName;
    private final List<Frame> frames = new ArrayList<>();
    private int[] frameAt;
    private Frame initial;
    private Frame cur;
    private Frame handlerFrame;
    private Interpreter interpreter;
    int maxStack;
    int maxLocals;

    FrameAnalyzer(byte[] code, int codeLength, List<int[]> handlers, ConstantPoolBuilder pool, MethodContext method,
                  ClassHierarchyResolver resolver) {
        this.code = code;
        this.codeLength = codeLength;
        this.handlers = handlers.toArray(new int[0][]);
        this.pool = pool;
        this.method = method;
        this.hierarchy = new TypeHierarchy(resolver);
        this.thisName = method.thisClass == null ? null : method.thisClass.asInternalName();
    }

    List<Frame> frames() { return frames; }

    List<Frame> deadFrames() {
        List<Frame> dead = new ArrayList<>();
        for (Frame f : frames) {
            if (!f.set) {
                f.dead = true;
                dead.add(f);
            }
        }
        return dead;
    }

    IllegalArgumentException error(String message, int bci) {
        return new IllegalArgumentException(message + " at bytecode offset " + bci + " of method "
            + methodDisplay(method) + CodeDump.dump(code, codeLength, pool, method));
    }

    static String methodDisplay(MethodContext m) {
        MethodTypeDesc mt = MethodTypeDesc.ofDescriptor(m.type.stringValue());
        StringBuilder sb = new StringBuilder(m.name.stringValue()).append('(');
        for (int i = 0; i < mt.parameterCount(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(mt.parameterType(i).displayName());
        }
        return sb.append(')').toString();
    }

    void analyze() {
        initial = initialFrame();
        detectFrames();
        cur = new Frame(-1, maxLocals, 8);
        handlerFrame = new Frame(-1, maxLocals, 1);
        handlerFrame.stackSize = 1;
        interpreter = new Interpreter(this, code, pool);
        boolean dirty;
        do {
            processMethod();
            dirty = false;
            for (Frame f : frames) {
                if (f.dirty) {
                    dirty = true;
                    break;
                }
            }
        } while (dirty);
    }

    private Frame initialFrame() {
        Frame f = new Frame(0, 8, 0);
        int slot = 0;
        if (!method.isStatic()) {
            boolean ctor = method.name.equalsString("<init>") && !"java/lang/Object".equals(thisName);
            f.setLocal(slot++, ctor ? VType.T_UNINIT_THIS : VType.ref(thisName));
        }
        String d = method.type.stringValue();
        int i = 1;
        while (d.charAt(i) != ')') {
            VType t = VType.ofDescriptor(d, i);
            if (t.isCategory2()) {
                f.setLocal2(slot, t);
                slot += 2;
            } else {
                f.setLocal(slot++, t);
            }
            i = skipType(d, i);
        }
        maxLocals = slot;
        return f;
    }

    static int skipType(String d, int i) {
        while (d.charAt(i) == '[') {
            i++;
        }
        return d.charAt(i) == 'L' ? d.indexOf(';', i) + 1 : i + 1;
    }

    private void detectFrames() {
        boolean[] start = new boolean[codeLength + 1];
        boolean[] target = new boolean[codeLength + 1];
        boolean[] afterStop = new boolean[codeLength + 1];
        List<int[]> branches = new ArrayList<>();
        int bci = 0;
        while (bci < codeLength) {
            start[bci] = true;
            int op = code[bci] & 0xff;
            if (op == 170 && StackMapGenerator.invertedTableSwitch(code, bci, codeLength)) {
                throw error("low must be less than or equal to high in tableswitch", bci);
            }
            if (op == 171 && !StackMapGenerator.sortedLookupSwitch(code, bci, codeLength)) {
                throw error("Bad lookupswitch instruction", bci);
            }
            int len = StackMapGenerator.instructionLength(code, bci);
            if (len <= 0 || bci + len > codeLength) {
                throw new IllegalArgumentException("unknown instruction: " + op);
            }
            if ((op >= 153 && op <= 168) || op == 198 || op == 199) {
                branches.add(new int[] {bci, bci + (short) (((code[bci + 1] & 0xff) << 8) | (code[bci + 2] & 0xff))});
            } else if (op == 200 || op == 201) {
                branches.add(new int[] {bci, bci + StackMapGenerator.readInt(code, bci + 1)});
            } else if (op == 170 || op == 171) {
                int p = (bci + 4) & ~3;
                branches.add(new int[] {bci, bci + StackMapGenerator.readInt(code, p)});
                if (op == 170) {
                    int n = StackMapGenerator.readInt(code, p + 8) - StackMapGenerator.readInt(code, p + 4) + 1;
                    for (int k = 0; k < n; k++) {
                        branches.add(new int[] {bci, bci + StackMapGenerator.readInt(code, p + 12 + 4 * k)});
                    }
                } else {
                    int n = StackMapGenerator.readInt(code, p + 4);
                    for (int k = 0; k < n; k++) {
                        branches.add(new int[] {bci, bci + StackMapGenerator.readInt(code, p + 12 + 8 * k)});
                    }
                }
            }
            if (endsFlow(op) && bci + len < codeLength) {
                afterStop[bci + len] = true;
            }
            bci += len;
        }
        for (int[] b : branches) {
            if (b[1] < 0 || b[1] >= codeLength || !start[b[1]]) {
                throw error("Detected branch target out of bytecode range", b[0]);
            }
            target[b[1]] = true;
        }
        for (int[] h : handlers) {
            target[h[2]] = true;
        }
        frameAt = new int[codeLength + 1];
        Arrays.fill(frameAt, -1);
        for (int i = 0; i < codeLength; i++) {
            if (target[i] || afterStop[i]) {
                frameAt[i] = frames.size();
                frames.add(new Frame(i, maxLocals, 4));
            }
        }
    }

    static boolean endsFlow(int op) {
        return (op >= 172 && op <= 177) || op == 167 || op == 200 || op == 191 || op == 170 || op == 171;
    }

    private void processMethod() {
        cur.copyFrom(initial);
        int fi = 0;
        int n = frames.size();
        boolean ncf = false;
        int bci = 0;
        while (bci < codeLength) {
            if (fi < n && frames.get(fi).bci == bci) {
                Frame f = frames.get(fi++);
                if (!ncf) {
                    merge(f, bci);
                }
                while (!f.dirty) {
                    if (fi == n) {
                        return;
                    }
                    f = frames.get(fi++);
                }
                bci = f.bci;
                cur.copyFrom(f);
                f.dirty = false;
            } else if (ncf) {
                return;
            }
            ncf = interpreter.step(bci);
            bci += StackMapGenerator.instructionLength(code, bci);
        }
    }

    Frame current() { return cur; }

    byte[] code() { return code; }

    String thisName() { return thisName; }

    void touchLocal(int size) {
        if (size > maxLocals) {
            maxLocals = size;
        }
    }

    void push(VType t) {
        cur.push(t);
        if (cur.stackSize > maxStack) {
            maxStack = cur.stackSize;
        }
    }

    void push2(VType t) {
        push(t);
        push(t == VType.T_LONG ? VType.T_LONG2 : VType.T_DOUBLE2);
    }

    void pushType(VType t) {
        if (t == null) {
            return;
        }
        if (t.isCategory2()) {
            push2(t);
        } else {
            push(t);
        }
    }

    VType pop(int bci) {
        if (cur.stackSize < 1) {
            throw error("Operand stack underflow", bci);
        }
        return cur.stack[--cur.stackSize];
    }

    void pop(int n, int bci) {
        if (cur.stackSize < n) {
            throw error("Operand stack underflow", bci);
        }
        cur.stackSize -= n;
    }

    void branch(int target, int bci) {
        merge(frames.get(frameAt[target]), bci);
    }

    void mergeHandlers(int bci) {
        for (int[] h : handlers) {
            if (bci >= h[0] && bci < h[1]) {
                Frame hf = handlerFrame;
                hf.locals = cur.locals;
                hf.localsSize = cur.localsSize;
                hf.stack[0] = h[3] == 0 ? VType.T_THROWABLE
                    : VType.ref(pool.entryByIndex(h[3], ClassEntry.class).asInternalName());
                mergeFrom(frames.get(frameAt[h[2]]), hf, bci);
            }
        }
    }

    private void merge(Frame target, int bci) {
        mergeFrom(target, cur, bci);
    }

    private void mergeFrom(Frame target, Frame from, int bci) {
        if (!target.set) {
            target.copyFrom(from);
            target.set = true;
            target.dirty = true;
            if (target.stackSize > maxStack) {
                maxStack = target.stackSize;
            }
            return;
        }
        if (target.stackSize != from.stackSize) {
            throw error("Stack size mismatch", bci);
        }
        for (int i = 0; i < target.stackSize; i++) {
            VType a = target.stack[i];
            VType m = mergeType(a, from.stack[i]);
            if (m == null) {
                throw error("Stack content mismatch", bci);
            }
            if (m != a) {
                target.stack[i] = m;
                target.dirty = true;
            }
        }
        for (int i = 0; i < target.localsSize; i++) {
            VType a = target.locals[i];
            if (a.tag == VType.TOP) {
                continue;
            }
            VType b = i < from.localsSize ? from.locals[i] : VType.T_TOP;
            VType m = mergeType(a, b);
            if (m == null) {
                m = VType.T_TOP;
            }
            if (m != a) {
                target.locals[i] = m;
                target.dirty = true;
            }
        }
    }

    /** Merges {@code b} into {@code a}; null when they are incompatible. */
    private VType mergeType(VType a, VType b) {
        if (a == b || a.equals(b)) {
            return a;
        }
        if (a.isReference() && b.isReference()) {
            if (a.tag == VType.NULL) {
                return b;
            }
            if (b.tag == VType.NULL) {
                return a;
            }
            VType m = hierarchy.merge(a, b);
            return m.equals(a) ? a : m;
        }
        return null;
    }

    int newInstructionClassIndex(int newBci) {
        return ((code[newBci + 1] & 0xff) << 8) | (code[newBci + 2] & 0xff);
    }
}
