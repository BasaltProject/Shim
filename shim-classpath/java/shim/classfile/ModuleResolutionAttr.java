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
import java.lang.classfile.attribute.ModuleResolutionAttribute;

public final class ModuleResolutionAttr extends AttrImpl<ModuleResolutionAttribute>
        implements ModuleResolutionAttribute {
    public static final Mapper<ModuleResolutionAttribute> MAPPER = new Mapper<>("ModuleResolution",
        AttributeStability.STATELESS, (owner, r, p) -> new ModuleResolutionAttr(r, p), ModuleResolutionAttr::write);

    private final int flags;

    public ModuleResolutionAttr(int flags) {
        super(MAPPER);
        this.flags = flags;
    }

    private ModuleResolutionAttr(ClassReaderImpl r, int p) {
        super(MAPPER, r, p);
        this.flags = -1;
    }

    @Override
    public int resolutionFlags() {
        return reader == null ? flags : reader.readU2(pos);
    }

    private static void write(BufWriterImpl out, ModuleResolutionAttribute a) {
        if (!writeDirect(out, a)) {
            out.writeU2(a.resolutionFlags());
        }
    }
}
