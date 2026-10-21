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
import java.lang.classfile.AnnotationValue;
import java.lang.classfile.constantpool.AnnotationConstantValueEntry;
import java.lang.classfile.constantpool.DoubleEntry;
import java.lang.classfile.constantpool.FloatEntry;
import java.lang.classfile.constantpool.IntegerEntry;
import java.lang.classfile.constantpool.LongEntry;
import java.lang.classfile.constantpool.Utf8Entry;
import java.lang.constant.ClassDesc;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Implementations of the annotation element values. */
public final class AnnotationValues {
    private AnnotationValues() { }

    abstract static class Const<E extends AnnotationConstantValueEntry> {
        final E constant;
        private final int tag;
        private final String kind;

        Const(E constant, int tag, String kind) {
            this.constant = TempEntries.nonNull(constant);
            this.tag = tag;
            this.kind = kind;
        }

        public final E constant() { return constant; }
        public final int tag() { return tag; }

        @Override
        public final boolean equals(Object o) {
            return o != null && o.getClass() == getClass() && constant.equals(((Const<?>) o).constant);
        }

        @Override public final int hashCode() { return constant.hashCode(); }
        @Override public final String toString() { return kind + "[constant=" + constant + "]"; }
    }

    static final class OfStringImpl extends Const<Utf8Entry> implements AnnotationValue.OfString {
        OfStringImpl(Utf8Entry c) { super(c, TAG_STRING, "OfStringImpl"); }
        @Override public String stringValue() { return constant.stringValue(); }
        @Override public String resolvedValue() { return stringValue(); }
    }

    static final class OfIntImpl extends Const<IntegerEntry> implements AnnotationValue.OfInt {
        OfIntImpl(IntegerEntry c) { super(c, TAG_INT, "OfIntImpl"); }
        @Override public int intValue() { return constant.intValue(); }
        @Override public Integer resolvedValue() { return intValue(); }
    }

    static final class OfLongImpl extends Const<LongEntry> implements AnnotationValue.OfLong {
        OfLongImpl(LongEntry c) { super(c, TAG_LONG, "OfLongImpl"); }
        @Override public long longValue() { return constant.longValue(); }
        @Override public Long resolvedValue() { return longValue(); }
    }

    static final class OfFloatImpl extends Const<FloatEntry> implements AnnotationValue.OfFloat {
        OfFloatImpl(FloatEntry c) { super(c, TAG_FLOAT, "OfFloatImpl"); }
        @Override public float floatValue() { return constant.floatValue(); }
        @Override public Float resolvedValue() { return floatValue(); }
    }

    static final class OfDoubleImpl extends Const<DoubleEntry> implements AnnotationValue.OfDouble {
        OfDoubleImpl(DoubleEntry c) { super(c, TAG_DOUBLE, "OfDoubleImpl"); }
        @Override public double doubleValue() { return constant.doubleValue(); }
        @Override public Double resolvedValue() { return doubleValue(); }
    }

    static final class OfByteImpl extends Const<IntegerEntry> implements AnnotationValue.OfByte {
        OfByteImpl(IntegerEntry c) { super(c, TAG_BYTE, "OfByteImpl"); }
        @Override public byte byteValue() { return (byte) constant.intValue(); }
        @Override public Byte resolvedValue() { return byteValue(); }
    }

    static final class OfShortImpl extends Const<IntegerEntry> implements AnnotationValue.OfShort {
        OfShortImpl(IntegerEntry c) { super(c, TAG_SHORT, "OfShortImpl"); }
        @Override public short shortValue() { return (short) constant.intValue(); }
        @Override public Short resolvedValue() { return shortValue(); }
    }

    static final class OfCharImpl extends Const<IntegerEntry> implements AnnotationValue.OfChar {
        OfCharImpl(IntegerEntry c) { super(c, TAG_CHAR, "OfCharImpl"); }
        @Override public char charValue() { return (char) constant.intValue(); }
        @Override public Character resolvedValue() { return charValue(); }
    }

