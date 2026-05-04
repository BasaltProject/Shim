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
import shim.classfile.Insns;
import shim.classfile.UnboundInsns;

public interface IncrementInstruction extends Instruction {
    int slot();

    int constant();

    static IncrementInstruction of(int slot, int constant) {
        Insns.checkSlot(slot);
        Insns.checkS2(constant);
        return new UnboundInsns.UnboundIncrementInstruction(slot, constant);
    }
}
