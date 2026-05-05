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
import java.lang.classfile.attribute.ModuleHashInfo;
import java.lang.classfile.attribute.ModuleHashesAttribute;
import java.lang.classfile.constantpool.ModuleEntry;
import java.lang.classfile.constantpool.Utf8Entry;
import java.util.List;
import java.util.Objects;

public final class ModuleHashesAttr extends AttrImpl<ModuleHashesAttribute> implements ModuleHashesAttribute {
    public static final Mapper<ModuleHashesAttribute> MAPPER = new Mapper<>("ModuleHashes",
        AttributeStability.CP_REFS, (owner, r, p) -> new ModuleHashesAttr(r, p), ModuleHashesAttr::write);

    private Utf8Entry algorithm;
    private List<ModuleHashInfo> hashes;

    public ModuleHashesAttr(Utf8Entry algorithm, List<ModuleHashInfo> hashes) {
        super(MAPPER);
        this.algorithm = Objects.requireNonNull(algorithm);
        this.hashes = List.copyOf(hashes);
    }

    private ModuleHashesAttr(ClassReaderImpl r, int p) {
        super(MAPPER, r, p);
    }

    @Override
    public Utf8Entry algorithm() {
        if (algorithm == null) {
            algorithm = reader.readEntry(pos, Utf8Entry.class);
        }
        return algorithm;
    }

    @Override
    public List<ModuleHashInfo> hashes() {
        if (hashes == null) {
            int n = reader.readU2(pos + 2);
            ModuleHashInfo[] out = new ModuleHashInfo[n];
            int q = pos + 4;
            for (int i = 0; i < n; i++) {
                int len = reader.readU2(q + 2);
                out[i] = new ModuleHashInfoImpl(reader.readEntry(q, ModuleEntry.class), reader.readBytes(q + 4, len));
                q += 4 + len;
            }
            hashes = List.of(out);
        }
        return hashes;
    }

    private static void write(BufWriterImpl out, ModuleHashesAttribute a) {
        if (writeDirect(out, a)) {
            return;
        }
        out.writeIndex(a.algorithm());
        List<ModuleHashInfo> list = a.hashes();
        out.writeU2(list.size());
        for (ModuleHashInfo h : list) {
            out.writeIndex(h.moduleName());
            out.writeU2(h.hash().length);
            out.writeBytes(h.hash());
        }
    }
}
