/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util;

final class LocaleTags {
  private LocaleTags() { }

  static final class Parsed {
    String language = "";
    final List<String> extlangs = new ArrayList<String>();
    String script = "";
    String region = "";
    final List<String> variants = new ArrayList<String>();
    final TreeMap<Character, List<String>> extensions = new TreeMap<Character, List<String>>();
    final List<String> privateUse = new ArrayList<String>();
    String errorMessage;
    int errorIndex = -1;
  }

  private static final Map<String, String> kGrandfathered = new HashMap<String, String>();

  static {
    String[] pairs = {
        "art-lojban", "jbo", "cel-gaulish", "xtg-x-cel-gaulish", "en-gb-oed", "en-GB-x-oed", "i-ami", "ami",
        "i-bnn", "bnn", "i-default", "en-x-i-default", "i-enochian", "x-i-enochian", "i-hak", "hak",
        "i-klingon", "tlh", "i-lux", "lb", "i-mingo", "see-x-i-mingo", "i-navajo", "nv", "i-pwn", "pwn",
        "i-tao", "tao", "i-tay", "tay", "i-tsu", "tsu", "no-bok", "nb", "no-nyn", "nn", "sgn-be-fr", "sfb",
        "sgn-be-nl", "vgt", "sgn-ch-de", "sgg", "zh-guoyu", "cmn", "zh-hakka", "hak", "zh-min",
        "nan-x-zh-min", "zh-min-nan", "nan", "zh-xiang", "hsn"};
    for (int i = 0; i < pairs.length; i += 2) {
      kGrandfathered.put(pairs[i], pairs[i + 1]);
    }
  }

