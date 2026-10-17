/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.time.chrono;

import java.io.Serializable;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.ResolverStyle;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalField;
import java.time.temporal.ValueRange;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class HijrahChronology extends AbstractChronology implements Serializable {
  private static final long serialVersionUID = 3127340209035924785L;

  public static final HijrahChronology INSTANCE = new HijrahChronology();

  static final int MIN_YEAR = 1300;
  static final int MAX_YEAR = 1600;
  private static final long MIN_EPOCH_DAY = -31826L;
  private static final long MAX_EPOCH_DAY = 74838L;

  private static final String LONG_MONTHS =
      "aaad54ec96d46ea36caad5556a9792ba95d4ada55cd2d69574ab54b6a5ad4aea4f51768b6a5ad52d695b49da4dd26d955ac9" +
      "b62baa5b52ba956caae92f49762b6956acaba4bd25d92dc96d54daa5b52ba55b49b655729754b6a3752b6556aaab52bc95d4" +
      "ada55caad69574ab94baa5b52b6a5752768b745b5555a95b49da4dd26e936aaad54db25d52da95b4aba55b49b64b715b4ab5" +
      "a55d25e92ec96d4ae996b4aba93d49da4db2ab94baa5b52ba95b2ab5555c4bd23d91da95b4ab5a56d2b693b49b6556a9754b" +
      "6a56caad555b29b92ba95d4ada55aaab595749764baa5b52b6a56e4db25b52b6a5ad2ae92f49764b6a56acad655d49da4dd1" +
      "6d955aa5b52da95b4ad5956ca6e4aea4f52b6956aaab54bd25d92ea96d4ada95b4aba55b29b54d6a97547693749b5556aa6b" +
      "52ba8bd46da35caad64db26b94baa5b52b695751768b725b52b5655b49da4ed16d8b6aa6d52da95d4ada95b4ab653729762b" +
      "a95b2ab5555b25d92ec96d2ae956b4aba55d29d54daa9b54baa3b49ba4daaaad52da95d45ea2ec9ad556b26b94baa5d52da9" +
      "5b52ba8bb45b92da95ab4ada4ed16e8b6a56d535695d4ada8dd46da55b29d62bb15b4ab955aaaae92ec8f5276956aaad655d" +
      "29d";

  private static final int[] START_EPOCH_MONTHS = new int[(MAX_YEAR - MIN_YEAR + 1) * 12 + 1];

  static {
    int day = (int) MIN_EPOCH_DAY;
    for (int i = 0; i < START_EPOCH_MONTHS.length - 1; i++) {
      START_EPOCH_MONTHS[i] = day;
      day += isLong(i) ? 30 : 29;
    }
    START_EPOCH_MONTHS[START_EPOCH_MONTHS.length - 1] = day;
  }

  private static boolean isLong(int epochMonth) {
    int nibble = Character.digit(LONG_MONTHS.charAt(epochMonth / 4), 16);
    return ((nibble >> (3 - epochMonth % 4)) & 1) != 0;
  }

  private HijrahChronology() {
  }

  private Object readResolve() {
    return INSTANCE;
  }

  @Override
  public String getId() {
    return "Hijrah-umalqura";
  }

  @Override
  public String getCalendarType() {
    return "islamic-umalqura";
  }

  @Override
  public HijrahDate date(Era era, int yearOfEra, int month, int dayOfMonth) {
    return date(prolepticYear(era, yearOfEra), month, dayOfMonth);
  }

  @Override
  public HijrahDate date(int prolepticYear, int month, int dayOfMonth) {
    return HijrahDate.of(this, prolepticYear, month, dayOfMonth);
  }

  @Override
  public HijrahDate dateYearDay(Era era, int yearOfEra, int dayOfYear) {
    return dateYearDay(prolepticYear(era, yearOfEra), dayOfYear);
  }

  @Override
  public HijrahDate dateYearDay(int prolepticYear, int dayOfYear) {
    HijrahDate date = HijrahDate.of(this, prolepticYear, 1, 1);
    if (dayOfYear > date.lengthOfYear()) {
      throw new DateTimeException("Invalid dayOfYear: " + dayOfYear);
    }
    return date.plusDays(dayOfYear - 1);
  }

  @Override
  public HijrahDate dateEpochDay(long epochDay) {
    return HijrahDate.ofEpochDay(this, epochDay);
  }

  @Override
  public HijrahDate dateNow() {
    return dateNow(Clock.systemDefaultZone());
  }

  @Override
  public HijrahDate dateNow(ZoneId zone) {
    return dateNow(Clock.system(zone));
  }

  @Override
  public HijrahDate dateNow(Clock clock) {
    return date(LocalDate.now(clock));
  }

  @Override
  public HijrahDate date(TemporalAccessor temporal) {
    if (temporal instanceof HijrahDate) {
      return (HijrahDate) temporal;
    }
    return HijrahDate.ofEpochDay(this, temporal.getLong(ChronoField.EPOCH_DAY));
  }

  @Override
  public ChronoZonedDateTime<HijrahDate> zonedDateTime(TemporalAccessor temporal) {
    try {
      ZoneId zone = ZoneId.from(temporal);
      try {
        Instant instant = Instant.from(temporal);
        return zonedDateTime(instant, zone);
      } catch (DateTimeException ex1) {
        @SuppressWarnings({"unchecked", "rawtypes"})
        ChronoLocalDateTimeImpl<ChronoLocalDate> cldt = (ChronoLocalDateTimeImpl) localDateTime(temporal);
        @SuppressWarnings("unchecked")
        ChronoZonedDateTime<HijrahDate> result = (ChronoZonedDateTime<HijrahDate>) (ChronoZonedDateTime<?>)
            ChronoZonedDateTimeImpl.ofBest(cldt, zone, null);
        return result;
      }
    } catch (DateTimeException ex) {
      throw new DateTimeException("Unable to obtain ChronoZonedDateTime from TemporalAccessor: "
          + temporal.getClass(), ex);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public ChronoZonedDateTime<HijrahDate> zonedDateTime(Instant instant, ZoneId zone) {
    return (ChronoZonedDateTime<HijrahDate>) (ChronoZonedDateTime<?>) ChronoZonedDateTimeImpl.ofInstant(this, instant,
        zone);
  }

  @Override
  public boolean isLeapYear(long prolepticYear) {
    if (prolepticYear < MIN_YEAR || prolepticYear > MAX_YEAR) {
      return false;
    }
    return getYearLength((int) prolepticYear) > 354;
  }

  @Override
  public int prolepticYear(Era era, int yearOfEra) {
    if (!(era instanceof HijrahEra)) {
      throw new ClassCastException("Era must be HijrahEra");
    }
    return yearOfEra;
  }

  @Override
  public HijrahEra eraOf(int eraValue) {
    switch (eraValue) {
      case 1:
        return HijrahEra.AH;
      default:
        throw new DateTimeException("invalid Hijrah era");
    }
  }

  @Override
  public List<Era> eras() {
    return Arrays.<Era>asList(HijrahEra.values());
  }

  @Override
  public ValueRange range(ChronoField field) {
    switch (field) {
      case DAY_OF_MONTH:
        return ValueRange.of(1, 29, 30);
      case DAY_OF_YEAR:
        return ValueRange.of(1, 355);
      case ALIGNED_WEEK_OF_MONTH:
        return ValueRange.of(1, 5);
      case ALIGNED_WEEK_OF_YEAR:
        return ValueRange.of(1, 53);
      case YEAR_OF_ERA:
      case YEAR:
        return ValueRange.of(MIN_YEAR, MAX_YEAR);
      case ERA:
        return ValueRange.of(1, 1);
      default:
        return field.range();
    }
  }

  @Override
  public ChronoPeriod period(int years, int months, int days) {
    return new ChronoPeriodImpl(this, years, months, days);
  }

  @Override
  public HijrahDate resolveDate(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
    if (fieldValues.containsKey(ChronoField.EPOCH_DAY)) {
      return dateEpochDay(fieldValues.remove(ChronoField.EPOCH_DAY));
    }
    Long pMonth = fieldValues.remove(ChronoField.PROLEPTIC_MONTH);
    if (pMonth != null) {
      if (resolverStyle != ResolverStyle.LENIENT) {
        ChronoField.PROLEPTIC_MONTH.checkValidValue(pMonth);
      }
      addFieldValue(fieldValues, ChronoField.MONTH_OF_YEAR, Math.floorMod(pMonth, 12) + 1);
      addFieldValue(fieldValues, ChronoField.YEAR, Math.floorDiv(pMonth, 12));
    }
    Long yoeLong = fieldValues.remove(ChronoField.YEAR_OF_ERA);
    if (yoeLong != null) {
      if (resolverStyle != ResolverStyle.LENIENT) {
        ChronoField.YEAR_OF_ERA.checkValidValue(yoeLong);
      }
      Long era = fieldValues.remove(ChronoField.ERA);
      if (era == null) {
        Long year = fieldValues.get(ChronoField.YEAR);
        if (resolverStyle == ResolverStyle.STRICT) {
          if (year != null) {
            addFieldValue(fieldValues, ChronoField.YEAR, (year > 0 ? yoeLong : Math.subtractExact(1, yoeLong)));
          } else {
            fieldValues.put(ChronoField.YEAR_OF_ERA, yoeLong);
          }
        } else {
          addFieldValue(fieldValues, ChronoField.YEAR, (year == null || year > 0 ? yoeLong
              : Math.subtractExact(1, yoeLong)));
        }
      } else {
        range(ChronoField.ERA).checkValidValue(era, ChronoField.ERA);
        addFieldValue(fieldValues, ChronoField.YEAR, yoeLong);
      }
    } else if (fieldValues.containsKey(ChronoField.ERA)) {
      range(ChronoField.ERA).checkValidValue(fieldValues.get(ChronoField.ERA), ChronoField.ERA);
    }
    if (!fieldValues.containsKey(ChronoField.YEAR)) {
      return null;
    }
    if (fieldValues.containsKey(ChronoField.MONTH_OF_YEAR)) {
      if (fieldValues.containsKey(ChronoField.DAY_OF_MONTH)) {
        return resolveYmd(fieldValues, resolverStyle);
      }
      if (fieldValues.containsKey(ChronoField.ALIGNED_WEEK_OF_MONTH)) {
        if (fieldValues.containsKey(ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH)) {
          return resolveAligned(fieldValues, resolverStyle, ChronoField.MONTH_OF_YEAR,
              ChronoField.ALIGNED_WEEK_OF_MONTH, ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH);
        }
        if (fieldValues.containsKey(ChronoField.DAY_OF_WEEK)) {
          return resolveAlignedDow(fieldValues, resolverStyle, ChronoField.MONTH_OF_YEAR,
              ChronoField.ALIGNED_WEEK_OF_MONTH);
        }
      }
    }
    if (fieldValues.containsKey(ChronoField.DAY_OF_YEAR)) {
      return resolveYd(fieldValues, resolverStyle);
    }
    if (fieldValues.containsKey(ChronoField.ALIGNED_WEEK_OF_YEAR)) {
      if (fieldValues.containsKey(ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR)) {
        return resolveAligned(fieldValues, resolverStyle, null, ChronoField.ALIGNED_WEEK_OF_YEAR,
            ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR);
      }
      if (fieldValues.containsKey(ChronoField.DAY_OF_WEEK)) {
        return resolveAlignedDow(fieldValues, resolverStyle, null, ChronoField.ALIGNED_WEEK_OF_YEAR);
      }
    }
    return null;
  }

  private static void addFieldValue(Map<TemporalField, Long> fieldValues, ChronoField field, long value) {
    Long old = fieldValues.get(field);
    if (old != null && old.longValue() != value) {
      throw new DateTimeException("Conflict found: " + field + " " + old + " differs from " + field + " "
          + value);
    }
    fieldValues.put(field, value);
  }

  private HijrahDate resolveYmd(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
    int y = range(ChronoField.YEAR).checkValidIntValue(fieldValues.remove(ChronoField.YEAR), ChronoField.YEAR);
    if (resolverStyle == ResolverStyle.LENIENT) {
      long months = Math.subtractExact(fieldValues.remove(ChronoField.MONTH_OF_YEAR), 1);
      long days = Math.subtractExact(fieldValues.remove(ChronoField.DAY_OF_MONTH), 1);
      return date(y, 1, 1).plus(months, ChronoUnit.MONTHS).plus(days, ChronoUnit.DAYS);
    }
    int moy = range(ChronoField.MONTH_OF_YEAR).checkValidIntValue(fieldValues.remove(ChronoField.MONTH_OF_YEAR),
        ChronoField.MONTH_OF_YEAR);
    ValueRange domRange = range(ChronoField.DAY_OF_MONTH);
    int dom = domRange.checkValidIntValue(fieldValues.remove(ChronoField.DAY_OF_MONTH), ChronoField.DAY_OF_MONTH);
    if (resolverStyle == ResolverStyle.SMART) {
      try {
        return date(y, moy, dom);
      } catch (DateTimeException ex) {
        return date(y, moy, 1).with(TemporalAdjusters.lastDayOfMonth());
      }
    }
    return date(y, moy, dom);
  }

  private HijrahDate resolveYd(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
    int y = range(ChronoField.YEAR).checkValidIntValue(fieldValues.remove(ChronoField.YEAR), ChronoField.YEAR);
    if (resolverStyle == ResolverStyle.LENIENT) {
      long days = Math.subtractExact(fieldValues.remove(ChronoField.DAY_OF_YEAR), 1);
      return dateYearDay(y, 1).plus(days, ChronoUnit.DAYS);
    }
    int doy = range(ChronoField.DAY_OF_YEAR).checkValidIntValue(fieldValues.remove(ChronoField.DAY_OF_YEAR),
        ChronoField.DAY_OF_YEAR);
    return dateYearDay(y, doy);
  }

  private HijrahDate resolveAligned(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle,
      ChronoField month, ChronoField week, ChronoField dow) {
    int y = range(ChronoField.YEAR).checkValidIntValue(fieldValues.remove(ChronoField.YEAR), ChronoField.YEAR);
    long weeks = fieldValues.remove(week);
    long days = fieldValues.remove(dow);
    if (resolverStyle == ResolverStyle.LENIENT) {
      long moy = month == null ? 0 : Math.subtractExact(fieldValues.remove(month), 1);
      return date(y, 1, 1).plus(moy, ChronoUnit.MONTHS).plus(Math.subtractExact(weeks, 1), ChronoUnit.WEEKS)
          .plus(Math.subtractExact(days, 1), ChronoUnit.DAYS);
    }
    int moy = month == null ? 0 : range(ChronoField.MONTH_OF_YEAR).checkValidIntValue(fieldValues.remove(month),
        month);
    int aw = range(week).checkValidIntValue(weeks, week);
    int ad = range(dow).checkValidIntValue(days, dow);
    HijrahDate date = month == null ? date(y, 1, 1) : date(y, moy, 1);
    date = date.plus((aw - 1) * 7L + (ad - 1), ChronoUnit.DAYS);
    if (resolverStyle == ResolverStyle.STRICT && month != null && date.get(ChronoField.MONTH_OF_YEAR) != moy) {
      throw new DateTimeException("Strict mode rejected resolved date as it is in a different month");
    }
    return date;
  }

  private HijrahDate resolveAlignedDow(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle,
      ChronoField month, ChronoField week) {
    int y = range(ChronoField.YEAR).checkValidIntValue(fieldValues.remove(ChronoField.YEAR), ChronoField.YEAR);
    long weeks = fieldValues.remove(week);
    long dow = fieldValues.remove(ChronoField.DAY_OF_WEEK);
    if (resolverStyle == ResolverStyle.LENIENT) {
      long moy = month == null ? 0 : Math.subtractExact(fieldValues.remove(month), 1);
      return date(y, 1, 1).plus(moy, ChronoUnit.MONTHS).plus(Math.subtractExact(weeks, 1), ChronoUnit.WEEKS)
          .with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.of((int) Math.floorMod(dow - 1, 7) + 1)));
    }
    int moy = month == null ? 0 : range(ChronoField.MONTH_OF_YEAR).checkValidIntValue(fieldValues.remove(month),
        month);
    int aw = range(week).checkValidIntValue(weeks, week);
    int d = ChronoField.DAY_OF_WEEK.checkValidIntValue(dow);
    HijrahDate date = (month == null ? date(y, 1, 1) : date(y, moy, 1)).plus((aw - 1) * 7L, ChronoUnit.DAYS)
        .with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.of(d)));
    if (resolverStyle == ResolverStyle.STRICT && month != null && date.get(ChronoField.MONTH_OF_YEAR) != moy) {
      throw new DateTimeException("Strict mode rejected resolved date as it is in a different month");
    }
    return date;
  }

  @Override
  public String getDisplayName(TextStyle style, Locale locale) {
    Objects.requireNonNull(style, "style");
    Objects.requireNonNull(locale, "locale");
    return "Islamic Umm al-Qura Calendar";
  }

  int getYearLength(int year) {
    int index = (year - MIN_YEAR) * 12;
    return START_EPOCH_MONTHS[index + 12] - START_EPOCH_MONTHS[index];
  }

  int getMonthLengthForResolve(int year, int month) {
    int index = (year - MIN_YEAR) * 12 + (month - 1);
    if (month < 1 || month > 12) {
      throw new DateTimeException("Invalid Hijrah month: " + month);
    }
    if (index < 0 || index >= START_EPOCH_MONTHS.length - 1) {
      throw new DateTimeException("Invalid Hijrah date, year: " + year + ", month: " + month);
    }
    return START_EPOCH_MONTHS[index + 1] - START_EPOCH_MONTHS[index];
  }

  int getMonthLength(int year, int month) {
    int index = (year - MIN_YEAR) * 12 + (month - 1);
    return START_EPOCH_MONTHS[index + 1] - START_EPOCH_MONTHS[index];
  }

  int getDayOfYear(int year, int month) {
    int index = (year - MIN_YEAR) * 12;
    return START_EPOCH_MONTHS[index + month - 1] - START_EPOCH_MONTHS[index];
  }

  long getEpochDay(int year, int month, int dayOfMonth) {
    if (month < 1 || month > 12) {
      throw new DateTimeException("Invalid Hijrah month: " + month);
    }
    int epochMonth = (year - MIN_YEAR) * 12 + (month - 1);
    if (epochMonth < 0 || epochMonth >= START_EPOCH_MONTHS.length) {
      throw new DateTimeException("Invalid Hijrah date, year: " + year + ", month: " + month);
    }
    if (epochMonth + 1 >= START_EPOCH_MONTHS.length) {
      if (epochMonth + 1 > START_EPOCH_MONTHS.length) {
        throw new DateTimeException("Invalid Hijrah date, year: " + year + ", month: " + month);
      }
      throw new ArrayIndexOutOfBoundsException("Index " + (epochMonth + 1) + " out of bounds for length "
          + START_EPOCH_MONTHS.length);
    }
    int length = START_EPOCH_MONTHS[epochMonth + 1] - START_EPOCH_MONTHS[epochMonth];
    if (dayOfMonth < 1 || dayOfMonth > length) {
      throw new DateTimeException("Invalid Hijrah day of month: " + dayOfMonth);
    }
    return START_EPOCH_MONTHS[epochMonth] + dayOfMonth - 1;
  }

  int[] getHijrahDateInfo(long epochDay) {
    if (epochDay < MIN_EPOCH_DAY || epochDay > MAX_EPOCH_DAY) {
      throw new DateTimeException("Hijrah date out of range");
    }
    int day = (int) epochDay;
    int low = 0;
    int high = START_EPOCH_MONTHS.length - 2;
    while (low < high) {
      int mid = (low + high + 1) >>> 1;
      if (START_EPOCH_MONTHS[mid] <= day) {
        low = mid;
      } else {
        high = mid - 1;
      }
    }
    return new int[] {MIN_YEAR + low / 12, low % 12 + 1, day - START_EPOCH_MONTHS[low] + 1};
  }
}
