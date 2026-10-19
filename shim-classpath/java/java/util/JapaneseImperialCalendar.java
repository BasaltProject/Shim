/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util;

final class JapaneseImperialCalendar extends GregorianCalendar {
  private static final long serialVersionUID = -3364572813905467929L;

  static final int BEFORE_MEIJI = 0;
  static final int MEIJI = 1;
  static final int TAISHO = 2;
  static final int SHOWA = 3;
  static final int HEISEI = 4;
  static final int REIWA = 5;

  private static final int[][] kStarts = {{0, 1, 1}, {1868, 9, 8}, {1912, 7, 30}, {1926, 12, 25}, {1989, 1, 8},
      {2019, 5, 1}};

  private transient boolean raw;
  private transient int explicitEra = -1;

  JapaneseImperialCalendar(TimeZone zone, Locale aLocale) {
    super(zone, aLocale);
  }

  @Override
  public String getCalendarType() {
    return "japanese";
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof JapaneseImperialCalendar && super.equals(obj);
  }

  @Override
  public int hashCode() {
    return super.hashCode() ^ 0x4a50;
  }

  private static boolean onOrAfter(int year, int month1, int day, int[] start) {
    if (year != start[0]) {
      return year > start[0];
    }
    if (month1 != start[1]) {
      return month1 > start[1];
    }
    return day >= start[2];
  }

  private int eraOfCurrent() {
    int gregorianEra = super.get(ERA);
    if (gregorianEra == BC) {
      return BEFORE_MEIJI;
    }
    int year = super.get(YEAR);
    int month1 = super.get(MONTH) + 1;
    int day = super.get(DAY_OF_MONTH);
    for (int era = REIWA; era > BEFORE_MEIJI; era--) {
      if (onOrAfter(year, month1, day, kStarts[era])) {
        return era;
      }
    }
    return BEFORE_MEIJI;
  }

  @Override
  public int get(int field) {
    if (raw) {
      return super.get(field);
    }
    if (field == ERA) {
      return eraOfCurrent();
    }
    if (field == YEAR) {
      int era = eraOfCurrent();
      int year = super.get(YEAR);
      return era == BEFORE_MEIJI ? year : year - kStarts[era][0] + 1;
    }
    return super.get(field);
  }

  @Override
  public void set(int field, int value) {
    if (raw) {
      super.set(field, value);
      return;
    }
    if (field == ERA) {
      if (value < BEFORE_MEIJI || value > REIWA) {
        throw new IllegalArgumentException();
      }
      int yearInEra = get(YEAR);
      explicitEra = value;
      super.set(YEAR, value == BEFORE_MEIJI ? yearInEra : yearInEra + kStarts[value][0] - 1);
      return;
    }
    if (field == YEAR) {
      int era = explicitEra >= 0 ? explicitEra : eraOfCurrent();
      super.set(YEAR, era == BEFORE_MEIJI ? value : value + kStarts[era][0] - 1);
      return;
    }
    super.set(field, value);
  }

  @Override
  public void setTimeInMillis(long millis) {
    explicitEra = -1;
    super.setTimeInMillis(millis);
  }

  @Override
  protected void computeFields() {
    explicitEra = -1;
    super.computeFields();
  }

  @Override
  public void add(int field, int amount) {
    boolean saved = raw;
    raw = true;
    try {
      super.add(field, amount);
    } finally {
      raw = saved;
    }
  }

  @Override
  public void roll(int field, int amount) {
    boolean saved = raw;
    raw = true;
    try {
      super.roll(field, amount);
    } finally {
      raw = saved;
    }
  }

  @Override
  public int getMinimum(int field) {
    return field == YEAR ? 1 : super.getMinimum(field);
  }

  @Override
  public int getGreatestMinimum(int field) {
    return field == YEAR ? 1 : super.getGreatestMinimum(field);
  }

  @Override
  public int getMaximum(int field) {
    if (field == ERA) {
      return REIWA;
    }
    return super.getMaximum(field);
  }

  @Override
  public int getLeastMaximum(int field) {
    if (field == ERA) {
      return REIWA;
    }
    if (field == YEAR) {
      return 45;
    }
    return super.getLeastMaximum(field);
  }

  @Override
  public int getActualMaximum(int field) {
    if (field == YEAR) {
      int era = eraOfCurrent();
      if (era == REIWA) {
        return super.getMaximum(YEAR) - kStarts[REIWA][0] + 1;
      }
      if (era == BEFORE_MEIJI) {
        return kStarts[MEIJI][0] - 1;
      }
      int nextStartYear = kStarts[era + 1][0];
      return nextStartYear - kStarts[era][0] + 1;
    }
    return super.getActualMaximum(field);
  }

  @Override
  public int getActualMinimum(int field) {
    return field == YEAR ? 1 : super.getActualMinimum(field);
  }

  @Override
  public Object clone() {
    JapaneseImperialCalendar copy = (JapaneseImperialCalendar) super.clone();
    copy.raw = false;
    return copy;
  }
}
