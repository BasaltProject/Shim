/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.io.Serializable;
import javax.swing.Icon;
import javax.swing.border.Border;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;

public class BorderUIResource implements Border, UIResource, Serializable {
  private static Border sEtched;
  private static Border sRaisedBevel;
  private static Border sLoweredBevel;
  private static Border sBlackLine;

  private final Border delegate;

  public static Border getEtchedBorderUIResource() {
    if (sEtched == null) {
      sEtched = new EtchedBorderUIResource();
    }
    return sEtched;
  }

  public static Border getLoweredBevelBorderUIResource() {
    if (sLoweredBevel == null) {
      sLoweredBevel = new BevelBorderUIResource(BevelBorder.LOWERED);
    }
    return sLoweredBevel;
  }

  public static Border getRaisedBevelBorderUIResource() {
    if (sRaisedBevel == null) {
      sRaisedBevel = new BevelBorderUIResource(BevelBorder.RAISED);
    }
    return sRaisedBevel;
  }

  public static Border getBlackLineBorderUIResource() {
    if (sBlackLine == null) {
      sBlackLine = new LineBorderUIResource(Color.black);
    }
    return sBlackLine;
  }

  public BorderUIResource(Border delegate) {
    if (delegate == null) {
      throw new IllegalArgumentException("null border delegate argument");
    }
    this.delegate = delegate;
  }

  public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    delegate.paintBorder(c, g, x, y, width, height);
  }

  public Insets getBorderInsets(Component c) { return delegate.getBorderInsets(c); }

  public boolean isBorderOpaque() { return delegate.isBorderOpaque(); }

  public static class EtchedBorderUIResource extends javax.swing.border.EtchedBorder implements javax.swing.plaf.UIResource {
    public EtchedBorderUIResource(int p0, java.awt.Color p1, java.awt.Color p2) {
      super(p0, p1, p2);
    }
    public EtchedBorderUIResource(java.awt.Color p0, java.awt.Color p1) {
      super(p0, p1);
    }
    public EtchedBorderUIResource(int p0) {
      super(p0);
    }
    public EtchedBorderUIResource() {
    }
  }

  public static class BevelBorderUIResource extends javax.swing.border.BevelBorder implements javax.swing.plaf.UIResource {
    public BevelBorderUIResource(int p0) {
      super(p0);
    }
    public BevelBorderUIResource(int p0, java.awt.Color p1, java.awt.Color p2) {
      super(p0, p1, p2);
    }
    public BevelBorderUIResource(int p0, java.awt.Color p1, java.awt.Color p2, java.awt.Color p3, java.awt.Color p4) {
      super(p0, p1, p2, p3, p4);
    }
  }

  public static class LineBorderUIResource extends javax.swing.border.LineBorder implements javax.swing.plaf.UIResource {
    public LineBorderUIResource(java.awt.Color p0) {
      super(p0);
    }
    public LineBorderUIResource(java.awt.Color p0, int p1) {
      super(p0, p1);
    }
  }

  public static class TitledBorderUIResource extends javax.swing.border.TitledBorder implements javax.swing.plaf.UIResource {
    public TitledBorderUIResource(javax.swing.border.Border p0, java.lang.String p1, int p2, int p3, java.awt.Font p4, java.awt.Color p5) {
      super(p0, p1, p2, p3, p4, p5);
    }
    public TitledBorderUIResource(javax.swing.border.Border p0, java.lang.String p1, int p2, int p3, java.awt.Font p4) {
      super(p0, p1, p2, p3, p4);
    }
    public TitledBorderUIResource(javax.swing.border.Border p0, java.lang.String p1, int p2, int p3) {
      super(p0, p1, p2, p3);
    }
    public TitledBorderUIResource(javax.swing.border.Border p0, java.lang.String p1) {
      super(p0, p1);
    }
    public TitledBorderUIResource(javax.swing.border.Border p0) {
      super(p0);
    }
    public TitledBorderUIResource(java.lang.String p0) {
      super(p0);
    }
  }

  public static class MatteBorderUIResource extends javax.swing.border.MatteBorder implements javax.swing.plaf.UIResource {
    public MatteBorderUIResource(int p0, int p1, int p2, int p3, java.awt.Color p4) {
      super(p0, p1, p2, p3, p4);
    }
    public MatteBorderUIResource(int p0, int p1, int p2, int p3, javax.swing.Icon p4) {
      super(p0, p1, p2, p3, p4);
    }
    public MatteBorderUIResource(javax.swing.Icon p0) {
      super(p0);
    }
  }

  public static class EmptyBorderUIResource extends javax.swing.border.EmptyBorder implements javax.swing.plaf.UIResource {
    public EmptyBorderUIResource(int p0, int p1, int p2, int p3) {
      super(p0, p1, p2, p3);
    }
    public EmptyBorderUIResource(java.awt.Insets p0) {
      super(p0);
    }
  }

  public static class CompoundBorderUIResource extends javax.swing.border.CompoundBorder implements javax.swing.plaf.UIResource {
    public CompoundBorderUIResource(javax.swing.border.Border p0, javax.swing.border.Border p1) {
      super(p0, p1);
    }
  }
}
