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
import java.lang.classfile.attribute.RecordAttribute;
import java.lang.classfile.attribute.RecordComponentInfo;
import java.util.List;

public final class RecordAttr extends AttrImpl<RecordAttribute> implements RecordAttribute {
    public static final Mapper<RecordAttribute> MAPPER = new Mapper<>("Record", AttributeStability.CP_REFS,
        (owner, r, p) -> new RecordAttr(r, p), RecordAttr::write);

    private List<RecordComponentInfo> components;

    public RecordAttr(List<RecordComponentInfo> components) {
        super(MAPPER);
        this.components = List.copyOf(components);
    }

    private RecordAttr(ClassReaderImpl r, int p) {
        super(MAPPER, r, p);
    }

    @Override
    public List<RecordComponentInfo> components() {
        if (components == null) {
            int n = reader.readU2(pos);
            RecordComponentInfo[] out = new RecordComponentInfo[n];
            int q = pos + 2;
            for (int i = 0; i < n; i++) {
                BoundRecordComponentInfo c = new BoundRecordComponentInfo(reader, q);
                out[i] = c;
                q = c.end();
            }
            components = List.of(out);
        }
        return components;
    }

    private static void write(BufWriterImpl out, RecordAttribute a) {
        if (writeDirect(out, a)) {
            return;
        }
        List<RecordComponentInfo> list = a.components();
        out.writeU2(list.size());
        for (RecordComponentInfo c : list) {
            out.writeIndex(c.name());
            out.writeIndex(c.descriptor());
            AttrSupport.writeAttributes(out, c.attributes());
        }
    }
}
