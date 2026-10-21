/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.Label;
import java.lang.classfile.Opcode;
import java.lang.classfile.TypeKind;
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.classfile.constantpool.FieldRefEntry;
import java.lang.classfile.constantpool.InterfaceMethodRefEntry;
import java.lang.classfile.constantpool.InvokeDynamicEntry;
import java.lang.classfile.constantpool.LoadableConstantEntry;
import java.lang.classfile.constantpool.MemberRefEntry;
import java.lang.classfile.instruction.ArrayLoadInstruction;
import java.lang.classfile.instruction.ArrayStoreInstruction;
import java.lang.classfile.instruction.BranchInstruction;
import java.lang.classfile.instruction.ConstantInstruction;
import java.lang.classfile.instruction.ConvertInstruction;
import java.lang.classfile.instruction.DiscontinuedInstruction;
import java.lang.classfile.instruction.FieldInstruction;
import java.lang.classfile.instruction.IncrementInstruction;
import java.lang.classfile.instruction.InvokeDynamicInstruction;
import java.lang.classfile.instruction.InvokeInstruction;
import java.lang.classfile.instruction.LoadInstruction;
import java.lang.classfile.instruction.LookupSwitchInstruction;
import java.lang.classfile.instruction.MonitorInstruction;
import java.lang.classfile.instruction.NewMultiArrayInstruction;
import java.lang.classfile.instruction.NewObjectInstruction;
import java.lang.classfile.instruction.NewPrimitiveArrayInstruction;
import java.lang.classfile.instruction.NewReferenceArrayInstruction;
import java.lang.classfile.instruction.NopInstruction;
import java.lang.classfile.instruction.OperatorInstruction;
import java.lang.classfile.instruction.ReturnInstruction;
import java.lang.classfile.instruction.StackInstruction;
import java.lang.classfile.instruction.StoreInstruction;
import java.lang.classfile.instruction.SwitchCase;
import java.lang.classfile.instruction.TableSwitchInstruction;
import java.lang.classfile.instruction.ThrowInstruction;
import java.lang.classfile.instruction.TypeCheckInstruction;
import java.lang.constant.ConstantDesc;
import java.util.List;
import java.util.Objects;

/** Instructions created by factories and builders, holding their operands directly. */
public final class UnboundInsns {
    private UnboundInsns() { }

    public static final class UnboundLoadInstruction extends AbstractInsn implements LoadInstruction {
        private final int slot;

        public UnboundLoadInstruction(Opcode op, int slot) {
            super(op);
            this.slot = slot;
        }

        @Override public int slot() { return slot; }
        @Override public TypeKind typeKind() { return Insns.typeOf(op); }
        @Override public String toString() { return local("Load", op, slot); }
    }

    public static final class UnboundStoreInstruction extends AbstractInsn implements StoreInstruction {
        private final int slot;

        public UnboundStoreInstruction(Opcode op, int slot) {
            super(op);
            this.slot = slot;
        }

        @Override public int slot() { return slot; }
        @Override public TypeKind typeKind() { return Insns.typeOf(op); }
        @Override public String toString() { return local("Store", op, slot); }
    }

    public static final class UnboundIncrementInstruction extends AbstractInsn implements IncrementInstruction {
        private final int slot;
        private final int constant;

        public UnboundIncrementInstruction(int slot, int constant) {
            super(slot < 256 && constant >= -128 && constant <= 127 ? Opcode.IINC : Opcode.IINC_W);
            this.slot = slot;
            this.constant = constant;
        }

        @Override public int slot() { return slot; }
        @Override public int constant() { return constant; }
        @Override public String toString() { return increment(op, slot, constant); }
    }

    public static final class UnboundBranchInstruction extends AbstractInsn implements BranchInstruction {
        private final Label target;

        public UnboundBranchInstruction(Opcode op, Label target) {
            super(op);
            this.target = requireLabel(target);
        }

        @Override public Label target() { return target; }
        @Override public String toString() { return simple("Branch", op); }
    }

    public static final class UnboundLookupSwitchInstruction extends AbstractInsn implements LookupSwitchInstruction {
        private final Label defaultTarget;
        private final List<SwitchCase> cases;

        public UnboundLookupSwitchInstruction(Label defaultTarget, List<SwitchCase> cases) {
            super(Opcode.LOOKUPSWITCH);
            this.defaultTarget = requireLabel(defaultTarget);
            this.cases = copyCases(cases);
        }

        @Override public Label defaultTarget() { return defaultTarget; }
        @Override public List<SwitchCase> cases() { return cases; }
        @Override public String toString() { return simple("LookupSwitch", op); }
    }

