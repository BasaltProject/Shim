/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.regex;

/** Extended grapheme cluster boundaries (UAX #29, rules GB3 to GB13). */
final class GraphemeBreak {
  private static final int OTHER = 0, CR = 1, LF = 2, CONTROL = 3, EXTEND = 4, ZWJ = 5, RI = 6, PREPEND = 7,
      SPACING = 8, L = 9, V = 10, T = 11, LV = 12, LVT = 13, PICT = 14;

  private GraphemeBreak() {
  }

  private static int category(int cp) {
    if (cp == '\r') {
      return CR;
    }
    if (cp == '\n') {
      return LF;
    }
    if (cp == 0x200d) {
      return ZWJ;
    }
    if (cp >= 0x1f1e6 && cp <= 0x1f1ff) {
      return RI;
    }
    if ((cp >= 0x1100 && cp <= 0x115f) || (cp >= 0xa960 && cp <= 0xa97c)) {
      return L;
    }
    if ((cp >= 0x1160 && cp <= 0x11a7) || (cp >= 0xd7b0 && cp <= 0xd7c6)) {
      return V;
    }
    if ((cp >= 0x11a8 && cp <= 0x11ff) || (cp >= 0xd7cb && cp <= 0xd7fb)) {
      return T;
    }
    if (cp >= 0xac00 && cp <= 0xd7a3) {
      return (cp - 0xac00) % 28 == 0 ? LV : LVT;
    }
    int t = Character.getType(cp);
    if (cp == 0x200c || (cp >= 0xff9e && cp <= 0xff9f) || (cp >= 0x1f3fb && cp <= 0x1f3ff)
        || (cp >= 0xe0020 && cp <= 0xe007f) || t == Character.NON_SPACING_MARK || t == Character.ENCLOSING_MARK) {
      return EXTEND;
    }
    if ((cp >= 0x600 && cp <= 0x605) || cp == 0x6dd || cp == 0x70f || cp == 0x890 || cp == 0x891 || cp == 0x8e2
        || cp == 0xd4e || cp == 0x110bd || cp == 0x110cd || cp == 0x111c2 || cp == 0x111c3 || cp == 0x1193f
        || cp == 0x11941 || cp == 0x11a3a || (cp >= 0x11a84 && cp <= 0x11a89) || cp == 0x11d46 || cp == 0x11f02) {
      return PREPEND;
    }
    if (t == Character.CONTROL || t == Character.LINE_SEPARATOR || t == Character.PARAGRAPH_SEPARATOR
        || t == Character.FORMAT || (t == Character.UNASSIGNED && cp >= 0xe0000 && cp <= 0xe0fff)) {
      return CONTROL;
    }
    if (t == Character.COMBINING_SPACING_MARK) {
      return SPACING;
    }
    if (Character.isExtendedPictographic(cp)) {
      return PICT;
    }
    return OTHER;
  }

  /** Returns the end index of the cluster starting at start. */
  static int next(CharSequence s, int start, int limit) {
    int cp = Character.codePointAt(s, start);
    int i = start + Character.charCount(cp);
    int prev = category(cp);
    int riCount = prev == RI ? 1 : 0;
    boolean pictSeq = prev == PICT;
    boolean pictZwj = false;
    while (i < limit) {
      int c = Character.codePointAt(s, i);
      if (c > 0xffff && i + 2 > limit) {
        c = s.charAt(i);
      }
      int cur = category(c);
      boolean noBreak;
      if (prev == CR && cur == LF) {
        noBreak = true;
      } else if (prev == CR || prev == LF || prev == CONTROL || cur == CR || cur == LF || cur == CONTROL) {
        noBreak = false;
      } else if (prev == L && (cur == L || cur == V || cur == LV || cur == LVT)) {
        noBreak = true;
      } else if ((prev == LV || prev == V) && (cur == V || cur == T)) {
        noBreak = true;
      } else if ((prev == LVT || prev == T) && cur == T) {
        noBreak = true;
      } else if (cur == EXTEND || cur == ZWJ || cur == SPACING) {
        noBreak = true;
      } else if (prev == PREPEND) {
        noBreak = true;
      } else if (pictZwj && cur == PICT) {
        noBreak = true;
      } else if (prev == RI && cur == RI) {
        noBreak = riCount % 2 == 1;
      } else {
        noBreak = false;
      }
      if (!noBreak) {
        break;
      }
      if (cur == PICT) {
        pictSeq = true;
        pictZwj = false;
      } else if (cur == ZWJ) {
        pictZwj = pictSeq;
      } else if (cur == EXTEND) {
        pictZwj = false;
      } else {
        pictSeq = false;
        pictZwj = false;
      }
      riCount = cur == RI ? riCount + 1 : 0;
      prev = cur;
      i += Character.charCount(c);
    }
    return i;
  }

  static boolean isBoundary(CharSequence s, int pos, int lo, int hi) {
    int i = lo;
    while (i < pos) {
      i = next(s, i, hi);
    }
    return i == pos;
  }
}
