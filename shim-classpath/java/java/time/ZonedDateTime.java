/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.time;

import static java.time.temporal.ChronoField.INSTANT_SECONDS;
import static java.time.temporal.ChronoField.NANO_OF_SECOND;
import static java.time.temporal.ChronoField.OFFSET_SECONDS;

import java.io.Serializable;
import java.time.chrono.ChronoLocalDateTime;
import java.time.chrono.ChronoZonedDateTime;
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
import java.time.temporal.ValueRange;
import java.time.zone.ZoneOffsetTransition;
import java.time.zone.ZoneRules;
import java.util.List;
import java.util.Objects;

public final class ZonedDateTime implements Temporal, ChronoZonedDateTime<LocalDate>, Serializable {
  private static final long serialVersionUID = -6260982410461394882L;

  private final LocalDateTime dateTime;
  private final ZoneOffset offset;
  private final ZoneId zone;

  public static ZonedDateTime now() {
    return now(Clock.systemDefaultZone());
  }

  public static ZonedDateTime now(ZoneId zone) {
    return now(Clock.system(zone));
  }

  public static ZonedDateTime now(Clock clock) {
    Objects.requireNonNull(clock, "clock");
    final Instant now = clock.instant();
    return ofInstant(now, clock.getZone());
  }

  public static ZonedDateTime of(LocalDate date, LocalTime time, ZoneId zone) {
    return of(LocalDateTime.of(date, time), zone);
  }

  public static ZonedDateTime of(LocalDateTime localDateTime, ZoneId zone) {
    return ofLocal(localDateTime, zone, null);
  }

  public static ZonedDateTime of(int year, int month, int dayOfMonth, int hour, int minute, int second,
      int nanoOfSecond, ZoneId zone) {
    LocalDateTime dt = LocalDateTime.of(year, month, dayOfMonth, hour, minute, second, nanoOfSecond);
    return ofLocal(dt, zone, null);
  }

  public static ZonedDateTime ofLocal(LocalDateTime localDateTime, ZoneId zone, ZoneOffset preferredOffset) {
    Objects.requireNonNull(localDateTime, "localDateTime");
    Objects.requireNonNull(zone, "zone");
    if (zone instanceof ZoneOffset) {
      return new ZonedDateTime(localDateTime, (ZoneOffset) zone, zone);
    }
    ZoneRules rules = zone.getRules();
    List<ZoneOffset> validOffsets = rules.getValidOffsets(localDateTime);
    ZoneOffset offset;
    if (validOffsets.size() == 1) {
      offset = validOffsets.get(0);
    } else if (validOffsets.size() == 0) {
      ZoneOffsetTransition trans = rules.getTransition(localDateTime);
      localDateTime = localDateTime.plusSeconds(trans.getDuration().getSeconds());
      offset = trans.getOffsetAfter();
    } else {
      if (preferredOffset != null && validOffsets.contains(preferredOffset)) {
        offset = preferredOffset;
      } else {
        offset = Objects.requireNonNull(validOffsets.get(0), "offset");
      }
    }
    return new ZonedDateTime(localDateTime, offset, zone);
  }

  public static ZonedDateTime ofInstant(Instant instant, ZoneId zone) {
    Objects.requireNonNull(instant, "instant");
    Objects.requireNonNull(zone, "zone");
    return create(instant.getEpochSecond(), instant.getNano(), zone);
  }

  public static ZonedDateTime ofInstant(LocalDateTime localDateTime, ZoneOffset offset, ZoneId zone) {
    Objects.requireNonNull(localDateTime, "localDateTime");
    Objects.requireNonNull(offset, "offset");
    Objects.requireNonNull(zone, "zone");
    if (zone.getRules().isValidOffset(localDateTime, offset)) {
      return new ZonedDateTime(localDateTime, offset, zone);
    }
    return create(localDateTime.toEpochSecond(offset), localDateTime.getNano(), zone);
  }

  private static ZonedDateTime create(long epochSecond, int nanoOfSecond, ZoneId zone) {
    ZoneRules rules = zone.getRules();
    Instant instant = Instant.ofEpochSecond(epochSecond, nanoOfSecond);
    ZoneOffset offset = rules.getOffset(instant);
    LocalDateTime ldt = LocalDateTime.ofEpochSecond(epochSecond, nanoOfSecond, offset);
    return new ZonedDateTime(ldt, offset, zone);
  }

