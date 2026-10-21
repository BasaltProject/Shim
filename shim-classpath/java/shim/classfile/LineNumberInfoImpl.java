/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.attribute.LineNumberInfo;

public final class LineNumberInfoImpl implements LineNumberInfo {
    private final int startPc;
    private final int lineNumber;

    public LineNumberInfoImpl(int startPc, int lineNumber) {
        this.startPc = startPc;
        this.lineNumber = lineNumber;
    }

    @Override public int startPc() { return startPc; }
    @Override public int lineNumber() { return lineNumber; }

    @Override
    public boolean equals(Object o) {
        return o instanceof LineNumberInfoImpl && ((LineNumberInfoImpl) o).startPc == startPc
            && ((LineNumberInfoImpl) o).lineNumber == lineNumber;
    }

    @Override public int hashCode() { return startPc * 31 + lineNumber; }

    @Override
    public String toString() {
        return "UnboundLineNumberInfo[startPc=" + startPc + ", lineNumber=" + lineNumber + "]";
    }
}
