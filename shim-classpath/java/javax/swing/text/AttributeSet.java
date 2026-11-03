/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.text;

public interface AttributeSet {
  java.lang.Object NameAttribute = StyleConstants.NameAttribute;
  java.lang.Object ResolveAttribute = StyleConstants.ResolveAttribute;
  boolean isDefined(java.lang.Object p0);
  boolean isEqual(javax.swing.text.AttributeSet p0);
  java.lang.Object getAttribute(java.lang.Object p0);
  int getAttributeCount();
  javax.swing.text.AttributeSet copyAttributes();
  java.util.Enumeration<?> getAttributeNames();
  boolean containsAttribute(java.lang.Object p0, java.lang.Object p1);
  boolean containsAttributes(javax.swing.text.AttributeSet p0);
  javax.swing.text.AttributeSet getResolveParent();

  public interface ParagraphAttribute {
  }

  public interface CharacterAttribute {
  }

  public interface ColorAttribute {
  }

  public interface FontAttribute {
  }
}
