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
import java.lang.classfile.Signature;
import java.lang.classfile.constantpool.Utf8Entry;
import shim.classfile.Insns;
import shim.classfile.Pseudos;

public interface LocalVariableType extends PseudoInstruction {
    int slot();

    Utf8Entry name();

    Utf8Entry signature();

    default Signature signatureSymbol() {
        return Signature.parseFrom(signature().stringValue());
    }

    Label startScope();

    Label endScope();

    static LocalVariableType of(int slot, Utf8Entry nameEntry, Utf8Entry signatureEntry, Label startScope,
                                Label endScope) {
        return new Pseudos.UnboundLocalVariableType(slot, nameEntry, signatureEntry, startScope, endScope);
    }

    static LocalVariableType of(int slot, String name, Signature signature, Label startScope, Label endScope) {
        Insns.checkSlot(slot);
        return of(slot, Insns.tempPool().utf8Entry(name), Insns.tempPool().utf8Entry(signature.signatureString()),
            startScope, endScope);
    }
}
