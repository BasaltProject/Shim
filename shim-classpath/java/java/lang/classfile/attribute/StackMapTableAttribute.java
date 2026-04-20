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
import java.lang.classfile.CodeElement;
import java.util.List;
import shim.classfile.StackMapTableAttr;

public interface StackMapTableAttribute extends Attribute<StackMapTableAttribute>, CodeElement {
    public static StackMapTableAttribute of(List<StackMapFrameInfo> list) {
        return new StackMapTableAttr(list);
    }

    List<StackMapFrameInfo> entries();
}
