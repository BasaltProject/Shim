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
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/** The offsets of the labels of a body being assembled; unbound labels map to -1. */
final class CodeLabelMap implements LabelContext, StackMapTableAttr.MethodAware {
    private final MethodContext method;
    private final Map<Label, Integer> bound = new IdentityHashMap<>();
    private final Map<Integer, Label> byBci = new HashMap<>();

    CodeLabelMap(MethodContext method) {
        this.method = method;
    }

    boolean isBound(Label label) {
        return bound.containsKey(label);
    }

    void bind(Label label, int bci) {
        bound.put(label, bci);
    }

    @Override
    public MethodContext methodContext() {
        return method;
    }

    @Override
    public Label newLabel() {
        return new LabelImpl(this, -1);
    }

    @Override
    public Label getLabel(int bci) {
        Label l = byBci.get(bci);
        if (l == null) {
            l = new LabelImpl(this, bci);
            byBci.put(bci, l);
            bound.put(l, bci);
        }
        return l;
    }

    @Override
    public int labelToBci(Label label) {
        Integer bci = bound.get(label);
        return bci == null ? -1 : bci;
    }
}
