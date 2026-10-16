/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util.regex;

final class Nodes {
  private Nodes() { }

  static final int kInfinity = Integer.MAX_VALUE;
  static final int kGreedy = 0;
  static final int kLazy = 1;
  static final int kPossessive = 2;

  abstract static class Node {
    Node next;

    abstract boolean match(Matcher m, int i, CharSequence seq);
  }

  static boolean isHigh(char c) {
    return c >= '\uD800' && c <= '\uDBFF';
  }

  static boolean isLow(char c) {
    return c >= '\uDC00' && c <= '\uDFFF';
  }

  static int cpAt(CharSequence seq, int i, int hi) {
    char c = seq.charAt(i);
    if (isHigh(c) && i + 1 < hi) {
      char d = seq.charAt(i + 1);
      if (isLow(d)) {
        return Character.toCodePoint(c, d);
      }
    }
    return c;
  }

  static int cpBefore(CharSequence seq, int i, int lo) {
    char c = seq.charAt(i - 1);
    if (isLow(c) && i - 2 >= lo) {
      char d = seq.charAt(i - 2);
      if (isHigh(d)) {
        return Character.toCodePoint(d, c);
      }
    }
    return c;
  }

  static final class Accept extends Node {
    @Override
    boolean match(Matcher m, int i, CharSequence seq) {
      if (m.acceptMode == Matcher.kEndAnchor && i != m.to) {
        m.hitEnd = i == m.to;
        return false;
      }
      m.last = i;
      m.groups[0] = m.first;
      m.groups[1] = i;
      return true;
    }
  }

  static final class Pass extends Node {
    @Override
    boolean match(Matcher m, int i, CharSequence seq) {
      return next.match(m, i, seq);
    }
  }

  static final class SubEnd extends Node {
    @Override
    boolean match(Matcher m, int i, CharSequence seq) {
      m.subEnd = i;
      return true;
    }
  }

  static final class BehindEnd extends Node {
    @Override
    boolean match(Matcher m, int i, CharSequence seq) {
      return i == m.behindTarget;
    }
  }

  static final class CharNode extends Node {
    final CharPredicate p;

    CharNode(CharPredicate p) {
      this.p = p;
    }

    @Override
    boolean match(Matcher m, int i, CharSequence seq) {
      if (i < m.hi) {
        int ch = cpAt(seq, i, m.hi);
        if (p.is(ch)) {
          return next.match(m, i + Character.charCount(ch), seq);
        }
        return false;
      }
      m.hitEnd = true;
      return false;
    }
  }

  static int fold(int c, boolean unicode) {
    if (c < 128) {
      return c >= 'A' && c <= 'Z' ? c + 32 : c;
    }
    if (unicode) {
      return Character.toLowerCase(Character.toUpperCase(c));
    }
    return c;
  }

  static final class Slice extends Node {
    final int[] cps;
    final boolean ci;
    final boolean unicode;
    final int units;

    Slice(int[] cps, boolean ci, boolean unicode) {
      this.cps = cps;
      this.ci = ci;
      this.unicode = unicode;
      int n = 0;
      for (int c : cps) {
        n += Character.charCount(c);
      }
      this.units = n;
    }

    @Override
    boolean match(Matcher m, int i, CharSequence seq) {
      int j = i;
      for (int k = 0; k < cps.length; k++) {
        if (j >= m.hi) {
          m.hitEnd = true;
          return false;
        }
        int c = cpAt(seq, j, m.hi);
        int want = cps[k];
        if (c != want) {
          if (!ci || fold(c, unicode) != fold(want, unicode)) {
            return false;
          }
        }
        j += Character.charCount(c);
      }
      return next.match(m, j, seq);
    }
  }

  static final class Begin extends Node {
    @Override
    boolean match(Matcher m, int i, CharSequence seq) {
      int fromIndex = m.anchoringBounds ? m.from : 0;
      return i == fromIndex && next.match(m, i, seq);
    }
  }

  static boolean isLineTerminator(int c, boolean unix) {
    if (unix) {
      return c == '\n';
    }
    return c == '\n' || c == '\r' || c == 0x85 || c == 0x2028 || c == 0x2029;
  }

