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
import java.lang.constant.ClassDesc;
import java.util.List;
import shim.classfile.AnnotationImpl;

public interface Annotation {
    Utf8Entry className();
    public static Annotation of(Utf8Entry utf8Entry, AnnotationElement... annotationElements) {
        return AnnotationImpl.of(utf8Entry, annotationElements);
    }
    public static Annotation of(ClassDesc classDesc, List<AnnotationElement> list) {
        return AnnotationImpl.of(classDesc, list);
    }
    public static Annotation of(ClassDesc classDesc, AnnotationElement... annotationElements) {
        return AnnotationImpl.of(classDesc, annotationElements);
    }
    public static Annotation of(Utf8Entry utf8Entry, List<AnnotationElement> list) {
        return AnnotationImpl.of(utf8Entry, list);
    }
    List<AnnotationElement> elements();
    default ClassDesc classSymbol() { return ClassDesc.ofDescriptor(className().stringValue()); }
}
