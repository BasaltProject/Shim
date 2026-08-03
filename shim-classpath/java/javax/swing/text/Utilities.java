/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.text;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.text.BreakIterator;

public class Utilities {
  public Utilities() { }

  public static final int drawTabbedText(Segment s, int x, int y, Graphics g, TabExpander e, int startOffset) {
    return (int) drawTabbedText(null, s, x, y, g, e, startOffset, false);
  }

  public static final float drawTabbedText(Segment s, float x, float y, Graphics2D g, TabExpander e,
      int startOffset) {
    return drawTabbedText(null, s, x, y, g, e, startOffset, false);
  }

  static final float drawTabbedText(View view, Segment s, float x, float y, Graphics g, TabExpander e,
      int startOffset, boolean useFPAPI) {
    FontMetrics metrics = g.getFontMetrics();
    float nextX = x;
    char[] txt = s.array;
    int txtOffset = s.offset;
    int flushLen = 0;
    int flushIndex = s.offset;
    int n = s.offset + s.count;
    for (int i = txtOffset; i < n; i++) {
      if (txt[i] == '\t') {
        if (flushLen > 0) {
          drawChunk(g, txt, flushIndex, flushLen, nextX, y, useFPAPI);
          nextX += metrics.charsWidth(txt, flushIndex, flushLen);
          flushLen = 0;
        }
        flushIndex = i + 1;
        if (txt[i] == '\t') {
          if (e != null) {
            nextX = e.nextTabStop(nextX, startOffset + i - txtOffset);
          } else {
            nextX += metrics.charWidth(' ');
          }
        }
      } else if (txt[i] == '\n' || txt[i] == '\r') {
        if (flushLen > 0) {
          drawChunk(g, txt, flushIndex, flushLen, nextX, y, useFPAPI);
          nextX += metrics.charsWidth(txt, flushIndex, flushLen);
          flushLen = 0;
        }
        flushIndex = i + 1;
      } else {
        flushLen += 1;
      }
    }
    if (flushLen > 0) {
      drawChunk(g, txt, flushIndex, flushLen, nextX, y, useFPAPI);
      nextX += metrics.charsWidth(txt, flushIndex, flushLen);
    }
    return nextX;
  }


  static int spaceExtra(int[] jd, int docPos, int spaceIdx) {
    if (docPos >= jd[3] && docPos < jd[4]) {
      return jd[0] + (spaceIdx < jd[1] ? 1 : 0);
    }
    return 0;
  }

  static float justifiedWidth(Segment s, FontMetrics metrics, float x, TabExpander e, int startOffset, int[] jd) {
    float nextX = x;
    int spaceIdx = jd[2];
    for (int i = s.offset; i < s.offset + s.count; i++) {
      char c = s.array[i];
      int pos = startOffset + i - s.offset;
      if (c == '\t') {
        nextX = e != null ? e.nextTabStop(nextX, pos) : nextX + metrics.charWidth(' ');
      } else if (c == '\n' || c == '\r') {
        continue;
      } else {
        nextX += metrics.charWidth(c);
        if (c == ' ') {
          nextX += spaceExtra(jd, pos, spaceIdx);
          if (pos >= jd[3] && pos < jd[4]) {
            spaceIdx++;
          }
        }
      }
    }
    return nextX - x;
  }

  static float justifiedDraw(Segment s, float x, float y, Graphics g, TabExpander e, int startOffset, int[] jd) {
    FontMetrics metrics = g.getFontMetrics();
    float nextX = x;
    int spaceIdx = jd[2];
    int flushIndex = s.offset;
    int flushLen = 0;
    for (int i = s.offset; i < s.offset + s.count; i++) {
      char c = s.array[i];
      int pos = startOffset + i - s.offset;
      boolean special = c == '\t' || c == '\n' || c == '\r' || c == ' ' && pos >= jd[3] && pos < jd[4];
      if (!special) {
        flushLen++;
        continue;
      }
      if (flushLen > 0) {
        drawChunk(g, s.array, flushIndex, flushLen, nextX, y, false);
        nextX += metrics.charsWidth(s.array, flushIndex, flushLen);
        flushLen = 0;
      }
      flushIndex = i + 1;
      if (c == '\t') {
        nextX = e != null ? e.nextTabStop(nextX, pos) : nextX + metrics.charWidth(' ');
      } else if (c == ' ') {
        nextX += metrics.charWidth(' ') + spaceExtra(jd, pos, spaceIdx);
        spaceIdx++;
      }
    }
    if (flushLen > 0) {
      drawChunk(g, s.array, flushIndex, flushLen, nextX, y, false);
      nextX += metrics.charsWidth(s.array, flushIndex, flushLen);
    }
    return nextX;
  }

