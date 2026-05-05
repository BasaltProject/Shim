/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.attribute.ModuleOpenInfo;
import java.lang.classfile.constantpool.ModuleEntry;
import java.lang.classfile.constantpool.PackageEntry;
import java.util.List;
import java.util.Objects;

public final class ModuleOpenInfoImpl implements ModuleOpenInfo {
    private final PackageEntry pkg;
    private final int flags;
    private final List<ModuleEntry> targets;

    public ModuleOpenInfoImpl(PackageEntry pkg, int flags, List<ModuleEntry> targets) {
        this.pkg = Objects.requireNonNull(pkg);
        this.flags = flags;
        this.targets = List.copyOf(targets);
    }

    @Override public PackageEntry openedPackage() { return pkg; }
    @Override public int opensFlagsMask() { return flags; }
    @Override public List<ModuleEntry> opensTo() { return targets; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ModuleOpenInfoImpl)) {
            return false;
        }
        ModuleOpenInfoImpl x = (ModuleOpenInfoImpl) o;
        return pkg.equals(x.pkg) && flags == x.flags && targets.equals(x.targets);
    }

    @Override public int hashCode() { return AttrSupport.hash(pkg, flags, targets); }

    @Override
    public String toString() {
        return "UnboundModuleOpenInfo[openedPackage=" + pkg + ", opensFlagsMask=" + flags + ", opensTo="
            + targets + "]";
    }
}
