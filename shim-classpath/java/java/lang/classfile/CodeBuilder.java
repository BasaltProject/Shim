/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile;

import java.lang.classfile.constantpool.ClassEntry;
import java.lang.classfile.constantpool.FieldRefEntry;
import java.lang.classfile.constantpool.InterfaceMethodRefEntry;
import java.lang.classfile.constantpool.InvokeDynamicEntry;
import java.lang.classfile.constantpool.LoadableConstantEntry;
import java.lang.classfile.constantpool.MemberRefEntry;
import java.lang.classfile.constantpool.MethodRefEntry;
import java.lang.classfile.constantpool.Utf8Entry;
import java.lang.classfile.instruction.ArrayLoadInstruction;
import java.lang.classfile.instruction.ArrayStoreInstruction;
import java.lang.classfile.instruction.BranchInstruction;
import java.lang.classfile.instruction.CharacterRange;
import java.lang.classfile.instruction.ConstantInstruction;
import java.lang.classfile.instruction.ConvertInstruction;
import java.lang.classfile.instruction.ExceptionCatch;
import java.lang.classfile.instruction.FieldInstruction;
import java.lang.classfile.instruction.IncrementInstruction;
import java.lang.classfile.instruction.InvokeDynamicInstruction;
import java.lang.classfile.instruction.InvokeInstruction;
import java.lang.classfile.instruction.LineNumber;
import java.lang.classfile.instruction.LoadInstruction;
import java.lang.classfile.instruction.LocalVariable;
import java.lang.classfile.instruction.LocalVariableType;
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
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.DynamicCallSiteDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import shim.classfile.BlockCodeBuilderImpl;
import shim.classfile.CodeBuilderImpl;
import shim.classfile.Insns;
import shim.classfile.LabelImpl;
import shim.classfile.Transforms;

public interface CodeBuilder extends ClassFileBuilder<CodeElement, CodeBuilder> {
    Label newLabel();
    Label startLabel();
    Label endLabel();
    int receiverSlot();
    int parameterSlot(int paramNo);
    int allocateLocal(TypeKind typeKind);

    default Label newBoundLabel() {
        Label label = newLabel();
        labelBinding(label);
        return label;
    }

    default CodeBuilder transforming(CodeTransform transform, Consumer<CodeBuilder> handler) {
        CodeTransform resolved = (CodeTransform) Transforms.resolve(transform);
        resolved.atStart(this);
        handler.accept(CodeBuilderImpl.forwarding(this, resolved));
        resolved.atEnd(this);
        return this;
    }

    default CodeBuilder block(Consumer<BlockCodeBuilder> handler) {
        BlockCodeBuilderImpl.block(this, handler);
        return this;
    }

    default CodeBuilder ifThen(Consumer<BlockCodeBuilder> thenHandler) {
        return ifThen(Opcode.IFNE, thenHandler);
    }

    default CodeBuilder ifThen(Opcode opcode, Consumer<BlockCodeBuilder> thenHandler) {
        BlockCodeBuilderImpl.ifThen(this, opcode, thenHandler);
        return this;
    }

    default CodeBuilder ifThenElse(Consumer<BlockCodeBuilder> thenHandler, Consumer<BlockCodeBuilder> elseHandler) {
        return ifThenElse(Opcode.IFNE, thenHandler, elseHandler);
    }

    default CodeBuilder ifThenElse(Opcode opcode, Consumer<BlockCodeBuilder> thenHandler,
                                   Consumer<BlockCodeBuilder> elseHandler) {
        BlockCodeBuilderImpl.ifThenElse(this, opcode, thenHandler, elseHandler);
        return this;
    }

    default CodeBuilder trying(Consumer<BlockCodeBuilder> tryHandler, Consumer<CatchBuilder> catchesHandler) {
        BlockCodeBuilderImpl.trying(this, tryHandler, catchesHandler);
        return this;
    }

    default CodeBuilder loadLocal(TypeKind tk, int slot) {
        return with(LoadInstruction.of(tk, slot));
    }

    default CodeBuilder storeLocal(TypeKind tk, int slot) {
        return with(StoreInstruction.of(tk, slot));
    }

    default CodeBuilder branch(Opcode op, Label target) {
        return with(BranchInstruction.of(op, target));
    }

    default CodeBuilder return_(TypeKind tk) {
        return with(ReturnInstruction.of(tk));
    }

