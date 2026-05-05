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
import java.lang.classfile.attribute.NestHostAttribute;
import java.lang.classfile.constantpool.ClassEntry;
import java.util.Objects;

public final class NestHostAttr extends AttrImpl<NestHostAttribute> implements NestHostAttribute {
    public static final Mapper<NestHostAttribute> MAPPER = new Mapper<>("NestHost", AttributeStability.CP_REFS,
        (owner, r, p) -> new NestHostAttr(r, p), NestHostAttr::write);

    private ClassEntry value;

    public NestHostAttr(ClassEntry value) {
        super(MAPPER);
        this.value = Objects.requireNonNull(value);
    }

    private NestHostAttr(ClassReaderImpl r, int p) {
        super(MAPPER, r, p);
    }

    @Override
    public ClassEntry nestHost() {
        if (value == null) {
            value = reader.readEntry(pos, ClassEntry.class);
        }
        return value;
    }

    private static void write(BufWriterImpl out, NestHostAttribute a) {
        if (!writeDirect(out, a)) {
            out.writeIndex(a.nestHost());
        }
    }
}
