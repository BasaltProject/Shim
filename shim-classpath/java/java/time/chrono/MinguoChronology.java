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

public final class MinguoChronology extends YearShiftedChronology {
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
  public String getCalendarType() {
    return "roc";
  }

  @Override
  int yearsDifference() {
    return 1911;
  }

  @Override
  ChronoLocalDate wrapIso(LocalDate isoDate) {
    return new MinguoDate(isoDate);
  }

  @Override
  public MinguoDate date(Era era, int yearOfEra, int month, int dayOfMonth) {
    return date(prolepticYear(era, yearOfEra), month, dayOfMonth);
  }

  @Override
  public MinguoDate date(int prolepticYear, int month, int dayOfMonth) {
    return (MinguoDate) super.date(prolepticYear, month, dayOfMonth);
  }

  @Override
  public MinguoDate dateYearDay(Era era, int yearOfEra, int dayOfYear) {
    return dateYearDay(prolepticYear(era, yearOfEra), dayOfYear);
  }

  @Override
  public MinguoDate dateYearDay(int prolepticYear, int dayOfYear) {
    return (MinguoDate) super.dateYearDay(prolepticYear, dayOfYear);
  }

  @Override
  public MinguoDate dateEpochDay(long epochDay) {
    return (MinguoDate) super.dateEpochDay(epochDay);
  }

  @Override
  public MinguoDate dateNow() {
    return (MinguoDate) super.dateNow();
  }

  @Override
  public MinguoDate dateNow(java.time.ZoneId zone) {
    return (MinguoDate) super.dateNow(zone);
  }

  @Override
  public MinguoDate dateNow(java.time.Clock clock) {
    return (MinguoDate) super.dateNow(clock);
  }

  @Override
  public MinguoDate date(java.time.temporal.TemporalAccessor temporal) {
    return (MinguoDate) super.date(temporal);
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
}
