/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.text;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CollationTable {
  private static final class Entry {
    final String key;
    final int level;
    final String expansion;

    Entry(String key, int level, String expansion) {
      this.key = key;
      this.level = level;
      this.expansion = expansion;
    }
  }

  private final Map<String, int[]> elements = new HashMap<String, int[]>();
  private final Map<Character, Integer> contractionLengths = new HashMap<Character, Integer>();
  private final Map<String, String> expansions = new HashMap<String, String>();
  private int unmappedBase;
  private final boolean decomposeComposed;
  private static String decomposable;

  private CollationTable(boolean decomposeComposed) {
    this.decomposeComposed = decomposeComposed;
  }

  private static final CollationTable kDefault = buildDefault();

  public static CollationTable defaultTable() {
    return kDefault;
  }

  private static final Map<String, CollationTable> kLocale = new HashMap<String, CollationTable>();
  private boolean frenchSecondary;
  private boolean thaiPrefixVowels;

  public boolean frenchSecondary() {
    return frenchSecondary;
  }

  public static synchronized CollationTable forLanguage(String language) {
    String rules = CollationRules.forLanguage(language);
    if (rules == null) {
      String full = CollationRulesCjk.forLanguage(language);
      if (full != null) {
        CollationTable t = kLocale.get(full);
        if (t == null) {
          try {
            t = parse(full, true);
          } catch (ParseException e) {
            throw new IllegalStateException(e);
          }
          kLocale.put(full, t);
        }
        return t;
      }
    }
    if (rules == null) {
      return kDefault;
    }
    CollationTable t = kLocale.get(rules);
    if (t == null) {
      t = buildDefault(rules);
      kLocale.put(rules, t);
    }
    return t;
  }

  private static CollationTable buildDefault() {
    return buildDefault("");
  }

  private static CollationTable buildDefault(String delta) {
    CollationTable t = new CollationTable(true);
    List<Entry> list = new ArrayList<Entry>();
    String markChars = CollationData.decode(CollationData.MARK_CHARS);
    String baseChars = CollationData.decode(CollationData.BASE_CHARS);
    for (int i = 0; i < markChars.length(); i++) {
      list.add(new Entry(String.valueOf(markChars.charAt(i)), level(CollationData.MARK_LEVELS.charAt(i)), null));
    }
    for (int i = 0; i < baseChars.length(); i++) {
      list.add(new Entry(String.valueOf(baseChars.charAt(i)), level(CollationData.BASE_LEVELS.charAt(i)), null));
    }
    for (boolean flags = true; flags;) {
      if (delta.startsWith("@")) {
        t.frenchSecondary = true;
        delta = delta.substring(1);
      } else if (delta.startsWith("!")) {
        t.thaiPrefixVowels = true;
        delta = delta.substring(1);
      } else {
        flags = false;
      }
    }
    java.util.Set<String> overridden = new java.util.HashSet<String>();
    if (!delta.trim().isEmpty()) {
      try {
        applyRules(list, delta, new ArrayList<String>(), true);
      } catch (ParseException e) {
        throw new IllegalStateException(e);
      }
      for (Entry e : list) {
        overridden.add(e.key);
      }
    }
    t.assign(list);
    String[][] ligatures = {{"\u00e6", "a", "e", "2"}, {"\u00c6", "a", "e", "3"}, {"\u00fe", "t", "h", "2"},
        {"\u00de", "t", "h", "3"}, {"\u0153", "o", "e", "2"}, {"\u0152", "o", "e", "3"}, {"\u00df", "s", "s", "2"}};
    for (String[] l : ligatures) {
      if (overridden.contains(l[0])) {
        continue;
      }
      int[] first = t.elements.get(l[1]);
      int[] second = t.elements.get(l[2]);
      t.elements.put(l[0], new int[] {first[0], first[1], Integer.parseInt(l[3]), second[0], second[1], 1});
    }
    String ignorable = CollationData.decode(CollationData.IGNORABLE);
    for (int i = 0; i < ignorable.length(); i++) {
      if (overridden.contains(String.valueOf(ignorable.charAt(i)))) {
        continue;
      }
      t.elements.put(String.valueOf(ignorable.charAt(i)), new int[0]);
    }
    return t;
  }

  private static int level(char c) {
    return c == '=' ? 0 : c - '0';
  }

  public static CollationTable parse(String rules) throws ParseException {
    return parse(rules, false);
  }

  private static CollationTable parse(String rules, boolean decomposeComposed) throws ParseException {
    CollationTable t = new CollationTable(decomposeComposed);
    List<Entry> list = new ArrayList<Entry>();
    List<String> implicit = new ArrayList<String>();
    boolean haveAny = applyRules(list, rules, implicit, false);
    if (!haveAny && list.isEmpty()) {
      throw new ParseException("Build rules empty.", 0);
    }
    List<Entry> all = new ArrayList<Entry>();
    for (String s : implicit) {
      if (indexOf(list, s) < 0) {
        all.add(new Entry(s, 1, null));
      }
    }
    all.addAll(list);
    t.assign(all);
    return t;
  }

  private static boolean applyRules(List<Entry> list, String rules, List<String> implicit, boolean replace)
      throws ParseException {
    int n = rules.length();
    int i = 0;
    int cursor = -1;
    boolean haveAny = false;
    while (true) {
      i = skip(rules, i);
      if (i >= n) {
        break;
      }
      char c = rules.charAt(i);
      if (!isRelation(c)) {
        throw new ParseException("missing char (=,;<&) : " + rules.substring(i), i);
      }
      i++;
      int after = skip(rules, i);
      if (after >= n || isRelation(rules.charAt(after))) {
        throw new ParseException("missing chars (=,;<&): " + rules.substring(after), after);
      }
      StringBuilder text = new StringBuilder();
      i = readText(rules, after, text);
      String expansion = null;
      int slash = skip(rules, i);
      if (slash < n && rules.charAt(slash) == '/') {
        StringBuilder ex = new StringBuilder();
        i = readText(rules, skip(rules, slash + 1), ex);
        expansion = ex.toString();
      }
      String key = text.toString();
      if (c == '&') {
        cursor = indexOf(list, key);
        if (cursor < 0 && key.length() > 1 && replace) {
          int last = indexOf(list, key.substring(key.length() - 1));
          if (last >= 0) {
            while (last + 1 < list.size() && list.get(last + 1).level > 1) {
              last++;
            }
            list.add(last + 1, new Entry(key, 1, null));
            cursor = last + 1;
          }
        }
        if (cursor < 0) {
          list.add(new Entry(key, 1, null));
          cursor = list.size() - 1;
        }
        while (cursor + 1 < list.size() && list.get(cursor + 1).level > 1) {
          cursor++;
        }
        continue;
      }
      int lv = c == '<' ? 1 : c == ';' ? 2 : c == ',' ? 3 : 0;
      Entry e = new Entry(key, lv, expansion);
      if (replace) {
        int old = indexOf(list, key);
        if (old >= 0) {
          list.remove(old);
          if (old <= cursor) {
            cursor--;
          }
        }
      }
      if (cursor >= 0) {
        list.add(cursor + 1, e);
        cursor++;
      } else {
        list.add(e);
      }
      if (key.length() > 1) {
        for (int k = 0; k < key.length(); k++) {
          String single = String.valueOf(key.charAt(k));
          if (!implicit.contains(single)) {
            implicit.add(single);
          }
        }
      }
      haveAny = true;
    }
    return haveAny;
  }

  private static int indexOf(List<Entry> list, String key) {
    for (int i = 0; i < list.size(); i++) {
      if (list.get(i).key.equals(key)) {
        return i;
      }
    }
    return -1;
  }

  private static boolean isRelation(char c) {
    return c == '<' || c == ';' || c == ',' || c == '=' || c == '&';
  }

  private static boolean isRuleSpace(char c) {
    return c == ' ' || (c >= 0x09 && c <= 0x0d);
  }

  private static int skip(String s, int i) {
    while (i < s.length() && isRuleSpace(s.charAt(i))) {
      i++;
    }
    return i;
  }

  private static int readText(String s, int i, StringBuilder out) {
    int n = s.length();
    while (i < n) {
      char c = s.charAt(i);
      if (c == '\'' && i + 2 < n && s.charAt(i + 1) == '\'' && s.charAt(i + 2) == '\'') {
        out.append('\'');
        i += 3;
        continue;
      }
      if (c == '\'') {
        i++;
        while (i < n) {
          char q = s.charAt(i);
          if (q == '\'') {
            if (i + 1 < n && s.charAt(i + 1) == '\'') {
              out.append('\'');
              i += 2;
              continue;
            }
            i++;
            break;
          }
          out.append(q);
          i++;
        }
        continue;
      }
      if (isRuleSpace(c) || isRelation(c) || c == '/') {
        break;
      }
      out.append(c);
      i++;
    }
    return i;
  }

  private void assign(List<Entry> list) {
    int p = 0;
    int s = 0;
    int tt = 0;
    int maxP = 0;
    for (Entry e : list) {
      switch (e.level) {
      case 1:
        p++;
        s = 0;
        tt = 0;
        break;
      case 2:
        s++;
        tt = 0;
        break;
      case 3:
        tt++;
        break;
      default:
        break;
      }
      maxP = Math.max(maxP, p);
      boolean ignorable = p == 0 && s == 0 && tt == 0;
      int[] el = ignorable ? new int[0] : new int[] {p, s, tt};
      elements.put(e.key, el);
      if (e.key.length() > 1) {
        Integer old = contractionLengths.get(e.key.charAt(0));
        if (old == null || old < e.key.length()) {
          contractionLengths.put(e.key.charAt(0), e.key.length());
        }
      }
      if (e.expansion != null) {
        expansions.put(e.key, e.expansion);
      }
    }
    unmappedBase = maxP + 1;
  }

  private static boolean isPrefixVowel(char c) {
    return (c >= 0x0e40 && c <= 0x0e44) || (c >= 0x0ec0 && c <= 0x0ec4);
  }

  private static boolean isBaseConsonant(char c) {
    return (c >= 0x0e01 && c <= 0x0e2e) || (c >= 0x0e81 && c <= 0x0eae);
  }

  private static String swapPrefixVowels(String text) {
    char[] a = text.toCharArray();
    for (int i = 0; i + 1 < a.length; i++) {
      if (isPrefixVowel(a[i]) && isBaseConsonant(a[i + 1])) {
        char c = a[i];
        a[i] = a[i + 1];
        a[i + 1] = c;
        i++;
      }
    }
    return new String(a);
  }

  public int[] elementsOf(String text, int[] offsets, int[] count) {
    if (thaiPrefixVowels) {
      text = swapPrefixVowels(text);
    }
    int n = text.length();
    int[] out = new int[(n + 2) * 6];
    int[] src = new int[out.length / 3];
    int k = 0;
    int i = 0;
    while (i < n) {
      int used = 1;
      String key = null;
      Integer max = contractionLengths.get(text.charAt(i));
      if (max != null) {
        for (int len = Math.min(max, n - i); len > 1; len--) {
          String cand = text.substring(i, i + len);
          if (elements.containsKey(cand)) {
            key = cand;
            used = len;
            break;
          }
        }
      }
      if (key == null) {
        key = text.substring(i, i + 1);
      }
      int[] el = lookup(key);
      String exp = expansions.get(key);
      int[] extra = exp == null ? new int[0] : elementsOf(exp, null, null);
      int need = el.length + extra.length;
      if (k * 3 + need > out.length) {
        out = java.util.Arrays.copyOf(out, Math.max(out.length * 2, k * 3 + need));
        src = java.util.Arrays.copyOf(src, out.length / 3);
      }
      for (int j = 0; j < el.length; j += 3) {
        src[k] = i + used;
        System.arraycopy(el, j, out, k * 3, 3);
        k++;
      }
      for (int j = 0; j < extra.length; j += 3) {
        src[k] = i + used;
        System.arraycopy(extra, j, out, k * 3, 3);
        k++;
      }
      i += used;
    }
    if (offsets != null) {
      System.arraycopy(src, 0, offsets, 0, Math.min(k, offsets.length));
    }
    if (count != null) {
      count[0] = k;
    }
    return java.util.Arrays.copyOf(out, k * 3);
  }

  private static synchronized boolean isDecomposable(char c) {
    if (decomposable == null) {
      decomposable = CollationData.decode(CollationData.DECOMPOSABLE);
    }
    return decomposable.indexOf(c) >= 0;
  }

  private int[] lookup(String key) {
    int[] el = elements.get(key);
    if (el != null) {
      return el;
    }
    if (key.length() == 1) {
      char c = key.charAt(0);
      if (decomposeComposed && isDecomposable(c) && !(c == '\u00c5' && elements.containsKey("\u212b"))) {
        String d = java.text.Normalizer.normalize(key, java.text.Normalizer.Form.NFD);
        if (!d.equals(key)) {
          return elementsOf(d, null, null);
        }
      }
      return new int[] {0x7fff, 0, 0, c, 0, 0};
    }
    return new int[0];
  }
}
