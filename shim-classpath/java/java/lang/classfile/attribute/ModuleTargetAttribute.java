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
import java.lang.classfile.constantpool.Utf8Entry;

public interface ModuleTargetAttribute extends Attribute<ModuleTargetAttribute>, ClassElement {
    Utf8Entry targetPlatform();

    static ModuleTargetAttribute of(String text) {
        return of(shim.classfile.AttrSupport.pool().utf8Entry(text));
    }

    static ModuleTargetAttribute of(Utf8Entry value) {
        return new shim.classfile.ModuleTargetAttr(value);
    }
}
