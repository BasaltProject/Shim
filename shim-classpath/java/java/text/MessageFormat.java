/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.text;

import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class MessageFormat extends Format {
  private static final long serialVersionUID = 6479157306784022952L;

  private static final int kSegRaw = 0;
  private static final int kSegIndex = 1;
  private static final int kSegType = 2;
  private static final int kSegModifier = 3;
  private static final int kTypeNull = 0;
  private static final int kTypeNumber = 1;
  private static final int kTypeDate = 2;
  private static final int kTypeTime = 3;
  private static final int kTypeChoice = 4;
  private static final String[] kTypeKeywords = {"", "number", "date", "time", "choice"};
  private static final String[] kNumberModifiers = {"", "currency", "percent", "integer"};
  private static final String[] kDateTimeModifiers = {"", "short", "medium", "long", "full"};
  private static final int[] kDateTimeStyles = {DateFormat.DEFAULT, DateFormat.SHORT, DateFormat.MEDIUM,
      DateFormat.LONG, DateFormat.FULL};

  private Locale locale;
  private String pattern = "";
  private Format[] formats = new Format[10];
  private int[] offsets = new int[10];
  private int[] argumentNumbers = new int[10];
  private int maxOffset = -1;

  public MessageFormat(String pattern) {
    this.locale = Locale.getDefault(Locale.Category.FORMAT);
    applyPattern(pattern);
  }

  public MessageFormat(String pattern, Locale locale) {
    this.locale = locale;
    applyPattern(pattern);
  }

  public void setLocale(Locale locale) {
    this.locale = locale;
  }

  public Locale getLocale() {
    return locale;
  }

  public void applyPattern(String pattern) {
    StringBuilder[] segments = new StringBuilder[4];
    segments[kSegRaw] = new StringBuilder();
    int part = kSegRaw;
    int formatNumber = 0;
    boolean inQuote = false;
    int braceStack = 0;
    maxOffset = -1;
    for (int i = 0; i < pattern.length(); ++i) {
      char ch = pattern.charAt(i);
      if (part == kSegRaw) {
        if (ch == '\'') {
          if (i + 1 < pattern.length() && pattern.charAt(i + 1) == '\'') {
            segments[part].append(ch);
            ++i;
          } else {
            inQuote = !inQuote;
          }
        } else if (ch == '{' && !inQuote) {
          part = kSegIndex;
          if (segments[kSegIndex] == null) {
            segments[kSegIndex] = new StringBuilder();
          }
        } else {
          segments[part].append(ch);
        }
      } else {
        if (inQuote) {
          segments[part].append(ch);
          if (ch == '\'') {
            inQuote = false;
          }
        } else {
          switch (ch) {
            case ',':
              if (part < kSegModifier) {
                if (segments[++part] == null) {
                  segments[part] = new StringBuilder();
                }
              } else {
                segments[part].append(ch);
              }
              break;
            case '{':
              ++braceStack;
              segments[part].append(ch);
              break;
            case '}':
              if (braceStack == 0) {
                part = kSegRaw;
                makeFormat(i, formatNumber, segments);
                formatNumber++;
                segments[kSegIndex] = null;
                segments[kSegType] = null;
                segments[kSegModifier] = null;
              } else {
                --braceStack;
                segments[part].append(ch);
              }
              break;
            case ' ':
              if (part != kSegType || segments[kSegType].length() > 0) {
                segments[part].append(ch);
              }
              break;
            case '\'':
              inQuote = true;
              segments[part].append(ch);
              break;
            default:
              segments[part].append(ch);
              break;
          }
        }
      }
    }
    if (braceStack == 0 && part != 0) {
      maxOffset = -1;
      throw new IllegalArgumentException("Unmatched braces in the pattern.");
    }
    this.pattern = segments[kSegRaw].toString();
  }

  public String toPattern() {
    StringBuilder result = new StringBuilder();
    int lastOffset = 0;
    for (int i = 0; i <= maxOffset; ++i) {
      copyAndFixQuotes(pattern, lastOffset, offsets[i], result);
      lastOffset = offsets[i];
      result.append('{').append(argumentNumbers[i]);
      Format fmt = formats[i];
      if (fmt == null) {
        // plain string argument
      } else if (fmt instanceof NumberFormat) {
        if (fmt.equals(NumberFormat.getInstance(locale))) {
          result.append(",number");
        } else if (fmt.equals(NumberFormat.getCurrencyInstance(locale))) {
          result.append(",number,currency");
        } else if (fmt.equals(NumberFormat.getPercentInstance(locale))) {
          result.append(",number,percent");
        } else if (fmt.equals(NumberFormat.getIntegerInstance(locale))) {
          result.append(",number,integer");
        } else if (fmt instanceof DecimalFormat) {
          result.append(",number,").append(((DecimalFormat) fmt).toPattern());
        } else if (fmt instanceof ChoiceFormat) {
          result.append(",choice,").append(((ChoiceFormat) fmt).toPattern());
        }
      } else if (fmt instanceof DateFormat) {
        int index;
        for (index = 0; index < kDateTimeStyles.length; index++) {
          if (fmt.equals(DateFormat.getDateInstance(kDateTimeStyles[index], locale))) {
            result.append(",date");
            break;
          }
          if (fmt.equals(DateFormat.getTimeInstance(kDateTimeStyles[index], locale))) {
            result.append(",time");
            break;
          }
        }
        if (index >= kDateTimeStyles.length) {
          if (fmt instanceof SimpleDateFormat) {
            result.append(",date,").append(((SimpleDateFormat) fmt).toPattern());
          }
        } else if (index != 0) {
          result.append(',').append(kDateTimeModifiers[index]);
        }
      }
      result.append('}');
    }
    copyAndFixQuotes(pattern, lastOffset, pattern.length(), result);
    return result.toString();
  }

  public void setFormatsByArgumentIndex(Format[] newFormats) {
    for (int i = 0; i <= maxOffset; i++) {
      int j = argumentNumbers[i];
      if (j < newFormats.length) {
        formats[i] = newFormats[j];
      }
    }
  }

  public void setFormats(Format[] newFormats) {
    int runsToCopy = newFormats.length;
    if (runsToCopy > maxOffset + 1) {
      runsToCopy = maxOffset + 1;
    }
    for (int i = 0; i < runsToCopy; i++) {
      formats[i] = newFormats[i];
    }
  }

  public void setFormatByArgumentIndex(int argumentIndex, Format newFormat) {
    for (int j = 0; j <= maxOffset; j++) {
      if (argumentNumbers[j] == argumentIndex) {
        formats[j] = newFormat;
      }
    }
  }

  public void setFormat(int formatElementIndex, Format newFormat) {
    if (formatElementIndex > maxOffset) {
      throw new ArrayIndexOutOfBoundsException(formatElementIndex);
    }
    formats[formatElementIndex] = newFormat;
  }

  public Format[] getFormatsByArgumentIndex() {
    int maximumArgumentNumber = -1;
    for (int i = 0; i <= maxOffset; i++) {
      if (argumentNumbers[i] > maximumArgumentNumber) {
        maximumArgumentNumber = argumentNumbers[i];
      }
    }
    Format[] resultArray = new Format[maximumArgumentNumber + 1];
    for (int i = 0; i <= maxOffset; i++) {
      resultArray[argumentNumbers[i]] = formats[i];
    }
    return resultArray;
  }

  public Format[] getFormats() {
    Format[] resultArray = new Format[maxOffset + 1];
    System.arraycopy(formats, 0, resultArray, 0, maxOffset + 1);
    return resultArray;
  }

  public final StringBuffer format(Object[] arguments, StringBuffer result, FieldPosition pos) {
    return subformat(arguments, result, pos);
  }

  public static String format(String pattern, Object... arguments) {
    MessageFormat temp = new MessageFormat(pattern);
    return temp.format(arguments);
  }

  @Override
  public final StringBuffer format(Object arguments, StringBuffer result, FieldPosition pos) {
    return subformat((Object[]) arguments, result, pos);
  }

  private StringBuffer subformat(Object[] arguments, StringBuffer result, FieldPosition fp) {
    int lastOffset = 0;
    for (int i = 0; i <= maxOffset; ++i) {
      result.append(pattern, lastOffset, offsets[i]);
      lastOffset = offsets[i];
      int argumentNumber = argumentNumbers[i];
      if (arguments == null || argumentNumber >= arguments.length) {
        result.append('{').append(argumentNumber).append('}');
        continue;
      }
      Object obj = arguments[argumentNumber];
      String arg = null;
      Format subFormatter = null;
      if (obj == null) {
        arg = "null";
      } else if (formats[i] != null) {
        subFormatter = formats[i];
        if (subFormatter instanceof ChoiceFormat) {
          arg = formats[i].format(obj);
          if (arg.indexOf('{') >= 0) {
            subFormatter = new MessageFormat(arg, locale);
            obj = arguments;
            arg = null;
          }
        }
      } else if (obj instanceof Number) {
        subFormatter = NumberFormat.getInstance(locale);
      } else if (obj instanceof Date) {
        subFormatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
      } else if (obj instanceof String) {
        arg = (String) obj;
      } else {
        arg = obj.toString();
        if (arg == null) {
          arg = "null";
        }
      }
      if (arg == null) {
        arg = subFormatter.format(obj);
      }
      result.append(arg);
    }
    result.append(pattern, lastOffset, pattern.length());
    return result;
  }

  public Object[] parse(String source, ParsePosition pos) {
    if (source == null) {
      return new Object[0];
    }
    int maximumArgumentNumber = -1;
    for (int i = 0; i <= maxOffset; i++) {
      if (argumentNumbers[i] > maximumArgumentNumber) {
        maximumArgumentNumber = argumentNumbers[i];
      }
    }
    Object[] resultArray = new Object[maximumArgumentNumber + 1];
    int patternOffset = 0;
    int sourceOffset = pos.getIndex();
    ParsePosition tempStatus = new ParsePosition(0);
    for (int i = 0; i <= maxOffset; ++i) {
      int len = offsets[i] - patternOffset;
      if (len == 0 || pattern.regionMatches(patternOffset, source, sourceOffset, len)) {
        sourceOffset += len;
        patternOffset += len;
      } else {
        pos.setErrorIndex(sourceOffset);
        return null;
      }
      if (formats[i] == null) {
        int tempLength = (i != maxOffset) ? offsets[i + 1] : pattern.length();
        int next;
        if (patternOffset >= tempLength) {
          next = source.length();
        } else {
          next = source.indexOf(pattern.substring(patternOffset, tempLength), sourceOffset);
        }
        if (next < 0) {
          pos.setErrorIndex(sourceOffset);
          return null;
        }
        String strValue = source.substring(sourceOffset, next);
        if (!strValue.equals("{" + argumentNumbers[i] + "}")) {
          resultArray[argumentNumbers[i]] = source.substring(sourceOffset, next);
        }
        sourceOffset = next;
      } else {
        tempStatus.setIndex(sourceOffset);
        resultArray[argumentNumbers[i]] = formats[i].parseObject(source, tempStatus);
        if (tempStatus.getIndex() == sourceOffset) {
          pos.setErrorIndex(sourceOffset);
          return null;
        }
        sourceOffset = tempStatus.getIndex();
      }
    }
    int len = pattern.length() - patternOffset;
    if (len == 0 || pattern.regionMatches(patternOffset, source, sourceOffset, len)) {
      pos.setIndex(sourceOffset + len);
    } else {
      pos.setErrorIndex(sourceOffset);
      return null;
    }
    return resultArray;
  }

  public Object[] parse(String source) throws ParseException {
    ParsePosition pos = new ParsePosition(0);
    Object[] result = parse(source, pos);
    if (pos.getIndex() == 0) {
      throw new ParseException("MessageFormat parse error!", pos.getErrorIndex());
    }
    return result;
  }

  @Override
  public Object parseObject(String source, ParsePosition pos) {
    return parse(source, pos);
  }

  @Override
  public Object clone() {
    MessageFormat other = (MessageFormat) super.clone();
    other.formats = formats.clone();
    for (int i = 0; i < formats.length; ++i) {
      if (formats[i] != null) {
        other.formats[i] = (Format) formats[i].clone();
      }
    }
    other.offsets = offsets.clone();
    other.argumentNumbers = argumentNumbers.clone();
    return other;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    MessageFormat other = (MessageFormat) obj;
    if (maxOffset != other.maxOffset || !pattern.equals(other.pattern)
        || !(locale == null ? other.locale == null : locale.equals(other.locale))) {
      return false;
    }
    for (int i = 0; i <= maxOffset; i++) {
      if (offsets[i] != other.offsets[i] || argumentNumbers[i] != other.argumentNumbers[i]
          || !(formats[i] == null ? other.formats[i] == null : formats[i].equals(other.formats[i]))) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    return pattern.hashCode();
  }

  private void makeFormat(int position, int offsetNumber, StringBuilder[] textSegments) {
    String[] segments = new String[4];
    for (int i = 0; i < 4; ++i) {
      segments[i] = textSegments[i] != null ? textSegments[i].toString() : "";
    }
    int argumentNumber;
    try {
      argumentNumber = Integer.parseInt(segments[kSegIndex]);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("can't parse argument number: " + segments[kSegIndex], e);
    }
    if (argumentNumber < 0) {
      throw new IllegalArgumentException("negative argument number: " + segments[kSegIndex]);
    }
    if (offsetNumber >= formats.length) {
      int newLength = formats.length * 2;
      formats = Arrays.copyOf(formats, newLength);
      offsets = Arrays.copyOf(offsets, newLength);
      argumentNumbers = Arrays.copyOf(argumentNumbers, newLength);
    }
    int oldMaxOffset = maxOffset;
    maxOffset = offsetNumber;
    offsets[offsetNumber] = segments[kSegRaw].length();
    argumentNumbers[offsetNumber] = argumentNumber;
    Format newFormat = null;
    if (segments[kSegType].length() != 0) {
      int type = findKeyword(segments[kSegType], kTypeKeywords);
      switch (type) {
        case kTypeNull:
          break;
        case kTypeNumber:
          switch (findKeyword(segments[kSegModifier], kNumberModifiers)) {
            case 0:
              newFormat = NumberFormat.getInstance(locale);
              break;
            case 1:
              newFormat = NumberFormat.getCurrencyInstance(locale);
              break;
            case 2:
              newFormat = NumberFormat.getPercentInstance(locale);
              break;
            case 3:
              newFormat = NumberFormat.getIntegerInstance(locale);
              break;
            default:
              try {
                newFormat = new DecimalFormat(segments[kSegModifier], DecimalFormatSymbols.getInstance(locale));
              } catch (IllegalArgumentException e) {
                maxOffset = oldMaxOffset;
                throw e;
              }
              break;
          }
          break;
        case kTypeDate:
        case kTypeTime: {
          int mod = findKeyword(segments[kSegModifier], kDateTimeModifiers);
          if (mod >= 0 && mod < kDateTimeModifiers.length) {
            if (type == kTypeDate) {
              newFormat = DateFormat.getDateInstance(kDateTimeStyles[mod], locale);
            } else {
              newFormat = DateFormat.getTimeInstance(kDateTimeStyles[mod], locale);
            }
          } else {
            try {
              newFormat = new SimpleDateFormat(segments[kSegModifier], locale);
            } catch (IllegalArgumentException e) {
              maxOffset = oldMaxOffset;
              throw e;
            }
          }
          break;
        }
        case kTypeChoice:
          try {
            newFormat = new ChoiceFormat(segments[kSegModifier]);
          } catch (Exception e) {
            maxOffset = oldMaxOffset;
            throw new IllegalArgumentException("Choice Pattern incorrect: " + e.getMessage(), e);
          }
          break;
        default:
          maxOffset = oldMaxOffset;
          throw new IllegalArgumentException("unknown format type: " + segments[kSegType]);
      }
    }
    formats[offsetNumber] = newFormat;
  }

  private static int findKeyword(String s, String[] list) {
    for (int i = 0; i < list.length; ++i) {
      if (s.equals(list[i])) {
        return i;
      }
    }
    String ls = s.trim().toLowerCase(Locale.ROOT);
    if (!ls.equals(s)) {
      for (int i = 0; i < list.length; ++i) {
        if (ls.equals(list[i])) {
          return i;
        }
      }
    }
    return -1;
  }

  private static void copyAndFixQuotes(String source, int start, int end, StringBuilder target) {
    boolean quoted = false;
    for (int i = start; i < end; ++i) {
      char ch = source.charAt(i);
      if (ch == '{') {
        if (!quoted) {
          target.append('\'');
          quoted = true;
        }
        target.append(ch);
      } else if (ch == '\'') {
        target.append("''");
      } else {
        if (quoted) {
          target.append('\'');
          quoted = false;
        }
        target.append(ch);
      }
    }
    if (quoted) {
      target.append('\'');
    }
  }
}
