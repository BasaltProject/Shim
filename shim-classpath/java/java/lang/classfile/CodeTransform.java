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
import java.util.function.Supplier;

@FunctionalInterface public interface CodeTransform extends ClassFileTransform<CodeTransform, CodeElement, CodeBuilder> {
    public static final CodeTransform ACCEPT_ALL = new CodeTransform() {
        @Override
        public void accept(CodeBuilder builder, CodeElement element) {
            builder.with(element);
        }
    };
    default CodeTransform andThen(CodeTransform next) { return new shim.classfile.Transforms.ChainedCode(this, next); }
    public static CodeTransform endHandler(Consumer<CodeBuilder> handler) {
        return new CodeTransform() {
            @Override
            public void accept(CodeBuilder builder, CodeElement element) {
                builder.with(element);
            }

            @Override
            public void atEnd(CodeBuilder builder) {
                handler.accept(builder);
            }
        };
    }
    public static CodeTransform ofStateful(Supplier<CodeTransform> supplier) { return new shim.classfile.Transforms.StatefulCode(supplier); }
}
