/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile;

import java.lang.classfile.constantpool.ClassEntry;

public interface Superclass extends ClassElement {
    public static Superclass of(ClassEntry classEntry) { return new shim.classfile.Elements.SuperclassImpl(classEntry); }
    ClassEntry superclassEntry();
}