    default CodeBuilder fieldAccess(Opcode opcode, FieldRefEntry ref) {
        return with(FieldInstruction.of(opcode, ref));
    }

    default CodeBuilder fieldAccess(Opcode opcode, ClassDesc owner, String name, ClassDesc type) {
        return fieldAccess(opcode, constantPool().fieldRefEntry(owner, name, type));
    }

    default CodeBuilder invoke(Opcode opcode, MemberRefEntry ref) {
        return with(InvokeInstruction.of(opcode, ref));
    }

    default CodeBuilder invoke(Opcode opcode, ClassDesc owner, String name, MethodTypeDesc desc, boolean isInterface) {
        return invoke(opcode, isInterface ? constantPool().interfaceMethodRefEntry(owner, name, desc)
            : constantPool().methodRefEntry(owner, name, desc));
    }

    default CodeBuilder arrayLoad(TypeKind tk) {
        return with(ArrayLoadInstruction.of(Insns.arrayLoadOpcode(tk)));
    }

    default CodeBuilder arrayStore(TypeKind tk) {
        return with(ArrayStoreInstruction.of(Insns.arrayStoreOpcode(tk)));
    }

    default CodeBuilder conversion(TypeKind fromType, TypeKind toType) {
        Insns.conversion(this, fromType, toType);
        return this;
    }

    default CodeBuilder loadConstant(ConstantDesc value) {
        if (value == null || value == ConstantDescs.NULL) {
            return aconst_null();
        }
        if (value instanceof Integer) {
            return loadConstant(((Integer) value).intValue());
        }
        if (value instanceof Long) {
            return loadConstant(((Long) value).longValue());
        }
        if (value instanceof Float) {
            return loadConstant(((Float) value).floatValue());
        }
        if (value instanceof Double) {
            return loadConstant(((Double) value).doubleValue());
        }
        return ldc(value);
    }

    default CodeBuilder loadConstant(int value) {
        switch (value) {
            case -1: return iconst_m1();
            case 0: return iconst_0();
            case 1: return iconst_1();
            case 2: return iconst_2();
            case 3: return iconst_3();
            case 4: return iconst_4();
            case 5: return iconst_5();
            default: break;
        }
        if (value == (byte) value) {
            return bipush(value);
        }
        if (value == (short) value) {
            return sipush(value);
        }
        return ldc(constantPool().intEntry(value));
    }

    default CodeBuilder loadConstant(long value) {
        return value == 0L ? lconst_0() : value == 1L ? lconst_1() : ldc(constantPool().longEntry(value));
    }

    default CodeBuilder loadConstant(float value) {
        if (Float.floatToRawIntBits(value) == 0) {
            return fconst_0();
        }
        return value == 1.0f ? fconst_1() : value == 2.0f ? fconst_2() : ldc(constantPool().floatEntry(value));
    }

    default CodeBuilder loadConstant(double value) {
        if (Double.doubleToRawLongBits(value) == 0L) {
            return dconst_0();
        }
        return value == 1.0d ? dconst_1() : ldc(constantPool().doubleEntry(value));
    }

    default CodeBuilder nop() {
        return with(NopInstruction.of());
    }

    default CodeBuilder labelBinding(Label label) {
        return with((LabelImpl) label);
    }

    default CodeBuilder lineNumber(int line) {
        return with(LineNumber.of(line));
    }

    default CodeBuilder exceptionCatch(Label start, Label end, Label handler, ClassEntry catchType) {
        return with(ExceptionCatch.of(handler, start, end, Optional.of(catchType)));
    }

    default CodeBuilder exceptionCatch(Label start, Label end, Label handler, Optional<ClassEntry> catchType) {
        return with(ExceptionCatch.of(handler, start, end, catchType));
    }

    default CodeBuilder exceptionCatch(Label start, Label end, Label handler, ClassDesc catchType) {
        return with(ExceptionCatch.of(handler, start, end, Optional.of(constantPool().classEntry(catchType))));
    }

    default CodeBuilder exceptionCatchAll(Label start, Label end, Label handler) {
        return with(ExceptionCatch.of(handler, start, end));
    }

    default CodeBuilder characterRange(Label startScope, Label endScope, int characterRangeStart,
                                       int characterRangeEnd, int flags) {
        return with(CharacterRange.of(startScope, endScope, characterRangeStart, characterRangeEnd, flags));
    }

