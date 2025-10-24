/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.datatype;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.GregorianCalendar;
import shim.xml.FactoryFinder;

public abstract class DatatypeFactory {
  public static final String DATATYPEFACTORY_PROPERTY = "javax.xml.datatype.DatatypeFactory";
  public static final String DATATYPEFACTORY_IMPLEMENTATION_CLASS = "shim.xml.datatype.DatatypeFactoryImpl";

  protected DatatypeFactory() {
  }

  public static DatatypeFactory newDefaultInstance() throws DatatypeConfigurationException {
    try {
      return FactoryFinder.instantiate(DatatypeFactory.class, DATATYPEFACTORY_IMPLEMENTATION_CLASS,
          DatatypeFactory.class.getClassLoader());
    } catch (FactoryFinder.FactoryError e) {
      throw new DatatypeConfigurationException(e.getMessage(), e.getCause());
    }
  }

  public static DatatypeFactory newInstance() throws DatatypeConfigurationException {
    try {
      return FactoryFinder.find(DatatypeFactory.class, DATATYPEFACTORY_IMPLEMENTATION_CLASS, null);
    } catch (FactoryFinder.FactoryError e) {
      throw new DatatypeConfigurationException(e.getMessage(), e.getCause());
    }
  }

  public static DatatypeFactory newInstance(String factoryClassName, ClassLoader classLoader)
      throws DatatypeConfigurationException {
    if (factoryClassName == null) {
      throw new DatatypeConfigurationException(
          "Provider null could not be instantiated: java.lang.NullPointerException");
    }
    try {
      return FactoryFinder.instantiate(DatatypeFactory.class, factoryClassName, classLoader);
    } catch (FactoryFinder.FactoryError e) {
      throw new DatatypeConfigurationException(e.getMessage(), e.getCause());
    }
  }

  public abstract Duration newDuration(String lexicalRepresentation);

  public abstract Duration newDuration(long durationInMilliSeconds);

  public abstract Duration newDuration(boolean isPositive, BigInteger years, BigInteger months, BigInteger days,
      BigInteger hours, BigInteger minutes, BigDecimal seconds);

  public Duration newDuration(boolean isPositive, int years, int months, int days, int hours, int minutes,
      int seconds) {
    BigInteger realYears = years != DatatypeConstants.FIELD_UNDEFINED ? BigInteger.valueOf((long) years) : null;
    BigInteger realMonths = months != DatatypeConstants.FIELD_UNDEFINED ? BigInteger.valueOf((long) months) : null;
    BigInteger realDays = days != DatatypeConstants.FIELD_UNDEFINED ? BigInteger.valueOf((long) days) : null;
    BigInteger realHours = hours != DatatypeConstants.FIELD_UNDEFINED ? BigInteger.valueOf((long) hours) : null;
    BigInteger realMinutes = minutes != DatatypeConstants.FIELD_UNDEFINED ? BigInteger.valueOf((long) minutes) : null;
    BigDecimal realSeconds = seconds != DatatypeConstants.FIELD_UNDEFINED ? BigDecimal.valueOf((long) seconds) : null;
    return newDuration(isPositive, realYears, realMonths, realDays, realHours, realMinutes, realSeconds);
  }

  public Duration newDurationDayTime(String lexicalRepresentation) {
    if (lexicalRepresentation == null) {
      throw new NullPointerException("The lexical representation cannot be null.");
    }
    int pos = lexicalRepresentation.indexOf('T');
    int length = lexicalRepresentation.length();
    if (pos >= 0) {
      for (int i = 0; i < pos; ++i) {
        char c = lexicalRepresentation.charAt(i);
        if (c == 'Y' || c == 'M') {
          throw new IllegalArgumentException("Invalid dayTimeDuration value: " + lexicalRepresentation);
        }
      }
    } else {
      for (int i = 0; i < length; ++i) {
        char c = lexicalRepresentation.charAt(i);
        if (c == 'Y' || c == 'M') {
          throw new IllegalArgumentException("Invalid dayTimeDuration value: " + lexicalRepresentation);
        }
      }
    }
    return newDuration(lexicalRepresentation);
  }

  public Duration newDurationDayTime(long durationInMilliseconds) {
    Duration d = newDuration(durationInMilliseconds);
    return newDuration(d.getSign() >= 0, null, null, (BigInteger) d.getField(DatatypeConstants.DAYS),
        (BigInteger) d.getField(DatatypeConstants.HOURS), (BigInteger) d.getField(DatatypeConstants.MINUTES),
        (BigDecimal) d.getField(DatatypeConstants.SECONDS));
  }

  public Duration newDurationDayTime(boolean isPositive, BigInteger day, BigInteger hour, BigInteger minute,
      BigInteger second) {
    return newDuration(isPositive, null, null, day, hour, minute, second != null ? new BigDecimal(second) : null);
  }

