/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.cs;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class StandardCharsets {
  private StandardCharsets() { }

  private static final Map<String, Object> kByName = new HashMap<String, Object>();
  private static final List<Object> kAll = new ArrayList<Object>();

  private static void register(Object cs, String name, String[] aliases) {
    kAll.add(cs);
    kByName.put(name.toLowerCase(Locale.ROOT), cs);
    for (String alias : aliases) {
      kByName.put(alias.toLowerCase(Locale.ROOT), cs);
    }
  }

  private static void register(Charset cs) {
    register(cs, cs.name(), cs.aliases().toArray(new String[0]));
  }

  static {
    register(UTF_8.INSTANCE);
    register(CESU_8.INSTANCE);
    register(ISO_8859_1.INSTANCE);
    register(US_ASCII.INSTANCE);
    register(UnicodeCharset.UTF_16);
    register(UnicodeCharset.UTF_16BE);
    register(UnicodeCharset.UTF_16LE);
    register(UnicodeCharset.UTF_16LE_BOM);
    register(UnicodeCharset.UTF_32);
    register(UnicodeCharset.UTF_32BE);
    register(UnicodeCharset.UTF_32LE);
    register(UnicodeCharset.UTF_32BE_BOM);
    register(UnicodeCharset.UTF_32LE_BOM);
    register(ISO2022_JP.INSTANCE);
    register(ISO2022_JP.INSTANCE2);
    register(ISO2022_KR.INSTANCE);
    register(ISO2022_CN.CNS);
    register(ISO2022_CN.GB);
    register(ISO2022_JP.MS50220);
    register(ISO2022_JP.MS50221);
    register(ISO2022_JP.WINDOWS);
    register(ISCII91.INSTANCE);
    register(JISAutoDetect.INSTANCE);
    for (int i = 0; i < SingleByteTables.kEntries.length; i++) {
      String entry = SingleByteTables.kEntries[i];
      int first = entry.indexOf('|');
      int second = entry.indexOf('|', first + 1);
      String aliases = entry.substring(first + 1, second);
      register(Integer.valueOf(i), entry.substring(0, first), aliases.isEmpty() ? new String[0] : aliases.split(","));
    }
    for (int i = 0; i < MultiByteTables.kEntries.length; i++) {
      String[] f = MultiByteTables.kEntries[i].split("\\|", -1);
      register(Long.valueOf(i), f[0], f[1].isEmpty() ? new String[0] : f[1].split(","));
    }
    for (int i = 0; i < ExtendedTables.kEntries.length; i++) {
      String[] f = ExtendedTables.kEntries[i].split("\\|", -1);
      if (!f[3].equals("X")) {
        register(Short.valueOf((short) i), f[0], f[1].isEmpty() ? new String[0] : f[1].split(","));
      }
    }
  }

  private static Charset resolve(Object o) {
    if (o instanceof Charset) {
      return (Charset) o;
    }
    synchronized (kAll) {
      Object current = kAll.get(kAll.indexOf(o));
      if (current instanceof Charset) {
        return (Charset) current;
      }
      if (o instanceof Short) {
        int extended = ((Short) o).intValue();
        Charset built = new TableCharset(ExtendedTables.kEntries[extended].split("\\|", -1), ExtendedTables.kBlobs[extended]);
        replace(o, built);
        return built;
      }
      if (o instanceof Long) {
        int multi = ((Long) o).intValue();
        String[] m = MultiByteTables.kEntries[multi].split("\\|", -1);
        Charset built = new MultiByte(m[0], m[1].isEmpty() ? new String[0] : m[1].split(","), m[2], m[3].equals("1"),
            m[4].equals("1"), MultiByteTables.kBlobs[multi]);
        replace(o, built);
        return built;
      }
      int index = ((Integer) o).intValue();
      String[] f = SingleByteTables.kEntries[index].split("\\|", -1);
      String[] aliases = f[1].isEmpty() ? new String[0] : f[1].split(",");
      Charset cs = new SingleByte(f[0], aliases, f[2], Integer.parseInt(f[3], 16), f[4].equals("1"),
          f[5].equals("1"), f[6], f[7]);
      replace(o, cs);
      return cs;
    }
  }

  private static void replace(Object placeholder, Charset cs) {
    kAll.set(kAll.indexOf(placeholder), cs);
    for (Map.Entry<String, Object> e : kByName.entrySet()) {
      if (e.getValue() == placeholder) {
        e.setValue(cs);
      }
    }
  }

  public static Charset lookup(String name) {
    Object o;
    synchronized (kAll) {
      o = kByName.get(name.toLowerCase(Locale.ROOT));
    }
    return o == null ? null : resolve(o);
  }

  public static List<Charset> all() {
    List<Charset> result = new ArrayList<Charset>();
    int size;
    synchronized (kAll) {
      size = kAll.size();
    }
    for (int i = 0; i < size; i++) {
      Object o;
      synchronized (kAll) {
        o = kAll.get(i);
      }
      result.add(resolve(o));
    }
    return result;
  }
}
