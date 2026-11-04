/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 21

import java.math.*;
import java.text.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.*;
import java.util.*;
import java.util.regex.*;

public class AuditTextModern {
    static void p(Object s) { System.out.println(s); }

    public static void main(String[] args) throws Exception {
        String[] tags = {"en-US", "en-GB", "de-DE", "fr-FR", "es-ES", "it-IT", "ja-JP", "ko-KR", "zh-CN", "zh-TW", "ru-RU", "pt-BR", "ar-SA", "hi-IN", "tr-TR", "pl-PL", "nl-NL", "sv-SE", "th-TH", "he-IL", "cs-CZ", "el-GR", "fi-FI", "da-DK", "uk-UA", "vi-VN", "id-ID", "hu-HU", "ro-RO", "nb-NO"};
        ZonedDateTime zdt = ZonedDateTime.of(2024, 3, 9, 14, 5, 7, 123_000_000, ZoneId.of("Europe/Paris"));
        for (String tag : tags) {
            Locale l = Locale.forLanguageTag(tag);
            StringBuilder sb = new StringBuilder(tag).append(" | ");
            for (FormatStyle fs : FormatStyle.values()) {
                try { sb.append(DateTimeFormatter.ofLocalizedDate(fs).withLocale(l).format(zdt)).append(" | "); } catch (Exception e) { sb.append("E "); }
            }
            sb.append(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(l).format(zdt)).append(" | ");
            sb.append(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy G a", l).format(zdt)).append(" | ");
            sb.append(NumberFormat.getInstance(l).format(1234567.891)).append(" | ");
            sb.append(NumberFormat.getCurrencyInstance(l).format(-1234.5)).append(" | ");
            sb.append(NumberFormat.getPercentInstance(l).format(0.256)).append(" | ");
            sb.append(NumberFormat.getCompactNumberInstance(l, NumberFormat.Style.SHORT).format(12_345_678)).append(" | ");
            sb.append(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, l).format(Date.from(zdt.toInstant()))).append(" | ");
            sb.append(l.getDisplayName(Locale.ENGLISH)).append(" | ").append(l.getDisplayName(l)).append(" | ");
            sb.append(Currency.getInstance(l).getSymbol(l)).append(" | ");
            sb.append(String.format(l, "%,.2f %tB %<tA", 98765.4321, zdt)).append(" | ");
            sb.append(String.format(l, "%s", "ıİiI".toUpperCase(l))).append(" | ");
            sb.append(Collator.getInstance(l).compare("a", "B")).append(" | ");
            sb.append(new DecimalFormatSymbols(l).getDecimalSeparator()).append(new DecimalFormatSymbols(l).getGroupingSeparator());
            p(sb);
        }
        p(ChoiceFormat.nextDouble(1.0) + " " + new ChoiceFormat("0#none|1#one|1<many").format(5));
        p(MessageFormat.format("{0,number,#.##} {1,date,short} {2} {3,choice,0#zero|1#one|1<many {3}}", 3.14159, new Date(0L), "x", 7));
        p(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ zzz", Locale.US).format(new Date(86400000L * 365)));
        p(DateTimeFormatter.ISO_WEEK_DATE.format(LocalDate.of(2021, 1, 3)));
        p(DateTimeFormatter.RFC_1123_DATE_TIME.format(zdt) + " " + DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(zdt));
        p(Duration.ofSeconds(3725, 5).toString() + " " + Period.between(LocalDate.of(2000, 2, 29), LocalDate.of(2024, 3, 1)));
        p(LocalDate.of(2024, 1, 31).plusMonths(1) + " " + LocalDate.of(2024, 12, 30).get(IsoFields.WEEK_OF_WEEK_BASED_YEAR) + " " + LocalDate.of(2024, 3, 9).with(TemporalAdjusters.lastInMonth(DayOfWeek.FRIDAY)));
        p(ZoneId.of("America/New_York").getRules().getTransitions().size() + " " + ZoneId.of("Asia/Kolkata").getRules().getOffset(Instant.EPOCH));
        p(zdt.withZoneSameInstant(ZoneId.of("Pacific/Auckland")) + " " + zdt.format(DateTimeFormatter.ofPattern("zzzz VV O xxx", Locale.FRANCE)));
        p(Year.isLeap(1900) + " " + YearMonth.of(2024, 2).lengthOfMonth() + " " + java.time.chrono.JapaneseDate.of(2024, 3, 9) + " " + java.time.chrono.ThaiBuddhistDate.from(zdt) + " " + java.time.chrono.HijrahDate.from(zdt) + " " + java.time.chrono.MinguoDate.from(zdt));

        int[] fl = {0, Pattern.CASE_INSENSITIVE, Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE, Pattern.MULTILINE | Pattern.DOTALL, Pattern.COMMENTS, Pattern.UNICODE_CHARACTER_CLASS, Pattern.CANON_EQ, Pattern.LITERAL};
        String[] pats = {"a.c$", "straße", "\\w+\\s\\d{2,3}", "(?<x>[a-z]+)(?:\\1|-)", "\\p{L}+\\p{N}", "\\b\\w{3}\\b", "é", "(?i)K", "[^\\x00-\\x7F]+", "\\R", "\\X", "(a|ab)(c|bcd)(d*)", "(?<!a)b++c*?", "[\\p{IsGreek}&&[^α]]+"};
        String[] ins = {"abc\nabc", "STRASSE straße", "word 12 abc 123 ", "ab-ab", "héllo5 日本語1", "the cat\ncot", "é É", "k K K", "añb日c", "a\r\nb\nc d", "👨‍👩‍👧x🇫🇷", "abcd", "abbbc", "αβγδ ωψ abc"};
        for (String pt : pats) {
            for (int f : fl) {
                if (f == Pattern.CANON_EQ && pt.startsWith("[^")) continue;
                StringBuilder sb = new StringBuilder();
                try {
                    Pattern pat = Pattern.compile(pt, f);
                    for (String in : ins) {
                        Matcher m = pat.matcher(in);
                        int c = 0; StringBuilder g = new StringBuilder();
                        while (m.find() && c++ < 5) g.append(m.start()).append('-').append(m.end()).append(' ');
                        sb.append(c).append(':').append(g).append(m.hitEnd()).append(';');
                    }
                } catch (PatternSyntaxException e) { sb.append("PSE ").append(e.getDescription()).append(e.getIndex()); }
                p(pt + " f" + f + " " + sb);
            }
        }
        p("a1b22c333".replaceAll("(\\d+)", "<$1>") + " " + "x".repeat(3).replaceFirst("x", "\\$") + " " + Arrays.toString("a,b,,c,,".split(",")) + Arrays.toString("a,b,,c,,".split(",", -1)) + Arrays.toString("1a2b3".split("(?<=\\d)")));
        p(Pattern.compile("(?<n>\\d+)").matcher("a12b").results().map(r -> r.group()).toList() + " " + Pattern.compile("x*").matcher("axxb").replaceAll("-") + " " + Pattern.quote("a.b") + " " + Matcher.quoteReplacement("$\\"));

        p(String.format("%08.3f|%-8d|%+d|%x|%X|%o|%e|%E|%g|%a|%b|%c|%10.4s|%%|%n", 3.14159, 42, 5, 255, 255, 8, 12345.6789, 0.00012, 0.0001234, 1.5, null, 'z', "abcdefgh").replace("\n", "N"));
        p(String.format("%1$s %1$S %2$tY %2$tm %2$td %2$tH:%2$tM:%2$tS.%2$tL %2$tZ %2$tz %2$ts %2$tp %2$tj %2$te", "str", zdt));
        p(String.format("%,d %(,.2f %#x %#o % d %s %h %-6b|", 1234567890123L, -1234.567, 255, 8, 42, new int[0].length, "hi", true));
        p(String.format("%.0f %.1f %.2f %.3f %10.2e %s %s", 0.5, 0.25, 1.005, 2.0005, 123456.789, 1.0E10, 100.0f));
        p(new Formatter(Locale.GERMANY).format("%,.3f %tc", 1234.5678, zdt).toString());
        try { String.format("%d", "x"); } catch (IllegalFormatException e) { p(e.getClass().getSimpleName() + " " + e.getMessage()); }
        try { String.format("%q", 1); } catch (IllegalFormatException e) { p(e.getClass().getSimpleName() + " " + e.getMessage()); }
        p("  hi  ".strip() + "|" + " a\n b\n".indent(2).replace("\n", "N") + "|" + "TitleCase".chars().filter(Character::isUpperCase).count() + "|" + "ß".toUpperCase() + "İ".toLowerCase().length() + "ǅ".toUpperCase() + "ǅ".toLowerCase() + "Σ".toLowerCase() + "ΑΣ".toLowerCase() + "|" + "a\tb".translateEscapes() + "\\n".translateEscapes().length());
        p("lines\r\nof\rtext\n".lines().toList() + " " + "%d items".formatted(3) + " " + String.join("/", List.of("a", "b")) + " " + "abc".compareToIgnoreCase("ABD") + " " + "ǆ".equalsIgnoreCase("Ǆ") + " " + "Hello".regionMatches(true, 1, "ELL", 0, 3) + " " + "a-b".transform(s -> s + "!"));
        StringBuilder sb2 = new StringBuilder("hello");
        sb2.insert(2, 3.5).reverse().appendCodePoint(0x1F600).setCharAt(0, 'X');
        sb2.replace(1, 3, "__").deleteCharAt(0).delete(0, 1);
        p(sb2 + " " + sb2.length() + " " + sb2.codePointCount(0, sb2.length()) + " " + sb2.indexOf("l") + " " + sb2.lastIndexOf("l") + " " + sb2.capacity());
        sb2.setLength(2); sb2.ensureCapacity(100); sb2.trimToSize(); p(sb2.capacity() + " " + new StringBuilder("ab").compareTo(new StringBuilder("ac")));

        BigDecimal a = new BigDecimal("123456789.123456789"), b = new BigDecimal("0.000300");
        p(a.multiply(b) + " " + a.divide(b, MathContext.DECIMAL64) + " " + a.divide(b, 5, RoundingMode.HALF_EVEN) + " " + b.stripTrailingZeros() + " " + b.stripTrailingZeros().toPlainString() + " " + a.sqrt(MathContext.DECIMAL128) + " " + b.pow(3) + " " + a.remainder(b) + " " + a.setScale(-3, RoundingMode.CEILING) + " " + a.toBigInteger() + " " + new BigDecimal(0.1) + " " + BigDecimal.valueOf(0.1) + " " + new BigDecimal("1E+3").toPlainString() + " " + new BigDecimal("1E+3").toEngineeringString() + " " + new BigDecimal("12345E-10").toEngineeringString());
        for (RoundingMode rm : RoundingMode.values()) if (rm != RoundingMode.UNNECESSARY) p(rm + " " + new BigDecimal("2.5").setScale(0, rm) + new BigDecimal("-2.5").setScale(0, rm) + new BigDecimal("1.15").setScale(1, rm) + new BigDecimal("-1.25").setScale(1, rm));
        try { new BigDecimal(1).divide(new BigDecimal(3)); } catch (ArithmeticException e) { p(e.getMessage()); }
        BigInteger x = new BigInteger("123456789012345678901234567890"), y = BigInteger.valueOf(97).pow(50);
        p(x.multiply(y).mod(BigInteger.valueOf(1000000007)) + " " + y.bitLength() + " " + y.toString(36) + " " + x.gcd(y) + " " + x.modPow(y, BigInteger.valueOf(1_000_003)) + " " + BigInteger.valueOf(1_000_003).modInverse(BigInteger.valueOf(97)) + " " + x.sqrt() + " " + x.isProbablePrime(20) + " " + new BigInteger("-ff", 16) + " " + x.shiftLeft(77).shiftRight(80) + " " + x.negate().and(y).or(BigInteger.TEN).xor(BigInteger.ONE) + " " + x.bitCount() + " " + x.getLowestSetBit() + " " + x.nextProbablePrime() + " " + Arrays.toString(x.divideAndRemainder(y)) + " " + x.toByteArray().length + " " + y.hashCode() + " " + x.doubleValue() + " " + x.negate().longValue());
        p(new BigInteger("1".repeat(300)).multiply(new BigInteger("9".repeat(300))).toString().hashCode() + " " + new BigInteger("7".repeat(2000)).pow(3).bitLength() + " " + BigInteger.TWO.pow(100000).mod(BigInteger.valueOf(999983)) + " " + new BigInteger("3".repeat(1500)).divide(new BigInteger("7".repeat(700))).bitLength());
    }
}