  static final class LastMatch extends Node {
    @Override
    boolean match(Matcher m, int i, CharSequence seq) {
      int ref = m.oldLast < 0 ? m.from : m.oldLast;
      return i == ref && next.match(m, i, seq);
    }
  }

  static final class Caret extends Node {
    final boolean multiline;
    final boolean unix;

    Caret(boolean multiline, boolean unix) {
      this.multiline = multiline;
      this.unix = unix;
    }

    @Override
    boolean match(Matcher m, int i, CharSequence seq) {
      int startIndex = m.anchoringBounds ? m.from : 0;
      int endIndex = m.anchoringBounds ? m.to : m.textLength();
      if (!multiline) {
        return i == startIndex && next.match(m, i, seq);
      }
      if (i == startIndex) {
        return next.match(m, i, seq);
      }
      if (i > startIndex && i < endIndex) {
        char ch = seq.charAt(i - 1);
        if (isLineTerminator(ch, unix)) {
          if (ch == '\r' && seq.charAt(i) == '\n' && !unix) {
            return false;
          }
          return next.match(m, i, seq);
        }
      }
      return false;
    }
  }

  static final class Dollar extends Node {
    final boolean multiline;
    final boolean unix;

    Dollar(boolean multiline, boolean unix) {
      this.multiline = multiline;
      this.unix = unix;
    }

    @Override
    boolean match(Matcher m, int i, CharSequence seq) {
      int endIndex = m.anchoringBounds ? m.to : m.textLength();
      if (i == endIndex) {
        m.hitEnd = true;
        m.requireEnd = true;
        return next.match(m, i, seq);
      }
      if (i > endIndex) {
        return false;
      }
      char ch = seq.charAt(i);
      if (!isLineTerminator(ch, unix)) {
        return false;
      }
      if (ch == '\n' && !unix && i > 0 && seq.charAt(i - 1) == '\r') {
        return false;
      }
      if (multiline) {
        return next.match(m, i, seq);
      }
      if (i == endIndex - 1) {
        m.hitEnd = true;
        m.requireEnd = true;
        return next.match(m, i, seq);
      }
      if (!unix && ch == '\r' && i == endIndex - 2 && seq.charAt(i + 1) == '\n') {
        m.hitEnd = true;
        m.requireEnd = true;
        return next.match(m, i, seq);
      }
      return false;
    }
  }

  static final class AbsEnd extends Node {
    @Override
    boolean match(Matcher m, int i, CharSequence seq) {
      int endIndex = m.anchoringBounds ? m.to : m.textLength();
      if (i == endIndex) {
        m.hitEnd = true;
        m.requireEnd = true;
        return next.match(m, i, seq);
      }
      return false;
    }
  }

  static boolean isWordChar(int c, boolean unicode) {
    if (unicode) {
      return RegexProperties.kUnicodeWord.is(c);
    }
    return Character.isLetterOrDigit(c) || c == '_';
  }

  static final class Bound extends Node {
    final boolean negate;
    final boolean unicode;

    Bound(boolean negate, boolean unicode) {
      this.negate = negate;
      this.unicode = unicode;
    }

    @Override
    boolean match(Matcher m, int i, CharSequence seq) {
      int startIndex = m.transparentBounds ? 0 : m.from;
      int endIndex = m.transparentBounds ? m.textLength() : m.to;
      boolean left = i > startIndex && isWordChar(cpBefore(seq, i, startIndex), unicode);
      boolean right = false;
      if (i < endIndex) {
        right = isWordChar(cpAt(seq, i, endIndex), unicode);
      } else {
        m.hitEnd = true;
        m.requireEnd = true;
      }
      boolean atBoundary = left != right;
      return (atBoundary != negate) && next.match(m, i, seq);
    }
  }

