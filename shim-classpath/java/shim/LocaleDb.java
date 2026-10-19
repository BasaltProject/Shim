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
import java.util.Base64;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public final class LocaleDb {
  private static final char kSeparator = '\u001f';

  private static Map<String, String> blocks;
  private static final Map<String, Map<String, String[]>> kResolved = new HashMap<String, Map<String, String[]>>();
  private static final Map<String, Map<String, String>> kParsed = new HashMap<String, Map<String, String>>();

  private LocaleDb() { }

  private static synchronized void load() {
    if (blocks != null) {
      return;
    }
    StringBuilder encoded = new StringBuilder();
    for (String chunk : LocaleDbData.kChunks) {
      encoded.append(chunk);
    }
    String text;
    try {
      byte[] gz = Base64.getDecoder().decode(encoded.toString());
      InputStream in = new GZIPInputStream(new ByteArrayInputStream(gz));
      ByteArrayOutputStream out = new ByteArrayOutputStream(1 << 20);
      byte[] buffer = new byte[16384];
      int n;
      while ((n = in.read(buffer)) > 0) {
        out.write(buffer, 0, n);
      }
      text = new String(out.toByteArray(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
    Map<String, String> map = new HashMap<String, String>();
    int pos = 0;
    int length = text.length();
    while (pos < length) {
      int lineEnd = text.indexOf('\n', pos);
      String tag = text.substring(pos + 1, lineEnd);
      int next = text.indexOf("\n@", lineEnd);
      int end = next < 0 ? length : next + 1;
      map.put(tag, text.substring(lineEnd + 1, end));
      pos = end;
    }
    blocks = map;
  }

  private static String unescape(String value) {
    if (value.indexOf('\\') < 0) {
      return value;
    }
    StringBuilder sb = new StringBuilder(value.length());
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      if (c == '\\' && i + 1 < value.length()) {
        char d = value.charAt(++i);
        sb.append(d == 'n' ? '\n' : d == 't' ? '\t' : d);
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  private static synchronized Map<String, String> block(String tag) {
    Map<String, String> parsed = kParsed.get(tag);
    if (parsed == null) {
      load();
      parsed = new HashMap<String, String>();
      String text = blocks.get(tag);
      if (text != null) {
        int pos = 0;
        while (pos < text.length()) {
          int lineEnd = text.indexOf('\n', pos);
          if (lineEnd < 0) {
            lineEnd = text.length();
          }
          int tab = text.indexOf('\t', pos);
          if (tab > pos && tab < lineEnd) {
            parsed.put(text.substring(pos, tab), unescape(text.substring(tab + 1, lineEnd)));
          }
          pos = lineEnd + 1;
        }
      }
      kParsed.put(tag, parsed);
    }
    return parsed;
  }

  public static int[] weekData(Locale locale) {
    String country = locale.getCountry();
    String[] values = country.isEmpty() ? null : list(Locale.ROOT, "W." + country);
    if (values == null) {
      values = list(locale, "WK");
    }
    int first = values == null ? 1 : Integer.parseInt(values[0]);
    int minimal = values == null ? 1 : Integer.parseInt(values[1]);
    String override = locale.getUnicodeLocaleType("fw");
    if (override != null) {
      int index = "mon tue wed thu fri sat sun".indexOf(override);
      if (index >= 0 && override.length() == 3) {
        first = index / 4 + 1;
      }
    }
    return new int[] {first, minimal};
  }

  public static String formatPattern(Locale locale, int dateStyle, int timeStyle) {
    return pattern(locale, dateStyle, timeStyle, dateStyle < 0 ? "FT" : timeStyle < 0 ? "FP" : "FD");
  }

  public static String pattern(Locale locale, int dateStyle, int timeStyle) {
    return pattern(locale, dateStyle, timeStyle, dateStyle < 0 ? "TP" : timeStyle < 0 ? "DP" : "DT");
  }

  private static String pattern(Locale locale, int dateStyle, int timeStyle, String key) {
    String[] patterns = list(locale, key);
    if (patterns == null) {
      return null;
    }
    int index = dateStyle < 0 ? timeStyle : timeStyle < 0 ? dateStyle : dateStyle * 4 + timeStyle;
    return index < patterns.length ? patterns[index] : null;
  }

  public static char zeroDigit(String numberingSystem) {
    switch (numberingSystem) {
    case "latn": return '0';
    case "arab": return '\u0660';
    case "arabext": return '\u06f0';
    case "bali": return '\u1b50';
    case "beng": return '\u09e6';
    case "cham": return '\uaa50';
    case "deva": return '\u0966';
    case "fullwide": return '\uff10';
    case "gujr": return '\u0ae6';
    case "guru": return '\u0a66';
    case "java": return '\ua9d0';
    case "kali": return '\ua900';
    case "khmr": return '\u17e0';
    case "knda": return '\u0ce6';
    case "lana": return '\u1a80';
    case "lanatham": return '\u1a90';
    case "laoo": return '\u0ed0';
    case "lepc": return '\u1c40';
    case "limb": return '\u1946';
    case "mlym": return '\u0d66';
    case "mong": return '\u1810';
    case "mtei": return '\uabf0';
    case "mymr": return '\u1040';
    case "mymrshan": return '\u1090';
    case "nkoo": return '\u07c0';
    case "olck": return '\u1c50';
    case "orya": return '\u0b66';
    case "saur": return '\ua8d0';
    case "sund": return '\u1bb0';
    case "talu": return '\u19d0';
    case "tamldec": return '\u0be6';
    case "telu": return '\u0c66';
    case "thai": return '\u0e50';
    case "tibt": return '\u0f20';
    case "vaii": return '\ua620';
    default: return 0;
    }
  }

  private static String tagOf(Locale locale) {
    String language = locale.getLanguage();
    if (language.isEmpty()) {
      return "";
    }
    StringBuilder sb = new StringBuilder(language);
    String script = locale.getScript();
    if (!script.isEmpty()) {
      sb.append('-').append(script);
    }
    String country = locale.getCountry();
    if (!country.isEmpty()) {
      sb.append('-').append(country);
    }
    return sb.toString();
  }

  public static synchronized String get(Locale locale, String key) {
    String[] values = resolve(locale, key);
    return values.length == 0 ? null : values[0];
  }

  public static synchronized String[] list(Locale locale, String key) {
    String[] values = resolve(locale, key);
    return values.length == 0 ? null : values.clone();
  }

  private static String[] resolve(Locale locale, String key) {
    String tag = tagOf(locale);
    Map<String, String[]> cache = kResolved.get(tag);
    if (cache == null) {
      cache = new HashMap<String, String[]>();
      kResolved.put(tag, cache);
    }
    String[] values = cache.get(key);
    if (values == null) {
      String value = lookup(tag, key);
      if (value == null) {
        values = new String[0];
      } else if (value.indexOf(kSeparator) < 0) {
        values = new String[] {value};
      } else {
        values = split(value);
      }
      cache.put(key, values);
    }
    return values;
  }

  private static String lookup(String tag, String key) {
    String current = tag;
    while (!current.isEmpty()) {
      String value = block(current).get(key);
      if (value != null) {
        return value;
      }
      int cut = current.lastIndexOf('-');
      current = cut < 0 ? "" : current.substring(0, cut);
    }
    return block("root").get(key);
  }

  private static String[] split(String value) {
    int count = 1;
    for (int i = 0; i < value.length(); i++) {
      if (value.charAt(i) == kSeparator) {
        count++;
      }
    }
    String[] parts = new String[count];
    int start = 0;
    int index = 0;
    for (int i = 0; i < value.length(); i++) {
      if (value.charAt(i) == kSeparator) {
        parts[index++] = value.substring(start, i);
        start = i + 1;
      }
    }
    parts[index] = value.substring(start);
    return parts;
  }
}
