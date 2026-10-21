/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile.constantpool;

import java.lang.classfile.BootstrapMethodEntry;

public interface DynamicConstantPoolEntry extends PoolEntry {
    default Utf8Entry name() { return nameAndType().name(); }
    default Utf8Entry type() { return nameAndType().type(); }
    BootstrapMethodEntry bootstrap();
    NameAndTypeEntry nameAndType();
    int bootstrapMethodIndex();
}
