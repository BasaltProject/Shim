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

package java.text;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

public class DateFormatSymbols implements Serializable, Cloneable {
  private static final long serialVersionUID = -5987973545549424702L;

  private String[] eras = new String[] {"BC", "AD"};
  private String[] months = new String[] {
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December", ""
  };
  private String[] shortMonths = new String[] {
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec", ""
  };
  private String[] weekdays = new String[] {
    "", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
  };
  private String[] shortWeekdays = new String[] {
    "", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"
  };
  private String[] ampm = new String[] {"AM", "PM"};
  private String[][] zoneStrings = new String[][] {
    {"GMT", "Greenwich Mean Time", "GMT", "Greenwich Mean Time", "GMT"}
  };
  private String localPatternChars = "GyMdkHmsSEDFwWahKzZ";

  public DateFormatSymbols() {
    this(Locale.getDefault());
  }

  public DateFormatSymbols(Locale locale) { }

  public static DateFormatSymbols getInstance() {
    return new DateFormatSymbols();
  }

  public static DateFormatSymbols getInstance(Locale locale) {
    return new DateFormatSymbols(locale);
  }

  public String[] getEras() {
    return eras.clone();
  }

  public void setEras(String[] newEras) {
    eras = newEras.clone();
  }

  public String[] getMonths() {
    return months.clone();
  }

  public void setMonths(String[] newMonths) {
    months = newMonths.clone();
  }

  public String[] getShortMonths() {
    return shortMonths.clone();
  }

  public void setShortMonths(String[] newShortMonths) {
    shortMonths = newShortMonths.clone();
  }

  public String[] getWeekdays() {
    return weekdays.clone();
  }

  public void setWeekdays(String[] newWeekdays) {
    weekdays = newWeekdays.clone();
  }

  public String[] getShortWeekdays() {
    return shortWeekdays.clone();
  }

  public void setShortWeekdays(String[] newShortWeekdays) {
    shortWeekdays = newShortWeekdays.clone();
  }

  public String[] getAmPmStrings() {
    return ampm.clone();
  }

  public void setAmPmStrings(String[] v) {
    ampm = v.clone();
  }

  public String[][] getZoneStrings() {
    String[][] copy = new String[zoneStrings.length][];
    for (int i = 0; i < zoneStrings.length; i++) {
      copy[i] = zoneStrings[i].clone();
    }
    return copy;
  }

  public void setZoneStrings(String[][] newZoneStrings) {
    String[][] copy = new String[newZoneStrings.length][];
    for (int i = 0; i < newZoneStrings.length; i++) {
      copy[i] = newZoneStrings[i].clone();
    }
    zoneStrings = copy;
  }

  public String getLocalPatternChars() {
    return localPatternChars;
  }

  public void setLocalPatternChars(String newLocalPatternChars) {
    localPatternChars = Objects.requireNonNull(newLocalPatternChars, "newLocalPatternChars");
  }

  @Override
  public Object clone() {
    try {
      DateFormatSymbols copy = (DateFormatSymbols) super.clone();
      copy.eras = eras.clone();
      copy.months = months.clone();
      copy.shortMonths = shortMonths.clone();
      copy.weekdays = weekdays.clone();
      copy.shortWeekdays = shortWeekdays.clone();
      copy.ampm = ampm.clone();
      copy.zoneStrings = getZoneStrings();
      return copy;
    } catch (CloneNotSupportedException e) {
      throw new InternalError(e);
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof DateFormatSymbols)) return false;
    DateFormatSymbols other = (DateFormatSymbols) obj;
    return Arrays.equals(eras, other.eras)
        && Arrays.equals(months, other.months)
        && Arrays.equals(shortMonths, other.shortMonths)
        && Arrays.equals(weekdays, other.weekdays)
        && Arrays.equals(shortWeekdays, other.shortWeekdays)
        && Arrays.equals(ampm, other.ampm)
        && Objects.equals(localPatternChars, other.localPatternChars);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(months);
  }
}
