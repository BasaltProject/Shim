/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.time;

import static java.time.LocalTime.NANOS_PER_HOUR;
import static java.time.LocalTime.NANOS_PER_MINUTE;
import static java.time.LocalTime.NANOS_PER_SECOND;
import static java.time.LocalTime.SECONDS_PER_DAY;
import static java.time.temporal.ChronoField.NANO_OF_DAY;
import static java.time.temporal.ChronoField.OFFSET_SECONDS;

import java.io.Serializable;
import java.time.chrono.IsoChronology;
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
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;
import java.time.zone.ZoneRules;
import java.util.Objects;

public final class OffsetTime implements Temporal, TemporalAdjuster, Comparable<OffsetTime>, Serializable {
  private static final long serialVersionUID = 7264499704384272492L;

  public static final OffsetTime MIN = LocalTime.MIN.atOffset(ZoneOffset.MAX);
  public static final OffsetTime MAX = LocalTime.MAX.atOffset(ZoneOffset.MIN);

  private final LocalTime time;
  private final ZoneOffset offset;

  public static OffsetTime now() {
    return now(Clock.systemDefaultZone());
  }

  public static OffsetTime now(ZoneId zone) {
    return now(Clock.system(zone));
  }

  public static OffsetTime now(Clock clock) {
    Objects.requireNonNull(clock, "clock");
    final Instant now = clock.instant();
    return ofInstant(now, clock.getZone().getRules().getOffset(now));
  }

  public static OffsetTime of(LocalTime time, ZoneOffset offset) {
    return new OffsetTime(time, offset);
  }

  public static OffsetTime of(int hour, int minute, int second, int nanoOfSecond, ZoneOffset offset) {
    return new OffsetTime(LocalTime.of(hour, minute, second, nanoOfSecond), offset);
  }

  public static OffsetTime ofInstant(Instant instant, ZoneId zone) {
    Objects.requireNonNull(instant, "instant");
    Objects.requireNonNull(zone, "zone");
    ZoneRules rules = zone.getRules();
    ZoneOffset offset = rules.getOffset(instant);
    long localSecond = instant.getEpochSecond() + offset.getTotalSeconds();
    int secsOfDay = Math.floorMod(localSecond, SECONDS_PER_DAY);
    LocalTime time = LocalTime.ofNanoOfDay(secsOfDay * NANOS_PER_SECOND + instant.getNano());
    return new OffsetTime(time, offset);
  }

  public static OffsetTime from(TemporalAccessor temporal) {
    if (temporal instanceof OffsetTime) {
      return (OffsetTime) temporal;
    }
    try {
      LocalTime time = LocalTime.from(temporal);
      ZoneOffset offset = ZoneOffset.from(temporal);
      return new OffsetTime(time, offset);
    } catch (DateTimeException ex) {
      throw new DateTimeException("Unable to obtain OffsetTime from TemporalAccessor: " + temporal + " of type "
          + temporal.getClass().getName(), ex);
    }
  }

  public static OffsetTime parse(CharSequence text) {
    return parse(text, DateTimeFormatter.ISO_OFFSET_TIME);
  }

  public static OffsetTime parse(CharSequence text, DateTimeFormatter formatter) {
    Objects.requireNonNull(formatter, "formatter");
    return formatter.parse(text, new TemporalQuery<OffsetTime>() {
      @Override
      public OffsetTime queryFrom(TemporalAccessor temporal) {
        return OffsetTime.from(temporal);
      }
    });
  }

  private OffsetTime(LocalTime time, ZoneOffset offset) {
    this.time = Objects.requireNonNull(time, "time");
    this.offset = Objects.requireNonNull(offset, "offset");
  }

  private OffsetTime with(LocalTime time, ZoneOffset offset) {
    if (this.time == time && this.offset.equals(offset)) {
      return this;
    }
    return new OffsetTime(time, offset);
  }

  @Override
  public boolean isSupported(TemporalField field) {
    if (field instanceof ChronoField) {
      return field.isTimeBased() || field == OFFSET_SECONDS;
    }
    return field != null && field.isSupportedBy(this);
  }

