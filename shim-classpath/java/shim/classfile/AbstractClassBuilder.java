/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.ClassBuilder;
import java.lang.classfile.ClassElement;
import java.lang.classfile.FieldModel;
import java.lang.classfile.FieldTransform;
import java.lang.classfile.MethodModel;
import java.lang.classfile.MethodTransform;
import java.lang.classfile.FieldBuilder;
import java.lang.classfile.MethodBuilder;
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.classfile.constantpool.Utf8Entry;
import java.util.function.Consumer;

/** Builds members into models and routes them through {@link #with}. */
public abstract class AbstractClassBuilder implements ClassBuilder {
    abstract ClassFileImpl classFile();

    abstract ClassEntry thisClass();

    @Override
    public ClassBuilder withMethod(Utf8Entry name, Utf8Entry type, int flags, Consumer<? super MethodBuilder> handler) {
        PoolImpl pool = (PoolImpl) constantPool();
        MethodBuilderImpl mb = new MethodBuilderImpl(this, (Utf8Entry) pool.adopt(name), (Utf8Entry) pool.adopt(type), flags);
        handler.accept(mb);
        return with(mb.toModel());
    }

    @Override
    public ClassBuilder withField(Utf8Entry name, Utf8Entry type, Consumer<? super FieldBuilder> handler) {
        PoolImpl pool = (PoolImpl) constantPool();
        FieldBuilderImpl fb = new FieldBuilderImpl(this, (Utf8Entry) pool.adopt(name), (Utf8Entry) pool.adopt(type), 0);
        handler.accept(fb);
        return with(fb.toModel());
    }

    @Override
    public ClassBuilder transformMethod(MethodModel model, MethodTransform transform) {
        MethodBuilderImpl mb = new MethodBuilderImpl(this, model.methodName(), model.methodType(), model.flags().flagsMask());
        mb.transform(model, transform);
        return with(mb.toModel());
    }

    @Override
    public ClassBuilder transformField(FieldModel model, FieldTransform transform) {
        FieldBuilderImpl fb = new FieldBuilderImpl(this, model.fieldName(), model.fieldType(), model.flags().flagsMask());
        fb.transform(model, transform);
        return with(fb.toModel());
    }
}
