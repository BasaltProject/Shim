/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.Attributes;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.MethodModel;
import java.lang.classfile.attribute.CodeAttribute;
import java.lang.classfile.attribute.StackMapFrameInfo;
import java.lang.classfile.attribute.StackMapFrameInfo.ObjectVerificationTypeInfo;
import java.lang.classfile.attribute.StackMapFrameInfo.SimpleVerificationTypeInfo;
import java.lang.classfile.attribute.StackMapFrameInfo.UninitializedVerificationTypeInfo;
import java.lang.classfile.attribute.StackMapFrameInfo.VerificationTypeInfo;
import java.lang.classfile.attribute.StackMapTableAttribute;
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.classfile.constantpool.ConstantPool;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/** Checks one method body against its stack map frames, one instruction at a time. */
final class MethodVerifier {
    final ClassModel cls;
    final MethodModel method;
    final CodeAttribute code;
    final byte[] bytes;
    final int codeLength;
    final ConstantPool pool;
    final VerifyHierarchy hierarchy;
    final String thisName;
    final VerifyType thisType;
    final String methodName;
    final String descriptor;
    final VerifyType returnType;
    final int maxLocals;
    final int maxStack;
    private final List<VerifyFrame> frames = new ArrayList<>();
    private final List<int[]> handlers = new ArrayList<>();
    VerifyFrame current;
    int bci;
    private VerifyType[] shownLocals;
    private VerifyType[] shownStack;

    MethodVerifier(ClassModel cls, MethodModel method, CodeAttribute code, VerifyHierarchy hierarchy) {
        this.cls = cls;
        this.method = method;
        this.code = code;
        this.bytes = code == null ? new byte[0] : code.codeArray();
        this.codeLength = bytes.length;
        this.pool = cls.constantPool();
        this.hierarchy = hierarchy;
        this.thisName = cls.thisClass().asInternalName();
        this.thisType = VerifyType.ref(thisName);
        this.methodName = method.methodName().stringValue();
        this.descriptor = method.methodType().stringValue();
        String ret = descriptor.substring(descriptor.indexOf(')') + 1);
        this.returnType = ret.equals("V") ? VerifyType.T_BOGUS : VerifyType.ofReturn(ret);
        this.maxLocals = code == null ? 0 : code.maxLocals();
        this.bci = hierarchy.lastOffset;
        this.maxStack = code == null ? 0 : code.maxStack();
    }

