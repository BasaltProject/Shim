/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.time;

import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalField;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class ZoneOffset extends ZoneId implements Comparable<ZoneOffset>, TemporalAccessor, TemporalAdjuster {
  private static final ConcurrentMap<Integer, ZoneOffset> SECONDS_CACHE = new ConcurrentHashMap<Integer, ZoneOffset>();
  private static final ConcurrentMap<String, ZoneOffset> ID_CACHE = new ConcurrentHashMap<String, ZoneOffset>();

  public static final ZoneOffset UTC = ofTotalSeconds(0);
  public static final ZoneOffset MIN = ofTotalSeconds(-64800);
  public static final ZoneOffset MAX = ofTotalSeconds(64800);

  private final int totalSeconds;
  private final transient String id;

  private ZoneOffset(int totalSeconds) {
    this.totalSeconds = totalSeconds;
    this.id = buildId(totalSeconds);
  }

  private static String buildId(int totalSeconds) {
    if (totalSeconds == 0) {
      return "Z";
    }
    int absSec = Math.abs(totalSeconds);
    StringBuilder buf = new StringBuilder();
    buf.append(totalSeconds < 0 ? "-" : "+");
    int absHours = absSec / 3600;
    int absMinutes = (absSec / 60) % 60;
    int absSeconds = absSec % 60;
    buf.append(absHours < 10 ? "0" : "").append(absHours);
    buf.append(absMinutes < 10 ? ":0" : ":").append(absMinutes);
    if (absSeconds != 0) {
      buf.append(absSeconds < 10 ? ":0" : ":").append(absSeconds);
    }
    return buf.toString();
  }

  public static ZoneOffset ofTotalSeconds(int totalSeconds) {
    if (totalSeconds < -64800 || totalSeconds > 64800) {
      throw new DateTimeException("Zone offset not in valid range: -18:00 to +18:00: " + totalSeconds);
    }
    Integer key = Integer.valueOf(totalSeconds);
    ZoneOffset offset = SECONDS_CACHE.get(key);
    if (offset == null) {
      offset = new ZoneOffset(totalSeconds);
      SECONDS_CACHE.putIfAbsent(key, offset);
      ID_CACHE.putIfAbsent(offset.getId(), offset);
      offset = SECONDS_CACHE.get(key);
    }
    return offset;
  }

  public static ZoneOffset ofHours(int hours) {
    return ofHoursMinutes(hours, 0);
  }

  public static ZoneOffset ofHoursMinutes(int hours, int minutes) {
    return ofHoursMinutesSeconds(hours, minutes, 0);
  }

  public static ZoneOffset ofHoursMinutesSeconds(int hours, int minutes, int seconds) {
    if (hours < -18 || hours > 18) {
      throw new DateTimeException("Zone offset hours out of range: " + hours);
    }
    if (hours > 0) {
      if (minutes < 0 || seconds < 0) {
        throw new DateTimeException("Zone offset minutes/seconds must be positive if hours positive");
      }
    } else if (hours < 0) {
      if (minutes > 0 || seconds > 0) {
        throw new DateTimeException("Zone offset minutes/seconds must be negative if hours negative");
      }
    }
    int total = hours * 3600 + minutes * 60 + seconds;
    return ofTotalSeconds(total);
  }

  public static ZoneOffset of(String offsetId) {
    Objects.requireNonNull(offsetId, "offsetId");
    ZoneOffset offset = ID_CACHE.get(offsetId);
    if (offset != null) {
      return offset;
    }
    if (offsetId.equals("Z")) {
      return UTC;
    }
    int sign = 1;
    String str = offsetId;
    if (str.startsWith("+")) {
      str = str.substring(1);
    } else if (str.startsWith("-")) {
      sign = -1;
      str = str.substring(1);
    } else {
      throw new DateTimeException("Invalid ID for ZoneOffset: " + offsetId);
    }
    String[] parts = str.split(":");
    int hours = Integer.parseInt(parts[0]);
    int minutes = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
    int seconds = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
    return ofTotalSeconds(sign * (hours * 3600 + minutes * 60 + seconds));
  }

  public int getTotalSeconds() {
    return totalSeconds;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public java.time.zone.ZoneRules getRules() {
    return java.time.zone.ZoneRules.of(this);
  }

  @Override
  public boolean isSupported(TemporalField field) {
    return field == ChronoField.OFFSET_SECONDS;
  }

  @Override
  public long getLong(TemporalField field) {
    if (field == ChronoField.OFFSET_SECONDS) {
      return totalSeconds;
    }
    throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
  }

  @Override
  public Temporal adjustInto(Temporal temporal) {
    return temporal.with(ChronoField.OFFSET_SECONDS, totalSeconds);
  }

  @Override
  public int compareTo(ZoneOffset other) {
    return other.totalSeconds - this.totalSeconds;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj instanceof ZoneOffset) {
      return totalSeconds == ((ZoneOffset) obj).totalSeconds;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return totalSeconds;
  }

  @Override
  public String toString() {
    return id;
  }
}
