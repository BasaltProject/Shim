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

public interface NewReferenceArrayInstruction extends Instruction {
    ClassEntry componentType();

    static NewReferenceArrayInstruction of(ClassEntry componentType) {
        return new UnboundInsns.UnboundNewReferenceArrayInstruction(componentType);
    }
}
