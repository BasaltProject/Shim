/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package shim;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public final class CharacterNames {
  private static int[] rangeStart;
  private static int[] rangeEnd;
  private static String[] rangePrefix;
  private static int[] codes;
  private static String[] names;
  private static Map<String, Integer> byName;

  private CharacterNames() { }

  private static synchronized void load() {
    if (codes != null) {
      return;
    }
    StringBuilder encoded = new StringBuilder();
    for (String chunk : CharacterNamesData.kChunks) {
      encoded.append(chunk);
    }
    String text;
    try {
      InputStream in = new GZIPInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(encoded.toString())));
      ByteArrayOutputStream out = new ByteArrayOutputStream(1 << 21);
      byte[] buffer = new byte[16384];
      int n;
      while ((n = in.read(buffer)) > 0) {
        out.write(buffer, 0, n);
      }
      text = new String(out.toByteArray(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
    String[] lines = text.split("\n");
    int at = 0;
    int ranges = Integer.parseInt(lines[at++].substring(2));
    int[] rs = new int[ranges];
    int[] re = new int[ranges];
    String[] rp = new String[ranges];
    for (int i = 0; i < ranges; i++) {
      String[] f = lines[at++].split("\t", 3);
      rs[i] = Integer.parseInt(f[0], 16);
      re[i] = Integer.parseInt(f[1], 16);
      rp[i] = f[2];
    }
    int count = Integer.parseInt(lines[at++].substring(2));
    int[] cs = new int[count];
    String[] ns = new String[count];
    int prev = 0;
    for (int i = 0; i < count; i++) {
      String line = lines[at++];
      int tab = line.indexOf('\t');
      prev += Integer.parseInt(line.substring(0, tab), 16);
      cs[i] = prev;
      ns[i] = line.substring(tab + 1);
    }
    rangeStart = rs;
    rangeEnd = re;
    rangePrefix = rp;
    names = ns;
    codes = cs;
  }

  public static String nameOf(int codePoint) {
    load();
    int i = Arrays.binarySearch(codes, codePoint);
    if (i >= 0) {
      return names[i];
    }
    for (int r = 0; r < rangeStart.length; r++) {
      if (codePoint >= rangeStart[r] && codePoint <= rangeEnd[r]) {
        return rangePrefix[r] + " " + Integer.toHexString(codePoint).toUpperCase(java.util.Locale.ROOT);
      }
    }
    return null;
  }

  public static synchronized int codePointOf(String name) {
    load();
    if (byName == null) {
      Map<String, Integer> map = new HashMap<String, Integer>(codes.length * 2);
      for (int i = 0; i < codes.length; i++) {
        map.putIfAbsent(names[i], codes[i]);
      }
      byName = map;
    }
    Integer found = byName.get(name);
    if (found != null) {
      return found;
    }
    int space = name.lastIndexOf(' ');
    if (space > 0 && name.length() - space - 1 <= 6) {
      String hex = name.substring(space + 1);
      boolean ok = !hex.isEmpty();
      for (int i = 0; i < hex.length() && ok; i++) {
        char c = hex.charAt(i);
        ok = (c >= '0' && c <= '9') || (c >= 'A' && c <= 'F');
      }
      if (ok) {
        int cp = Integer.parseInt(hex, 16);
        if (cp <= 0x10FFFF && name.equals(nameOf(cp))) {
          return cp;
        }
      }
    }
    return -1;
  }
}
