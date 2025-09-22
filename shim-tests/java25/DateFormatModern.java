/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.text.*;
import java.util.*;
import java.util.function.Supplier;

public class DateFormatModern {
  static void probe(String name, Supplier<Object> body) {
    String out;
    try {
      out = String.valueOf(body.get());
    } catch (Throwable t) {
      out = "!" + t.getClass().getName() + ":" + t.getMessage();
    }
    System.out.println(name + "=" + out.replace(' ', '~'));
  }

  static SimpleDateFormat sdf(String pattern, String zone) {
    SimpleDateFormat f = new SimpleDateFormat(pattern, Locale.US);
    f.setTimeZone(TimeZone.getTimeZone(zone));
    return f;
  }

  public static void main(String[] args) {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    Locale.setDefault(Locale.US);
    long[] times = {0L, 1700000000123L, 1720000000999L, -1L, 951782400000L, 1709208000000L, 1711846800000L, -62135769600000L,
        4102444800000L, 1735689599000L, 1704067200000L};
    String[] patterns = {"yyyy-MM-dd HH:mm:ss.SSS", "y/M/d H:m:s", "yy-MMM-dd EEE", "EEEE, MMMM d, yyyy h:mm:ss a", "G yyyy GGGG", "D F w W u k K h",
        "yyyy-MM-dd'T'HH:mm:ssXXX", "yyyyMMddHHmmss", "MMMMM EEEEE", "''yy'' 'literal' d", "Z X XX XXX", "YYYY-ww-u", "hh 'o''clock' a", "SSSSSS", "L LL LLL LLLL"};
    for (String z : new String[] {"UTC", "America/New_York", "Asia/Kolkata"}) {
      for (String p : patterns) {
        for (long t : times) {
          probe("fmt[" + z + "][" + p + "][" + t + "]", () -> sdf(p, z).format(new Date(t)));
        }
      }
    }
    for (String z : new String[] {"UTC", "America/New_York", "Europe/Paris", "Asia/Kolkata", "GMT+05:30"}) {
      for (long t : new long[] {1700000000123L, 1720000000999L}) {
        probe("zone[" + z + "][" + t + "]", () -> sdf("z zzzz", z).format(new Date(t)));
      }
    }
    String[][] parses = {
        {"yyyy-MM-dd HH:mm:ss", "2024-02-29 13:05:09"}, {"yyyy-MM-dd HH:mm:ss", "2024-02-29 13:05"}, {"yyyy-MM-dd", "2024-02-29 trailing"},
        {"yyyy-MM-dd", "24-2-9"}, {"yy-MM-dd", "24-02-29"}, {"yy-MM-dd", "99-02-28"}, {"yy-MM-dd", "50-02-28"}, {"yy-MM-dd", "2024-02-29"},
        {"yyyyMMddHHmmss", "20240229130509"}, {"yyyyMMdd", "20240229"}, {"MMM d, yyyy", "Feb 29, 2024"}, {"MMMM d, yyyy", "February 29, 2024"},
        {"MMM d, yyyy", "february 29, 2024"}, {"EEE MMM dd HH:mm:ss zzz yyyy", "Thu Feb 29 13:05:09 UTC 2024"},
        {"EEE MMM dd HH:mm:ss zzz yyyy", "Thu Feb 29 13:05:09 PST 2024"}, {"EEE MMM dd HH:mm:ss zzz yyyy", "Thu Feb 29 13:05:09 GMT+05:30 2024"},
        {"h:mm a", "1:05 PM"}, {"h:mm a", "12:05 AM"}, {"hh:mm a", "12:05 PM"}, {"K:mm a", "0:05 PM"}, {"k:mm", "24:05"}, {"H:mm", "25:61"},
        {"yyyy-MM-dd'T'HH:mm:ssXXX", "2024-02-29T13:05:09+05:30"}, {"yyyy-MM-dd'T'HH:mm:ssXXX", "2024-02-29T13:05:09Z"}, {"yyyy-MM-dd'T'HH:mm:ssZ", "2024-02-29T13:05:09-0800"},
        {"yyyy-MM-dd", "2024-13-45"}, {"yyyy-MM-dd", "abcd"}, {"yyyy-MM-dd", ""}, {"G yyyy", "BC 0044"}, {"G yyyy", "AD 2024"}, {"yyyy-MM-dd HH:mm:ss.SSS", "2024-02-29 13:05:09.123"},
        {"d MMM yyyy", "5 Mar 2024"}, {"EEEE", "Monday"}, {"u", "3"}, {"yyyy-MM-dd", "2024-02-30"}, {"hh:mm a", "1:05 PM"}, {"hh:mm a", "1:05 PM"}};
    for (boolean lenient : new boolean[] {true, false}) {
      for (String[] pt : parses) {
        probe("parse[" + lenient + "][" + pt[0] + "][" + pt[1] + "]", () -> {
          SimpleDateFormat f = sdf(pt[0], "UTC");
          f.setLenient(lenient);
          ParsePosition pos = new ParsePosition(0);
          Date d = f.parse(pt[1], pos);
          return (d == null ? "null" : d.getTime() + "") + " " + pos.getIndex() + " " + pos.getErrorIndex();
        });
      }
    }
    probe("parse-throws", () -> { try { sdf("yyyy-MM-dd", "UTC").parse("nope"); return "no"; } catch (ParseException e) { return e.getMessage() + " " + e.getErrorOffset(); } });
    probe("parse-nz", () -> { try { return sdf("yyyy-MM-dd HH:mm", "America/New_York").parse("2024-07-01 12:00").getTime(); } catch (ParseException e) { return e; } });
    probe("parse-explicit-zone", () -> { try { return sdf("yyyy-MM-dd HH:mm Z", "America/New_York").parse("2024-07-01 12:00 +0100").getTime(); } catch (ParseException e) { return e; } });
    probe("parse-start", () -> { SimpleDateFormat f = sdf("yyyy-MM-dd", "UTC"); ParsePosition p = new ParsePosition(3); Date d = f.parse("xx 2024-02-29", p); return d + " " + p; });
    probe("parse-start2", () -> { SimpleDateFormat f = sdf("yyyy-MM-dd", "UTC"); ParsePosition p = new ParsePosition(3); Date d = f.parse("xx 2024-02-29", p); return d; });
    probe("parse-object", () -> { try { return sdf("yyyy", "UTC").parseObject("2020"); } catch (ParseException e) { return e; } });
    probe("bad-pattern", () -> new SimpleDateFormat("yyyy-bb", Locale.US));
    probe("bad-pattern2", () -> new SimpleDateFormat("yyyy 'oops", Locale.US));
    probe("bad-pattern3", () -> new SimpleDateFormat("yyyy-MM-dd").format("string"));
    probe("bad-apply", () -> { SimpleDateFormat f = new SimpleDateFormat("yyyy"); f.applyPattern("q"); return f; });
    probe("pattern", () -> { SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH", Locale.US); return f.toPattern() + " " + f.toLocalizedPattern(); });
    probe("format-number", () -> sdf("yyyy-MM-dd", "UTC").format((Object) Long.valueOf(86400000L)));
    probe("format-object", () -> sdf("yyyy-MM-dd", "UTC").format(new Date(86400000L)));
    probe("field-pos", () -> { FieldPosition fp = new FieldPosition(DateFormat.MONTH_FIELD); StringBuffer sb = sdf("yyyy-MM-dd", "UTC").format(new Date(1700000000123L), new StringBuffer(), fp); return sb + " " + fp.getBeginIndex() + " " + fp.getEndIndex(); });
    probe("field-pos2", () -> { FieldPosition fp = new FieldPosition(DateFormat.AM_PM_FIELD); StringBuffer sb = sdf("h:mm a", "UTC").format(new Date(1700000000123L), new StringBuffer(), fp); return sb + " " + fp.getBeginIndex() + " " + fp.getEndIndex(); });
    probe("field-pos3", () -> { FieldPosition fp = new FieldPosition(DateFormat.HOUR1_FIELD); StringBuffer sb = sdf("h:mm a", "UTC").format(new Date(1700000000123L), new StringBuffer(), fp); return sb + " " + fp.getBeginIndex() + " " + fp.getEndIndex(); });
    probe("field-pos4", () -> { FieldPosition fp = new FieldPosition(DateFormat.YEAR_FIELD); StringBuffer sb = sdf("yyyy-MM-dd", "UTC").format(new Date(1700000000123L), new StringBuffer("pre:"), fp); return sb + " " + fp.getBeginIndex() + " " + fp.getEndIndex(); });
    String[] styles = {"FULL", "LONG", "MEDIUM", "SHORT"};
    for (int ds = 0; ds < 4; ds++) {
      for (int ts = 0; ts < 4; ts++) {
        final int d = ds, t = ts;
        probe("style[" + styles[ds] + "," + styles[ts] + "]", () -> { DateFormat f = DateFormat.getDateTimeInstance(d, t, Locale.US); f.setTimeZone(TimeZone.getTimeZone("America/New_York")); return f.format(new Date(1720000000999L)) + " | " + ((SimpleDateFormat) f).toPattern(); });
      }
      final int d = ds;
      probe("date-style[" + styles[ds] + "]", () -> { DateFormat f = DateFormat.getDateInstance(d, Locale.US); return f.format(new Date(1720000000999L)) + " | " + ((SimpleDateFormat) f).toPattern(); });
      probe("time-style[" + styles[ds] + "]", () -> { DateFormat f = DateFormat.getTimeInstance(d, Locale.US); return f.format(new Date(1720000000999L)) + " | " + ((SimpleDateFormat) f).toPattern(); });
    }
    probe("style-default", () -> DateFormat.getInstance().format(new Date(1720000000999L)) + " | " + DateFormat.getDateInstance().format(new Date(1720000000999L)) + " | " + DateFormat.getTimeInstance().format(new Date(1720000000999L)) + " | " + DateFormat.getDateTimeInstance().format(new Date(1720000000999L)));
    probe("style-bad", () -> DateFormat.getDateInstance(7));
    probe("style-bad2", () -> DateFormat.getTimeInstance(-1));
    probe("default-pattern", () -> new SimpleDateFormat().toPattern() + " " + new SimpleDateFormat().format(new Date(1720000000999L)));
    probe("symbols", () -> { DateFormatSymbols s = DateFormatSymbols.getInstance(Locale.US); return Arrays.toString(s.getMonths()) + Arrays.toString(s.getShortMonths()) + Arrays.toString(s.getWeekdays()) + Arrays.toString(s.getShortWeekdays()) + Arrays.toString(s.getAmPmStrings()) + Arrays.toString(s.getEras()) + s.getLocalPatternChars(); });
    probe("custom-symbols", () -> { DateFormatSymbols s = new DateFormatSymbols(Locale.US); s.setMonths(new String[] {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", ""}); s.setAmPmStrings(new String[] {"vorm", "nachm"}); SimpleDateFormat f = new SimpleDateFormat("MMMM a", s); f.setTimeZone(TimeZone.getTimeZone("UTC")); return f.format(new Date(1700000000123L)); });
    probe("century", () -> { SimpleDateFormat f = sdf("yy", "UTC"); f.set2DigitYearStart(new Date(0L)); try { return f.parse("69").getTime() + " " + f.parse("70").getTime() + " " + f.get2DigitYearStart().getTime(); } catch (ParseException e) { return e; } });
    probe("equals", () -> { SimpleDateFormat a = new SimpleDateFormat("yyyy", Locale.US), b = new SimpleDateFormat("yyyy", Locale.US), c = new SimpleDateFormat("yy", Locale.US); return a.equals(b) + " " + a.equals(c) + " " + (a.hashCode() == b.hashCode()) + " " + a.equals(a.clone()) + " " + (a.hashCode() == "yyyy".hashCode()); });
    probe("clone-independent", () -> { SimpleDateFormat a = sdf("yyyy-MM-dd HH", "UTC"); SimpleDateFormat b = (SimpleDateFormat) a.clone(); b.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo")); return a.format(new Date(0L)) + " | " + b.format(new Date(0L)) + " " + a.getTimeZone().getID() + " " + b.getTimeZone().getID(); });
    probe("lenient-default", () -> new SimpleDateFormat("yyyy").isLenient() + " " + new SimpleDateFormat("yyyy").getTimeZone().getID() + " " + new SimpleDateFormat("yyyy").getCalendar().getClass().getSimpleName() + " " + new SimpleDateFormat("yyyy").getNumberFormat().getClass().getSimpleName());
    probe("nonlenient-format-parse", () -> { SimpleDateFormat f = sdf("yyyy-MM-dd", "UTC"); f.setLenient(false); try { return f.parse("2023-02-29").getTime(); } catch (ParseException e) { return e.getMessage() + " " + e.getErrorOffset(); } });
    probe("lenient-overflow", () -> { SimpleDateFormat f = sdf("yyyy-MM-dd", "UTC"); try { return f.parse("2023-02-31").getTime() + " " + f.format(f.parse("2023-14-01")); } catch (ParseException e) { return e.getMessage(); } });
    probe("calendar-set", () -> { SimpleDateFormat f = sdf("yyyy-MM-dd HH:mm", "UTC"); Calendar c = new GregorianCalendar(TimeZone.getTimeZone("Asia/Tokyo"), Locale.US); f.setCalendar(c); return f.format(new Date(0L)) + " " + f.getTimeZone().getID(); });
    probe("set-timezone", () -> { SimpleDateFormat f = sdf("HH:mm z", "UTC"); f.setTimeZone(TimeZone.getTimeZone("America/New_York")); return f.format(new Date(1720000000999L)) + " " + f.format(new Date(1700000000123L)); });
    probe("week-date", () -> { SimpleDateFormat f = sdf("YYYY-'W'ww-u", "UTC"); return f.format(new Date(1735603200000L)) + " " + f.format(new Date(1704067200000L)) + " " + f.format(new Date(1609459200000L)); });
  }
}
