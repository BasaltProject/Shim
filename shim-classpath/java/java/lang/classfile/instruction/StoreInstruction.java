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

public interface StoreInstruction extends Instruction {
    int slot();

    TypeKind typeKind();

    static StoreInstruction of(TypeKind kind, int slot) {
        return new UnboundInsns.UnboundStoreInstruction(Insns.storeOpcode(kind, slot), slot);
    }

    static StoreInstruction of(Opcode op, int slot) {
        Insns.checkKind(op, Opcode.Kind.STORE);
        Insns.checkSlot(op, slot);
        return new UnboundInsns.UnboundStoreInstruction(op, slot);
    }
}
