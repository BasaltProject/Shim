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
import java.lang.classfile.attribute.LineNumberInfo;
import java.lang.classfile.attribute.LineNumberTableAttribute;
import java.util.List;

public final class LineNumberTableAttr extends AttrImpl<LineNumberTableAttribute>
        implements LineNumberTableAttribute {
    public static final Mapper<LineNumberTableAttribute> MAPPER = new Mapper<>("LineNumberTable",
        AttributeStability.LABELS, true,
        (owner, r, p) -> new LineNumberTableAttr(r, p), LineNumberTableAttr::write);

    private List<LineNumberInfo> list;

    public LineNumberTableAttr(List<LineNumberInfo> list) {
        super(MAPPER);
        this.list = List.copyOf(list);
    }

    private LineNumberTableAttr(ClassReaderImpl r, int p) {
        super(MAPPER, r, p);
    }

    @Override
    public List<LineNumberInfo> lineNumbers() {
        if (list == null) {
            ClassReaderImpl r = reader;
            int n = r.readU2(pos);
            LineNumberInfo[] out = new LineNumberInfo[n];
            int q = pos + 2;
            for (int i = 0; i < n; i++, q += 4) {
                out[i] = new LineNumberInfoImpl(r.readU2(q), r.readU2(q + 2));
            }
            list = List.of(out);
        }
        return list;
    }

    private static void write(BufWriterImpl out, LineNumberTableAttribute a) {
        if (!writeDirect(out, a)) {
            List<LineNumberInfo> list = a.lineNumbers();
            out.writeU2(list.size());
            for (LineNumberInfo e : list) {
                out.writeU2(e.startPc());
                out.writeU2(e.lineNumber());
            }
        }
    }
}
