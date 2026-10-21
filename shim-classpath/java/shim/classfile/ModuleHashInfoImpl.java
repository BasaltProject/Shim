/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.attribute.ModuleHashInfo;
import java.lang.classfile.constantpool.ModuleEntry;
import java.util.Objects;

public final class ModuleHashInfoImpl implements ModuleHashInfo {
    private final ModuleEntry moduleName;
    private final byte[] hash;

    public ModuleHashInfoImpl(ModuleEntry moduleName, byte[] hash) {
        this.moduleName = Objects.requireNonNull(moduleName);
        this.hash = Objects.requireNonNull(hash);
    }

    @Override public ModuleEntry moduleName() { return moduleName; }
    @Override public byte[] hash() { return hash; }

    @Override
    public boolean equals(Object o) {
        return o instanceof ModuleHashInfoImpl && ((ModuleHashInfoImpl) o).moduleName.equals(moduleName)
            && ((ModuleHashInfoImpl) o).hash == hash;
    }

    @Override public int hashCode() { return moduleName.hashCode() * 31 + System.identityHashCode(hash); }

    @Override
    public String toString() {
        return "UnboundModuleHashInfo[moduleName=" + moduleName + ", hash=" + hash + "]";
    }
}