    default CodeBuilder localVariable(int slot, Utf8Entry nameEntry, Utf8Entry descriptorEntry, Label startScope,
                                      Label endScope) {
        return with(LocalVariable.of(slot, nameEntry, descriptorEntry, startScope, endScope));
    }

    default CodeBuilder localVariable(int slot, String name, ClassDesc descriptor, Label startScope,
                                      Label endScope) {
        return localVariable(slot, constantPool().utf8Entry(name),
            constantPool().utf8Entry(descriptor.descriptorString()), startScope, endScope);
    }

    default CodeBuilder localVariableType(int slot, Utf8Entry nameEntry, Utf8Entry signatureEntry, Label startScope,
                                          Label endScope) {
        return with(LocalVariableType.of(slot, nameEntry, signatureEntry, startScope, endScope));
    }

    default CodeBuilder localVariableType(int slot, String name, Signature signature, Label startScope,
                                          Label endScope) {
        return localVariableType(slot, constantPool().utf8Entry(name),
            constantPool().utf8Entry(signature.signatureString()), startScope, endScope);
    }

    default CodeBuilder aconst_null() {
        return with(ConstantInstruction.ofIntrinsic(Opcode.ACONST_NULL));
    }

    default CodeBuilder aaload() { return arrayLoad(TypeKind.REFERENCE); }
    default CodeBuilder aastore() { return arrayStore(TypeKind.REFERENCE); }
    default CodeBuilder baload() { return arrayLoad(TypeKind.BYTE); }
    default CodeBuilder bastore() { return arrayStore(TypeKind.BYTE); }
    default CodeBuilder caload() { return arrayLoad(TypeKind.CHAR); }
    default CodeBuilder castore() { return arrayStore(TypeKind.CHAR); }
    default CodeBuilder daload() { return arrayLoad(TypeKind.DOUBLE); }
    default CodeBuilder dastore() { return arrayStore(TypeKind.DOUBLE); }
    default CodeBuilder faload() { return arrayLoad(TypeKind.FLOAT); }
    default CodeBuilder fastore() { return arrayStore(TypeKind.FLOAT); }
    default CodeBuilder iaload() { return arrayLoad(TypeKind.INT); }
    default CodeBuilder iastore() { return arrayStore(TypeKind.INT); }
    default CodeBuilder laload() { return arrayLoad(TypeKind.LONG); }
    default CodeBuilder lastore() { return arrayStore(TypeKind.LONG); }
    default CodeBuilder saload() { return arrayLoad(TypeKind.SHORT); }
    default CodeBuilder sastore() { return arrayStore(TypeKind.SHORT); }
    default CodeBuilder aload(int slot) { return loadLocal(TypeKind.REFERENCE, slot); }
    default CodeBuilder astore(int slot) { return storeLocal(TypeKind.REFERENCE, slot); }
    default CodeBuilder areturn() { return return_(TypeKind.REFERENCE); }
    default CodeBuilder dload(int slot) { return loadLocal(TypeKind.DOUBLE, slot); }
    default CodeBuilder dstore(int slot) { return storeLocal(TypeKind.DOUBLE, slot); }
    default CodeBuilder dreturn() { return return_(TypeKind.DOUBLE); }
    default CodeBuilder fload(int slot) { return loadLocal(TypeKind.FLOAT, slot); }
    default CodeBuilder fstore(int slot) { return storeLocal(TypeKind.FLOAT, slot); }
    default CodeBuilder freturn() { return return_(TypeKind.FLOAT); }
    default CodeBuilder iload(int slot) { return loadLocal(TypeKind.INT, slot); }
    default CodeBuilder istore(int slot) { return storeLocal(TypeKind.INT, slot); }
    default CodeBuilder ireturn() { return return_(TypeKind.INT); }
    default CodeBuilder lload(int slot) { return loadLocal(TypeKind.LONG, slot); }
    default CodeBuilder lstore(int slot) { return storeLocal(TypeKind.LONG, slot); }
    default CodeBuilder lreturn() { return return_(TypeKind.LONG); }
    default CodeBuilder return_() {
        return return_(TypeKind.VOID);
    }

