/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf.metal;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.UIManager;

public class MetalSliderUI extends javax.swing.plaf.basic.BasicSliderUI {
  protected final int TICK_BUFFER = 4;
  protected boolean filledSlider;
  protected static java.awt.Color thumbColor;
  protected static java.awt.Color highlightColor;
  protected static java.awt.Color darkShadowColor;
  protected static int trackWidth;
  protected static int tickLength;
  protected static javax.swing.Icon horizThumbIcon;
  protected static javax.swing.Icon vertThumbIcon;
  protected final String SLIDER_FILL = "JSlider.isFilled";
  public MetalSliderUI() {
    super((JSlider) null);
  }
  public void paintFocus(Graphics g) {
  }
  public void paintTrack(Graphics g) {
    Color trackColor = !slider.isEnabled() ? UIManager.getColor("Slider.shadow") : slider.getForeground();
    boolean leftToRight = slider.getComponentOrientation().isLeftToRight();
    g.translate(trackRect.x, trackRect.y);
    int trackLeft = 0;
    int trackTop = 0;
    int trackRight;
    int trackBottom;
    if (slider.getOrientation() == JSlider.HORIZONTAL) {
      trackBottom = (trackRect.height - 1) - getThumbOverhang();
      trackTop = trackBottom - (getTrackWidth() - 1);
      trackRight = trackRect.width - 1;
    } else {
      if (leftToRight) {
        trackLeft = (trackRect.width - getThumbOverhang()) - getTrackWidth();
        trackRight = (trackRect.width - getThumbOverhang()) - 1;
      } else {
        trackLeft = getThumbOverhang();
        trackRight = getThumbOverhang() + getTrackWidth() - 1;
      }
      trackBottom = trackRect.height - 1;
    }
    g.setColor(trackColor);
    g.drawRect(trackLeft, trackTop, trackRight - trackLeft, trackBottom - trackTop);
    g.translate(-trackRect.x, -trackRect.y);
  }
  protected java.beans.PropertyChangeListener createPropertyChangeListener(JSlider slider) {
    return new MetalPropertyListener();
  }
  public void paintThumb(Graphics g) {
    Rectangle knobBounds = thumbRect;
    g.translate(knobBounds.x, knobBounds.y);
    if (slider.getOrientation() == JSlider.HORIZONTAL) {
      horizThumbIcon.paintIcon(slider, g, 0, 0);
    } else {
      vertThumbIcon.paintIcon(slider, g, 0, 0);
    }
    g.translate(-knobBounds.x, -knobBounds.y);
  }
  protected Dimension getThumbSize() {
    Dimension size = new Dimension();
    if (slider.getOrientation() == JSlider.VERTICAL) {
      size.width = vertThumbIcon.getIconWidth();
      size.height = vertThumbIcon.getIconHeight();
    } else {
      size.width = horizThumbIcon.getIconWidth();
      size.height = horizThumbIcon.getIconHeight();
    }
    return size;
  }
  protected void paintMinorTickForHorizSlider(Graphics g, Rectangle tickBounds, int x) {
    g.setColor(slider.isEnabled() ? slider.getForeground() : UIManager.getColor("Slider.shadow"));
    g.drawLine(x, TICK_BUFFER, x, TICK_BUFFER + (tickLength / 2));
  }
  protected void paintMajorTickForHorizSlider(Graphics g, Rectangle tickBounds, int x) {
    g.setColor(slider.isEnabled() ? slider.getForeground() : UIManager.getColor("Slider.shadow"));
    g.drawLine(x, TICK_BUFFER, x, TICK_BUFFER + (tickLength - 1));
  }
  protected void paintMinorTickForVertSlider(Graphics g, Rectangle tickBounds, int y) {
    g.setColor(slider.isEnabled() ? slider.getForeground() : UIManager.getColor("Slider.shadow"));
    if (slider.getComponentOrientation().isLeftToRight()) {
      g.drawLine(TICK_BUFFER, y, TICK_BUFFER + (tickLength / 2), y);
    } else {
      g.drawLine(0, y, tickLength / 2, y);
    }
  }
  protected void paintMajorTickForVertSlider(Graphics g, Rectangle tickBounds, int y) {
    g.setColor(slider.isEnabled() ? slider.getForeground() : UIManager.getColor("Slider.shadow"));
    if (slider.getComponentOrientation().isLeftToRight()) {
      g.drawLine(TICK_BUFFER, y, TICK_BUFFER + tickLength, y);
    } else {
      g.drawLine(0, y, tickLength, y);
    }
  }
  protected int getThumbOverhang() {
    return 2;
  }
  protected int getTrackWidth() {
    return trackWidth;
  }
  protected int getTrackLength() {
    if (slider.getOrientation() == JSlider.HORIZONTAL) {
      return trackRect.width;
    }
    return trackRect.height;
  }
  protected void scrollDueToClickInTrack(int dir) {
    scrollByUnit(dir);
  }
  public int getTickLength() {
    return slider.getOrientation() == JSlider.HORIZONTAL ? tickLength + TICK_BUFFER + 1 : tickLength + TICK_BUFFER + 3;
  }
  public static javax.swing.plaf.ComponentUI createUI(JComponent c) {
    return new MetalSliderUI();
  }
  public void installUI(JComponent c) {
    trackWidth = ((Integer) UIManager.get("Slider.trackWidth")).intValue();
    tickLength = ((Integer) UIManager.get("Slider.majorTickLength")).intValue();
    horizThumbIcon = UIManager.getIcon("Slider.horizontalThumbIcon");
    vertThumbIcon = UIManager.getIcon("Slider.verticalThumbIcon");
    super.installUI(c);
    thumbColor = UIManager.getColor("Slider.thumb");
    highlightColor = UIManager.getColor("Slider.highlight");
    darkShadowColor = UIManager.getColor("Slider.darkShadow");
    scrollListener.setScrollByBlock(false);
    Object sliderFillProp = c.getClientProperty(SLIDER_FILL);
    if (sliderFillProp != null) {
      filledSlider = ((Boolean) sliderFillProp).booleanValue();
    }
  }

  protected class MetalPropertyListener extends javax.swing.plaf.basic.BasicSliderUI.PropertyChangeHandler {
    public void propertyChange(java.beans.PropertyChangeEvent e) {
      super.propertyChange(e);
      String name = e.getPropertyName();
      if (name.equals(SLIDER_FILL)) {
        if (e.getNewValue() != null) {
          filledSlider = ((Boolean) e.getNewValue()).booleanValue();
        } else {
          filledSlider = false;
        }
      } else if (name.equals("JSlider.isFilled")) {
        filledSlider = Boolean.TRUE.equals(e.getNewValue());
      }
    }
  }
}