  static int justifiedOffset(Segment s, FontMetrics metrics, float x0, float x, TabExpander e, int startOffset,
      boolean round, int[] jd) {
    if (x0 >= x) {
      return 0;
    }
    float currX = x0;
    float nextX = currX;
    int spaceIdx = jd[2];
    boolean seenTab = false;
    for (int i = s.offset; i < s.offset + s.count; i++) {
      char c = s.array[i];
      int pos = startOffset + i - s.offset;
      if (c == '\t') {
        seenTab = true;
        nextX = e != null ? e.nextTabStop(nextX, pos) : nextX + metrics.charWidth(' ');
      } else if (c != '\n' && c != '\r') {
        nextX += metrics.charWidth(c);
        if (c == ' ') {
          nextX += spaceExtra(jd, pos, spaceIdx);
          if (pos >= jd[3] && pos < jd[4]) {
            spaceIdx++;
          }
        }
      }
      if (x >= currX && x < nextX) {
        if (round && (seenTab || (x - currX) >= (nextX - x))) {
          return i + 1 - s.offset;
        }
        return i - s.offset;
      }
      currX = nextX;
    }
    return s.count;
  }

  private static void drawChunk(Graphics g, char[] txt, int off, int len, float x, float y, boolean fp) {
    if (g instanceof Graphics2D) {
      ((Graphics2D) g).drawString(new String(txt, off, len), x, y);
    } else {
      g.drawChars(txt, off, len, (int) x, (int) y);
    }
  }

  public static final int getTabbedTextWidth(Segment s, FontMetrics metrics, int x, TabExpander e,
      int startOffset) {
    return (int) getTabbedTextWidth(null, s, metrics, x, e, startOffset, null);
  }

  public static final float getTabbedTextWidth(Segment s, FontMetrics metrics, float x, TabExpander e,
      int startOffset) {
    return getTabbedTextWidth(null, s, metrics, x, e, startOffset, null);
  }

  static final float getTabbedTextWidth(View view, Segment s, FontMetrics metrics, float x, TabExpander e,
      int startOffset, int[] justificationData) {
    float nextX = x;
    char[] txt = s.array;
    int txtOffset = s.offset;
    int n = s.offset + s.count;
    int charCount = 0;
    int spaceAddon = 0;
    int spaceAddonLeftoverEnd = -1;
    int startJustifiableContent = 0;
    int endJustifiableContent = 0;
    for (int i = txtOffset; i < n; i++) {
      if (txt[i] == '\t') {
        nextX += metrics.charsWidth(txt, i - charCount, charCount);
        charCount = 0;
        if (e != null) {
          nextX = e.nextTabStop(nextX, startOffset + i - txtOffset);
        } else {
          nextX += metrics.charWidth(' ');
        }
      } else if (txt[i] == '\n' || txt[i] == '\r') {
        nextX += metrics.charsWidth(txt, i - charCount, charCount);
        charCount = 0;
      } else {
        charCount++;
      }
    }
    nextX += metrics.charsWidth(txt, n - charCount, charCount);
    return nextX - x;
  }

  public static final int getTabbedTextOffset(Segment s, FontMetrics metrics, int x0, int x, TabExpander e,
      int startOffset) {
    return getTabbedTextOffset(s, metrics, x0, x, e, startOffset, true);
  }

  public static final int getTabbedTextOffset(Segment s, FontMetrics metrics, int x0, int x, TabExpander e,
      int startOffset, boolean round) {
    return getTabbedTextOffset(s, metrics, (float) x0, (float) x, e, startOffset, round);
  }

