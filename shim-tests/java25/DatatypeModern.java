/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

public class DatatypeModern {
static class D1 {
  static String s(Object o) { return String.valueOf(o); }
  static String fail(Throwable t) { return "!" + t.getClass().getName() + ":" + t.getMessage(); }
  public static void run() throws Exception {
    DatatypeFactory f = DatatypeFactory.newInstance();
    String[] lex = {"P1Y2M3DT4H5M6S", "P1Y2M3DT4H5M6.789S", "-P1Y", "PT0S", "P0D", "P1.5Y", "P-1Y", "PT1.5S", "P1M", "PT1M", "P1DT", "P", "PT", "", "p1y", "P1Y1Y", " P1Y ", "P1W", "P99999999999999999999Y", "PT.5S", "PT5.S", "P1Y2D", "PT1H1S", "P1MT1M", "-PT0.000S", "PT00001S", "PT1.2345678901234567890S", "P0Y", "P0001Y", "+P1Y", "P1Y-", "P1DT1H1M1S1", "P1D1Y", "PT1S1M", "P1YT", "P2M3Y", "P10675199DT2H48M5.477S", "PT0.0S", "PT100S", "P12M", "P1Y2M3DT", "PT1H.5S", "-P0Y", "-PT0S", "P1,5Y", "PT1,5S", "P1Y2M3DT4H5M6S ", "\tP1Y", "P 1Y", "P1 Y", "Pnull"};
    for (String l : lex) {
      StringBuilder sb = new StringBuilder("[" + l + "] ");
      try {
        Duration d = f.newDuration(l);
        sb.append(d).append(" sign=").append(d.getSign());
        sb.append(" y=" + d.getYears() + " m=" + d.getMonths() + " d=" + d.getDays() + " h=" + d.getHours() + " mi=" + d.getMinutes() + " s=" + d.getSeconds());
        sb.append(" set=");
        for (DatatypeConstants.Field fl : new DatatypeConstants.Field[]{DatatypeConstants.YEARS, DatatypeConstants.MONTHS, DatatypeConstants.DAYS, DatatypeConstants.HOURS, DatatypeConstants.MINUTES, DatatypeConstants.SECONDS}) sb.append(d.isSet(fl) ? '1' : '0');
        sb.append(" f=" + s(d.getField(DatatypeConstants.SECONDS)) + "/" + s(d.getField(DatatypeConstants.YEARS)));
        try { sb.append(" type=" + d.getXMLSchemaType()); } catch (Throwable t) { sb.append(" type=" + fail(t)); }
        sb.append(" hash=" + d.hashCode() + " neg=" + d.negate() + " x2=" + d.multiply(2) + " x-1=" + d.multiply(-1) + " x0.5=" + d.multiply(new BigDecimal("0.5")));
      } catch (Throwable t) { sb.append(fail(t)); }
      System.out.println(sb);
    }
    System.out.println("--- factory");
    Object[][] cs = {
      {1, 2, 3, 4, 5, 6}, {0, 0, 0, 0, 0, 0}, {1, Integer.MIN_VALUE, Integer.MIN_VALUE, 1, Integer.MIN_VALUE, 1}, {1, -1, 0, 0, 0, 0}, {0, 1, 1, 1, 1, 1, 1},
    };
    for (boolean pos : new boolean[]{true, false}) {
      int[][] ints = {{1,2,3,4,5,6}, {0,0,0,0,0,0}, {1,Integer.MIN_VALUE,Integer.MIN_VALUE,1,Integer.MIN_VALUE,1}, {Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE}, {-1,0,0,0,0,0}, {0,0,0,0,0,-5}, {Integer.MIN_VALUE,Integer.MIN_VALUE,2,3,4,5}, {1,2,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE}};
      for (int[] v : ints) { try { Duration d = f.newDuration(pos, v[0], v[1], v[2], v[3], v[4], v[5]); System.out.println("newDuration(" + pos + "," + Arrays.toString(v) + ")=" + d + " sign=" + d.getSign()); } catch (Throwable t) { System.out.println("newDuration(" + pos + "," + Arrays.toString(v) + ")" + fail(t)); } }
    }
    try { f.newDuration((String) null); } catch (Throwable t) { System.out.println("null lex" + fail(t)); }
    try { f.newDuration(true, (BigInteger) null, null, null, null, null, (BigDecimal) null); } catch (Throwable t) { System.out.println("all null" + fail(t)); }
    long[] ms = {0, 1, 999, 1000, 61000, 3600000, 86400000, 90061001L, -1, -90061001L, Long.MAX_VALUE, Long.MIN_VALUE, 31536000000L, 2678400000L, 123456789012L};
    for (long m : ms) { try { System.out.println("ms " + m + " -> " + f.newDuration(m) + " | " + f.newDurationDayTime(m) + " | " + f.newDurationYearMonth(m)); } catch (Throwable t) { System.out.println("ms " + m + fail(t)); } }
    String[] dt = {"P1D", "PT1H", "P1Y", "P1M", "PT1M", "P1DT1H", "-P1DT1S", "P1Y1D", "PT", "", "P1YT1H", "P1MT1H", "PT1M1Y"};
    for (String l : dt) { try { System.out.println("dayTime [" + l + "] " + f.newDurationDayTime(l)); } catch (Throwable t) { System.out.println("dayTime [" + l + "]" + fail(t)); } try { System.out.println("yearMonth [" + l + "] " + f.newDurationYearMonth(l)); } catch (Throwable t) { System.out.println("yearMonth [" + l + "]" + fail(t)); } }
    try { f.newDurationDayTime((String) null); } catch (Throwable t) { System.out.println("dt null" + fail(t)); }
    try { f.newDurationYearMonth((String) null); } catch (Throwable t) { System.out.println("ym null" + fail(t)); }
    try { System.out.println(f.newDurationDayTime(true, 1, 2, 3, 4) + " " + f.newDurationDayTime(false, BigInteger.ONE, BigInteger.ZERO, BigInteger.TEN, BigInteger.TWO) + " " + f.newDurationYearMonth(true, 1, 2) + " " + f.newDurationYearMonth(false, BigInteger.ONE, BigInteger.TEN)); } catch (Throwable t) { System.out.println("typed" + fail(t)); }
    try { System.out.println(f.newDurationDayTime(true, null, null, null, null)); } catch (Throwable t) { System.out.println("typed null" + fail(t)); }
    try { System.out.println(f.newDurationYearMonth(true, null, null)); } catch (Throwable t) { System.out.println("ym null2" + fail(t)); }
    try { System.out.println(f.newDurationDayTime(true, null, BigInteger.ONE, null, null)); } catch (Throwable t) { System.out.println("typed null2" + fail(t)); }
    try { System.out.println(f.newDuration(true, BigInteger.valueOf(-1), null, null, null, null, null)); } catch (Throwable t) { System.out.println("neg field" + fail(t)); }
    try { System.out.println(f.newDuration(true, null, null, null, null, null, new BigDecimal("-1"))); } catch (Throwable t) { System.out.println("neg sec" + fail(t)); }
    try { System.out.println(f.newDuration(true, null, null, null, null, null, new BigDecimal("1.50"))); } catch (Throwable t) { System.out.println("sec scale" + fail(t)); }
    try { System.out.println(f.newDuration(true, null, null, null, null, null, new BigDecimal("0.00"))); } catch (Throwable t) { System.out.println("sec zero" + fail(t)); }
    try { System.out.println(f.newDuration(false, null, null, null, null, null, new BigDecimal("0"))); } catch (Throwable t) { System.out.println("neg zero" + fail(t)); }
    System.out.println("--- ops");
    String[] ops = {"P1Y", "P12M", "P1M", "P30D", "P31D", "P365D", "P366D", "PT1H", "PT60M", "PT3600S", "P1DT1H", "P1D", "PT24H", "P1Y1M", "-P1M", "PT0S", "P0Y", "P1MT1S", "P29D", "P5M", "P153D", "P1Y1D", "PT1.5S", "PT1.50S"};
    for (String x : ops) for (String y : ops) {
      StringBuilder sb = new StringBuilder(x + " vs " + y + " ");
      try { Duration dx = f.newDuration(x), dy = f.newDuration(y); sb.append("cmp=" + dx.compare(dy) + " eq=" + dx.equals(dy) + " lt=" + dx.isShorterThan(dy) + " gt=" + dx.isLongerThan(dy) + " hash=" + (dx.hashCode() == dy.hashCode())); } catch (Throwable t) { sb.append(fail(t)); }
      try { Duration dx = f.newDuration(x), dy = f.newDuration(y); sb.append(" add=" + dx.add(dy) + " sub=" + dx.subtract(dy)); } catch (Throwable t) { sb.append(" add" + fail(t)); }
      System.out.println(sb);
    }
  } }

static class G1 {
  static String fail(Throwable t) { return "!" + t.getClass().getName() + ":" + t.getMessage(); }
  static String desc(XMLGregorianCalendar c) {
    StringBuilder sb = new StringBuilder();
    sb.append("Y=" + c.getYear() + " eon=" + c.getEon() + " eY=" + c.getEonAndYear() + " M=" + c.getMonth() + " D=" + c.getDay() + " tz=" + c.getTimezone() + " h=" + c.getHour() + " mi=" + c.getMinute() + " s=" + c.getSecond() + " ms=" + c.getMillisecond() + " fs=" + c.getFractionalSecond());
    sb.append(" xml=" + c.toXMLFormat());
    try { sb.append(" type=" + c.getXMLSchemaType().getLocalPart()); } catch (Throwable t) { sb.append(" type=" + fail(t)); }
    sb.append(" valid=" + c.isValid() + " hash=" + c.hashCode());
    return sb.toString();
  }
  public static void run() throws Exception {
    DatatypeFactory f = DatatypeFactory.newInstance();
    String[] lex = {"2004-02-29T12:30:45.123+05:30", "2004-02-29T12:30:45Z", "2004-02-29T12:30:45", "2004-02-29", "2004-02-29Z", "2004-02-29-08:00", "12:30:45", "12:30:45.5Z", "12:30:45+14:00", "2004-02", "2004-02Z", "2004", "2004Z", "--02", "--02Z", "--02-29", "--02-29Z", "---29", "---29Z", "-0001-01-01T00:00:00", "12345-06-07T08:09:10", "0001-01-01", "10000-01-01", "-10000-01-01", "2004-13-01", "2004-02-30", "2003-02-29", "2004-00-01", "2004-01-00", "24:00:00", "2004-01-01T24:00:00", "2004-01-01T24:00:01", "12:60:00", "12:30:60", "12:30:61", "12:30:45.", "12:30:45.1234567890123", "2004-1-1", "04-01-01", "2004-01-01T1:00:00", "2004-01-01 12:00:00", " 2004-01-01", "2004-01-01 ", "", "T12:00:00", "2004-01-01T", "2004-01-01T12", "2004-01-01T12:00", "2004-01-01T12:00:00+5:00", "2004-01-01T12:00:00+05:0", "2004-01-01T12:00:00+15:00", "2004-01-01T12:00:00-14:01", "2004-01-01T12:00:00+14:00", "2004-01-01T12:00:00+00:00", "2004-01-01T12:00:00-00:00", "2004-01-01T12:00:00z", "2004-01-01T12:00:00+0500", "2004-01-01T12:00:00+05", "-2004", "+2004", "02004", "0000", "0000-01-01", "-0000-01-01", "99999999999999999999-01-01", "-99999999999999999999-01-01", "2004-02-29T12:30:45.1E1", "---", "--", "--0", "---1", "--1-1", "2004--01", "2004-01-01T12:00:00.", "12:00:00.0", "12:00:00.00Z", "12:00:00.000000", "1999-12-31T23:59:59.999999999Z", "gibberish", "2004-01-01T12:00:00Zx", "2004-01-01TT12:00:00", "-2004-02-29", "+14:00", "Z", "0001-01-01T00:00:00Z", "9999-12-31T23:59:59Z", "2004-12-31T24:00:00", "2004-12-31T24:00:00.000", "2004-12-31T24:00:00.5"};
    for (String l : lex) {
      System.out.println("[" + l + "]");
      try { XMLGregorianCalendar c = f.newXMLGregorianCalendar(l); System.out.println("  " + desc(c)); } catch (Throwable t) { System.out.println("  " + fail(t)); }
    }
    System.out.println("--- null/empty");
    try { f.newXMLGregorianCalendar((String) null); } catch (Throwable t) { System.out.println("null " + fail(t)); }
    try { XMLGregorianCalendar c = f.newXMLGregorianCalendar(); System.out.println("empty " + desc(c)); } catch (Throwable t) { System.out.println("empty " + fail(t)); }
  } }

static class G2 {
  static String fail(Throwable t) { return "!" + t.getClass().getName() + ":" + t.getMessage(); }
  interface Op { void run(XMLGregorianCalendar c) throws Exception; }
  static void t(String name, XMLGregorianCalendar base, Op op) { XMLGregorianCalendar c = (XMLGregorianCalendar) base.clone(); try { op.run(c); System.out.println(name + " => " + show(c)); } catch (Throwable x) { System.out.println(name + " => " + fail(x)); } }
  static String show(XMLGregorianCalendar c) { String type; try { type = c.getXMLSchemaType().getLocalPart(); } catch (Throwable x) { type = "?"; } String x; try { x = c.toXMLFormat(); } catch (Throwable e) { x = fail(e); } return "Y=" + c.getYear() + " eon=" + c.getEon() + " M=" + c.getMonth() + " D=" + c.getDay() + " tz=" + c.getTimezone() + " h=" + c.getHour() + " mi=" + c.getMinute() + " s=" + c.getSecond() + " fs=" + c.getFractionalSecond() + " type=" + type + " valid=" + c.isValid() + " xml=" + x; }
  public static void run() throws Exception {
    DatatypeFactory f = DatatypeFactory.newInstance();
    XMLGregorianCalendar full = f.newXMLGregorianCalendar("2004-02-29T12:30:45.5+01:00");
    XMLGregorianCalendar empty = f.newXMLGregorianCalendar();
    int U = DatatypeConstants.FIELD_UNDEFINED;
    for (final int v : new int[]{U, -1, 0, 1, 12, 13, 24, 25, 31, 32, 59, 60, 61, 840, 841, -841, 1000, 999999999, Integer.MAX_VALUE}) {
      t("setYear " + v, full, new Op() { public void run(XMLGregorianCalendar c) { c.setYear(v); } });
      t("setMonth " + v, full, new Op() { public void run(XMLGregorianCalendar c) { c.setMonth(v); } });
      t("setDay " + v, full, new Op() { public void run(XMLGregorianCalendar c) { c.setDay(v); } });
      t("setHour " + v, full, new Op() { public void run(XMLGregorianCalendar c) { c.setHour(v); } });
      t("setMinute " + v, full, new Op() { public void run(XMLGregorianCalendar c) { c.setMinute(v); } });
      t("setSecond " + v, full, new Op() { public void run(XMLGregorianCalendar c) { c.setSecond(v); } });
      t("setMillisecond " + v, full, new Op() { public void run(XMLGregorianCalendar c) { c.setMillisecond(v); } });
      t("setTimezone " + v, full, new Op() { public void run(XMLGregorianCalendar c) { c.setTimezone(v); } });
      t("E setYear " + v, empty, new Op() { public void run(XMLGregorianCalendar c) { c.setYear(v); } });
      t("E setHour " + v, empty, new Op() { public void run(XMLGregorianCalendar c) { c.setHour(v); } });
      t("E setMillisecond " + v, empty, new Op() { public void run(XMLGregorianCalendar c) { c.setMillisecond(v); } });
    }
    for (final String fs : new String[]{null, "0", "0.5", "1", "1.0", "1.5", "-0.5", "0.123456789012345", "0.000", "2", "00.5"}) {
      t("setFractionalSecond " + fs, full, new Op() { public void run(XMLGregorianCalendar c) { c.setFractionalSecond(fs == null ? null : new BigDecimal(fs)); } });
      t("E setFractionalSecond " + fs, empty, new Op() { public void run(XMLGregorianCalendar c) { c.setFractionalSecond(fs == null ? null : new BigDecimal(fs)); } });
    }
    for (final String y : new String[]{null, "0", "1", "-1", "999999999", "1000000000", "-1000000000", "123456789012345678901234567890", "-123456789012345678901234567890", "1000000001"}) {
      t("setYear(Big) " + y, full, new Op() { public void run(XMLGregorianCalendar c) { c.setYear(y == null ? null : new BigInteger(y)); } });
    }
    t("setTime 3", empty, new Op() { public void run(XMLGregorianCalendar c) { c.setTime(1, 2, 3); } });
    t("setTime 4 frac", empty, new Op() { public void run(XMLGregorianCalendar c) { c.setTime(1, 2, 3, new BigDecimal("0.25")); } });
    t("setTime 4 ms", empty, new Op() { public void run(XMLGregorianCalendar c) { c.setTime(1, 2, 3, 250); } });
    t("setTime bad", empty, new Op() { public void run(XMLGregorianCalendar c) { c.setTime(25, 2, 3); } });
    t("setTime 24", empty, new Op() { public void run(XMLGregorianCalendar c) { c.setTime(24, 0, 0); } });
    t("setTime 24b", empty, new Op() { public void run(XMLGregorianCalendar c) { c.setTime(24, 0, 1); } });
    t("clear", full, new Op() { public void run(XMLGregorianCalendar c) { c.clear(); } });
    t("reset", full, new Op() { public void run(XMLGregorianCalendar c) { c.reset(); } });
    t("reset ms", f.newXMLGregorianCalendar("2004-02-29T12:30:45.5+01:00"), new Op() { public void run(XMLGregorianCalendar c) { c.setMillisecond(7); c.reset(); } });
    t("day feb 30", f.newXMLGregorianCalendar("2004-02-29"), new Op() { public void run(XMLGregorianCalendar c) { c.setDay(30); } });
    t("year to 2003 feb 29", f.newXMLGregorianCalendar("2004-02-29"), new Op() { public void run(XMLGregorianCalendar c) { c.setYear(2003); } });
    t("month to 4 day 31", f.newXMLGregorianCalendar("2004-03-31"), new Op() { public void run(XMLGregorianCalendar c) { c.setMonth(4); } });
    t("hour undefined", full, new Op() { public void run(XMLGregorianCalendar c) { c.setHour(U); } });
    t("hour undef type", full, new Op() { public void run(XMLGregorianCalendar c) { c.setHour(U); c.setMinute(U); c.setSecond(U); c.setFractionalSecond(null); } });
    t("only min", empty, new Op() { public void run(XMLGregorianCalendar c) { c.setMinute(5); } });
    t("only sec", empty, new Op() { public void run(XMLGregorianCalendar c) { c.setSecond(5); } });
    t("h+s", empty, new Op() { public void run(XMLGregorianCalendar c) { c.setHour(1); c.setSecond(5); } });
    t("y+d", empty, new Op() { public void run(XMLGregorianCalendar c) { c.setYear(2000); c.setDay(5); } });
    t("m+d+y", empty, new Op() { public void run(XMLGregorianCalendar c) { c.setYear(2000); c.setMonth(1); c.setDay(5); c.setTimezone(60); } });
    t("tz only", empty, new Op() { public void run(XMLGregorianCalendar c) { c.setTimezone(60); } });
    t("frac only", empty, new Op() { public void run(XMLGregorianCalendar c) { c.setFractionalSecond(new BigDecimal("0.5")); } });
    t("dateTime no tz", empty, new Op() { public void run(XMLGregorianCalendar c) { c.setYear(2000); c.setMonth(1); c.setDay(5); c.setHour(1); c.setMinute(2); c.setSecond(3); } });
    t("year 0", empty, new Op() { public void run(XMLGregorianCalendar c) { c.setYear(0); } });
    t("year 5", empty, new Op() { public void run(XMLGregorianCalendar c) { c.setYear(5); } });
    t("year -5", empty, new Op() { public void run(XMLGregorianCalendar c) { c.setYear(-5); } });
    t("year 12345", empty, new Op() { public void run(XMLGregorianCalendar c) { c.setYear(12345); } });
    t("year huge", empty, new Op() { public void run(XMLGregorianCalendar c) { c.setYear(new BigInteger("-12345678901234567890")); } });
    t("year 1e9", empty, new Op() { public void run(XMLGregorianCalendar c) { c.setYear(new BigInteger("1000000000")); c.setMonth(1); c.setDay(1); } });
    System.out.println("--- factory");
    Object[][] fa = {
      {2004, 2, 29, 12, 30, 45, 500, 60}, {2004, 2, 30, 12, 30, 45, 500, 60}, {U, 2, 29, U, U, U, U, U}, {2004, U, U, U, U, U, U, 0}, {2004, 2, 29, 24, 0, 0, 0, 0}, {2004, 2, 29, 12, 30, 45, 1000, 0}, {2004, 2, 29, 12, 30, 45, 1001, 0}, {2004, 2, 29, 12, 30, 45, -1, 0}, {2004, 13, 1, 0, 0, 0, 0, 0}, {0, 1, 1, 0, 0, 0, 0, 0}, {2004, 1, 1, 0, 0, 0, 0, 841}, {U, U, U, U, U, U, U, U}, {2004, 1, 1, 25, 0, 0, 0, 0}, {-5, 1, 1, U, U, U, U, U}, {2004, U, 1, U, U, U, U, U},
    };
    for (Object[] r : fa) {
      try { XMLGregorianCalendar c = f.newXMLGregorianCalendar((Integer) r[0], (Integer) r[1], (Integer) r[2], (Integer) r[3], (Integer) r[4], (Integer) r[5], (Integer) r[6], (Integer) r[7]); System.out.println("new " + Arrays.toString(r) + " => " + show(c)); } catch (Throwable x) { System.out.println("new " + Arrays.toString(r) + " => " + fail(x)); }
    }
    try { System.out.println(show(f.newXMLGregorianCalendar(new BigInteger("12345678901234567890"), 1, 2, 3, 4, 5, new BigDecimal("0.5"), 0))); } catch (Throwable x) { System.out.println("big " + fail(x)); }
    try { System.out.println(show(f.newXMLGregorianCalendar(new BigInteger("2000"), 1, 2, 3, 4, 5, new BigDecimal("1.5"), 0))); } catch (Throwable x) { System.out.println("frac1.5 " + fail(x)); }
    try { System.out.println(show(f.newXMLGregorianCalendar(new BigInteger("2000"), 1, 2, 3, 4, 5, new BigDecimal("1"), 0))); } catch (Throwable x) { System.out.println("frac1 " + fail(x)); }
    try { System.out.println(show(f.newXMLGregorianCalendar(null, 1, 2, U, U, U, null, U))); } catch (Throwable x) { System.out.println("nullyear " + fail(x)); }
    try { System.out.println(show(f.newXMLGregorianCalendarDate(2004, 2, 29, 60))); System.out.println(show(f.newXMLGregorianCalendarTime(1, 2, 3, 60))); System.out.println(show(f.newXMLGregorianCalendarTime(1, 2, 3, new BigDecimal("0.5"), 60))); System.out.println(show(f.newXMLGregorianCalendarTime(1, 2, 3, 500, 60))); System.out.println(show(f.newXMLGregorianCalendarTime(1, 2, 3, 2000, 60))); } catch (Throwable x) { System.out.println("date/time " + fail(x)); }
  } }

static class G3 {
  static String fail(Throwable t) { return "!" + t.getClass().getName() + ":" + t.getMessage(); }
  static String show(XMLGregorianCalendar c) { String x; try { x = c.toXMLFormat(); } catch (Throwable e) { x = "?"; } return x + "{Y=" + c.getYear() + ",M=" + c.getMonth() + ",D=" + c.getDay() + ",tz=" + c.getTimezone() + ",h=" + c.getHour() + ",mi=" + c.getMinute() + ",s=" + c.getSecond() + ",fs=" + c.getFractionalSecond() + ",eon=" + c.getEon() + "}"; }
  public static void run() throws Exception {
    DatatypeFactory f = DatatypeFactory.newInstance();
    String[] dts = {"2004-02-29T12:30:45.5+01:00", "2004-02-29T12:30:45.5Z", "2004-02-29T12:30:45", "2004-02-29T12:30:45Z", "2004-03-01T00:00:00Z", "2004-02-29T23:59:59.999-14:00", "2004-01-01T00:00:00+14:00", "2004-01-01T00:00:00Z", "2003-12-31T10:00:00Z", "2003-12-31T10:00:00", "2004-01-01", "2004-01-01Z", "2004-01-02Z", "2004-01-01+14:00", "2004-01-01-14:00", "12:00:00", "12:00:00Z", "13:00:00Z", "12:00:00+01:00", "11:00:00Z", "2004-02", "2004-02Z", "2004-03", "2004", "2005", "2004Z", "--02", "--03", "--02-29", "--03-01", "---15", "---16", "-0001-12-31T23:59:59Z", "0001-01-01T00:00:00Z", "99999999999999999999-01-01T00:00:00Z", "2004-12-31T23:59:60Z", "2005-01-01T00:00:00Z", "2004-02-29T12:30:45.50Z", "2004-02-29T12:30:45.500Z"};
    System.out.println("--- normalize");
    for (String l : dts) { XMLGregorianCalendar c = f.newXMLGregorianCalendar(l); try { System.out.println("norm " + l + " => " + show(c.normalize()) + " orig=" + show(c)); } catch (Throwable t) { System.out.println("norm " + l + fail(t)); } }
    System.out.println("--- compare");
    for (String x : dts) for (String y : dts) { XMLGregorianCalendar cx = f.newXMLGregorianCalendar(x), cy = f.newXMLGregorianCalendar(y); StringBuilder sb = new StringBuilder("cmp " + x + " vs " + y + " "); try { sb.append(cx.compare(cy)); } catch (Throwable t) { sb.append(fail(t)); } try { sb.append(" eq=" + cx.equals(cy) + " hash=" + (cx.hashCode() == cy.hashCode())); } catch (Throwable t) { sb.append(fail(t)); } System.out.println(sb); }
    System.out.println("--- add");
    String[] durs = {"P1Y", "P1M", "P1D", "PT1H", "PT1M", "PT1S", "PT0.5S", "P1Y2M3DT4H5M6.7S", "-P1Y", "-P1M", "-P1D", "-PT1H", "-PT1S", "-PT0.5S", "P13M", "P366D", "PT25H", "PT61M", "PT61S", "-P1Y2M3DT4H5M6.7S", "P0D", "P1MT1S", "-P1MT1S", "PT0S", "P100Y", "-P100Y", "P12M31D"};
    String[] bases = {"2004-02-29T12:30:45.5+01:00", "2004-01-31T00:00:00Z", "2004-01-31T23:59:59", "2003-12-31T23:59:59.999Z", "2004-02-29", "2004-02-29Z", "2004-02", "2004", "--02-29", "--02", "---31", "12:00:00", "23:59:59Z", "0001-01-01T00:00:00Z", "-0001-12-31T23:59:59Z", "2004-03-31T00:00:00Z", "2004-12-31T24:00:00", "9999-12-31T23:59:59Z", "2004-02-29T00:00:00.123456789012345678901234567890Z"};
    for (String b : bases) for (String d : durs) { XMLGregorianCalendar c = f.newXMLGregorianCalendar(b); StringBuilder sb = new StringBuilder("add " + b + " + " + d + " => "); try { c.add(f.newDuration(d)); sb.append(show(c)); } catch (Throwable t) { sb.append(fail(t)); } System.out.println(sb); }
  } }

static class G4 {
  static String fail(Throwable t) { return "!" + t.getClass().getName() + ":" + t.getMessage(); }
  static String gc(GregorianCalendar g) { return g.getTimeInMillis() + " tz=" + g.getTimeZone().getID() + " " + g.get(Calendar.ERA) + "/" + g.get(Calendar.YEAR) + "-" + (g.get(Calendar.MONTH) + 1) + "-" + g.get(Calendar.DAY_OF_MONTH) + "T" + g.get(Calendar.HOUR_OF_DAY) + ":" + g.get(Calendar.MINUTE) + ":" + g.get(Calendar.SECOND) + "." + g.get(Calendar.MILLISECOND); }
  public static void run() throws Exception {
    DatatypeFactory f = DatatypeFactory.newInstance();
    String[] lex = {"2004-02-29T12:30:45.123+05:30", "2004-02-29T12:30:45Z", "2004-02-29T12:30:45", "2004-02-29", "2004-02-29-08:00", "12:30:45", "12:30:45.5Z", "2004-02", "2004", "--02", "--02-29", "---29", "-0001-01-01T00:00:00Z", "0001-01-01T00:00:00Z", "-0100-12-31T23:59:59Z", "1500-03-01T00:00:00Z", "1582-10-10T00:00:00Z", "99999-01-01T00:00:00Z"};
    for (String l : lex) {
      XMLGregorianCalendar c = f.newXMLGregorianCalendar(l);
      try { System.out.println(l + " gc=" + gc(c.toGregorianCalendar())); } catch (Throwable t) { System.out.println(l + " gc" + fail(t)); }
      try { System.out.println(l + " gc(tz,loc,null)=" + gc(c.toGregorianCalendar(TimeZone.getTimeZone("GMT-03:00"), Locale.FRANCE, null))); } catch (Throwable t) { System.out.println(l + " gcx" + fail(t)); }
      XMLGregorianCalendar defs = f.newXMLGregorianCalendar("1999-12-25T10:20:30.5+03:00");
      try { System.out.println(l + " gc(null,null,defs)=" + gc(c.toGregorianCalendar(null, null, defs))); } catch (Throwable t) { System.out.println(l + " gcd" + fail(t)); }
      try { System.out.println(l + " tz=" + c.getTimeZone(60).getID() + "/" + c.getTimeZone(DatatypeConstants.FIELD_UNDEFINED).getID() + "/" + c.getTimeZone(-1000).getID() + "/" + c.getTimeZone(0).getID()); } catch (Throwable t) { System.out.println(l + " tz" + fail(t)); }
    }
    GregorianCalendar[] gcs = new GregorianCalendar[4];
    gcs[0] = new GregorianCalendar(TimeZone.getTimeZone("GMT+05:30"), Locale.US); gcs[0].clear(); gcs[0].set(2004, 1, 29, 12, 30, 45); gcs[0].set(Calendar.MILLISECOND, 7);
    gcs[1] = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US); gcs[1].clear(); gcs[1].set(Calendar.ERA, GregorianCalendar.BC); gcs[1].set(5, 0, 1, 0, 0, 0);
    gcs[2] = new GregorianCalendar(TimeZone.getTimeZone("America/New_York"), Locale.US); gcs[2].clear(); gcs[2].set(2004, 6, 4, 8, 0, 0);
    gcs[3] = new GregorianCalendar(TimeZone.getTimeZone("GMT-14:00"), Locale.US); gcs[3].clear(); gcs[3].set(1900, 0, 1, 0, 0, 0);
    for (GregorianCalendar g : gcs) { try { XMLGregorianCalendar c = f.newXMLGregorianCalendar(g); System.out.println("from gc: " + c + " tz=" + c.getTimezone() + " fs=" + c.getFractionalSecond() + " type=" + c.getXMLSchemaType().getLocalPart()); } catch (Throwable t) { System.out.println("from gc" + fail(t)); } }
    try { f.newXMLGregorianCalendar((GregorianCalendar) null); } catch (Throwable t) { System.out.println("gc null " + t.getClass().getName()); }
    // duration calendar ops
    GregorianCalendar base = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US); base.clear(); base.set(2004, 0, 31, 10, 0, 0);
    String[] durs = {"P1Y", "P1M", "P1D", "PT1H", "-P1M", "P1Y2M3DT4H5M6.789S", "-P1Y2M3DT4H5M6.789S", "PT0.5S", "-PT0.5S", "PT0S", "P1M1D", "P12M", "P365D", "P366D"};
    for (String d : durs) {
      Duration du = f.newDuration(d);
      GregorianCalendar c = (GregorianCalendar) base.clone();
      du.addTo(c);
      Date date = new Date(base.getTimeInMillis()); du.addTo(date);
      System.out.println("dur " + d + " addTo=" + c.getTimeInMillis() + " date=" + date.getTime() + " ms(cal)=" + du.getTimeInMillis(base) + " ms(date)=" + du.getTimeInMillis(new Date(base.getTimeInMillis())) + " norm=" + du.normalizeWith(base) + " hash=" + du.hashCode());
    }
    try { f.newDuration("P1D").addTo((Date) null); } catch (Throwable t) { System.out.println("addTo null date " + fail(t)); }
    try { f.newDuration("P1D").addTo((Calendar) null); } catch (Throwable t) { System.out.println("addTo null cal " + t.getClass().getName()); }
    try { f.newDuration("P1D").isSet(null); } catch (Throwable t) { System.out.println("isSet null " + fail(t)); }
    try { f.newDuration("P1D").getField(null); } catch (Throwable t) { System.out.println("getField null " + fail(t)); }
    try { f.newDuration("P1D").multiply(null); } catch (Throwable t) { System.out.println("mul null " + t.getClass().getName()); }
    try { f.newDuration("P1D").add(null); } catch (Throwable t) { System.out.println("add null " + t.getClass().getName()); }
    try { f.newDuration("P1D").compare(null); } catch (Throwable t) { System.out.println("compare null " + t.getClass().getName()); }
    try { f.newDuration("P1D").normalizeWith(null); } catch (Throwable t) { System.out.println("norm null " + t.getClass().getName()); }
    try { System.out.println("equals null " + f.newDuration("P1D").equals(null) + " " + f.newDuration("P1D").equals("P1D")); } catch (Throwable t) { System.out.println("equals null " + fail(t)); }
    try { f.newXMLGregorianCalendar("2004-01-01").compare(null); } catch (Throwable t) { System.out.println("xgc compare null " + t.getClass().getName()); }
    try { f.newXMLGregorianCalendar("2004-01-01").add(null); } catch (Throwable t) { System.out.println("xgc add null " + t.getClass().getName()); }
    try { System.out.println("xgc equals " + f.newXMLGregorianCalendar("2004-01-01").equals(null) + " " + f.newXMLGregorianCalendar("2004-01-01").equals("x")); } catch (Throwable t) { System.out.println("xgc equals " + fail(t)); }
    // mul
    for (String d : new String[]{"P1Y2M3DT4H5M6.789S", "P1M", "P1D", "PT1S", "P1Y", "-P1D", "PT0S", "P1DT1S"}) for (String m : new String[]{"0", "1", "2", "0.5", "-1", "-0.5", "1.5", "0.25", "10", "0.000001", "3.3"}) { try { System.out.println("mul " + d + " x " + m + " = " + f.newDuration(d).multiply(new BigDecimal(m))); } catch (Throwable t) { System.out.println("mul " + d + " x " + m + fail(t)); } }
    for (int m : new int[]{0, 1, -1, 3}) System.out.println("mulint " + f.newDuration("P1Y2M3DT4H5M6S").multiply(m));
    // eon / big
    XMLGregorianCalendar big = f.newXMLGregorianCalendar("99999999999-12-31T23:59:59Z"); big.add(f.newDuration("PT1S")); System.out.println("big add " + big + " eon=" + big.getEon());
    big = f.newXMLGregorianCalendar("-99999999999-01-01T00:00:00Z"); big.add(f.newDuration("-PT1S")); System.out.println("big sub " + big);
    System.out.println("clone " + big.clone().equals(big) + " " + (big.clone() != big));
    XMLGregorianCalendar r = f.newXMLGregorianCalendar("2004-02-29T12:30:45.5+01:00"); r.setHour(1); r.setYear(1999); r.clear(); r.reset(); System.out.println("reset " + r);
    r = f.newXMLGregorianCalendar("2004-02-29T12:30:45.5+01:00"); XMLGregorianCalendar cl = (XMLGregorianCalendar) r.clone(); cl.setHour(1); cl.reset(); System.out.println("clone reset " + cl + " " + r);
    r = f.newXMLGregorianCalendar("2004-02-29T00:00:00"); r.setHour(24); System.out.println("setHour24 " + r);
    r = f.newXMLGregorianCalendar("2004-02-29T00:00:00"); try { r.setHour(24); } catch (Throwable t) { System.out.println("setHour24 " + fail(t)); }
    r = f.newXMLGregorianCalendar("2004-02-29T10:00:00"); try { r.setTime(24, 0, 0); System.out.println("setTime24 " + r); } catch (Throwable t) { System.out.println("setTime24 " + fail(t)); }
    r = f.newXMLGregorianCalendar("2004-02-29T10:00:00"); try { r.setTime(24, 0, 0, 500); System.out.println("setTime24ms " + r); } catch (Throwable t) { System.out.println("setTime24ms " + fail(t)); }
    r = f.newXMLGregorianCalendar("2004-02-29T10:00:00"); try { r.setTime(24, 0, 0, new BigDecimal("0.5")); System.out.println("setTime24frac " + r); } catch (Throwable t) { System.out.println("setTime24frac " + fail(t)); }
    System.out.println("consts " + DatatypeConstants.DATETIME + " " + DatatypeConstants.DURATION_DAYTIME + " " + DatatypeConstants.YEARS + " " + DatatypeConstants.SECONDS.getId() + " " + DatatypeConstants.MAX_TIMEZONE_OFFSET + " " + DatatypeConstants.MIN_TIMEZONE_OFFSET);
    System.out.println("factory " + DatatypeFactory.DATATYPEFACTORY_PROPERTY);
    try { DatatypeFactory.newInstance("nope.Missing", null); } catch (Throwable t) { System.out.println("newInstance missing " + fail(t)); }
    try { DatatypeFactory.newInstance(null, null); } catch (Throwable t) { System.out.println("newInstance null " + fail(t)); }
    System.out.println(f.newXMLGregorianCalendarDate(2004, 2, 29, 60));
  } }

  public static void main(String[] args) throws Exception {
    TimeZone.setDefault(TimeZone.getTimeZone("GMT+02:00"));
    Locale.setDefault(Locale.US);
    System.out.println("--- durations");
    D1.run();
    System.out.println("--- parse");
    G1.run();
    System.out.println("--- setters");
    G2.run();
    System.out.println("--- compare");
    G3.run();
    System.out.println("--- conversions");
    G4.run();
  }
}
