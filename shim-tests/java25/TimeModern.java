/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 17

import java.time.Clock;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalQueries;
import java.time.zone.ZoneRules;

public class TimeModern {
    public static void main(String[] args) {
        testInstant();
        testDuration();
        testLocalDate();
        testLocalTime();
        testLocalDateTime();
        testFormatters();
        testEnumsAndZones();
        System.out.println("ALL TIME MODERN TESTS PASSED");
    }

    private static void testInstant() {
        Instant ep = Instant.EPOCH;
        System.out.println("epoch: " + ep + ", sec: " + ep.getEpochSecond() + ", nano: " + ep.getNano() + ", milli: " + ep.toEpochMilli());

        Instant i1 = Instant.ofEpochSecond(1700000000L, 500000000L);
        System.out.println("i1: " + i1 + ", milli: " + i1.toEpochMilli());

        Instant i2 = i1.plusSeconds(3600).plusMillis(250).plusNanos(100);
        System.out.println("i2: " + i2);

        Instant i3 = i2.minus(Duration.ofMinutes(30));
        System.out.println("i3: " + i3);

        System.out.println("i1 before i2: " + i1.isBefore(i2));
        System.out.println("i2 after i1: " + i2.isAfter(i1));
        System.out.println("until seconds: " + i1.until(i2, ChronoUnit.SECONDS));

        Instant parsed = Instant.parse("2026-10-14T12:30:45.123456789Z");
        System.out.println("parsed instant: " + parsed);
        System.out.println("parsed sec: " + parsed.getEpochSecond() + ", nano: " + parsed.getNano());

        Instant fixedNow = Instant.now(Clock.fixed(i1, ZoneOffset.UTC));
        System.out.println("fixed now: " + fixedNow.equals(i1));
    }

    private static void testDuration() {
        Duration d1 = Duration.ofSeconds(90, 500000000L);
        System.out.println("d1: " + d1 + ", secs: " + d1.getSeconds() + ", nano: " + d1.getNano());
        System.out.println("d1 toMillis: " + d1.toMillis() + ", toNanos: " + d1.toNanos());
        System.out.println("d1 parts: minutes=" + d1.toMinutesPart() + ", seconds=" + d1.toSecondsPart() + ", millis=" + d1.toMillisPart());

        Duration d2 = Duration.ofDays(2).plusHours(5).plusMinutes(30);
        System.out.println("d2: " + d2 + ", toHours: " + d2.toHours() + ", toMinutes: " + d2.toMinutes());

        Duration sum = d1.plus(Duration.ofSeconds(30));
        System.out.println("sum: " + sum);

        Duration diff = d2.minus(Duration.ofHours(5));
        System.out.println("diff: " + diff);

        Duration mult = Duration.ofSeconds(10).multipliedBy(3);
        Duration div = Duration.ofSeconds(30).dividedBy(2);
        System.out.println("mult: " + mult + ", div: " + div);

        Instant start = Instant.ofEpochSecond(1000);
        Instant end = Instant.ofEpochSecond(1050, 250000000);
        Duration between = Duration.between(start, end);
        System.out.println("between: " + between);
    }

    private static void testLocalDate() {
        LocalDate date = LocalDate.of(2026, Month.OCTOBER, 14);
        System.out.println("date: " + date + ", year: " + date.getYear() + ", month: " + date.getMonth() + " (" + date.getMonthValue() + "), day: " + date.getDayOfMonth());
        System.out.println("dayOfWeek: " + date.getDayOfWeek() + ", dayOfYear: " + date.getDayOfYear());
        System.out.println("isLeapYear: " + date.isLeapYear() + ", lengthOfMonth: " + date.lengthOfMonth() + ", lengthOfYear: " + date.lengthOfYear());

        LocalDate leap = LocalDate.of(2024, 2, 29);
        System.out.println("leap: " + leap + ", isLeapYear: " + leap.isLeapYear() + ", lengthOfMonth: " + leap.lengthOfMonth());

        LocalDate epoch = LocalDate.ofEpochDay(0);
        System.out.println("epoch day 0: " + epoch + ", toEpochDay: " + epoch.toEpochDay());

        LocalDate added = date.plusDays(20).plusMonths(2).plusYears(1);
        System.out.println("added: " + added);

        LocalDate subtracted = added.minusDays(5).minusWeeks(2);
        System.out.println("subtracted: " + subtracted);

        LocalDate parsed = LocalDate.parse("2026-12-25");
        System.out.println("parsed date: " + parsed);
        System.out.println("date compare: " + date.compareTo(parsed) + ", before: " + date.isBefore(parsed) + ", after: " + date.isAfter(parsed));
    }

