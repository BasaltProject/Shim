/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.text;

import shim.text.BidiAlgorithm;

public final class Bidi {
  public static final int DIRECTION_LEFT_TO_RIGHT = 0;
  public static final int DIRECTION_RIGHT_TO_LEFT = 1;
  public static final int DIRECTION_DEFAULT_LEFT_TO_RIGHT = -2;
  public static final int DIRECTION_DEFAULT_RIGHT_TO_LEFT = -1;

  private final int length;
  private final byte[] levels;
  private final int baseLevel;
  private final int direction;
  private char[] source = new char[0];
  private int[] runStarts;
  private int[] runLevels;

  public Bidi(String paragraph, int flags) {
    this(checkParagraph(paragraph), 0, null, 0, paragraph.length(), flags);
  }

  private static char[] checkParagraph(String paragraph) {
    if (paragraph == null) {
      throw new IllegalArgumentException("paragraph is null");
    }
    return paragraph.toCharArray();
  }

  public Bidi(AttributedCharacterIterator paragraph) {
    this(text(paragraph), 0, null, 0, paragraph.getEndIndex() - paragraph.getBeginIndex(),
        flagsOf(paragraph));
  }

  private static char[] text(AttributedCharacterIterator p) {
    StringBuilder sb = new StringBuilder();
    for (char c = p.first(); c != CharacterIterator.DONE; c = p.next()) {
      sb.append(c);
    }
    return sb.toString().toCharArray();
  }

  private static int flagsOf(AttributedCharacterIterator p) {
    return DIRECTION_DEFAULT_LEFT_TO_RIGHT;
  }

  public Bidi(char[] text, int textStart, byte[] embeddings, int embStart, int paragraphLength, int flags) {
    if (text == null) {
      throw new IllegalArgumentException("text is null");
    }
    if (paragraphLength < 0) {
      throw new IllegalArgumentException("bad length: " + paragraphLength);
    }
    if (textStart < 0 || paragraphLength > text.length - textStart) {
      throw new IllegalArgumentException("bad range: " + textStart + " length: " + paragraphLength
          + " for text of length: " + text.length);
    }
    if (embeddings != null && (embStart < 0 || paragraphLength > embeddings.length - embStart)) {
      throw new IllegalArgumentException("bad range: " + embStart + " length: " + paragraphLength
          + " for embeddings of length: " + text.length);
    }
    this.length = paragraphLength;
    byte[] base = new byte[1];
    int[] dir = new int[1];
    this.levels = BidiAlgorithm.resolve(text, textStart, paragraphLength, embeddings, embStart, flags, base, dir);
    this.baseLevel = base[0];
    this.direction = dir[0];
    this.source = java.util.Arrays.copyOfRange(text, textStart, textStart + paragraphLength);
    buildRuns();
  }

  private Bidi(byte[] levels, int baseLevel, char[] source) {
    this.source = source;
    this.length = levels.length;
    this.levels = levels;
    this.baseLevel = baseLevel;
    int dir = 2;
    boolean uniform = true;
    for (int i = 0; i < levels.length; i++) {
      if (levels[i] != levels[0]) {
        uniform = false;
      }
    }
    if (uniform && levels.length > 0 && levels[0] == baseLevel) {
      dir = baseLevel & 1;
    } else if (levels.length == 0) {
      dir = baseLevel & 1;
    }
    this.direction = dir;
    buildRuns();
  }

  private void buildRuns() {
    if (direction != 2 && length > 0) {
      runStarts = new int[] {0, length};
      runLevels = new int[] {baseLevel, baseLevel};
      return;
    }
    int count = 0;
    int[] starts = new int[length + 1];
    int[] lv = new int[length + 1];
    for (int i = 0; i < length; i++) {
      if (i == 0 || levels[i] != levels[i - 1]) {
        starts[count] = i;
        lv[count] = levels[i];
        count++;
      }
    }
    runStarts = java.util.Arrays.copyOf(starts, count + 1);
    runStarts[count] = length;
    runLevels = java.util.Arrays.copyOf(lv, count + 1);
  }

  public Bidi createLineBidi(int lineStart, int lineLimit) {
    if (lineStart < 0 || lineStart >= lineLimit) {
      throw new IllegalArgumentException("Value " + lineStart + " is out of range 0 to " + lineLimit);
    }
    if (lineLimit < 0 || lineLimit >= length + 1) {
      throw new IllegalArgumentException("Value " + lineLimit + " is out of range 0 to " + (length + 1));
    }
    byte[] sub = java.util.Arrays.copyOfRange(levels, lineStart, lineLimit);
    return new Bidi(sub, baseLevel, java.util.Arrays.copyOfRange(source, lineStart, lineLimit));
  }

