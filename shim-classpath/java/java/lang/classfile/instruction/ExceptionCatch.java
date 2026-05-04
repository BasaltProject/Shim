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
import java.lang.classfile.constantpool.ClassEntry;
import java.util.Optional;
import shim.classfile.Pseudos;

public interface ExceptionCatch extends PseudoInstruction {
    Label handler();

    Label tryStart();

    Label tryEnd();

    Optional<ClassEntry> catchType();

    static ExceptionCatch of(Label handler, Label tryStart, Label tryEnd, Optional<ClassEntry> catchTypeEntry) {
        return new Pseudos.ExceptionCatchImpl(handler, tryStart, tryEnd, catchTypeEntry.orElse(null));
    }

    static ExceptionCatch of(Label handler, Label tryStart, Label tryEnd) {
        return new Pseudos.ExceptionCatchImpl(handler, tryStart, tryEnd, null);
    }
}
