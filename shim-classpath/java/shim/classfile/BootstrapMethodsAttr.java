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
import java.lang.classfile.BootstrapMethodEntry;
import java.lang.classfile.attribute.BootstrapMethodsAttribute;
import java.lang.classfile.constantpool.LoadableConstantEntry;
import java.util.List;

public final class BootstrapMethodsAttr extends AttrImpl<BootstrapMethodsAttribute>
        implements BootstrapMethodsAttribute {
    public static final Mapper<BootstrapMethodsAttribute> MAPPER = new Mapper<>("BootstrapMethods",
        AttributeStability.CP_REFS, (owner, r, p) -> new BootstrapMethodsAttr(r, p), BootstrapMethodsAttr::write);

    private List<BootstrapMethodEntry> entries;

    private BootstrapMethodsAttr(ClassReaderImpl r, int p) {
        super(MAPPER, r, p);
    }

    @Override
    public int bootstrapMethodsSize() {
        return reader.readU2(pos);
    }

    @Override
    public List<BootstrapMethodEntry> bootstrapMethods() {
        if (entries == null) {
            BootstrapMethodEntry[] out = new BootstrapMethodEntry[bootstrapMethodsSize()];
            for (int i = 0; i < out.length; i++) {
                out[i] = reader.bootstrapMethodEntry(i);
            }
            entries = List.of(out);
        }
        return entries;
    }

    private static void write(BufWriterImpl out, BootstrapMethodsAttribute a) {
        if (writeDirect(out, a)) {
            return;
        }
        List<BootstrapMethodEntry> list = a.bootstrapMethods();
        out.writeU2(list.size());
        for (BootstrapMethodEntry e : list) {
            out.writeIndex(e.bootstrapMethod());
            out.writeU2(e.arguments().size());
            for (LoadableConstantEntry arg : e.arguments()) {
                out.writeIndex(arg);
            }
        }
    }
}
