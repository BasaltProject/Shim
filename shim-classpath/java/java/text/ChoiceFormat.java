/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.text;

import java.util.Arrays;

public class ChoiceFormat extends NumberFormat {
  private static final long serialVersionUID = 1795184449645032964L;

  private double[] choiceLimits;
  private String[] choiceFormats;

  public ChoiceFormat(String newPattern) {
    applyPattern(newPattern);
  }

  public ChoiceFormat(double[] limits, String[] formats) {
    setChoices(limits, formats);
  }

  public void applyPattern(String newPattern) {
    StringBuilder[] segments = new StringBuilder[2];
    segments[0] = new StringBuilder();
    segments[1] = new StringBuilder();
    double[] newChoiceLimits = new double[30];
    String[] newChoiceFormats = new String[30];
    int count = 0;
    int part = 0;
    double startValue = 0;
    double oldStartValue = Double.NaN;
    boolean inQuote = false;
    for (int i = 0; i < newPattern.length(); ++i) {
      char ch = newPattern.charAt(i);
      if (ch == '\'') {
        if ((i + 1) < newPattern.length() && newPattern.charAt(i + 1) == ch) {
          segments[part].append(ch);
          ++i;
        } else {
          inQuote = !inQuote;
        }
      } else if (inQuote) {
        segments[part].append(ch);
      } else if (ch == '<' || ch == '#' || ch == '\u2264') {
        if (segments[0].length() == 0) {
          throw new IllegalArgumentException("Each interval must contain a number before a format");
        }
        String text = segments[0].toString();
        if (text.equals("\u221E")) {
          startValue = Double.POSITIVE_INFINITY;
        } else if (text.equals("-\u221E")) {
          startValue = Double.NEGATIVE_INFINITY;
        } else {
          startValue = Double.parseDouble(text);
        }
        if (ch == '<' && startValue != Double.POSITIVE_INFINITY && startValue != Double.NEGATIVE_INFINITY) {
          startValue = nextDouble(startValue);
        }
        if (startValue <= oldStartValue) {
          throw new IllegalArgumentException("Incorrect order of intervals, must be in ascending order");
        }
        segments[0].setLength(0);
        part = 1;
      } else if (ch == '|') {
        if (count == newChoiceLimits.length) {
          newChoiceLimits = Arrays.copyOf(newChoiceLimits, count * 2);
          newChoiceFormats = Arrays.copyOf(newChoiceFormats, count * 2);
        }
        newChoiceLimits[count] = startValue;
        newChoiceFormats[count] = segments[1].toString();
        ++count;
        oldStartValue = startValue;
        segments[1].setLength(0);
        part = 0;
      } else {
        segments[part].append(ch);
      }
    }
    if (part == 1) {
      if (count == newChoiceLimits.length) {
        newChoiceLimits = Arrays.copyOf(newChoiceLimits, count + 1);
        newChoiceFormats = Arrays.copyOf(newChoiceFormats, count + 1);
      }
      newChoiceLimits[count] = startValue;
      newChoiceFormats[count] = segments[1].toString();
      ++count;
    }
    choiceLimits = Arrays.copyOf(newChoiceLimits, count);
    choiceFormats = Arrays.copyOf(newChoiceFormats, count);
  }

  public String toPattern() {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < choiceLimits.length; ++i) {
      if (i != 0) {
        result.append('|');
      }
      double less = previousDouble(choiceLimits[i]);
      double tryLessOrEqual = Math.abs(Math.IEEEremainder(choiceLimits[i], 1.0d));
      double tryLess = Math.abs(Math.IEEEremainder(less, 1.0d));
      if (tryLessOrEqual < tryLess) {
        result.append(choiceLimits[i]);
        result.append('#');
      } else {
        if (choiceLimits[i] == Double.POSITIVE_INFINITY) {
          result.append("\u221E");
        } else if (choiceLimits[i] == Double.NEGATIVE_INFINITY) {
          result.append("-\u221E");
        } else {
          result.append(less);
        }
        result.append('<');
      }
      String text = choiceFormats[i];
      boolean needQuote = text.indexOf('<') >= 0 || text.indexOf('#') >= 0 || text.indexOf('\u2264') >= 0
          || text.indexOf('|') >= 0;
      if (needQuote) {
        result.append('\'');
      }
      if (text.indexOf('\'') < 0) {
        result.append(text);
      } else {
        for (int j = 0; j < text.length(); ++j) {
          char c = text.charAt(j);
          result.append(c);
          if (c == '\'') {
            result.append(c);
          }
        }
      }
      if (needQuote) {
        result.append('\'');
      }
    }
    return result.toString();
  }

  public void setChoices(double[] limits, String[] formats) {
    if (limits.length != formats.length) {
      throw new IllegalArgumentException("Array and limit arrays must be of the same length.");
    }
    choiceLimits = Arrays.copyOf(limits, limits.length);
    choiceFormats = Arrays.copyOf(formats, formats.length);
  }

  public double[] getLimits() {
    return Arrays.copyOf(choiceLimits, choiceLimits.length);
  }

  public Object[] getFormats() {
    return Arrays.copyOf(choiceFormats, choiceFormats.length);
  }

  @Override
  public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition status) {
    return format((double) number, toAppendTo, status);
  }

  @Override
  public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition status) {
    int i;
    for (i = 0; i < choiceLimits.length; ++i) {
      if (!(number >= choiceLimits[i])) {
        break;
      }
    }
    --i;
    if (i < 0) {
      i = 0;
    }
    return toAppendTo.append(choiceFormats[i]);
  }

  @Override
  public Number parse(String text, ParsePosition status) {
    int start = status.getIndex();
    int furthest = start;
    double bestNumber = Double.NaN;
    double tempNumber = 0.0;
    for (int i = 0; i < choiceFormats.length; ++i) {
      String tempString = choiceFormats[i];
      if (text.regionMatches(start, tempString, 0, tempString.length())) {
        status.setIndex(start + tempString.length());
        tempNumber = choiceLimits[i];
        if (status.getIndex() > furthest) {
          furthest = status.getIndex();
          bestNumber = tempNumber;
          if (furthest == text.length()) {
            break;
          }
        }
      }
    }
    status.setIndex(furthest);
    if (status.getIndex() == start) {
      status.setErrorIndex(furthest);
    }
    return Double.valueOf(bestNumber);
  }

  public static final double nextDouble(double d) {
    return nextDouble(d, true);
  }

  public static final double previousDouble(double d) {
    return nextDouble(d, false);
  }

  public static double nextDouble(double d, boolean positive) {
    if (Double.isNaN(d)) {
      return d;
    }
    return positive ? Math.nextUp(d) : Math.nextDown(d);
  }

  @Override
  public Object clone() {
    ChoiceFormat other = (ChoiceFormat) super.clone();
    other.choiceLimits = choiceLimits.clone();
    other.choiceFormats = choiceFormats.clone();
    return other;
  }

  @Override
  public int hashCode() {
    int result = choiceLimits.length;
    if (choiceFormats.length > 0) {
      result ^= choiceFormats[choiceFormats.length - 1].hashCode();
    }
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (this == obj) {
      return true;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    ChoiceFormat other = (ChoiceFormat) obj;
    return Arrays.equals(choiceLimits, other.choiceLimits) && Arrays.equals(choiceFormats, other.choiceFormats);
  }
}
