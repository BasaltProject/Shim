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
import java.lang.classfile.Instruction;
import java.lang.classfile.Label;
import java.lang.classfile.Opcode;
import java.lang.classfile.TypeKind;
import java.lang.classfile.constantpool.ConstantPoolBuilder;
import java.lang.classfile.instruction.LabelTarget;
import java.util.Objects;
import java.util.function.Consumer;

/** A nested block: elements go to the parent; tracks reachability and its own local slots. */
public final class BlockCodeBuilderImpl implements CodeBuilder.BlockCodeBuilder {
    private final CodeBuilder parent;
    private final Label startLabel;
    private final Label endLabel;
    private final Label breakLabel;
    private boolean reachable = true;
    private boolean hasInstructions;
    private int topLocal;

    BlockCodeBuilderImpl(CodeBuilder parent, Label breakLabel) {
        this.parent = parent;
        this.startLabel = parent.newLabel();
        this.endLabel = parent.newLabel();
        this.breakLabel = Objects.requireNonNull(breakLabel);
    }

    void start() {
        topLocal = parent.allocateLocal(TypeKind.VOID);
        parent.labelBinding(startLabel);
    }

    void end() {
        parent.labelBinding(endLabel);
    }

    boolean reachable() { return reachable; }

    boolean isEmpty() { return !hasInstructions; }

    @Override
    public CodeBuilder with(CodeElement element) {
        parent.with(element);
        if (element instanceof Instruction) {
            hasInstructions = true;
            reachable = !Insns.isUnconditional(((Instruction) element).opcode());
        } else if (element instanceof LabelTarget) {
            reachable = true;
        }
        return this;
    }

    @Override public Label breakLabel() { return breakLabel; }
    @Override public Label startLabel() { return startLabel; }
    @Override public Label endLabel() { return endLabel; }
    @Override public Label newLabel() { return parent.newLabel(); }
    @Override public ConstantPoolBuilder constantPool() { return parent.constantPool(); }
    @Override public int receiverSlot() { return parent.receiverSlot(); }
    @Override public int parameterSlot(int paramNo) { return parent.parameterSlot(paramNo); }

    @Override
    public int allocateLocal(TypeKind typeKind) {
        int slot = topLocal;
        topLocal += typeKind.slotSize();
        return slot;
    }

    private static void checkBranch(Opcode op) {
        if (op.kind() != Opcode.Kind.BRANCH || op == Opcode.GOTO || op == Opcode.GOTO_W) {
            throw new IllegalArgumentException("Illegal branch opcode: " + op);
        }
    }

    public static void block(CodeBuilder b, Consumer<BlockCodeBuilder> handler) {
        Label breakLabel = b.newLabel();
        BlockCodeBuilderImpl child = new BlockCodeBuilderImpl(b, breakLabel);
        child.start();
        handler.accept(child);
        child.end();
        b.labelBinding(breakLabel);
    }

    public static void ifThen(CodeBuilder b, Opcode op, Consumer<BlockCodeBuilder> thenHandler) {
        checkBranch(op);
        Label breakLabel = b.newLabel();
        BlockCodeBuilderImpl thenBlock = new BlockCodeBuilderImpl(b, breakLabel);
        b.branch(Insns.reverseBranch(op), thenBlock.endLabel());
        thenBlock.start();
        thenHandler.accept(thenBlock);
        thenBlock.end();
        b.labelBinding(breakLabel);
    }

    public static void ifThenElse(CodeBuilder b, Opcode op, Consumer<BlockCodeBuilder> thenHandler,
                                  Consumer<BlockCodeBuilder> elseHandler) {
        checkBranch(op);
        Label breakLabel = b.newLabel();
        BlockCodeBuilderImpl thenBlock = new BlockCodeBuilderImpl(b, breakLabel);
        BlockCodeBuilderImpl elseBlock = new BlockCodeBuilderImpl(b, breakLabel);
        b.branch(Insns.reverseBranch(op), elseBlock.startLabel());
        thenBlock.start();
        thenHandler.accept(thenBlock);
        if (thenBlock.reachable()) {
            thenBlock.branch(Opcode.GOTO, thenBlock.breakLabel());
        }
        thenBlock.end();
        elseBlock.start();
        elseHandler.accept(elseBlock);
        elseBlock.end();
        b.labelBinding(breakLabel);
    }

    public static void trying(CodeBuilder b, Consumer<BlockCodeBuilder> tryHandler,
                              Consumer<CodeBuilder.CatchBuilder> catchesHandler) {
        Objects.requireNonNull(tryHandler);
        Objects.requireNonNull(catchesHandler);
        Label tryCatchEnd = b.newLabel();
        BlockCodeBuilderImpl tryBlock = new BlockCodeBuilderImpl(b, tryCatchEnd);
        tryBlock.start();
        tryHandler.accept(tryBlock);
        tryBlock.end();
        if (tryBlock.isEmpty()) {
            throw new IllegalArgumentException("The body of the try block is empty");
        }
        CatchBuilderImpl catches = new CatchBuilderImpl(b, tryBlock, tryCatchEnd);
        catchesHandler.accept(catches);
        catches.finish();
    }
}
