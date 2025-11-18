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
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.ResolverStyle;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.ValueRange;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public interface Chronology extends Comparable<Chronology> {
  default boolean isIsoBased() {
    return false;
  }

  static Chronology from(TemporalAccessor temporal) {
    Objects.requireNonNull(temporal, "temporal");
    Chronology obj = temporal.query(TemporalQueries.chronology());
    return (obj != null ? obj : IsoChronology.INSTANCE);
  }

  static Chronology ofLocale(Locale locale) {
    Objects.requireNonNull(locale, "locale");
    String type = locale.getUnicodeLocaleType("ca");
    if (type == null || "iso".equals(type) || "iso8601".equals(type)) {
      return IsoChronology.INSTANCE;
    }
    Chronology chrono = AbstractChronology.byType(type);
    if (chrono == null) {
      throw new DateTimeException("Unknown calendar system: " + type);
    }
    return chrono;
  }

  static Chronology of(String id) {
    Objects.requireNonNull(id, "id");
    Chronology chrono = AbstractChronology.byId(id);
    if (chrono == null) {
      chrono = AbstractChronology.byType(id);
    }
    if (chrono == null) {
      throw new DateTimeException("Unknown chronology: " + id);
    }
    return chrono;
  }

  static Set<Chronology> getAvailableChronologies() {
    return new HashSet<Chronology>(AbstractChronology.all());
  }

  String getId();

  String getCalendarType();

  default ChronoLocalDate date(Era era, int yearOfEra, int month, int dayOfMonth) {
    return date(prolepticYear(era, yearOfEra), month, dayOfMonth);
  }

  ChronoLocalDate date(int prolepticYear, int month, int dayOfMonth);

  default ChronoLocalDate dateYearDay(Era era, int yearOfEra, int dayOfYear) {
    return dateYearDay(prolepticYear(era, yearOfEra), dayOfYear);
  }

  ChronoLocalDate dateYearDay(int prolepticYear, int dayOfYear);

  ChronoLocalDate dateEpochDay(long epochDay);

  default ChronoLocalDate dateNow() {
    return dateNow(Clock.systemDefaultZone());
  }

  default ChronoLocalDate dateNow(ZoneId zone) {
    return dateNow(Clock.system(zone));
  }

  default ChronoLocalDate dateNow(Clock clock) {
    Objects.requireNonNull(clock, "clock");
    return date(LocalDate.now(clock));
  }

  ChronoLocalDate date(TemporalAccessor temporal);

  default ChronoLocalDateTime<? extends ChronoLocalDate> localDateTime(TemporalAccessor temporal) {
    try {
      return date(temporal).atTime(LocalTime.from(temporal));
    } catch (DateTimeException ex) {
      throw new DateTimeException("Unable to obtain ChronoLocalDateTime from TemporalAccessor: "
          + temporal.getClass(), ex);
    }
  }

  ChronoZonedDateTime<? extends ChronoLocalDate> zonedDateTime(TemporalAccessor temporal);

  ChronoZonedDateTime<? extends ChronoLocalDate> zonedDateTime(Instant instant, ZoneId zone);

  boolean isLeapYear(long prolepticYear);

  int prolepticYear(Era era, int yearOfEra);

  Era eraOf(int eraValue);

  List<Era> eras();

  ValueRange range(ChronoField field);

  default String getDisplayName(TextStyle style, Locale locale) {
    return getId();
  }

  ChronoLocalDate resolveDate(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle);

  ChronoPeriod period(int years, int months, int days);

  default long epochSecond(int prolepticYear, int month, int dayOfMonth, int hour, int minute, int second,
      ZoneOffset zoneOffset) {
    Objects.requireNonNull(zoneOffset, "zoneOffset");
    ChronoField.HOUR_OF_DAY.checkValidValue(hour);
    ChronoField.MINUTE_OF_HOUR.checkValidValue(minute);
    ChronoField.SECOND_OF_MINUTE.checkValidValue(second);
    long daysInSec = Math.multiplyExact(date(prolepticYear, month, dayOfMonth).toEpochDay(), 86400L);
    long timeinSec = (hour * 60 + minute) * 60 + second;
    return Math.addExact(daysInSec, timeinSec - zoneOffset.getTotalSeconds());
  }

  default long epochSecond(Era era, int yearOfEra, int month, int dayOfMonth, int hour, int minute, int second,
      ZoneOffset zoneOffset) {
    Objects.requireNonNull(era, "era");
    return epochSecond(prolepticYear(era, yearOfEra), month, dayOfMonth, hour, minute, second, zoneOffset);
  }

  @Override
  int compareTo(Chronology other);

  @Override
  boolean equals(Object obj);

  @Override
  int hashCode();

  @Override
  String toString();
}
