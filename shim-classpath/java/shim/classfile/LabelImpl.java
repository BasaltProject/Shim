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
import java.lang.classfile.instruction.LabelTarget;

/** A label owned by one code body; {@code bci} is -1 until it is bound to an offset. */
public final class LabelImpl implements Label, LabelTarget {
    public final LabelContext context;
    public int bci;

    public LabelImpl(LabelContext context, int bci) {
        this.context = context;
        this.bci = bci;
    }

    @Override
    public Label label() {
        return this;
    }

    @Override
    public String toString() {
        return "Label[context=" + context + ", bci=" + bci + "]";
    }
}
