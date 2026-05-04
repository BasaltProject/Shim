/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.Instruction;
import java.lang.classfile.Label;
import java.lang.classfile.Opcode;
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.classfile.constantpool.FieldRefEntry;
import java.lang.classfile.constantpool.MemberRefEntry;
import java.lang.classfile.instruction.SwitchCase;
import java.util.List;
import java.util.Objects;

/** Base of all instruction implementations, with the shared {@code toString} formats. */
public abstract class AbstractInsn implements Instruction {
    final Opcode op;

    AbstractInsn(Opcode op) {
        this.op = op;
    }

    @Override public Opcode opcode() { return op; }
    @Override public int sizeInBytes() { return op.sizeIfFixed(); }
    @Override public String toString() { return getClass().getSimpleName() + "[op=" + op + "]"; }

    static String local(String kind, Opcode op, int slot) {
        return kind + "[OP=" + op + ", slot=" + slot + "]";
    }

    static String increment(Opcode op, int slot, int constant) {
        return "Increment[OP=" + op + ", slot=" + slot + ", val=" + constant + "]";
    }

    static String simple(String kind, Opcode op) {
        return kind + "[OP=" + op + "]";
    }

    static String field(Opcode op, FieldRefEntry f) {
        return "Field[OP=" + op + ", field=" + Insns.member(f) + ":" + f.type().stringValue() + "]";
    }

    static String invoke(String kind, Opcode op, MemberRefEntry m) {
        return kind + "[OP=" + op + ", m=" + Insns.member(m) + m.type().stringValue() + "]";
    }

    static String typed(String kind, Opcode op, ClassEntry c) {
        return kind + "[OP=" + op + ", type=" + c.asInternalName() + "]";
    }

    static String value(String kind, Opcode op, Object value) {
        return kind + "[OP=" + op + ", val=" + value + "]";
    }

    static Label requireLabel(Label l) {
        return Objects.requireNonNull(l);
    }

    static List<SwitchCase> copyCases(List<SwitchCase> cases) {
        for (SwitchCase c : cases) {
            Objects.requireNonNull(c);
        }
        return java.util.Collections.unmodifiableList(new java.util.ArrayList<>(cases));
    }
}
