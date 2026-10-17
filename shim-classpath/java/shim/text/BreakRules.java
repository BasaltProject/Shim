/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.text;

import java.util.ArrayList;
import java.util.List;

public final class BreakRules {
  private BreakRules() {
  }

  public static int[] chars(String s) {
    List<Integer> out = new ArrayList<Integer>();
    int n = s.length();
    out.add(0);
    int i = 0;
    while (i < n) {
      int j = clusterEnd(s, i);
      out.add(j);
      i = j;
    }
    return toArray(out, n);
  }

  private static boolean isExtend(int cp) {
    int t = Character.getType(cp);
    return t == Character.NON_SPACING_MARK || t == Character.ENCLOSING_MARK || t == Character.COMBINING_SPACING_MARK
        || cp == 0x200c || cp == 0x200d;
  }

  private static int hangulKind(int cp) {
    if (cp >= 0x1100 && cp <= 0x115f) {
      return 1;
    }
    if (cp >= 0x1160 && cp <= 0x11a7) {
      return 2;
    }
    if (cp >= 0x11a8 && cp <= 0x11ff) {
      return 3;
    }
    if (cp >= 0xac00 && cp <= 0xd7a3) {
      return (cp - 0xac00) % 28 == 0 ? 4 : 5;
    }
    return 0;
  }

  private static int clusterEnd(String s, int i) {
    int n = s.length();
    int cp = s.codePointAt(i);
    int j = i + Character.charCount(cp);
    if (cp == '\r') {
      return j < n && s.charAt(j) == '\n' ? j + 1 : j;
    }
    int t = Character.getType(cp);
    if (cp == '\n' || t == Character.CONTROL || t == Character.LINE_SEPARATOR
        || t == Character.PARAGRAPH_SEPARATOR) {
      return j;
    }
    int prev = hangulKind(cp);
    while (j < n) {
      int next = s.codePointAt(j);
      int nk = hangulKind(next);
      boolean join = isExtend(next);
      if (!join && prev != 0 && nk != 0) {
        join = (prev == 1 && (nk == 1 || nk == 2 || nk == 4 || nk == 5))
            || ((prev == 2 || prev == 4) && (nk == 2 || nk == 3))
            || ((prev == 3 || prev == 5) && nk == 3);
      }
      if (!join) {
        break;
      }
      if (nk != 0) {
        prev = nk;
      }
      j += Character.charCount(next);
    }
    return j;
  }

  private static int[] toArray(List<Integer> l, int n) {
    int[] a = new int[l.size()];
    for (int i = 0; i < a.length; i++) {
      a[i] = l.get(i);
    }
    if (n == 0) {
      return new int[] {0};
    }
    return a;
  }

  private static boolean isHan(int cp) {
    return (cp >= 0x4e00 && cp <= 0x9fff) || (cp >= 0x3400 && cp <= 0x4dbf) || (cp >= 0xf900 && cp <= 0xfaff)
        || (cp >= 0x20000 && cp <= 0x2ffff) || cp == 0x3005 || cp == 0x3007;
  }

  private static boolean isHiragana(int cp) {
    return cp >= 0x3041 && cp <= 0x309f;
  }

  private static boolean isKatakana(int cp) {
    return (cp >= 0x30a0 && cp <= 0x30ff) || (cp >= 0x31f0 && cp <= 0x31ff) || (cp >= 0xff66 && cp <= 0xff9f);
  }

  private static int wordClass(int cp) {
    if (isHan(cp)) {
      return 1;
    }
    if (isHiragana(cp)) {
      return 2;
    }
    if (isKatakana(cp)) {
      return 3;
    }
    int t = Character.getType(cp);
    if (t == Character.DECIMAL_DIGIT_NUMBER) {
      return 5;
    }
    if (Character.isLetter(cp) || t == Character.LETTER_NUMBER) {
      return 4;
    }
    return 0;
  }

