/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.Attribute;
import java.lang.classfile.ClassFile;
import java.lang.classfile.CodeElement;
import java.lang.classfile.Instruction;
import java.lang.classfile.Label;
import java.lang.classfile.Opcode;
import java.lang.classfile.attribute.StackMapTableAttribute;
import java.lang.classfile.constantpool.DoubleEntry;
import java.lang.classfile.constantpool.LoadableConstantEntry;
import java.lang.classfile.constantpool.LongEntry;
import java.lang.classfile.constantpool.PoolEntry;
import java.lang.classfile.instruction.BranchInstruction;
import java.lang.classfile.instruction.CharacterRange;
import java.lang.classfile.instruction.ConstantInstruction;
import java.lang.classfile.instruction.DiscontinuedInstruction;
import java.lang.classfile.instruction.ExceptionCatch;
import java.lang.classfile.instruction.FieldInstruction;
import java.lang.classfile.instruction.IncrementInstruction;
import java.lang.classfile.instruction.InvokeDynamicInstruction;
import java.lang.classfile.instruction.InvokeInstruction;
import java.lang.classfile.instruction.LabelTarget;
import java.lang.classfile.instruction.LineNumber;
import java.lang.classfile.instruction.LoadInstruction;
import java.lang.classfile.instruction.LocalVariable;
import java.lang.classfile.instruction.LocalVariableType;
import java.lang.classfile.instruction.LookupSwitchInstruction;
import java.lang.classfile.instruction.NewMultiArrayInstruction;
import java.lang.classfile.instruction.NewObjectInstruction;
import java.lang.classfile.instruction.NewPrimitiveArrayInstruction;
import java.lang.classfile.instruction.NewReferenceArrayInstruction;
import java.lang.classfile.instruction.StoreInstruction;
import java.lang.classfile.instruction.SwitchCase;
import java.lang.classfile.instruction.TableSwitchInstruction;
import java.lang.classfile.instruction.TypeCheckInstruction;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/** Lays out and encodes the instructions of one code body; pseudo-instructions are collected for the tables. */
final class CodeAssembler {
    private static final int SHORT = 0;
    private static final int LONG = 1;

    final PoolImpl pool;
    final ClassFileImpl options;
    final List<Instruction> insns = new ArrayList<>();
    private final List<Integer> entryIndex = new ArrayList<>();
    private final List<Integer> catchIndex = new ArrayList<>();
    private final Map<CodeElement, Integer> adopted;
    private final Map<Label, Integer> labelIndex = new IdentityHashMap<>();
    final List<ExceptionCatch> catches = new ArrayList<>();
    final List<int[]> lines = new ArrayList<>();
    final List<LocalVariable> localVariables = new ArrayList<>();
    final List<LocalVariableType> localVariableTypes = new ArrayList<>();
    final List<CharacterRange> characterRanges = new ArrayList<>();
    final List<Attribute<?>> attributes = new ArrayList<>();
    int[] pcs;
    private byte[] forms;

    CodeAssembler(PoolImpl pool, ClassFileImpl options, Map<CodeElement, Integer> adopted) {
        this.pool = pool;
        this.options = options;
        this.adopted = adopted;
    }

    /** Collects the elements in order, adopting the pool entries of instructions and catch types as they come. */
    void scan(List<? extends CodeElement> elements) {
        boolean debug = options.debugElements == ClassFile.DebugElementsOption.PASS_DEBUG;
        for (CodeElement e : elements) {
            if (e instanceof Instruction) {
                Instruction insn = (Instruction) e;
                entryIndex.add(adoptCached(insn));
                insns.add(insn);
            } else if (e instanceof LabelTarget) {
                Label l = ((LabelTarget) e).label();
                if (labelIndex.containsKey(l)) {
                    throw new IllegalArgumentException("Setting label target for already-set label");
                }
                labelIndex.put(l, insns.size());
            } else if (e instanceof ExceptionCatch) {
                ExceptionCatch c = (ExceptionCatch) e;
                catchIndex.add(adoptCached(c));
                catches.add(c);
            } else if (e instanceof LineNumber) {
                lines.add(new int[] {insns.size(), ((LineNumber) e).line()});
            } else if (e instanceof LocalVariable) {
                if (debug) {
                    localVariables.add((LocalVariable) e);
                }
            } else if (e instanceof LocalVariableType) {
                if (debug) {
                    localVariableTypes.add((LocalVariableType) e);
                }
            } else if (e instanceof CharacterRange) {
                if (debug) {
                    characterRanges.add((CharacterRange) e);
                }
            } else if (e instanceof Attribute<?>) {
                addAttribute((Attribute<?>) e);
            }
        }
    }

