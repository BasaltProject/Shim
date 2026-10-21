/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.Attribute;
import java.lang.classfile.AttributeMapper;
import java.lang.classfile.constantpool.Utf8Entry;

/** Common base of the attribute implementations: name entry, mapper and the shared toString. */
public abstract class AttrBase<A extends Attribute<A>> implements Attribute<A> {
    private final AttributeMapper<A> mapper;
    private Utf8Entry name;

    protected AttrBase(AttributeMapper<A> mapper) {
        this.mapper = mapper;
    }

    protected AttrBase(AttributeMapper<A> mapper, Utf8Entry name) {
        this.mapper = mapper;
        this.name = name;
    }

    @Override
    public Utf8Entry attributeName() {
        if (name == null) {
            name = new PoolImpl().utf8Entry(mapper.name());
        }
        return name;
    }

    @Override public AttributeMapper<A> attributeMapper() { return mapper; }

    @Override public String toString() { return "Attribute[name=" + mapper.name() + "]"; }
}
