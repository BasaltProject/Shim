/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.time;

import static java.time.LocalTime.NANOS_PER_MINUTE;
import static java.time.LocalTime.NANOS_PER_SECOND;

import java.io.Serializable;
import java.util.Objects;

public abstract class Clock implements InstantSource {
  protected Clock() { }

  public static Clock systemUTC() {
    return SystemClock.UTC;
  }

  public static Clock systemDefaultZone() {
    return new SystemClock(ZoneId.systemDefault());
  }

  public static Clock system(ZoneId zone) {
    Objects.requireNonNull(zone, "zone");
    if (zone == ZoneOffset.UTC) {
      return SystemClock.UTC;
    }
    return new SystemClock(zone);
  }

  public static Clock tickMillis(ZoneId zone) {
    return new TickClock(system(zone), 1000_000);
  }

  public static Clock tickSeconds(ZoneId zone) {
    return new TickClock(system(zone), NANOS_PER_SECOND);
  }

  public static Clock tickMinutes(ZoneId zone) {
    return new TickClock(system(zone), NANOS_PER_MINUTE);
  }

  public static Clock tick(Clock baseClock, Duration tickDuration) {
    Objects.requireNonNull(baseClock, "baseClock");
    Objects.requireNonNull(tickDuration, "tickDuration");
    if (tickDuration.isNegative()) {
      throw new IllegalArgumentException("Tick duration must not be negative");
    }
    long tickNanos = tickDuration.toNanos();
    if (tickNanos % 1000_000 == 0) {
    } else if (1000_000_000 % tickNanos == 0) {
    } else {
      throw new IllegalArgumentException("Invalid tick duration");
    }
    if (tickNanos <= 1) {
      return baseClock;
    }
    return new TickClock(baseClock, tickNanos);
  }

  public static Clock fixed(Instant fixedInstant, ZoneId zone) {
    Objects.requireNonNull(fixedInstant, "fixedInstant");
    Objects.requireNonNull(zone, "zone");
    return new FixedClock(fixedInstant, zone);
  }

  public static Clock offset(Clock baseClock, Duration offsetDuration) {
    Objects.requireNonNull(baseClock, "baseClock");
    Objects.requireNonNull(offsetDuration, "offsetDuration");
    if (offsetDuration.equals(Duration.ZERO)) {
      return baseClock;
    }
    return new OffsetClock(baseClock, offsetDuration);
  }

  static Clock of(final InstantSource source, final ZoneId zone) {
    Objects.requireNonNull(source, "source");
    Objects.requireNonNull(zone, "zone");
    return new SourceClock(source, zone);
  }

  public abstract ZoneId getZone();

  @Override
  public abstract Clock withZone(ZoneId zone);

  @Override
  public long millis() {
    return instant().toEpochMilli();
  }

  @Override
  public abstract Instant instant();

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  static final class SourceClock extends Clock implements Serializable {
    private final InstantSource baseSource;
    private final ZoneId zone;

    SourceClock(InstantSource baseSource, ZoneId zone) {
      this.baseSource = baseSource;
      this.zone = zone;
    }

    @Override
    public ZoneId getZone() {
      return zone;
    }

    @Override
    public Clock withZone(ZoneId zone) {
      if (zone.equals(this.zone)) {
        return this;
      }
      return new SourceClock(baseSource, zone);
    }

    @Override
    public long millis() {
      return baseSource.millis();
    }

    @Override
    public Instant instant() {
      return baseSource.instant();
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof SourceClock) {
        SourceClock other = (SourceClock) obj;
        return zone.equals(other.zone) && baseSource.equals(other.baseSource);
      }
      return false;
    }

    @Override
    public int hashCode() {
      return baseSource.hashCode() ^ zone.hashCode();
    }

