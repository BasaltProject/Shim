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
import java.lang.classfile.constantpool.ConstantPoolBuilder;
import java.lang.classfile.constantpool.MemberRefEntry;
import java.lang.classfile.constantpool.NameAndTypeEntry;
import java.lang.classfile.constantpool.Utf8Entry;
import java.lang.constant.MethodTypeDesc;
import shim.classfile.Insns;
import shim.classfile.UnboundInsns;

public interface InvokeInstruction extends Instruction {
    MemberRefEntry method();

    boolean isInterface();

    int count();

    default ClassEntry owner() {
        return method().owner();
    }

    default Utf8Entry name() {
        return method().nameAndType().name();
    }

    default Utf8Entry type() {
        return method().nameAndType().type();
    }

    default MethodTypeDesc typeSymbol() {
        return MethodTypeDesc.ofDescriptor(type().stringValue());
    }

    static InvokeInstruction of(Opcode op, MemberRefEntry method) {
        Insns.checkKind(op, Opcode.Kind.INVOKE);
        return new UnboundInsns.UnboundInvokeInstruction(op, method);
    }

    static InvokeInstruction of(Opcode op, ClassEntry owner, Utf8Entry name, Utf8Entry type, boolean isInterface) {
        return of(op, owner, Insns.pool(owner).nameAndTypeEntry(name, type), isInterface);
    }

    static InvokeInstruction of(Opcode op, ClassEntry owner, NameAndTypeEntry nameAndType, boolean isInterface) {
        ConstantPoolBuilder pool = Insns.pool(owner);
        return of(op, isInterface ? pool.interfaceMethodRefEntry(owner, nameAndType)
            : pool.methodRefEntry(owner, nameAndType));
    }
}
