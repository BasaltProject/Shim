/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package shim;

import java.util.Locale;

public final class LocaleDb {
  private static LocaleTable main;
  private static LocaleTable currency;
  private static LocaleTable names;
  private static LocaleTable zones;

  private LocaleDb() { }

  private static synchronized LocaleTable main() {
    if (main == null) {
      main = new LocaleTable(LocaleDbDataMain.kLanguages, LocaleDbDataMain.kParts);
    }
    return main;
  }

  public static synchronized LocaleTable currency() {
    if (currency == null) {
      currency = new LocaleTable(LocaleDbDataCurrency.kLanguages, LocaleDbDataCurrency.kParts);
    }
    return currency;
  }

  public static synchronized LocaleTable names() {
    if (names == null) {
      names = new LocaleTable(LocaleDbDataNames.kLanguages, LocaleDbDataNames.kParts);
    }
    return names;
  }

  public static synchronized LocaleTable zones() {
    if (zones == null) {
      zones = new LocaleTable(LocaleDbDataZones.kLanguages, LocaleDbDataZones.kParts);
    }
    return zones;
  }

  public static String get(Locale locale, String key) {
    return main().get(locale, key);
  }

  public static String[] list(Locale locale, String key) {
    return main().list(locale, key);
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

  public static String calendarType(Locale locale) {
    String type = locale.getUnicodeLocaleType("ca");
    if (type != null) {
      return type.equals("buddhist") || type.equals("japanese") ? type : "gregory";
    }
    String found = get(locale, "CT");
    return found == null ? "gregory" : found;
  }

  public static String[] eraNames(Locale locale, String calendar) {
    return list(locale, calendar.equals("buddhist") ? "GB" : "GJ");
  }

  public static String formatPattern(Locale locale, int dateStyle, int timeStyle) {
    String key = dateStyle < 0 ? "FT" : timeStyle < 0 ? "FP" : "FD";
    if (dateStyle >= 0) {
      String type = calendarType(locale);
      if (!type.equals("gregory")) {
        String specific = pattern(locale, dateStyle, timeStyle, key + (type.equals("buddhist") ? "B" : "J"));
        if (specific != null) {
          return specific;
        }
      }
    }
    return pattern(locale, dateStyle, timeStyle, key);
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
}
