/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.time;

import java.io.Serializable;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoPeriod;
import java.time.chrono.Chronology;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalUnit;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Period implements ChronoPeriod, Serializable {
  private static final long serialVersionUID = -3587258372562876L;

  public static final Period ZERO = new Period(0, 0, 0);

  private static final Pattern PATTERN = Pattern.compile(
      "([-+]?)P(?:([-+]?[0-9]+)Y)?(?:([-+]?[0-9]+)M)?(?:([-+]?[0-9]+)W)?(?:([-+]?[0-9]+)D)?");

  private static class SupportedUnits {
    static final List<TemporalUnit> UNITS = Collections.unmodifiableList(
        Arrays.<TemporalUnit>asList(ChronoUnit.YEARS, ChronoUnit.MONTHS, ChronoUnit.DAYS));
  }

  private final int years;
  private final int months;
  private final int days;

  public static Period ofYears(int years) {
    return create(years, 0, 0);
  }

  public static Period ofMonths(int months) {
    return create(0, months, 0);
  }

  public static Period ofWeeks(int weeks) {
    return create(0, 0, Math.multiplyExact(weeks, 7));
  }

  public static Period ofDays(int days) {
    return create(0, 0, days);
  }

  public static Period of(int years, int months, int days) {
    return create(years, months, days);
  }

  public static Period from(TemporalAmount amount) {
    if (amount instanceof Period) {
      return (Period) amount;
    }
    if (amount instanceof ChronoPeriod) {
      if (IsoChronology.INSTANCE.equals(((ChronoPeriod) amount).getChronology()) == false) {
        throw new DateTimeException("Period requires ISO chronology: " + amount);
      }
    }
    Objects.requireNonNull(amount, "amount");
    int years = 0;
    int months = 0;
    int days = 0;
    for (TemporalUnit unit : amount.getUnits()) {
      long unitAmount = amount.get(unit);
      if (unit == ChronoUnit.YEARS) {
        years = Math.toIntExact(unitAmount);
      } else if (unit == ChronoUnit.MONTHS) {
        months = Math.toIntExact(unitAmount);
      } else if (unit == ChronoUnit.DAYS) {
        days = Math.toIntExact(unitAmount);
      } else {
        throw new DateTimeException("Unit must be Years, Months or Days, but was " + unit);
      }
    }
    return create(years, months, days);
  }

  public static Period parse(CharSequence text) {
    Objects.requireNonNull(text, "text");
    Matcher matcher = PATTERN.matcher(text.toString().toUpperCase());
    if (matcher.matches()) {
      int negate = ("-".equals(matcher.group(1)) ? -1 : 1);
      String yearMatch = matcher.group(2);
      String monthMatch = matcher.group(3);
      String weekMatch = matcher.group(4);
      String dayMatch = matcher.group(5);
      if (yearMatch != null || monthMatch != null || dayMatch != null || weekMatch != null) {
        try {
          int years = parseNumber(text, yearMatch, negate);
          int months = parseNumber(text, monthMatch, negate);
          int weeks = parseNumber(text, weekMatch, negate);
          int days = parseNumber(text, dayMatch, negate);
          days = Math.addExact(days, Math.multiplyExact(weeks, 7));
          return create(years, months, days);
        } catch (NumberFormatException ex) {
          throw (DateTimeParseException) new DateTimeParseException("Text cannot be parsed to a Period", text, 0)
              .initCause(ex);
        } catch (ArithmeticException ex) {
          throw (DateTimeParseException) new DateTimeParseException("Text cannot be parsed to a Period", text, 0)
              .initCause(ex);
        }
      }
    }
    throw new DateTimeParseException("Text cannot be parsed to a Period", text, 0);
  }

  private static int parseNumber(CharSequence text, String str, int negate) {
    if (str == null) {
      return 0;
    }
    int val = Integer.parseInt(str);
    try {
      return Math.multiplyExact(val, negate);
    } catch (ArithmeticException ex) {
      throw (DateTimeParseException) new DateTimeParseException("Text cannot be parsed to a Period", text, 0)
          .initCause(ex);
    }
  }

  public static Period between(LocalDate startDateInclusive, LocalDate endDateExclusive) {
    return startDateInclusive.until(endDateExclusive);
  }

  private static Period create(int years, int months, int days) {
    if ((years | months | days) == 0) {
      return ZERO;
    }
    return new Period(years, months, days);
  }

  private Period(int years, int months, int days) {
    this.years = years;
    this.months = months;
    this.days = days;
  }

  @Override
  public long get(TemporalUnit unit) {
    if (unit == ChronoUnit.YEARS) {
      return getYears();
    } else if (unit == ChronoUnit.MONTHS) {
      return getMonths();
    } else if (unit == ChronoUnit.DAYS) {
      return getDays();
    } else {
      throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
    }
  }

  @Override
  public List<TemporalUnit> getUnits() {
    return SupportedUnits.UNITS;
  }

  @Override
  public IsoChronology getChronology() {
    return IsoChronology.INSTANCE;
  }

  @Override
  public boolean isZero() {
    return (this == ZERO);
  }

  @Override
  public boolean isNegative() {
    return years < 0 || months < 0 || days < 0;
  }

  public int getYears() {
    return years;
  }

  public int getMonths() {
    return months;
  }

  public int getDays() {
    return days;
  }

  public Period withYears(int years) {
    if (years == this.years) {
      return this;
    }
    return create(years, months, days);
  }

  public Period withMonths(int months) {
    if (months == this.months) {
      return this;
    }
    return create(years, months, days);
  }

  public Period withDays(int days) {
    if (days == this.days) {
      return this;
    }
    return create(years, months, days);
  }

  @Override
  public Period plus(TemporalAmount amountToAdd) {
    Period isoAmount = Period.from(amountToAdd);
    return create(Math.addExact(years, isoAmount.years), Math.addExact(months, isoAmount.months),
        Math.addExact(days, isoAmount.days));
  }

  public Period plusYears(long yearsToAdd) {
    if (yearsToAdd == 0) {
      return this;
    }
    return create(Math.toIntExact(Math.addExact(years, yearsToAdd)), months, days);
  }

  public Period plusMonths(long monthsToAdd) {
    if (monthsToAdd == 0) {
      return this;
    }
    return create(years, Math.toIntExact(Math.addExact(months, monthsToAdd)), days);
  }

  public Period plusDays(long daysToAdd) {
    if (daysToAdd == 0) {
      return this;
    }
    return create(years, months, Math.toIntExact(Math.addExact(days, daysToAdd)));
  }

  @Override
  public Period minus(TemporalAmount amountToSubtract) {
    Period isoAmount = Period.from(amountToSubtract);
    return create(Math.subtractExact(years, isoAmount.years), Math.subtractExact(months, isoAmount.months),
        Math.subtractExact(days, isoAmount.days));
  }

  public Period minusYears(long yearsToSubtract) {
    return (yearsToSubtract == Long.MIN_VALUE ? plusYears(Long.MAX_VALUE).plusYears(1)
        : plusYears(-yearsToSubtract));
  }

  public Period minusMonths(long monthsToSubtract) {
    return (monthsToSubtract == Long.MIN_VALUE ? plusMonths(Long.MAX_VALUE).plusMonths(1)
        : plusMonths(-monthsToSubtract));
  }

  public Period minusDays(long daysToSubtract) {
    return (daysToSubtract == Long.MIN_VALUE ? plusDays(Long.MAX_VALUE).plusDays(1) : plusDays(-daysToSubtract));
  }

  @Override
  public Period multipliedBy(int scalar) {
    if (this == ZERO || scalar == 1) {
      return this;
    }
    return create(Math.multiplyExact(years, scalar), Math.multiplyExact(months, scalar),
        Math.multiplyExact(days, scalar));
  }

  @Override
  public Period negated() {
    return multipliedBy(-1);
  }

  @Override
  public Period normalized() {
    long totalMonths = toTotalMonths();
    long splitYears = totalMonths / 12;
    int splitMonths = (int) (totalMonths % 12);
    if (splitYears == years && splitMonths == months) {
      return this;
    }
    return create(Math.toIntExact(splitYears), splitMonths, days);
  }

  public long toTotalMonths() {
    return years * 12L + months;
  }

  @Override
  public Temporal addTo(Temporal temporal) {
    validateChrono(temporal);
    if (months == 0) {
      if (years != 0) {
        temporal = temporal.plus(years, ChronoUnit.YEARS);
      }
    } else {
      long totalMonths = toTotalMonths();
      if (totalMonths != 0) {
        temporal = temporal.plus(totalMonths, ChronoUnit.MONTHS);
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
      long totalMonths = toTotalMonths();
      if (totalMonths != 0) {
        temporal = temporal.minus(totalMonths, ChronoUnit.MONTHS);
      }
    }
    if (days != 0) {
      temporal = temporal.minus(days, ChronoUnit.DAYS);
    }
    return temporal;
  }

  private void validateChrono(TemporalAccessor temporal) {
    Objects.requireNonNull(temporal, "temporal");
    Chronology temporalChrono = temporal.query(TemporalQueries.chronology());
    if (temporalChrono != null && IsoChronology.INSTANCE.equals(temporalChrono) == false) {
      throw new DateTimeException("Chronology mismatch, expected: ISO, actual: " + temporalChrono.getId());
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof Period) {
      Period other = (Period) obj;
      return years == other.years && months == other.months && days == other.days;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return years + Integer.rotateLeft(months, 8) + Integer.rotateLeft(days, 16);
  }

  @Override
  public String toString() {
    if (this == ZERO) {
      return "P0D";
    } else {
      StringBuilder buf = new StringBuilder();
      buf.append('P');
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