  public Duration newDurationDayTime(boolean isPositive, int day, int hour, int minute, int second) {
    return newDurationDayTime(isPositive, BigInteger.valueOf((long) day), BigInteger.valueOf((long) hour),
        BigInteger.valueOf((long) minute), BigInteger.valueOf((long) second));
  }

  public Duration newDurationYearMonth(String lexicalRepresentation) {
    if (lexicalRepresentation == null) {
      throw new NullPointerException("The lexical representation cannot be null.");
    }
    int length = lexicalRepresentation.length();
    char lastChar = lexicalRepresentation.charAt(length - 1);
    if (lastChar != 'Y' && lastChar != 'M') {
      throw new IllegalArgumentException("Invalid yearMonthDuration value: " + lexicalRepresentation);
    }
    return newDuration(lexicalRepresentation);
  }

  public Duration newDurationYearMonth(long durationInMilliseconds) {
    Duration d = newDuration(durationInMilliseconds);
    return newDuration(d.getSign() >= 0, (BigInteger) d.getField(DatatypeConstants.YEARS),
        (BigInteger) d.getField(DatatypeConstants.MONTHS), null, null, null, null);
  }

  public Duration newDurationYearMonth(boolean isPositive, BigInteger year, BigInteger month) {
    return newDuration(isPositive, year, month, null, null, null, null);
  }

  public Duration newDurationYearMonth(boolean isPositive, int year, int month) {
    return newDurationYearMonth(isPositive, BigInteger.valueOf((long) year), BigInteger.valueOf((long) month));
  }

  public abstract XMLGregorianCalendar newXMLGregorianCalendar();

  public abstract XMLGregorianCalendar newXMLGregorianCalendar(String lexicalRepresentation);

  public abstract XMLGregorianCalendar newXMLGregorianCalendar(GregorianCalendar cal);

  public abstract XMLGregorianCalendar newXMLGregorianCalendar(BigInteger year, int month, int day, int hour,
      int minute, int second, BigDecimal fractionalSecond, int timezone);

  public XMLGregorianCalendar newXMLGregorianCalendar(int year, int month, int day, int hour, int minute,
      int second, int millisecond, int timezone) {
    BigInteger realYear = year != DatatypeConstants.FIELD_UNDEFINED ? BigInteger.valueOf((long) year) : null;
    BigDecimal realMillisecond = null;
    if (millisecond != DatatypeConstants.FIELD_UNDEFINED) {
      if (millisecond < 0 || millisecond > 1000) {
        throw new IllegalArgumentException(
            "javax.xml.datatype.DatatypeFactory#newXMLGregorianCalendar(int year, int month, int day, int hour, "
            + "int minute, int second, int millisecond, int timezone)with invalid millisecond: " + millisecond);
      }
      realMillisecond = BigDecimal.valueOf((long) millisecond).movePointLeft(3);
    }
    return newXMLGregorianCalendar(realYear, month, day, hour, minute, second, realMillisecond, timezone);
  }

  public XMLGregorianCalendar newXMLGregorianCalendarDate(int year, int month, int day, int timezone) {
    return newXMLGregorianCalendar(year, month, day, DatatypeConstants.FIELD_UNDEFINED,
        DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED,
        timezone);
  }

  public XMLGregorianCalendar newXMLGregorianCalendarTime(int hours, int minutes, int seconds, int timezone) {
    return newXMLGregorianCalendar(DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED,
        DatatypeConstants.FIELD_UNDEFINED, hours, minutes, seconds, DatatypeConstants.FIELD_UNDEFINED, timezone);
  }

  public XMLGregorianCalendar newXMLGregorianCalendarTime(int hours, int minutes, int seconds,
      BigDecimal fractionalSecond, int timezone) {
    return newXMLGregorianCalendar(null, DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED,
        hours, minutes, seconds, fractionalSecond, timezone);
  }

  public XMLGregorianCalendar newXMLGregorianCalendarTime(int hours, int minutes, int seconds, int milliseconds,
      int timezone) {
    BigDecimal realMilliseconds = null;
    if (milliseconds != DatatypeConstants.FIELD_UNDEFINED) {
      if (milliseconds < 0 || milliseconds > 1000) {
        throw new IllegalArgumentException(
            "javax.xml.datatype.DatatypeFactory#newXMLGregorianCalendarTime(int hours, int minutes, int seconds, "
            + "int milliseconds, int timezone)with invalid milliseconds: " + milliseconds);
      }
      realMilliseconds = BigDecimal.valueOf((long) milliseconds).movePointLeft(3);
    }
    return newXMLGregorianCalendarTime(hours, minutes, seconds, realMilliseconds, timezone);
  }
}
