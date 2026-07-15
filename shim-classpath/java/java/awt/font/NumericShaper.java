/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.font;

import java.io.Serializable;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public final class NumericShaper implements Serializable {
  public static final int EUROPEAN = 1 << 0;
  public static final int ARABIC = 1 << 1;
  public static final int EASTERN_ARABIC = 1 << 2;
  public static final int DEVANAGARI = 1 << 3;
  public static final int BENGALI = 1 << 4;
  public static final int GURMUKHI = 1 << 5;
  public static final int GUJARATI = 1 << 6;
  public static final int ORIYA = 1 << 7;
  public static final int TAMIL = 1 << 8;
  public static final int TELUGU = 1 << 9;
  public static final int KANNADA = 1 << 10;
  public static final int MALAYALAM = 1 << 11;
  public static final int THAI = 1 << 12;
  public static final int LAO = 1 << 13;
  public static final int TIBETAN = 1 << 14;
  public static final int MYANMAR = 1 << 15;
  public static final int ETHIOPIC = 1 << 16;
  public static final int KHMER = 1 << 17;
  public static final int MONGOLIAN = 1 << 18;
  public static final int ALL_RANGES = 0x0007ffff;

  public enum Range {
    EUROPEAN(0x0030, 0x0000, 0x0000) { },
    ARABIC(0x0660, 0x0600, 0x06FF),
    EASTERN_ARABIC(0x06F0, 0x0600, 0x06FF),
    DEVANAGARI(0x0966, 0x0900, 0x097F),
    BENGALI(0x09E6, 0x0980, 0x09FF),
    GURMUKHI(0x0A66, 0x0A00, 0x0A7F),
    GUJARATI(0x0AE6, 0x0A80, 0x0AFF),
    ORIYA(0x0B66, 0x0B00, 0x0B7F),
    TAMIL(0x0BE6, 0x0B80, 0x0BFF),
    TELUGU(0x0C66, 0x0C00, 0x0C7F),
    KANNADA(0x0CE6, 0x0C80, 0x0CFF),
    MALAYALAM(0x0D66, 0x0D00, 0x0D7F),
    THAI(0x0E50, 0x0E00, 0x0E7F),
    LAO(0x0ED0, 0x0E80, 0x0EFF),
    TIBETAN(0x0F20, 0x0F00, 0x0FFF),
    MYANMAR(0x1040, 0x1000, 0x109F),
    ETHIOPIC(0x1368, 0x1200, 0x137F),
    KHMER(0x17E0, 0x1780, 0x17FF),
    MONGOLIAN(0x1810, 0x1800, 0x18AF),
    NKO(0x07C0, 0x07C0, 0x07FF),
    MYANMAR_SHAN(0x1090, 0x1000, 0x109F),
    LIMBU(0x1946, 0x1900, 0x194F),
    NEW_TAI_LUE(0x19D0, 0x1980, 0x19DF),
    BALINESE(0x1B50, 0x1B00, 0x1B7F),
    SUNDANESE(0x1BB0, 0x1B80, 0x1BBF),
    LEPCHA(0x1C40, 0x1C00, 0x1C4F),
    OL_CHIKI(0x1C50, 0x1C50, 0x1C7F),
    VAI(0xA620, 0xA500, 0xA62B),
    SAURASHTRA(0xA8D0, 0xA880, 0xA8DF),
    KAYAH_LI(0xA900, 0xA900, 0xA92F),
    CHAM(0xAA50, 0xAA00, 0xAA5F),
    TAI_THAM_HORA(0x1A80, 0x1A20, 0x1AAF),
    TAI_THAM_THAM(0x1A90, 0x1A20, 0x1AAF),
    JAVANESE(0xA9D0, 0xA980, 0xA9DF),
    MEETEI_MAYEK(0xABF0, 0xABC0, 0xABFF),
    SINHALA(0x0DE6, 0x0D80, 0x0DFF),
    MYANMAR_TAI_LAING(0xA9F0, 0xA9E0, 0xA9FF);

    private final int base;
    private final int strongLow;
    private final int strongHigh;

    Range(int base, int strongLow, int strongHigh) {
      this.base = base;
      this.strongLow = strongLow;
      this.strongHigh = strongHigh;
    }

    int digitBase() { return base; }

    boolean isStrong(int cp) {
      return this != EUROPEAN && cp >= strongLow && cp <= strongHigh && Character.isLetter(cp);
    }
  }

  private static final int kLegacyBits = 19;

  private final Set<Range> rangeSet;
  private final Range defaultContext;
  private final boolean contextual;

  private NumericShaper(Set<Range> ranges, Range defaultContext, boolean contextual) {
    this.rangeSet = ranges;
    this.defaultContext = defaultContext;
    this.contextual = contextual;
  }

  private static Set<Range> toSet(int mask) {
    Set<Range> s = EnumSet.noneOf(Range.class);
    Range[] all = Range.values();
    for (int i = 0; i < kLegacyBits; i++) {
      if ((mask & (1 << i)) != 0) {
        s.add(all[i]);
      }
    }
    return s;
  }

  public static NumericShaper getShaper(int singleRange) {
    Set<Range> s = toSet(singleRange);
    return new NumericShaper(s, Range.EUROPEAN, false);
  }

  public static NumericShaper getShaper(Range singleRange) {
    return new NumericShaper(EnumSet.of(singleRange), Range.EUROPEAN, false);
  }

  public static NumericShaper getContextualShaper(int ranges) {
    return new NumericShaper(toSet(ranges), Range.EUROPEAN, true);
  }

  public static NumericShaper getContextualShaper(int ranges, int defaultContext) {
    Set<Range> s = toSet(ranges);
    Range ctx = Range.EUROPEAN;
    Range[] all = Range.values();
    for (int i = 0; i < kLegacyBits; i++) {
      if (defaultContext == (1 << i)) {
        ctx = all[i];
      }
    }
    return new NumericShaper(s, ctx, true);
  }

  public static NumericShaper getContextualShaper(Set<Range> ranges) {
    Set<Range> s = EnumSet.noneOf(Range.class);
    s.addAll(ranges);
    return new NumericShaper(s, Range.EUROPEAN, true);
  }

  public static NumericShaper getContextualShaper(Set<Range> ranges, Range defaultContext) {
    if (defaultContext == null) {
      throw new NullPointerException();
    }
    Set<Range> s = EnumSet.noneOf(Range.class);
    s.addAll(ranges);
    return new NumericShaper(s, defaultContext, true);
  }

  public void shape(char[] text, int start, int count) {
    checkParams(text, start, count);
    if (contextual) {
      shapeContextually(text, start, count, defaultContext);
    } else {
      shapeNonContextually(text, start, count);
    }
  }

  public void shape(char[] text, int start, int count, int context) {
    checkParams(text, start, count);
    if (contextual) {
      Range ctx = Range.EUROPEAN;
      Range[] all = Range.values();
      boolean found = false;
      for (int i = 0; i < kLegacyBits; i++) {
        if (context == (1 << i)) {
          ctx = all[i];
          found = true;
        }
      }
      if (!found) {
        throw new IllegalArgumentException("invalid context");
      }
      shapeContextually(text, start, count, ctx);
    } else {
      shapeNonContextually(text, start, count);
    }
  }

  public void shape(char[] text, int start, int count, Range context) {
    checkParams(text, start, count);
    if (context == null) {
      throw new NullPointerException("context is null");
    }
    if (contextual) {
      shapeContextually(text, start, count, context);
    } else {
      shapeNonContextually(text, start, count);
    }
  }

  private static void checkParams(char[] text, int start, int count) {
    if (text == null) {
      throw new NullPointerException("text is null");
    }
    if ((start < 0) || (start > text.length) || ((start + count) < 0) || ((start + count) > text.length)) {
      throw new IndexOutOfBoundsException("bad start or count for text of length " + text.length);
    }
  }

  private void shapeNonContextually(char[] text, int start, int count) {
    Range target = rangeSet.isEmpty() ? Range.EUROPEAN : rangeSet.iterator().next();
    int end = start + count;
    for (int i = start; i < end; i++) {
      char c = text[i];
      if (c >= '0' && c <= '9') {
        text[i] = convert(c, target);
      }
    }
  }

  private void shapeContextually(char[] text, int start, int count, Range ctx) {
    Range key = ctx;
    if (!rangeSet.contains(key) && key != Range.EUROPEAN) {
      key = Range.EUROPEAN;
    }
    int end = start + count;
    for (int i = start; i < end; i++) {
      char c = text[i];
      if (c >= '0' && c <= '9') {
        if (key != Range.EUROPEAN) {
          text[i] = convert(c, key);
        }
      } else {
        Range found = null;
        for (Range r : rangeSet) {
          if (r.isStrong(c)) {
            found = r;
            break;
          }
        }
        if (found != null) {
          key = found;
        } else if (Character.isLetter(c) && Character.getDirectionality(c) == Character.DIRECTIONALITY_LEFT_TO_RIGHT) {
          key = Range.EUROPEAN;
        }
      }
    }
  }

  private static char convert(char c, Range target) {
    if (target == Range.EUROPEAN) {
      return c;
    }
    if (target == Range.ETHIOPIC) {
      return c == '0' ? c : (char) (0x1368 + (c - '0'));
    }
    return (char) (target.digitBase() + (c - '0'));
  }

  public boolean isContextual() { return contextual; }

  public int getRanges() {
    int mask = 0;
    for (Range r : rangeSet) {
      if (r.ordinal() < kLegacyBits) {
        mask |= 1 << r.ordinal();
      }
    }
    return mask;
  }

  public Set<Range> getRangeSet() { return Collections.unmodifiableSet(EnumSet.copyOf(rangeSet.isEmpty() ? EnumSet.noneOf(Range.class) : rangeSet)); }

  public int hashCode() {
    int hash = rangeSet.hashCode();
    if (contextual) {
      hash ^= (1 << 30) ^ defaultContext.hashCode();
    }
    return hash;
  }

  public boolean equals(Object o) {
    if (o != null) {
      try {
        NumericShaper rhs = (NumericShaper) o;
        return rhs.contextual == contextual && rhs.defaultContext == defaultContext && rhs.rangeSet.equals(rangeSet);
      } catch (ClassCastException e) {
        // not equal
      }
    }
    return false;
  }

  public String toString() {
    StringBuilder buf = new StringBuilder(super.toString());
    buf.append("[contextual:").append(contextual);
    buf.append(", ranges:").append(rangeSet);
    if (contextual) {
      buf.append(", default context:").append(defaultContext);
    }
    buf.append(']');
    return buf.toString();
  }
}
