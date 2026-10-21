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

public interface CharacterRangeTableAttribute extends Attribute<CharacterRangeTableAttribute> {
    List<CharacterRangeInfo> characterRangeTable();

    static CharacterRangeTableAttribute of(List<CharacterRangeInfo> ranges) {
        return new shim.classfile.CharacterRangeTableAttr(ranges);
    }
}
