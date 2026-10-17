/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.datatype;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

public final class DurationImpl extends Duration implements Serializable {
  private static final long serialVersionUID = 1L;
  private static final DatatypeConstants.Field[] kFields = {DatatypeConstants.YEARS, DatatypeConstants.MONTHS,
      DatatypeConstants.DAYS, DatatypeConstants.HOURS, DatatypeConstants.MINUTES, DatatypeConstants.SECONDS};
  private static final BigDecimal[] kFactors = {BigDecimal.valueOf(12), null, BigDecimal.valueOf(24),
      BigDecimal.valueOf(60), BigDecimal.valueOf(60)};
  private static final XMLGregorianCalendar[] kTestPoints = {
      new XMLGregorianCalendarImpl("1696-09-01T00:00:00Z"), new XMLGregorianCalendarImpl("1697-02-01T00:00:00Z"),
      new XMLGregorianCalendarImpl("1903-03-01T00:00:00Z"), new XMLGregorianCalendarImpl("1903-07-01T00:00:00Z")};

  private int signum;
  private BigInteger years;
  private BigInteger months;
  private BigInteger days;
  private BigInteger hours;
  private BigInteger minutes;
  private BigDecimal seconds;

  public DurationImpl(boolean isPositive, BigInteger years, BigInteger months, BigInteger days, BigInteger hours,
      BigInteger minutes, BigDecimal seconds) {
    this.years = years;
    this.months = months;
    this.days = days;
    this.hours = hours;
    this.minutes = minutes;
    this.seconds = seconds;
    this.signum = calcSignum(isPositive);
    if (years == null && months == null && days == null && hours == null && minutes == null && seconds == null) {
      throw new IllegalArgumentException("All the fields (javax.xml.datatype.DatatypeConstants.Field) are null.");
    }
    testNonNegative(years, DatatypeConstants.YEARS);
    testNonNegative(months, DatatypeConstants.MONTHS);
    testNonNegative(days, DatatypeConstants.DAYS);
    testNonNegative(hours, DatatypeConstants.HOURS);
    testNonNegative(minutes, DatatypeConstants.MINUTES);
    testNonNegative(seconds, DatatypeConstants.SECONDS);
  }

  public DurationImpl(long millis) {
    long l = millis;
    boolean tweak = false;
    if (l > 0) {
      signum = 1;
    } else if (l < 0) {
      signum = -1;
      if (l == Long.MIN_VALUE) {
        l++;
        tweak = true;
      }
      l *= -1;
    }
    GregorianCalendar gc = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    gc.setGregorianChange(new Date(Long.MIN_VALUE));
    gc.setTimeInMillis(l);
    this.years = BigInteger.valueOf(gc.get(Calendar.YEAR) - 1970);
    this.months = BigInteger.valueOf(gc.get(Calendar.MONTH));
    this.days = BigInteger.valueOf(gc.get(Calendar.DAY_OF_MONTH) - 1);
    this.hours = BigInteger.valueOf(gc.get(Calendar.HOUR_OF_DAY));
    this.minutes = BigInteger.valueOf(gc.get(Calendar.MINUTE));
    long ms = gc.get(Calendar.SECOND) * 1000L + gc.get(Calendar.MILLISECOND);
    if (tweak) {
      ms++;
    }
    this.seconds = BigDecimal.valueOf(ms, 3);
  }

  public DurationImpl(String lexical) {
    if (lexical == null) {
      throw new NullPointerException();
    }
    parse(lexical);
  }

  private void parse(String lexical) {
    int n = lexical.length();
    int i = 0;
    boolean negative = false;
    if (i < n && lexical.charAt(i) == '-') {
      negative = true;
      i++;
    }
    if (i >= n || lexical.charAt(i) != 'P') {
      throw new IllegalArgumentException(lexical);
    }
    i++;
    boolean inTime = false;
    int lastRank = -1;
    boolean any = false;
    boolean timeSeen = false;
    boolean pendingTime = false;
    while (i < n) {
      char c = lexical.charAt(i);
      if (c == 'T') {
        if (inTime) {
          throw new IllegalArgumentException(lexical);
        }
        inTime = true;
        timeSeen = true;
        pendingTime = true;
        lastRank = 2;
        i++;
        continue;
      }
      int start = i;
      while (i < n && (lexical.charAt(i) == '.' || (lexical.charAt(i) >= '0' && lexical.charAt(i) <= '9'))) {
        i++;
      }
      if (i == start || i >= n) {
        throw new IllegalArgumentException(lexical);
      }
      String number = lexical.substring(start, i);
      char designator = lexical.charAt(i++);
      int rank;
      if (!inTime) {
        rank = designator == 'Y' ? 0 : designator == 'M' ? 1 : designator == 'D' ? 2 : -1;
      } else {
        rank = designator == 'H' ? 3 : designator == 'M' ? 4 : designator == 'S' ? 5 : -1;
      }
      if (rank < 0 || rank <= lastRank) {
        throw new IllegalArgumentException(lexical);
      }
      lastRank = rank;
      any = true;
      pendingTime = false;
      switch (rank) {
        case 0:
          years = new BigInteger(number);
          break;
        case 1:
          months = new BigInteger(number);
          break;
        case 2:
          days = new BigInteger(number);
          break;
        case 3:
          hours = new BigInteger(number);
          break;
        case 4:
          minutes = new BigInteger(number);
          break;
        default:
          seconds = new BigDecimal(number);
          break;
      }
    }
    if (!any || (timeSeen && pendingTime)) {
      throw new IllegalArgumentException(lexical);
    }
    this.signum = calcSignum(!negative);
  }