  private static boolean isWordSpace(int cp) {
    return cp == ' ' || cp == '\t' || cp == 0xa0 || (Character.getType(cp) == Character.SPACE_SEPARATOR
        && cp != 0x3000);
  }

  public static int[] words(String s) {
    List<Integer> out = new ArrayList<Integer>();
    int n = s.length();
    out.add(0);
    int i = 0;
    while (i < n) {
      int j = wordEnd(s, i);
      out.add(j);
      i = j;
    }
    return toArray(out, n);
  }

  private static int skipMarks(String s, int j) {
    int n = s.length();
    while (j < n) {
      int cp = s.codePointAt(j);
      if (!isExtend(cp)) {
        break;
      }
      j += Character.charCount(cp);
    }
    return j;
  }

  private static boolean isCurrencyPrefix(int cp) {
    return Character.getType(cp) == Character.CURRENCY_SYMBOL;
  }

  private static int wordEnd(String s, int i) {
    int n = s.length();
    int cp = s.codePointAt(i);
    int j = i + Character.charCount(cp);
    if (cp == '\r') {
      return j < n && s.charAt(j) == '\n' ? j + 1 : j;
    }
    if (isWordSpace(cp)) {
      while (j < n && isWordSpace(s.codePointAt(j))) {
        j += Character.charCount(s.codePointAt(j));
      }
      return j;
    }
    int cls = wordClass(cp);
    boolean number = false;
    if (cls == 0 && isCurrencyPrefix(cp) && j < n && wordClass(s.codePointAt(j)) == 5) {
      cls = 5;
      number = true;
    } else if (cls == 0 && cp == '.' && j < n && wordClass(s.codePointAt(j)) == 5) {
      cls = 5;
      number = true;
    }
    if (cls == 0) {
      return skipMarks(s, j);
    }
    if (cls == 1 || cls == 2 || cls == 3) {
      j = skipMarks(s, j);
      while (j < n) {
        int c = s.codePointAt(j);
        int k = wordClass(c);
        boolean same = k == cls || (cls == 3 && c == 0x30fc) || (cls == 2 && c == 0x30fc);
        if (!same) {
          break;
        }
        j = skipMarks(s, j + Character.charCount(c));
      }
      return j;
    }
    j = skipMarks(s, j);
    int last = number ? 5 : cls;
    while (j < n) {
      int c = s.codePointAt(j);
      int k = wordClass(c);
      if (k == 4 || k == 5) {
        last = k;
        j = skipMarks(s, j + Character.charCount(c));
        continue;
      }
      if (j + Character.charCount(c) < n) {
        int nx = s.codePointAt(j + Character.charCount(c));
        int nk = wordClass(nx);
        if (last == 4 && nk == 4 && (c == '\'' || c == '"' || c == '.' || c == '-' || c == 0x2010 || c == 0x2013 || c == '_')) {
          j += 1;
          continue;
        }
        if (last == 5 && nk == 5 && (c == ',' || c == '.')) {
          j += 1;
          continue;
        }
      }
      if (last == 5 && c == '%' ) {
        j += 1;
      }
      break;
    }
    return j;
  }

  private static boolean isSentenceCloser(int cp) {
    if (cp == '"' || cp == '\'' || cp == ')' || cp == ']' || cp == '}') {
      return true;
    }
    int t = Character.getType(cp);
    return t == Character.END_PUNCTUATION || t == Character.FINAL_QUOTE_PUNCTUATION;
  }

  private static boolean isOpener(int cp) {
    int t = Character.getType(cp);
    return cp == '"' || cp == '\'' || t == Character.START_PUNCTUATION || t == Character.INITIAL_QUOTE_PUNCTUATION;
  }

