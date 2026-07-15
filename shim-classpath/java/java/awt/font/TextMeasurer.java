/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.font;

import java.awt.Font;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

public final class TextMeasurer implements Cloneable {
  private AttributedCharacterIterator text;
  private FontRenderContext frc;
  private int start;
  private int end;
  private float[] advances;
  private Font[] fonts;

  public TextMeasurer(AttributedCharacterIterator text, FontRenderContext frc) {
    this.frc = frc;
    setText(text);
  }

  private void setText(AttributedCharacterIterator t) {
    text = t;
    start = t.getBeginIndex();
    end = t.getEndIndex();
    advances = new float[end - start];
    fonts = new Font[end - start];
    char c = t.first();
    int idx = 0;
    while (c != AttributedCharacterIterator.DONE) {
      Object f = t.getAttribute(TextAttribute.FONT);
      Font font = f instanceof Font ? (Font) f : Font.getFont(t.getAttributes());
      if (font == null) {
        font = new Font(t.getAttributes());
      }
      fonts[idx] = font;
      advances[idx] = (float) font.getStringBounds(String.valueOf(c), frc).getWidth();
      idx++;
      c = t.next();
    }
  }

  protected Object clone() {
    TextMeasurer other;
    try {
      other = (TextMeasurer) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new Error();
    }
    return other;
  }

  public int getLineBreakIndex(int startPos, float maxAdvance) {
    checkRange(startPos, startPos);
    float sum = 0;
    int i = startPos;
    while (i < end) {
      float w = advances[i - start];
      if (sum + w > maxAdvance) {
        break;
      }
      sum += w;
      i++;
    }
    return i;
  }

  public float getAdvanceBetween(int startPos, int limit) {
    checkRange(startPos, limit);
    float sum = 0;
    for (int i = startPos; i < limit; i++) {
      sum += advances[i - start];
    }
    return sum;
  }

  public TextLayout getLayout(int startPos, int limit) {
    checkRange(startPos, limit);
    if (startPos >= limit) {
      throw new IllegalArgumentException("Start must be before limit");
    }
    AttributedString sub = new AttributedString(text, startPos, limit);
    return new TextLayout(sub.getIterator(), frc);
  }

  public void insertChar(AttributedCharacterIterator newParagraph, int insertPos) {
    setText(newParagraph);
  }

  public void deleteChar(AttributedCharacterIterator newParagraph, int deletePos) {
    if (newParagraph.getBeginIndex() == newParagraph.getEndIndex()) {
      text = newParagraph;
      start = end = newParagraph.getBeginIndex();
      advances = new float[0];
      fonts = new Font[0];
      return;
    }
    setText(newParagraph);
  }

  private void checkRange(int s, int e) {
    if (s < start || e > end || s > e) {
      throw new IndexOutOfBoundsException("Invalid range: " + s + "," + e);
    }
  }
}
