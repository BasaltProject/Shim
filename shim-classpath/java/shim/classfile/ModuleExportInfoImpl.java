/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.attribute.ModuleExportInfo;
import java.lang.classfile.constantpool.ModuleEntry;
import java.lang.classfile.constantpool.PackageEntry;
import java.util.List;
import java.util.Objects;

public final class ModuleExportInfoImpl implements ModuleExportInfo {
    private final PackageEntry pkg;
    private final int flags;
    private final List<ModuleEntry> targets;

    public ModuleExportInfoImpl(PackageEntry pkg, int flags, List<ModuleEntry> targets) {
        this.pkg = Objects.requireNonNull(pkg);
        this.flags = flags;
        this.targets = List.copyOf(targets);
    }

    @Override public PackageEntry exportedPackage() { return pkg; }
    @Override public int exportsFlagsMask() { return flags; }
    @Override public List<ModuleEntry> exportsTo() { return targets; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ModuleExportInfoImpl)) {
            return false;
        }
        ModuleExportInfoImpl x = (ModuleExportInfoImpl) o;
        return pkg.equals(x.pkg) && flags == x.flags && targets.equals(x.targets);
    }

    @Override public int hashCode() { return AttrSupport.hash(pkg, flags, targets); }

    @Override
    public String toString() {
        return "UnboundModuleExportInfo[exportedPackage=" + pkg + ", exportsFlagsMask=" + flags + ", exportsTo="
            + targets + "]";
    }
}