  @Override
  public boolean isSupported(TemporalUnit unit) {
    if (unit instanceof ChronoUnit) {
      return unit.isTimeBased();
    }
    return unit != null && unit.isSupportedBy(this);
  }

  @Override
  public ValueRange range(TemporalField field) {
    if (field instanceof ChronoField) {
      if (field == OFFSET_SECONDS) {
        return field.range();
      }
      return time.range(field);
    }
    return field.rangeRefinedBy(this);
  }

  @Override
  public int get(TemporalField field) {
    return Temporal.super.get(field);
  }

  @Override
  public long getLong(TemporalField field) {
    if (field instanceof ChronoField) {
      if (field == OFFSET_SECONDS) {
        return offset.getTotalSeconds();
      }
      return time.getLong(field);
    }
    return field.getFrom(this);
  }

  public ZoneOffset getOffset() {
    return offset;
  }

  public OffsetTime withOffsetSameLocal(ZoneOffset offset) {
    return offset != null && offset.equals(this.offset) ? this : new OffsetTime(time, offset);
  }

  public OffsetTime withOffsetSameInstant(ZoneOffset offset) {
    if (offset.equals(this.offset)) {
      return this;
    }
    int difference = offset.getTotalSeconds() - this.offset.getTotalSeconds();
    LocalTime adjusted = time.plusSeconds(difference);
    return new OffsetTime(adjusted, offset);
  }

  public LocalTime toLocalTime() {
    return time;
  }

  public int getHour() {
    return time.getHour();
  }

  public int getMinute() {
    return time.getMinute();
  }

  public int getSecond() {
    return time.getSecond();
  }

  public int getNano() {
    return time.getNano();
  }

  @Override
  public OffsetTime with(TemporalAdjuster adjuster) {
    if (adjuster instanceof LocalTime) {
      return with((LocalTime) adjuster, offset);
    } else if (adjuster instanceof ZoneOffset) {
      return with(time, (ZoneOffset) adjuster);
    } else if (adjuster instanceof OffsetTime) {
      return (OffsetTime) adjuster;
    }
    return (OffsetTime) adjuster.adjustInto(this);
  }

  @Override
  public OffsetTime with(TemporalField field, long newValue) {
    if (field instanceof ChronoField) {
      if (field == OFFSET_SECONDS) {
        ChronoField f = (ChronoField) field;
        return with(time, ZoneOffset.ofTotalSeconds(f.checkValidIntValue(newValue)));
      }
      return with(time.with(field, newValue), offset);
    }
    return field.adjustInto(this, newValue);
  }

  public OffsetTime withHour(int v) {
    return with(time.withHour(v), offset);
  }

  public OffsetTime withMinute(int v) {
    return with(time.withMinute(v), offset);
  }

  public OffsetTime withSecond(int v) {
    return with(time.withSecond(v), offset);
  }

  public OffsetTime withNano(int v) {
    return with(time.withNano(v), offset);
  }

  public OffsetTime truncatedTo(TemporalUnit unit) {
    return with(time.truncatedTo(unit), offset);
  }

  @Override
  public OffsetTime plus(TemporalAmount amountToAdd) {
    return (OffsetTime) amountToAdd.addTo(this);
  }

  @Override
  public OffsetTime plus(long amountToAdd, TemporalUnit unit) {
    if (unit instanceof ChronoUnit) {
      return with(time.plus(amountToAdd, unit), offset);
    }
    return unit.addTo(this, amountToAdd);
  }

  public OffsetTime plusHours(long hours) {
    return with(time.plusHours(hours), offset);
  }

  public OffsetTime plusMinutes(long minutes) {
    return with(time.plusMinutes(minutes), offset);
  }

  public OffsetTime plusSeconds(long seconds) {
    return with(time.plusSeconds(seconds), offset);
  }

  public OffsetTime plusNanos(long nanos) {
    return with(time.plusNanos(nanos), offset);
  }

  public OffsetTime minusHours(long hours) {
    return with(time.minusHours(hours), offset);
  }

