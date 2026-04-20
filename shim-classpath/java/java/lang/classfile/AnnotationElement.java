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
import shim.classfile.AnnotationElementImpl;

public interface AnnotationElement {
    Utf8Entry name();
    AnnotationValue value();
    public static AnnotationElement of(String text, AnnotationValue annotationValue) {
        return AnnotationElementImpl.of(text, annotationValue);
    }
    public static AnnotationElement of(Utf8Entry utf8Entry, AnnotationValue annotationValue) {
        return AnnotationElementImpl.of(utf8Entry, annotationValue);
    }
    public static AnnotationElement ofClass(String text, ClassDesc classDesc) {
        return of(text, AnnotationValue.ofClass(classDesc));
    }
    public static AnnotationElement ofArray(String text, AnnotationValue... annotationValues) {
        return of(text, AnnotationValue.ofArray(annotationValues));
    }
    public static AnnotationElement ofString(String text, String text2) {
        return of(text, AnnotationValue.ofString(text2));
    }
    public static AnnotationElement ofLong(String text, long l) {
        return of(text, AnnotationValue.ofLong(l));
    }
    public static AnnotationElement ofInt(String text, int i) {
        return of(text, AnnotationValue.ofInt(i));
    }
    public static AnnotationElement ofChar(String text, char c) {
        return of(text, AnnotationValue.ofChar(c));
    }
    public static AnnotationElement ofShort(String text, short s) {
        return of(text, AnnotationValue.ofShort(s));
    }
    public static AnnotationElement ofByte(String text, byte b) {
        return of(text, AnnotationValue.ofByte(b));
    }
    public static AnnotationElement ofBoolean(String text, boolean flag) {
        return of(text, AnnotationValue.ofBoolean(flag));
    }
    public static AnnotationElement ofDouble(String text, double d) {
        return of(text, AnnotationValue.ofDouble(d));
    }
    public static AnnotationElement ofFloat(String text, float f) {
        return of(text, AnnotationValue.ofFloat(f));
    }
    public static AnnotationElement ofAnnotation(String text, Annotation annotation) {
        return of(text, AnnotationValue.ofAnnotation(annotation));
    }
}
