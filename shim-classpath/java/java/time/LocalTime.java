/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.time;

import java.io.Serializable;
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

public final class LocalTime implements Temporal, TemporalAdjuster, Comparable<LocalTime>, Serializable {
  public static final LocalTime MIN = new LocalTime(0, 0, 0, 0);
  public static final LocalTime MAX = new LocalTime(23, 59, 59, 999999999);
  public static final LocalTime MIDNIGHT = new LocalTime(0, 0, 0, 0);
  public static final LocalTime NOON = new LocalTime(12, 0, 0, 0);

  private static final long NANOS_PER_SECOND = 1000000000L;
  private static final long NANOS_PER_MINUTE = 60L * NANOS_PER_SECOND;
  private static final long NANOS_PER_HOUR = 3600L * NANOS_PER_SECOND;
  private static final long NANOS_PER_DAY = 86400L * NANOS_PER_SECOND;
  private static final int SECONDS_PER_DAY = 86400;

  private final byte hour;
  private final byte minute;
  private final byte second;
  private final int nano;

  private LocalTime(int hour, int minute, int second, int nano) {
    this.hour = (byte) hour;
    this.minute = (byte) minute;
    this.second = (byte) second;
    this.nano = nano;
  }

  public static LocalTime now() {
    return now(Clock.systemDefaultZone());
  }

  public static LocalTime now(ZoneId zone) {
    return now(Clock.system(zone));
  }

  public static LocalTime now(Clock clock) {
    Objects.requireNonNull(clock, "clock");
    Instant now = clock.instant();
    ZoneOffset offset = clock.getZone().getRules().getOffset(now);
    long localSecond = now.getEpochSecond() + offset.getTotalSeconds();
    int secsOfDay = (int) Math.floorMod(localSecond, (long) SECONDS_PER_DAY);
    return ofNanoOfDay(((long) secsOfDay) * NANOS_PER_SECOND + now.getNano());
  }

  public static LocalTime of(int hour, int minute) {
    return of(hour, minute, 0, 0);
  }

  public static LocalTime of(int hour, int minute, int second) {
    return of(hour, minute, second, 0);
  }

  public static LocalTime of(int hour, int minute, int second, int nanoOfSecond) {
    ChronoField.HOUR_OF_DAY.checkValidValue(hour);
    ChronoField.MINUTE_OF_HOUR.checkValidValue(minute);
    ChronoField.SECOND_OF_MINUTE.checkValidValue(second);
    ChronoField.NANO_OF_SECOND.checkValidValue(nanoOfSecond);
    return create(hour, minute, second, nanoOfSecond);
  }

  public static LocalTime ofSecondOfDay(long secondOfDay) {
    return ofSecondOfDay(secondOfDay, 0);
  }

  public static LocalTime ofSecondOfDay(long secondOfDay, int nanoOfSecond) {
    ChronoField.SECOND_OF_DAY.checkValidValue(secondOfDay);
    ChronoField.NANO_OF_SECOND.checkValidValue(nanoOfSecond);
    int hours = (int) (secondOfDay / 3600);
    secondOfDay -= hours * 3600;
    int minutes = (int) (secondOfDay / 60);
    secondOfDay -= minutes * 60;
    return create(hours, minutes, (int) secondOfDay, nanoOfSecond);
  }

  public static LocalTime ofNanoOfDay(long nanoOfDay) {
    ChronoField.NANO_OF_DAY.checkValidValue(nanoOfDay);
    int hours = (int) (nanoOfDay / NANOS_PER_HOUR);
    nanoOfDay -= hours * NANOS_PER_HOUR;
    int minutes = (int) (nanoOfDay / NANOS_PER_MINUTE);
    nanoOfDay -= minutes * NANOS_PER_MINUTE;
    int seconds = (int) (nanoOfDay / NANOS_PER_SECOND);
    nanoOfDay -= seconds * NANOS_PER_SECOND;
    return create(hours, minutes, seconds, (int) nanoOfDay);
  }

