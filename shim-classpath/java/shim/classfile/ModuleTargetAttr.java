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
import java.lang.classfile.attribute.ModuleTargetAttribute;
import java.lang.classfile.constantpool.Utf8Entry;
import java.util.Objects;

public final class ModuleTargetAttr extends AttrImpl<ModuleTargetAttribute> implements ModuleTargetAttribute {
    public static final Mapper<ModuleTargetAttribute> MAPPER = new Mapper<>("ModuleTarget", AttributeStability.CP_REFS,
        (owner, r, p) -> new ModuleTargetAttr(r, p), ModuleTargetAttr::write);

    private Utf8Entry value;

    public ModuleTargetAttr(Utf8Entry value) {
        super(MAPPER);
        this.value = Objects.requireNonNull(value);
    }

    private ModuleTargetAttr(ClassReaderImpl r, int p) {
        super(MAPPER, r, p);
    }

    @Override
    public Utf8Entry targetPlatform() {
        if (value == null) {
            value = reader.readEntry(pos, Utf8Entry.class);
        }
        return value;
    }

    private static void write(BufWriterImpl out, ModuleTargetAttribute a) {
        if (!writeDirect(out, a)) {
            out.writeIndex(a.targetPlatform());
        }
    }
}
