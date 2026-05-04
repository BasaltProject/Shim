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
import java.lang.classfile.TypeKind;
import shim.classfile.Insns;
import shim.classfile.UnboundInsns;

public interface LoadInstruction extends Instruction {
    int slot();

    TypeKind typeKind();

    static LoadInstruction of(TypeKind kind, int slot) {
        return new UnboundInsns.UnboundLoadInstruction(Insns.loadOpcode(kind, slot), slot);
    }

    static LoadInstruction of(Opcode op, int slot) {
        Insns.checkKind(op, Opcode.Kind.LOAD);
        Insns.checkSlot(op, slot);
        return new UnboundInsns.UnboundLoadInstruction(op, slot);
    }
}
