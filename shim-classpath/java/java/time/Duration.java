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
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Duration implements TemporalAmount, Comparable<Duration>, Serializable {
  public static final Duration ZERO = new Duration(0, 0);

  private static final List<TemporalUnit> UNITS = Collections.unmodifiableList(
      Arrays.asList(ChronoUnit.SECONDS, ChronoUnit.NANOS));

  private static final long NANOS_PER_SECOND = 1000000000L;
  private static final long MILLIS_PER_SECOND = 1000L;

  private final long seconds;
  private final int nanos;

  private Duration(long seconds, int nanos) {
    this.seconds = seconds;
    this.nanos = nanos;
  }

  public static Duration ofSeconds(long seconds) {
    return create(seconds, 0);
  }

  public static Duration ofSeconds(long seconds, long nanoAdjustment) {
    long sec = Math.addExact(seconds, Math.floorDiv(nanoAdjustment, NANOS_PER_SECOND));
    int nos = (int) Math.floorMod(nanoAdjustment, NANOS_PER_SECOND);
    return create(sec, nos);
  }

  public static Duration ofMillis(long millis) {
    long sec = Math.floorDiv(millis, MILLIS_PER_SECOND);
    int mos = (int) Math.floorMod(millis, MILLIS_PER_SECOND);
    return create(sec, mos * 1000000);
  }

  public static Duration ofNanos(long nanos) {
    long sec = Math.floorDiv(nanos, NANOS_PER_SECOND);
    int nos = (int) Math.floorMod(nanos, NANOS_PER_SECOND);
    return create(sec, nos);
  }

  public static Duration ofMinutes(long minutes) {
    return create(Math.multiplyExact(minutes, 60L), 0);
  }

  public static Duration ofHours(long hours) {
    return create(Math.multiplyExact(hours, 3600L), 0);
  }

  public static Duration ofDays(long days) {
    return create(Math.multiplyExact(days, 86400L), 0);
  }

  public static Duration of(long amount, TemporalUnit unit) {
    Objects.requireNonNull(unit, "unit");
    if (unit instanceof ChronoUnit) {
      switch ((ChronoUnit) unit) {
        case NANOS:
          return ofNanos(amount);
        case MICROS:
          return ofSeconds(amount / 1000000L, (amount % 1000000L) * 1000L);
        case MILLIS:
          return ofMillis(amount);
        case SECONDS:
          return ofSeconds(amount);
        case MINUTES:
          return ofMinutes(amount);
        case HOURS:
          return ofHours(amount);
        case HALF_DAYS:
          return ofSeconds(Math.multiplyExact(amount, 43200L));
        case DAYS:
          return ofDays(amount);
        default:
          throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
      }
    }
    return ofNanos(Math.multiplyExact(amount, (long) unit.getDuration().toNanos()));
  }

  public static Duration between(Temporal startInclusive, Temporal endExclusive) {
    Objects.requireNonNull(startInclusive, "startInclusive");
    Objects.requireNonNull(endExclusive, "endExclusive");
    try {
      return ofNanos(startInclusive.until(endExclusive, ChronoUnit.NANOS));
    } catch (DateTimeException | ArithmeticException ex) {
      long secs = startInclusive.until(endExclusive, ChronoUnit.SECONDS);
      long nanos;
      try {
        long startNano = startInclusive.getLong(ChronoField.NANO_OF_SECOND);
        long endNano = endExclusive.getLong(ChronoField.NANO_OF_SECOND);
        nanos = endNano - startNano;
        if (secs > 0 && nanos < 0) {
          secs++;
        } else if (secs < 0 && nanos > 0) {
          secs--;
        }
      } catch (DateTimeException ex2) {
        nanos = 0;
      }
      return ofSeconds(secs, nanos);
    }
  }

  private static Duration create(long seconds, int nanoOfSecond) {
    if ((seconds | nanoOfSecond) == 0) {
      return ZERO;
    }
    return new Duration(seconds, nanoOfSecond);
  }

  public long getSeconds() {
    return seconds;
  }

  public int getNano() {
    return nanos;
  }

  public boolean isZero() {
    return (seconds | nanos) == 0;
  }

  public boolean isNegative() {
    return seconds < 0;
  }

  public Duration plus(Duration duration) {
    return plus(duration.getSeconds(), duration.getNano());
  }

  public Duration plus(long amountToAdd, TemporalUnit unit) {
    Objects.requireNonNull(unit, "unit");
    if (unit == ChronoUnit.DAYS) {
      return plus(Math.multiplyExact(amountToAdd, 86400L), 0);
    }
    if (unit.isDurationEstimated()) {
      throw new UnsupportedTemporalTypeException("Unit must not have an estimated duration");
    }
    if (amountToAdd == 0) {
      return this;
    }
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
        default:
          return plusSeconds(Math.multiplyExact(unit.getDuration().seconds, amountToAdd));
      }
    }
    Duration toAdd = unit.getDuration().multipliedBy(amountToAdd);
    return plusSeconds(toAdd.getSeconds()).plusNanos(toAdd.getNano());
  }

  public Duration plusDays(long daysToAdd) {
    return plus(Math.multiplyExact(daysToAdd, 86400L), 0);
  }

  public Duration plusHours(long hoursToAdd) {
    return plus(Math.multiplyExact(hoursToAdd, 3600L), 0);
  }

  public Duration plusMinutes(long minutesToAdd) {
    return plus(Math.multiplyExact(minutesToAdd, 60L), 0);
  }

  public Duration plusSeconds(long secondsToAdd) {
    return plus(secondsToAdd, 0);
  }

  public Duration plusMillis(long millisToAdd) {
    return plus(millisToAdd / MILLIS_PER_SECOND, (millisToAdd % MILLIS_PER_SECOND) * 1000000L);
  }

  public Duration plusNanos(long nanosToAdd) {
    return plus(0, nanosToAdd);
  }

  private Duration plus(long secondsToAdd, long nanosToAdd) {
    if ((secondsToAdd | nanosToAdd) == 0) {
      return this;
    }
    long epochSec = Math.addExact(seconds, secondsToAdd);
    epochSec = Math.addExact(epochSec, nanosToAdd / NANOS_PER_SECOND);
    long nos = nanos + (nanosToAdd % NANOS_PER_SECOND);
    return ofSeconds(epochSec, nos);
  }

  public Duration minus(Duration duration) {
    long sec = duration.getSeconds();
    int nos = duration.getNano();
    if (sec == Long.MIN_VALUE) {
      return plus(Long.MAX_VALUE, -nos).plus(1, 0);
    }
    return plus(-sec, -nos);
  }

  public Duration minus(long amountToSubtract, TemporalUnit unit) {
    return (amountToSubtract == Long.MIN_VALUE)
        ? plus(Long.MAX_VALUE, unit).plus(1, unit)
        : plus(-amountToSubtract, unit);
  }

  public Duration minusDays(long daysToSubtract) {
    return (daysToSubtract == Long.MIN_VALUE)
        ? plusDays(Long.MAX_VALUE).plusDays(1)
        : plusDays(-daysToSubtract);
  }

  public Duration minusHours(long hoursToSubtract) {
    return (hoursToSubtract == Long.MIN_VALUE)
        ? plusHours(Long.MAX_VALUE).plusHours(1)
        : plusHours(-hoursToSubtract);
  }

  public Duration minusMinutes(long minutesToSubtract) {
    return (minutesToSubtract == Long.MIN_VALUE)
        ? plusMinutes(Long.MAX_VALUE).plusMinutes(1)
        : plusMinutes(-minutesToSubtract);
  }

  public Duration minusSeconds(long secondsToSubtract) {
    return (secondsToSubtract == Long.MIN_VALUE)
        ? plusSeconds(Long.MAX_VALUE).plusSeconds(1)
        : plusSeconds(-secondsToSubtract);
  }

  public Duration minusMillis(long millisToSubtract) {
    return (millisToSubtract == Long.MIN_VALUE)
        ? plusMillis(Long.MAX_VALUE).plusMillis(1)
        : plusMillis(-millisToSubtract);
  }

  public Duration minusNanos(long nanosToSubtract) {
    return (nanosToSubtract == Long.MIN_VALUE)
        ? plusNanos(Long.MAX_VALUE).plusNanos(1)
        : plusNanos(-nanosToSubtract);
  }

  public Duration multipliedBy(long multiplicand) {
    if (multiplicand == 0) {
      return ZERO;
    }
    if (multiplicand == 1) {
      return this;
    }
    long sec = Math.multiplyExact(seconds, multiplicand);
    long nos = (long) nanos * multiplicand;
    return ofSeconds(sec, nos);
  }

  public Duration dividedBy(long divisor) {
    if (divisor == 0) {
      throw new ArithmeticException("Cannot divide by zero");
    }
    if (divisor == 1) {
      return this;
    }
    long totalNanos = toNanos();
    return ofNanos(totalNanos / divisor);
  }

  public Duration negated() {
    return multipliedBy(-1);
  }

  public Duration abs() {
    return isNegative() ? negated() : this;
  }

  public long toDays() {
    return seconds / 86400L;
  }

  public long toHours() {
    return seconds / 3600L;
  }

  public long toMinutes() {
    return seconds / 60L;
  }

  public long toSeconds() {
    return seconds;
  }

  public long toMillis() {
    long millis = Math.multiplyExact(seconds, MILLIS_PER_SECOND);
    return Math.addExact(millis, nanos / 1000000);
  }

  public long toNanos() {
    long totalNanos = Math.multiplyExact(seconds, NANOS_PER_SECOND);
    return Math.addExact(totalNanos, nanos);
  }

  public int toDaysPart() {
    return (int) (seconds / 86400L);
  }

  public int toHoursPart() {
    return (int) ((seconds % 86400L) / 3600L);
  }

  public int toMinutesPart() {
    return (int) ((seconds % 3600L) / 60L);
  }

  public int toSecondsPart() {
    return (int) (seconds % 60L);
  }

  public int toMillisPart() {
    return nanos / 1000000;
  }

  public int toNanosPart() {
    return nanos;
  }

  @Override
  public long get(TemporalUnit unit) {
    if (unit == ChronoUnit.SECONDS) {
      return seconds;
    } else if (unit == ChronoUnit.NANOS) {
      return nanos;
    } else {
      throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
    }
  }

  @Override
  public List<TemporalUnit> getUnits() {
    return UNITS;
  }

  @Override
  public Temporal addTo(Temporal temporal) {
    if (seconds != 0) {
      temporal = temporal.plus(seconds, ChronoUnit.SECONDS);
    }
    if (nanos != 0) {
      temporal = temporal.plus(nanos, ChronoUnit.NANOS);
    }
    return temporal;
  }

  @Override
  public Temporal subtractFrom(Temporal temporal) {
    if (seconds != 0) {
      temporal = temporal.minus(seconds, ChronoUnit.SECONDS);
    }
    if (nanos != 0) {
      temporal = temporal.minus(nanos, ChronoUnit.NANOS);
    }
    return temporal;
  }

  @Override
  public int compareTo(Duration otherDuration) {
    int cmp = Long.compare(seconds, otherDuration.seconds);
    if (cmp != 0) {
      return cmp;
    }
    return Integer.compare(nanos, otherDuration.nanos);
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other instanceof Duration) {
      Duration otherDuration = (Duration) other;
      return this.seconds == otherDuration.seconds && this.nanos == otherDuration.nanos;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return ((int) (seconds ^ (seconds >>> 32))) + 51 * nanos;
  }

  @Override
  public String toString() {
    if (this == ZERO) {
      return "PT0S";
    }
    long hours = seconds / 3600L;
    int minutes = (int) ((seconds % 3600L) / 60L);
    int secs = (int) (seconds % 60L);

    StringBuilder sb = new StringBuilder(24);
    sb.append("PT");
    if (hours != 0) {
      sb.append(hours).append('H');
    }
    if (minutes != 0) {
      sb.append(minutes).append('M');
    }
    if (secs == 0 && nanos == 0 && sb.length() > 2) {
      return sb.toString();
    }
    if (secs < 0 && nanos > 0) {
      if (secs == -1) {
        sb.append("-0");
      } else {
        sb.append(secs + 1);
      }
    } else {
      sb.append(secs);
    }
    if (nanos > 0) {
      sb.append('.');
      int dotNanos = (secs < 0) ? (int) (NANOS_PER_SECOND - nanos) : nanos;
      String nStr = Integer.toString(dotNanos);
      StringBuilder nSb = new StringBuilder(9);
      for (int i = nStr.length(); i < 9; ++i) {
        nSb.append('0');
      }
      nSb.append(nStr);
      int end = 9;
      while (end > 0 && nSb.charAt(end - 1) == '0') {
        end--;
      }
      sb.append(nSb.substring(0, end));
    }
    sb.append('S');
    return sb.toString();
  }

  private static void appendZeroPadded(StringBuilder sb, int value, int digits) {
    String s = Integer.toString(value);
    for (int i = s.length(); i < digits; ++i) {
      sb.append('0');
    }
    sb.append(s);
  }
}
