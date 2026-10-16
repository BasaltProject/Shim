/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.time;

import static java.time.temporal.ChronoField.EPOCH_DAY;
import static java.time.temporal.ChronoField.INSTANT_SECONDS;
import static java.time.temporal.ChronoField.NANO_OF_DAY;
import static java.time.temporal.ChronoField.OFFSET_SECONDS;

import java.io.Serializable;
import java.time.chrono.ChronoLocalDateTime;
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
import java.util.Comparator;
import java.util.Objects;

public final class OffsetDateTime implements Temporal, TemporalAdjuster, Comparable<OffsetDateTime>, Serializable {
  private static final long serialVersionUID = 2287754244819255394L;

  public static final OffsetDateTime MIN = LocalDateTime.MIN.atOffset(ZoneOffset.MAX);
  public static final OffsetDateTime MAX = LocalDateTime.MAX.atOffset(ZoneOffset.MIN);

  private final LocalDateTime dateTime;
  private final ZoneOffset offset;

  public static Comparator<OffsetDateTime> timeLineOrder() {
    return new Comparator<OffsetDateTime>() {
      @Override
      public int compare(OffsetDateTime a, OffsetDateTime b) {
        return compareInstant(a, b);
      }
    };
  }

  private static int compareInstant(OffsetDateTime datetime1, OffsetDateTime datetime2) {
    if (datetime1.getOffset().equals(datetime2.getOffset())) {
      return datetime1.toLocalDateTime().compareTo(datetime2.toLocalDateTime());
    }
    int cmp = Long.compare(datetime1.toEpochSecond(), datetime2.toEpochSecond());
    if (cmp == 0) {
      cmp = datetime1.toLocalTime().getNano() - datetime2.toLocalTime().getNano();
    }
    return cmp;
  }

  public static OffsetDateTime now() {
    return now(Clock.systemDefaultZone());
  }

  public static OffsetDateTime now(ZoneId zone) {
    return now(Clock.system(zone));
  }

  public static OffsetDateTime now(Clock clock) {
    Objects.requireNonNull(clock, "clock");
    final Instant now = clock.instant();
    return ofInstant(now, clock.getZone().getRules().getOffset(now));
  }

  public static OffsetDateTime of(LocalDate date, LocalTime time, ZoneOffset offset) {
    LocalDateTime dt = LocalDateTime.of(date, time);
    return new OffsetDateTime(dt, offset);
  }

  public static OffsetDateTime of(LocalDateTime dateTime, ZoneOffset offset) {
    return new OffsetDateTime(dateTime, offset);
  }

  public static OffsetDateTime of(int year, int month, int dayOfMonth, int hour, int minute, int second,
      int nanoOfSecond, ZoneOffset offset) {
    LocalDateTime dt = LocalDateTime.of(year, month, dayOfMonth, hour, minute, second, nanoOfSecond);
    return new OffsetDateTime(dt, offset);
  }

  public static OffsetDateTime ofInstant(Instant instant, ZoneId zone) {
    Objects.requireNonNull(instant, "instant");
    Objects.requireNonNull(zone, "zone");
    ZoneRules rules = zone.getRules();
    ZoneOffset offset = rules.getOffset(instant);
    LocalDateTime ldt = LocalDateTime.ofEpochSecond(instant.getEpochSecond(), instant.getNano(), offset);
    return new OffsetDateTime(ldt, offset);
  }

  public static OffsetDateTime from(TemporalAccessor temporal) {
    if (temporal instanceof OffsetDateTime) {
      return (OffsetDateTime) temporal;
    }
    try {
      ZoneOffset offset = ZoneOffset.from(temporal);
      LocalDate date = temporal.query(TemporalQueries.localDate());
      LocalTime time = temporal.query(TemporalQueries.localTime());
      if (date != null && time != null) {
        return OffsetDateTime.of(date, time, offset);
      } else {
        Instant instant = Instant.from(temporal);
        return OffsetDateTime.ofInstant(instant, offset);
      }
    } catch (DateTimeException ex) {
      throw new DateTimeException("Unable to obtain OffsetDateTime from TemporalAccessor: " + temporal + " of type "
          + temporal.getClass().getName(), ex);
    }
  }

