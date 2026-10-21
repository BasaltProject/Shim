/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile.attribute;

import java.lang.classfile.Attribute;
import java.lang.classfile.ClassElement;

public interface ModuleResolutionAttribute extends Attribute<ModuleResolutionAttribute>, ClassElement {
    int resolutionFlags();

    static ModuleResolutionAttribute of(int resolutionFlags) {
        return new shim.classfile.ModuleResolutionAttr(resolutionFlags);
    }
}