  private int calcSignum(boolean isPositive) {
    if ((years == null || years.signum() == 0) && (months == null || months.signum() == 0)
        && (days == null || days.signum() == 0) && (hours == null || hours.signum() == 0)
        && (minutes == null || minutes.signum() == 0) && (seconds == null || seconds.signum() == 0)) {
      return 0;
    }
    return isPositive ? 1 : -1;
  }

  private static void testNonNegative(Number n, DatatypeConstants.Field field) {
    if (n != null && ((n instanceof BigInteger) ? ((BigInteger) n).signum() : ((BigDecimal) n).signum()) < 0) {
      throw new IllegalArgumentException(field + " field is negative");
    }
  }

  @Override
  public int getSign() {
    return signum;
  }

  @Override
  public Number getField(DatatypeConstants.Field field) {
    if (field == null) {
      throw new NullPointerException(
          "javax.xml.datatype.Duration#isSet(DatatypeConstants.Field field)  cannot be called with null parameter.");
    }
    switch (field.getId()) {
      case 0:
        return years;
      case 1:
        return months;
      case 2:
        return days;
      case 3:
        return hours;
      case 4:
        return minutes;
      default:
        return seconds;
    }
  }

  @Override
  public boolean isSet(DatatypeConstants.Field field) {
    if (field == null) {
      throw new NullPointerException(
          "javax.xml.datatype.Duration#isSet(DatatypeConstants.Field field) cannot be called with null parameter.");
    }
    return getField(field) != null;
  }

  private BigDecimal decimal(DatatypeConstants.Field field) {
    Number n = getField(field);
    if (n == null) {
      return BigDecimal.ZERO;
    }
    return n instanceof BigDecimal ? (BigDecimal) n : new BigDecimal((BigInteger) n);
  }

  private static BigDecimal sanitize(Number value, int signum) {
    if (signum == 0 || value == null) {
      return BigDecimal.ZERO;
    }
    BigDecimal v = value instanceof BigDecimal ? (BigDecimal) value : new BigDecimal((BigInteger) value);
    return signum > 0 ? v : v.negate();
  }

  private static BigDecimal sanitize(BigDecimal value, int signum) {
    if (signum == 0) {
      return BigDecimal.ZERO;
    }
    return signum > 0 ? value : value.negate();
  }

  private static BigInteger toBigInteger(BigDecimal value, boolean canBeNull) {
    if (canBeNull && value.signum() == 0) {
      return null;
    }
    return value.unscaledValue().multiply(BigInteger.TEN.pow(Math.max(0, -value.scale()))).divide(
        BigInteger.TEN.pow(Math.max(0, value.scale())));
  }

  private static BigDecimal toBigDecimal(BigDecimal value, boolean canBeNull) {
    if (canBeNull && value.signum() == 0) {
      return null;
    }
    return value;
  }

  private static void alignSigns(BigDecimal[] buf, int start, int end) {
    boolean touched;
    do {
      touched = false;
      int s = 0;
      for (int i = start; i < end; i++) {
        if (s * buf[i].signum() < 0) {
          touched = true;
          BigDecimal borrow = buf[i].abs().divide(kFactors[i - 1], 0, RoundingMode.UP);
          if (buf[i].signum() > 0) {
            borrow = borrow.negate();
          }
          buf[i - 1] = buf[i - 1].subtract(borrow);
          buf[i] = buf[i].add(borrow.multiply(kFactors[i - 1]));
        }
        if (buf[i].signum() != 0) {
          s = buf[i].signum();
        }
      }
    } while (touched);
  }

  @Override
  public Duration add(Duration rhs) {
    Duration lhs = this;
    BigDecimal[] buf = new BigDecimal[6];
    for (int i = 0; i < 6; i++) {
      buf[i] = sanitize(lhs.getField(kFields[i]), lhs.getSign()).add(sanitize(rhs.getField(kFields[i]),
          rhs.getSign()));
    }
    alignSigns(buf, 0, 2);
    alignSigns(buf, 2, 6);
    int s = 0;
    for (int i = 0; i < 6; i++) {
      if (s * buf[i].signum() < 0) {
        throw new IllegalStateException();
      }
      if (s == 0) {
        s = buf[i].signum();
      }
    }
    return new DurationImpl(s >= 0, toBigInteger(sanitize(buf[0], s), lhs.getField(kFields[0]) == null
        && rhs.getField(kFields[0]) == null), toBigInteger(sanitize(buf[1], s), lhs.getField(kFields[1]) == null
        && rhs.getField(kFields[1]) == null), toBigInteger(sanitize(buf[2], s), lhs.getField(kFields[2]) == null
        && rhs.getField(kFields[2]) == null), toBigInteger(sanitize(buf[3], s), lhs.getField(kFields[3]) == null
        && rhs.getField(kFields[3]) == null), toBigInteger(sanitize(buf[4], s), lhs.getField(kFields[4]) == null
        && rhs.getField(kFields[4]) == null), toBigDecimal(sanitize(buf[5], s), lhs.getField(kFields[5]) == null
        && rhs.getField(kFields[5]) == null));
  }

