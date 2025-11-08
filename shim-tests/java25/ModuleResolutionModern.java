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

public class ModuleResolutionModern {
  static ModuleReference ref(ModuleDescriptor d) {
    return new ModuleReference(d, URI.create("mem:" + d.name())) {
      public ModuleReader open() throws IOException {
        return new ModuleReader() {
          public Optional<URI> find(String n) { return Optional.empty(); }
          public java.util.stream.Stream<String> list() { return java.util.stream.Stream.of("a", "b"); }
          public void close() { }
        };
      }
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

  static String names(Collection<ResolvedModule> c) {
    return c.stream().map(ResolvedModule::name).sorted().collect(Collectors.joining(","));
  }

  static void t(String n, java.util.concurrent.Callable<Object> c) {
    try {
      System.out.println(n + ": " + c.call());
    } catch (Throwable e) {
      System.out.println(n + ": !" + e.getClass().getName() + ":" + (e instanceof NullPointerException ? null : e.getMessage()));
    }
  }

  static String dump(Configuration cf) {
    StringBuilder sb = new StringBuilder();
    List<ResolvedModule> ms = new ArrayList<>(cf.modules());
    ms.sort(Comparator.comparing(ResolvedModule::name));
    for (ResolvedModule r : ms) sb.append(r.name()).append("->").append(names(r.reads())).append(" ");
    return sb.toString();
  }

  public static void main(String[] args) throws Exception {
    ModuleDescriptor a = ModuleDescriptor.newModule("a").requires("b").requires(Set.of(ModuleDescriptor.Requires.Modifier.STATIC), "opt").build();
    ModuleDescriptor b = ModuleDescriptor.newModule("b").requires(Set.of(ModuleDescriptor.Requires.Modifier.TRANSITIVE), "c").build();
    ModuleDescriptor c = ModuleDescriptor.newModule("c").requires("d").build();
    ModuleDescriptor d = ModuleDescriptor.newModule("d").build();
    ModuleDescriptor svc = ModuleDescriptor.newModule("svc").exports("svc").build();
    ModuleDescriptor e = ModuleDescriptor.newModule("e").uses("svc.S").requires("a").requires("svc").build();
    ModuleDescriptor e2 = ModuleDescriptor.newModule("e2").uses("nopkg.S").build();
    ModuleDescriptor p = ModuleDescriptor.newModule("p").requires("svc").provides("svc.S", List.of("p.Impl")).build();
    ModuleDescriptor auto = ModuleDescriptor.newAutomaticModule("auto").build();
    ModuleDescriptor cyc1 = ModuleDescriptor.newModule("cyc1").requires("cyc2").build();
    ModuleDescriptor cyc2 = ModuleDescriptor.newModule("cyc2").requires("cyc1").build();
    ModuleFinder f = finder(a, b, c, d, e, e2, svc, p, auto, cyc1, cyc2);
    ModuleFinder sys = ModuleFinder.ofSystem();
    ModuleFinder none = new ModuleFinder() {
      public Optional<ModuleReference> find(String n) { return n.equals("java.base") ? sys.find(n) : Optional.empty(); }
      public Set<ModuleReference> findAll() { return sys.find("java.base").map(Set::of).get(); }
    };
    Configuration cfa = Configuration.resolve(f, List.of(Configuration.empty()), none, List.of("a"));
    System.out.println(names(cfa.modules()) + " | " + dump(cfa));
    Configuration cfe = Configuration.resolve(f, List.of(Configuration.empty()), none, List.of("e"));
    System.out.println(names(cfe.modules()) + " | " + dump(cfe));
    Configuration bound = Configuration.resolveAndBind(f, List.of(Configuration.empty()), none, List.of("e"));
    System.out.println(names(bound.modules()));
    Configuration au = Configuration.resolve(f, List.of(Configuration.empty()), none, List.of("auto", "d"));
    System.out.println(dump(au));
    t("cycle", () -> Configuration.resolve(f, List.of(Configuration.empty()), none, List.of("cyc1")));
    Configuration child = cfa.resolve(finder(svc, e), none, List.of("e"));
    System.out.println(names(child.modules()) + " | " + dump(child) + " | " + (child.parents().get(0) == cfa) + " " + child.findModule("a").isPresent() + " " + child.findModule("zz").isPresent());
    System.out.println(child.findModule("a").get().configuration() == cfa);
    t("missingRoot", () -> Configuration.resolve(f, List.of(Configuration.empty()), none, List.of("nope")));
    t("missingReq", () -> Configuration.resolve(finder(a), List.of(Configuration.empty()), none, List.of("a")));
    t("emptyParents", () -> Configuration.resolve(f, List.of(), none, List.of("a")));
    t("nullRoot", () -> Configuration.resolve(f, List.of(Configuration.empty()), none, Arrays.asList((String) null)));
    t("empty", () -> Configuration.empty() + "|" + Configuration.empty().modules() + Configuration.empty().parents());
    t("afterFinder", () -> names(Configuration.resolve(none, List.of(Configuration.empty()), f, List.of("d")).modules()));
    t("beforeWins", () -> {
      ModuleDescriptor d2 = ModuleDescriptor.newModule("d").requires("svc").build();
      Configuration cf = Configuration.resolve(finder(d2), List.of(Configuration.empty()), ModuleFinder.compose(f, none), List.of("d"));
      return dump(cf);
    });
    t("ref", () -> {
      ResolvedModule r = cfa.findModule("a").get();
      return r.name() + " " + r.reference().descriptor().name() + " " + r.reference().location().get() + " " + (r.configuration() == cfa) + " " + r.equals(cfa.findModule("a").get());
    });
    t("reader", () -> {
      try (ModuleReader rd = cfa.findModule("a").get().reference().open()) {
        return rd.list().collect(Collectors.toList()) + " " + rd.find("x") + " " + rd.open("x");
      }
    });
    t("refNull", () -> new ModuleReference(null, null) { public ModuleReader open() { return null; } });
    t("refNoLoc", () -> new ModuleReference(a, null) { public ModuleReader open() { return null; } }.location());
    t("compose", () -> {
      ModuleFinder cm = ModuleFinder.compose(finder(a), finder(b, ModuleDescriptor.newModule("a").build()));
      return cm.find("a").get().descriptor().requires().size() + " " + cm.findAll().size() + " " + cm.find("zz").isPresent();
    });
    t("composeNull", () -> ModuleFinder.compose((ModuleFinder) null));
    t("composeEmpty", () -> ModuleFinder.compose().findAll());
    t("ofNone", () -> ModuleFinder.of().findAll());
    t("ofMissing", () -> ModuleFinder.of(java.nio.file.Path.of("/nonexistent/dir")).find("x"));
    t("exceptions", () -> new FindException("m", new RuntimeException("c")).getCause().getMessage() + new ResolutionException(new RuntimeException("q")).getMessage() + new InvalidModuleDescriptorException("z").getMessage());
    t("usesFail", () -> Configuration.resolve(f, List.of(Configuration.empty()), none, List.of("e2")));
    t("cfString", () -> Configuration.resolve(f, List.of(Configuration.empty()), none, List.of("svc")).modules().size());
  }
}
