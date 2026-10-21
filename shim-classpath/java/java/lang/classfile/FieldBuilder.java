/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile;

import java.lang.reflect.AccessFlag;

public interface FieldBuilder extends ClassFileBuilder<FieldElement, FieldBuilder> {
    default FieldBuilder withFlags(int i) { return with(new shim.classfile.Elements.AccessFlagsImpl(AccessFlag.Location.FIELD, i)); }
    default FieldBuilder withFlags(AccessFlag... accessFlags) {
        int mask = 0;
        for (AccessFlag f : accessFlags) {
            mask |= f.mask();
        }
        return withFlags(mask);
    }
}
