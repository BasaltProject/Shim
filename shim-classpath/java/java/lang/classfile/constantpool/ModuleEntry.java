/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile.constantpool;

import java.lang.constant.ModuleDesc;

public interface ModuleEntry extends PoolEntry {
    Utf8Entry name();
    boolean matches(ModuleDesc moduleDesc);
    ModuleDesc asSymbol();
}
