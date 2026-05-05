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
import java.lang.classfile.attribute.LocalVariableInfo;
import java.lang.classfile.attribute.LocalVariableTypeInfo;
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.classfile.constantpool.Utf8Entry;
import java.lang.classfile.instruction.CharacterRange;
import java.lang.classfile.instruction.ExceptionCatch;
import java.lang.classfile.instruction.LineNumber;
import java.lang.classfile.instruction.LocalVariable;
import java.lang.classfile.instruction.LocalVariableType;
import java.lang.classfile.instruction.SwitchCase;
import java.lang.constant.ClassDesc;
import java.util.Objects;
import java.util.Optional;

/** Pseudo-instructions and switch cases, both parsed and unbound. */
public final class Pseudos {
    private Pseudos() { }

    private static final LineNumberImpl[] LINES = new LineNumberImpl[1000];

    public static LineNumber lineNumber(int line) {
        if (line < LINES.length) {
            LineNumberImpl l = LINES[line];
            if (l == null) {
                l = new LineNumberImpl(line);
                LINES[line] = l;
            }
            return l;
        }
        return new LineNumberImpl(line);
    }

    public static final class LineNumberImpl implements LineNumber {
        private final int line;

        LineNumberImpl(int line) { this.line = line; }

        @Override public int line() { return line; }
        @Override public String toString() { return "LineNumber[line=" + line + "]"; }
    }

    public static final class SwitchCaseImpl implements SwitchCase {
        private final int caseValue;
        private final Label target;

        public SwitchCaseImpl(int caseValue, Label target) {
            this.caseValue = caseValue;
            this.target = Objects.requireNonNull(target);
        }

        @Override public int caseValue() { return caseValue; }
        @Override public Label target() { return target; }

        @Override
        public boolean equals(Object o) {
            return o instanceof SwitchCaseImpl && ((SwitchCaseImpl) o).caseValue == caseValue
                && ((SwitchCaseImpl) o).target.equals(target);
        }

        @Override public int hashCode() { return 31 * Integer.hashCode(caseValue) + target.hashCode(); }
        @Override
        public String toString() {
            return "SwitchCaseImpl[caseValue=" + caseValue + ", target=" + target + "]";
        }
    }

    public static final class ExceptionCatchImpl implements ExceptionCatch {
        private final Label handler;
        private final Label tryStart;
        private final Label tryEnd;
        private final ClassEntry catchType;

        public ExceptionCatchImpl(Label handler, Label tryStart, Label tryEnd, ClassEntry catchType) {
            this.handler = Objects.requireNonNull(handler);
            this.tryStart = Objects.requireNonNull(tryStart);
            this.tryEnd = Objects.requireNonNull(tryEnd);
            this.catchType = catchType;
        }

        @Override public Label handler() { return handler; }
        @Override public Label tryStart() { return tryStart; }
        @Override public Label tryEnd() { return tryEnd; }
        @Override public Optional<ClassEntry> catchType() { return Optional.ofNullable(catchType); }

        @Override
        public String toString() {
            return "ExceptionCatch[catchType=" + (catchType == null ? "<any>" : catchType.asInternalName()) + "]";
        }
    }

    /** A LocalVariableTable or LocalVariableTypeTable entry at {@code pos} of a parsed code attribute. */
    abstract static class BoundLocal {
        final CodeImpl code;
        final int pos;

        BoundLocal(CodeImpl code, int pos) {
            this.code = code;
            this.pos = pos;
        }

        public int startPc() { return code.reader.readU2(pos); }
        public int length() { return code.reader.readU2(pos + 2); }
        public Utf8Entry name() { return code.reader.readEntry(pos + 4, Utf8Entry.class); }
        Utf8Entry descriptor() { return code.reader.readEntry(pos + 6, Utf8Entry.class); }
        public int slot() { return code.reader.readU2(pos + 8); }
        public Label startScope() { return code.getLabel(startPc()); }
        public Label endScope() { return code.getLabel(startPc() + length()); }
    }

    public static final class BoundLocalVariable extends BoundLocal implements LocalVariable, LocalVariableInfo {
        BoundLocalVariable(CodeImpl code, int pos) { super(code, pos); }

        @Override public Utf8Entry type() { return descriptor(); }
        @Override public ClassDesc typeSymbol() { return ClassDesc.ofDescriptor(type().stringValue()); }

