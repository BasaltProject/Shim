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
import java.lang.classfile.constantpool.ClassEntry;
import shim.classfile.UnboundInsns;

public interface NewMultiArrayInstruction extends Instruction {
    ClassEntry arrayType();

    int dimensions();

    static NewMultiArrayInstruction of(ClassEntry arrayTypeEntry, int dimensions) {
        if (dimensions < 1 || dimensions > 255) {
            throw new IllegalArgumentException("Not a valid array dimension: " + dimensions);
        }
        return new UnboundInsns.UnboundNewMultidimensionalArrayInstruction(arrayTypeEntry, dimensions);
    }
}
