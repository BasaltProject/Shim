/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.text;

public interface MutableAttributeSet extends javax.swing.text.AttributeSet {
  void addAttribute(java.lang.Object p0, java.lang.Object p1);
  void removeAttributes(javax.swing.text.AttributeSet p0);
  void removeAttributes(java.util.Enumeration<?> p0);
  void addAttributes(javax.swing.text.AttributeSet p0);
  void setResolveParent(javax.swing.text.AttributeSet p0);
  void removeAttribute(java.lang.Object p0);
}
