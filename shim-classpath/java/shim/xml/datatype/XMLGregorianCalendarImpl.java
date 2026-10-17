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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

public final class XMLGregorianCalendarImpl extends XMLGregorianCalendar implements Serializable, Cloneable {
  private static final long serialVersionUID = 1L;
  private static final int kUndefined = DatatypeConstants.FIELD_UNDEFINED;
  private static final BigInteger kBillion = BigInteger.valueOf(1000000000L);
  private static final BigInteger kTwelve = BigInteger.valueOf(12);
  private static final BigInteger kSixty = BigInteger.valueOf(60);
  private static final BigInteger kTwentyFour = BigInteger.valueOf(24);
  private static final String kImplName =
      "com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl";

  private BigInteger eon;
  private int year = kUndefined;
  private int month = kUndefined;
  private int day = kUndefined;
  private int timezone = kUndefined;
  private int hour = kUndefined;
  private int minute = kUndefined;
  private int second = kUndefined;
  private BigDecimal fractionalSecond;

  private BigInteger origEon;
  private int origYear = kUndefined;
  private int origMonth = kUndefined;
  private int origDay = kUndefined;
  private int origTimezone = kUndefined;
  private int origHour = kUndefined;
  private int origMinute = kUndefined;
  private int origSecond = kUndefined;
  private BigDecimal origFractionalSecond;

  public XMLGregorianCalendarImpl() {
  }

  public XMLGregorianCalendarImpl(String lexical) {
    new LexicalParser(lexical).parseInto(this);
    save();
  }

  public XMLGregorianCalendarImpl(BigInteger year, int month, int day, int hour, int minute, int second,
      BigDecimal fractionalSecond, int timezone) {
    setYear(year);
    setMonth(month);
    setDay(day);
    setTimezone(timezone);
    setTime(hour, minute, second, fractionalSecond);
    if (!isValid()) {
      throw new IllegalArgumentException(DatatypeMessages.invalidFields(new Object[] {
          Integer.valueOf(getYear()), Integer.valueOf(getMonth()), Integer.valueOf(getDay()),
          Integer.valueOf(getHour()), Integer.valueOf(getMinute()), Integer.valueOf(getSecond()),
          getFractionalSecond(), Integer.valueOf(getTimezone())}));
    }
    save();
  }

