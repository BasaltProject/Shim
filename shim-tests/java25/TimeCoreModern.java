/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.time.*;
import java.time.temporal.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class TimeCoreModern {
  static void probe(String name, Supplier<Object> body) {
    String out;
    try {
      out = String.valueOf(body.get());
    } catch (Throwable t) {
      out = "!" + t.getClass().getName() + ":" + t.getMessage();
    }
    System.out.println(name + "=" + out);
  }

  public static void main(String[] args) {
    LocalDate d = LocalDate.of(2024, 2, 29);
    LocalTime t = LocalTime.of(13, 45, 30, 123456789);
    LocalDateTime dt = LocalDateTime.of(d, t);
    probe("date", () -> d + " " + d.getDayOfWeek() + " " + d.getDayOfYear() + " " + d.isLeapYear() + " " + d.lengthOfMonth() + " " + d.lengthOfYear() + " " + d.toEpochDay());
    probe("date-plus", () -> d.plusYears(1) + " " + d.plusMonths(12) + " " + d.plusDays(366) + " " + d.minusMonths(1) + " " + d.plusWeeks(2) + " " + d.withDayOfMonth(1) + " " + d.withMonth(4) + " " + d.withYear(2023));
    probe("date-bad", () -> LocalDate.of(2023, 2, 29));
    probe("date-bad2", () -> LocalDate.of(2023, 13, 1));
    probe("date-bad3", () -> LocalDate.of(2023, 4, 31));
    probe("date-bad4", () -> d.withDayOfYear(367));
    probe("date-epoch", () -> LocalDate.ofEpochDay(0) + " " + LocalDate.ofEpochDay(-1) + " " + LocalDate.ofYearDay(2024, 366) + " " + LocalDate.MIN + " " + LocalDate.MAX + " " + LocalDate.EPOCH);
    probe("date-until", () -> d.until(LocalDate.of(2025, 1, 1), ChronoUnit.DAYS) + " " + d.until(LocalDate.of(2025, 1, 1), ChronoUnit.MONTHS) + " " + d.until(LocalDate.of(2025, 3, 1), ChronoUnit.YEARS) + " " + ChronoUnit.WEEKS.between(d, LocalDate.of(2024, 5, 1)));
    probe("date-fields", () -> d.get(ChronoField.DAY_OF_WEEK) + " " + d.getLong(ChronoField.EPOCH_DAY) + " " + d.get(ChronoField.ALIGNED_WEEK_OF_YEAR) + " " + d.getLong(ChronoField.PROLEPTIC_MONTH) + " " + d.get(ChronoField.YEAR_OF_ERA) + " " + d.get(ChronoField.ERA));
    probe("date-unsupported", () -> d.get(ChronoField.HOUR_OF_DAY));
    probe("date-range", () -> d.range(ChronoField.DAY_OF_MONTH) + " " + d.range(ChronoField.DAY_OF_YEAR) + " " + d.range(ChronoField.ALIGNED_WEEK_OF_MONTH) + " " + d.range(ChronoField.YEAR));
    probe("date-compare", () -> d.compareTo(LocalDate.of(2024, 3, 1)) + " " + d.isBefore(LocalDate.MAX) + " " + d.isAfter(LocalDate.MIN) + " " + d.isEqual(d) + " " + d.equals(LocalDate.of(2024, 2, 29)) + " " + d.hashCode());
    probe("date-atTime", () -> d.atStartOfDay() + " " + d.atTime(5, 6) + " " + d.atTime(t) + " " + d.atTime(1, 2, 3));
    probe("date-parse", () -> LocalDate.parse("2024-02-29") + " " + LocalDate.parse("+12345-01-01") + " " + LocalDate.parse("-0001-12-31"));
    probe("date-parse-bad", () -> LocalDate.parse("2024-2-29"));
    probe("date-datesUntil", () -> d.datesUntil(d.plusDays(3)).toList() + " " + d.datesUntil(d.plusMonths(2), Period.ofMonths(1)).toList());
    probe("date-adjust", () -> d.with(TemporalAdjusters.firstDayOfMonth()) + " " + d.with(TemporalAdjusters.lastDayOfYear()) + " " + d.with(TemporalAdjusters.next(DayOfWeek.MONDAY)) + " " + d.with(TemporalAdjusters.previousOrSame(DayOfWeek.THURSDAY)) + " " + d.with(TemporalAdjusters.dayOfWeekInMonth(2, DayOfWeek.FRIDAY)) + " " + d.with(TemporalAdjusters.lastInMonth(DayOfWeek.SUNDAY)) + " " + d.with(TemporalAdjusters.firstDayOfNextMonth()));
    probe("date-period", () -> d.plus(Period.of(1, 2, 3)) + " " + d.until(LocalDate.of(2025, 5, 1)) + " " + d.minus(Period.ofWeeks(1)));
    probe("time", () -> t + " " + t.toSecondOfDay() + " " + t.toNanoOfDay() + " " + t.getHour() + t.getMinute() + t.getSecond() + t.getNano());
    probe("time-plus", () -> t.plusHours(12) + " " + t.minusMinutes(50) + " " + t.plusNanos(900000000) + " " + t.plusSeconds(86400 * 3 + 5) + " " + t.withHour(0).withNano(0));
    probe("time-bad", () -> LocalTime.of(24, 0));
    probe("time-bad2", () -> LocalTime.of(1, 60));
    probe("time-str", () -> LocalTime.of(1, 2) + " " + LocalTime.of(1, 2, 3) + " " + LocalTime.of(1, 2, 3, 4000000) + " " + LocalTime.of(1, 2, 3, 4000) + " " + LocalTime.of(1, 2, 3, 4) + " " + LocalTime.MIDNIGHT + " " + LocalTime.NOON + " " + LocalTime.MAX + " " + LocalTime.MIN);
    probe("time-trunc", () -> t.truncatedTo(ChronoUnit.HOURS) + " " + t.truncatedTo(ChronoUnit.MILLIS) + " " + t.truncatedTo(ChronoUnit.SECONDS));
    probe("time-trunc-bad", () -> t.truncatedTo(ChronoUnit.DAYS) + " " + t.truncatedTo(ChronoUnit.WEEKS));
    probe("time-until", () -> t.until(LocalTime.of(20, 0), ChronoUnit.MINUTES) + " " + t.until(LocalTime.of(1, 0), ChronoUnit.HOURS) + " " + ChronoUnit.MICROS.between(t, LocalTime.NOON));
    probe("time-parse", () -> LocalTime.parse("10:15") + " " + LocalTime.parse("10:15:30.5") + " " + LocalTime.parse("23:59:59.999999999"));
    probe("time-parse-bad", () -> LocalTime.parse("25:00"));
    probe("time-fields", () -> t.get(ChronoField.HOUR_OF_AMPM) + " " + t.get(ChronoField.CLOCK_HOUR_OF_AMPM) + " " + t.get(ChronoField.AMPM_OF_DAY) + " " + t.getLong(ChronoField.MILLI_OF_DAY) + " " + t.get(ChronoField.MICRO_OF_SECOND) + " " + t.get(ChronoField.CLOCK_HOUR_OF_DAY) + " " + t.getLong(ChronoField.NANO_OF_DAY));
    probe("time-with", () -> t.with(ChronoField.MINUTE_OF_DAY, 61) + " " + t.with(ChronoField.AMPM_OF_DAY, 0) + " " + t.with(ChronoField.SECOND_OF_DAY, 5) + " " + t.with(ChronoField.CLOCK_HOUR_OF_DAY, 24));
    probe("time-with-bad", () -> t.with(ChronoField.HOUR_OF_DAY, 24));
    probe("time-with-bad2", () -> t.with(ChronoField.DAY_OF_MONTH, 2));
    probe("time-range", () -> t.range(ChronoField.HOUR_OF_DAY) + " " + t.range(ChronoField.NANO_OF_DAY) + " " + t.range(ChronoField.CLOCK_HOUR_OF_AMPM));
    probe("dt", () -> dt + " " + dt.getMonth() + " " + dt.toLocalDate() + " " + dt.toLocalTime() + " " + dt.getDayOfWeek());
    probe("dt-plus", () -> dt.plusHours(11) + " " + dt.plusMonths(1) + " " + dt.minusDays(60) + " " + dt.plusNanos(877000000) + " " + dt.plus(Duration.ofHours(30)) + " " + dt.plus(Period.ofMonths(2)));
    probe("dt-until", () -> dt.until(dt.plusDays(40).plusHours(3), ChronoUnit.WEEKS) + " " + dt.until(dt.plusMonths(14), ChronoUnit.MONTHS) + " " + dt.until(dt.plusSeconds(100), ChronoUnit.MINUTES) + " " + dt.until(dt.plusDays(1).minusNanos(1), ChronoUnit.DAYS));
    probe("dt-with", () -> dt.withHour(1).withMinute(2) + " " + dt.with(LocalTime.MIDNIGHT) + " " + dt.with(LocalDate.EPOCH) + " " + dt.with(ChronoField.EPOCH_DAY, 1) + " " + dt.truncatedTo(ChronoUnit.MINUTES));
    probe("dt-zone", () -> dt.atZone(ZoneOffset.UTC) + " " + dt.toEpochSecond(ZoneOffset.ofHours(2)) + " " + dt.toInstant(ZoneOffset.UTC) + " " + dt.atOffset(ZoneOffset.ofHours(-5)));
    probe("dt-of", () -> LocalDateTime.ofEpochSecond(1700000000L, 5, ZoneOffset.UTC) + " " + LocalDateTime.ofInstant(Instant.ofEpochSecond(0), ZoneOffset.ofHours(1)) + " " + LocalDateTime.MIN + " " + LocalDateTime.MAX);
    probe("dt-parse", () -> LocalDateTime.parse("2024-02-29T10:15") + " " + LocalDateTime.parse("2024-02-29T10:15:30.123"));
    probe("dt-compare", () -> dt.compareTo(dt.plusNanos(1)) + " " + dt.equals(LocalDateTime.of(d, t)) + " " + dt.isBefore(dt.plusDays(1)) + " " + dt.hashCode());
    probe("instant", () -> Instant.ofEpochSecond(1700000000L, 123) + " " + Instant.ofEpochMilli(-1) + " " + Instant.EPOCH + " " + Instant.MIN + " " + Instant.MAX + " " + Instant.ofEpochSecond(0, -1));
    probe("instant-ops", () -> { Instant i = Instant.ofEpochSecond(1700000000L, 500000000); return i.plusSeconds(1) + " " + i.plusMillis(600) + " " + i.minusNanos(500000001) + " " + i.plus(2, ChronoUnit.HOURS) + " " + i.plus(Duration.ofDays(1)) + " " + i.toEpochMilli() + " " + i.getEpochSecond() + " " + i.getNano() + " " + i.truncatedTo(ChronoUnit.SECONDS) + " " + i.truncatedTo(ChronoUnit.DAYS); });
    probe("instant-parse", () -> Instant.parse("2024-02-29T10:15:30Z") + " " + Instant.parse("2024-02-29T10:15:30.123456Z") + " " + Instant.parse("2024-02-29T10:15:30+01:00"));
    probe("instant-until", () -> Instant.EPOCH.until(Instant.ofEpochSecond(100000), ChronoUnit.HOURS) + " " + Instant.EPOCH.until(Instant.ofEpochSecond(1, 1), ChronoUnit.NANOS));
    probe("instant-bad", () -> Instant.EPOCH.plus(1, ChronoUnit.MONTHS));
    probe("instant-overflow", () -> Instant.MAX.plusSeconds(1));
    probe("instant-fields", () -> { Instant i = Instant.ofEpochSecond(5, 1500000); return i.get(ChronoField.MILLI_OF_SECOND) + " " + i.getLong(ChronoField.INSTANT_SECONDS) + " " + i.get(ChronoField.MICRO_OF_SECOND) + " " + i.isSupported(ChronoField.DAY_OF_MONTH) + " " + i.with(ChronoField.NANO_OF_SECOND, 7); });
    probe("duration", () -> Duration.ofSeconds(3661, 5) + " " + Duration.ofMillis(-1500) + " " + Duration.ofDays(2) + " " + Duration.ZERO + " " + Duration.ofNanos(-1) + " " + Duration.ofMinutes(90) + " " + Duration.ofHours(25).plusMinutes(1));
    probe("duration-ops", () -> { Duration x = Duration.ofSeconds(100, 500); return x.multipliedBy(3) + " " + x.dividedBy(3) + " " + x.negated() + " " + x.abs() + " " + x.toMinutes() + " " + x.toMillis() + " " + x.toNanos() + " " + x.toHours() + " " + x.toDays() + " " + x.compareTo(Duration.ofSeconds(101)) + " " + x.minus(Duration.ofSeconds(200)) + " " + x.dividedBy(Duration.ofSeconds(7)) + " " + x.isNegative() + x.isZero(); });
    probe("duration-parts", () -> { Duration x = Duration.ofSeconds(100000, 123456789); return x.toDaysPart() + " " + x.toHoursPart() + " " + x.toMinutesPart() + " " + x.toSecondsPart() + " " + x.toMillisPart() + " " + x.toNanosPart(); });
    probe("duration-parse", () -> Duration.parse("PT1H30M") + " " + Duration.parse("P2DT3H4M") + " " + Duration.parse("PT-1.5S") + " " + Duration.parse("-PT1S") + " " + Duration.parse("PT0.000000001S"));
    probe("duration-parse-bad", () -> Duration.parse("1H"));
    probe("duration-between", () -> Duration.between(LocalTime.of(1, 0), LocalTime.of(3, 30)) + " " + Duration.between(dt, dt.plusDays(2)) + " " + Duration.between(Instant.EPOCH, Instant.ofEpochSecond(90)));
    probe("duration-truncate", () -> Duration.ofSeconds(3725, 999).truncatedTo(ChronoUnit.MINUTES) + " " + Duration.of(3, ChronoUnit.HALF_DAYS) + " " + Duration.ofSeconds(5).plus(2, ChronoUnit.MINUTES));
    probe("duration-units", () -> Duration.ofSeconds(5).getUnits() + " " + Duration.ofSeconds(5, 7).get(ChronoUnit.NANOS) + " " + Duration.ofSeconds(5).get(ChronoUnit.SECONDS));
    probe("duration-bad", () -> Duration.ofSeconds(5).get(ChronoUnit.DAYS));
    probe("month", () -> Month.FEBRUARY.length(true) + " " + Month.of(12) + " " + Month.JANUARY.minus(1) + " " + Month.MARCH.plus(11) + " " + Month.APRIL.firstMonthOfQuarter() + " " + Month.DECEMBER.maxLength() + " " + Month.FEBRUARY.minLength() + " " + Month.JUNE.firstDayOfYear(false) + " " + Month.from(d) + " " + Month.of(2).getValue());
    probe("month-bad", () -> Month.of(0));
    probe("dow", () -> DayOfWeek.MONDAY.minus(1) + " " + DayOfWeek.of(7) + " " + DayOfWeek.FRIDAY.plus(10) + " " + DayOfWeek.from(d) + " " + DayOfWeek.SUNDAY.getValue());
    probe("dow-bad", () -> DayOfWeek.of(8));
    probe("offset", () -> ZoneOffset.ofHours(5) + " " + ZoneOffset.ofHoursMinutes(-3, -30) + " " + ZoneOffset.ofTotalSeconds(3661) + " " + ZoneOffset.UTC + " " + ZoneOffset.of("+02:00") + " " + ZoneOffset.of("Z") + " " + ZoneOffset.of("-0530") + " " + ZoneOffset.ofHours(3).getTotalSeconds() + " " + ZoneOffset.ofHours(3).getId());
    probe("offset-bad", () -> ZoneOffset.ofHours(19));
    probe("offset-bad2", () -> ZoneOffset.of("garbage"));
    probe("offset-cmp", () -> ZoneOffset.ofHours(1).compareTo(ZoneOffset.ofHours(2)) + " " + ZoneOffset.MAX + " " + ZoneOffset.MIN);
    probe("zoneid", () -> ZoneId.of("UTC") + " " + ZoneId.of("Z") + " " + ZoneId.of("+01:00") + " " + ZoneId.of("UTC+2") + " " + ZoneId.of("GMT") + " " + ZoneId.of("UT") + " " + ZoneId.of("GMT-05:30").getId());
    probe("zoneid-bad", () -> ZoneId.of("Nope/Zone"));
    probe("clock", () -> { Clock c = Clock.fixed(Instant.ofEpochSecond(1700000000L), ZoneOffset.UTC); return c.instant() + " " + c.millis() + " " + c.getZone() + " " + LocalDate.now(c) + " " + LocalTime.now(c) + " " + LocalDateTime.now(c) + " " + Instant.now(c) + " " + Clock.offset(c, Duration.ofHours(1)).instant() + " " + Clock.tick(c, Duration.ofMinutes(1)).instant() + " " + c; });
    probe("chrono-units", () -> ChronoUnit.DAYS.getDuration() + " " + ChronoUnit.YEARS.getDuration() + " " + ChronoUnit.FOREVER.getDuration() + " " + ChronoUnit.HALF_DAYS.isTimeBased() + " " + ChronoUnit.MONTHS.isDateBased() + " " + ChronoUnit.CENTURIES + " " + ChronoUnit.DAYS.isDurationEstimated() + " " + ChronoUnit.WEEKS.isDurationEstimated());
    probe("chrono-fields", () -> ChronoField.DAY_OF_MONTH.range() + " " + ChronoField.YEAR.range() + " " + ChronoField.NANO_OF_DAY.range() + " " + ChronoField.ERA.range() + " " + ChronoField.MONTH_OF_YEAR.range() + " " + ChronoField.INSTANT_SECONDS.range() + " " + ChronoField.OFFSET_SECONDS.range() + " " + ChronoField.PROLEPTIC_MONTH.range() + " " + ChronoField.DAY_OF_YEAR.range() + " " + ChronoField.ALIGNED_WEEK_OF_MONTH.range());
    probe("chrono-field-check", () -> ChronoField.HOUR_OF_DAY.checkValidValue(24));
    probe("chrono-field-check2", () -> ChronoField.HOUR_OF_DAY.checkValidIntValue(23) + " " + ChronoField.MONTH_OF_YEAR.isDateBased() + " " + ChronoField.AMPM_OF_DAY.isTimeBased() + " " + ChronoField.EPOCH_DAY.getBaseUnit() + " " + ChronoField.EPOCH_DAY.getRangeUnit() + " " + ChronoField.YEAR.getDisplayName(Locale.ENGLISH));
    probe("valuerange", () -> ValueRange.of(1, 28, 31) + " " + ValueRange.of(1, 12) + " " + ValueRange.of(1, 5, 10, 20) + " " + ValueRange.of(1, 12).isFixed() + " " + ValueRange.of(1, 28, 31).isFixed() + " " + ValueRange.of(1, 28, 31).getSmallestMaximum() + " " + ValueRange.of(0, 10).isValidValue(11) + " " + ValueRange.of(0, Long.MAX_VALUE).isIntValue());
    probe("valuerange-bad", () -> ValueRange.of(5, 1));
    probe("valuerange-check", () -> ValueRange.of(1, 12).checkValidIntValue(13, ChronoField.MONTH_OF_YEAR));
    probe("queries", () -> d.query(TemporalQueries.localDate()) + " " + t.query(TemporalQueries.localTime()) + " " + d.query(TemporalQueries.precision()) + " " + t.query(TemporalQueries.precision()) + " " + dt.query(TemporalQueries.localTime()) + " " + d.query(TemporalQueries.zone()) + " " + d.query(TemporalQueries.offset()) + " " + d.query(TemporalQueries.chronology()));
    probe("instant-query", () -> Instant.EPOCH.query(TemporalQueries.precision()));
    probe("local-from", () -> LocalDate.from(dt) + " " + LocalTime.from(dt) + " " + LocalDateTime.from(dt) + " " + Instant.from(dt.atZone(ZoneOffset.UTC)));
    probe("local-from-bad", () -> LocalDate.from(t));
    probe("timeunit", () -> TimeUnit.MINUTES.toChronoUnit() + " " + TimeUnit.of(ChronoUnit.HOURS) + " " + TimeUnit.SECONDS.convert(Duration.ofMillis(2500)) + " " + TimeUnit.MILLISECONDS.convert(Duration.ofSeconds(Long.MAX_VALUE)) + " " + TimeUnit.MILLISECONDS.convert(Duration.ofSeconds(-3, 500_000_000)) + " " + TimeUnit.NANOSECONDS.convert(Duration.ofSeconds(1, 5)) + " " + TimeUnit.HOURS.convert(Duration.ofDays(2)) + " " + TimeUnit.DAYS.convert(Duration.ofHours(47)) + " " + TimeUnit.SECONDS.toNanos(Long.MAX_VALUE) + " " + TimeUnit.SECONDS.toNanos(Long.MIN_VALUE) + " " + TimeUnit.MINUTES.toMillis(Long.MAX_VALUE / 1000) + " " + TimeUnit.DAYS.toSeconds(-1) + " " + TimeUnit.MICROSECONDS.toNanos(Long.MAX_VALUE / 1000) + " " + TimeUnit.MICROSECONDS.toNanos(Long.MAX_VALUE / 1000 + 1));
    probe("timeunit-bad", () -> TimeUnit.of(ChronoUnit.WEEKS));
    probe("date-interop", () -> java.util.Date.from(Instant.ofEpochMilli(1700000000123L)).toInstant() + " " + java.util.Date.from(Instant.ofEpochSecond(5)).getTime());
    probe("hash-str", () -> t.hashCode() + " " + Duration.ofSeconds(5, 3).hashCode() + " " + Instant.ofEpochSecond(5, 3).hashCode() + " " + Month.MAY.hashCode() * 0);
  }
}