    static final class OfBooleanImpl extends Const<IntegerEntry> implements AnnotationValue.OfBoolean {
        OfBooleanImpl(IntegerEntry c) { super(c, TAG_BOOLEAN, "OfBooleanImpl"); }
        @Override public boolean booleanValue() { return constant.intValue() != 0; }
        @Override public Boolean resolvedValue() { return booleanValue(); }
    }

    static final class OfEnumImpl implements AnnotationValue.OfEnum {
        private final Utf8Entry className;
        private final Utf8Entry constantName;

        OfEnumImpl(Utf8Entry className, Utf8Entry constantName) {
            this.className = TempEntries.nonNull(className);
            this.constantName = TempEntries.nonNull(constantName);
        }

        @Override public int tag() { return TAG_ENUM; }
        @Override public Utf8Entry className() { return className; }
        @Override public Utf8Entry constantName() { return constantName; }

        @Override
        public boolean equals(Object o) {
            return o instanceof OfEnumImpl && className.equals(((OfEnumImpl) o).className)
                && constantName.equals(((OfEnumImpl) o).constantName);
        }

        @Override public int hashCode() { return TempEntries.hash(className, constantName); }

        @Override
        public String toString() {
            return "OfEnumImpl[className=" + className.stringValue() + ", constantName=" + constantName.stringValue()
                + "]";
        }
    }

    static final class OfClassImpl implements AnnotationValue.OfClass {
        private final Utf8Entry className;

        OfClassImpl(Utf8Entry className) {
            this.className = TempEntries.nonNull(className);
        }

        @Override public int tag() { return TAG_CLASS; }
        @Override public Utf8Entry className() { return className; }
        @Override
        public boolean equals(Object o) {
            return o instanceof OfClassImpl && className.equals(((OfClassImpl) o).className);
        }

        @Override public int hashCode() { return className.hashCode(); }
        @Override public String toString() { return "OfClassImpl[className=" + className.stringValue() + "]"; }
    }

    static final class OfAnnotationImpl implements AnnotationValue.OfAnnotation {
        private final Annotation annotation;

        OfAnnotationImpl(Annotation annotation) {
            this.annotation = TempEntries.nonNull(annotation);
        }

        @Override public int tag() { return TAG_ANNOTATION; }
        @Override public Annotation annotation() { return annotation; }

        @Override
        public boolean equals(Object o) {
            return o instanceof OfAnnotationImpl && annotation.equals(((OfAnnotationImpl) o).annotation);
        }

        @Override public int hashCode() { return annotation.hashCode(); }
        @Override public String toString() { return "OfAnnotationImpl[annotation=" + annotation + "]"; }
    }

    static final class OfArrayImpl implements AnnotationValue.OfArray {
        private final List<AnnotationValue> values;

        OfArrayImpl(List<AnnotationValue> values) {
            this.values = values;
        }

        @Override public int tag() { return TAG_ARRAY; }
        @Override public List<AnnotationValue> values() { return values; }
        @Override
        public boolean equals(Object o) {
            return o instanceof OfArrayImpl && values.equals(((OfArrayImpl) o).values);
        }

        @Override public int hashCode() { return values.hashCode(); }
        @Override public String toString() { return "OfArrayImpl[values=" + values + "]"; }
    }

