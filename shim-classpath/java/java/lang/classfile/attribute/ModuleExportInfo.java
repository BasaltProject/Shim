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
import java.lang.classfile.constantpool.PackageEntry;
import java.lang.constant.ModuleDesc;
import java.lang.constant.PackageDesc;
import java.lang.reflect.AccessFlag;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface ModuleExportInfo {
    PackageEntry exportedPackage();

    int exportsFlagsMask();

    default Set<AccessFlag> exportsFlags() {
        return AccessFlag.maskToAccessFlags(exportsFlagsMask(), AccessFlag.Location.MODULE_EXPORTS);
    }

    List<ModuleEntry> exportsTo();

    default boolean has(AccessFlag flag) {
        return shim.classfile.AttrSupport.has(AccessFlag.Location.MODULE_EXPORTS, exportsFlagsMask(), flag);
    }

    static ModuleExportInfo of(PackageEntry pkg, int flags, List<ModuleEntry> to) {
        return new shim.classfile.ModuleExportInfoImpl(pkg, flags, to);
    }

    static ModuleExportInfo of(PackageEntry pkg, Collection<AccessFlag> flags, List<ModuleEntry> to) {
        return of(pkg, shim.classfile.AttrSupport.mask(AccessFlag.Location.MODULE_EXPORTS, flags), to);
    }

    static ModuleExportInfo of(PackageEntry pkg, int flags, ModuleEntry... to) {
        return of(pkg, flags, Arrays.asList(to));
    }

    static ModuleExportInfo of(PackageEntry pkg, Collection<AccessFlag> flags, ModuleEntry... to) {
        return of(pkg, shim.classfile.AttrSupport.mask(AccessFlag.Location.MODULE_EXPORTS, flags), to);
    }

    static ModuleExportInfo of(PackageDesc pkg, int flags, List<ModuleDesc> to) {
        shim.classfile.PoolImpl pool = shim.classfile.AttrSupport.pool();
        return of(pool.packageEntry(pkg), flags, shim.classfile.AttrSupport.moduleEntries(pool, to));
    }

    static ModuleExportInfo of(PackageDesc pkg, Collection<AccessFlag> flags, List<ModuleDesc> to) {
        return of(pkg, shim.classfile.AttrSupport.mask(AccessFlag.Location.MODULE_EXPORTS, flags), to);
    }

    static ModuleExportInfo of(PackageDesc pkg, int flags, ModuleDesc... to) {
        return of(pkg, flags, Arrays.asList(to));
    }

    static ModuleExportInfo of(PackageDesc pkg, Collection<AccessFlag> flags, ModuleDesc... to) {
        return of(pkg, shim.classfile.AttrSupport.mask(AccessFlag.Location.MODULE_EXPORTS, flags), to);
    }
}