  public static final int getTabbedTextOffset(Segment s, FontMetrics metrics, float x0, float x, TabExpander e,
      int startOffset, boolean round) {
    if (x0 >= x) {
      return 0;
    }
    float currX = x0;
    float nextX = currX;
    char[] txt = s.array;
    int txtOffset = s.offset;
    int txtCount = s.count;
    int n = s.offset + s.count;
    boolean seenTab = false;
    for (int i = s.offset; i < n; i++) {
      if (txt[i] == '\t') {
        seenTab = true;
        if (e != null) {
          nextX = e.nextTabStop(nextX, startOffset + i - txtOffset);
        } else {
          nextX += metrics.charWidth(' ');
        }
      } else if (txt[i] == '\n' || txt[i] == '\r') {
        // zero advance
      } else {
        nextX += metrics.charWidth(txt[i]);
      }
      if ((x >= currX) && (x < nextX)) {
        if (round && (seenTab || (x - currX) >= (nextX - x))) {
          return i + 1 - txtOffset;
        }
        return i - txtOffset;
      }
      currX = nextX;
    }
    return txtCount;
  }

  public static final int getBreakLocation(Segment s, FontMetrics metrics, int x0, int x, TabExpander e,
      int startOffset) {
    return getBreakLocation(s, metrics, (float) x0, (float) x, e, startOffset);
  }

  public static final int getBreakLocation(Segment s, FontMetrics metrics, float x0, float x, TabExpander e,
      int startOffset) {
    char[] txt = s.array;
    int txtOffset = s.offset;
    int txtCount = s.count;
    int index = Utilities.getTabbedTextOffset(s, metrics, x0, x, e, startOffset, false);
    if (index >= txtCount - 1) {
      return txtCount;
    }
    for (int i = txtOffset + index; i >= txtOffset; i--) {
      char ch = txt[i];
      if (ch < 256) {
        if (Character.isWhitespace(ch)) {
          index = i - txtOffset + 1;
          break;
        }
      } else {
        BreakIterator bit = BreakIterator.getLineInstance();
        bit.setText(s);
        int breakPos = bit.preceding(i + 1);
        if (breakPos > txtOffset) {
          index = breakPos - txtOffset;
        }
        break;
      }
    }
    return index;
  }

  public static final int getRowStart(JTextComponent c, int offs) throws BadLocationException {
    Rectangle r = c.modelToView(offs);
    if (r == null) {
      return -1;
    }
    int lastOffs = offs;
    int y = r.y;
    while ((r != null) && (y == r.y)) {
      if (r.height != 0) {
        offs = lastOffs;
      }
      lastOffs -= 1;
      r = (lastOffs >= 0) ? c.modelToView(lastOffs) : null;
    }
    return offs;
  }

  public static final int getRowEnd(JTextComponent c, int offs) throws BadLocationException {
    Rectangle r = c.modelToView(offs);
    if (r == null) {
      return -1;
    }
    int n = c.getDocument().getLength();
    int lastOffs = offs;
    int y = r.y;
    while ((r != null) && (y == r.y)) {
      if (r.height != 0) {
        offs = lastOffs;
      }
      lastOffs += 1;
      r = (lastOffs <= n) ? c.modelToView(lastOffs) : null;
    }
    return offs;
  }

  public static final int getPositionAbove(JTextComponent c, int offs, int x) throws BadLocationException {
    return getPositionAbove(c, offs, (float) x);
  }

  public static final int getPositionAbove(JTextComponent c, int offs, float x) throws BadLocationException {
    int lastOffs = getRowStart(c, offs) - 1;
    if (lastOffs < 0) {
      return -1;
    }
    float bestSpan = Integer.MAX_VALUE;
    float span;
    Rectangle r = c.modelToView(lastOffs);
    int best = lastOffs;
    if (r != null) {
      int y = r.y;
      while ((r != null) && (y == r.y)) {
        span = Math.abs(r.x - x);
        if (span < bestSpan) {
          best = lastOffs;
          bestSpan = span;
        }
        lastOffs -= 1;
        r = (lastOffs >= 0) ? c.modelToView(lastOffs) : null;
      }
    }
    return best;
  }

  public static final int getPositionBelow(JTextComponent c, int offs, int x) throws BadLocationException {
    return getPositionBelow(c, offs, (float) x);
  }

