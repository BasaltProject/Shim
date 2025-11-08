/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.time.*;
import java.time.chrono.*;
import java.time.format.*;
import java.time.temporal.*;
import java.util.*;

public class JapaneseChronologyModern {
static class A {
  static void t(String n, java.util.concurrent.Callable<Object> c) { try { Object o = c.call(); System.out.println(n + ": " + o); } catch (Throwable e) { System.out.println(n + ": !" + e.getClass().getName() + ":" + e.getMessage()); } }
  public static void run() throws Exception {
    Locale.setDefault(Locale.ENGLISH);
    JapaneseChronology c = JapaneseChronology.INSTANCE;
    t("id", () -> c.getId() + " " + c.getCalendarType() + " " + c + " " + c.equals(Chronology.of("Japanese")) + " " + Chronology.of("japanese"));
    t("eras", () -> c.eras());
    t("eravals", () -> { StringBuilder sb = new StringBuilder(); for (JapaneseEra e : JapaneseEra.values()) sb.append(e).append("=").append(e.getValue()).append(" "); return sb; });
    t("eraOf", () -> c.eraOf(-1) + " " + c.eraOf(0) + " " + c.eraOf(1) + " " + c.eraOf(2) + " " + c.eraOf(3));
    t("eraOfBad", () -> c.eraOf(-2));
    t("eraOfBad2", () -> c.eraOf(4));
    t("jeraOf", () -> JapaneseEra.of(3) + " " + JapaneseEra.valueOf("Heisei") + " " + JapaneseEra.REIWA.equals(JapaneseEra.of(3)));
    t("jeraOfBad", () -> JapaneseEra.of(99));
    t("jeraOfBad2", () -> JapaneseEra.valueOf("Nope"));
    t("jeraOfBad3", () -> JapaneseEra.valueOf("REIWA"));
    t("eraField", () -> JapaneseEra.HEISEI.range(ChronoField.ERA) + " " + JapaneseEra.HEISEI.isSupported(ChronoField.ERA) + " " + JapaneseEra.HEISEI.getLong(ChronoField.ERA) + " " + JapaneseEra.HEISEI.isSupported(ChronoField.YEAR));
    t("eraYear", () -> JapaneseEra.HEISEI.getLong(ChronoField.YEAR));
    t("eraName", () -> { StringBuilder sb = new StringBuilder(); for (JapaneseEra e : JapaneseEra.values()) sb.append(e.getDisplayName(TextStyle.FULL, Locale.ENGLISH)).append("/").append(e.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)).append("/").append(e.getDisplayName(TextStyle.NARROW, Locale.ENGLISH)).append("/").append(e.getDisplayName(TextStyle.FULL, Locale.JAPANESE)).append(" "); return sb; });
    t("prolepticYear", () -> c.prolepticYear(JapaneseEra.HEISEI, 5) + " " + c.prolepticYear(JapaneseEra.REIWA, 1) + " " + c.prolepticYear(JapaneseEra.MEIJI, 6) + " " + c.prolepticYear(JapaneseEra.SHOWA, 64));
    t("prolepticYearBad", () -> c.prolepticYear(JapaneseEra.SHOWA, 65));
    t("prolepticYearBad2", () -> c.prolepticYear(JapaneseEra.MEIJI, 1));
    t("prolepticYearBad3", () -> c.prolepticYear(IsoEra.CE, 5));
    t("prolepticYearBad4", () -> c.prolepticYear(JapaneseEra.HEISEI, 0));
    t("leap", () -> c.isLeapYear(2020) + " " + c.isLeapYear(1900) + " " + c.isLeapYear(2000));
    t("of1", () -> JapaneseDate.of(2020, 5, 3));
    t("of2", () -> JapaneseDate.of(JapaneseEra.REIWA, 2, 5, 3));
    t("of3", () -> JapaneseDate.of(JapaneseEra.HEISEI, 31, 4, 30));
    t("of4", () -> JapaneseDate.of(JapaneseEra.HEISEI, 31, 5, 1));
    t("of5", () -> JapaneseDate.of(JapaneseEra.HEISEI, 1, 1, 7));
    t("of6", () -> JapaneseDate.of(JapaneseEra.HEISEI, 1, 1, 8));
    t("of7", () -> JapaneseDate.of(1872, 12, 31));
    t("of8", () -> JapaneseDate.of(1873, 1, 1));
    t("of9", () -> JapaneseDate.of(JapaneseEra.SHOWA, 64, 1, 7));
    t("of10", () -> JapaneseDate.of(JapaneseEra.SHOWA, 64, 1, 8));
    t("of11", () -> JapaneseDate.of(JapaneseEra.MEIJI, 6, 1, 1));
    t("of12", () -> JapaneseDate.of(JapaneseEra.MEIJI, 5, 12, 31));
    t("of13", () -> JapaneseDate.of(JapaneseEra.TAISHO, 1, 7, 30));
    t("of14", () -> JapaneseDate.of(JapaneseEra.TAISHO, 1, 7, 29));
    t("of15", () -> JapaneseDate.of(2019, 4, 30) + " " + JapaneseDate.of(2019, 5, 1) + " " + JapaneseDate.of(1989, 1, 7) + " " + JapaneseDate.of(1989, 1, 8) + " " + JapaneseDate.of(1926, 12, 24) + " " + JapaneseDate.of(1926, 12, 25) + " " + JapaneseDate.of(1912, 7, 29) + " " + JapaneseDate.of(1912, 7, 30));
    t("cdate", () -> c.date(JapaneseEra.REIWA, 6, 2, 29) + " " + c.date(2024, 2, 29) + " " + c.dateYearDay(2024, 60) + " " + c.dateYearDay(JapaneseEra.REIWA, 6, 60) + " " + c.dateEpochDay(19000));
    t("cdateBad", () -> c.date(2023, 2, 29));
    t("cdateBad2", () -> c.date(JapaneseEra.REIWA, 1, 4, 30));
    t("cdateBad3", () -> c.dateYearDay(JapaneseEra.REIWA, 1, 1));
    t("cdateBad4", () -> c.dateYearDay(JapaneseEra.HEISEI, 1, 1));
    t("cdateFrom", () -> c.date(LocalDate.of(2020, 5, 3)) + " " + JapaneseDate.from(LocalDate.of(1990, 1, 1)) + " " + c.date(JapaneseDate.of(2020, 1, 1)));
    t("cdateFromBad", () -> c.date(LocalDate.of(1850, 1, 1)));
    t("cdateFromBad2", () -> c.date(HijrahDate.now()));
    t("ldt", () -> c.localDateTime(LocalDateTime.of(2020, 5, 3, 1, 2)));
    t("zdt", () -> c.zonedDateTime(Instant.ofEpochSecond(1600000000L), ZoneOffset.UTC));
    JapaneseDate d = JapaneseDate.of(JapaneseEra.HEISEI, 31, 4, 30);
    JapaneseDate r = JapaneseDate.of(2020, 5, 3);
    t("fields", () -> r.getEra() + " " + r.get(ChronoField.YEAR_OF_ERA) + " " + r.get(ChronoField.YEAR) + " " + r.get(ChronoField.ERA) + " " + r.getLong(ChronoField.PROLEPTIC_MONTH) + " " + r.get(ChronoField.DAY_OF_YEAR) + " " + r.lengthOfMonth() + " " + r.lengthOfYear() + " " + r.isLeapYear() + " " + r.get(ChronoField.DAY_OF_WEEK) + " " + r.toEpochDay() + " " + r.getChronology());
    t("ranges", () -> { StringBuilder sb = new StringBuilder(); for (JapaneseDate x : new JapaneseDate[] {JapaneseDate.of(JapaneseEra.HEISEI, 1, 1, 8), JapaneseDate.of(JapaneseEra.HEISEI, 1, 6, 15), JapaneseDate.of(JapaneseEra.HEISEI, 31, 4, 30), JapaneseDate.of(JapaneseEra.REIWA, 1, 5, 1), JapaneseDate.of(JapaneseEra.REIWA, 1, 8, 1), JapaneseDate.of(JapaneseEra.REIWA, 2, 2, 1), JapaneseDate.of(JapaneseEra.SHOWA, 64, 1, 3), JapaneseDate.of(JapaneseEra.SHOWA, 1, 12, 26), JapaneseDate.of(JapaneseEra.MEIJI, 6, 6, 6), JapaneseDate.of(JapaneseEra.MEIJI, 45, 7, 29), JapaneseDate.of(JapaneseEra.TAISHO, 1, 8, 1)}) { sb.append(x).append(" | "); for (ChronoField f : new ChronoField[] {ChronoField.YEAR_OF_ERA, ChronoField.DAY_OF_YEAR, ChronoField.MONTH_OF_YEAR, ChronoField.DAY_OF_MONTH, ChronoField.ERA, ChronoField.YEAR, ChronoField.PROLEPTIC_MONTH, ChronoField.ALIGNED_WEEK_OF_YEAR, ChronoField.ALIGNED_WEEK_OF_MONTH}) { try { sb.append(f).append("=").append(x.range(f)).append(" "); } catch (Exception e) { sb.append(f).append("!").append(e.getClass().getSimpleName()).append(" "); } } sb.append(x.lengthOfMonth()).append("/").append(x.lengthOfYear()).append('\n'); } return sb; });
    t("chronoRanges", () -> { StringBuilder sb = new StringBuilder(); for (ChronoField f : ChronoField.values()) { try { sb.append(f).append("=").append(c.range(f)).append(" "); } catch (Exception e) { sb.append(f).append("!").append(e.getClass().getSimpleName()).append(" "); } } return sb; });
    t("plus", () -> d.plus(1, ChronoUnit.DAYS) + " " + d.plus(1, ChronoUnit.MONTHS) + " " + d.plus(1, ChronoUnit.YEARS) + " " + d.plus(1, ChronoUnit.WEEKS) + " " + d.plus(1, ChronoUnit.DECADES) + " " + d.minus(1, ChronoUnit.DAYS) + " " + d.plus(1, ChronoUnit.CENTURIES) + " " + d.plus(1, ChronoUnit.ERAS));
    t("plusBad", () -> d.plus(1, ChronoUnit.HOURS));
    t("plusBad2", () -> JapaneseDate.of(1873, 1, 1).minus(1, ChronoUnit.DAYS));
    t("plusBad3", () -> JapaneseDate.of(1873, 6, 1).minus(1, ChronoUnit.YEARS));
    t("plusEraBoundary", () -> JapaneseDate.of(JapaneseEra.HEISEI, 31, 4, 30).plus(1, ChronoUnit.DAYS) + " " + JapaneseDate.of(JapaneseEra.REIWA, 1, 5, 1).minus(1, ChronoUnit.DAYS) + " " + JapaneseDate.of(JapaneseEra.HEISEI, 31, 4, 30).plus(2, ChronoUnit.YEARS));
    t("plusMonthsEnd", () -> JapaneseDate.of(2020, 1, 31).plus(1, ChronoUnit.MONTHS) + " " + JapaneseDate.of(2020, 2, 29).plus(1, ChronoUnit.YEARS));
    t("with1", () -> d.with(ChronoField.YEAR_OF_ERA, 10) + " " + d.with(ChronoField.YEAR, 1995) + " " + d.with(ChronoField.MONTH_OF_YEAR, 2) + " " + d.with(ChronoField.DAY_OF_MONTH, 1) + " " + d.with(ChronoField.DAY_OF_YEAR, 100) + " " + d.with(ChronoField.ERA, 1) + " " + d.with(ChronoField.EPOCH_DAY, 10000));
    t("with2", () -> d.with(ChronoField.ERA, 3));
    t("with3", () -> d.with(ChronoField.YEAR_OF_ERA, 1));
    t("with4", () -> d.with(ChronoField.YEAR_OF_ERA, 99));
    t("with5", () -> JapaneseDate.of(JapaneseEra.REIWA, 3, 1, 1).with(ChronoField.YEAR_OF_ERA, 1));
    t("with6", () -> JapaneseDate.of(JapaneseEra.HEISEI, 5, 1, 1).with(ChronoField.YEAR_OF_ERA, 1));
    t("with7", () -> JapaneseDate.of(JapaneseEra.HEISEI, 5, 3, 1).with(ChronoField.YEAR_OF_ERA, 1));
    t("with8", () -> JapaneseDate.of(JapaneseEra.SHOWA, 40, 6, 1).with(ChronoField.YEAR_OF_ERA, 64));
    t("with9", () -> d.with(ChronoField.HOUR_OF_DAY, 1));
    t("with10", () -> d.with(LocalDate.of(2000, 1, 1)) + " " + d.with(TemporalAdjusters.firstDayOfMonth()) + " " + d.with(TemporalAdjusters.lastDayOfYear()));
    t("with11", () -> d.with(ChronoField.ERA, 2));
    t("with12", () -> d.with(ChronoField.ERA, 0));
    t("with13", () -> JapaneseDate.of(1989, 1, 8).with(ChronoField.ERA, 1));
    t("with14", () -> JapaneseDate.of(2019, 5, 10).with(ChronoField.ERA, 2));
    t("until", () -> JapaneseDate.of(2020, 5, 3).until(JapaneseDate.of(2024, 7, 10)) + " " + JapaneseDate.of(2020, 5, 3).until(LocalDate.of(2024, 7, 10)) + " " + JapaneseDate.of(2020, 5, 3).until(JapaneseDate.of(2024, 7, 10), ChronoUnit.DAYS) + " " + JapaneseDate.of(2020, 5, 3).until(JapaneseDate.of(2024, 7, 10), ChronoUnit.MONTHS) + " " + JapaneseDate.of(2020, 5, 3).until(JapaneseDate.of(2024, 7, 10), ChronoUnit.ERAS));
    t("untilBack", () -> JapaneseDate.of(2024, 7, 10).until(JapaneseDate.of(2020, 5, 3)));
    t("period", () -> c.period(1, 2, 3));
    t("cmp", () -> JapaneseDate.of(2020, 1, 1).compareTo(JapaneseDate.of(2020, 1, 2)) + " " + JapaneseDate.of(2020, 1, 1).isBefore(JapaneseDate.of(2021, 1, 1)) + " " + JapaneseDate.of(2020, 1, 1).equals(JapaneseDate.of(2020, 1, 1)) + " " + (JapaneseDate.of(2020, 1, 1).hashCode() == JapaneseDate.of(2020, 1, 1).hashCode()) + " " + JapaneseDate.of(2020, 1, 1).equals(LocalDate.of(2020, 1, 1)) + " " + JapaneseDate.of(2020, 1, 1).hashCode());
    t("minmax", () -> JapaneseDate.of(1873, 1, 1).toEpochDay() + " " + JapaneseDate.of(1873, 1, 1).getEra() + " " + JapaneseDate.of(1873, 1, 1).get(ChronoField.YEAR_OF_ERA));
    t("lit", () -> c.date(JapaneseEra.REIWA, 1, 5, 1).atTime(LocalTime.of(5, 6)) + " " + c.date(2020, 5, 3).atTime(LocalTime.NOON).toLocalDate().getClass().getSimpleName());
    t("fmt", () -> DateTimeFormatter.ofPattern("GGGG y-MM-dd").withChronology(c).format(r) + " | " + DateTimeFormatter.ofPattern("G y/M/d").withChronology(c).format(r) + " | " + DateTimeFormatter.ofPattern("GGGGG y").withChronology(c).format(r) + " | " + DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withChronology(c).withLocale(Locale.ENGLISH).format(r) + " | " + DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withChronology(c).withLocale(Locale.ENGLISH).format(r));
    t("parse", () -> JapaneseDate.from(DateTimeFormatter.ofPattern("GGGG y-MM-dd").withChronology(c).parse("Reiwa 2-05-03")) + " " + c.date(DateTimeFormatter.ofPattern("G y-M-d").withChronology(c).parse("Heisei 31-4-30")));
    t("parse2", () -> JapaneseDate.from(DateTimeFormatter.ofPattern("GGGG y-MM-dd").withChronology(c).parse("Heisei 1-01-07")));
    t("parse3", () -> JapaneseDate.from(DateTimeFormatter.ofPattern("GGGG y-MM-dd").withChronology(c).withResolverStyle(ResolverStyle.LENIENT).parse("Heisei 1-01-07")));
    t("parse4", () -> JapaneseDate.from(DateTimeFormatter.ofPattern("GGGG y-MM-dd").withChronology(c).parse("Reiwa 1-04-30")));
    t("parse5", () -> JapaneseDate.from(DateTimeFormatter.ofPattern("GGGG y-MM-dd").withChronology(c).withResolverStyle(ResolverStyle.SMART).parse("Reiwa 1-04-30")));
    t("parseLenient", () -> JapaneseDate.from(DateTimeFormatter.ofPattern("GGGG y-MM-dd").withChronology(c).withResolverStyle(ResolverStyle.LENIENT).parse("Reiwa 1-04-30")));
    t("pattern", () -> DateTimeFormatterBuilder.getLocalizedDateTimePattern(FormatStyle.MEDIUM, null, c, Locale.ENGLISH));
    t("era toString", () -> JapaneseDate.of(2020, 5, 3).getEra().toString() + " " + JapaneseDate.of(2020, 5, 3).getEra().getClass().getName());
    t("serial", () -> { java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream(); try (java.io.ObjectOutputStream o = new java.io.ObjectOutputStream(bo)) { o.writeObject(JapaneseDate.of(2020, 5, 3)); } try (java.io.ObjectInputStream i = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bo.toByteArray()))) { return i.readObject(); } });
    t("serialEra", () -> { java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream(); try (java.io.ObjectOutputStream o = new java.io.ObjectOutputStream(bo)) { o.writeObject(JapaneseEra.HEISEI); } try (java.io.ObjectInputStream i = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bo.toByteArray()))) { return i.readObject() == JapaneseEra.HEISEI; } });
    t("resolve", () -> { Map<TemporalField, Long> m = new HashMap<>(); m.put(ChronoField.ERA, 3L); m.put(ChronoField.YEAR_OF_ERA, 2L); m.put(ChronoField.MONTH_OF_YEAR, 5L); m.put(ChronoField.DAY_OF_MONTH, 3L); return c.resolveDate(m, ResolverStyle.SMART); });
    t("resolve2", () -> { Map<TemporalField, Long> m = new HashMap<>(); m.put(ChronoField.YEAR_OF_ERA, 2L); m.put(ChronoField.MONTH_OF_YEAR, 5L); m.put(ChronoField.DAY_OF_MONTH, 3L); return c.resolveDate(m, ResolverStyle.SMART); });
    t("resolve3", () -> { Map<TemporalField, Long> m = new HashMap<>(); m.put(ChronoField.YEAR, 2020L); m.put(ChronoField.DAY_OF_YEAR, 50L); return c.resolveDate(m, ResolverStyle.STRICT); });
    t("resolve4", () -> { Map<TemporalField, Long> m = new HashMap<>(); m.put(ChronoField.ERA, 2L); m.put(ChronoField.YEAR_OF_ERA, 1L); m.put(ChronoField.DAY_OF_YEAR, 1L); return c.resolveDate(m, ResolverStyle.SMART); });
    t("resolve5", () -> { Map<TemporalField, Long> m = new HashMap<>(); m.put(ChronoField.ERA, 2L); m.put(ChronoField.YEAR_OF_ERA, 1L); m.put(ChronoField.DAY_OF_YEAR, 8L); return c.resolveDate(m, ResolverStyle.SMART); });
    t("resolve6", () -> { Map<TemporalField, Long> m = new HashMap<>(); m.put(ChronoField.ERA, 2L); m.put(ChronoField.YEAR_OF_ERA, 1L); m.put(ChronoField.DAY_OF_YEAR, 400L); return c.resolveDate(m, ResolverStyle.LENIENT); });
    t("resolve7", () -> { Map<TemporalField, Long> m = new HashMap<>(); m.put(ChronoField.ERA, 2L); m.put(ChronoField.YEAR_OF_ERA, 1L); return c.resolveDate(m, ResolverStyle.SMART); });
    t("resolve8", () -> { Map<TemporalField, Long> m = new HashMap<>(); m.put(ChronoField.ERA, 2L); m.put(ChronoField.YEAR_OF_ERA, 1L); m.put(ChronoField.MONTH_OF_YEAR, 1L); m.put(ChronoField.DAY_OF_MONTH, 1L); return c.resolveDate(m, ResolverStyle.LENIENT); });
  }
}

