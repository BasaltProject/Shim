/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 17

import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TextAndFormatsModern {

  public static void main(String[] args) throws Exception {
    testDecimalFormatSymbols();
    testDecimalFormat();
    testNumberFormatFactories();
    testDateFormatSymbols();
    testSimpleDateFormat();
    testFormatParseObject();
    System.out.println("TextAndFormatsModern passed successfully");
  }

  private static void testDecimalFormatSymbols() {
    DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
    symbols.setDecimalSeparator('.');
    symbols.setGroupingSeparator(',');
    symbols.setMinusSign('-');

    System.out.println("decSep: " + symbols.getDecimalSeparator());
    System.out.println("grpSep: " + symbols.getGroupingSeparator());
    System.out.println("minus: " + symbols.getMinusSign());
    System.out.println("nan: " + symbols.getNaN());
    System.out.println("infinity: " + symbols.getInfinity());

    DecimalFormatSymbols cloned = (DecimalFormatSymbols) symbols.clone();
    System.out.println("cloned equals: " + symbols.equals(cloned));
  }

  private static void testDecimalFormat() throws Exception {
    DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
    symbols.setDecimalSeparator('.');
    symbols.setGroupingSeparator(',');

    DecimalFormat df = new DecimalFormat("#,##0.00", symbols);
    System.out.println("pattern: " + df.toPattern());
    System.out.println("format 1234567.89: " + df.format(1234567.89));
    System.out.println("format 0: " + df.format(0));
    System.out.println("format -42.5: " + df.format(-42.5));

    df.applyPattern("0.###");
    System.out.println("pattern 0.### 12.3456: " + df.format(12.3456));
    System.out.println("pattern 0.### 12: " + df.format(12));

    df.applyPattern("#,##0%");
    System.out.println("percent 0.75: " + df.format(0.75));

    df.applyPattern("#,##0.00;(#,##0.00)");
    System.out.println("neg pattern -123.45: " + df.format(-123.45));
    System.out.println("pos pattern 123.45: " + df.format(123.45));

    DecimalFormat parseDf = new DecimalFormat("#,##0.00", symbols);
    Number parsed = parseDf.parse("1,234.56");
    System.out.println("parsed double: " + parsed.doubleValue());

    Number parsedInt = parseDf.parse("500");
    System.out.println("parsed int: " + parsedInt.longValue());
  }

  private static void testNumberFormatFactories() throws Exception {
    NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
    System.out.println("nf grouping: " + nf.isGroupingUsed());
    System.out.println("nf format 9876543: " + nf.format(9876543));

    NumberFormat intFmt = NumberFormat.getIntegerInstance(Locale.US);
    System.out.println("intFmt format 123.789: " + intFmt.format(123.789));

    NumberFormat pctFmt = NumberFormat.getPercentInstance(Locale.US);
    System.out.println("pctFmt format 0.5: " + pctFmt.format(0.5));
  }

  private static void testDateFormatSymbols() {
    DateFormatSymbols dfs = new DateFormatSymbols(Locale.US);
    String[] months = dfs.getMonths();
    System.out.println("first month: " + months[0]);
    System.out.println("last month: " + months[11]);

    String[] shortMonths = dfs.getShortMonths();
    System.out.println("first short month: " + shortMonths[0]);

    String[] weekdays = dfs.getWeekdays();
    System.out.println("sunday weekday: " + weekdays[1]);

    String[] ampm = dfs.getAmPmStrings();
    System.out.println("am string: " + ampm[0]);
    System.out.println("pm string: " + ampm[1]);
  }

  private static void testSimpleDateFormat() throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

    Date epoch = new Date(0L);
    System.out.println("epoch format: " + sdf.format(epoch));

    Date parsed = sdf.parse("2026-10-15 12:30:45");
    System.out.println("parsed year millis > 0: " + (parsed.getTime() > 0));

    sdf.applyPattern("dd/MM/yyyy");
    System.out.println("dd/MM/yyyy: " + sdf.format(epoch));

    sdf.applyPattern("yyyy 'year' MM 'month'");
    System.out.println("quoted literal: " + sdf.format(epoch));
  }

  private static void testFormatParseObject() throws Exception {
    Format format = new DecimalFormat("#,##0.00", new DecimalFormatSymbols(Locale.US));
    Object obj = format.parseObject("1,000.50");
    System.out.println("parseObject number: " + ((Number) obj).doubleValue());

    ParsePosition pp = new ParsePosition(0);
    Object res = format.parseObject("99.99", pp);
    System.out.println("parseObject with pos: " + ((Number) res).doubleValue());
    System.out.println("pos index: " + pp.getIndex());
  }
}
