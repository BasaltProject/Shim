/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.time;

import java.io.Serializable;
import java.time.chrono.ChronoLocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.Objects;

public final class LocalDateTime implements Temporal, TemporalAdjuster, ChronoLocalDateTime<LocalDate>, Serializable {
  public static final LocalDateTime MIN = LocalDateTime.of(LocalDate.MIN, LocalTime.MIN);
  public static final LocalDateTime MAX = LocalDateTime.of(LocalDate.MAX, LocalTime.MAX);

  private final LocalDate date;
  private final LocalTime time;

  private LocalDateTime(LocalDate date, LocalTime time) {
    this.date = date;
    this.time = time;
  }

  public static LocalDateTime now() {
    return now(Clock.systemDefaultZone());
  }

  public static LocalDateTime now(ZoneId zone) {
    return now(Clock.system(zone));
  }

  public static LocalDateTime now(Clock clock) {
    Objects.requireNonNull(clock, "clock");
    Instant now = clock.instant();
    ZoneOffset offset = clock.getZone().getRules().getOffset(now);
    return ofEpochSecond(now.getEpochSecond(), now.getNano(), offset);
  }

  public static LocalDateTime of(int year, Month month, int dayOfMonth, int hour, int minute) {
    LocalDate d = LocalDate.of(year, month, dayOfMonth);
    LocalTime t = LocalTime.of(hour, minute);
    return new LocalDateTime(d, t);
  }

  public static LocalDateTime of(int year, Month month, int dayOfMonth, int hour, int minute, int second) {
    LocalDate d = LocalDate.of(year, month, dayOfMonth);
    LocalTime t = LocalTime.of(hour, minute, second);
    return new LocalDateTime(d, t);
  }

  public static LocalDateTime of(int year, Month month, int dayOfMonth, int hour, int minute, int second, int nanoOfSecond) {
    LocalDate d = LocalDate.of(year, month, dayOfMonth);
    LocalTime t = LocalTime.of(hour, minute, second, nanoOfSecond);
    return new LocalDateTime(d, t);
  }

  public static LocalDateTime of(int year, int month, int dayOfMonth, int hour, int minute) {
    LocalDate d = LocalDate.of(year, month, dayOfMonth);
    LocalTime t = LocalTime.of(hour, minute);
    return new LocalDateTime(d, t);
  }

  public static LocalDateTime of(int year, int month, int dayOfMonth, int hour, int minute, int second) {
    LocalDate d = LocalDate.of(year, month, dayOfMonth);
    LocalTime t = LocalTime.of(hour, minute, second);
    return new LocalDateTime(d, t);
  }

  public static LocalDateTime of(int year, int month, int dayOfMonth, int hour, int minute, int second, int nanoOfSecond) {
    LocalDate d = LocalDate.of(year, month, dayOfMonth);
    LocalTime t = LocalTime.of(hour, minute, second, nanoOfSecond);
    return new LocalDateTime(d, t);
  }

  public static LocalDateTime of(LocalDate date, LocalTime time) {
    Objects.requireNonNull(date, "date");
    Objects.requireNonNull(time, "time");
    return new LocalDateTime(date, time);
  }

  public static LocalDateTime ofInstant(Instant instant, ZoneId zone) {
    Objects.requireNonNull(instant, "instant");
    Objects.requireNonNull(zone, "zone");
    ZoneOffset offset = zone.getRules().getOffset(instant);
    return ofEpochSecond(instant.getEpochSecond(), instant.getNano(), offset);
  }

  public static LocalDateTime ofEpochSecond(long epochSecond, int nanoOfSecond, ZoneOffset offset) {
    Objects.requireNonNull(offset, "offset");
    ChronoField.NANO_OF_SECOND.checkValidValue(nanoOfSecond);
    long localSecond = epochSecond + offset.getTotalSeconds();
    long localEpochDay = Math.floorDiv(localSecond, 86400L);
    int secsOfDay = (int) Math.floorMod(localSecond, 86400L);
    LocalDate date = LocalDate.ofEpochDay(localEpochDay);
    LocalTime time = LocalTime.ofNanoOfDay(((long) secsOfDay) * 1000000000L + nanoOfSecond);
    return new LocalDateTime(date, time);
  }