  static boolean isAlpha(String s) {
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'))) {
        return false;
      }
    }
    return true;
  }

  static boolean isDigits(String s) {
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c < '0' || c > '9') {
        return false;
      }
    }
    return true;
  }

  static boolean isAlphaNum(String s) {
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9'))) {
        return false;
      }
    }
    return true;
  }

  static boolean isLanguage(String s) {
    return s.length() >= 2 && s.length() <= 8 && isAlpha(s);
  }

  static boolean isExtlang(String s) {
    return s.length() == 3 && isAlpha(s);
  }

  static boolean isScript(String s) {
    return s.length() == 4 && isAlpha(s);
  }

  static boolean isRegion(String s) {
    return (s.length() == 2 && isAlpha(s)) || (s.length() == 3 && isDigits(s));
  }

  static boolean isVariant(String s) {
    int n = s.length();
    if (n >= 5 && n <= 8) {
      return isAlphaNum(s);
    }
    return n == 4 && s.charAt(0) >= '0' && s.charAt(0) <= '9' && isAlphaNum(s);
  }

  static boolean isExtensionSingleton(String s) {
    if (s.length() != 1) {
      return false;
    }
    char c = Character.toLowerCase(s.charAt(0));
    return c >= 'a' && c <= 'z' && c != 'x';
  }

  static boolean isExtensionSubtag(String s) {
    return s.length() >= 2 && s.length() <= 8 && isAlphaNum(s);
  }

  static boolean isPrivateUseSubtag(String s) {
    return s.length() >= 1 && s.length() <= 8 && isAlphaNum(s);
  }

  static boolean isUnicodeKey(String s) {
    if (s.length() != 2) {
      return false;
    }
    char a = s.charAt(0);
    char b = s.charAt(1);
    return ((a >= 'a' && a <= 'z') || (a >= 'A' && a <= 'Z') || (a >= '0' && a <= '9'))
        && ((b >= 'a' && b <= 'z') || (b >= 'A' && b <= 'Z'));
  }

  static boolean isUnicodeType(String s) {
    return s.length() >= 3 && s.length() <= 8 && isAlphaNum(s);
  }

  static String lower(String s) {
    return mapAscii(s, 'A', 'Z', 32);
  }

  static String upper(String s) {
    return mapAscii(s, 'a', 'z', -32);
  }

  private static String mapAscii(String s, char from, char to, int delta) {
    char[] chars = null;
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c >= from && c <= to) {
        if (chars == null) {
          chars = s.toCharArray();
        }
        chars[i] = (char) (c + delta);
      }
    }
    return chars == null ? s : new String(chars);
  }

  static String title(String s) {
    return s.isEmpty() ? s : upper(s.substring(0, 1)) + lower(s.substring(1));
  }

  static Parsed parse(String tag, boolean strict) {
    String replacement = kGrandfathered.get(lower(tag));
    if (replacement != null) {
      tag = replacement;
    }
    Parsed p = new Parsed();
    List<String> tokens = new ArrayList<String>();
    List<Integer> starts = new ArrayList<Integer>();
    int from = 0;
    while (true) {
      int dash = tag.indexOf('-', from);
      int end = dash < 0 ? tag.length() : dash;
      tokens.add(tag.substring(from, end));
      starts.add(from);
      if (dash < 0) {
        break;
      }
      from = dash + 1;
    }
    int n = tokens.size();
    int idx = 0;
    String first = tokens.get(0);
    if (first.isEmpty()) {
      return error(p, "Empty subtag", 0);
    }
    if (first.equalsIgnoreCase("x")) {
      return parsePrivateUse(p, tokens, starts, 0, n, strict);
    }
    if (!isLanguage(first)) {
      return error(p, "Invalid subtag: " + first, 0);
    }
    p.language = first;
    idx = 1;
    while (idx < n && p.extlangs.size() < 3 && isExtlang(tokens.get(idx))) {
      p.extlangs.add(tokens.get(idx++));
    }
    if (idx < n && isScript(tokens.get(idx))) {
      p.script = tokens.get(idx++);
    }
    if (idx < n && isRegion(tokens.get(idx))) {
      p.region = tokens.get(idx++);
    }
    while (idx < n && isVariant(tokens.get(idx))) {
      p.variants.add(tokens.get(idx++));
    }
    while (idx < n && isExtensionSingleton(tokens.get(idx))) {
      int start = starts.get(idx);
      char key = Character.toLowerCase(tokens.get(idx).charAt(0));
      idx++;
      List<String> value = new ArrayList<String>();
      while (idx < n && isExtensionSubtag(tokens.get(idx))) {
        value.add(lower(tokens.get(idx++)));
      }
      if (value.isEmpty()) {
        return error(p, "Incomplete extension '" + key + "'", start);
      }
      if (!p.extensions.containsKey(key)) {
        p.extensions.put(key, value);
      }
    }
    if (idx < n && tokens.get(idx).equalsIgnoreCase("x")) {
      return parsePrivateUse(p, tokens, starts, idx, n, strict);
    }
    if (idx < n) {
      return unexpected(p, tokens.get(idx), starts.get(idx));
    }
    return p;
  }

  private static Parsed unexpected(Parsed p, String token, int start) {
    return error(p, token.isEmpty() ? "Empty subtag" : "Invalid subtag: " + token, start);
  }

  private static Parsed parsePrivateUse(Parsed p, List<String> tokens, List<Integer> starts, int idx, int n,
      boolean strict) {
    int start = starts.get(idx);
    idx++;
    while (idx < n && isPrivateUseSubtag(tokens.get(idx))) {
      p.privateUse.add(tokens.get(idx++));
    }
    if (p.privateUse.isEmpty()) {
      return error(p, "Incomplete privateuse", start);
    }
    if (idx < n) {
      return unexpected(p, tokens.get(idx), starts.get(idx));
    }
    return p;
  }

  private static Parsed error(Parsed p, String message, int index) {
    p.errorMessage = message;
    p.errorIndex = index;
    return p;
  }

  static boolean isGrandfathered(String tag) {
    return kGrandfathered.containsKey(lower(tag));
  }

  static void checkStrict(Parsed p) {
    if (p.errorMessage != null) {
      throw new IllformedLocaleException(p.errorMessage, p.errorIndex);
    }
  }
}
