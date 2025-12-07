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

  private static String get(Locale in, String key) {
    return shim.LocaleDb.names().get(in, key);
  }

  static String language(Locale l, Locale in) {
    String code = l.getLanguage();
    if (code.isEmpty()) {
      return "";
    }
    String name = get(in, "n.L." + code);
    if (name == null && code.length() == 3) {
      String two = Locale.languageForIso3(code);
      name = two == null ? null : get(in, "n.L." + two);
    }
    return name != null ? name : code;
  }

  static String script(Locale l, Locale in) {
    String code = l.getScript();
    if (code.isEmpty()) {
      return "";
    }
    String name = get(in, "n.S." + code);
    return name != null ? name : code;
  }

  static String country(Locale l, Locale in) {
    String code = l.getCountry();
    if (code.isEmpty()) {
      return "";
    }
    String name = get(in, "n.R." + code);
    if (name == null && code.length() == 3 && !LocaleTags.isDigits(code)) {
      String two = Locale.countryForIso3(code);
      name = two == null ? null : get(in, "n.R." + two);
    }
    return name != null ? name : code;
  }

  private static String[] patterns(Locale in) {
    String[] parts = shim.LocaleDb.names().list(in, "n.P");
    return parts != null && parts.length == 4 ? parts : new String[] {"", " (", ")", ", "};
  }

  private static String compose(Locale in, String a, String b) {
    return a + patterns(in)[3] + b;
  }

  private static List<String> variantNames(Locale l, Locale in) {
    List<String> names = new ArrayList<String>();
    String variant = l.getVariant();
    if (!variant.isEmpty()) {
      for (String piece : variant.split("_", -1)) {
        String name = get(in, "n.V." + piece);
        names.add(name != null ? name : piece);
      }
    }
    return names;
  }

  static String variant(Locale l, Locale in) {
    String result = null;
    for (String name : variantNames(l, in)) {
      result = result == null ? name : compose(in, result, name);
    }
    return result == null ? "" : result;
  }

  private static String keywordName(String key, String type, Locale in) {
    if (key.equals("cu") && !type.isEmpty()) {
      String[] currency = shim.LocaleDb.currency().list(in, "c." + LocaleTags.upper(type));
      if (currency != null) {
        return typed(in, key, currency[1]);
      }
    }
    String typeName = type.isEmpty() ? null : get(in, "n.T." + key + "." + type);
    if (typeName != null) {
      return typeName;
    }
    return typed(in, key, type);
  }

  private static String typed(Locale in, String key, String type) {
    String template = get(in, "n.K." + key);
    if (template == null) {
      return key + ": " + type;
    }
    return template.replace("zzzz", type);
  }

  static String name(Locale l, Locale in) {
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
    parts.addAll(variantNames(l, in));
    LocaleExtensions ext = l.extensions();
    if (ext != null) {
      parts.addAll(ext.attributes);
      for (Map.Entry<String, String> kw : ext.keywords.entrySet()) {
        parts.add(keywordName(kw.getKey(), kw.getValue(), in));
      }
    }
    if (parts.isEmpty()) {
      return "";
    }
    String main = parts.get(0);
    if (parts.size() == 1) {
      return main;
    }
    String qualifiers = parts.get(1);
    for (int i = 2; i < parts.size(); i++) {
      qualifiers = compose(in, qualifiers, parts.get(i));
    }
    String[] pattern = patterns(in);
    return pattern[0] + main + pattern[1] + qualifiers + pattern[2];
  }
}
