/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import java.time.*; import java.time.format.*; import java.time.temporal.*; import java.time.chrono.*; import java.time.zone.*; import java.util.*;
public class TimeFormattingModern {
 static void p(Object o){ System.out.println(o); }
 static void t(String n, java.util.concurrent.Callable<Object> c){ try{ p(n+" "+c.call()); }catch(Throwable e){ p(n+" EXC "+e.getClass().getName()+": "+e.getMessage()); } }
 public static void main(String[] a) throws Exception {
  Locale.setDefault(Locale.US);
  ZonedDateTime z=ZonedDateTime.of(2024,3,10,1,30,0,123456789,ZoneId.of("America/New_York"));
  t("gap",()->z.plusHours(1)); t("gap2",()->z.plusDays(1)); t("overlap",()->ZonedDateTime.of(2024,11,3,1,30,0,0,ZoneId.of("America/New_York")).withLaterOffsetAtOverlap());
  for (String pat: new String[]{"yyyy-MM-dd'T'HH:mm:ss.SSSXXX","EEEE, MMMM d, uuuu h:mm a z","EEE dd MMM yy HH:mm Z","QQQ yyyy","w W e c F","G GGGG yyyy","VV zzzz OOOO","B","hh:mm:ss.nnnnnnnnn","D DDD","MMMMM LLLL","yyyyy Y YYYY","[yyyy][-MM]'x'","ppH"}) t(pat,()->DateTimeFormatter.ofPattern(pat).format(z));
  for (FormatStyle f: FormatStyle.values()) { t("loc"+f,()->DateTimeFormatter.ofLocalizedDateTime(f).format(z)); t("locD"+f,()->DateTimeFormatter.ofLocalizedDate(f).format(z)); }
  t("iso",()->DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(z)); t("isoz",()->DateTimeFormatter.ISO_ZONED_DATE_TIME.format(z)); t("rfc",()->DateTimeFormatter.RFC_1123_DATE_TIME.format(z)); t("week",()->DateTimeFormatter.ISO_WEEK_DATE.format(z)); t("ord",()->DateTimeFormatter.ISO_ORDINAL_DATE.format(z)); t("bas",()->DateTimeFormatter.BASIC_ISO_DATE.format(z));
  t("parse1",()->ZonedDateTime.parse("2024-02-29T23:59:59.999+05:30[Asia/Kolkata]")); t("parse2",()->LocalDate.parse("2023-02-29")); t("parse3",()->LocalTime.parse("25:00")); t("parse4",()->OffsetDateTime.parse("2024-01-01T00:00:00-02:30"));
  t("parse5",()->DateTimeFormatter.ofPattern("d MMM uuuu",Locale.FRANCE).parse("5 janv. 2024")); t("parse6",()->LocalDate.parse("5 Jan 2024",DateTimeFormatter.ofPattern("d MMM uuuu"))); t("strict",()->LocalDate.parse("2024-02-30",DateTimeFormatter.ISO_LOCAL_DATE.withResolverStyle(ResolverStyle.LENIENT)));
  t("smart",()->LocalDate.parse("2024-02-30",DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.SMART)));
  t("dur",()->Duration.parse("PT-1H30M").plusNanos(5)); t("dur2",()->Duration.ofSeconds(-3661,-1)); t("dur3",()->Duration.between(z,z.plusMonths(1))); t("per",()->Period.between(LocalDate.of(2020,2,29),LocalDate.of(2024,2,28))); t("per2",()->Period.parse("P1Y-2M3W4D").normalized());
  t("until",()->LocalDate.of(2000,1,31).plusMonths(1)+" "+LocalDate.of(2000,1,31).until(LocalDate.of(2024,3,1),ChronoUnit.WEEKS)+" "+LocalDate.of(2024,1,1).with(TemporalAdjusters.lastInMonth(DayOfWeek.FRIDAY)));
  t("week-fields",()->{ List<String> o=new ArrayList<>(); for (Locale l: new Locale[]{Locale.US,Locale.FRANCE,Locale.GERMANY,new Locale("ar","SA")}) { WeekFields w=WeekFields.of(l); o.add(l+" "+w.getFirstDayOfWeek()+" "+w.getMinimalDaysInFirstWeek()+" "+LocalDate.of(2024,12,30).get(w.weekOfWeekBasedYear())+" "+LocalDate.of(2024,1,1).get(w.weekOfYear())); } return o; });
  t("hij",()->HijrahDate.from(LocalDate.of(2024,3,11))+" "+JapaneseDate.from(LocalDate.of(2019,5,1))+" "+ThaiBuddhistDate.from(LocalDate.of(2024,3,11))+" "+MinguoDate.from(LocalDate.of(2024,3,11))+" "+JapaneseDate.of(JapaneseEra.REIWA,6,3,11));
  t("chrono",()->Chronology.getAvailableChronologies().stream().map(Chronology::getId).sorted().toList());
  t("fmt-jp",()->DateTimeFormatter.ofPattern("GGGGy年M月d日",Locale.JAPAN).withChronology(JapaneseChronology.INSTANCE).format(LocalDate.of(2019,5,1)));
  t("fmt-jp2",()->DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(Locale.JAPAN).format(LocalDate.of(2024,3,11)));
  t("fmt-de",()->DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(Locale.GERMANY).format(z)); t("fmt-fr",()->DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL).withLocale(Locale.FRANCE).format(z)); t("fmt-es",()->DateTimeFormatter.ofPattern("EEEE d 'de' MMMM",new Locale("es","ES")).format(z));
  t("zones",()->{ ZoneRules r=ZoneId.of("Europe/London").getRules(); return r.getTransitions().size()+" "+r.nextTransition(Instant.parse("2024-01-01T00:00:00Z"))+" "+r.getOffset(Instant.parse("2024-07-01T00:00:00Z"))+" "+r.getTransitionRules(); });
  t("zoneids",()->ZoneId.getAvailableZoneIds().size()+" "+ZoneId.of("GMT+5")+" "+ZoneId.of("UTC+05:30").normalized()+" "+ZoneId.ofOffset("UT",ZoneOffset.ofHours(2))+" "+ZoneId.of("Z")+" "+ZoneId.SHORT_IDS.get("PST"));
  t("tzname",()->{ List<String> o=new ArrayList<>(); for (TextStyle s: TextStyle.values()) o.add(ZoneId.of("Europe/Paris").getDisplayName(s,Locale.US)+"|"+ZoneId.of("Europe/Paris").getDisplayName(s,Locale.FRANCE)); return o; });
  t("inst",()->Instant.MAX+" "+Instant.MIN+" "+Instant.ofEpochSecond(-1,1)+" "+Instant.parse("2024-01-01T00:00:00.123456789Z").truncatedTo(ChronoUnit.MILLIS)+" "+Instant.ofEpochMilli(Long.MIN_VALUE));
  t("year",()->Year.isLeap(1900)+" "+Year.of(2024).atDay(366)+" "+YearMonth.parse("2024-02").lengthOfMonth()+" "+MonthDay.of(2,29).isValidYear(2023)+" "+Month.FEBRUARY.getDisplayName(TextStyle.SHORT_STANDALONE,Locale.GERMAN)+" "+DayOfWeek.MONDAY.getDisplayName(TextStyle.NARROW,Locale.US));
  t("adj",()->z.with(ChronoField.DAY_OF_YEAR,200)+" "+z.range(ChronoField.ALIGNED_WEEK_OF_MONTH)+" "+z.getLong(ChronoField.EPOCH_DAY)+" "+z.get(IsoFields.QUARTER_OF_YEAR)+" "+z.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)+" "+z.getLong(JulianFields.JULIAN_DAY));
  t("clock",()->Clock.fixed(Instant.EPOCH,ZoneOffset.UTC).instant()+" "+Clock.offset(Clock.fixed(Instant.EPOCH,ZoneOffset.UTC),Duration.ofHours(1)).millis()+" "+Clock.tick(Clock.fixed(Instant.ofEpochMilli(1234),ZoneOffset.UTC),Duration.ofSeconds(1)).instant());
  t("exc1",()->LocalDate.of(2024,13,1)); t("exc2",()->LocalTime.of(24,0)); t("exc3",()->z.get(ChronoField.INSTANT_SECONDS)); t("exc4",()->LocalDate.now().get(ChronoField.HOUR_OF_DAY)); t("exc5",()->Duration.ofSeconds(Long.MAX_VALUE).plusSeconds(1)); t("exc6",()->ZoneId.of("Nowhere/City")); t("exc7",()->DateTimeFormatter.ofPattern("zzzzzz"));
  t("buil",()->new DateTimeFormatterBuilder().appendValue(ChronoField.YEAR,4,10,SignStyle.EXCEEDS_PAD).appendLiteral('/').appendText(ChronoField.MONTH_OF_YEAR,TextStyle.FULL).appendFraction(ChronoField.NANO_OF_SECOND,0,9,true).toFormatter().format(z));
  t("fmtdesc",()->DateTimeFormatter.ofPattern("yyyy[-MM[-dd]]'T'HH:mm").toString()+" "+DateTimeFormatter.ISO_LOCAL_DATE_TIME);
  t("dayperiod",()->{ List<String> o=new ArrayList<>(); for (int h: new int[]{0,6,12,15,19,23}) o.add(DateTimeFormatter.ofPattern("h B").format(LocalTime.of(h,0))); return o; });
  t("temporalq",()->z.query(TemporalQueries.zone())+" "+z.query(TemporalQueries.offset())+" "+z.query(TemporalQueries.precision())+" "+z.query(TemporalQueries.chronology()));
 }}
