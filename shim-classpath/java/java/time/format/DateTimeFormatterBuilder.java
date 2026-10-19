/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.time.format;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.DAY_OF_WEEK;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.INSTANT_SECONDS;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.NANO_OF_SECOND;
import static java.time.temporal.ChronoField.OFFSET_SECONDS;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;
import static java.time.temporal.ChronoField.YEAR;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.ParsePosition;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.Chronology;
import java.time.chrono.IsoChronology;
import java.time.temporal.ChronoField;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;
import java.time.temporal.ValueRange;
import java.time.temporal.WeekFields;
import java.time.zone.ZoneRulesProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class DateTimeFormatterBuilder {
  private DateTimeFormatterBuilder active = this;
  private final DateTimeFormatterBuilder parent;
  private final List<DateTimePrinterParser> printerParsers = new ArrayList<DateTimePrinterParser>();
  private final boolean optional;
  private int padNextWidth;
  private char padNextChar;
  private int valueParserIndex = -1;

  public static String getLocalizedDateTimePattern(FormatStyle dateStyle, FormatStyle timeStyle, Chronology chrono,
      Locale locale) {
    Objects.requireNonNull(locale, "locale");
    Objects.requireNonNull(chrono, "chrono");
    if (dateStyle == null && timeStyle == null) {
      throw new IllegalArgumentException("Either dateStyle or timeStyle must be non-null");
    }
    if (chrono == java.time.chrono.IsoChronology.INSTANCE) {
      String found = shim.LocaleDb.pattern(locale, dateStyle == null ? -1 : dateStyle.ordinal(),
          timeStyle == null ? -1 : timeStyle.ordinal());
      if (found != null) {
        return found;
      }
    }
    String datePattern = null;
    String timePattern = null;
    if (dateStyle != null) {
      boolean iso = chrono == java.time.chrono.IsoChronology.INSTANCE;
      switch (dateStyle) {
        case FULL:
          datePattern = iso ? "EEEE, MMMM d, y" : "EEEE, MMMM d, y G";
          break;
        case LONG:
          datePattern = iso ? "MMMM d, y" : "MMMM d, y G";
          break;
        case MEDIUM:
          datePattern = iso ? "MMM d, y" : "MMM d, y G";
          break;
        default:
          datePattern = iso ? "M/d/yy" : "M/d/y GGGGG";
          break;
      }
    }
    if (timeStyle != null) {
      switch (timeStyle) {
        case FULL:
          timePattern = "h:mm:ss a zzzz";
          break;
        case LONG:
          timePattern = "h:mm:ss a z";
          break;
        case MEDIUM:
          timePattern = "h:mm:ss a";
          break;
        default:
          timePattern = "h:mm a";
          break;
      }
    }
    if (datePattern != null && timePattern != null) {
      return datePattern + ", " + timePattern;
    }
    return datePattern != null ? datePattern : timePattern;
  }

  public DateTimeFormatterBuilder() {
    super();
    parent = null;
    optional = false;
  }

  private DateTimeFormatterBuilder(DateTimeFormatterBuilder parent, boolean optional) {
    super();
    this.parent = parent;
    this.optional = optional;
  }

  public DateTimeFormatterBuilder parseCaseSensitive() {
    appendInternal(SettingsParser.SENSITIVE);
    return this;
  }

  public DateTimeFormatterBuilder parseCaseInsensitive() {
    appendInternal(SettingsParser.INSENSITIVE);
    return this;
  }

  public DateTimeFormatterBuilder parseStrict() {
    appendInternal(SettingsParser.STRICT);
    return this;
  }

  public DateTimeFormatterBuilder parseLenient() {
    appendInternal(SettingsParser.LENIENT);
    return this;
  }

  public DateTimeFormatterBuilder parseDefaulting(TemporalField field, long value) {
    Objects.requireNonNull(field, "field");
    appendInternal(new DefaultValueParser(field, value));
    return this;
  }

  public DateTimeFormatterBuilder appendValue(TemporalField field) {
    Objects.requireNonNull(field, "field");
    appendValue(new NumberPrinterParser(field, 1, 19, SignStyle.NORMAL));
    return this;
  }

  public DateTimeFormatterBuilder appendValue(TemporalField field, int width) {
    Objects.requireNonNull(field, "field");
    if (width < 1 || width > 19) {
      throw new IllegalArgumentException("The width must be from 1 to 19 inclusive but was " + width);
    }
    NumberPrinterParser pp = new NumberPrinterParser(field, width, width, SignStyle.NOT_NEGATIVE);
    appendValue(pp);
    return this;
  }

  public DateTimeFormatterBuilder appendValue(TemporalField field, int minWidth, int maxWidth,
      SignStyle signStyle) {
    if (minWidth == maxWidth && signStyle == SignStyle.NOT_NEGATIVE) {
      return appendValue(field, maxWidth);
    }
    Objects.requireNonNull(field, "field");
    Objects.requireNonNull(signStyle, "signStyle");
    if (minWidth < 1 || minWidth > 19) {
      throw new IllegalArgumentException("The minimum width must be from 1 to 19 inclusive but was " + minWidth);
    }
    if (maxWidth < 1 || maxWidth > 19) {
      throw new IllegalArgumentException("The maximum width must be from 1 to 19 inclusive but was " + maxWidth);
    }
    if (maxWidth < minWidth) {
      throw new IllegalArgumentException(
          "The maximum width must exceed or equal the minimum width but " + maxWidth + " < " + minWidth);
    }
    NumberPrinterParser pp = new NumberPrinterParser(field, minWidth, maxWidth, signStyle);
    appendValue(pp);
    return this;
  }

  public DateTimeFormatterBuilder appendValueReduced(TemporalField field, int width, int maxWidth, int baseValue) {
    Objects.requireNonNull(field, "field");
    ReducedPrinterParser pp = new ReducedPrinterParser(field, width, maxWidth, baseValue, null);
    appendValue(pp);
    return this;
  }

  public DateTimeFormatterBuilder appendValueReduced(TemporalField field, int width, int maxWidth,
      ChronoLocalDate baseDate) {
    Objects.requireNonNull(field, "field");
    Objects.requireNonNull(baseDate, "baseDate");
    ReducedPrinterParser pp = new ReducedPrinterParser(field, width, maxWidth, 0, baseDate);
    appendValue(pp);
    return this;
  }

  private DateTimeFormatterBuilder appendValue(NumberPrinterParser pp) {
    if (active.valueParserIndex >= 0) {
      final int activeValueParser = active.valueParserIndex;
      NumberPrinterParser basePP = (NumberPrinterParser) active.printerParsers.get(activeValueParser);
      if (pp.minWidth == pp.maxWidth && pp.signStyle == SignStyle.NOT_NEGATIVE) {
        basePP = basePP.withSubsequentWidth(pp.maxWidth);
        appendInternal(pp.withFixedWidth());
        active.valueParserIndex = activeValueParser;
      } else {
        basePP = basePP.withFixedWidth();
        active.valueParserIndex = appendInternal(pp);
      }
      active.printerParsers.set(activeValueParser, basePP);
    } else {
      active.valueParserIndex = appendInternal(pp);
    }
    return this;
  }

  public DateTimeFormatterBuilder appendFraction(TemporalField field, int minWidth, int maxWidth,
      boolean decimalPoint) {
    if (minWidth == maxWidth && decimalPoint == false) {
      appendValue(new FractionPrinterParser(field, minWidth, maxWidth, decimalPoint));
    } else {
      appendInternal(new FractionPrinterParser(field, minWidth, maxWidth, decimalPoint));
    }
    return this;
  }

  public DateTimeFormatterBuilder appendText(TemporalField field) {
    return appendText(field, TextStyle.FULL);
  }

  public DateTimeFormatterBuilder appendText(TemporalField field, TextStyle textStyle) {
    Objects.requireNonNull(field, "field");
    Objects.requireNonNull(textStyle, "textStyle");
    appendInternal(new TextPrinterParser(field, textStyle, DateTimeTextProvider.getInstance(), null));
    return this;
  }

  public DateTimeFormatterBuilder appendText(TemporalField field, Map<Long, String> textLookup) {
    Objects.requireNonNull(field, "field");
    Objects.requireNonNull(textLookup, "textLookup");
    Map<Long, String> copy = new LinkedHashMap<Long, String>(textLookup);
    appendInternal(new TextPrinterParser(field, TextStyle.FULL, null, copy));
    return this;
  }

  public DateTimeFormatterBuilder appendDayPeriodText(TextStyle style) {
    Objects.requireNonNull(style, "style");
    appendInternal(new DayPeriodPrinterParser(style));
    return this;
  }

  public DateTimeFormatterBuilder appendInstant() {
    appendInternal(new InstantPrinterParser(-2));
    return this;
  }

  public DateTimeFormatterBuilder appendInstant(int fractionalDigits) {
    if (fractionalDigits < -1 || fractionalDigits > 9) {
      throw new IllegalArgumentException("The fractional digits must be from -1 to 9 inclusive but was "
          + fractionalDigits);
    }
    appendInternal(new InstantPrinterParser(fractionalDigits));
    return this;
  }

  public DateTimeFormatterBuilder appendOffsetId() {
    appendInternal(OffsetIdPrinterParser.INSTANCE_ID_Z);
    return this;
  }

  public DateTimeFormatterBuilder appendOffset(String pattern, String noOffsetText) {
    appendInternal(new OffsetIdPrinterParser(pattern, noOffsetText));
    return this;
  }

  public DateTimeFormatterBuilder appendLocalizedOffset(TextStyle style) {
    Objects.requireNonNull(style, "style");
    if (style != TextStyle.FULL && style != TextStyle.SHORT) {
      throw new IllegalArgumentException("Style must be either full or short");
    }
    appendInternal(new LocalizedOffsetIdPrinterParser(style));
    return this;
  }

  public DateTimeFormatterBuilder appendZoneId() {
    appendInternal(new ZoneIdPrinterParser(TemporalQueries.zoneId(), "ZoneId()"));
    return this;
  }

  public DateTimeFormatterBuilder appendZoneRegionId() {
    appendInternal(new ZoneIdPrinterParser(QUERY_REGION_ONLY, "ZoneRegionId()"));
    return this;
  }

  public DateTimeFormatterBuilder appendZoneOrOffsetId() {
    appendInternal(new ZoneIdPrinterParser(TemporalQueries.zone(), "ZoneOrOffsetId()"));
    return this;
  }

  public DateTimeFormatterBuilder appendZoneText(TextStyle textStyle) {
    appendInternal(new ZoneTextPrinterParser(textStyle, false));
    return this;
  }

  public DateTimeFormatterBuilder appendZoneText(TextStyle textStyle, Set<ZoneId> preferredZones) {
    Objects.requireNonNull(preferredZones, "preferredZones");
    appendInternal(new ZoneTextPrinterParser(textStyle, false));
    return this;
  }

  public DateTimeFormatterBuilder appendGenericZoneText(TextStyle textStyle) {
    appendInternal(new ZoneTextPrinterParser(textStyle, true));
    return this;
  }

  public DateTimeFormatterBuilder appendChronologyId() {
    appendInternal(new ChronoPrinterParser(null));
    return this;
  }

  public DateTimeFormatterBuilder appendChronologyText(TextStyle textStyle) {
    Objects.requireNonNull(textStyle, "textStyle");
    appendInternal(new ChronoPrinterParser(textStyle));
    return this;
  }

  public DateTimeFormatterBuilder appendLocalized(FormatStyle dateStyle, FormatStyle timeStyle) {
    if (dateStyle == null && timeStyle == null) {
      throw new IllegalArgumentException("Either the date or time style must be non-null");
    }
    appendInternal(new LocalizedPrinterParser(dateStyle, timeStyle));
    return this;
  }

  public DateTimeFormatterBuilder appendLiteral(char literal) {
    appendInternal(new CharLiteralPrinterParser(literal));
    return this;
  }

  public DateTimeFormatterBuilder appendLiteral(String literal) {
    Objects.requireNonNull(literal, "literal");
    if (!literal.isEmpty()) {
      if (literal.length() == 1) {
        appendInternal(new CharLiteralPrinterParser(literal.charAt(0)));
      } else {
        appendInternal(new StringLiteralPrinterParser(literal));
      }
    }
    return this;
  }

  public DateTimeFormatterBuilder append(DateTimeFormatter formatter) {
    Objects.requireNonNull(formatter, "formatter");
    appendInternal(formatter.toPrinterParser(false));
    return this;
  }

  public DateTimeFormatterBuilder appendOptional(DateTimeFormatter formatter) {
    Objects.requireNonNull(formatter, "formatter");
    appendInternal(formatter.toPrinterParser(true));
    return this;
  }

  public DateTimeFormatterBuilder appendPattern(String pattern) {
    Objects.requireNonNull(pattern, "pattern");
    parsePattern(pattern);
    return this;
  }

  private void parsePattern(String pattern) {
    for (int pos = 0; pos < pattern.length(); pos++) {
      char cur = pattern.charAt(pos);
      if ((cur >= 'A' && cur <= 'Z') || (cur >= 'a' && cur <= 'z')) {
        int start = pos++;
        for (; pos < pattern.length() && pattern.charAt(pos) == cur; pos++) {
        }
        int count = pos - start;
        if (cur == 'p') {
          int pad = 0;
          if (pos < pattern.length()) {
            cur = pattern.charAt(pos);
            if ((cur >= 'A' && cur <= 'Z') || (cur >= 'a' && cur <= 'z')) {
              pad = count;
              start = pos++;
              for (; pos < pattern.length() && pattern.charAt(pos) == cur; pos++) {
              }
              count = pos - start;
            }
          }
          if (pad == 0) {
            throw new IllegalArgumentException("Pad letter 'p' must be followed by valid pad pattern: " + pattern);
          }
          padNext(pad);
        }
        TemporalField field = fieldFor(cur);
        if (field != null) {
          parseField(cur, count, field);
        } else if (cur == 'z') {
          if (count > 4) {
            throw new IllegalArgumentException("Too many pattern letters: " + cur);
          } else if (count == 4) {
            appendZoneText(TextStyle.FULL);
          } else {
            appendZoneText(TextStyle.SHORT);
          }
        } else if (cur == 'V') {
          if (count != 2) {
            throw new IllegalArgumentException("Pattern letter count must be 2: " + cur);
          }
          appendZoneId();
        } else if (cur == 'v') {
          if (count == 1) {
            appendGenericZoneText(TextStyle.SHORT);
          } else if (count == 4) {
            appendGenericZoneText(TextStyle.FULL);
          } else {
            throw new IllegalArgumentException("Wrong number of pattern letters: " + cur);
          }
        } else if (cur == 'Z') {
          if (count < 4) {
            appendOffset("+HHMM", "+0000");
          } else if (count == 4) {
            appendLocalizedOffset(TextStyle.FULL);
          } else if (count == 5) {
            appendOffset("+HH:MM:ss", "Z");
          } else {
            throw new IllegalArgumentException("Too many pattern letters: " + cur);
          }
        } else if (cur == 'O') {
          if (count == 1) {
            appendLocalizedOffset(TextStyle.SHORT);
          } else if (count == 4) {
            appendLocalizedOffset(TextStyle.FULL);
          } else {
            throw new IllegalArgumentException("Pattern letter count must be 1 or 4: " + cur);
          }
        } else if (cur == 'X') {
          if (count > 5) {
            throw new IllegalArgumentException("Too many pattern letters: " + cur);
          }
          appendOffset(OffsetIdPrinterParser.PATTERNS[count + (count == 1 ? 0 : 1)], "Z");
        } else if (cur == 'x') {
          if (count > 5) {
            throw new IllegalArgumentException("Too many pattern letters: " + cur);
          }
          String zero = (count == 1 ? "+00" : (count % 2 == 0 ? "+0000" : "+00:00"));
          appendOffset(OffsetIdPrinterParser.PATTERNS[count + (count == 1 ? 0 : 1)], zero);
        } else if (cur == 'B') {
          if (count == 1 || count == 2 || count == 3) {
            appendDayPeriodText(TextStyle.SHORT);
          } else if (count == 4) {
            appendDayPeriodText(TextStyle.FULL);
          } else if (count == 5) {
            appendDayPeriodText(TextStyle.NARROW);
          } else {
            throw new IllegalArgumentException("Too many pattern letters: " + cur);
          }
        } else if (cur == 'W') {
          if (count > 1) {
            throw new IllegalArgumentException("Too many pattern letters: " + cur);
          }
          appendValue(new WeekBasedFieldPrinterParser(cur, count, count, count));
        } else if (cur == 'w') {
          if (count > 2) {
            throw new IllegalArgumentException("Too many pattern letters: " + cur);
          }
          appendValue(new WeekBasedFieldPrinterParser(cur, count, count, 2));
        } else if (cur == 'Y') {
          if (count == 2) {
            appendValue(new WeekBasedFieldPrinterParser(cur, count, count, 2));
          } else {
            appendValue(new WeekBasedFieldPrinterParser(cur, count, count, 19));
          }
        } else {
          throw new IllegalArgumentException("Unknown pattern letter: " + cur);
        }
        pos--;
      } else if (cur == '\'') {
        int start = pos++;
        for (; pos < pattern.length(); pos++) {
          if (pattern.charAt(pos) == '\'') {
            if (pos + 1 < pattern.length() && pattern.charAt(pos + 1) == '\'') {
              pos++;
            } else {
              break;
            }
          }
        }
        if (pos >= pattern.length()) {
          throw new IllegalArgumentException("Pattern ends with an incomplete string literal: " + pattern);
        }
        String str = pattern.substring(start + 1, pos);
        if (str.isEmpty()) {
          appendLiteral('\'');
        } else {
          appendLiteral(str.replace("''", "'"));
        }
      } else if (cur == '[') {
        optionalStart();
      } else if (cur == ']') {
        if (active.parent == null) {
          throw new IllegalArgumentException("Pattern invalid as it contains ] without previous [");
        }
        optionalEnd();
      } else if (cur == '{' || cur == '}' || cur == '#') {
        throw new IllegalArgumentException("Pattern includes reserved character: '" + cur + "'");
      } else {
        appendLiteral(cur);
      }
    }
  }

  private static TemporalField fieldFor(char c) {
    switch (c) {
      case 'G':
        return ChronoField.ERA;
      case 'y':
        return ChronoField.YEAR_OF_ERA;
      case 'u':
        return ChronoField.YEAR;
      case 'Q':
      case 'q':
        return IsoFields.QUARTER_OF_YEAR;
      case 'M':
      case 'L':
        return ChronoField.MONTH_OF_YEAR;
      case 'D':
        return ChronoField.DAY_OF_YEAR;
      case 'd':
        return ChronoField.DAY_OF_MONTH;
      case 'F':
        return ChronoField.ALIGNED_WEEK_OF_MONTH;
      case 'E':
      case 'c':
      case 'e':
        return ChronoField.DAY_OF_WEEK;
      case 'a':
        return ChronoField.AMPM_OF_DAY;
      case 'H':
        return ChronoField.HOUR_OF_DAY;
      case 'k':
        return ChronoField.CLOCK_HOUR_OF_DAY;
      case 'K':
        return ChronoField.HOUR_OF_AMPM;
      case 'h':
        return ChronoField.CLOCK_HOUR_OF_AMPM;
      case 'm':
        return ChronoField.MINUTE_OF_HOUR;
      case 's':
        return ChronoField.SECOND_OF_MINUTE;
      case 'S':
        return ChronoField.NANO_OF_SECOND;
      case 'A':
        return ChronoField.MILLI_OF_DAY;
      case 'n':
        return ChronoField.NANO_OF_SECOND;
      case 'N':
        return ChronoField.NANO_OF_DAY;
      case 'g':
        return java.time.temporal.JulianFields.MODIFIED_JULIAN_DAY;
      default:
        return null;
    }
  }

  private void parseField(char cur, int count, TemporalField field) {
    boolean standalone = false;
    switch (cur) {
      case 'u':
      case 'y':
        if (count == 2) {
          appendValueReduced(field, 2, 2, ReducedPrinterParser.BASE_DATE);
        } else if (count < 4) {
          appendValue(field, count, 19, SignStyle.NORMAL);
        } else {
          appendValue(field, count, 19, SignStyle.EXCEEDS_PAD);
        }
        break;
      case 'c':
        if (count == 1) {
          appendValue(new WeekBasedFieldPrinterParser(cur, count, count, count));
          break;
        } else if (count == 2) {
          throw new IllegalArgumentException("Invalid pattern \"cc\"");
        }
        standalone = true;
        textOrNumber(cur, count, field, standalone);
        break;
      case 'L':
      case 'q':
        standalone = true;
        textOrNumber(cur, count, field, standalone);
        break;
      case 'e':
        switch (count) {
          case 1:
          case 2:
            appendValue(new WeekBasedFieldPrinterParser(cur, count, count, count));
            break;
          case 3:
            appendText(field, TextStyle.SHORT);
            break;
          case 4:
            appendText(field, TextStyle.FULL);
            break;
          case 5:
            appendText(field, TextStyle.NARROW);
            break;
          default:
            throw new IllegalArgumentException("Too many pattern letters: " + cur);
        }
        break;
      case 'M':
      case 'Q':
        textOrNumber(cur, count, field, false);
        break;
      case 'E':
        switch (count) {
          case 1:
          case 2:
          case 3:
            appendText(field, TextStyle.SHORT);
            break;
          case 4:
            appendText(field, TextStyle.FULL);
            break;
          case 5:
            appendText(field, TextStyle.NARROW);
            break;
          default:
            throw new IllegalArgumentException("Too many pattern letters: " + cur);
        }
        break;
      case 'a':
        if (count == 1) {
          appendText(field, TextStyle.SHORT);
        } else {
          throw new IllegalArgumentException("Too many pattern letters: " + cur);
        }
        break;
      case 'G':
        switch (count) {
          case 1:
          case 2:
          case 3:
            appendText(field, TextStyle.SHORT);
            break;
          case 4:
            appendText(field, TextStyle.FULL);
            break;
          case 5:
            appendText(field, TextStyle.NARROW);
            break;
          default:
            throw new IllegalArgumentException("Too many pattern letters: " + cur);
        }
        break;
      case 'S':
        appendFraction(NANO_OF_SECOND, count, count, false);
        break;
      case 'F':
        if (count == 1) {
          appendValue(field);
        } else {
          throw new IllegalArgumentException("Too many pattern letters: " + cur);
        }
        break;
      case 'd':
      case 'h':
      case 'H':
      case 'k':
      case 'K':
      case 'm':
      case 's':
        if (count == 1) {
          appendValue(field);
        } else if (count == 2) {
          appendValue(field, count);
        } else {
          throw new IllegalArgumentException("Too many pattern letters: " + cur);
        }
        break;
      case 'D':
        if (count == 1) {
          appendValue(field);
        } else if (count == 2 || count == 3) {
          appendValue(field, count, 3, SignStyle.NOT_NEGATIVE);
        } else {
          throw new IllegalArgumentException("Too many pattern letters: " + cur);
        }
        break;
      default:
        if (count == 1) {
          appendValue(field);
        } else {
          appendValue(field, count, 19, SignStyle.NOT_NEGATIVE);
        }
        break;
    }
  }

  private void textOrNumber(char cur, int count, TemporalField field, boolean standalone) {
    switch (count) {
      case 1:
        appendValue(field);
        break;
      case 2:
        appendValue(field, 2);
        break;
      case 3:
        appendText(field, standalone ? TextStyle.SHORT_STANDALONE : TextStyle.SHORT);
        break;
      case 4:
        appendText(field, standalone ? TextStyle.FULL_STANDALONE : TextStyle.FULL);
        break;
      case 5:
        appendText(field, standalone ? TextStyle.NARROW_STANDALONE : TextStyle.NARROW);
        break;
      default:
        throw new IllegalArgumentException("Too many pattern letters: " + cur);
    }
  }

  public DateTimeFormatterBuilder padNext(int padWidth) {
    return padNext(padWidth, ' ');
  }

  public DateTimeFormatterBuilder padNext(int padWidth, char padChar) {
    if (padWidth < 1) {
      throw new IllegalArgumentException("The pad width must be at least one but was " + padWidth);
    }
    active.padNextWidth = padWidth;
    active.padNextChar = padChar;
    active.valueParserIndex = -1;
    return this;
  }

  public DateTimeFormatterBuilder optionalStart() {
    active.valueParserIndex = -1;
    active = new DateTimeFormatterBuilder(active, true);
    return this;
  }

  public DateTimeFormatterBuilder optionalEnd() {
    if (active.parent == null) {
      throw new IllegalStateException("Cannot call optionalEnd() as there was no previous call to optionalStart()");
    }
    if (active.printerParsers.size() > 0) {
      CompositePrinterParser cpp = new CompositePrinterParser(active.printerParsers, active.optional);
      active = active.parent;
      appendInternal(cpp);
    } else {
      active = active.parent;
    }
    return this;
  }

  private int appendInternal(DateTimePrinterParser pp) {
    Objects.requireNonNull(pp, "pp");
    if (active.padNextWidth > 0) {
      if (pp != null) {
        pp = new PadPrinterParserDecorator(pp, active.padNextWidth, active.padNextChar);
      }
      active.padNextWidth = 0;
      active.padNextChar = 0;
    }
    active.printerParsers.add(pp);
    active.valueParserIndex = -1;
    return active.printerParsers.size() - 1;
  }

  public DateTimeFormatter toFormatter() {
    return toFormatter(Locale.getDefault());
  }

  public DateTimeFormatter toFormatter(Locale locale) {
    return toFormatter(locale, ResolverStyle.SMART, null);
  }

  DateTimeFormatter toFormatter(ResolverStyle resolverStyle, Chronology chrono) {
    return toFormatter(Locale.getDefault(), resolverStyle, chrono);
  }

  private DateTimeFormatter toFormatter(Locale locale, ResolverStyle resolverStyle, Chronology chrono) {
    Objects.requireNonNull(locale, "locale");
    while (active.parent != null) {
      optionalEnd();
    }
    CompositePrinterParser pp = new CompositePrinterParser(printerParsers, false);
    return new DateTimeFormatter(pp, locale, DecimalStyle.STANDARD, resolverStyle, null, chrono, null);
  }

  static final class LocalizedPrinterParser implements DateTimePrinterParser {
    private final FormatStyle dateStyle;
    private final FormatStyle timeStyle;

    LocalizedPrinterParser(FormatStyle dateStyle, FormatStyle timeStyle) {
      this.dateStyle = dateStyle;
      this.timeStyle = timeStyle;
    }

    private DateTimeFormatter formatter(Locale locale, Chronology chrono) {
      String pattern = getLocalizedDateTimePattern(dateStyle, timeStyle, chrono, locale);
      return new DateTimeFormatterBuilder().appendPattern(pattern).toFormatter(locale);
    }

    @Override
    public boolean format(DateTimePrintContext context, StringBuilder buf) {
      Chronology chrono = Chronology.from(context.getTemporal());
      return formatter(context.getLocale(), chrono).toPrinterParser(false).format(context, buf);
    }

    @Override
    public int parse(DateTimeParseContext context, CharSequence text, int position) {
      Chronology chrono = context.getEffectiveChronology();
      return formatter(context.getLocale(), chrono).toPrinterParser(false).parse(context, text, position);
    }

    @Override
    public String toString() {
      return "Localized(" + (dateStyle != null ? dateStyle : "") + "," + (timeStyle != null ? timeStyle : "") + ")";
    }
  }

  interface DateTimePrinterParser {
    boolean format(DateTimePrintContext context, StringBuilder buf);

    int parse(DateTimeParseContext context, CharSequence text, int position);
  }

  static final class CompositePrinterParser implements DateTimePrinterParser {
    private final DateTimePrinterParser[] printerParsers;
    private final boolean optional;

    CompositePrinterParser(List<DateTimePrinterParser> printerParsers, boolean optional) {
      this(printerParsers.toArray(new DateTimePrinterParser[printerParsers.size()]), optional);
    }

    CompositePrinterParser(DateTimePrinterParser[] printerParsers, boolean optional) {
      this.printerParsers = printerParsers;
      this.optional = optional;
    }

    public CompositePrinterParser withOptional(boolean optional) {
      if (optional == this.optional) {
        return this;
      }
      return new CompositePrinterParser(printerParsers, optional);
    }

    @Override
    public boolean format(DateTimePrintContext context, StringBuilder buf) {
      int length = buf.length();
      if (optional) {
        context.startOptional();
      }
      try {
        for (DateTimePrinterParser pp : printerParsers) {
          if (pp.format(context, buf) == false) {
            buf.setLength(length);
            return true;
          }
        }
      } finally {
        if (optional) {
          context.endOptional();
        }
      }
      return true;
    }

    @Override
    public int parse(DateTimeParseContext context, CharSequence text, int position) {
      if (optional) {
        context.startOptional();
        int pos = position;
        for (DateTimePrinterParser pp : printerParsers) {
          pos = pp.parse(context, text, pos);
          if (pos < 0) {
            context.endOptional(false);
            return position;
          }
        }
        context.endOptional(true);
        return pos;
      } else {
        for (DateTimePrinterParser pp : printerParsers) {
          position = pp.parse(context, text, position);
          if (position < 0) {
            break;
          }
        }
        return position;
      }
    }

    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder();
      if (printerParsers != null) {
        buf.append(optional ? "[" : "(");
        for (DateTimePrinterParser pp : printerParsers) {
          buf.append(pp);
        }
        buf.append(optional ? "]" : ")");
      }
      return buf.toString();
    }
  }

  static final class PadPrinterParserDecorator implements DateTimePrinterParser {
    private final DateTimePrinterParser printerParser;
    private final int padWidth;
    private final char padChar;

    PadPrinterParserDecorator(DateTimePrinterParser printerParser, int padWidth, char padChar) {
      this.printerParser = printerParser;
      this.padWidth = padWidth;
      this.padChar = padChar;
    }

    @Override
    public boolean format(DateTimePrintContext context, StringBuilder buf) {
      int preLen = buf.length();
      if (printerParser.format(context, buf) == false) {
        return false;
      }
      int len = buf.length() - preLen;
      if (len > padWidth) {
        throw new DateTimeException("Cannot print as output of " + len + " characters exceeds pad width of "
            + padWidth);
      }
      for (int i = 0; i < padWidth - len; i++) {
        buf.insert(preLen, padChar);
      }
      return true;
    }

    @Override
    public int parse(DateTimeParseContext context, CharSequence text, int position) {
      final boolean strict = context.isStrict();
      if (position > text.length()) {
        throw new IndexOutOfBoundsException();
      }
      if (position == text.length()) {
        return ~position;
      }
      int endPos = position + padWidth;
      if (endPos > text.length()) {
        if (strict) {
          return ~position;
        }
        endPos = text.length();
      }
      int pos = position;
      while (pos < endPos && context.charEquals(text.charAt(pos), padChar)) {
        pos++;
      }
      text = text.subSequence(0, endPos);
      int resultPos = printerParser.parse(context, text, pos);
      if (resultPos != endPos && strict) {
        return ~(position + pos);
      }
      return resultPos;
    }

    @Override
    public String toString() {
      return "Pad(" + printerParser + "," + padWidth + (padChar == ' ' ? ")" : ",'" + padChar + "')");
    }
  }

  enum SettingsParser implements DateTimePrinterParser {
    SENSITIVE,
    INSENSITIVE,
    STRICT,
    LENIENT;

    @Override
    public boolean format(DateTimePrintContext context, StringBuilder buf) {
      return true;
    }

    @Override
    public int parse(DateTimeParseContext context, CharSequence text, int position) {
      switch (ordinal()) {
        case 0:
          context.setCaseSensitive(true);
          break;
        case 1:
          context.setCaseSensitive(false);
          break;
        case 2:
          context.setStrict(true);
          break;
        case 3:
          context.setStrict(false);
          break;
        default:
          break;
      }
      return position;
    }

    @Override
    public String toString() {
      switch (ordinal()) {
        case 0:
          return "ParseCaseSensitive(true)";
        case 1:
          return "ParseCaseSensitive(false)";
        case 2:
          return "ParseStrict(true)";
        case 3:
          return "ParseStrict(false)";
        default:
          throw new IllegalStateException("Unreachable");
      }
    }
  }

  static class DefaultValueParser implements DateTimePrinterParser {
    private final TemporalField field;
    private final long value;

    DefaultValueParser(TemporalField field, long value) {
      this.field = field;
      this.value = value;
    }

    @Override
    public boolean format(DateTimePrintContext context, StringBuilder buf) {
      return true;
    }

    @Override
    public int parse(DateTimeParseContext context, CharSequence text, int position) {
      if (context.getParsed(field) == null) {
        context.setParsedField(field, value, position, position);
      }
      return position;
    }
  }

  static final class CharLiteralPrinterParser implements DateTimePrinterParser {
    private final char literal;

    CharLiteralPrinterParser(char literal) {
      this.literal = literal;
    }

    @Override
    public boolean format(DateTimePrintContext context, StringBuilder buf) {
      buf.append(literal);
      return true;
    }

    @Override
    public int parse(DateTimeParseContext context, CharSequence text, int position) {
      int length = text.length();
      if (position == length) {
        return ~position;
      }
      char ch = text.charAt(position);
      if (ch != literal) {
        if (context.isCaseSensitive() || (Character.toUpperCase(ch) != Character.toUpperCase(literal)
            && Character.toLowerCase(ch) != Character.toLowerCase(literal))) {
          return ~position;
        }
      }
      return position + 1;
    }

    @Override
    public String toString() {
      if (literal == '\'') {
        return "''";
      }
      return "'" + literal + "'";
    }
  }

  static final class StringLiteralPrinterParser implements DateTimePrinterParser {
    private final String literal;

    StringLiteralPrinterParser(String literal) {
      this.literal = literal;
    }

    @Override
    public boolean format(DateTimePrintContext context, StringBuilder buf) {
      buf.append(literal);
      return true;
    }

    @Override
    public int parse(DateTimeParseContext context, CharSequence text, int position) {
      int length = text.length();
      if (position > length || position < 0) {
        throw new IndexOutOfBoundsException();
      }
      if (context.subSequenceEquals(text, position, literal, 0, literal.length()) == false) {
        return ~position;
      }
      return position + literal.length();
    }

    @Override
    public String toString() {
      String converted = literal.replace("'", "''");
      return "'" + converted + "'";
    }
  }

  static class NumberPrinterParser implements DateTimePrinterParser {
    static final long[] EXCEED_POINTS = new long[] {0L, 10L, 100L, 1000L, 10000L, 100000L, 1000000L, 10000000L,
        100000000L, 1000000000L, 10000000000L, 100000000000L, 1000000000000L, 10000000000000L, 100000000000000L,
        1000000000000000L, 10000000000000000L, 100000000000000000L, 1000000000000000000L};

    final TemporalField field;
    final int minWidth;
    final int maxWidth;
    final SignStyle signStyle;
    final int subsequentWidth;

    NumberPrinterParser(TemporalField field, int minWidth, int maxWidth, SignStyle signStyle) {
      this.field = field;
      this.minWidth = minWidth;
      this.maxWidth = maxWidth;
      this.signStyle = signStyle;
      this.subsequentWidth = 0;
    }

    protected NumberPrinterParser(TemporalField field, int minWidth, int maxWidth, SignStyle signStyle,
        int subsequentWidth) {
      this.field = field;
      this.minWidth = minWidth;
      this.maxWidth = maxWidth;
      this.signStyle = signStyle;
      this.subsequentWidth = subsequentWidth;
    }

    NumberPrinterParser withFixedWidth() {
      if (subsequentWidth == -1) {
        return this;
      }
      return new NumberPrinterParser(field, minWidth, maxWidth, signStyle, -1);
    }

    NumberPrinterParser withSubsequentWidth(int subsequentWidth) {
      return new NumberPrinterParser(field, minWidth, maxWidth, signStyle, this.subsequentWidth + subsequentWidth);
    }

    @Override
    public boolean format(DateTimePrintContext context, StringBuilder buf) {
      Long valueLong = context.getValue(field);
      if (valueLong == null) {
        return false;
      }
      long value = getValue(context, valueLong);
      DecimalStyle decimalStyle = context.getDecimalStyle();
      String str = (value == Long.MIN_VALUE ? "9223372036854775808" : Long.toString(Math.abs(value)));
      if (str.length() > maxWidth) {
        throw new DateTimeException("Field " + field + " cannot be printed as the value " + value
            + " exceeds the maximum print width of " + maxWidth);
      }
      str = decimalStyle.convertNumberToI18N(str);
      if (value >= 0) {
        switch (signStyle) {
          case EXCEEDS_PAD:
            if (minWidth < 19 && value >= EXCEED_POINTS[minWidth]) {
              buf.append(decimalStyle.getPositiveSign());
            }
            break;
          case ALWAYS:
            buf.append(decimalStyle.getPositiveSign());
            break;
          default:
            break;
        }
      } else {
        switch (signStyle) {
          case NORMAL:
          case EXCEEDS_PAD:
          case ALWAYS:
            buf.append(decimalStyle.getNegativeSign());
            break;
          case NOT_NEGATIVE:
            throw new DateTimeException("Field " + field + " cannot be printed as the value " + value
                + " cannot be negative according to the SignStyle");
          default:
            break;
        }
      }
      for (int i = 0; i < minWidth - str.length(); i++) {
        buf.append(decimalStyle.getZeroDigit());
      }
      buf.append(str);
      return true;
    }

    long getValue(DateTimePrintContext context, long value) {
      return value;
    }

    boolean isFixedWidth(DateTimeParseContext context) {
      return subsequentWidth == -1 || (subsequentWidth > 0 && minWidth == maxWidth
          && signStyle == SignStyle.NOT_NEGATIVE);
    }

    @Override
    public int parse(DateTimeParseContext context, CharSequence text, int position) {
      int length = text.length();
      if (position == length) {
        return ~position;
      }
      char sign = text.charAt(position);
      boolean negative = false;
      boolean positive = false;
      if (sign == context.getDecimalStyle().getPositiveSign()) {
        if (signStyle.parse(true, context.isStrict(), minWidth == maxWidth) == false) {
          return ~position;
        }
        positive = true;
        position++;
      } else if (sign == context.getDecimalStyle().getNegativeSign()) {
        if (signStyle.parse(false, context.isStrict(), minWidth == maxWidth) == false) {
          return ~position;
        }
        negative = true;
        position++;
      } else if (signStyle == SignStyle.ALWAYS && context.isStrict()) {
        return ~position;
      }
      int effMinWidth = (context.isStrict() || isFixedWidth(context) ? minWidth : 1);
      int minEndPos = position + effMinWidth;
      if (minEndPos > length) {
        return ~position;
      }
      int effMaxWidth = (context.isStrict() || isFixedWidth(context) ? maxWidth : 9) + Math.max(subsequentWidth, 0);
      long total = 0;
      BigInteger totalBig = null;
      int pos = position;
      for (int pass = 0; pass < 2; pass++) {
        int maxEndPos = Math.min(pos + effMaxWidth, length);
        while (pos < maxEndPos) {
          char ch = text.charAt(pos++);
          int digit = context.getDecimalStyle().convertToDigit(ch);
          if (digit < 0) {
            pos--;
            if (pos < minEndPos) {
              return ~position;
            }
            break;
          }
          if ((pos - position) > 18) {
            if (totalBig == null) {
              totalBig = BigInteger.valueOf(total);
            }
            totalBig = totalBig.multiply(BigInteger.TEN).add(BigInteger.valueOf(digit));
          } else {
            total = total * 10 + digit;
          }
        }
        if (subsequentWidth > 0 && pass == 0) {
          int parseLen = pos - position;
          effMaxWidth = Math.max(effMinWidth, parseLen - subsequentWidth);
          pos = position;
          total = 0;
          totalBig = null;
        } else {
          break;
        }
      }
      if (negative) {
        if (totalBig != null) {
          if (totalBig.equals(BigInteger.ZERO) && context.isStrict()) {
            return ~(position - 1);
          }
          totalBig = totalBig.negate();
        } else {
          if (total == 0 && context.isStrict()) {
            return ~(position - 1);
          }
          total = -total;
        }
      } else if (signStyle == SignStyle.EXCEEDS_PAD && context.isStrict()) {
        int parseLen = pos - position;
        if (positive) {
          if (parseLen <= minWidth) {
            return ~(position - 1);
          }
        } else {
          if (parseLen > minWidth) {
            return ~position;
          }
        }
      }
      if (totalBig != null) {
        if (totalBig.bitLength() > 63) {
          totalBig = totalBig.divide(BigInteger.TEN);
          pos--;
        }
        return setValue(context, totalBig.longValue(), position, pos);
      }
      return setValue(context, total, position, pos);
    }

    int setValue(DateTimeParseContext context, long value, int errorPos, int successPos) {
      return context.setParsedField(field, value, errorPos, successPos);
    }

    @Override
    public String toString() {
      if (minWidth == 1 && maxWidth == 19 && signStyle == SignStyle.NORMAL) {
        return "Value(" + field + ")";
      }
      if (minWidth == maxWidth && signStyle == SignStyle.NOT_NEGATIVE) {
        return "Value(" + field + "," + minWidth + ")";
      }
      return "Value(" + field + "," + minWidth + "," + maxWidth + "," + signStyle + ")";
    }
  }

  static final class ReducedPrinterParser extends NumberPrinterParser {
    static final LocalDate BASE_DATE = LocalDate.of(2000, 1, 1);
    private final int baseValue;
    private final ChronoLocalDate baseDate;

    ReducedPrinterParser(TemporalField field, int minWidth, int maxWidth, int baseValue,
        ChronoLocalDate baseDate) {
      this(field, minWidth, maxWidth, baseValue, baseDate, 0);
      if (minWidth < 1 || minWidth > 10) {
        throw new IllegalArgumentException("The minWidth must be from 1 to 10 inclusive but was " + minWidth);
      }
      if (maxWidth < 1 || maxWidth > 10) {
        throw new IllegalArgumentException("The maxWidth must be from 1 to 10 inclusive but was " + minWidth);
      }
      if (maxWidth < minWidth) {
        throw new IllegalArgumentException("Maximum width must exceed or equal the minimum width but " + maxWidth
            + " < " + minWidth);
      }
      if (baseDate == null) {
        if (field.range().isValidValue(baseValue) == false) {
          throw new IllegalArgumentException("The base value must be within the range of the field");
        }
        if ((((long) baseValue) + EXCEED_POINTS[maxWidth]) > Integer.MAX_VALUE) {
          throw new DateTimeException("Unable to add printer-parser as the range exceeds the capacity of an int");
        }
      }
    }

    private ReducedPrinterParser(TemporalField field, int minWidth, int maxWidth, int baseValue,
        ChronoLocalDate baseDate, int subsequentWidth) {
      super(field, minWidth, maxWidth, SignStyle.NOT_NEGATIVE, subsequentWidth);
      this.baseValue = baseValue;
      this.baseDate = baseDate;
    }

    @Override
    long getValue(DateTimePrintContext context, long value) {
      long absValue = Math.abs(value);
      int baseValue = this.baseValue;
      if (baseDate != null) {
        Chronology chrono = Chronology.from(context.getTemporal());
        baseValue = chrono.date(baseDate).get(field);
      }
      if (value >= baseValue && value < baseValue + EXCEED_POINTS[minWidth]) {
        return absValue % EXCEED_POINTS[minWidth];
      }
      return absValue % EXCEED_POINTS[maxWidth];
    }

    @Override
    int setValue(DateTimeParseContext context, long value, int errorPos, int successPos) {
      int baseValue = this.baseValue;
      if (baseDate != null) {
        Chronology chrono = context.getEffectiveChronology();
        baseValue = chrono.date(baseDate).get(field);
      }
      int parseLen = successPos - errorPos;
      if (parseLen == minWidth && value >= 0) {
        long range = EXCEED_POINTS[minWidth];
        long lastPart = baseValue % range;
        long basePart = baseValue - lastPart;
        if (baseValue > 0) {
          value = basePart + value;
        } else {
          value = basePart - value;
        }
        if (value < baseValue) {
          value += range;
        }
      }
      return context.setParsedField(field, value, errorPos, successPos);
    }

    @Override
    ReducedPrinterParser withFixedWidth() {
      if (subsequentWidth == -1) {
        return this;
      }
      return new ReducedPrinterParser(field, minWidth, maxWidth, baseValue, baseDate, -1);
    }

    @Override
    ReducedPrinterParser withSubsequentWidth(int subsequentWidth) {
      return new ReducedPrinterParser(field, minWidth, maxWidth, baseValue, baseDate,
          this.subsequentWidth + subsequentWidth);
    }

    @Override
    boolean isFixedWidth(DateTimeParseContext context) {
      if (context.isStrict() == false) {
        return false;
      }
      return super.isFixedWidth(context);
    }

    @Override
    public String toString() {
      return "ReducedValue(" + field + "," + minWidth + "," + maxWidth + ","
          + (baseDate != null ? baseDate : baseValue) + ")";
    }
  }

  static final class FractionPrinterParser extends NumberPrinterParser {
    private final boolean decimalPoint;

    FractionPrinterParser(TemporalField field, int minWidth, int maxWidth, boolean decimalPoint) {
      this(field, minWidth, maxWidth, decimalPoint, 0);
      Objects.requireNonNull(field, "field");
      if (field.range().isFixed() == false) {
        throw new IllegalArgumentException("Field must have a fixed set of values: " + field);
      }
      if (minWidth < 0 || minWidth > 9) {
        throw new IllegalArgumentException("Minimum width must be from 0 to 9 inclusive but was " + minWidth);
      }
      if (maxWidth < 1 || maxWidth > 9) {
        throw new IllegalArgumentException("Maximum width must be from 1 to 9 inclusive but was " + maxWidth);
      }
      if (maxWidth < minWidth) {
        throw new IllegalArgumentException("Maximum width must exceed or equal the minimum width but " + maxWidth
            + " < " + minWidth);
      }
    }

    private FractionPrinterParser(TemporalField field, int minWidth, int maxWidth, boolean decimalPoint,
        int subsequentWidth) {
      super(field, minWidth, maxWidth, SignStyle.NOT_NEGATIVE, subsequentWidth);
      this.decimalPoint = decimalPoint;
    }

    @Override
    FractionPrinterParser withFixedWidth() {
      if (subsequentWidth == -1) {
        return this;
      }
      return new FractionPrinterParser(field, minWidth, maxWidth, decimalPoint, -1);
    }

    @Override
    FractionPrinterParser withSubsequentWidth(int subsequentWidth) {
      return new FractionPrinterParser(field, minWidth, maxWidth, decimalPoint,
          this.subsequentWidth + subsequentWidth);
    }

    @Override
    boolean isFixedWidth(DateTimeParseContext context) {
      if (context.isStrict() && minWidth == maxWidth && decimalPoint == false) {
        return true;
      }
      return false;
    }

    @Override
    public boolean format(DateTimePrintContext context, StringBuilder buf) {
      Long value = context.getValue(field);
      if (value == null) {
        return false;
      }
      DecimalStyle decimalStyle = context.getDecimalStyle();
      BigDecimal fraction = convertToFraction(value);
      if (fraction.scale() == 0) {
        if (minWidth > 0) {
          if (decimalPoint) {
            buf.append(decimalStyle.getDecimalSeparator());
          }
          for (int i = 0; i < minWidth; i++) {
            buf.append(decimalStyle.getZeroDigit());
          }
        }
      } else {
        int outputScale = Math.min(Math.max(fraction.scale(), minWidth), maxWidth);
        fraction = fraction.setScale(outputScale, RoundingMode.FLOOR);
        String str = fraction.toPlainString().substring(2);
        str = decimalStyle.convertNumberToI18N(str);
        if (decimalPoint) {
          buf.append(decimalStyle.getDecimalSeparator());
        }
        buf.append(str);
      }
      return true;
    }

    @Override
    public int parse(DateTimeParseContext context, CharSequence text, int position) {
      int effectiveMin = (context.isStrict() || isFixedWidth(context) ? minWidth : 0);
      int effectiveMax = (context.isStrict() || isFixedWidth(context) ? maxWidth : 9);
      int length = text.length();
      if (position == length) {
        return (effectiveMin > 0 ? ~position : position);
      }
      if (decimalPoint) {
        if (text.charAt(position) != context.getDecimalStyle().getDecimalSeparator()) {
          return (effectiveMin > 0 ? ~position : position);
        }
        position++;
      }
      int minEndPos = position + effectiveMin;
      if (minEndPos > length) {
        return ~position;
      }
      int maxEndPos = Math.min(position + effectiveMax, length);
      int total = 0;
      int pos = position;
      while (pos < maxEndPos) {
        char ch = text.charAt(pos++);
        int digit = context.getDecimalStyle().convertToDigit(ch);
        if (digit < 0) {
          if (pos < minEndPos) {
            return ~position;
          }
          pos--;
          break;
        }
        total = total * 10 + digit;
      }
      BigDecimal fraction = new BigDecimal(total).movePointLeft(pos - position);
      long value = convertFromFraction(fraction);
      return context.setParsedField(field, value, position, pos);
    }

    private BigDecimal convertToFraction(long value) {
      ValueRange range = field.range();
      range.checkValidValue(value, field);
      BigDecimal minBD = BigDecimal.valueOf(range.getMinimum());
      BigDecimal rangeBD = BigDecimal.valueOf(range.getMaximum()).subtract(minBD).add(BigDecimal.ONE);
      BigDecimal valueBD = BigDecimal.valueOf(value).subtract(minBD);
      BigDecimal fraction = valueBD.divide(rangeBD, 9, RoundingMode.FLOOR);
      return fraction.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : fraction.stripTrailingZeros();
    }

    private long convertFromFraction(BigDecimal fraction) {
      ValueRange range = field.range();
      BigDecimal minBD = BigDecimal.valueOf(range.getMinimum());
      BigDecimal rangeBD = BigDecimal.valueOf(range.getMaximum()).subtract(minBD).add(BigDecimal.ONE);
      BigDecimal valueBD = fraction.multiply(rangeBD).setScale(0, RoundingMode.FLOOR).add(minBD);
      return valueBD.longValueExact();
    }

    @Override
    public String toString() {
      String decimal = (decimalPoint ? ",DecimalPoint" : "");
      return "Fraction(" + field + "," + minWidth + "," + maxWidth + decimal + ")";
    }
  }

  static final class TextPrinterParser implements DateTimePrinterParser {
    private final TemporalField field;
    private final TextStyle textStyle;
    private final DateTimeTextProvider provider;
    private final Map<Long, String> custom;
    private volatile NumberPrinterParser numberPrinterParser;

    TextPrinterParser(TemporalField field, TextStyle textStyle, DateTimeTextProvider provider,
        Map<Long, String> custom) {
      this.field = field;
      this.textStyle = textStyle;
      this.provider = provider;
      this.custom = custom;
    }

    @Override
    public boolean format(DateTimePrintContext context, StringBuilder buf) {
      Long value = context.getValue(field);
      if (value == null) {
        return false;
      }
      String text;
      if (custom != null) {
        text = custom.get(value);
      } else {
        java.time.chrono.Chronology chrono = context.getTemporal().query(java.time.temporal.TemporalQueries.chronology());
        if (chrono == null || chrono == java.time.chrono.IsoChronology.INSTANCE) {
          text = provider.getText(field, value, textStyle, context.getLocale());
        } else {
          text = provider.getText(chrono, field, value, textStyle, context.getLocale());
        }
      }
      if (text == null) {
        return numberPrinterParser().format(context, buf);
      }
      buf.append(text);
      return true;
    }

    @Override
    public int parse(DateTimeParseContext context, CharSequence parseText, int position) {
      int length = parseText.length();
      if (position < 0 || position > length) {
        throw new IndexOutOfBoundsException();
      }
      TextStyle style = (context.isStrict() ? textStyle : null);
      Iterator<Map.Entry<String, Long>> it;
      if (custom != null) {
        List<Map.Entry<String, Long>> list = new ArrayList<Map.Entry<String, Long>>();
        for (Map.Entry<Long, String> e : custom.entrySet()) {
          list.add(new java.util.AbstractMap.SimpleImmutableEntry<String, Long>(e.getValue(), e.getKey()));
        }
        DateTimeTextProvider.sortByLength(list);
        it = list.iterator();
      } else {
        it = provider.getTextIterator(context.getEffectiveChronology(), field, style, context.getLocale());
      }
      if (it != null) {
        while (it.hasNext()) {
          Map.Entry<String, Long> entry = it.next();
          String itText = entry.getKey();
          if (context.subSequenceEquals(itText, 0, parseText, position, itText.length())) {
            return context.setParsedField(field, entry.getValue(), position, position + itText.length());
          }
        }
        if (context.isStrict()) {
          return ~position;
        }
      }
      return numberPrinterParser().parse(context, parseText, position);
    }

    private NumberPrinterParser numberPrinterParser() {
      if (numberPrinterParser == null) {
        numberPrinterParser = new NumberPrinterParser(field, 1, 19, SignStyle.NORMAL);
      }
      return numberPrinterParser;
    }

    @Override
    public String toString() {
      if (textStyle == TextStyle.FULL) {
        return "Text(" + field + ")";
      }
      return "Text(" + field + "," + textStyle + ")";
    }
  }

  static final class InstantPrinterParser implements DateTimePrinterParser {
    private static final long SECONDS_PER_10000_YEARS = 146097L * 25L * 86400L;
    private static final long SECONDS_0000_TO_1970 = ((146097L * 5L) - (30L * 365L + 7L)) * 86400L;
    private final int fractionalDigits;

    InstantPrinterParser(int fractionalDigits) {
      this.fractionalDigits = fractionalDigits;
    }

    @Override
    public boolean format(DateTimePrintContext context, StringBuilder buf) {
      Long inSecs = context.getValue(INSTANT_SECONDS);
      Long inNanos = null;
      if (context.getTemporal().isSupported(NANO_OF_SECOND)) {
        inNanos = context.getTemporal().getLong(NANO_OF_SECOND);
      }
      if (inSecs == null) {
        return false;
      }
      long inSec = inSecs;
      int inNano = NANO_OF_SECOND.checkValidIntValue(inNanos != null ? inNanos : 0);
      if (inSec >= -SECONDS_0000_TO_1970) {
        long zeroSecs = inSec - SECONDS_PER_10000_YEARS + SECONDS_0000_TO_1970;
        long hi = Math.floorDiv(zeroSecs, SECONDS_PER_10000_YEARS) + 1;
        long lo = Math.floorMod(zeroSecs, SECONDS_PER_10000_YEARS);
        LocalDateTime ldt = LocalDateTime.ofEpochSecond(lo - SECONDS_0000_TO_1970, 0, ZoneOffset.UTC);
        if (hi > 0) {
          buf.append('+').append(hi);
        }
        buf.append(ldt);
        if (ldt.getSecond() == 0) {
          buf.append(":00");
        }
      } else {
        long zeroSecs = inSec + SECONDS_0000_TO_1970;
        long hi = zeroSecs / SECONDS_PER_10000_YEARS;
        long lo = zeroSecs % SECONDS_PER_10000_YEARS;
        LocalDateTime ldt = LocalDateTime.ofEpochSecond(lo - SECONDS_0000_TO_1970, 0, ZoneOffset.UTC);
        int pos = buf.length();
        buf.append(ldt);
        if (ldt.getSecond() == 0) {
          buf.append(":00");
        }
        if (hi < 0) {
          if (ldt.getYear() == -10_000) {
            buf.replace(pos, pos + 2, Long.toString(hi - 1));
          } else if (lo == 0) {
            buf.insert(pos, hi);
          } else {
            buf.insert(pos + 1, Math.abs(hi));
          }
        }
      }
      if ((fractionalDigits < 0 && inNano > 0) || fractionalDigits > 0) {
        buf.append('.');
        int div = 100_000_000;
        for (int i = 0; ((fractionalDigits == -1 && inNano > 0) || (fractionalDigits == -2 && (inNano > 0
            || (i % 3) != 0))
            || i < fractionalDigits); i++) {
          int digit = inNano / div;
          buf.append((char) (digit + '0'));
          inNano = inNano - (digit * div);
          div = div / 10;
        }
      }
      buf.append('Z');
      return true;
    }

    @Override
    public int parse(DateTimeParseContext context, CharSequence text, int position) {
      int minDigits = (fractionalDigits < 0 ? 0 : fractionalDigits);
      int maxDigits = (fractionalDigits < 0 ? 9 : fractionalDigits);
      CompositePrinterParser parser = new DateTimeFormatterBuilder().append(DateTimeFormatter.ISO_LOCAL_DATE)
          .appendLiteral('T').appendValue(HOUR_OF_DAY, 2).appendLiteral(':').appendValue(MINUTE_OF_HOUR, 2)
          .appendLiteral(':').appendValue(SECOND_OF_MINUTE, 2).appendFraction(NANO_OF_SECOND, minDigits, maxDigits,
              true)
          .appendOffsetId().toFormatter().toPrinterParser(false);
      DateTimeParseContext newContext = context.copy();
      int pos = parser.parse(newContext, text, position);
      if (pos < 0) {
        return pos;
      }
      long yearParsed = newContext.getParsed(YEAR);
      int month = newContext.getParsed(MONTH_OF_YEAR).intValue();
      int day = newContext.getParsed(DAY_OF_MONTH).intValue();
      int hour = newContext.getParsed(HOUR_OF_DAY).intValue();
      int min = newContext.getParsed(MINUTE_OF_HOUR).intValue();
      Long secVal = newContext.getParsed(SECOND_OF_MINUTE);
      Long nanoVal = newContext.getParsed(NANO_OF_SECOND);
      int sec = (secVal != null ? secVal.intValue() : 0);
      int nano = (nanoVal != null ? nanoVal.intValue() : 0);
      int offset = newContext.getParsed(OFFSET_SECONDS).intValue();
      int days = 0;
      if (hour == 24 && min == 0 && sec == 0 && nano == 0) {
        hour = 0;
        days = 1;
      } else if (hour == 23 && min == 59 && sec == 60) {
        context.setParsedLeapSecond();
        sec = 59;
      }
      int year = (int) yearParsed % 10_000;
      long instantSecs;
      try {
        LocalDateTime ldt = LocalDateTime.of(year, month, day, hour, min, sec, 0).plusDays(days);
        instantSecs = ldt.toEpochSecond(ZoneOffset.ofTotalSeconds(offset));
        instantSecs += Math.multiplyExact(yearParsed / 10_000L, SECONDS_PER_10000_YEARS);
      } catch (RuntimeException ex) {
        return ~position;
      }
      int successPos = pos;
      successPos = context.setParsedField(INSTANT_SECONDS, instantSecs, position, successPos);
      return context.setParsedField(NANO_OF_SECOND, nano, position, successPos);
    }

    @Override
    public String toString() {
      return "Instant()";
    }
  }

  static final class OffsetIdPrinterParser implements DateTimePrinterParser {
    static final String[] PATTERNS = new String[] {"+HH", "+HHmm", "+HH:mm", "+HHMM", "+HH:MM", "+HHMMss",
        "+HH:MM:ss", "+HHMMSS", "+HH:MM:SS", "+HHmmss", "+HH:mm:ss"};

    static final OffsetIdPrinterParser INSTANCE_ID_Z = new OffsetIdPrinterParser("+HH:MM:ss", "Z");
    static final OffsetIdPrinterParser INSTANCE_ID_ZERO = new OffsetIdPrinterParser("+HH:MM:ss", "0");

    private final String noOffsetText;
    private final int type;

    OffsetIdPrinterParser(String pattern, String noOffsetText) {
      Objects.requireNonNull(pattern, "pattern");
      Objects.requireNonNull(noOffsetText, "noOffsetText");
      this.type = checkPattern(pattern);
      this.noOffsetText = noOffsetText;
    }

    private int checkPattern(String pattern) {
      for (int i = 0; i < PATTERNS.length; i++) {
        if (PATTERNS[i].equals(pattern)) {
          return i;
        }
      }
      throw new IllegalArgumentException("Invalid zone offset pattern: " + pattern);
    }

    private boolean isColon() {
      return type > 0 && (type % 2) == 0;
    }

    @Override
    public boolean format(DateTimePrintContext context, StringBuilder buf) {
      Long offsetSecs = context.getValue(OFFSET_SECONDS);
      if (offsetSecs == null) {
        return false;
      }
      int totalSecs = Math.toIntExact(offsetSecs);
      if (totalSecs == 0) {
        buf.append(noOffsetText);
      } else {
        int absHours = Math.abs((totalSecs / 3600) % 100);
        int absMinutes = Math.abs((totalSecs / 60) % 60);
        int absSeconds = Math.abs(totalSecs % 60);
        int bufPos = buf.length();
        int output = absHours;
        buf.append(totalSecs < 0 ? "-" : "+");
        formatZeroPad(false, absHours, buf);
        if ((type >= 3 && type <= 8) || (type >= 9 && absSeconds > 0) || (type >= 1 && absMinutes > 0)) {
          formatZeroPad(isColon(), absMinutes, buf);
          output += absMinutes;
          if (type == 7 || type == 8 || (type >= 5 && absSeconds > 0)) {
            formatZeroPad(isColon(), absSeconds, buf);
            output += absSeconds;
          }
        }
        if (output == 0) {
          buf.setLength(bufPos);
          buf.append(noOffsetText);
        }
      }
      return true;
    }

    private void formatZeroPad(boolean colon, int value, StringBuilder buf) {
      buf.append(colon ? ":" : "").append((char) (value / 10 + '0')).append((char) (value % 10 + '0'));
    }

    @Override
    public int parse(DateTimeParseContext context, CharSequence text, int position) {
      int length = text.length();
      int noOffsetLen = noOffsetText.length();
      if (noOffsetLen == 0) {
        if (position == length) {
          return context.setParsedField(OFFSET_SECONDS, 0, position, position);
        }
      } else {
        if (position == length) {
          return ~position;
        }
        if (context.subSequenceEquals(text, position, noOffsetText, 0, noOffsetLen)) {
          return context.setParsedField(OFFSET_SECONDS, 0, position, position + noOffsetLen);
        }
      }
      char sign = text.charAt(position);
      if (sign == '+' || sign == '-') {
        int negative = (sign == '-' ? -1 : 1);
        boolean colon = isColon();
        int minutesMode = minutesMode();
        int secondsMode = secondsMode();
        if (!context.isStrict()) {
          colon = length > position + 3 && text.charAt(position + 3) == ':';
          minutesMode = minutesMode == 0 ? 1 : minutesMode == 2 ? 1 : minutesMode;
          secondsMode = secondsMode == 0 ? 1 : secondsMode == 2 ? 1 : secondsMode;
          if (type == 0) {
            minutesMode = 1;
            secondsMode = 1;
          }
        }
        int[] array = new int[4];
        array[0] = position + 1;
        if (!parseNumber(array, 1, text, false)) {
          array[0] = ~array[0];
        } else {
          boolean gotMinutes = false;
          if (minutesMode != 0) {
            gotMinutes = parseMinutesSeconds(array, 2, text, colon, minutesMode == 2);
            if (array[0] < 0) {
              // mandatory part missing
            } else if (gotMinutes && secondsMode != 0) {
              parseMinutesSeconds(array, 3, text, colon, secondsMode == 2);
            }
          }
        }
        if (array[0] > 0) {
          if (array[1] > 23 || array[2] > 59 || array[3] > 59) {
            throw new DateTimeException("Value out of range: Hour[0-23], Minute[0-59], Second[0-59]");
          }
          long offsetSecs = negative * (array[1] * 3600L + array[2] * 60L + array[3]);
          return context.setParsedField(OFFSET_SECONDS, offsetSecs, position, array[0]);
        }
      }
      if (noOffsetLen == 0) {
        return context.setParsedField(OFFSET_SECONDS, 0, position, position);
      }
      return ~position;
    }

    private int minutesMode() {
      switch (type) {
        case 0:
          return 0;
        case 1:
        case 2:
        case 9:
        case 10:
          return 1;
        default:
          return 2;
      }
    }

    private int secondsMode() {
      switch (type) {
        case 5:
        case 6:
        case 9:
        case 10:
          return 1;
        case 7:
        case 8:
          return 2;
        default:
          return 0;
      }
    }

    private boolean parseMinutesSeconds(int[] array, int index, CharSequence text, boolean colon,
        boolean mandatory) {
      int pos = array[0];
      if (colon) {
        if (pos >= text.length() || text.charAt(pos) != ':') {
          if (mandatory) {
            array[0] = ~array[0];
          }
          return false;
        }
        pos++;
      }
      if (pos + 2 > text.length()) {
        if (mandatory) {
          array[0] = ~array[0];
        }
        return false;
      }
      char ch1 = text.charAt(pos);
      char ch2 = text.charAt(pos + 1);
      if (ch1 < '0' || ch1 > '9' || ch2 < '0' || ch2 > '9') {
        if (mandatory) {
          array[0] = ~array[0];
        }
        return false;
      }
      int value = (ch1 - 48) * 10 + (ch2 - 48);
      if (value > 59) {
        if (mandatory) {
          array[0] = ~array[0];
        }
        return false;
      }
      array[index] = value;
      array[0] = pos + 2;
      return true;
    }

    private boolean parseNumber(int[] array, int arrayIndex, CharSequence parseText, boolean required) {
      int pos = array[0];
      if (pos + 2 > parseText.length()) {
        return required;
      }
      char ch1 = parseText.charAt(pos++);
      char ch2 = parseText.charAt(pos++);
      if (ch1 < '0' || ch1 > '9' || ch2 < '0' || ch2 > '9') {
        return required;
      }
      int value = (ch1 - 48) * 10 + (ch2 - 48);
      if (value < 0 || value > 59) {
        return required;
      }
      array[arrayIndex] = value;
      array[0] = pos;
      return true;
    }

    @Override
    public String toString() {
      String converted = noOffsetText.replace("'", "''");
      return "Offset(" + PATTERNS[type] + ",'" + converted + "')";
    }
  }

  static final class LocalizedOffsetIdPrinterParser implements DateTimePrinterParser {
    private final TextStyle style;

    LocalizedOffsetIdPrinterParser(TextStyle style) {
      this.style = style;
    }

    private static StringBuilder appendHMS(StringBuilder buf, int t) {
      return buf.append((char) (t / 10 + '0')).append((char) (t % 10 + '0'));
    }

    @Override
    public boolean format(DateTimePrintContext context, StringBuilder buf) {
      Long offsetSecs = context.getValue(OFFSET_SECONDS);
      if (offsetSecs == null) {
        return false;
      }
      String gmtText = "GMT";
      buf.append(gmtText);
      int totalSecs = Math.toIntExact(offsetSecs);
      if (totalSecs != 0) {
        int absHours = Math.abs((totalSecs / 3600) % 100);
        int absMinutes = Math.abs((totalSecs / 60) % 60);
        int absSeconds = Math.abs(totalSecs % 60);
        buf.append(totalSecs < 0 ? "-" : "+");
        if (style == TextStyle.FULL) {
          appendHMS(buf, absHours);
          buf.append(':');
          appendHMS(buf, absMinutes);
          if (absSeconds != 0) {
            buf.append(':');
            appendHMS(buf, absSeconds);
          }
        } else {
          if (absHours >= 10) {
            buf.append((char) (absHours / 10 + '0'));
          }
          buf.append((char) (absHours % 10 + '0'));
          if (absMinutes != 0 || absSeconds != 0) {
            buf.append(':');
            appendHMS(buf, absMinutes);
            if (absSeconds != 0) {
              buf.append(':');
              appendHMS(buf, absSeconds);
            }
          }
        }
      }
      return true;
    }

    @Override
    public int parse(DateTimeParseContext context, CharSequence text, int position) {
      int pos = position;
      int end = text.length();
      String gmtText = "GMT";
      if (!context.subSequenceEquals(text, pos, gmtText, 0, gmtText.length())) {
        return ~position;
      }
      pos += gmtText.length();
      if (pos == end) {
        return context.setParsedField(OFFSET_SECONDS, 0, position, pos);
      }
      char sign = text.charAt(pos);
      int negative;
      if (sign == '+') {
        negative = 1;
      } else if (sign == '-') {
        negative = -1;
      } else {
        return context.setParsedField(OFFSET_SECONDS, 0, position, pos);
      }
      pos++;
      int h = 0;
      int m = 0;
      int s = 0;
      int digits = 0;
      int start = pos;
      while (pos < end && digits < 2 && Character.isDigit(text.charAt(pos))) {
        h = h * 10 + (text.charAt(pos) - '0');
        pos++;
        digits++;
      }
      if (digits == 0) {
        return ~position;
      }
      if (pos < end && text.charAt(pos) == ':') {
        int p = pos + 1;
        if (p + 2 <= end && Character.isDigit(text.charAt(p)) && Character.isDigit(text.charAt(p + 1))) {
          m = (text.charAt(p) - '0') * 10 + (text.charAt(p + 1) - '0');
          pos = p + 2;
          if (pos < end && text.charAt(pos) == ':' && pos + 3 <= end && Character.isDigit(text.charAt(pos + 1))
              && Character.isDigit(text.charAt(pos + 2))) {
            s = (text.charAt(pos + 1) - '0') * 10 + (text.charAt(pos + 2) - '0');
            pos += 3;
          }
        }
      }
      long offsetSecs = negative * (h * 3600L + m * 60L + s);
      return context.setParsedField(OFFSET_SECONDS, offsetSecs, position, pos);
    }

    @Override
    public String toString() {
      return "LocalizedOffset(" + style + ")";
    }
  }

  static final TemporalQuery<ZoneId> QUERY_REGION_ONLY = new TemporalQuery<ZoneId>() {
    @Override
    public ZoneId queryFrom(TemporalAccessor temporal) {
      ZoneId zone = temporal.query(TemporalQueries.zoneId());
      return (zone != null && !(zone instanceof ZoneOffset) ? zone : null);
    }

    @Override
    public String toString() {
      return "ZoneRegion";
    }
  };

  static class ZoneIdPrinterParser implements DateTimePrinterParser {
    private final TemporalQuery<ZoneId> query;
    private final String description;

    ZoneIdPrinterParser(TemporalQuery<ZoneId> query, String description) {
      this.query = query;
      this.description = description;
    }

    @Override
    public boolean format(DateTimePrintContext context, StringBuilder buf) {
      ZoneId zone = context.getValue(query);
      if (zone == null) {
        return false;
      }
      buf.append(zone.getId());
      return true;
    }

    @Override
    public int parse(DateTimeParseContext context, CharSequence text, int position) {
      int length = text.length();
      if (position > length) {
        throw new IndexOutOfBoundsException();
      }
      if (position == length) {
        return ~position;
      }
      char nextChar = text.charAt(position);
      if (nextChar == '+' || nextChar == '-') {
        return parseOffsetBased(context, text, position, position, OffsetIdPrinterParser.INSTANCE_ID_Z);
      } else if (length >= position + 2) {
        char nextNextChar = text.charAt(position + 1);
        if (context.charEquals(nextChar, 'U') && context.charEquals(nextNextChar, 'T')) {
          if (length >= position + 3 && context.charEquals(text.charAt(position + 2), 'C')) {
            return parseOffsetBased(context, text, position, position + 3, OffsetIdPrinterParser.INSTANCE_ID_ZERO);
          }
          return parseOffsetBased(context, text, position, position + 2, OffsetIdPrinterParser.INSTANCE_ID_ZERO);
        } else if (context.charEquals(nextChar, 'G') && length >= position + 3
            && context.charEquals(nextNextChar, 'M') && context.charEquals(text.charAt(position + 2), 'T')) {
          if (length >= position + 4 && context.charEquals(text.charAt(position + 3), '0')) {
            context.setParsed(ZoneId.of("GMT0"));
            return position + 4;
          }
          return parseOffsetBased(context, text, position, position + 3, OffsetIdPrinterParser.INSTANCE_ID_ZERO);
        }
      }
      String best = null;
      int bestEnd = -1;
      Set<String> ids = ZoneRulesProvider.getAvailableZoneIds();
      int end = position;
      while (end < length && isZoneIdChar(text.charAt(end))) {
        end++;
      }
      for (int e = end; e > position; e--) {
        String candidate = text.subSequence(position, e).toString();
        if (context.isCaseSensitive()) {
          if (ids.contains(candidate)) {
            best = candidate;
            bestEnd = e;
            break;
          }
        } else {
          for (String id : ids) {
            if (id.equalsIgnoreCase(candidate)) {
              best = id;
              bestEnd = e;
              break;
            }
          }
          if (best != null) {
            break;
          }
        }
      }
      if (best == null) {
        if (context.charEquals(nextChar, 'Z')) {
          context.setParsed(ZoneOffset.UTC);
          return position + 1;
        }
        return ~position;
      }
      context.setParsed(ZoneId.of(best));
      return bestEnd;
    }

    private static boolean isZoneIdChar(char c) {
      return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '/' || c == '_'
          || c == '-' || c == '+' || c == '~' || c == '.';
    }

    private int parseOffsetBased(DateTimeParseContext context, CharSequence parseText, int prefixPos, int position,
        OffsetIdPrinterParser parser) {
      String prefix = parseText.subSequence(prefixPos, position).toString().toUpperCase();
      if (position >= parseText.length()) {
        context.setParsed(ZoneId.of(prefix));
        return position;
      }
      if (parseText.charAt(position) == '0' || context.charEquals(parseText.charAt(position), 'Z')) {
        context.setParsed(ZoneId.of(prefix));
        return position;
      }
      DateTimeParseContext newContext = context.copy();
      int endPos = parser.parse(newContext, parseText, position);
      try {
        if (endPos < 0) {
          if (parser == OffsetIdPrinterParser.INSTANCE_ID_Z) {
            return ~prefixPos;
          }
          context.setParsed(ZoneId.of(prefix));
          return position;
        }
        int offset = (int) newContext.getParsed(OFFSET_SECONDS).longValue();
        ZoneOffset zoneOffset = ZoneOffset.ofTotalSeconds(offset);
        context.setParsed(ZoneId.ofOffset(prefix, zoneOffset));
        return endPos;
      } catch (DateTimeException dte) {
        return ~prefixPos;
      }
    }

    @Override
    public String toString() {
      return description;
    }
  }

  static final class ZoneTextPrinterParser extends ZoneIdPrinterParser {
    private final TextStyle textStyle;
    private final boolean isGeneric;

    ZoneTextPrinterParser(TextStyle textStyle, boolean isGeneric) {
      super(TemporalQueries.zone(), "ZoneText(" + textStyle + ")");
      this.textStyle = Objects.requireNonNull(textStyle, "textStyle");
      this.isGeneric = isGeneric;
    }

    @Override
    public boolean format(DateTimePrintContext context, StringBuilder buf) {
      ZoneId zone = context.getValue(TemporalQueries.zoneId());
      if (zone == null) {
        return false;
      }
      String zname = zone.getId();
      if (!(zone instanceof ZoneOffset)) {
        TemporalAccessor dt = context.getTemporal();
        boolean daylight = false;
        boolean generic = true;
        if (!isGeneric) {
          if (dt.isSupported(INSTANT_SECONDS)) {
            generic = false;
            daylight = zone.getRules().isDaylightSavings(Instant.from(dt));
          } else if (dt.isSupported(ChronoField.EPOCH_DAY) && dt.isSupported(ChronoField.NANO_OF_DAY)) {
            java.time.LocalDateTime ldt = java.time.LocalDate.ofEpochDay(dt.getLong(ChronoField.EPOCH_DAY))
                .atTime(java.time.LocalTime.ofNanoOfDay(dt.getLong(ChronoField.NANO_OF_DAY)));
            if (zone.getRules().getTransition(ldt) == null) {
              generic = false;
              daylight = zone.getRules().isDaylightSavings(ldt.atZone(zone).toInstant());
            }
          }
        }
        String name = sun.util.locale.provider.ZoneNames.get(zname, textStyle, generic, daylight);
        if (name != null) {
          zname = name;
        }
      }
      buf.append(zname);
      return true;
    }
  }

  static final class DayPeriodPrinterParser implements DateTimePrinterParser {
    private final TextStyle style;

    DayPeriodPrinterParser(TextStyle style) {
      this.style = style;
    }

    private String[] runs(Locale locale) {
      String key = style.asNormal() == TextStyle.FULL ? "DPF" : style.asNormal() == TextStyle.SHORT ? "DPS" : "DPN";
      String[] runs = shim.LocaleDb.list(locale, key);
      return runs == null ? new String[] {"0=AM", "720=PM"} : runs;
    }

    @Override
    public boolean format(DateTimePrintContext context, StringBuilder buf) {
      Long value = context.getValue(ChronoField.MINUTE_OF_DAY);
      if (value == null) {
        return false;
      }
      String[] runs = runs(context.getLocale());
      String found = null;
      for (String run : runs) {
        int eq = run.indexOf('=');
        if (Integer.parseInt(run.substring(0, eq)) <= value.longValue()) {
          found = run.substring(eq + 1);
        } else {
          break;
        }
      }
      buf.append(found);
      return true;
    }

    @Override
    public int parse(DateTimeParseContext context, CharSequence text, int position) {
      if (position < 0 || position > text.length()) {
        throw new IndexOutOfBoundsException();
      }
      String[] runs = runs(context.getLocale());
      int bestMinute = -1;
      int bestLength = -1;
      for (String run : runs) {
        int eq = run.indexOf('=');
        String name = run.substring(eq + 1);
        if (name.length() > bestLength && context.subSequenceEquals(name, 0, text, position, name.length())) {
          bestMinute = Integer.parseInt(run.substring(0, eq));
          bestLength = name.length();
        }
      }
      if (bestLength < 0) {
        return ~position;
      }
      return context.setParsedField(ChronoField.MINUTE_OF_DAY, bestMinute, position, position + bestLength);
    }

    @Override
    public String toString() {
      return "DayPeriod(" + style + ")";
    }
  }

  static final class ChronoPrinterParser implements DateTimePrinterParser {
    private final TextStyle textStyle;

    ChronoPrinterParser(TextStyle textStyle) {
      this.textStyle = textStyle;
    }

    @Override
    public boolean format(DateTimePrintContext context, StringBuilder buf) {
      Chronology chrono = context.getValue(TemporalQueries.chronology());
      if (chrono == null) {
        return false;
      }
      if (textStyle == null) {
        buf.append(chrono.getId());
      } else {
        buf.append(chrono.getDisplayName(textStyle, context.getLocale()));
      }
      return true;
    }

    @Override
    public int parse(DateTimeParseContext context, CharSequence text, int position) {
      if (position < 0 || position > text.length()) {
        throw new IndexOutOfBoundsException();
      }
      String[] ids = {"ISO", "iso8601"};
      for (String id : ids) {
        if (context.subSequenceEquals(id, 0, text, position, id.length())) {
          context.setParsed(IsoChronology.INSTANCE);
          return position + id.length();
        }
      }
      return ~position;
    }

    @Override
    public String toString() {
      return "ChronologyText(" + textStyle + ")";
    }
  }

  static final class WeekBasedFieldPrinterParser extends NumberPrinterParser {
    private final char chr;
    private final int count;

    WeekBasedFieldPrinterParser(char chr, int count, int minWidth, int maxWidth) {
      this(chr, count, minWidth, maxWidth, 0);
    }

    WeekBasedFieldPrinterParser(char chr, int count, int minWidth, int maxWidth, int subsequentWidth) {
      super(null, minWidth, maxWidth, SignStyle.NOT_NEGATIVE, subsequentWidth);
      this.chr = chr;
      this.count = count;
    }

    @Override
    WeekBasedFieldPrinterParser withFixedWidth() {
      if (subsequentWidth == -1) {
        return this;
      }
      return new WeekBasedFieldPrinterParser(chr, count, minWidth, maxWidth, -1);
    }

    @Override
    WeekBasedFieldPrinterParser withSubsequentWidth(int subsequentWidth) {
      return new WeekBasedFieldPrinterParser(chr, count, minWidth, maxWidth, this.subsequentWidth + subsequentWidth);
    }

    private NumberPrinterParser printerParser(Locale locale) {
      WeekFields weekDef = WeekFields.of(locale);
      TemporalField field = null;
      switch (chr) {
        case 'Y':
          field = weekDef.weekBasedYear();
          if (count == 2) {
            return new ReducedPrinterParser(field, 2, 2, 0, ReducedPrinterParser.BASE_DATE, subsequentWidth);
          } else {
            return new NumberPrinterParser(field, count, 19, (count < 4) ? SignStyle.NORMAL : SignStyle.EXCEEDS_PAD,
                subsequentWidth);
          }
        case 'e':
        case 'c':
          field = weekDef.dayOfWeek();
          break;
        case 'w':
          field = weekDef.weekOfWeekBasedYear();
          break;
        case 'W':
          field = weekDef.weekOfMonth();
          break;
        default:
          throw new IllegalStateException("unreachable");
      }
      return new NumberPrinterParser(field, minWidth, maxWidth, SignStyle.NOT_NEGATIVE, subsequentWidth);
    }

    @Override
    public boolean format(DateTimePrintContext context, StringBuilder buf) {
      return printerParser(context.getLocale()).format(context, buf);
    }

    @Override
    public int parse(DateTimeParseContext context, CharSequence text, int position) {
      return printerParser(context.getLocale()).parse(context, text, position);
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder(30);
      sb.append("Localized(");
      if (chr == 'Y') {
        if (count == 1) {
          sb.append("WeekBasedYear");
        } else if (count == 2) {
          sb.append("ReducedValue(WeekBasedYear,2,2,2000-01-01)");
        } else {
          sb.append("WeekBasedYear,").append(count).append(",").append(19).append(",")
              .append((count < 4) ? SignStyle.NORMAL : SignStyle.EXCEEDS_PAD);
        }
      } else {
        switch (chr) {
          case 'c':
          case 'e':
            sb.append("DayOfWeek");
            break;
          case 'w':
            sb.append("WeekOfWeekBasedYear");
            break;
          case 'W':
            sb.append("WeekOfMonth");
            break;
          default:
            break;
        }
        sb.append(",");
        sb.append(count);
      }
      sb.append(")");
      return sb.toString();
    }
  }
}
