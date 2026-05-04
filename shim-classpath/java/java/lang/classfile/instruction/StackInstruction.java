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
import shim.classfile.Insns;
import shim.classfile.UnboundInsns;

public interface StackInstruction extends Instruction {
    static StackInstruction of(Opcode op) {
        Insns.checkKind(op, Opcode.Kind.STACK);
        return new UnboundInsns.UnboundStackInstruction(op);
    }
}