    default CodeBuilder iadd() { return with(OperatorInstruction.of(Opcode.IADD)); }
    default CodeBuilder ladd() { return with(OperatorInstruction.of(Opcode.LADD)); }
    default CodeBuilder fadd() { return with(OperatorInstruction.of(Opcode.FADD)); }
    default CodeBuilder dadd() { return with(OperatorInstruction.of(Opcode.DADD)); }
    default CodeBuilder isub() { return with(OperatorInstruction.of(Opcode.ISUB)); }
    default CodeBuilder lsub() { return with(OperatorInstruction.of(Opcode.LSUB)); }
    default CodeBuilder fsub() { return with(OperatorInstruction.of(Opcode.FSUB)); }
    default CodeBuilder dsub() { return with(OperatorInstruction.of(Opcode.DSUB)); }
    default CodeBuilder imul() { return with(OperatorInstruction.of(Opcode.IMUL)); }
    default CodeBuilder lmul() { return with(OperatorInstruction.of(Opcode.LMUL)); }
    default CodeBuilder fmul() { return with(OperatorInstruction.of(Opcode.FMUL)); }
    default CodeBuilder dmul() { return with(OperatorInstruction.of(Opcode.DMUL)); }
    default CodeBuilder idiv() { return with(OperatorInstruction.of(Opcode.IDIV)); }
    default CodeBuilder ldiv() { return with(OperatorInstruction.of(Opcode.LDIV)); }
    default CodeBuilder fdiv() { return with(OperatorInstruction.of(Opcode.FDIV)); }
    default CodeBuilder ddiv() { return with(OperatorInstruction.of(Opcode.DDIV)); }
    default CodeBuilder irem() { return with(OperatorInstruction.of(Opcode.IREM)); }
    default CodeBuilder lrem() { return with(OperatorInstruction.of(Opcode.LREM)); }
    default CodeBuilder frem() { return with(OperatorInstruction.of(Opcode.FREM)); }
    default CodeBuilder drem() { return with(OperatorInstruction.of(Opcode.DREM)); }
    default CodeBuilder ineg() { return with(OperatorInstruction.of(Opcode.INEG)); }
    default CodeBuilder lneg() { return with(OperatorInstruction.of(Opcode.LNEG)); }
    default CodeBuilder fneg() { return with(OperatorInstruction.of(Opcode.FNEG)); }
    default CodeBuilder dneg() { return with(OperatorInstruction.of(Opcode.DNEG)); }
    default CodeBuilder ishl() { return with(OperatorInstruction.of(Opcode.ISHL)); }
    default CodeBuilder lshl() { return with(OperatorInstruction.of(Opcode.LSHL)); }
    default CodeBuilder ishr() { return with(OperatorInstruction.of(Opcode.ISHR)); }
    default CodeBuilder lshr() { return with(OperatorInstruction.of(Opcode.LSHR)); }
    default CodeBuilder iushr() { return with(OperatorInstruction.of(Opcode.IUSHR)); }
    default CodeBuilder lushr() { return with(OperatorInstruction.of(Opcode.LUSHR)); }
    default CodeBuilder iand() { return with(OperatorInstruction.of(Opcode.IAND)); }
    default CodeBuilder land() { return with(OperatorInstruction.of(Opcode.LAND)); }
    default CodeBuilder ior() { return with(OperatorInstruction.of(Opcode.IOR)); }
    default CodeBuilder lor() { return with(OperatorInstruction.of(Opcode.LOR)); }
    default CodeBuilder ixor() { return with(OperatorInstruction.of(Opcode.IXOR)); }
    default CodeBuilder lxor() { return with(OperatorInstruction.of(Opcode.LXOR)); }
    default CodeBuilder lcmp() { return with(OperatorInstruction.of(Opcode.LCMP)); }
    default CodeBuilder fcmpl() { return with(OperatorInstruction.of(Opcode.FCMPL)); }
    default CodeBuilder fcmpg() { return with(OperatorInstruction.of(Opcode.FCMPG)); }
    default CodeBuilder dcmpl() { return with(OperatorInstruction.of(Opcode.DCMPL)); }
    default CodeBuilder dcmpg() { return with(OperatorInstruction.of(Opcode.DCMPG)); }
    default CodeBuilder arraylength() { return with(OperatorInstruction.of(Opcode.ARRAYLENGTH)); }
    default CodeBuilder i2l() { return with(ConvertInstruction.of(Opcode.I2L)); }
    default CodeBuilder i2f() { return with(ConvertInstruction.of(Opcode.I2F)); }
    default CodeBuilder i2d() { return with(ConvertInstruction.of(Opcode.I2D)); }
    default CodeBuilder l2i() { return with(ConvertInstruction.of(Opcode.L2I)); }
    default CodeBuilder l2f() { return with(ConvertInstruction.of(Opcode.L2F)); }
    default CodeBuilder l2d() { return with(ConvertInstruction.of(Opcode.L2D)); }
    default CodeBuilder f2i() { return with(ConvertInstruction.of(Opcode.F2I)); }
    default CodeBuilder f2l() { return with(ConvertInstruction.of(Opcode.F2L)); }
    default CodeBuilder f2d() { return with(ConvertInstruction.of(Opcode.F2D)); }
    default CodeBuilder d2i() { return with(ConvertInstruction.of(Opcode.D2I)); }
    default CodeBuilder d2l() { return with(ConvertInstruction.of(Opcode.D2L)); }
    default CodeBuilder d2f() { return with(ConvertInstruction.of(Opcode.D2F)); }
    default CodeBuilder i2b() { return with(ConvertInstruction.of(Opcode.I2B)); }
    default CodeBuilder i2c() { return with(ConvertInstruction.of(Opcode.I2C)); }
    default CodeBuilder i2s() { return with(ConvertInstruction.of(Opcode.I2S)); }
    default CodeBuilder pop() { return with(StackInstruction.of(Opcode.POP)); }
    default CodeBuilder pop2() { return with(StackInstruction.of(Opcode.POP2)); }
    default CodeBuilder dup() { return with(StackInstruction.of(Opcode.DUP)); }
    default CodeBuilder dup_x1() { return with(StackInstruction.of(Opcode.DUP_X1)); }
    default CodeBuilder dup_x2() { return with(StackInstruction.of(Opcode.DUP_X2)); }
    default CodeBuilder dup2() { return with(StackInstruction.of(Opcode.DUP2)); }
    default CodeBuilder dup2_x1() { return with(StackInstruction.of(Opcode.DUP2_X1)); }
    default CodeBuilder dup2_x2() { return with(StackInstruction.of(Opcode.DUP2_X2)); }
    default CodeBuilder swap() { return with(StackInstruction.of(Opcode.SWAP)); }
    default CodeBuilder iconst_m1() { return with(ConstantInstruction.ofIntrinsic(Opcode.ICONST_M1)); }
    default CodeBuilder iconst_0() { return with(ConstantInstruction.ofIntrinsic(Opcode.ICONST_0)); }
    default CodeBuilder iconst_1() { return with(ConstantInstruction.ofIntrinsic(Opcode.ICONST_1)); }
    default CodeBuilder iconst_2() { return with(ConstantInstruction.ofIntrinsic(Opcode.ICONST_2)); }
    default CodeBuilder iconst_3() { return with(ConstantInstruction.ofIntrinsic(Opcode.ICONST_3)); }
    default CodeBuilder iconst_4() { return with(ConstantInstruction.ofIntrinsic(Opcode.ICONST_4)); }
    default CodeBuilder iconst_5() { return with(ConstantInstruction.ofIntrinsic(Opcode.ICONST_5)); }
    default CodeBuilder lconst_0() { return with(ConstantInstruction.ofIntrinsic(Opcode.LCONST_0)); }
    default CodeBuilder lconst_1() { return with(ConstantInstruction.ofIntrinsic(Opcode.LCONST_1)); }
    default CodeBuilder fconst_0() { return with(ConstantInstruction.ofIntrinsic(Opcode.FCONST_0)); }
    default CodeBuilder fconst_1() { return with(ConstantInstruction.ofIntrinsic(Opcode.FCONST_1)); }
    default CodeBuilder fconst_2() { return with(ConstantInstruction.ofIntrinsic(Opcode.FCONST_2)); }
    default CodeBuilder dconst_0() { return with(ConstantInstruction.ofIntrinsic(Opcode.DCONST_0)); }
    default CodeBuilder dconst_1() { return with(ConstantInstruction.ofIntrinsic(Opcode.DCONST_1)); }
    default CodeBuilder monitorenter() { return with(MonitorInstruction.of(Opcode.MONITORENTER)); }
    default CodeBuilder monitorexit() { return with(MonitorInstruction.of(Opcode.MONITOREXIT)); }
    default CodeBuilder ifeq(Label target) { return branch(Opcode.IFEQ, target); }
    default CodeBuilder ifne(Label target) { return branch(Opcode.IFNE, target); }
    default CodeBuilder iflt(Label target) { return branch(Opcode.IFLT, target); }
    default CodeBuilder ifge(Label target) { return branch(Opcode.IFGE, target); }
    default CodeBuilder ifgt(Label target) { return branch(Opcode.IFGT, target); }
    default CodeBuilder ifle(Label target) { return branch(Opcode.IFLE, target); }
    default CodeBuilder if_icmpeq(Label target) { return branch(Opcode.IF_ICMPEQ, target); }
    default CodeBuilder if_icmpne(Label target) { return branch(Opcode.IF_ICMPNE, target); }
    default CodeBuilder if_icmplt(Label target) { return branch(Opcode.IF_ICMPLT, target); }
    default CodeBuilder if_icmpge(Label target) { return branch(Opcode.IF_ICMPGE, target); }
    default CodeBuilder if_icmpgt(Label target) { return branch(Opcode.IF_ICMPGT, target); }
    default CodeBuilder if_icmple(Label target) { return branch(Opcode.IF_ICMPLE, target); }
    default CodeBuilder if_acmpeq(Label target) { return branch(Opcode.IF_ACMPEQ, target); }
    default CodeBuilder if_acmpne(Label target) { return branch(Opcode.IF_ACMPNE, target); }
    default CodeBuilder ifnull(Label target) { return branch(Opcode.IFNULL, target); }
    default CodeBuilder ifnonnull(Label target) { return branch(Opcode.IFNONNULL, target); }
    default CodeBuilder goto_w(Label target) { return branch(Opcode.GOTO_W, target); }
    default CodeBuilder goto_(Label target) {
        return branch(Opcode.GOTO, target);
    }