  public static int[] sentences(String s) {
    List<Integer> out = new ArrayList<Integer>();
    int n = s.length();
    out.add(0);
    int i = 0;
    while (i < n) {
      int cp = s.codePointAt(i);
      int j = i + Character.charCount(cp);
      if (cp == 0x2029) {
        out.add(j);
        i = j;
        continue;
      }
      boolean strong = cp == '!' || cp == '?' || cp == 0x3002 || cp == 0x0964 || cp == 0xff01 || cp == 0xff1f;
      boolean period = cp == '.';
      if (!strong && !period) {
        i = j;
        continue;
      }
      while (j < n && (s.charAt(j) == '.' || s.charAt(j) == '!' || s.charAt(j) == '?')) {
        if (s.charAt(j) != '.') {
          strong = true;
        }
        j++;
      }
      int afterTerm = j;
      while (j < n && isSentenceCloser(s.codePointAt(j))) {
        j += Character.charCount(s.codePointAt(j));
      }
      boolean closed = j > afterTerm;
      int k = j;
      boolean sawSpace = false;
      boolean sawNewline = false;
      while (k < n) {
        int c = s.codePointAt(k);
        if (c == '\n' || c == '\r' || c == 0x85) {
          sawNewline = true;
        } else if (!(c == ' ' || c == '\t' || Character.getType(c) == Character.SPACE_SEPARATOR)) {
          break;
        }
        sawSpace = true;
        k += Character.charCount(c);
      }
      boolean brk;
      if (strong && (cp == '!' || cp == '?' || cp == 0xff01 || cp == 0xff1f || sawSpace || closed || true)) {
        brk = true;
        if (cp == 0x3002 || cp == 0x0964) {
          brk = sawSpace || true;
        }
      } else {
        brk = false;
      }
      if (!strong) {
        if (k >= n) {
          brk = true;
        } else if (sawNewline) {
          brk = true;
        } else if (sawSpace || closed) {
          int m = k;
          while (m < n && isOpener(s.codePointAt(m))) {
            m += Character.charCount(s.codePointAt(m));
          }
          brk = m < n && (Character.isUpperCase(s.codePointAt(m)) || Character.isTitleCase(s.codePointAt(m)));
        } else {
          brk = false;
        }
      }
      if (!strong && !brk) {
        i = Math.max(j, i + 1);
        continue;
      }
      if (cp == 0x3002 || cp == 0x0964) {
        int e = sawSpace ? k : j;
        out.add(e);
        i = e;
        continue;
      }
      out.add(k);
      i = k;
    }
    if (out.get(out.size() - 1) != n && n > 0) {
      out.add(n);
    }
    List<Integer> clean = new ArrayList<Integer>();
    for (int v : out) {
      if (clean.isEmpty() || clean.get(clean.size() - 1) < v) {
        clean.add(v);
      }
    }
    return toArray(clean, n);
  }

  private static boolean isLineSpace(int cp) {
    return cp == ' ' || cp == '\t' || cp == 0x3000 || (cp >= 0x2000 && cp <= 0x200a && cp != 0x2007)
        || cp == 0x1680 || cp == 0x205f;
  }

  private static boolean isLineBreakChar(int cp) {
    return cp == '\n' || cp == '\r' || cp == 0x2028 || cp == 0x2029 || cp == 0x85 || cp == 0x0b || cp == 0x0c;
  }

  private static boolean isNbsp(int cp) {
    return cp == 0xa0 || cp == 0x2007 || cp == 0x202f || cp == 0x2060 || cp == 0x200b;
  }

  private static boolean isCjk(int cp) {
    return isHan(cp) || isHiragana(cp) || isKatakana(cp) || (cp >= 0xac00 && cp <= 0xd7a3);
  }

