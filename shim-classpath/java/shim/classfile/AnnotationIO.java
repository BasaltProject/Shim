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
import java.lang.classfile.AnnotationValue;
import java.lang.classfile.constantpool.DoubleEntry;
import java.lang.classfile.constantpool.FloatEntry;
import java.lang.classfile.constantpool.IntegerEntry;
import java.lang.classfile.constantpool.LongEntry;
import java.lang.classfile.constantpool.Utf8Entry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Reads and writes annotations and element values (JVMS 4.7.16). */
final class AnnotationIO {
    private final ClassReaderImpl r;
    int pos;

    AnnotationIO(ClassReaderImpl r, int pos) {
        this.r = r;
        this.pos = pos;
    }

    int u1() {
        int v = r.readU1(pos);
        pos += 1;
        return v;
    }

    int peekU2() {
        return r.readU2(pos);
    }

    int u2() {
        int v = r.readU2(pos);
        pos += 2;
        return v;
    }

    private <T extends java.lang.classfile.constantpool.PoolEntry> T entry(Class<T> cls) {
        T e = r.readEntry(pos, cls);
        pos += 2;
        return e;
    }

    List<Annotation> annotations() {
        int n = u2();
        List<Annotation> out = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            out.add(annotation());
        }
        return Collections.unmodifiableList(out);
    }

    Annotation annotation() {
        Utf8Entry type = entry(Utf8Entry.class);
        int n = u2();
        List<AnnotationElement> elems = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            Utf8Entry name = entry(Utf8Entry.class);
            elems.add(new AnnotationElementImpl(name, value()));
        }
        return new AnnotationImpl(type, Collections.unmodifiableList(elems));
    }

    AnnotationValue value() {
        int start = pos;
        int tag = u1();
        switch (tag) {
            case AnnotationValue.TAG_BYTE: return AnnotationValues.ofByte(entry(IntegerEntry.class));
            case AnnotationValue.TAG_CHAR: return AnnotationValues.ofChar(entry(IntegerEntry.class));
            case AnnotationValue.TAG_SHORT: return AnnotationValues.ofShort(entry(IntegerEntry.class));
            case AnnotationValue.TAG_INT: return AnnotationValues.ofInt(entry(IntegerEntry.class));
            case AnnotationValue.TAG_BOOLEAN: return AnnotationValues.ofBoolean(entry(IntegerEntry.class));
            case AnnotationValue.TAG_LONG: return AnnotationValues.ofLong(entry(LongEntry.class));
            case AnnotationValue.TAG_FLOAT: return AnnotationValues.ofFloat(entry(FloatEntry.class));
            case AnnotationValue.TAG_DOUBLE: return AnnotationValues.ofDouble(entry(DoubleEntry.class));
            case AnnotationValue.TAG_STRING: return AnnotationValues.ofString(entry(Utf8Entry.class));
            case AnnotationValue.TAG_CLASS: return AnnotationValues.ofClass(entry(Utf8Entry.class));
            case AnnotationValue.TAG_ENUM: {
                Utf8Entry type = entry(Utf8Entry.class);
                return AnnotationValues.ofEnum(type, entry(Utf8Entry.class));
            }
            case AnnotationValue.TAG_ANNOTATION: return AnnotationValues.ofAnnotation(annotation());
            case AnnotationValue.TAG_ARRAY: {
                int n = u2();
                List<AnnotationValue> values = new ArrayList<>(n);
                for (int i = 0; i < n; i++) {
                    values.add(value());
                }
                return new AnnotationValues.OfArrayImpl(Collections.unmodifiableList(values));
            }
            default:
                throw new IllegalArgumentException("Unexpected tag '" + (char) tag + "' in AnnotationValue, pos = "
                    + start);
        }
    }

    static void writeAnnotations(BufWriterImpl out, List<Annotation> annotations) {
        out.writeU2(annotations.size());
        for (Annotation a : annotations) {
            writeAnnotation(out, a);
        }
    }

    static void writeAnnotation(BufWriterImpl out, Annotation a) {
        out.writeIndex(a.className());
        out.writeU2(a.elements().size());
        for (AnnotationElement e : a.elements()) {
            out.writeIndex(e.name());
            writeValue(out, e.value());
        }
    }

    static void writeValue(BufWriterImpl out, AnnotationValue v) {
        out.writeU1(v.tag());
        if (v instanceof AnnotationValue.OfConstant) {
            out.writeIndex(((AnnotationValue.OfConstant) v).constant());
        } else if (v instanceof AnnotationValue.OfEnum) {
            out.writeIndex(((AnnotationValue.OfEnum) v).className());
            out.writeIndex(((AnnotationValue.OfEnum) v).constantName());
        } else if (v instanceof AnnotationValue.OfClass) {
            out.writeIndex(((AnnotationValue.OfClass) v).className());
        } else if (v instanceof AnnotationValue.OfAnnotation) {
            writeAnnotation(out, ((AnnotationValue.OfAnnotation) v).annotation());
        } else {
            List<AnnotationValue> values = ((AnnotationValue.OfArray) v).values();
            out.writeU2(values.size());
            for (AnnotationValue x : values) {
                writeValue(out, x);
            }
        }
    }
}
