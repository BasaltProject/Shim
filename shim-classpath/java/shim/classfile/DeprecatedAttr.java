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
import java.lang.classfile.attribute.DeprecatedAttribute;

public final class DeprecatedAttr extends AttrImpl<DeprecatedAttribute> implements DeprecatedAttribute {
    public static final Mapper<DeprecatedAttribute> MAPPER = new Mapper<>(
        "Deprecated", AttributeStability.STATELESS, true,
        (owner, r, p) -> new DeprecatedAttr(r, p), (out, a) -> { });

    public DeprecatedAttr() {
        super(MAPPER);
    }

    private DeprecatedAttr(ClassReaderImpl r, int p) {
        super(MAPPER, r, p);
    }
}
