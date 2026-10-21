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
import java.lang.classfile.attribute.RuntimeInvisibleParameterAnnotationsAttribute;
import java.lang.classfile.constantpool.Utf8Entry;
import java.util.List;

public final class RuntimeInvisibleParameterAnnotationsAttr
        extends AttrBase<RuntimeInvisibleParameterAnnotationsAttribute>
        implements RuntimeInvisibleParameterAnnotationsAttribute {
    public static final Mapper<RuntimeInvisibleParameterAnnotationsAttribute> MAPPER = new Mapper<>(
        Attributes.NAME_RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS, AttributeMapper.AttributeStability.CP_REFS,
        (owner, cf, pos) -> new RuntimeInvisibleParameterAnnotationsAttr(cf, pos),
        (out, a) -> ParameterAnnotations.write(out, a.parameterAnnotations()));

    private ClassReaderImpl reader;
    private int pos;
    private List<List<Annotation>> parameterAnnotations;

    private RuntimeInvisibleParameterAnnotationsAttr(ClassReaderImpl reader, int pos) {
        super(MAPPER, reader.readEntry(pos - 6, Utf8Entry.class));
        this.reader = reader;
        this.pos = pos;
    }

    public RuntimeInvisibleParameterAnnotationsAttr(List<List<Annotation>> parameterAnnotations) {
        super(MAPPER);
        this.parameterAnnotations = ParameterAnnotations.copy(parameterAnnotations);
    }

    @Override
    public List<List<Annotation>> parameterAnnotations() {
        if (parameterAnnotations == null) {
            parameterAnnotations = ParameterAnnotations.read(reader, pos);
            reader = null;
        }
        return parameterAnnotations;
    }
}
