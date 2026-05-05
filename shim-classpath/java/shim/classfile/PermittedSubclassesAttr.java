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
import java.lang.classfile.attribute.PermittedSubclassesAttribute;
import java.lang.classfile.constantpool.ClassEntry;
import java.util.List;

public final class PermittedSubclassesAttr extends AttrImpl<PermittedSubclassesAttribute>
        implements PermittedSubclassesAttribute {
    public static final Mapper<PermittedSubclassesAttribute> MAPPER = new Mapper<>(
        "PermittedSubclasses", AttributeStability.CP_REFS,
        (owner, r, p) -> new PermittedSubclassesAttr(r, p), PermittedSubclassesAttr::write);

    private List<ClassEntry> values;

    public PermittedSubclassesAttr(List<ClassEntry> values) {
        super(MAPPER);
        this.values = List.copyOf(values);
    }

    private PermittedSubclassesAttr(ClassReaderImpl r, int p) {
        super(MAPPER, r, p);
    }

    @Override
    public List<ClassEntry> permittedSubclasses() {
        if (values == null) {
            values = AttrSupport.readEntryList(reader, pos, ClassEntry.class);
        }
        return values;
    }

    private static void write(BufWriterImpl out, PermittedSubclassesAttribute a) {
        if (!writeDirect(out, a)) {
            AttrSupport.writeEntryList(out, a.permittedSubclasses());
        }
    }
}
