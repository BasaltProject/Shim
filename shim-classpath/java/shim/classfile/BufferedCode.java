/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.Attribute;
import java.lang.classfile.CodeElement;
import java.lang.classfile.CodeModel;
import java.lang.classfile.Label;
import java.lang.classfile.MethodModel;
import java.lang.classfile.instruction.ExceptionCatch;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/** The elements of a code body built by a {@link CodeBuilderImpl}, from its start label to its end label. */
public final class BufferedCode implements CodeModel, LabelContext {
    private final CodeBuilderImpl builder;
    private final List<CodeElement> elements;

    BufferedCode(CodeBuilderImpl builder, List<CodeElement> elements) {
        this.builder = builder;
        this.elements = Collections.unmodifiableList(elements);
    }

    /** The method this body was built for. */
    public MethodContext context() { return builder.context; }

    /** The builder that produced this body. */
    public CodeBuilderImpl builder() { return builder; }

    /** All elements, including the start and end labels. */
    public List<CodeElement> elements() { return elements; }

    @Override public Optional<MethodModel> parent() { return Optional.empty(); }
    @Override public List<Attribute<?>> attributes() { return Collections.emptyList(); }

    @Override
    public List<ExceptionCatch> exceptionHandlers() {
        builder.export(elements);
        List<ExceptionCatch> out = new ArrayList<>();
        for (CodeElement e : elements) {
            if (e instanceof ExceptionCatch) {
                out.add((ExceptionCatch) e);
            }
        }
        return Collections.unmodifiableList(out);
    }

    @Override
    public void forEach(Consumer<? super CodeElement> consumer) {
        builder.export(elements);
        for (CodeElement e : elements) {
            consumer.accept(e);
        }
    }

    @Override public Label newLabel() { return builder.newLabel(); }
    @Override public Label getLabel(int bci) { return builder.getLabel(bci); }
    @Override public int labelToBci(Label label) { return builder.labelToBci(label); }

    @Override
    public String toString() {
        return "CodeModel[id=" + Integer.toHexString(hashCode()) + "]";
    }
}
