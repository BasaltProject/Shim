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
    if (!slider.isEnabled()) {
      paintDisabledTrack(g);
      return;
    }
    boolean horizontal = slider.getOrientation() == JSlider.HORIZONTAL;
    boolean leftToRight = slider.getComponentOrientation().isLeftToRight();
    g.translate(trackRect.x, trackRect.y);
    int len = horizontal ? trackRect.width : trackRect.height;
    int cross = horizontal ? trackRect.height : trackRect.width;
    int first = (cross - (getTrackWidth() - 1)) / 2;
    int last = first + getTrackWidth() - 2;
    int fillEnd;
    if (horizontal) {
      fillEnd = thumbRect.x + thumbRect.width / 2 - trackRect.x;
    } else {
      fillEnd = thumbRect.y + thumbRect.height / 2 - trackRect.y;
    }
    Color primary = MetalLookAndFeel.getPrimaryControlDarkShadow();
    Color dark = MetalLookAndFeel.getControlDarkShadow();
    Color fg = slider.getForeground();
    Color[] fillRows = {UIManager.getColor("Slider.highlight"), UIManager.getColor("Slider.altTrackColor"),
        UIManager.getColor("Slider.shadow"), fg};
    boolean fillAtStart = horizontal && leftToRight;
    int fillFrom = fillAtStart ? 0 : fillEnd;
    int fillTo = fillAtStart ? fillEnd : len - 2;
    int emptyFrom = fillAtStart ? fillEnd + 1 : 0;
    int emptyTo = fillAtStart ? len - 2 : fillEnd - 1;
    for (int k = 0; k < 4; k++) {
      g.setColor(fillRows[k]);
      line(g, horizontal, fillFrom + 1, first + 1 + k, fillTo, first + 1 + k);
    }
    g.setColor(primary);
    line(g, horizontal, fillFrom, first, fillTo, first);
    line(g, horizontal, fillFrom, last, fillTo, last);
    int fillCap = fillAtStart ? fillFrom : fillTo;
    line(g, horizontal, fillCap, first, fillCap, last);
    if (!horizontal) {
      g.setColor(fg);
      line(g, horizontal, emptyFrom, first + 1, emptyTo, first + 1);
    }
    g.setColor(dark);
    line(g, horizontal, emptyFrom, first, emptyTo, first);
    line(g, horizontal, emptyFrom, last, emptyTo, last);
    int emptyCap = fillAtStart ? emptyTo : emptyFrom;
    line(g, horizontal, emptyCap, first, emptyCap, last);
    if (horizontal) {
      g.setColor(fg);
      line(g, horizontal, emptyFrom, first + 1, emptyTo, first + 1);
    }
    g.translate(-trackRect.x, -trackRect.y);
  }

  private static void line(Graphics g, boolean horizontal, int l1, int t1, int l2, int t2) {
    if (horizontal) {
      g.fillRect(l1, t1, l2 - l1 + 1, t2 - t1 + 1);
    } else {
      g.fillRect(t1, l1, t2 - t1 + 1, l2 - l1 + 1);
    }
  }

  private void paintDisabledTrack(Graphics g) {
    Color trackColor = UIManager.getColor("Slider.shadow");
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