  public static LocalTime from(TemporalAccessor temporal) {
    Objects.requireNonNull(temporal, "temporal");
    if (temporal instanceof LocalTime) {
      return (LocalTime) temporal;
    }
    if (temporal.isSupported(ChronoField.NANO_OF_DAY)) {
      return ofNanoOfDay(temporal.getLong(ChronoField.NANO_OF_DAY));
    }
    if (temporal.isSupported(ChronoField.SECOND_OF_DAY)) {
      return ofSecondOfDay(temporal.getLong(ChronoField.SECOND_OF_DAY));
    }
    int hour = temporal.get(ChronoField.HOUR_OF_DAY);
    int minute = temporal.get(ChronoField.MINUTE_OF_HOUR);
    int second = temporal.isSupported(ChronoField.SECOND_OF_MINUTE) ? temporal.get(ChronoField.SECOND_OF_MINUTE) : 0;
    int nano = temporal.isSupported(ChronoField.NANO_OF_SECOND) ? temporal.get(ChronoField.NANO_OF_SECOND) : 0;
    return of(hour, minute, second, nano);
  }

  private static LocalTime create(int hour, int minute, int second, int nano) {
    if ((hour | minute | second | nano) == 0) {
      return MIDNIGHT;
    }
    return new LocalTime(hour, minute, second, nano);
  }

  public int getHour() {
    return hour;
  }

  public int getMinute() {
    return minute;
  }

  public int getSecond() {
    return second;
  }

  public int getNano() {
    return nano;
  }

  public int toSecondOfDay() {
    return hour * 3600 + minute * 60 + second;
  }

  public long toNanoOfDay() {
    long total = hour * NANOS_PER_HOUR;
    total += minute * NANOS_PER_MINUTE;
    total += second * NANOS_PER_SECOND;
    return total + nano;
  }

