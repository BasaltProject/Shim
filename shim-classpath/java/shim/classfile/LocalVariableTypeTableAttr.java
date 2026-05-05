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
import java.lang.classfile.attribute.LocalVariableTypeInfo;
import java.lang.classfile.attribute.LocalVariableTypeTableAttribute;
import java.util.List;

public final class LocalVariableTypeTableAttr extends AttrImpl<LocalVariableTypeTableAttribute>
        implements LocalVariableTypeTableAttribute {
    public static final Mapper<LocalVariableTypeTableAttribute> MAPPER = new Mapper<>("LocalVariableTypeTable",
        AttributeStability.LABELS, true,
        (owner, r, p) -> read(owner, r, p), LocalVariableTypeTableAttr::write);

    private LabelContext labels;
    private List<LocalVariableTypeInfo> list;

    public LocalVariableTypeTableAttr(List<LocalVariableTypeInfo> list) {
        super(MAPPER);
        this.list = List.copyOf(list);
    }

    private LocalVariableTypeTableAttr(ClassReaderImpl r, int p) {
        super(MAPPER, r, p);
    }

    private static LocalVariableTypeTableAttr read(AttributedElement owner, ClassReaderImpl r, int p) {
        if (!(owner instanceof CodeAttribute)) {
            throw new IllegalArgumentException("Invalid LocalVariableTypeTable attribute location");
        }
        LocalVariableTypeTableAttr a = new LocalVariableTypeTableAttr(r, p);
        a.labels = owner instanceof LabelContext ? (LabelContext) owner : null;
        return a;
    }

    @Override
    public List<LocalVariableTypeInfo> localVariableTypes() {
        if (list == null) {
            ClassReaderImpl r = reader;
            int n = r.readU2(pos);
            LocalVariableTypeInfo[] out = new LocalVariableTypeInfo[n];
            int q = pos + 2;
            for (int i = 0; i < n; i++, q += 10) {
                out[i] = LocalVariableTypeInfoImpl.read(r, q, labels);
            }
            list = List.of(out);
        }
        return list;
    }

    private static void write(BufWriterImpl out, LocalVariableTypeTableAttribute a) {
        if (!writeDirect(out, a)) {
            List<LocalVariableTypeInfo> list = a.localVariableTypes();
            out.writeU2(list.size());
            for (LocalVariableTypeInfo e : list) {
                out.writeU2(e.startPc());
                out.writeU2(e.length());
                out.writeIndex(e.name());
                out.writeIndex(e.signature());
                out.writeU2(e.slot());
            }
        }
    }
}
