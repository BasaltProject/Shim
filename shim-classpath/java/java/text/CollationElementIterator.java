/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.text;

public final class CollationElementIterator {
  public static final int NULLORDER = 0xffffffff;

  private String text;
  private final RuleBasedCollator owner;
  private int[] elements;
  private int[] offsets;
  private int position;

  CollationElementIterator(String text, RuleBasedCollator owner) {
    this.owner = owner;
    setText(text);
  }

  public void reset() {
    position = 0;
  }

  public int next() {
    if (position >= elements.length / 3) {
      return NULLORDER;
    }
    int i = position++ * 3;
    return (elements[i] << 16) | ((elements[i + 1] & 0xff) << 8) | (elements[i + 2] & 0xff);
  }

  public int previous() {
    if (position <= 0) {
      return NULLORDER;
    }
    position--;
    int i = position * 3;
    return (elements[i] << 16) | ((elements[i + 1] & 0xff) << 8) | (elements[i + 2] & 0xff);
  }

  public static final int primaryOrder(int order) {
    return (order & 0xffff0000) >>> 16;
  }

  public static final short secondaryOrder(int order) {
    return (short) ((order & 0x0000ff00) >> 8);
  }

  public static final short tertiaryOrder(int order) {
    return (short) (order & 0x000000ff);
  }

  public int getOffset() {
    return position == 0 ? 0 : offsets[position - 1];
  }

  public void setOffset(int newOffset) {
    if (newOffset < 0 || newOffset > text.length()) {
      throw new IllegalArgumentException("Offset out of bounds");
    }
    position = 0;
    while (position < elements.length / 3 && offsets[position] <= newOffset) {
      position++;
    }
  }

  public int getMaxExpansion(int order) {
    return 1;
  }

  public void setText(String source) {
    text = source;
    int[] count = new int[1];
    int[] off = new int[source.length() * 4 + 8];
    elements = owner.table().elementsOf(owner.prepare(source), off, count);
    offsets = off;
    position = 0;
  }

  public void setText(CharacterIterator source) {
    StringBuilder sb = new StringBuilder();
    for (char c = source.first(); c != CharacterIterator.DONE; c = source.next()) {
      sb.append(c);
    }
    setText(sb.toString());
  }
}