        @Override
        public String toString() {
            return "LocalVariable[name=" + name().stringValue() + ", slot=" + slot() + ", type="
                + type().stringValue() + "]";
        }
    }

    public static final class BoundLocalVariableType extends BoundLocal
            implements LocalVariableType, LocalVariableTypeInfo {
        BoundLocalVariableType(CodeImpl code, int pos) { super(code, pos); }

        @Override public Utf8Entry signature() { return descriptor(); }

        @Override
        public String toString() {
            return "LocalVariableType[name=" + name().stringValue() + ", slot=" + slot() + ", signature="
                + signature().stringValue() + "]";
        }
    }

    public static final class BoundCharacterRange implements CharacterRange {
        private final CodeImpl code;
        private final int pos;

        BoundCharacterRange(CodeImpl code, int pos) {
            this.code = code;
            this.pos = pos;
        }

        @Override public Label startScope() { return code.getLabel(code.reader.readU2(pos)); }
        @Override public Label endScope() { return code.getLabel(code.reader.readU2(pos + 2) + 1); }
        @Override public int characterRangeStart() { return code.reader.readInt(pos + 4); }
        @Override public int characterRangeEnd() { return code.reader.readInt(pos + 8); }
        @Override public int flags() { return code.reader.readU2(pos + 12); }

        @Override
        public String toString() {
            return "CharacterRange[startScope=" + startScope() + ", endScope=" + endScope()
                + ", characterRangeStart=" + characterRangeStart() + ", characterRangeEnd=" + characterRangeEnd()
                + ", flags=" + flags() + "]";
        }
    }

    public static final class UnboundLocalVariable implements LocalVariable {
        private final int slot;
        private final Utf8Entry name;
        private final Utf8Entry type;
        private final Label start;
        private final Label end;

        public UnboundLocalVariable(int slot, Utf8Entry name, Utf8Entry type, Label start, Label end) {
            this.slot = Insns.checkSlot(slot);
            this.name = Objects.requireNonNull(name);
            this.type = Objects.requireNonNull(type);
            this.start = Objects.requireNonNull(start);
            this.end = Objects.requireNonNull(end);
        }

        @Override public int slot() { return slot; }
        @Override public Utf8Entry name() { return name; }
        @Override public Utf8Entry type() { return type; }
        @Override public Label startScope() { return start; }
        @Override public Label endScope() { return end; }

        @Override
        public String toString() {
            return "LocalVariable[Slot=" + slot + ", name=" + name.stringValue() + ", descriptor='"
                + type.stringValue() + "']";
        }
    }

    public static final class UnboundLocalVariableType implements LocalVariableType {
        private final int slot;
        private final Utf8Entry name;
        private final Utf8Entry signature;
        private final Label start;
        private final Label end;

        public UnboundLocalVariableType(int slot, Utf8Entry name, Utf8Entry signature, Label start, Label end) {
            this.slot = Insns.checkSlot(slot);
            this.name = Objects.requireNonNull(name);
            this.signature = Objects.requireNonNull(signature);
            this.start = Objects.requireNonNull(start);
            this.end = Objects.requireNonNull(end);
        }

        @Override public int slot() { return slot; }
        @Override public Utf8Entry name() { return name; }
        @Override public Utf8Entry signature() { return signature; }
        @Override public Label startScope() { return start; }
        @Override public Label endScope() { return end; }

        @Override
        public String toString() {
            return "LocalVariableType[Slot=" + slot + ", name=" + name.stringValue() + ", signature='"
                + signature.stringValue() + "']";
        }
    }

    public static final class UnboundCharacterRange implements CharacterRange {
        private final Label start;
        private final Label end;
        private final int rangeStart;
        private final int rangeEnd;
        private final int flags;

        public UnboundCharacterRange(Label start, Label end, int rangeStart, int rangeEnd, int flags) {
            this.start = Objects.requireNonNull(start);
            this.end = Objects.requireNonNull(end);
            this.rangeStart = rangeStart;
            this.rangeEnd = rangeEnd;
            this.flags = flags;
        }

        @Override public Label startScope() { return start; }
        @Override public Label endScope() { return end; }
        @Override public int characterRangeStart() { return rangeStart; }
        @Override public int characterRangeEnd() { return rangeEnd; }
        @Override public int flags() { return flags; }
    }
}
