/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile.instruction;

import java.lang.classfile.Instruction;
import java.lang.classfile.Opcode;
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.classfile.constantpool.FieldRefEntry;
import java.lang.classfile.constantpool.NameAndTypeEntry;
import java.lang.classfile.constantpool.Utf8Entry;
import java.lang.constant.ClassDesc;
import shim.classfile.Insns;
import shim.classfile.UnboundInsns;

public interface FieldInstruction extends Instruction {
    FieldRefEntry field();

    default ClassEntry owner() {
        return field().owner();
    }

    default Utf8Entry name() {
        return field().nameAndType().name();
    }

    default Utf8Entry type() {
        return field().nameAndType().type();
    }

    default ClassDesc typeSymbol() {
        return Insns.fieldType(type());
    }

    static FieldInstruction of(Opcode op, FieldRefEntry field) {
        Insns.checkKind(op, Opcode.Kind.FIELD_ACCESS);
        return new UnboundInsns.UnboundFieldInstruction(op, field);
    }

    static FieldInstruction of(Opcode op, ClassEntry owner, Utf8Entry name, Utf8Entry type) {
        return of(op, owner, Insns.pool(owner).nameAndTypeEntry(name, type));
    }

    static FieldInstruction of(Opcode op, ClassEntry owner, NameAndTypeEntry nameAndType) {
        return of(op, Insns.pool(owner).fieldRefEntry(owner, nameAndType));
    }
}