    public static AnnotationValue.OfString ofString(Utf8Entry e) { return new OfStringImpl(e); }
    public static AnnotationValue.OfInt ofInt(IntegerEntry e) { return new OfIntImpl(e); }
    public static AnnotationValue.OfLong ofLong(LongEntry e) { return new OfLongImpl(e); }
    public static AnnotationValue.OfFloat ofFloat(FloatEntry e) { return new OfFloatImpl(e); }
    public static AnnotationValue.OfDouble ofDouble(DoubleEntry e) { return new OfDoubleImpl(e); }
    public static AnnotationValue.OfByte ofByte(IntegerEntry e) { return new OfByteImpl(e); }
    public static AnnotationValue.OfShort ofShort(IntegerEntry e) { return new OfShortImpl(e); }
    public static AnnotationValue.OfChar ofChar(IntegerEntry e) { return new OfCharImpl(e); }
    public static AnnotationValue.OfBoolean ofBoolean(IntegerEntry e) { return new OfBooleanImpl(e); }
    public static AnnotationValue.OfEnum ofEnum(Utf8Entry c, Utf8Entry n) { return new OfEnumImpl(c, n); }
    public static AnnotationValue.OfClass ofClass(Utf8Entry c) { return new OfClassImpl(c); }
    public static AnnotationValue.OfAnnotation ofAnnotation(Annotation a) { return new OfAnnotationImpl(a); }

    public static AnnotationValue.OfString ofString(String s) { return ofString(TempEntries.utf8(s)); }
    public static AnnotationValue.OfInt ofInt(int v) { return ofInt(TempEntries.intEntry(v)); }
    public static AnnotationValue.OfLong ofLong(long v) { return ofLong(TempEntries.longEntry(v)); }
    public static AnnotationValue.OfFloat ofFloat(float v) { return ofFloat(TempEntries.floatEntry(v)); }
    public static AnnotationValue.OfDouble ofDouble(double v) { return ofDouble(TempEntries.doubleEntry(v)); }
    public static AnnotationValue.OfByte ofByte(byte v) { return ofByte(TempEntries.intEntry(v)); }
    public static AnnotationValue.OfShort ofShort(short v) { return ofShort(TempEntries.intEntry(v)); }
    public static AnnotationValue.OfChar ofChar(char v) { return ofChar(TempEntries.intEntry(v)); }
    public static AnnotationValue.OfBoolean ofBoolean(boolean v) { return ofBoolean(TempEntries.intEntry(v ? 1 : 0)); }

    public static AnnotationValue.OfEnum ofEnum(ClassDesc c, String n) {
        Utf8Entry cn = TempEntries.descriptor(c);
        return ofEnum(cn, TempEntries.utf8(n));
    }

    public static AnnotationValue.OfClass ofClass(ClassDesc c) { return ofClass(TempEntries.descriptor(c)); }

    public static AnnotationValue.OfArray ofArray(List<AnnotationValue> values) {
        return new OfArrayImpl(TempEntries.copyOf(values));
    }

    public static AnnotationValue.OfArray ofArray(AnnotationValue[] values) {
        return new OfArrayImpl(TempEntries.listOf(values));
    }

    public static AnnotationValue of(Object value) {
        if (value == null) {
            throw new NullPointerException();
        }
        if (value instanceof String) {
            return ofString((String) value);
        }
        if (value instanceof Integer) {
            return ofInt((Integer) value);
        }
        if (value instanceof Boolean) {
            return ofBoolean((Boolean) value);
        }
        if (value instanceof Byte) {
            return ofByte((Byte) value);
        }
        if (value instanceof Character) {
            return ofChar((Character) value);
        }
        if (value instanceof Short) {
            return ofShort((Short) value);
        }
        if (value instanceof Long) {
            return ofLong((Long) value);
        }
        if (value instanceof Float) {
            return ofFloat((Float) value);
        }
        if (value instanceof Double) {
            return ofDouble((Double) value);
        }
        if (value instanceof ClassDesc) {
            return ofClass((ClassDesc) value);
        }
        if (value instanceof Enum) {
            Enum<?> e = (Enum<?>) value;
            return ofEnum(ClassDesc.of(e.getDeclaringClass().getName()), e.name());
        }
        if (value.getClass().isArray()) {
            int n = java.lang.reflect.Array.getLength(value);
            List<AnnotationValue> out = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                out.add(of(java.lang.reflect.Array.get(value, i)));
            }
            return new OfArrayImpl(Collections.unmodifiableList(out));
        }
        throw new IllegalArgumentException("Illegal annotation constant value type " + value.getClass());
    }
}
