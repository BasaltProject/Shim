/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.time.chrono;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;
import java.util.Objects;

final class ChronoLocalDateTimeImpl<D extends ChronoLocalDate> implements ChronoLocalDateTime<D>, Temporal,
    Serializable {
  private static final long serialVersionUID = 4556003607393004514L;

  private final D date;
  private final LocalTime time;

  static <R extends ChronoLocalDate> ChronoLocalDateTimeImpl<R> of(R date, LocalTime time) {
    return new ChronoLocalDateTimeImpl<R>(date, time);
  }

  private ChronoLocalDateTimeImpl(D date, LocalTime time) {
    Objects.requireNonNull(date, "date");
    Objects.requireNonNull(time, "time");
    this.date = date;
    this.time = time;
  }

  @SuppressWarnings("unchecked")
  private ChronoLocalDateTimeImpl<D> with(Temporal newDate, LocalTime newTime) {
    if (date == newDate && time == newTime) {
      return this;
    }
    D cd = (D) ChronoLocalDateOps.ensureValid(date.getChronology(), newDate);
    return new ChronoLocalDateTimeImpl<D>(cd, newTime);
  }

  LocalDateTime iso() {
    return LocalDateTime.of(LocalDate.ofEpochDay(date.toEpochDay()), time);
  }

  @SuppressWarnings("unchecked")
  private ChronoLocalDateTimeImpl<D> fromIso(LocalDateTime ldt) {
    D newDate = (D) date.getChronology().dateEpochDay(ldt.toLocalDate().toEpochDay());
    return new ChronoLocalDateTimeImpl<D>(newDate, ldt.toLocalTime());
  }

  @Override
  public D toLocalDate() {
    return date;
  }

  @Override
  public LocalTime toLocalTime() {
    return time;
  }

  @Override
  public boolean isSupported(TemporalField field) {
    if (field instanceof ChronoField) {
      ChronoField f = (ChronoField) field;
      return f.isDateBased() || f.isTimeBased();
    }
    return field != null && field.isSupportedBy(this);
  }

  @Override
  public ValueRange range(TemporalField field) {
    if (field instanceof ChronoField) {
      ChronoField f = (ChronoField) field;
      return (f.isTimeBased() ? time.range(field) : date.range(field));
    }
    return field.rangeRefinedBy(this);
  }

  @Override
  public int get(TemporalField field) {
    if (field instanceof ChronoField) {
      ChronoField f = (ChronoField) field;
      return (f.isTimeBased() ? time.get(field) : date.get(field));
    }
    return range(field).checkValidIntValue(getLong(field), field);
  }

  @Override
  public long getLong(TemporalField field) {
    if (field instanceof ChronoField) {
      ChronoField f = (ChronoField) field;
      return (f.isTimeBased() ? time.getLong(field) : date.getLong(field));
    }
    return field.getFrom(this);
  }

  @Override
  @SuppressWarnings("unchecked")
  public ChronoLocalDateTimeImpl<D> with(TemporalField field, long newValue) {
    if (field instanceof ChronoField) {
      ChronoField f = (ChronoField) field;
      if (f.isTimeBased()) {
        return with(date, time.with(field, newValue));
      } else {
        return with(date.with(field, newValue), time);
      }
    }
    return (ChronoLocalDateTimeImpl<D>) (field.adjustInto(this, newValue));
  }

  @Override
  @SuppressWarnings("unchecked")
  public ChronoLocalDateTimeImpl<D> plus(long amountToAdd, TemporalUnit unit) {
    if (unit instanceof ChronoUnit) {
      ChronoUnit f = (ChronoUnit) unit;
      if (f.isTimeBased()) {
        return fromIso(iso().plus(amountToAdd, unit));
      }
      return with(date.plus(amountToAdd, unit), time);
    }
    return (ChronoLocalDateTimeImpl<D>) (unit.addTo(this, amountToAdd));
  }

  @Override
  public long until(Temporal endExclusive, TemporalUnit unit) {
    Objects.requireNonNull(endExclusive, "endExclusive");
    @SuppressWarnings("unchecked")
    ChronoLocalDateTime<D> end = (ChronoLocalDateTime<D>) date.getChronology().localDateTime(endExclusive);
    if (unit instanceof ChronoUnit) {
      if (unit.isTimeBased()) {
        long amount = end.toLocalDate().toEpochDay() - date.toEpochDay();
        switch ((ChronoUnit) unit) {
          case NANOS:
            amount = Math.multiplyExact(amount, 86400L * 1000000000L);
            break;
          case MICROS:
            amount = Math.multiplyExact(amount, 86400L * 1000000L);
            break;
          case MILLIS:
            amount = Math.multiplyExact(amount, 86400L * 1000L);
            break;
          case SECONDS:
            amount = Math.multiplyExact(amount, 86400L);
            break;
          case MINUTES:
            amount = Math.multiplyExact(amount, 1440L);
            break;
          case HOURS:
            amount = Math.multiplyExact(amount, 24L);
            break;
          case HALF_DAYS:
            amount = Math.multiplyExact(amount, 2L);
            break;
          default:
            break;
        }
        return Math.addExact(amount, time.until(end.toLocalTime(), unit));
      }
      ChronoLocalDate endDate = end.toLocalDate();
      if (end.toLocalTime().isBefore(time)) {
        endDate = endDate.minus(1, ChronoUnit.DAYS);
      }
      return date.until(endDate, unit);
    }
    Objects.requireNonNull(unit, "unit");
    return unit.between(this, end);
  }

  @Override
  public ChronoZonedDateTime<D> atZone(ZoneId zone) {
    return ChronoZonedDateTimeImpl.ofBest(this, zone, null);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof ChronoLocalDateTime) {
      return compareTo((ChronoLocalDateTime<?>) obj) == 0;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return toLocalDate().hashCode() ^ toLocalTime().hashCode();
  }

  @Override
  public String toString() {
    return toLocalDate().toString() + 'T' + toLocalTime().toString();
  }
}
