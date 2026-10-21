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

@FunctionalInterface public interface MethodTransform extends ClassFileTransform<MethodTransform, MethodElement, MethodBuilder> {
    public static final MethodTransform ACCEPT_ALL = new MethodTransform() {
        @Override
        public void accept(MethodBuilder builder, MethodElement element) {
            builder.with(element);
        }
    };
    default MethodTransform andThen(MethodTransform next) { return new shim.classfile.Transforms.ChainedMethod(this, next); }
    public static MethodTransform endHandler(Consumer<MethodBuilder> handler) {
        return new MethodTransform() {
            @Override
            public void accept(MethodBuilder builder, MethodElement element) {
                builder.with(element);
            }

            @Override
            public void atEnd(MethodBuilder builder) {
                handler.accept(builder);
            }
        };
    }
    public static MethodTransform ofStateful(Supplier<MethodTransform> supplier) { return new shim.classfile.Transforms.StatefulMethod(supplier); }
    public static MethodTransform dropping(Predicate<MethodElement> filter) {
        return (builder, element) -> {
            if (!filter.test(element)) {
                builder.with(element);
            }
        };
    }
    public static MethodTransform transformingCode(CodeTransform codeTransform) {
        return (builder, element) -> {
            if (element instanceof CodeModel) {
                builder.transformCode((CodeModel) element, codeTransform);
            } else {
                builder.with(element);
            }
        };
    }
}