  @Override
  public boolean isSupported(TemporalField field) {
    if (field instanceof ChronoField) {
      return field.isTimeBased();
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
  public long getLong(TemporalField field) {
    if (field instanceof ChronoField) {
      switch ((ChronoField) field) {
        case NANO_OF_SECOND:
          return nano;
        case NANO_OF_DAY:
          return toNanoOfDay();
        case MICRO_OF_SECOND:
          return nano / 1000L;
        case MICRO_OF_DAY:
          return toNanoOfDay() / 1000L;
        case MILLI_OF_SECOND:
          return nano / 1000000L;
        case MILLI_OF_DAY:
          return toNanoOfDay() / 1000000L;
        case SECOND_OF_MINUTE:
          return second;
        case SECOND_OF_DAY:
          return toSecondOfDay();
        case MINUTE_OF_HOUR:
          return minute;
        case MINUTE_OF_DAY:
          return hour * 60 + minute;
        case HOUR_OF_AMPM:
          return hour % 12;
        case CLOCK_HOUR_OF_AMPM: {
          int pm = hour % 12;
          return (pm % 12 == 0 ? 12 : pm);
        }
        case HOUR_OF_DAY:
          return hour;
        case CLOCK_HOUR_OF_DAY:
          return (hour == 0 ? 24 : hour);
        case AMPM_OF_DAY:
          return hour / 12;
        default:
          throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
      }
    }
    return field.getFrom(this);
  }

  @Override
  public LocalTime with(TemporalField field, long newValue) {
    if (field instanceof ChronoField) {
      ChronoField cf = (ChronoField) field;
      cf.checkValidValue(newValue);
      switch (cf) {
        case NANO_OF_SECOND:
          return withNano((int) newValue);
        case NANO_OF_DAY:
          return LocalTime.ofNanoOfDay(newValue);
        case MICRO_OF_SECOND:
          return withNano((int) newValue * 1000);
        case MICRO_OF_DAY:
          return plusNanos((newValue - toNanoOfDay() / 1000) * 1000);
        case MILLI_OF_SECOND:
          return withNano((int) newValue * 1000000);
        case MILLI_OF_DAY:
          return plusNanos((newValue - toNanoOfDay() / 1000000) * 1000000);
        case SECOND_OF_MINUTE:
          return withSecond((int) newValue);
        case SECOND_OF_DAY:
          return plusSeconds(newValue - toSecondOfDay());
        case MINUTE_OF_HOUR:
          return withMinute((int) newValue);
        case MINUTE_OF_DAY:
          return plusMinutes(newValue - (hour * 60 + minute));
        case HOUR_OF_AMPM:
          return plusHours(newValue - (hour % 12));
        case CLOCK_HOUR_OF_AMPM:
          return plusHours((newValue == 12 ? 0 : newValue) - (hour % 12));
        case HOUR_OF_DAY:
          return withHour((int) newValue);
        case CLOCK_HOUR_OF_DAY:
          return withHour((int) (newValue == 24 ? 0 : newValue));
        case AMPM_OF_DAY:
          return plusHours((newValue - (hour / 12)) * 12);
        default:
          throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
      }
    }
    return (LocalTime) field.adjustInto(this, newValue);
  }

  public LocalTime withHour(int hour) {
    if (this.hour == hour) {
      return this;
    }
    ChronoField.HOUR_OF_DAY.checkValidValue(hour);
    return create(hour, minute, second, nano);
  }

  public LocalTime withMinute(int minute) {
    if (this.minute == minute) {
      return this;
    }
    ChronoField.MINUTE_OF_HOUR.checkValidValue(minute);
    return create(hour, minute, second, nano);
  }

  public LocalTime withSecond(int second) {
    if (this.second == second) {
      return this;
    }
    ChronoField.SECOND_OF_MINUTE.checkValidValue(second);
    return create(hour, minute, second, nano);
  }

  public LocalTime withNano(int nanoOfSecond) {
    if (this.nano == nanoOfSecond) {
      return this;
    }
    ChronoField.NANO_OF_SECOND.checkValidValue(nanoOfSecond);
    return create(hour, minute, second, nanoOfSecond);
  }

  @Override
  public LocalTime plus(TemporalAmount amount) {
    return (LocalTime) amount.addTo(this);
  }

  @Override
  public LocalTime plus(long amountToAdd, TemporalUnit unit) {
    if (unit instanceof ChronoUnit) {
      ChronoUnit f = (ChronoUnit) unit;
      switch (f) {
        case NANOS:
          return plusNanos(amountToAdd);
        case MICROS:
          return plusNanos((amountToAdd % (NANOS_PER_DAY / 1000)) * 1000);
        case MILLIS:
          return plusNanos((amountToAdd % (NANOS_PER_DAY / 1000000)) * 1000000);
        case SECONDS:
          return plusSeconds(amountToAdd);
        case MINUTES:
          return plusMinutes(amountToAdd);
        case HOURS:
          return plusHours(amountToAdd);
        case HALF_DAYS:
          return plusHours((amountToAdd % 2) * 12);
        default:
          throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
      }
    }
    return (LocalTime) unit.addTo(this, amountToAdd);
  }

  public LocalTime plusHours(long hoursToAdd) {
    if (hoursToAdd == 0) {
      return this;
    }
    int newHour = ((int) (hoursToAdd % 24) + hour + 24) % 24;
    return create(newHour, minute, second, nano);
  }

  public LocalTime plusMinutes(long minutesToAdd) {
    if (minutesToAdd == 0) {
      return this;
    }
    int m = (int) (minutesToAdd % 1440);
    int newMinutes = hour * 60 + minute + m;
    newMinutes = (newMinutes % 1440 + 1440) % 1440;
    return create(newMinutes / 60, newMinutes % 60, second, nano);
  }

  public LocalTime plusSeconds(long secondsToAdd) {
    if (secondsToAdd == 0) {
      return this;
    }
    int s = (int) (secondsToAdd % SECONDS_PER_DAY);
    int newSec = toSecondOfDay() + s;
    newSec = (newSec % SECONDS_PER_DAY + SECONDS_PER_DAY) % SECONDS_PER_DAY;
    return ofSecondOfDay(newSec, nano);
  }

  public LocalTime plusNanos(long nanosToAdd) {
    if (nanosToAdd == 0) {
      return this;
    }
    long totalNanos = (nanosToAdd % NANOS_PER_DAY) + toNanoOfDay();
    totalNanos = (totalNanos % NANOS_PER_DAY + NANOS_PER_DAY) % NANOS_PER_DAY;
    return ofNanoOfDay(totalNanos);
  }

  @Override
  public LocalTime minus(TemporalAmount amount) {
    return (LocalTime) amount.subtractFrom(this);
  }

  @Override
  public LocalTime minus(long amountToSubtract, TemporalUnit unit) {
    return (amountToSubtract == Long.MIN_VALUE)
        ? plus(Long.MAX_VALUE, unit).plus(1, unit)
        : plus(-amountToSubtract, unit);
  }

  public LocalTime minusHours(long hoursToSubtract) {
    return plusHours(-(hoursToSubtract % 24));
  }

  public LocalTime minusMinutes(long minutesToSubtract) {
    return plusMinutes(-(minutesToSubtract % 1440));
  }

  public LocalTime minusSeconds(long secondsToSubtract) {
    return plusSeconds(-(secondsToSubtract % SECONDS_PER_DAY));
  }

  public LocalTime minusNanos(long nanosToSubtract) {
    return plusNanos(-(nanosToSubtract % NANOS_PER_DAY));
  }

  @Override
  public Temporal adjustInto(Temporal temporal) {
    return temporal.with(ChronoField.NANO_OF_DAY, toNanoOfDay());
  }

  @Override
  public long until(Temporal endExclusive, TemporalUnit unit) {
    LocalTime end = LocalTime.from(endExclusive);
    if (unit instanceof ChronoUnit) {
      long nanos = end.toNanoOfDay() - toNanoOfDay();
      switch ((ChronoUnit) unit) {
        case NANOS:
          return nanos;
        case MICROS:
          return nanos / 1000;
        case MILLIS:
          return nanos / 1000000;
        case SECONDS:
          return nanos / NANOS_PER_SECOND;
        case MINUTES:
          return nanos / NANOS_PER_MINUTE;
        case HOURS:
          return nanos / NANOS_PER_HOUR;
        case HALF_DAYS:
          return nanos / (12 * NANOS_PER_HOUR);
        default:
          throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
      }
    }
    return unit.between(this, end);
  }

  public LocalDateTime atDate(LocalDate date) {
    return LocalDateTime.of(date, this);
  }

  @Override
  public int compareTo(LocalTime other) {
    int cmp = Byte.compare(hour, other.hour);
    if (cmp == 0) {
      cmp = Byte.compare(minute, other.minute);
      if (cmp == 0) {
        cmp = Byte.compare(second, other.second);
        if (cmp == 0) {
          cmp = Integer.compare(nano, other.nano);
        }
      }
    }
    return cmp;
  }

  public boolean isAfter(LocalTime other) {
    return compareTo(other) > 0;
  }

  public boolean isBefore(LocalTime other) {
    return compareTo(other) < 0;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof LocalTime) {
      LocalTime other = (LocalTime) obj;
      return hour == other.hour && minute == other.minute && second == other.second && nano == other.nano;
    }
    return false;
  }

  @Override
  public int hashCode() {
    long nod = toNanoOfDay();
    return (int) (nod ^ (nod >>> 32));
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(18);
    appendZeroPadded(sb, hour, 2);
    sb.append(':');
    appendZeroPadded(sb, minute, 2);
    if (second > 0 || nano > 0) {
      sb.append(':');
      appendZeroPadded(sb, second, 2);
      if (nano > 0) {
        sb.append('.');
        if (nano % 1000000 == 0) {
          appendZeroPadded(sb, nano / 1000000, 3);
        } else if (nano % 1000 == 0) {
          appendZeroPadded(sb, nano / 1000, 6);
        } else {
          appendZeroPadded(sb, nano, 9);
        }
      }
    }
    return sb.toString();
  }

  public String format(DateTimeFormatter formatter) {
    Objects.requireNonNull(formatter, "formatter");
    return formatter.format(this);
  }

  public static LocalTime parse(CharSequence text) {
    return parse(text, DateTimeFormatter.ISO_LOCAL_TIME);
  }

  public static LocalTime parse(CharSequence text, DateTimeFormatter formatter) {
    Objects.requireNonNull(formatter, "formatter");
    return formatter.parse(text, LocalTime::from);
  }

  private static void appendZeroPadded(StringBuilder sb, int value, int digits) {
    String s = Integer.toString(value);
    for (int i = s.length(); i < digits; ++i) {
      sb.append('0');
    }
    sb.append(s);
  }
}
