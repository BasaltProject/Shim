/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.io.*;
import java.lang.module.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import java.util.stream.Collectors;

public class ModuleFinderPathsModern {
  static final List<Object> pool = new ArrayList<>();

  static int utf(String s) {
    int i = pool.indexOf("U" + s);
    if (i < 0) { pool.add("U" + s); i = pool.size() - 1; }
    return i + 1;
  }

  static int mod(String s) {
    int u = utf(s);
    int i = pool.indexOf("M" + u);
    if (i < 0) { pool.add("M" + u); i = pool.size() - 1; }
    return i + 1;
  }

  static int pkg(String s) {
    int u = utf(s.replace('.', '/'));
    int i = pool.indexOf("P" + u);
    if (i < 0) { pool.add("P" + u); i = pool.size() - 1; }
    return i + 1;
  }

  static int cls(String s) {
    int u = utf(s.replace('.', '/'));
    int i = pool.indexOf("C" + u);
    if (i < 0) { pool.add("C" + u); i = pool.size() - 1; }
    return i + 1;
  }

  static byte[] moduleInfo(String name, int flags, String version, String[][] requires, String[] exports, String[] provides, String uses, String main, String[] packages) throws IOException {
    pool.clear();
    ByteArrayOutputStream body = new ByteArrayOutputStream();
    DataOutputStream b = new DataOutputStream(body);
    int thisClass = cls("module-info");
    int moduleAttr = utf("Module");
    int nameIdx = mod(name);
    int verIdx = version == null ? 0 : utf(version);
    ByteArrayOutputStream ma = new ByteArrayOutputStream();
    DataOutputStream m = new DataOutputStream(ma);
    m.writeShort(nameIdx);
    m.writeShort(flags);
    m.writeShort(verIdx);
    m.writeShort(requires.length);
    for (String[] r : requires) {
      m.writeShort(mod(r[0]));
      m.writeShort(Integer.parseInt(r[1]));
      m.writeShort(0);
    }
    m.writeShort(exports.length);
    for (String e : exports) {
      String[] parts = e.split("@");
      m.writeShort(pkg(parts[0]));
      m.writeShort(0);
      if (parts.length > 1) {
        m.writeShort(1);
        m.writeShort(mod(parts[1]));
      } else {
        m.writeShort(0);
      }
    }
    m.writeShort(0);
    if (uses == null) {
      m.writeShort(0);
    } else {
      m.writeShort(1);
      m.writeShort(cls(uses));
    }
    if (provides.length == 0) {
      m.writeShort(0);
    } else {
      m.writeShort(1);
      m.writeShort(cls(provides[0]));
      m.writeShort(provides.length - 1);
      for (int i = 1; i < provides.length; i++) {
        m.writeShort(cls(provides[i]));
      }
    }
    int pkgAttr = packages == null ? 0 : utf("ModulePackages");
    int mainAttr = main == null ? 0 : utf("ModuleMainClass");
    int[] pk = new int[packages == null ? 0 : packages.length];
    for (int i = 0; i < pk.length; i++) pk[i] = pkg(packages[i]);
    int mc = main == null ? 0 : cls(main);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    DataOutputStream o = new DataOutputStream(out);
    o.writeInt(0xCAFEBABE);
    o.writeShort(0);
    o.writeShort(69);
    o.writeShort(pool.size() + 1);
    for (Object p : pool) {
      String s = (String) p;
      char k = s.charAt(0);
      String v = s.substring(1);
      if (k == 'U') { o.writeByte(1); o.writeUTF(v); }
      else if (k == 'C') { o.writeByte(7); o.writeShort(Integer.parseInt(v)); }
      else if (k == 'M') { o.writeByte(19); o.writeShort(Integer.parseInt(v)); }
      else { o.writeByte(20); o.writeShort(Integer.parseInt(v)); }
    }
    o.writeShort(0x8000);
    o.writeShort(thisClass);
    o.writeShort(0);
    o.writeShort(0);
    o.writeShort(0);
    o.writeShort(0);
    o.writeShort(1 + (packages != null ? 1 : 0) + (main != null ? 1 : 0));
    o.writeShort(moduleAttr);
    o.writeInt(ma.size());
    o.write(ma.toByteArray());
    if (packages != null) {
      o.writeShort(pkgAttr);
      o.writeInt(2 + 2 * pk.length);
      o.writeShort(pk.length);
      for (int x : pk) o.writeShort(x);
    }
    if (main != null) {
      o.writeShort(mainAttr);
      o.writeInt(2);
      o.writeShort(mc);
    }
    return out.toByteArray();
  }

