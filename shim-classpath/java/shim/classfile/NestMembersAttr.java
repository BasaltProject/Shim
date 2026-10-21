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
import java.lang.classfile.attribute.NestMembersAttribute;
import java.lang.classfile.constantpool.ClassEntry;
import java.util.List;

public final class NestMembersAttr extends AttrImpl<NestMembersAttribute> implements NestMembersAttribute {
    public static final Mapper<NestMembersAttribute> MAPPER = new Mapper<>("NestMembers", AttributeStability.CP_REFS,
        (owner, r, p) -> new NestMembersAttr(r, p), NestMembersAttr::write);

    private List<ClassEntry> values;

    public NestMembersAttr(List<ClassEntry> values) {
        super(MAPPER);
        this.values = List.copyOf(values);
    }

    private NestMembersAttr(ClassReaderImpl r, int p) {
        super(MAPPER, r, p);
    }

    @Override
    public List<ClassEntry> nestMembers() {
        if (values == null) {
            values = AttrSupport.readEntryList(reader, pos, ClassEntry.class);
        }
        return values;
    }

    private static void write(BufWriterImpl out, NestMembersAttribute a) {
        if (!writeDirect(out, a)) {
            AttrSupport.writeEntryList(out, a.nestMembers());
        }
    }
}
