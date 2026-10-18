/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.time.chrono;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class ThaiBuddhistChronology extends YearShiftedChronology {
  private static final long serialVersionUID = 2775954514031616474L;

  public static final ThaiBuddhistChronology INSTANCE = new ThaiBuddhistChronology();

  private ThaiBuddhistChronology() { }

  private Object readResolve() {
    return INSTANCE;
  }

  @Override
  public String getId() {
    return "ThaiBuddhist";
  }

  @Override
  public boolean isIsoBased() {
    return true;
  }

  public String getCalendarType() {
    return "buddhist";
  }

  @Override
  int yearsDifference() {
    return -543;
  }

  @Override
  ChronoLocalDate wrapIso(LocalDate isoDate) {
    return new ThaiBuddhistDate(isoDate);
  }

  @Override
  public ThaiBuddhistDate date(Era era, int yearOfEra, int month, int dayOfMonth) {
    return date(prolepticYear(era, yearOfEra), month, dayOfMonth);
  }

  @Override
  public ThaiBuddhistDate date(int prolepticYear, int month, int dayOfMonth) {
    return (ThaiBuddhistDate) super.date(prolepticYear, month, dayOfMonth);
  }

  @Override
  public ThaiBuddhistDate dateYearDay(Era era, int yearOfEra, int dayOfYear) {
    return dateYearDay(prolepticYear(era, yearOfEra), dayOfYear);
  }

  @Override
  public ThaiBuddhistDate dateYearDay(int prolepticYear, int dayOfYear) {
    return (ThaiBuddhistDate) super.dateYearDay(prolepticYear, dayOfYear);
  }

  @Override
  public ThaiBuddhistDate dateEpochDay(long epochDay) {
    return (ThaiBuddhistDate) super.dateEpochDay(epochDay);
  }

  @Override
  public ThaiBuddhistDate dateNow() {
    return (ThaiBuddhistDate) super.dateNow();
  }

  @Override
  public ThaiBuddhistDate dateNow(java.time.ZoneId zone) {
    return (ThaiBuddhistDate) super.dateNow(zone);
  }

  @Override
  public ThaiBuddhistDate dateNow(java.time.Clock clock) {
    return (ThaiBuddhistDate) super.dateNow(clock);
  }

  @Override
  public ThaiBuddhistDate date(java.time.temporal.TemporalAccessor temporal) {
    return (ThaiBuddhistDate) super.date(temporal);
  }

  @Override
  public int prolepticYear(Era era, int yearOfEra) {
    if (!(era instanceof ThaiBuddhistEra)) {
      throw new ClassCastException("Era must be BuddhistEra");
    }
    return (era == ThaiBuddhistEra.BE ? yearOfEra : 1 - yearOfEra);
  }

  @Override
  public ThaiBuddhistEra eraOf(int eraValue) {
    return ThaiBuddhistEra.of(eraValue);
  }

  @Override
  public List<Era> eras() {
    return Arrays.<Era>asList(ThaiBuddhistEra.values());
  }

  @Override
  public String getDisplayName(TextStyle style, Locale locale) {
    Objects.requireNonNull(style, "style");
    Objects.requireNonNull(locale, "locale");
    return "Buddhist Calendar";
  }
}