  public OffsetTime minusMinutes(long minutes) {
    return with(time.minusMinutes(minutes), offset);
  }

  public OffsetTime minusSeconds(long seconds) {
    return with(time.minusSeconds(seconds), offset);
  }

  public OffsetTime minusNanos(long nanos) {
    return with(time.minusNanos(nanos), offset);
  }

  @Override
  public OffsetTime minus(TemporalAmount amountToSubtract) {
    return (OffsetTime) amountToSubtract.subtractFrom(this);
  }

  @Override
  public OffsetTime minus(long amountToSubtract, TemporalUnit unit) {
    return (amountToSubtract == Long.MIN_VALUE ? plus(Long.MAX_VALUE, unit).plus(1, unit)
        : plus(-amountToSubtract, unit));
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R> R query(TemporalQuery<R> query) {
    if (query == TemporalQueries.offset() || query == TemporalQueries.zone()) {
      return (R) offset;
    } else if (query == TemporalQueries.zoneId() || query == TemporalQueries.chronology()
        || query == TemporalQueries.localDate()) {
      return null;
    } else if (query == TemporalQueries.localTime()) {
      return (R) time;
    } else if (query == TemporalQueries.precision()) {
      return (R) ChronoUnit.NANOS;
    }
    return query.queryFrom(this);
  }

  @Override
  public Temporal adjustInto(Temporal temporal) {
    return temporal.with(NANO_OF_DAY, time.toNanoOfDay()).with(OFFSET_SECONDS, offset.getTotalSeconds());
  }

  @Override
  public long until(Temporal endExclusive, TemporalUnit unit) {
    OffsetTime end = OffsetTime.from(endExclusive);
    if (unit instanceof ChronoUnit) {
      long nanosUntil = end.toEpochNano() - toEpochNano();
      switch ((ChronoUnit) unit) {
        case NANOS:
          return nanosUntil;
        case MICROS:
          return nanosUntil / 1000;
        case MILLIS:
          return nanosUntil / 1000_000;
        case SECONDS:
          return nanosUntil / NANOS_PER_SECOND;
        case MINUTES:
          return nanosUntil / NANOS_PER_MINUTE;
        case HOURS:
          return nanosUntil / NANOS_PER_HOUR;
        case HALF_DAYS:
          return nanosUntil / (12 * NANOS_PER_HOUR);
        default:
          throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
      }
    }
    return unit.between(this, end);
  }

  public String format(DateTimeFormatter formatter) {
    Objects.requireNonNull(formatter, "formatter");
    return formatter.format(this);
  }

  public OffsetDateTime atDate(LocalDate date) {
    return OffsetDateTime.of(date, time, offset);
  }

  private long toEpochNano() {
    long nod = time.toNanoOfDay();
    long offsetNanos = offset.getTotalSeconds() * NANOS_PER_SECOND;
    return nod - offsetNanos;
  }

  public long toEpochSecond(LocalDate date) {
    Objects.requireNonNull(date, "date");
    long epochDay = date.toEpochDay();
    long secs = epochDay * 86400 + time.toSecondOfDay();
    secs -= offset.getTotalSeconds();
    return secs;
  }

  @Override
  public int compareTo(OffsetTime other) {
    if (offset.equals(other.offset)) {
      return time.compareTo(other.time);
    }
    int compare = Long.compare(toEpochNano(), other.toEpochNano());
    if (compare == 0) {
      compare = time.compareTo(other.time);
    }
    return compare;
  }

  public boolean isAfter(OffsetTime other) {
    return toEpochNano() > other.toEpochNano();
  }

  public boolean isBefore(OffsetTime other) {
    return toEpochNano() < other.toEpochNano();
  }

  public boolean isEqual(OffsetTime other) {
    return toEpochNano() == other.toEpochNano();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof OffsetTime) {
      OffsetTime other = (OffsetTime) obj;
      return time.equals(other.time) && offset.equals(other.offset);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return time.hashCode() ^ offset.hashCode();
  }

  @Override
  public String toString() {
    return time.toString() + offset.toString();
  }
}
