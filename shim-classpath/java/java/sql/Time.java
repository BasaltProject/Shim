/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.sql;

import java.time.LocalTime;

public class Time extends java.util.Date {
  private static final long serialVersionUID = 8397324403548013681L;

  @Deprecated
  public Time(int hour, int minute, int second) {
    super(70, 0, 1, hour, minute, second);
  }

  public Time(long time) {
    super(time);
  }

  public void setTime(long time) {
    super.setTime(time);
  }

  public static Time valueOf(String s) {
    if (s == null) {
      throw new IllegalArgumentException();
    }
    int firstColon = s.indexOf(':');
    int secondColon = s.indexOf(':', firstColon + 1);
    if (firstColon > 0 && secondColon > 0 && secondColon < s.length() - 1) {
      int hour = Integer.parseInt(s.substring(0, firstColon));
      int minute = Integer.parseInt(s.substring(firstColon + 1, secondColon));
      int second = Integer.parseInt(s.substring(secondColon + 1));
      return new Time(hour, minute, second);
    }
    throw new IllegalArgumentException();
  }

  @SuppressWarnings("deprecation")
  public String toString() {
    int hour = super.getHours();
    int minute = super.getMinutes();
    int second = super.getSeconds();
    return (hour < 10 ? "0" + hour : Integer.toString(hour)) + ":" + (minute < 10 ? "0" + minute
        : Integer.toString(minute))
        + ":" + (second < 10 ? "0" + second : Integer.toString(second));
  }

  @Deprecated
  public int getYear() {
    throw new IllegalArgumentException();
  }

  @Deprecated
  public int getMonth() {
    throw new IllegalArgumentException();
  }

  @Deprecated
  public int getDay() {
    throw new IllegalArgumentException();
  }

  @Deprecated
  public int getDate() {
    throw new IllegalArgumentException();
  }

  @Deprecated
  public void setYear(int i) {
    throw new IllegalArgumentException();
  }

  @Deprecated
  public void setMonth(int i) {
    throw new IllegalArgumentException();
  }

  @Deprecated
  public void setDate(int i) {
    throw new IllegalArgumentException();
  }

  public static Time valueOf(LocalTime time) {
    return new Time(time.getHour(), time.getMinute(), time.getSecond());
  }

  @SuppressWarnings("deprecation")
  public LocalTime toLocalTime() {
    return LocalTime.of(getHours(), getMinutes(), getSeconds());
  }

  @Override
  public java.time.Instant toInstant() {
    throw new UnsupportedOperationException();
  }
}
