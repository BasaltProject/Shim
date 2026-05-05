/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.util.Arrays;

/** The locals and operand stack the verifier tracks at one point of a method. */
final class VerifyFrame {
    /** Thrown to report the first error found in a method. */
    static final class Failure extends RuntimeException {
        private static final long serialVersionUID = 1L;
        final String detail;
        final boolean bare;
        String frames;
        boolean noOffset;

        Failure(String message, String detail) {
            this(message, detail, false);
        }

        Failure(String message, String detail, boolean bare) {
            super(message, null, false, false);
            this.detail = detail;
            this.bare = bare;
        }
    }

    final int maxLocals;
    final int maxStack;
    final VerifyType[] locals;
    final VerifyType[] stack;
    int localsSize;
    int stackSize;
    boolean thisUninit;
    int offset;
    /** The arrays a stack map frame was read into, which may be longer than its types; null for working frames. */
    VerifyType[] localsPrint;
    VerifyType[] stackPrint;
    private final VerifyHierarchy hierarchy;

    VerifyFrame(int maxLocals, int maxStack, VerifyHierarchy hierarchy) {
        this.maxLocals = maxLocals;
        this.maxStack = maxStack;
        this.locals = new VerifyType[maxLocals];
        this.stack = new VerifyType[maxStack];
        this.hierarchy = hierarchy;
        Arrays.fill(locals, VerifyType.T_BOGUS);
        Arrays.fill(stack, VerifyType.T_BOGUS);
    }

    static Failure fail(String message) {
        return new Failure(message, null);
    }

    static Failure fail(String message, VerifyType expected, VerifyType found) {
        return new Failure(message, expected + " is not assignable from " + found);
    }

    VerifyFrame copy() {
        VerifyFrame f = new VerifyFrame(maxLocals, maxStack, hierarchy);
        copyTo(f);
        return f;
    }

    void copyTo(VerifyFrame f) {
        System.arraycopy(locals, 0, f.locals, 0, maxLocals);
        System.arraycopy(stack, 0, f.stack, 0, maxStack);
        f.localsSize = localsSize;
        f.stackSize = stackSize;
        f.thisUninit = thisUninit;
        f.offset = offset;
    }

    boolean assignable(VerifyType to, VerifyType from) {
        return to.isAssignableFrom(from, hierarchy);
    }

    void push(VerifyType t) {
        if (stackSize >= maxStack) {
            throw fail("Operand stack overflow");
        }
        stack[stackSize++] = t;
    }

    /** Pushes a category 2 type and its second half. */
    void push2(VerifyType t) {
        if (stackSize + 1 >= maxStack) {
            throw fail("Operand stack overflow");
        }
        stack[stackSize++] = t;
        stack[stackSize++] = t.secondHalf();
    }

    /** Pushes the type of a value of a field descriptor, using two slots for long and double. */
    void pushValue(VerifyType t) {
        if (t.isCategory2()) {
            push2(t);
        } else {
            push(t);
        }
    }

    VerifyType pop(VerifyType expected) {
        if (stackSize <= 0) {
            throw fail("Operand stack underflow");
        }
        VerifyType top = stack[--stackSize];
        if (!assignable(expected, top)) {
            throw fail("Bad type on operand stack", expected, top);
        }
        return top;
    }

    /** Pops a long or double: the second half first, then the value itself. */
    void pop2(VerifyType t) {
        pop(t.secondHalf());
        pop(t);
    }

    /** Pops a value of a field descriptor type. */
    void popValue(VerifyType t) {
        if (t.isCategory2()) {
            pop2(t);
        } else {
            pop(t);
        }
    }

    VerifyType peek() {
        if (stackSize <= 0) {
            throw fail("Operand stack underflow");
        }
        return stack[stackSize - 1];
    }

    VerifyType getLocal(int index, VerifyType expected) {
        if (index >= maxLocals) {
            throw fail("Local variable table overflow");
        }
        VerifyType t = locals[index];
        if (!assignable(expected, t)) {
            throw fail("Bad local variable type", expected, t);
        }
        if (index >= localsSize) {
            localsSize = index + 1;
        }
        return t;
    }

