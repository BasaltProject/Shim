/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.lang.module.*;
import java.lang.module.ModuleDescriptor.*;
import java.util.*;
import java.util.concurrent.Callable;
public class ModuleDescriptorModern {
  static void t(String n, Callable<Object> c) { try { System.out.println(n + ": " + c.call()); } catch (Throwable e) { System.out.println(n + ": !" + e.getClass().getName() + ":" + (e instanceof NullPointerException ? null : e.getMessage())); } }
  public static void main(String[] a) throws Exception {
    VersionParsing.run();
    ModuleDescriptor d = ModuleDescriptor.newModule("m.one").version("1.2.3-rc1+b4").requires(Set.of(Requires.Modifier.TRANSITIVE), "m.two").requires(Set.of(Requires.Modifier.STATIC), "m.three", Version.parse("2.0")).exports("p.q").exports("p.r", Set.of("x", "y")).opens("p.s").opens("p.t", Set.of("z")).uses("p.q.Svc").provides("p.q.Svc", List.of("p.impl.A", "p.impl.B")).mainClass("p.q.Main").packages(Set.of("p.u")).build();
    t("toString", () -> d.toString().length());
    t("basic", () -> d.name() + " " + d.version() + " " + d.rawVersion() + " " + d.mainClass() + " " + new TreeSet<>(d.packages()) + " " + d.modifiers() + " " + d.accessFlags() + " " + d.toNameAndVersion() + " " + d.isOpen() + " " + d.isAutomatic());
    t("requires", () -> { List<String> l = new ArrayList<>(); for (Requires r : d.requires()) l.add(r + "|" + r.modifiers() + "|" + r.compiledVersion() + "|" + r.rawCompiledVersion() + "|" + r.accessFlags() + "|" + r.name()); Collections.sort(l); return l; });
    t("exports", () -> { List<String> l = new ArrayList<>(); for (Exports r : d.exports()) l.add(r.source() + (r.isQualified() ? " to " + new TreeSet<>(r.targets()) : "") + "|" + r.modifiers() + "|" + r.isQualified() + "|" + new TreeSet<>(r.targets()) + "|" + r.accessFlags() + "|" + r.source()); Collections.sort(l); return l; });
    t("opens", () -> { List<String> l = new ArrayList<>(); for (Opens r : d.opens()) l.add(r.source() + (r.isQualified() ? " to " + new TreeSet<>(r.targets()) : "") + "|" + r.modifiers() + "|" + r.isQualified() + "|" + new TreeSet<>(r.targets()) + "|" + r.accessFlags() + "|" + r.source()); Collections.sort(l); return l; });
    t("provides", () -> { List<String> l = new ArrayList<>(); for (Provides r : d.provides()) l.add(r + "|" + r.service() + "|" + r.providers()); return l; });
    t("open", () -> { ModuleDescriptor o = ModuleDescriptor.newOpenModule("o").build(); return o + " " + o.modifiers() + " " + o.accessFlags() + " " + o.isOpen() + " " + o.opens() + " " + o.requires(); });
    t("auto", () -> { ModuleDescriptor o = ModuleDescriptor.newAutomaticModule("au").build(); return o + " " + o.modifiers() + " " + o.accessFlags() + " " + o.isAutomatic() + " " + o.requires() + " " + o.toNameAndVersion(); });
    t("newModuleMods", () -> { ModuleDescriptor o = ModuleDescriptor.newModule("nm", Set.of(Modifier.SYNTHETIC)).build(); return o + " " + o.modifiers() + " " + o.accessFlags(); });
    t("newModuleModsAuto", () -> ModuleDescriptor.newModule("nm", Set.of(Modifier.AUTOMATIC)).getClass().getName());
    t("newModuleModsOpen", () -> { ModuleDescriptor o = ModuleDescriptor.newModule("nm", Set.of(Modifier.OPEN, Modifier.MANDATED)).build(); return new TreeSet<>(o.modifiers()) + " " + new TreeSet<>(o.accessFlags()) + " " + o.isOpen(); });
    t("javabase", () -> { ModuleDescriptor o = ModuleDescriptor.newModule("java.base").build(); return o.requires(); });
    t("badName", () -> ModuleDescriptor.newModule("a..b"));
    t("badName2", () -> ModuleDescriptor.newModule(""));
    t("badName3", () -> ModuleDescriptor.newModule("a.1b"));
    t("badName4", () -> ModuleDescriptor.newModule("a.class"));
    t("kw", () -> ModuleDescriptor.newModule("a.if"));
    t("badPkg", () -> ModuleDescriptor.newModule("a").exports("p..q"));
    t("badPkg2", () -> ModuleDescriptor.newModule("a").packages(Set.of("1p")));
    t("badClass", () -> ModuleDescriptor.newModule("a").uses("noPackage"));
    t("badClass2", () -> ModuleDescriptor.newModule("a").mainClass("Main"));
    t("badProvides", () -> ModuleDescriptor.newModule("a").provides("p.S", List.of()));
    t("dupRequires", () -> ModuleDescriptor.newModule("a").requires("b").requires("b"));
    t("dupExports", () -> ModuleDescriptor.newModule("a").exports("p").exports("p"));
    t("selfRequires", () -> ModuleDescriptor.newModule("a").requires("a"));
    t("javaBaseTransitive", () -> ModuleDescriptor.newModule("a").requires(Set.of(Requires.Modifier.TRANSITIVE), "java.base").getClass().getName());
    t("openOpens", () -> ModuleDescriptor.newOpenModule("a").opens("p"));
    t("autoRequires", () -> ModuleDescriptor.newAutomaticModule("a").requires("b"));
    t("versionBad", () -> ModuleDescriptor.newModule("a").version("not.a.version!"));
    t("versionEmpty", () -> Version.parse(""));
    t("versionNull", () -> Version.parse(null));
    t("version", () -> { Version v = Version.parse("1.2.3-rc1+build4"); return v + " " + v.compareTo(Version.parse("1.2.3")) + " " + v.equals(Version.parse("1.2.3-rc1+build4")); });
    t("compare", () -> ModuleDescriptor.newModule("a").build().compareTo(ModuleDescriptor.newModule("b").build()) + " " + ModuleDescriptor.newModule("a").version("1").build().compareTo(ModuleDescriptor.newModule("a").version("2").build()));
    t("equals", () -> ModuleDescriptor.newModule("a").build().equals(ModuleDescriptor.newModule("a").build()) + " " + (ModuleDescriptor.newModule("a").build().hashCode() == ModuleDescriptor.newModule("a").build().hashCode()));
    t("builderFrom", () -> { ModuleDescriptor.Builder b = ModuleDescriptor.newModule("bb"); b.requires(d.requires().iterator().next()); b.exports(d.exports().iterator().next()); b.opens(d.opens().iterator().next()); b.provides(d.provides().iterator().next()); return b.build().name(); });
    t("requiresVersion", () -> { ModuleDescriptor o = ModuleDescriptor.newModule("v").requires(Set.of(), "x", Version.parse("3")).build(); for (Requires r : o.requires()) if (r.name().equals("x")) return r.compiledVersion() + " " + r; return null; });
    t("exportsMods", () -> { ModuleDescriptor o = ModuleDescriptor.newModule("v").exports(Set.of(Exports.Modifier.SYNTHETIC), "p").build(); return o.exports().iterator().next() + " " + o.exports().iterator().next().modifiers(); });
    t("reqMods", () -> { ModuleDescriptor o = ModuleDescriptor.newModule("v").requires(Set.of(Requires.Modifier.SYNTHETIC), "p").build(); for (Requires r : o.requires()) if (r.name().equals("p")) return r + " " + r.accessFlags(); return null; });
    t("pkgsFromExports", () -> new TreeSet<>(ModuleDescriptor.newModule("v").exports("a.b").opens("c.d").provides("e.F", List.of("g.H")).build().packages()));
    t("compareReq", () -> { List<Requires> l = new ArrayList<>(d.requires()); Collections.sort(l); return l; });
  }

