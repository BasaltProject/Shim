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
import java.lang.classfile.CodeModel;
import java.lang.classfile.CodeTransform;
import java.lang.classfile.Instruction;
import java.lang.classfile.Label;
import java.lang.classfile.Opcode;
import java.lang.classfile.TypeKind;
import java.lang.classfile.constantpool.ConstantPoolBuilder;
import java.lang.classfile.instruction.BranchInstruction;
import java.lang.classfile.instruction.CharacterRange;
import java.lang.classfile.instruction.DiscontinuedInstruction;
import java.lang.classfile.instruction.ExceptionCatch;
import java.lang.classfile.instruction.LabelTarget;
import java.lang.classfile.instruction.LocalVariable;
import java.lang.classfile.instruction.LocalVariableType;
import java.lang.classfile.instruction.LookupSwitchInstruction;
import java.lang.classfile.instruction.SwitchCase;
import java.lang.classfile.instruction.TableSwitchInstruction;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** The terminal code builder of a method body; collects elements into a {@link BufferedCode}. */
public class CodeBuilderImpl implements CodeBuilder, LabelContext {
    final MethodContext context;
    private final List<CodeElement> elements = new ArrayList<>();
    private final LabelImpl startLabel;
    private final LabelImpl endLabel;
    private final int[] parameterSlots;
    private int topLocal;
    private int maxLocal;
    private int bci;
    private boolean finished;
    private boolean exported;
    private CodeModel original;
    private final Map<CodeElement, Integer> adopted = new IdentityHashMap<>();

    public CodeBuilderImpl(MethodContext context) {
        this.context = context;
        this.startLabel = new LabelImpl(this, 0);
        this.endLabel = new LabelImpl(this, -1);
        String descriptor = context.type.stringValue();
        java.lang.constant.MethodTypeDesc.ofDescriptor(descriptor);
        int count = 0;
        for (int i = 1; descriptor.charAt(i) != ')'; i = nextParameter(descriptor, i)) {
            count++;
        }
        parameterSlots = new int[count];
        int slot = context.isStatic() ? 0 : 1;
        int k = 0;
        for (int i = 1; descriptor.charAt(i) != ')'; i = nextParameter(descriptor, i)) {
            parameterSlots[k++] = slot;
            char c = descriptor.charAt(i);
            slot += c == 'J' || c == 'D' ? 2 : 1;
        }
        topLocal = slot;
        maxLocal = slot;
    }

    private static int nextParameter(String descriptor, int i) {
        while (descriptor.charAt(i) == '[') {
            i++;
        }
        return descriptor.charAt(i) == 'L' ? descriptor.indexOf(';', i) + 1 : i + 1;
    }

    /** A builder that hands every element to {@code next}, which writes to {@code downstream}. */
    public static CodeBuilder forwarding(CodeBuilder downstream, CodeTransform next) {
        return new ChainedCodeBuilder(downstream, next);
    }

    @Override public ConstantPoolBuilder constantPool() { return context.pool; }
    @Override public Label newLabel() { return new LabelImpl(this, -1); }
    @Override public Label startLabel() { return startLabel; }
    @Override public Label endLabel() { return endLabel; }

    @Override
    public int receiverSlot() {
        if (context.isStatic()) {
            throw new IllegalStateException("not an instance method");
        }
        return 0;
    }

    @Override public int parameterSlot(int paramNo) { return parameterSlots[paramNo]; }

    @Override
    public int allocateLocal(TypeKind typeKind) {
        int slot = topLocal;
        topLocal += typeKind.slotSize();
        maxLocal = Math.max(maxLocal, topLocal);
        return slot;
    }

    /** The pool indexes the entries of instructions and catch types were adopted with when they were added. */
    Map<CodeElement, Integer> adoptedEntries() { return adopted; }

    /** The parsed body this one is transformed from, or null. */
    public CodeModel original() { return original; }

    void setOriginal(CodeModel original) { this.original = original; }

    /** The highest local slot count reached by the parameters and {@link #allocateLocal}. */
    public int allocatedLocals() { return maxLocal; }

    /** The offset the body has reached, estimated from the sizes of the instructions added so far. */
    public int currentBci() { return bci; }

    @Override
    public CodeBuilder with(CodeElement element) {
        Objects.requireNonNull(element);
        if (element instanceof LabelTarget) {
            bind(((LabelTarget) element).label());
        } else if (element instanceof Instruction) {
            Instruction insn = (Instruction) element;
            checkTargets(insn);
            adopted.put(insn, CodeAssembler.adopt(context.pool, insn));
            bci += size(insn, bci);
        } else if (element instanceof ExceptionCatch) {
            adopted.put(element, CodeAssembler.adopt(context.pool, element));
        }
        elements.add(element);
        return this;
    }

