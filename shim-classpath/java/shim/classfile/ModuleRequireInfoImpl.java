/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.attribute.ModuleRequireInfo;
import java.lang.classfile.constantpool.ModuleEntry;
import java.lang.classfile.constantpool.Utf8Entry;
import java.util.Objects;
import java.util.Optional;

public final class ModuleRequireInfoImpl implements ModuleRequireInfo {
    private final ModuleEntry requires;
    private final int flags;
    private final Optional<Utf8Entry> version;

    public ModuleRequireInfoImpl(ModuleEntry requires, int flags, Utf8Entry version) {
        this.requires = Objects.requireNonNull(requires);
        this.flags = flags;
        this.version = Optional.ofNullable(version);
    }

    @Override public ModuleEntry requires() { return requires; }
    @Override public int requiresFlagsMask() { return flags; }
    @Override public Optional<Utf8Entry> requiresVersion() { return version; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ModuleRequireInfoImpl)) {
            return false;
        }
        ModuleRequireInfoImpl x = (ModuleRequireInfoImpl) o;
        return requires.equals(x.requires) && flags == x.flags && version.equals(x.version);
    }

    @Override public int hashCode() { return AttrSupport.hash(requires, flags, version); }

    @Override
    public String toString() {
        return "UnboundModuleRequiresInfo[requires=" + requires + ", requiresFlagsMask=" + flags
            + ", requiresVersion=" + version + "]";
    }
}
