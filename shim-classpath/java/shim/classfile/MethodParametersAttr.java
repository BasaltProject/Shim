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
import java.lang.classfile.attribute.MethodParameterInfo;
import java.lang.classfile.attribute.MethodParametersAttribute;
import java.lang.classfile.constantpool.Utf8Entry;
import java.util.List;
import java.util.Optional;

public final class MethodParametersAttr extends AttrImpl<MethodParametersAttribute>
        implements MethodParametersAttribute {
    public static final Mapper<MethodParametersAttribute> MAPPER = new Mapper<>("MethodParameters",
        AttributeStability.CP_REFS, false,
        (owner, r, p) -> new MethodParametersAttr(r, p), MethodParametersAttr::write);

    private List<MethodParameterInfo> list;

    public MethodParametersAttr(List<MethodParameterInfo> list) {
        super(MAPPER);
        this.list = List.copyOf(list);
    }

    private MethodParametersAttr(ClassReaderImpl r, int p) {
        super(MAPPER, r, p);
    }

    @Override
    public List<MethodParameterInfo> parameters() {
        if (list == null) {
            ClassReaderImpl r = reader;
            int n = r.readU1(pos);
            MethodParameterInfo[] out = new MethodParameterInfo[n];
            int q = pos + 1;
            for (int i = 0; i < n; i++, q += 4) {
                out[i] = new MethodParameterInfoImpl(Optional.ofNullable(r.readEntryOrNull(q, Utf8Entry.class)),
                    r.readU2(q + 2));
            }
            list = List.of(out);
        }
        return list;
    }

    private static void write(BufWriterImpl out, MethodParametersAttribute a) {
        if (!writeDirect(out, a)) {
            List<MethodParameterInfo> list = a.parameters();
            out.writeU1(list.size());
            for (MethodParameterInfo e : list) {
                out.writeIndexOrZero(e.name().orElse(null));
                out.writeU2(e.flagsMask());
            }
        }
    }
}
