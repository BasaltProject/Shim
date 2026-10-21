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
import java.lang.classfile.attribute.LocalVariableTypeInfo;
import java.lang.classfile.instruction.LocalVariableType;
import java.lang.classfile.constantpool.Utf8Entry;

/** One entry of a LocalVariableTypeTable read from a class file. */
public final class LocalVariableTypeInfoImpl implements LocalVariableTypeInfo, LocalVariableType {
    private final int startPc;
    private final int length;
    private final Utf8Entry name;
    private final Utf8Entry signature;
    private final int slot;
    private final LabelContext labels;

    LocalVariableTypeInfoImpl(int startPc, int length, Utf8Entry name, Utf8Entry signature, int slot,
                              LabelContext labels) {
        this.startPc = startPc;
        this.length = length;
        this.name = name;
        this.signature = signature;
        this.slot = slot;
        this.labels = labels;
    }

    static LocalVariableTypeInfoImpl read(ClassReaderImpl r, int p, LabelContext labels) {
        return new LocalVariableTypeInfoImpl(r.readU2(p), r.readU2(p + 2), r.readEntry(p + 4, Utf8Entry.class),
            r.readEntry(p + 6, Utf8Entry.class), r.readU2(p + 8), labels);
    }

    @Override public int startPc() { return startPc; }
    @Override public int length() { return length; }
    @Override public Utf8Entry name() { return name; }
    @Override public Utf8Entry signature() { return signature; }
    @Override public int slot() { return slot; }
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
        return "LocalVariableType[name=" + name.stringValue() + ", slot=" + slot + ", signature="
            + signature.stringValue() + "]";
    }
}
