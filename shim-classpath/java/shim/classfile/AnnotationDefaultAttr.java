/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.AnnotationValue;
import java.lang.classfile.AttributeMapper;
import java.lang.classfile.Attributes;
import java.lang.classfile.attribute.AnnotationDefaultAttribute;
import java.lang.classfile.constantpool.Utf8Entry;

public final class AnnotationDefaultAttr extends AttrBase<AnnotationDefaultAttribute>
        implements AnnotationDefaultAttribute {
    public static final Mapper<AnnotationDefaultAttribute> MAPPER = new Mapper<>(
        Attributes.NAME_ANNOTATION_DEFAULT, AttributeMapper.AttributeStability.CP_REFS,
        (owner, cf, pos) -> new AnnotationDefaultAttr(cf, pos),
        (out, a) -> AnnotationIO.writeValue(out, a.defaultValue()));

    private ClassReaderImpl reader;
    private int pos;
    private AnnotationValue value;

    private AnnotationDefaultAttr(ClassReaderImpl reader, int pos) {
        super(MAPPER, reader.readEntry(pos - 6, Utf8Entry.class));
        this.reader = reader;
        this.pos = pos;
    }

    public AnnotationDefaultAttr(AnnotationValue value) {
        super(MAPPER);
        this.value = TempEntries.nonNull(value);
    }

    @Override
    public AnnotationValue defaultValue() {
        if (value == null) {
            value = new AnnotationIO(reader, pos).value();
            reader = null;
        }
        return value;
    }
}