  static String show(ModuleDescriptor d) {
    return d.name() + " open=" + d.isOpen() + " auto=" + d.isAutomatic() + " ver=" + d.rawVersion()
        + " main=" + d.mainClass() + " pkgs=" + new TreeSet<>(d.packages())
        + " req=" + d.requires().stream().map(r -> r.name() + new TreeSet<>(r.modifiers())).sorted().collect(Collectors.toList())
        + " exp=" + d.exports().stream().map(e -> e.source() + new TreeSet<>(e.targets())).sorted().collect(Collectors.toList())
        + " uses=" + new TreeSet<>(d.uses())
        + " prov=" + d.provides().stream().map(p -> p.service() + p.providers()).sorted().collect(Collectors.toList());
  }

  static void t(String n, java.util.concurrent.Callable<Object> c) {
    try {
      System.out.println(n + ": " + c.call());
    } catch (Throwable e) {
      System.out.println(n + ": !" + e.getClass().getName() + ":" + (e instanceof NullPointerException ? null : e.getMessage()));
    }
  }

  public static void main(String[] args) throws Exception {
    Path root = Files.createTempDirectory("mfp");
    Path mods = root.resolve("mods");
    Path expl = mods.resolve("m.one");
    Files.createDirectories(expl.resolve("p/q"));
    Files.createDirectories(expl.resolve("p/r"));
    Files.write(expl.resolve("module-info.class"), moduleInfo("m.one", 0, "1.5", new String[][] {{"java.base", "32768"}, {"m.two", "32"}}, new String[] {"p.q", "p.r@m.two"}, new String[] {"p.S", "p.q.Impl"}, "p.S", "p.q.Main", null));
    Files.write(expl.resolve("p/q/Main.class"), new byte[] {1});
    Files.write(expl.resolve("p/r/R.class"), new byte[] {1});
    Files.write(expl.resolve("p/S.class"), new byte[] {1});
    Path jar = mods.resolve("two.jar");
    try (JarOutputStream jo = new JarOutputStream(Files.newOutputStream(jar))) {
      jo.putNextEntry(new JarEntry("module-info.class"));
      jo.write(moduleInfo("m.two", 0x20, null, new String[][] {{"java.base", "32768"}, {"m.three", "64"}}, new String[0], new String[0], null, null, new String[] {"t.w"}));
      jo.closeEntry();
      jo.putNextEntry(new JarEntry("t/w/X.class"));
      jo.write(new byte[] {2});
      jo.closeEntry();
      jo.putNextEntry(new JarEntry("res.txt"));
      jo.write("hello".getBytes());
      jo.closeEntry();
    }
    Path lib = mods.resolve("my-lib_core-1.2.3.jar");
    try (JarOutputStream jo = new JarOutputStream(Files.newOutputStream(lib))) {
      jo.putNextEntry(new JarEntry("lib/core/Y.class"));
      jo.write(new byte[] {3});
      jo.closeEntry();
    }
    ModuleFinder dirFinder = ModuleFinder.of(mods);
    List<ModuleReference> all = new ArrayList<>(dirFinder.findAll());
    all.sort(Comparator.comparing(r -> r.descriptor().name()));
    for (ModuleReference r : all) System.out.println(show(r.descriptor()));
    t("find", () -> dirFinder.find("m.one").isPresent() + " " + dirFinder.find("m.two").get().location().get().toString().endsWith("two.jar") + " " + dirFinder.find("zzz").isPresent());
    t("autoName", () -> dirFinder.find("my.lib.core").isPresent());
    t("jarFinder", () -> ModuleFinder.of(jar).findAll().size() + " " + ModuleFinder.of(expl).find("m.one").isPresent());
    t("reader", () -> {
      try (ModuleReader rd = dirFinder.find("m.two").get().open()) {
        List<String> l = rd.list().sorted().collect(Collectors.toList());
        return l + " " + new String(rd.open("res.txt").get().readAllBytes()) + " " + rd.find("nope").isPresent() + " " + rd.read("res.txt").get().remaining();
      }
    });
    t("explodedReader", () -> {
      try (ModuleReader rd = dirFinder.find("m.one").get().open()) {
        return rd.list().sorted().collect(Collectors.toList()) + " " + rd.find("p/q/Main.class").isPresent();
      }
    });
    t("closed", () -> {
      ModuleReader rd = dirFinder.find("m.two").get().open();
      rd.close();
      return rd.find("res.txt");
    });
    t("resolve", () -> {
      Configuration cf = Configuration.resolve(dirFinder, List.of(Configuration.empty()), ModuleFinder.ofSystem(), List.of("m.one"));
      return cf.findModule("m.two").isPresent() + " " + cf.findModule("m.three").isPresent();
    });
    t("read", () -> show(ModuleDescriptor.read(new ByteArrayInputStream(Files.readAllBytes(expl.resolve("module-info.class"))))));
    t("readBuf", () -> show(ModuleDescriptor.read(java.nio.ByteBuffer.wrap(Files.readAllBytes(expl.resolve("module-info.class"))))));
    t("readBad", () -> ModuleDescriptor.read(new ByteArrayInputStream(new byte[] {1, 2, 3, 4, 5})));
  }
}
