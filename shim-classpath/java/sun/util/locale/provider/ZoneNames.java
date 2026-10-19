/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package sun.util.locale.provider;

import java.time.format.TextStyle;
import java.util.Locale;

public final class ZoneNames {
  private ZoneNames() { }

  private static String[] tuple(Locale locale, String zoneId, int depth) {
    String[] values = shim.LocaleDb.zones().list(locale, "z." + zoneId);
    if (values == null) {
      return null;
    }
    if (values.length == 1 && values[0].startsWith("=@") && depth < 4) {
      return tuple(locale, values[0].substring(2), depth + 1);
    }
    if (values.length != 10) {
      return null;
    }
    for (int i = 0; i < values.length; i++) {
      String value = values[i];
      if (value.length() == 2 && value.charAt(0) == '=' && value.charAt(1) >= '0' && value.charAt(1) <= '9') {
        values[i] = values[value.charAt(1) - '0'];
      }
    }
    return values;
  }

  private static String[] tupleOrEnglish(Locale locale, String zoneId) {
    String[] values = tuple(locale, zoneId, 0);
    return values != null ? values : tuple(Locale.ENGLISH, zoneId, 0);
  }

  public static String timeZoneName(Locale locale, String zoneId, boolean daylight, boolean shortName) {
    String[] values = tupleOrEnglish(locale, zoneId);
    if (values == null) {
      return null;
    }
    return values[(shortName ? 0 : 1) + (daylight ? 2 : 0)];
  }

  public static String get(Locale locale, String zoneId, TextStyle style, boolean generic, boolean daylight) {
    String[] values = tupleOrEnglish(locale, zoneId);
    if (values == null) {
      return null;
    }
    if (generic) {
      switch (style) {
        case FULL: return values[4];
        case FULL_STANDALONE: return values[5];
        case SHORT: return values[6];
        case SHORT_STANDALONE: return values[7];
        case NARROW: return values[8];
        default: return values[9];
      }
    }
    boolean full = style.asNormal() == TextStyle.FULL;
    return values[(full ? 1 : 0) + (daylight ? 2 : 0)];
  }

  private static int digitValue(char c) {
    return Character.digit(c, 10);
  }

  public static String gmtOffset(Locale locale, int totalSeconds, boolean full) {
    String[] samples = shim.LocaleDb.zones().list(locale, "gmt");
    if (samples == null) {
      samples = shim.LocaleDb.zones().list(Locale.ENGLISH, "gmt");
    }
    if (samples == null || samples.length != 10) {
      return null;
    }
    int base = full ? 5 : 0;
    if (totalSeconds == 0) {
      return samples[base];
    }
    int hours = Math.abs(totalSeconds / 3600) % 100;
    int minutes = Math.abs(totalSeconds / 60) % 60;
    boolean negative = totalSeconds < 0;
    String template = samples[base + (minutes != 0 ? (negative ? 2 : 1) : (negative ? 4 : 3))];
    String padReference = samples[base + (negative ? 2 : 1)];
    int hourWidth = 1;
    for (int i = 0; i < padReference.length(); i++) {
      if (digitValue(padReference.charAt(i)) >= 0) {
        int j = i;
        while (j < padReference.length() && digitValue(padReference.charAt(j)) >= 0) {
          j++;
        }
        hourWidth = j - i;
        break;
      }
    }
    char zero = '0';
    for (int i = 0; i < template.length(); i++) {
      int d = digitValue(template.charAt(i));
      if (d >= 0) {
        zero = (char) (template.charAt(i) - d);
        break;
      }
    }
    StringBuilder sb = new StringBuilder();
    int run = 0;
    int i = 0;
    while (i < template.length()) {
      char c = template.charAt(i);
      if (digitValue(c) >= 0) {
        int j = i;
        while (j < template.length() && digitValue(template.charAt(j)) >= 0) {
          j++;
        }
        int value = run == 0 ? hours : minutes;
        int width = run == 0 ? (value >= 10 ? 2 : hourWidth) : 2;
        String digits = Integer.toString(value);
        for (int pad = digits.length(); pad < width; pad++) {
          sb.append(zero);
        }
        for (int k = 0; k < digits.length(); k++) {
          sb.append((char) (zero + (digits.charAt(k) - '0')));
        }
        run++;
        i = j;
      } else {
        sb.append(c);
        i++;
      }
    }
    return sb.toString();
  }
}
