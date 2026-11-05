/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.util.concurrent;

public enum TimeUnit {
  NANOSECONDS(1L),
  MICROSECONDS(1000L),
  MILLISECONDS(1000000L),
  SECONDS(1000000000L),
  MINUTES(60000000000L),
  HOURS(3600000000000L),
  DAYS(86400000000000L);

  private static final long NANOSECONDS_PER_MILLISECOND = 1000000L;
  private static final long NANOSECONDS_PER_SECOND = 1000000000L;

  private final long scale;

  TimeUnit(long scale) {
    this.scale = scale;
  }

  public long convert(java.time.Duration duration) {
    long secs = duration.getSeconds();
    int nano = duration.getNano();
    if (secs < 0 && nano > 0) {
      secs++;
      nano -= (int) NANOSECONDS_PER_SECOND;
    }
    long perUnitNanos = toNanos(1);
    if (perUnitNanos > NANOSECONDS_PER_SECOND) {
      return secs / toSeconds(1);
    }
    long unitsPerSecond = NANOSECONDS_PER_SECOND / perUnitNanos;
    long whole = secs > Long.MAX_VALUE / unitsPerSecond ? Long.MAX_VALUE
        : secs < Long.MIN_VALUE / unitsPerSecond ? Long.MIN_VALUE : secs * unitsPerSecond;
    long frac = nano / perUnitNanos;
    if (frac > 0 && whole > Long.MAX_VALUE - frac) {
      return Long.MAX_VALUE;
    }
    if (frac < 0 && whole < Long.MIN_VALUE - frac) {
      return Long.MIN_VALUE;
    }
    return whole + frac;
  }

  public java.time.temporal.ChronoUnit toChronoUnit() {
    switch (this) {
      case NANOSECONDS:
        return java.time.temporal.ChronoUnit.NANOS;
      case MICROSECONDS:
        return java.time.temporal.ChronoUnit.MICROS;
      case MILLISECONDS:
        return java.time.temporal.ChronoUnit.MILLIS;
      case SECONDS:
        return java.time.temporal.ChronoUnit.SECONDS;
      case MINUTES:
        return java.time.temporal.ChronoUnit.MINUTES;
      case HOURS:
        return java.time.temporal.ChronoUnit.HOURS;
      case DAYS:
        return java.time.temporal.ChronoUnit.DAYS;
      default:
        throw new AssertionError();
    }
  }

  public static TimeUnit of(java.time.temporal.ChronoUnit chronoUnit) {
    java.util.Objects.requireNonNull(chronoUnit, "chronoUnit");
    switch (chronoUnit) {
      case NANOS:
        return TimeUnit.NANOSECONDS;
      case MICROS:
        return TimeUnit.MICROSECONDS;
      case MILLIS:
        return TimeUnit.MILLISECONDS;
      case SECONDS:
        return TimeUnit.SECONDS;
      case MINUTES:
        return TimeUnit.MINUTES;
      case HOURS:
        return TimeUnit.HOURS;
      case DAYS:
        return TimeUnit.DAYS;
      default:
        throw new IllegalArgumentException("No TimeUnit equivalent for " + chronoUnit);
    }
  }
  
  private long cvt(long d, long targetScale) {
    if (scale == targetScale) {
      return d;
    }
    if (scale < targetScale) {
      return d / (targetScale / scale);
    }
    long ratio = scale / targetScale;
    long over = Long.MAX_VALUE / ratio;
    if (d > over) {
      return Long.MAX_VALUE;
    }
    if (d < -over) {
      return Long.MIN_VALUE;
    }
    return d * ratio;
  }

  public long convert(long sourceDuration, TimeUnit sourceUnit) {
    return sourceUnit.cvt(sourceDuration, scale);
  }

  public long toNanos(long duration) {
    return cvt(duration, 1L);
  }

  public long toMicros(long duration) {
    return cvt(duration, 1000L);
  }

  public long toMillis(long duration) {
    return cvt(duration, NANOSECONDS_PER_MILLISECOND);
  }

  public long toSeconds(long duration) {
    return cvt(duration, NANOSECONDS_PER_SECOND);
  }

  public long toMinutes(long duration) {
    return cvt(duration, 60000000000L);
  }

  public long toHours(long duration) {
    return cvt(duration, 3600000000000L);
  }

  public long toDays(long duration) {
    return cvt(duration, 86400000000000L);
  }

  int excessNanos(long d, long m) {
    if (scale >= NANOSECONDS_PER_MILLISECOND) {
      return 0;
    }
    return (int) ((d * scale) - (m * NANOSECONDS_PER_MILLISECOND));
  }

  public void timedWait(Object obj, long timeout) throws InterruptedException {
    if (timeout > 0) {
      long ms = toMillis(timeout);
      int ns = excessNanos(timeout, ms);
      obj.wait(ms, ns);
    }
  }
  
  public void timedJoin(Thread thread, long timeout) throws InterruptedException {
    if (timeout > 0) {
      long ms = toMillis(timeout);
      int ns = excessNanos(timeout, ms);
      thread.join(ms, ns);
    }
  }
  
  public void sleep(long timeout) throws InterruptedException {
    if (timeout > 0) {
      long ms = toMillis(timeout);
      int ns = excessNanos(timeout, ms);
      Thread.sleep(ms, ns);
    }
  }
}
