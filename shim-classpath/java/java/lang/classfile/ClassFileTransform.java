/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile;

public interface ClassFileTransform<C extends ClassFileTransform<C, E, B>, E extends ClassFileElement, B extends ClassFileBuilder<E, B>> {
    void accept(B b, E e);
    C andThen(C c);
    default void atEnd(B b) {  }
    default void atStart(B b) {  }
}
