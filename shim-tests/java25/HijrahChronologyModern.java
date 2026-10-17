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

public class HijrahChronologyModern {
static class A {
  static void t(String n, java.util.concurrent.Callable<Object> c) { try { Object o = c.call(); System.out.println(n + ": " + o); } catch (Throwable e) { System.out.println(n + ": !" + e.getClass().getName() + ":" + e.getMessage()); } }
  public static void run() throws Exception {
    Locale.setDefault(Locale.ENGLISH);
    HijrahChronology c = HijrahChronology.INSTANCE;
    t("id", () -> c.getId() + " " + c.getCalendarType() + " " + c + " " + c.equals(Chronology.of("Hijrah-umalqura")) + " " + Chronology.of("Hijrah-umalqura") + " " + Chronology.of("islamic-umalqura") + " " + HijrahChronology.INSTANCE.equals(Chronology.of("Hijrah-umalqura")));
    t("ofBad", () -> Chronology.of("Hijrah"));
    t("eras", () -> c.eras() + " " + c.eraOf(1) + " " + HijrahEra.AH.getValue() + " " + HijrahEra.of(1) + " " + Arrays.toString(HijrahEra.values()));
    t("eraOfBad", () -> c.eraOf(0));
    t("eraOfBad2", () -> HijrahEra.of(2));
    t("eraNames", () -> HijrahEra.AH.getDisplayName(TextStyle.FULL, Locale.ENGLISH) + "/" + HijrahEra.AH.getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + "/" + HijrahEra.AH.getDisplayName(TextStyle.NARROW, Locale.ENGLISH) + "/" + HijrahEra.AH.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("ar")));
    t("eraRange", () -> HijrahEra.AH.range(ChronoField.ERA) + " " + HijrahEra.AH.getLong(ChronoField.ERA));
    t("prolepticYear", () -> c.prolepticYear(HijrahEra.AH, 1445) + " " + c.prolepticYear(HijrahEra.AH, 1300));
    t("prolepticYearBad", () -> c.prolepticYear(HijrahEra.AH, 1299));
    t("prolepticYearBad2", () -> c.prolepticYear(HijrahEra.AH, 1601));
    t("prolepticYearBad3", () -> c.prolepticYear(IsoEra.CE, 1400));
    t("leap", () -> c.isLeapYear(1445) + " " + c.isLeapYear(1444) + " " + c.isLeapYear(1300) + " " + c.isLeapYear(1600) + " " + c.isLeapYear(1299) + " " + c.isLeapYear(1601) + " " + c.isLeapYear(-5));
    t("of1", () -> HijrahDate.of(1445, 9, 1) + " " + c.date(HijrahEra.AH, 1445, 9, 30) + " " + c.date(1445, 12, 29));
    t("of2", () -> HijrahDate.of(1445, 9, 31));
    t("of3", () -> HijrahDate.of(1445, 13, 1));
    t("of4", () -> HijrahDate.of(1299, 1, 1));
    t("of5", () -> HijrahDate.of(1601, 1, 1));
    t("of6", () -> HijrahDate.of(1600, 12, 30) + " " + HijrahDate.of(1600, 12, 29));
    t("of7", () -> HijrahDate.of(1300, 1, 1) + " " + HijrahDate.of(1300, 1, 1).toEpochDay());
    t("of8", () -> HijrahDate.of(1445, 1, 30));
    t("of9", () -> HijrahDate.of(1445, 2, 0));
    t("of10", () -> c.dateYearDay(1445, 355) + " " + c.dateYearDay(1445, 1) + " " + c.dateYearDay(HijrahEra.AH, 1445, 100));
    t("of11", () -> c.dateYearDay(1446, 355));
    t("of12", () -> c.dateYearDay(1445, 0));
    t("of13", () -> c.dateEpochDay(19000) + " " + c.dateEpochDay(0));
    t("of14", () -> c.dateEpochDay(-31827));
    t("of15", () -> c.dateEpochDay(74839));
    t("from", () -> HijrahDate.from(LocalDate.of(2024, 3, 11)) + " " + c.date(LocalDate.of(2024, 3, 12)) + " " + HijrahDate.from(JapaneseDate.of(2020, 5, 3)));
    t("fromBad", () -> HijrahDate.from(LocalDate.of(1800, 1, 1)));
    t("fromBad2", () -> HijrahDate.from(LocalDate.of(2200, 1, 1)));
    t("ldt", () -> c.localDateTime(LocalDateTime.of(2024, 3, 11, 4, 5)) + " " + c.zonedDateTime(Instant.ofEpochSecond(1700000000L), ZoneOffset.UTC));
    HijrahDate d = HijrahDate.of(1445, 9, 15);
    t("fields", () -> d.getEra() + " " + d.getLong(ChronoField.YEAR_OF_ERA) + " " + d.getLong(ChronoField.YEAR) + " " + d.getLong(ChronoField.ERA) + " " + d.getLong(ChronoField.PROLEPTIC_MONTH) + " " + d.getLong(ChronoField.DAY_OF_YEAR) + " " + d.lengthOfMonth() + " " + d.lengthOfYear() + " " + d.isLeapYear() + " " + d.getLong(ChronoField.DAY_OF_WEEK) + " " + d.toEpochDay() + " " + d.getChronology() + " " + d.getLong(ChronoField.ALIGNED_WEEK_OF_MONTH) + " " + d.getLong(ChronoField.ALIGNED_WEEK_OF_YEAR) + " " + d.getLong(ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH) + " " + d.getLong(ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR) + " " + d.getLong(ChronoField.MONTH_OF_YEAR) + " " + d.getLong(ChronoField.DAY_OF_MONTH));
    t("chronoRanges", () -> { StringBuilder sb = new StringBuilder(); for (ChronoField f : ChronoField.values()) { try { sb.append(f).append("=").append(c.range(f)).append(" "); } catch (Exception e) { sb.append(f).append("!").append(e.getClass().getSimpleName()).append(" "); } } return sb; });
    t("ranges", () -> { StringBuilder sb = new StringBuilder(); for (HijrahDate x : new HijrahDate[] {HijrahDate.of(1445, 9, 15), HijrahDate.of(1300, 1, 1), HijrahDate.of(1600, 12, 29), HijrahDate.of(1446, 2, 1)}) { sb.append(x).append(" | "); for (ChronoField f : new ChronoField[] {ChronoField.YEAR_OF_ERA, ChronoField.DAY_OF_YEAR, ChronoField.MONTH_OF_YEAR, ChronoField.DAY_OF_MONTH, ChronoField.ERA, ChronoField.YEAR, ChronoField.PROLEPTIC_MONTH, ChronoField.ALIGNED_WEEK_OF_YEAR, ChronoField.ALIGNED_WEEK_OF_MONTH, ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH, ChronoField.DAY_OF_WEEK, ChronoField.EPOCH_DAY}) { try { sb.append(f).append("=").append(x.range(f)).append(" "); } catch (Exception e) { sb.append(f).append("!").append(e.getClass().getSimpleName()).append(" "); } } sb.append(x.lengthOfMonth()).append("/").append(x.lengthOfYear()).append("\\n"); } return sb; });
    t("plus", () -> d.plus(1, ChronoUnit.DAYS) + " " + d.plus(1, ChronoUnit.MONTHS) + " " + d.plus(1, ChronoUnit.YEARS) + " " + d.plus(1, ChronoUnit.WEEKS) + " " + d.plus(1, ChronoUnit.DECADES) + " " + d.minus(1, ChronoUnit.DAYS) + " " + d.plus(1, ChronoUnit.CENTURIES) + " " + d.plus(1, ChronoUnit.MILLENNIA) + " " + d.plus(0, ChronoUnit.ERAS));
    t("plusEra", () -> d.plus(1, ChronoUnit.ERAS));
    t("plusBad", () -> d.plus(1, ChronoUnit.HOURS));
    t("plusBad2", () -> HijrahDate.of(1300, 1, 1).minus(1, ChronoUnit.DAYS));
    t("plusBad3", () -> HijrahDate.of(1600, 12, 29).plus(2, ChronoUnit.DAYS));
    t("plusBad4", () -> HijrahDate.of(1600, 12, 29).plus(1, ChronoUnit.YEARS));
    t("plusBad5", () -> HijrahDate.of(1300, 1, 1).minus(1, ChronoUnit.MONTHS));
    t("plusEnd", () -> HijrahDate.of(1445, 1, 30).plus(1, ChronoUnit.MONTHS) + " " + HijrahDate.of(1445, 12, 29).plus(1, ChronoUnit.MONTHS) + " " + HijrahDate.of(1444, 12, 30).plus(1, ChronoUnit.YEARS) + " " + HijrahDate.of(1445, 2, 30).plus(12, ChronoUnit.MONTHS) + " " + HijrahDate.of(1445, 2, 30).plus(-1, ChronoUnit.MONTHS));
    t("plusLarge", () -> d.plus(1000, ChronoUnit.MONTHS) + " " + d.plus(100, ChronoUnit.WEEKS) + " " + d.plus(10000, ChronoUnit.DAYS) + " " + d.minus(5000, ChronoUnit.DAYS));
    t("with1", () -> d.with(ChronoField.YEAR_OF_ERA, 1400) + " " + d.with(ChronoField.YEAR, 1500) + " " + d.with(ChronoField.MONTH_OF_YEAR, 2) + " " + d.with(ChronoField.DAY_OF_MONTH, 1) + " " + d.with(ChronoField.DAY_OF_YEAR, 100) + " " + d.with(ChronoField.ERA, 1) + " " + d.with(ChronoField.EPOCH_DAY, 10000) + " " + d.with(ChronoField.DAY_OF_WEEK, 1) + " " + d.with(ChronoField.ALIGNED_WEEK_OF_MONTH, 1) + " " + d.with(ChronoField.ALIGNED_WEEK_OF_YEAR, 3) + " " + d.with(ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH, 2) + " " + d.with(ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR, 2) + " " + d.with(ChronoField.PROLEPTIC_MONTH, 17000));
    t("with2", () -> d.with(ChronoField.YEAR_OF_ERA, 1700));
    t("with3", () -> d.with(ChronoField.DAY_OF_MONTH, 31));
    t("with4", () -> d.with(ChronoField.MONTH_OF_YEAR, 13));
    t("with5", () -> d.with(ChronoField.ERA, 0));
    t("with6", () -> HijrahDate.of(1445, 2, 30).with(ChronoField.MONTH_OF_YEAR, 3));
    t("with7", () -> HijrahDate.of(1445, 2, 30).with(ChronoField.YEAR, 1446));
    t("with8", () -> HijrahDate.of(1444, 12, 30).with(ChronoField.YEAR, 1445));
    t("with9", () -> d.with(ChronoField.HOUR_OF_DAY, 1));
    t("with10", () -> d.with(LocalDate.of(2000, 1, 1)) + " " + d.with(TemporalAdjusters.firstDayOfMonth()) + " " + d.with(TemporalAdjusters.lastDayOfYear()) + " " + d.with(TemporalAdjusters.next(DayOfWeek.MONDAY)));
    t("with11", () -> d.with(ChronoField.DAY_OF_YEAR, 356));
    t("with12", () -> d.with(ChronoField.DAY_OF_YEAR, 355));
    t("with13", () -> HijrahDate.of(1444, 1, 1).with(ChronoField.DAY_OF_YEAR, 355));
    t("with14", () -> d.with(ChronoField.YEAR, 1299));
    t("until", () -> d.until(HijrahDate.of(1447, 11, 20)) + " " + d.until(LocalDate.of(2030, 1, 1)) + " " + d.until(HijrahDate.of(1447, 11, 20), ChronoUnit.DAYS) + " " + d.until(HijrahDate.of(1447, 11, 20), ChronoUnit.MONTHS) + " " + d.until(HijrahDate.of(1447, 11, 20), ChronoUnit.YEARS) + " " + d.until(HijrahDate.of(1447, 11, 20), ChronoUnit.WEEKS) + " " + d.until(HijrahDate.of(1447, 11, 20), ChronoUnit.DECADES) + " " + d.until(HijrahDate.of(1447, 11, 20), ChronoUnit.ERAS));
    t("untilBack", () -> HijrahDate.of(1447, 11, 20).until(d));
    t("untilEdge", () -> HijrahDate.of(1445, 1, 30).until(HijrahDate.of(1445, 3, 1)) + " " + HijrahDate.of(1445, 1, 30).until(HijrahDate.of(1445, 2, 29)) + " " + HijrahDate.of(1445, 1, 15).until(HijrahDate.of(1445, 1, 15)));
    t("period", () -> c.period(1, 2, 3));
    t("cmp", () -> d.compareTo(HijrahDate.of(1445, 9, 16)) + " " + d.isBefore(HijrahDate.of(1446, 1, 1)) + " " + d.equals(HijrahDate.of(1445, 9, 15)) + " " + (d.hashCode() == HijrahDate.of(1445, 9, 15).hashCode()) + " " + d.equals(LocalDate.of(2024, 3, 25)) + " " + d.hashCode());
    t("toString", () -> d + " " + HijrahDate.of(1300, 1, 1) + " " + HijrahDate.of(1600, 12, 30));
    t("iso", () -> LocalDate.from(d) + " " + LocalDate.ofEpochDay(d.toEpochDay()) + " " + d.query(TemporalQueries.chronology()) + " " + d.query(TemporalQueries.precision()) + " " + d.isSupported(ChronoField.ALIGNED_WEEK_OF_MONTH) + " " + d.isSupported(ChronoField.HOUR_OF_DAY) + " " + d.isSupported(ChronoUnit.ERAS));
    t("lit", () -> d.atTime(LocalTime.of(5, 6)));
    t("fmt", () -> DateTimeFormatter.ofPattern("GGGG y-MM-dd").withChronology(c).format(d) + " | " + DateTimeFormatter.ofPattern("G y/M/d").withChronology(c).format(d) + " | " + DateTimeFormatter.ofPattern("GGGGG y MMMM EEEE").withChronology(c).format(d) + " | " + DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withChronology(c).withLocale(Locale.ENGLISH).format(d) + " | " + DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withChronology(c).withLocale(Locale.ENGLISH).format(d) + " | " + DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withChronology(c).withLocale(Locale.ENGLISH).format(d));
    t("months", () -> { StringBuilder sb = new StringBuilder(); for (int m = 1; m <= 12; m++) sb.append(DateTimeFormatter.ofPattern("MMMM/MMM/MMMMM").withChronology(c).format(HijrahDate.of(1445, m, 1))).append(' '); return sb; });
    t("days", () -> { StringBuilder sb = new StringBuilder(); for (int i = 1; i <= 7; i++) sb.append(DateTimeFormatter.ofPattern("EEEE/EEE/EEEEE").withChronology(c).format(HijrahDate.of(1445, 9, i))).append(' '); return sb; });
    t("parse", () -> HijrahDate.from(DateTimeFormatter.ofPattern("GGGG y-MM-dd").withChronology(c).parse("AH 1445-09-15")) + " " + c.date(DateTimeFormatter.ofPattern("G y-M-d").withChronology(c).parse("AH 1445-9-30")));
    t("parse2", () -> HijrahDate.from(DateTimeFormatter.ofPattern("y-MM-dd").withChronology(c).parse("1445-09-31")));
    t("parse3", () -> HijrahDate.from(DateTimeFormatter.ofPattern("y-MM-dd").withChronology(c).withResolverStyle(ResolverStyle.LENIENT).parse("1445-09-31")));
    t("parse4", () -> HijrahDate.from(DateTimeFormatter.ofPattern("y-MM-dd").withChronology(c).withResolverStyle(ResolverStyle.STRICT).parse("1445-09-30")));
    t("parse5", () -> HijrahDate.from(DateTimeFormatter.ofPattern("y-MMMM-dd").withChronology(c).parse("1445-Ramadan-10")));
    t("serial", () -> { java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream(); try (java.io.ObjectOutputStream o = new java.io.ObjectOutputStream(bo)) { o.writeObject(d); } try (java.io.ObjectInputStream i = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bo.toByteArray()))) { return i.readObject(); } });
    t("resolve", () -> { Map<TemporalField, Long> m = new HashMap<>(); m.put(ChronoField.ERA, 1L); m.put(ChronoField.YEAR_OF_ERA, 1445L); m.put(ChronoField.MONTH_OF_YEAR, 9L); m.put(ChronoField.DAY_OF_MONTH, 3L); return c.resolveDate(m, ResolverStyle.SMART); });
    t("resolve2", () -> { Map<TemporalField, Long> m = new HashMap<>(); m.put(ChronoField.YEAR, 1445L); m.put(ChronoField.DAY_OF_YEAR, 50L); return c.resolveDate(m, ResolverStyle.STRICT); });
    t("resolve3", () -> { Map<TemporalField, Long> m = new HashMap<>(); m.put(ChronoField.YEAR, 1445L); m.put(ChronoField.MONTH_OF_YEAR, 9L); m.put(ChronoField.DAY_OF_MONTH, 31L); return c.resolveDate(m, ResolverStyle.SMART); });
    t("resolve4", () -> { Map<TemporalField, Long> m = new HashMap<>(); m.put(ChronoField.YEAR, 1445L); m.put(ChronoField.MONTH_OF_YEAR, 9L); m.put(ChronoField.DAY_OF_MONTH, 31L); return c.resolveDate(m, ResolverStyle.STRICT); });
    t("resolve5", () -> { Map<TemporalField, Long> m = new HashMap<>(); m.put(ChronoField.YEAR, 1445L); m.put(ChronoField.MONTH_OF_YEAR, 9L); m.put(ChronoField.ALIGNED_WEEK_OF_MONTH, 2L); m.put(ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH, 3L); return c.resolveDate(m, ResolverStyle.SMART); });
    t("resolve6", () -> { Map<TemporalField, Long> m = new HashMap<>(); m.put(ChronoField.YEAR, 1445L); m.put(ChronoField.ALIGNED_WEEK_OF_YEAR, 5L); m.put(ChronoField.DAY_OF_WEEK, 3L); return c.resolveDate(m, ResolverStyle.SMART); });
    t("resolve7", () -> { Map<TemporalField, Long> m = new HashMap<>(); m.put(ChronoField.PROLEPTIC_MONTH, 17000L); m.put(ChronoField.DAY_OF_MONTH, 3L); return c.resolveDate(m, ResolverStyle.SMART); });
    t("resolve8", () -> { Map<TemporalField, Long> m = new HashMap<>(); m.put(ChronoField.YEAR_OF_ERA, 1445L); m.put(ChronoField.MONTH_OF_YEAR, 9L); m.put(ChronoField.DAY_OF_MONTH, 3L); return c.resolveDate(m, ResolverStyle.SMART); });
    t("resolve9", () -> { Map<TemporalField, Long> m = new HashMap<>(); m.put(ChronoField.YEAR_OF_ERA, 1445L); m.put(ChronoField.MONTH_OF_YEAR, 9L); m.put(ChronoField.DAY_OF_MONTH, 3L); return c.resolveDate(m, ResolverStyle.STRICT); });
    t("resolve10", () -> { Map<TemporalField, Long> m = new HashMap<>(); m.put(ChronoField.YEAR_OF_ERA, 1445L); m.put(ChronoField.ERA, 0L); m.put(ChronoField.MONTH_OF_YEAR, 9L); m.put(ChronoField.DAY_OF_MONTH, 3L); return c.resolveDate(m, ResolverStyle.SMART); });
    t("ordinary", () -> HijrahChronology.INSTANCE.dateNow().getChronology() + " " + HijrahDate.now().getChronology());
    t("variant", () -> Chronology.getAvailableChronologies().contains(HijrahChronology.INSTANCE));
    t("hdate5", () -> { HijrahDate x = HijrahDate.of(1445, 9, 15); return x.withVariant(HijrahChronology.INSTANCE) == x; });
  }
}

  public static void main(String[] args) throws Exception {
    A.run();
  }
}
