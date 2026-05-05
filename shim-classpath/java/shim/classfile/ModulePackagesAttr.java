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
import java.lang.classfile.attribute.ModulePackagesAttribute;
import java.lang.classfile.constantpool.PackageEntry;
import java.util.List;

public final class ModulePackagesAttr extends AttrImpl<ModulePackagesAttribute> implements ModulePackagesAttribute {
    public static final Mapper<ModulePackagesAttribute> MAPPER = new Mapper<>(
        "ModulePackages", AttributeStability.CP_REFS,
        (owner, r, p) -> new ModulePackagesAttr(r, p), ModulePackagesAttr::write);

    private List<PackageEntry> values;

    public ModulePackagesAttr(List<PackageEntry> values) {
        super(MAPPER);
        this.values = List.copyOf(values);
    }

    private ModulePackagesAttr(ClassReaderImpl r, int p) {
        super(MAPPER, r, p);
    }

    @Override
    public List<PackageEntry> packages() {
        if (values == null) {
            values = AttrSupport.readEntryList(reader, pos, PackageEntry.class);
        }
        return values;
    }

    private static void write(BufWriterImpl out, ModulePackagesAttribute a) {
        if (!writeDirect(out, a)) {
            AttrSupport.writeEntryList(out, a.packages());
        }
    }
}