  static final class GraphemeBound extends Node {
    @Override
    boolean match(Matcher m, int i, CharSequence seq) {
      int lo = m.transparentBounds ? 0 : m.from;
      int hiBound = m.transparentBounds ? m.textLength() : m.to;
      if (i <= lo || i >= hiBound) {
        return next.match(m, i, seq);
      }
      int prev = cpBefore(seq, i, lo);
      int cur = cpAt(seq, i, hiBound);
      if (prev == '\r' && cur == '\n') {
        return false;
      }
      int t = Character.getType(cur);
      boolean extend = t == Character.NON_SPACING_MARK || t == Character.ENCLOSING_MARK
          || t == Character.COMBINING_SPACING_MARK || cur == 0x200d || (cur >= 0xfe00 && cur <= 0xfe0f)
          || (cur >= 0x1f3fb && cur <= 0x1f3ff);
      if (extend || prev == 0x200d) {
        return false;
      }
      return next.match(m, i, seq);
    }
  }

  static final class LineEnding extends Node {
    @Override
    boolean match(Matcher m, int i, CharSequence seq) {
      if (i < m.hi) {
        char ch = seq.charAt(i);
        if (ch == '\r') {
          if (i + 1 < m.hi && seq.charAt(i + 1) == '\n') {
            return next.match(m, i + 2, seq);
          }
          return next.match(m, i + 1, seq);
        }
        if (ch == '\n' || ch == 0x0b || ch == 0x0c || ch == 0x85 || ch == 0x2028 || ch == 0x2029) {
          return next.match(m, i + 1, seq);
        }
        return false;
      }
      m.hitEnd = true;
      return false;
    }
  }

  static final class Grapheme extends Node {
    @Override
    boolean match(Matcher m, int i, CharSequence seq) {
      if (i >= m.hi) {
        m.hitEnd = true;
        return false;
      }
      int j = i;
      int c = cpAt(seq, j, m.hi);
      j += Character.charCount(c);
      if (c == '\r' && j < m.hi && seq.charAt(j) == '\n') {
        return next.match(m, j + 1, seq);
      }
      for (;;) {
        while (j < m.hi) {
          int d = cpAt(seq, j, m.hi);
          int t = Character.getType(d);
          if (t == Character.NON_SPACING_MARK || t == Character.ENCLOSING_MARK
              || t == Character.COMBINING_SPACING_MARK || d == 0x200d || (d >= 0xfe00 && d <= 0xfe0f)
              || (d >= 0x1f3fb && d <= 0x1f3ff) || (d >= 0xe0020 && d <= 0xe007f)) {
            j += Character.charCount(d);
            if (d == 0x200d && j < m.hi) {
              int e = cpAt(seq, j, m.hi);
              j += Character.charCount(e);
            }
          } else {
            break;
          }
        }
        break;
      }
      return next.match(m, j, seq);
    }
  }

  static final class Branch extends Node {
    final Node[] alts;
    final Node conn;

    Branch(Node[] alts, Node conn) {
      this.alts = alts;
      this.conn = conn;
    }

    @Override
    boolean match(Matcher m, int i, CharSequence seq) {
      for (Node alt : alts) {
        if (alt.match(m, i, seq)) {
          return true;
        }
      }
      return false;
    }
  }

  static final class BranchConn extends Node {
    @Override
    boolean match(Matcher m, int i, CharSequence seq) {
      return next.match(m, i, seq);
    }
  }

  static final class GroupHead extends Node {
    final int localIndex;

    GroupHead(int localIndex) {
      this.localIndex = localIndex;
    }

    @Override
    boolean match(Matcher m, int i, CharSequence seq) {
      int save = m.locals[localIndex];
      m.locals[localIndex] = i;
      boolean ret = next.match(m, i, seq);
      m.locals[localIndex] = save;
      return ret;
    }
  }

  static final class GroupTail extends Node {
    final int localIndex;
    final int groupIndex;

    GroupTail(int localIndex, int groupIndex) {
      this.localIndex = localIndex;
      this.groupIndex = groupIndex * 2;
    }

    @Override
    boolean match(Matcher m, int i, CharSequence seq) {
      int tmp = m.locals[localIndex];
      if (tmp >= 0) {
        int groupStart = m.groups[groupIndex];
        int groupEnd = m.groups[groupIndex + 1];
        m.groups[groupIndex] = tmp;
        m.groups[groupIndex + 1] = i;
        if (next.match(m, i, seq)) {
          return true;
        }
        m.groups[groupIndex] = groupStart;
        m.groups[groupIndex + 1] = groupEnd;
        return false;
      }
      m.groups[groupIndex] = -1;
      return next.match(m, i, seq);
    }
  }

