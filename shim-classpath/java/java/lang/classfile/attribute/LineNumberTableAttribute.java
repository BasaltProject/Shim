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
import java.util.List;

public interface LineNumberTableAttribute extends Attribute<LineNumberTableAttribute> {
    List<LineNumberInfo> lineNumbers();

    static LineNumberTableAttribute of(List<LineNumberInfo> lines) {
        return new shim.classfile.LineNumberTableAttr(lines);
    }
}
