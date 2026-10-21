/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile;

public interface ClassFileVersion extends ClassElement {
    public static ClassFileVersion of(int i, int i2) { return new shim.classfile.Elements.ClassFileVersionImpl(i, i2); }
    int majorVersion();
    int minorVersion();
}
