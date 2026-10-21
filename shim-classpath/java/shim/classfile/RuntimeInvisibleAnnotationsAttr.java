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
import java.lang.classfile.AttributeMapper;
import java.lang.classfile.Attributes;
import java.lang.classfile.attribute.RuntimeInvisibleAnnotationsAttribute;
import java.lang.classfile.constantpool.Utf8Entry;
import java.util.List;

public final class RuntimeInvisibleAnnotationsAttr extends AttrBase<RuntimeInvisibleAnnotationsAttribute>
        implements RuntimeInvisibleAnnotationsAttribute {
    public static final Mapper<RuntimeInvisibleAnnotationsAttribute> MAPPER = new Mapper<>(
        Attributes.NAME_RUNTIME_INVISIBLE_ANNOTATIONS, AttributeMapper.AttributeStability.CP_REFS,
        (owner, cf, pos) -> new RuntimeInvisibleAnnotationsAttr(cf, pos),
        (out, a) -> AnnotationIO.writeAnnotations(out, a.annotations()));

    private ClassReaderImpl reader;
    private int pos;
    private List<Annotation> annotations;

    private RuntimeInvisibleAnnotationsAttr(ClassReaderImpl reader, int pos) {
        super(MAPPER, reader.readEntry(pos - 6, Utf8Entry.class));
        this.reader = reader;
        this.pos = pos;
    }

    public RuntimeInvisibleAnnotationsAttr(List<Annotation> annotations) {
        super(MAPPER);
        this.annotations = TempEntries.copyOf(annotations);
    }

    @Override
    public List<Annotation> annotations() {
        if (annotations == null) {
            annotations = new AnnotationIO(reader, pos).annotations();
            reader = null;
        }
        return annotations;
    }
}
