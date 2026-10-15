/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.time;

import java.io.Serializable;
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

public final class Instant implements Temporal, TemporalAdjuster, Comparable<Instant>, Serializable {
  public static final Instant EPOCH = new Instant(0, 0);

  public static final Instant MIN = new Instant(-31557014167219200L, 0);

  public static final Instant MAX = new Instant(31556889864403199L, 999999999);

  private static final long NANOS_PER_SECOND = 1000000000L;
  private static final long MILLIS_PER_SECOND = 1000L;

  private final long seconds;
  private final int nanos;

  private Instant(long epochSecond, int nanos) {
    this.seconds = epochSecond;
    this.nanos = nanos;
  }

  public static Instant now() {
    return Clock.systemUTC().instant();
  }

  public static Instant now(Clock clock) {
    Objects.requireNonNull(clock, "clock");
    return clock.instant();
  }

  public static Instant ofEpochSecond(long epochSecond) {
    return create(epochSecond, 0);
  }

  public static Instant ofEpochSecond(long epochSecond, long nanoAdjustment) {
    long sec = Math.addExact(epochSecond, Math.floorDiv(nanoAdjustment, NANOS_PER_SECOND));
    int nos = (int) Math.floorMod(nanoAdjustment, NANOS_PER_SECOND);
    return create(sec, nos);
  }

  public static Instant ofEpochMilli(long epochMilli) {
    long sec = Math.floorDiv(epochMilli, MILLIS_PER_SECOND);
    int mos = (int) Math.floorMod(epochMilli, MILLIS_PER_SECOND);
    return create(sec, mos * 1000000);
  }

  public static Instant from(TemporalAccessor temporal) {
    Objects.requireNonNull(temporal, "temporal");
    if (temporal instanceof Instant) {
      return (Instant) temporal;
    }
    long sec = temporal.getLong(ChronoField.INSTANT_SECONDS);
    int nos = 0;
    if (temporal.isSupported(ChronoField.NANO_OF_SECOND)) {
      nos = (int) temporal.getLong(ChronoField.NANO_OF_SECOND);
    }
    return ofEpochSecond(sec, nos);
  }

  private static Instant create(long seconds, int nanoOfSecond) {
    if (seconds == 0 && nanoOfSecond == 0) {
      return EPOCH;
    }
    if (seconds < MIN.seconds || seconds > MAX.seconds) {
      throw new DateTimeException("Instant exceeds minimum or maximum bounds");
    }
    return new Instant(seconds, nanoOfSecond);
  }

  public long getEpochSecond() {
    return seconds;
  }

  public int getNano() {
    return nanos;
  }

  public long toEpochMilli() {
    if (seconds >= 0) {
      long millis = Math.multiplyExact(seconds, MILLIS_PER_SECOND);
      return Math.addExact(millis, nanos / 1000000);
    } else {
      long millis = Math.multiplyExact(seconds + 1, MILLIS_PER_SECOND);
      return Math.addExact(millis, (nanos / 1000000) - 1000L);
    }
  }

  @Override
  public boolean isSupported(TemporalField field) {
    if (field instanceof ChronoField) {
      return field == ChronoField.INSTANT_SECONDS
          || field == ChronoField.NANO_OF_SECOND
          || field == ChronoField.MICRO_OF_SECOND
          || field == ChronoField.MILLI_OF_SECOND;
    }
    return field != null && field.isSupportedBy(this);
  }

  @Override
  public boolean isSupported(TemporalUnit unit) {
    if (unit instanceof ChronoUnit) {
      return unit.isTimeBased() || unit == ChronoUnit.DAYS;
    }
    return unit != null && unit.isSupportedBy(this);
  }

  @Override
  public long getLong(TemporalField field) {
    if (field instanceof ChronoField) {
      switch ((ChronoField) field) {
        case NANO_OF_SECOND:
          return nanos;
        case MICRO_OF_SECOND:
          return nanos / 1000L;
        case MILLI_OF_SECOND:
          return nanos / 1000000L;
        case INSTANT_SECONDS:
          return seconds;
        default:
          throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
      }
    }
    return field.getFrom(this);
  }

  @Override
  public Instant with(TemporalField field, long newValue) {
    if (field instanceof ChronoField) {
      ChronoField cf = (ChronoField) field;
      cf.checkValidValue(newValue);
      switch (cf) {
        case MILLI_OF_SECOND: {
          int n = (int) newValue * 1000000;
          return (n != nanos) ? create(seconds, n) : this;
        }
        case MICRO_OF_SECOND: {
          int n = (int) newValue * 1000;
          return (n != nanos) ? create(seconds, n) : this;
        }
        case NANO_OF_SECOND:
          return (newValue != nanos) ? create(seconds, (int) newValue) : this;
        case INSTANT_SECONDS:
          return (newValue != seconds) ? create(newValue, nanos) : this;
        default:
          throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
      }
    }
    return (Instant) field.adjustInto(this, newValue);
  }

