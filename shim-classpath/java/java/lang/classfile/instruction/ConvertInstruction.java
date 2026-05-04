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
import java.lang.classfile.TypeKind;
import shim.classfile.Insns;
import shim.classfile.UnboundInsns;

public interface ConvertInstruction extends Instruction {
    TypeKind fromType();

    TypeKind toType();

    static ConvertInstruction of(TypeKind fromType, TypeKind toType) {
        return of(Insns.convertOpcode(fromType, toType));
    }

    static ConvertInstruction of(Opcode op) {
        Insns.checkKind(op, Opcode.Kind.CONVERT);
        return new UnboundInsns.UnboundConvertInstruction(op);
    }
}
