/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util;

final class LocaleNames {
  private LocaleNames() { }

  private static final Map<String, Map<String, String>> kTables = new HashMap<String, Map<String, String>>();

  private static synchronized Map<String, String> load(String tag) {
    Map<String, String> table = kTables.get(tag);
    if (table != null) {
      return table;
    }
    table = new HashMap<String, String>();
    String[] chunks = LocaleDisplayData.lines(tag);
    if (chunks != null) {
      for (String chunk : chunks) {
        int from = 0;
        while (from < chunk.length()) {
          int end = chunk.indexOf('\n', from);
          if (end < 0) {
            end = chunk.length();
          }
          int eq = chunk.indexOf('=', from);
          if (eq > from && eq < end) {
            table.put(chunk.substring(from, eq), chunk.substring(eq + 1, end));
          }
          from = end + 1;
        }
      }
    }
    kTables.put(tag, table);
    return table;
  }

  private static Map<String, String> table(Locale in) {
    String lang = in.getLanguage();
    String tag = lang;
    if (lang.equals("zh")) {
      String region = in.getCountry();
      boolean traditional = in.getScript().equals("Hant")
          || (in.getScript().isEmpty() && (region.equals("TW") || region.equals("HK") || region.equals("MO")));
      tag = traditional ? "zh-Hant" : "zh";
    }
    boolean known = false;
    for (String t : LocaleDisplayData.kTags) {
      if (t.equals(tag)) {
        known = true;
        break;
      }
    }
    return load(known ? tag : "en");
  }

  static String language(Locale l, Locale in) {
    String code = l.getLanguage();
    if (code.isEmpty()) {
      return "";
    }
    Map<String, String> t = table(in);
    String name = t.get("L" + code);
    if (name == null && code.length() == 3) {
      String two = Locale.languageForIso3(code);
      name = two == null ? null : t.get("L" + two);
    }
    return name != null ? name : code;
  }

  static String script(Locale l, Locale in) {
    String code = l.getScript();
    if (code.isEmpty()) {
      return "";
    }
    String name = table(in).get("S" + code);
    return name != null ? name : code;
  }

  static String country(Locale l, Locale in) {
    String code = l.getCountry();
    if (code.isEmpty()) {
      return "";
    }
    Map<String, String> t = table(in);
    String name = t.get("R" + code);
    if (name == null && code.length() == 3 && !LocaleTags.isDigits(code)) {
      String two = Locale.countryForIso3(code);
      name = two == null ? null : t.get("R" + two);
    }
    return name != null ? name : code;
  }

  private static String compose(Map<String, String> t, String a, String b) {
    String pattern = t.get("PListCompositionPattern");
    if (pattern == null) {
      pattern = "{0}, {1}";
    }
    return pattern.replace("{0}", a).replace("{1}", b);
  }

  private static List<String> variantNames(Locale l, Map<String, String> t) {
    List<String> names = new ArrayList<String>();
    String variant = l.getVariant();
    if (!variant.isEmpty()) {
      for (String piece : variant.split("_", -1)) {
        String name = t.get("V" + piece);
        names.add(name != null ? name : piece);
      }
    }
    return names;
  }

  static String currency(String code, Locale in) {
    return table(in).get("C" + code);
  }

  static String variant(Locale l, Locale in) {
    Map<String, String> t = table(in);
    String result = null;
    for (String name : variantNames(l, t)) {
      result = result == null ? name : compose(t, result, name);
    }
    return result == null ? "" : result;
  }

  private static String keywordName(Map<String, String> t, String key, String type, Locale in) {
    String keyName = t.get("K" + key);
    if (key.equals("cu") && !type.isEmpty()) {
      String currency = t.get("C" + LocaleTags.upper(type));
      if (currency != null) {
        return pattern(t, keyName != null ? keyName : key, currency);
      }
    }
    String typeName = type.isEmpty() ? null : t.get("T" + key + "." + type);
    if (typeName != null) {
      return typeName;
    }
    return pattern(t, keyName != null ? keyName : key, type);
  }

  private static String pattern(Map<String, String> t, String key, String type) {
    String pattern = t.get("PListKeyTypePattern");
    if (pattern == null) {
      pattern = "{0}: {1}";
    }
    return pattern.replace("{0}", key).replace("{1}", type);
  }

  static String name(Locale l, Locale in) {
    Map<String, String> t = table(in);
    if (l.getLanguage().isEmpty() && l.getScript().isEmpty() && l.getCountry().isEmpty()
        && l.getVariant().isEmpty()) {
      return "";
    }
    List<String> parts = new ArrayList<String>();
    if (!l.getLanguage().isEmpty()) {
      parts.add(language(l, in));
    }
    if (!l.getScript().isEmpty()) {
      parts.add(script(l, in));
    }
    if (!l.getCountry().isEmpty()) {
      parts.add(country(l, in));
    }
    parts.addAll(variantNames(l, t));
    LocaleExtensions ext = l.extensions();
    if (ext != null) {
      parts.addAll(ext.attributes);
      for (Map.Entry<String, String> kw : ext.keywords.entrySet()) {
        parts.add(keywordName(t, kw.getKey(), kw.getValue(), in));
      }
    }
    String main = parts.get(0);
    if (parts.size() == 1) {
      return main;
    }
    String qualifiers = parts.get(1);
    for (int i = 2; i < parts.size(); i++) {
      qualifiers = compose(t, qualifiers, parts.get(i));
    }
    String pattern = t.get("PDisplayNamePattern");
    String template = "{1} ({2})";
    if (pattern != null) {
      int two = pattern.indexOf("2#");
      if (two >= 0) {
        template = pattern.substring(two + 2, pattern.lastIndexOf('}'));
      } else {
        template = pattern;
      }
    }
    return template.replace("{1}", main).replace("{2}", qualifiers);
  }
}
