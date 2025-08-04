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
  private final Instant instant;
  private final long millis;

  private FileTime(Instant instant, long millis) {
    this.instant = instant;
    this.millis = millis;
  }

  public static FileTime from(Instant instant) {
    Objects.requireNonNull(instant, "instant");
    return new FileTime(instant, instant.toEpochMilli());
  }

  public static FileTime fromMillis(long value) {
    return new FileTime(Instant.ofEpochMilli(value), value);
  }

  public static FileTime from(long value, TimeUnit unit) {
    Objects.requireNonNull(unit, "unit");
    long millis = unit.toMillis(value);
    return fromMillis(millis);
  }

  public Instant toInstant() {
    return instant;
  }

  public long toMillis() {
    return millis;
  }

  public long to(TimeUnit unit) {
    Objects.requireNonNull(unit, "unit");
    return unit.convert(millis, TimeUnit.MILLISECONDS);
  }

  @Override
  public int compareTo(FileTime other) {
    return instant.compareTo(other.instant);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj instanceof FileTime) {
      return instant.equals(((FileTime) obj).instant);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return instant.hashCode();
  }

  @Override
  public String toString() {
    return instant.toString();
  }
}
