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

public interface ReturnInstruction extends Instruction {
    TypeKind typeKind();

    static ReturnInstruction of(TypeKind typeKind) {
        return of(Insns.returnOpcode(typeKind));
    }

    static ReturnInstruction of(Opcode op) {
        Insns.checkKind(op, Opcode.Kind.RETURN);
        return new UnboundInsns.UnboundReturnInstruction(op);
    }
}
