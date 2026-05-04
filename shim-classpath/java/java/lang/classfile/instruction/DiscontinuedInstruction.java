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
import java.lang.classfile.Opcode;
import shim.classfile.Insns;
import shim.classfile.UnboundInsns;

public interface DiscontinuedInstruction extends Instruction {
    interface JsrInstruction extends DiscontinuedInstruction {
        Label target();

        static JsrInstruction of(Opcode op, Label target) {
            Insns.checkKind(op, Opcode.Kind.DISCONTINUED_JSR);
            return new UnboundInsns.UnboundJsrInstruction(op, target);
        }

        static JsrInstruction of(Label target) {
            return of(Opcode.JSR, target);
        }
    }

    interface RetInstruction extends DiscontinuedInstruction {
        int slot();

        static RetInstruction of(Opcode op, int slot) {
            Insns.checkRetSlot(op, slot);
            Insns.checkKind(op, Opcode.Kind.DISCONTINUED_RET);
            return new UnboundInsns.UnboundRetInstruction(op, slot);
        }

        static RetInstruction of(int slot) {
            return of(slot < 256 ? Opcode.RET : Opcode.RET_W, slot);
        }
    }
}
