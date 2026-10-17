/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.util;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.zone.ZoneRules;
import java.time.zone.ZoneRulesException;
import java.time.zone.ZoneRulesProvider;
import java.time.ZonedDateTime;

public abstract class TimeZone implements Serializable, Cloneable {
  private static final long serialVersionUID = 3581463369166924961L;

  public static final int SHORT = 0;
  public static final int LONG = 1;

  private static final int kMillisPerHour = 3600000;
  private static volatile TimeZone defaultTimeZone;

  private String id;

  public TimeZone() { }

  public abstract int getOffset(int era, int year, int month, int day, int dayOfWeek, int milliseconds);

  public int getOffset(long date) {
    if (inDaylightTime(new Date(date))) {
      return getRawOffset() + getDSTSavings();
    }
    return getRawOffset();
  }

  int getOffsets(long date, int[] offsets) {
    int rawoffset = getRawOffset();
    int dstoffset = 0;
    if (inDaylightTime(new Date(date))) {
      dstoffset = getDSTSavings();
    }
    if (offsets != null) {
      offsets[0] = rawoffset;
      offsets[1] = dstoffset;
    }
    return rawoffset + dstoffset;
  }

  public abstract void setRawOffset(int offsetMillis);

  public abstract int getRawOffset();

  public String getID() {
    return id;
  }

  public void setID(String id) {
    if (id == null) {
      throw new NullPointerException();
    }
    this.id = id;
  }

  public final String getDisplayName() {
    return getDisplayName(false, LONG, Locale.getDefault());
  }

  public final String getDisplayName(Locale locale) {
    return getDisplayName(false, LONG, locale);
  }

  public final String getDisplayName(boolean daylight, int style) {
    return getDisplayName(daylight, style, Locale.getDefault());
  }

  public String getDisplayName(boolean daylight, int style, Locale locale) {
    if (style != SHORT && style != LONG) {
      throw new IllegalArgumentException("Illegal style: " + style);
    }
    String id = getID();
    if (id.startsWith("GMT") && id.length() > 3 && (id.charAt(3) == '+' || id.charAt(3) == '-')) {
      return id;
    }
    switch (id) {
      case "EST":
        return style == SHORT ? (daylight ? "EDT" : "EST") : (daylight ? "Eastern Daylight Time" : "Eastern Standard Time");
      case "MST":
        return style == SHORT ? (daylight ? "MDT" : "MST") : (daylight ? "Mountain Daylight Time" : "Mountain Standard Time");
      case "HST":
        return style == SHORT ? (daylight ? "HDT" : "HST")
            : (daylight ? "Hawaii-Aleutian Daylight Time" : "Hawaii-Aleutian Standard Time");
      default:
        break;
    }
    String known = sun.util.locale.provider.ZoneNames.get(id, style == SHORT ? java.time.format.TextStyle.SHORT
        : java.time.format.TextStyle.FULL, false, daylight);
    if (known != null) {
      return known;
    }
    ZoneId zone = toZoneId();
    ZoneRules rules = zone.getRules();
    Instant sample = Instant.parse("2024-01-15T12:00:00Z");
    Instant other = Instant.parse("2024-07-15T12:00:00Z");
    Instant chosen = rules.isDaylightSavings(sample) == daylight ? sample
        : (rules.isDaylightSavings(other) == daylight ? other : sample);
    String pattern = style == SHORT ? "z" : "zzzz";
    return DateTimeFormatter.ofPattern(pattern, Locale.US).format(ZonedDateTime.ofInstant(chosen, zone));
  }

  public int getDSTSavings() {
    if (useDaylightTime()) {
      return 3600000;
    }
    return 0;
  }

  public abstract boolean useDaylightTime();

  public boolean observesDaylightTime() {
    return useDaylightTime() || inDaylightTime(new Date());
  }

  public abstract boolean inDaylightTime(Date date);

  public static synchronized TimeZone getTimeZone(String id) {
    return getTimeZone(id, true);
  }

  public static TimeZone getTimeZone(ZoneId zoneId) {
    String tzid = zoneId.getId();
    char c = tzid.charAt(0);
    if (c == '+' || c == '-') {
      tzid = "GMT" + tzid;
    } else if (c == 'Z' && tzid.length() == 1) {
      tzid = "UTC";
    }
    return getTimeZone(tzid, true);
  }

  public ZoneId toZoneId() {
    String tzid = getID();
    if (tzid.startsWith("GMT") && tzid.length() > 3) {
      char c = tzid.charAt(3);
      if (c == '+' || c == '-') {
        try {
          return ZoneId.of(tzid);
        } catch (RuntimeException ex) {
          return ZoneOffset.ofTotalSeconds(getRawOffset() / 1000);
        }
      }
    }
    return toZoneId0(tzid);
  }

  private ZoneId toZoneId0(String id) {
    String alias = kOldIds.get(id);
    if (alias != null) {
      if (alias.charAt(0) == '-') {
        return ZoneId.of(alias);
      }
      return ZoneId.of(alias);
    }
    try {
      return ZoneId.of(id);
    } catch (RuntimeException ex) {
      return ZoneOffset.ofTotalSeconds(getRawOffset() / 1000);
    }
  }

  static final Map<String, String> kOldIds = new HashMap<String, String>();

