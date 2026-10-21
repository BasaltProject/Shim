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
import java.lang.constant.ConstantDesc;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Instructions decoded from a parsed code array; operands are read from the bytes on demand. */
public final class BoundInsns {
    private BoundInsns() { }

    abstract static class Bound extends AbstractInsn {
        final CodeImpl code;
        final int pos;
        private final int size;

        Bound(Opcode op, CodeImpl code, int pos, int size) {
            super(op);
            this.code = code;
            this.pos = pos;
            this.size = size;
        }

        @Override public int sizeInBytes() { return size; }

        int localSlot() { return op.isWide() ? code.u2(pos + 2) : code.u1(pos + 1); }

        Label branch(int offset) { return code.getLabel(pos + offset); }
    }

    public static final class BoundLoadInstruction extends Bound implements LoadInstruction {
        BoundLoadInstruction(Opcode op, CodeImpl code, int pos, int size) { super(op, code, pos, size); }

        @Override public int slot() { return localSlot(); }
        @Override public TypeKind typeKind() { return Insns.typeOf(op); }
        @Override public String toString() { return local("Load", op, slot()); }
    }

    public static final class BoundStoreInstruction extends Bound implements StoreInstruction {
        BoundStoreInstruction(Opcode op, CodeImpl code, int pos, int size) { super(op, code, pos, size); }

        @Override public int slot() { return localSlot(); }
        @Override public TypeKind typeKind() { return Insns.typeOf(op); }
        @Override public String toString() { return local("Store", op, slot()); }
    }

    public static final class BoundIncrementInstruction extends Bound implements IncrementInstruction {
        BoundIncrementInstruction(Opcode op, CodeImpl code, int pos, int size) { super(op, code, pos, size); }

        @Override public int slot() { return localSlot(); }
        @Override public int constant() { return op.isWide() ? code.s2(pos + 4) : code.s1(pos + 2); }
        @Override public String toString() { return increment(op, slot(), constant()); }
    }

    public static final class BoundBranchInstruction extends Bound implements BranchInstruction {
        BoundBranchInstruction(Opcode op, CodeImpl code, int pos, int size) { super(op, code, pos, size); }

        @Override public Label target() { return branch(op == Opcode.GOTO_W ? code.s4(pos + 1) : code.s2(pos + 1)); }
        @Override public String toString() { return simple("Branch", op); }
    }

    public static final class BoundJsrInstruction extends Bound implements DiscontinuedInstruction.JsrInstruction {
        BoundJsrInstruction(Opcode op, CodeImpl code, int pos, int size) { super(op, code, pos, size); }

        @Override public Label target() { return branch(op == Opcode.JSR_W ? code.s4(pos + 1) : code.s2(pos + 1)); }
        @Override public String toString() { return simple("Discontinued", op); }
    }

    public static final class BoundRetInstruction extends Bound implements DiscontinuedInstruction.RetInstruction {
        BoundRetInstruction(Opcode op, CodeImpl code, int pos, int size) { super(op, code, pos, size); }

        @Override public int slot() { return localSlot(); }
        @Override public String toString() { return simple("Discontinued", op); }
    }

    static int switchBase(int pos) {
        return pos + 1 + ((4 - ((pos + 1) & 3)) & 3);
    }

    public static final class BoundTableSwitchInstruction extends Bound implements TableSwitchInstruction {
        BoundTableSwitchInstruction(CodeImpl code, int pos, int size) { super(Opcode.TABLESWITCH, code, pos, size); }

        @Override public Label defaultTarget() { return branch(code.s4(switchBase(pos))); }
        @Override public int lowValue() { return code.s4(switchBase(pos) + 4); }
        @Override public int highValue() { return code.s4(switchBase(pos) + 8); }

        @Override
        public List<SwitchCase> cases() {
            int base = switchBase(pos);
            int low = lowValue();
            int n = highValue() - low + 1;
            int dflt = code.s4(base);
            List<SwitchCase> out = new ArrayList<>(Math.max(n, 0));
            for (int i = 0; i < n; i++) {
                int offset = code.s4(base + 12 + 4 * i);
                if (offset != dflt) {
                    out.add(new Pseudos.SwitchCaseImpl(low + i, branch(offset)));
                }
            }
            return Collections.unmodifiableList(out);
        }

        @Override public String toString() { return simple("TableSwitch", op); }
    }

    public static final class BoundLookupSwitchInstruction extends Bound implements LookupSwitchInstruction {
        BoundLookupSwitchInstruction(CodeImpl code, int pos, int size) { super(Opcode.LOOKUPSWITCH, code, pos, size); }

        @Override public Label defaultTarget() { return branch(code.s4(switchBase(pos))); }

        @Override
        public List<SwitchCase> cases() {
            int base = switchBase(pos);
            int n = code.s4(base + 4);
            List<SwitchCase> out = new ArrayList<>(Math.max(n, 0));
            for (int i = 0; i < n; i++) {
                int p = base + 8 + 8 * i;
                out.add(new Pseudos.SwitchCaseImpl(code.s4(p), branch(code.s4(p + 4))));
            }
            return Collections.unmodifiableList(out);
        }

        @Override public String toString() { return simple("LookupSwitch", op); }
    }

    public static final class BoundFieldInstruction extends Bound implements FieldInstruction {
        BoundFieldInstruction(Opcode op, CodeImpl code, int pos) { super(op, code, pos, 3); }

