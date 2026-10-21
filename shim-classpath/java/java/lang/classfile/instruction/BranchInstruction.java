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
import java.lang.classfile.Label;
import java.lang.classfile.Opcode;
import shim.classfile.Insns;
import shim.classfile.UnboundInsns;

public interface BranchInstruction extends Instruction {
    Label target();

    static BranchInstruction of(Opcode op, Label target) {
        Insns.checkKind(op, Opcode.Kind.BRANCH);
        return new UnboundInsns.UnboundBranchInstruction(op, target);
    }
}
