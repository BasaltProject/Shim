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

public interface LookupSwitchInstruction extends Instruction {
    Label defaultTarget();

    List<SwitchCase> cases();

    static LookupSwitchInstruction of(Label defaultTarget, List<SwitchCase> cases) {
        return new UnboundInsns.UnboundLookupSwitchInstruction(defaultTarget, cases);
    }
}