  private static boolean isNoBreakBefore(int cp) {
    if (cp == 0x3001 || cp == 0x3002 || cp == 0xff0c || cp == 0xff0e || cp == 0x30fc || cp == 0x30fb
        || cp == 0xff1a || cp == 0xff1b || cp == 0xff1f || cp == 0xff01) {
      return true;
    }
    String small = "\u3041\u3043\u3045\u3047\u3049\u3063\u3083\u3085\u3087\u308e\u30a1\u30a3\u30a5\u30a7\u30a9\u30c3"
        + "\u30e3\u30e5\u30e7\u30ee\u30f5\u30f6\u309d\u309e\u30fd\u30fe";
    if (small.indexOf(cp) >= 0) {
      return true;
    }
    int t = Character.getType(cp);
    return t == Character.END_PUNCTUATION || t == Character.FINAL_QUOTE_PUNCTUATION;
  }

  private static boolean isBreakAfterChar(int cp) {
    return cp == '-' || cp == 0x2010 || cp == 0x2013 || cp == 0xad || cp == ',' || cp == ';' || cp == ':'
        || cp == '!' || cp == '?' || cp == '%' || cp == '.' || cp == 0x3001 || cp == 0x3002 || cp == 0xff0c
        || cp == 0xff0e || cp == 0x2014 || cp == 0x2019 || cp == 0x201d || cp == '|' && false;
  }

  public static int[] lines(String s) {
    int n = s.length();
    boolean[] b = new boolean[n + 1];
    int[] cps = new int[n];
    int[] starts = new int[n];
    int count = 0;
    for (int i = 0; i < n; ) {
      int cp = s.codePointAt(i);
      cps[count] = cp;
      starts[count] = i;
      count++;
      i += Character.charCount(cp);
    }
    for (int x = 0; x < count; x++) {
      int cp = cps[x];
      int end = x + 1 < count ? starts[x + 1] : n;
      boolean last = x + 1 >= count;
      int next = last ? -1 : cps[x + 1];
      int prev = x > 0 ? cps[x - 1] : -1;
      if (last) {
        continue;
      }
      if (isExtend(next)) {
        continue;
      }
      boolean brk = false;
      if (isLineBreakChar(cp)) {
        brk = !(cp == '\r' && next == '\n');
      } else if (cp == '\t') {
        brk = !isLineBreakChar(next);
      } else if (isLineSpace(cp)) {
        brk = !isLineSpace(next) && !isLineBreakChar(next) && !isNbsp(next);
        if (isLineBreakChar(next)) {
          brk = false;
        }
      } else if (isLineSpace(next) || isLineBreakChar(next) || isNbsp(cp)) {
        brk = false;
      } else if (isCjk(cp) || isCjk(next)) {
        brk = !isNoBreakBefore(next) && !(Character.getType(cp) == Character.START_PUNCTUATION)
            && !(Character.getType(cp) == Character.INITIAL_QUOTE_PUNCTUATION);
        if (cp == '"' && !isCjk(next)) {
          brk = true;
        }
      } else if (cp == '"') {
        boolean opening = prev < 0 || isLineSpace(prev) || isLineBreakChar(prev);
        brk = !opening;
      } else if (isBreakAfterChar(cp)) {
        boolean digitsAround = prev >= 0 && Character.isDigit(prev) && Character.isDigit(next);
        if ((cp == ',' || cp == '.') && digitsAround) {
          brk = false;
        } else if (cp == '%' && Character.isDigit(prev)) {
          brk = !(Character.isLetterOrDigit(next) && false) && !isBreakAfterChar(next);
        } else {
          brk = !isBreakAfterChar(next) && next != '"';
        }
      } else if (Character.getType(next) == Character.START_PUNCTUATION && Character.getType(cp) != Character.START_PUNCTUATION) {
        brk = true;
      } else if (Character.getType(cp) == Character.END_PUNCTUATION && (Character.isLetterOrDigit(next))) {
        brk = true;
      }
      b[end] = brk;
    }
    List<Integer> out = new ArrayList<Integer>();
    out.add(0);
    for (int i = 1; i < n; i++) {
      if (b[i]) {
        out.add(i);
      }
    }
    if (n > 0) {
      out.add(n);
    }
    return toArray(out, n);
  }
}
