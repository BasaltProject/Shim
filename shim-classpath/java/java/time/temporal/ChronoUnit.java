/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.time.temporal;

import java.time.Duration;

public enum ChronoUnit implements TemporalUnit {
  NANOS("Nanos", Duration.ofNanos(1)),
  MICROS("Micros", Duration.ofNanos(1000)),
  MILLIS("Millis", Duration.ofMillis(1)),
  SECONDS("Seconds", Duration.ofSeconds(1)),
  MINUTES("Minutes", Duration.ofSeconds(60)),
  HOURS("Hours", Duration.ofSeconds(3600)),
  HALF_DAYS("HalfDays", Duration.ofSeconds(43200)),
  DAYS("Days", Duration.ofSeconds(86400)),
  WEEKS("Weeks", Duration.ofSeconds(7 * 86400)),
  MONTHS("Months", Duration.ofSeconds(31556952L / 12)),
  YEARS("Years", Duration.ofSeconds(31556952L)),
  DECADES("Decades", Duration.ofSeconds(31556952L * 10)),
  CENTURIES("Centuries", Duration.ofSeconds(31556952L * 100)),
  MILLENNIA("Millennia", Duration.ofSeconds(31556952L * 1000)),
  ERAS("Eras", Duration.ofSeconds(31556952L * 1000000000L)),
  FOREVER("Forever", Duration.ofSeconds(Long.MAX_VALUE, 999999999));

  private final String name;
  private final Duration duration;

  private ChronoUnit(String name, Duration duration) {
    this.name = name;
    this.duration = duration;
  }

  @Override
  public Duration getDuration() {
    return duration;
  }

  @Override
  public boolean isDurationEstimated() {
    return compareTo(DAYS) >= 0;
  }

  @Override
  public boolean isDateBased() {
    return compareTo(DAYS) >= 0 && this != FOREVER;
  }

  @Override
  public boolean isTimeBased() {
    return compareTo(DAYS) < 0;
  }

  @Override
  public <R extends Temporal> R addTo(R temporal, long amount) {
    return (R) temporal.plus(amount, this);
  }

  @Override
  public long between(Temporal temporal1Inclusive, Temporal temporal2Exclusive) {
    return temporal1Inclusive.until(temporal2Exclusive, this);
  }

  @Override
  public String toString() {
    return name;
  }
}
