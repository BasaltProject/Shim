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

public interface OperatorInstruction extends Instruction {
    TypeKind typeKind();

    static OperatorInstruction of(Opcode op) {
        Insns.checkKind(op, Opcode.Kind.OPERATOR);
        return new UnboundInsns.UnboundOperatorInstruction(op);
    }
}