  public boolean isMixed() {
    return direction == 2;
  }

  public boolean isLeftToRight() {
    return direction == 0;
  }

  public boolean isRightToLeft() {
    return direction == 1;
  }

  public int getLength() {
    return length;
  }

  public boolean baseIsLeftToRight() {
    return baseLevel % 2 == 0;
  }

  public int getBaseLevel() {
    return baseLevel;
  }

  public int getLevelAt(int offset) {
    if (offset < 0 || offset >= length) {
      return baseLevel;
    }
    return levels[offset];
  }

  public int getRunCount() {
    return length == 0 ? 0 : runStarts.length - 1;
  }

  private void checkRun(int run) {
    int count = getRunCount();
    if (run < 0 || run >= count) {
      throw new ArrayIndexOutOfBoundsException("Index " + run + " out of bounds for length " + count);
    }
  }

  public int getRunLevel(int run) {
    if (length == 0 || run < 0 || run >= getRunCount()) {
      return length == 0 ? baseLevel : 0;
    }
    return runLevels[run];
  }

  public int getRunStart(int run) {
    checkRun(run);
    return runStarts[run];
  }

  public int getRunLimit(int run) {
    checkRun(run);
    return runStarts[run + 1];
  }

  public static boolean requiresBidi(char[] text, int start, int limit) {
    if (start < 0 || start >= limit || limit > text.length) {
      if (!(start == limit && start >= 0 && limit <= text.length)) {
        throw new IllegalArgumentException("Value start " + start + " is out of range 0 to " + limit
            + ", or limit " + limit + " is beyond the text length " + text.length);
      }
    }
    for (int i = start; i < limit; i++) {
      byte d = Character.getDirectionality(text[i]);
      if (d == Character.DIRECTIONALITY_RIGHT_TO_LEFT || d == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC
          || d == Character.DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING
          || d == Character.DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE
          || d == Character.DIRECTIONALITY_ARABIC_NUMBER) {
        return true;
      }
    }
    return false;
  }

  public static void reorderVisually(byte[] levels, int levelStart, Object[] objects, int objectStart, int count) {
    if (levelStart < 0 || levelStart >= levels.length) {
      throw new IllegalArgumentException("Value levelStart " + levelStart + " is out of range 0 to "
          + (levels.length - 1));
    }
    if (objectStart < 0 || objectStart >= objects.length) {
      throw new IllegalArgumentException("Value objectStart " + objectStart + " is out of range 0 to "
          + (objects.length - 1));
    }
    if (count < 0 || objectStart + count > objects.length) {
      throw new IllegalArgumentException("Value count " + count
          + " is less than zero, or objectStart + count is beyond objects length " + objects.length);
    }
    if (levelStart + count > levels.length) {
      throw new IllegalArgumentException("Value count " + count
          + " is less than zero, or levelStart + count is beyond levels length " + levels.length);
    }
    int highest = 0;
    int lowestOdd = 63;
    for (int i = levelStart; i < levelStart + count; i++) {
      int lv = levels[i];
      if (lv > highest) {
        highest = lv;
      }
      if ((lv & 1) != 0 && lv < lowestOdd) {
        lowestOdd = lv;
      }
    }
    for (int lv = highest; lv >= lowestOdd; --lv) {
      for (int i = 0; i < count; ++i) {
        if (levels[levelStart + i] >= lv) {
          int limit = i + 1;
          while (limit < count && levels[levelStart + limit] >= lv) {
            ++limit;
          }
          for (int j = i, k = limit - 1; j < k; ++j, --k) {
            Object t = objects[objectStart + j];
            objects[objectStart + j] = objects[objectStart + k];
            objects[objectStart + k] = t;
          }
          i = limit;
        }
      }
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("jdk.internal.icu.text.BidiBase[dir: ");
    sb.append(direction).append(" baselevel: ").append(baseLevel).append(" length: ").append(length)
        .append(" runs: [");
    for (int i = 0; i < length; i++) {
      if (i > 0) {
        sb.append(' ');
      }
      sb.append(levels[i]);
    }
    sb.append("] text: [");
    for (int i = 0; i < source.length; i++) {
      if (i > 0) {
        sb.append(' ');
      }
      sb.append("0x").append(Integer.toHexString(source[i]));
    }
    sb.append("]]");
    return sb.toString();
  }
}
