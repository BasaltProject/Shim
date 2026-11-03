/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf.synth;

public class SynthSliderUI extends javax.swing.plaf.basic.BasicSliderUI implements java.beans.PropertyChangeListener, javax.swing.plaf.synth.SynthUI {
  protected SynthSliderUI(javax.swing.JSlider p0) {
    super(p0);
  }
  public void update(java.awt.Graphics p0, javax.swing.JComponent p1) { javax.swing.plaf.synth.SynthSupport.update(p0, p1, Region.SLIDER); paint(p0, p1); }
  public javax.swing.plaf.synth.SynthContext getContext(javax.swing.JComponent p0) { return javax.swing.plaf.synth.SynthSupport.context(p0, Region.SLIDER, javax.swing.plaf.synth.SynthSupport.state(p0)); }
  protected void paintTrack(javax.swing.plaf.synth.SynthContext p0, java.awt.Graphics p1, java.awt.Rectangle p2) {  }
  protected void installDefaults(javax.swing.JSlider p0) { super.installDefaults(p0); }
  protected void installListeners(javax.swing.JSlider p0) { super.installListeners(p0); }
  protected void uninstallListeners(javax.swing.JSlider p0) { super.uninstallListeners(p0); }
  protected void uninstallDefaults(javax.swing.JSlider p0) { super.uninstallDefaults(p0); }
  protected javax.swing.plaf.basic.BasicSliderUI.TrackListener createTrackListener(javax.swing.JSlider p0) { return super.createTrackListener(p0); }
  protected void paintThumb(javax.swing.plaf.synth.SynthContext p0, java.awt.Graphics p1, java.awt.Rectangle p2) {  }
  protected void calculateGeometry() { super.calculateGeometry(); }
  protected java.awt.Dimension getThumbSize() { return super.getThumbSize(); }
  protected int yPositionForValue(int p0, int p1, int p2) { return super.yPositionForValue(p0, p1, p2); }
  protected void recalculateIfInsetsChanged() { super.recalculateIfInsetsChanged(); }
  protected void calculateThumbLocation() { super.calculateThumbLocation(); }
  protected int xPositionForValue(int p0) { return super.xPositionForValue(p0); }
  public void setThumbLocation(int p0, int p1) { super.setThumbLocation(p0, p1); }
  public int valueForYPosition(int p0) { return super.valueForYPosition(p0); }
  public int valueForXPosition(int p0) { return super.valueForXPosition(p0); }
  public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent p0) { return new SynthSliderUI((javax.swing.JSlider) p0); }
  protected void layout() {  }
  public void paint(java.awt.Graphics p0, javax.swing.JComponent p1) { super.paint(p0, p1); }
  protected void paint(javax.swing.plaf.synth.SynthContext p0, java.awt.Graphics p1) {  }
  public java.awt.Dimension getPreferredSize(javax.swing.JComponent p0) { return super.getPreferredSize(p0); }
  public java.awt.Dimension getMinimumSize(javax.swing.JComponent p0) { return super.getMinimumSize(p0); }
  public int getBaseline(javax.swing.JComponent p0, int p1, int p2) { return super.getBaseline(p0, p1, p2); }
  public void propertyChange(java.beans.PropertyChangeEvent p0) { javax.swing.plaf.synth.SynthSupport.propertyChange(p0, Region.SLIDER); }
  public void paintBorder(javax.swing.plaf.synth.SynthContext p0, java.awt.Graphics p1, int p2, int p3, int p4, int p5) {  }
  public void installUI(javax.swing.JComponent c) {
    super.installUI(c);
    javax.swing.plaf.synth.SynthSupport.install(c, Region.SLIDER);
  }
  public void uninstallUI(javax.swing.JComponent c) {
    javax.swing.plaf.synth.SynthSupport.uninstall(c, Region.SLIDER);
    super.uninstallUI(c);
  }
}
