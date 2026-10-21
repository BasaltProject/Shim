/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile;

import java.lang.classfile.constantpool.Utf8Entry;

public abstract class CustomAttribute<T extends CustomAttribute<T>>
        implements Attribute<T>, CodeElement, ClassElement, MethodElement, FieldElement {
    private final AttributeMapper<T> mapper;

    protected CustomAttribute(AttributeMapper<T> mapper) {
        this.mapper = mapper;
    }

    @Override
    public final AttributeMapper<T> attributeMapper() {
        return mapper;
    }

    @Override
    public Utf8Entry attributeName() {
        return shim.classfile.AttrSupport.pool().utf8Entry(mapper.name());
    }

    @Override
    public String toString() {
        return "CustomAttribute[name=" + mapper.name() + "]";
    }
}
