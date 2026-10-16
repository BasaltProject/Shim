/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class ListFormat extends Format {
  private static final long serialVersionUID = 6738158845788457619L;

  private static final int kStart = 0;
  private static final int kMiddle = 1;
  private static final int kEnd = 2;
  private static final int kTwo = 3;
  private static final int kThree = 4;

  private final Locale locale;
  private final String[] patterns;

  public enum Type {
    STANDARD,
    OR,
    UNIT
  }

  public enum Style {
    FULL,
    SHORT,
    NARROW
  }

  private static final class Data {
    static final Map<String, int[]> kStored = new HashMap<String, int[]>();
    static final String[][] kPatternSets = new String[ListFormatData.kPatterns.length][];
    static Locale[] kAvailable;

    static {
      for (int i = 0; i < kPatternSets.length; i++) {
        kPatternSets[i] = ListFormatData.kPatterns[i].split("\u0001", -1);
      }
      for (String line : ListFormatData.kLocales) {
        int eq = line.indexOf('=');
        String[] parts = line.substring(eq + 1).split(",");
        int[] ids = new int[9];
        for (int i = 0; i < 9; i++) {
          ids[i] = Integer.parseInt(parts[i]);
        }
        kStored.put(line.substring(0, eq), ids);
      }
    }

    static synchronized Locale[] available() {
      if (kAvailable == null) {
        String[] tags = ListFormatData.kAvailable.split(" ");
        Locale[] result = new Locale[tags.length];
        for (int i = 0; i < tags.length; i++) {
          result[i] = tags[i].equals("und") ? Locale.ROOT : Locale.forLanguageTag(tags[i]);
        }
        kAvailable = result;
      }
      return kAvailable.clone();
    }

    static String[] lookup(Locale locale, Type type, Style style) {
      String lang = locale.getLanguage();
      String script = locale.getScript();
      String region = locale.getCountry();
      List<String> chain = new ArrayList<String>();
      if (!lang.isEmpty()) {
        if (!region.isEmpty()) {
          chain.add(script.isEmpty() ? lang + "-" + region : lang + "-" + script + "-" + region);
        }
        if (!script.isEmpty()) {
          chain.add(lang + "-" + script);
        }
        chain.add(lang);
      }
      chain.add("und");
      for (String tag : chain) {
        int[] ids = kStored.get(tag);
        if (ids != null) {
          return kPatternSets[ids[type.ordinal() * 3 + style.ordinal()]].clone();
        }
      }
      throw new InternalError("missing root list patterns");
    }
  }

  private ListFormat(Locale locale, String[] patterns) {
    this.locale = locale;
    this.patterns = patterns;
  }

  public static Locale[] getAvailableLocales() {
    return Data.available();
  }

  public static ListFormat getInstance() {
    return getInstance(Locale.getDefault(Locale.Category.FORMAT), Type.STANDARD, Style.FULL);
  }

  public static ListFormat getInstance(Locale locale, Type type, Style style) {
    Objects.requireNonNull(locale);
    Objects.requireNonNull(type);
    Objects.requireNonNull(style);
    return new ListFormat(locale, Data.lookup(locale, type, style));
  }

  public static ListFormat getInstance(String[] patterns) {
    Objects.requireNonNull(patterns);
    if (patterns.length != 5) {
      throw new IllegalArgumentException("Pattern array length should be 5");
    }
    for (String p : patterns) {
      if (p == null) {
        throw new IllegalArgumentException("patterns array contains one or more null elements");
      }
    }
    String[] names = {"start pattern", "middle pattern", "end pattern", "pattern for two", "pattern for three"};
    for (int i = 0; i < 5; i++) {
      if (!valid(patterns[i], i == kThree)) {
        throw new IllegalArgumentException(names[i] + " is incorrect: " + patterns[i]);
      }
    }
    return new ListFormat(Locale.ROOT, patterns.clone());
  }

  private static boolean valid(String p, boolean three) {
    int zero = p.indexOf("{0}");
    if (zero < 0) {
      return false;
    }
    if (three) {
      int one = p.indexOf("{1}", zero + 3);
      return one >= 0 && p.indexOf("{2}", one + 3) >= 0;
    }
    return p.endsWith("{1}") && p.length() - 3 >= zero + 3;
  }

  public Locale getLocale() {
    return locale;
  }

  public String[] getPatterns() {
    return patterns.clone();
  }

  public String format(List<String> objs) {
    return format((Object) objs, new StringBuffer(), new FieldPosition(0)).toString();
  }

  @Override
  public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
    Objects.requireNonNull(obj);
    Objects.requireNonNull(toAppendTo);
    Objects.requireNonNull(pos);
    Object[] items = items(obj, "The object to format should be a List<Object> or an Object[]");
    return toAppendTo.append(join(items));
  }

  private static Object[] items(Object obj, String message) {
    if (obj instanceof List) {
      return ((List<?>) obj).toArray();
    } else if (obj instanceof Object[]) {
      return (Object[]) obj;
    }
    throw new IllegalArgumentException(message);
  }

  private static String apply(String pattern, String a, String b) {
    int zero = pattern.indexOf("{0}");
    int one = pattern.lastIndexOf("{1}");
    return pattern.substring(0, zero) + a + pattern.substring(zero + 3, one) + b + pattern.substring(one + 3);
  }

  private String applyThree(String a, String b, String c) {
    String p = patterns[kThree];
    int zero = p.indexOf("{0}");
    int one = p.indexOf("{1}", zero + 3);
    int two = p.indexOf("{2}", one + 3);
    return p.substring(0, zero) + a + p.substring(zero + 3, one) + b + p.substring(one + 3, two) + c
        + p.substring(two + 3);
  }

  private String join(Object[] items) {
    int n = items.length;
    if (n == 0) {
      throw new IllegalArgumentException("There should at least be one input string");
    }
    String[] s = new String[n];
    for (int i = 0; i < n; i++) {
      s[i] = String.valueOf(items[i]);
    }
    if (n == 1) {
      return s[0];
    } else if (n == 2) {
      return apply(patterns[kTwo], s[0], s[1]);
    } else if (n == 3) {
      return applyThree(s[0], s[1], s[2]);
    }
    String result = apply(patterns[kEnd], s[n - 2], s[n - 1]);
    for (int i = n - 3; i >= 1; i--) {
      result = apply(patterns[kMiddle], s[i], result);
    }
    return apply(patterns[kStart], s[0], result);
  }

  private static String separator(String pattern) {
    int zero = pattern.indexOf("{0}");
    int one = pattern.lastIndexOf("{1}");
    return pattern.substring(zero + 3, one);
  }

  public List<String> parse(String source) throws ParseException {
    Objects.requireNonNull(source);
    return parseText(source);
  }

  private List<String> parseText(String text) {
    List<String> result = new ArrayList<String>();
    if (text.isEmpty()) {
      return result;
    }
    String endSep = separator(patterns[kEnd]);
    String twoSep = separator(patterns[kTwo]);
    int at = endSep.isEmpty() ? -1 : text.lastIndexOf(endSep);
    String sep = endSep;
    if (at < 0) {
      sep = twoSep;
      at = twoSep.isEmpty() ? -1 : text.lastIndexOf(twoSep);
    }
    if (at >= 0) {
      splitHead(text.substring(0, at), result);
      result.add(text.substring(at + sep.length()));
    } else {
      splitHead(text, result);
    }
    return result;
  }

  private void splitHead(String s, List<String> out) {
    String startSep = separator(patterns[kStart]);
    String middleSep = separator(patterns[kMiddle]);
    int i = startSep.isEmpty() ? -1 : s.indexOf(startSep);
    if (i < 0) {
      out.add(s);
      return;
    }
    out.add(s.substring(0, i));
    String rest = s.substring(i + startSep.length());
    for (;;) {
      int j = middleSep.isEmpty() ? -1 : rest.indexOf(middleSep);
      if (j < 0) {
        out.add(rest);
        return;
      }
      out.add(rest.substring(0, j));
      rest = rest.substring(j + middleSep.length());
    }
  }

  @Override
  public Object parseObject(String source, ParsePosition parsePos) {
    Objects.requireNonNull(source);
    Objects.requireNonNull(parsePos);
    int from = parsePos.getIndex();
    if (from < 0 || from > source.length()) {
      parsePos.setErrorIndex(from);
      return null;
    }
    List<String> parsed = parseText(source.substring(from));
    parsePos.setIndex(source.length());
    return parsed;
  }

  @Override
  public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
    Objects.requireNonNull(obj);
    Object[] items = items(obj, "The arguments should be a List<Object> or an Object[]");
    String text = join(items);
    AttributedString as = new AttributedString(text);
    int from = 0;
    for (int i = 0; i < items.length; i++) {
      String element = String.valueOf(items[i]);
      int at = text.indexOf(element, from);
      if (at >= 0 && !element.isEmpty()) {
        as.addAttribute(MessageFormat.Field.ARGUMENT, Integer.valueOf(i), at, at + element.length());
        from = at + element.length();
      }
    }
    return as.getIterator();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ListFormat)) {
      return false;
    }
    ListFormat other = (ListFormat) o;
    return locale.equals(other.locale) && Arrays.equals(patterns, other.patterns);
  }

  @Override
  public int hashCode() {
    return 31 * locale.hashCode() + Arrays.hashCode(patterns);
  }

  @Override
  public String toString() {
    return "ListFormat [locale: \"" + locale.getDisplayName() + "\", start: \"" + patterns[kStart]
        + "\", middle: \"" + patterns[kMiddle] + "\", end: \"" + patterns[kEnd] + "\", two: \""
        + patterns[kTwo] + "\", three: \"" + patterns[kThree] + "\"]\n";
  }
}
