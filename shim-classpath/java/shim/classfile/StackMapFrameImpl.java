/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.Label;
import java.lang.classfile.attribute.StackMapFrameInfo;
import java.util.List;
import java.util.Objects;

/** One stack map frame: its encoded frame type, target label, locals and stack. */
public final class StackMapFrameImpl implements StackMapFrameInfo {
    private final int frameType;
    private final Label target;
    private final List<VerificationTypeInfo> locals;
    private final List<VerificationTypeInfo> stack;

    public StackMapFrameImpl(int frameType, Label target, List<VerificationTypeInfo> locals,
                             List<VerificationTypeInfo> stack) {
        this.frameType = frameType;
        this.target = Objects.requireNonNull(target);
        this.locals = List.copyOf(locals);
        this.stack = List.copyOf(stack);
    }

    @Override public int frameType() { return frameType; }
    @Override public Label target() { return target; }
    @Override public List<VerificationTypeInfo> locals() { return locals; }
    @Override public List<VerificationTypeInfo> stack() { return stack; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof StackMapFrameImpl)) {
            return false;
        }
        StackMapFrameImpl f = (StackMapFrameImpl) o;
        return frameType == f.frameType && target.equals(f.target) && locals.equals(f.locals) && stack.equals(f.stack);
    }

    @Override
    public int hashCode() {
        return ((frameType * 31 + target.hashCode()) * 31 + locals.hashCode()) * 31 + stack.hashCode();
    }

    @Override
    public String toString() {
        return "StackMapFrameImpl[frameType=" + frameType + ", target=" + target + ", locals=" + locals + ", stack="
            + stack + "]";
    }
}