  public static ZonedDateTime ofStrict(LocalDateTime localDateTime, ZoneOffset offset, ZoneId zone) {
    Objects.requireNonNull(localDateTime, "localDateTime");
    Objects.requireNonNull(offset, "offset");
    Objects.requireNonNull(zone, "zone");
    ZoneRules rules = zone.getRules();
    if (rules.isValidOffset(localDateTime, offset) == false) {
      ZoneOffsetTransition trans = rules.getTransition(localDateTime);
      if (trans != null && trans.isGap()) {
        throw new DateTimeException("LocalDateTime '" + localDateTime + "' does not exist in zone '" + zone
            + "' due to a gap in the local time-line, typically caused by daylight savings");
      }
      throw new DateTimeException("ZoneOffset '" + offset + "' is not valid for LocalDateTime '" + localDateTime
          + "' in zone '" + zone + "'");
    }
    return new ZonedDateTime(localDateTime, offset, zone);
  }

  private static ZonedDateTime ofLenient(LocalDateTime localDateTime, ZoneOffset offset, ZoneId zone) {
    Objects.requireNonNull(localDateTime, "localDateTime");
    Objects.requireNonNull(offset, "offset");
    Objects.requireNonNull(zone, "zone");
    if (zone instanceof ZoneOffset && offset.equals(zone) == false) {
      throw new IllegalArgumentException("ZoneId must match ZoneOffset");
    }
    return new ZonedDateTime(localDateTime, offset, zone);
  }

  public static ZonedDateTime from(TemporalAccessor temporal) {
    if (temporal instanceof ZonedDateTime) {
      return (ZonedDateTime) temporal;
    }
    try {
      ZoneId zone = ZoneId.from(temporal);
      if (temporal.isSupported(INSTANT_SECONDS)) {
        long epochSecond = temporal.getLong(INSTANT_SECONDS);
        int nanoOfSecond = temporal.get(NANO_OF_SECOND);
        return create(epochSecond, nanoOfSecond, zone);
      } else {
        LocalDate date = LocalDate.from(temporal);
        LocalTime time = LocalTime.from(temporal);
        return of(date, time, zone);
      }
    } catch (DateTimeException ex) {
      throw new DateTimeException("Unable to obtain ZonedDateTime from TemporalAccessor: " + temporal + " of type "
          + temporal.getClass().getName(), ex);
    }
  }

  public static ZonedDateTime parse(CharSequence text) {
    return parse(text, DateTimeFormatter.ISO_ZONED_DATE_TIME);
  }

  public static ZonedDateTime parse(CharSequence text, DateTimeFormatter formatter) {
    Objects.requireNonNull(formatter, "formatter");
    return formatter.parse(text, new TemporalQuery<ZonedDateTime>() {
      @Override
      public ZonedDateTime queryFrom(TemporalAccessor temporal) {
        return ZonedDateTime.from(temporal);
      }
    });
  }

  private ZonedDateTime(LocalDateTime dateTime, ZoneOffset offset, ZoneId zone) {
    this.dateTime = dateTime;
    this.offset = offset;
    this.zone = zone;
  }

  private ZonedDateTime resolveLocal(LocalDateTime newDateTime) {
    return ofLocal(newDateTime, zone, offset);
  }

  private ZonedDateTime resolveInstant(LocalDateTime newDateTime) {
    return ofInstant(newDateTime, offset, zone);
  }

  private ZonedDateTime resolveOffset(ZoneOffset offset) {
    if (offset.equals(this.offset) == false && zone.getRules().isValidOffset(dateTime, offset)) {
      return new ZonedDateTime(dateTime, offset, zone);
    }
    return this;
  }

  @Override
  public boolean isSupported(TemporalField field) {
    return field instanceof ChronoField || (field != null && field.isSupportedBy(this));
  }

  @Override
  public boolean isSupported(TemporalUnit unit) {
    return ChronoZonedDateTime.super.isSupported(unit);
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
    return ChronoZonedDateTime.super.get(field);
  }

  @Override
  public long getLong(TemporalField field) {
    return ChronoZonedDateTime.super.getLong(field);
  }

  @Override
  public ZoneOffset getOffset() {
    return offset;
  }

  @Override
  public ZonedDateTime withEarlierOffsetAtOverlap() {
    ZoneOffsetTransition trans = getZone().getRules().getTransition(dateTime);
    if (trans != null && trans.isOverlap()) {
      ZoneOffset earlierOffset = trans.getOffsetBefore();
      if (earlierOffset.equals(offset) == false) {
        return new ZonedDateTime(dateTime, earlierOffset, zone);
      }
    }
    return this;
  }

