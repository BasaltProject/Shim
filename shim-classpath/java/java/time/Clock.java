/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.time;

import java.util.Objects;

public abstract class Clock {
  protected Clock() {}

  public static Clock systemUTC() {
    return new SystemClock(ZoneOffset.UTC);
  }

  public static Clock systemDefaultZone() {
    return new SystemClock(ZoneId.systemDefault());
  }

  public static Clock system(ZoneId zone) {
    Objects.requireNonNull(zone, "zone");
    return new SystemClock(zone);
  }

  public static Clock fixed(Instant fixedInstant, ZoneId zone) {
    Objects.requireNonNull(fixedInstant, "fixedInstant");
    Objects.requireNonNull(zone, "zone");
    return new FixedClock(fixedInstant, zone);
  }

  public abstract ZoneId getZone();

  public abstract Clock withZone(ZoneId zone);

  public abstract long millis();

  public abstract Instant instant();

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  static final class SystemClock extends Clock {
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
      if (zone.equals(this.zone)) return this;
      return new SystemClock(zone);
    }

    @Override
    public long millis() {
      return System.currentTimeMillis();
    }

    @Override
    public Instant instant() {
      return Instant.ofEpochMilli(millis());
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

  static final class FixedClock extends Clock {
    private final Instant instant;
    private final ZoneId zone;

    FixedClock(Instant instant, ZoneId zone) {
      this.instant = instant;
      this.zone = zone;
    }

    @Override
    public ZoneId getZone() {
      return zone;
    }

    @Override
    public Clock withZone(ZoneId zone) {
      if (zone.equals(this.zone)) return this;
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
}
