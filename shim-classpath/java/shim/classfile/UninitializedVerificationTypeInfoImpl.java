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
import java.util.Objects;

/** The verification type of an object created by the {@code new} instruction at a label, not yet initialized. */
public final class UninitializedVerificationTypeInfoImpl
    implements StackMapFrameInfo.UninitializedVerificationTypeInfo {
    private final Label newTarget;

    public UninitializedVerificationTypeInfoImpl(Label newTarget) {
        this.newTarget = Objects.requireNonNull(newTarget);
    }

    @Override public int tag() { return ITEM_UNINITIALIZED; }
    @Override public Label newTarget() { return newTarget; }

    @Override
    public boolean equals(Object o) {
        return o instanceof UninitializedVerificationTypeInfoImpl
            && newTarget.equals(((UninitializedVerificationTypeInfoImpl) o).newTarget);
    }

    @Override public int hashCode() { return newTarget.hashCode(); }
    @Override public String toString() { return "UNINIT(" + newTarget + ")"; }
}