  static final class BackRef extends Node {
    final int group;
    final boolean ci;
    final boolean unicode;

    BackRef(int group, boolean ci, boolean unicode) {
      this.group = group;
      this.ci = ci;
      this.unicode = unicode;
    }

    @Override
    boolean match(Matcher m, int i, CharSequence seq) {
      if (group * 2 + 1 >= m.groups.length) {
        return false;
      }
      int j = m.groups[group * 2];
      int k = m.groups[group * 2 + 1];
      int groupSize = k - j;
      if (j < 0) {
        return false;
      }
      if (i + groupSize > m.hi) {
        m.hitEnd = true;
        return false;
      }
      for (int index = 0; index < groupSize; index++) {
        char a = seq.charAt(i + index);
        char b = seq.charAt(j + index);
        if (a != b) {
          if (!ci || fold(a, unicode) != fold(b, unicode)) {
            return false;
          }
        }
      }
      return next.match(m, i + groupSize, seq);
    }
  }

  static final class Look extends Node {
    final Node body;
    final boolean positive;

    Look(Node body, boolean positive) {
      this.body = body;
      this.positive = positive;
    }

    @Override
    boolean match(Matcher m, int i, CharSequence seq) {
      int savedHi = m.hi;
      int[] savedGroups = m.groups.clone();
      m.hi = m.transparentBounds ? m.textLength() : m.to;
      boolean ok = body.match(m, i, seq);
      m.hi = savedHi;
      if (positive) {
        if (!ok) {
          return false;
        }
        if (next.match(m, i, seq)) {
          return true;
        }
        System.arraycopy(savedGroups, 0, m.groups, 0, savedGroups.length);
        return false;
      }
      if (ok) {
        System.arraycopy(savedGroups, 0, m.groups, 0, savedGroups.length);
        return false;
      }
      return next.match(m, i, seq);
    }
  }

  static final class Behind extends Node {
    final Node body;
    final boolean positive;
    final int minLen;
    final int maxLen;

    Behind(Node body, boolean positive, int minLen, int maxLen) {
      this.body = body;
      this.positive = positive;
      this.minLen = minLen;
      this.maxLen = maxLen;
    }

    @Override
    boolean match(Matcher m, int i, CharSequence seq) {
      int lower = m.transparentBounds ? 0 : m.from;
      int savedTarget = m.behindTarget;
      int savedHi = m.hi;
      int[] savedGroups = m.groups.clone();
      boolean found = false;
      m.behindTarget = i;
      m.hi = i;
      int from = Math.max(i - maxLen, lower);
      for (int j = i - minLen; j >= from; j--) {
        if (j < lower) {
          break;
        }
        if (body.match(m, j, seq)) {
          found = true;
          break;
        }
      }
      m.behindTarget = savedTarget;
      m.hi = savedHi;
      if (positive) {
        if (!found) {
          return false;
        }
        if (next.match(m, i, seq)) {
          return true;
        }
        System.arraycopy(savedGroups, 0, m.groups, 0, savedGroups.length);
        return false;
      }
      if (found) {
        System.arraycopy(savedGroups, 0, m.groups, 0, savedGroups.length);
        return false;
      }
      return next.match(m, i, seq);
    }
  }

  static final class Atomic extends Node {
    final Node body;

    Atomic(Node body) {
      this.body = body;
    }

    @Override
    boolean match(Matcher m, int i, CharSequence seq) {
      int savedEnd = m.subEnd;
      if (!body.match(m, i, seq)) {
        m.subEnd = savedEnd;
        return false;
      }
      int end = m.subEnd;
      m.subEnd = savedEnd;
      return next.match(m, end, seq);
    }
  }

  static final class RepeatChar extends Node {
    final CharPredicate p;
    final int min;
    final int max;
    final int type;

