/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile.attribute;



public interface CharacterRangeInfo {
    int startPc();

    int endPc();

    int characterRangeStart();

    int characterRangeEnd();

    int flags();

    static CharacterRangeInfo of(int startPc, int endPc, int characterRangeStart, int characterRangeEnd, int flags) {
        return new shim.classfile.CharacterRangeInfoImpl(startPc, endPc, characterRangeStart, characterRangeEnd, flags);
    }
}
