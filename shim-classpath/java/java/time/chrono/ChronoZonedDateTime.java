/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.time.chrono;

import static java.time.temporal.ChronoField.INSTANT_SECONDS;
import static java.time.temporal.ChronoField.NANO_OF_SECOND;
import static java.time.temporal.ChronoField.OFFSET_SECONDS;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;
import java.time.temporal.TemporalUnit;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;
import java.util.Comparator;
import java.util.Objects;

public interface ChronoZonedDateTime<D extends ChronoLocalDate> extends Temporal, Comparable<ChronoZonedDateTime<?>> {
  static Comparator<ChronoZonedDateTime<?>> timeLineOrder() {
    return new Comparator<ChronoZonedDateTime<?>>() {
      public int compare(ChronoZonedDateTime<?> dateTime1, ChronoZonedDateTime<?> dateTime2) {
        int cmp = Long.compare(dateTime1.toEpochSecond(), dateTime2.toEpochSecond());
        if (cmp == 0) {
          cmp = Long.compare(dateTime1.toLocalTime().getNano(), dateTime2.toLocalTime().getNano());
        }
        return cmp;
      }
    };
  }

  static ChronoZonedDateTime<?> from(TemporalAccessor temporal) {
    if (temporal instanceof ChronoZonedDateTime) {
      return (ChronoZonedDateTime<?>) temporal;
    }
    Objects.requireNonNull(temporal, "temporal");
    return java.time.ZonedDateTime.from(temporal);
  }

  @Override
  default ValueRange range(TemporalField field) {
    if (field instanceof ChronoField) {
      if (field == INSTANT_SECONDS || field == OFFSET_SECONDS) {
        return field.range();
      }
      return toLocalDateTime().range(field);
    }
    return field.rangeRefinedBy(this);
  }

  @Override
  default int get(TemporalField field) {
    if (field instanceof ChronoField) {
      switch ((ChronoField) field) {
        case INSTANT_SECONDS:
          throw new UnsupportedTemporalTypeException("Invalid field 'InstantSeconds' for get() method, use getLong() instead");
        case OFFSET_SECONDS:
          return getOffset().getTotalSeconds();
        default:
          break;
      }
      return toLocalDateTime().get(field);
    }
    return Temporal.super.get(field);
  }

  @Override
  default long getLong(TemporalField field) {
    if (field instanceof ChronoField) {
      switch ((ChronoField) field) {
        case INSTANT_SECONDS:
          return toEpochSecond();
        case OFFSET_SECONDS:
          return getOffset().getTotalSeconds();
        default:
          break;
      }
      return toLocalDateTime().getLong(field);
    }
    return field.getFrom(this);
  }

  default D toLocalDate() {
    return toLocalDateTime().toLocalDate();
  }

  default LocalTime toLocalTime() {
    return toLocalDateTime().toLocalTime();
  }

  ChronoLocalDateTime<D> toLocalDateTime();

  default Chronology getChronology() {
    return toLocalDate().getChronology();
  }

  ZoneOffset getOffset();

  ZoneId getZone();

  ChronoZonedDateTime<D> withEarlierOffsetAtOverlap();

  ChronoZonedDateTime<D> withLaterOffsetAtOverlap();

  ChronoZonedDateTime<D> withZoneSameLocal(ZoneId zone);

  ChronoZonedDateTime<D> withZoneSameInstant(ZoneId zone);

  @Override
  boolean isSupported(TemporalField field);

  @Override
  default boolean isSupported(TemporalUnit unit) {
    if (unit instanceof ChronoUnit) {
      return unit != ChronoUnit.FOREVER;
    }
    return unit != null && unit.isSupportedBy(this);
  }

  @SuppressWarnings("unchecked")
  @Override
  default ChronoZonedDateTime<D> with(java.time.temporal.TemporalAdjuster adjuster) {
    return (ChronoZonedDateTime<D>) Temporal.super.with(adjuster);
  }

  @SuppressWarnings("unchecked")
  @Override
  default ChronoZonedDateTime<D> plus(java.time.temporal.TemporalAmount amount) {
    return (ChronoZonedDateTime<D>) Temporal.super.plus(amount);
  }

  @SuppressWarnings("unchecked")
  @Override
  default ChronoZonedDateTime<D> minus(java.time.temporal.TemporalAmount amount) {
    return (ChronoZonedDateTime<D>) Temporal.super.minus(amount);
  }

  @SuppressWarnings("unchecked")
  @Override
  default ChronoZonedDateTime<D> minus(long amountToSubtract, TemporalUnit unit) {
    return (ChronoZonedDateTime<D>) Temporal.super.minus(amountToSubtract, unit);
  }

  @Override
  ChronoZonedDateTime<D> with(TemporalField field, long newValue);

  @Override
  ChronoZonedDateTime<D> plus(long amountToAdd, TemporalUnit unit);

  @SuppressWarnings("unchecked")
  @Override
  default <R> R query(TemporalQuery<R> query) {
    if (query == TemporalQueries.zone() || query == TemporalQueries.zoneId()) {
      return (R) getZone();
    } else if (query == TemporalQueries.offset()) {
      return (R) getOffset();
    } else if (query == TemporalQueries.localTime()) {
      return (R) toLocalTime();
    } else if (query == TemporalQueries.chronology()) {
      return (R) getChronology();
    } else if (query == TemporalQueries.precision()) {
      return (R) ChronoUnit.NANOS;
    }
    return query.queryFrom(this);
  }

  default String format(DateTimeFormatter formatter) {
    Objects.requireNonNull(formatter, "formatter");
    return formatter.format(this);
  }

  default Instant toInstant() {
    return Instant.ofEpochSecond(toEpochSecond(), toLocalTime().getNano());
  }

  default long toEpochSecond() {
    long epochDay = toLocalDate().toEpochDay();
    long secs = epochDay * 86400 + toLocalTime().toSecondOfDay();
    secs -= getOffset().getTotalSeconds();
    return secs;
  }

  @Override
  default int compareTo(ChronoZonedDateTime<?> other) {
    int cmp = Long.compare(toEpochSecond(), other.toEpochSecond());
    if (cmp == 0) {
      cmp = toLocalTime().getNano() - other.toLocalTime().getNano();
      if (cmp == 0) {
        cmp = toLocalDateTime().compareTo(other.toLocalDateTime());
        if (cmp == 0) {
          cmp = getZone().getId().compareTo(other.getZone().getId());
          if (cmp == 0) {
            cmp = getChronology().compareTo(other.getChronology());
          }
        }
      }
    }
    return cmp;
  }

  default boolean isBefore(ChronoZonedDateTime<?> other) {
    long thisEpochSec = toEpochSecond();
    long otherEpochSec = other.toEpochSecond();
    return thisEpochSec < otherEpochSec
        || (thisEpochSec == otherEpochSec && toLocalTime().getNano() < other.toLocalTime().getNano());
  }

  default boolean isAfter(ChronoZonedDateTime<?> other) {
    long thisEpochSec = toEpochSecond();
    long otherEpochSec = other.toEpochSecond();
    return thisEpochSec > otherEpochSec
        || (thisEpochSec == otherEpochSec && toLocalTime().getNano() > other.toLocalTime().getNano());
  }

  default boolean isEqual(ChronoZonedDateTime<?> other) {
    return toEpochSecond() == other.toEpochSecond() && toLocalTime().getNano() == other.toLocalTime().getNano();
  }

  @Override
  boolean equals(Object obj);

  @Override
  int hashCode();

  @Override
  String toString();
}
