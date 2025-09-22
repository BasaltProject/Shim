/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.util.*;
import java.util.function.Supplier;

public class CalendarModern {
  static void probe(String name, Supplier<Object> body) {
    String out;
    try {
      out = String.valueOf(body.get());
    } catch (Throwable t) {
      out = "!" + t.getClass().getName() + ":" + t.getMessage();
    }
    System.out.println(name + "=" + out);
  }

  static String fields(Calendar c) {
    StringBuilder sb = new StringBuilder();
    for (int f = 0; f < Calendar.FIELD_COUNT; f++) {
      sb.append(c.get(f)).append(f == Calendar.FIELD_COUNT - 1 ? "" : ",");
    }
    return sb.toString();
  }

  static Calendar cal(String zone, long millis) {
    Calendar c = new GregorianCalendar(TimeZone.getTimeZone(zone), Locale.US);
    c.setTimeInMillis(millis);
    return c;
  }

  static Calendar at(String zone, int y, int m, int d, int h, int mi, int s) {
    Calendar c = new GregorianCalendar(TimeZone.getTimeZone(zone), Locale.US);
    c.clear();
    c.set(y, m, d, h, mi, s);
    return c;
  }

  public static void main(String[] args) {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    long[] instants = {0L, 1700000000123L, -1L, 951782400000L, 1709208000000L, -62135769600000L, -12219292800000L, -12219292800001L,
        4102444800000L, -2208988800000L, 1711846800000L, 1730613600000L};
    String[] zones = {"UTC", "America/New_York", "Europe/Paris", "Asia/Kolkata", "Australia/Sydney", "GMT+05:30", "Pacific/Auckland"};
    for (String z : zones) {
      for (int i = 0; i < instants.length; i++) {
        final String zone = z;
        final long ms = instants[i];
        if (!z.equals("UTC") && !z.startsWith("GMT") && ms < 1500000000000L && ms != 4102444800000L) {
          continue;
        }
        probe("fields[" + z + "][" + ms + "]", () -> fields(cal(zone, ms)) + " " + cal(zone, ms).getTime());
      }
    }
    int[][] setCases = {{2024, 1, 29, 12, 0, 0}, {2023, 1, 29, 12, 0, 0}, {2024, 12, 1, 25, 61, 61}, {2024, -1, 1, 0, 0, 0}, {2024, 0, 0, 0, 0, 0}, {2024, 0, 32, 0, 0, 0},
        {1582, 9, 4, 12, 0, 0}, {1582, 9, 10, 12, 0, 0}, {1582, 9, 15, 12, 0, 0}, {1582, 9, 31, 12, 0, 0}, {1582, 10, 1, 0, 0, 0}, {1, 0, 1, 0, 0, 0}, {0, 0, 1, 0, 0, 0}, {-100, 5, 15, 0, 0, 0},
        {2024, 2, 10, 2, 30, 0}, {2024, 10, 3, 1, 30, 0}, {2024, 2, 31, 2, 30, 0}, {1900, 1, 29, 0, 0, 0}, {2000, 1, 29, 0, 0, 0}, {2100, 1, 29, 0, 0, 0}};
    int[][] historic = {{1582, 9, 4, 12, 0, 0}, {1582, 9, 10, 12, 0, 0}, {1582, 9, 15, 12, 0, 0}, {1582, 9, 31, 12, 0, 0}, {1582, 10, 1, 0, 0, 0}, {1, 0, 1, 0, 0, 0}, {0, 0, 1, 0, 0, 0}, {-100, 5, 15, 0, 0, 0}, {1900, 1, 29, 0, 0, 0}, {2000, 1, 29, 0, 0, 0}};
    for (String z : new String[] {"UTC", "America/New_York", "Europe/Paris"}) {
      for (int[] sc : setCases) {
        if (!z.equals("UTC") && sc[0] < 2007) {
          continue;
        }
        final String zone = z;
        probe("set[" + z + "][" + Arrays.toString(sc) + "]", () -> { Calendar c = at(zone, sc[0], sc[1], sc[2], sc[3], sc[4], sc[5]); return c.getTimeInMillis() + " " + fields(c); });
      }
    }
    probe("nonlenient", () -> { Calendar c = at("UTC", 2024, 1, 30, 0, 0, 0); c.setLenient(false); return c.getTimeInMillis(); });
    probe("nonlenient2", () -> { Calendar c = new GregorianCalendar(TimeZone.getTimeZone("UTC")); c.setLenient(false); c.clear(); c.set(2024, 12, 1); return c.getTimeInMillis(); });
    probe("nonlenient3", () -> { Calendar c = new GregorianCalendar(TimeZone.getTimeZone("UTC")); c.setLenient(false); c.clear(); c.set(2024, 1, 29); c.set(Calendar.HOUR_OF_DAY, 25); return c.getTimeInMillis(); });
    probe("nonlenient4", () -> { Calendar c = new GregorianCalendar(TimeZone.getTimeZone("UTC")); c.setLenient(false); c.clear(); c.set(2024, 1, 29); return c.get(Calendar.DAY_OF_WEEK) + " " + c.isLenient(); });
    probe("nonlenient5", () -> { Calendar c = new GregorianCalendar(TimeZone.getTimeZone("UTC")); c.setLenient(false); c.clear(); c.set(Calendar.YEAR, 2024); c.set(Calendar.WEEK_OF_YEAR, 60); return c.getTime(); });
    probe("nonlenient6", () -> { Calendar c = new GregorianCalendar(TimeZone.getTimeZone("UTC")); c.setLenient(false); c.clear(); c.set(2024, 2, 10, 2, 30, 0); return c.getTime(); });
    long[] addInstants = {1709208000000L, 1704067200000L, 1711846800000L, 1730613600000L, 1700000000123L, -12219292800000L - 86400000L * 2};
    int[] fieldsToTest = {Calendar.ERA, Calendar.YEAR, Calendar.MONTH, Calendar.WEEK_OF_YEAR, Calendar.WEEK_OF_MONTH, Calendar.DAY_OF_MONTH, Calendar.DAY_OF_YEAR, Calendar.DAY_OF_WEEK,
        Calendar.DAY_OF_WEEK_IN_MONTH, Calendar.AM_PM, Calendar.HOUR, Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND};
    for (String z : new String[] {"UTC", "America/New_York"}) {
      for (long ms : addInstants) {
        if (!z.equals("UTC") && ms < -10000000000000L) {
          continue;
        }
        for (int f : fieldsToTest) {
          for (int amt : new int[] {1, -1, 14, -30, 400}) {
            if (!z.equals("UTC") && (f == Calendar.ERA || ((f == Calendar.YEAR) && amt == -30))) {
              continue;
            }
            if (ms < -12000000000000L && (f == Calendar.WEEK_OF_MONTH || f == Calendar.WEEK_OF_YEAR || f == Calendar.DAY_OF_WEEK
                || f == Calendar.DAY_OF_MONTH || f == Calendar.DAY_OF_YEAR || f == Calendar.DAY_OF_WEEK_IN_MONTH)) {
              continue;
            }
            final String zone = z;
            probe("add[" + z + "][" + ms + "][" + Calendar.getInstance().getClass().getSimpleName().length() + f + "][" + amt + "]", () -> { Calendar c = cal(zone, ms); c.add(f, amt); return c.getTimeInMillis() + " " + c.get(Calendar.YEAR) + "-" + c.get(Calendar.MONTH) + "-" + c.get(Calendar.DAY_OF_MONTH) + " " + c.get(Calendar.HOUR_OF_DAY); });
            probe("roll[" + z + "][" + ms + "][" + f + "][" + amt + "]", () -> { Calendar c = cal(zone, ms); c.roll(f, amt); return c.getTimeInMillis() + " " + c.get(Calendar.YEAR) + "-" + c.get(Calendar.MONTH) + "-" + c.get(Calendar.DAY_OF_MONTH) + " " + c.get(Calendar.HOUR_OF_DAY); });
          }
        }
      }
    }
    probe("roll-feb29", () -> { Calendar c = at("UTC", 2024, 1, 29, 0, 0, 0); c.roll(Calendar.YEAR, 1); return c.getTime() + " " + c.get(Calendar.MONTH); });
    probe("add-month-end", () -> { Calendar c = at("UTC", 2024, 0, 31, 0, 0, 0); c.add(Calendar.MONTH, 1); return c.getTime().toString(); });
    probe("limits", () -> { Calendar c = at("UTC", 2024, 1, 15, 0, 0, 0); StringBuilder sb = new StringBuilder(); for (int f = 0; f < Calendar.FIELD_COUNT; f++) { sb.append(c.getMinimum(f)).append('/').append(c.getGreatestMinimum(f)).append('/').append(c.getLeastMaximum(f)).append('/').append(c.getMaximum(f)).append('/').append(c.getActualMinimum(f)).append('/').append(c.getActualMaximum(f)).append(' '); } return sb; });
    probe("limits-months", () -> { StringBuilder sb = new StringBuilder(); for (int[] ym : new int[][] {{2023, 1}, {2024, 1}, {2024, 3}, {1900, 1}, {2000, 1}}) { Calendar c = at("UTC", ym[0], ym[1], 1, 0, 0, 0); sb.append(c.getActualMaximum(Calendar.DAY_OF_MONTH)).append('/').append(c.getActualMaximum(Calendar.DAY_OF_YEAR)).append('/').append(c.getActualMaximum(Calendar.WEEK_OF_YEAR)).append('/').append(c.getActualMaximum(Calendar.WEEK_OF_MONTH)).append('/').append(c.getActualMaximum(Calendar.DAY_OF_WEEK_IN_MONTH)).append('/').append(c.getActualMinimum(Calendar.DAY_OF_MONTH)).append(' '); } return sb; });
    probe("week-defs", () -> { StringBuilder sb = new StringBuilder(); for (int fd = 1; fd <= 7; fd += 3) for (int md = 1; md <= 7; md += 3) { Calendar c = at("UTC", 2024, 0, 1, 0, 0, 0); c.setFirstDayOfWeek(fd); c.setMinimalDaysInFirstWeek(md); sb.append(c.get(Calendar.WEEK_OF_YEAR)).append('/').append(c.get(Calendar.WEEK_OF_MONTH)).append(' '); Calendar d = at("UTC", 2024, 11, 30, 0, 0, 0); d.setFirstDayOfWeek(fd); d.setMinimalDaysInFirstWeek(md); sb.append(d.get(Calendar.WEEK_OF_YEAR)).append('/').append(d.get(Calendar.WEEK_OF_MONTH)).append(' '); } return sb; });
    probe("week-year", () -> { StringBuilder sb = new StringBuilder(); for (int[] d : new int[][] {{2024, 0, 1}, {2024, 11, 30}, {2021, 0, 1}, {2020, 11, 31}, {2026, 0, 1}}) { Calendar c = at("UTC", d[0], d[1], d[2], 0, 0, 0); c.setFirstDayOfWeek(Calendar.MONDAY); c.setMinimalDaysInFirstWeek(4); sb.append(c.getWeekYear()).append('/').append(c.get(Calendar.WEEK_OF_YEAR)).append('/').append(c.getWeeksInWeekYear()).append(' '); } return sb; });
    probe("set-week-date", () -> { Calendar c = at("UTC", 2024, 0, 1, 0, 0, 0); c.setFirstDayOfWeek(Calendar.MONDAY); c.setMinimalDaysInFirstWeek(4); c.setWeekDate(2021, 1, Calendar.MONDAY); return c.getTime().getTime() + " " + c.getWeekYear() + " " + c.isWeekDateSupported(); });
    probe("set-order", () -> { Calendar c = at("UTC", 2024, 0, 1, 0, 0, 0); c.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY); c.set(Calendar.WEEK_OF_MONTH, 3); return c.getTime().getTime() + " " + fields(c); });
    probe("set-order2", () -> { Calendar c = at("UTC", 2024, 0, 1, 0, 0, 0); c.set(Calendar.DAY_OF_WEEK_IN_MONTH, 2); c.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY); return c.getTime().getTime(); });
    probe("set-order3", () -> { Calendar c = at("UTC", 2024, 0, 1, 0, 0, 0); c.set(Calendar.DAY_OF_YEAR, 100); return c.getTime().getTime(); });
    probe("set-order4", () -> { Calendar c = at("UTC", 2024, 0, 1, 0, 0, 0); c.set(Calendar.WEEK_OF_YEAR, 10); c.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY); return c.getTime().getTime(); });
    probe("set-order5", () -> { Calendar c = at("UTC", 2024, 0, 1, 0, 0, 0); c.set(Calendar.HOUR, 5); c.set(Calendar.AM_PM, Calendar.PM); return c.getTime().getTime() + " " + c.get(Calendar.HOUR_OF_DAY); });
    probe("set-order6", () -> { Calendar c = at("UTC", 2024, 0, 1, 7, 0, 0); c.set(Calendar.HOUR, 5); return c.getTime().getTime(); });
    probe("set-order7", () -> { Calendar c = at("UTC", 2024, 0, 1, 7, 0, 0); c.set(Calendar.HOUR, 5); c.set(Calendar.HOUR_OF_DAY, 9); return c.getTime().getTime(); });
    probe("clear", () -> { Calendar c = new GregorianCalendar(TimeZone.getTimeZone("UTC")); c.clear(); return c.getTimeInMillis() + " " + c.isSet(Calendar.YEAR) + " " + c.get(Calendar.YEAR) + " " + c.isSet(Calendar.YEAR); });
    probe("clear-field", () -> { Calendar c = at("UTC", 2024, 5, 15, 10, 20, 30); c.clear(Calendar.MONTH); c.clear(Calendar.HOUR_OF_DAY); return c.getTime().getTime() + " " + c.isSet(Calendar.MONTH); });
    probe("isset", () -> { Calendar c = new GregorianCalendar(TimeZone.getTimeZone("UTC")); c.clear(); c.set(Calendar.YEAR, 2000); return c.isSet(Calendar.YEAR) + " " + c.isSet(Calendar.MONTH) + " " + c.getTimeInMillis() + " " + c.isSet(Calendar.MONTH); });
    probe("zone-offset-set", () -> { Calendar c = new GregorianCalendar(TimeZone.getTimeZone("America/New_York")); c.clear(); c.set(2024, 6, 1, 12, 0, 0); c.set(Calendar.ZONE_OFFSET, 0); c.set(Calendar.DST_OFFSET, 0); return c.getTimeInMillis(); });
    probe("zone-switch", () -> { Calendar c = at("UTC", 2024, 6, 1, 12, 0, 0); long t = c.getTimeInMillis(); c.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo")); return t + " " + c.getTimeInMillis() + " " + c.get(Calendar.HOUR_OF_DAY) + " " + c.getTimeZone().getID(); });
    probe("cutover", () -> { GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US); c.setGregorianChange(new Date(Long.MIN_VALUE)); c.setTimeInMillis(-12219292800000L - 86400000L); return fields(c) + " " + c.isLeapYear(1500) + " " + c.getGregorianChange().getTime(); });
    probe("cutover-julian", () -> { GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US); c.setGregorianChange(new Date(Long.MAX_VALUE)); c.setTimeInMillis(0L); return fields(c) + " " + c.isLeapYear(1900) + " " + c.isLeapYear(2000); });
    probe("leap", () -> { GregorianCalendar c = new GregorianCalendar(); return c.isLeapYear(1900) + " " + c.isLeapYear(2000) + " " + c.isLeapYear(2024) + " " + c.isLeapYear(1500) + " " + c.isLeapYear(1582) + " " + c.isLeapYear(0) + " " + c.isLeapYear(-4) + " " + c.isLeapYear(100); });
    probe("builder", () -> { Calendar c = new Calendar.Builder().setCalendarType("gregory").setDate(2024, 1, 29).setTimeOfDay(13, 5, 9, 12).setTimeZone(TimeZone.getTimeZone("Europe/Paris")).setLocale(Locale.US).build(); return c.getTimeInMillis() + " " + fields(c) + " " + c.getClass().getSimpleName(); });
    probe("builder-instant", () -> new Calendar.Builder().setInstant(1700000000000L).setTimeZone(TimeZone.getTimeZone("UTC")).build().get(Calendar.YEAR));
    probe("builder-week", () -> { Calendar c = new Calendar.Builder().setWeekDate(2024, 10, Calendar.MONDAY).setTimeZone(TimeZone.getTimeZone("UTC")).setWeekDefinition(Calendar.MONDAY, 4).setLocale(Locale.US).build(); return c.getTime().getTime(); });
    probe("builder-iso", () -> { Calendar c = new Calendar.Builder().setCalendarType("iso8601").setDate(1500, 0, 1).setTimeZone(TimeZone.getTimeZone("UTC")).build(); return c.getTimeInMillis() + " " + c.getFirstDayOfWeek() + " " + c.getMinimalDaysInFirstWeek(); });
    probe("builder-bad", () -> new Calendar.Builder().setInstant(1L).set(Calendar.YEAR, 2000));
    probe("builder-lenient", () -> new Calendar.Builder().setLenient(false).setDate(2024, 1, 30).setTimeZone(TimeZone.getTimeZone("UTC")).build().getTime());
    probe("display", () -> { Calendar c = at("UTC", 2024, 1, 29, 13, 0, 0); return c.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US) + " " + c.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US) + " " + c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US) + " " + c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US) + " " + c.getDisplayName(Calendar.AM_PM, Calendar.SHORT, Locale.US) + " " + c.getDisplayName(Calendar.ERA, Calendar.LONG, Locale.US) + " " + c.getDisplayName(Calendar.YEAR, Calendar.LONG, Locale.US) + " " + c.getDisplayName(Calendar.MONTH, Calendar.NARROW_FORMAT, Locale.US); });
    probe("display-names", () -> new TreeMap<>(at("UTC", 2024, 1, 29, 13, 0, 0).getDisplayNames(Calendar.DAY_OF_WEEK, Calendar.ALL_STYLES, Locale.US)) + " " + new TreeMap<>(at("UTC", 2024, 1, 29, 13, 0, 0).getDisplayNames(Calendar.MONTH, Calendar.SHORT, Locale.US)) + " " + new TreeMap<>(at("UTC", 2024, 1, 29, 13, 0, 0).getDisplayNames(Calendar.AM_PM, Calendar.LONG, Locale.US)));
    probe("display-bad", () -> at("UTC", 2024, 1, 29, 13, 0, 0).getDisplayName(Calendar.MONTH, 3, Locale.US));
    probe("compare", () -> { Calendar a = at("UTC", 2024, 1, 29, 0, 0, 0), b = at("UTC", 2024, 1, 29, 0, 0, 1); return a.compareTo(b) + " " + a.before(b) + " " + a.after(b) + " " + a.equals(b) + " " + a.equals(a.clone()) + " " + (a.hashCode() == ((Calendar) a.clone()).hashCode()) + " " + a.before("x"); });
    probe("equals-zone", () -> { Calendar a = at("UTC", 2024, 1, 29, 0, 0, 0); Calendar b = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"), Locale.US); b.setTimeInMillis(a.getTimeInMillis()); return a.equals(b) + " " + a.compareTo(b); });
    probe("clone-independent", () -> { Calendar a = at("UTC", 2024, 1, 29, 0, 0, 0); Calendar b = (Calendar) a.clone(); b.add(Calendar.DAY_OF_MONTH, 1); return a.get(Calendar.DAY_OF_MONTH) + " " + b.get(Calendar.DAY_OF_MONTH); });
    probe("instance", () -> { Calendar c = Calendar.getInstance(); return c.getClass().getSimpleName() + " " + c.getTimeZone().getID() + " " + c.getFirstDayOfWeek() + " " + c.getMinimalDaysInFirstWeek() + " " + Calendar.getInstance(Locale.FRANCE).getFirstDayOfWeek() + " " + Calendar.getInstance(Locale.FRANCE).getMinimalDaysInFirstWeek() + " " + Calendar.getInstance(Locale.UK).getFirstDayOfWeek() + " " + Calendar.getInstance(new Locale("ar", "EG")).getFirstDayOfWeek() + " " + c.getCalendarType(); });
    probe("to-instant", () -> at("UTC", 2024, 1, 29, 13, 5, 9).toInstant() + " " + ((GregorianCalendar) at("America/New_York", 2024, 6, 1, 12, 0, 0)).toZonedDateTime() + " " + GregorianCalendar.from(java.time.ZonedDateTime.parse("2024-02-29T13:05:09+01:00[Europe/Paris]")).getTime().getTime() + " " + GregorianCalendar.from(java.time.ZonedDateTime.parse("2024-02-29T13:05:09Z")).getFirstDayOfWeek());
    probe("to-string", () -> { Calendar c = at("UTC", 2024, 1, 29, 13, 5, 9); String s = c.toString(); return s.substring(0, s.indexOf("zone=")) + " | " + s.substring(s.indexOf("firstDayOfWeek")); });
    probe("to-string-unset", () -> { Calendar c = new GregorianCalendar(TimeZone.getTimeZone("UTC")); c.clear(); c.set(Calendar.YEAR, 2000); String s = c.toString(); return s.substring(0, s.indexOf("zone=")) + " | " + s.substring(s.indexOf("firstDayOfWeek")); });
    probe("date", () -> { Date d = new Date(1700000000123L); return d + " | " + new Date(0) + " | " + new Date(-1L) + " | " + new Date(1709208000000L) + " | " + d.getTime() + " " + d.equals(new Date(1700000000123L)) + " " + d.hashCode() + " " + d.compareTo(new Date(0)) + " " + d.before(new Date(0)) + " " + d.after(new Date(0)) + " " + d.toInstant() + " " + Date.from(java.time.Instant.EPOCH).getTime() + " " + d.clone().equals(d); });
    probe("date-tz", () -> { TimeZone old = TimeZone.getDefault(); try { TimeZone.setDefault(TimeZone.getTimeZone("America/New_York")); return new Date(1700000000123L) + " | " + new Date(1720000000000L) + " | " + new Date(0); } finally { TimeZone.setDefault(old); } });
    probe("date-deprecated", () -> { @SuppressWarnings("deprecation") Date d = new Date(124, 1, 29, 13, 5, 9); return d + " " + d.getYear() + " " + d.getMonth() + " " + d.getDate() + " " + d.getDay() + " " + d.getHours() + " " + d.getMinutes() + " " + d.getSeconds() + " " + d.getTimezoneOffset() + " " + d.toGMTString() + " " + Date.UTC(124, 1, 29, 13, 5, 9); });
    probe("date-overflow", () -> new Date(Long.MAX_VALUE).toString() + " | " + new Date(Long.MIN_VALUE).toString());
    probe("date-from-bad", () -> Date.from(java.time.Instant.MAX));
    probe("tz-basic", () -> { StringBuilder sb = new StringBuilder(); for (String id : new String[] {"UTC", "GMT", "America/New_York", "Europe/Paris", "Asia/Kolkata", "Australia/Sydney", "PST", "EST", "MST", "HST", "CET", "GMT+5", "GMT-05:30", "GMT+0530", "GMT+14:00", "GMT+24:00", "GMT+5:7", "Nowhere/City", "UTC+3", "Z", "+01:00"}) { TimeZone tz = TimeZone.getTimeZone(id); sb.append(tz.getID()).append('/').append(tz.getRawOffset()).append('/').append(tz.useDaylightTime()).append('/').append(tz.getDSTSavings()).append('/').append(tz.getOffset(1720000000000L)).append('/').append(tz.getOffset(1700000000000L)).append(' '); } return sb; });
    probe("tz-names", () -> { StringBuilder sb = new StringBuilder(); for (String id : new String[] {"UTC", "America/New_York", "Europe/Paris", "PST", "EST", "GMT+05:30"}) { TimeZone tz = TimeZone.getTimeZone(id); sb.append(tz.getDisplayName(false, TimeZone.SHORT, Locale.US)).append('|').append(tz.getDisplayName(true, TimeZone.SHORT, Locale.US)).append('|').append(tz.getDisplayName(false, TimeZone.LONG, Locale.US)).append('|').append(tz.getDisplayName(true, TimeZone.LONG, Locale.US)).append(' '); } return sb; });
    probe("tz-names-bad", () -> TimeZone.getTimeZone("UTC").getDisplayName(false, 5, Locale.US));
    probe("tz-dst", () -> { TimeZone tz = TimeZone.getTimeZone("America/New_York"); return tz.inDaylightTime(new Date(1720000000000L)) + " " + tz.inDaylightTime(new Date(1700000000000L)) + " " + tz.getOffset(GregorianCalendar.AD, 2024, 6, 1, Calendar.MONDAY, 0) + " " + tz.getOffset(GregorianCalendar.AD, 2024, 0, 1, Calendar.MONDAY, 0) + " " + tz.observesDaylightTime() + " " + TimeZone.getTimeZone("UTC").observesDaylightTime() + " " + tz.hasSameRules(TimeZone.getTimeZone("UTC")); });
    probe("tz-offset-bad", () -> TimeZone.getTimeZone("UTC").getOffset(3, 2024, 0, 1, 1, 0));
    probe("tz-to-zoneid", () -> TimeZone.getTimeZone("America/New_York").toZoneId() + " " + TimeZone.getTimeZone("GMT+05:30").toZoneId() + " " + TimeZone.getTimeZone("PST").toZoneId() + " " + TimeZone.getTimeZone(java.time.ZoneId.of("Europe/Paris")).getID() + " " + TimeZone.getTimeZone(java.time.ZoneId.of("+02:00")).getID() + " " + TimeZone.getTimeZone(java.time.ZoneOffset.UTC).getID());
    probe("tz-default", () -> { TimeZone old = TimeZone.getDefault(); TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tokyo")); String a = TimeZone.getDefault().getID(); TimeZone.setDefault(null); TimeZone.setDefault(old); return a + " " + (TimeZone.getDefault() != TimeZone.getDefault()) + " " + TimeZone.getDefault().equals(TimeZone.getDefault()); });
    probe("tz-ids", () -> { String[] ids = TimeZone.getAvailableIDs(); List<String> l = Arrays.asList(ids); return l.contains("Europe/Paris") + " " + l.contains("UTC") + " " + l.contains("PST") + " " + Arrays.asList(TimeZone.getAvailableIDs(3600000)).contains("Europe/Paris") + " " + Arrays.asList(TimeZone.getAvailableIDs(0)).contains("UTC"); });
    probe("tz-set-raw", () -> { TimeZone tz = TimeZone.getTimeZone("America/New_York"); tz.setRawOffset(3600000); return tz.getRawOffset() + " " + tz.getOffset(1720000000000L) + " " + tz.getOffset(1700000000000L); });
    probe("tz-setid", () -> { TimeZone tz = TimeZone.getTimeZone("UTC"); tz.setID("X"); return tz.getID() + " " + TimeZone.getTimeZone("UTC").getID(); });
    probe("simple-tz", () -> { SimpleTimeZone tz = new SimpleTimeZone(-18000000, "MyEST", Calendar.MARCH, 8, -Calendar.SUNDAY, 7200000, Calendar.NOVEMBER, 1, -Calendar.SUNDAY, 7200000, 3600000); return tz.getRawOffset() + " " + tz.getDSTSavings() + " " + tz.useDaylightTime() + " " + tz.getOffset(1720000000000L) + " " + tz.getOffset(1700000000000L) + " " + tz.inDaylightTime(new Date(1720000000000L)) + " " + tz.inDaylightTime(new Date(1700000000000L)) + " " + tz.getOffset(1710054000000L - 1) + " " + tz.getOffset(1710054000000L) + " " + tz; });
    probe("simple-tz-modes", () -> { SimpleTimeZone tz = new SimpleTimeZone(0, "X", Calendar.APRIL, 1, Calendar.SUNDAY, 3600000, SimpleTimeZone.UTC_TIME, Calendar.OCTOBER, -1, Calendar.SUNDAY, 3600000, SimpleTimeZone.UTC_TIME, 3600000); StringBuilder sb = new StringBuilder(); for (long t : new long[] {1711846800000L - 1, 1711846800000L, 1730001600000L, 1735689600000L, 1730048400000L - 1, 1730048400000L}) sb.append(tz.getOffset(t)).append(' '); return sb + tz.toString(); });
    probe("simple-tz-fixed", () -> { SimpleTimeZone tz = new SimpleTimeZone(3600000, "Fixed"); return tz.useDaylightTime() + " " + tz.getDSTSavings() + " " + tz.getOffset(0) + " " + tz.getID() + " " + tz.hasSameRules(new SimpleTimeZone(3600000, "Other")) + " " + tz.equals(new SimpleTimeZone(3600000, "Fixed")) + " " + tz; });
    probe("simple-tz-bad", () -> new SimpleTimeZone(0, "X", 13, 1, 1, 0, 1, 1, 1, 0));
    probe("simple-tz-bad2", () -> new SimpleTimeZone(0, "X", 1, 32, 0, 0, 1, 1, 1, 0));
    probe("simple-tz-calendar", () -> { SimpleTimeZone tz = new SimpleTimeZone(-18000000, "MyEST", Calendar.MARCH, 8, -Calendar.SUNDAY, 7200000, Calendar.NOVEMBER, 1, -Calendar.SUNDAY, 7200000); Calendar c = new GregorianCalendar(tz); c.clear(); c.set(2024, 6, 1, 12, 0, 0); return c.getTimeInMillis() + " " + c.get(Calendar.DST_OFFSET) + " " + c.get(Calendar.ZONE_OFFSET); });
    probe("gc-ctors", () -> { GregorianCalendar a = new GregorianCalendar(2024, 1, 29); GregorianCalendar b = new GregorianCalendar(2024, 1, 29, 13, 5); GregorianCalendar c = new GregorianCalendar(2024, 1, 29, 13, 5, 9); return a.getTimeInMillis() + " " + b.getTimeInMillis() + " " + c.getTimeInMillis() + " " + c.get(Calendar.HOUR) + " " + c.get(Calendar.AM_PM); });
    probe("add-dst", () -> { Calendar c = at("America/New_York", 2024, 2, 9, 12, 0, 0); c.add(Calendar.DAY_OF_MONTH, 1); String a = c.getTime() + " " + c.get(Calendar.HOUR_OF_DAY); c.add(Calendar.HOUR_OF_DAY, 24); return a + " | " + c.getTime() + " " + c.get(Calendar.HOUR_OF_DAY) + " " + c.get(Calendar.DST_OFFSET); });
    probe("add-dst-fall", () -> { Calendar c = at("America/New_York", 2024, 10, 2, 12, 0, 0); c.add(Calendar.DAY_OF_MONTH, 1); String a = c.getTime() + " " + c.get(Calendar.HOUR_OF_DAY); c.add(Calendar.HOUR, 24); return a + " | " + c.getTime(); });
    probe("hour-roll-dst", () -> { Calendar c = at("America/New_York", 2024, 2, 10, 1, 30, 0); c.roll(Calendar.HOUR_OF_DAY, 1); return c.getTime() + " " + c.get(Calendar.HOUR_OF_DAY); });
    probe("get-after-set", () -> { Calendar c = at("UTC", 2024, 0, 31, 0, 0, 0); c.set(Calendar.MONTH, 1); return c.get(Calendar.MONTH) + " " + c.get(Calendar.DAY_OF_MONTH); });
    probe("lenient-month-overflow", () -> { Calendar c = at("UTC", 2024, 0, 1, 0, 0, 0); c.set(Calendar.MONTH, 14); c.set(Calendar.DAY_OF_MONTH, -3); return c.getTime().toString(); });
    probe("era-set", () -> { Calendar c = at("UTC", 2024, 0, 1, 0, 0, 0); c.set(Calendar.ERA, GregorianCalendar.BC); c.set(Calendar.YEAR, 44); return c.getTimeInMillis() + " " + fields(c); });
    probe("year-zero", () -> { Calendar c = at("UTC", 0, 0, 1, 0, 0, 0); return c.get(Calendar.ERA) + " " + c.get(Calendar.YEAR) + " " + c.getTimeInMillis(); });
    probe("get-time-no-fields", () -> { Calendar c = new GregorianCalendar(TimeZone.getTimeZone("UTC")); c.setTimeInMillis(86400000L * 365); return c.getTime() + " " + c.get(Calendar.DAY_OF_YEAR) + " " + c.get(Calendar.DAY_OF_WEEK); });
  }
}
