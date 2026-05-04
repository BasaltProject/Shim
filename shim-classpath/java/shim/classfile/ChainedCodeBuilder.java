/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.CodeBuilder;
import java.lang.classfile.CodeElement;
import java.lang.classfile.CodeTransform;
import java.lang.classfile.Label;
import java.lang.classfile.TypeKind;
import java.lang.classfile.constantpool.ConstantPoolBuilder;

/** The builder a code transform sees in a chain: elements go through the transform to the downstream builder. */
final class ChainedCodeBuilder implements CodeBuilder {
    private final CodeBuilder downstream;
    private final CodeTransform transform;

    ChainedCodeBuilder(CodeBuilder downstream, CodeTransform transform) {
        this.downstream = downstream;
        this.transform = transform;
    }

    @Override
    public CodeBuilder with(CodeElement element) {
        transform.accept(downstream, element);
        return this;
    }

    @Override public ConstantPoolBuilder constantPool() { return downstream.constantPool(); }
    @Override public Label newLabel() { return downstream.newLabel(); }
    @Override public Label startLabel() { return downstream.startLabel(); }
    @Override public Label endLabel() { return downstream.endLabel(); }
    @Override public int receiverSlot() { return downstream.receiverSlot(); }
    @Override public int parameterSlot(int paramNo) { return downstream.parameterSlot(paramNo); }
    @Override public int allocateLocal(TypeKind typeKind) { return downstream.allocateLocal(typeKind); }
}
