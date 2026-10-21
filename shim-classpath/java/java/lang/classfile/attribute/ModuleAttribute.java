/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile.attribute;

import java.lang.classfile.Attribute;
import java.lang.classfile.ClassElement;
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.classfile.constantpool.ModuleEntry;
import java.lang.classfile.constantpool.Utf8Entry;
import java.lang.constant.ClassDesc;
import java.lang.constant.ModuleDesc;
import java.lang.constant.PackageDesc;
import java.lang.reflect.AccessFlag;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public interface ModuleAttribute extends Attribute<ModuleAttribute>, ClassElement {
    ModuleEntry moduleName();

    int moduleFlagsMask();

    default Set<AccessFlag> moduleFlags() {
        return AccessFlag.maskToAccessFlags(moduleFlagsMask(), AccessFlag.Location.MODULE);
    }

    default boolean has(AccessFlag flag) {
        return shim.classfile.AttrSupport.has(AccessFlag.Location.MODULE, moduleFlagsMask(), flag);
    }

    Optional<Utf8Entry> moduleVersion();

    List<ModuleRequireInfo> requires();

    List<ModuleExportInfo> exports();

    List<ModuleOpenInfo> opens();

    List<ClassEntry> uses();

    List<ModuleProvideInfo> provides();

    static ModuleAttribute of(ModuleEntry moduleName, int moduleFlags, Utf8Entry moduleVersion,
                              Collection<ModuleRequireInfo> requires, Collection<ModuleExportInfo> exports,
                              Collection<ModuleOpenInfo> opens, Collection<ClassEntry> uses,
                              Collection<ModuleProvideInfo> provides) {
        return new shim.classfile.ModuleAttr(moduleName, moduleFlags, moduleVersion, requires, exports, opens, uses,
            provides);
    }

    static ModuleAttribute of(ModuleDesc moduleName, Consumer<ModuleAttributeBuilder> attrHandler) {
        return of(shim.classfile.AttrSupport.pool().moduleEntry(moduleName), attrHandler);
    }

    static ModuleAttribute of(ModuleEntry moduleName, Consumer<ModuleAttributeBuilder> attrHandler) {
        Objects.requireNonNull(attrHandler);
        shim.classfile.ModuleAttributeBuilderImpl b = new shim.classfile.ModuleAttributeBuilderImpl(moduleName);
        attrHandler.accept(b);
        return b.build();
    }

    interface ModuleAttributeBuilder {
        ModuleAttributeBuilder moduleName(ModuleDesc moduleName);

        ModuleAttributeBuilder moduleFlags(int flagsMask);

        default ModuleAttributeBuilder moduleFlags(AccessFlag... moduleFlags) {
            return moduleFlags(shim.classfile.AttrSupport.mask(AccessFlag.Location.MODULE, moduleFlags));
        }

        ModuleAttributeBuilder moduleVersion(String version);

        ModuleAttributeBuilder requires(ModuleDesc module, int requiresFlagsMask, String version);

        default ModuleAttributeBuilder requires(ModuleDesc module, Collection<AccessFlag> requiresFlags,
                                                String version) {
            return requires(module, shim.classfile.AttrSupport.mask(AccessFlag.Location.MODULE_REQUIRES,
                requiresFlags), version);
        }

        ModuleAttributeBuilder requires(ModuleRequireInfo requires);

        ModuleAttributeBuilder exports(PackageDesc pkg, int exportsFlagsMask, ModuleDesc... exportsToModules);

        default ModuleAttributeBuilder exports(PackageDesc pkg, Collection<AccessFlag> exportsFlags,
                                               ModuleDesc... exportsToModules) {
            return exports(pkg, shim.classfile.AttrSupport.mask(AccessFlag.Location.MODULE_EXPORTS, exportsFlags),
                exportsToModules);
        }

        ModuleAttributeBuilder exports(ModuleExportInfo exports);

        ModuleAttributeBuilder opens(PackageDesc pkg, int opensFlagsMask, ModuleDesc... opensToModules);

        default ModuleAttributeBuilder opens(PackageDesc pkg, Collection<AccessFlag> opensFlags,
                                             ModuleDesc... opensToModules) {
            return opens(pkg, shim.classfile.AttrSupport.mask(AccessFlag.Location.MODULE_OPENS, opensFlags),
                opensToModules);
        }

        ModuleAttributeBuilder opens(ModuleOpenInfo opens);

        ModuleAttributeBuilder uses(ClassEntry service);

        ModuleAttributeBuilder uses(ClassDesc service);

        ModuleAttributeBuilder provides(ClassDesc service, ClassDesc... implClasses);

        ModuleAttributeBuilder provides(ModuleProvideInfo provides);
    }
}