  @Override
  public Instant plus(TemporalAmount amount) {
    return (Instant) amount.addTo(this);
  }

  @Override
  public Instant plus(long amountToAdd, TemporalUnit unit) {
    if (unit instanceof ChronoUnit) {
      switch ((ChronoUnit) unit) {
        case NANOS:
          return plusNanos(amountToAdd);
        case MICROS:
          return plus(amountToAdd / 1000000L, (amountToAdd % 1000000L) * 1000L);
        case MILLIS:
          return plusMillis(amountToAdd);
        case SECONDS:
          return plusSeconds(amountToAdd);
        case MINUTES:
          return plusSeconds(Math.multiplyExact(amountToAdd, 60L));
        case HOURS:
          return plusSeconds(Math.multiplyExact(amountToAdd, 3600L));
        case HALF_DAYS:
          return plusSeconds(Math.multiplyExact(amountToAdd, 43200L));
        case DAYS:
          return plusSeconds(Math.multiplyExact(amountToAdd, 86400L));
        default:
          throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
      }
    }
    return (Instant) unit.addTo(this, amountToAdd);
  }

  public Instant plusSeconds(long secondsToAdd) {
    return plus(secondsToAdd, 0);
  }

  public Instant plusMillis(long millisToAdd) {
    return plus(millisToAdd / MILLIS_PER_SECOND, (millisToAdd % MILLIS_PER_SECOND) * 1000000L);
  }

  public Instant plusNanos(long nanosToAdd) {
    return plus(0, nanosToAdd);
  }

  private Instant plus(long secondsToAdd, long nanosToAdd) {
    if ((secondsToAdd | nanosToAdd) == 0) {
      return this;
    }
    long sec = Math.addExact(seconds, secondsToAdd);
    sec = Math.addExact(sec, nanosToAdd / NANOS_PER_SECOND);
    long nos = nanos + (nanosToAdd % NANOS_PER_SECOND);
    return ofEpochSecond(sec, nos);
  }

  @Override
  public Instant minus(TemporalAmount amount) {
    return (Instant) amount.subtractFrom(this);
  }

  @Override
  public Instant minus(long amountToSubtract, TemporalUnit unit) {
    return (amountToSubtract == Long.MIN_VALUE)
        ? plus(Long.MAX_VALUE, unit).plus(1, unit)
        : plus(-amountToSubtract, unit);
  }

  public Instant minusSeconds(long secondsToSubtract) {
    return (secondsToSubtract == Long.MIN_VALUE)
        ? plusSeconds(Long.MAX_VALUE).plusSeconds(1)
        : plusSeconds(-secondsToSubtract);
  }

  public Instant minusMillis(long millisToSubtract) {
    return (millisToSubtract == Long.MIN_VALUE)
        ? plusMillis(Long.MAX_VALUE).plusMillis(1)
        : plusMillis(-millisToSubtract);
  }

  public Instant minusNanos(long nanosToSubtract) {
    return (nanosToSubtract == Long.MIN_VALUE)
        ? plusNanos(Long.MAX_VALUE).plusNanos(1)
        : plusNanos(-nanosToSubtract);
  }

  @Override
  public Temporal adjustInto(Temporal temporal) {
    return temporal.with(ChronoField.INSTANT_SECONDS, seconds).with(ChronoField.NANO_OF_SECOND, nanos);
  }

  @Override
  public long until(Temporal endExclusive, TemporalUnit unit) {
    Instant end = Instant.from(endExclusive);
    if (unit instanceof ChronoUnit) {
      ChronoUnit f = (ChronoUnit) unit;
      switch (f) {
        case NANOS:
          return nanosUntil(end);
        case MICROS:
          return nanosUntil(end) / 1000L;
        case MILLIS:
          return Math.subtractExact(end.toEpochMilli(), toEpochMilli());
        case SECONDS:
          return secondsUntil(end);
        case MINUTES:
          return secondsUntil(end) / 60L;
        case HOURS:
          return secondsUntil(end) / 3600L;
        case HALF_DAYS:
          return secondsUntil(end) / 43200L;
        case DAYS:
          return secondsUntil(end) / 86400L;
        default:
          throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
      }
    }
    return unit.between(this, end);
  }

