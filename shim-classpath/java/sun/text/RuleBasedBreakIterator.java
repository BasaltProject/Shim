/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.text;

import java.text.BreakIterator;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import shim.text.BreakRules;

public class RuleBasedBreakIterator extends BreakIterator {
  public static final int CHARACTER = 0;
  public static final int WORD = 1;
  public static final int SENTENCE = 2;
  public static final int LINE = 3;

  private final int kind;
  private CharacterIterator text = new StringCharacterIterator("");
  private int[] bounds = {0};
  private int begin;
  private int index;

  public RuleBasedBreakIterator(int kind) {
    this.kind = kind;
  }

  @Override
  public Object clone() {
    RuleBasedBreakIterator copy = (RuleBasedBreakIterator) super.clone();
    copy.text = (CharacterIterator) text.clone();
    return copy;
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof RuleBasedBreakIterator && ((RuleBasedBreakIterator) o).kind == kind
        && ((RuleBasedBreakIterator) o).text.equals(text);
  }

  @Override
  public int hashCode() {
    return kind;
  }

  @Override
  public void setText(CharacterIterator newText) {
    text = newText;
    begin = newText.getBeginIndex();
    StringBuilder sb = new StringBuilder();
    for (char c = newText.first(); c != CharacterIterator.DONE; c = newText.next()) {
      sb.append(c);
    }
    newText.first();
    String s = sb.toString();
    switch (kind) {
    case CHARACTER:
      bounds = BreakRules.chars(s);
      break;
    case WORD:
      bounds = BreakRules.words(s);
      break;
    case SENTENCE:
      bounds = BreakRules.sentences(s);
      break;
    default:
      bounds = BreakRules.lines(s);
      break;
    }
    index = 0;
  }

  @Override
  public CharacterIterator getText() {
    return text;
  }

  @Override
  public int first() {
    index = 0;
    return begin + bounds[0];
  }

  @Override
  public int last() {
    index = bounds.length - 1;
    return begin + bounds[index];
  }

  @Override
  public int current() {
    return begin + bounds[index];
  }

  @Override
  public int next() {
    if (index >= bounds.length - 1) {
      return DONE;
    }
    index++;
    return begin + bounds[index];
  }

  @Override
  public int previous() {
    if (index <= 0) {
      return DONE;
    }
    index--;
    return begin + bounds[index];
  }

  @Override
  public int next(int n) {
    int result = current();
    while (n > 0 && result != DONE) {
      result = next();
      n--;
    }
    while (n < 0 && result != DONE) {
      result = previous();
      n++;
    }
    return result;
  }

  private void check(int offset) {
    if (offset < text.getBeginIndex() || offset > text.getEndIndex()) {
      throw new IllegalArgumentException("offset out of bounds");
    }
  }

  @Override
  public int following(int offset) {
    check(offset);
    int rel = offset - begin;
    for (int i = 0; i < bounds.length; i++) {
      if (bounds[i] > rel) {
        index = i;
        return begin + bounds[i];
      }
    }
    index = bounds.length - 1;
    return DONE;
  }

  @Override
  public int preceding(int offset) {
    check(offset);
    int rel = offset - begin;
    for (int i = bounds.length - 1; i >= 0; i--) {
      if (bounds[i] < rel) {
        index = i;
        return begin + bounds[i];
      }
    }
    index = 0;
    return DONE;
  }

  @Override
  public boolean isBoundary(int offset) {
    check(offset);
    int rel = offset - begin;
    for (int i = 0; i < bounds.length; i++) {
      if (bounds[i] == rel) {
        index = i;
        return true;
      }
      if (bounds[i] > rel) {
        index = i;
        return false;
      }
    }
    index = bounds.length - 1;
    return false;
  }
}
