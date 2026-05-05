/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.AttributeMapper;
import java.lang.classfile.Attributes;
import java.lang.classfile.TypeAnnotation;
import java.lang.classfile.attribute.RuntimeVisibleTypeAnnotationsAttribute;
import java.lang.classfile.constantpool.Utf8Entry;
import java.util.List;

public final class RuntimeVisibleTypeAnnotationsAttr extends AttrBase<RuntimeVisibleTypeAnnotationsAttribute>
        implements RuntimeVisibleTypeAnnotationsAttribute {
    public static final Mapper<RuntimeVisibleTypeAnnotationsAttribute> MAPPER = new Mapper<>(
        Attributes.NAME_RUNTIME_VISIBLE_TYPE_ANNOTATIONS, AttributeMapper.AttributeStability.UNSTABLE,
        (owner, cf, pos) -> new RuntimeVisibleTypeAnnotationsAttr(cf, pos,
            owner instanceof LabelContext ? (LabelContext) owner : null),
        (out, a) -> TypeAnnotationIO.writeAll(out, a.annotations()));

    private ClassReaderImpl reader;
    private int pos;
    private final ClassReaderImpl source;
    private final int sourcePos;
    private LabelContext labels;
    private List<TypeAnnotation> annotations;

    private RuntimeVisibleTypeAnnotationsAttr(ClassReaderImpl reader, int pos, LabelContext labels) {
        super(MAPPER, reader.readEntry(pos - 6, Utf8Entry.class));
        this.reader = reader;
        this.pos = pos;
        this.source = reader;
        this.sourcePos = pos;
        this.labels = labels;
    }

    public RuntimeVisibleTypeAnnotationsAttr(List<TypeAnnotation> annotations) {
        super(MAPPER);
        this.annotations = TempEntries.copyOf(annotations);
        this.source = null;
        this.sourcePos = 0;
    }

    /** Copies a parsed attribute with its header unchanged when the target pool is the source pool. */
    boolean copyDirect(BufWriterImpl out) {
        if (source == null || !out.canWriteDirect(source)) {
            return false;
        }
        source.copyBytesTo(out, sourcePos - 6, 6 + source.readInt(sourcePos - 4));
        return true;
    }

    @Override
    public List<TypeAnnotation> annotations() {
        if (annotations == null) {
            annotations = TypeAnnotationIO.readAll(reader, pos, labels);
            reader = null;
            labels = null;
        }
        return annotations;
    }
}
