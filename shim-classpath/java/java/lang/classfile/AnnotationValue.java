/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile;

import java.lang.classfile.constantpool.AnnotationConstantValueEntry;
import java.lang.classfile.constantpool.DoubleEntry;
import java.lang.classfile.constantpool.FloatEntry;
import java.lang.classfile.constantpool.IntegerEntry;
import java.lang.classfile.constantpool.LongEntry;
import java.lang.classfile.constantpool.Utf8Entry;
import java.lang.constant.ClassDesc;
import java.lang.constant.Constable;
import java.util.List;
import shim.classfile.AnnotationValues;

public interface AnnotationValue {
    public static final int TAG_BYTE = 66;
    public static final int TAG_CHAR = 67;
    public static final int TAG_DOUBLE = 68;
    public static final int TAG_FLOAT = 70;
    public static final int TAG_INT = 73;
    public static final int TAG_LONG = 74;
    public static final int TAG_SHORT = 83;
    public static final int TAG_BOOLEAN = 90;
    public static final int TAG_STRING = 115;
    public static final int TAG_ENUM = 101;
    public static final int TAG_CLASS = 99;
    public static final int TAG_ANNOTATION = 64;
    public static final int TAG_ARRAY = 91;
    public static AnnotationValue of(Object value) {
        return AnnotationValues.of(value);
    }
    public static AnnotationValue.OfClass ofClass(Utf8Entry utf8Entry) {
        return AnnotationValues.ofClass(utf8Entry);
    }
    public static AnnotationValue.OfClass ofClass(ClassDesc classDesc) {
        return AnnotationValues.ofClass(classDesc);
    }
    int tag();
    public static AnnotationValue.OfArray ofArray(List<AnnotationValue> list) {
        return AnnotationValues.ofArray(list);
    }
    public static AnnotationValue.OfArray ofArray(AnnotationValue... annotationValues) {
        return AnnotationValues.ofArray(annotationValues);
    }
    public static AnnotationValue.OfString ofString(String text) {
        return AnnotationValues.ofString(text);
    }
    public static AnnotationValue.OfString ofString(Utf8Entry utf8Entry) {
        return AnnotationValues.ofString(utf8Entry);
    }
    public static AnnotationValue.OfLong ofLong(long l) {
        return AnnotationValues.ofLong(l);
    }
    public static AnnotationValue.OfLong ofLong(LongEntry longEntry) {
        return AnnotationValues.ofLong(longEntry);
    }
    public static AnnotationValue.OfInt ofInt(int i) {
        return AnnotationValues.ofInt(i);
    }
    public static AnnotationValue.OfInt ofInt(IntegerEntry integerEntry) {
        return AnnotationValues.ofInt(integerEntry);
    }
    public static AnnotationValue.OfChar ofChar(char c) {
        return AnnotationValues.ofChar(c);
    }
    public static AnnotationValue.OfChar ofChar(IntegerEntry integerEntry) {
        return AnnotationValues.ofChar(integerEntry);
    }
    public static AnnotationValue.OfShort ofShort(short s) {
        return AnnotationValues.ofShort(s);
    }
    public static AnnotationValue.OfShort ofShort(IntegerEntry integerEntry) {
        return AnnotationValues.ofShort(integerEntry);
    }
    public static AnnotationValue.OfByte ofByte(IntegerEntry integerEntry) {
        return AnnotationValues.ofByte(integerEntry);
    }
    public static AnnotationValue.OfByte ofByte(byte b) {
        return AnnotationValues.ofByte(b);
    }
    public static AnnotationValue.OfBoolean ofBoolean(IntegerEntry integerEntry) {
        return AnnotationValues.ofBoolean(integerEntry);
    }
    public static AnnotationValue.OfBoolean ofBoolean(boolean flag) {
        return AnnotationValues.ofBoolean(flag);
    }
    public static AnnotationValue.OfDouble ofDouble(double d) {
        return AnnotationValues.ofDouble(d);
    }
    public static AnnotationValue.OfDouble ofDouble(DoubleEntry doubleEntry) {
        return AnnotationValues.ofDouble(doubleEntry);
    }
    public static AnnotationValue.OfFloat ofFloat(float f) {
        return AnnotationValues.ofFloat(f);
    }
    public static AnnotationValue.OfFloat ofFloat(FloatEntry floatEntry) {
        return AnnotationValues.ofFloat(floatEntry);
    }
    public static AnnotationValue.OfAnnotation ofAnnotation(Annotation annotation) {
        return AnnotationValues.ofAnnotation(annotation);
    }
    public static AnnotationValue.OfEnum ofEnum(ClassDesc classDesc, String text) {
        return AnnotationValues.ofEnum(classDesc, text);
    }
    public static AnnotationValue.OfEnum ofEnum(Utf8Entry utf8Entry, Utf8Entry utf8Entry2) {
        return AnnotationValues.ofEnum(utf8Entry, utf8Entry2);
    }

    public interface OfEnum extends AnnotationValue {
        Utf8Entry className();
        Utf8Entry constantName();
        default ClassDesc classSymbol() { return ClassDesc.ofDescriptor(className().stringValue()); }
    }

    public interface OfClass extends AnnotationValue {
        Utf8Entry className();
        default ClassDesc classSymbol() { return ClassDesc.ofDescriptor(className().stringValue()); }
    }

    public interface OfString extends AnnotationValue.OfConstant {
        Utf8Entry constant();
        String stringValue();
        default String resolvedValue() { return stringValue(); }
    }

    public interface OfDouble extends AnnotationValue.OfConstant {
        double doubleValue();
        DoubleEntry constant();
        default Double resolvedValue() { return doubleValue(); }
    }

    public interface OfFloat extends AnnotationValue.OfConstant {
        float floatValue();
        FloatEntry constant();
        default Float resolvedValue() { return floatValue(); }
    }

    public interface OfLong extends AnnotationValue.OfConstant {
        long longValue();
        LongEntry constant();
        default Long resolvedValue() { return longValue(); }
    }

    public interface OfInt extends AnnotationValue.OfConstant {
        int intValue();
        IntegerEntry constant();
        default Integer resolvedValue() { return intValue(); }
    }

    public interface OfShort extends AnnotationValue.OfConstant {
        short shortValue();
        IntegerEntry constant();
        default Short resolvedValue() { return shortValue(); }
    }

    public interface OfChar extends AnnotationValue.OfConstant {
        char charValue();
        IntegerEntry constant();
        default Character resolvedValue() { return charValue(); }
    }

    public interface OfByte extends AnnotationValue.OfConstant {
        byte byteValue();
        IntegerEntry constant();
        default Byte resolvedValue() { return byteValue(); }
    }

    public interface OfBoolean extends AnnotationValue.OfConstant {
        boolean booleanValue();
        IntegerEntry constant();
        default Boolean resolvedValue() { return booleanValue(); }
    }

    public interface OfArray extends AnnotationValue {
        List<AnnotationValue> values();
    }

    public interface OfConstant extends AnnotationValue {
        AnnotationConstantValueEntry constant();
        Constable resolvedValue();
    }

    public interface OfAnnotation extends AnnotationValue {
        Annotation annotation();
    }
}
