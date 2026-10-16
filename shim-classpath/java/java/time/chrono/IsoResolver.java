/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.time.chrono;

import static java.time.temporal.ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH;
import static java.time.temporal.ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR;
import static java.time.temporal.ChronoField.ALIGNED_WEEK_OF_MONTH;
import static java.time.temporal.ChronoField.ALIGNED_WEEK_OF_YEAR;
import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.DAY_OF_WEEK;
import static java.time.temporal.ChronoField.DAY_OF_YEAR;
import static java.time.temporal.ChronoField.EPOCH_DAY;
import static java.time.temporal.ChronoField.ERA;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.PROLEPTIC_MONTH;
import static java.time.temporal.ChronoField.YEAR;
import static java.time.temporal.ChronoField.YEAR_OF_ERA;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalField;
import java.util.Map;

final class IsoResolver {
  private IsoResolver() { }

  static LocalDate resolveDate(Map<TemporalField, Long> fieldValues, ResolverStyle style) {
    if (fieldValues.containsKey(EPOCH_DAY)) {
      return LocalDate.ofEpochDay(fieldValues.remove(EPOCH_DAY));
    }
    resolveProlepticMonth(fieldValues, style);
    LocalDate resolved = resolveYearOfEra(fieldValues, style);
    if (resolved != null) {
      return resolved;
    }
    if (fieldValues.containsKey(YEAR)) {
      if (fieldValues.containsKey(MONTH_OF_YEAR)) {
        if (fieldValues.containsKey(DAY_OF_MONTH)) {
          return resolveYMD(fieldValues, style);
        }
        if (fieldValues.containsKey(ALIGNED_WEEK_OF_MONTH)) {
          if (fieldValues.containsKey(ALIGNED_DAY_OF_WEEK_IN_MONTH)) {
            return resolveYMAA(fieldValues, style);
          }
          if (fieldValues.containsKey(DAY_OF_WEEK)) {
            return resolveYMAD(fieldValues, style);
          }
        }
      }
      if (fieldValues.containsKey(DAY_OF_YEAR)) {
        return resolveYD(fieldValues, style);
      }
      if (fieldValues.containsKey(ALIGNED_WEEK_OF_YEAR)) {
        if (fieldValues.containsKey(ALIGNED_DAY_OF_WEEK_IN_YEAR)) {
          return resolveYAA(fieldValues, style);
        }
        if (fieldValues.containsKey(DAY_OF_WEEK)) {
          return resolveYAD(fieldValues, style);
        }
      }
    }
    return null;
  }

  private static void addFieldValue(Map<TemporalField, Long> fieldValues, TemporalField field, long value) {
    Long old = fieldValues.get(field);
    if (old != null && old.longValue() != value) {
      throw new DateTimeException("Conflict found: " + field + " " + old + " differs from " + field + " " + value);
    }
    fieldValues.put(field, value);
  }

  private static void resolveProlepticMonth(Map<TemporalField, Long> fieldValues, ResolverStyle style) {
    Long pMonth = fieldValues.remove(PROLEPTIC_MONTH);
    if (pMonth != null) {
      if (style != ResolverStyle.LENIENT) {
        PROLEPTIC_MONTH.checkValidValue(pMonth);
      }
      addFieldValue(fieldValues, MONTH_OF_YEAR, Math.floorMod(pMonth, 12) + 1);
      addFieldValue(fieldValues, YEAR, Math.floorDiv(pMonth, 12));
    }
  }

  private static LocalDate resolveYearOfEra(Map<TemporalField, Long> fieldValues, ResolverStyle style) {
    Long yoeLong = fieldValues.remove(YEAR_OF_ERA);
    if (yoeLong != null) {
      if (style != ResolverStyle.LENIENT) {
        YEAR_OF_ERA.checkValidValue(yoeLong);
      }
      Long era = fieldValues.remove(ERA);
      if (era == null) {
        Long year = fieldValues.get(YEAR);
        if (style == ResolverStyle.STRICT) {
          if (year != null) {
            addFieldValue(fieldValues, YEAR, (year > 0 ? yoeLong : Math.subtractExact(1, yoeLong)));
          } else {
            fieldValues.put(YEAR_OF_ERA, yoeLong);
          }
        } else {
          addFieldValue(fieldValues, YEAR, (year == null || year > 0 ? yoeLong : Math.subtractExact(1, yoeLong)));
        }
      } else if (era.longValue() == 1L) {
        addFieldValue(fieldValues, YEAR, yoeLong);
      } else if (era.longValue() == 0L) {
        addFieldValue(fieldValues, YEAR, Math.subtractExact(1, yoeLong));
      } else {
        throw new DateTimeException("Invalid value for era: " + era);
      }
    } else if (fieldValues.containsKey(ERA)) {
      ERA.checkValidValue(fieldValues.get(ERA));
    }
    return null;
  }

