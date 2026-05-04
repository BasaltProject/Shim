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
import shim.classfile.UnboundInsns;

public interface NopInstruction extends Instruction {
    static NopInstruction of() {
        return new UnboundInsns.UnboundNopInstruction();
    }
}
