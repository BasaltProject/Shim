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
import java.lang.classfile.attribute.ExceptionsAttribute;
import java.lang.classfile.constantpool.ClassEntry;
import java.util.List;

public final class ExceptionsAttr extends AttrImpl<ExceptionsAttribute> implements ExceptionsAttribute {
    public static final Mapper<ExceptionsAttribute> MAPPER = new Mapper<>("Exceptions", AttributeStability.CP_REFS,
        (owner, r, p) -> new ExceptionsAttr(r, p), ExceptionsAttr::write);

    private List<ClassEntry> values;

    public ExceptionsAttr(List<ClassEntry> values) {
        super(MAPPER);
        this.values = List.copyOf(values);
    }

    private ExceptionsAttr(ClassReaderImpl r, int p) {
        super(MAPPER, r, p);
    }

    @Override
    public List<ClassEntry> exceptions() {
        if (values == null) {
            values = AttrSupport.readEntryList(reader, pos, ClassEntry.class);
        }
        return values;
    }

    private static void write(BufWriterImpl out, ExceptionsAttribute a) {
        if (!writeDirect(out, a)) {
            AttrSupport.writeEntryList(out, a.exceptions());
        }
    }
}
