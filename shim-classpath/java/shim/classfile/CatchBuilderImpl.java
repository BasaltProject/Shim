/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.CodeBuilder;
import java.lang.classfile.Label;
import java.lang.classfile.Opcode;
import java.lang.constant.ClassDesc;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/** The catch clauses of a {@code trying} block; each handler is a block ending at the shared end label. */
final class CatchBuilderImpl implements CodeBuilder.CatchBuilder {
    private final CodeBuilder b;
    private final BlockCodeBuilderImpl tryBlock;
    private final Label tryCatchEnd;
    private final Set<ClassDesc> catchTypes = new HashSet<>();
    private BlockCodeBuilderImpl catchBlock;

    CatchBuilderImpl(CodeBuilder b, BlockCodeBuilderImpl tryBlock, Label tryCatchEnd) {
        this.b = b;
        this.tryBlock = tryBlock;
        this.tryCatchEnd = tryCatchEnd;
    }

    @Override
    public CodeBuilder.CatchBuilder catching(ClassDesc exceptionType,
                                             Consumer<CodeBuilder.BlockCodeBuilder> catchHandler) {
        List<ClassDesc> types = exceptionType == null ? Collections.<ClassDesc>emptyList()
            : Collections.singletonList(exceptionType);
        return catchingMulti(types, catchHandler);
    }

    @Override
    public CodeBuilder.CatchBuilder catchingMulti(List<ClassDesc> exceptionTypes,
                                                  Consumer<CodeBuilder.BlockCodeBuilder> catchHandler) {
        Objects.requireNonNull(exceptionTypes);
        Objects.requireNonNull(catchHandler);
        if (catchBlock == null && tryBlock.reachable()) {
            b.branch(Opcode.GOTO, tryCatchEnd);
        }
        for (ClassDesc t : exceptionTypes) {
            if (!catchTypes.add(t)) {
                throw new IllegalArgumentException("Existing catch block catches exception of type: " + t);
            }
        }
        if (catchBlock != null) {
            catchBlock.end();
            if (catchBlock.reachable()) {
                b.branch(Opcode.GOTO, tryCatchEnd);
            }
        }
        catchBlock = new BlockCodeBuilderImpl(b, tryCatchEnd);
        Label tryStart = tryBlock.startLabel();
        Label tryEnd = tryBlock.endLabel();
        if (exceptionTypes.isEmpty()) {
            b.exceptionCatchAll(tryStart, tryEnd, catchBlock.startLabel());
        } else {
            for (ClassDesc t : exceptionTypes) {
                b.exceptionCatch(tryStart, tryEnd, catchBlock.startLabel(), b.constantPool().classEntry(t));
            }
        }
        catchBlock.start();
        catchHandler.accept(catchBlock);
        return this;
    }

    @Override
    public void catchingAll(Consumer<CodeBuilder.BlockCodeBuilder> catchAllHandler) {
        catchingMulti(Collections.<ClassDesc>emptyList(), catchAllHandler);
    }

    void finish() {
        if (catchBlock != null) {
            catchBlock.end();
        }
        b.labelBinding(tryCatchEnd);
    }
}
