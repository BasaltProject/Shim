/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.*;
import java.util.*;
import java.util.function.Supplier;

public class NumberFormatModern {
  static void probe(String name, Supplier<Object> body) {
    String out;
    try {
      out = String.valueOf(body.get());
    } catch (Throwable t) {
      out = "!" + t.getClass().getName() + ":" + t.getMessage();
    }
    System.out.println(name + "=" + out.replace(' ', '~').replace(' ', '^'));
  }

  public static void main(String[] args) {
    Locale.setDefault(Locale.US);
    String[] patterns = {"#,##0.00", "0.###", "#.##", "000.000", "#,###", "0.00E0", "##0.#####E0", "00.00E00", "#%", "0.0%", "‰ #", "#,##0.00;(#,##0.00)", "'#'#", "$#,##0.00", "¤#,##0.00", "¤¤ #", "#,##,##0", "0", "#", "0.0", ".00", "#.", "+#;-#", "'x'0'y'", "#,##0.###E0"};
    double[] values = {0, -0.0, 1, -1, 0.5, 1.5, 2.5, 0.125, 0.135, 1234.5678, -1234.5678, 123456789.987654321, 0.000123456, 1e-10, 1e15, 3e20, 12345678901234567890.0, 0.99999, 9.995, 99.995, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, 100, 0.1 + 0.2, 1.005, 2.675, 5e-324};
    for (String p : patterns) {
      DecimalFormat f;
      try {
        f = new DecimalFormat(p, DecimalFormatSymbols.getInstance(Locale.US));
      } catch (Throwable t) {
        System.out.println("pat[" + p + "]=!" + t.getClass().getName() + ":" + t.getMessage());
        continue;
      }
      System.out.println("pat[" + p + "]=" + f.toPattern() + " " + f.getMinimumIntegerDigits() + "/" + f.getMaximumIntegerDigits() + "/" + f.getMinimumFractionDigits() + "/" + f.getMaximumFractionDigits() + " g" + f.getGroupingSize() + " m" + f.getMultiplier() + " " + f.getPositivePrefix() + "|" + f.getPositiveSuffix() + "|" + f.getNegativePrefix() + "|" + f.getNegativeSuffix());
      for (double v : values) {
        final DecimalFormat ff = f;
        final double vv = v;
        probe("d[" + p + "][" + v + "]", () -> ff.format(vv));
      }
      for (long v : new long[] {0, 1, -1, 999, 1000, 123456789012L, Long.MAX_VALUE, Long.MIN_VALUE}) {
        final DecimalFormat ff = f;
        final long vv = v;
        probe("l[" + p + "][" + v + "]", () -> ff.format(vv));
      }
    }
    for (RoundingMode rm : RoundingMode.values()) {
      if (rm == RoundingMode.UNNECESSARY) continue;
      for (double v : new double[] {0.5, 1.5, 2.5, -0.5, -1.5, 1.25, 1.35, 2.675, 0.125, 0.375, 1.0001}) {
        final RoundingMode r = rm;
        final double vv = v;
        probe("round[" + rm + "][" + v + "]", () -> { DecimalFormat f = new DecimalFormat("0.#"); f.setRoundingMode(r); DecimalFormat g = new DecimalFormat("0.00"); g.setRoundingMode(r); return f.format(vv) + " " + g.format(vv); });
      }
    }
    probe("unnecessary", () -> { DecimalFormat f = new DecimalFormat("0.0"); f.setRoundingMode(RoundingMode.UNNECESSARY); return f.format(0.25); });
    probe("unnecessary-ok", () -> { DecimalFormat f = new DecimalFormat("0.0"); f.setRoundingMode(RoundingMode.UNNECESSARY); return f.format(0.5); });
    probe("bigdecimal", () -> { DecimalFormat f = new DecimalFormat("#,##0.00"); return f.format(new BigDecimal("12345678901234567890.12345")) + " " + f.format(new BigDecimal("-0.005")) + " " + f.format(new BigInteger("123456789012345678901234567890")) + " " + f.format(new BigDecimal("1E+30")); });
    probe("sci-bigdecimal", () -> new DecimalFormat("0.###E0").format(new BigDecimal("123456789.123456789")));
    probe("integer-instance", () -> NumberFormat.getIntegerInstance(Locale.US).format(1234567.5) + " " + NumberFormat.getIntegerInstance(Locale.US).format(2.5) + " " + NumberFormat.getIntegerInstance(Locale.US).format(3.5) + " " + NumberFormat.getIntegerInstance(Locale.US).format(-0.4));
    probe("number-instance", () -> NumberFormat.getInstance(Locale.US).format(1234567.891234) + " " + NumberFormat.getNumberInstance(Locale.US).format(0.0005) + " " + NumberFormat.getInstance(Locale.US).format(-1234.5));
    probe("percent-instance", () -> NumberFormat.getPercentInstance(Locale.US).format(0.256) + " " + NumberFormat.getPercentInstance(Locale.US).format(1.5) + " " + NumberFormat.getPercentInstance(Locale.US).format(-0.005));
    probe("currency-instance", () -> NumberFormat.getCurrencyInstance(Locale.US).format(1234.567) + " " + NumberFormat.getCurrencyInstance(Locale.US).format(-1234.567) + " " + NumberFormat.getCurrencyInstance(Locale.US).format(0));
    probe("instance-patterns", () -> ((DecimalFormat) NumberFormat.getInstance(Locale.US)).toPattern() + " " + ((DecimalFormat) NumberFormat.getPercentInstance(Locale.US)).toPattern() + " " + ((DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US)).toPattern() + " " + ((DecimalFormat) NumberFormat.getIntegerInstance(Locale.US)).toPattern());
    probe("digits-limits", () -> { NumberFormat f = NumberFormat.getInstance(Locale.US); f.setMaximumFractionDigits(1); f.setMinimumFractionDigits(3); String a = f.getMaximumFractionDigits() + " " + f.getMinimumFractionDigits(); f.setMaximumIntegerDigits(2); f.setMinimumIntegerDigits(5); return a + " " + f.getMaximumIntegerDigits() + " " + f.getMinimumIntegerDigits() + " " + f.format(123456.789); });
    probe("digits-neg", () -> { NumberFormat f = NumberFormat.getInstance(Locale.US); f.setMaximumFractionDigits(-5); return f.getMaximumFractionDigits() + " " + f.format(1.7); });
    probe("max-int-digits", () -> { NumberFormat f = NumberFormat.getInstance(Locale.US); f.setMaximumIntegerDigits(3); return f.format(123456.5) + " " + f.format(1234567L); });
    probe("grouping", () -> { DecimalFormat f = new DecimalFormat("#,##0"); f.setGroupingUsed(false); String a = f.format(1234567); f.setGroupingUsed(true); f.setGroupingSize(2); return a + " " + f.format(1234567) + " " + f.getGroupingSize() + " " + f.toPattern(); });
    probe("multiplier", () -> { DecimalFormat f = new DecimalFormat("0"); f.setMultiplier(100); return f.format(0.256) + " " + f.format(5) + " " + f.getMultiplier() + " " + f.toPattern(); });
    probe("decimal-always", () -> { DecimalFormat f = new DecimalFormat("0"); f.setDecimalSeparatorAlwaysShown(true); return f.format(5) + " " + f.isDecimalSeparatorAlwaysShown() + " " + f.toPattern(); });
    probe("prefix-suffix", () -> { DecimalFormat f = new DecimalFormat("0.0"); f.setPositivePrefix("+"); f.setNegativePrefix("neg:"); f.setPositiveSuffix(" units"); f.setNegativeSuffix("!"); return f.format(1.25) + " " + f.format(-1.25) + " " + f.toPattern(); });
    probe("symbols-custom", () -> { DecimalFormatSymbols s = new DecimalFormatSymbols(Locale.US); s.setDecimalSeparator(','); s.setGroupingSeparator('.'); s.setMinusSign('~'); s.setZeroDigit('0'); s.setPercent('p'); s.setInfinity("inf"); s.setNaN("nan"); DecimalFormat f = new DecimalFormat("#,##0.00", s); return f.format(1234567.891) + " " + f.format(-5) + " " + f.format(Double.POSITIVE_INFINITY) + " " + f.format(Double.NaN) + " " + new DecimalFormat("#%", s).format(0.5); });
    probe("symbols-default", () -> { DecimalFormatSymbols s = DecimalFormatSymbols.getInstance(Locale.US); return s.getDecimalSeparator() + "" + s.getGroupingSeparator() + s.getMinusSign() + s.getPercent() + s.getPerMill() + s.getZeroDigit() + s.getDigit() + s.getPatternSeparator() + s.getInfinity() + s.getNaN() + s.getCurrencySymbol() + s.getInternationalCurrencySymbol() + s.getMonetaryDecimalSeparator() + s.getExponentSeparator(); });
    probe("symbols-equals", () -> DecimalFormatSymbols.getInstance(Locale.US).equals(new DecimalFormatSymbols(Locale.US)) + " " + (DecimalFormatSymbols.getInstance(Locale.US).hashCode() == new DecimalFormatSymbols(Locale.US).hashCode()));
    probe("symbols-clone", () -> { DecimalFormatSymbols a = new DecimalFormatSymbols(Locale.US); DecimalFormatSymbols b = (DecimalFormatSymbols) a.clone(); b.setDecimalSeparator('#'); return a.getDecimalSeparator() + " " + b.getDecimalSeparator() + " " + a.equals(b); });
    String[][] parses = {{"#,##0.###", "1,234.5"}, {"#,##0.###", "1234.5abc"}, {"#,##0.###", "-1,234.5"}, {"#,##0.###", "abc"}, {"#,##0.###", ""}, {"#,##0.###", "1e3"}, {"#,##0.###", ".5"}, {"#,##0.###", "1,2,3"}, {"0.00%", "25.5%"}, {"0.00%", "25.5"}, {"$#,##0.00", "$1,234.56"}, {"$#,##0.00", "1,234.56"}, {"#,##0.00;(#,##0.00)", "(1,234.50)"}, {"0", "99999999999999999999"}, {"0", "-0"}, {"0.0", "NaN"}, {"0.0", "∞"}, {"0.0", "-∞"}, {"0", "  12"}, {"0", "12  "}, {"0.0E0", "1.5E3"}, {"0.0E0", "1.5E-3"}, {"0.0E0", "15"}, {"0", "+5"}, {"0", "1 000"}, {"0", "9223372036854775807"}, {"0", "9223372036854775808"}, {"0", "-9223372036854775808"}, {"0.0", "123456789012345678.5"}, {"0.0", "0.00000000000000000001"}};
    for (String[] pt : parses) {
      probe("parse[" + pt[0] + "][" + pt[1] + "]", () -> {
        DecimalFormat f = new DecimalFormat(pt[0], DecimalFormatSymbols.getInstance(Locale.US));
        ParsePosition pos = new ParsePosition(0);
        Number n = f.parse(pt[1], pos);
        return (n == null ? "null" : n.getClass().getSimpleName() + ":" + n) + " " + pos.getIndex() + " " + pos.getErrorIndex();
      });
    }
    probe("parse-throws", () -> { try { NumberFormat.getInstance(Locale.US).parse("xyz"); return "no"; } catch (ParseException e) { return e.getMessage() + " " + e.getErrorOffset(); } });
    probe("parse-integer-only", () -> { NumberFormat f = NumberFormat.getInstance(Locale.US); f.setParseIntegerOnly(true); ParsePosition p = new ParsePosition(0); Number n = f.parse("123.456", p); return n + " " + p.getIndex() + " " + f.isParseIntegerOnly(); });
    probe("parse-bigdecimal", () -> { DecimalFormat f = new DecimalFormat("#,##0.###"); f.setParseBigDecimal(true); ParsePosition p = new ParsePosition(0); Number n = f.parse("1,234,567,890,123,456,789.123456789", p); return n.getClass().getSimpleName() + ":" + n + " " + f.isParseBigDecimal() + " " + p.getIndex(); });
    probe("parse-percent", () -> { try { return NumberFormat.getPercentInstance(Locale.US).parse("45%") + " " + NumberFormat.getCurrencyInstance(Locale.US).parse("$1,000.50") + " " + NumberFormat.getCurrencyInstance(Locale.US).parse("-$5"); } catch (ParseException e) { return e; } });
    probe("parse-object", () -> { try { return NumberFormat.getInstance(Locale.US).parseObject("12.5"); } catch (ParseException e) { return e; } });
    probe("format-object", () -> { NumberFormat f = NumberFormat.getInstance(Locale.US); return f.format((Object) 12345L) + " " + f.format((Object) new BigDecimal("1.5")) + " " + f.format((Object) Integer.valueOf(7)) + " " + f.format((Object) new java.util.concurrent.atomic.AtomicInteger(9)) + " " + f.format((Object) Short.valueOf((short) 3)); });
    probe("format-bad", () -> NumberFormat.getInstance(Locale.US).format("text"));
    probe("format-null", () -> NumberFormat.getInstance(Locale.US).format((Object) null));
    probe("field-pos", () -> { DecimalFormat f = new DecimalFormat("#,##0.00"); FieldPosition fp = new FieldPosition(NumberFormat.FRACTION_FIELD); StringBuffer sb = f.format(1234567.891, new StringBuffer("pre "), fp); FieldPosition ip = new FieldPosition(NumberFormat.INTEGER_FIELD); f.format(1234567.891, new StringBuffer(), ip); return sb + " " + fp.getBeginIndex() + " " + fp.getEndIndex() + " " + ip.getBeginIndex() + " " + ip.getEndIndex(); });
    probe("apply-pattern", () -> { DecimalFormat f = new DecimalFormat(); f.applyPattern("#,##0.0#"); String a = f.format(1234.5); f.applyPattern("0000"); return a + " " + f.format(7) + " " + f.toPattern(); });
    probe("bad-pattern", () -> new DecimalFormat("#,##0.0.0"));
    probe("bad-pattern2", () -> new DecimalFormat("0#"));
    probe("bad-pattern3", () -> new DecimalFormat("0.0E"));
    probe("bad-pattern4", () -> new DecimalFormat("'unterminated"));
    probe("bad-pattern5", () -> new DecimalFormat("#,##0;"));
    probe("default-ctor", () -> { DecimalFormat f = new DecimalFormat(); return f.toPattern() + " " + f.format(1234.56789); });
    probe("equals", () -> { DecimalFormat a = new DecimalFormat("#,##0.00"), b = new DecimalFormat("#,##0.00"), c = new DecimalFormat("0.0"); return a.equals(b) + " " + a.equals(c) + " " + (a.hashCode() == b.hashCode()) + " " + a.equals(a.clone()) + " " + a.equals(null); });
    probe("clone", () -> { DecimalFormat a = new DecimalFormat("0.0"); DecimalFormat b = (DecimalFormat) a.clone(); b.applyPattern("0.000"); return a.format(1.23456) + " " + b.format(1.23456); });
    probe("currency", () -> { NumberFormat f = NumberFormat.getCurrencyInstance(Locale.US); return f.getCurrency() + " " + f.getMaximumFractionDigits() + " " + f.getMinimumFractionDigits(); });
    probe("currency-set", () -> { NumberFormat f = NumberFormat.getCurrencyInstance(Locale.US); f.setCurrency(Currency.getInstance("JPY")); return f.format(1234.5) + " " + f.getMaximumFractionDigits(); });
    probe("currency-eur", () -> { NumberFormat f = NumberFormat.getCurrencyInstance(Locale.US); f.setCurrency(Currency.getInstance("EUR")); return f.format(1234.5); });
    probe("currency-intl", () -> new DecimalFormat("¤¤ #,##0.00", DecimalFormatSymbols.getInstance(Locale.US)).format(1234.5));
    probe("rounding-mode-default", () -> new DecimalFormat("0").getRoundingMode() + " " + NumberFormat.getInstance(Locale.US).getRoundingMode());
    probe("scientific-parse-pattern", () -> { DecimalFormat f = new DecimalFormat("0.###E0"); return f.toPattern() + " " + f.format(0) + " " + f.format(12345) + " " + f.format(-0.00012) + " " + f.format(1); });
    probe("eng-notation", () -> { DecimalFormat f = new DecimalFormat("##0.###E0"); StringBuilder sb = new StringBuilder(); for (double v : new double[] {1, 12, 123, 1234, 12345, 123456, 0.1, 0.01, 0.001, 0.0001}) sb.append(f.format(v)).append(' '); return sb; });
    probe("available-locales", () -> NumberFormat.getAvailableLocales().length > 0);
    probe("long-double", () -> { DecimalFormat f = new DecimalFormat("0.#"); return f.format(1e18) + " " + f.format(123456789012345.67) + " " + f.format(Double.MAX_VALUE).length() + " " + f.format(Double.MIN_VALUE) + " " + f.format(1.0E-5); });
    probe("neg-zero", () -> new DecimalFormat("0.0").format(-0.01) + " " + new DecimalFormat("0.0").format(-0.0) + " " + new DecimalFormat("0").format(-0.4) + " " + new DecimalFormat("0;(0)").format(-0.4));
    probe("min-max-set", () -> { DecimalFormat f = new DecimalFormat("0.00"); f.setMaximumFractionDigits(5); return f.format(1.123456789) + " " + f.getMinimumFractionDigits() + " " + f.toPattern(); });
    probe("min-greater-max", () -> { DecimalFormat f = new DecimalFormat("0.00"); f.setMinimumFractionDigits(7); return f.getMaximumFractionDigits() + " " + f.format(1); });
    probe("locale-specific", () -> NumberFormat.getInstance(Locale.GERMANY).format(1234567.891) + " " + NumberFormat.getInstance(Locale.FRANCE).format(1234567.891) + " " + NumberFormat.getCurrencyInstance(Locale.GERMANY).format(1234.5));
    probe("message-format", () -> MessageFormat.format("{0} has {1,number,integer} items costing {2,number,currency} on {3}", "Bob", 1234, 5.5, "x"));
    probe("message-format-quote", () -> MessageFormat.format("it''s {0} '{1}' '{'x'}'", "ok"));
    probe("message-format-missing", () -> MessageFormat.format("{0} {1} {2}", "a"));
    probe("message-format-null", () -> MessageFormat.format("{0} {1}", null, "b"));
    probe("message-format-choice", () -> { String p = "There {0,choice,0#are no files|1#is one file|1<are {0,number,integer} files}."; return MessageFormat.format(p, 0) + " " + MessageFormat.format(p, 1) + " " + MessageFormat.format(p, 1234); });
    probe("message-format-date", () -> { MessageFormat f = new MessageFormat("{0,date,yyyy-MM-dd} {0,time,HH:mm}", Locale.US); f.setFormatByArgumentIndex(0, new SimpleDateFormat("yyyy/MM/dd", Locale.US)); return f.toPattern(); });
    probe("message-format-bad", () -> new MessageFormat("{0"));
    probe("message-format-bad2", () -> new MessageFormat("{x}"));
    probe("message-format-bad3", () -> new MessageFormat("{0,bogus}"));
    probe("message-format-parse", () -> { try { return Arrays.toString(new MessageFormat("{0} is {1}").parse("Bob is cool")); } catch (ParseException e) { return e; } });
    probe("message-format-parse-bad", () -> { try { return Arrays.toString(new MessageFormat("{0} is {1}").parse("nothing")); } catch (ParseException e) { return e.getMessage() + " " + e.getErrorOffset(); } });
    probe("message-format-formats", () -> { MessageFormat f = new MessageFormat("{0,number,#.#} {1}"); return f.getFormats().length + " " + f.getFormatsByArgumentIndex().length + " " + f.toPattern(); });
    probe("message-format-locale", () -> { MessageFormat f = new MessageFormat("{0,number}", Locale.GERMANY); return f.format(new Object[] {1234.5}) + " " + f.getLocale(); });
    probe("message-format-number-sub", () -> MessageFormat.format("{0,number,#.##} {0,number,percent} {0,number}", 0.256));
  }
}
