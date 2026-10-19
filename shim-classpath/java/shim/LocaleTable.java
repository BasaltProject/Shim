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

public final class LocaleTable {
  private static final char kSeparator = '\u001f';

  private Map<String, String> blocks;
  private final Map<String, Map<String, String[]>> kResolved = new HashMap<String, Map<String, String[]>>();
  private final Map<String, Map<String, String>> kParsed = new HashMap<String, Map<String, String>>();

  private final String[] chunks;

  LocaleTable(String[] chunks) {
    this.chunks = chunks;
  }

  private synchronized void load() {
    if (blocks != null) {
      return;
    }
    StringBuilder encoded = new StringBuilder();
    for (String chunk : chunks) {
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

  private synchronized Map<String, String> block(String tag) {
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

  static String tagOf(Locale locale) {
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

  public synchronized String get(Locale locale, String key) {
    String[] values = resolve(locale, key);
    return values.length == 0 ? null : values[0];
  }

  public synchronized String[] list(Locale locale, String key) {
    String[] values = resolve(locale, key);
    return values.length == 0 ? null : values.clone();
  }

  private String[] resolve(Locale locale, String key) {
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

  private String lookup(String tag, String key) {
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
