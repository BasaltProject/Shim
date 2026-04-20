/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

@FunctionalInterface public interface ClassTransform extends ClassFileTransform<ClassTransform, ClassElement, ClassBuilder> {
    public static final ClassTransform ACCEPT_ALL = new ClassTransform() {
        @Override
        public void accept(ClassBuilder builder, ClassElement element) {
            builder.with(element);
        }
    };
    default ClassTransform andThen(ClassTransform next) { return new shim.classfile.Transforms.ChainedClass(this, next); }
    public static ClassTransform endHandler(Consumer<ClassBuilder> handler) {
        return new ClassTransform() {
            @Override
            public void accept(ClassBuilder builder, ClassElement element) {
                builder.with(element);
            }

            @Override
            public void atEnd(ClassBuilder builder) {
                handler.accept(builder);
            }
        };
    }
    public static ClassTransform ofStateful(Supplier<ClassTransform> supplier) { return new shim.classfile.Transforms.StatefulClass(supplier); }
    public static ClassTransform dropping(Predicate<ClassElement> filter) {
        return (builder, element) -> {
            if (!filter.test(element)) {
                builder.with(element);
            }
        };
    }
    public static ClassTransform transformingMethods(Predicate<MethodModel> predicate, MethodTransform methodTransform) {
        return (builder, element) -> {
            if (element instanceof MethodModel && predicate.test((MethodModel) element)) {
                builder.transformMethod((MethodModel) element, methodTransform);
            } else {
                builder.with(element);
            }
        };
    }
    public static ClassTransform transformingMethods(MethodTransform methodTransform) { return transformingMethods(m -> true, methodTransform); }
    public static ClassTransform transformingMethodBodies(Predicate<MethodModel> predicate, CodeTransform codeTransform) { return transformingMethods(predicate, MethodTransform.transformingCode(codeTransform)); }
    public static ClassTransform transformingMethodBodies(CodeTransform codeTransform) { return transformingMethods(MethodTransform.transformingCode(codeTransform)); }
    public static ClassTransform transformingFields(FieldTransform fieldTransform) {
        return (builder, element) -> {
            if (element instanceof FieldModel) {
                builder.transformField((FieldModel) element, fieldTransform);
            } else {
                builder.with(element);
            }
        };
    }
}
