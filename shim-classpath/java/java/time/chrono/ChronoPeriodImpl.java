/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.time.chrono;

import java.io.Serializable;
import java.time.DateTimeException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

final class ChronoPeriodImpl implements ChronoPeriod, Serializable {
  private static final long serialVersionUID = 57387258289L;

  private static final List<TemporalUnit> kSupportedUnits =
      Collections.unmodifiableList(Arrays.<TemporalUnit>asList(ChronoUnit.YEARS, ChronoUnit.MONTHS, ChronoUnit.DAYS));

  private final Chronology chrono;
  final int years;
  final int months;
  final int days;

  ChronoPeriodImpl(Chronology chrono, int years, int months, int days) {
    Objects.requireNonNull(chrono, "chrono");
    this.chrono = chrono;
    this.years = years;
    this.months = months;
    this.days = days;
  }

  @Override
  public long get(TemporalUnit unit) {
    if (unit == ChronoUnit.YEARS) {
      return years;
    } else if (unit == ChronoUnit.MONTHS) {
      return months;
    } else if (unit == ChronoUnit.DAYS) {
      return days;
    }
    throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
  }

  @Override
  public List<TemporalUnit> getUnits() {
    return kSupportedUnits;
  }

  @Override
  public Chronology getChronology() {
    return chrono;
  }

  @Override
  public boolean isZero() {
    return years == 0 && months == 0 && days == 0;
  }

  @Override
  public boolean isNegative() {
    return years < 0 || months < 0 || days < 0;
  }

  @Override
  public ChronoPeriod plus(TemporalAmount amountToAdd) {
    ChronoPeriodImpl amount = validateAmount(amountToAdd);
    return new ChronoPeriodImpl(chrono, Math.addExact(years, amount.years), Math.addExact(months, amount.months),
        Math.addExact(days, amount.days));
  }

  @Override
  public ChronoPeriod minus(TemporalAmount amountToSubtract) {
    ChronoPeriodImpl amount = validateAmount(amountToSubtract);
    return new ChronoPeriodImpl(chrono, Math.subtractExact(years, amount.years),
        Math.subtractExact(months, amount.months), Math.subtractExact(days, amount.days));
  }

  private ChronoPeriodImpl validateAmount(TemporalAmount amount) {
    Objects.requireNonNull(amount, "amount");
    if (!(amount instanceof ChronoPeriodImpl)) {
      throw new DateTimeException("Unable to obtain ChronoPeriod from TemporalAmount: " + amount.getClass());
    }
    ChronoPeriodImpl period = (ChronoPeriodImpl) amount;
    if (!chrono.equals(period.getChronology())) {
      throw new ClassCastException("Chronology mismatch, expected: " + chrono.getId() + ", actual: "
          + period.getChronology().getId());
    }
    return period;
  }

  @Override
  public ChronoPeriod multipliedBy(int scalar) {
    if (this.isZero() || scalar == 1) {
      return this;
    }
    return new ChronoPeriodImpl(chrono, Math.multiplyExact(years, scalar), Math.multiplyExact(months, scalar),
        Math.multiplyExact(days, scalar));
  }

  @Override
  public ChronoPeriod normalized() {
    long monthRange = monthRange();
    if (monthRange > 0) {
      long totalMonths = years * monthRange + months;
      long splitYears = totalMonths / monthRange;
      int splitMonths = (int) (totalMonths % monthRange);
      if (splitYears == years && splitMonths == months) {
        return this;
      }
      return new ChronoPeriodImpl(chrono, Math.toIntExact(splitYears), splitMonths, days);
    }
    return this;
  }

  private long monthRange() {
    java.time.temporal.ValueRange startRange = chrono.range(java.time.temporal.ChronoField.MONTH_OF_YEAR);
    if (startRange.isFixed() && startRange.isIntValue()) {
      return startRange.getMaximum() - startRange.getMinimum() + 1;
    }
    return -1;
  }

  @Override
  public Temporal addTo(Temporal temporal) {
    validateChrono(temporal);
    if (months == 0) {
      if (years != 0) {
        temporal = temporal.plus(years, ChronoUnit.YEARS);
      }
    } else {
      long monthRange = monthRange();
      if (monthRange > 0) {
        temporal = temporal.plus(years * monthRange + months, ChronoUnit.MONTHS);
      } else {
        if (years != 0) {
          temporal = temporal.plus(years, ChronoUnit.YEARS);
        }
        temporal = temporal.plus(months, ChronoUnit.MONTHS);
      }
    }
    if (days != 0) {
      temporal = temporal.plus(days, ChronoUnit.DAYS);
    }
    return temporal;
  }

  @Override
  public Temporal subtractFrom(Temporal temporal) {
    validateChrono(temporal);
    if (months == 0) {
      if (years != 0) {
        temporal = temporal.minus(years, ChronoUnit.YEARS);
      }
    } else {
      long monthRange = monthRange();
      if (monthRange > 0) {
        temporal = temporal.minus(years * monthRange + months, ChronoUnit.MONTHS);
      } else {
        if (years != 0) {
          temporal = temporal.minus(years, ChronoUnit.YEARS);
        }
        temporal = temporal.minus(months, ChronoUnit.MONTHS);
      }
    }
    if (days != 0) {
      temporal = temporal.minus(days, ChronoUnit.DAYS);
    }
    return temporal;
  }

  private void validateChrono(java.time.temporal.TemporalAccessor temporal) {
    Objects.requireNonNull(temporal, "temporal");
    Chronology temporalChrono = temporal.query(java.time.temporal.TemporalQueries.chronology());
    if (temporalChrono != null && !chrono.equals(temporalChrono)) {
      throw new DateTimeException("Chronology mismatch, expected: " + chrono.getId() + ", actual: "
          + temporalChrono.getId());
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof ChronoPeriodImpl) {
      ChronoPeriodImpl other = (ChronoPeriodImpl) obj;
      return years == other.years && months == other.months && days == other.days && chrono.equals(other.chrono);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return (years + Integer.rotateLeft(months, 8) + Integer.rotateLeft(days, 16)) ^ chrono.hashCode();
  }

  @Override
  public String toString() {
    if (isZero()) {
      return getChronology().toString() + " P0D";
    } else {
      StringBuilder buf = new StringBuilder();
      buf.append(getChronology().toString()).append(' ').append('P');
      if (years != 0) {
        buf.append(years).append('Y');
      }
      if (months != 0) {
        buf.append(months).append('M');
      }
      if (days != 0) {
        buf.append(days).append('D');
      }
      return buf.toString();
    }
  }
}
