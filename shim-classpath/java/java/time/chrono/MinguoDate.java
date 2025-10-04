/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.time.chrono;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.io.Serializable;
import java.util.Objects;

public final class MinguoDate extends YearShiftedDate<MinguoDate> implements ChronoLocalDate, Serializable {
  private static final long serialVersionUID = 1300372329181994526L;

  public static MinguoDate now() {
    return now(Clock.systemDefaultZone());
  }

  public static MinguoDate now(ZoneId zone) {
    return now(Clock.system(zone));
  }

  public static MinguoDate now(Clock clock) {
    return new MinguoDate(LocalDate.now(clock));
  }

  public static MinguoDate of(int prolepticYear, int month, int dayOfMonth) {
    return MinguoChronology.INSTANCE.date(prolepticYear, month, dayOfMonth);
  }

  public static MinguoDate from(TemporalAccessor temporal) {
    return MinguoChronology.INSTANCE.date(temporal);
  }

  MinguoDate(LocalDate isoDate) {
    super(isoDate);
  }

  @Override
  int yearsDifference() {
    return 1911;
  }

  @Override
  MinguoDate create(LocalDate isoDate) {
    return new MinguoDate(isoDate);
  }

  @Override
  public MinguoChronology getChronology() {
    return MinguoChronology.INSTANCE;
  }

  @Override
  public MinguoEra getEra() {
    return (prolepticYear() >= 1 ? MinguoEra.ROC : MinguoEra.BEFORE_ROC);
  }

  @Override
  public MinguoDate with(TemporalAdjuster adjuster) {
    return super.with(adjuster);
  }

  @Override
  public MinguoDate with(TemporalField field, long newValue) {
    return super.with(field, newValue);
  }

  @Override
  public MinguoDate plus(TemporalAmount amount) {
    return super.plus(amount);
  }

  @Override
  public MinguoDate plus(long amountToAdd, TemporalUnit unit) {
    return super.plus(amountToAdd, unit);
  }

  @Override
  public MinguoDate minus(TemporalAmount amount) {
    return super.minus(amount);
  }

  @Override
  public MinguoDate minus(long amountToSubtract, TemporalUnit unit) {
    return super.minus(amountToSubtract, unit);
  }

  @Override
  @SuppressWarnings("unchecked")
  public ChronoLocalDateTime<MinguoDate> atTime(LocalTime localTime) {
    return (ChronoLocalDateTime<MinguoDate>) super.atTime(localTime);
  }
}
