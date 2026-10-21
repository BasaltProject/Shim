/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.AttributeMapper.AttributeStability;
import java.lang.classfile.attribute.CompilationIDAttribute;
import java.lang.classfile.constantpool.Utf8Entry;
import java.util.Objects;

public final class CompilationIDAttr extends AttrImpl<CompilationIDAttribute> implements CompilationIDAttribute {
    public static final Mapper<CompilationIDAttribute> MAPPER = new Mapper<>(
        "CompilationID", AttributeStability.CP_REFS,
        (owner, r, p) -> new CompilationIDAttr(r, p), CompilationIDAttr::write);

    private Utf8Entry value;

    public CompilationIDAttr(Utf8Entry value) {
        super(MAPPER);
        this.value = Objects.requireNonNull(value);
    }

    private CompilationIDAttr(ClassReaderImpl r, int p) {
        super(MAPPER, r, p);
    }

    @Override
    public Utf8Entry compilationId() {
        if (value == null) {
            value = reader.readEntry(pos, Utf8Entry.class);
        }
        return value;
    }

    private static void write(BufWriterImpl out, CompilationIDAttribute a) {
        if (!writeDirect(out, a)) {
            out.writeIndex(a.compilationId());
        }
    }
}
