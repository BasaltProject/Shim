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
import java.lang.classfile.attribute.SyntheticAttribute;

public final class SyntheticAttr extends AttrImpl<SyntheticAttribute> implements SyntheticAttribute {
    public static final Mapper<SyntheticAttribute> MAPPER = new Mapper<>(
        "Synthetic", AttributeStability.STATELESS, true,
        (owner, r, p) -> new SyntheticAttr(r, p), (out, a) -> { });

    public SyntheticAttr() {
        super(MAPPER);
    }

    private SyntheticAttr(ClassReaderImpl r, int p) {
        super(MAPPER, r, p);
    }
}
