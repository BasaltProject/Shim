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
import java.lang.classfile.TypeKind;
import shim.classfile.UnboundInsns;

public interface NewPrimitiveArrayInstruction extends Instruction {
    TypeKind typeKind();

    static NewPrimitiveArrayInstruction of(TypeKind typeKind) {
        if (typeKind == TypeKind.REFERENCE || typeKind == TypeKind.VOID) {
            throw new IllegalArgumentException("Illegal component type for primitive array: " + typeKind);
        }
        return new UnboundInsns.UnboundNewPrimitiveArrayInstruction(typeKind);
    }
}
