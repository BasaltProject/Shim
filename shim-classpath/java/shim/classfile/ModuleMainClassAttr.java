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
import java.lang.classfile.attribute.ModuleMainClassAttribute;
import java.lang.classfile.constantpool.ClassEntry;
import java.util.Objects;

public final class ModuleMainClassAttr extends AttrImpl<ModuleMainClassAttribute> implements ModuleMainClassAttribute {
    public static final Mapper<ModuleMainClassAttribute> MAPPER = new Mapper<>(
        "ModuleMainClass", AttributeStability.CP_REFS,
        (owner, r, p) -> new ModuleMainClassAttr(r, p), ModuleMainClassAttr::write);

    private ClassEntry value;

    public ModuleMainClassAttr(ClassEntry value) {
        super(MAPPER);
        this.value = Objects.requireNonNull(value);
    }

    private ModuleMainClassAttr(ClassReaderImpl r, int p) {
        super(MAPPER, r, p);
    }

    @Override
    public ClassEntry mainClass() {
        if (value == null) {
            value = reader.readEntry(pos, ClassEntry.class);
        }
        return value;
    }

    private static void write(BufWriterImpl out, ModuleMainClassAttribute a) {
        if (!writeDirect(out, a)) {
            out.writeIndex(a.mainClass());
        }
    }
}
