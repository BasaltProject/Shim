/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile.instruction;

import java.lang.classfile.Label;
import java.lang.classfile.PseudoInstruction;
import java.lang.classfile.constantpool.Utf8Entry;
import java.lang.constant.ClassDesc;
import shim.classfile.Insns;
import shim.classfile.Pseudos;

public interface LocalVariable extends PseudoInstruction {
    int slot();

    Utf8Entry name();

    Utf8Entry type();

    default ClassDesc typeSymbol() {
        return Insns.fieldType(type());
    }

    Label startScope();

    Label endScope();

    static LocalVariable of(int slot, Utf8Entry nameEntry, Utf8Entry descriptorEntry, Label startScope,
                            Label endScope) {
        return new Pseudos.UnboundLocalVariable(slot, nameEntry, descriptorEntry, startScope, endScope);
    }

    static LocalVariable of(int slot, String name, ClassDesc descriptor, Label startScope, Label endScope) {
        Insns.checkSlot(slot);
        return of(slot, Insns.tempPool().utf8Entry(name), Insns.tempPool().utf8Entry(descriptor.descriptorString()),
            startScope, endScope);
    }
}
