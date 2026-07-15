/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.accessibility;

public interface AccessibleEditableText extends javax.accessibility.AccessibleText {
  void delete(int p0, int p1);
  void setTextContents(java.lang.String p0);
  void insertTextAtIndex(int p0, java.lang.String p1);
  java.lang.String getTextRange(int p0, int p1);
  void cut(int p0, int p1);
  void paste(int p0);
  void selectText(int p0, int p1);
  void setAttributes(int p0, int p1, javax.swing.text.AttributeSet p2);
  void replaceText(int p0, int p1, java.lang.String p2);
}
