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
import java.lang.classfile.AttributedElement;
import java.lang.classfile.attribute.CodeAttribute;
import java.lang.classfile.attribute.LocalVariableInfo;
import java.lang.classfile.attribute.LocalVariableTableAttribute;
import java.util.List;

public final class LocalVariableTableAttr extends AttrImpl<LocalVariableTableAttribute>
        implements LocalVariableTableAttribute {
    public static final Mapper<LocalVariableTableAttribute> MAPPER = new Mapper<>("LocalVariableTable",
        AttributeStability.LABELS, true,
        (owner, r, p) -> read(owner, r, p), LocalVariableTableAttr::write);

    private LabelContext labels;
    private List<LocalVariableInfo> list;

    public LocalVariableTableAttr(List<LocalVariableInfo> list) {
        super(MAPPER);
        this.list = List.copyOf(list);
    }

    private LocalVariableTableAttr(ClassReaderImpl r, int p) {
        super(MAPPER, r, p);
    }

    private static LocalVariableTableAttr read(AttributedElement owner, ClassReaderImpl r, int p) {
        if (!(owner instanceof CodeAttribute)) {
            throw new IllegalArgumentException("Invalid LocalVariableTable attribute location");
        }
        LocalVariableTableAttr a = new LocalVariableTableAttr(r, p);
        a.labels = owner instanceof LabelContext ? (LabelContext) owner : null;
        return a;
    }

    @Override
    public List<LocalVariableInfo> localVariables() {
        if (list == null) {
            ClassReaderImpl r = reader;
            int n = r.readU2(pos);
            LocalVariableInfo[] out = new LocalVariableInfo[n];
            int q = pos + 2;
            for (int i = 0; i < n; i++, q += 10) {
                out[i] = LocalVariableInfoImpl.read(r, q, labels);
            }
            list = List.of(out);
        }
        return list;
    }

    private static void write(BufWriterImpl out, LocalVariableTableAttribute a) {
        if (!writeDirect(out, a)) {
            List<LocalVariableInfo> list = a.localVariables();
            out.writeU2(list.size());
            for (LocalVariableInfo e : list) {
                out.writeU2(e.startPc());
                out.writeU2(e.length());
                out.writeIndex(e.name());
                out.writeIndex(e.type());
                out.writeU2(e.slot());
            }
        }
    }
}