        @Override public FieldRefEntry field() { return code.entry(pos + 1, FieldRefEntry.class); }
        @Override public String toString() { return field(op, field()); }
    }

    public static final class BoundInvokeInstruction extends Bound implements InvokeInstruction {
        BoundInvokeInstruction(Opcode op, CodeImpl code, int pos) { super(op, code, pos, 3); }

        @Override public MemberRefEntry method() { return code.entry(pos + 1, MemberRefEntry.class); }
        @Override public boolean isInterface() { return method() instanceof InterfaceMethodRefEntry; }
        @Override public int count() { return 0; }
        @Override public String toString() { return invoke("Invoke", op, method()); }
    }

    public static final class BoundInvokeInterfaceInstruction extends Bound implements InvokeInstruction {
        BoundInvokeInterfaceInstruction(CodeImpl code, int pos) { super(Opcode.INVOKEINTERFACE, code, pos, 5); }

        @Override public MemberRefEntry method() { return code.entry(pos + 1, InterfaceMethodRefEntry.class); }
        @Override public boolean isInterface() { return true; }
        @Override public int count() { return code.u1(pos + 3); }
        @Override public String toString() { return invoke("InvokeInterface", op, method()); }
    }

    public static final class BoundInvokeDynamicInstruction extends Bound implements InvokeDynamicInstruction {
        BoundInvokeDynamicInstruction(CodeImpl code, int pos) { super(Opcode.INVOKEDYNAMIC, code, pos, 5); }

        @Override public InvokeDynamicEntry invokedynamic() { return code.entry(pos + 1, InvokeDynamicEntry.class); }

        @Override
        public String toString() {
            return "InvokeDynamic[OP=" + op + ", bsm=" + bootstrapMethod() + " " + bootstrapArgs() + "]";
        }
    }

    public static final class BoundNewObjectInstruction extends Bound implements NewObjectInstruction {
        BoundNewObjectInstruction(CodeImpl code, int pos) { super(Opcode.NEW, code, pos, 3); }

        @Override public ClassEntry className() { return code.entry(pos + 1, ClassEntry.class); }
        @Override public String toString() { return typed("NewObj", op, className()); }
    }

    public static final class BoundNewPrimitiveArrayInstruction extends Bound implements NewPrimitiveArrayInstruction {
        BoundNewPrimitiveArrayInstruction(CodeImpl code, int pos) { super(Opcode.NEWARRAY, code, pos, 2); }

        @Override public TypeKind typeKind() { return TypeKind.fromNewarrayCode(code.u1(pos + 1)); }
        @Override public String toString() { return "NewPrimitiveArray[OP=" + op + ", type=" + typeKind() + "]"; }
    }

    public static final class BoundNewReferenceArrayInstruction extends Bound implements NewReferenceArrayInstruction {
        BoundNewReferenceArrayInstruction(CodeImpl code, int pos) { super(Opcode.ANEWARRAY, code, pos, 3); }

        @Override public ClassEntry componentType() { return code.entry(pos + 1, ClassEntry.class); }
        @Override public String toString() { return typed("NewRefArray", op, componentType()); }
    }

    public static final class BoundNewMultidimensionalArrayInstruction extends Bound
            implements NewMultiArrayInstruction {
        BoundNewMultidimensionalArrayInstruction(CodeImpl code, int pos) {
            super(Opcode.MULTIANEWARRAY, code, pos, 4);
        }

        @Override public ClassEntry arrayType() { return code.entry(pos + 1, ClassEntry.class); }
        @Override public int dimensions() { return code.u1(pos + 3); }

        @Override
        public String toString() {
            return "NewMultiArray[OP=" + op + ", type=" + arrayType().asInternalName() + "[" + dimensions() + "]]";
        }
    }

    public static final class BoundTypeCheckInstruction extends Bound implements TypeCheckInstruction {
        BoundTypeCheckInstruction(Opcode op, CodeImpl code, int pos) { super(op, code, pos, 3); }

        @Override public ClassEntry type() { return code.entry(pos + 1, ClassEntry.class); }
        @Override public String toString() { return typed("TypeCheck", op, type()); }
    }

    public static final class BoundArgumentConstantInstruction extends Bound
            implements ConstantInstruction.ArgumentConstantInstruction {
        BoundArgumentConstantInstruction(Opcode op, CodeImpl code, int pos, int size) { super(op, code, pos, size); }

        @Override public Integer constantValue() { return op == Opcode.BIPUSH ? code.s1(pos + 1) : code.s2(pos + 1); }
        @Override public String toString() { return value("ArgumentConstant", op, constantValue()); }
    }

    public static final class BoundLoadConstantInstruction extends Bound
            implements ConstantInstruction.LoadConstantInstruction {
        BoundLoadConstantInstruction(Opcode op, CodeImpl code, int pos, int size) { super(op, code, pos, size); }

        @Override
        public LoadableConstantEntry constantEntry() {
            int index = op == Opcode.LDC ? code.u1(pos + 1) : code.u2(pos + 1);
            return code.reader.entryByIndex(index, LoadableConstantEntry.class);
        }

        @Override public ConstantDesc constantValue() { return constantEntry().constantValue(); }
        @Override public String toString() { return value("LoadConstant", op, constantValue()); }
    }
}
