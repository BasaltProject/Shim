/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.attribute.CharacterRangeInfo;

public final class CharacterRangeInfoImpl implements CharacterRangeInfo {
    private final int startPc;
    private final int endPc;
    private final int characterRangeStart;
    private final int characterRangeEnd;
    private final int flags;

    public CharacterRangeInfoImpl(int startPc, int endPc, int characterRangeStart, int characterRangeEnd, int flags) {
        this.startPc = startPc;
        this.endPc = endPc;
        this.characterRangeStart = characterRangeStart;
        this.characterRangeEnd = characterRangeEnd;
        this.flags = flags;
    }

    @Override public int startPc() { return startPc; }
    @Override public int endPc() { return endPc; }
    @Override public int characterRangeStart() { return characterRangeStart; }
    @Override public int characterRangeEnd() { return characterRangeEnd; }
    @Override public int flags() { return flags; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CharacterRangeInfoImpl)) {
            return false;
        }
        CharacterRangeInfoImpl x = (CharacterRangeInfoImpl) o;
        return startPc == x.startPc && endPc == x.endPc && characterRangeStart == x.characterRangeStart
            && characterRangeEnd == x.characterRangeEnd && flags == x.flags;
    }

    @Override
    public int hashCode() {
        return AttrSupport.hash(startPc, endPc, characterRangeStart, characterRangeEnd, flags);
    }

    @Override
    public String toString() {
        return "UnboundCharacterRangeInfo[startPc=" + startPc + ", endPc=" + endPc + ", characterRangeStart="
            + characterRangeStart + ", characterRangeEnd=" + characterRangeEnd + ", flags=" + flags + "]";
    }
}
