/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.time.chrono;

import java.io.Serializable;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.util.Objects;

final class ChronoZonedDateTimeImpl<D extends ChronoLocalDate> implements ChronoZonedDateTime<D>, Serializable {
  private static final long serialVersionUID = -5261813987200935591L;

  private final ChronoLocalDateTimeImpl<D> dateTime;
  private final ZoneOffset offset;
  private final ZoneId zone;

  static <R extends ChronoLocalDate> ChronoZonedDateTime<R> ofBest(ChronoLocalDateTimeImpl<R> localDateTime,
      ZoneId zone, ZoneOffset preferredOffset) {
    Objects.requireNonNull(localDateTime, "localDateTime");
    Objects.requireNonNull(zone, "zone");
    if (zone instanceof ZoneOffset) {
      return new ChronoZonedDateTimeImpl<R>(localDateTime, (ZoneOffset) zone, zone);
    }
    ZonedDateTime iso = ZonedDateTime.ofLocal(localDateTime.iso(), zone, preferredOffset);
    return fromIso(localDateTime.toLocalDate().getChronology(), iso);
  }

  @SuppressWarnings("unchecked")
  private static <R extends ChronoLocalDate> ChronoZonedDateTime<R> fromIso(Chronology chrono, ZonedDateTime iso) {
    R date = (R) chrono.dateEpochDay(iso.toLocalDate().toEpochDay());
    return new ChronoZonedDateTimeImpl<R>(ChronoLocalDateTimeImpl.of(date, iso.toLocalTime()), iso.getOffset(),
        iso.getZone());
  }

  static ChronoZonedDateTimeImpl<?> ofInstant(Chronology chrono, Instant instant, ZoneId zone) {
    return (ChronoZonedDateTimeImpl<?>) fromIso(chrono, ZonedDateTime.ofInstant(instant, zone));
  }

  private ChronoZonedDateTimeImpl(ChronoLocalDateTimeImpl<D> dateTime, ZoneOffset offset, ZoneId zone) {
    this.dateTime = Objects.requireNonNull(dateTime, "dateTime");
    this.offset = Objects.requireNonNull(offset, "offset");
    this.zone = Objects.requireNonNull(zone, "zone");
  }

  private ZonedDateTime iso() {
    return ZonedDateTime.ofStrict(dateTime.iso(), offset, zone);
  }

  private ChronoZonedDateTime<D> wrap(ZonedDateTime iso) {
    return fromIso(toLocalDate().getChronology(), iso);
  }

  @Override
  public ZoneOffset getOffset() {
    return offset;
  }

  @Override
  public ChronoZonedDateTime<D> withEarlierOffsetAtOverlap() {
    return wrap(iso().withEarlierOffsetAtOverlap());
  }

  @Override
  public ChronoZonedDateTime<D> withLaterOffsetAtOverlap() {
    return wrap(iso().withLaterOffsetAtOverlap());
  }

  @Override
  public ChronoLocalDateTime<D> toLocalDateTime() {
    return dateTime;
  }

  @Override
  public ZoneId getZone() {
    return zone;
  }

  @Override
  public ChronoZonedDateTime<D> withZoneSameLocal(ZoneId zone) {
    return ofBest(dateTime, zone, offset);
  }

  @Override
  public ChronoZonedDateTime<D> withZoneSameInstant(ZoneId zone) {
    Objects.requireNonNull(zone, "zone");
    return this.zone.equals(zone) ? this : wrap(iso().withZoneSameInstant(zone));
  }

  @Override
  public boolean isSupported(TemporalField field) {
    return field instanceof ChronoField || (field != null && field.isSupportedBy(this));
  }

  @Override
  @SuppressWarnings("unchecked")
  public ChronoZonedDateTime<D> with(TemporalField field, long newValue) {
    if (field instanceof ChronoField) {
      ChronoField f = (ChronoField) field;
      switch (f) {
        case INSTANT_SECONDS:
          return plus(newValue - toEpochSecond(), ChronoUnit.SECONDS);
        case OFFSET_SECONDS: {
          ZoneOffset offset = ZoneOffset.ofTotalSeconds(f.checkValidIntValue(newValue));
          return (ChronoZonedDateTime<D>) ofInstant(toLocalDate().getChronology(),
              dateTime.toInstant(offset), zone);
        }
        default:
          return ofBest(dateTime.with(field, newValue), zone, offset);
      }
    }
    return (ChronoZonedDateTime<D>) (field.adjustInto(this, newValue));
  }

  @Override
  @SuppressWarnings("unchecked")
  public ChronoZonedDateTime<D> plus(long amountToAdd, TemporalUnit unit) {
    if (unit instanceof ChronoUnit) {
      if (unit.isDateBased()) {
        return ofBest(dateTime.plus(amountToAdd, unit), zone, offset);
      }
      return wrap(iso().plus(amountToAdd, unit));
    }
    return (ChronoZonedDateTime<D>) (unit.addTo(this, amountToAdd));
  }

  @Override
  public long until(Temporal endExclusive, TemporalUnit unit) {
    Objects.requireNonNull(endExclusive, "endExclusive");
    @SuppressWarnings("unchecked")
    ChronoZonedDateTime<D> end = (ChronoZonedDateTime<D>) toLocalDate().getChronology().zonedDateTime(endExclusive);
    if (unit instanceof ChronoUnit) {
      end = end.withZoneSameInstant(offset);
      return dateTime.until(end.toLocalDateTime(), unit);
    }
    Objects.requireNonNull(unit, "unit");
    return unit.between(this, end);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof ChronoZonedDateTime) {
      return compareTo((ChronoZonedDateTime<?>) obj) == 0;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return toLocalDateTime().hashCode() ^ getOffset().hashCode() ^ Integer.rotateLeft(getZone().hashCode(), 3);
  }

  @Override
  public String toString() {
    String str = toLocalDateTime().toString() + getOffset().toString();
    if (getOffset() != getZone()) {
      str += '[' + getZone().toString() + ']';
    }
    return str;
  }
}
