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
import java.lang.classfile.PseudoInstruction;
import shim.classfile.Pseudos;

public interface CharacterRange extends PseudoInstruction {
    int FLAG_STATEMENT = 1;
    int FLAG_BLOCK = 2;
    int FLAG_ASSIGNMENT = 4;
    int FLAG_FLOW_CONTROLLER = 8;
    int FLAG_FLOW_TARGET = 16;
    int FLAG_INVOKE = 32;
    int FLAG_CREATE = 64;
    int FLAG_BRANCH_TRUE = 128;
    int FLAG_BRANCH_FALSE = 256;

    Label startScope();

    Label endScope();

    int characterRangeStart();

    int characterRangeEnd();

    int flags();

    static CharacterRange of(Label startScope, Label endScope, int characterRangeStart, int characterRangeEnd,
                             int flags) {
        return new Pseudos.UnboundCharacterRange(startScope, endScope, characterRangeStart, characterRangeEnd, flags);
    }
}
