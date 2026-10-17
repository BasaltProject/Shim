/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.io.IOException;
import java.lang.module.*;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

public class ModuleLayerDefineModern {
  static ModuleReference ref(ModuleDescriptor d) {
    return new ModuleReference(d, URI.create("mem:" + d.name())) {
      public ModuleReader open() throws IOException { throw new IOException("none"); }
    };
  }

  static ModuleFinder finder(ModuleDescriptor... ds) {
    Map<String, ModuleReference> m = new LinkedHashMap<>();
    for (ModuleDescriptor d : ds) m.put(d.name(), ref(d));
    return new ModuleFinder() {
      public Optional<ModuleReference> find(String n) { return Optional.ofNullable(m.get(n)); }
      public Set<ModuleReference> findAll() { return new HashSet<>(m.values()); }
    };
  }

  static void t(String n, java.util.concurrent.Callable<Object> c) {
    try {
      System.out.println(n + ": " + c.call());
    } catch (Throwable e) {
      System.out.println(n + ": !" + e.getClass().getName() + ":" + (e instanceof NullPointerException ? null : e.getMessage()));
    }
  }

  public static void main(String[] args) throws Exception {
    ModuleDescriptor a = ModuleDescriptor.newModule("a").requires("b").exports("a.api").exports("a.q", Set.of("b")).opens("a.res", Set.of("b")).packages(Set.of("a.api", "a.q", "a.res", "a.hidden")).build();
    ModuleDescriptor b = ModuleDescriptor.newModule("b").exports("b.api").packages(Set.of("b.api", "b.internal")).build();
    ModuleDescriptor o = ModuleDescriptor.newOpenModule("o").packages(Set.of("o.x")).build();
    ModuleDescriptor lone = ModuleDescriptor.newModule("lone").packages(Set.of("lone.p")).build();
    ModuleFinder sysf = ModuleFinder.ofSystem();
    ModuleFinder f = finder(a, b, o, lone);
    ModuleLayer boot = ModuleLayer.boot();
    Configuration cf = boot.configuration().resolve(f, ModuleFinder.of(), List.of("a", "o", "lone"));
    ClassLoader cl = ClassLoader.getSystemClassLoader();
    ModuleLayer.Controller ctl = ModuleLayer.defineModules(cf, List.of(boot), n -> cl);
    ModuleLayer layer = ctl.layer();
    Module ma = layer.findModule("a").get(), mb = layer.findModule("b").get(), mo = layer.findModule("o").get(), ml = layer.findModule("lone").get();
    System.out.println(layer.modules().stream().map(Module::getName).sorted().collect(Collectors.toList()));
    System.out.println(layer.parents().size() + " " + (layer.parents().get(0) == boot) + " " + (layer.configuration() == cf));
    System.out.println(ma + " " + ma.getName() + " " + ma.isNamed() + " " + (ma.getLayer() == layer) + " " + (ma.getClassLoader() == cl) + " " + new TreeSet<>(ma.getPackages()));
    System.out.println("reads " + ma.canRead(mb) + " " + mb.canRead(ma) + " " + ma.canRead(ml) + " " + ma.canRead(ma));
    System.out.println("exp " + ma.isExported("a.api") + " " + ma.isExported("a.q") + " " + ma.isExported("a.q", mb) + " " + ma.isExported("a.q", ml) + " " + ma.isExported("a.hidden") + " " + ma.isExported("nope"));
    System.out.println("open " + ma.isOpen("a.res") + " " + ma.isOpen("a.res", mb) + " " + ma.isOpen("a.res", ml) + " " + ma.isOpen("a.api") + " " + mo.isOpen("o.x") + " " + mo.isExported("o.x") + " " + mo.isOpen("nope"));
    t("addReads", () -> { ctl.addReads(ml, ma); return ml.canRead(ma) + " " + ma.canRead(ml); });
    t("addExports", () -> { ctl.addExports(ma, "a.hidden", ml); return ma.isExported("a.hidden", ml) + " " + ma.isExported("a.hidden", mb) + " " + ma.isExported("a.hidden"); });
    t("addOpens", () -> { ctl.addOpens(mb, "b.internal", ma); return mb.isOpen("b.internal", ma) + " " + mb.isOpen("b.internal", ml) + " " + mb.isExported("b.internal", ma); });
    t("addExportsBadPkg", () -> ctl.addExports(ma, "nope", mb));
    t("addOutside", () -> ctl.addReads(boot.findModule("java.base").get(), ma));
    t("addOutside2", () -> ctl.addReads(ma, null));
    t("findLoader", () -> (layer.findLoader("a") == cl) + " " + (layer.findLoader("java.base") == boot.findLoader("java.base")));
    t("findLoaderBad", () -> layer.findLoader("zz"));
    t("findModule", () -> layer.findModule("java.base").isPresent() + " " + layer.findModule("zz").isPresent());
    t("nullLoader", () -> ModuleLayer.defineModules(cf, List.of(boot), n -> null));
    t("wrongParent", () -> ModuleLayer.defineModules(cf, List.of(ModuleLayer.empty()), n -> cl));
    t("noParents", () -> ModuleLayer.defineModules(cf, List.of(), n -> cl));
    t("oneLoader", () -> { ModuleLayer l = ModuleLayer.defineModulesWithOneLoader(cf, List.of(boot), cl).layer(); return l.findLoader("a") == l.findLoader("b"); });
    t("manyLoaders", () -> { ModuleLayer l = ModuleLayer.defineModulesWithManyLoaders(cf, List.of(boot), cl).layer(); return l.modules().size(); });
    t("empty", () -> ModuleLayer.empty().modules() + " " + ModuleLayer.empty().parents() + " " + ModuleLayer.empty().configuration().modules().size());
    t("child", () -> {
      Configuration cf2 = cf.resolve(finder(ModuleDescriptor.newModule("c").requires("a").build()), ModuleFinder.of(), List.of("c"));
      ModuleLayer l2 = ModuleLayer.defineModules(cf2, List.of(layer), n -> cl).layer();
      Module mc = l2.findModule("c").get();
      return l2.findModule("a").get().equals(ma) + " " + mc.canRead(ma) + " " + (l2.findLoader("a") == cl) + " " + l2.modules().size();
    });
  }
}