  public XMLGregorianCalendarImpl(GregorianCalendar cal) {
    int era = cal.get(java.util.Calendar.ERA);
    int y = cal.get(java.util.Calendar.YEAR);
    setYear(era == GregorianCalendar.BC ? -y : y);
    setMonth(cal.get(java.util.Calendar.MONTH) + 1);
    setDay(cal.get(java.util.Calendar.DAY_OF_MONTH));
    setTime(cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE),
        cal.get(java.util.Calendar.SECOND), BigDecimal.valueOf(cal.get(java.util.Calendar.MILLISECOND), 3));
    int offset = (cal.get(java.util.Calendar.ZONE_OFFSET) + cal.get(java.util.Calendar.DST_OFFSET)) / (60 * 1000);
    setTimezone(offset);
    save();
  }

  private void save() {
    origEon = eon;
    origYear = year;
    origMonth = month;
    origDay = day;
    origTimezone = timezone;
    origHour = hour;
    origMinute = minute;
    origSecond = second;
    origFractionalSecond = fractionalSecond;
  }

  @Override
  public void clear() {
    eon = null;
    year = month = day = timezone = hour = minute = second = kUndefined;
    fractionalSecond = null;
  }

  @Override
  public void reset() {
    eon = origEon;
    year = origYear;
    month = origMonth;
    day = origDay;
    timezone = origTimezone;
    hour = origHour;
    minute = origMinute;
    second = origSecond;
    fractionalSecond = origFractionalSecond;
  }

  private static void check(int value, int min, int max, String name) {
    if (value != kUndefined && (value < min || value > max)) {
      throw new IllegalArgumentException(DatatypeMessages.invalidField(Integer.valueOf(value), name));
    }
  }

  @Override
  public void setYear(BigInteger y) {
    if (y == null) {
      eon = null;
      year = kUndefined;
      return;
    }
    BigInteger[] qr = y.divideAndRemainder(kBillion);
    year = qr[1].intValue();
    eon = qr[0].signum() == 0 ? null : qr[0].multiply(kBillion);
  }

  @Override
  public void setYear(int y) {
    if (y == kUndefined) {
      eon = null;
      year = kUndefined;
    } else {
      setYear(BigInteger.valueOf(y));
    }
  }

  @Override
  public void setMonth(int m) {
    check(m, DatatypeConstants.JANUARY, DatatypeConstants.DECEMBER, "Month");
    month = m;
  }

  @Override
  public void setDay(int d) {
    check(d, 1, 31, "Day");
    day = d;
  }

  @Override
  public void setTimezone(int offset) {
    check(offset, DatatypeConstants.MAX_TIMEZONE_OFFSET, DatatypeConstants.MIN_TIMEZONE_OFFSET, "Timezone");
    timezone = offset;
  }

  @Override
  public void setTime(int h, int m, int s) {
    setTime(h, m, s, null);
  }

  @Override
  public void setTime(int h, int m, int s, BigDecimal fractional) {
    setHour(h, false);
    setMinute(m);
    setSecond(s);
    setFractionalSecond(fractional);
    testHour();
  }

  @Override
  public void setTime(int h, int m, int s, int millisecond) {
    setHour(h, false);
    setMinute(m);
    setSecond(s);
    setMillisecond(millisecond);
    testHour();
  }

  @Override
  public void setHour(int h) {
    setHour(h, true);
  }

  private void setHour(int h, boolean validate) {
    check(h, 0, 24, "Hour");
    hour = h;
    if (validate) {
      testHour();
    }
  }

  private void testHour() {
    if (hour == 24) {
      if (minute != 0 || second != 0) {
        throw new IllegalArgumentException(DatatypeMessages.invalidField(Integer.valueOf(24), "Hour"));
      }
      hour = 0;
      add(new DurationImpl(true, null, null, BigInteger.ONE, null, null, null));
    }
  }

  @Override
  public void setMinute(int m) {
    check(m, 0, 59, "Minute");
    minute = m;
  }

  @Override
  public void setSecond(int s) {
    check(s, 0, 60, "Second");
    second = s;
  }

  @Override
  public void setMillisecond(int ms) {
    if (ms == kUndefined) {
      fractionalSecond = null;
      return;
    }
    check(ms, 0, 999, "Millisecond");
    fractionalSecond = BigDecimal.valueOf(ms, 3);
  }

  @Override
  public void setFractionalSecond(BigDecimal fractional) {
    if (fractional != null
        && (fractional.compareTo(BigDecimal.ZERO) < 0 || fractional.compareTo(BigDecimal.ONE) > 0)) {
      throw new IllegalArgumentException(DatatypeMessages.invalidFraction(fractional));
    }
    fractionalSecond = fractional;
  }

  @Override
  public BigInteger getEon() {
    return eon;
  }

  @Override
  public int getYear() {
    return year;
  }

  @Override
  public BigInteger getEonAndYear() {
    if (year == kUndefined) {
      return null;
    }
    return eon == null ? BigInteger.valueOf(year) : eon.add(BigInteger.valueOf(year));
  }

  @Override
  public int getMonth() {
    return month;
  }

  @Override
  public int getDay() {
    return day;
  }

  @Override
  public int getTimezone() {
    return timezone;
  }

  @Override
  public int getHour() {
    return hour;
  }

  @Override
  public int getMinute() {
    return minute;
  }

  @Override
  public int getSecond() {
    return second;
  }

  @Override
  public BigDecimal getFractionalSecond() {
    return fractionalSecond;
  }

  @Override
  public int hashCode() {
    int tz = getTimezone();
    if (tz == kUndefined) {
      tz = 0;
    }
    XMLGregorianCalendar gc = this;
    if (tz != 0) {
      gc = normalize();
    }
    int h = 31 + gc.getYear();
    h = h * 31 + gc.getMonth();
    h = h * 31 + gc.getDay();
    h = h * 31 + gc.getHour();
    h = h * 31 + gc.getMinute();
    h = h * 31 + gc.getSecond();
    return h * 31 + gc.getMillisecond();
  }

  @Override
  public XMLGregorianCalendar normalize() {
    XMLGregorianCalendarImpl result = (XMLGregorianCalendarImpl) clone();
    if (timezone == kUndefined) {
      return result;
    }
    result.shiftByMinutes(-timezone);
    result.timezone = 0;
    if (getMillisecond() == kUndefined) {
      result.fractionalSecond = null;
    }
    return result;
  }

  private XMLGregorianCalendarImpl shifted(int tz) {
    XMLGregorianCalendarImpl result = (XMLGregorianCalendarImpl) clone();
    result.shiftByMinutes(-tz);
    result.timezone = 0;
    return result;
  }

  private void shiftByMinutes(int minutes) {
    add(new DurationImpl(minutes >= 0, null, null, null, null, BigInteger.valueOf(Math.abs((long) minutes)), null));
  }

  private static int compareField(int p, int q) {
    if (p == q) {
      return DatatypeConstants.EQUAL;
    }
    if (p == kUndefined || q == kUndefined) {
      return DatatypeConstants.INDETERMINATE;
    }
    return p < q ? DatatypeConstants.LESSER : DatatypeConstants.GREATER;
  }

  private static int compareField(BigInteger p, BigInteger q) {
    if (p == null) {
      return q == null ? DatatypeConstants.EQUAL : DatatypeConstants.INDETERMINATE;
    }
    if (q == null) {
      return DatatypeConstants.INDETERMINATE;
    }
    return p.compareTo(q);
  }

  private static int compareField(BigDecimal p, BigDecimal q) {
    if (p == q) {
      return DatatypeConstants.EQUAL;
    }
    return (p == null ? BigDecimal.ZERO : p).compareTo(q == null ? BigDecimal.ZERO : q);
  }

  private static int internalCompare(XMLGregorianCalendarImpl p, XMLGregorianCalendarImpl q) {
    int result;
    if (p.eon == q.eon || (p.eon != null && p.eon.equals(q.eon))) {
      result = compareField(p.year, q.year);
    } else {
      result = compareField(p.getEonAndYear(), q.getEonAndYear());
    }
    if (result != DatatypeConstants.EQUAL) {
      return result;
    }
    result = compareField(p.month, q.month);
    if (result != DatatypeConstants.EQUAL) {
      return result;
    }
    result = compareField(p.day, q.day);
    if (result != DatatypeConstants.EQUAL) {
      return result;
    }
    result = compareField(p.hour, q.hour);
    if (result != DatatypeConstants.EQUAL) {
      return result;
    }
    result = compareField(p.minute, q.minute);
    if (result != DatatypeConstants.EQUAL) {
      return result;
    }
    result = compareField(p.second, q.second);
    if (result != DatatypeConstants.EQUAL) {
      return result;
    }
    return compareField(p.fractionalSecond, q.fractionalSecond);
  }

  @Override
  public int compare(XMLGregorianCalendar rhs) {
    XMLGregorianCalendarImpl p = this;
    XMLGregorianCalendarImpl q = (XMLGregorianCalendarImpl) rhs;
    if (p.timezone == q.timezone) {
      return internalCompare(p, q);
    }
    if (p.timezone != kUndefined && q.timezone != kUndefined) {
      return internalCompare((XMLGregorianCalendarImpl) p.normalize(), (XMLGregorianCalendarImpl) q.normalize());
    }
    if (p.timezone != kUndefined) {
      if (p.timezone != 0) {
        p = p.shifted(p.timezone);
      }
      int result = internalCompare(p, q.shifted(DatatypeConstants.MIN_TIMEZONE_OFFSET));
      if (result == DatatypeConstants.LESSER) {
        return result;
      }
      result = internalCompare(p, q.shifted(DatatypeConstants.MAX_TIMEZONE_OFFSET));
      if (result == DatatypeConstants.GREATER) {
        return result;
      }
      return DatatypeConstants.INDETERMINATE;
    }
    if (q.timezone != 0) {
      q = q.shifted(q.timezone);
    }
    int result = internalCompare(p.shifted(DatatypeConstants.MAX_TIMEZONE_OFFSET), q);
    if (result == DatatypeConstants.LESSER) {
      return result;
    }
    result = internalCompare(p.shifted(DatatypeConstants.MIN_TIMEZONE_OFFSET), q);
    if (result == DatatypeConstants.GREATER) {
      return result;
    }
    return DatatypeConstants.INDETERMINATE;
  }

  @Override
  public QName getXMLSchemaType() {
    int mask = 0;
    mask |= year != kUndefined ? 1 : 0;
    mask |= month != kUndefined ? 2 : 0;
    mask |= day != kUndefined ? 4 : 0;
    mask |= hour != kUndefined ? 8 : 0;
    mask |= minute != kUndefined ? 16 : 0;
    mask |= second != kUndefined ? 32 : 0;
    switch (mask) {
      case 63:
        return DatatypeConstants.DATETIME;
      case 7:
        return DatatypeConstants.DATE;
      case 56:
        return DatatypeConstants.TIME;
      case 3:
        return DatatypeConstants.GYEARMONTH;
      case 6:
        return DatatypeConstants.GMONTHDAY;
      case 1:
        return DatatypeConstants.GYEAR;
      case 2:
        return DatatypeConstants.GMONTH;
      case 4:
        return DatatypeConstants.GDAY;
      default:
        throw new IllegalStateException(kImplName
            + "#getXMLSchemaType() :Invalid set of fields set for XMLGregorianCalendar");
    }
  }

  @Override
  public String toXMLFormat() {
    QName type = getXMLSchemaType();
    StringBuilder sb = new StringBuilder();
    if (type == DatatypeConstants.DATETIME) {
      appendDate(sb);
      sb.append('T');
      appendTime(sb);
    } else if (type == DatatypeConstants.DATE) {
      appendDate(sb);
    } else if (type == DatatypeConstants.TIME) {
      appendTime(sb);
    } else if (type == DatatypeConstants.GYEARMONTH) {
      appendYear(sb);
      sb.append('-');
      two(sb, month);
    } else if (type == DatatypeConstants.GMONTHDAY) {
      sb.append("--");
      two(sb, month);
      sb.append('-');
      two(sb, day);
    } else if (type == DatatypeConstants.GYEAR) {
      appendYear(sb);
    } else if (type == DatatypeConstants.GMONTH) {
      sb.append("--");
      two(sb, month);
    } else {
      sb.append("---");
      two(sb, day);
    }
    appendTimezone(sb);
    return sb.toString();
  }

  private void appendDate(StringBuilder sb) {
    appendYear(sb);
    sb.append('-');
    two(sb, month);
    sb.append('-');
    two(sb, day);
  }

  private void appendTime(StringBuilder sb) {
    two(sb, hour);
    sb.append(':');
    two(sb, minute);
    sb.append(':');
    two(sb, second);
    if (fractionalSecond != null) {
      String plain = fractionalSecond.toPlainString();
      int dot = plain.indexOf('.');
      if (dot >= 0) {
        sb.append(plain.substring(dot));
      }
    }
  }

  private void appendYear(StringBuilder sb) {
    if (eon == null) {
      if (year < 0) {
        sb.append('-');
      }
      String digits = Integer.toString(Math.abs(year));
      for (int i = digits.length(); i < 4; i++) {
        sb.append('0');
      }
      sb.append(digits);
    } else {
      sb.append(getEonAndYear().toString());
    }
  }

  private void appendTimezone(StringBuilder sb) {
    if (timezone == kUndefined) {
      return;
    }
    if (timezone == 0) {
      sb.append('Z');
      return;
    }
    int t = timezone;
    if (t < 0) {
      sb.append('-');
      t = -t;
    } else {
      sb.append('+');
    }
    two(sb, t / 60);
    sb.append(':');
    two(sb, t % 60);
  }

  private static void two(StringBuilder sb, int v) {
    if (v < 10) {
      sb.append('0');
    }
    sb.append(v);
  }

  static boolean isLeapYear(BigInteger y) {
    return y.mod(BigInteger.valueOf(400)).signum() == 0
        || (y.mod(BigInteger.valueOf(100)).signum() != 0 && y.mod(BigInteger.valueOf(4)).signum() == 0);
  }

  static int maximumDayInMonthFor(BigInteger y, int m) {
    switch (m) {
      case 2:
        return y != null && !isLeapYear(y) ? 28 : 29;
      case 4:
      case 6:
      case 9:
      case 11:
        return 30;
      default:
        return 31;
    }
  }

  @Override
  public boolean isValid() {
    if (month != kUndefined && day != kUndefined) {
      int max = maximumDayInMonthFor(getEonAndYear(), month);
      if (day > max) {
        return false;
      }
    }
    if (hour == 24 && (minute != 0 || second != 0)) {
      return false;
    }
    if (year != kUndefined) {
      BigInteger y = getEonAndYear();
      if (y.signum() == 0) {
        return false;
      }
    }
    return true;
  }

  private static BigInteger sanitize(Number value, int signum) {
    if (value == null || signum == 0) {
      return BigInteger.ZERO;
    }
    BigInteger v = (BigInteger) value;
    return signum > 0 ? v : v.negate();
  }

  private static BigDecimal sanitizeDecimal(Number value, int signum) {
    if (value == null || signum == 0) {
      return BigDecimal.ZERO;
    }
    BigDecimal v = (BigDecimal) value;
    return signum > 0 ? v : v.negate();
  }

  private static BigInteger floorDiv(BigInteger a, BigInteger b) {
    BigInteger[] qr = a.divideAndRemainder(b);
    return qr[1].signum() < 0 ? qr[0].subtract(BigInteger.ONE) : qr[0];
  }

  @Override
  public void add(Duration duration) {
    boolean[] undefinedFields = new boolean[6];
    int signum = duration.getSign();
    int startMonth = getMonth();
    if (startMonth == kUndefined) {
      startMonth = DatatypeConstants.JANUARY;
      undefinedFields[1] = true;
    }
    BigInteger dMonths = sanitize(duration.getField(DatatypeConstants.MONTHS), signum);
    BigInteger temp = BigInteger.valueOf(startMonth).add(dMonths);
    setMonth(temp.subtract(BigInteger.ONE).mod(kTwelve).intValue() + 1);
    BigInteger carry = floorDiv(temp.subtract(BigInteger.ONE), kTwelve);
    BigInteger startYear = getEonAndYear();
    if (startYear == null) {
      undefinedFields[0] = true;
      startYear = BigInteger.ZERO;
    }
    BigInteger dYears = sanitize(duration.getField(DatatypeConstants.YEARS), signum);
    setYear(startYear.add(dYears).add(carry));
    BigDecimal startSeconds;
    if (getSecond() == kUndefined) {
      undefinedFields[5] = true;
      startSeconds = BigDecimal.ZERO;
    } else {
      startSeconds = BigDecimal.ZERO.add(BigDecimal.valueOf(getSecond()));
    }
    if (getFractionalSecond() != null) {
      startSeconds = startSeconds.add(getFractionalSecond());
    }
    BigDecimal dSeconds = sanitizeDecimal(duration.getField(DatatypeConstants.SECONDS), signum);
    BigDecimal tempBd = startSeconds.add(dSeconds);
    BigDecimal quotient = new BigDecimal(tempBd.toBigInteger()).divide(BigDecimal.valueOf(60), 0,
        RoundingMode.FLOOR);
    BigDecimal endSeconds = tempBd.subtract(quotient.multiply(BigDecimal.valueOf(60)));
    carry = quotient.toBigInteger();
    setSecond(endSeconds.intValue());
    BigDecimal fraction = endSeconds.subtract(new BigDecimal(BigInteger.valueOf(getSecond())));
    if (fraction.compareTo(BigDecimal.ZERO) < 0) {
      setFractionalSecond(BigDecimal.ONE.add(fraction));
      if (getSecond() == 0) {
        setSecond(59);
        carry = carry.subtract(BigInteger.ONE);
      } else {
        setSecond(getSecond() - 1);
      }
    } else {
      setFractionalSecond(fraction);
    }
    int startMinutes = getMinute();
    if (startMinutes == kUndefined) {
      undefinedFields[4] = true;
      startMinutes = 0;
    }
    temp = BigInteger.valueOf(startMinutes).add(sanitize(duration.getField(DatatypeConstants.MINUTES), signum))
        .add(carry);
    setMinute(temp.mod(kSixty).intValue());
    carry = floorDiv(temp, kSixty);
    int startHours = getHour();
    if (startHours == kUndefined) {
      undefinedFields[3] = true;
      startHours = 0;
    }
    temp = BigInteger.valueOf(startHours).add(sanitize(duration.getField(DatatypeConstants.HOURS), signum))
        .add(carry);
    setHour(temp.mod(kTwentyFour).intValue(), false);
    carry = floorDiv(temp, kTwentyFour);
    int startDay = getDay();
    if (startDay == kUndefined) {
      undefinedFields[2] = true;
      startDay = 1;
    }
    BigInteger dDays = sanitize(duration.getField(DatatypeConstants.DAYS), signum);
    int maxDay = maximumDayInMonthFor(getEonAndYear(), getMonth());
    BigInteger tempDays;
    if (startDay > maxDay) {
      tempDays = BigInteger.valueOf(maxDay);
    } else if (startDay < 1) {
      tempDays = BigInteger.ONE;
    } else {
      tempDays = BigInteger.valueOf(startDay);
    }
    BigInteger endDays = tempDays.add(dDays).add(carry);
    while (true) {
      int monthCarry;
      if (endDays.compareTo(BigInteger.ONE) < 0) {
        int previous;
        if (getMonth() >= 2) {
          previous = maximumDayInMonthFor(getEonAndYear(), getMonth() - 1);
        } else {
          previous = maximumDayInMonthFor(getEonAndYear().subtract(BigInteger.ONE), 12);
        }
        endDays = endDays.add(BigInteger.valueOf(previous));
        monthCarry = -1;
      } else if (endDays.compareTo(BigInteger.valueOf(maximumDayInMonthFor(getEonAndYear(), getMonth()))) > 0) {
        endDays = endDays.add(BigInteger.valueOf(-maximumDayInMonthFor(getEonAndYear(), getMonth())));
        monthCarry = 1;
      } else {
        break;
      }
      int sum = getMonth() + monthCarry;
      int endMonth = (sum - 1) % 12;
      int yearCarry;
      if (endMonth < 0) {
        endMonth = 12 + endMonth + 1;
        yearCarry = Math.floorDiv(sum - 1, 12);
      } else {
        yearCarry = (sum - 1) / 12;
        endMonth += 1;
      }
      setMonth(endMonth);
      if (yearCarry != 0) {
        setYear(getEonAndYear().add(BigInteger.valueOf(yearCarry)));
      }
    }
    setDay(endDays.intValue());
    if (undefinedFields[0]) {
      setYear(kUndefined);
    }
    if (undefinedFields[1]) {
      setMonth(kUndefined);
    }
    if (undefinedFields[2]) {
      setDay(kUndefined);
    }
    if (undefinedFields[3]) {
      setHour(kUndefined, false);
    }
    if (undefinedFields[4]) {
      setMinute(kUndefined);
    }
    if (undefinedFields[5]) {
      setSecond(kUndefined);
      setFractionalSecond(null);
    }
  }

  private static GregorianCalendar newCalendar(TimeZone tz, Locale locale) {
    GregorianCalendar result = new GregorianCalendar(tz, locale);
    result.clear();
    result.setGregorianChange(new Date(Long.MIN_VALUE));
    return result;
  }

  @Override
  public GregorianCalendar toGregorianCalendar() {
    return buildCalendar(getTimeZone(kUndefined), Locale.getDefault(Locale.Category.FORMAT), null);
  }

  @Override
  public GregorianCalendar toGregorianCalendar(TimeZone tz, Locale aLocale, XMLGregorianCalendar defaults) {
    TimeZone zone = tz;
    if (zone == null) {
      int defaultZoneOffset = kUndefined;
      if (defaults != null) {
        defaultZoneOffset = defaults.getTimezone();
      }
      zone = getTimeZone(defaultZoneOffset);
    }
    Locale locale = aLocale == null ? Locale.getDefault(Locale.Category.FORMAT) : aLocale;
    return buildCalendar(zone, locale, defaults);
  }

  private GregorianCalendar buildCalendar(TimeZone zone, Locale locale, XMLGregorianCalendar defaults) {
    GregorianCalendar result = newCalendar(zone, locale);
    BigInteger y = getEonAndYear();
    if (y == null && defaults != null) {
      y = defaults.getEonAndYear();
    }
    if (y != null) {
      result.set(java.util.Calendar.ERA, y.signum() == -1 ? GregorianCalendar.BC : GregorianCalendar.AD);
      result.set(java.util.Calendar.YEAR, y.abs().intValue());
    }
    int m = month != kUndefined ? month : defaults != null ? defaults.getMonth() : kUndefined;
    if (m != kUndefined) {
      result.set(java.util.Calendar.MONTH, m - 1);
    }
    int d = day != kUndefined ? day : defaults != null ? defaults.getDay() : kUndefined;
    if (d != kUndefined) {
      result.set(java.util.Calendar.DAY_OF_MONTH, d);
    }
    int h = hour != kUndefined ? hour : defaults != null ? defaults.getHour() : kUndefined;
    if (h != kUndefined) {
      result.set(java.util.Calendar.HOUR_OF_DAY, h);
    }
    int mi = minute != kUndefined ? minute : defaults != null ? defaults.getMinute() : kUndefined;
    if (mi != kUndefined) {
      result.set(java.util.Calendar.MINUTE, mi);
    }
    int s = second != kUndefined ? second : defaults != null ? defaults.getSecond() : kUndefined;
    if (s != kUndefined) {
      result.set(java.util.Calendar.SECOND, s);
    }
    if (fractionalSecond != null) {
      result.set(java.util.Calendar.MILLISECOND, getMillisecond());
    } else if (defaults != null && defaults.getFractionalSecond() != null) {
      result.set(java.util.Calendar.MILLISECOND, defaults.getMillisecond());
    }
    return result;
  }

  @Override
  public TimeZone getTimeZone(int defaultZoneOffset) {
    int offset = getTimezone();
    if (offset == kUndefined) {
      offset = defaultZoneOffset;
    }
    if (offset == kUndefined) {
      return TimeZone.getDefault();
    }
    char sign = offset < 0 ? '-' : '+';
    if (offset < 0) {
      offset = -offset;
    }
    int hh = offset / 60;
    int mm = offset % 60;
    return TimeZone.getTimeZone("GMT" + sign + (hh < 10 ? "0" : "") + hh + ':' + (mm < 10 ? "0" : "") + mm);
  }

  @Override
  public Object clone() {
    XMLGregorianCalendarImpl c = new XMLGregorianCalendarImpl();
    c.eon = eon;
    c.year = year;
    c.month = month;
    c.day = day;
    c.timezone = timezone;
    c.hour = hour;
    c.minute = minute;
    c.second = second;
    c.fractionalSecond = fractionalSecond;
    c.origEon = origEon;
    c.origYear = origYear;
    c.origMonth = origMonth;
    c.origDay = origDay;
    c.origTimezone = origTimezone;
    c.origHour = origHour;
    c.origMinute = origMinute;
    c.origSecond = origSecond;
    c.origFractionalSecond = origFractionalSecond;
    return c;
  }

  private static final class LexicalParser {
    private final String value;
    private int index;

    LexicalParser(String value) {
      if (value == null) {
        throw new NullPointerException("Cannot invoke \"String.length()\" because \"lexRep\" is null");
      }
      this.value = value;
    }

    private IllegalArgumentException bad() {
      return new IllegalArgumentException(value);
    }

    void parseInto(XMLGregorianCalendarImpl c) {
      String format;
      int length = value.length();
      if (value.indexOf('T') != -1) {
        format = "%Y-%M-%DT%h:%m:%s%z";
      } else if (length >= 3 && value.charAt(2) == ':') {
        format = "%h:%m:%s%z";
      } else if (value.startsWith("--")) {
        if (length >= 3 && value.charAt(2) == '-') {
          format = "---%D%z";
        } else if (length == 4 || (length == 5 && value.charAt(4) == 'Z')
            || (length == 10 && (value.charAt(4) == '+' || value.charAt(4) == '-') && value.charAt(7) == ':')) {
          format = "--%M%z";
        } else {
          format = "--%M-%D%z";
        }
      } else {
        int separators = 0;
        int scanLength = length;
        if (value.indexOf(':') != -1) {
          scanLength -= 6;
        }
        for (int i = 1; i < scanLength; i++) {
          if (value.charAt(i) == '-') {
            separators++;
          }
        }
        if (separators == 0) {
          format = "%Y%z";
        } else if (separators == 1) {
          format = "%Y-%M%z";
        } else {
          format = "%Y-%M-%D%z";
        }
      }
      run(format, c);
      if (index != length) {
        throw bad();
      }
      c.testHour();
      if (!c.isValid()) {
        throw new IllegalArgumentException(DatatypeMessages.invalidLexical(value));
      }
    }

    private void run(String format, XMLGregorianCalendarImpl c) {
      int f = 0;
      while (f < format.length()) {
        char ch = format.charAt(f++);
        if (ch != '%') {
          expect(ch);
          continue;
        }
        char directive = format.charAt(f++);
        switch (directive) {
          case 'Y':
            c.setYear(parseYear());
            break;
          case 'M':
            c.setMonth(parseTwo());
            break;
          case 'D':
            c.setDay(parseTwo());
            break;
          case 'h':
            c.setHour(parseTwo(), false);
            break;
          case 'm':
            c.setMinute(parseTwo());
            break;
          case 's':
            c.setSecond(parseTwo());
            if (index < value.length() && value.charAt(index) == '.') {
              index++;
              int start = index;
              while (index < value.length() && Character.isDigit(value.charAt(index))
                  && value.charAt(index) < 128) {
                index++;
              }
              if (index == start) {
                throw new NumberFormatException("No digits found.");
              }
              c.setFractionalSecond(new BigDecimal("0." + value.substring(start, index)));
            }
            break;
          default:
            parseTimezone(c);
            break;
        }
      }
    }

    private void expect(char ch) {
      if (index >= value.length() || value.charAt(index) != ch) {
        throw bad();
      }
      index++;
    }

    private static boolean digit(char ch) {
      return ch >= '0' && ch <= '9';
    }

    private int parseTwo() {
      if (index + 2 > value.length() || !digit(value.charAt(index)) || !digit(value.charAt(index + 1))) {
        throw bad();
      }
      int v = (value.charAt(index) - '0') * 10 + (value.charAt(index + 1) - '0');
      index += 2;
      return v;
    }

    private BigInteger parseYear() {
      boolean negative = false;
      if (index < value.length() && value.charAt(index) == '-') {
        negative = true;
        index++;
      }
      int start = index;
      while (index < value.length() && digit(value.charAt(index))) {
        index++;
      }
      if (index - start < 4) {
        throw bad();
      }
      BigInteger y = new BigInteger(value.substring(start, index));
      return negative ? y.negate() : y;
    }

    private void parseTimezone(XMLGregorianCalendarImpl c) {
      if (index >= value.length()) {
        return;
      }
      char ch = value.charAt(index);
      if (ch == 'Z') {
        index++;
        c.setTimezone(0);
        return;
      }
      if (ch != '+' && ch != '-') {
        throw bad();
      }
      index++;
      int hh = parseTwo();
      expect(':');
      int mm = parseTwo();
      int minutes = hh * 60 + mm;
      c.setTimezone(ch == '-' ? -minutes : minutes);
    }
  }
}
