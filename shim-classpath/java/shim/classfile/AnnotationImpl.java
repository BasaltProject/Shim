/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.Annotation;
import java.lang.classfile.AnnotationElement;
import java.lang.classfile.constantpool.Utf8Entry;
import java.lang.constant.ClassDesc;
import java.util.List;

public final class AnnotationImpl implements Annotation {
    private final Utf8Entry className;
    private final List<AnnotationElement> elements;

    AnnotationImpl(Utf8Entry className, List<AnnotationElement> elements) {
        this.className = className;
        this.elements = elements;
    }

    public static Annotation of(Utf8Entry className, List<AnnotationElement> elements) {
        TempEntries.nonNull(className);
        return new AnnotationImpl(className, TempEntries.copyOf(elements));
    }

    public static Annotation of(Utf8Entry className, AnnotationElement[] elements) {
        TempEntries.nonNull(className);
        return new AnnotationImpl(className, TempEntries.listOf(elements));
    }

    public static Annotation of(ClassDesc className, List<AnnotationElement> elements) {
        return of(TempEntries.descriptor(className), elements);
    }

    public static Annotation of(ClassDesc className, AnnotationElement[] elements) {
        return of(TempEntries.descriptor(className), elements);
    }

    @Override public Utf8Entry className() { return className; }
    @Override public List<AnnotationElement> elements() { return elements; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AnnotationImpl)) {
            return false;
        }
        AnnotationImpl a = (AnnotationImpl) o;
        return className.equals(a.className) && elements.equals(a.elements);
    }

    @Override public int hashCode() { return TempEntries.hash(className, elements); }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Annotation[").append(className.stringValue());
        if (!elements.isEmpty()) {
            sb.append(" [");
            for (int i = 0; i < elements.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(elements.get(i));
            }
            sb.append(']');
        }
        return sb.append(']').toString();
    }
}
