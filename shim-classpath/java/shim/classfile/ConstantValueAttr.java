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
import java.lang.classfile.attribute.ConstantValueAttribute;
import java.lang.classfile.constantpool.ConstantValueEntry;
import java.lang.constant.ConstantDesc;
import java.util.Objects;

public final class ConstantValueAttr extends AttrImpl<ConstantValueAttribute> implements ConstantValueAttribute {
    public static final Mapper<ConstantValueAttribute> MAPPER = new Mapper<>("ConstantValue",
        AttributeStability.CP_REFS, (owner, r, p) -> new ConstantValueAttr(r, p), ConstantValueAttr::write);

    private ConstantValueEntry constant;

    public ConstantValueAttr(ConstantValueEntry constant) {
        super(MAPPER);
        this.constant = Objects.requireNonNull(constant);
    }

    private ConstantValueAttr(ClassReaderImpl r, int p) {
        super(MAPPER, r, p);
    }

    public static ConstantValueAttribute of(ConstantDesc value) {
        Objects.requireNonNull(value);
        PoolImpl pool = AttrSupport.pool();
        ConstantValueEntry e;
        if (value instanceof Integer) {
            e = pool.intEntry((Integer) value);
        } else if (value instanceof Long) {
            e = pool.longEntry((Long) value);
        } else if (value instanceof Float) {
            e = pool.floatEntry((Float) value);
        } else if (value instanceof Double) {
            e = pool.doubleEntry((Double) value);
        } else if (value instanceof String) {
            e = pool.stringEntry((String) value);
        } else {
            throw new IllegalArgumentException("Invalid ConstantValueAttribute value: " + value);
        }
        return new ConstantValueAttr(e);
    }

    @Override
    public ConstantValueEntry constant() {
        if (constant == null) {
            constant = reader.readEntry(pos, ConstantValueEntry.class);
        }
        return constant;
    }

    private static void write(BufWriterImpl out, ConstantValueAttribute a) {
        if (!writeDirect(out, a)) {
            out.writeIndex(a.constant());
        }
    }
}
