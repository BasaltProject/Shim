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
import java.lang.classfile.instruction.LocalVariable;
import java.lang.classfile.constantpool.Utf8Entry;
import java.lang.constant.ClassDesc;

/** One entry of a LocalVariableTable read from a class file. */
public final class LocalVariableInfoImpl implements LocalVariableInfo, LocalVariable {
    private final int startPc;
    private final int length;
    private final Utf8Entry name;
    private final Utf8Entry type;
    private final int slot;
    private final LabelContext labels;

    LocalVariableInfoImpl(int startPc, int length, Utf8Entry name, Utf8Entry type, int slot,
                          LabelContext labels) {
        this.startPc = startPc;
        this.length = length;
        this.name = name;
        this.type = type;
        this.slot = slot;
        this.labels = labels;
    }

    static LocalVariableInfoImpl read(ClassReaderImpl r, int p, LabelContext labels) {
        return new LocalVariableInfoImpl(r.readU2(p), r.readU2(p + 2), r.readEntry(p + 4, Utf8Entry.class),
            r.readEntry(p + 6, Utf8Entry.class), r.readU2(p + 8), labels);
    }

    @Override public int startPc() { return startPc; }
    @Override public int length() { return length; }
    @Override public Utf8Entry name() { return name; }
    @Override public Utf8Entry type() { return type; }
    @Override public int slot() { return slot; }
    @Override public ClassDesc typeSymbol() { return ClassDesc.ofDescriptor(type.stringValue()); }
    @Override public Label startScope() { return context().getLabel(startPc); }
    @Override public Label endScope() { return context().getLabel(startPc + length); }

    private LabelContext context() {
        if (labels == null) {
            throw new IllegalStateException("No code context for labels");
        }
        return labels;
    }

    @Override
    public String toString() {
        return "LocalVariable[name=" + name.stringValue() + ", slot=" + slot + ", type=" + type.stringValue() + "]";
    }
}
