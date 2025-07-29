/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 17

import java.lang.module.ModuleDescriptor;
import java.util.Collections;

public class ModuleSystemReflection {
    public static void main(String[] args) {
        testClassGetModule();
        testClassLoaderModules();
        testModuleLayer();
        testModuleDescriptor();
        System.out.println("ALL MODULE SYSTEM TESTS PASSED");
    }

    private static void testClassGetModule() {
        Module appMod = ModuleSystemReflection.class.getModule();
        System.out.println("appMod named: " + appMod.isNamed());
        System.out.println("appMod name: " + appMod.getName());
        System.out.println("appMod desc: " + appMod.getDescriptor());
        System.out.println("appMod layer: " + appMod.getLayer());
        System.out.println("appMod loader match: " + (appMod.getClassLoader() == ModuleSystemReflection.class.getClassLoader()));
        System.out.println("appMod canRead: " + appMod.canRead(String.class.getModule()));
        System.out.println("appMod isExported: " + appMod.isExported("test"));
        System.out.println("appMod isOpen: " + appMod.isOpen("test"));
        System.out.println("appMod canUse: " + appMod.canUse(Runnable.class));

        Module base = String.class.getModule();
        System.out.println("base named: " + base.isNamed());
        System.out.println("base name: " + base.getName());
        System.out.println("base desc name: " + base.getDescriptor().name());
        System.out.println("base layer is boot: " + (base.getLayer() == ModuleLayer.boot()));
        System.out.println("base loader null: " + (base.getClassLoader() == null));
        System.out.println("base has java.lang: " + base.getPackages().contains("java.lang"));

        System.out.println("int is base: " + (int.class.getModule() == base));
        System.out.println("Object[] is base: " + (Object[].class.getModule() == base));
        System.out.println("int[] is base: " + (int[].class.getModule() == base));
        System.out.println("appMod array is appMod: " + (ModuleSystemReflection[].class.getModule() == appMod));
    }

    private static void testClassLoaderModules() {
        System.out.println("platform loader present: " + (ClassLoader.getPlatformClassLoader() != null));
        System.out.println("app unnamed is appMod: " + (ClassLoader.getSystemClassLoader().getUnnamedModule() == ModuleSystemReflection.class.getModule()));
    }

    private static void testModuleLayer() {
        ModuleLayer boot = ModuleLayer.boot();
        System.out.println("boot find java.base: " + (boot.findModule("java.base").get() == String.class.getModule()));
        System.out.println("boot contains java.base: " + boot.modules().contains(String.class.getModule()));
        System.out.println("boot findLoader null: " + (boot.findLoader("java.base") == null));

        ModuleLayer empty = ModuleLayer.empty();
        System.out.println("empty modules count: " + empty.modules().size());
        System.out.println("empty findModule: " + empty.findModule("java.base").isPresent());
    }

    private static void testModuleDescriptor() {
        ModuleDescriptor desc = ModuleDescriptor.newModule("custom.foo")
                .version("1.2.3")
                .mainClass("custom.foo.Main")
                .packages(Collections.singleton("custom.foo"))
                .requires("java.base")
                .exports("custom.foo")
                .build();

        System.out.println("desc name: " + desc.name());
        System.out.println("desc open: " + desc.isOpen());
        System.out.println("desc automatic: " + desc.isAutomatic());
        System.out.println("desc ver: " + desc.version().get().toString());
        System.out.println("desc rawVer: " + desc.rawVersion().orElse(null));
        System.out.println("desc mainClass: " + desc.mainClass().orElse(null));
        System.out.println("desc hasPkg: " + desc.packages().contains("custom.foo"));
        System.out.println("desc req: " + desc.requires().iterator().next().name());
        System.out.println("desc exp: " + desc.exports().iterator().next().source());

        ModuleDescriptor openDesc = ModuleDescriptor.newOpenModule("open.foo").build();
        System.out.println("openDesc open: " + openDesc.isOpen());

        ModuleDescriptor autoDesc = ModuleDescriptor.newAutomaticModule("auto.foo").build();
        System.out.println("autoDesc auto: " + autoDesc.isAutomatic());

        ModuleDescriptor.Version v1 = ModuleDescriptor.Version.parse("1.2.0");
        ModuleDescriptor.Version v2 = ModuleDescriptor.Version.parse("1.10.0");
        ModuleDescriptor.Version v3 = ModuleDescriptor.Version.parse("2.0");
        ModuleDescriptor.Version v4 = ModuleDescriptor.Version.parse("1.9");
        ModuleDescriptor.Version v5 = ModuleDescriptor.Version.parse("1.0.0");
        ModuleDescriptor.Version v6 = ModuleDescriptor.Version.parse("1.0.0");

        System.out.println("v1 cmp v2: " + Integer.signum(v1.compareTo(v2)));
        System.out.println("v3 cmp v4: " + Integer.signum(v3.compareTo(v4)));
        System.out.println("v5 equals v6: " + v5.equals(v6));
        System.out.println("v5 hash match: " + (v5.hashCode() == v6.hashCode()));

        try {
            ModuleDescriptor.Version.parse(null);
            throw new AssertionError();
        } catch (IllegalArgumentException expected) {
            System.out.println("parse null: iae");
        }

        try {
            ModuleDescriptor.Version.parse("");
            throw new AssertionError();
        } catch (IllegalArgumentException expected) {
            System.out.println("parse empty: iae");
        }
    }
}
