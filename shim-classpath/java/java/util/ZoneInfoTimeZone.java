/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.zone.ZoneOffsetTransition;
import java.time.zone.ZoneRules;
import java.time.zone.ZoneRulesException;
import java.time.zone.ZoneRulesProvider;
import java.util.List;

final class ZoneInfoTimeZone extends TimeZone {
  private static final long serialVersionUID = 2653134537216586139L;

  private final transient ZoneRules rules;
  private int rawDelta;

  ZoneInfoTimeZone(String id, ZoneRules rules) {
    this.rules = rules;
    setID(id);
  }

  static TimeZone find(String id) {
    if (id == null) {
      throw new NullPointerException();
    }
    String alias = TimeZone.kOldIds.get(id);
    try {
      if (alias != null) {
        if (alias.charAt(0) == '-') {
          return new ZoneInfoTimeZone(id, ZoneRules.of(ZoneOffset.of(alias)));
        }
        return new ZoneInfoTimeZone(id, ZoneRulesProvider.getRules(alias, false));
      }
      return new ZoneInfoTimeZone(id, ZoneRulesProvider.getRules(id, false));
    } catch (ZoneRulesException ex) {
      return null;
    }
  }

  private Object readResolve() {
    ZoneInfoTimeZone resolved = (ZoneInfoTimeZone) find(getID());
    if (resolved == null) {
      throw new IllegalStateException("unknown time zone " + getID());
    }
    resolved.rawDelta = rawDelta;
    return resolved;
  }

  ZoneRules rules() {
    return rules;
  }

  private static final long kUtc1900 = -2208988800L;

  private transient volatile int tableState;
  private transient int currentRaw;

  private static int millis(ZoneOffset offset) {
    return offset.getTotalSeconds() * 1000;
  }

  private boolean hasTable() {
    int state = tableState;
    if (state == 0) {
      boolean table = false;
      for (ZoneOffsetTransition t : rules.getTransitions()) {
        if (t.toEpochSecond() >= kUtc1900) {
          table = true;
          break;
        }
      }
      currentRaw = millis(rules.getStandardOffset(Instant.ofEpochSecond(32503680000L)));
      state = table ? 1 : 2;
      tableState = state;
    }
    return state == 1;
  }

  private boolean rawOnly(long epochSecond) {
    if (rules.isFixedOffset()) {
      return false;
    }
    return !hasTable() || epochSecond < kUtc1900;
  }

  @Override
  public int getOffset(long date) {
    if (rawOnly(Math.floorDiv(date, 1000L))) {
      return currentRaw + rawDelta;
    }
    return millis(rules.getOffset(Instant.ofEpochMilli(date))) + rawDelta;
  }

  @Override
  int getOffsets(long date, int[] offsets) {
    Instant instant = Instant.ofEpochMilli(date);
    int std = millis(rules.getStandardOffset(instant)) + rawDelta;
    int total = millis(rules.getOffset(instant)) + rawDelta;
    if (rawOnly(Math.floorDiv(date, 1000L))) {
      std = currentRaw + rawDelta;
      total = std;
    }
    if (offsets != null) {
      offsets[0] = std;
      offsets[1] = total - std;
    }
    return total;
  }

  int getOffsetsByWall(long wall, int[] offsets) {
    long seconds = Math.floorDiv(wall, 1000L);
    int nanos = (int) Math.floorMod(wall, 1000L) * 1000000;
    LocalDateTime ldt = LocalDateTime.ofEpochSecond(seconds, nanos, ZoneOffset.UTC);
    List<ZoneOffset> valid = rules.getValidOffsets(ldt);
    ZoneOffset chosen;
    if (valid.size() == 1) {
      chosen = valid.get(0);
    } else if (valid.size() == 2) {
      chosen = valid.get(0).getTotalSeconds() < valid.get(1).getTotalSeconds() ? valid.get(0) : valid.get(1);
    } else {
      ZoneOffsetTransition t = rules.getTransition(ldt);
      chosen = t.getOffsetBefore();
    }
    Instant instant = Instant.ofEpochSecond(seconds - chosen.getTotalSeconds());
    int std = millis(rules.getStandardOffset(instant)) + rawDelta;
    int total = millis(chosen) + rawDelta;
    if (rawOnly(instant.getEpochSecond())) {
      std = currentRaw + rawDelta;
      total = std;
    }
    if (offsets != null) {
      offsets[0] = std;
      offsets[1] = total - std;
    }
    return total;
  }

  @Override
  public int getOffset(int era, int year, int month, int day, int dayOfWeek, int milliseconds) {
    if (era != GregorianCalendar.AD && era != GregorianCalendar.BC) {
      throw new IllegalArgumentException();
    }
    if (month < 0 || month > 11 || day < 1 || day > 31 || dayOfWeek < 1 || dayOfWeek > 7 || milliseconds < 0
        || milliseconds >= 86400000) {
      throw new IllegalArgumentException();
    }
    int y = era == GregorianCalendar.BC ? 1 - year : year;
    LocalDateTime ldt;
    try {
      ldt = LocalDateTime.of(y, month + 1, day, 0, 0).plusNanos(milliseconds * 1000000L);
    } catch (RuntimeException ex) {
      throw new IllegalArgumentException(ex);
    }
    long wall = ldt.toEpochSecond(ZoneOffset.UTC) * 1000L + ldt.getNano() / 1000000;
    return getOffsetsByWall(wall, null);
  }

  @Override
  public void setRawOffset(int offsetMillis) {
    rawDelta = offsetMillis - getRawOffset0();
  }

  private int getRawOffset0() {
    return millis(rules.getStandardOffset(Instant.now()));
  }

  @Override
  public int getRawOffset() {
    return getRawOffset0() + rawDelta;
  }

  @Override
  public boolean useDaylightTime() {
    return !rules.getTransitionRules().isEmpty();
  }

  @Override
  public boolean observesDaylightTime() {
    return useDaylightTime() || rules.nextTransition(Instant.now()) != null;
  }

  @Override
  public boolean inDaylightTime(Date date) {
    if (rawOnly(Math.floorDiv(date.getTime(), 1000L))) {
      return false;
    }
    return rules.isDaylightSavings(date.toInstant());
  }

  @Override
  public int getDSTSavings() {
    return useDaylightTime() ? 3600000 : 0;
  }

  @Override
  public boolean hasSameRules(TimeZone other) {
    if (other == null) {
      return false;
    }
    if (!(other instanceof ZoneInfoTimeZone)) {
      return false;
    }
    ZoneInfoTimeZone o = (ZoneInfoTimeZone) other;
    return rules.equals(o.rules) && rawDelta == o.rawDelta;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ZoneInfoTimeZone)) {
      return false;
    }
    ZoneInfoTimeZone that = (ZoneInfoTimeZone) obj;
    return getID().equals(that.getID()) && hasSameRules(that);
  }

  @Override
  public int hashCode() {
    return getID().hashCode() ^ rules.hashCode() ^ rawDelta;
  }

  @Override
  public String toString() {
    return getClass().getName() + "[id=\"" + getID() + "\",offset=" + getRawOffset() + ",dstSavings="
        + getDSTSavings() + ",useDaylight=" + useDaylightTime() + ",transitions=" + rules.getTransitions().size()
        + ",lastRule=null]";
  }
}
