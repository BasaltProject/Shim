/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.time.temporal;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.chrono.Chronology;

public final class TemporalQueries {
  private TemporalQueries() { }

  private static final class Named<R> implements TemporalQuery<R> {
    private final String name;
    private final TemporalQuery<R> query;

    Named(String name, TemporalQuery<R> query) {
      this.name = name;
      this.query = query;
    }

    @Override
    public R queryFrom(TemporalAccessor temporal) {
      return query.queryFrom(temporal);
    }

    @Override
    public String toString() {
      return name;
    }
  }

  static final TemporalQuery<ZoneId> ZONE_ID = new Named<ZoneId>("ZoneId", new TemporalQuery<ZoneId>() {
    @Override
    public ZoneId queryFrom(TemporalAccessor temporal) {
      return temporal.query(TemporalQueries.ZONE_ID);
    }
  });

  static final TemporalQuery<Chronology> CHRONO = new Named<Chronology>("Chronology", new TemporalQuery<Chronology>() {
    @Override
    public Chronology queryFrom(TemporalAccessor temporal) {
      return temporal.query(TemporalQueries.CHRONO);
    }
  });

  static final TemporalQuery<TemporalUnit> PRECISION =
      new Named<TemporalUnit>("Precision", new TemporalQuery<TemporalUnit>() {
        @Override
        public TemporalUnit queryFrom(TemporalAccessor temporal) {
          return temporal.query(TemporalQueries.PRECISION);
        }
      });

  static final TemporalQuery<ZoneId> ZONE = new Named<ZoneId>("Zone", new TemporalQuery<ZoneId>() {
    @Override
    public ZoneId queryFrom(TemporalAccessor temporal) {
      ZoneId zone = temporal.query(ZONE_ID);
      return (zone != null ? zone : temporal.query(OFFSET));
    }
  });

  static final TemporalQuery<ZoneOffset> OFFSET = new Named<ZoneOffset>("ZoneOffset", new TemporalQuery<ZoneOffset>() {
    @Override
    public ZoneOffset queryFrom(TemporalAccessor temporal) {
      if (temporal.isSupported(ChronoField.OFFSET_SECONDS)) {
        return ZoneOffset.ofTotalSeconds(temporal.get(ChronoField.OFFSET_SECONDS));
      }
      return null;
    }
  });

  static final TemporalQuery<LocalDate> LOCAL_DATE = new Named<LocalDate>("LocalDate", new TemporalQuery<LocalDate>() {
    @Override
    public LocalDate queryFrom(TemporalAccessor temporal) {
      if (temporal.isSupported(ChronoField.EPOCH_DAY)) {
        return LocalDate.ofEpochDay(temporal.getLong(ChronoField.EPOCH_DAY));
      }
      return null;
    }
  });

  static final TemporalQuery<LocalTime> LOCAL_TIME = new Named<LocalTime>("LocalTime", new TemporalQuery<LocalTime>() {
    @Override
    public LocalTime queryFrom(TemporalAccessor temporal) {
      if (temporal.isSupported(ChronoField.NANO_OF_DAY)) {
        return LocalTime.ofNanoOfDay(temporal.getLong(ChronoField.NANO_OF_DAY));
      }
      return null;
    }
  });

  public static TemporalQuery<ZoneId> zoneId() {
    return ZONE_ID;
  }

  public static TemporalQuery<Chronology> chronology() {
    return CHRONO;
  }

  public static TemporalQuery<TemporalUnit> precision() {
    return PRECISION;
  }

  public static TemporalQuery<ZoneId> zone() {
    return ZONE;
  }

  public static TemporalQuery<ZoneOffset> offset() {
    return OFFSET;
  }

  public static TemporalQuery<LocalDate> localDate() {
    return LOCAL_DATE;
  }

  public static TemporalQuery<LocalTime> localTime() {
    return LOCAL_TIME;
  }
}
