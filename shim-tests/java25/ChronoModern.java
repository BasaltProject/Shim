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

public class ChronoModern {
  interface Body {
    Object get() throws Exception;
  }

  static void probe(String name, Body body) {
    String out;
    try {
      out = String.valueOf(body.get());
    } catch (Throwable t) {
      out = "!" + t.getClass().getName() + ":" + (t instanceof NullPointerException ? "npe" : t.getMessage());
    }
    System.out.println(name + "=" + out);
  }

  static String dump(ChronoLocalDate d) {
    return d + "|" + d.getEra() + "|" + d.get(ChronoField.YEAR) + "|" + d.get(ChronoField.YEAR_OF_ERA) + "|" + d.get(ChronoField.MONTH_OF_YEAR) + "|" + d.get(ChronoField.DAY_OF_MONTH) + "|" + d.getLong(ChronoField.PROLEPTIC_MONTH) + "|" + d.toEpochDay() + "|" + d.lengthOfMonth() + "|" + d.lengthOfYear() + "|" + d.isLeapYear() + "|" + d.get(ChronoField.DAY_OF_WEEK) + "|" + d.get(ChronoField.DAY_OF_YEAR) + "|" + LocalDate.from(d);
  }

  public static void main(String[] args) throws Exception {
    Locale.setDefault(Locale.US);
    Chronology[] chronos = {MinguoChronology.INSTANCE, ThaiBuddhistChronology.INSTANCE};
    for (Chronology c : chronos) {
      String n = c.getId();
      probe(n + " ids", () -> c.getId() + " " + c.getCalendarType() + " " + c + " " + c.eras() + " " + c.hashCode() / 1000000 * 0);
      probe(n + " display", () -> c.getDisplayName(TextStyle.FULL, Locale.US) + "|" + c.getDisplayName(TextStyle.SHORT, Locale.US));
      probe(n + " of", () -> (Chronology.of(c.getId()) == c) + " " + (Chronology.of(c.getCalendarType()) == c));
      probe(n + " locale", () -> Chronology.ofLocale(Locale.forLanguageTag("en-u-ca-" + c.getCalendarType())));
      probe(n + " date", () -> dump(c.date(100, 2, 29)));
      probe(n + " date2", () -> dump(c.date(2570, 12, 31)));
      probe(n + " date3", () -> dump(c.date(1, 1, 1)));
      probe(n + " date0", () -> dump(c.date(0, 1, 1)));
      probe(n + " date-5", () -> dump(c.date(-5, 6, 7)));
      probe(n + " dateEra", () -> dump(c.date(c.eras().get(0), 5, 3, 4)) + " " + dump(c.date(c.eras().get(1), 5, 3, 4)));
      probe(n + " yearday", () -> dump(c.dateYearDay(2000, 60)) + " " + dump(c.dateYearDay(c.eras().get(1), 2000, 366)));
      probe(n + " epoch", () -> dump(c.dateEpochDay(0)) + " " + dump(c.dateEpochDay(-100000)) + " " + dump(c.dateEpochDay(1000000)));
      probe(n + " from", () -> dump(c.date(LocalDate.of(2024, 2, 29))) + " " + dump(c.date(c.date(2000, 1, 1))));
      probe(n + " invalid", () -> c.date(100, 2, 30));
      probe(n + " invalid2", () -> c.date(100, 13, 1));
      probe(n + " invalid3", () -> c.dateYearDay(100, 367));
      probe(n + " leap", () -> c.isLeapYear(0) + " " + c.isLeapYear(1) + " " + c.isLeapYear(2000) + " " + c.isLeapYear(1911) + " " + c.isLeapYear(2024 - (c == MinguoChronology.INSTANCE ? 1911 : -543)));
      probe(n + " proleptic", () -> c.prolepticYear(c.eras().get(1), 7) + " " + c.prolepticYear(c.eras().get(0), 7));
      probe(n + " proleptic bad", () -> c.prolepticYear(IsoEra.CE, 7));
      probe(n + " eraOf", () -> c.eraOf(0) + " " + c.eraOf(1));
      probe(n + " eraOf bad", () -> c.eraOf(2));
      probe(n + " eraNames", () -> { StringBuilder sb = new StringBuilder(); for (Era e : c.eras()) for (TextStyle st : new TextStyle[] {TextStyle.FULL, TextStyle.SHORT, TextStyle.NARROW}) sb.append(e.getDisplayName(st, Locale.US)).append('|').append(e.getValue()).append(';'); return sb; });
      probe(n + " range", () -> { StringBuilder sb = new StringBuilder(); for (ChronoField f : new ChronoField[] {ChronoField.YEAR, ChronoField.YEAR_OF_ERA, ChronoField.PROLEPTIC_MONTH, ChronoField.MONTH_OF_YEAR, ChronoField.DAY_OF_MONTH, ChronoField.ERA, ChronoField.DAY_OF_YEAR, ChronoField.ALIGNED_WEEK_OF_MONTH}) sb.append(f).append('=').append(c.range(f)).append(';'); return sb; });
      probe(n + " date range", () -> { ChronoLocalDate d = c.date(100, 2, 10); StringBuilder sb = new StringBuilder(); for (ChronoField f : new ChronoField[] {ChronoField.YEAR, ChronoField.YEAR_OF_ERA, ChronoField.PROLEPTIC_MONTH, ChronoField.DAY_OF_MONTH, ChronoField.DAY_OF_YEAR, ChronoField.ALIGNED_WEEK_OF_MONTH, ChronoField.HOUR_OF_DAY}) { try { sb.append(d.range(f)); } catch (Exception e) { sb.append(e.getClass().getSimpleName()); } sb.append(';'); } d = c.date(-3, 2, 10); sb.append(d.range(ChronoField.YEAR_OF_ERA)); return sb; });
      probe(n + " plus", () -> { ChronoLocalDate d = c.date(100, 1, 31); StringBuilder sb = new StringBuilder(); for (ChronoUnit u : ChronoUnit.values()) { try { sb.append(d.plus(3, u)).append(';'); } catch (Exception e) { sb.append(e.getClass().getSimpleName()).append(';'); } } return sb; });
      probe(n + " minus", () -> { ChronoLocalDate d = c.date(100, 3, 31); StringBuilder sb = new StringBuilder(); for (ChronoUnit u : ChronoUnit.values()) { try { sb.append(d.minus(7, u)).append(';'); } catch (Exception e) { sb.append(e.getClass().getSimpleName()).append(';'); } } sb.append(d.minus(Long.MIN_VALUE, ChronoUnit.DAYS)); return sb; });
      probe(n + " plus amount", () -> c.date(100, 1, 31).plus(Period.of(1, 1, 1)) + " " + c.date(100, 1, 31).minus(Period.ofDays(40)) + " " + c.date(100, 1, 31).plus(c.period(1, 13, 1)));
      probe(n + " plus mismatch", () -> c.date(100, 1, 31).plus(Chronology.of(c == MinguoChronology.INSTANCE ? "ThaiBuddhist" : "Minguo").period(1, 1, 1)));
      probe(n + " with field", () -> { ChronoLocalDate d = c.date(100, 6, 15); StringBuilder sb = new StringBuilder(); for (ChronoField f : new ChronoField[] {ChronoField.DAY_OF_MONTH, ChronoField.MONTH_OF_YEAR, ChronoField.YEAR, ChronoField.YEAR_OF_ERA, ChronoField.ERA, ChronoField.PROLEPTIC_MONTH, ChronoField.DAY_OF_YEAR, ChronoField.EPOCH_DAY, ChronoField.DAY_OF_WEEK, ChronoField.HOUR_OF_DAY}) { try { sb.append(d.with(f, f == ChronoField.PROLEPTIC_MONTH ? 1200 * 3 + 4 : f == ChronoField.ERA ? 0 : f == ChronoField.YEAR ? 150 : f == ChronoField.EPOCH_DAY ? 5000 : 3)).append(';'); } catch (Exception e) { sb.append(e.getClass().getSimpleName()).append(':').append(e.getMessage()).append(';'); } } return sb; });
      probe(n + " with adjuster", () -> c.date(100, 6, 15).with(TemporalAdjusters.lastDayOfMonth()) + " " + c.date(100, 6, 15).with(TemporalAdjusters.firstDayOfNextYear()) + " " + c.date(100, 6, 15).with(DayOfWeek.MONDAY));
      probe(n + " with localdate", () -> c.date(100, 6, 15).with(LocalDate.of(2000, 1, 1)));
      probe(n + " with era-neg", () -> c.date(-3, 6, 15).with(ChronoField.YEAR_OF_ERA, 5) + " " + c.date(-3, 6, 15).with(ChronoField.ERA, 1) + " " + c.date(5, 6, 15).with(ChronoField.ERA, 0));
      probe(n + " until", () -> { ChronoLocalDate a = c.date(100, 1, 31); ChronoLocalDate b = c.date(105, 3, 1); StringBuilder sb = new StringBuilder(); for (ChronoUnit u : ChronoUnit.values()) { try { sb.append(a.until(b, u)).append(';'); } catch (Exception e) { sb.append(e.getClass().getSimpleName()).append(';'); } } sb.append(a.until(b)).append(';').append(b.until(a)).append(';').append(a.until(LocalDate.of(2020, 1, 1), ChronoUnit.DAYS)); return sb; });
      probe(n + " until period", () -> c.date(100, 1, 31).until(c.date(100, 1, 31)) + " " + c.date(100, 1, 31).until(c.date(98, 3, 1)).isNegative() + " " + c.date(100, 1, 1).until(c.date(100, 1, 1)).isZero());
      probe(n + " period", () -> { ChronoPeriod p = c.period(14, 25, 3); return p + " " + p.normalized() + " " + p.negated() + " " + p.multipliedBy(2) + " " + p.plus(c.period(1, 1, 1)) + " " + p.minus(c.period(1, 1, 1)) + " " + p.getUnits() + " " + p.get(ChronoUnit.MONTHS) + " " + p.isZero() + " " + p.isNegative() + " " + p.getChronology() + " " + c.period(0, 0, 0) + " " + p.equals(c.period(14, 25, 3)) + " " + (p.hashCode() == c.period(14, 25, 3).hashCode()); });
      probe(n + " period bad unit", () -> c.period(1, 2, 3).get(ChronoUnit.HOURS));
      probe(n + " period add", () -> c.period(1, 2, 3).addTo(c.date(100, 5, 5)) + " " + c.period(1, 2, 3).subtractFrom(c.date(100, 5, 5)) + " " + c.date(100, 5, 5).plus(c.period(0, 0, 400)));
      probe(n + " period add iso", () -> c.period(1, 2, 3).addTo(LocalDate.of(2000, 1, 1)));
      probe(n + " period between", () -> ChronoPeriod.between(c.date(100, 1, 1), c.date(101, 2, 2)));
      probe(n + " equals", () -> { ChronoLocalDate a = c.date(100, 1, 1); ChronoLocalDate b = c.dateEpochDay(a.toEpochDay()); return a.equals(b) + " " + (a.hashCode() == b.hashCode()) + " " + a.equals(LocalDate.of(a.toEpochDay() == 0 ? 1970 : (a.getLong(ChronoField.YEAR) > 0 ? LocalDate.from(a).getYear() : 0), 1, 1)) + " " + a.equals(null) + " " + a.isEqual(LocalDate.from(a)) + " " + a.compareTo(LocalDate.from(a)) + " " + a.isBefore(c.date(100, 1, 2)) + " " + a.isAfter(c.date(100, 1, 2)); });
      probe(n + " hash", () -> c.date(100, 1, 1).hashCode() == (c.getId().hashCode() ^ LocalDate.from(c.date(100, 1, 1)).hashCode()));
      probe(n + " compare mixed", () -> { List<ChronoLocalDate> l = new ArrayList<>(List.of(c.date(100, 1, 1), LocalDate.of(2000, 1, 1), c.date(50, 1, 1), JapaneseChronologyAvoid.skip())); return "n/a"; });
      probe(n + " timeline", () -> { List<ChronoLocalDate> l = new ArrayList<>(List.of(c.date(100, 1, 1), LocalDate.of(2000, 1, 1), c.date(50, 1, 1))); l.sort(ChronoLocalDate.timeLineOrder()); return l; });
      probe(n + " attime", () -> { ChronoLocalDateTime<?> t = c.date(100, 1, 31).atTime(LocalTime.of(23, 59, 30, 123)); return t + "|" + t.getChronology() + "|" + t.toLocalDate() + "|" + t.get(ChronoField.HOUR_OF_DAY) + "|" + t.getLong(ChronoField.EPOCH_DAY) + "|" + t.get(ChronoField.YEAR) + "|" + t.plus(1, ChronoUnit.HOURS) + "|" + t.plus(1, ChronoUnit.DAYS) + "|" + t.plus(1, ChronoUnit.MONTHS) + "|" + t.plus(1, ChronoUnit.YEARS) + "|" + t.minus(1, ChronoUnit.NANOS) + "|" + t.with(ChronoField.HOUR_OF_DAY, 1) + "|" + t.with(ChronoField.MONTH_OF_YEAR, 2) + "|" + t.isSupported(ChronoField.HOUR_OF_DAY) + t.isSupported(ChronoField.OFFSET_SECONDS) + t.isSupported(ChronoUnit.HALF_DAYS) + "|" + t.range(ChronoField.MINUTE_OF_HOUR) + "|" + t.range(ChronoField.YEAR); });
      probe(n + " attime until", () -> { ChronoLocalDateTime<?> a = c.date(100, 1, 31).atTime(LocalTime.of(10, 0)); ChronoLocalDateTime<?> b = c.date(101, 3, 1).atTime(LocalTime.of(9, 0)); StringBuilder sb = new StringBuilder(); for (ChronoUnit u : ChronoUnit.values()) { try { sb.append(a.until(b, u)).append(';'); } catch (Exception e) { sb.append(e.getClass().getSimpleName()).append(';'); } } return sb; });
      probe(n + " attime equals", () -> { ChronoLocalDateTime<?> a = c.date(100, 1, 31).atTime(LocalTime.NOON); return a.equals(c.date(100, 1, 31).atTime(LocalTime.NOON)) + " " + (a.hashCode() == c.date(100, 1, 31).atTime(LocalTime.NOON).hashCode()) + " " + a.isBefore(a.plus(1, ChronoUnit.SECONDS)) + " " + a.compareTo(a) + " " + a.equals(LocalDateTime.from(a)); });
      probe(n + " localDateTime", () -> c.localDateTime(LocalDateTime.of(2024, 5, 6, 7, 8, 9)) + " " + c.localDateTime(c.date(100, 1, 1).atTime(LocalTime.MIDNIGHT)));
      probe(n + " atzone", () -> { ChronoZonedDateTime<?> z = c.dateEpochDay(LocalDate.of(2024, 3, 9).toEpochDay()).atTime(LocalTime.of(2, 30)).atZone(ZoneId.of("America/New_York")); return z + "|" + z.getOffset() + "|" + z.getZone() + "|" + z.toInstant() + "|" + z.toEpochSecond() + "|" + z.get(ChronoField.YEAR) + "|" + z.getLong(ChronoField.INSTANT_SECONDS) + "|" + z.plus(1, ChronoUnit.HOURS) + "|" + z.plus(1, ChronoUnit.DAYS) + "|" + z.minus(1, ChronoUnit.MONTHS) + "|" + z.withZoneSameInstant(ZoneId.of("Asia/Tokyo")) + "|" + z.withZoneSameLocal(ZoneOffset.UTC) + "|" + z.toLocalDateTime() + "|" + z.getChronology(); });
      probe(n + " atzone gap", () -> { ChronoLocalDate d = c.dateEpochDay(LocalDate.of(2024, 3, 10).toEpochDay()); ChronoZonedDateTime<?> z = d.atTime(LocalTime.of(2, 30)).atZone(ZoneId.of("America/New_York")); ChronoLocalDate d2 = c.dateEpochDay(LocalDate.of(2024, 11, 3).toEpochDay()); ChronoZonedDateTime<?> o = d2.atTime(LocalTime.of(1, 30)).atZone(ZoneId.of("America/New_York")); return z + "|" + o + "|" + o.withLaterOffsetAtOverlap() + "|" + o.withEarlierOffsetAtOverlap(); });
      probe(n + " atzone until", () -> { ChronoZonedDateTime<?> a = c.dateEpochDay(19000).atTime(LocalTime.of(2, 30)).atZone(ZoneOffset.UTC); ChronoZonedDateTime<?> b = c.dateEpochDay(19400).atTime(LocalTime.of(3, 30)).atZone(ZoneOffset.ofHours(2)); StringBuilder sb = new StringBuilder(); for (ChronoUnit u : ChronoUnit.values()) { try { sb.append(a.until(b, u)).append(';'); } catch (Exception e) { sb.append(e.getClass().getSimpleName()).append(';'); } } return sb; });
      probe(n + " atzone with", () -> { ChronoZonedDateTime<?> z = c.date(100, 3, 10).atTime(LocalTime.of(2, 30)).atZone(ZoneOffset.UTC); return z.with(ChronoField.OFFSET_SECONDS, 3600) + " " + z.with(ChronoField.INSTANT_SECONDS, 0) + " " + z.with(ChronoField.MONTH_OF_YEAR, 5) + " " + z.equals(c.date(100, 3, 10).atTime(LocalTime.of(2, 30)).atZone(ZoneOffset.UTC)) + " " + z.isSupported(ChronoField.OFFSET_SECONDS); });
      probe(n + " zonedDateTime", () -> c.zonedDateTime(Instant.ofEpochSecond(86400L * 365 * 30), ZoneId.of("Europe/Paris")) + " " + c.zonedDateTime(ZonedDateTime.of(2024, 5, 6, 7, 8, 9, 0, ZoneId.of("Asia/Kolkata"))) + " " + c.zonedDateTime(OffsetDateTime.of(2024, 5, 6, 7, 8, 9, 0, ZoneOffset.ofHours(-3))));
      probe(n + " zonedDateTime bad", () -> c.zonedDateTime(LocalDate.of(2024, 5, 6)));
      probe(n + " epochSecond", () -> c.epochSecond(100, 1, 2, 3, 4, 5, ZoneOffset.ofHours(1)) + " " + c.epochSecond(c.eras().get(1), 100, 1, 2, 3, 4, 5, ZoneOffset.UTC));
      probe(n + " from temporal", () -> ChronoLocalDate.from(c.date(100, 1, 1)) + " " + Chronology.from(c.date(100, 1, 1)) + " " + c.date(100, 1, 1).query(TemporalQueries.chronology()) + " " + c.date(100, 1, 1).query(TemporalQueries.precision()) + " " + c.date(100, 1, 1).query(TemporalQueries.localDate()) + " " + c.date(100, 1, 1).query(TemporalQueries.localTime()) + " " + c.date(100, 1, 1).query(TemporalQueries.zone()));
      probe(n + " adjustInto", () -> c.date(100, 1, 1).adjustInto(LocalDate.of(1999, 12, 31)) + " " + LocalDate.of(1999, 12, 31).with(c.date(100, 1, 1)) + " " + c.date(100, 1, 1).adjustInto(LocalDateTime.of(1999, 12, 31, 5, 6)));
      probe(n + " resolve", () -> { Map<TemporalField, Long> m = new HashMap<>(); m.put(ChronoField.YEAR, 100L); m.put(ChronoField.MONTH_OF_YEAR, 2L); m.put(ChronoField.DAY_OF_MONTH, 3L); return c.resolveDate(m, ResolverStyle.SMART) + " " + m; });
      probe(n + " resolve yoe", () -> { StringBuilder sb = new StringBuilder(); for (ResolverStyle rs : ResolverStyle.values()) { Map<TemporalField, Long> m = new HashMap<>(); m.put(ChronoField.YEAR_OF_ERA, 5L); m.put(ChronoField.ERA, 0L); m.put(ChronoField.MONTH_OF_YEAR, 14L); m.put(ChronoField.DAY_OF_MONTH, 40L); try { sb.append(c.resolveDate(m, rs)); } catch (Exception e) { sb.append(e.getClass().getSimpleName()).append(':').append(e.getMessage()); } sb.append(' ').append(new TreeMap<>(Map.of("n", m.size()))).append(';'); } return sb; });
      probe(n + " resolve pm", () -> { Map<TemporalField, Long> m = new HashMap<>(); m.put(ChronoField.PROLEPTIC_MONTH, 1201L); m.put(ChronoField.DAY_OF_MONTH, 3L); return c.resolveDate(m, ResolverStyle.STRICT) + " " + m.size(); });
      probe(n + " resolve yd", () -> { Map<TemporalField, Long> m = new HashMap<>(); m.put(ChronoField.YEAR, 100L); m.put(ChronoField.DAY_OF_YEAR, 366L); return c.resolveDate(m, ResolverStyle.STRICT) + " " + m.size(); });
      probe(n + " resolve epoch", () -> { Map<TemporalField, Long> m = new HashMap<>(); m.put(ChronoField.EPOCH_DAY, 100L); return c.resolveDate(m, ResolverStyle.STRICT) + " " + m.size(); });
      probe(n + " resolve none", () -> { Map<TemporalField, Long> m = new HashMap<>(); m.put(ChronoField.YEAR, 100L); return c.resolveDate(m, ResolverStyle.STRICT) + " " + m; });
      probe(n + " format", () -> { DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd GGGG", Locale.US).withChronology(c); return f.format(LocalDate.of(2024, 5, 6)) + " " + f.format(c.date(100, 1, 2)) + " " + f.format(LocalDateTime.of(2024, 5, 6, 7, 8)) + " " + f.getChronology(); });
      probe(n + " format short", () -> { DateTimeFormatter f = DateTimeFormatter.ofPattern("y/M/d G", Locale.US).withChronology(c); return f.format(LocalDate.of(2024, 5, 6)) + " " + f.format(LocalDate.of(1800, 5, 6)); });
      probe(n + " parse", () -> { DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US).withChronology(c); ChronoLocalDate d = c.date(f.parse("0100-02-03")); return d + " " + LocalDate.from(f.parse("0100-02-03")) + " " + f.parse("0100-02-03").query(TemporalQueries.chronology()) + " " + f.parse("0100-02-03"); });
      probe(n + " parse bad", () -> DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US).withChronology(c).parse("0100-02-30"));
      probe(n + " parse era", () -> { DateTimeFormatter f = DateTimeFormatter.ofPattern("G y-M-d", Locale.US).withChronology(c); return f.parse(f.format(c.date(5, 2, 3))) + " " + f.format(c.date(-5, 2, 3)); });
      probe(n + " zone format", () -> DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm VV", Locale.US).withChronology(c).format(ZonedDateTime.of(2024, 5, 6, 7, 8, 9, 0, ZoneId.of("Europe/Paris"))));
      probe(n + " date.format", () -> c.date(100, 1, 2).format(DateTimeFormatter.ofPattern("yyyy/MM/dd", Locale.US)));
      probe(n + " now", () -> { ChronoLocalDate d = c.dateNow(Clock.fixed(Instant.parse("2024-05-06T10:00:00Z"), ZoneOffset.UTC)); return d + " " + c.dateNow(ZoneOffset.UTC).getChronology(); });
      probe(n + " eras period", () -> c.date(100, 1, 1).plus(1, ChronoUnit.ERAS) + " " + c.date(100, 1, 1).until(c.date(-10, 1, 1), ChronoUnit.ERAS));
      probe(n + " get unsupported", () -> c.date(100, 1, 1).get(ChronoField.HOUR_OF_DAY));
      probe(n + " isSupported", () -> c.date(100, 1, 1).isSupported(ChronoField.DAY_OF_MONTH) + " " + c.date(100, 1, 1).isSupported(ChronoField.HOUR_OF_DAY) + " " + c.date(100, 1, 1).isSupported(ChronoUnit.DAYS) + " " + c.date(100, 1, 1).isSupported(ChronoUnit.HOURS) + " " + c.date(100, 1, 1).isSupported((TemporalField) null));
      probe(n + " null args", () -> { StringBuilder sb = new StringBuilder(); try { c.date((TemporalAccessor) null); } catch (Exception e) { sb.append(e.getClass().getSimpleName()); } try { c.date(100, 1, 1).plus(null); } catch (Exception e) { sb.append(e.getClass().getSimpleName()); } try { c.date(100, 1, 1).with((TemporalAdjuster) null); } catch (Exception e) { sb.append(e.getClass().getSimpleName()); } return sb; });
      probe(n + " serialization", () -> { java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream(); java.io.ObjectOutputStream os = new java.io.ObjectOutputStream(bo); os.writeObject(c.date(100, 1, 2)); os.writeObject(c); os.close(); java.io.ObjectInputStream is = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bo.toByteArray())); Object d = is.readObject(); Object ch = is.readObject(); return d + " " + (ch == c) + " " + ch; });
    }
    probe("available", () -> { List<String> ids = new ArrayList<>(); for (Chronology c : Chronology.getAvailableChronologies()) { if (c.getId().equals("ISO") || c.getId().equals("Minguo") || c.getId().equals("ThaiBuddhist")) ids.add(c.getId()); } Collections.sort(ids); return ids; });
    probe("of unknown", () -> Chronology.of("nope"));
    probe("ofLocale unknown", () -> Chronology.ofLocale(Locale.forLanguageTag("en-u-ca-nope")));
    probe("ofLocale iso", () -> Chronology.ofLocale(Locale.US) + " " + Chronology.ofLocale(Locale.forLanguageTag("en-u-ca-iso8601")) + " " + Chronology.ofLocale(Locale.forLanguageTag("th-TH-u-ca-buddhist")));
    probe("chrono compare", () -> MinguoChronology.INSTANCE.compareTo(ThaiBuddhistChronology.INSTANCE) + " " + MinguoChronology.INSTANCE.compareTo(IsoChronology.INSTANCE) + " " + MinguoChronology.INSTANCE.equals(MinguoChronology.INSTANCE) + " " + MinguoChronology.INSTANCE.equals(IsoChronology.INSTANCE));
    probe("minguo static", () -> MinguoDate.of(114, 10, 16) + " " + MinguoDate.from(LocalDate.of(2025, 10, 16)) + " " + MinguoDate.of(114, 10, 16).getChronology() + " " + MinguoDate.of(114, 10, 16).getEra() + " " + MinguoDate.now(Clock.fixed(Instant.parse("2025-10-16T00:00:00Z"), ZoneOffset.UTC)));
    probe("thai static", () -> ThaiBuddhistDate.of(2568, 10, 16) + " " + ThaiBuddhistDate.from(LocalDate.of(2025, 10, 16)) + " " + ThaiBuddhistDate.of(2568, 10, 16).getEra() + " " + ThaiBuddhistDate.now(Clock.fixed(Instant.parse("2025-10-16T00:00:00Z"), ZoneOffset.UTC)));
    probe("minguo covariant", () -> { MinguoDate d = MinguoDate.of(114, 10, 16); MinguoDate e = d.plus(1, ChronoUnit.DAYS).minus(1, ChronoUnit.MONTHS).with(ChronoField.DAY_OF_MONTH, 5).with(TemporalAdjusters.firstDayOfYear()).plus(Period.ofDays(1)).minus(Period.ofDays(1)); ChronoLocalDateTime<MinguoDate> t = d.atTime(LocalTime.NOON); return e + " " + t.toLocalDate().getClass().getSimpleName() + " " + MinguoEra.of(1) + " " + MinguoEra.valueOf("ROC") + " " + Arrays.toString(MinguoEra.values()); });
    probe("thai covariant", () -> { ThaiBuddhistDate d = ThaiBuddhistDate.of(2568, 10, 16); ThaiBuddhistDate e = d.plus(1, ChronoUnit.DAYS).minus(1, ChronoUnit.MONTHS).with(ChronoField.DAY_OF_MONTH, 5); ChronoLocalDateTime<ThaiBuddhistDate> t = d.atTime(LocalTime.NOON); return e + " " + t.toLocalDate().getClass().getSimpleName() + " " + ThaiBuddhistEra.of(1) + " " + Arrays.toString(ThaiBuddhistEra.values()); });
    probe("era bad", () -> MinguoEra.of(5));
    probe("era iso cast", () -> MinguoChronology.INSTANCE.date(IsoEra.CE, 5, 1, 1));
  }

  static final class JapaneseChronologyAvoid {
    static ChronoLocalDate skip() {
      return LocalDate.of(1, 1, 1);
    }
  }
}
