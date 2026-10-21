/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile.attribute;

import java.lang.classfile.constantpool.ModuleEntry;
import java.lang.constant.ModuleDesc;

public interface ModuleHashInfo {
    ModuleEntry moduleName();

    byte[] hash();

    static ModuleHashInfo of(ModuleEntry moduleName, byte[] hash) {
        return new shim.classfile.ModuleHashInfoImpl(moduleName, hash);
    }

    static ModuleHashInfo of(ModuleDesc moduleDesc, byte[] hash) {
        shim.classfile.PoolImpl pool = shim.classfile.AttrSupport.pool();
        return of(pool.moduleEntry(pool.utf8Entry(moduleDesc.name())), hash);
    }
}
