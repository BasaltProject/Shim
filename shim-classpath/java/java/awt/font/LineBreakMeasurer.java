/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.font;

import java.text.AttributedCharacterIterator;
import java.text.BreakIterator;
import java.text.CharacterIterator;

public final class LineBreakMeasurer {
  private final BreakIterator breakIter;
  private final TextMeasurer measurer;
  private int limit;
  private int pos;
  private AttributedCharacterIterator text;

  public LineBreakMeasurer(AttributedCharacterIterator text, FontRenderContext frc) {
    this(text, BreakIterator.getLineInstance(), frc);
  }

  public LineBreakMeasurer(AttributedCharacterIterator text, BreakIterator breakIter, FontRenderContext frc) {
    if (text.getEndIndex() - text.getBeginIndex() < 1) {
      throw new IllegalArgumentException("Text must contain at least one character.");
    }
    this.breakIter = breakIter;
    this.measurer = new TextMeasurer(text, frc);
    this.limit = text.getEndIndex();
    this.pos = text.getBeginIndex();
    this.text = text;
    this.breakIter.setText((CharacterIterator) text);
  }

  public int nextOffset(float wrappingWidth) { return nextOffset(wrappingWidth, limit, false); }

  public int nextOffset(float wrappingWidth, int offsetLimit, boolean requireNextWord) {
    int nextOffset = pos;
    if (pos < limit) {
      if (offsetLimit <= pos) {
        throw new IllegalArgumentException("offsetLimit must be after current position");
      }
      int charAtMaxAdvance = measurer.getLineBreakIndex(pos, wrappingWidth);
      if (charAtMaxAdvance == limit) {
        nextOffset = limit;
      } else if (Character.isWhitespace(textCharAt(charAtMaxAdvance))) {
        nextOffset = breakIter.following(charAtMaxAdvance);
      } else {
        int testPos = charAtMaxAdvance + 1;
        if (testPos == limit) {
          breakIter.last();
          nextOffset = breakIter.previous();
        } else {
          nextOffset = breakIter.preceding(testPos);
        }
        if (nextOffset <= pos) {
          if (requireNextWord) {
            nextOffset = limit;
          } else {
            nextOffset = Math.max(pos + 1, charAtMaxAdvance);
          }
        }
      }
    }
    if (nextOffset > offsetLimit) {
      nextOffset = offsetLimit;
    }
    for (int i = pos; i < nextOffset; i++) {
      if (textCharAt(i) == '\n') {
        nextOffset = i + 1;
        break;
      }
    }
    return nextOffset;
  }

  private char textCharAt(int index) {
    char c = text.setIndex(index);
    return c;
  }

  public TextLayout nextLayout(float wrappingWidth) { return nextLayout(wrappingWidth, limit, false); }

  public TextLayout nextLayout(float wrappingWidth, int offsetLimit, boolean requireNextWord) {
    if (pos < limit) {
      int layoutLimit = nextOffset(wrappingWidth, offsetLimit, requireNextWord);
      if (layoutLimit == pos) {
        return null;
      }
      TextLayout result = measurer.getLayout(pos, layoutLimit);
      pos = layoutLimit;
      return result;
    }
    return null;
  }

  public int getPosition() { return pos; }

  public void setPosition(int newPosition) {
    if (newPosition < text.getBeginIndex() || newPosition > limit) {
      throw new IllegalArgumentException("position out of range");
    }
    pos = newPosition;
  }

  public void insertChar(AttributedCharacterIterator newParagraph, int insertPos) {
    measurer.insertChar(newParagraph, insertPos);
    text = newParagraph;
    limit = newParagraph.getEndIndex();
    pos = insertPos + 1 > pos ? pos : pos + 1;
    breakIter.setText((CharacterIterator) newParagraph);
  }

  public void deleteChar(AttributedCharacterIterator newParagraph, int deletePos) {
    measurer.deleteChar(newParagraph, deletePos);
    text = newParagraph;
    limit = newParagraph.getEndIndex();
    if (pos > deletePos) {
      pos--;
    }
    breakIter.setText((CharacterIterator) newParagraph);
  }
}
