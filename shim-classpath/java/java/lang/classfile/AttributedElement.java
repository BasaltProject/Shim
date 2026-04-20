/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile;

import java.util.List;
import java.util.Optional;

public interface AttributedElement extends ClassFileElement {
    List<Attribute<?>> attributes();
    default <T extends Attribute<T>> Optional<T> findAttribute(AttributeMapper<T> attributeMapper) {
        for (Attribute<?> a : attributes()) {
            if (a.attributeMapper() == attributeMapper) {
                @SuppressWarnings("unchecked")
                T t = (T) a;
                return Optional.of(t);
            }
        }
        return Optional.empty();
    }
    default <T extends Attribute<T>> List<T> findAttributes(AttributeMapper<T> attributeMapper) {
        List<T> list = new java.util.ArrayList<>();
        for (Attribute<?> a : attributes()) {
            if (a.attributeMapper() == attributeMapper) {
                @SuppressWarnings("unchecked")
                T t = (T) a;
                list.add(t);
            }
        }
        return list;
    }
}