    public static final class UnboundTableSwitchInstruction extends AbstractInsn implements TableSwitchInstruction {
        private final int low;
        private final int high;
        private final Label defaultTarget;
        private final List<SwitchCase> cases;

        public UnboundTableSwitchInstruction(int low, int high, Label defaultTarget, List<SwitchCase> cases) {
            super(Opcode.TABLESWITCH);
            this.low = low;
            this.high = high;
            this.defaultTarget = requireLabel(defaultTarget);
            this.cases = copyCases(cases);
        }

        @Override public int lowValue() { return low; }
        @Override public int highValue() { return high; }
        @Override public Label defaultTarget() { return defaultTarget; }
        @Override public List<SwitchCase> cases() { return cases; }
        @Override public String toString() { return simple("TableSwitch", op); }
    }

    public static final class UnboundReturnInstruction extends AbstractInsn implements ReturnInstruction {
        public UnboundReturnInstruction(Opcode op) { super(op); }

        @Override public TypeKind typeKind() { return Insns.typeOf(op); }
        @Override public String toString() { return simple("Return", op); }
    }

    public static final class UnboundThrowInstruction extends AbstractInsn implements ThrowInstruction {
        public UnboundThrowInstruction() { super(Opcode.ATHROW); }

        @Override public String toString() { return simple("Throw", op); }
    }

    public static final class UnboundFieldInstruction extends AbstractInsn implements FieldInstruction {
        private final FieldRefEntry field;

        public UnboundFieldInstruction(Opcode op, FieldRefEntry field) {
            super(op);
            this.field = Objects.requireNonNull(field);
        }

        @Override public FieldRefEntry field() { return field; }
        @Override public String toString() { return field(op, field); }
    }

    public static final class UnboundInvokeInstruction extends AbstractInsn implements InvokeInstruction {
        private final MemberRefEntry method;

        public UnboundInvokeInstruction(Opcode op, MemberRefEntry method) {
            super(op);
            this.method = Objects.requireNonNull(method);
        }

        @Override public MemberRefEntry method() { return method; }
        @Override
        public boolean isInterface() {
            return op == Opcode.INVOKEINTERFACE || method instanceof InterfaceMethodRefEntry;
        }

        @Override
        public int count() {
            return op == Opcode.INVOKEINTERFACE ? Insns.argSlots(method.type().stringValue()) + 1 : 0;
        }

        @Override public String toString() { return invoke("Invoke", op, method); }
    }

    public static final class UnboundInvokeDynamicInstruction extends AbstractInsn
            implements InvokeDynamicInstruction {
        private final InvokeDynamicEntry entry;

        public UnboundInvokeDynamicInstruction(InvokeDynamicEntry entry) {
            super(Opcode.INVOKEDYNAMIC);
            this.entry = Objects.requireNonNull(entry);
        }

        @Override public InvokeDynamicEntry invokedynamic() { return entry; }

        @Override
        public String toString() {
            return "InvokeDynamic[OP=" + op + ", bsm=" + bootstrapMethod() + " " + bootstrapArgs() + "]";
        }
    }

    public static final class UnboundNewObjectInstruction extends AbstractInsn implements NewObjectInstruction {
        private final ClassEntry type;

        public UnboundNewObjectInstruction(ClassEntry type) {
            super(Opcode.NEW);
            this.type = Objects.requireNonNull(type);
        }

        @Override public ClassEntry className() { return type; }
        @Override public String toString() { return typed("NewObj", op, type); }
    }

    public static final class UnboundNewPrimitiveArrayInstruction extends AbstractInsn
            implements NewPrimitiveArrayInstruction {
        private final TypeKind type;

        public UnboundNewPrimitiveArrayInstruction(TypeKind type) {
            super(Opcode.NEWARRAY);
            this.type = type;
        }

        @Override public TypeKind typeKind() { return type; }
        @Override public String toString() { return "NewPrimitiveArray[OP=" + op + ", type=" + type + "]"; }
    }

    public static final class UnboundNewReferenceArrayInstruction extends AbstractInsn
            implements NewReferenceArrayInstruction {
        private final ClassEntry type;

        public UnboundNewReferenceArrayInstruction(ClassEntry type) {
            super(Opcode.ANEWARRAY);
            this.type = Objects.requireNonNull(type);
        }

        @Override public ClassEntry componentType() { return type; }
        @Override public String toString() { return typed("NewRefArray", op, type); }
    }

