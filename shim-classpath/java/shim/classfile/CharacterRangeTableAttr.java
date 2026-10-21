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
import java.lang.classfile.attribute.CharacterRangeInfo;
import java.lang.classfile.attribute.CharacterRangeTableAttribute;
import java.util.List;

public final class CharacterRangeTableAttr extends AttrImpl<CharacterRangeTableAttribute>
        implements CharacterRangeTableAttribute {
    public static final Mapper<CharacterRangeTableAttribute> MAPPER = new Mapper<>("CharacterRangeTable",
        AttributeStability.LABELS, true,
        (owner, r, p) -> new CharacterRangeTableAttr(r, p), CharacterRangeTableAttr::write);

    private List<CharacterRangeInfo> list;

    public CharacterRangeTableAttr(List<CharacterRangeInfo> list) {
        super(MAPPER);
        this.list = List.copyOf(list);
    }

    private CharacterRangeTableAttr(ClassReaderImpl r, int p) {
        super(MAPPER, r, p);
    }

    @Override
    public List<CharacterRangeInfo> characterRangeTable() {
        if (list == null) {
            ClassReaderImpl r = reader;
            int n = r.readU2(pos);
            CharacterRangeInfo[] out = new CharacterRangeInfo[n];
            int q = pos + 2;
            for (int i = 0; i < n; i++, q += 14) {
                out[i] = new CharacterRangeInfoImpl(r.readU2(q), r.readU2(q + 2), r.readInt(q + 4), r.readInt(q + 8),
                    r.readU2(q + 12));
            }
            list = List.of(out);
        }
        return list;
    }

    private static void write(BufWriterImpl out, CharacterRangeTableAttribute a) {
        if (!writeDirect(out, a)) {
            List<CharacterRangeInfo> list = a.characterRangeTable();
            out.writeU2(list.size());
            for (CharacterRangeInfo e : list) {
                out.writeU2(e.startPc());
                out.writeU2(e.endPc());
                out.writeInt(e.characterRangeStart());
                out.writeInt(e.characterRangeEnd());
                out.writeU2(e.flags());
            }
        }
    }
}
