/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util;

import java.util.Locale.FilteringMode;
import java.util.Locale.LanguageRange;

final class LocaleMatcher {
  private LocaleMatcher() { }

  private static Map<String, String> kSingle;
  private static Map<String, String[]> kMulti;
  private static List<String[]> kRegionVariant;

  private static synchronized void load() {
    if (kSingle != null) {
      return;
    }
    Map<String, String> single = new HashMap<String, String>();
    for (String entry : LocaleEquivalentData.kSingleEquivMap) {
      int eq = entry.indexOf('=');
      single.put(entry.substring(0, eq), entry.substring(eq + 1));
    }
    Map<String, String[]> multi = new HashMap<String, String[]>();
    for (String entry : LocaleEquivalentData.kMultiEquivsMap) {
      int eq = entry.indexOf('=');
      multi.put(entry.substring(0, eq), entry.substring(eq + 1).split(" "));
    }
    List<String[]> regionVariant = new ArrayList<String[]>();
    for (String entry : LocaleEquivalentData.kRegionVariantEquivMap) {
      int eq = entry.indexOf('=');
      regionVariant.add(new String[] {entry.substring(0, eq), entry.substring(eq + 1)});
    }
    kMulti = multi;
    kRegionVariant = regionVariant;
    kSingle = single;
  }

  static boolean isValidRange(String range) {
    if (range.isEmpty()) {
      return false;
    }
    String[] subtags = range.split("-", -1);
    for (int i = 0; i < subtags.length; i++) {
      String s = subtags[i];
      if (s.equals("*")) {
        continue;
      }
      if (s.isEmpty() || s.length() > 8) {
        return false;
      }
      if (i == 0 ? !LocaleTags.isAlpha(s) : !LocaleTags.isAlphaNum(s)) {
        return false;
      }
    }
    return true;
  }

  static List<LanguageRange> parse(String ranges, Map<String, List<String>> map) {
    ranges = ranges.replace(" ", "").toLowerCase(Locale.ROOT);
    String[] pieces = ranges.split(",", -1);
    List<LanguageRange> list = new ArrayList<LanguageRange>();
    List<String> seen = new ArrayList<String>();
    for (String piece : pieces) {
      String range;
      double weight = 1.0;
      int q = piece.indexOf(";q=");
      if (q >= 0) {
        range = piece.substring(0, q);
        String w = piece.substring(q + 3);
        try {
          weight = Double.parseDouble(w);
        } catch (RuntimeException e) {
          throw new IllegalArgumentException("weight=\"" + w + "\" for language range \"" + range + "\"");
        }
        if (weight < 0.0 || weight > 1.0) {
          throw new IllegalArgumentException("weight=" + weight + " for language range \"" + range
              + "\". It must be between 0.0 and 1.0.");
        }
      } else {
        range = piece;
      }
      if (!isValidRange(range)) {
        throw new IllegalArgumentException("range=" + range);
      }
      if (!seen.contains(range)) {
        seen.add(range);
        list.add(new LanguageRange(range, weight));
        List<String> equivalents = equivalentsFor(range);
        for (String e : equivalents) {
          if (!seen.contains(e)) {
            seen.add(e);
            list.add(new LanguageRange(e, weight));
          }
        }
      }
    }
    List<LanguageRange> sorted = new ArrayList<LanguageRange>(list);
    Collections.sort(sorted, new Comparator<LanguageRange>() {
      public int compare(LanguageRange a, LanguageRange b) {
        return Double.compare(b.getWeight(), a.getWeight());
      }
    });
    if (map != null) {
      return mapEquivalents(sorted, map);
    }
    return sorted;
  }

  private static List<String> equivalentsFor(String range) {
    load();
    List<String> result = new ArrayList<String>();
    List<String> language = languageEquivalents(range);
    if (language != null) {
      result.addAll(language);
    }
    String rv = regionVariantEquivalent(range);
    if (rv != null) {
      result.add(rv);
    }
    if (language != null) {
      for (String l : language) {
        String lrv = regionVariantEquivalent(l);
        if (lrv != null) {
          result.add(lrv);
        }
      }
    }
    return result;
  }