    public static final class UnboundNewMultidimensionalArrayInstruction extends AbstractInsn
            implements NewMultiArrayInstruction {
        private final ClassEntry type;
        private final int dimensions;

        public UnboundNewMultidimensionalArrayInstruction(ClassEntry type, int dimensions) {
            super(Opcode.MULTIANEWARRAY);
            this.type = Objects.requireNonNull(type);
            this.dimensions = dimensions;
        }

        @Override public ClassEntry arrayType() { return type; }
        @Override public int dimensions() { return dimensions; }

        @Override
        public String toString() {
            return "NewMultiArray[OP=" + op + ", type=" + type.asInternalName() + "[" + dimensions + "]]";
        }
    }

    public static final class UnboundTypeCheckInstruction extends AbstractInsn implements TypeCheckInstruction {
        private final ClassEntry type;

        public UnboundTypeCheckInstruction(Opcode op, ClassEntry type) {
            super(op);
            this.type = Objects.requireNonNull(type);
        }

        @Override public ClassEntry type() { return type; }
        @Override public String toString() { return typed("TypeCheck", op, type); }
    }

    public static final class UnboundArgumentConstantInstruction extends AbstractInsn
            implements ConstantInstruction.ArgumentConstantInstruction {
        private final int value;

        public UnboundArgumentConstantInstruction(Opcode op, int value) {
            super(op);
            this.value = value;
        }

        @Override public Integer constantValue() { return value; }
        @Override public String toString() { return value("ArgumentConstant", op, value); }
    }

    public static final class UnboundIntrinsicConstantInstruction extends AbstractInsn
            implements ConstantInstruction.IntrinsicConstantInstruction {
        public UnboundIntrinsicConstantInstruction(Opcode op) { super(op); }

        @Override public ConstantDesc constantValue() { return Insns.intrinsicValue(op); }
    }

    public static final class UnboundLoadConstantInstruction extends AbstractInsn
            implements ConstantInstruction.LoadConstantInstruction {
        private final LoadableConstantEntry entry;

        public UnboundLoadConstantInstruction(Opcode op, LoadableConstantEntry entry) {
            super(op);
            this.entry = Objects.requireNonNull(entry);
        }

        @Override public LoadableConstantEntry constantEntry() { return entry; }
        @Override public ConstantDesc constantValue() { return entry.constantValue(); }
        @Override public String toString() { return value("LoadConstant", op, constantValue()); }
    }

    public static final class UnboundMonitorInstruction extends AbstractInsn implements MonitorInstruction {
        public UnboundMonitorInstruction(Opcode op) { super(op); }
    }

    public static final class UnboundNopInstruction extends AbstractInsn implements NopInstruction {
        public UnboundNopInstruction() { super(Opcode.NOP); }
    }

    public static final class UnboundStackInstruction extends AbstractInsn implements StackInstruction {
        public UnboundStackInstruction(Opcode op) { super(op); }
    }

    public static final class UnboundOperatorInstruction extends AbstractInsn implements OperatorInstruction {
        public UnboundOperatorInstruction(Opcode op) { super(op); }

        @Override public TypeKind typeKind() { return Insns.typeOf(op); }
    }

    public static final class UnboundConvertInstruction extends AbstractInsn implements ConvertInstruction {
        public UnboundConvertInstruction(Opcode op) { super(op); }

        @Override public TypeKind fromType() { return Insns.convertFrom(op); }
        @Override public TypeKind toType() { return Insns.convertTo(op); }
    }

    public static final class UnboundArrayLoadInstruction extends AbstractInsn implements ArrayLoadInstruction {
        public UnboundArrayLoadInstruction(Opcode op) { super(op); }

        @Override public TypeKind typeKind() { return Insns.typeOf(op); }
    }

    public static final class UnboundArrayStoreInstruction extends AbstractInsn implements ArrayStoreInstruction {
        public UnboundArrayStoreInstruction(Opcode op) { super(op); }

        @Override public TypeKind typeKind() { return Insns.typeOf(op); }
    }

    public static final class UnboundJsrInstruction extends AbstractInsn
            implements DiscontinuedInstruction.JsrInstruction {
        private final Label target;

        public UnboundJsrInstruction(Opcode op, Label target) {
            super(op);
            this.target = requireLabel(target);
        }

        @Override public Label target() { return target; }
        @Override public String toString() { return simple("Discontinued", op); }
    }

    public static final class UnboundRetInstruction extends AbstractInsn
            implements DiscontinuedInstruction.RetInstruction {
        private final int slot;

        public UnboundRetInstruction(Opcode op, int slot) {
            super(op);
            this.slot = slot;
        }

        @Override public int slot() { return slot; }
        @Override public String toString() { return simple("Discontinued", op); }
    }
}