    /** The message of a verification error at the current instruction. */
    String message(String text, String detail, String frames, boolean noOffset) {
        MethodTypeDesc mt = MethodTypeDesc.ofDescriptor(descriptor);
        StringBuilder sb = new StringBuilder(text).append(" in ").append(thisName).append("::").append(methodName)
            .append('(');
        for (int i = 0; i < mt.parameterCount(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(mt.parameterType(i).displayName());
        }
        sb.append(')');
        if (noOffset) {
            return sb.toString();
        }
        sb.append(" @").append(bci);
        if (frames != null) {
            sb.append(' ');
            if (detail != null) {
                sb.append('(').append(detail).append(')');
            }
            return sb.append(frames).toString();
        }
        if (detail != null) {
            sb.append(" (").append(detail).append(')');
        }
        return sb.toString();
    }

    void verify() {
        current = initialFrame();
        boolean[] starts = new boolean[codeLength];
        for (int p = 0; p < codeLength; ) {
            int len = StackMapGenerator.instructionLength(bytes, p);
            if (len <= 0 || p + len > codeLength) {
                int op = bytes[p] & 0xff;
                throw new VerifyFrame.Failure(op == 196 ? "unknown wide instruction: " + (bytes[p + 1] & 0xff)
                    : "unknown instruction: " + op, null, true);
            }
            starts[p] = true;
            p += len;
        }
        readHandlers(starts);
        readFrames(starts);
        VerifyInsns insns = new VerifyInsns(this);
        int frameIndex = 0;
        boolean noControlFlow = false;
        bci = 0;
        while (bci < codeLength) {
            int op = bytes[bci] & 0xff;
            int len = StackMapGenerator.instructionLength(bytes, bci);
            if (frameIndex < frames.size()) {
                VerifyFrame f = frames.get(frameIndex);
                if (noControlFlow && f.offset > bci) {
                    throw VerifyFrame.fail("Expecting a stack map frame");
                }
                if (f.offset == bci) {
                    if (!noControlFlow) {
                        current.offset = bci;
                        current.checkAssignableTo(f, current.shownLocals(), current.shownStack(maxStack));
                    }
                    f.copyTo(current);
                    frameIndex++;
                } else if (f.offset < bci) {
                    throw VerifyFrame.fail("Bad stack map offset");
                }
            } else if (noControlFlow) {
                throw VerifyFrame.fail("Expecting a stack map frame");
            }
            shownLocals = current.shownLocals();
            shownStack = current.shownStack(maxStack);
            boolean store = isStore(op, bytes, bci);
            if (store) {
                checkHandlers();
            }
            noControlFlow = insns.execute(op, len);
            if (!store) {
                checkHandlers();
            }
            bci += len;
        }
        bci = lastInstruction();
        if (!noControlFlow) {
            throw VerifyFrame.fail("Control flow falls through code end");
        }
    }

    private int lastInstruction() {
        int last = 0;
        for (int p = 0; p < codeLength; p += StackMapGenerator.instructionLength(bytes, p)) {
            last = p;
        }
        return last;
    }

    /** The number of local slots the receiver and parameters take. */
    static int argumentSlots(MethodModel method) {
        int receiver = (method.flags().flagsMask() & ClassFile.ACC_STATIC) == 0 ? 1 : 0;
        return receiver + VerifyInsns.argumentTypes(method.methodType().stringValue()).size();
    }

    private static boolean isStore(int op, byte[] code, int bci) {
        if (op == 196) {
            op = code[bci + 1] & 0xff;
        }
        return op >= 54 && op <= 78;
    }

    private VerifyFrame initialFrame() {
        VerifyFrame f = new VerifyFrame(maxLocals, maxStack, hierarchy);
        int slot = 0;
        if ((method.flags().flagsMask() & ClassFile.ACC_STATIC) == 0) {
            if (methodName.equals("<init>") && !thisName.equals("java/lang/Object")) {
                f.setLocal(slot++, VerifyType.T_UNINIT_THIS);
                f.thisUninit = true;
            } else {
                f.setLocal(slot++, thisType);
            }
        }
        MethodTypeDesc mt = MethodTypeDesc.ofDescriptor(descriptor);
        for (int i = 0; i < mt.parameterCount(); i++) {
            VerifyType t = VerifyType.ofDescriptor(mt.parameterType(i).descriptorString());
            if (t.isCategory2()) {
                f.setLocal2(slot, t);
                slot += 2;
            } else {
                f.setLocal(slot++, t);
            }
        }
        return f;
    }

    private void readFrames(boolean[] starts) {
        Optional<StackMapTableAttribute> sm = code.findAttribute(Attributes.stackMapTable());
        if (!sm.isPresent()) {
            return;
        }
        if (sm.get() instanceof StackMapTableAttr) {
            int declared = ((StackMapTableAttr) sm.get()).declaredLength();
            int used = AttributeSizes.of(sm.get());
            if (used > declared) {
                throw formatError("access beyond the end of attribute");
            }
            if (used < declared) {
                throw formatError("wrong attribute size");
            }
        }
        int prevReal = current.localsSize;
        VerifyType[] prevPrint = current.locals;
        for (StackMapFrameInfo info : sm.get().entries()) {
            VerifyFrame f = new VerifyFrame(maxLocals, maxStack, hierarchy);
            f.offset = code.labelToBci(info.target());
            if (f.offset >= codeLength || !starts[f.offset]) {
                throw VerifyFrame.fail("StackMapTable error: bad offset");
            }
            int n = 0;
            for (VerificationTypeInfo v : info.locals()) {
                VerifyType t = convert(v);
                if (n >= maxLocals || t.isCategory2() && n + 1 >= maxLocals) {
                    throw formatError();
                }
                f.locals[n++] = t;
                if (t.isCategory2()) {
                    f.locals[n++] = t.secondHalf();
                }
                if (t.kind == VerifyType.UNINIT_THIS) {
                    f.thisUninit = true;
                }
            }
            f.localsSize = n;
            int s = 0;
            for (VerificationTypeInfo v : info.stack()) {
                VerifyType t = convert(v);
                if (s >= maxStack || t.isCategory2() && s + 1 >= maxStack) {
                    throw formatError();
                }
                f.stack[s++] = t;
                if (t.isCategory2()) {
                    f.stack[s++] = t.secondHalf();
                }
            }
            f.stackSize = s;
            int type = info.frameType();
            boolean first = frames.isEmpty();
            if (type < 128 || type == 247 || type == 251) {
                f.localsPrint = first ? Arrays.copyOf(f.locals, prevReal) : prevPrint;
                f.stackPrint = type < 64 || type == 251 ? new VerifyType[0] : Arrays.copyOf(f.stack, 2);
            } else if (type < 251) {
                f.localsPrint = prevPrint.clone();
                f.stackPrint = new VerifyType[0];
            } else if (type < 255) {
                f.localsPrint = Arrays.copyOf(f.locals, prevReal + 2 * (type - 251));
                f.stackPrint = new VerifyType[0];
            } else {
                f.localsPrint = Arrays.copyOf(f.locals, 2 * info.locals().size());
                f.stackPrint = Arrays.copyOf(f.stack, 2 * info.stack().size());
            }
            for (int i = n; i < f.localsPrint.length && f.localsPrint != prevPrint && type >= 251; i++) {
                f.localsPrint[i] = null;
            }
            if (f.stackPrint.length == 2 && s == 1) {
                f.stackPrint[1] = null;
            }
            for (int i = s; i < f.stackPrint.length; i++) {
                f.stackPrint[i] = null;
            }
            prevPrint = f.localsPrint;
            prevReal = n;
            frames.add(f);
        }
    }

    private VerifyFrame.Failure formatError() {
        return formatError("StackMapTable format error: bad type array size");
    }

    private VerifyFrame.Failure formatError(String message) {
        VerifyFrame.Failure f = VerifyFrame.fail(message);
        f.noOffset = true;
        return f;
    }

    private VerifyType convert(VerificationTypeInfo v) {
        if (v instanceof ObjectVerificationTypeInfo) {
            return VerifyType.ref(((ObjectVerificationTypeInfo) v).className().asInternalName());
        }
        if (v instanceof UninitializedVerificationTypeInfo) {
            int offset = code.labelToBci(((UninitializedVerificationTypeInfo) v).newTarget());
            if (offset < 0 || offset >= codeLength || (bytes[offset] & 0xff) != 187) {
                throw formatError("StackMapTable format error: bad offset for Uninitialized");
            }
            return VerifyType.uninit(offset);
        }
        switch ((SimpleVerificationTypeInfo) v) {
            case INTEGER: return VerifyType.T_INTEGER;
            case FLOAT: return VerifyType.T_FLOAT;
            case LONG: return VerifyType.T_LONG;
            case DOUBLE: return VerifyType.T_DOUBLE;
            case NULL: return VerifyType.T_NULL;
            case UNINITIALIZED_THIS: return VerifyType.T_UNINIT_THIS;
            default: return VerifyType.T_BOGUS;
        }
    }

    private void readHandlers(boolean[] starts) {
        for (java.lang.classfile.instruction.ExceptionCatch c : code.exceptionHandlers()) {
            int start = code.labelToBci(c.tryStart());
            int end = code.labelToBci(c.tryEnd());
            int handler = code.labelToBci(c.handler());
            ClassEntry type = c.catchType().orElse(null);
            if (start >= codeLength || !starts[start]) {
                throw formatError("Illegal exception table start_pc " + start);
            }
            if (end != codeLength && (end > codeLength || !starts[end])) {
                throw formatError("Illegal exception table end_pc " + end);
            }
            if (handler >= codeLength || !starts[handler]) {
                throw formatError("Illegal exception table handler_pc " + handler);
            }
            handlers.add(new int[] {start, end, handler, type == null ? 0 : type.index()});
            if (type != null) {
                VerifyType t = VerifyType.ref(type.asInternalName());
                if (!VerifyType.T_THROWABLE.isAssignableFrom(t, hierarchy)) {
                    throw VerifyFrame.fail("Catch type is not a subclass of Throwable in exception handler "
                        + handler, VerifyType.T_THROWABLE, t);
                }
            }
        }
    }

    /** The frame recorded for {@code target}, or null. */
    VerifyFrame frameAt(int target) {
        for (VerifyFrame f : frames) {
            if (f.offset == target) {
                return f;
            }
        }
        return null;
    }

    void checkJumpTarget(int target) {
        VerifyFrame f = frameAt(target);
        if (f == null) {
            throw VerifyFrame.fail("Expecting a stackmap frame at branch target " + target);
        }
        current.offset = bci;
        current.checkAssignableTo(f, shownLocals, shownStack);
    }

    private void checkHandlers() {
        for (int[] h : handlers) {
            if (bci < h[0] || bci >= h[1]) {
                continue;
            }
            VerifyType catchType = h[3] == 0 ? VerifyType.T_THROWABLE
                : VerifyType.ref(((ClassEntry) pool.entryByIndex(h[3])).asInternalName());
            VerifyFrame f = current.copy();
            f.stackSize = 0;
            if (maxStack < 1) {
                throw VerifyFrame.fail("Operand stack overflow");
            }
            f.stack[f.stackSize++] = catchType;
            VerifyFrame target = frameAt(h[2]);
            if (target == null) {
                throw VerifyFrame.fail("Expecting a stackmap frame at branch target " + h[2]);
            }
            f.offset = bci;
            f.checkAssignableTo(target, f.shownLocals(), f.shownStack(1));
        }
    }
}
