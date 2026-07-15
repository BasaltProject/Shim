/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.im;

public final class InputSubset extends java.lang.Character.Subset {
  public static final java.awt.im.InputSubset LATIN = new java.awt.im.InputSubset("LATIN");
  public static final java.awt.im.InputSubset LATIN_DIGITS = new java.awt.im.InputSubset("LATIN_DIGITS");
  public static final java.awt.im.InputSubset TRADITIONAL_HANZI = new java.awt.im.InputSubset("TRADITIONAL_HANZI");
  public static final java.awt.im.InputSubset SIMPLIFIED_HANZI = new java.awt.im.InputSubset("SIMPLIFIED_HANZI");
  public static final java.awt.im.InputSubset KANJI = new java.awt.im.InputSubset("KANJI");
  public static final java.awt.im.InputSubset HANJA = new java.awt.im.InputSubset("HANJA");
  public static final java.awt.im.InputSubset HALFWIDTH_KATAKANA = new java.awt.im.InputSubset("HALFWIDTH_KATAKANA");
  public static final java.awt.im.InputSubset FULLWIDTH_LATIN = new java.awt.im.InputSubset("FULLWIDTH_LATIN");
  public static final java.awt.im.InputSubset FULLWIDTH_DIGITS = new java.awt.im.InputSubset("FULLWIDTH_DIGITS");
  InputSubset(java.lang.String p0) {
    super(p0);
  }
  InputSubset() {
    super((java.lang.String) null);
  }
}