  static {
    kOldIds.put("ACT", "Australia/Darwin");
    kOldIds.put("AET", "Australia/Sydney");
    kOldIds.put("AGT", "America/Argentina/Buenos_Aires");
    kOldIds.put("ART", "Africa/Cairo");
    kOldIds.put("AST", "America/Anchorage");
    kOldIds.put("BET", "America/Sao_Paulo");
    kOldIds.put("BST", "Asia/Dhaka");
    kOldIds.put("CAT", "Africa/Harare");
    kOldIds.put("CNT", "America/St_Johns");
    kOldIds.put("CST", "America/Chicago");
    kOldIds.put("CTT", "Asia/Shanghai");
    kOldIds.put("EAT", "Africa/Addis_Ababa");
    kOldIds.put("ECT", "Europe/Paris");
    kOldIds.put("IET", "America/Indiana/Indianapolis");
    kOldIds.put("IST", "Asia/Kolkata");
    kOldIds.put("JST", "Asia/Tokyo");
    kOldIds.put("MIT", "Pacific/Apia");
    kOldIds.put("NET", "Asia/Yerevan");
    kOldIds.put("NST", "Pacific/Auckland");
    kOldIds.put("PLT", "Asia/Karachi");
    kOldIds.put("PNT", "America/Phoenix");
    kOldIds.put("PRT", "America/Puerto_Rico");
    kOldIds.put("PST", "America/Los_Angeles");
    kOldIds.put("SST", "Pacific/Guadalcanal");
    kOldIds.put("VST", "Asia/Ho_Chi_Minh");
    kOldIds.put("EST", "-05:00");
    kOldIds.put("MST", "-07:00");
    kOldIds.put("HST", "-10:00");
  }

  private static TimeZone getTimeZone(String id, boolean fallback) {
    TimeZone tz = ZoneInfoTimeZone.find(id);
    if (tz == null) {
      tz = parseCustomTimeZone(id);
      if (tz == null && fallback) {
        tz = ZoneInfoTimeZone.find("GMT");
      }
    }
    return tz;
  }

  private static TimeZone parseCustomTimeZone(String id) {
    int length;
    if ((length = id.length()) < 5 || id.indexOf("GMT") != 0) {
      return null;
    }
    int index = 3;
    int sign;
    char c = id.charAt(index++);
    if (c == '-') {
      sign = -1;
    } else if (c == '+') {
      sign = 1;
    } else {
      return null;
    }
    int hours = 0;
    int num = 0;
    int countDelim = 0;
    int len = 0;
    while (index < length) {
      c = id.charAt(index++);
      if (c == ':') {
        if (countDelim > 0) {
          return null;
        }
        if (len > 2) {
          return null;
        }
        hours = num;
        countDelim++;
        num = 0;
        len = 0;
        continue;
      }
      if (c < '0' || c > '9') {
        return null;
      }
      num = num * 10 + (c - '0');
      len++;
    }
    if (index != length) {
      return null;
    }
    if (countDelim == 0) {
      if (len <= 2) {
        hours = num;
        num = 0;
      } else if (len <= 4) {
        hours = num / 100;
        num = num % 100;
      } else {
        return null;
      }
    } else if (len != 2) {
      return null;
    }
    int minutes = num;
    if (hours > 23 || minutes > 59) {
      return null;
    }
    int gmtOffset = (hours * 60 + minutes) * 60 * 1000;
    if (gmtOffset == 0) {
      return ZoneInfoTimeZone.find("UTC") == null ? null : zoneWithId(sign < 0 ? "GMT-00:00" : "GMT+00:00", 0);
    }
    return zoneWithId(String.format("GMT%c%02d:%02d", sign < 0 ? '-' : '+', hours, minutes), sign * gmtOffset);
  }

  private static TimeZone zoneWithId(String id, int offset) {
    return new ZoneInfoTimeZone(id, ZoneRules.of(ZoneOffset.ofTotalSeconds(offset / 1000)));
  }

  public static synchronized String[] getAvailableIDs(int rawOffset) {
    List<String> ids = new ArrayList<String>();
    for (String id : getAvailableIDs()) {
      TimeZone tz = getTimeZone(id, false);
      if (tz != null && tz.getRawOffset() == rawOffset) {
        ids.add(id);
      }
    }
    return ids.toArray(new String[0]);
  }

  public static synchronized String[] getAvailableIDs() {
    Set<String> all = new TreeSet<String>(ZoneRulesProvider.getAvailableZoneIds());
    all.addAll(kOldIds.keySet());
    return all.toArray(new String[0]);
  }

  public static TimeZone getDefault() {
    return (TimeZone) getDefaultRef().clone();
  }

  static TimeZone getDefaultRef() {
    TimeZone defaultZone = defaultTimeZone;
    if (defaultZone == null) {
      synchronized (TimeZone.class) {
        defaultZone = defaultTimeZone;
        if (defaultZone == null) {
          String id = System.getProperty("user.timezone");
          if (id == null || id.isEmpty()) {
            id = shim.PlatformZone.detect();
            if (id != null) {
              System.setProperty("user.timezone", id);
            }
          }
          TimeZone tz = id == null ? null : getTimeZone(id, false);
          if (tz == null) {
            tz = getTimeZone(id == null ? "UTC" : "GMT", true);
          }
          defaultTimeZone = defaultZone = tz;
        }
      }
    }
    return defaultZone;
  }

  public static void setDefault(TimeZone zone) {
    synchronized (TimeZone.class) {
      defaultTimeZone = zone != null ? (TimeZone) zone.clone() : null;
    }
  }

  public boolean hasSameRules(TimeZone other) {
    return other != null && getRawOffset() == other.getRawOffset() && useDaylightTime() == other.useDaylightTime();
  }

  @Override
  public Object clone() {
    try {
      TimeZone other = (TimeZone) super.clone();
      other.id = id;
      return other;
    } catch (CloneNotSupportedException e) {
      throw new InternalError(e);
    }
  }
}