    void getLocal2(int index, VerifyType t) {
        if (index >= localsSize - 1) {
            throw fail("get long/double overflows locals");
        }
        if (!assignable(t, locals[index])) {
            throw fail("Bad local variable type", t, locals[index]);
        }
        if (!assignable(t.secondHalf(), locals[index + 1])) {
            throw fail("Bad local variable type", t.secondHalf(), locals[index + 1]);
        }
    }

    void setLocal(int index, VerifyType t) {
        if (index >= maxLocals) {
            throw fail("Local variable table overflow");
        }
        clearHalves(index);
        locals[index] = t;
        if (index >= localsSize) {
            localsSize = index + 1;
        }
    }

    void setLocal2(int index, VerifyType t) {
        if (index >= maxLocals - 1) {
            throw fail("Local variable table overflow");
        }
        clearHalves(index);
        clearHalves(index + 1);
        locals[index] = t;
        locals[index + 1] = t.secondHalf();
        if (index + 1 >= localsSize) {
            localsSize = index + 2;
        }
    }

    private void clearHalves(int index) {
        VerifyType old = locals[index];
        if ((old.kind == VerifyType.LONG || old.kind == VerifyType.DOUBLE) && index + 1 < maxLocals) {
            locals[index + 1] = VerifyType.T_BOGUS;
        }
        if ((old.kind == VerifyType.LONG2 || old.kind == VerifyType.DOUBLE2) && index >= 1) {
            locals[index - 1] = VerifyType.T_BOGUS;
        }
    }

    /** Replaces every occurrence of an uninitialized type once its constructor has run. */
    void initialize(VerifyType from, VerifyType to) {
        for (int i = 0; i < maxLocals; i++) {
            if (locals[i].equals(from)) {
                locals[i] = to;
            }
        }
        for (int i = 0; i < stackSize; i++) {
            if (stack[i].equals(from)) {
                stack[i] = to;
            }
        }
        if (from.kind == VerifyType.UNINIT_THIS) {
            thisUninit = false;
        }
    }

    /** Fails with the reason and both frames when this frame may not flow into {@code target}. */
    void checkAssignableTo(VerifyFrame target, VerifyType[] shownLocals, VerifyType[] shownStack) {
        Failure f = null;
        if (maxLocals != target.maxLocals) {
            f = fail("Locals size mismatch");
        } else if (stackSize != target.stackSize) {
            f = fail("Stack size mismatch");
        } else {
            for (int i = 0; i < target.localsSize && f == null; i++) {
                if (!assignable(target.locals[i], locals[i])) {
                    f = fail("Bad type", target.locals[i], locals[i]);
                }
            }
            for (int i = 0; i < stackSize && f == null; i++) {
                if (!assignable(target.stack[i], stack[i])) {
                    f = fail("Bad type", target.stack[i], stack[i]);
                }
            }
            if (f == null && thisUninit && !target.thisUninit) {
                f = fail("Bad flags");
            }
        }
        if (f != null) {
            f.frames = "\n  while assigning frame @" + offset + " with locals " + list(shownLocals) + " and stack "
                + list(shownStack) + "\n  to frame @" + target.offset + " with locals "
                + list(target.localsPrint != null ? target.localsPrint : target.locals) + " and stack "
                + list(target.stackPrint != null ? target.stackPrint : target.stack);
            throw f;
        }
    }

    /** The locals as an error message shows them. */
    VerifyType[] shownLocals() {
        return locals.clone();
    }

    /** The stack as an error message shows it: the live entries, then bogus up to {@code length}. */
    VerifyType[] shownStack(int length) {
        VerifyType[] out = new VerifyType[length];
        for (int i = 0; i < length; i++) {
            out[i] = i < stackSize ? stack[i] : VerifyType.T_BOGUS;
        }
        return out;
    }

    private static String list(VerifyType[] types) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < types.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(types[i]);
        }
        return sb.append(']').toString();
    }
}
