/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package sun.util;

import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class BuddhistCalendar extends GregorianCalendar {
  private static final long serialVersionUID = -8527488697350388578L;

  private static final int BUDDHIST_YEAR_OFFSET = 543;

  private transient int yearOffset = BUDDHIST_YEAR_OFFSET;

  public BuddhistCalendar() {
    super();
  }

  public BuddhistCalendar(TimeZone zone, Locale aLocale) {
    super(zone, aLocale);
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof BuddhistCalendar && super.equals(obj);
  }

  @Override
  public int hashCode() {
    return super.hashCode() ^ BUDDHIST_YEAR_OFFSET;
  }

  @Override
  public int get(int field) {
    int value = super.get(field);
    if (field == YEAR) {
      value += yearOffset;
    }
    return value;
  }

  @Override
  public void set(int field, int value) {
    super.set(field, field == YEAR ? value - yearOffset : value);
  }

  @Override
  public void add(int field, int amount) {
    int saved = yearOffset;
    yearOffset = 0;
    try {
      super.add(field, amount);
    } finally {
      yearOffset = saved;
    }
  }

  @Override
  public void roll(int field, int amount) {
    int saved = yearOffset;
    yearOffset = 0;
    try {
      super.roll(field, amount);
    } finally {
      yearOffset = saved;
    }
  }

  @Override
  public String getCalendarType() {
    return "buddhist";
  }

  @Override
  public int getActualMaximum(int field) {
    int value = super.getActualMaximum(field);
    return field == YEAR ? value + yearOffset : value;
  }

  @Override
  public int getActualMinimum(int field) {
    int value = super.getActualMinimum(field);
    return field == YEAR ? value + yearOffset : value;
  }

  @Override
  public int getMaximum(int field) {
    int value = super.getMaximum(field);
    return field == YEAR ? value + yearOffset : value;
  }

  @Override
  public int getMinimum(int field) {
    int value = super.getMinimum(field);
    return field == YEAR ? value + yearOffset : value;
  }

  @Override
  public int getGreatestMinimum(int field) {
    int value = super.getGreatestMinimum(field);
    return field == YEAR ? value + yearOffset : value;
  }

  @Override
  public int getLeastMaximum(int field) {
    int value = super.getLeastMaximum(field);
    return field == YEAR ? value + yearOffset : value;
  }

  @Override
  public Object clone() {
    BuddhistCalendar copy = (BuddhistCalendar) super.clone();
    copy.yearOffset = BUDDHIST_YEAR_OFFSET;
    return copy;
  }
}