    default CodeBuilder athrow() {
        return with(ThrowInstruction.of());
    }

    default CodeBuilder bipush(int b) {
        return with(ConstantInstruction.ofArgument(Opcode.BIPUSH, b));
    }

    default CodeBuilder sipush(int s) {
        return with(ConstantInstruction.ofArgument(Opcode.SIPUSH, s));
    }

    default CodeBuilder iinc(int slot, int val) {
        return with(IncrementInstruction.of(slot, val));
    }

    default CodeBuilder ldc(ConstantDesc value) {
        return ldc(constantPool().loadableConstantEntry(java.util.Objects.requireNonNull(value)));
    }

    default CodeBuilder ldc(LoadableConstantEntry entry) {
        return with(ConstantInstruction.ofLoad(shim.classfile.Insns.ldcOpcode(constantPool(), entry), entry));
    }

    default CodeBuilder anewarray(ClassEntry className) {
        return with(NewReferenceArrayInstruction.of(className));
    }

    default CodeBuilder anewarray(ClassDesc className) {
        return anewarray(constantPool().classEntry(className));
    }

    default CodeBuilder checkcast(ClassEntry type) {
        return with(TypeCheckInstruction.of(Opcode.CHECKCAST, type));
    }

    default CodeBuilder checkcast(ClassDesc type) {
        return checkcast(constantPool().classEntry(type));
    }

