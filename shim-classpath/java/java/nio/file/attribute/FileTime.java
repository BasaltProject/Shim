/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.file.attribute;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public final class FileTime implements Comparable<FileTime> {
  private static final long kMillisPerSecond = 1000L;
  private static final long kMicrosPerSecond = 1000_000L;
  private static final long kNanosPerSecond = 1000_000_000L;
  private static final int kNanosPerMilli = 1000_000;
  private static final int kNanosPerMicro = 1000;
  private static final long kSecondsPerMinute = 60L;
  private static final long kSecondsPerHour = 3600L;
  private static final long kSecondsPerDay = 86400L;
  private static final long kMinSecond = -31557014167219200L;
  private static final long kMaxSecond = 31556889864403199L;

  private final TimeUnit unit;
  private final long value;
  private Instant instant;
  private String valueAsString;
  private int hash;

  private FileTime(long value, TimeUnit unit, Instant instant) {
    this.value = value;
    this.unit = unit;
    this.instant = instant;
  }

  public static FileTime from(long value, TimeUnit unit) {
    Objects.requireNonNull(unit, "unit");
    return new FileTime(value, unit, null);
  }

  public static FileTime fromMillis(long value) {
    return new FileTime(value, TimeUnit.MILLISECONDS, null);
  }

  public static FileTime from(Instant instant) {
    Objects.requireNonNull(instant, "instant");
    return new FileTime(0, null, instant);
  }

  public long to(TimeUnit unit) {
    Objects.requireNonNull(unit, "unit");
    if (this.unit != null) {
      return unit.convert(this.value, this.unit);
    } else {
      long secs = unit.convert(instant.getEpochSecond(), TimeUnit.SECONDS);
      if (secs == Long.MIN_VALUE || secs == Long.MAX_VALUE) {
        return secs;
      }
      long nanos = unit.convert(instant.getNano(), TimeUnit.NANOSECONDS);
      long r = secs + nanos;
      if (((secs ^ r) & (nanos ^ r)) < 0) {
        return (secs < 0) ? Long.MIN_VALUE : Long.MAX_VALUE;
      } else {
        return r;
      }
    }
  }

  public long toMillis() {
    if (unit != null) {
      return unit.toMillis(value);
    } else {
      long secs = instant.getEpochSecond();
      int nanos = instant.getNano();
      long r = secs * kMillisPerSecond;
      long ax = Math.abs(secs);
      if (((ax | kMillisPerSecond) >>> 31) != 0) {
        if ((r / kMillisPerSecond) != secs) {
          return (secs < 0) ? Long.MIN_VALUE : Long.MAX_VALUE;
        }
      }
      return r + nanos / kNanosPerMilli;
    }
  }

  private static long scale(long d, long m, long over) {
    if (d > over) {
      return Long.MAX_VALUE;
    }
    if (d < -over) {
      return Long.MIN_VALUE;
    }
    return d * m;
  }

  public Instant toInstant() {
    if (instant == null) {
      long secs = 0L;
      int nanos = 0;
      switch (unit) {
        case DAYS:
          secs = scale(value, kSecondsPerDay, Long.MAX_VALUE / kSecondsPerDay);
          break;
        case HOURS:
          secs = scale(value, kSecondsPerHour, Long.MAX_VALUE / kSecondsPerHour);
          break;
        case MINUTES:
          secs = scale(value, kSecondsPerMinute, Long.MAX_VALUE / kSecondsPerMinute);
          break;
        case SECONDS:
          secs = value;
          break;
        case MILLISECONDS:
          secs = Math.floorDiv(value, kMillisPerSecond);
          nanos = (int) Math.floorMod(value, kMillisPerSecond) * kNanosPerMilli;
          break;
        case MICROSECONDS:
          secs = Math.floorDiv(value, kMicrosPerSecond);
          nanos = (int) Math.floorMod(value, kMicrosPerSecond) * kNanosPerMicro;
          break;
        case NANOSECONDS:
          secs = Math.floorDiv(value, kNanosPerSecond);
          nanos = (int) Math.floorMod(value, kNanosPerSecond);
          break;
        default:
          throw new AssertionError("Unit not handled");
      }
      if (secs <= kMinSecond) {
        instant = Instant.MIN;
      } else if (secs >= kMaxSecond) {
        instant = Instant.MAX;
      } else {
        instant = Instant.ofEpochSecond(secs, nanos);
      }
    }
    return instant;
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof FileTime) ? compareTo((FileTime) obj) == 0 : false;
  }

  @Override
  public int hashCode() {
    if (hash == 0) {
      hash = toInstant().hashCode();
    }
    return hash;
  }

  private long toDays() {
    if (unit != null) {
      return unit.toDays(value);
    } else {
      return TimeUnit.SECONDS.toDays(toInstant().getEpochSecond());
    }
  }

  private long toExcessNanos(long days) {
    if (unit != null) {
      return unit.toNanos(value - unit.convert(days, TimeUnit.DAYS));
    } else {
      return TimeUnit.SECONDS.toNanos(toInstant().getEpochSecond() - TimeUnit.DAYS.toSeconds(days));
    }
  }

  @Override
  public int compareTo(FileTime other) {
    if (unit != null && unit == other.unit) {
      return Long.compare(value, other.value);
    } else {
      long secs = toInstant().getEpochSecond();
      long secsOther = other.toInstant().getEpochSecond();
      int cmp = Long.compare(secs, secsOther);
      if (cmp != 0) {
        return cmp;
      }
      cmp = Long.compare(toInstant().getNano(), other.toInstant().getNano());
      if (cmp != 0) {
        return cmp;
      }
      if (secs != kMaxSecond && secs != kMinSecond) {
        return 0;
      }
      long days = toDays();
      long daysOther = other.toDays();
      if (days == daysOther) {
        return Long.compare(toExcessNanos(days), other.toExcessNanos(daysOther));
      }
      return Long.compare(days, daysOther);
    }
  }

  private StringBuilder append(StringBuilder sb, int n, int digits) {
    int v = n;
    char[] buf = new char[digits];
    for (int i = digits - 1; i >= 0; i--) {
      buf[i] = (char) ('0' + (v % 10));
      v /= 10;
    }
    sb.append(buf);
    return sb;
  }

  @Override
  public String toString() {
    if (valueAsString == null) {
      long secs = 0L;
      int nanos = 0;
      if (instant == null && unit.compareTo(TimeUnit.SECONDS) >= 0) {
        secs = unit.toSeconds(value);
      } else {
        secs = toInstant().getEpochSecond();
        nanos = toInstant().getNano();
      }
      long days = Math.floorDiv(secs, kSecondsPerDay);
      int secOfDay = (int) Math.floorMod(secs, kSecondsPerDay);
      long z = days + 719468;
      long era = Math.floorDiv(z, 146097L);
      long doe = z - era * 146097L;
      long yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365;
      long year = yoe + era * 400;
      long doy = doe - (365 * yoe + yoe / 4 - yoe / 100);
      long mp = (5 * doy + 2) / 153;
      int dom = (int) (doy - (153 * mp + 2) / 5 + 1);
      int month = (int) (mp < 10 ? mp + 3 : mp - 9);
      if (month <= 2) {
        year++;
      }
      int hour = secOfDay / 3600;
      int minute = (secOfDay % 3600) / 60;
      int second = secOfDay % 60;
      StringBuilder sb = new StringBuilder(64);
      if (year > 9999) {
        sb.append('+');
        sb.append(year);
      } else if (year >= 0) {
        append(sb, (int) year, 4);
      } else if (year > -10000) {
        sb.append('-');
        append(sb, (int) -year, 4);
      } else {
        sb.append(year);
      }
      sb.append('-');
      append(sb, month, 2);
      sb.append('-');
      append(sb, dom, 2);
      sb.append('T');
      append(sb, hour, 2);
      sb.append(':');
      append(sb, minute, 2);
      sb.append(':');
      append(sb, second, 2);
      if (nanos > 0) {
        sb.append('.');
        append(sb, nanos, 9);
        int i = sb.length() - 1;
        while (sb.charAt(i) == '0') {
          i--;
        }
        sb.setLength(i + 1);
      }
      sb.append('Z');
      valueAsString = sb.toString();
    }
    return valueAsString;
  }
}
