/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.font;

import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;

public class FontRenderContext {
  private transient AffineTransform tx;
  private transient Object aaHintValue;
  private transient Object fmHintValue;
  private transient boolean defaulting;

  protected FontRenderContext() {
    aaHintValue = RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT;
    fmHintValue = RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT;
    defaulting = true;
  }

  public FontRenderContext(AffineTransform tx, boolean isAntiAliased, boolean usesFractionalMetrics) {
    if (tx != null && !tx.isIdentity()) {
      this.tx = new AffineTransform(tx);
    }
    if (isAntiAliased) {
      aaHintValue = RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
    } else {
      aaHintValue = RenderingHints.VALUE_TEXT_ANTIALIAS_OFF;
    }
    if (usesFractionalMetrics) {
      fmHintValue = RenderingHints.VALUE_FRACTIONALMETRICS_ON;
    } else {
      fmHintValue = RenderingHints.VALUE_FRACTIONALMETRICS_OFF;
    }
  }

  public FontRenderContext(AffineTransform tx, Object aaHint, Object fmHint) {
    if (aaHint == null) {
      throw new IllegalArgumentException("AA hint cannot be null");
    }
    if (fmHint == null) {
      throw new IllegalArgumentException("FM hint cannot be null");
    }
    if (tx != null && !tx.isIdentity()) {
      this.tx = new AffineTransform(tx);
    }
    if (!RenderingHints.KEY_TEXT_ANTIALIASING.isCompatibleValue(aaHint)) {
      throw new IllegalArgumentException("AA hint:" + aaHint);
    }
    if (!RenderingHints.KEY_FRACTIONALMETRICS.isCompatibleValue(fmHint)) {
      throw new IllegalArgumentException("FM hint:" + fmHint);
    }
    this.aaHintValue = aaHint;
    this.fmHintValue = fmHint;
  }

  public boolean isTransformed() {
    if (!defaulting) {
      return tx != null;
    }
    return !getTransform().isIdentity();
  }

  public int getTransformType() {
    if (!defaulting) {
      if (tx == null) {
        return AffineTransform.TYPE_IDENTITY;
      }
      return tx.getType();
    }
    return getTransform().getType();
  }

  public AffineTransform getTransform() {
    return (tx == null) ? new AffineTransform() : new AffineTransform(tx);
  }

  public boolean isAntiAliased() {
    return !(aaHintValue == RenderingHints.VALUE_TEXT_ANTIALIAS_OFF || aaHintValue == RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);
  }

  public boolean usesFractionalMetrics() {
    return !(fmHintValue == RenderingHints.VALUE_FRACTIONALMETRICS_OFF || fmHintValue == RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT);
  }

  public Object getAntiAliasingHint() {
    if (defaulting) {
      if (isAntiAliased()) {
        return RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
      } else {
        return RenderingHints.VALUE_TEXT_ANTIALIAS_OFF;
      }
    }
    return aaHintValue;
  }

  public Object getFractionalMetricsHint() {
    if (defaulting) {
      if (usesFractionalMetrics()) {
        return RenderingHints.VALUE_FRACTIONALMETRICS_ON;
      } else {
        return RenderingHints.VALUE_FRACTIONALMETRICS_OFF;
      }
    }
    return fmHintValue;
  }

  public boolean equals(Object obj) {
    try {
      return equals((FontRenderContext) obj);
    } catch (ClassCastException e) {
      return false;
    }
  }

  public boolean equals(FontRenderContext rhs) {
    if (this == rhs) {
      return true;
    }
    if (rhs == null) {
      return false;
    }
    if (!rhs.defaulting && !defaulting) {
      if (rhs.aaHintValue == aaHintValue && rhs.fmHintValue == fmHintValue) {
        return tx == null ? rhs.tx == null : tx.equals(rhs.tx);
      }
      return false;
    }
    return rhs.getAntiAliasingHint() == getAntiAliasingHint() && rhs.getFractionalMetricsHint() == getFractionalMetricsHint()
        && rhs.getTransform().equals(getTransform());
  }

  public int hashCode() {
    int hash = tx == null ? 0 : tx.hashCode();
    if (defaulting) {
      hash += getAntiAliasingHint().hashCode();
      hash += getFractionalMetricsHint().hashCode();
    } else {
      hash += aaHintValue.hashCode();
      hash += fmHintValue.hashCode();
    }
    return hash;
  }
}
