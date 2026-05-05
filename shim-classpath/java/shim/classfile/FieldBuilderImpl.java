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
import java.lang.classfile.FieldBuilder;
import java.lang.classfile.FieldElement;
import java.lang.classfile.constantpool.ConstantPoolBuilder;
import java.lang.classfile.constantpool.Utf8Entry;
import java.util.ArrayList;
import java.lang.reflect.AccessFlag;
import java.util.List;
import shim.classfile.Elements.AccessFlagsImpl;

/** Collects the elements of one field. */
public class FieldBuilderImpl implements FieldBuilder {
    final AbstractClassBuilder parent;
    final Utf8Entry name;
    final Utf8Entry type;
    final List<FieldElement> elements;
    int flags;

    public FieldBuilderImpl(AbstractClassBuilder parent, Utf8Entry name, Utf8Entry type, int flags) {
        this.parent = parent;
        this.name = name;
        this.type = type;
        this.flags = flags;
        this.elements = new ArrayList<>();
    }

    FieldBuilderImpl(FieldBuilderImpl other) {
        this.parent = other.parent;
        this.name = other.name;
        this.type = other.type;
        this.flags = other.flags;
        this.elements = other.elements;
    }

    @Override public ConstantPoolBuilder constantPool() { return parent.constantPool(); }

    @Override
    public FieldBuilder with(FieldElement element) {
        if (element instanceof Attribute<?> && !((Attribute<?>) element).attributeMapper().allowMultiple()) {
            Object mapper = ((Attribute<?>) element).attributeMapper();
            elements.removeIf(e -> e instanceof Attribute<?> && ((Attribute<?>) e).attributeMapper() == mapper);
        } else if (element instanceof java.lang.classfile.AccessFlags) {
            flags = ((java.lang.classfile.AccessFlags) element).flagsMask();
            return this;
        }
        elements.add(element);
        return this;
    }

    BufferedModels.Field toModel() {
        List<FieldElement> all = new ArrayList<>(elements.size() + 1);
        all.add(new AccessFlagsImpl(AccessFlag.Location.FIELD, flags));
        all.addAll(elements);
        return new BufferedModels.Field(name, type, all);
    }
}
