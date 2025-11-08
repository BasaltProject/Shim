/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.lang.constant.*;
import java.util.*;
import java.util.concurrent.Callable;
public class PackageModuleDescModern {
  static void t(String n, Callable<Object> c) { try { System.out.println(n + ": " + c.call()); } catch (Throwable e) { System.out.println(n + ": !" + e.getClass().getName() + ":" + (e instanceof NullPointerException ? null : e.getMessage())); } }
  public static void main(String[] a) throws Exception {
    for (String s : new String[] {"java.lang", "", "a", "a.b.c", ".a", "a.", "a..b", "a/b", "a;b", "a[b", "java.lang.", "1a.b", "a-b.c", "a b", "é.ü"})
      t("pkg[" + s + "]", () -> { PackageDesc p = PackageDesc.of(s); return p + " " + p.name() + " " + p.internalName() + " " + p.equals(PackageDesc.of(s)) + " " + p.hashCode(); });
    for (String s : new String[] {"java/lang", "", "a", "a/b/c", "/a", "a/", "a//b", "a.b", "a;b", "a[b"})
      t("pkgInternal[" + s + "]", () -> { PackageDesc p = PackageDesc.ofInternalName(s); return p + " " + p.name() + " " + p.internalName(); });
    for (String s : new String[] {"java.base", "", "a", "a.b", "a@b", "a:b", "a\\\\b", ".a", "a.", "a..b", "a/b", "java.base ", "a;b", "é"})
      t("mod[" + s + "]", () -> { ModuleDesc m = ModuleDesc.of(s); return m + " " + m.name() + " " + m.equals(ModuleDesc.of(s)) + " " + m.hashCode(); });
    t("nulls", () -> PackageDesc.of(null));
    t("nulls2", () -> ModuleDesc.of(null));
    t("nulls3", () -> PackageDesc.ofInternalName(null));
    t("eq", () -> PackageDesc.of("a.b").equals(PackageDesc.of("a.c")) + " " + PackageDesc.of("a").equals("a") + " " + ModuleDesc.of("a").equals(ModuleDesc.of("a")));
    t("cdOfInternal", () -> { ClassDesc c = ClassDesc.ofInternalName("java/lang/String"); return c + " " + c.descriptorString() + " " + c.packageName(); });
    t("cdOfInternalBad", () -> ClassDesc.ofInternalName("java.lang.String"));
    t("cdOfInternalEmpty", () -> ClassDesc.ofInternalName(""));
    t("cdOfInternalArr", () -> ClassDesc.ofInternalName("[I"));
    t("cdOfInternalNull", () -> ClassDesc.ofInternalName(null));
    t("cdOfInternalSlash", () -> ClassDesc.ofInternalName("a//b"));
    t("cdOfInternalDefault", () -> ClassDesc.ofInternalName("Foo").packageName().isEmpty());
    t("ifaces", () -> Arrays.toString(PackageDesc.class.getInterfaces()) + " " + Arrays.toString(ModuleDesc.class.getInterfaces()) + " " + ModuleDesc.class.isInterface());
    t("resolve", () -> PackageDesc.of("a").getClass().getSimpleName());
  }
}