  private long nanosUntil(Instant end) {
    long secsDiff = Math.subtractExact(end.seconds, this.seconds);
    long totalNanos = Math.multiplyExact(secsDiff, NANOS_PER_SECOND);
    return Math.addExact(totalNanos, end.nanos - this.nanos);
  }

  private long secondsUntil(Instant end) {
    long secsDiff = Math.subtractExact(end.seconds, this.seconds);
    long nanosDiff = end.nanos - this.nanos;
    if (secsDiff > 0 && nanosDiff < 0) {
      secsDiff--;
    } else if (secsDiff < 0 && nanosDiff > 0) {
      secsDiff++;
    }
    return secsDiff;
  }

  @Override
  public int compareTo(Instant otherInstant) {
    int cmp = Long.compare(seconds, otherInstant.seconds);
    if (cmp != 0) {
      return cmp;
    }
    return Integer.compare(nanos, otherInstant.nanos);
  }

  public boolean isAfter(Instant otherInstant) {
    return compareTo(otherInstant) > 0;
  }

  public boolean isBefore(Instant otherInstant) {
    return compareTo(otherInstant) < 0;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other instanceof Instant) {
      Instant otherInstant = (Instant) other;
      return this.seconds == otherInstant.seconds && this.nanos == otherInstant.nanos;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return ((int) (seconds ^ (seconds >>> 32))) + 51 * nanos;
  }

  @Override
  public String toString() {
    // Format as ISO-8601: YYYY-MM-DDTHH:mm:ss[.SSS...]Z
    long zeroDay = Math.floorDiv(seconds, 86400L);
    long timeSec = Math.floorMod(seconds, 86400L);

    // Gregorian calculation from zeroDay (epochDay 0 = 1970-01-01)
    long zeroDayAdjusted = zeroDay + 719468L;
    long era = (zeroDayAdjusted >= 0 ? zeroDayAdjusted : zeroDayAdjusted - 146096L) / 146097L;
    long doe = zeroDayAdjusted - era * 146097L;
    long yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365L;
    long y = yoe + era * 400L;
    long doy = doe - (365L * yoe + yoe / 4L - yoe / 100L);
    long mp = (5L * doy + 2L) / 153L;
    int d = (int) (doy - (153L * mp + 2L) / 5L + 1L);
    int m = (int) (mp < 10L ? mp + 3L : mp - 9L);
    int year = (int) (m <= 2 ? y + 1L : y);

    int hour = (int) (timeSec / 3600L);
    int minute = (int) ((timeSec % 3600L) / 60L);
    int second = (int) (timeSec % 60L);

    StringBuilder sb = new StringBuilder(30);
    if (year < 0) {
      sb.append('-');
      appendZeroPadded(sb, -year, 4);
    } else if (year > 9999) {
      sb.append('+');
      sb.append(year);
    } else {
      appendZeroPadded(sb, year, 4);
    }
    sb.append('-');
    appendZeroPadded(sb, m, 2);
    sb.append('-');
    appendZeroPadded(sb, d, 2);
    sb.append('T');
    appendZeroPadded(sb, hour, 2);
    sb.append(':');
    appendZeroPadded(sb, minute, 2);
    sb.append(':');
    appendZeroPadded(sb, second, 2);

    if (nanos > 0) {
      sb.append('.');
      if (nanos % 1000000 == 0) {
        appendZeroPadded(sb, nanos / 1000000, 3);
      } else if (nanos % 1000 == 0) {
        appendZeroPadded(sb, nanos / 1000, 6);
      } else {
        appendZeroPadded(sb, nanos, 9);
      }
    }
    sb.append('Z');
    return sb.toString();
  }

  public static Instant parse(CharSequence text) {
    Objects.requireNonNull(text, "text");
    String str = text.toString().trim();
    if (!str.endsWith("Z") && !str.endsWith("z")) {
      throw new DateTimeException("Instant text must end with Z: " + str);
    }
    int tIndex = str.indexOf('T');
    if (tIndex < 0) {
      tIndex = str.indexOf('t');
    }
    if (tIndex < 0) {
      throw new DateTimeException("Invalid Instant format: " + str);
    }
    String datePart = str.substring(0, tIndex);
    String timePart = str.substring(tIndex + 1, str.length() - 1);

    LocalDate date = LocalDate.parse(datePart);
    LocalTime time = LocalTime.parse(timePart);
    return LocalDateTime.of(date, time).toInstant(ZoneOffset.UTC);
  }

  private static void appendZeroPadded(StringBuilder sb, int value, int digits) {
    String s = Integer.toString(value);
    for (int i = s.length(); i < digits; ++i) {
      sb.append('0');
    }
    sb.append(s);
  }
}
