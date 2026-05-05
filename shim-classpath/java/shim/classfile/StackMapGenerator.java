/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassHierarchyResolver;
import java.lang.classfile.Opcode;
import java.lang.classfile.attribute.StackMapFrameInfo;
import java.lang.classfile.attribute.StackMapFrameInfo.SimpleVerificationTypeInfo;
import java.lang.classfile.attribute.StackMapFrameInfo.VerificationTypeInfo;
import java.lang.classfile.constantpool.ConstantPoolBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Computes max stack, max locals and stack map frames from raw bytecode. */
public final class StackMapGenerator {
    /** What the analysis of one method body produced. */
    public static final class Result {
        public final int maxStack;
        public final int maxLocals;
        /** The frames in increasing bci order, excluding the implicit initial frame. */
        public final List<StackMapFrameInfo> frames;
        /** The exception table after dead code removal: {startPc, endPc, handlerPc, catchTypeIndex}. */
        public final List<int[]> handlers;
        /** Whether unreachable code was rewritten in the code array. */
        public final boolean patched;

        Result(int maxStack, int maxLocals, List<StackMapFrameInfo> frames, List<int[]> handlers, boolean patched) {
            this.maxStack = maxStack;
            this.maxLocals = maxLocals;
            this.frames = frames;
            this.handlers = handlers;
            this.patched = patched;
        }
    }

    static final int[] LENGTHS = new int[256];

    static {
        for (Opcode o : Opcode.values()) {
            if (!o.isWide()) {
                LENGTHS[o.bytecode()] = o.sizeIfFixed();
            }
        }
        LENGTHS[196] = -1;
    }

    private StackMapGenerator() { }

    /** Analyzes a method body, patching unreachable code in {@code code} unless the options keep it. */
    public static Result generate(byte[] code, int codeLength, List<int[]> handlers, ConstantPoolBuilder pool,
                                  MethodContext method, ClassHierarchyResolver resolver, LabelContext labels,
                                  boolean wantFrames) {
        if (!wantFrames) {
            return StackCounter.count(code, codeLength, handlers, pool, method);
        }
        boolean patch = method.classFile == null
            || method.classFile.deadCode == ClassFile.DeadCodeOption.PATCH_DEAD_CODE;
        FrameAnalyzer a = new FrameAnalyzer(code, codeLength, handlers, pool, method, resolver);
        a.analyze();
        List<Frame> dead = a.deadFrames();
        List<int[]> outHandlers = handlers;
        if (!dead.isEmpty()) {
            if (!patch) {
                throw a.error("Unable to generate stack map frame for dead code", dead.get(0).bci);
            }
            outHandlers = patchDeadCode(code, codeLength, handlers, a.frames(), dead);
        }
        List<StackMapFrameInfo> frames = new ArrayList<>(a.frames().size());
        PoolImpl temp = new PoolImpl();
        List<VerificationTypeInfo> deadLocals = List.of();
        List<VerificationTypeInfo> deadStack =
            List.<VerificationTypeInfo>of(new ObjectVerificationTypeInfoImpl(temp.classEntry(temp.utf8Entry(
                "java/lang/Throwable"))));
        for (Frame f : a.frames()) {
            if (f.dead) {
                frames.add(new StackMapFrameImpl(255, labels.getLabel(f.bci), deadLocals, deadStack));
            } else {
                frames.add(new StackMapFrameImpl(255, labels.getLabel(f.bci),
                    convert(f.locals, trimmedLocals(f), method, temp, labels),
                    convert(f.stack, f.stackSize, method, temp, labels)));
            }
        }
        int maxStack = a.maxStack;
        if (!dead.isEmpty() && maxStack < 1) {
            maxStack = 1;
        }
        return new Result(maxStack, a.maxLocals, frames, outHandlers, !dead.isEmpty());
    }

    private static int trimmedLocals(Frame f) {
        int n = f.localsSize;
        while (n > 0 && f.locals[n - 1].tag == VType.TOP) {
            n--;
        }
        return n;
    }