    private static void testLocalTime() {
        LocalTime time = LocalTime.of(14, 30, 45, 123456789);
        System.out.println("time: " + time + ", hour: " + time.getHour() + ", min: " + time.getMinute() + ", sec: " + time.getSecond() + ", nano: " + time.getNano());
        System.out.println("secOfDay: " + time.toSecondOfDay() + ", nanoOfDay: " + time.toNanoOfDay());

        LocalTime plusTime = time.plusHours(2).plusMinutes(15).plusSeconds(10);
        System.out.println("plusTime: " + plusTime);

        LocalTime minusTime = time.minusHours(5).minusMinutes(45);
        System.out.println("minusTime: " + minusTime);

        LocalTime midnight = LocalTime.MIDNIGHT;
        LocalTime noon = LocalTime.NOON;
        System.out.println("midnight: " + midnight + ", noon: " + noon);

        LocalTime parsed = LocalTime.parse("09:15:30");
        System.out.println("parsed time: " + parsed);
    }

    private static void testLocalDateTime() {
        LocalDate date = LocalDate.of(2026, 10, 14);
        LocalTime time = LocalTime.of(16, 45, 30);
        LocalDateTime dt = LocalDateTime.of(date, time);
        System.out.println("dt: " + dt);
        System.out.println("dt date: " + dt.toLocalDate() + ", time: " + dt.toLocalTime());

        LocalDateTime added = dt.plusDays(5).plusHours(3).plusMinutes(20);
        System.out.println("added dt: " + added);

        Instant inst = dt.toInstant(ZoneOffset.UTC);
        System.out.println("dt toInstant UTC: " + inst);

        LocalDateTime fromEpoch = LocalDateTime.ofEpochSecond(inst.getEpochSecond(), inst.getNano(), ZoneOffset.UTC);
        System.out.println("fromEpoch dt: " + fromEpoch + ", equals: " + dt.equals(fromEpoch));

        LocalDateTime parsed = LocalDateTime.parse("2026-10-14T16:45:30");
        System.out.println("parsed dt: " + parsed + ", equals: " + dt.equals(parsed));
    }

    private static void testFormatters() {
        LocalDate date = LocalDate.of(2026, 10, 14);
        LocalTime time = LocalTime.of(15, 30, 0);
        LocalDateTime dt = LocalDateTime.of(date, time);

        System.out.println("ISO_LOCAL_DATE: " + DateTimeFormatter.ISO_LOCAL_DATE.format(date));
        System.out.println("ISO_LOCAL_TIME: " + DateTimeFormatter.ISO_LOCAL_TIME.format(time));
        System.out.println("ISO_LOCAL_DATE_TIME: " + DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(dt));
        System.out.println("BASIC_ISO_DATE: " + DateTimeFormatter.BASIC_ISO_DATE.format(date));

        DateTimeFormatter custom = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        String formattedCustom = custom.format(dt);
        System.out.println("custom formatted: " + formattedCustom);

        LocalDate parsedBasic = LocalDate.parse("20261014", DateTimeFormatter.BASIC_ISO_DATE);
        System.out.println("parsed basic date: " + parsedBasic);
    }

    private static void testEnumsAndZones() {
        DayOfWeek dow = DayOfWeek.WEDNESDAY;
        System.out.println("dow: " + dow + ", value: " + dow.getValue() + ", plus 3: " + dow.plus(3) + ", minus 2: " + dow.minus(2));

        Month m = Month.FEBRUARY;
        System.out.println("month: " + m + ", value: " + m.getValue() + ", leap len: " + m.length(true) + ", non-leap: " + m.length(false));

        ZoneOffset zo1 = ZoneOffset.UTC;
        ZoneOffset zo2 = ZoneOffset.ofHours(3);
        ZoneOffset zo3 = ZoneOffset.ofHoursMinutes(-5, -30);
        System.out.println("zo1: " + zo1 + " (" + zo1.getTotalSeconds() + ")");
        System.out.println("zo2: " + zo2 + " (" + zo2.getTotalSeconds() + ")");
        System.out.println("zo3: " + zo3 + " (" + zo3.getTotalSeconds() + ")");

        ZoneRules rules = zo2.getRules();
        System.out.println("rules fixed: " + rules.isFixedOffset() + ", offset: " + rules.getOffset(Instant.EPOCH));

        ZoneId zone = ZoneId.of("UTC");
        System.out.println("zone: " + zone.getId() + ", rules offset: " + zone.getRules().getOffset(Instant.EPOCH));
    }
}