  private static List<String> languageEquivalents(String range) {
    String r = range;
    while (!r.isEmpty()) {
      String single = kSingle.get(r);
      if (single != null) {
        List<String> out = new ArrayList<String>();
        out.add(replaceFirst(range, r, single));
        return out;
      }
      String[] multi = kMulti.get(r);
      if (multi != null) {
        List<String> out = new ArrayList<String>();
        for (int i = multi.length - 1; i >= 0; i--) {
          out.add(replaceFirst(range, r, multi[i]));
        }
        return out;
      }
      int index = r.lastIndexOf('-');
      if (index < 0) {
        break;
      }
      r = r.substring(0, index);
    }
    return null;
  }

  private static String replaceFirst(String range, String target, String replacement) {
    int index = range.indexOf(target);
    if (index < 0) {
      return range;
    }
    return range.substring(0, index) + replacement + range.substring(index + target.length());
  }

  private static int extensionKeyIndex(String range) {
    int length = range.length();
    int index = -1;
    String[] subtags = range.split("-", -1);
    int pos = 0;
    for (int i = 0; i < subtags.length; i++) {
      if (i > 0 && subtags[i].length() == 1) {
        index = pos;
        break;
      }
      pos += subtags[i].length() + 1;
    }
    return index < 0 || index > length ? Integer.MIN_VALUE : index;
  }

  private static String regionVariantEquivalent(String range) {
    int extension = extensionKeyIndex(range);
    for (String[] entry : kRegionVariant) {
      String subtag = entry[0];
      int index = range.indexOf(subtag);
      if (index != -1) {
        if (extension != Integer.MIN_VALUE && index > extension) {
          continue;
        }
        int len = index + subtag.length();
        if (range.length() == len || range.charAt(len) == '-') {
          return replaceFirst(range, subtag, entry[1]);
        }
      }
    }
    return null;
  }

  static List<LanguageRange> mapEquivalents(List<LanguageRange> priorityList, Map<String, List<String>> map) {
    if (priorityList.isEmpty()) {
      return new ArrayList<LanguageRange>();
    }
    if (map == null || map.isEmpty()) {
      return new ArrayList<LanguageRange>(priorityList);
    }
    Map<String, List<String>> lowered = new HashMap<String, List<String>>();
    for (Map.Entry<String, List<String>> e : map.entrySet()) {
      lowered.put(e.getKey().toLowerCase(Locale.ROOT), e.getValue());
    }
    List<LanguageRange> result = new ArrayList<LanguageRange>();
    for (LanguageRange lr : priorityList) {
      String range = lr.getRange();
      String r = range;
      boolean hit = false;
      while (!r.isEmpty()) {
        if (lowered.containsKey(r)) {
          hit = true;
          List<String> equivalents = lowered.get(r);
          if (equivalents != null) {
            int len = r.length();
            for (String equivalent : equivalents) {
              String mapped = equivalent.toLowerCase(Locale.ROOT) + range.substring(len);
              add(result, new LanguageRange(mapped, lr.getWeight()));
            }
          }
          break;
        }
        int index = r.lastIndexOf('-');
        if (index == -1) {
          break;
        }
        r = r.substring(0, index);
      }
      if (!hit) {
        add(result, lr);
      }
    }
    return result;
  }

  private static void add(List<LanguageRange> list, LanguageRange lr) {
    for (LanguageRange existing : list) {
      if (existing.getRange().equals(lr.getRange())) {
        return;
      }
    }
    list.add(lr);
  }

  private static String truncate(String range) {
    int index = range.lastIndexOf('-');
    if (index < 0) {
      return "";
    }
    String r = range.substring(0, index);
    if (r.length() >= 2 && r.charAt(r.length() - 2) == '-') {
      r = r.substring(0, r.length() - 2);
    }
    return r;
  }

  static String lookupTag(List<LanguageRange> priorityList, Collection<String> tags) {
    if (priorityList.isEmpty() || tags.isEmpty()) {
      return null;
    }
    List<String> original = new ArrayList<String>(tags);
    List<String> lower = new ArrayList<String>();
    for (String t : original) {
      lower.add(t.toLowerCase(Locale.ROOT));
    }
    for (LanguageRange lr : priorityList) {
      if (lr.getWeight() == 0.0) {
        continue;
      }
      String range = lr.getRange();
      if (range.equals("*")) {
        continue;
      }
      String regex = range.replace("*", "\\p{Alnum}*");
      while (!regex.isEmpty()) {
        for (int i = 0; i < lower.size(); i++) {
          if (lower.get(i).matches(regex)) {
            return original.get(i);
          }
        }
        regex = truncate(regex);
      }
    }
    return null;
  }

