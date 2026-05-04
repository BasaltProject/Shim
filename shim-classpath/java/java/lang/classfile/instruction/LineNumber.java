/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile.instruction;

import java.lang.classfile.PseudoInstruction;
import shim.classfile.Pseudos;

public interface LineNumber extends PseudoInstruction {
    int line();

    static LineNumber of(int line) {
        return Pseudos.lineNumber(line);
    }
}
