/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.util.calendar;

import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

public class ZoneInfo extends TimeZone {
  private static final long serialVersionUID = 2653134537216586139L;

  private int rawOffset;
  private int rawOffsetDiff;
  private int checksum;
  private int dstSavings;
  private long[] transitions;
  private int[] offsets;
  private int[] simpleTimeZoneParams;
  private boolean willGMTOffsetChange;

  public ZoneInfo() {
  }

  public static ZoneInfo fromZone(TimeZone tz) {
    ZoneInfo z = new ZoneInfo();
    z.setID(tz.getID());
    z.rawOffset = tz.getRawOffset();
    z.dstSavings = tz.useDaylightTime() ? tz.getDSTSavings() : 0;
    try {
      java.lang.reflect.Method m = tz.getClass().getDeclaredMethod("transitionRules");
      m.setAccessible(true);
      java.util.List<?> rules = (java.util.List<?>) m.invoke(tz);
      if (rules.size() == 2) {
        int[] p = new int[10];
        java.time.zone.ZoneOffsetTransitionRule a = (java.time.zone.ZoneOffsetTransitionRule) rules.get(0);
        java.time.zone.ZoneOffsetTransitionRule b = (java.time.zone.ZoneOffsetTransitionRule) rules.get(1);
        if (a.getOffsetAfter().getTotalSeconds() < a.getOffsetBefore().getTotalSeconds()) {
          java.time.zone.ZoneOffsetTransitionRule t = a;
          a = b;
          b = t;
        }
        fill(p, 0, a);
        fill(p, 5, b);
        z.simpleTimeZoneParams = p;
        z.dstSavings = (a.getOffsetAfter().getTotalSeconds() - a.getOffsetBefore().getTotalSeconds()) * 1000;
        z.transitions = new long[] {-2208988800000L << 12};
        z.offsets = new int[] {z.rawOffset};
      }
    } catch (ReflectiveOperationException e) {
    }
    return z;
  }

  private static void fill(int[] p, int o, java.time.zone.ZoneOffsetTransitionRule r) {
    int day = r.getDayOfMonthIndicator();
    int dow = r.getDayOfWeek() == null ? 0 : (r.getDayOfWeek().getValue() % 7) + 1;
    p[o] = r.getMonth().getValue() - 1;
    if (dow == 0) {
      p[o + 1] = day;
      p[o + 2] = 0;
    } else if (day == -1) {
      p[o + 1] = -1;
      p[o + 2] = dow;
    } else {
      p[o + 1] = day;
      p[o + 2] = -dow;
    }
    p[o + 3] = r.getLocalTime().toSecondOfDay() * 1000;
    java.time.zone.ZoneOffsetTransitionRule.TimeDefinition d = r.getTimeDefinition();
    p[o + 4] = d == java.time.zone.ZoneOffsetTransitionRule.TimeDefinition.UTC ? 2
        : d == java.time.zone.ZoneOffsetTransitionRule.TimeDefinition.STANDARD ? 1 : 0;
  }

  private Object readResolve() {
    TimeZone tz = TimeZone.getTimeZone(getID());
    if (tz.getID().equals("GMT") && !getID().equals("GMT")) {
      SimpleTimeZone s = new SimpleTimeZone(rawOffset + rawOffsetDiff, getID());
      return s;
    }
    return tz;
  }

  @Override
  public int getOffset(int era, int year, int month, int day, int dayOfWeek, int milliseconds) {
    return rawOffset;
  }

  @Override
  public void setRawOffset(int offsetMillis) {
    rawOffset = offsetMillis;
  }

  @Override
  public int getRawOffset() {
    return rawOffset + rawOffsetDiff;
  }

  @Override
  public boolean useDaylightTime() {
    return false;
  }

  @Override
  public boolean inDaylightTime(Date date) {
    return false;
  }
}