    @Override
    public String toString() {
      return "SourceClock[" + baseSource + "," + zone + "]";
    }
  }

  static final class SystemClock extends Clock implements Serializable {
    static final SystemClock UTC = new SystemClock(ZoneOffset.UTC);

    private final ZoneId zone;

    SystemClock(ZoneId zone) {
      this.zone = zone;
    }

    @Override
    public ZoneId getZone() {
      return zone;
    }

    @Override
    public Clock withZone(ZoneId zone) {
      if (zone.equals(this.zone)) {
        return this;
      }
      return new SystemClock(zone);
    }

    @Override
    public long millis() {
      return System.currentTimeMillis();
    }

    @Override
    public Instant instant() {
      return Instant.ofEpochMilli(System.currentTimeMillis());
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof SystemClock) {
        return zone.equals(((SystemClock) obj).zone);
      }
      return false;
    }

    @Override
    public int hashCode() {
      return zone.hashCode() + 1;
    }

    @Override
    public String toString() {
      return "SystemClock[" + zone + "]";
    }
  }

  static final class FixedClock extends Clock implements Serializable {
    private final Instant instant;
    private final ZoneId zone;

    FixedClock(Instant fixedInstant, ZoneId zone) {
      this.instant = fixedInstant;
      this.zone = zone;
    }

    @Override
    public ZoneId getZone() {
      return zone;
    }

    @Override
    public Clock withZone(ZoneId zone) {
      if (zone.equals(this.zone)) {
        return this;
      }
      return new FixedClock(instant, zone);
    }

    @Override
    public long millis() {
      return instant.toEpochMilli();
    }

    @Override
    public Instant instant() {
      return instant;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof FixedClock) {
        FixedClock other = (FixedClock) obj;
        return instant.equals(other.instant) && zone.equals(other.zone);
      }
      return false;
    }

    @Override
    public int hashCode() {
      return instant.hashCode() ^ zone.hashCode();
    }

    @Override
    public String toString() {
      return "FixedClock[" + instant + "," + zone + "]";
    }
  }

  static final class OffsetClock extends Clock implements Serializable {
    private final Clock baseClock;
    private final Duration offset;

    OffsetClock(Clock baseClock, Duration offset) {
      this.baseClock = baseClock;
      this.offset = offset;
    }

    @Override
    public ZoneId getZone() {
      return baseClock.getZone();
    }

    @Override
    public Clock withZone(ZoneId zone) {
      if (zone.equals(baseClock.getZone())) {
        return this;
      }
      return new OffsetClock(baseClock.withZone(zone), offset);
    }

    @Override
    public long millis() {
      return Math.addExact(baseClock.millis(), offset.toMillis());
    }

    @Override
    public Instant instant() {
      return baseClock.instant().plus(offset);
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof OffsetClock) {
        OffsetClock other = (OffsetClock) obj;
        return baseClock.equals(other.baseClock) && offset.equals(other.offset);
      }
      return false;
    }

    @Override
    public int hashCode() {
      return baseClock.hashCode() ^ offset.hashCode();
    }

    @Override
    public String toString() {
      return "OffsetClock[" + baseClock + "," + offset + "]";
    }
  }

  static final class TickClock extends Clock implements Serializable {
    private final Clock baseClock;
    private final long tickNanos;

    TickClock(Clock baseClock, long tickNanos) {
      this.baseClock = baseClock;
      this.tickNanos = tickNanos;
    }

    @Override
    public ZoneId getZone() {
      return baseClock.getZone();
    }

    @Override
    public Clock withZone(ZoneId zone) {
      if (zone.equals(baseClock.getZone())) {
        return this;
      }
      return new TickClock(baseClock.withZone(zone), tickNanos);
    }

    @Override
    public long millis() {
      long millis = baseClock.millis();
      return millis - Math.floorMod(millis, tickNanos / 1000_000L);
    }

    @Override
    public Instant instant() {
      if ((tickNanos % 1000_000) == 0) {
        long millis = baseClock.millis();
        return Instant.ofEpochMilli(millis - Math.floorMod(millis, tickNanos / 1000_000L));
      }
      Instant instant = baseClock.instant();
      long nanos = instant.getNano();
      long adjust = Math.floorMod(nanos, tickNanos);
      return instant.minusNanos(adjust);
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof TickClock) {
        TickClock other = (TickClock) obj;
        return baseClock.equals(other.baseClock) && tickNanos == other.tickNanos;
      }
      return false;
    }

    @Override
    public int hashCode() {
      return baseClock.hashCode() ^ ((int) (tickNanos ^ (tickNanos >>> 32)));
    }

    @Override
    public String toString() {
      return "TickClock[" + baseClock + "," + Duration.ofNanos(tickNanos) + "]";
    }
  }
}
