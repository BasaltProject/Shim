/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.time;

import static java.time.LocalTime.MINUTES_PER_HOUR;
import static java.time.LocalTime.NANOS_PER_SECOND;
import static java.time.LocalTime.SECONDS_PER_DAY;
import static java.time.LocalTime.SECONDS_PER_HOUR;
import static java.time.LocalTime.SECONDS_PER_MINUTE;
import static java.time.temporal.ChronoField.NANO_OF_SECOND;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.NANOS;
import static java.time.temporal.ChronoUnit.SECONDS;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Duration implements TemporalAmount, Comparable<Duration>, Serializable {
  private static final long serialVersionUID = 3078945930695997490L;

  public static final Duration ZERO = new Duration(0, 0);

  private static final BigInteger BI_NANOS_PER_SECOND = BigInteger.valueOf(NANOS_PER_SECOND);

  private static final Pattern PATTERN = Pattern.compile(
      "([-+]?)P(?:([-+]?[0-9]+)D)?" + "(T(?:([-+]?[0-9]+)H)?(?:([-+]?[0-9]+)M)?(?:([-+]?[0-9]+)(?:[.,]([0-9]{0,9}))?S)?)?");

  private static class DurationUnits {
    static final List<TemporalUnit> UNITS =
        Collections.unmodifiableList(Arrays.<TemporalUnit>asList(SECONDS, NANOS));
  }

  private final long seconds;
  private final int nanos;

  public static Duration ofDays(long days) {
    return create(Math.multiplyExact(days, SECONDS_PER_DAY), 0);
  }

  public static Duration ofHours(long hours) {
    return create(Math.multiplyExact(hours, SECONDS_PER_HOUR), 0);
  }

  public static Duration ofMinutes(long minutes) {
    return create(Math.multiplyExact(minutes, SECONDS_PER_MINUTE), 0);
  }

  public static Duration ofSeconds(long seconds) {
    return create(seconds, 0);
  }

  public static Duration ofSeconds(long seconds, long nanoAdjustment) {
    long secs = Math.addExact(seconds, Math.floorDiv(nanoAdjustment, NANOS_PER_SECOND));
    int nos = (int) Math.floorMod(nanoAdjustment, NANOS_PER_SECOND);
    return create(secs, nos);
  }

  public static Duration ofMillis(long millis) {
    long secs = millis / 1000;
    int mos = (int) (millis % 1000);
    if (mos < 0) {
      mos += 1000;
      secs--;
    }
    return create(secs, mos * 1000_000);
  }

  public static Duration ofNanos(long nanos) {
    long secs = nanos / NANOS_PER_SECOND;
    int nos = (int) (nanos % NANOS_PER_SECOND);
    if (nos < 0) {
      nos += NANOS_PER_SECOND;
      secs--;
    }
    return create(secs, nos);
  }

  public static Duration of(long amount, TemporalUnit unit) {
    return ZERO.plus(amount, unit);
  }

  public static Duration from(TemporalAmount amount) {
    Objects.requireNonNull(amount, "amount");
    Duration duration = ZERO;
    for (TemporalUnit unit : amount.getUnits()) {
      duration = duration.plus(amount.get(unit), unit);
    }
    return duration;
  }

  public static Duration parse(CharSequence text) {
    Objects.requireNonNull(text, "text");
    Matcher matcher = PATTERN.matcher(text.toString().toUpperCase());
    if (matcher.matches()) {
      if (!"T".equalsIgnoreCase(matcher.group(3))) {
        boolean negate = "-".equals(matcher.group(1));
        String dayMatch = matcher.group(2);
        String hourMatch = matcher.group(4);
        String minuteMatch = matcher.group(5);
        String secondMatch = matcher.group(6);
        String fractionMatch = matcher.group(7);
        if (dayMatch != null || hourMatch != null || minuteMatch != null || secondMatch != null) {
          long daysAsSecs = parseNumber(text, dayMatch, SECONDS_PER_DAY, "days");
          long hoursAsSecs = parseNumber(text, hourMatch, SECONDS_PER_HOUR, "hours");
          long minsAsSecs = parseNumber(text, minuteMatch, SECONDS_PER_MINUTE, "minutes");
          long seconds = parseNumber(text, secondMatch, 1, "seconds");
          boolean negativeSecs = secondMatch != null && secondMatch.charAt(0) == '-';
          int nanos = parseFraction(text, fractionMatch, negativeSecs ? -1 : 1);
          try {
            return create(negate, daysAsSecs, hoursAsSecs, minsAsSecs, seconds, nanos);
          } catch (ArithmeticException ex) {
            throw (DateTimeParseException) new DateTimeParseException("Text cannot be parsed to a Duration: overflow",
                text, 0).initCause(ex);
          }
        }
      }
    }
    throw new DateTimeParseException("Text cannot be parsed to a Duration", text, 0);
  }

  private static long parseNumber(CharSequence text, String parsed, int multiplier, String errorText) {
    if (parsed == null) {
      return 0;
    }
    try {
      long val = Long.parseLong(parsed);
      return Math.multiplyExact(val, multiplier);
    } catch (NumberFormatException | ArithmeticException ex) {
      throw (DateTimeParseException) new DateTimeParseException("Text cannot be parsed to a Duration: " + errorText,
          text, 0).initCause(ex);
    }
  }

  private static int parseFraction(CharSequence text, String parsed, int negate) {
    if (parsed == null || parsed.length() == 0) {
      return 0;
    }
    try {
      parsed = (parsed + "000000000").substring(0, 9);
      return Integer.parseInt(parsed) * negate;
    } catch (NumberFormatException | ArithmeticException ex) {
      throw (DateTimeParseException) new DateTimeParseException("Text cannot be parsed to a Duration: fraction",
          text, 0).initCause(ex);
    }
  }

  private static Duration create(boolean negate, long daysAsSecs, long hoursAsSecs, long minsAsSecs, long secs,
      int nanos) {
    long seconds = Math.addExact(daysAsSecs, Math.addExact(hoursAsSecs, Math.addExact(minsAsSecs, secs)));
    if (negate) {
      return ofSeconds(seconds, nanos).negated();
    }
    return ofSeconds(seconds, nanos);
  }

  public static Duration between(Temporal startInclusive, Temporal endExclusive) {
    try {
      return ofNanos(startInclusive.until(endExclusive, NANOS));
    } catch (DateTimeException | ArithmeticException ex) {
      long secs = startInclusive.until(endExclusive, SECONDS);
      long nanos;
      try {
        nanos = endExclusive.getLong(NANO_OF_SECOND) - startInclusive.getLong(NANO_OF_SECOND);
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

  private static Duration create(long seconds, int nanoAdjustment) {
    if ((seconds | nanoAdjustment) == 0) {
      return ZERO;
    }
    return new Duration(seconds, nanoAdjustment);
  }

  private Duration(long seconds, int nanos) {
    super();
    this.seconds = seconds;
    this.nanos = nanos;
  }

  @Override
  public long get(TemporalUnit unit) {
    if (unit == SECONDS) {
      return seconds;
    } else if (unit == NANOS) {
      return nanos;
    } else {
      throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
    }
  }

  @Override
  public List<TemporalUnit> getUnits() {
    return DurationUnits.UNITS;
  }

  public boolean isZero() {
    return (seconds | nanos) == 0L;
  }

  public boolean isNegative() {
    return seconds < 0;
  }

  public boolean isPositive() {
    return (seconds | nanos) != 0 && seconds >= 0;
  }

  public long getSeconds() {
    return seconds;
  }

  public int getNano() {
    return nanos;
  }

  public Duration withSeconds(long seconds) {
    return create(seconds, nanos);
  }

  public Duration withNanos(int nanoOfSecond) {
    NANO_OF_SECOND.checkValidIntValue(nanoOfSecond);
    return create(seconds, nanoOfSecond);
  }

  public Duration plus(Duration duration) {
    return plus(duration.getSeconds(), duration.getNano());
  }

  public Duration plus(long amountToAdd, TemporalUnit unit) {
    Objects.requireNonNull(unit, "unit");
    if (unit == DAYS) {
      return plus(Math.multiplyExact(amountToAdd, SECONDS_PER_DAY), 0);
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
          return plusSeconds((amountToAdd / (1000_000L * 1000)) * 1000).plusNanos((amountToAdd % (1000_000L * 1000)) * 1000);
        case MILLIS:
          return plusMillis(amountToAdd);
        case SECONDS:
          return plusSeconds(amountToAdd);
        default:
          return plusSeconds(Math.multiplyExact(unit.getDuration().seconds, amountToAdd));
      }
    }
    Duration duration = unit.getDuration().multipliedBy(amountToAdd);
    return plusSeconds(duration.getSeconds()).plusNanos(duration.getNano());
  }

  public Duration plusDays(long daysToAdd) {
    return plus(Math.multiplyExact(daysToAdd, SECONDS_PER_DAY), 0);
  }

  public Duration plusHours(long hoursToAdd) {
    return plus(Math.multiplyExact(hoursToAdd, SECONDS_PER_HOUR), 0);
  }

  public Duration plusMinutes(long minutesToAdd) {
    return plus(Math.multiplyExact(minutesToAdd, SECONDS_PER_MINUTE), 0);
  }

  public Duration plusSeconds(long secondsToAdd) {
    return plus(secondsToAdd, 0);
  }

  public Duration plusMillis(long millisToAdd) {
    return plus(millisToAdd / 1000, (millisToAdd % 1000) * 1000_000);
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
    nanosToAdd = nanosToAdd % NANOS_PER_SECOND;
    long nanoAdjustment = nanos + nanosToAdd;
    return ofSeconds(epochSec, nanoAdjustment);
  }

  public Duration minus(Duration duration) {
    long secsToSubtract = duration.getSeconds();
    int nanosToSubtract = duration.getNano();
    if (secsToSubtract == Long.MIN_VALUE) {
      return plus(Long.MAX_VALUE, -nanosToSubtract).plus(1, 0);
    }
    return plus(-secsToSubtract, -nanosToSubtract);
  }

  public Duration minus(long amountToSubtract, TemporalUnit unit) {
    return (amountToSubtract == Long.MIN_VALUE ? plus(Long.MAX_VALUE, unit).plus(1, unit)
        : plus(-amountToSubtract, unit));
  }

  public Duration minusDays(long daysToSubtract) {
    return (daysToSubtract == Long.MIN_VALUE ? plusDays(Long.MAX_VALUE).plusDays(1) : plusDays(-daysToSubtract));
  }

  public Duration minusHours(long hoursToSubtract) {
    return (hoursToSubtract == Long.MIN_VALUE ? plusHours(Long.MAX_VALUE).plusHours(1)
        : plusHours(-hoursToSubtract));
  }

  public Duration minusMinutes(long minutesToSubtract) {
    return (minutesToSubtract == Long.MIN_VALUE ? plusMinutes(Long.MAX_VALUE).plusMinutes(1)
        : plusMinutes(-minutesToSubtract));
  }

  public Duration minusSeconds(long secondsToSubtract) {
    return (secondsToSubtract == Long.MIN_VALUE ? plusSeconds(Long.MAX_VALUE).plusSeconds(1)
        : plusSeconds(-secondsToSubtract));
  }

  public Duration minusMillis(long millisToSubtract) {
    return (millisToSubtract == Long.MIN_VALUE ? plusMillis(Long.MAX_VALUE).plusMillis(1)
        : plusMillis(-millisToSubtract));
  }

  public Duration minusNanos(long nanosToSubtract) {
    return (nanosToSubtract == Long.MIN_VALUE ? plusNanos(Long.MAX_VALUE).plusNanos(1)
        : plusNanos(-nanosToSubtract));
  }

  public Duration multipliedBy(long multiplicand) {
    if (multiplicand == 0) {
      return ZERO;
    }
    if (multiplicand == 1) {
      return this;
    }
    return create(toBigDecimalSeconds().multiply(BigDecimal.valueOf(multiplicand)));
  }

  public Duration dividedBy(long divisor) {
    if (divisor == 0) {
      throw new ArithmeticException("Cannot divide by zero");
    }
    if (divisor == 1) {
      return this;
    }
    return create(toBigDecimalSeconds().divide(BigDecimal.valueOf(divisor), RoundingMode.DOWN));
  }

  public long dividedBy(Duration divisor) {
    Objects.requireNonNull(divisor, "divisor");
    BigDecimal dividendBigD = toBigDecimalSeconds();
    BigDecimal divisorBigD = divisor.toBigDecimalSeconds();
    return dividendBigD.divideToIntegralValue(divisorBigD).longValueExact();
  }

  private BigDecimal toBigDecimalSeconds() {
    return BigDecimal.valueOf(seconds).add(BigDecimal.valueOf(nanos, 9));
  }

  private static Duration create(BigDecimal secondsBigDecimal) {
    BigInteger nanosBigInteger = secondsBigDecimal.movePointRight(9).toBigIntegerExact();
    BigInteger[] divRem = nanosBigInteger.divideAndRemainder(BI_NANOS_PER_SECOND);
    if (divRem[0].bitLength() > 63) {
      throw new ArithmeticException("Exceeds capacity of Duration: " + nanosBigInteger);
    }
    return ofSeconds(divRem[0].longValue(), divRem[1].intValue());
  }

  public Duration negated() {
    return multipliedBy(-1);
  }

  public Duration abs() {
    return isNegative() ? negated() : this;
  }

  @Override
  public Temporal addTo(Temporal temporal) {
    if (seconds != 0) {
      temporal = temporal.plus(seconds, SECONDS);
    }
    if (nanos != 0) {
      temporal = temporal.plus(nanos, NANOS);
    }
    return temporal;
  }

  @Override
  public Temporal subtractFrom(Temporal temporal) {
    if (seconds != 0) {
      temporal = temporal.minus(seconds, SECONDS);
    }
    if (nanos != 0) {
      temporal = temporal.minus(nanos, NANOS);
    }
    return temporal;
  }

  public long toDays() {
    return seconds / SECONDS_PER_DAY;
  }

  public long toHours() {
    return seconds / SECONDS_PER_HOUR;
  }

  public long toMinutes() {
    return seconds / SECONDS_PER_MINUTE;
  }

  public long toSeconds() {
    return seconds;
  }

  public long toMillis() {
    long tempSeconds = seconds;
    long tempNanos = nanos;
    if (tempSeconds < 0) {
      tempSeconds = tempSeconds + 1;
      tempNanos = tempNanos - NANOS_PER_SECOND;
    }
    long millis = Math.multiplyExact(tempSeconds, 1000);
    millis = Math.addExact(millis, tempNanos / 1000_000);
    return millis;
  }

  public long toNanos() {
    long tempSeconds = seconds;
    long tempNanos = nanos;
    if (tempSeconds < 0) {
      tempSeconds = tempSeconds + 1;
      tempNanos = tempNanos - NANOS_PER_SECOND;
    }
    long totalNanos = Math.multiplyExact(tempSeconds, NANOS_PER_SECOND);
    totalNanos = Math.addExact(totalNanos, tempNanos);
    return totalNanos;
  }

  public long toDaysPart() {
    return seconds / SECONDS_PER_DAY;
  }

  public int toHoursPart() {
    return (int) (toHours() % 24);
  }

  public int toMinutesPart() {
    return (int) (toMinutes() % MINUTES_PER_HOUR);
  }

  public int toSecondsPart() {
    return (int) (seconds % SECONDS_PER_MINUTE);
  }

  public int toMillisPart() {
    return nanos / 1000_000;
  }

  public int toNanosPart() {
    return nanos;
  }

  public Duration truncatedTo(TemporalUnit unit) {
    Objects.requireNonNull(unit, "unit");
    if (unit == ChronoUnit.SECONDS && (seconds >= 0 || nanos == 0)) {
      return new Duration(seconds, 0);
    } else if (unit == ChronoUnit.NANOS) {
      return this;
    }
    Duration unitDur = unit.getDuration();
    if (unitDur.getSeconds() > LocalTime.SECONDS_PER_DAY) {
      throw new UnsupportedTemporalTypeException("Unit is too large to be used for truncation");
    }
    long dur = unitDur.toNanos();
    if ((LocalTime.NANOS_PER_DAY % dur) != 0) {
      throw new UnsupportedTemporalTypeException("Unit must divide into a standard day without remainder");
    }
    long nod = (seconds % LocalTime.SECONDS_PER_DAY) * LocalTime.NANOS_PER_SECOND + nanos;
    long result = (nod / dur) * dur;
    return plusNanos(result - nod);
  }

  @Override
  public int compareTo(Duration otherDuration) {
    int cmp = Long.compare(seconds, otherDuration.seconds);
    if (cmp != 0) {
      return cmp;
    }
    return nanos - otherDuration.nanos;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    return (other instanceof Duration) && this.seconds == ((Duration) other).seconds
        && this.nanos == ((Duration) other).nanos;
  }

  @Override
  public int hashCode() {
    return ((int) (seconds ^ (seconds >>> 32))) + (51 * nanos);
  }

  @Override
  public String toString() {
    if (this == ZERO) {
      return "PT0S";
    }
    long effectiveTotalSecs = seconds;
    if (seconds < 0 && nanos > 0) {
      effectiveTotalSecs++;
    }
    long hours = effectiveTotalSecs / SECONDS_PER_HOUR;
    int minutes = (int) ((effectiveTotalSecs % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE);
    int secs = (int) (effectiveTotalSecs % SECONDS_PER_MINUTE);
    StringBuilder buf = new StringBuilder(24);
    buf.append("PT");
    if (hours != 0) {
      buf.append(hours).append('H');
    }
    if (minutes != 0) {
      buf.append(minutes).append('M');
    }
    if (secs == 0 && nanos == 0 && buf.length() > 2) {
      return buf.toString();
    }
    if (seconds < 0 && nanos > 0) {
      if (secs == 0) {
        buf.append("-0");
      } else {
        buf.append(secs);
      }
    } else {
      buf.append(secs);
    }
    if (nanos > 0) {
      int pos = buf.length();
      if (seconds < 0) {
        buf.append(2 * NANOS_PER_SECOND - nanos);
      } else {
        buf.append(nanos + NANOS_PER_SECOND);
      }
      while (buf.charAt(buf.length() - 1) == '0') {
        buf.setLength(buf.length() - 1);
      }
      buf.setCharAt(pos, '.');
    }
    buf.append('S');
    return buf.toString();
  }
}
