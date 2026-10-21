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
import java.lang.classfile.AttributeMapper;
import java.lang.classfile.AttributedElement;
import java.lang.classfile.ClassFile;
import java.lang.classfile.CodeElement;
import java.lang.classfile.Instruction;
import java.lang.classfile.Label;
import java.lang.classfile.MethodModel;
import java.lang.classfile.Opcode;
import java.lang.classfile.attribute.CodeAttribute;
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.classfile.constantpool.PoolEntry;
import java.lang.classfile.constantpool.Utf8Entry;
import java.lang.classfile.instruction.ExceptionCatch;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import shim.classfile.BoundInsns.*;

/** A parsed Code attribute; instructions and pseudo-instructions are decoded from the bytes on iteration. */
public final class CodeImpl extends AttrBase<CodeAttribute> implements CodeAttribute, LabelContext {
    public static final Mapper<CodeAttribute> MAPPER = new Mapper<>("Code", AttributeMapper.AttributeStability.CP_REFS,
        CodeImpl::new, CodeImpl::writeBody);

    private static final Instruction[] SIMPLE = new Instruction[256];

    static {
        for (Opcode op : Opcode.values()) {
            if (op.isWide() || op.sizeIfFixed() != 1) {
                continue;
            }
            Instruction i;
            switch (op.kind()) {
                case LOAD: i = new UnboundInsns.UnboundLoadInstruction(op, Insns.impliedSlot(op)); break;
                case STORE: i = new UnboundInsns.UnboundStoreInstruction(op, Insns.impliedSlot(op)); break;
                case RETURN: i = new UnboundInsns.UnboundReturnInstruction(op); break;
                case THROW_EXCEPTION: i = new UnboundInsns.UnboundThrowInstruction(); break;
                case ARRAY_LOAD: i = new UnboundInsns.UnboundArrayLoadInstruction(op); break;
                case ARRAY_STORE: i = new UnboundInsns.UnboundArrayStoreInstruction(op); break;
                case STACK: i = new UnboundInsns.UnboundStackInstruction(op); break;
                case CONVERT: i = new UnboundInsns.UnboundConvertInstruction(op); break;
                case OPERATOR: i = new UnboundInsns.UnboundOperatorInstruction(op); break;
                case CONSTANT: i = new UnboundInsns.UnboundIntrinsicConstantInstruction(op); break;
                case MONITOR: i = new UnboundInsns.UnboundMonitorInstruction(op); break;
                case NOP: i = new UnboundInsns.UnboundNopInstruction(); break;
                default: i = null; break;
            }
            SIMPLE[op.bytecode()] = i;
        }
    }

    private final AttributedElement owner;
    final ClassReaderImpl reader;
    private final int pos;
    final int codeStart;
    private final int codeLength;
    private final int handlersPos;
    private final int attributesPos;
    private LabelImpl[] labels;
    private boolean inflated;
    private boolean scanTargets;
    private List<Attribute<?>> attributes;
    private List<ExceptionCatch> handlers;

    CodeImpl(AttributedElement owner, ClassReaderImpl reader, int pos) {
        super(MAPPER, reader.entryByIndex(reader.readU2(pos - 6), Utf8Entry.class));
        this.owner = owner;
        this.reader = reader;
        this.pos = pos;
        this.codeStart = pos + 8;
        this.codeLength = reader.readInt(pos + 4);
        this.handlersPos = codeStart + codeLength;
        this.attributesPos = handlersPos + 2 + 8 * reader.readU2(handlersPos);
    }

    private static void writeBody(BufWriterImpl out, CodeAttribute code) {
        if (code instanceof CodeImpl && out.canWriteDirect(((CodeImpl) code).reader)) {
            CodeImpl c = (CodeImpl) code;
            int end = c.reader.skipAttributes(c.attributesPos);
            c.reader.copyBytesTo(out, c.pos, end - c.pos);
            return;
        }
        throw new UnsupportedOperationException("Code attribute needs its method context to be written");
    }

    int u1(int bci) { return reader.readU1(codeStart + bci); }
    int s1(int bci) { return reader.readS1(codeStart + bci); }
    int u2(int bci) { return reader.readU2(codeStart + bci); }
    int s2(int bci) { return reader.readS2(codeStart + bci); }
    int s4(int bci) { return reader.readInt(codeStart + bci); }

    <T extends PoolEntry> T entry(int bci, Class<T> cls) { return reader.readEntry(codeStart + bci, cls); }

