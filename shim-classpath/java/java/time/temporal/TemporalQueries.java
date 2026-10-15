/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.time.temporal;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

public final class TemporalQueries {
  private TemporalQueries() {}

  private static final TemporalQuery<ZoneId> ZONE_ID = new TemporalQuery<ZoneId>() {
    @Override
    public ZoneId queryFrom(TemporalAccessor temporal) {
      return temporal.query(TemporalQueries.ZONE_ID);
    }
  };

  private static final TemporalQuery<ZoneOffset> OFFSET = new TemporalQuery<ZoneOffset>() {
    @Override
    public ZoneOffset queryFrom(TemporalAccessor temporal) {
      if (temporal.isSupported(ChronoField.OFFSET_SECONDS)) {
        return ZoneOffset.ofTotalSeconds(temporal.get(ChronoField.OFFSET_SECONDS));
      }
      return null;
    }
  };

  private static final TemporalQuery<LocalDate> LOCAL_DATE = new TemporalQuery<LocalDate>() {
    @Override
    public LocalDate queryFrom(TemporalAccessor temporal) {
      if (temporal.isSupported(ChronoField.EPOCH_DAY)) {
        return LocalDate.ofEpochDay(temporal.getLong(ChronoField.EPOCH_DAY));
      }
      return null;
    }
  };

  private static final TemporalQuery<LocalTime> LOCAL_TIME = new TemporalQuery<LocalTime>() {
    @Override
    public LocalTime queryFrom(TemporalAccessor temporal) {
      if (temporal.isSupported(ChronoField.NANO_OF_DAY)) {
        return LocalTime.ofNanoOfDay(temporal.getLong(ChronoField.NANO_OF_DAY));
      }
      return null;
    }
  };

  private static final TemporalQuery<TemporalUnit> PRECISION = new TemporalQuery<TemporalUnit>() {
    @Override
    public TemporalUnit queryFrom(TemporalAccessor temporal) {
      return temporal.query(TemporalQueries.PRECISION);
    }
  };

  public static TemporalQuery<ZoneId> zoneId() {
    return ZONE_ID;
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

  public static TemporalQuery<TemporalUnit> precision() {
    return PRECISION;
  }
}