    default CodeBuilder instanceOf(ClassEntry target) {
        return with(TypeCheckInstruction.of(Opcode.INSTANCEOF, target));
    }

    default CodeBuilder instanceOf(ClassDesc target) {
        return instanceOf(constantPool().classEntry(target));
    }

    default CodeBuilder multianewarray(ClassEntry array, int dims) {
        return with(NewMultiArrayInstruction.of(array, dims));
    }

    default CodeBuilder multianewarray(ClassDesc array, int dims) {
        return multianewarray(constantPool().classEntry(array), dims);
    }

    default CodeBuilder new_(ClassEntry clazz) {
        return with(NewObjectInstruction.of(clazz));
    }

    default CodeBuilder new_(ClassDesc clazz) {
        return new_(constantPool().classEntry(clazz));
    }

    default CodeBuilder newarray(TypeKind typeKind) {
        return with(NewPrimitiveArrayInstruction.of(typeKind));
    }

    default CodeBuilder invokedynamic(InvokeDynamicEntry ref) {
        return with(InvokeDynamicInstruction.of(ref));
    }

    default CodeBuilder invokedynamic(DynamicCallSiteDesc ref) {
        return with(InvokeDynamicInstruction.of(constantPool().invokeDynamicEntry(ref)));
    }

    default CodeBuilder invokeinterface(InterfaceMethodRefEntry ref) {
        return invoke(Opcode.INVOKEINTERFACE, ref);
    }