  @Override
  public ZonedDateTime withLaterOffsetAtOverlap() {
    ZoneOffsetTransition trans = getZone().getRules().getTransition(toLocalDateTime());
    if (trans != null) {
      ZoneOffset laterOffset = trans.getOffsetAfter();
      if (laterOffset.equals(offset) == false) {
        return new ZonedDateTime(dateTime, laterOffset, zone);
      }
    }
    return this;
  }

  @Override
  public ZoneId getZone() {
    return zone;
  }

  @Override
  public ZonedDateTime withZoneSameLocal(ZoneId zone) {
    Objects.requireNonNull(zone, "zone");
    return this.zone.equals(zone) ? this : ofLocal(dateTime, zone, offset);
  }

  @Override
  public ZonedDateTime withZoneSameInstant(ZoneId zone) {
    Objects.requireNonNull(zone, "zone");
    return this.zone.equals(zone) ? this : create(dateTime.toEpochSecond(offset), dateTime.getNano(), zone);
  }

  public ZonedDateTime withFixedOffsetZone() {
    return this.zone.equals(offset) ? this : new ZonedDateTime(dateTime, offset, offset);
  }

  @Override
  public LocalDateTime toLocalDateTime() {
    return dateTime;
  }

  @Override
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

  @Override
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
  public ZonedDateTime with(TemporalAdjuster adjuster) {
    if (adjuster instanceof LocalDate) {
      return resolveLocal(LocalDateTime.of((LocalDate) adjuster, dateTime.toLocalTime()));
    } else if (adjuster instanceof LocalTime) {
      return resolveLocal(LocalDateTime.of(dateTime.toLocalDate(), (LocalTime) adjuster));
    } else if (adjuster instanceof LocalDateTime) {
      return resolveLocal((LocalDateTime) adjuster);
    } else if (adjuster instanceof OffsetDateTime) {
      OffsetDateTime odt = (OffsetDateTime) adjuster;
      return ofLenient(odt.toLocalDateTime(), odt.getOffset(), zone);
    } else if (adjuster instanceof Instant) {
      Instant instant = (Instant) adjuster;
      return create(instant.getEpochSecond(), instant.getNano(), zone);
    } else if (adjuster instanceof ZoneOffset) {
      return resolveOffset((ZoneOffset) adjuster);
    }
    return (ZonedDateTime) adjuster.adjustInto(this);
  }

  @Override
  public ZonedDateTime with(TemporalField field, long newValue) {
    if (field instanceof ChronoField) {
      ChronoField f = (ChronoField) field;
      switch (f) {
        case INSTANT_SECONDS:
          return create(newValue, getNano(), zone);
        case OFFSET_SECONDS:
          ZoneOffset offset = ZoneOffset.ofTotalSeconds(f.checkValidIntValue(newValue));
          return resolveOffset(offset);
        default:
          break;
      }
      return resolveLocal(dateTime.with(field, newValue));
    }
    return field.adjustInto(this, newValue);
  }

  public ZonedDateTime withYear(int v) {
    return resolveLocal(dateTime.withYear(v));
  }

  public ZonedDateTime withMonth(int v) {
    return resolveLocal(dateTime.withMonth(v));
  }

  public ZonedDateTime withDayOfMonth(int v) {
    return resolveLocal(dateTime.withDayOfMonth(v));
  }

  public ZonedDateTime withDayOfYear(int v) {
    return resolveLocal(dateTime.withDayOfYear(v));
  }

  public ZonedDateTime withHour(int v) {
    return resolveLocal(dateTime.withHour(v));
  }

  public ZonedDateTime withMinute(int v) {
    return resolveLocal(dateTime.withMinute(v));
  }

  public ZonedDateTime withSecond(int v) {
    return resolveLocal(dateTime.withSecond(v));
  }

  public ZonedDateTime withNano(int v) {
    return resolveLocal(dateTime.withNano(v));
  }

  public ZonedDateTime truncatedTo(TemporalUnit unit) {
    return resolveLocal(dateTime.truncatedTo(unit));
  }

  @Override
  public ZonedDateTime plus(TemporalAmount amountToAdd) {
    if (amountToAdd instanceof Period) {
      Period periodToAdd = (Period) amountToAdd;
      return resolveLocal(dateTime.plus(periodToAdd));
    }
    Objects.requireNonNull(amountToAdd, "amountToAdd");
    return (ZonedDateTime) amountToAdd.addTo(this);
  }

