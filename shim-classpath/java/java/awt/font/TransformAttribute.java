/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.font;

public final class TransformAttribute implements java.io.Serializable {
  public static final java.awt.font.TransformAttribute IDENTITY = new java.awt.font.TransformAttribute(new java.awt.geom.AffineTransform());
  private java.awt.geom.AffineTransform transform;

  public TransformAttribute(java.awt.geom.AffineTransform p0) {
    if (p0 == null) {
      throw new IllegalArgumentException("transform may not be null");
    }
    if (!p0.isIdentity()) {
      this.transform = new java.awt.geom.AffineTransform(p0);
    }
  }
  public boolean equals(java.lang.Object p0) {
    if (p0 == null) {
      return false;
    }
    try {
      TransformAttribute rhs = (TransformAttribute) p0;
      if (transform == null) {
        return rhs.transform == null;
      }
      return transform.equals(rhs.transform);
    } catch (ClassCastException e) {
      return false;
    }
  }
  public int hashCode() { return transform == null ? 0 : transform.hashCode(); }
  public boolean isIdentity() { return transform == null; }
  public java.awt.geom.AffineTransform getTransform() {
    java.awt.geom.AffineTransform at = transform;
    return (at == null) ? new java.awt.geom.AffineTransform() : new java.awt.geom.AffineTransform(at);
  }
}
