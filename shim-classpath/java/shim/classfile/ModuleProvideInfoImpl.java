/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.attribute.ModuleProvideInfo;
import java.lang.classfile.constantpool.ClassEntry;
import java.util.List;
import java.util.Objects;

public final class ModuleProvideInfoImpl implements ModuleProvideInfo {
    private final ClassEntry provides;
    private final List<ClassEntry> providesWith;

    public ModuleProvideInfoImpl(ClassEntry provides, List<ClassEntry> providesWith) {
        this.provides = Objects.requireNonNull(provides);
        this.providesWith = List.copyOf(providesWith);
    }

    @Override public ClassEntry provides() { return provides; }
    @Override public List<ClassEntry> providesWith() { return providesWith; }

    @Override
    public boolean equals(Object o) {
        return o instanceof ModuleProvideInfoImpl && ((ModuleProvideInfoImpl) o).provides.equals(provides)
            && ((ModuleProvideInfoImpl) o).providesWith.equals(providesWith);
    }

    @Override public int hashCode() { return provides.hashCode() * 31 + providesWith.hashCode(); }

    @Override
    public String toString() {
        return "UnboundModuleProvideInfo[provides=" + provides + ", providesWith=" + providesWith + "]";
    }
}
