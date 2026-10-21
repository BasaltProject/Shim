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
import java.lang.classfile.attribute.SourceIDAttribute;
import java.lang.classfile.constantpool.Utf8Entry;
import java.util.Objects;

public final class SourceIDAttr extends AttrImpl<SourceIDAttribute> implements SourceIDAttribute {
    public static final Mapper<SourceIDAttribute> MAPPER = new Mapper<>("SourceID", AttributeStability.CP_REFS,
        (owner, r, p) -> new SourceIDAttr(r, p), SourceIDAttr::write);

    private Utf8Entry value;

    public SourceIDAttr(Utf8Entry value) {
        super(MAPPER);
        this.value = Objects.requireNonNull(value);
    }

    private SourceIDAttr(ClassReaderImpl r, int p) {
        super(MAPPER, r, p);
    }

    @Override
    public Utf8Entry sourceId() {
        if (value == null) {
            value = reader.readEntry(pos, Utf8Entry.class);
        }
        return value;
    }

    private static void write(BufWriterImpl out, SourceIDAttribute a) {
        if (!writeDirect(out, a)) {
            out.writeIndex(a.sourceId());
        }
    }
}
