/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util;

final class LocaleExtensions {
  final TreeMap<Character, String> others;
  final TreeSet<String> attributes;
  final TreeMap<String, String> keywords;
  final String privateUse;
  private final String id;

  LocaleExtensions(Map<Character, String> others, Set<String> attributes, Map<String, String> keywords,
      String privateUse) {
    this.others = new TreeMap<Character, String>(others);
    this.attributes = new TreeSet<String>(attributes);
    this.keywords = new TreeMap<String, String>(keywords);
    this.privateUse = privateUse == null || privateUse.isEmpty() ? null : privateUse;
    this.id = buildId();
  }

  static LocaleExtensions create(Map<Character, String> others, Set<String> attributes,
      Map<String, String> keywords, String privateUse) {
    LocaleExtensions e = new LocaleExtensions(others, attributes, keywords, privateUse);
    return e.isEmpty() ? null : e;
  }

  boolean isEmpty() {
    return others.isEmpty() && attributes.isEmpty() && keywords.isEmpty() && privateUse == null;
  }

  private String unicodeValue() {
    if (attributes.isEmpty() && keywords.isEmpty()) {
      return null;
    }
    StringBuilder sb = new StringBuilder();
    for (String attr : attributes) {
      if (sb.length() > 0) {
        sb.append('-');
      }
      sb.append(attr);
    }
    for (Map.Entry<String, String> kw : keywords.entrySet()) {
      if (sb.length() > 0) {
        sb.append('-');
      }
      sb.append(kw.getKey());
      if (!kw.getValue().isEmpty()) {
        sb.append('-').append(kw.getValue());
      }
    }
    return sb.toString();
  }

  private String buildId() {
    TreeMap<Character, String> all = new TreeMap<Character, String>(others);
    String u = unicodeValue();
    if (u != null) {
      all.put('u', u);
    }
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<Character, String> e : all.entrySet()) {
      if (sb.length() > 0) {
        sb.append('-');
      }
      sb.append(e.getKey()).append('-').append(e.getValue());
    }
    if (privateUse != null) {
      if (sb.length() > 0) {
        sb.append('-');
      }
      sb.append("x-").append(privateUse);
    }
    return sb.toString();
  }

  String id() {
    return id;
  }

  String get(char key) {
    key = Character.toLowerCase(key);
    if (key == 'x') {
      return privateUse;
    }
    if (key == 'u') {
      return unicodeValue();
    }
    return others.get(key);
  }

  Set<Character> keys() {
    TreeSet<Character> result = new TreeSet<Character>(others.keySet());
    if (unicodeValue() != null) {
      result.add('u');
    }
    if (privateUse != null) {
      result.add('x');
    }
    return Collections.unmodifiableSet(result);
  }

  static void addUnicode(List<String> tokens, Set<String> attributes, Map<String, String> keywords) {
    int i = 0;
    while (i < tokens.size() && tokens.get(i).length() != 2) {
      attributes.add(tokens.get(i));
      i++;
    }
    while (i < tokens.size()) {
      String key = tokens.get(i++);
      StringBuilder type = new StringBuilder();
      while (i < tokens.size() && tokens.get(i).length() != 2) {
        if (type.length() > 0) {
          type.append('-');
        }
        type.append(tokens.get(i++));
      }
      if (!keywords.containsKey(key)) {
        keywords.put(key, type.toString());
      }
    }
  }

  static String join(List<String> tokens, String sep) {
    StringBuilder sb = new StringBuilder();
    for (String t : tokens) {
      if (sb.length() > 0) {
        sb.append(sep);
      }
      sb.append(t);
    }
    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof LocaleExtensions && id.equals(((LocaleExtensions) o).id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
