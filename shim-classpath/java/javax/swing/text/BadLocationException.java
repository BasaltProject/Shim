/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.text;

public class BadLocationException extends Exception {
  private final int offs;

  public BadLocationException(String s, int offs) {
    super(s);
    this.offs = offs;
  }

  public int offsetRequested() { return offs; }
}
