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
import java.lang.classfile.Opcode;
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.constant.ClassDesc;
import shim.classfile.Insns;
import shim.classfile.UnboundInsns;

public interface TypeCheckInstruction extends Instruction {
    ClassEntry type();

    static TypeCheckInstruction of(Opcode op, ClassEntry type) {
        Insns.checkKind(op, Opcode.Kind.TYPE_CHECK);
        return new UnboundInsns.UnboundTypeCheckInstruction(op, type);
    }

    static TypeCheckInstruction of(Opcode op, ClassDesc type) {
        return of(op, Insns.tempPool().classEntry(type));
    }
}