    RepeatChar(CharPredicate p, int min, int max, int type) {
      this.p = p;
      this.min = min;
      this.max = max;
      this.type = type;
    }

    @Override
    boolean match(Matcher m, int i, CharSequence seq) {
      int j = i;
      int count = 0;
      while (count < min) {
        if (j >= m.hi) {
          m.hitEnd = true;
          return false;
        }
        int ch = cpAt(seq, j, m.hi);
        if (!p.is(ch)) {
          return false;
        }
        j += Character.charCount(ch);
        count++;
      }
      if (type == kLazy) {
        for (;;) {
          if (next.match(m, j, seq)) {
            return true;
          }
          if (count >= max) {
            return false;
          }
          if (j >= m.hi) {
            m.hitEnd = true;
            return false;
          }
          int ch = cpAt(seq, j, m.hi);
          if (!p.is(ch)) {
            return false;
          }
          j += Character.charCount(ch);
          count++;
        }
      }
      int lowest = j;
      while (count < max) {
        if (j >= m.hi) {
          m.hitEnd = true;
          break;
        }
        int ch = cpAt(seq, j, m.hi);
        if (!p.is(ch)) {
          break;
        }
        j += Character.charCount(ch);
        count++;
      }
      if (type == kPossessive) {
        return next.match(m, j, seq);
      }
      while (true) {
        if (next.match(m, j, seq)) {
          return true;
        }
        if (j <= lowest) {
          return false;
        }
        int step = 1;
        if (j - 2 >= lowest && isLow(seq.charAt(j - 1)) && isHigh(seq.charAt(j - 2))) {
          step = 2;
        }
        j -= step;
      }
    }
  }

  static final class Loop extends Node {
    Node body;
    final int cmin;
    final int cmax;
    final int type;
    final int countIndex;
    final int beginIndex;

    Loop(int cmin, int cmax, int type, int countIndex, int beginIndex) {
      this.cmin = cmin;
      this.cmax = cmax;
      this.type = type;
      this.countIndex = countIndex;
      this.beginIndex = beginIndex;
    }

    boolean iterate(Matcher m, int i, CharSequence seq) {
      int count = m.locals[countIndex];
      int begin = m.locals[beginIndex];
      if (count > cmin - 1 && i == begin && count > 0) {
        return exit(m, i, seq);
      }
      if (count < cmin) {
        return enter(m, i, seq, count);
      }
      if (type == kLazy) {
        if (exit(m, i, seq)) {
          return true;
        }
        if (count < cmax) {
          return enter(m, i, seq, count);
        }
        return false;
      }
      if (count < cmax) {
        if (enter(m, i, seq, count)) {
          return true;
        }
      }
      return exit(m, i, seq);
    }

    private boolean enter(Matcher m, int i, CharSequence seq, int count) {
      int savedBegin = m.locals[beginIndex];
      m.locals[countIndex] = count + 1;
      m.locals[beginIndex] = i;
      boolean ret = body.match(m, i, seq);
      m.locals[countIndex] = count;
      m.locals[beginIndex] = savedBegin;
      return ret;
    }

    private boolean exit(Matcher m, int i, CharSequence seq) {
      int savedCount = m.locals[countIndex];
      int savedBegin = m.locals[beginIndex];
      boolean ret = next.match(m, i, seq);
      m.locals[countIndex] = savedCount;
      m.locals[beginIndex] = savedBegin;
      return ret;
    }

    @Override
    boolean match(Matcher m, int i, CharSequence seq) {
      return iterate(m, i, seq);
    }
  }

  static final class LoopEntry extends Node {
    final Loop loop;

    LoopEntry(Loop loop) {
      this.loop = loop;
    }

    @Override
    boolean match(Matcher m, int i, CharSequence seq) {
      int savedCount = m.locals[loop.countIndex];
      int savedBegin = m.locals[loop.beginIndex];
      m.locals[loop.countIndex] = 0;
      m.locals[loop.beginIndex] = -1;
      boolean ret = loop.iterate(m, i, seq);
      m.locals[loop.countIndex] = savedCount;
      m.locals[loop.beginIndex] = savedBegin;
      return ret;
    }
  }
}
