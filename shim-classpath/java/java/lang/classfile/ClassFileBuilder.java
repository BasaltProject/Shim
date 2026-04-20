/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile;

import java.lang.classfile.constantpool.ConstantPoolBuilder;
import java.util.function.Consumer;

public interface ClassFileBuilder<E extends ClassFileElement, B extends ClassFileBuilder<E, B>> extends Consumer<E> {
    default B transform(CompoundElement<E> compoundElement, ClassFileTransform<?, E, B> classFileTransform) {
        @SuppressWarnings("unchecked")
        B self = (B) this;
        ClassFileTransform<?, E, B> t = shim.classfile.Transforms.resolve(classFileTransform);
        t.atStart(self);
        compoundElement.forEach(e -> t.accept(self, e));
        t.atEnd(self);
        return self;
    }
    default void accept(E e) { with(e); }
    B with(E e);
    ConstantPoolBuilder constantPool();
}