  public static final int getPositionBelow(JTextComponent c, int offs, float x) throws BadLocationException {
    int lastOffs = getRowEnd(c, offs) + 1;
    if (lastOffs <= 0) {
      return -1;
    }
    if (lastOffs > c.getDocument().getLength()) {
      return offs;
    }
    float bestSpan = Integer.MAX_VALUE;
    float span;
    int n = c.getDocument().getLength();
    Rectangle r = c.modelToView(lastOffs);
    int best = lastOffs;
    if (r != null) {
      int y = r.y;
      while ((r != null) && (y == r.y)) {
        span = Math.abs(x - r.x);
        if (span < bestSpan) {
          best = lastOffs;
          bestSpan = span;
        }
        lastOffs += 1;
        r = (lastOffs <= n) ? c.modelToView(lastOffs) : null;
      }
    }
    return best;
  }

  public static final int getWordStart(JTextComponent c, int offs) throws BadLocationException {
    Document doc = c.getDocument();
    Element line = getParagraphElement(c, offs);
    if (line == null) {
      throw new BadLocationException("No word at " + offs, offs);
    }
    int lineStart = line.getStartOffset();
    int lineEnd = Math.min(line.getEndOffset(), doc.getLength());
    Segment seg = new Segment();
    doc.getText(lineStart, lineEnd - lineStart, seg);
    if (seg.count > 0) {
      BreakIterator words = BreakIterator.getWordInstance(c.getLocale());
      words.setText(seg);
      int wordPosition = seg.offset + offs - lineStart;
      if (wordPosition >= words.last()) {
        wordPosition = words.last() - 1;
      }
      words.following(wordPosition);
      offs = lineStart + words.previous() - seg.offset;
    }
    return offs;
  }

  public static final int getWordEnd(JTextComponent c, int offs) throws BadLocationException {
    Document doc = c.getDocument();
    Element line = getParagraphElement(c, offs);
    if (line == null) {
      throw new BadLocationException("No word at " + offs, offs);
    }
    int lineStart = line.getStartOffset();
    int lineEnd = Math.min(line.getEndOffset(), doc.getLength());
    Segment seg = new Segment();
    doc.getText(lineStart, lineEnd - lineStart, seg);
    if (seg.count > 0) {
      BreakIterator words = BreakIterator.getWordInstance(c.getLocale());
      words.setText(seg);
      int wordPosition = offs - lineStart + seg.offset;
      if (wordPosition >= words.last()) {
        wordPosition = words.last() - 1;
      }
      offs = lineStart + words.following(wordPosition) - seg.offset;
    }
    return offs;
  }

  public static final int getNextWord(JTextComponent c, int offs) throws BadLocationException {
    Element line = getParagraphElement(c, offs);
    int nextWord = getNextWordInParagraph(c, line, offs, false);
    while (nextWord == BreakIterator.DONE) {
      offs = line.getEndOffset();
      line = getParagraphElement(c, offs);
      nextWord = getNextWordInParagraph(c, line, offs, true);
    }
    return nextWord;
  }

  static int getNextWordInParagraph(JTextComponent c, Element line, int offs, boolean first)
      throws BadLocationException {
    if (line == null) {
      throw new BadLocationException("No more words", offs);
    }
    Document doc = line.getDocument();
    int lineStart = line.getStartOffset();
    int lineEnd = Math.min(line.getEndOffset(), doc.getLength());
    if ((offs >= lineEnd) || (offs < lineStart)) {
      throw new BadLocationException("No more words", offs);
    }
    Segment seg = new Segment();
    doc.getText(lineStart, lineEnd - lineStart, seg);
    BreakIterator words = BreakIterator.getWordInstance(c.getLocale());
    words.setText(seg);
    if ((first && (words.first() == (seg.offset + offs - lineStart)))
        && (!Character.isWhitespace(seg.array[words.first()]))) {
      return offs;
    }
    int wordPosition = words.following(seg.offset + offs - lineStart);
    if ((wordPosition == BreakIterator.DONE) || (wordPosition >= seg.offset + seg.count)) {
      return BreakIterator.DONE;
    }
    char ch = seg.array[wordPosition];
    if (!Character.isWhitespace(ch)) {
      return lineStart + wordPosition - seg.offset;
    }
    int nextWord = words.next();
    if (nextWord != BreakIterator.DONE) {
      offs = lineStart + nextWord - seg.offset;
      if (offs != lineEnd) {
        return offs;
      }
    }
    return BreakIterator.DONE;
  }

