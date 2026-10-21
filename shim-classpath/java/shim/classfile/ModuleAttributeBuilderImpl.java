/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.attribute.ModuleAttribute;
import java.lang.classfile.attribute.ModuleExportInfo;
import java.lang.classfile.attribute.ModuleOpenInfo;
import java.lang.classfile.attribute.ModuleProvideInfo;
import java.lang.classfile.attribute.ModuleRequireInfo;
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.classfile.constantpool.ModuleEntry;
import java.lang.classfile.constantpool.Utf8Entry;
import java.lang.constant.ClassDesc;
import java.lang.constant.ModuleDesc;
import java.lang.constant.PackageDesc;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public final class ModuleAttributeBuilderImpl implements ModuleAttribute.ModuleAttributeBuilder {
    private final PoolImpl pool = AttrSupport.pool();
    private ModuleEntry moduleName;
    private int flags;
    private Utf8Entry version;
    private final Set<ModuleRequireInfo> requires = new LinkedHashSet<>();
    private final Set<ModuleExportInfo> exports = new LinkedHashSet<>();
    private final Set<ModuleOpenInfo> opens = new LinkedHashSet<>();
    private final Set<ClassEntry> uses = new LinkedHashSet<>();
    private final Set<ModuleProvideInfo> provides = new LinkedHashSet<>();

    public ModuleAttributeBuilderImpl(ModuleEntry moduleName) {
        this.moduleName = Objects.requireNonNull(moduleName);
    }

    public ModuleAttribute build() {
        return new ModuleAttr(moduleName, flags, version, requires, exports, opens, uses, provides);
    }

    @Override
    public ModuleAttribute.ModuleAttributeBuilder moduleName(ModuleDesc moduleName) {
        this.moduleName = pool.moduleEntry(Objects.requireNonNull(moduleName));
        return this;
    }

    @Override
    public ModuleAttribute.ModuleAttributeBuilder moduleFlags(int flags) {
        this.flags = flags;
        return this;
    }

    @Override
    public ModuleAttribute.ModuleAttributeBuilder moduleVersion(String version) {
        this.version = version == null ? null : pool.utf8Entry(version);
        return this;
    }

    @Override
    public ModuleAttribute.ModuleAttributeBuilder requires(ModuleDesc module, int flags, String version) {
        return requires(ModuleRequireInfo.of(module, flags, version));
    }

    @Override
    public ModuleAttribute.ModuleAttributeBuilder requires(ModuleRequireInfo requires) {
        this.requires.add(Objects.requireNonNull(requires));
        return this;
    }

    @Override
    public ModuleAttribute.ModuleAttributeBuilder exports(PackageDesc pkg, int flags, ModuleDesc... to) {
        return exports(ModuleExportInfo.of(pkg, flags, Arrays.asList(to)));
    }

    @Override
    public ModuleAttribute.ModuleAttributeBuilder exports(ModuleExportInfo exports) {
        this.exports.add(Objects.requireNonNull(exports));
        return this;
    }

    @Override
    public ModuleAttribute.ModuleAttributeBuilder opens(PackageDesc pkg, int flags, ModuleDesc... to) {
        return opens(ModuleOpenInfo.of(pkg, flags, Arrays.asList(to)));
    }

    @Override
    public ModuleAttribute.ModuleAttributeBuilder opens(ModuleOpenInfo opens) {
        this.opens.add(Objects.requireNonNull(opens));
        return this;
    }

    @Override
    public ModuleAttribute.ModuleAttributeBuilder uses(ClassEntry service) {
        this.uses.add(Objects.requireNonNull(service));
        return this;
    }

    @Override
    public ModuleAttribute.ModuleAttributeBuilder uses(ClassDesc service) {
        return uses(pool.classEntry(Objects.requireNonNull(service)));
    }

    @Override
    public ModuleAttribute.ModuleAttributeBuilder provides(ClassDesc service, ClassDesc... implementations) {
        return provides(ModuleProvideInfo.of(service, implementations));
    }

    @Override
    public ModuleAttribute.ModuleAttributeBuilder provides(ModuleProvideInfo provides) {
        this.provides.add(Objects.requireNonNull(provides));
        return this;
    }
}
