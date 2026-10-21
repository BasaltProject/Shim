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
import java.lang.classfile.attribute.RecordComponentInfo;
import java.lang.classfile.constantpool.Utf8Entry;
import java.util.List;
import java.util.Objects;

public final class RecordComponentInfoImpl implements RecordComponentInfo {
    private final Utf8Entry name;
    private final Utf8Entry descriptor;
    private final List<Attribute<?>> attributes;

    public RecordComponentInfoImpl(Utf8Entry name, Utf8Entry descriptor, List<Attribute<?>> attributes) {
        this.name = Objects.requireNonNull(name);
        this.descriptor = Objects.requireNonNull(descriptor);
        this.attributes = List.copyOf(attributes);
    }

    @Override public Utf8Entry name() { return name; }
    @Override public Utf8Entry descriptor() { return descriptor; }
    @Override public List<Attribute<?>> attributes() { return attributes; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RecordComponentInfoImpl)) {
            return false;
        }
        RecordComponentInfoImpl x = (RecordComponentInfoImpl) o;
        return name.equals(x.name) && descriptor.equals(x.descriptor) && attributes.equals(x.attributes);
    }

    @Override public int hashCode() { return AttrSupport.hash(name, descriptor, attributes); }

    @Override
    public String toString() {
        return "UnboundRecordComponentInfo[name=" + name + ", descriptor=" + descriptor + ", attributes="
            + attributes + "]";
    }
}