  public static OffsetDateTime parse(CharSequence text) {
    return parse(text, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
  }

  public static OffsetDateTime parse(CharSequence text, DateTimeFormatter formatter) {
    Objects.requireNonNull(formatter, "formatter");
    return formatter.parse(text, new TemporalQuery<OffsetDateTime>() {
      @Override
      public OffsetDateTime queryFrom(TemporalAccessor temporal) {
        return OffsetDateTime.from(temporal);
      }
    });
  }

  private OffsetDateTime(LocalDateTime dateTime, ZoneOffset offset) {
    this.dateTime = Objects.requireNonNull(dateTime, "dateTime");
    this.offset = Objects.requireNonNull(offset, "offset");
  }

  private OffsetDateTime with(LocalDateTime dateTime, ZoneOffset offset) {
    if (this.dateTime == dateTime && this.offset.equals(offset)) {
      return this;
    }
    return new OffsetDateTime(dateTime, offset);
  }

  @Override
  public boolean isSupported(TemporalField field) {
    return field instanceof ChronoField || (field != null && field.isSupportedBy(this));
  }

  @Override
  public boolean isSupported(TemporalUnit unit) {
    if (unit instanceof ChronoUnit) {
      return unit != ChronoUnit.FOREVER;
    }
    return unit != null && unit.isSupportedBy(this);
  }

  @Override
  public ValueRange range(TemporalField field) {
    if (field instanceof ChronoField) {
      if (field == INSTANT_SECONDS || field == OFFSET_SECONDS) {
        return field.range();
      }
      return dateTime.range(field);
    }
    return field.rangeRefinedBy(this);
  }

  @Override
  public int get(TemporalField field) {
    if (field instanceof ChronoField) {
      switch ((ChronoField) field) {
        case INSTANT_SECONDS:
          throw new UnsupportedTemporalTypeException(
              "Invalid field 'InstantSeconds' for get() method, use getLong() instead");
        case OFFSET_SECONDS:
          return getOffset().getTotalSeconds();
        default:
          return dateTime.get(field);
      }
    }
    return Temporal.super.get(field);
  }

  @Override
  public long getLong(TemporalField field) {
    if (field instanceof ChronoField) {
      switch ((ChronoField) field) {
        case INSTANT_SECONDS:
          return toEpochSecond();
        case OFFSET_SECONDS:
          return getOffset().getTotalSeconds();
        default:
          return dateTime.getLong(field);
      }
    }
    return field.getFrom(this);
  }

  public ZoneOffset getOffset() {
    return offset;
  }

  public OffsetDateTime withOffsetSameLocal(ZoneOffset offset) {
    return with(dateTime, offset);
  }

  public OffsetDateTime withOffsetSameInstant(ZoneOffset offset) {
    if (offset.equals(this.offset)) {
      return this;
    }
    int difference = offset.getTotalSeconds() - this.offset.getTotalSeconds();
    LocalDateTime adjusted = dateTime.plusSeconds(difference);
    return new OffsetDateTime(adjusted, offset);
  }

  public LocalDateTime toLocalDateTime() {
    return dateTime;
  }

  public LocalDate toLocalDate() {
    return dateTime.toLocalDate();
  }

  public int getYear() {
    return dateTime.getYear();
  }

  public int getMonthValue() {
    return dateTime.getMonthValue();
  }

  public Month getMonth() {
    return dateTime.getMonth();
  }

  public int getDayOfMonth() {
    return dateTime.getDayOfMonth();
  }

  public int getDayOfYear() {
    return dateTime.getDayOfYear();
  }

  public DayOfWeek getDayOfWeek() {
    return dateTime.getDayOfWeek();
  }

  public LocalTime toLocalTime() {
    return dateTime.toLocalTime();
  }

  public int getHour() {
    return dateTime.getHour();
  }

  public int getMinute() {
    return dateTime.getMinute();
  }

  public int getSecond() {
    return dateTime.getSecond();
  }

  public int getNano() {
    return dateTime.getNano();
  }

  @Override
  public OffsetDateTime with(TemporalAdjuster adjuster) {
    if (adjuster instanceof LocalDate || adjuster instanceof LocalTime || adjuster instanceof LocalDateTime) {
      return with(dateTime.with(adjuster), offset);
    } else if (adjuster instanceof Instant) {
      return ofInstant((Instant) adjuster, offset);
    } else if (adjuster instanceof ZoneOffset) {
      return with(dateTime, (ZoneOffset) adjuster);
    } else if (adjuster instanceof OffsetDateTime) {
      return (OffsetDateTime) adjuster;
    }
    return (OffsetDateTime) adjuster.adjustInto(this);
  }

  @Override
  public OffsetDateTime with(TemporalField field, long newValue) {
    if (field instanceof ChronoField) {
      ChronoField f = (ChronoField) field;
      switch (f) {
        case INSTANT_SECONDS:
          return ofInstant(Instant.ofEpochSecond(newValue, getNano()), offset);
        case OFFSET_SECONDS:
          return with(dateTime, ZoneOffset.ofTotalSeconds(f.checkValidIntValue(newValue)));
        default:
          return with(dateTime.with(field, newValue), offset);
      }
    }
    return field.adjustInto(this, newValue);
  }

  public OffsetDateTime withYear(int v) {
    return with(dateTime.withYear(v), offset);
  }

  public OffsetDateTime withMonth(int v) {
    return with(dateTime.withMonth(v), offset);
  }

  public OffsetDateTime withDayOfMonth(int v) {
    return with(dateTime.withDayOfMonth(v), offset);
  }

  public OffsetDateTime withDayOfYear(int v) {
    return with(dateTime.withDayOfYear(v), offset);
  }

  public OffsetDateTime withHour(int v) {
    return with(dateTime.withHour(v), offset);
  }

  public OffsetDateTime withMinute(int v) {
    return with(dateTime.withMinute(v), offset);
  }

  public OffsetDateTime withSecond(int v) {
    return with(dateTime.withSecond(v), offset);
  }

  public OffsetDateTime withNano(int v) {
    return with(dateTime.withNano(v), offset);
  }

  public OffsetDateTime truncatedTo(TemporalUnit unit) {
    return with(dateTime.truncatedTo(unit), offset);
  }

  @Override
  public OffsetDateTime plus(TemporalAmount amountToAdd) {
    return (OffsetDateTime) amountToAdd.addTo(this);
  }

  @Override
  public OffsetDateTime plus(long amountToAdd, TemporalUnit unit) {
    if (unit instanceof ChronoUnit) {
      return with(dateTime.plus(amountToAdd, unit), offset);
    }
    return unit.addTo(this, amountToAdd);
  }

  public OffsetDateTime plusYears(long years) {
    return with(dateTime.plusYears(years), offset);
  }

  public OffsetDateTime plusMonths(long months) {
    return with(dateTime.plusMonths(months), offset);
  }

  public OffsetDateTime plusWeeks(long weeks) {
    return with(dateTime.plusWeeks(weeks), offset);
  }

  public OffsetDateTime plusDays(long days) {
    return with(dateTime.plusDays(days), offset);
  }

  public OffsetDateTime plusHours(long hours) {
    return with(dateTime.plusHours(hours), offset);
  }

  public OffsetDateTime plusMinutes(long minutes) {
    return with(dateTime.plusMinutes(minutes), offset);
  }

  public OffsetDateTime plusSeconds(long seconds) {
    return with(dateTime.plusSeconds(seconds), offset);
  }

  public OffsetDateTime plusNanos(long nanos) {
    return with(dateTime.plusNanos(nanos), offset);
  }

  public OffsetDateTime minusYears(long years) {
    return with(dateTime.minusYears(years), offset);
  }

  public OffsetDateTime minusMonths(long months) {
    return with(dateTime.minusMonths(months), offset);
  }

  public OffsetDateTime minusWeeks(long weeks) {
    return with(dateTime.minusWeeks(weeks), offset);
  }

  public OffsetDateTime minusDays(long days) {
    return with(dateTime.minusDays(days), offset);
  }

  public OffsetDateTime minusHours(long hours) {
    return with(dateTime.minusHours(hours), offset);
  }

  public OffsetDateTime minusMinutes(long minutes) {
    return with(dateTime.minusMinutes(minutes), offset);
  }

  public OffsetDateTime minusSeconds(long seconds) {
    return with(dateTime.minusSeconds(seconds), offset);
  }

  public OffsetDateTime minusNanos(long nanos) {
    return with(dateTime.minusNanos(nanos), offset);
  }

  @Override
  public OffsetDateTime minus(TemporalAmount amountToSubtract) {
    return (OffsetDateTime) amountToSubtract.subtractFrom(this);
  }

  @Override
  public OffsetDateTime minus(long amountToSubtract, TemporalUnit unit) {
    return (amountToSubtract == Long.MIN_VALUE ? plus(Long.MAX_VALUE, unit).plus(1, unit)
        : plus(-amountToSubtract, unit));
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R> R query(TemporalQuery<R> query) {
    if (query == TemporalQueries.offset() || query == TemporalQueries.zone()) {
      return (R) getOffset();
    } else if (query == TemporalQueries.zoneId()) {
      return null;
    } else if (query == TemporalQueries.localDate()) {
      return (R) toLocalDate();
    } else if (query == TemporalQueries.localTime()) {
      return (R) toLocalTime();
    } else if (query == TemporalQueries.chronology()) {
      return (R) IsoChronology.INSTANCE;
    } else if (query == TemporalQueries.precision()) {
      return (R) ChronoUnit.NANOS;
    }
    return query.queryFrom(this);
  }

  @Override
  public Temporal adjustInto(Temporal temporal) {
    return temporal.with(EPOCH_DAY, toLocalDate().toEpochDay()).with(NANO_OF_DAY, toLocalTime().toNanoOfDay())
        .with(OFFSET_SECONDS, getOffset().getTotalSeconds());
  }

  @Override
  public long until(Temporal endExclusive, TemporalUnit unit) {
    OffsetDateTime end = OffsetDateTime.from(endExclusive);
    if (unit instanceof ChronoUnit) {
      OffsetDateTime start = this;
      end = end.withOffsetSameInstant(offset);
      return start.dateTime.until(end.dateTime, unit);
    }
    return unit.between(this, end);
  }

  public String format(DateTimeFormatter formatter) {
    Objects.requireNonNull(formatter, "formatter");
    return formatter.format(this);
  }

  public ZonedDateTime atZoneSameInstant(ZoneId zone) {
    return ZonedDateTime.ofInstant(dateTime, offset, zone);
  }

  public ZonedDateTime atZoneSimilarLocal(ZoneId zone) {
    return ZonedDateTime.ofLocal(dateTime, zone, offset);
  }

  public OffsetTime toOffsetTime() {
    return OffsetTime.of(dateTime.toLocalTime(), offset);
  }

  public ZonedDateTime toZonedDateTime() {
    return ZonedDateTime.of(dateTime, offset);
  }

  public Instant toInstant() {
    return dateTime.toInstant(offset);
  }

  public long toEpochSecond() {
    return dateTime.toEpochSecond(offset);
  }

  @Override
  public int compareTo(OffsetDateTime other) {
    int cmp = compareInstant(this, other);
    if (cmp == 0) {
      cmp = toLocalDateTime().compareTo(other.toLocalDateTime());
    }
    return cmp;
  }

  public boolean isAfter(OffsetDateTime other) {
    long thisEpochSec = toEpochSecond();
    long otherEpochSec = other.toEpochSecond();
    return thisEpochSec > otherEpochSec
        || (thisEpochSec == otherEpochSec && toLocalTime().getNano() > other.toLocalTime().getNano());
  }

  public boolean isBefore(OffsetDateTime other) {
    long thisEpochSec = toEpochSecond();
    long otherEpochSec = other.toEpochSecond();
    return thisEpochSec < otherEpochSec
        || (thisEpochSec == otherEpochSec && toLocalTime().getNano() < other.toLocalTime().getNano());
  }

  public boolean isEqual(OffsetDateTime other) {
    return toEpochSecond() == other.toEpochSecond() && toLocalTime().getNano() == other.toLocalTime().getNano();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof OffsetDateTime) {
      OffsetDateTime other = (OffsetDateTime) obj;
      return dateTime.equals(other.dateTime) && offset.equals(other.offset);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return dateTime.hashCode() ^ offset.hashCode();
  }

  @Override
  public String toString() {
    return dateTime.toString() + offset.toString();
  }
}
