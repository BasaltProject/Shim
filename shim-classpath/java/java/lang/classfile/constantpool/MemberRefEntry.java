/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile.constantpool;

public interface MemberRefEntry extends PoolEntry {
    default Utf8Entry name() { return nameAndType().name(); }
    default Utf8Entry type() { return nameAndType().type(); }
    ClassEntry owner();
    NameAndTypeEntry nameAndType();
}