  @Override
  public void addTo(Calendar calendar) {
    calendar.add(Calendar.YEAR, getYears() * signum);
    calendar.add(Calendar.MONTH, getMonths() * signum);
    calendar.add(Calendar.DAY_OF_MONTH, getDays() * signum);
    calendar.add(Calendar.HOUR_OF_DAY, getHours() * signum);
    calendar.add(Calendar.MINUTE, getMinutes() * signum);
    calendar.add(Calendar.SECOND, getSeconds() * signum);
    if (seconds != null) {
      BigDecimal fraction = seconds.subtract(seconds.setScale(0, RoundingMode.DOWN));
      int millis = fraction.movePointRight(3).intValue();
      calendar.add(Calendar.MILLISECOND, millis * signum);
    }
  }

  @Override
  public Duration multiply(BigDecimal factor) {
    BigDecimal carry = BigDecimal.ZERO;
    int factorSign = factor.signum();
    BigDecimal absFactor = factor.abs();
    BigDecimal[] buf = new BigDecimal[6];
    for (int i = 0; i < 5; i++) {
      BigDecimal bd = decimal(kFields[i]);
      bd = bd.multiply(absFactor).add(carry);
      buf[i] = bd.setScale(0, RoundingMode.DOWN);
      bd = bd.subtract(buf[i]);
      if (i == 1) {
        if (bd.signum() != 0) {
          throw new IllegalStateException();
        }
        carry = BigDecimal.ZERO;
      } else {
        carry = bd.multiply(kFactors[i]);
      }
    }
    if (seconds != null) {
      buf[5] = seconds.multiply(absFactor).add(carry);
    } else {
      buf[5] = carry;
    }
    return new DurationImpl(this.signum * factorSign >= 0, toBigInteger(buf[0], null == years),
        toBigInteger(buf[1], null == months), toBigInteger(buf[2], null == days), toBigInteger(buf[3], null == hours),
        toBigInteger(buf[4], null == minutes), toBigDecimal(buf[5], null == seconds));
  }

  @Override
  public Duration negate() {
    DurationImpl d = new DurationImpl(true, years, months, days, hours, minutes, seconds);
    d.signum = -signum;
    return d;
  }

  @Override
  public Duration normalizeWith(Calendar startTimeInstant) {
    Calendar c = (Calendar) startTimeInstant.clone();
    c.add(Calendar.YEAR, getYears() * signum);
    c.add(Calendar.MONTH, getMonths() * signum);
    c.add(Calendar.DAY_OF_MONTH, getDays() * signum);
    long diff = c.getTimeInMillis() - startTimeInstant.getTimeInMillis();
    int dayCount = (int) (diff / (1000L * 60L * 60L * 24L));
    return new DurationImpl(dayCount >= 0, null, null, BigInteger.valueOf(Math.abs(dayCount)), hours, minutes,
        seconds);
  }

  @Override
  public int compare(Duration rhs) {
    BigInteger maxInt = BigInteger.valueOf(Integer.MAX_VALUE);
    BigInteger minInt = BigInteger.valueOf(Integer.MIN_VALUE);
    for (int i = 0; i < 5; i++) {
      checkRange(this, kFields[i], maxInt, minInt);
      checkRange(rhs, kFields[i], maxInt, minInt);
    }
    int resultA = DatatypeConstants.INDETERMINATE;
    for (int i = 0; i < kTestPoints.length; i++) {
      XMLGregorianCalendar a = (XMLGregorianCalendar) kTestPoints[i].clone();
      XMLGregorianCalendar b = (XMLGregorianCalendar) kTestPoints[i].clone();
      a.add(this);
      b.add(rhs);
      int r = a.compare(b);
      if (r == DatatypeConstants.INDETERMINATE) {
        return r;
      }
      if (i == 0) {
        resultA = r;
      } else if (r != resultA) {
        return DatatypeConstants.INDETERMINATE;
      }
    }
    return resultA;
  }

  private static void checkRange(Duration d, DatatypeConstants.Field field, BigInteger max, BigInteger min) {
    Number n = d.getField(field);
    if (n instanceof BigInteger && (((BigInteger) n).compareTo(max) > 0 || ((BigInteger) n).compareTo(min) < 0)) {
      throw new UnsupportedOperationException("Value too large: " + d.getClass().getName() + "#compare(Duration duration)"
          + field + " " + n);
    }
  }

  @Override
  public int hashCode() {
    Calendar cal = kTestPoints[0].toGregorianCalendar();
    addTo(cal);
    long ms = cal.getTimeInMillis();
    return (int) ms;
  }
}