  public static LocalDateTime from(TemporalAccessor temporal) {
    Objects.requireNonNull(temporal, "temporal");
    if (temporal instanceof LocalDateTime) {
      return (LocalDateTime) temporal;
    }
    LocalDate d = LocalDate.from(temporal);
    LocalTime t = LocalTime.from(temporal);
    return new LocalDateTime(d, t);
  }

  public LocalDate toLocalDate() {
    return date;
  }

  public LocalTime toLocalTime() {
    return time;
  }

  public int getYear() {
    return date.getYear();
  }

  public int getMonthValue() {
    return date.getMonthValue();
  }

  public Month getMonth() {
    return date.getMonth();
  }

  public int getDayOfMonth() {
    return date.getDayOfMonth();
  }

  public int getDayOfYear() {
    return date.getDayOfYear();
  }

  public DayOfWeek getDayOfWeek() {
    return date.getDayOfWeek();
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
  public boolean isSupported(TemporalField field) {
    if (field instanceof ChronoField) {
      return field.isDateBased() || field.isTimeBased();
    }
    return field != null && field.isSupportedBy(this);
  }

  @Override
  public boolean isSupported(TemporalUnit unit) {
    if (unit instanceof ChronoUnit) {
      return true;
    }
    return unit != null && unit.isSupportedBy(this);
  }

  @Override
  public long getLong(TemporalField field) {
    if (field instanceof ChronoField) {
      ChronoField cf = (ChronoField) field;
      return cf.isTimeBased() ? time.getLong(field) : date.getLong(field);
    }
    return field.getFrom(this);
  }

  @Override
  public LocalDateTime with(TemporalField field, long newValue) {
    if (field instanceof ChronoField) {
      ChronoField cf = (ChronoField) field;
      if (cf.isTimeBased()) {
        return with(date, time.with(field, newValue));
      } else {
        return with(date.with(field, newValue), time);
      }
    }
    return (LocalDateTime) field.adjustInto(this, newValue);
  }

  private LocalDateTime with(LocalDate newDate, LocalTime newTime) {
    if (date == newDate && time == newTime) {
      return this;
    }
    return new LocalDateTime(newDate, newTime);
  }

  public LocalDateTime withYear(int year) {
    return with(date.withYear(year), time);
  }

  public LocalDateTime withMonth(int month) {
    return with(date.withMonth(month), time);
  }

  public LocalDateTime withDayOfMonth(int dayOfMonth) {
    return with(date.withDayOfMonth(dayOfMonth), time);
  }

  public LocalDateTime withDayOfYear(int dayOfYear) {
    return with(date.withDayOfYear(dayOfYear), time);
  }

  public LocalDateTime withHour(int hour) {
    return with(date, time.withHour(hour));
  }

  public LocalDateTime withMinute(int minute) {
    return with(date, time.withMinute(minute));
  }

  public LocalDateTime withSecond(int second) {
    return with(date, time.withSecond(second));
  }

  public LocalDateTime withNano(int nanoOfSecond) {
    return with(date, time.withNano(nanoOfSecond));
  }

  @Override
  public LocalDateTime plus(TemporalAmount amount) {
    return (LocalDateTime) amount.addTo(this);
  }

  @Override
  public LocalDateTime plus(long amountToAdd, TemporalUnit unit) {
    if (unit instanceof ChronoUnit) {
      ChronoUnit f = (ChronoUnit) unit;
      switch (f) {
        case NANOS:
          return plusNanos(amountToAdd);
        case MICROS:
          return plusDays(amountToAdd / (86400L * 1000000L)).plusNanos((amountToAdd % (86400L * 1000000L)) * 1000L);
        case MILLIS:
          return plusDays(amountToAdd / (86400L * 1000L)).plusNanos((amountToAdd % (86400L * 1000L)) * 1000000L);
        case SECONDS:
          return plusSeconds(amountToAdd);
        case MINUTES:
          return plusMinutes(amountToAdd);
        case HOURS:
          return plusHours(amountToAdd);
        case HALF_DAYS:
          return plusDays(amountToAdd / 2).plusHours((amountToAdd % 2) * 12);
        case DAYS:
          return plusDays(amountToAdd);
        case WEEKS:
          return plusWeeks(amountToAdd);
        case MONTHS:
          return plusMonths(amountToAdd);
        case YEARS:
          return plusYears(amountToAdd);
        case DECADES:
          return plusYears(Math.multiplyExact(amountToAdd, 10L));
        case CENTURIES:
          return plusYears(Math.multiplyExact(amountToAdd, 100L));
        case MILLENNIA:
          return plusYears(Math.multiplyExact(amountToAdd, 1000L));
        case ERAS:
          return with(ChronoField.ERA, Math.addExact(getLong(ChronoField.ERA), amountToAdd));
        default:
          throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
      }
    }
    return (LocalDateTime) unit.addTo(this, amountToAdd);
  }

  public LocalDateTime plusYears(long years) {
    return with(date.plusYears(years), time);
  }

  public LocalDateTime plusMonths(long months) {
    return with(date.plusMonths(months), time);
  }

  public LocalDateTime plusWeeks(long weeks) {
    return with(date.plusWeeks(weeks), time);
  }

  public LocalDateTime plusDays(long days) {
    return with(date.plusDays(days), time);
  }

  public LocalDateTime plusHours(long hours) {
    return plusWithOverflow(date, hours, 0, 0, 0, 1);
  }

  public LocalDateTime plusMinutes(long minutes) {
    return plusWithOverflow(date, 0, minutes, 0, 0, 1);
  }

  public LocalDateTime plusSeconds(long seconds) {
    return plusWithOverflow(date, 0, 0, seconds, 0, 1);
  }

  public LocalDateTime plusNanos(long nanos) {
    return plusWithOverflow(date, 0, 0, 0, nanos, 1);
  }

  private LocalDateTime plusWithOverflow(LocalDate newDate, long hours, long minutes, long seconds, long nanos, int sign) {
    if ((hours | minutes | seconds | nanos) == 0) {
      return with(newDate, time);
    }
    long totDays = nanos / 86400000000000L +
                   seconds / 86400L +
                   minutes / 1440L +
                   hours / 24L;
    totDays *= sign;
    long totNanos = nanos % 86400000000000L +
                    (seconds % 86400L) * 1000000000L +
                    (minutes % 1440L) * 60000000000L +
                    (hours % 24L) * 3600000000000L;
    long curNoD = time.toNanoOfDay();
    totNanos = totNanos * sign + curNoD;
    totDays += Math.floorDiv(totNanos, 86400000000000L);
    long newNoD = Math.floorMod(totNanos, 86400000000000L);
    LocalTime newTime = (newNoD == curNoD ? time : LocalTime.ofNanoOfDay(newNoD));
    return with(newDate.plusDays(totDays), newTime);
  }

  @Override
  public LocalDateTime minus(TemporalAmount amount) {
    return (LocalDateTime) amount.subtractFrom(this);
  }

  @Override
  public LocalDateTime minus(long amountToSubtract, TemporalUnit unit) {
    return (amountToSubtract == Long.MIN_VALUE)
        ? plus(Long.MAX_VALUE, unit).plus(1, unit)
        : plus(-amountToSubtract, unit);
  }

  public LocalDateTime minusYears(long years) {
    return (years == Long.MIN_VALUE) ? plusYears(Long.MAX_VALUE).plusYears(1) : plusYears(-years);
  }

  public LocalDateTime minusMonths(long months) {
    return (months == Long.MIN_VALUE) ? plusMonths(Long.MAX_VALUE).plusMonths(1) : plusMonths(-months);
  }

  public LocalDateTime minusWeeks(long weeks) {
    return (weeks == Long.MIN_VALUE) ? plusWeeks(Long.MAX_VALUE).plusWeeks(1) : plusWeeks(-weeks);
  }

  public LocalDateTime minusDays(long days) {
    return (days == Long.MIN_VALUE) ? plusDays(Long.MAX_VALUE).plusDays(1) : plusDays(-days);
  }

  public LocalDateTime minusHours(long hours) {
    return plusWithOverflow(date, hours, 0, 0, 0, -1);
  }

  public LocalDateTime minusMinutes(long minutes) {
    return plusWithOverflow(date, 0, minutes, 0, 0, -1);
  }

  public LocalDateTime minusSeconds(long seconds) {
    return plusWithOverflow(date, 0, 0, seconds, 0, -1);
  }

  public LocalDateTime minusNanos(long nanos) {
    return plusWithOverflow(date, 0, 0, 0, nanos, -1);
  }

  @Override
  public Temporal adjustInto(Temporal temporal) {
    return temporal.with(ChronoField.EPOCH_DAY, date.toEpochDay())
                   .with(ChronoField.NANO_OF_DAY, time.toNanoOfDay());
  }

  @Override
  public long until(Temporal endExclusive, TemporalUnit unit) {
    LocalDateTime end = LocalDateTime.from(endExclusive);
    if (unit instanceof ChronoUnit) {
      if (unit.isTimeBased()) {
        long daysUntil = date.until(end.date, ChronoUnit.DAYS);
        long nanosUntil = end.time.toNanoOfDay() - time.toNanoOfDay();
        if (daysUntil > 0 && nanosUntil < 0) {
          daysUntil--;
          nanosUntil += 86400000000000L;
        } else if (daysUntil < 0 && nanosUntil > 0) {
          daysUntil++;
          nanosUntil -= 86400000000000L;
        }
        long totalNanos = Math.addExact(Math.multiplyExact(daysUntil, 86400000000000L), nanosUntil);
        switch ((ChronoUnit) unit) {
          case NANOS:
            return totalNanos;
          case MICROS:
            return totalNanos / 1000L;
          case MILLIS:
            return totalNanos / 1000000L;
          case SECONDS:
            return totalNanos / 1000000000L;
          case MINUTES:
            return totalNanos / 60000000000L;
          case HOURS:
            return totalNanos / 3600000000000L;
          case HALF_DAYS:
            return totalNanos / (12 * 3600000000000L);
          default:
            throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
        }
      }
      LocalDate endDate = end.date;
      if (endDate.isAfter(date) && end.time.isBefore(time)) {
        endDate = endDate.minusDays(1);
      } else if (endDate.isBefore(date) && end.time.isAfter(time)) {
        endDate = endDate.plusDays(1);
      }
      return date.until(endDate, unit);
    }
    return unit.between(this, end);
  }

  public Instant toInstant(ZoneOffset offset) {
    return Instant.ofEpochSecond(toEpochSecond(offset), time.getNano());
  }

  public long toEpochSecond(ZoneOffset offset) {
    Objects.requireNonNull(offset, "offset");
    long epochDay = date.toEpochDay();
    long secs = epochDay * 86400L + time.toSecondOfDay();
    return secs - offset.getTotalSeconds();
  }

  @Override
  public int compareTo(ChronoLocalDateTime<?> other) {
    if (other instanceof LocalDateTime) {
      return compareTo((LocalDateTime) other);
    }
    return ChronoLocalDateTime.super.compareTo(other);
  }

  public int compareTo(LocalDateTime other) {
    int cmp = date.compareTo(other.date);
    if (cmp == 0) {
      cmp = time.compareTo(other.time);
    }
    return cmp;
  }

  @Override
  public boolean isAfter(ChronoLocalDateTime<?> other) {
    if (other instanceof LocalDateTime) {
      return compareTo((LocalDateTime) other) > 0;
    }
    return ChronoLocalDateTime.super.isAfter(other);
  }

  @Override
  public boolean isBefore(ChronoLocalDateTime<?> other) {
    if (other instanceof LocalDateTime) {
      return compareTo((LocalDateTime) other) < 0;
    }
    return ChronoLocalDateTime.super.isBefore(other);
  }

  @Override
  public boolean isEqual(ChronoLocalDateTime<?> other) {
    if (other instanceof LocalDateTime) {
      return compareTo((LocalDateTime) other) == 0;
    }
    return ChronoLocalDateTime.super.isEqual(other);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof LocalDateTime) {
      LocalDateTime other = (LocalDateTime) obj;
      return date.equals(other.date) && time.equals(other.time);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return date.hashCode() ^ time.hashCode();
  }

  @Override
  public String toString() {
    return date.toString() + 'T' + time.toString();
  }

  public String format(DateTimeFormatter formatter) {
    Objects.requireNonNull(formatter, "formatter");
    return formatter.format(this);
  }

  public static LocalDateTime parse(CharSequence text) {
    return parse(text, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
  }

  public static LocalDateTime parse(CharSequence text, DateTimeFormatter formatter) {
    Objects.requireNonNull(formatter, "formatter");
    return formatter.parse(text, LocalDateTime::from);
  }
}