    default CodeBuilder invokeinterface(ClassDesc owner, String name, MethodTypeDesc type) {
        return invoke(Opcode.INVOKEINTERFACE, constantPool().interfaceMethodRefEntry(owner, name, type));
    }

    default CodeBuilder invokespecial(MethodRefEntry ref) {
        return invoke(Opcode.INVOKESPECIAL, ref);
    }

    default CodeBuilder invokespecial(InterfaceMethodRefEntry ref) {
        return invoke(Opcode.INVOKESPECIAL, ref);
    }

    default CodeBuilder invokespecial(ClassDesc owner, String name, MethodTypeDesc type) {
        return invoke(Opcode.INVOKESPECIAL, owner, name, type, false);
    }

    default CodeBuilder invokespecial(ClassDesc owner, String name, MethodTypeDesc type, boolean isInterface) {
        return invoke(Opcode.INVOKESPECIAL, owner, name, type, isInterface);
    }

    default CodeBuilder invokestatic(MethodRefEntry ref) {
        return invoke(Opcode.INVOKESTATIC, ref);
    }

    default CodeBuilder invokestatic(InterfaceMethodRefEntry ref) {
        return invoke(Opcode.INVOKESTATIC, ref);
    }

    default CodeBuilder invokestatic(ClassDesc owner, String name, MethodTypeDesc type) {
        return invoke(Opcode.INVOKESTATIC, owner, name, type, false);
    }

    default CodeBuilder invokestatic(ClassDesc owner, String name, MethodTypeDesc type, boolean isInterface) {
        return invoke(Opcode.INVOKESTATIC, owner, name, type, isInterface);
    }

    default CodeBuilder invokevirtual(MethodRefEntry ref) {
        return invoke(Opcode.INVOKEVIRTUAL, ref);
    }

    default CodeBuilder invokevirtual(ClassDesc owner, String name, MethodTypeDesc type) {
        return invoke(Opcode.INVOKEVIRTUAL, owner, name, type, false);
    }

    default CodeBuilder getfield(FieldRefEntry ref) { return fieldAccess(Opcode.GETFIELD, ref); }
    default CodeBuilder getfield(ClassDesc owner, String name, ClassDesc type) {
        return fieldAccess(Opcode.GETFIELD, owner, name, type);
    }
    default CodeBuilder getstatic(FieldRefEntry ref) { return fieldAccess(Opcode.GETSTATIC, ref); }
    default CodeBuilder getstatic(ClassDesc owner, String name, ClassDesc type) {
        return fieldAccess(Opcode.GETSTATIC, owner, name, type);
    }
    default CodeBuilder putfield(FieldRefEntry ref) { return fieldAccess(Opcode.PUTFIELD, ref); }
    default CodeBuilder putfield(ClassDesc owner, String name, ClassDesc type) {
        return fieldAccess(Opcode.PUTFIELD, owner, name, type);
    }
    default CodeBuilder putstatic(FieldRefEntry ref) { return fieldAccess(Opcode.PUTSTATIC, ref); }
    default CodeBuilder putstatic(ClassDesc owner, String name, ClassDesc type) {
        return fieldAccess(Opcode.PUTSTATIC, owner, name, type);
    }
    default CodeBuilder lookupswitch(Label defaultTarget, List<SwitchCase> cases) {
        return with(LookupSwitchInstruction.of(defaultTarget, cases));
    }

    default CodeBuilder tableswitch(int low, int high, Label defaultTarget, List<SwitchCase> cases) {
        return with(TableSwitchInstruction.of(low, high, defaultTarget, cases));
    }

    default CodeBuilder tableswitch(Label defaultTarget, List<SwitchCase> cases) {
        int low = Integer.MAX_VALUE;
        int high = Integer.MIN_VALUE;
        for (SwitchCase c : cases) {
            low = Math.min(low, c.caseValue());
            high = Math.max(high, c.caseValue());
        }
        return tableswitch(low, high, defaultTarget, cases);
    }

    interface BlockCodeBuilder extends CodeBuilder {
        Label breakLabel();
    }

    interface CatchBuilder {
        CatchBuilder catching(ClassDesc exceptionType, Consumer<BlockCodeBuilder> catchHandler);

        CatchBuilder catchingMulti(List<ClassDesc> exceptionTypes, Consumer<BlockCodeBuilder> catchHandler);

        void catchingAll(Consumer<BlockCodeBuilder> catchAllHandler);
    }
}
