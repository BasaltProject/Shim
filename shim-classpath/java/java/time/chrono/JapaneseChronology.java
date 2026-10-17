/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.time.chrono;

import java.io.Serializable;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneId;
import java.time.format.ResolverStyle;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalField;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class JapaneseChronology extends AbstractChronology implements Serializable {
  private static final long serialVersionUID = 459996390165777884L;

  public static final JapaneseChronology INSTANCE = new JapaneseChronology();

  private JapaneseChronology() {
  }

  private Object readResolve() {
    return INSTANCE;
  }

  @Override
  public String getId() {
    return "Japanese";
  }

  @Override
  public String getCalendarType() {
    return "japanese";
  }

  @Override
  public JapaneseDate date(Era era, int yearOfEra, int month, int dayOfMonth) {
    if (!(era instanceof JapaneseEra)) {
      throw new ClassCastException("Era must be JapaneseEra");
    }
    return JapaneseDate.of((JapaneseEra) era, yearOfEra, month, dayOfMonth);
  }

  @Override
  public JapaneseDate date(int prolepticYear, int month, int dayOfMonth) {
    return new JapaneseDate(LocalDate.of(prolepticYear, month, dayOfMonth));
  }

  @Override
  public JapaneseDate dateYearDay(Era era, int yearOfEra, int dayOfYear) {
    if (!(era instanceof JapaneseEra)) {
      throw new ClassCastException("Era must be JapaneseEra");
    }
    return JapaneseDate.ofYearDay((JapaneseEra) era, yearOfEra, dayOfYear);
  }

  @Override
  public JapaneseDate dateYearDay(int prolepticYear, int dayOfYear) {
    return new JapaneseDate(LocalDate.ofYearDay(prolepticYear, dayOfYear));
  }

  @Override
  public JapaneseDate dateEpochDay(long epochDay) {
    return new JapaneseDate(LocalDate.ofEpochDay(epochDay));
  }

  @Override
  public JapaneseDate dateNow() {
    return dateNow(Clock.systemDefaultZone());
  }

  @Override
  public JapaneseDate dateNow(ZoneId zone) {
    return dateNow(Clock.system(zone));
  }

  @Override
  public JapaneseDate dateNow(Clock clock) {
    return date(LocalDate.now(clock));
  }

  @Override
  public JapaneseDate date(TemporalAccessor temporal) {
    if (temporal instanceof JapaneseDate) {
      return (JapaneseDate) temporal;
    }
    return new JapaneseDate(LocalDate.from(temporal));
  }

  @Override
  public ChronoZonedDateTime<JapaneseDate> zonedDateTime(TemporalAccessor temporal) {
    return castZoned(zonedDateTimeImpl(temporal));
  }

  @Override
  public ChronoZonedDateTime<JapaneseDate> zonedDateTime(Instant instant, ZoneId zone) {
    return castZoned(ChronoZonedDateTimeImpl.ofInstant(this, instant, zone));
  }

  @SuppressWarnings("unchecked")
  private static ChronoZonedDateTime<JapaneseDate> castZoned(ChronoZonedDateTime<?> z) {
    return (ChronoZonedDateTime<JapaneseDate>) z;
  }

  private ChronoZonedDateTime<? extends ChronoLocalDate> zonedDateTimeImpl(TemporalAccessor temporal) {
    try {
      ZoneId zone = ZoneId.from(temporal);
      try {
        Instant instant = Instant.from(temporal);
        return ChronoZonedDateTimeImpl.ofInstant(this, instant, zone);
      } catch (DateTimeException ex1) {
        @SuppressWarnings({"unchecked", "rawtypes"})
        ChronoLocalDateTimeImpl<ChronoLocalDate> cldt = (ChronoLocalDateTimeImpl) localDateTime(temporal);
        return ChronoZonedDateTimeImpl.ofBest(cldt, zone, null);
      }
    } catch (DateTimeException ex) {
      throw new DateTimeException("Unable to obtain ChronoZonedDateTime from TemporalAccessor: "
          + temporal.getClass(), ex);
    }
  }

  @Override
  public boolean isLeapYear(long prolepticYear) {
    return IsoChronology.INSTANCE.isLeapYear(prolepticYear);
  }

  @Override
  public int prolepticYear(Era era, int yearOfEra) {
    if (!(era instanceof JapaneseEra)) {
      throw new ClassCastException("Era must be JapaneseEra");
    }
    JapaneseEra jera = (JapaneseEra) era;
    int year = jera.startDate().getYear() + yearOfEra - 1;
    if (yearOfEra == 1) {
      return year;
    }
    if (year >= Year.MIN_VALUE && year <= Year.MAX_VALUE) {
      LocalDate first = LocalDate.of(year, 1, 1);
      if (!first.isBefore(jera.startDate()) && !first.isAfter(jera.endDate())) {
        return year;
      }
    }
    throw new DateTimeException("Invalid yearOfEra value");
  }

  @Override
  public JapaneseEra eraOf(int eraValue) {
    return JapaneseEra.of(eraValue);
  }

  @Override
  public List<Era> eras() {
    return Arrays.<Era>asList(JapaneseEra.values());
  }

  @Override
  public ValueRange range(ChronoField field) {
    switch (field) {
      case ALIGNED_DAY_OF_WEEK_IN_MONTH:
      case ALIGNED_DAY_OF_WEEK_IN_YEAR:
      case ALIGNED_WEEK_OF_MONTH:
      case ALIGNED_WEEK_OF_YEAR:
        throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
      case YEAR_OF_ERA:
        return ValueRange.of(1, 15, Year.MAX_VALUE - JapaneseEra.REIWA.startDate().getYear());
      case DAY_OF_YEAR:
        return ValueRange.of(1, 7, 366);
      case YEAR:
        return ValueRange.of(1873, Year.MAX_VALUE);
      case ERA:
        return ValueRange.of(JapaneseEra.MEIJI.getValue(), JapaneseEra.REIWA.getValue());
      default:
        return field.range();
    }
  }

  @Override
  public ChronoPeriod period(int years, int months, int days) {
    return new ChronoPeriodImpl(this, years, months, days);
  }

  @Override
  public JapaneseDate resolveDate(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
    if (fieldValues.containsKey(ChronoField.EPOCH_DAY)) {
      return dateEpochDay(fieldValues.remove(ChronoField.EPOCH_DAY));
    }
    Long pMonth = fieldValues.remove(ChronoField.PROLEPTIC_MONTH);
    if (pMonth != null) {
      if (resolverStyle != ResolverStyle.LENIENT) {
        ChronoField.PROLEPTIC_MONTH.checkValidValue(pMonth);
      }
      addFieldValue(fieldValues, ChronoField.MONTH_OF_YEAR, Math.floorMod(pMonth, 12) + 1);
      addFieldValue(fieldValues, ChronoField.YEAR, Math.floorDiv(pMonth, 12));
    }
    JapaneseDate resolved = resolveYearOfEra(fieldValues, resolverStyle);
    if (resolved != null) {
      return resolved;
    }
    if (fieldValues.containsKey(ChronoField.YEAR)) {
      if (fieldValues.containsKey(ChronoField.MONTH_OF_YEAR)
          && fieldValues.containsKey(ChronoField.DAY_OF_MONTH)) {
        return resolveYmd(fieldValues, resolverStyle);
      }
      if (fieldValues.containsKey(ChronoField.DAY_OF_YEAR)) {
        return resolveYd(fieldValues, resolverStyle);
      }
    }
    return null;
  }

  private static void addFieldValue(Map<TemporalField, Long> fieldValues, ChronoField field, long value) {
    Long old = fieldValues.get(field);
    if (old != null && old.longValue() != value) {
      throw new DateTimeException("Conflict found: " + field + " " + old + " differs from " + field + " "
          + value);
    }
    fieldValues.put(field, value);
  }

  private JapaneseDate resolveYmd(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
    int y = range(ChronoField.YEAR).checkValidIntValue(fieldValues.remove(ChronoField.YEAR), ChronoField.YEAR);
    if (resolverStyle == ResolverStyle.LENIENT) {
      long months = Math.subtractExact(fieldValues.remove(ChronoField.MONTH_OF_YEAR), 1);
      long days = Math.subtractExact(fieldValues.remove(ChronoField.DAY_OF_MONTH), 1);
      return date(y, 1, 1).plus(months, ChronoUnit.MONTHS).plus(days, ChronoUnit.DAYS);
    }
    int moy = range(ChronoField.MONTH_OF_YEAR).checkValidIntValue(fieldValues.remove(ChronoField.MONTH_OF_YEAR),
        ChronoField.MONTH_OF_YEAR);
    ValueRange domRange = range(ChronoField.DAY_OF_MONTH);
    int dom = domRange.checkValidIntValue(fieldValues.remove(ChronoField.DAY_OF_MONTH), ChronoField.DAY_OF_MONTH);
    if (resolverStyle == ResolverStyle.SMART) {
      try {
        return date(y, moy, dom);
      } catch (DateTimeException ex) {
        return date(y, moy, 1).with(TemporalAdjusters.lastDayOfMonth());
      }
    }
    return date(y, moy, dom);
  }

  private JapaneseDate resolveYd(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
    int y = range(ChronoField.YEAR).checkValidIntValue(fieldValues.remove(ChronoField.YEAR), ChronoField.YEAR);
    if (resolverStyle == ResolverStyle.LENIENT) {
      long days = Math.subtractExact(fieldValues.remove(ChronoField.DAY_OF_YEAR), 1);
      return dateYearDay(y, 1).plus(days, ChronoUnit.DAYS);
    }
    int doy = range(ChronoField.DAY_OF_YEAR).checkValidIntValue(fieldValues.remove(ChronoField.DAY_OF_YEAR),
        ChronoField.DAY_OF_YEAR);
    return dateYearDay(y, doy);
  }

  private JapaneseDate resolveYearOfEra(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
    Long eraLong = fieldValues.get(ChronoField.ERA);
    JapaneseEra era = null;
    if (eraLong != null) {
      era = eraOf(range(ChronoField.ERA).checkValidIntValue(eraLong, ChronoField.ERA));
    }
    Long yoeLong = fieldValues.get(ChronoField.YEAR_OF_ERA);
    if (yoeLong != null) {
      int yoe = range(ChronoField.YEAR_OF_ERA).checkValidIntValue(yoeLong, ChronoField.YEAR_OF_ERA);
      if (era == null && resolverStyle != ResolverStyle.STRICT && !fieldValues.containsKey(ChronoField.YEAR)) {
        List<Era> eras = eras();
        era = (JapaneseEra) eras.get(eras.size() - 1);
      }
      if (era != null && fieldValues.containsKey(ChronoField.MONTH_OF_YEAR)
          && fieldValues.containsKey(ChronoField.DAY_OF_MONTH)) {
        fieldValues.remove(ChronoField.ERA);
        fieldValues.remove(ChronoField.YEAR_OF_ERA);
        return resolveEraYmd(era, yoe, fieldValues, resolverStyle);
      }
      if (era != null && fieldValues.containsKey(ChronoField.DAY_OF_YEAR)) {
        fieldValues.remove(ChronoField.ERA);
        fieldValues.remove(ChronoField.YEAR_OF_ERA);
        return resolveEraYd(era, yoe, fieldValues, resolverStyle);
      }
    }
    return null;
  }

  private JapaneseDate resolveEraYmd(JapaneseEra era, int yoe, Map<TemporalField, Long> fieldValues,
      ResolverStyle resolverStyle) {
    if (resolverStyle == ResolverStyle.LENIENT) {
      int y = era.startDate().getYear() + yoe - 1;
      long months = Math.subtractExact(fieldValues.remove(ChronoField.MONTH_OF_YEAR), 1);
      long days = Math.subtractExact(fieldValues.remove(ChronoField.DAY_OF_MONTH), 1);
      return date(y, 1, 1).plus(months, ChronoUnit.MONTHS).plus(days, ChronoUnit.DAYS);
    }
    int moy = range(ChronoField.MONTH_OF_YEAR).checkValidIntValue(fieldValues.remove(ChronoField.MONTH_OF_YEAR),
        ChronoField.MONTH_OF_YEAR);
    int dom = range(ChronoField.DAY_OF_MONTH).checkValidIntValue(fieldValues.remove(ChronoField.DAY_OF_MONTH),
        ChronoField.DAY_OF_MONTH);
    if (resolverStyle == ResolverStyle.SMART) {
      if (yoe < 1) {
        throw new DateTimeException("Invalid YearOfEra: " + yoe);
      }
      int y = era.startDate().getYear() + yoe - 1;
      if (dom > 28) {
        dom = Math.min(dom, date(y, moy, 1).lengthOfMonth());
      }
      JapaneseDate jd = date(y, moy, dom);
      if (jd.getEra() != era && Math.abs(jd.getEra().getValue() - era.getValue()) > 1) {
        throw new DateTimeException("Invalid Era/YearOfEra: " + era + " " + yoe);
      }
      if (jd.getEra() == era && jd.get(ChronoField.YEAR_OF_ERA) != yoe
          && Math.abs(jd.get(ChronoField.YEAR_OF_ERA) - yoe) > 1) {
        throw new DateTimeException("Invalid Era/YearOfEra: " + era + " " + yoe);
      }
      return jd;
    }
    return date(era, yoe, moy, dom);
  }

  private JapaneseDate resolveEraYd(JapaneseEra era, int yoe, Map<TemporalField, Long> fieldValues,
      ResolverStyle resolverStyle) {
    if (resolverStyle == ResolverStyle.LENIENT) {
      int y = era.startDate().getYear() + yoe - 1;
      long days = Math.subtractExact(fieldValues.remove(ChronoField.DAY_OF_YEAR), 1);
      return dateYearDay(y, 1).plus(days, ChronoUnit.DAYS);
    }
    int doy = range(ChronoField.DAY_OF_YEAR).checkValidIntValue(fieldValues.remove(ChronoField.DAY_OF_YEAR),
        ChronoField.DAY_OF_YEAR);
    return dateYearDay(era, yoe, doy);
  }

  @Override
  public String getDisplayName(TextStyle style, Locale locale) {
    Objects.requireNonNull(style, "style");
    Objects.requireNonNull(locale, "locale");
    return "Japanese Calendar";
  }
}
