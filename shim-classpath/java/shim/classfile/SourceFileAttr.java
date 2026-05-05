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
import java.lang.classfile.attribute.SourceFileAttribute;
import java.lang.classfile.constantpool.Utf8Entry;
import java.util.Objects;

public final class SourceFileAttr extends AttrImpl<SourceFileAttribute> implements SourceFileAttribute {
    public static final Mapper<SourceFileAttribute> MAPPER = new Mapper<>("SourceFile", AttributeStability.CP_REFS,
        (owner, r, p) -> new SourceFileAttr(r, p), SourceFileAttr::write);

    private Utf8Entry value;

    public SourceFileAttr(Utf8Entry value) {
        super(MAPPER);
        this.value = Objects.requireNonNull(value);
    }

    private SourceFileAttr(ClassReaderImpl r, int p) {
        super(MAPPER, r, p);
    }

    @Override
    public Utf8Entry sourceFile() {
        if (value == null) {
            value = reader.readEntry(pos, Utf8Entry.class);
        }
        return value;
    }

    private static void write(BufWriterImpl out, SourceFileAttribute a) {
        if (!writeDirect(out, a)) {
            out.writeIndex(a.sourceFile());
        }
    }
}
