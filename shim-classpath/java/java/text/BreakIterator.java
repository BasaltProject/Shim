/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.text;

import java.util.Locale;
import sun.text.RuleBasedBreakIterator;

public abstract class BreakIterator implements Cloneable {
  protected BreakIterator() {
  }

  @Override
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      throw new InternalError(e);
    }
  }

  public static final int DONE = -1;

  public abstract int first();

  public abstract int last();

  public abstract int next(int n);

  public abstract int next();

  public abstract int previous();

  public abstract int following(int offset);

  public int preceding(int offset) {
    int pos = following(offset);
    while (pos >= offset && pos != DONE) {
      pos = previous();
    }
    return pos;
  }

  public boolean isBoundary(int offset) {
    if (offset == 0) {
      return true;
    }
    int boundary = following(offset - 1);
    if (boundary == DONE) {
      throw new IllegalArgumentException();
    }
    return boundary == offset;
  }

  public abstract int current();

  public abstract CharacterIterator getText();

  public void setText(String newText) {
    setText(new StringCharacterIterator(newText));
  }

  public abstract void setText(CharacterIterator newText);

  public static BreakIterator getWordInstance() {
    return getWordInstance(Locale.getDefault());
  }

  public static BreakIterator getWordInstance(Locale locale) {
    return new RuleBasedBreakIterator(RuleBasedBreakIterator.WORD);
  }

  public static BreakIterator getLineInstance() {
    return getLineInstance(Locale.getDefault());
  }

  public static BreakIterator getLineInstance(Locale locale) {
    return new RuleBasedBreakIterator(RuleBasedBreakIterator.LINE);
  }

  public static BreakIterator getCharacterInstance() {
    return getCharacterInstance(Locale.getDefault());
  }

  public static BreakIterator getCharacterInstance(Locale locale) {
    return new RuleBasedBreakIterator(RuleBasedBreakIterator.CHARACTER);
  }

  public static BreakIterator getSentenceInstance() {
    return getSentenceInstance(Locale.getDefault());
  }

  public static BreakIterator getSentenceInstance(Locale locale) {
    return new RuleBasedBreakIterator(RuleBasedBreakIterator.SENTENCE);
  }

  public static synchronized Locale[] getAvailableLocales() {
    return Locale.getAvailableLocales();
  }
}
