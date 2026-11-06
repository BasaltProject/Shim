/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package shim.awt;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

final class SystemFonts {
  private SystemFonts() { }

  static final class Entry {
    String family;
    String sub;
    String full;
    String ps;
    File file;
    int style;
  }

  private static List<Entry> kEntries;

  private static synchronized List<Entry> all() {
    if (kEntries == null) {
      List<Entry> out = new ArrayList<Entry>();
      String home = System.getProperty("user.home");
      String[] roots = {"/usr/share/fonts", "/usr/local/share/fonts", home + "/.fonts", home + "/.local/share/fonts"};
      for (String r : roots) {
        scan(new File(r), 0, out);
      }
      kEntries = out;
    }
    return kEntries;
  }

  private static void scan(File dir, int depth, List<Entry> out) {
    File[] files = dir.listFiles();
    if (files == null || depth > 6) {
      return;
    }
    java.util.Arrays.sort(files);
    for (File f : files) {
      if (f.isDirectory()) {
        scan(f, depth + 1, out);
        continue;
      }
      String n = f.getName().toLowerCase(Locale.ROOT);
      if (n.endsWith(".pfb") || n.endsWith(".pfa")) {
        try {
          Type1Face t = Type1Face.parse(java.nio.file.Files.readAllBytes(f.toPath()));
          Entry e = new Entry();
          e.family = t.family();
          e.full = t.fullName();
          e.ps = t.postScriptName();
          e.file = f;
          e.style = t.fontStyle();
          e.sub = "Regular";
          if (e.family != null) {
            out.add(e);
          }
        } catch (IOException | java.awt.FontFormatException | RuntimeException ex) {
          continue;
        }
        continue;
      }
      if (n.endsWith(".ttf") || n.endsWith(".otf")) {
        try {
          Entry e = read(f);
          if (e != null) {
            out.add(e);
          }
        } catch (IOException | RuntimeException ex) {
          continue;
        }
      }
    }
  }

  private static Entry read(File f) throws IOException {
    try (RandomAccessFile in = new RandomAccessFile(f, "r")) {
      byte[] head = new byte[12];
      in.readFully(head);
      int tag = u32(head, 0);
      if (tag != 0x00010000 && tag != 0x4F54544F && tag != 0x74727565) {
        return null;
      }
      int count = u16(head, 4);
      byte[] dir = new byte[count * 16];
      in.readFully(dir);
      int off = -1;
      int len = 0;
      for (int i = 0; i < count; i++) {
        if (u32(dir, i * 16) == 0x6E616D65) {
          off = u32(dir, i * 16 + 8);
          len = u32(dir, i * 16 + 12);
        }
      }
      if (off < 0 || len <= 6 || len > (1 << 20)) {
        return null;
      }
      byte[] t = new byte[len];
      in.seek(off);
      in.readFully(t);
      int n = u16(t, 2);
      int strings = u16(t, 4);
      String[] best = new String[19];
      int[] rank = new int[19];
      for (int i = 0; i < n; i++) {
        int r = 6 + i * 12;
        int platform = u16(t, r);
        int lang = u16(t, r + 4);
        int id = u16(t, r + 6);
        int l = u16(t, r + 8);
        int o = strings + u16(t, r + 10);
        if (id >= 19 || o + l > len) {
          continue;
        }
        int score = platform == 3 && lang == 0x409 ? 3 : platform == 3 ? 2 : platform == 1 && lang == 0 ? 1 : 0;
        if (score == 0 || score <= rank[id]) {
          continue;
        }
        best[id] = platform == 3 ? new String(t, o, l, StandardCharsets.UTF_16BE)
            : new String(t, o, l, StandardCharsets.ISO_8859_1);
        rank[id] = score;
      }
      Entry e = new Entry();
      e.family = best[1] != null ? best[1] : best[16];
      e.sub = best[2] != null ? best[2] : best[17];
      e.full = best[4];
      e.ps = best[6];
      e.file = f;
      if (e.family == null) {
        return null;
      }
      if (e.sub == null) {
        e.sub = "Regular";
      }
      if (e.full == null) {
        e.full = e.family + (e.sub.equals("Regular") ? "" : " " + e.sub);
      }
      String s = e.sub.toLowerCase(Locale.ROOT);
      e.style = (s.contains("bold") ? 1 : 0) | (s.contains("italic") || s.contains("oblique") ? 2 : 0);
      return e;
    }
  }

  private static int u16(byte[] b, int o) {
    return ((b[o] & 255) << 8) | (b[o + 1] & 255);
  }

  private static int u32(byte[] b, int o) {
    return (u16(b, o) << 16) | u16(b, o + 2);
  }

  static java.util.List<Entry> entries() {
    return all();
  }

  static Entry find(String name, int style) {
    if (name == null) {
      return null;
    }
    String lower = name.toLowerCase(Locale.ROOT);
    Entry plain = null;
    Entry any = null;
    for (Entry e : all()) {
      if (e.full.toLowerCase(Locale.ROOT).equals(lower) && !e.full.equalsIgnoreCase(e.family)) {
        return e;
      }
    }
    Entry styled = null;
    for (Entry e : all()) {
      if (e.family.toLowerCase(Locale.ROOT).equals(lower)) {
        if (e.style == (style & 3)) {
          if (e.full.equalsIgnoreCase(e.family)) {
            return e;
          }
          if (styled == null) {
            styled = e;
          }
          continue;
        }
        if (e.style == 0 && plain == null) {
          plain = e;
        }
        if (any == null) {
          any = e;
        }
      }
    }
    if (styled != null) {
      return styled;
    }
    if (plain != null || any != null) {
      return plain != null ? plain : any;
    }
    for (Entry e : all()) {
      if (e.full.toLowerCase(Locale.ROOT).equals(lower) || e.ps != null && e.ps.toLowerCase(Locale.ROOT).equals(lower)) {
        return e;
      }
    }
    return null;
  }

  static String[] families() {
    TreeSet<String> out = new TreeSet<String>();
    for (Entry e : all()) {
      out.add(e.family);
    }
    return out.toArray(new String[0]);
  }
}