  private static LocalDate resolveYMD(Map<TemporalField, Long> fieldValues, ResolverStyle style) {
    int y = YEAR.checkValidIntValue(fieldValues.remove(YEAR));
    if (style == ResolverStyle.LENIENT) {
      long months = Math.subtractExact(fieldValues.remove(MONTH_OF_YEAR), 1);
      long days = Math.subtractExact(fieldValues.remove(DAY_OF_MONTH), 1);
      return LocalDate.of(y, 1, 1).plusMonths(months).plusDays(days);
    }
    int moy = MONTH_OF_YEAR.checkValidIntValue(fieldValues.remove(MONTH_OF_YEAR));
    int dom = DAY_OF_MONTH.checkValidIntValue(fieldValues.remove(DAY_OF_MONTH));
    if (style == ResolverStyle.SMART) {
      if (moy == 4 || moy == 6 || moy == 9 || moy == 11) {
        dom = Math.min(dom, 30);
      } else if (moy == 2) {
        dom = Math.min(dom, Month.FEBRUARY.length(IsoChronology.INSTANCE.isLeapYear(y)));
      }
    }
    return LocalDate.of(y, moy, dom);
  }

  private static LocalDate resolveYD(Map<TemporalField, Long> fieldValues, ResolverStyle style) {
    int y = YEAR.checkValidIntValue(fieldValues.remove(YEAR));
    if (style == ResolverStyle.LENIENT) {
      long days = Math.subtractExact(fieldValues.remove(DAY_OF_YEAR), 1);
      return LocalDate.ofYearDay(y, 1).plusDays(days);
    }
    int doy = DAY_OF_YEAR.checkValidIntValue(fieldValues.remove(DAY_OF_YEAR));
    return LocalDate.ofYearDay(y, doy);
  }

  private static LocalDate resolveYMAA(Map<TemporalField, Long> fieldValues, ResolverStyle style) {
    int y = YEAR.checkValidIntValue(fieldValues.remove(YEAR));
    if (style == ResolverStyle.LENIENT) {
      long months = Math.subtractExact(fieldValues.remove(MONTH_OF_YEAR), 1);
      long weeks = Math.subtractExact(fieldValues.remove(ALIGNED_WEEK_OF_MONTH), 1);
      long days = Math.subtractExact(fieldValues.remove(ALIGNED_DAY_OF_WEEK_IN_MONTH), 1);
      return LocalDate.of(y, 1, 1).plusMonths(months).plusWeeks(weeks).plusDays(days);
    }
    int moy = MONTH_OF_YEAR.checkValidIntValue(fieldValues.remove(MONTH_OF_YEAR));
    int aw = ALIGNED_WEEK_OF_MONTH.checkValidIntValue(fieldValues.remove(ALIGNED_WEEK_OF_MONTH));
    int ad = ALIGNED_DAY_OF_WEEK_IN_MONTH.checkValidIntValue(fieldValues.remove(ALIGNED_DAY_OF_WEEK_IN_MONTH));
    LocalDate date = LocalDate.of(y, moy, 1).plusDays((aw - 1) * 7 + (ad - 1));
    if (style == ResolverStyle.STRICT && date.get(MONTH_OF_YEAR) != moy) {
      throw new DateTimeException("Strict mode rejected resolved date as it is in a different month");
    }
    return date;
  }

