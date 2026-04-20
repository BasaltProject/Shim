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
import java.lang.classfile.constantpool.Utf8Entry;
import java.lang.constant.ModuleDesc;
import java.lang.reflect.AccessFlag;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface ModuleRequireInfo {
    ModuleEntry requires();

    int requiresFlagsMask();

    default Set<AccessFlag> requiresFlags() {
        return AccessFlag.maskToAccessFlags(requiresFlagsMask(), AccessFlag.Location.MODULE_REQUIRES);
    }

    Optional<Utf8Entry> requiresVersion();

    default boolean has(AccessFlag flag) {
        return shim.classfile.AttrSupport.has(AccessFlag.Location.MODULE_REQUIRES, requiresFlagsMask(), flag);
    }

    static ModuleRequireInfo of(ModuleEntry requires, int requiresFlags, Utf8Entry requiresVersion) {
        return new shim.classfile.ModuleRequireInfoImpl(requires, requiresFlags, requiresVersion);
    }

    static ModuleRequireInfo of(ModuleEntry requires, Collection<AccessFlag> requiresFlags,
                                Utf8Entry requiresVersion) {
        return of(requires, shim.classfile.AttrSupport.mask(AccessFlag.Location.MODULE_REQUIRES, requiresFlags),
            requiresVersion);
    }

    static ModuleRequireInfo of(ModuleDesc requires, int requiresFlags, String requiresVersion) {
        shim.classfile.PoolImpl pool = shim.classfile.AttrSupport.pool();
        return of(pool.moduleEntry(pool.utf8Entry(requires.name())), requiresFlags,
            requiresVersion == null ? null : pool.utf8Entry(requiresVersion));
    }

    static ModuleRequireInfo of(ModuleDesc requires, Collection<AccessFlag> requiresFlags, String requiresVersion) {
        return of(requires, shim.classfile.AttrSupport.mask(AccessFlag.Location.MODULE_REQUIRES, requiresFlags),
            requiresVersion);
    }
}