    void addAttribute(Attribute<?> a) {
        if (!a.attributeMapper().allowMultiple()) {
            for (int i = attributes.size() - 1; i >= 0; i--) {
                if (attributes.get(i).attributeMapper() == a.attributeMapper()) {
                    attributes.remove(i);
                }
            }
        }
        attributes.add(a);
    }

    void removeStackMaps() {
        for (int i = attributes.size() - 1; i >= 0; i--) {
            if (attributes.get(i) instanceof StackMapTableAttribute) {
                attributes.remove(i);
            }
        }
    }

    /** The pool entry an instruction refers to, or null. */
    static PoolEntry entryOf(Instruction insn) {
        PoolEntry e = null;
        if (insn instanceof FieldInstruction) {
            e = ((FieldInstruction) insn).field();
        } else if (insn instanceof InvokeInstruction) {
            e = ((InvokeInstruction) insn).method();
        } else if (insn instanceof InvokeDynamicInstruction) {
            e = ((InvokeDynamicInstruction) insn).invokedynamic();
        } else if (insn instanceof NewObjectInstruction) {
            e = ((NewObjectInstruction) insn).className();
        } else if (insn instanceof NewReferenceArrayInstruction) {
            e = ((NewReferenceArrayInstruction) insn).componentType();
        } else if (insn instanceof NewMultiArrayInstruction) {
            e = ((NewMultiArrayInstruction) insn).arrayType();
        } else if (insn instanceof TypeCheckInstruction) {
            e = ((TypeCheckInstruction) insn).type();
        } else if (insn instanceof ConstantInstruction.LoadConstantInstruction) {
            e = ((ConstantInstruction.LoadConstantInstruction) insn).constantEntry();
        }
        return e;
    }

    /** Adopts the entry an element refers to; returns its index in {@code pool} or 0. */
    static int adopt(PoolImpl pool, CodeElement element) {
        PoolEntry e;
        if (element instanceof ExceptionCatch) {
            e = ((ExceptionCatch) element).catchType().orElse(null);
        } else {
            e = entryOf((Instruction) element);
        }
        return e == null ? 0 : pool.indexOf(e);
    }

    private int adoptCached(CodeElement element) {
        if (adopted != null) {
            Integer i = adopted.get(element);
            if (i != null) {
                return i;
            }
        }
        return adopt(pool, element);
    }

    /** The instruction index a label is bound before, or -1. */
    int indexOf(Label label) {
        Integer i = labelIndex.get(label);
        return i == null ? -1 : i;
    }

    int codeLength() {
        return pcs[insns.size()];
    }

    /** The offset of a label after layout, or -1 for an unbound one. */
    int bciOf(Label label) {
        Integer i = labelIndex.get(label);
        return i == null ? -1 : pcs[i];
    }

    void bindAll(CodeLabelMap labels) {
        for (Map.Entry<Label, Integer> e : labelIndex.entrySet()) {
            labels.bind(e.getKey(), pcs[e.getValue()]);
        }
    }

