/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.sql;

import java.time.Instant;
import java.time.LocalDateTime;

public class Timestamp extends java.util.Date {
  private static final long serialVersionUID = 2745179027874758501L;
  private static final int MILLIS_PER_SECOND = 1000;

  private int nanos;

  @Deprecated
  public Timestamp(int year, int month, int date, int hour, int minute, int second, int nano) {
    super(year, month, date, hour, minute, second);
    if (nano > 999999999 || nano < 0) {
      throw new IllegalArgumentException("nanos > 999999999 or < 0");
    }
    nanos = nano;
  }

  public Timestamp(long time) {
    super((time / 1000) * 1000);
    nanos = (int) ((time % 1000) * 1000000);
    if (nanos < 0) {
      nanos = 1000000000 + nanos;
      super.setTime(((time / 1000) - 1) * 1000);
    }
  }

  public void setTime(long time) {
    super.setTime((time / 1000) * 1000);
    nanos = (int) ((time % 1000) * 1000000);
    if (nanos < 0) {
      nanos = 1000000000 + nanos;
      super.setTime(((time / 1000) - 1) * 1000);
    }
  }

  public long getTime() {
    long time = super.getTime();
    return time + (nanos / 1000000);
  }

  public static Timestamp valueOf(String s) {
    final String formatError = "Timestamp format must be yyyy-mm-dd hh:mm:ss[.fffffffff]";
    if (s == null) {
      throw new IllegalArgumentException("null string");
    }
    s = s.trim();
    int firstSpace = s.indexOf(' ');
    if (firstSpace <= 0) {
      throw new IllegalArgumentException(formatError);
    }
    String dateS = s.substring(0, firstSpace);
    String timeS = s.substring(firstSpace + 1);
    int year = 0;
    int month = 0;
    int day = 0;
    boolean dateOk = false;
    int firstDash = dateS.indexOf('-');
    int secondDash = dateS.indexOf('-', firstDash + 1);
    if (firstDash > 0 && secondDash > 0 && secondDash < dateS.length() - 1) {
      String yyyy = dateS.substring(0, firstDash);
      String mm = dateS.substring(firstDash + 1, secondDash);
      String dd = dateS.substring(secondDash + 1);
      if (yyyy.length() == 4 && mm.length() >= 1 && mm.length() <= 2 && dd.length() >= 1 && dd.length() <= 2) {
        year = Integer.parseInt(yyyy);
        month = Integer.parseInt(mm);
        day = Integer.parseInt(dd);
        if (month >= 1 && month <= 12 && day >= 1 && day <= 31) {
          dateOk = true;
        }
      }
    }
    if (!dateOk) {
      throw new IllegalArgumentException(formatError);
    }
    int hour;
    int minute;
    int second;
    int aNanos = 0;
    int firstColon = timeS.indexOf(':');
    int secondColon = timeS.indexOf(':', firstColon + 1);
    int period = timeS.indexOf('.', secondColon + 1);
    if (firstColon > 0 && secondColon > 0 && secondColon < timeS.length() - 1) {
      hour = Integer.parseInt(timeS.substring(0, firstColon));
      minute = Integer.parseInt(timeS.substring(firstColon + 1, secondColon));
      if (period > 0 && period < timeS.length() - 1) {
        second = Integer.parseInt(timeS.substring(secondColon + 1, period));
        String nanosS = timeS.substring(period + 1);
        if (nanosS.length() > 9) {
          throw new IllegalArgumentException(formatError);
        }
        if (!Character.isDigit(nanosS.charAt(0))) {
          throw new IllegalArgumentException(formatError);
        }
        nanosS = nanosS + "000000000".substring(0, 9 - nanosS.length());
        aNanos = Integer.parseInt(nanosS);
      } else if (period > 0) {
        throw new IllegalArgumentException(formatError);
      } else {
        second = Integer.parseInt(timeS.substring(secondColon + 1));
      }
    } else {
      throw new IllegalArgumentException(formatError);
    }
    return new Timestamp(year - 1900, month - 1, day, hour, minute, second, aNanos);
  }

  @SuppressWarnings("deprecation")
  public String toString() {
    int year = super.getYear() + 1900;
    int month = super.getMonth() + 1;
    int day = super.getDate();
    int hour = super.getHours();
    int minute = super.getMinutes();
    int second = super.getSeconds();
    String yearString = Integer.toString(year);
    if (year < 1000) {
      yearString = "0000".substring(yearString.length()) + yearString;
    }
    String nanosString;
    if (nanos == 0) {
      nanosString = "0";
    } else {
      nanosString = Integer.toString(nanos);
      nanosString = "000000000".substring(nanosString.length()) + nanosString;
      int end = nanosString.length();
      while (end > 1 && nanosString.charAt(end - 1) == '0') {
        end--;
      }
      nanosString = nanosString.substring(0, end);
    }
    return yearString + "-" + two(month) + "-" + two(day) + " " + two(hour) + ":" + two(minute) + ":" + two(second)
        + "." + nanosString;
  }

  private static String two(int value) {
    return value < 10 ? "0" + value : Integer.toString(value);
  }

  public int getNanos() {
    return nanos;
  }

  public void setNanos(int n) {
    if (n > 999999999 || n < 0) {
      throw new IllegalArgumentException("nanos > 999999999 or < 0");
    }
    nanos = n;
  }

  public boolean equals(Timestamp ts) {
    if (super.equals(ts)) {
      return nanos == ts.nanos;
    }
    return false;
  }

  public boolean equals(Object ts) {
    if (ts instanceof Timestamp) {
      return this.equals((Timestamp) ts);
    }
    return false;
  }

  public boolean before(Timestamp ts) {
    return compareTo(ts) < 0;
  }

  public boolean after(Timestamp ts) {
    return compareTo(ts) > 0;
  }

  public int compareTo(Timestamp ts) {
    long thisTime = this.getTime();
    long anotherTime = ts.getTime();
    int i = (thisTime < anotherTime ? -1 : (thisTime == anotherTime ? 0 : 1));
    if (i == 0) {
      if (nanos > ts.nanos) {
        return 1;
      } else if (nanos < ts.nanos) {
        return -1;
      }
    }
    return i;
  }

  public int compareTo(java.util.Date o) {
    if (o instanceof Timestamp) {
      return compareTo((Timestamp) o);
    }
    Timestamp ts = new Timestamp(o.getTime());
    return this.compareTo(ts);
  }

  public int hashCode() {
    return super.hashCode();
  }

  @SuppressWarnings("deprecation")
  public static Timestamp valueOf(LocalDateTime dateTime) {
    return new Timestamp(dateTime.getYear() - 1900, dateTime.getMonthValue() - 1, dateTime.getDayOfMonth(),
        dateTime.getHour(), dateTime.getMinute(), dateTime.getSecond(), dateTime.getNano());
  }

  @SuppressWarnings("deprecation")
  public LocalDateTime toLocalDateTime() {
    return LocalDateTime.of(getYear() + 1900, getMonth() + 1, getDate(), getHours(), getMinutes(), getSeconds(),
        getNanos());
  }

  public static Timestamp from(Instant instant) {
    try {
      Timestamp stamp = new Timestamp(Math.multiplyExact(instant.getEpochSecond(), (long) MILLIS_PER_SECOND));
      stamp.nanos = instant.getNano();
      return stamp;
    } catch (ArithmeticException ex) {
      throw new IllegalArgumentException(ex);
    }
  }

  @Override
  public Instant toInstant() {
    return Instant.ofEpochSecond(super.getTime() / MILLIS_PER_SECOND, nanos);
  }
}
