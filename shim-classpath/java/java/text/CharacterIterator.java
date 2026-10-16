/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.text;

public interface CharacterIterator extends Cloneable {
  char DONE = '\uFFFF';

  char first();

  char last();

  char current();

  char next();

  char previous();

  char setIndex(int position);

  int getBeginIndex();

  int getEndIndex();

  int getIndex();

  Object clone();
}
