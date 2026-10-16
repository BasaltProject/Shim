/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.cs;

import java.nio.CharBuffer;
import java.nio.charset.CoderResult;

public final class Surrogate {
  private Surrogate() { }

  public static final class Parser {
    private CoderResult error = CoderResult.UNDERFLOW;
    private int character;

    public int character() {
      return character;
    }

    public CoderResult error() {
      return error;
    }

    public int parse(char c, CharBuffer in) {
      if (Character.isHighSurrogate(c)) {
        if (!in.hasRemaining()) {
          error = CoderResult.UNDERFLOW;
          return -1;
        }
        char d = in.get();
        if (Character.isLowSurrogate(d)) {
          character = Character.toCodePoint(c, d);
          error = null;
          return character;
        }
        error = CoderResult.malformedForLength(1);
        return -1;
      }
      if (Character.isLowSurrogate(c)) {
        error = CoderResult.malformedForLength(1);
        return -1;
      }
      character = c;
      error = null;
      return character;
    }
  }
}