  public static final int getPreviousWord(JTextComponent c, int offs) throws BadLocationException {
    Element line = getParagraphElement(c, offs);
    int prevWord = getPrevWordInParagraph(c, line, offs);
    while (prevWord == BreakIterator.DONE) {
      int lineStart = line.getStartOffset();
      if (lineStart == 0) {
        throw new BadLocationException("No more words", offs);
      }
      offs = lineStart - 1;
      line = getParagraphElement(c, offs);
      prevWord = getPrevWordInParagraph(c, line, offs);
    }
    return prevWord;
  }

  static int getPrevWordInParagraph(JTextComponent c, Element line, int offs) throws BadLocationException {
    if (line == null) {
      throw new BadLocationException("No more words", offs);
    }
    Document doc = line.getDocument();
    int lineStart = line.getStartOffset();
    int lineEnd = line.getEndOffset();
    if (offs > lineEnd || offs < lineStart) {
      throw new BadLocationException("No more words", offs);
    }
    Segment seg = new Segment();
    doc.getText(lineStart, Math.min(lineEnd, doc.getLength()) - lineStart, seg);
    BreakIterator words = BreakIterator.getWordInstance(c.getLocale());
    words.setText(seg);
    int wordPosition = words.preceding(seg.offset + offs - lineStart);
    if (wordPosition == BreakIterator.DONE) {
      return BreakIterator.DONE;
    }
    char ch = seg.array[wordPosition];
    if (!Character.isWhitespace(ch)) {
      return lineStart + wordPosition - seg.offset;
    }
    wordPosition = words.previous();
    if (wordPosition != BreakIterator.DONE) {
      return lineStart + wordPosition - seg.offset;
    }
    return BreakIterator.DONE;
  }

  public static final Element getParagraphElement(JTextComponent c, int offs) {
    Document doc = c.getDocument();
    if (doc instanceof StyledDocument) {
      return ((StyledDocument) doc).getParagraphElement(offs);
    }
    Element map = doc.getDefaultRootElement();
    int index = map.getElementIndex(offs);
    Element paragraph = map.getElement(index);
    if ((offs >= paragraph.getStartOffset()) && (offs < paragraph.getEndOffset())) {
      return paragraph;
    }
    return null;
  }

  static int getNextVisualPositionFrom(View v, int pos, Position.Bias b, java.awt.Shape alloc, int direction,
      Position.Bias[] biasRet) throws BadLocationException {
    if (v.getViewCount() == 0) {
      return pos;
    }
    boolean top = (direction == javax.swing.SwingConstants.NORTH || direction == javax.swing.SwingConstants.WEST);
    int retValue;
    if (pos == -1) {
      int childIndex = (top) ? v.getViewCount() - 1 : 0;
      View child = v.getView(childIndex);
      java.awt.Shape childBounds = v.getChildAllocation(childIndex, alloc);
      retValue = child.getNextVisualPositionFrom(pos, b, childBounds, direction, biasRet);
      if (retValue == -1 && !top && v.getViewCount() > 1) {
        child = v.getView(1);
        childBounds = v.getChildAllocation(1, alloc);
        retValue = child.getNextVisualPositionFrom(-1, biasRet[0], childBounds, direction, biasRet);
      }
    } else {
      int increment = (top) ? -1 : 1;
      int childIndex;
      if (b == Position.Bias.Backward && pos > 0) {
        childIndex = v.getViewIndex(pos - 1, Position.Bias.Forward);
      } else {
        childIndex = v.getViewIndex(pos, Position.Bias.Forward);
      }
      View child = v.getView(childIndex);
      int startPos = child.getStartOffset();
      int endPos = child.getEndOffset();
      if ((top && startPos == v.getStartOffset()) || (!top && endPos == v.getEndOffset())) {
        retValue = -1;
      } else {
        java.awt.Shape childBounds = v.getChildAllocation(childIndex, alloc);
        retValue = child.getNextVisualPositionFrom(pos, b, childBounds, direction, biasRet);
      }
      if (retValue == -1) {
        childIndex += increment;
        if (childIndex >= 0 && childIndex < v.getViewCount()) {
          child = v.getView(childIndex);
          java.awt.Shape childBounds = v.getChildAllocation(childIndex, alloc);
          retValue = child.getNextVisualPositionFrom(-1, b, childBounds, direction, biasRet);
          if (retValue == pos && biasRet[0] != b) {
            return v.getNextVisualPositionFrom(pos, biasRet[0], alloc, direction, biasRet);
          }
        }
      }
    }
    return retValue;
  }
}
