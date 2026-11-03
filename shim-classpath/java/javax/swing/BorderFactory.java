/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.BasicStroke;
import javax.swing.border.*;

public class BorderFactory {
  BorderFactory() {
  }
  public static javax.swing.border.Border createRaisedSoftBevelBorder() { return new SoftBevelBorder(BevelBorder.RAISED); }
  public static javax.swing.border.Border createLoweredSoftBevelBorder() { return new SoftBevelBorder(BevelBorder.LOWERED); }
  public static javax.swing.border.Border createDashedBorder(java.awt.Paint p0, float p1, float p2, float p3, boolean p4) { return new StrokeBorder(new BasicStroke(p1, p4 ? BasicStroke.CAP_ROUND : BasicStroke.CAP_BUTT, p4 ? BasicStroke.JOIN_ROUND : BasicStroke.JOIN_MITER, 10.0f, new float[] {p2}, p3), p0); }
  public static javax.swing.border.Border createDashedBorder(java.awt.Paint p0) { return createDashedBorder(p0, 1.0f, 1.0f, 1.0f, false); }
  public static javax.swing.border.Border createDashedBorder(java.awt.Paint p0, float p1, float p2) { return createDashedBorder(p0, 1.0f, p1, p2, false); }
  public static javax.swing.border.Border createStrokeBorder(java.awt.BasicStroke p0, java.awt.Paint p1) { return new StrokeBorder(p0, p1); }
  public static javax.swing.border.Border createStrokeBorder(java.awt.BasicStroke p0) { return new StrokeBorder(p0); }
  public static javax.swing.border.Border createLineBorder(java.awt.Color p0) { return new LineBorder(p0, 1); }
  public static javax.swing.border.Border createLineBorder(java.awt.Color p0, int p1, boolean p2) { return new LineBorder(p0, p1, p2); }
  public static javax.swing.border.Border createLineBorder(java.awt.Color p0, int p1) { return new LineBorder(p0, p1); }
  public static javax.swing.border.Border createRaisedBevelBorder() { return new BevelBorder(BevelBorder.RAISED); }
  public static javax.swing.border.Border createLoweredBevelBorder() { return new BevelBorder(BevelBorder.LOWERED); }
  public static javax.swing.border.Border createBevelBorder(int p0, java.awt.Color p1, java.awt.Color p2, java.awt.Color p3, java.awt.Color p4) { return new BevelBorder(p0, p1, p2, p3, p4); }
  public static javax.swing.border.Border createBevelBorder(int p0, java.awt.Color p1, java.awt.Color p2) { return new BevelBorder(p0, p1, p2); }
  public static javax.swing.border.Border createBevelBorder(int p0) { return new BevelBorder(p0); }
  public static javax.swing.border.Border createSoftBevelBorder(int p0, java.awt.Color p1, java.awt.Color p2) { return new SoftBevelBorder(p0, p1, p2); }
  public static javax.swing.border.Border createSoftBevelBorder(int p0) { return new SoftBevelBorder(p0); }
  public static javax.swing.border.Border createSoftBevelBorder(int p0, java.awt.Color p1, java.awt.Color p2, java.awt.Color p3, java.awt.Color p4) { return new SoftBevelBorder(p0, p1, p2, p3, p4); }
  public static javax.swing.border.Border createEtchedBorder(int p0, java.awt.Color p1, java.awt.Color p2) { return new EtchedBorder(p0, p1, p2); }
  public static javax.swing.border.Border createEtchedBorder(int p0) { return new EtchedBorder(p0); }
  public static javax.swing.border.Border createEtchedBorder(java.awt.Color p0, java.awt.Color p1) { return new EtchedBorder(p0, p1); }
  public static javax.swing.border.Border createEtchedBorder() { return new EtchedBorder(); }
  public static javax.swing.border.TitledBorder createTitledBorder(java.lang.String p0) { return new TitledBorder(p0); }
  public static javax.swing.border.TitledBorder createTitledBorder(javax.swing.border.Border p0) { return new TitledBorder(p0); }
  public static javax.swing.border.TitledBorder createTitledBorder(javax.swing.border.Border p0, java.lang.String p1, int p2, int p3, java.awt.Font p4) { return new TitledBorder(p0, p1, p2, p3, p4); }
  public static javax.swing.border.TitledBorder createTitledBorder(javax.swing.border.Border p0, java.lang.String p1, int p2, int p3) { return new TitledBorder(p0, p1, p2, p3); }
  public static javax.swing.border.TitledBorder createTitledBorder(javax.swing.border.Border p0, java.lang.String p1, int p2, int p3, java.awt.Font p4, java.awt.Color p5) { return new TitledBorder(p0, p1, p2, p3, p4, p5); }
  public static javax.swing.border.TitledBorder createTitledBorder(javax.swing.border.Border p0, java.lang.String p1) { return new TitledBorder(p0, p1); }
  public static javax.swing.border.Border createEmptyBorder(int p0, int p1, int p2, int p3) { return new EmptyBorder(p0, p1, p2, p3); }
  public static javax.swing.border.Border createEmptyBorder() { return new EmptyBorder(0, 0, 0, 0); }
  public static javax.swing.border.CompoundBorder createCompoundBorder(javax.swing.border.Border p0, javax.swing.border.Border p1) { return new CompoundBorder(p0, p1); }
  public static javax.swing.border.CompoundBorder createCompoundBorder() { return new CompoundBorder(); }
  public static javax.swing.border.MatteBorder createMatteBorder(int p0, int p1, int p2, int p3, javax.swing.Icon p4) { return new MatteBorder(p0, p1, p2, p3, p4); }
  public static javax.swing.border.MatteBorder createMatteBorder(int p0, int p1, int p2, int p3, java.awt.Color p4) { return new MatteBorder(p0, p1, p2, p3, p4); }
}
