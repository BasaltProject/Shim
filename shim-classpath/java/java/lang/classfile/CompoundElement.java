/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface CompoundElement<E extends ClassFileElement> extends ClassFileElement, Iterable<E> {
    default Iterator<E> iterator() { return elementList().iterator(); }
    void forEach(Consumer<? super E> consumer);
    default List<E> elementList() {
        List<E> list = new java.util.ArrayList<>();
        forEach(list::add);
        return java.util.Collections.unmodifiableList(list);
    }
    default Stream<E> elementStream() { return elementList().stream(); }
    default String toDebugString() { return shim.classfile.DebugPrinter.toDebugString(this); }
}