  private static LocalDate resolveYMAD(Map<TemporalField, Long> fieldValues, ResolverStyle style) {
    int y = YEAR.checkValidIntValue(fieldValues.remove(YEAR));
    if (style == ResolverStyle.LENIENT) {
      long months = Math.subtractExact(fieldValues.remove(MONTH_OF_YEAR), 1);
      long weeks = Math.subtractExact(fieldValues.remove(ALIGNED_WEEK_OF_MONTH), 1);
      long dow = Math.subtractExact(fieldValues.remove(DAY_OF_WEEK), 1);
      return resolveAligned(LocalDate.of(y, 1, 1), months, weeks, dow);
    }
    int moy = MONTH_OF_YEAR.checkValidIntValue(fieldValues.remove(MONTH_OF_YEAR));
    int aw = ALIGNED_WEEK_OF_MONTH.checkValidIntValue(fieldValues.remove(ALIGNED_WEEK_OF_MONTH));
    int dow = DAY_OF_WEEK.checkValidIntValue(fieldValues.remove(DAY_OF_WEEK));
    LocalDate date = LocalDate.of(y, moy, 1).plusDays((aw - 1) * 7)
        .with(TemporalAdjusters.nextOrSame(DayOfWeek.of(dow)));
    if (style == ResolverStyle.STRICT && date.get(MONTH_OF_YEAR) != moy) {
      throw new DateTimeException("Strict mode rejected resolved date as it is in a different month");
    }
    return date;
  }

  private static LocalDate resolveYAA(Map<TemporalField, Long> fieldValues, ResolverStyle style) {
    int y = YEAR.checkValidIntValue(fieldValues.remove(YEAR));
    if (style == ResolverStyle.LENIENT) {
      long weeks = Math.subtractExact(fieldValues.remove(ALIGNED_WEEK_OF_YEAR), 1);
      long days = Math.subtractExact(fieldValues.remove(ALIGNED_DAY_OF_WEEK_IN_YEAR), 1);
      return LocalDate.ofYearDay(y, 1).plusWeeks(weeks).plusDays(days);
    }
    int aw = ALIGNED_WEEK_OF_YEAR.checkValidIntValue(fieldValues.remove(ALIGNED_WEEK_OF_YEAR));
    int ad = ALIGNED_DAY_OF_WEEK_IN_YEAR.checkValidIntValue(fieldValues.remove(ALIGNED_DAY_OF_WEEK_IN_YEAR));
    LocalDate date = LocalDate.ofYearDay(y, 1).plusDays((aw - 1) * 7 + (ad - 1));
    if (style == ResolverStyle.STRICT && date.get(YEAR) != y) {
      throw new DateTimeException("Strict mode rejected resolved date as it is in a different year");
    }
    return date;
  }

  private static LocalDate resolveYAD(Map<TemporalField, Long> fieldValues, ResolverStyle style) {
    int y = YEAR.checkValidIntValue(fieldValues.remove(YEAR));
    if (style == ResolverStyle.LENIENT) {
      long weeks = Math.subtractExact(fieldValues.remove(ALIGNED_WEEK_OF_YEAR), 1);
      long dow = Math.subtractExact(fieldValues.remove(DAY_OF_WEEK), 1);
      return resolveAligned(LocalDate.ofYearDay(y, 1), 0, weeks, dow);
    }
    int aw = ALIGNED_WEEK_OF_YEAR.checkValidIntValue(fieldValues.remove(ALIGNED_WEEK_OF_YEAR));
    int dow = DAY_OF_WEEK.checkValidIntValue(fieldValues.remove(DAY_OF_WEEK));
    LocalDate date = LocalDate.ofYearDay(y, 1).plusDays((aw - 1) * 7)
        .with(TemporalAdjusters.nextOrSame(DayOfWeek.of(dow)));
    if (style == ResolverStyle.STRICT && date.get(YEAR) != y) {
      throw new DateTimeException("Strict mode rejected resolved date as it is in a different year");
    }
    return date;
  }

  private static LocalDate resolveAligned(LocalDate base, long months, long weeks, long dow) {
    LocalDate date = base.plus(months, ChronoUnit.MONTHS).plus(weeks, ChronoUnit.WEEKS);
    if (dow > 7) {
      date = date.plus((dow - 1) / 7, ChronoUnit.WEEKS);
      dow = ((dow - 1) % 7) + 1;
    } else if (dow < 1) {
      date = date.plus(Math.subtractExact(dow, 7) / 7, ChronoUnit.WEEKS);
      dow = ((int) dow + 6) % 7 + 1;
    }
    return date.with(TemporalAdjusters.nextOrSame(DayOfWeek.of((int) dow)));
  }
}
