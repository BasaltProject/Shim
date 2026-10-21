/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.Instruction;
import java.lang.classfile.attribute.CodeAttribute;
import java.lang.classfile.attribute.LocalVariableInfo;
import java.lang.classfile.instruction.BranchInstruction;
import java.lang.classfile.instruction.ConstantInstruction;
import java.lang.classfile.instruction.DiscontinuedInstruction;
import java.lang.classfile.instruction.FieldInstruction;
import java.lang.classfile.instruction.IncrementInstruction;
import java.lang.classfile.instruction.InvokeDynamicInstruction;
import java.lang.classfile.instruction.InvokeInstruction;
import java.lang.classfile.instruction.LoadInstruction;
import java.lang.classfile.instruction.LookupSwitchInstruction;
import java.lang.classfile.instruction.NewMultiArrayInstruction;
import java.lang.classfile.instruction.NewObjectInstruction;
import java.lang.classfile.instruction.NewPrimitiveArrayInstruction;
import java.lang.classfile.instruction.NewReferenceArrayInstruction;
import java.lang.classfile.instruction.StoreInstruction;
import java.lang.classfile.instruction.SwitchCase;
import java.lang.classfile.instruction.TableSwitchInstruction;
import java.lang.classfile.instruction.TypeCheckInstruction;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** The operand part of one instruction line in the debug listing. */
final class DebugInstructions {
    private DebugInstructions() { }

    static void details(DebugNode in, Instruction ins, CodeAttribute com, List<LocalVariableInfo> locals, int bci) {
        if (ins instanceof LoadInstruction) {
            int slot = ((LoadInstruction) ins).slot();
            in.with("slot", slot);
            variable(in, locals, slot, bci);
        } else if (ins instanceof StoreInstruction) {
            int slot = ((StoreInstruction) ins).slot();
            in.with("slot", slot);
            variable(in, locals, slot, bci);
        } else if (ins instanceof IncrementInstruction) {
            IncrementInstruction ii = (IncrementInstruction) ins;
            in.with("slot", ii.slot());
            in.with("const", ii.constant());
            variable(in, locals, ii.slot(), bci);
        } else if (ins instanceof ConstantInstruction) {
            in.with("constant value", ((ConstantInstruction) ins).constantValue());
        } else if (ins instanceof NewObjectInstruction) {
            in.with("type", ((NewObjectInstruction) ins).className().asInternalName());
        } else if (ins instanceof NewPrimitiveArrayInstruction) {
            in.with("dimensions", 1);
            in.with("descriptor", ((NewPrimitiveArrayInstruction) ins).typeKind().name().toLowerCase(Locale.ROOT));
        } else if (ins instanceof NewReferenceArrayInstruction) {
            in.with("dimensions", 1);
            in.with("descriptor", ((NewReferenceArrayInstruction) ins).componentType().asInternalName());
        } else if (ins instanceof NewMultiArrayInstruction) {
            NewMultiArrayInstruction nmai = (NewMultiArrayInstruction) ins;
            in.with("dimensions", nmai.dimensions());
            in.with("descriptor", nmai.arrayType().asInternalName());
        } else if (ins instanceof InvokeInstruction) {
            InvokeInstruction ii = (InvokeInstruction) ins;
            in.with("owner", ii.owner().asInternalName());
            in.with("method name", ii.name().stringValue());
            in.with("method type", ii.type().stringValue());
        } else if (ins instanceof InvokeDynamicInstruction) {
            InvokeDynamicInstruction idi = (InvokeDynamicInstruction) ins;
            in.with("name", idi.name().stringValue());
            in.with("descriptor", idi.type().stringValue());
            DirectMethodHandleDesc bsm = idi.bootstrapMethod();
            in.with("bootstrap method", bsm.kind().name() + " " + internalName(bsm.owner()) + "::" + bsm.methodName());
            List<Object> args = new ArrayList<>();
            for (ConstantDesc a : idi.bootstrapArgs()) {
                args.add(a);
            }
            in.with(DebugNode.flowList("arguments", args));
        } else if (ins instanceof FieldInstruction) {
            FieldInstruction fi = (FieldInstruction) ins;
            in.with("owner", fi.owner().asInternalName());
            in.with("field name", fi.name().stringValue());
            in.with("field type", fi.type().stringValue());
        } else if (ins instanceof TypeCheckInstruction) {
            in.with("type", ((TypeCheckInstruction) ins).type().asInternalName());
        } else if (ins instanceof BranchInstruction) {
            in.with("target", com.labelToBci(((BranchInstruction) ins).target()));
        } else if (ins instanceof LookupSwitchInstruction) {
            LookupSwitchInstruction si = (LookupSwitchInstruction) ins;
            in.with(targets(com, com.labelToBci(si.defaultTarget()), si.cases()));
        } else if (ins instanceof TableSwitchInstruction) {
            TableSwitchInstruction si = (TableSwitchInstruction) ins;
            in.with(targets(com, com.labelToBci(si.defaultTarget()), si.cases()));
        } else if (ins instanceof DiscontinuedInstruction.JsrInstruction) {
            in.with("target", com.labelToBci(((DiscontinuedInstruction.JsrInstruction) ins).target()));
        } else if (ins instanceof DiscontinuedInstruction.RetInstruction) {
            in.with("slot", ((DiscontinuedInstruction.RetInstruction) ins).slot());
        }
    }

    private static DebugNode targets(CodeAttribute com, int defaultTarget, List<SwitchCase> cases) {
        List<Object> targets = new ArrayList<>();
        targets.add(defaultTarget);
        for (SwitchCase c : cases) {
            targets.add(com.labelToBci(c.target()));
        }
        return DebugNode.flowList("targets", targets);
    }

    private static void variable(DebugNode in, List<LocalVariableInfo> locals, int slot, int bci) {
        for (LocalVariableInfo v : locals) {
            if (v.slot() == slot && v.startPc() <= bci && v.length() + v.startPc() >= bci) {
                in.with("type", v.type().stringValue());
                in.with("variable name", v.name().stringValue());
                return;
            }
        }
    }

    private static String internalName(ClassDesc cd) {
        String desc = cd.descriptorString();
        return desc.charAt(0) == 'L' ? desc.substring(1, desc.length() - 1) : desc;
    }
}
