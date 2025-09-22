/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util;

public class SimpleTimeZone extends TimeZone {
  private static final long serialVersionUID = -403250971215465050L;

  public static final int WALL_TIME = 0;
  public static final int STANDARD_TIME = 1;
  public static final int UTC_TIME = 2;

  private static final int DOM_MODE = 1;
  private static final int DOW_IN_MONTH_MODE = 2;
  private static final int DOW_GE_DOM_MODE = 3;
  private static final int DOW_LE_DOM_MODE = 4;
  private static final int millisPerHour = 60 * 60 * 1000;
  private static final int millisPerDay = 24 * millisPerHour;
  private static final byte[] staticMonthLength = {31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

  private int startMonth;
  private int startDay;
  private int startDayOfWeek;
  private int startTime;
  private int startTimeMode;
  private int endMonth;
  private int endDay;
  private int endDayOfWeek;
  private int endTime;
  private int endTimeMode;
  private int startYear;
  private int rawOffset;
  private boolean useDaylight = false;
  private int startMode;
  private int endMode;
  private int dstSavings;

  public SimpleTimeZone(int rawOffset, String ID) {
    this.rawOffset = rawOffset;
    setID(ID);
    dstSavings = millisPerHour;
  }

  public SimpleTimeZone(int rawOffset, String ID, int startMonth, int startDay, int startDayOfWeek, int startTime,
      int endMonth, int endDay, int endDayOfWeek, int endTime) {
    this(rawOffset, ID, startMonth, startDay, startDayOfWeek, startTime, WALL_TIME, endMonth, endDay, endDayOfWeek,
        endTime, WALL_TIME, millisPerHour);
  }

  public SimpleTimeZone(int rawOffset, String ID, int startMonth, int startDay, int startDayOfWeek, int startTime,
      int endMonth, int endDay, int endDayOfWeek, int endTime, int dstSavings) {
    this(rawOffset, ID, startMonth, startDay, startDayOfWeek, startTime, WALL_TIME, endMonth, endDay, endDayOfWeek,
        endTime, WALL_TIME, dstSavings);
  }

  public SimpleTimeZone(int rawOffset, String ID, int startMonth, int startDay, int startDayOfWeek, int startTime,
      int startTimeMode, int endMonth, int endDay, int endDayOfWeek, int endTime, int endTimeMode, int dstSavings) {
    setID(ID);
    this.rawOffset = rawOffset;
    this.startMonth = startMonth;
    this.startDay = startDay;
    this.startDayOfWeek = startDayOfWeek;
    this.startTime = startTime;
    this.startTimeMode = startTimeMode;
    this.endMonth = endMonth;
    this.endDay = endDay;
    this.endDayOfWeek = endDayOfWeek;
    this.endTime = endTime;
    this.endTimeMode = endTimeMode;
    this.dstSavings = dstSavings;
    this.startYear = 0;
    if (dstSavings <= 0) {
      throw new IllegalArgumentException("Illegal daylight saving value: " + dstSavings);
    }
    decodeRules();
  }

  public void setStartYear(int year) {
    startYear = year;
  }

  public void setStartRule(int startMonth, int startDay, int startDayOfWeek, int startTime) {
    this.startMonth = startMonth;
    this.startDay = startDay;
    this.startDayOfWeek = startDayOfWeek;
    this.startTime = startTime;
    startTimeMode = WALL_TIME;
    decodeStartRule();
  }

  public void setStartRule(int startMonth, int startDay, int startTime) {
    setStartRule(startMonth, startDay, 0, startTime);
  }

  public void setStartRule(int startMonth, int startDay, int startDayOfWeek, int startTime, boolean after) {
    if (after) {
      setStartRule(startMonth, startDay, -startDayOfWeek, startTime);
    } else {
      setStartRule(startMonth, -startDay, -startDayOfWeek, startTime);
    }
  }

  public void setEndRule(int endMonth, int endDay, int endDayOfWeek, int endTime) {
    this.endMonth = endMonth;
    this.endDay = endDay;
    this.endDayOfWeek = endDayOfWeek;
    this.endTime = endTime;
    this.endTimeMode = WALL_TIME;
    decodeEndRule();
  }

  public void setEndRule(int endMonth, int endDay, int endTime) {
    setEndRule(endMonth, endDay, 0, endTime);
  }

  public void setEndRule(int endMonth, int endDay, int endDayOfWeek, int endTime, boolean after) {
    if (after) {
      setEndRule(endMonth, endDay, -endDayOfWeek, endTime);
    } else {
      setEndRule(endMonth, -endDay, -endDayOfWeek, endTime);
    }
  }

  private static boolean isLeap(long year) {
    return (year & 3) == 0 && (year % 100 != 0 || year % 400 == 0);
  }

  private static int monthLength(int year, int month) {
    return month == 1 ? (isLeap(year) ? 29 : 28) : new int[] {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31}[month];
  }

  private static long fixedDate(long year, int month, int day) {
    long days = 365L * (year - 1) + Math.floorDiv(year - 1, 4L) - Math.floorDiv(year - 1, 100L)
        + Math.floorDiv(year - 1, 400L);
    int[] cumulative = {0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334};
    days += cumulative[month] + (month > 1 && isLeap(year) ? 1 : 0) + day;
    return days;
  }

  private static int dayOfWeek(long fixedDate) {
    return (int) Math.floorMod(fixedDate, 7L) + 1;
  }

  private static final long kEpochOffset = 719163L;

  private long transitionDay(int year, int month, int day, int dayOfWeek, int mode) {
    int len = monthLength(year, month);
    long first = fixedDate(year, month, 1);
    long result;
    switch (mode) {
      case DOM_MODE:
        result = first + day - 1;
        break;
      case DOW_IN_MONTH_MODE:
        if (day > 0) {
          long target = first + 7L * (day - 1);
          int dow = dayOfWeek(target);
          result = target + Math.floorMod(dayOfWeek - dow, 7);
        } else {
          long last = first + len - 1;
          long target = last + 7L * (day + 1);
          int dow = dayOfWeek(target);
          result = target - Math.floorMod(dow - dayOfWeek, 7);
        }
        break;
      case DOW_GE_DOM_MODE: {
        long target = first + day - 1;
        int dow = dayOfWeek(target);
        result = target + Math.floorMod(dayOfWeek - dow, 7);
        break;
      }
      default: {
        long target = first + day - 1;
        int dow = dayOfWeek(target);
        result = target - Math.floorMod(dow - dayOfWeek, 7);
        break;
      }
    }
    return result;
  }

  private long startStd(int year) {
    long day = transitionDay(year, startMonth, startDay, startDayOfWeek, startMode);
    long ms = (day - kEpochOffset) * millisPerDay + startTime;
    if (startTimeMode == UTC_TIME) {
      ms += rawOffset;
    }
    return ms;
  }

  private long endStd(int year) {
    long day = transitionDay(year, endMonth, endDay, endDayOfWeek, endMode);
    long ms = (day - kEpochOffset) * millisPerDay + endTime;
    if (endTimeMode == UTC_TIME) {
      ms += rawOffset;
    } else if (endTimeMode == WALL_TIME) {
      ms -= dstSavings;
    }
    return ms;
  }

  private boolean dstAtStandardLocal(long t, int year) {
    if (!useDaylight || year < startYear) {
      return false;
    }
    long s = startStd(year);
    long e = endStd(year);
    if (s <= e) {
      return t >= s && t < e;
    }
    return t >= s || t < e;
  }

  private static int yearOf(long localMs) {
    long day = Math.floorDiv(localMs, (long) millisPerDay) + kEpochOffset;
    return (int) java.time.LocalDate.ofEpochDay(day - kEpochOffset).getYear();
  }

  @Override
  public int getOffset(long date) {
    long t = date + rawOffset;
    int year = yearOf(t);
    return rawOffset + (dstAtStandardLocal(t, year) ? dstSavings : 0);
  }

  @Override
  public int getOffset(int era, int year, int month, int day, int dayOfWeek, int millis) {
    if (era != GregorianCalendar.AD && era != GregorianCalendar.BC) {
      throw new IllegalArgumentException("Illegal era " + era);
    }
    int y = era == GregorianCalendar.BC ? 1 - year : year;
    if (month < 0 || month > 11) {
      throw new IllegalArgumentException("Illegal month " + month);
    }
    if (day < 1 || day > monthLength(y, month)) {
      throw new IllegalArgumentException("Illegal day of month " + day);
    }
    if (dayOfWeek < 1 || dayOfWeek > 7) {
      throw new IllegalArgumentException("Illegal day of week " + dayOfWeek);
    }
    if (millis < 0 || millis >= millisPerDay) {
      throw new IllegalArgumentException("Illegal milliseconds " + millis);
    }
    if (!useDaylight || y < startYear) {
      return rawOffset;
    }
    long wall = (fixedDate(y, month, day) - kEpochOffset) * millisPerDay + millis;
    long s = startStd(y);
    long e = endStd(y) + dstSavings;
    boolean dst;
    if (s <= e) {
      dst = wall >= s && wall < e;
    } else {
      dst = wall >= s || wall < e;
    }
    return rawOffset + (dst ? dstSavings : 0);
  }

  @Override
  public int getRawOffset() {
    return rawOffset;
  }

  @Override
  public void setRawOffset(int offsetMillis) {
    this.rawOffset = offsetMillis;
  }

  public void setDSTSavings(int millisSavedDuringDST) {
    if (millisSavedDuringDST <= 0) {
      throw new IllegalArgumentException("Illegal daylight saving value: " + millisSavedDuringDST);
    }
    dstSavings = millisSavedDuringDST;
  }

  @Override
  public int getDSTSavings() {
    return useDaylight ? dstSavings : 0;
  }

  @Override
  public boolean useDaylightTime() {
    return useDaylight;
  }

  @Override
  public boolean observesDaylightTime() {
    return useDaylightTime();
  }

  @Override
  public boolean inDaylightTime(Date date) {
    long t = date.getTime() + rawOffset;
    return dstAtStandardLocal(t, yearOf(t));
  }

  @Override
  public Object clone() {
    return super.clone();
  }

  @Override
  public int hashCode() {
    return startMonth ^ startDay ^ startDayOfWeek ^ startTime ^ endMonth ^ endDay ^ endDayOfWeek ^ endTime
        ^ rawOffset;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof SimpleTimeZone)) {
      return false;
    }
    SimpleTimeZone that = (SimpleTimeZone) obj;
    return getID().equals(that.getID()) && hasSameRules(that);
  }

  @Override
  public boolean hasSameRules(TimeZone other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof SimpleTimeZone)) {
      return false;
    }
    SimpleTimeZone that = (SimpleTimeZone) other;
    return rawOffset == that.rawOffset && useDaylight == that.useDaylight
        && (!useDaylight || (dstSavings == that.dstSavings && startMode == that.startMode
            && startMonth == that.startMonth && startDay == that.startDay && startDayOfWeek == that.startDayOfWeek
            && startTime == that.startTime && startTimeMode == that.startTimeMode && endMode == that.endMode
            && endMonth == that.endMonth && endDay == that.endDay && endDayOfWeek == that.endDayOfWeek
            && endTime == that.endTime && endTimeMode == that.endTimeMode && startYear == that.startYear));
  }

  @Override
  public String toString() {
    return getClass().getName() + "[id=" + getID() + ",offset=" + rawOffset + ",dstSavings=" + dstSavings
        + ",useDaylight=" + useDaylight + ",startYear=" + startYear + ",startMode=" + startMode + ",startMonth="
        + startMonth + ",startDay=" + startDay + ",startDayOfWeek=" + startDayOfWeek + ",startTime=" + startTime
        + ",startTimeMode=" + startTimeMode + ",endMode=" + endMode + ",endMonth=" + endMonth + ",endDay=" + endDay
        + ",endDayOfWeek=" + endDayOfWeek + ",endTime=" + endTime + ",endTimeMode=" + endTimeMode + ']';
  }

  private void decodeRules() {
    decodeStartRule();
    decodeEndRule();
  }

  private void decodeStartRule() {
    useDaylight = (startDay != 0) && (endDay != 0);
    if (startDay != 0) {
      if (startMonth < Calendar.JANUARY || startMonth > Calendar.DECEMBER) {
        throw new IllegalArgumentException("Illegal start month " + startMonth);
      }
      if (startTime < 0 || startTime > millisPerDay) {
        throw new IllegalArgumentException("Illegal start time " + startTime);
      }
      if (startDayOfWeek == 0) {
        startMode = DOM_MODE;
      } else {
        if (startDayOfWeek > 0) {
          startMode = DOW_IN_MONTH_MODE;
        } else {
          startDayOfWeek = -startDayOfWeek;
          if (startDay > 0) {
            startMode = DOW_GE_DOM_MODE;
          } else {
            startDay = -startDay;
            startMode = DOW_LE_DOM_MODE;
          }
        }
        if (startDayOfWeek > Calendar.SATURDAY) {
          throw new IllegalArgumentException("Illegal start day of week " + startDayOfWeek);
        }
      }
      if (startMode == DOW_IN_MONTH_MODE) {
        if (startDay < -5 || startDay > 5) {
          throw new IllegalArgumentException("Illegal start day of week in month " + startDay);
        }
      } else if (startDay < 1 || startDay > staticMonthLength[startMonth]) {
        throw new IllegalArgumentException("Illegal start day " + startDay);
      }
    }
  }

  private void decodeEndRule() {
    useDaylight = (startDay != 0) && (endDay != 0);
    if (endDay != 0) {
      if (endMonth < Calendar.JANUARY || endMonth > Calendar.DECEMBER) {
        throw new IllegalArgumentException("Illegal end month " + endMonth);
      }
      if (endTime < 0 || endTime > millisPerDay) {
        throw new IllegalArgumentException("Illegal end time " + endTime);
      }
      if (endDayOfWeek == 0) {
        endMode = DOM_MODE;
      } else {
        if (endDayOfWeek > 0) {
          endMode = DOW_IN_MONTH_MODE;
        } else {
          endDayOfWeek = -endDayOfWeek;
          if (endDay > 0) {
            endMode = DOW_GE_DOM_MODE;
          } else {
            endDay = -endDay;
            endMode = DOW_LE_DOM_MODE;
          }
        }
        if (endDayOfWeek > Calendar.SATURDAY) {
          throw new IllegalArgumentException("Illegal end day of week " + endDayOfWeek);
        }
      }
      if (endMode == DOW_IN_MONTH_MODE) {
        if (endDay < -5 || endDay > 5) {
          throw new IllegalArgumentException("Illegal end day of week in month " + endDay);
        }
      } else if (endDay < 1 || endDay > staticMonthLength[endMonth]) {
        throw new IllegalArgumentException("Illegal end day " + endDay);
      }
    }
  }
}
