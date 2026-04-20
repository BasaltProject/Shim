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

public interface SourceDebugExtensionAttribute extends Attribute<SourceDebugExtensionAttribute>, ClassElement {
    byte[] contents();

    static SourceDebugExtensionAttribute of(byte[] contents) {
        return new shim.classfile.SourceDebugExtensionAttr(contents);
    }
}
