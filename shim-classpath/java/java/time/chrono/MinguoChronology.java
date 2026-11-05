/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.time.chrono;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.ResolverStyle;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.ValueRange;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class MinguoChronology extends AbstractChronology implements java.io.Serializable {
  private static final long serialVersionUID = 1039765215346859963L;

  public static final MinguoChronology INSTANCE = new MinguoChronology();

  private MinguoChronology() { }

  private Object readResolve() {
    return INSTANCE;
  }

  @Override
  public String getId() {
    return "Minguo";
  }

  @Override
  public boolean isIsoBased() {
    return true;
  }

  public String getCalendarType() {
    return "roc";
  }

  int yearsDifference() {
    return 1911;
  }

  MinguoDate wrapIso(LocalDate isoDate) {
    return new MinguoDate(isoDate);
  }

  @Override
  public MinguoDate date(Era era, int yearOfEra, int month, int dayOfMonth) {
    return date(prolepticYear(era, yearOfEra), month, dayOfMonth);
  }

  @Override
  public MinguoDate dateYearDay(Era era, int yearOfEra, int dayOfYear) {
    return dateYearDay(prolepticYear(era, yearOfEra), dayOfYear);
  }

  @Override
  public int prolepticYear(Era era, int yearOfEra) {
    if (!(era instanceof MinguoEra)) {
      throw new ClassCastException("Era must be MinguoEra");
    }
    return (era == MinguoEra.ROC ? yearOfEra : 1 - yearOfEra);
  }

  @Override
  public MinguoEra eraOf(int eraValue) {
    return MinguoEra.of(eraValue);
  }

  @Override
  public List<Era> eras() {
    return Arrays.<Era>asList(MinguoEra.values());
  }

  @Override
  public String getDisplayName(TextStyle style, Locale locale) {
    Objects.requireNonNull(style, "style");
    Objects.requireNonNull(locale, "locale");
    return "Minguo Calendar";
  }

  @Override
  public MinguoDate date(int prolepticYear, int month, int dayOfMonth) {
    return wrapIso(LocalDate.of(prolepticYear + yearsDifference(), month, dayOfMonth));
  }

  @Override
  public MinguoDate dateYearDay(int prolepticYear, int dayOfYear) {
    return wrapIso(LocalDate.ofYearDay(prolepticYear + yearsDifference(), dayOfYear));
  }

  @Override
  public MinguoDate dateEpochDay(long epochDay) {
    return wrapIso(LocalDate.ofEpochDay(epochDay));
  }

  @Override
  public MinguoDate dateNow() {
    return dateNow(Clock.systemDefaultZone());
  }

  @Override
  public MinguoDate dateNow(ZoneId zone) {
    return dateNow(Clock.system(zone));
  }

  @Override
  public MinguoDate dateNow(Clock clock) {
    Objects.requireNonNull(clock, "clock");
    return date(LocalDate.now(clock));
  }

  @Override
  public MinguoDate date(TemporalAccessor temporal) {
    if (temporal instanceof MinguoDate) {
      return (MinguoDate) temporal;
    }
    return wrapIso(LocalDate.from(temporal));
  }

  @Override
  public ChronoZonedDateTime<? extends ChronoLocalDate> zonedDateTime(TemporalAccessor temporal) {
    try {
      ZoneId zone = ZoneId.from(temporal);
      try {
        Instant instant = Instant.from(temporal);
        return zonedDateTime(instant, zone);
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
  public ChronoZonedDateTime<? extends ChronoLocalDate> zonedDateTime(Instant instant, ZoneId zone) {
    return ChronoZonedDateTimeImpl.ofInstant(this, instant, zone);
  }

  @Override
  public boolean isLeapYear(long prolepticYear) {
    return IsoChronology.INSTANCE.isLeapYear(prolepticYear + yearsDifference());
  }

  @Override
  public ValueRange range(ChronoField field) {
    switch (field) {
      case PROLEPTIC_MONTH: {
        ValueRange range = ChronoField.PROLEPTIC_MONTH.range();
        return ValueRange.of(range.getMinimum() - yearsDifference() * 12L,
            range.getMaximum() - yearsDifference() * 12L);
      }
      case YEAR_OF_ERA: {
        ValueRange range = ChronoField.YEAR.range();
        long a = range.getMaximum() - yearsDifference();
        long b = -range.getMinimum() + 1 + yearsDifference();
        return ValueRange.of(1, Math.min(a, b), Math.max(a, b));
      }
      case YEAR: {
        ValueRange range = ChronoField.YEAR.range();
        return ValueRange.of(range.getMinimum() - yearsDifference(), range.getMaximum() - yearsDifference());
      }
      default:
        return field.range();
    }
  }

  @Override
  public ChronoPeriod period(int years, int months, int days) {
    return new ChronoPeriodImpl(this, years, months, days);
  }

  @Override
  public MinguoDate resolveDate(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
    Long pMonth = fieldValues.remove(ChronoField.PROLEPTIC_MONTH);
    if (pMonth != null) {
      if (resolverStyle != ResolverStyle.LENIENT) {
        ChronoField.PROLEPTIC_MONTH.checkValidValue(pMonth);
      }
      put(fieldValues, ChronoField.MONTH_OF_YEAR, Math.floorMod(pMonth, 12) + 1);
      put(fieldValues, ChronoField.YEAR, Math.floorDiv(pMonth, 12));
    }
    Long yoeLong = fieldValues.remove(ChronoField.YEAR_OF_ERA);
    if (yoeLong != null) {
      if (resolverStyle != ResolverStyle.LENIENT) {
        ChronoField.YEAR_OF_ERA.checkValidValue(yoeLong);
      }
      Long era = fieldValues.remove(ChronoField.ERA);
      if (era == null) {
        Long year = fieldValues.get(ChronoField.YEAR);
        if (resolverStyle == ResolverStyle.STRICT) {
          if (year != null) {
            put(fieldValues, ChronoField.YEAR, (year > 0 ? yoeLong : Math.subtractExact(1, yoeLong)));
          } else {
            fieldValues.put(ChronoField.YEAR_OF_ERA, yoeLong);
          }
        } else {
          put(fieldValues, ChronoField.YEAR, (year == null || year > 0 ? yoeLong : Math.subtractExact(1, yoeLong)));
        }
      } else if (era.longValue() == 1L) {
        put(fieldValues, ChronoField.YEAR, yoeLong);
      } else if (era.longValue() == 0L) {
        put(fieldValues, ChronoField.YEAR, Math.subtractExact(1, yoeLong));
      } else {
        throw new DateTimeException("Invalid value for era: " + era);
      }
    } else if (fieldValues.containsKey(ChronoField.ERA)) {
      ChronoField.ERA.checkValidValue(fieldValues.get(ChronoField.ERA));
    }
    Long year = fieldValues.get(ChronoField.YEAR);
    if (year != null) {
      fieldValues.put(ChronoField.YEAR, year + yearsDifference());
    }
    LocalDate iso = IsoResolver.resolveDate(fieldValues, resolverStyle);
    if (iso == null) {
      Long shifted = fieldValues.get(ChronoField.YEAR);
      if (shifted != null) {
        fieldValues.put(ChronoField.YEAR, shifted - yearsDifference());
      }
      return null;
    }
    return wrapIso(iso);
  }

  private static void put(Map<TemporalField, Long> fieldValues, ChronoField field, long value) {
    Long old = fieldValues.get(field);
    if (old != null && old.longValue() != value) {
      throw new DateTimeException("Conflict found: " + field + " " + old + " differs from " + field + " " + value);
    }
    fieldValues.put(field, value);
  }

  @Override
  @SuppressWarnings("unchecked")
  public ChronoLocalDateTime<MinguoDate> localDateTime(java.time.temporal.TemporalAccessor temporal) {
    return (ChronoLocalDateTime<MinguoDate>) super.localDateTime(temporal);
  }
}
