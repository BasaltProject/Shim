/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile.instruction;

import java.lang.classfile.Label;
import shim.classfile.Pseudos;

public interface SwitchCase {
    int caseValue();

    Label target();

    static SwitchCase of(int caseValue, Label target) {
        return new Pseudos.SwitchCaseImpl(caseValue, target);
    }
}
