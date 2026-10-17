/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.io.*;
import java.math.*;
import java.net.*;
import java.net.spi.*;
import java.security.*;
import java.security.cert.*;
import java.text.*;
import java.text.spi.*;
import java.util.*;
import java.util.spi.*;
import javax.security.auth.x500.X500PrivateCredential;

public class CompactNumberModern {
static class A {
  static void t(String n, java.util.concurrent.Callable<Object> c) { try { System.out.println(n + ": " + c.call()); } catch (Throwable e) { System.out.println(n + ": !" + e.getClass().getName() + ":" + (e instanceof NullPointerException ? null : e.getMessage())); } }
  public static void run() throws Exception {
    Locale.setDefault(Locale.ENGLISH);

    Locale en = Locale.ENGLISH;
    NumberFormat s = NumberFormat.getCompactNumberInstance(en, NumberFormat.Style.SHORT), l = NumberFormat.getCompactNumberInstance(en, NumberFormat.Style.LONG);
    double[] vals = {0, 1, 5, 12, 99, 100, 999, 1000, 1001, 1499, 1500, 1999, 2500, 9999, 10000, 12345, 99999, 100000, 999999, 1000000, 1234567, 15000000, 999999999, 1000000000L, 2.5e9, 1e12, 1.5e12, 1e15, 9.99e14, 1e18, -1000, -1500, -999, 0.5, 0.0001, 1e-9, Double.NaN, Double.POSITIVE_INFINITY, -0.0, 1e100};
    for (double v : vals) System.out.println(v + " | " + s.format(v) + " | " + l.format(v));
    long[] ls = {0, 1, 1000, 1500, 999999, 1000000, Long.MAX_VALUE, Long.MIN_VALUE, -1234567890123L};
    for (long v : ls) System.out.println(v + " | " + s.format(v) + " | " + l.format(v));
    t("bigdec", () -> s.format(new BigDecimal("12345678.9")) + " " + l.format(new BigDecimal("12345678.9")) + " " + s.format(new BigInteger("123456789012345678901234567890")));
    t("obj", () -> s.format((Object) 12345) + " " + s.format((Object) 12.5f));
    t("objbad", () -> s.format("x"));
    NumberFormat f2 = NumberFormat.getCompactNumberInstance(en, NumberFormat.Style.SHORT);
    f2.setMaximumFractionDigits(2);
    t("frac", () -> f2.format(1234) + " " + f2.format(1250) + " " + f2.format(1999) + " " + f2.format(1000) + " " + f2.format(12345) + " " + f2.format(123456) + " " + f2.format(1234567));
    f2.setMinimumFractionDigits(1);
    t("minfrac", () -> f2.format(1000) + " " + f2.format(1234) + " " + f2.format(5) + " " + f2.format(500) + " " + f2.format(1500000));
    f2.setRoundingMode(RoundingMode.UP);
    t("round", () -> f2.format(1234) + " " + f2.format(1001) + " " + f2.getRoundingMode());
    f2.setRoundingMode(RoundingMode.HALF_UP);
    f2.setMaximumFractionDigits(0); f2.setMinimumFractionDigits(0);
    t("halfup", () -> f2.format(1500) + " " + f2.format(2500) + " " + f2.format(1499));
    NumberFormat f3 = NumberFormat.getCompactNumberInstance(en, NumberFormat.Style.SHORT);
    f3.setGroupingUsed(true);
    t("group", () -> f3.format(1234) + " " + f3.format(123456) + " " + f3.format(1234567890123456L) + " " + f3.isGroupingUsed());
    f3.setMinimumIntegerDigits(3);
    t("minint", () -> f3.format(5) + " " + f3.format(1500) + " " + f3.format(12000));
    f3.setMinimumIntegerDigits(1); f3.setMaximumIntegerDigits(2);
    t("maxint", () -> f3.format(5) + " " + f3.format(123) + " " + f3.format(12345) + " " + f3.format(1234567));
    t("props", () -> s.getMaximumFractionDigits() + " " + s.getMinimumFractionDigits() + " " + s.getMaximumIntegerDigits() + " " + s.getMinimumIntegerDigits() + " " + s.getRoundingMode() + " " + s.isGroupingUsed() + " " + ((CompactNumberFormat) s).isParseBigDecimal() + " " + s.isParseIntegerOnly() + " " + ((CompactNumberFormat) s).isStrict() + " " + ((CompactNumberFormat) s).getGroupingSize());
    t("parse", () -> s.parse("1K") + " " + s.parse("1.5K") + " " + s.parse("2M") + " " + s.parse("1B") + " " + s.parse("3T") + " " + s.parse("12") + " " + s.parse("-5K") + " " + s.parse("1,234K") + " " + s.parse("0.5K") + " " + s.parse("1K extra"));
    t("parseLong", () -> l.parse("1 thousand") + " " + l.parse("2.5 million") + " " + l.parse("1 billion") + " " + l.parse("1 trillion") + " " + l.parse("-3 thousand") + " " + l.parse("12"));
    t("parseCls", () -> s.parse("1K").getClass().getSimpleName() + " " + s.parse("1.5K").getClass().getSimpleName() + " " + s.parse("12").getClass().getSimpleName() + " " + s.parse("3.7").getClass().getSimpleName() + " " + s.parse("2B").getClass().getSimpleName());
    t("parseBad", () -> s.parse("abc"));
    t("parseBad2", () -> s.parse(""));
    t("parseBad3", () -> s.parse("K"));
    t("parsePos", () -> { ParsePosition p = new ParsePosition(0); Number n = s.parse("12K tail", p); return n + " " + p.getIndex() + " " + p.getErrorIndex(); });
    t("parsePos2", () -> { ParsePosition p = new ParsePosition(0); Number n = s.parse("xyz", p); return n + " " + p.getIndex() + " " + p.getErrorIndex(); });
    t("parsePos3", () -> { ParsePosition p = new ParsePosition(2); Number n = s.parse("ab5K", p); return n + " " + p.getIndex() + " " + p.getErrorIndex(); });
    NumberFormat pb = NumberFormat.getCompactNumberInstance(en, NumberFormat.Style.SHORT);
    ((CompactNumberFormat) pb).setParseBigDecimal(true);
    t("parseBig", () -> pb.parse("1.5K") + " " + pb.parse("1.5K").getClass().getSimpleName() + " " + pb.parse("7").getClass().getSimpleName());
    NumberFormat pi = NumberFormat.getCompactNumberInstance(en, NumberFormat.Style.SHORT);
    pi.setParseIntegerOnly(true);
    t("parseInt", () -> pi.parse("1.9K") + " " + pi.parse("5.5"));
    NumberFormat st = NumberFormat.getCompactNumberInstance(en, NumberFormat.Style.SHORT);
    ((CompactNumberFormat) st).setStrict(true);
    t("strict", () -> st.parse("1K") + " " + st.parse("1.5K"));
    t("strictBad", () -> st.parse("1 K"));
    t("strictBad2", () -> st.parse("1KK"));
    t("nullStyle", () -> NumberFormat.getCompactNumberInstance(en, null));
    t("nullLocale", () -> NumberFormat.getCompactNumberInstance(null, NumberFormat.Style.SHORT));
    t("styles", () -> Arrays.toString(NumberFormat.Style.values()) + " " + NumberFormat.Style.valueOf("LONG"));
    t("equals", () -> s.equals(NumberFormat.getCompactNumberInstance(en, NumberFormat.Style.SHORT)) + " " + s.equals(l) + " " + (s.hashCode() == NumberFormat.getCompactNumberInstance(en, NumberFormat.Style.SHORT).hashCode()) + " " + s.equals(null));
    t("clone", () -> { Object c = s.clone(); return c != s && c.equals(s) && c.getClass() == s.getClass(); });
    t("fieldPos", () -> { FieldPosition fp = new FieldPosition(NumberFormat.INTEGER_FIELD); StringBuffer sb = s.format(12345.0, new StringBuffer(), fp); return sb + " " + fp.getBeginIndex() + "-" + fp.getEndIndex(); });
    t("fieldPos2", () -> { FieldPosition fp = new FieldPosition(NumberFormat.Field.SUFFIX); StringBuffer sb = s.format(12345.0, new StringBuffer(), fp); return sb + " " + fp.getBeginIndex() + "-" + fp.getEndIndex(); });
    t("cci", () -> { AttributedCharacterIterator it = s.formatToCharacterIterator(12345); StringBuilder sb = new StringBuilder(); for (char c = it.first(); c != CharacterIterator.DONE; c = it.next()) { sb.append(c); Map<AttributedCharacterIterator.Attribute, Object> m = it.getAttributes(); if (!m.isEmpty()) sb.append(m.keySet().toString().replace("java.text.NumberFormat$Field(", "(")); } return sb; });
    t("ctor", () -> new CompactNumberFormat("#,##0", DecimalFormatSymbols.getInstance(en), new String[] {"", "", "", "0K", "00K", "000K", "0M", "00M", "000M", "0B"}).format(1234567));
    t("ctor2", () -> new CompactNumberFormat("#,##0.0", DecimalFormatSymbols.getInstance(en), new String[] {"", "", "", "0 thou", "00 thou", "000 thou", "0 mil"}, "{one:n} {other:n}").format(1500));
    t("ctor4", () -> new CompactNumberFormat(null, DecimalFormatSymbols.getInstance(en), new String[] {"0K"}));
    t("ctor5", () -> new CompactNumberFormat("#", DecimalFormatSymbols.getInstance(en), new String[] {"0K"}).format(5000));
    t("ctor6", () -> new CompactNumberFormat("#", DecimalFormatSymbols.getInstance(en), new String[] {"", "", "", "0K", "0K", "0K"}).format(12345));
    t("ctor7", () -> new CompactNumberFormat("#", DecimalFormatSymbols.getInstance(en), new String[] {"", "", "", "x'0'K", "0K;-0K"}).format(12345));
    t("neg", () -> new CompactNumberFormat("#", DecimalFormatSymbols.getInstance(en), new String[] {"", "", "", "0K;(0K)"}).format(-5000));
    t("zeroPat", () -> new CompactNumberFormat("#", DecimalFormatSymbols.getInstance(en), new String[] {"", "0 x", "", "0K"}).format(55));
  }
}

static class B {
  public static void run() throws Exception {

    NumberFormat s = NumberFormat.getCompactNumberInstance(Locale.ENGLISH, NumberFormat.Style.SHORT);
    for (double v : new double[] {999.4, 999.5, 999.6, 99.5, 0.7, 1.5, 2.5, 0.51, 9999.5, 999500, 999499, 99950, 1e6 - 0.4, 123456789.987, 1.0E14, 5e14, 5e15}) System.out.println(v + " -> " + s.format(v));
    NumberFormat f = NumberFormat.getCompactNumberInstance(Locale.ENGLISH, NumberFormat.Style.SHORT);
    f.setMaximumFractionDigits(1);
    for (double v : new double[] {999.96, 999.94, 999949, 999950, 999999, 1049, 1050, 1051, 9950, 9949, 0.05, 0.04, 0.96, 12.345}) System.out.println("mf1 " + v + " -> " + f.format(v));
    f.setMinimumFractionDigits(1); f.setMaximumFractionDigits(1);
    for (double v : new double[] {5, 1000, 1500, 999999, 0.04, 12}) System.out.println("mf11 " + v + " -> " + f.format(v));
    CompactNumberFormat c = (CompactNumberFormat) NumberFormat.getCompactNumberInstance(Locale.ENGLISH, NumberFormat.Style.SHORT);
    System.out.println(c.format(1234L) + " " + c.format(-5L) + " " + c.format(5.0f) + " " + c.format(new BigDecimal("-1500")) + " " + c.format((Object) new BigInteger("-1500")) + " " + c.format((Object) (short) 5000) + " " + c.format((Object) new java.util.concurrent.atomic.AtomicLong(7000)));
    System.out.println(c.parse("1K", new ParsePosition(0)) + " " + c.parse("1.5k", new ParsePosition(0)) + " " + c.parse("1 K", new ParsePosition(0)) + " " + c.parse(" 1K", new ParsePosition(0)) + " " + c.parse("+1K", new ParsePosition(0)) + " " + c.parse("1e3", new ParsePosition(0)) + " " + c.parse("1K2", new ParsePosition(0)));
    NumberFormat l = NumberFormat.getCompactNumberInstance(Locale.ENGLISH, NumberFormat.Style.LONG);
    System.out.println(l.parse("1 thousand") + " " + l.parse("1thousand") + " " + l.parse("1 Thousand") + " " + l.parse("2 thousand") + " " + l.parse("1 thousands"));
    System.out.println(l.format(1000) + "|" + l.format(1000.5) + "|" + l.format(2000) + "|" + l.format(1500));
    NumberFormat lf = NumberFormat.getCompactNumberInstance(Locale.ENGLISH, NumberFormat.Style.LONG);
    lf.setMaximumFractionDigits(1);
    System.out.println(lf.format(1500) + "|" + lf.format(1000) + "|" + lf.format(1050) + "|" + lf.format(2000));
  }
}

static class C {
  public static void run() throws Exception {

    NumberFormat s = NumberFormat.getCompactNumberInstance(Locale.ENGLISH, NumberFormat.Style.SHORT);
    for (double v : new double[] {999499, 999500, 999501, 999500.4, 999999, 999999.4, 1999500, 1999501, 99500, 99499, 9500, 9499, 949, 999.5, 999, 99999, 99999.9, 999999999, 999500000, 999499999, 999500001}) System.out.println("f0 " + v + " -> " + s.format(v) + " | long " + (v == Math.rint(v) ? s.format((long) v) : ""));
    s.setMaximumFractionDigits(1);
    for (double v : new double[] {999949, 999950, 999951, 999900, 999999, 99949, 99950, 99951, 9949, 9950, 9951, 949, 950}) System.out.println("f1 " + v + " -> " + s.format(v) + " | long " + (v == Math.rint(v) ? s.format((long) v) : ""));
    s.setRoundingMode(java.math.RoundingMode.DOWN);
    for (double v : new double[] {999999, 999999.9, 999.9, 1999, 9999}) System.out.println("down " + v + " -> " + s.format(v));
    s.setRoundingMode(java.math.RoundingMode.UP);
    for (double v : new double[] {1001, 999001, 999, 1, 1000001}) System.out.println("up " + v + " -> " + s.format(v));
  }
}

  public static void main(String[] args) throws Exception {
    A.run();
    B.run();
    C.run();
  }
}