    private static List<VerificationTypeInfo> convert(VType[] slots, int size, MethodContext method, PoolImpl temp,
                                                      LabelContext labels) {
        List<VerificationTypeInfo> out = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            VType t = slots[i];
            switch (t.tag) {
                case VType.LONG2:
                case VType.DOUBLE2:
                    break;
                case VType.TOP:
                    out.add(SimpleVerificationTypeInfo.TOP);
                    break;
                case VType.INTEGER:
                    out.add(SimpleVerificationTypeInfo.INTEGER);
                    break;
                case VType.FLOAT:
                    out.add(SimpleVerificationTypeInfo.FLOAT);
                    break;
                case VType.LONG:
                    out.add(SimpleVerificationTypeInfo.LONG);
                    break;
                case VType.DOUBLE:
                    out.add(SimpleVerificationTypeInfo.DOUBLE);
                    break;
                case VType.NULL:
                    out.add(SimpleVerificationTypeInfo.NULL);
                    break;
                case VType.UNINIT_THIS:
                    out.add(SimpleVerificationTypeInfo.UNINITIALIZED_THIS);
                    break;
                case VType.UNINIT:
                    out.add(new UninitializedVerificationTypeInfoImpl(labels.getLabel(t.bci)));
                    break;
                default:
                    if (method.thisClass != null && method.thisClass.name().equalsString(t.name)) {
                        out.add(new ObjectVerificationTypeInfoImpl(method.thisClass));
                    } else {
                        out.add(new ObjectVerificationTypeInfoImpl(temp.classEntry(temp.utf8Entry(t.name))));
                    }
                    break;
            }
        }
        return out;
    }

    private static List<int[]> patchDeadCode(byte[] code, int codeLength, List<int[]> handlers, List<Frame> frames,
                                             List<Frame> dead) {
        List<int[]> ranges = new ArrayList<>();
        for (int i = 0; i < frames.size(); i++) {
            Frame f = frames.get(i);
            if (!f.dead) {
                continue;
            }
            int end = i + 1 < frames.size() ? frames.get(i + 1).bci : codeLength;
            Arrays.fill(code, f.bci, end - 1, (byte) 0);
            code[end - 1] = (byte) 0xbf;
            ranges.add(new int[] {f.bci, end});
        }
        List<int[]> out = new ArrayList<>(handlers.size());
        for (int[] h : handlers) {
            boolean overlaps = false;
            for (int[] r : ranges) {
                if (h[0] < r[1] && h[1] > r[0]) {
                    overlaps = true;
                    break;
                }
            }
            if (!overlaps) {
                out.add(h);
                continue;
            }
            int start = h[0];
            for (int[] r : ranges) {
                if (r[1] <= start || r[0] >= h[1]) {
                    continue;
                }
                if (r[0] > start) {
                    out.add(new int[] {start, r[0], h[2], h[3]});
                }
                start = Math.max(start, r[1]);
            }
            if (start < h[1]) {
                out.add(new int[] {start, h[1], h[2], h[3]});
            }
        }
        return out;
    }

    /** The length of the instruction at {@code bci}, or -1 for an unknown opcode. */
    static int instructionLength(byte[] code, int bci) {
        int op = code[bci] & 0xff;
        int len = LENGTHS[op];
        if (len > 0) {
            return len;
        }
        switch (op) {
            case 170: {
                int p = (bci + 4) & ~3;
                int lo = readInt(code, p + 4);
                int hi = readInt(code, p + 8);
                return p + 12 + 4 * (hi - lo + 1) - bci;
            }
            case 171: {
                int p = (bci + 4) & ~3;
                return p + 8 + 8 * readInt(code, p + 4) - bci;
            }
            case 196:
                return (code[bci + 1] & 0xff) == 132 ? 6 : 4;
            default:
                return -1;
        }
    }

    /** Whether the tableswitch at {@code bci} has its high value below its low value. */
    static boolean invertedTableSwitch(byte[] code, int bci, int codeLength) {
        int p = (bci + 4) & ~3;
        return p + 12 <= codeLength && readInt(code, p + 8) < readInt(code, p + 4);
    }

    /** Whether the keys of the lookupswitch at {@code bci} are strictly increasing. */
    static boolean sortedLookupSwitch(byte[] code, int bci, int codeLength) {
        int p = (bci + 4) & ~3;
        if (p + 8 > codeLength) {
            return true;
        }
        int n = readInt(code, p + 4);
        for (int k = 1; k < n && p + 16 + 8 * k <= codeLength; k++) {
            if (readInt(code, p + 8 + 8 * k) <= readInt(code, p + 8 * k)) {
                return false;
            }
        }
        return true;
    }

    static int readInt(byte[] code, int p) {
        return ((code[p] & 0xff) << 24) | ((code[p + 1] & 0xff) << 16) | ((code[p + 2] & 0xff) << 8)
            | (code[p + 3] & 0xff);
    }
}
