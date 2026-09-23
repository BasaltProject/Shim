/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.javac;

public final class Token {
  public static final int EOF = 0;
  public static final int IDENT = 1;
  public static final int INTLIT = 2;
  public static final int LONGLIT = 3;
  public static final int FLOATLIT = 4;
  public static final int DOUBLELIT = 5;
  public static final int CHARLIT = 6;
  public static final int STRINGLIT = 7;
  public static final int KEYWORD = 8;
  public static final int OP = 9;
  public static final int ERROR = 10;

  public final int kind;
  public final String text;
  public final int start;
  public final int end;
  public final Object value;
  public String doc;
  public boolean textBlock;

  public Token(int kind, String text, int start, int end, Object value) {
    this.kind = kind;
    this.text = text;
    this.start = start;
    this.end = end;
    this.value = value;
  }

  public boolean is(String t) {
    return (kind == KEYWORD || kind == OP) && text.equals(t);
  }

  public String toString() {
    return kind + ":" + text;
  }
}