    private void bind(Label label) {
        LabelImpl l = (LabelImpl) label;
        if (l.context == this) {
            if (l.bci != -1) {
                throw new IllegalArgumentException("Setting label target for already-set label");
            }
            l.bci = bci;
        } else {
            checkContext(l);
        }
    }

    private void checkContext(Label label) {
        if (label instanceof LabelImpl) {
            LabelContext c = ((LabelImpl) label).context;
            if (c != this && c instanceof CodeBuilderImpl && !((CodeBuilderImpl) c).exported) {
                throw new IllegalStateException("Unexpected label context " + c + " in =" + this);
            }
        }
    }

    private void checkTargets(Instruction insn) {
        if (insn instanceof BranchInstruction) {
            checkContext(((BranchInstruction) insn).target());
        } else if (insn instanceof DiscontinuedInstruction.JsrInstruction) {
            checkContext(((DiscontinuedInstruction.JsrInstruction) insn).target());
        } else if (insn instanceof TableSwitchInstruction) {
            checkContext(((TableSwitchInstruction) insn).defaultTarget());
            for (SwitchCase c : ((TableSwitchInstruction) insn).cases()) {
                checkContext(c.target());
            }
        } else if (insn instanceof LookupSwitchInstruction) {
            checkContext(((LookupSwitchInstruction) insn).defaultTarget());
            for (SwitchCase c : ((LookupSwitchInstruction) insn).cases()) {
                checkContext(c.target());
            }
        }
    }

    static int size(Instruction insn, int bci) {
        Opcode op = insn.opcode();
        if (insn instanceof TableSwitchInstruction) {
            TableSwitchInstruction t = (TableSwitchInstruction) insn;
            return Insns.switchSize(op, bci, (int) Math.max(0L, (long) t.highValue() - t.lowValue() + 1));
        }
        if (insn instanceof LookupSwitchInstruction) {
            return Insns.switchSize(op, bci, ((LookupSwitchInstruction) insn).cases().size());
        }
        return op.sizeIfFixed();
    }

    /** Ends the body: binds the end label and returns the collected elements as a model. */
    public CodeModel toModel() {
        if (!finished) {
            finished = true;
            if (endLabel.bci == -1) {
                endLabel.bci = bci;
            }
            for (CodeElement e : elements) {
                if (e instanceof ExceptionCatch) {
                    ExceptionCatch c = (ExceptionCatch) e;
                    checkContext(c.tryStart());
                    checkContext(c.tryEnd());
                    checkContext(c.handler());
                } else if (e instanceof LocalVariable) {
                    checkContext(((LocalVariable) e).startScope());
                    checkContext(((LocalVariable) e).endScope());
                } else if (e instanceof LocalVariableType) {
                    checkContext(((LocalVariableType) e).startScope());
                    checkContext(((LocalVariableType) e).endScope());
                } else if (e instanceof CharacterRange) {
                    checkContext(((CharacterRange) e).startScope());
                    checkContext(((CharacterRange) e).endScope());
                }
            }
        }
        List<CodeElement> all = new ArrayList<>(elements.size() + 2);
        all.add(startLabel);
        all.addAll(elements);
        all.add(endLabel);
        BufferedCode model = new BufferedCode(this, all);
        if (context.classFile != null
                && context.classFile.shortJumps == java.lang.classfile.ClassFile.ShortJumpsOption.FAIL_ON_SHORT_JUMPS) {
            CodeWriter.checkShortJumps(model, context.pool);
        }
        return model;
    }

    /** Marks the body as handed to other builders: its labels lose their estimated offsets, as buffered ones. */
    void export(List<CodeElement> all) {
        if (!exported) {
            exported = true;
            for (CodeElement e : all) {
                if (e instanceof LabelImpl && ((LabelImpl) e).context == this) {
                    ((LabelImpl) e).bci = -1;
                }
            }
        }
    }

    @Override
    public Label getLabel(int offset) {
        throw new UnsupportedOperationException("labels of a code builder have no fixed offsets");
    }

    @Override
    public int labelToBci(Label label) {
        LabelImpl l = (LabelImpl) label;
        if (l.context != this) {
            throw new IllegalArgumentException("Illegal label reuse; context=" + this + ", label=" + l.context);
        }
        return l.bci;
    }

    @Override
    public String toString() {
        return (exported ? "CodeModel[id=" : "CodeBuilder[id=") + System.identityHashCode(this) + "]";
    }
}
