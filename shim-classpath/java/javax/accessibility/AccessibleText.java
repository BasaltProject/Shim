/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.accessibility;

public interface AccessibleText {
  int CHARACTER = 1;
  int WORD = 2;
  int SENTENCE = 3;
  int getIndexAtPoint(java.awt.Point p0);
  java.awt.Rectangle getCharacterBounds(int p0);
  int getCharCount();
  javax.swing.text.AttributeSet getCharacterAttribute(int p0);
  java.lang.String getAtIndex(int p0, int p1);
  java.lang.String getAfterIndex(int p0, int p1);
  java.lang.String getBeforeIndex(int p0, int p1);
  int getSelectionStart();
  int getSelectionEnd();
  int getCaretPosition();
  java.lang.String getSelectedText();
}
