/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.text.spi;

import java.text.BreakIterator;
import java.util.Locale;
import java.util.spi.LocaleServiceProvider;

public abstract class BreakIteratorProvider extends LocaleServiceProvider {
  protected BreakIteratorProvider() {
  }

  public abstract BreakIterator getWordInstance(Locale locale);

  public abstract BreakIterator getLineInstance(Locale locale);

  public abstract BreakIterator getCharacterInstance(Locale locale);

  public abstract BreakIterator getSentenceInstance(Locale locale);
}