    @Override public int maxStack() { return reader.readU2(pos); }
    @Override public int maxLocals() { return reader.readU2(pos + 2); }
    @Override public int codeLength() { return codeLength; }
    @Override public byte[] codeArray() { return reader.readBytes(codeStart, codeLength); }

    @Override
    public Optional<MethodModel> parent() {
        return owner instanceof MethodModel ? Optional.of((MethodModel) owner) : Optional.empty();
    }

    @Override
    public List<Attribute<?>> attributes() {
        if (attributes == null) {
            attributes = Collections.unmodifiableList(reader.readAttributes(this, attributesPos));
        }
        return attributes;
    }

    @Override
    public List<ExceptionCatch> exceptionHandlers() {
        if (handlers == null) {
            int n = reader.readU2(handlersPos);
            List<ExceptionCatch> out = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                out.add(handler(handlersPos + 2 + 8 * i));
            }
            handlers = Collections.unmodifiableList(out);
        }
        return handlers;
    }

    private ExceptionCatch handler(int p) {
        Label start = getLabel(reader.readU2(p));
        Label end = getLabel(reader.readU2(p + 2));
        Label handler = getLabel(reader.readU2(p + 4));
        ClassEntry type = reader.readEntryOrNull(p + 6, ClassEntry.class);
        return new Pseudos.ExceptionCatchImpl(handler, start, end, type);
    }

    @Override
    public Label newLabel() {
        return new LabelImpl(this, -1);
    }

    @Override
    public Label getLabel(int bci) {
        if (bci < 0 || bci > codeLength) {
            throw new IllegalArgumentException("Bytecode offset out of range; bci=" + bci + ", codeLength="
                + codeLength);
        }
        if (labels == null) {
            labels = new LabelImpl[codeLength + 1];
        }
        LabelImpl l = labels[bci];
        if (l == null) {
            l = new LabelImpl(this, bci);
            labels[bci] = l;
        }
        return l;
    }

    @Override
    public int labelToBci(Label label) {
        LabelImpl l = (LabelImpl) label;
        if (l.context != this) {
            throw new IllegalArgumentException("Illegal label reuse; context=" + this + ", label=" + l.context);
        }
        return l.bci;
    }

    private ClassFileImpl options() {
        return reader.options;
    }

    private boolean passDebug() {
        return options().debugElements == ClassFile.DebugElementsOption.PASS_DEBUG;
    }

    private boolean passLines() {
        return options().lineNumbers == ClassFile.LineNumbersOption.PASS_LINE_NUMBERS;
    }

    private String attributeName(int p) {
        return reader.entryByIndex(reader.readU2(p), Utf8Entry.class).stringValue();
    }

    /** Decodes the labels of branch targets, handlers and debug and frame metadata, validating them. */
    void inflateLabels() {
        inflate();
    }

    private void inflate() {
        if (inflated) {
            return;
        }
        inflated = true;
        scanTargets = reader.readU2(6) < 51 && !hasStackMaps();
        for (int bci = 0; bci < codeLength; ) {
            bci += inflateTargets(bci);
        }
        int n = reader.readU2(handlersPos);
        for (int i = 0; i < n; i++) {
            int p = handlersPos + 2 + 8 * i;
            getLabel(reader.readU2(p));
            getLabel(reader.readU2(p + 2));
            getLabel(reader.readU2(p + 4));
        }
        boolean debug = passDebug();
        int count = reader.readU2(attributesPos);
        int p = attributesPos + 2;
        for (int i = 0; i < count; i++) {
            String name = attributeName(p);
            int body = p + 6;
            switch (name) {
                case "LocalVariableTable":
                case "LocalVariableTypeTable":
                    if (debug) {
                        int k = reader.readU2(body);
                        for (int j = 0; j < k; j++) {
                            int start = reader.readU2(body + 2 + 10 * j);
                            getLabel(start);
                            getLabel(start + reader.readU2(body + 4 + 10 * j));
                        }
                    }
                    break;
                case "CharacterRangeTable":
                    if (debug) {
                        int k = reader.readU2(body);
                        for (int j = 0; j < k; j++) {
                            getLabel(reader.readU2(body + 2 + 14 * j));
                            getLabel(reader.readU2(body + 4 + 14 * j) + 1);
                        }
                    }
                    break;
                case "StackMapTable":
                    CodeLabels.inflateFrames(this, body);
                    break;
                case "RuntimeVisibleTypeAnnotations":
                case "RuntimeInvisibleTypeAnnotations":
                    CodeLabels.inflateTypeAnnotations(this, body);
                    break;
                default:
                    break;
            }
            p += 6 + reader.readInt(p + 2);
        }
    }

    private int inflateTargets(int bci) {
        int b = u1(bci);
        Opcode op = b == 0xc4 ? Insns.ofWideCode(u1(bci + 1)) : Insns.ofCode(b);
        if (op == null) {
            throw badOpcode(b, bci);
        }
        switch (op.kind()) {
            case BRANCH:
            case DISCONTINUED_JSR:
                target(bci + (op == Opcode.GOTO_W || op == Opcode.JSR_W ? s4(bci + 1) : s2(bci + 1)));
                return op.sizeIfFixed();
            case TABLE_SWITCH: {
                int base = BoundInsns.switchBase(bci);
                int low = s4(base + 4);
                int high = s4(base + 8);
                if (high < low || (long) high - low >= 65536) {
                    throw new IllegalArgumentException("Invalid tableswitch values low: " + low + " high: " + high);
                }
                target(bci + s4(base));
                int entries = high - low + 1;
                for (int i = 0; i < entries; i++) {
                    target(bci + s4(base + 12 + 4 * i));
                }
                return Insns.switchSize(op, bci, entries);
            }
            case LOOKUP_SWITCH: {
                int base = BoundInsns.switchBase(bci);
                int entries = s4(base + 4);
                if (entries < 0 || entries >= 65536) {
                    throw new IllegalArgumentException("Invalid lookupswitch npairs value: " + entries);
                }
                target(bci + s4(base));
                for (int i = 0; i < entries; i++) {
                    target(bci + s4(base + 12 + 8 * i));
                }
                return Insns.switchSize(op, bci, entries);
            }
            default:
                return op.sizeIfFixed();
        }
    }

    private IllegalArgumentException badOpcode(int b, int bci) {
        return new IllegalArgumentException(b == 0xc4 ? "unknown wide instruction: " + u1(bci + 1)
            : "unknown instruction: " + b);
    }

    private boolean hasStackMaps() {
        int count = reader.readU2(attributesPos);
        int p = attributesPos + 2;
        for (int i = 0; i < count; i++) {
            if (attributeName(p).equals("StackMapTable")) {
                return true;
            }
            p += 6 + reader.readInt(p + 2);
        }
        return false;
    }

    private void target(int bci) {
        if (scanTargets && bci >= 0 && bci <= codeLength) {
            getLabel(bci);
        }
    }

    private Instruction decode(int bci) {
        int b = u1(bci);
        if (b != 0xc4) {
            Instruction simple = SIMPLE[b];
            if (simple != null) {
                return simple;
            }
        }
        Opcode op = b == 0xc4 ? Insns.ofWideCode(u1(bci + 1)) : Insns.ofCode(b);
        if (op == null) {
            throw badOpcode(b, bci);
        }
        int size = op.sizeIfFixed();
        switch (op.kind()) {
            case LOAD: return new BoundLoadInstruction(op, this, bci, size);
            case STORE: return new BoundStoreInstruction(op, this, bci, size);
            case INCREMENT: return new BoundIncrementInstruction(op, this, bci, size);
            case BRANCH: return new BoundBranchInstruction(op, this, bci, size);
            case DISCONTINUED_JSR: return new BoundJsrInstruction(op, this, bci, size);
            case DISCONTINUED_RET: return new BoundRetInstruction(op, this, bci, size);
            case TABLE_SWITCH: {
                int base = BoundInsns.switchBase(bci);
                int entries = s4(base + 8) - s4(base + 4) + 1;
                return new BoundTableSwitchInstruction(this, bci, Insns.switchSize(op, bci, entries));
            }
            case LOOKUP_SWITCH: {
                int entries = s4(BoundInsns.switchBase(bci) + 4);
                return new BoundLookupSwitchInstruction(this, bci, Insns.switchSize(op, bci, entries));
            }
            case FIELD_ACCESS: return new BoundFieldInstruction(op, this, bci);
            case INVOKE:
                return op == Opcode.INVOKEINTERFACE ? new BoundInvokeInterfaceInstruction(this, bci)
                    : new BoundInvokeInstruction(op, this, bci);
            case INVOKE_DYNAMIC: return new BoundInvokeDynamicInstruction(this, bci);
            case NEW_OBJECT: return new BoundNewObjectInstruction(this, bci);
            case NEW_PRIMITIVE_ARRAY: return new BoundNewPrimitiveArrayInstruction(this, bci);
            case NEW_REF_ARRAY: return new BoundNewReferenceArrayInstruction(this, bci);
            case NEW_MULTI_ARRAY: return new BoundNewMultidimensionalArrayInstruction(this, bci);
            case TYPE_CHECK: return new BoundTypeCheckInstruction(op, this, bci);
            case CONSTANT:
                if (op == Opcode.BIPUSH || op == Opcode.SIPUSH) {
                    return new BoundArgumentConstantInstruction(op, this, bci, size);
                }
                return new BoundLoadConstantInstruction(op, this, bci, size);
            default:
                throw badOpcode(b, bci);
        }
    }

    @Override
    public void forEach(Consumer<? super CodeElement> consumer) {
        inflate();
        int[] lineAt = passLines() ? lineNumbers() : null;
        int n = reader.readU2(handlersPos);
        for (int i = 0; i < n; i++) {
            consumer.accept(handler(handlersPos + 2 + 8 * i));
        }
        boolean debug = passDebug();
        int count = reader.readU2(attributesPos);
        int p = attributesPos + 2;
        for (int i = 0; i < count; i++) {
            String name = attributeName(p);
            int body = p + 6;
            int k = reader.readU2(body);
            switch (name) {
                case "LocalVariableTable":
                    for (int j = 0; debug && j < k; j++) {
                        consumer.accept(new Pseudos.BoundLocalVariable(this, body + 2 + 10 * j));
                    }
                    break;
                case "LocalVariableTypeTable":
                    for (int j = 0; debug && j < k; j++) {
                        consumer.accept(new Pseudos.BoundLocalVariableType(this, body + 2 + 10 * j));
                    }
                    break;
                case "CharacterRangeTable":
                    for (int j = 0; debug && j < k; j++) {
                        consumer.accept(new Pseudos.BoundCharacterRange(this, body + 2 + 14 * j));
                    }
                    break;
                default:
                    break;
            }
            p += 6 + reader.readInt(p + 2);
        }
        for (Attribute<?> a : attributes()) {
            String an = a.attributeMapper().name();
            if (debug && a instanceof CodeElement
                    && (an.equals("RuntimeVisibleTypeAnnotations") || an.equals("RuntimeInvisibleTypeAnnotations"))) {
                consumer.accept((CodeElement) a);
            }
        }
        for (Attribute<?> a : attributes()) {
            switch (a.attributeMapper().name()) {
                case "LocalVariableTable":
                case "LocalVariableTypeTable":
                case "CharacterRangeTable":
                case "LineNumberTable":
                case "StackMapTable":
                case "RuntimeVisibleTypeAnnotations":
                case "RuntimeInvisibleTypeAnnotations":
                    break;
                default:
                    if (a instanceof CodeElement) {
                        consumer.accept((CodeElement) a);
                    }
                    break;
            }
        }
        LabelImpl[] ls = labels;
        for (int bci = 0; bci < codeLength; ) {
            ls = labels;
            if (ls != null && ls[bci] != null) {
                consumer.accept(ls[bci]);
            }
            if (lineAt != null && lineAt[bci] >= 0) {
                consumer.accept(Pseudos.lineNumber(lineAt[bci]));
            }
            Instruction insn = decode(bci);
            consumer.accept(insn);
            bci += insn.sizeInBytes();
        }
        ls = labels;
        if (ls != null && ls[codeLength] != null) {
            consumer.accept(ls[codeLength]);
        }
    }

    private int[] lineNumbers() {
        int[] lineAt = null;
        int count = reader.readU2(attributesPos);
        int p = attributesPos + 2;
        for (int i = 0; i < count; i++) {
            if (attributeName(p).equals("LineNumberTable")) {
                int body = p + 6;
                int k = reader.readU2(body);
                if (lineAt == null) {
                    lineAt = new int[codeLength];
                    java.util.Arrays.fill(lineAt, -1);
                }
                for (int j = 0; j < k; j++) {
                    int pc = reader.readU2(body + 2 + 4 * j);
                    if (pc >= codeLength) {
                        throw new IllegalArgumentException("Line number start_pc out of range; start_pc=" + pc
                            + ", codeLength=" + codeLength);
                    }
                    lineAt[pc] = reader.readU2(body + 4 + 4 * j);
                }
            }
            p += 6 + reader.readInt(p + 2);
        }
        return lineAt;
    }

    @Override
    public String toString() {
        return "CodeModel[id=" + System.identityHashCode(this) + "]";
    }
}
