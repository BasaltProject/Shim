/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.datatype;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

public final class DatatypeFactoryImpl extends DatatypeFactory {
  public Duration newDuration(String lexicalRepresentation) {
    return new DurationImpl(lexicalRepresentation);
  }

  public Duration newDuration(long durationInMilliSeconds) {
    return new DurationImpl(durationInMilliSeconds);
  }

  public Duration newDuration(boolean isPositive, BigInteger years, BigInteger months, BigInteger days,
      BigInteger hours, BigInteger minutes, BigDecimal seconds) {
    return new DurationImpl(isPositive, years, months, days, hours, minutes, seconds);
  }

  @Override
  public Duration newDurationDayTime(String lexicalRepresentation) {
    if (lexicalRepresentation == null) {
      throw new NullPointerException("Trying to create an xdt:dayTimeDuration with an invalid lexical "
          + "representation of \"null\"");
    }
    Duration d = newDuration(lexicalRepresentation);
    if (d.isSet(javax.xml.datatype.DatatypeConstants.YEARS) || d.isSet(javax.xml.datatype.DatatypeConstants.MONTHS)) {
      throw new IllegalArgumentException("Trying to create an xdt:dayTimeDuration with an invalid lexical "
          + "representation of \"" + lexicalRepresentation + "\", data model requires a format PnDTnHnMnS.");
    }
    return d;
  }

  @Override
  public Duration newDurationYearMonth(String lexicalRepresentation) {
    Duration d = newDuration(lexicalRepresentation);
    if (d.isSet(javax.xml.datatype.DatatypeConstants.DAYS) || d.isSet(javax.xml.datatype.DatatypeConstants.HOURS)
        || d.isSet(javax.xml.datatype.DatatypeConstants.MINUTES)
        || d.isSet(javax.xml.datatype.DatatypeConstants.SECONDS)) {
      throw new IllegalArgumentException("Trying to create an xdt:yearMonthDuration with an invalid lexical "
          + "representation of \"" + lexicalRepresentation + "\", data model requires PnYnM.");
    }
    return d;
  }

  public XMLGregorianCalendar newXMLGregorianCalendar() {
    return new XMLGregorianCalendarImpl();
  }

  public XMLGregorianCalendar newXMLGregorianCalendar(String lexicalRepresentation) {
    return new XMLGregorianCalendarImpl(lexicalRepresentation);
  }

  public XMLGregorianCalendar newXMLGregorianCalendar(GregorianCalendar cal) {
    return new XMLGregorianCalendarImpl(cal);
  }

  public XMLGregorianCalendar newXMLGregorianCalendar(BigInteger year, int month, int day, int hour, int minute,
      int second, BigDecimal fractionalSecond, int timezone) {
    return new XMLGregorianCalendarImpl(year, month, day, hour, minute, second, fractionalSecond, timezone);
  }
}