  @Override
  public ZonedDateTime plus(long amountToAdd, TemporalUnit unit) {
    if (unit instanceof ChronoUnit) {
      if (unit.isDateBased()) {
        return resolveLocal(dateTime.plus(amountToAdd, unit));
      } else {
        return resolveInstant(dateTime.plus(amountToAdd, unit));
      }
    }
    return unit.addTo(this, amountToAdd);
  }

  public ZonedDateTime plusYears(long years) {
    return resolveLocal(dateTime.plusYears(years));
  }

  public ZonedDateTime plusMonths(long months) {
    return resolveLocal(dateTime.plusMonths(months));
  }

  public ZonedDateTime plusWeeks(long weeks) {
    return resolveLocal(dateTime.plusWeeks(weeks));
  }

  public ZonedDateTime plusDays(long days) {
    return resolveLocal(dateTime.plusDays(days));
  }

  public ZonedDateTime plusHours(long hours) {
    return resolveInstant(dateTime.plusHours(hours));
  }

  public ZonedDateTime plusMinutes(long minutes) {
    return resolveInstant(dateTime.plusMinutes(minutes));
  }

  public ZonedDateTime plusSeconds(long seconds) {
    return resolveInstant(dateTime.plusSeconds(seconds));
  }

  public ZonedDateTime plusNanos(long nanos) {
    return resolveInstant(dateTime.plusNanos(nanos));
  }

  public ZonedDateTime minusYears(long years) {
    return resolveLocal(dateTime.minusYears(years));
  }

  public ZonedDateTime minusMonths(long months) {
    return resolveLocal(dateTime.minusMonths(months));
  }

  public ZonedDateTime minusWeeks(long weeks) {
    return resolveLocal(dateTime.minusWeeks(weeks));
  }

  public ZonedDateTime minusDays(long days) {
    return resolveLocal(dateTime.minusDays(days));
  }

  public ZonedDateTime minusHours(long hours) {
    return resolveInstant(dateTime.minusHours(hours));
  }

  public ZonedDateTime minusMinutes(long minutes) {
    return resolveInstant(dateTime.minusMinutes(minutes));
  }

  public ZonedDateTime minusSeconds(long seconds) {
    return resolveInstant(dateTime.minusSeconds(seconds));
  }

  public ZonedDateTime minusNanos(long nanos) {
    return resolveInstant(dateTime.minusNanos(nanos));
  }

  @Override
  public ZonedDateTime minus(TemporalAmount amountToSubtract) {
    if (amountToSubtract instanceof Period) {
      Period periodToSubtract = (Period) amountToSubtract;
      return resolveLocal(dateTime.minus(periodToSubtract));
    }
    Objects.requireNonNull(amountToSubtract, "amountToSubtract");
    return (ZonedDateTime) amountToSubtract.subtractFrom(this);
  }

  @Override
  public ZonedDateTime minus(long amountToSubtract, TemporalUnit unit) {
    return (amountToSubtract == Long.MIN_VALUE ? plus(Long.MAX_VALUE, unit).plus(1, unit)
        : plus(-amountToSubtract, unit));
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R> R query(TemporalQuery<R> query) {
    if (query == TemporalQueries.localDate()) {
      return (R) toLocalDate();
    }
    return ChronoZonedDateTime.super.query(query);
  }

  @Override
  public long until(Temporal endExclusive, TemporalUnit unit) {
    ZonedDateTime end = ZonedDateTime.from(endExclusive);
    if (unit instanceof ChronoUnit) {
      ZonedDateTime start = this;
      end = end.withZoneSameInstant(zone);
      if (unit.isDateBased()) {
        return start.dateTime.until(end.dateTime, unit);
      } else {
        return start.toOffsetDateTime().until(end.toOffsetDateTime(), unit);
      }
    }
    return unit.between(this, end);
  }

  @Override
  public String format(DateTimeFormatter formatter) {
    Objects.requireNonNull(formatter, "formatter");
    return formatter.format(this);
  }

  public OffsetDateTime toOffsetDateTime() {
    return OffsetDateTime.of(dateTime, offset);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof ZonedDateTime) {
      ZonedDateTime other = (ZonedDateTime) obj;
      return dateTime.equals(other.dateTime) && offset.equals(other.offset) && zone.equals(other.zone);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return dateTime.hashCode() ^ offset.hashCode() ^ Integer.rotateLeft(zone.hashCode(), 3);
  }

  @Override
  public String toString() {
    String str = dateTime.toString() + offset.toString();
    if (offset != zone) {
      str += '[' + zone.toString() + ']';
    }
    return str;
  }
}
