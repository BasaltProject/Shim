/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.text;

public interface Element {
  java.lang.String getName();
  javax.swing.text.AttributeSet getAttributes();
  javax.swing.text.Element getElement(int p0);
  boolean isLeaf();
  int getElementCount();
  javax.swing.text.Document getDocument();
  int getElementIndex(int p0);
  javax.swing.text.Element getParentElement();
  int getEndOffset();
  int getStartOffset();
}
