/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.time.chrono;

import static java.time.temporal.ChronoField.EPOCH_DAY;
import static java.time.temporal.ChronoField.NANO_OF_DAY;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;
import java.time.temporal.TemporalUnit;
import java.util.Comparator;
import java.util.Objects;

public interface ChronoLocalDateTime<D extends ChronoLocalDate>
    extends Temporal, TemporalAdjuster, Comparable<ChronoLocalDateTime<?>> {
  static Comparator<ChronoLocalDateTime<?>> timeLineOrder() {
    return new Comparator<ChronoLocalDateTime<?>>() {
      public int compare(ChronoLocalDateTime<?> dateTime1, ChronoLocalDateTime<?> dateTime2) {
        int cmp = Long.compare(dateTime1.toLocalDate().toEpochDay(), dateTime2.toLocalDate().toEpochDay());
        if (cmp == 0) {
          cmp = Long.compare(dateTime1.toLocalTime().toNanoOfDay(), dateTime2.toLocalTime().toNanoOfDay());
        }
        return cmp;
      }
    };
  }

  static ChronoLocalDateTime<?> from(TemporalAccessor temporal) {
    if (temporal instanceof ChronoLocalDateTime) {
      return (ChronoLocalDateTime<?>) temporal;
    }
    Objects.requireNonNull(temporal, "temporal");
    return java.time.LocalDateTime.from(temporal);
  }

  default Chronology getChronology() {
    return toLocalDate().getChronology();
  }

  D toLocalDate();

  LocalTime toLocalTime();

  @Override
  boolean isSupported(TemporalField field);

  @Override
  default boolean isSupported(TemporalUnit unit) {
    if (unit instanceof ChronoUnit) {
      return unit != ChronoUnit.FOREVER;
    }
    return unit != null && unit.isSupportedBy(this);
  }

  @Override
  ChronoLocalDateTime<D> with(TemporalField field, long newValue);

  @SuppressWarnings("unchecked")
  @Override
  default ChronoLocalDateTime<D> with(TemporalAdjuster adjuster) {
    return (ChronoLocalDateTime<D>) Temporal.super.with(adjuster);
  }

  @SuppressWarnings("unchecked")
  @Override
  default ChronoLocalDateTime<D> plus(TemporalAmount amount) {
    return (ChronoLocalDateTime<D>) Temporal.super.plus(amount);
  }

  @SuppressWarnings("unchecked")
  @Override
  default ChronoLocalDateTime<D> minus(TemporalAmount amount) {
    return (ChronoLocalDateTime<D>) Temporal.super.minus(amount);
  }

  @SuppressWarnings("unchecked")
  @Override
  default ChronoLocalDateTime<D> minus(long amountToSubtract, TemporalUnit unit) {
    return (ChronoLocalDateTime<D>) Temporal.super.minus(amountToSubtract, unit);
  }

  @Override
  ChronoLocalDateTime<D> plus(long amountToAdd, TemporalUnit unit);

  @SuppressWarnings("unchecked")
  @Override
  default <R> R query(TemporalQuery<R> query) {
    if (query == TemporalQueries.zoneId() || query == TemporalQueries.zone() || query == TemporalQueries.offset()) {
      return null;
    } else if (query == TemporalQueries.localTime()) {
      return (R) toLocalTime();
    } else if (query == TemporalQueries.chronology()) {
      return (R) getChronology();
    } else if (query == TemporalQueries.precision()) {
      return (R) ChronoUnit.NANOS;
    }
    return query.queryFrom(this);
  }

  @Override
  default Temporal adjustInto(Temporal temporal) {
    return temporal.with(EPOCH_DAY, toLocalDate().toEpochDay()).with(NANO_OF_DAY, toLocalTime().toNanoOfDay());
  }

  default String format(DateTimeFormatter formatter) {
    Objects.requireNonNull(formatter, "formatter");
    return formatter.format(this);
  }

  ChronoZonedDateTime<D> atZone(ZoneId zone);

  default Instant toInstant(ZoneOffset offset) {
    return Instant.ofEpochSecond(toEpochSecond(offset), toLocalTime().getNano());
  }

  default long toEpochSecond(ZoneOffset offset) {
    Objects.requireNonNull(offset, "offset");
    long epochDay = toLocalDate().toEpochDay();
    long secs = epochDay * 86400 + toLocalTime().toSecondOfDay();
    secs -= offset.getTotalSeconds();
    return secs;
  }

  @Override
  default int compareTo(ChronoLocalDateTime<?> other) {
    int cmp = toLocalDate().compareTo(other.toLocalDate());
    if (cmp == 0) {
      cmp = toLocalTime().compareTo(other.toLocalTime());
      if (cmp == 0) {
        cmp = getChronology().compareTo(other.getChronology());
      }
    }
    return cmp;
  }

  default boolean isAfter(ChronoLocalDateTime<?> other) {
    long thisEpDay = this.toLocalDate().toEpochDay();
    long otherEpDay = other.toLocalDate().toEpochDay();
    return thisEpDay > otherEpDay
        || (thisEpDay == otherEpDay && this.toLocalTime().toNanoOfDay() > other.toLocalTime().toNanoOfDay());
  }

  default boolean isBefore(ChronoLocalDateTime<?> other) {
    long thisEpDay = this.toLocalDate().toEpochDay();
    long otherEpDay = other.toLocalDate().toEpochDay();
    return thisEpDay < otherEpDay
        || (thisEpDay == otherEpDay && this.toLocalTime().toNanoOfDay() < other.toLocalTime().toNanoOfDay());
  }

  default boolean isEqual(ChronoLocalDateTime<?> other) {
    return this.toLocalTime().toNanoOfDay() == other.toLocalTime().toNanoOfDay()
        && this.toLocalDate().toEpochDay() == other.toLocalDate().toEpochDay();
  }

  @Override
  boolean equals(Object obj);

  @Override
  int hashCode();

  @Override
  String toString();
}
