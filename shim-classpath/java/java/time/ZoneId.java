/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.time;

import java.io.Serializable;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.time.zone.ZoneRules;
import java.time.zone.ZoneRulesException;
import java.time.zone.ZoneRulesProvider;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;

public abstract class ZoneId implements Serializable {
  public static final Map<String, String> SHORT_IDS;

  static {
    Map<String, String> map = new HashMap<String, String>();
    map.put("ACT", "Australia/Darwin");
    map.put("AET", "Australia/Sydney");
    map.put("AGT", "America/Argentina/Buenos_Aires");
    map.put("ART", "Africa/Cairo");
    map.put("AST", "America/Anchorage");
    map.put("BET", "America/Sao_Paulo");
    map.put("BST", "Asia/Dhaka");
    map.put("CAT", "Africa/Harare");
    map.put("CNT", "America/St_Johns");
    map.put("CST", "America/Chicago");
    map.put("CTT", "Asia/Shanghai");
    map.put("EAT", "Africa/Addis_Ababa");
    map.put("ECT", "Europe/Paris");
    map.put("IET", "America/Indiana/Indianapolis");
    map.put("IST", "Asia/Kolkata");
    map.put("JST", "Asia/Tokyo");
    map.put("MIT", "Pacific/Apia");
    map.put("NET", "Asia/Yerevan");
    map.put("NST", "Pacific/Auckland");
    map.put("PLT", "Asia/Karachi");
    map.put("PNT", "America/Phoenix");
    map.put("PRT", "America/Puerto_Rico");
    map.put("PST", "America/Los_Angeles");
    map.put("SST", "Pacific/Guadalcanal");
    map.put("VST", "Asia/Ho_Chi_Minh");
    map.put("EST", "America/Panama");
    map.put("MST", "America/Phoenix");
    map.put("HST", "Pacific/Honolulu");
    SHORT_IDS = Collections.unmodifiableMap(map);
  }

  private static final long serialVersionUID = 8352817235686L;

  public static ZoneId systemDefault() {
    return TimeZone.getDefault().toZoneId();
  }

  public static Set<String> getAvailableZoneIds() {
    return new java.util.HashSet<String>(ZoneRulesProvider.getAvailableZoneIds());
  }

  public static ZoneId of(String zoneId, Map<String, String> aliasMap) {
    Objects.requireNonNull(zoneId, "zoneId");
    Objects.requireNonNull(aliasMap, "aliasMap");
    String id = aliasMap.get(zoneId);
    id = (id != null ? id : zoneId);
    return of(id);
  }

  public static ZoneId of(String zoneId) {
    return of(zoneId, true);
  }

  public static ZoneId ofOffset(String prefix, ZoneOffset offset) {
    Objects.requireNonNull(prefix, "prefix");
    Objects.requireNonNull(offset, "offset");
    if (prefix.isEmpty()) {
      return offset;
    }
    if (!prefix.equals("GMT") && !prefix.equals("UTC") && !prefix.equals("UT")) {
      throw new IllegalArgumentException("prefix should be GMT, UTC or UT, is: " + prefix);
    }
    if (offset.getTotalSeconds() != 0) {
      prefix = prefix.concat(offset.getId());
    }
    return new ZoneRegion(prefix, offset.getRules());
  }

  static ZoneId of(String zoneId, boolean checkAvailable) {
    Objects.requireNonNull(zoneId, "zoneId");
    if (zoneId.length() <= 1 || zoneId.startsWith("+") || zoneId.startsWith("-")) {
      return ZoneOffset.of(zoneId);
    } else if (zoneId.startsWith("UTC") || zoneId.startsWith("GMT")) {
      return ofWithPrefix(zoneId, 3, checkAvailable);
    } else if (zoneId.startsWith("UT")) {
      return ofWithPrefix(zoneId, 2, checkAvailable);
    }
    return ZoneRegion.ofId(zoneId, checkAvailable);
  }

  private static ZoneId ofWithPrefix(String zoneId, int prefixLength, boolean checkAvailable) {
    String prefix = zoneId.substring(0, prefixLength);
    if (zoneId.length() == prefixLength) {
      return ofOffset(prefix, ZoneOffset.UTC);
    }
    if (zoneId.charAt(prefixLength) != '+' && zoneId.charAt(prefixLength) != '-') {
      return ZoneRegion.ofId(zoneId, checkAvailable);
    }
    try {
      ZoneOffset offset = ZoneOffset.of(zoneId.substring(prefixLength));
      if (offset == ZoneOffset.UTC) {
        return ofOffset(prefix, offset);
      }
      return ofOffset(prefix, offset);
    } catch (DateTimeException ex) {
      throw new DateTimeException("Invalid ID for offset-based ZoneId: " + zoneId, ex);
    }
  }

  public static ZoneId from(TemporalAccessor temporal) {
    ZoneId obj = temporal.query(TemporalQueries.zone());
    if (obj == null) {
      throw new DateTimeException("Unable to obtain ZoneId from TemporalAccessor: " + temporal + " of type "
          + temporal.getClass().getName());
    }
    return obj;
  }

  ZoneId() {
    if (getClass() != ZoneOffset.class && getClass() != ZoneRegion.class) {
      throw new AssertionError("Invalid subclass");
    }
  }

  public abstract String getId();

  public String getDisplayName(TextStyle style, Locale locale) {
    Objects.requireNonNull(style, "style");
    Objects.requireNonNull(locale, "locale");
    return new java.time.format.DateTimeFormatterBuilder().appendZoneText(style).toFormatter(locale)
        .format(new java.time.temporal.TemporalAccessor() {
          @Override
          public boolean isSupported(java.time.temporal.TemporalField field) {
            return false;
          }

          @Override
          public long getLong(java.time.temporal.TemporalField field) {
            throw new java.time.temporal.UnsupportedTemporalTypeException("Unsupported field: " + field);
          }

          @SuppressWarnings("unchecked")
          @Override
          public <R> R query(java.time.temporal.TemporalQuery<R> query) {
            if (query == java.time.temporal.TemporalQueries.zoneId()) {
              return (R) ZoneId.this;
            }
            return java.time.temporal.TemporalAccessor.super.query(query);
          }
        });
  }

  public abstract ZoneRules getRules();

  public ZoneId normalized() {
    try {
      ZoneRules rules = getRules();
      if (rules.isFixedOffset()) {
        return rules.getOffset(Instant.EPOCH);
      }
    } catch (ZoneRulesException ex) {
      // not normalizable
    }
    return this;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof ZoneId) {
      ZoneId other = (ZoneId) obj;
      return getId().equals(other.getId());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return getId().hashCode();
  }

  @Override
  public String toString() {
    return getId();
  }
}
