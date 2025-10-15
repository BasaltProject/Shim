/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.time.*; import java.util.*;
public class FormatterDatesModern {
  static String f(Locale l, String fmt, Object arg) { try { return String.format(l, fmt, arg); } catch (Throwable t) { return "!" + t.getClass().getSimpleName() + ":" + t.getMessage(); } }
  public static void main(String[] a) {
    Locale en = Locale.US;
    String convs = "HIklMSLNpzZsQBbhAaCYyjmdeRTrDFc";
    long millis = 1709654828123L;
    TimeZone.setDefault(TimeZone.getTimeZone("Europe/Madrid"));
    Object[] args = {millis, new Date(millis), new GregorianCalendar(TimeZone.getTimeZone("America/New_York")) {{ setTimeInMillis(millis); }}, ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.of("Asia/Kolkata")), ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.of("UTC")), ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.of("America/Los_Angeles")), OffsetDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneOffset.ofHoursMinutes(-3, -30)), LocalDateTime.of(2024, 3, 5, 6, 7, 8, 123456789), LocalDate.of(2024, 12, 31), LocalTime.of(23, 59, 1, 5000000), Instant.ofEpochMilli(millis), Year.of(2020), YearMonth.of(2020, 2), -1L, 0L, Long.MAX_VALUE / 1000000};
    for (Object arg : args) {
      StringBuilder sb = new StringBuilder(arg.getClass().getSimpleName() + ":");
      for (char c : convs.toCharArray()) sb.append(' ').append(c).append('=').append(f(en, "%t" + c, arg));
      System.out.println(sb);
    }
    System.out.println(f(en, "%Tp|%TB|%Ta|%TZ|%1$tp", ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.of("Europe/Madrid"))));
    System.out.println(f(en, "[%20tF] [%-20tT] [%020tY]", LocalDateTime.of(2024, 3, 5, 6, 7, 8)));
    System.out.println(f(en, "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL %1$tZ %1$tz", ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.of("Europe/Madrid"))));
    System.out.println(f(en, "%tZ %tZ", new Date(millis)) );
    System.out.println(f(en, "%tc", ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.of("Europe/Madrid"))));
    System.out.println(f(en, "%tc", millis));
    System.out.println(f(en, "%tc", ZonedDateTime.of(2024, 7, 1, 12, 0, 0, 0, ZoneId.of("Europe/Madrid"))));
    System.out.println(f(en, "%tZ", ZonedDateTime.of(2024, 7, 1, 12, 0, 0, 0, ZoneId.of("Europe/Madrid"))));
    System.out.println(f(en, "%tZ", ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneId.of("Europe/Madrid"))));
    System.out.println(f(en, "%tZ", ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.ofHours(5))));
    System.out.println(f(en, "%#tY", millis));
    System.out.println(f(en, "%,tY", millis));
    System.out.println(f(en, "%tK", millis));
    System.out.println(f(en, "%t", millis));
    System.out.println(f(en, "%tH", "string"));
    System.out.println(f(en, "%tH", (Object) null));
    System.out.println(f(en, "%.2tH", millis));
  }
}
