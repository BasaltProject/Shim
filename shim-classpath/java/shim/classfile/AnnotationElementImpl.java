/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.AnnotationElement;
import java.lang.classfile.AnnotationValue;
import java.lang.classfile.constantpool.Utf8Entry;

public final class AnnotationElementImpl implements AnnotationElement {
    private final Utf8Entry name;
    private final AnnotationValue value;

    AnnotationElementImpl(Utf8Entry name, AnnotationValue value) {
        this.name = name;
        this.value = value;
    }

    public static AnnotationElement of(Utf8Entry name, AnnotationValue value) {
        return new AnnotationElementImpl(TempEntries.nonNull(name), TempEntries.nonNull(value));
    }

    public static AnnotationElement of(String name, AnnotationValue value) {
        return of(TempEntries.utf8(name), value);
    }

    @Override public Utf8Entry name() { return name; }
    @Override public AnnotationValue value() { return value; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AnnotationElementImpl)) {
            return false;
        }
        AnnotationElementImpl e = (AnnotationElementImpl) o;
        return name.equals(e.name) && value.equals(e.value);
    }

    @Override public int hashCode() { return TempEntries.hash(name, value); }
    @Override public String toString() { return name.stringValue() + "=" + value; }
}
