/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile.attribute;



public interface LineNumberInfo {
    int startPc();

    int lineNumber();

    static LineNumberInfo of(int startPc, int lineNumber) {
        return new shim.classfile.LineNumberInfoImpl(startPc, lineNumber);
    }
}