static class B { public static void run() {
  JapaneseDate[] ds = {JapaneseDate.of(JapaneseEra.REIWA, 1, 5, 1), JapaneseDate.of(JapaneseEra.REIWA, 1, 12, 31), JapaneseDate.of(JapaneseEra.REIWA, 2, 1, 1), JapaneseDate.of(JapaneseEra.HEISEI, 1, 1, 8), JapaneseDate.of(JapaneseEra.HEISEI, 31, 4, 30), JapaneseDate.of(JapaneseEra.SHOWA, 1, 12, 25), JapaneseDate.of(JapaneseEra.SHOWA, 64, 1, 7), JapaneseDate.of(JapaneseEra.MEIJI, 6, 1, 1), JapaneseDate.of(JapaneseEra.TAISHO, 1, 7, 30), JapaneseDate.of(JapaneseEra.TAISHO, 15, 12, 24)};
  for (JapaneseDate d : ds) { StringBuilder sb = new StringBuilder(d + ": "); for (ChronoField f : new ChronoField[] {ChronoField.DAY_OF_YEAR, ChronoField.DAY_OF_MONTH, ChronoField.DAY_OF_WEEK, ChronoField.YEAR_OF_ERA, ChronoField.EPOCH_DAY, ChronoField.PROLEPTIC_MONTH, ChronoField.ALIGNED_WEEK_OF_YEAR, ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH}) { try { sb.append(f).append("=").append(d.getLong(f)).append(" "); } catch (Exception e) { sb.append(f).append("!").append(e.getClass().getSimpleName()).append(" "); } } sb.append(d.lengthOfMonth()).append("/").append(d.lengthOfYear()); System.out.println(sb); }
  JapaneseDate r = JapaneseDate.of(JapaneseEra.REIWA, 1, 5, 1);
  for (long n : new long[] {1, 100, 245, 246}) { try { System.out.println("with DOY " + n + " " + r.with(ChronoField.DAY_OF_YEAR, n)); } catch (Exception e) { System.out.println("with DOY " + n + " !" + e.getMessage()); } }
  JapaneseDate h = JapaneseDate.of(JapaneseEra.HEISEI, 1, 3, 4);
  for (long n : new long[] {1, 100, 358, 359}) { try { System.out.println("with DOY " + n + " " + h.with(ChronoField.DAY_OF_YEAR, n)); } catch (Exception e) { System.out.println("with DOY " + n + " !" + e.getMessage()); } }
  for (long n : new long[] {1, 12, 13, 0}) { try { System.out.println("h with MONTH " + n + " " + h.with(ChronoField.MONTH_OF_YEAR, n)); } catch (Exception e) { System.out.println("h with MONTH " + n + " !" + e.getMessage()); } }
  JapaneseDate h1 = JapaneseDate.of(JapaneseEra.HEISEI, 1, 1, 8);
  for (long n : new long[] {1, 7, 8, 31, 32}) { try { System.out.println("h1 with DOM " + n + " " + h1.with(ChronoField.DAY_OF_MONTH, n)); } catch (Exception e) { System.out.println("h1 with DOM " + n + " !" + e.getMessage()); } }
  t("p1", () -> h1.with(ChronoField.MONTH_OF_YEAR, 1) + " " + h1.plus(0, ChronoUnit.MONTHS) + " " + h1.plus(14, ChronoUnit.MONTHS));
  t("p2", () -> h1.minus(Period.ofDays(1)));
  t("p3", () -> JapaneseDate.of(2020, 5, 3).query(TemporalQueries.chronology()) + " " + JapaneseDate.of(2020, 5, 3).query(TemporalQueries.precision()) + " " + JapaneseDate.of(2020, 5, 3).isSupported(ChronoUnit.HOURS) + " " + JapaneseDate.of(2020, 5, 3).isSupported(ChronoUnit.ERAS) + " " + JapaneseDate.of(2020,5,3).isSupported(ChronoField.ERA) + " " + JapaneseDate.of(2020,5,3).isSupported(ChronoField.ALIGNED_WEEK_OF_MONTH) + " " + JapaneseDate.of(2020,5,3).isSupported(ChronoField.EPOCH_DAY));
  t("p4", () -> JapaneseDate.of(2020, 5, 3).plus(1, ChronoUnit.YEARS) + " " + JapaneseDate.of(2020, 5, 3).minus(5, ChronoUnit.MONTHS) + " " + JapaneseDate.of(2020, 5, 3).plus(2, ChronoUnit.WEEKS) + " " + JapaneseDate.of(2020, 5, 3).minus(10, ChronoUnit.DAYS));
  t("p5", () -> LocalDate.from(JapaneseDate.of(2020, 5, 3)) + " " + JapaneseDate.of(2020, 5, 3).toEpochDay() + " " + LocalDate.of(2020,5,3).with(JapaneseDate.of(2021, 1, 1)) + " " + JapaneseDate.of(2020, 5, 3).adjustInto(LocalDate.of(1999,1,1)));
  t("p6", () -> JapaneseDate.of(2020, 5, 3).format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE) + " " + java.time.format.DateTimeFormatter.ISO_LOCAL_DATE.format(JapaneseDate.of(2020, 5, 3)) + " " + JapaneseChronology.INSTANCE.period(0, 0, 0) + " " + JapaneseDate.of(2020, 5, 3).atTime(LocalTime.MIDNIGHT) + " " + JapaneseDate.now().getChronology());
  t("p7", () -> Chronology.getAvailableChronologies().stream().map(Chronology::getId).sorted().toList());
  t("p8", () -> ChronoLocalDate.timeLineOrder().compare(JapaneseDate.of(2020,1,1), LocalDate.of(2020,1,2)) + " " + JapaneseDate.of(2020,1,1).isEqual(LocalDate.of(2020,1,1)) + " " + JapaneseDate.of(2020,1,1).equals(JapaneseDate.from(LocalDate.of(2020,1,1))));
}
  static void t(String n, java.util.concurrent.Callable<Object> c) { try { System.out.println(n + ": " + c.call()); } catch (Throwable e) { System.out.println(n + ": !" + e.getClass().getName() + ":" + e.getMessage()); } }
}

  public static void main(String[] args) throws Exception {
    A.run();
    B.run();
  }
}
