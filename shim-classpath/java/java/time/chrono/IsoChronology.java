/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.time.chrono;

import java.io.Serializable;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.ValueRange;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class IsoChronology extends AbstractChronology implements Serializable {
  private static final long serialVersionUID = -1440403870442975015L;

  public static final IsoChronology INSTANCE = new IsoChronology();

  private IsoChronology() { }

  @Override
  public String getId() {
    return "ISO";
  }

  @Override
  public boolean isIsoBased() {
    return true;
  }

  public String getCalendarType() {
    return "iso8601";
  }

  @Override
  public LocalDate date(Era era, int yearOfEra, int month, int dayOfMonth) {
    return date(prolepticYear(era, yearOfEra), month, dayOfMonth);
  }

  @Override
  public LocalDate date(int prolepticYear, int month, int dayOfMonth) {
    return LocalDate.of(prolepticYear, month, dayOfMonth);
  }

  @Override
  public LocalDate dateYearDay(Era era, int yearOfEra, int dayOfYear) {
    return dateYearDay(prolepticYear(era, yearOfEra), dayOfYear);
  }

  @Override
  public LocalDate dateYearDay(int prolepticYear, int dayOfYear) {
    return LocalDate.ofYearDay(prolepticYear, dayOfYear);
  }

  @Override
  public LocalDate dateEpochDay(long epochDay) {
    return LocalDate.ofEpochDay(epochDay);
  }

  @Override
  public LocalDate dateNow() {
    return dateNow(Clock.systemDefaultZone());
  }

  @Override
  public LocalDate dateNow(ZoneId zone) {
    return dateNow(Clock.system(zone));
  }

  @Override
  public LocalDate dateNow(Clock clock) {
    Objects.requireNonNull(clock, "clock");
    return date(LocalDate.now(clock));
  }

  @Override
  public LocalDate date(TemporalAccessor temporal) {
    return LocalDate.from(temporal);
  }

  @Override
  public LocalDateTime localDateTime(TemporalAccessor temporal) {
    return LocalDateTime.from(temporal);
  }

  @Override
  public ZonedDateTime zonedDateTime(TemporalAccessor temporal) {
    return ZonedDateTime.from(temporal);
  }

  @Override
  public ZonedDateTime zonedDateTime(Instant instant, ZoneId zone) {
    return ZonedDateTime.ofInstant(instant, zone);
  }

  @Override
  public boolean isLeapYear(long prolepticYear) {
    return ((prolepticYear & 3) == 0) && ((prolepticYear % 100) != 0 || (prolepticYear % 400) == 0);
  }

  @Override
  public int prolepticYear(Era era, int yearOfEra) {
    if (!(era instanceof IsoEra)) {
      throw new ClassCastException("Era must be IsoEra");
    }
    return (era == IsoEra.CE ? yearOfEra : 1 - yearOfEra);
  }

  @Override
  public IsoEra eraOf(int eraValue) {
    return IsoEra.of(eraValue);
  }

  @Override
  public List<Era> eras() {
    return Arrays.<Era>asList(IsoEra.values());
  }

  @Override
  public ValueRange range(ChronoField field) {
    return field.range();
  }

  @Override
  public Period period(int years, int months, int days) {
    return Period.of(years, months, days);
  }

  @Override
  public LocalDate resolveDate(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
    return IsoResolver.resolveDate(fieldValues, resolverStyle);
  }
}
