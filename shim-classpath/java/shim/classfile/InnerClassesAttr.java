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
import java.lang.classfile.attribute.InnerClassInfo;
import java.lang.classfile.attribute.InnerClassesAttribute;
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.classfile.constantpool.Utf8Entry;
import java.util.List;
import java.util.Optional;

public final class InnerClassesAttr extends AttrImpl<InnerClassesAttribute>
        implements InnerClassesAttribute {
    public static final Mapper<InnerClassesAttribute> MAPPER = new Mapper<>("InnerClasses",
        AttributeStability.CP_REFS, false,
        (owner, r, p) -> new InnerClassesAttr(r, p), InnerClassesAttr::write);

    private List<InnerClassInfo> list;

    public InnerClassesAttr(List<InnerClassInfo> list) {
        super(MAPPER);
        this.list = List.copyOf(list);
    }

    private InnerClassesAttr(ClassReaderImpl r, int p) {
        super(MAPPER, r, p);
    }

    @Override
    public List<InnerClassInfo> classes() {
        if (list == null) {
            ClassReaderImpl r = reader;
            int n = r.readU2(pos);
            InnerClassInfo[] out = new InnerClassInfo[n];
            int q = pos + 2;
            for (int i = 0; i < n; i++, q += 8) {
                out[i] = new InnerClassInfoImpl(r.readEntry(q, ClassEntry.class),
                    Optional.ofNullable(r.readEntryOrNull(q + 2, ClassEntry.class)),
                    Optional.ofNullable(r.readEntryOrNull(q + 4, Utf8Entry.class)), r.readU2(q + 6));
            }
            list = List.of(out);
        }
        return list;
    }

    private static void write(BufWriterImpl out, InnerClassesAttribute a) {
        if (!writeDirect(out, a)) {
            List<InnerClassInfo> list = a.classes();
            out.writeU2(list.size());
            for (InnerClassInfo e : list) {
                out.writeIndex(e.innerClass());
                out.writeIndexOrZero(e.outerClass().orElse(null));
                out.writeIndexOrZero(e.innerName().orElse(null));
                out.writeU2(e.flagsMask());
            }
        }
    }
}
