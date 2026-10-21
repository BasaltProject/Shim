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
import java.lang.classfile.constantpool.LoadableConstantEntry;
import java.lang.constant.ConstantDesc;
import shim.classfile.Insns;
import shim.classfile.UnboundInsns;

public interface ConstantInstruction extends Instruction {
    ConstantDesc constantValue();

    TypeKind typeKind();

    interface IntrinsicConstantInstruction extends ConstantInstruction {
        @Override
        default TypeKind typeKind() {
            return Insns.constantType(opcode());
        }
    }

    interface ArgumentConstantInstruction extends ConstantInstruction {
        @Override
        Integer constantValue();

        @Override
        default TypeKind typeKind() {
            return TypeKind.INT;
        }
    }

    interface LoadConstantInstruction extends ConstantInstruction {
        LoadableConstantEntry constantEntry();

        @Override
        default TypeKind typeKind() {
            return constantEntry().typeKind();
        }
    }

    static IntrinsicConstantInstruction ofIntrinsic(Opcode op) {
        Insns.checkKind(op, Opcode.Kind.CONSTANT);
        if (op.sizeIfFixed() != 1) {
            throw new IllegalArgumentException("Wrong opcode specified; found " + op + ", expected xCONST_val");
        }
        return new UnboundInsns.UnboundIntrinsicConstantInstruction(op);
    }

    static ArgumentConstantInstruction ofArgument(Opcode op, int value) {
        Insns.checkArgument(op, value);
        return new UnboundInsns.UnboundArgumentConstantInstruction(op, value);
    }

    static LoadConstantInstruction ofLoad(Opcode op, LoadableConstantEntry constant) {
        Insns.checkKind(op, Opcode.Kind.CONSTANT);
        if (op != Opcode.LDC && op != Opcode.LDC_W && op != Opcode.LDC2_W) {
            throw new IllegalArgumentException("Wrong opcode specified; found " + op
                + ", expected LDC, LDC_W or LDC2_W");
        }
        return new UnboundInsns.UnboundLoadConstantInstruction(op, constant);
    }
}