  static class VersionParsing {
    public static void run() {
      String[] vs = {"1", "1.0", "1.0.0", "1.2", "1.2.3", "1.2.3-rc1", "1.2.3-rc2", "1.2.3+build", "1.2.3+build2", "1.2.3-rc1+b", "1.10", "1.9", "2", "2.0.1", "1.a", "1.b", "1.2a", "1.2-1", "1.2-alpha", "1.2-alpha.1", "1.2-alpha.2", "1.2-beta", "1.2+1", "1.2+a", "1.2.3.4.5", "10", "9", "1.0-0", "1.0-", "1.0+", "", "a", "-1", "1..2", "1.2.3--x", "1.2.3-+x", "1-2-3", "1.01", "1.1", "01", "1", "123456789012", "99999999999999999999", "1.2.3-rc1-x", "1.2.3-rc.1.2", "1.2.3+b.1-2", "1.0.0-SNAPSHOT", "1.0.0.RELEASE", "1_0", "1 0"};
      for (String v : vs) {
        try { ModuleDescriptor.Version x = ModuleDescriptor.Version.parse(v); System.out.println("parse[" + v + "] ok " + x + " " + x.hashCode()); } catch (Throwable e) { System.out.println("parse[" + v + "] !" + e.getClass().getName() + ":" + e.getMessage()); }
      }
      List<ModuleDescriptor.Version> ok = new ArrayList<>();
      for (String v : vs) { try { ok.add(ModuleDescriptor.Version.parse(v)); } catch (Exception e) { } }
      StringBuilder sb = new StringBuilder();
      for (ModuleDescriptor.Version x : ok) { for (ModuleDescriptor.Version y : ok) sb.append(Integer.signum(x.compareTo(y))).append(x.equals(y) ? "=" : ""); sb.append('\n'); }
      System.out.print(sb);
    }
  }
}
