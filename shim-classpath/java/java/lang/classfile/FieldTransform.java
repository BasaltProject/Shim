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

@FunctionalInterface public interface FieldTransform extends ClassFileTransform<FieldTransform, FieldElement, FieldBuilder> {
    public static final FieldTransform ACCEPT_ALL = new FieldTransform() {
        @Override
        public void accept(FieldBuilder builder, FieldElement element) {
            builder.with(element);
        }
    };
    default FieldTransform andThen(FieldTransform next) { return new shim.classfile.Transforms.ChainedField(this, next); }
    public static FieldTransform endHandler(Consumer<FieldBuilder> handler) {
        return new FieldTransform() {
            @Override
            public void accept(FieldBuilder builder, FieldElement element) {
                builder.with(element);
            }

            @Override
            public void atEnd(FieldBuilder builder) {
                handler.accept(builder);
            }
        };
    }
    public static FieldTransform ofStateful(Supplier<FieldTransform> supplier) { return new shim.classfile.Transforms.StatefulField(supplier); }
    public static FieldTransform dropping(Predicate<FieldElement> filter) {
        return (builder, element) -> {
            if (!filter.test(element)) {
                builder.with(element);
            }
        };
    }
}