  static Locale lookup(List<LanguageRange> priorityList, Collection<Locale> locales) {
    if (priorityList.isEmpty() || locales.isEmpty()) {
      return null;
    }
    List<Locale> list = new ArrayList<Locale>(locales);
    List<String> tags = new ArrayList<String>();
    for (Locale l : list) {
      tags.add(l.toLanguageTag());
    }
    String tag = lookupTag(priorityList, tags);
    if (tag == null) {
      return null;
    }
    return list.get(tags.indexOf(tag));
  }

  private static boolean isExtendedRange(String range) {
    return range.indexOf('*') >= 0 && !range.equals("*");
  }

  static List<String> filterTags(List<LanguageRange> priorityList, Collection<String> tags, FilteringMode mode) {
    if (priorityList.isEmpty() || tags.isEmpty()) {
      return new ArrayList<String>();
    }
    List<LanguageRange> ranges = new ArrayList<LanguageRange>();
    boolean extended = false;
    if (mode == FilteringMode.EXTENDED_FILTERING) {
      extended = true;
    }
    for (LanguageRange lr : priorityList) {
      String range = lr.getRange();
      if (isExtendedRange(range)) {
        if (mode == FilteringMode.REJECT_EXTENDED_RANGES) {
          throw new IllegalArgumentException("An extended range \"" + range
              + "\" found in REJECT_EXTENDED_RANGES mode.");
        } else if (mode == FilteringMode.IGNORE_EXTENDED_RANGES) {
          continue;
        } else if (mode == FilteringMode.MAP_EXTENDED_RANGES) {
          ranges.add(new LanguageRange(mapExtended(range), lr.getWeight()));
          continue;
        }
        extended = true;
      }
      ranges.add(lr);
    }
    if (mode == null || mode == FilteringMode.AUTOSELECT_FILTERING) {
      // extended only when an extended range is present
    } else if (mode != FilteringMode.EXTENDED_FILTERING) {
      extended = false;
    }
    List<String> result = new ArrayList<String>();
    List<String> seen = new ArrayList<String>();
    for (LanguageRange lr : ranges) {
      if (lr.getWeight() == 0.0) {
        continue;
      }
      String range = lr.getRange();
      for (String tag : tags) {
        String lowerTag = tag.toLowerCase(Locale.ROOT);
        boolean match = range.equals("*") || (extended ? extendedMatch(range, lowerTag) : basicMatch(range, lowerTag));
        if (match && !seen.contains(lowerTag)) {
          seen.add(lowerTag);
          result.add(tag);
        }
      }
    }
    return result;
  }

  private static String mapExtended(String range) {
    if (range.startsWith("*")) {
      return "*";
    }
    StringBuilder sb = new StringBuilder();
    for (String subtag : range.split("-")) {
      if (!subtag.equals("*")) {
        sb.append(sb.length() > 0 ? "-" : "").append(subtag);
      }
    }
    return sb.toString();
  }

  private static boolean basicMatch(String range, String tag) {
    return tag.equals(range) || tag.startsWith(range + "-");
  }

  private static boolean extendedMatch(String range, String tag) {
    String[] r = range.split("-");
    String[] t = tag.split("-");
    if (!r[0].equals("*") && !r[0].equals(t[0])) {
      return false;
    }
    int ri = 1;
    int ti = 1;
    while (ri < r.length) {
      if (r[ri].equals("*")) {
        ri++;
        continue;
      }
      if (ti >= t.length) {
        return false;
      }
      if (r[ri].equals(t[ti])) {
        ri++;
        ti++;
      } else if (t[ti].length() == 1) {
        return false;
      } else {
        ti++;
      }
    }
    return true;
  }

  static List<Locale> filter(List<LanguageRange> priorityList, Collection<Locale> locales, FilteringMode mode) {
    if (priorityList.isEmpty() || locales.isEmpty()) {
      return new ArrayList<Locale>();
    }
    List<Locale> list = new ArrayList<Locale>(locales);
    List<String> tags = new ArrayList<String>();
    for (Locale l : list) {
      tags.add(l.toLanguageTag());
    }
    List<String> filtered = filterTags(priorityList, tags, mode);
    List<Locale> result = new ArrayList<Locale>();
    for (String tag : filtered) {
      result.add(list.get(tags.indexOf(tag)));
    }
    return result;
  }
}
