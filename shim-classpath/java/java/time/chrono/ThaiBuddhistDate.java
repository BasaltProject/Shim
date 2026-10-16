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

public final class ThaiBuddhistDate extends YearShiftedDate<ThaiBuddhistDate> implements ChronoLocalDate, Serializable {
  private static final long serialVersionUID = -8722293800195731463L;

  public static ThaiBuddhistDate now() {
    return now(Clock.systemDefaultZone());
  }

  public static ThaiBuddhistDate now(ZoneId zone) {
    return now(Clock.system(zone));
  }

  public static ThaiBuddhistDate now(Clock clock) {
    return new ThaiBuddhistDate(LocalDate.now(clock));
  }

  public static ThaiBuddhistDate of(int prolepticYear, int month, int dayOfMonth) {
    return ThaiBuddhistChronology.INSTANCE.date(prolepticYear, month, dayOfMonth);
  }

  public static ThaiBuddhistDate from(TemporalAccessor temporal) {
    return ThaiBuddhistChronology.INSTANCE.date(temporal);
  }

  ThaiBuddhistDate(LocalDate isoDate) {
    super(isoDate);
  }

  @Override
  int yearsDifference() {
    return -543;
  }

  @Override
  boolean validatesEra() {
    return true;
  }

  @Override
  ThaiBuddhistDate create(LocalDate isoDate) {
    return new ThaiBuddhistDate(isoDate);
  }

  @Override
  public ThaiBuddhistChronology getChronology() {
    return ThaiBuddhistChronology.INSTANCE;
  }

  @Override
  public ThaiBuddhistEra getEra() {
    return (prolepticYear() >= 1 ? ThaiBuddhistEra.BE : ThaiBuddhistEra.BEFORE_BE);
  }

  @Override
  public ThaiBuddhistDate with(TemporalAdjuster adjuster) {
    return super.with(adjuster);
  }

  @Override
  public ThaiBuddhistDate with(TemporalField field, long newValue) {
    return super.with(field, newValue);
  }

  @Override
  public ThaiBuddhistDate plus(TemporalAmount amount) {
    return super.plus(amount);
  }

  @Override
  public ThaiBuddhistDate plus(long amountToAdd, TemporalUnit unit) {
    return super.plus(amountToAdd, unit);
  }

  @Override
  public ThaiBuddhistDate minus(TemporalAmount amount) {
    return super.minus(amount);
  }

  @Override
  public ThaiBuddhistDate minus(long amountToSubtract, TemporalUnit unit) {
    return super.minus(amountToSubtract, unit);
  }

  @Override
  @SuppressWarnings("unchecked")
  public ChronoLocalDateTime<ThaiBuddhistDate> atTime(LocalTime localTime) {
    return (ChronoLocalDateTime<ThaiBuddhistDate>) super.atTime(localTime);
  }
}
