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
import java.util.List;
import shim.classfile.UnboundInsns;

public interface TableSwitchInstruction extends Instruction {
    int lowValue();

    int highValue();

    Label defaultTarget();

    List<SwitchCase> cases();

    static TableSwitchInstruction of(int lowValue, int highValue, Label defaultTarget, List<SwitchCase> cases) {
        return new UnboundInsns.UnboundTableSwitchInstruction(lowValue, highValue, defaultTarget, cases);
    }
}
