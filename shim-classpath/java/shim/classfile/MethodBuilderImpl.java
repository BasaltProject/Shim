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
import java.lang.classfile.CodeModel;
import java.lang.classfile.MethodBuilder;
import java.lang.classfile.MethodElement;
import java.lang.classfile.MethodModel;
import java.lang.classfile.CodeTransform;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.constantpool.ConstantPoolBuilder;
import java.lang.classfile.constantpool.Utf8Entry;
import java.lang.reflect.AccessFlag;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import shim.classfile.Elements.AccessFlagsImpl;

/** Collects the elements of one method; the finished list becomes a {@link BufferedModels.Method}. */
public class MethodBuilderImpl implements MethodBuilder {
    final AbstractClassBuilder parent;
    final Utf8Entry name;
    final Utf8Entry type;
    final List<MethodElement> elements;
    int flags;

    public MethodBuilderImpl(AbstractClassBuilder parent, Utf8Entry name, Utf8Entry type, int flags) {
        this.parent = parent;
        this.name = name;
        this.type = type;
        this.flags = flags;
        this.elements = new ArrayList<>();
    }

    MethodBuilderImpl(MethodBuilderImpl other) {
        this.parent = other.parent;
        this.name = other.name;
        this.type = other.type;
        this.flags = other.flags;
        this.elements = other.elements;
    }

    @Override public ConstantPoolBuilder constantPool() { return parent.constantPool(); }

    @Override
    public MethodBuilder with(MethodElement element) {
        if (element instanceof Attribute<?> && !((Attribute<?>) element).attributeMapper().allowMultiple()) {
            Object mapper = ((Attribute<?>) element).attributeMapper();
            elements.removeIf(e -> e instanceof Attribute<?> && ((Attribute<?>) e).attributeMapper() == mapper);
        } else if (element instanceof java.lang.classfile.AccessFlags) {
            int updated = ((java.lang.classfile.AccessFlags) element).flagsMask();
            if (((flags ^ updated) & java.lang.classfile.ClassFile.ACC_STATIC) != 0) {
                throw new IllegalArgumentException("Cannot change ACC_STATIC flag of method");
            }
            flags = updated;
            return this;
        } else if (element instanceof CodeModel) {
            elements.removeIf(e -> e instanceof CodeModel);
        }
        elements.add(element);
        return this;
    }

    int currentFlags() {
        return flags;
    }

    MethodContext context() {
        return new MethodContext(parent.classFile(), (PoolImpl) parent.constantPool(), parent.thisClass(), name, type,
            currentFlags());
    }

    @Override
    public MethodBuilder withCode(Consumer<? super CodeBuilder> handler) {
        return with(buildCode(null, handler));
    }

    @Override
    public MethodBuilder transformCode(CodeModel model, CodeTransform transform) {
        return with(buildCode(model, cb -> cb.transform(model, transform)));
    }

    /** Runs the handler again when a short jump cannot reach its target, as the body is then laid out anew. */
    private CodeModel buildCode(CodeModel original, Consumer<? super CodeBuilder> handler) {
        CodeBuilderImpl code = new CodeBuilderImpl(context());
        code.setOriginal(original);
        handler.accept(code);
        BufferedCode model = (BufferedCode) code.toModel();
        if (CodeWriter.needsLongJumps(model)) {
            code = new CodeBuilderImpl(context());
            code.setOriginal(original);
            handler.accept(code);
            model = (BufferedCode) code.toModel();
        }
        return model;
    }

    BufferedModels.Method toModel() {
        List<MethodElement> all = new ArrayList<>(elements.size() + 1);
        all.add(new AccessFlagsImpl(AccessFlag.Location.METHOD, flags));
        all.addAll(elements);
        return new BufferedModels.Method(name, type, all);
    }
}