    /** Computes instruction offsets; returns false when a short forward jump does not reach its target. */
    boolean layout(boolean allLong) {
        int n = insns.size();
        pcs = new int[n + 1];
        forms = new byte[n];
        boolean fail = options.shortJumps == ClassFile.ShortJumpsOption.FAIL_ON_SHORT_JUMPS;
        long pc = 0;
        for (int i = 0; i < n; i++) {
            pcs[i] = (int) pc;
            Instruction insn = insns.get(i);
            Opcode op = insn.opcode();
            int size;
            switch (op.kind()) {
                case BRANCH:
                case DISCONTINUED_JSR:
                    size = jumpSize(i, op, jumpTarget(insn), allLong, fail);
                    break;
                case TABLE_SWITCH: {
                    TableSwitchInstruction t = (TableSwitchInstruction) insn;
                    long entries = (long) t.highValue() - t.lowValue() + 1;
                    size = (int) Math.min(Integer.MAX_VALUE / 2, 1 + pad((int) pc) + 12 + 4 * Math.max(entries, 0));
                    break;
                }
                case LOOKUP_SWITCH:
                    size = 1 + pad((int) pc) + 8 + 8 * ((LookupSwitchInstruction) insn).cases().size();
                    break;
                case CONSTANT:
                    size = insn instanceof ConstantInstruction.LoadConstantInstruction
                        ? loadConstantOpcode((ConstantInstruction.LoadConstantInstruction) insn, i).sizeIfFixed()
                        : op.sizeIfFixed();
                    break;
                default:
                    size = op.sizeIfFixed();
                    break;
            }
            pc += size;
            if (pc > Integer.MAX_VALUE / 2) {
                pc = Integer.MAX_VALUE / 2;
            }
        }
        pcs[n] = (int) pc;
        if (allLong) {
            return true;
        }
        for (int i = 0; i < n; i++) {
            Instruction insn = insns.get(i);
            Opcode op = insn.opcode();
            if ((op.kind() == Opcode.Kind.BRANCH || op.kind() == Opcode.Kind.DISCONTINUED_JSR)
                    && op != Opcode.GOTO_W && op != Opcode.JSR_W) {
                int t = indexOf(jumpTarget(insn));
                if (t > i) {
                    int offset = pcs[t] - pcs[i];
                    if (offset > Short.MAX_VALUE) {
                        if (fail) {
                            throw overflow();
                        }
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private int jumpSize(int i, Opcode op, Label target, boolean allLong, boolean fail) {
        if (op == Opcode.GOTO_W || op == Opcode.JSR_W) {
            return 5;
        }
        int t = indexOf(target);
        if (!allLong) {
            if (t < 0 || t > i || pcs[t] - pcs[i] >= Short.MIN_VALUE) {
                return 3;
            }
            if (fail) {
                throw overflow();
            }
        }
        forms[i] = LONG;
        return op == Opcode.GOTO || op == Opcode.JSR ? 5 : 8;
    }

    private static IllegalArgumentException overflow() {
        return new IllegalArgumentException("Label target offset overflow");
    }

    static Label jumpTarget(Instruction insn) {
        return insn instanceof BranchInstruction ? ((BranchInstruction) insn).target()
            : ((DiscontinuedInstruction.JsrInstruction) insn).target();
    }

    private static int pad(int pc) {
        return (4 - ((pc + 1) & 3)) & 3;
    }

    /** The opcode a load constant instruction is written with: entries of other pools are re-encoded by index. */
    Opcode loadConstantOpcode(ConstantInstruction.LoadConstantInstruction insn, int i) {
        Opcode op = insn.opcode();
        if (insn instanceof BoundInsns.Bound && !pool.canWriteDirect(((BoundInsns.Bound) insn).code.reader)) {
            LoadableConstantEntry e = insn.constantEntry();
            if (e instanceof LongEntry || e instanceof DoubleEntry) {
                return Opcode.LDC2_W;
            }
            return entryIndex.get(i) < 256 ? Opcode.LDC : Opcode.LDC_W;
        }
        return op;
    }

    /** Encodes all instructions after {@link #layout}. */
    byte[] encode() {
        BufWriterImpl b = new BufWriterImpl(pool, Math.max(16, codeLength()));
        int n = insns.size();
        for (int i = 0; i < n; i++) {
            encode(b, i, insns.get(i));
        }
        return b.toByteArray();
    }

    private void writeOp(BufWriterImpl b, Opcode op) {
        if (op.isWide()) {
            b.writeU2(op.bytecode());
        } else {
            b.writeU1(op.bytecode());
        }
    }

    private int offsetTo(Label target, int pc) {
        int t = indexOf(target);
        return t < 0 ? -1 - pc : pcs[t] - pc;
    }

    private void encode(BufWriterImpl b, int i, Instruction insn) {
        Opcode op = insn.opcode();
        int pc = pcs[i];
        switch (op.kind()) {
            case LOAD:
                writeLocal(b, op, ((LoadInstruction) insn).slot());
                break;
            case STORE:
                writeLocal(b, op, ((StoreInstruction) insn).slot());
                break;
            case DISCONTINUED_RET:
                writeLocal(b, op, ((DiscontinuedInstruction.RetInstruction) insn).slot());
                break;
            case INCREMENT: {
                IncrementInstruction inc = (IncrementInstruction) insn;
                writeOp(b, op);
                if (op.isWide()) {
                    b.writeU2(inc.slot());
                    b.writeU2(inc.constant());
                } else {
                    b.writeU1(inc.slot());
                    b.writeU1(inc.constant());
                }
                break;
            }
            case BRANCH:
            case DISCONTINUED_JSR:
                writeJump(b, i, op, jumpTarget(insn), pc);
                break;
            case TABLE_SWITCH:
                writeTableSwitch(b, (TableSwitchInstruction) insn, pc);
                break;
            case LOOKUP_SWITCH:
                writeLookupSwitch(b, (LookupSwitchInstruction) insn, pc);
                break;
            case FIELD_ACCESS:
                writeOp(b, op);
                b.writeU2(entryIndex.get(i));
                break;
            case INVOKE: {
                InvokeInstruction inv = (InvokeInstruction) insn;
                writeOp(b, op);
                b.writeU2(entryIndex.get(i));
                if (op == Opcode.INVOKEINTERFACE) {
                    b.writeU1(inv.count());
                    b.writeU1(0);
                }
                break;
            }
            case INVOKE_DYNAMIC:
                writeOp(b, op);
                b.writeU2(entryIndex.get(i));
                b.writeU2(0);
                break;
            case NEW_OBJECT:
                writeOp(b, op);
                b.writeU2(entryIndex.get(i));
                break;
            case NEW_PRIMITIVE_ARRAY:
                writeOp(b, op);
                b.writeU1(((NewPrimitiveArrayInstruction) insn).typeKind().newarrayCode());
                break;
            case NEW_REF_ARRAY:
                writeOp(b, op);
                b.writeU2(entryIndex.get(i));
                break;
            case NEW_MULTI_ARRAY: {
                NewMultiArrayInstruction m = (NewMultiArrayInstruction) insn;
                writeOp(b, op);
                b.writeU2(entryIndex.get(i));
                b.writeU1(m.dimensions());
                break;
            }
            case TYPE_CHECK:
                writeOp(b, op);
                b.writeU2(entryIndex.get(i));
                break;
            case CONSTANT:
                writeConstant(b, insn, op, i);
                break;
            default:
                writeOp(b, op);
                break;
        }
    }

    private void writeLocal(BufWriterImpl b, Opcode op, int slot) {
        writeOp(b, op);
        if (op.isWide()) {
            b.writeU2(slot);
        } else if (op.sizeIfFixed() > 1) {
            b.writeU1(slot);
        }
    }

    private void writeConstant(BufWriterImpl b, Instruction insn, Opcode op, int i) {
        if (insn instanceof ConstantInstruction.LoadConstantInstruction) {
            ConstantInstruction.LoadConstantInstruction ldc = (ConstantInstruction.LoadConstantInstruction) insn;
            Opcode real = loadConstantOpcode(ldc, i);
            writeOp(b, real);
            int index = entryIndex.get(i);
            if (real == Opcode.LDC) {
                b.writeU1(index);
            } else {
                b.writeU2(index);
            }
        } else if (insn instanceof ConstantInstruction.ArgumentConstantInstruction) {
            int v = ((ConstantInstruction.ArgumentConstantInstruction) insn).constantValue();
            writeOp(b, op);
            if (op == Opcode.SIPUSH) {
                b.writeU2(v);
            } else {
                b.writeU1(v);
            }
        } else {
            writeOp(b, op);
        }
    }

    private void writeJump(BufWriterImpl b, int i, Opcode op, Label target, int pc) {
        if (op == Opcode.GOTO_W || op == Opcode.JSR_W) {
            writeOp(b, op);
            b.writeInt(offsetTo(target, pc));
        } else if (forms[i] == SHORT) {
            writeOp(b, op);
            b.writeU2(offsetTo(target, pc));
        } else if (op == Opcode.GOTO || op == Opcode.JSR) {
            writeOp(b, op == Opcode.GOTO ? Opcode.GOTO_W : Opcode.JSR_W);
            b.writeInt(offsetTo(target, pc));
        } else {
            writeOp(b, Insns.reverseBranch(op));
            b.writeU2(8);
            writeOp(b, Opcode.GOTO_W);
            b.writeInt(offsetTo(target, pc + 3));
        }
    }

    private void writeSwitchStart(BufWriterImpl b, int pc, Label defaultTarget) {
        writeOp(b, Opcode.LOOKUPSWITCH);
        for (int k = pad(pc); k > 0; k--) {
            b.writeU1(0);
        }
        b.writeInt(offsetTo(defaultTarget, pc));
    }

    private void writeTableSwitch(BufWriterImpl b, TableSwitchInstruction t, int pc) {
        b.writeU1(Opcode.TABLESWITCH.bytecode());
        for (int k = pad(pc); k > 0; k--) {
            b.writeU1(0);
        }
        int low = t.lowValue();
        int high = t.highValue();
        int dflt = offsetTo(t.defaultTarget(), pc);
        b.writeInt(dflt);
        b.writeInt(low);
        b.writeInt(high);
        int entries = (int) Math.max(0L, (long) high - low + 1);
        int[] offsets = new int[entries];
        java.util.Arrays.fill(offsets, dflt);
        for (SwitchCase c : t.cases()) {
            long k = (long) c.caseValue() - low;
            if (k >= 0 && k < entries) {
                offsets[(int) k] = offsetTo(c.target(), pc);
            }
        }
        for (int off : offsets) {
            b.writeInt(off);
        }
    }

    private void writeLookupSwitch(BufWriterImpl b, LookupSwitchInstruction l, int pc) {
        writeSwitchStart(b, pc, l.defaultTarget());
        List<SwitchCase> cases = new ArrayList<>(l.cases());
        cases.sort((x, y) -> Integer.compare(x.caseValue(), y.caseValue()));
        b.writeInt(cases.size());
        for (SwitchCase c : cases) {
            b.writeInt(c.caseValue());
            b.writeInt(offsetTo(c.target(), pc));
        }
    }

    /** Exception table rows {start, end, handler, catchTypeIndex}; unbound labels fail or drop the row. */
    List<int[]> handlerRows(boolean failOnUnbound) {
        boolean drop = !failOnUnbound || options.deadLabels == ClassFile.DeadLabelsOption.DROP_DEAD_LABELS;
        List<int[]> rows = new ArrayList<>(catches.size());
        for (int i = 0; i < catches.size(); i++) {
            ExceptionCatch c = catches.get(i);
            int start = bciOf(c.tryStart());
            int end = bciOf(c.tryEnd());
            int handler = bciOf(c.handler());
            if (start < 0 || end < 0 || handler < 0) {
                if (drop) {
                    continue;
                }
                throw new IllegalArgumentException("Unbound label in exception handler");
            }
            rows.add(new int[] {start, end, handler, catchIndex.get(i)});
        }
        return rows;
    }
}
