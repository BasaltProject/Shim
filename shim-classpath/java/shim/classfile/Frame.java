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

/** Locals and operand stack at one point of a method, in slots (long and double take two). */
final class Frame {
    final int bci;
    VType[] locals;
    int localsSize;
    VType[] stack;
    int stackSize;
    boolean set;
    boolean dirty;
    boolean dead;

    Frame(int bci, int maxLocals, int maxStack) {
        this.bci = bci;
        this.locals = new VType[Math.max(maxLocals, 4)];
        this.stack = new VType[Math.max(maxStack, 4)];
    }

    void copyFrom(Frame f) {
        if (locals.length < f.localsSize) {
            locals = new VType[f.locals.length];
        }
        System.arraycopy(f.locals, 0, locals, 0, f.localsSize);
        if (localsSize > f.localsSize) {
            Arrays.fill(locals, f.localsSize, localsSize, null);
        }
        localsSize = f.localsSize;
        if (stack.length < f.stackSize) {
            stack = new VType[f.stack.length];
        }
        System.arraycopy(f.stack, 0, stack, 0, f.stackSize);
        stackSize = f.stackSize;
    }

    void push(VType t) {
        if (stackSize == stack.length) {
            stack = Arrays.copyOf(stack, stack.length * 2);
        }
        stack[stackSize++] = t;
    }

    void push2(VType t) {
        push(t);
        push(t == VType.T_LONG ? VType.T_LONG2 : VType.T_DOUBLE2);
    }

    VType local(int slot) {
        return slot < localsSize ? locals[slot] : VType.T_TOP;
    }

    void ensureLocals(int size) {
        if (size > locals.length) {
            locals = Arrays.copyOf(locals, Math.max(size, locals.length * 2));
        }
        while (localsSize < size) {
            locals[localsSize++] = VType.T_TOP;
        }
    }

    void setLocal(int slot, VType t) {
        ensureLocals(slot + 1);
        VType old = locals[slot];
        if (old.tag == VType.LONG || old.tag == VType.DOUBLE) {
            if (slot + 1 < localsSize) {
                locals[slot + 1] = VType.T_TOP;
            }
        }
        if (slot > 0 && (locals[slot - 1].tag == VType.LONG || locals[slot - 1].tag == VType.DOUBLE)) {
            locals[slot - 1] = VType.T_TOP;
        }
        locals[slot] = t;
    }

    void setLocal2(int slot, VType t) {
        ensureLocals(slot + 2);
        VType next = locals[slot + 1];
        if ((next.tag == VType.LONG || next.tag == VType.DOUBLE) && slot + 2 < localsSize) {
            locals[slot + 2] = VType.T_TOP;
        }
        if (slot > 0 && (locals[slot - 1].tag == VType.LONG || locals[slot - 1].tag == VType.DOUBLE)) {
            locals[slot - 1] = VType.T_TOP;
        }
        locals[slot] = t;
        locals[slot + 1] = t == VType.T_LONG ? VType.T_LONG2 : VType.T_DOUBLE2;
    }

    void replace(VType from, VType to) {
        for (int i = 0; i < localsSize; i++) {
            if (locals[i].equals(from)) {
                locals[i] = to;
            }
        }
        for (int i = 0; i < stackSize; i++) {
            if (stack[i].equals(from)) {
                stack[i] = to;
            }
        }
    }
}
