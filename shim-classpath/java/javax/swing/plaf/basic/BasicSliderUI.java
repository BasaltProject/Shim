/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf.basic;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.Dictionary;
import java.util.Enumeration;
import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.BoundedRangeModel;
import javax.swing.event.ChangeEvent;

public class BasicSliderUI extends javax.swing.plaf.SliderUI {
  private java.awt.Dimension bp_PreferredVerticalSize;
  private java.awt.Dimension bp_PreferredHorizontalSize;
  private java.awt.Dimension bp_MinimumVerticalSize;
  private java.awt.Dimension bp_MinimumHorizontalSize;
  public static final int POSITIVE_SCROLL = 1;
  public static final int NEGATIVE_SCROLL = -1;
  public static final int MIN_SCROLL = -2;
  public static final int MAX_SCROLL = 2;
  protected javax.swing.Timer scrollTimer;
  protected javax.swing.JSlider slider;
  protected java.awt.Insets focusInsets;
  protected java.awt.Insets insetCache;
  protected boolean leftToRightCache;
  protected java.awt.Rectangle focusRect;
  protected java.awt.Rectangle contentRect;
  protected java.awt.Rectangle labelRect;
  protected java.awt.Rectangle tickRect;
  protected java.awt.Rectangle trackRect;
  protected java.awt.Rectangle thumbRect;
  protected int trackBuffer;
  protected javax.swing.plaf.basic.BasicSliderUI.TrackListener trackListener;
  protected javax.swing.event.ChangeListener changeListener;
  protected java.awt.event.ComponentListener componentListener;
  protected java.awt.event.FocusListener focusListener;
  protected javax.swing.plaf.basic.BasicSliderUI.ScrollListener scrollListener;
  protected java.beans.PropertyChangeListener propertyChangeListener;
  public BasicSliderUI(JSlider b) {
  }
  public void paintFocus(Graphics g) {
    g.setColor(getFocusColor());
    javax.swing.plaf.basic.BasicGraphicsUtils.drawDashedRect(g, focusRect.x, focusRect.y, focusRect.width,
        focusRect.height);
  }
  protected javax.swing.event.ChangeListener createChangeListener(JSlider slider) {
    return new ChangeHandler();
  }
  public void paintTicks(Graphics g) {
    Rectangle tickBounds = tickRect;
    g.setColor(UIManager.getColor("Slider.tickColor") == null ? Color.black : UIManager.getColor("Slider.tickColor"));
    if (slider.getOrientation() == JSlider.HORIZONTAL) {
      g.translate(0, tickBounds.y);
      if (slider.getMinorTickSpacing() > 0) {
        int value = slider.getMinimum();
        while (value <= slider.getMaximum()) {
          int xPos = xPositionForValue(value);
          paintMinorTickForHorizSlider(g, tickBounds, xPos);
          if (Integer.MAX_VALUE - slider.getMinorTickSpacing() < value) {
            break;
          }
          value += slider.getMinorTickSpacing();
        }
      }
      if (slider.getMajorTickSpacing() > 0) {
        int value = slider.getMinimum();
        while (value <= slider.getMaximum()) {
          int xPos = xPositionForValue(value);
          paintMajorTickForHorizSlider(g, tickBounds, xPos);
          if (Integer.MAX_VALUE - slider.getMajorTickSpacing() < value) {
            break;
          }
          value += slider.getMajorTickSpacing();
        }
      }
      g.translate(0, -tickBounds.y);
    } else {
      g.translate(tickBounds.x, 0);
      if (slider.getMinorTickSpacing() > 0) {
        int offset = 0;
        if (!slider.getComponentOrientation().isLeftToRight()) {
          offset = tickBounds.width - tickBounds.width / 2;
          g.translate(offset, 0);
        }
        int value = slider.getMinimum();
        while (value <= slider.getMaximum()) {
          int yPos = yPositionForValue(value);
          paintMinorTickForVertSlider(g, tickBounds, yPos);
          if (Integer.MAX_VALUE - slider.getMinorTickSpacing() < value) {
            break;
          }
          value += slider.getMinorTickSpacing();
        }
        if (!slider.getComponentOrientation().isLeftToRight()) {
          g.translate(-offset, 0);
        }
      }
      if (slider.getMajorTickSpacing() > 0) {
        if (!slider.getComponentOrientation().isLeftToRight()) {
          g.translate(2, 0);
        }
        int value = slider.getMinimum();
        while (value <= slider.getMaximum()) {
          int yPos = yPositionForValue(value);
          paintMajorTickForVertSlider(g, tickBounds, yPos);
          if (Integer.MAX_VALUE - slider.getMajorTickSpacing() < value) {
            break;
          }
          value += slider.getMajorTickSpacing();
        }
        if (!slider.getComponentOrientation().isLeftToRight()) {
          g.translate(-2, 0);
        }
      }
      g.translate(-tickBounds.x, 0);
    }
  }
  public void paintTrack(Graphics g) {
    Rectangle trackBounds = trackRect;
    if (slider.getOrientation() == JSlider.HORIZONTAL) {
      int cy = (trackBounds.height / 2) - 2;
      int cw = trackBounds.width;
      g.translate(trackBounds.x, trackBounds.y + cy);
      g.setColor(getShadowColor());
      g.drawLine(0, 0, cw - 1, 0);
      g.drawLine(0, 1, 0, 2);
      g.setColor(getHighlightColor());
      g.drawLine(0, 3, cw, 3);
      g.drawLine(cw, 0, cw, 3);
      g.setColor(java.awt.Color.black);
      g.drawLine(1, 1, cw - 2, 1);
      g.translate(-trackBounds.x, -(trackBounds.y + cy));
    } else {
      int cx = (trackBounds.width / 2) - 2;
      int ch = trackBounds.height;
      g.translate(trackBounds.x + cx, trackBounds.y);
      g.setColor(getShadowColor());
      g.drawLine(0, 0, 0, ch - 1);
      g.drawLine(1, 0, 2, 0);
      g.setColor(getHighlightColor());
      g.drawLine(3, 0, 3, ch);
      g.drawLine(0, ch, 3, ch);
      g.setColor(java.awt.Color.black);
      g.drawLine(1, 1, 1, ch - 2);
      g.translate(-(trackBounds.x + cx), -trackBounds.y);
    }
  }
  public void paintLabels(Graphics g) {
    Rectangle labelBounds = labelRect;
    Dictionary<?, ?> dictionary = slider.getLabelTable();
    if (dictionary != null) {
      Enumeration<?> keys = dictionary.keys();
      int minValue = slider.getMinimum();
      int maxValue = slider.getMaximum();
      boolean enabled = slider.isEnabled();
      while (keys.hasMoreElements()) {
        Integer key = (Integer) keys.nextElement();
        int value = key.intValue();
        if (value >= minValue && value <= maxValue) {
          Component label = (Component) dictionary.get(key);
          if (label instanceof javax.swing.JLabel) {
            javax.swing.JLabel jl = (javax.swing.JLabel) label;
            jl.setEnabled(enabled);
          }
          if (slider.getOrientation() == JSlider.HORIZONTAL) {
            g.translate(0, labelBounds.y);
            paintHorizontalLabel(g, value, label);
            g.translate(0, -labelBounds.y);
          } else {
            int offset = 0;
            if (!slider.getComponentOrientation().isLeftToRight()) {
              offset = labelBounds.width - label.getPreferredSize().width;
            }
            g.translate(labelBounds.x + offset, 0);
            paintVerticalLabel(g, value, label);
            g.translate(-labelBounds.x - offset, 0);
          }
        }
      }
    }
  }
  protected java.awt.event.FocusListener createFocusListener(JSlider slider) {
    return new FocusHandler();
  }
  protected java.awt.event.ComponentListener createComponentListener(JSlider slider) {
    return new ComponentHandler();
  }
  protected void installKeyboardActions(JSlider slider) {
    shim.swing.KeyActions.install(slider, JComponent.WHEN_FOCUSED, "Slider.focusInputMap",
        shim.swing.KeyActions.sliderActions());
  }
  protected void uninstallKeyboardActions(JSlider slider) {
    shim.swing.KeyActions.uninstall(slider, JComponent.WHEN_FOCUSED);
  }
  protected void installDefaults(JSlider slider) {
    LookAndFeel.installColorsAndFont(slider, "Slider.background", "Slider.foreground", "Slider.font");
    highlightColor = UIManager.getColor("Slider.highlight");
    shadowColor = UIManager.getColor("Slider.shadow");
    focusColor = UIManager.getColor("Slider.focus");
    focusInsets = UIManager.getInsets("Slider.focusInsets");
    if (focusInsets == null) {
      focusInsets = new Insets(0, 0, 0, 0);
    }
    preferredHorizontalSize = defaultSize("Slider.horizontalSize", 200, 21);
    preferredVerticalSize = defaultSize("Slider.verticalSize", 21, 200);
    minimumHorizontalSize = defaultSize("Slider.minimumHorizontalSize", 36, 21);
    minimumVerticalSize = defaultSize("Slider.minimumVerticalSize", 21, 36);
  }
  protected void installListeners(JSlider slider) {
    slider.addMouseListener(trackListener);
    slider.addMouseMotionListener(trackListener);
    slider.addFocusListener(focusListener);
    slider.addComponentListener(componentListener);
    slider.addPropertyChangeListener(propertyChangeListener);
    slider.getModel().addChangeListener(changeListener);
  }
  protected void uninstallListeners(JSlider slider) {
    slider.removeMouseListener(trackListener);
    slider.removeMouseMotionListener(trackListener);
    slider.removeFocusListener(focusListener);
    slider.removeComponentListener(componentListener);
    slider.removePropertyChangeListener(propertyChangeListener);
    slider.getModel().removeChangeListener(changeListener);
  }
  protected void uninstallDefaults(JSlider slider) {
    LookAndFeel.uninstallBorder(slider);
    focusInsets = null;
  }
  protected java.beans.PropertyChangeListener createPropertyChangeListener(JSlider slider) {
    return new PropertyChangeHandler();
  }
  protected TrackListener createTrackListener(JSlider slider) {
    return new TrackListener();
  }
  protected ScrollListener createScrollListener(JSlider slider) {
    return new ScrollListener();
  }
  public void paintThumb(Graphics g) {
    Rectangle knobBounds = thumbRect;
    int w = knobBounds.width;
    int h = knobBounds.height;
    g.translate(knobBounds.x, knobBounds.y);
    if (slider.isEnabled()) {
      g.setColor(slider.getBackground());
    } else {
      g.setColor(slider.getBackground().darker());
    }
    g.fillRect(0, 0, w - 1, h - 1);
    g.setColor(Color.black);
    g.drawRect(0, 0, w - 1, h - 1);
    g.translate(-knobBounds.x, -knobBounds.y);
  }
  public void scrollByBlock(int direction) {
    synchronized (slider) {
      int blockIncrement = (slider.getMaximum() - slider.getMinimum()) / 10;
      if (blockIncrement == 0) {
        blockIncrement = 1;
      }
      if (slider.getSnapToTicks()) {
        int tickSpacing = getTickSpacing();
        if (blockIncrement < tickSpacing) {
          blockIncrement = tickSpacing;
        }
      }
      int delta = blockIncrement * ((direction > 0) ? POSITIVE_SCROLL : NEGATIVE_SCROLL);
      slider.setValue(slider.getValue() + delta);
    }
  }
  public void scrollByUnit(int direction) {
    synchronized (slider) {
      int delta = ((direction > 0) ? POSITIVE_SCROLL : NEGATIVE_SCROLL);
      if (slider.getSnapToTicks()) {
        delta *= getTickSpacing();
      }
      slider.setValue(slider.getValue() + delta);
    }
  }
  protected void calculateGeometry() {
    calculateFocusRect();
    calculateContentRect();
    calculateThumbSize();
    calculateTrackBuffer();
    calculateTrackRect();
    calculateTickRect();
    calculateLabelRect();
    calculateThumbLocation();
  }
  protected boolean labelsHaveSameBaselines() {
    return true;
  }
  protected Dimension getThumbSize() {
    Dimension size = new Dimension();
    if (slider.getOrientation() == JSlider.VERTICAL) {
      size.width = 20;
      size.height = 11;
    } else {
      size.width = 11;
      size.height = 20;
    }
    return size;
  }
  protected int getHeightOfTallestLabel() {
    Dictionary<?, ?> dictionary = slider.getLabelTable();
    int tallest = 0;
    if (dictionary != null) {
      Enumeration<?> keys = dictionary.keys();
      while (keys.hasMoreElements()) {
        JComponent label = (JComponent) dictionary.get(keys.nextElement());
        tallest = Math.max(label.getPreferredSize().height, tallest);
      }
    }
    return tallest;
  }
  protected Integer getLowestValue() {
    Dictionary<?, ?> dictionary = slider.getLabelTable();
    if (dictionary == null) {
      return null;
    }
    Enumeration<?> keys = dictionary.keys();
    Integer min = null;
    while (keys.hasMoreElements()) {
      Integer i = (Integer) keys.nextElement();
      if (min == null || i.intValue() < min.intValue()) {
        min = i;
      }
    }
    return min;
  }
  protected Integer getHighestValue() {
    Dictionary<?, ?> dictionary = slider.getLabelTable();
    if (dictionary == null) {
      return null;
    }
    Enumeration<?> keys = dictionary.keys();
    Integer max = null;
    while (keys.hasMoreElements()) {
      Integer i = (Integer) keys.nextElement();
      if (max == null || i.intValue() > max.intValue()) {
        max = i;
      }
    }
    return max;
  }
  protected int yPositionForValue(int value) {
    return yPositionForValue(value, trackRect.y, trackRect.height);
  }
  protected int yPositionForValue(int value, int trackY, int trackHeight) {
    int min = slider.getMinimum();
    int max = slider.getMaximum();
    double valueRange = (double) max - (double) min;
    double pixelsPerValue = (double) trackHeight / valueRange;
    int trackBottom = trackY + (trackHeight - 1);
    int yPosition;
    if (!drawInverted()) {
      yPosition = trackY;
      yPosition += Math.round(pixelsPerValue * ((double) max - value));
    } else {
      yPosition = trackY;
      yPosition += Math.round(pixelsPerValue * ((double) value - min));
    }
    yPosition = Math.max(trackY, yPosition);
    yPosition = Math.min(trackBottom, yPosition);
    return yPosition;
  }
  protected void recalculateIfInsetsChanged() {
    Insets newInsets = slider.getInsets();
    if (!newInsets.equals(insetCache)) {
      insetCache = newInsets;
      calculateGeometry();
    }
  }
  public Dimension getPreferredVerticalSize() {
    return new Dimension(preferredVerticalSize);
  }
  public Dimension getPreferredHorizontalSize() {
    return new Dimension(preferredHorizontalSize);
  }
  public Dimension getMinimumVerticalSize() {
    return new Dimension(minimumVerticalSize);
  }
  public Dimension getMinimumHorizontalSize() {
    return new Dimension(minimumHorizontalSize);
  }
  protected void calculateFocusRect() {
    focusRect.x = insetCache.left;
    focusRect.y = insetCache.top;
    focusRect.width = slider.getWidth() - (insetCache.left + insetCache.right);
    focusRect.height = slider.getHeight() - (insetCache.top + insetCache.bottom);
  }
  protected void calculateContentRect() {
    contentRect.x = focusRect.x + focusInsets.left;
    contentRect.y = focusRect.y + focusInsets.top;
    contentRect.width = focusRect.width - (focusInsets.left + focusInsets.right);
    contentRect.height = focusRect.height - (focusInsets.top + focusInsets.bottom);
  }
  protected void calculateThumbSize() {
    Dimension size = getThumbSize();
    thumbRect.setSize(size.width, size.height);
  }
  protected void calculateTrackBuffer() {
    if (slider.getPaintLabels() && slider.getLabelTable() != null) {
      Component highLabel = getHighestValueLabel();
      Component lowLabel = getLowestValueLabel();
      if (slider.getOrientation() == JSlider.HORIZONTAL) {
        trackBuffer = Math.max(highLabel.getPreferredSize().width, lowLabel.getPreferredSize().width) / 2;
        trackBuffer = Math.max(trackBuffer, thumbRect.width / 2);
      } else {
        trackBuffer = Math.max(highLabel.getPreferredSize().height, lowLabel.getPreferredSize().height) / 2;
        trackBuffer = Math.max(trackBuffer, thumbRect.height / 2);
      }
    } else if (slider.getOrientation() == JSlider.HORIZONTAL) {
      trackBuffer = thumbRect.width / 2;
    } else {
      trackBuffer = thumbRect.height / 2;
    }
  }
  protected void calculateTrackRect() {
    int centerSpacing;
    if (slider.getOrientation() == JSlider.HORIZONTAL) {
      centerSpacing = thumbRect.height;
      if (slider.getPaintTicks()) {
        centerSpacing += getTickLength();
      }
      if (slider.getPaintLabels()) {
        centerSpacing += getHeightOfTallestLabel();
      }
      trackRect.x = contentRect.x + trackBuffer;
      trackRect.y = contentRect.y + (contentRect.height - centerSpacing - 1) / 2;
      trackRect.width = contentRect.width - (trackBuffer * 2);
      trackRect.height = thumbRect.height;
    } else {
      centerSpacing = thumbRect.width;
      if (slider.getComponentOrientation().isLeftToRight()) {
        if (slider.getPaintTicks()) {
          centerSpacing += getTickLength();
        }
        if (slider.getPaintLabels()) {
          centerSpacing += getWidthOfWidestLabel();
        }
      } else {
        if (slider.getPaintTicks()) {
          centerSpacing -= getTickLength();
        }
        if (slider.getPaintLabels()) {
          centerSpacing -= getWidthOfWidestLabel();
        }
      }
      trackRect.x = contentRect.x + (contentRect.width - centerSpacing - 1) / 2;
      trackRect.y = contentRect.y + trackBuffer;
      trackRect.width = thumbRect.width;
      trackRect.height = contentRect.height - (trackBuffer * 2);
    }
  }
  protected void calculateTickRect() {
    if (slider.getOrientation() == JSlider.HORIZONTAL) {
      tickRect.x = trackRect.x;
      tickRect.y = trackRect.y + trackRect.height;
      tickRect.width = trackRect.width;
      tickRect.height = slider.getPaintTicks() ? getTickLength() : 0;
    } else {
      tickRect.width = slider.getPaintTicks() ? getTickLength() : 0;
      if (slider.getComponentOrientation().isLeftToRight()) {
        tickRect.x = trackRect.x + trackRect.width;
      } else {
        tickRect.x = trackRect.x - tickRect.width;
      }
      tickRect.y = trackRect.y;
      tickRect.height = trackRect.height;
    }
  }
  protected void calculateLabelRect() {
    if (slider.getPaintLabels()) {
      if (slider.getOrientation() == JSlider.HORIZONTAL) {
        labelRect.x = tickRect.x - trackBuffer;
        labelRect.y = tickRect.y + tickRect.height;
        labelRect.width = tickRect.width + (trackBuffer * 2);
        labelRect.height = getHeightOfTallestLabel();
      } else {
        if (slider.getComponentOrientation().isLeftToRight()) {
          labelRect.x = tickRect.x + tickRect.width;
          labelRect.width = getWidthOfWidestLabel();
        } else {
          labelRect.width = getWidthOfWidestLabel();
          labelRect.x = tickRect.x - labelRect.width;
        }
        labelRect.y = tickRect.y - trackBuffer;
        labelRect.height = tickRect.height + (trackBuffer * 2);
      }
    } else {
      if (slider.getOrientation() == JSlider.HORIZONTAL) {
        labelRect.x = tickRect.x;
        labelRect.y = tickRect.y + tickRect.height;
        labelRect.width = tickRect.width;
        labelRect.height = 0;
      } else {
        if (slider.getComponentOrientation().isLeftToRight()) {
          labelRect.x = tickRect.x + tickRect.width;
        } else {
          labelRect.x = tickRect.x;
        }
        labelRect.y = tickRect.y;
        labelRect.width = 0;
        labelRect.height = tickRect.height;
      }
    }
  }
  protected void calculateThumbLocation() {
    if (slider.getSnapToTicks()) {
      int sliderValue = slider.getValue();
      int snappedValue = sliderValue;
      int tickSpacing = getTickSpacing();
      if (tickSpacing != 0) {
        if ((sliderValue - slider.getMinimum()) % tickSpacing != 0) {
          float temp = (float) (sliderValue - slider.getMinimum()) / (float) tickSpacing;
          int whichTick = Math.round(temp);
          snappedValue = slider.getMinimum() + (whichTick * tickSpacing);
        }
        if (snappedValue != sliderValue) {
          slider.setValue(snappedValue);
        }
      }
    }
    if (slider.getOrientation() == JSlider.HORIZONTAL) {
      int valuePosition = xPositionForValue(slider.getValue());
      thumbRect.x = valuePosition - (thumbRect.width / 2);
      thumbRect.y = trackRect.y;
    } else {
      int valuePosition = yPositionForValue(slider.getValue());
      thumbRect.x = trackRect.x;
      thumbRect.y = valuePosition - (thumbRect.height / 2);
    }
  }
  protected int xPositionForValue(int value) {
    int min = slider.getMinimum();
    int max = slider.getMaximum();
    int trackLength = trackRect.width;
    double valueRange = (double) max - (double) min;
    double pixelsPerValue = (double) trackLength / valueRange;
    int trackLeft = trackRect.x;
    int trackRight = trackRect.x + (trackRect.width - 1);
    int xPosition;
    if (!drawInverted()) {
      xPosition = trackLeft;
      xPosition += Math.round(pixelsPerValue * ((double) value - min));
    } else {
      xPosition = trackRight;
      xPosition -= Math.round(pixelsPerValue * ((double) value - min));
    }
    xPosition = Math.max(trackLeft, xPosition);
    xPosition = Math.min(trackRight, xPosition);
    return xPosition;
  }
  protected Component getHighestValueLabel() {
    Integer max = getHighestValue();
    return max != null ? (Component) slider.getLabelTable().get(max) : null;
  }
  protected Component getLowestValueLabel() {
    Integer min = getLowestValue();
    return min != null ? (Component) slider.getLabelTable().get(min) : null;
  }
  protected int getWidthOfWidestLabel() {
    Dictionary<?, ?> dictionary = slider.getLabelTable();
    int widest = 0;
    if (dictionary != null) {
      Enumeration<?> keys = dictionary.keys();
      while (keys.hasMoreElements()) {
        JComponent label = (JComponent) dictionary.get(keys.nextElement());
        widest = Math.max(label.getPreferredSize().width, widest);
      }
    }
    return widest;
  }
  protected void recalculateIfOrientationChanged() {
    boolean ltr = slider.getComponentOrientation().isLeftToRight();
    if (ltr != leftToRightCache) {
      leftToRightCache = ltr;
      calculateGeometry();
    }
  }
  protected Color getFocusColor() {
    return focusColor;
  }
  protected void paintMinorTickForHorizSlider(Graphics g, Rectangle tickBounds, int x) {
    g.drawLine(x, 0, x, tickBounds.height / 2 - 1);
  }
  protected void paintMajorTickForHorizSlider(Graphics g, Rectangle tickBounds, int x) {
    g.drawLine(x, 0, x, tickBounds.height - 2);
  }
  protected void paintMinorTickForVertSlider(Graphics g, Rectangle tickBounds, int y) {
    g.drawLine(0, y, tickBounds.width / 2 - 1, y);
  }
  protected void paintMajorTickForVertSlider(Graphics g, Rectangle tickBounds, int y) {
    g.drawLine(0, y, tickBounds.width - 2, y);
  }
  protected void paintHorizontalLabel(Graphics g, int value, Component label) {
    int labelCenter = xPositionForValue(value);
    int labelLeft = labelCenter - (label.getPreferredSize().width / 2);
    g.translate(labelLeft, 0);
    label.setSize(label.getPreferredSize());
    label.paint(g);
    g.translate(-labelLeft, 0);
  }
  protected boolean isDragging() {
    return isDragging;
  }
  protected void paintVerticalLabel(Graphics g, int value, Component label) {
    int labelCenter = yPositionForValue(value);
    int labelTop = labelCenter - (label.getPreferredSize().height / 2);
    g.translate(0, labelTop);
    label.setSize(label.getPreferredSize());
    label.paint(g);
    g.translate(0, -labelTop);
  }
  protected boolean drawInverted() {
    if (slider.getOrientation() == JSlider.HORIZONTAL) {
      return slider.getComponentOrientation().isLeftToRight() ? slider.getInverted() : !slider.getInverted();
    }
    return slider.getInverted();
  }
  protected int getWidthOfHighValueLabel() {
    Component label = getHighestValueLabel();
    return label != null ? label.getPreferredSize().width : 0;
  }
  protected int getWidthOfLowValueLabel() {
    Component label = getLowestValueLabel();
    return label != null ? label.getPreferredSize().width : 0;
  }
  protected int getHeightOfHighValueLabel() {
    Component label = getHighestValueLabel();
    return label != null ? label.getPreferredSize().height : 0;
  }
  protected int getHeightOfLowValueLabel() {
    Component label = getLowestValueLabel();
    return label != null ? label.getPreferredSize().height : 0;
  }
  public void setThumbLocation(int x, int y) {
    Rectangle unionRect = new Rectangle(thumbRect);
    thumbRect.setLocation(x, y);
    SwingUtilities.computeUnion(thumbRect.x, thumbRect.y, thumbRect.width, thumbRect.height, unionRect);
    slider.repaint(unionRect.x, unionRect.y, unionRect.width, unionRect.height);
  }
  protected void scrollDueToClickInTrack(int dir) {
    scrollByBlock(dir);
  }
  public int valueForYPosition(int yPos) {
    int value;
    final int minValue = slider.getMinimum();
    final int maxValue = slider.getMaximum();
    final int trackLength = trackRect.height;
    final int trackTop = trackRect.y;
    final int trackBottom = trackRect.y + (trackRect.height - 1);
    if (yPos <= trackTop) {
      value = drawInverted() ? minValue : maxValue;
    } else if (yPos >= trackBottom) {
      value = drawInverted() ? maxValue : minValue;
    } else {
      int distanceFromTrackTop = yPos - trackTop;
      double valueRange = (double) maxValue - (double) minValue;
      double valuePerPixel = valueRange / (double) trackLength;
      int valueFromTrackTop = (int) Math.round(distanceFromTrackTop * valuePerPixel);
      value = drawInverted() ? minValue + valueFromTrackTop : maxValue - valueFromTrackTop;
    }
    return value;
  }
  public int valueForXPosition(int xPos) {
    int value;
    final int minValue = slider.getMinimum();
    final int maxValue = slider.getMaximum();
    final int trackLength = trackRect.width;
    final int trackLeft = trackRect.x;
    final int trackRight = trackRect.x + (trackRect.width - 1);
    if (xPos <= trackLeft) {
      value = drawInverted() ? maxValue : minValue;
    } else if (xPos >= trackRight) {
      value = drawInverted() ? minValue : maxValue;
    } else {
      int distanceFromTrackLeft = xPos - trackLeft;
      double valueRange = (double) maxValue - (double) minValue;
      double valuePerPixel = valueRange / (double) trackLength;
      int valueFromTrackLeft = (int) Math.round(distanceFromTrackLeft * valuePerPixel);
      value = drawInverted() ? maxValue - valueFromTrackLeft : minValue + valueFromTrackLeft;
    }
    return value;
  }
  protected int getTickLength() {
    return 8;
  }
  protected Color getHighlightColor() {
    return highlightColor;
  }
  protected Color getShadowColor() {
    return shadowColor;
  }
  public static javax.swing.plaf.ComponentUI createUI(JComponent b) {
    return new BasicSliderUI((JSlider) b);
  }
  public Dimension getMaximumSize(JComponent c) {
    Dimension d = getPreferredSize(c);
    if (slider.getOrientation() == JSlider.VERTICAL) {
      d.height = Short.MAX_VALUE;
    } else {
      d.width = Short.MAX_VALUE;
    }
    return d;
  }
  public void paint(Graphics g, JComponent c) {
    recalculateIfInsetsChanged();
    recalculateIfOrientationChanged();
    Rectangle clip = g.getClipBounds();
    if (!clip.intersects(trackRect) && slider.getPaintTrack()) {
      calculateGeometry();
    }
    if (slider.getPaintTrack() && clip.intersects(trackRect)) {
      paintTrack(g);
    }
    if (slider.getPaintTicks() && clip.intersects(tickRect)) {
      paintTicks(g);
    }
    if (slider.getPaintLabels() && clip.intersects(labelRect)) {
      paintLabels(g);
    }
    if (slider.hasFocus() && clip.intersects(focusRect)) {
      paintFocus(g);
    }
    if (clip.intersects(thumbRect)) {
      paintThumb(g);
    }
  }
  public Dimension getPreferredSize(JComponent c) {
    recalculateIfInsetsChanged();
    Dimension d;
    if (slider.getOrientation() == JSlider.VERTICAL) {
      d = new Dimension(getPreferredVerticalSize());
      d.width = insetCache.left + insetCache.right;
      d.width += focusInsets.left + focusInsets.right;
      d.width += trackRect.width + tickRect.width + labelRect.width;
    } else {
      d = new Dimension(getPreferredHorizontalSize());
      d.height = insetCache.top + insetCache.bottom;
      d.height += focusInsets.top + focusInsets.bottom;
      d.height += trackRect.height + tickRect.height + labelRect.height;
    }
    return d;
  }
  public Dimension getMinimumSize(JComponent c) {
    recalculateIfInsetsChanged();
    Dimension d;
    if (slider.getOrientation() == JSlider.VERTICAL) {
      d = new Dimension(getMinimumVerticalSize());
      d.width = insetCache.left + insetCache.right;
      d.width += focusInsets.left + focusInsets.right;
      d.width += trackRect.width + tickRect.width + labelRect.width;
    } else {
      d = new Dimension(getMinimumHorizontalSize());
      d.height = insetCache.top + insetCache.bottom;
      d.height += focusInsets.top + focusInsets.bottom;
      d.height += trackRect.height + tickRect.height + labelRect.height;
    }
    return d;
  }
  public int getBaseline(JComponent c, int width, int height) {
    super.getBaseline(c, width, height);
    return -1;
  }
  public Component.BaselineResizeBehavior getBaselineResizeBehavior(JComponent c) {
    super.getBaselineResizeBehavior(c);
    return Component.BaselineResizeBehavior.OTHER;
  }
  public void installUI(JComponent c) {
    slider = (JSlider) c;
    slider.setEnabled(slider.isEnabled());
    LookAndFeel.installProperty(slider, "opaque", Boolean.TRUE);
    isDragging = false;
    trackListener = createTrackListener(slider);
    changeListener = createChangeListener(slider);
    componentListener = createComponentListener(slider);
    focusListener = createFocusListener(slider);
    scrollListener = createScrollListener(slider);
    propertyChangeListener = createPropertyChangeListener(slider);
    installDefaults(slider);
    installListeners(slider);
    installKeyboardActions(slider);
    scrollTimer = new Timer(100, scrollListener);
    scrollTimer.setInitialDelay(300);
    insetCache = slider.getInsets();
    leftToRightCache = slider.getComponentOrientation().isLeftToRight();
    focusRect = new Rectangle();
    contentRect = new Rectangle();
    labelRect = new Rectangle();
    tickRect = new Rectangle();
    trackRect = new Rectangle();
    thumbRect = new Rectangle();
    lastValue = slider.getValue();
    calculateGeometry();
  }
  public void uninstallUI(JComponent c) {
    uninstallKeyboardActions(slider);
    uninstallListeners(slider);
    uninstallDefaults(slider);
    focusInsets = null;
    insetCache = null;
    leftToRightCache = true;
    focusRect = null;
    contentRect = null;
    labelRect = null;
    tickRect = null;
    trackRect = null;
    thumbRect = null;
    trackListener = null;
    changeListener = null;
    componentListener = null;
    focusListener = null;
    scrollListener = null;
    propertyChangeListener = null;
    slider = null;
  }

  public class TrackListener extends javax.swing.event.MouseInputAdapter {
    protected int offset;
    protected int currentMouseX;
    protected int currentMouseY;
    public TrackListener() {
    }
    public boolean shouldScroll(int direction) {
      Rectangle r = thumbRect;
      if (slider.getOrientation() == JSlider.VERTICAL) {
        if (drawInverted() ? direction < 0 : direction > 0) {
          if (r.y <= currentMouseY) {
            return false;
          }
        } else if (r.y + r.height >= currentMouseY) {
          return false;
        }
      } else {
        if (drawInverted() ? direction < 0 : direction > 0) {
          if (r.x + r.width >= currentMouseX) {
            return false;
          }
        } else if (r.x <= currentMouseX) {
          return false;
        }
      }
      if (direction > 0 && slider.getValue() + slider.getExtent() >= slider.getMaximum()) {
        return false;
      } else if (direction < 0 && slider.getValue() <= slider.getMinimum()) {
        return false;
      }
      return true;
    }
    public void mousePressed(MouseEvent e) {
      if (!slider.isEnabled()) {
        return;
      }
      currentMouseX = e.getX();
      currentMouseY = e.getY();
      if (slider.isRequestFocusEnabled()) {
        slider.requestFocus();
      }
      if (thumbRect.contains(currentMouseX, currentMouseY)) {
        if (UIManager.getBoolean("Slider.onlyLeftMouseButtonDrag") && !SwingUtilities.isLeftMouseButton(e)) {
          return;
        }
        switch (slider.getOrientation()) {
        case JSlider.VERTICAL:
          offset = currentMouseY - thumbRect.y;
          break;
        case JSlider.HORIZONTAL:
          offset = currentMouseX - thumbRect.x;
          break;
        default:
          break;
        }
        isDragging = true;
        return;
      }
      if (!SwingUtilities.isLeftMouseButton(e)) {
        return;
      }
      isDragging = false;
      slider.setValueIsAdjusting(true);
      Dimension sbSize = slider.getSize();
      int direction = POSITIVE_SCROLL;
      switch (slider.getOrientation()) {
      case JSlider.VERTICAL:
        if (thumbRect.isEmpty()) {
          int scrollbarCenter = sbSize.height / 2;
          if (!drawInverted()) {
            direction = (currentMouseY < scrollbarCenter) ? POSITIVE_SCROLL : NEGATIVE_SCROLL;
          } else {
            direction = (currentMouseY < scrollbarCenter) ? NEGATIVE_SCROLL : POSITIVE_SCROLL;
          }
        } else {
          int thumbY = thumbRect.y;
          if (!drawInverted()) {
            direction = (currentMouseY < thumbY) ? POSITIVE_SCROLL : NEGATIVE_SCROLL;
          } else {
            direction = (currentMouseY < thumbY) ? NEGATIVE_SCROLL : POSITIVE_SCROLL;
          }
        }
        break;
      case JSlider.HORIZONTAL:
        if (thumbRect.isEmpty()) {
          int scrollbarCenter = sbSize.width / 2;
          if (!drawInverted()) {
            direction = (currentMouseX < scrollbarCenter) ? NEGATIVE_SCROLL : POSITIVE_SCROLL;
          } else {
            direction = (currentMouseX < scrollbarCenter) ? POSITIVE_SCROLL : NEGATIVE_SCROLL;
          }
        } else {
          int thumbX = thumbRect.x;
          if (!drawInverted()) {
            direction = (currentMouseX < thumbX) ? NEGATIVE_SCROLL : POSITIVE_SCROLL;
          } else {
            direction = (currentMouseX < thumbX) ? POSITIVE_SCROLL : NEGATIVE_SCROLL;
          }
        }
        break;
      default:
        break;
      }
      scrollDueToClickInTrack(direction);
      Rectangle r = thumbRect;
      if (!r.contains(currentMouseX, currentMouseY)) {
        if (shouldScroll(direction)) {
          scrollTimer.stop();
          scrollListener.setDirection(direction);
          scrollListener.setScrollByBlock(true);
          scrollTimer.start();
        }
      }
    }
    public void mouseReleased(MouseEvent e) {
      if (!slider.isEnabled()) {
        return;
      }
      offset = 0;
      scrollTimer.stop();
      isDragging = false;
      slider.setValueIsAdjusting(false);
      slider.repaint();
    }
    public void mouseMoved(MouseEvent e) {
    }
    public void mouseDragged(MouseEvent e) {
      if (!slider.isEnabled()) {
        return;
      }
      currentMouseX = e.getX();
      currentMouseY = e.getY();
      if (!isDragging) {
        return;
      }
      slider.setValueIsAdjusting(true);
      switch (slider.getOrientation()) {
      case JSlider.VERTICAL: {
        int halfThumbHeight = thumbRect.height / 2;
        int thumbTop = e.getY() - offset;
        int trackTop = trackRect.y;
        int trackBottom = trackRect.y + (trackRect.height - 1);
        int vMax = yPositionForValue(slider.getMaximum() - slider.getExtent());
        if (drawInverted()) {
          trackBottom = vMax;
        } else {
          trackTop = vMax;
        }
        thumbTop = Math.max(thumbTop, trackTop - halfThumbHeight);
        thumbTop = Math.min(thumbTop, trackBottom - halfThumbHeight);
        setThumbLocation(thumbRect.x, thumbTop);
        int thumbMiddle = thumbTop + halfThumbHeight;
        slider.setValue(valueForYPosition(thumbMiddle));
        break;
      }
      case JSlider.HORIZONTAL: {
        int halfThumbWidth = thumbRect.width / 2;
        int thumbLeft = e.getX() - offset;
        int trackLeft = trackRect.x;
        int trackRight = trackRect.x + (trackRect.width - 1);
        int hMax = xPositionForValue(slider.getMaximum() - slider.getExtent());
        if (drawInverted()) {
          trackLeft = hMax;
        } else {
          trackRight = hMax;
        }
        thumbLeft = Math.max(thumbLeft, trackLeft - halfThumbWidth);
        thumbLeft = Math.min(thumbLeft, trackRight - halfThumbWidth);
        setThumbLocation(thumbLeft, thumbRect.y);
        int thumbMiddle = thumbLeft + halfThumbWidth;
        slider.setValue(valueForXPosition(thumbMiddle));
        break;
      }
      default:
        break;
      }
    }
  }

  public class ScrollListener implements java.awt.event.ActionListener {
    public ScrollListener() {
    }
    public ScrollListener(int dir, boolean block) {
      direction = dir;
      useBlockIncrement = block;
    }
    public void setDirection(int direction) {
      this.direction = direction;
    }
    public void setScrollByBlock(boolean block) {
      this.useBlockIncrement = block;
    }
    public void actionPerformed(java.awt.event.ActionEvent e) {
      if (useBlockIncrement) {
        scrollByBlock(direction);
      } else {
        scrollByUnit(direction);
      }
      if (!trackListener.shouldScroll(direction)) {
        scrollTimer.stop();
      }
    }
    private int direction = 1;
    private boolean useBlockIncrement;
  }

  public class ActionScroller extends javax.swing.AbstractAction {
    public ActionScroller(JSlider slider, int dir, boolean block) {
      this.dir = dir;
      this.block = block;
      this.target = slider;
    }
    public boolean isEnabled() {
      return target != null && target.isEnabled();
    }
    public void actionPerformed(java.awt.event.ActionEvent e) {
      if (block) {
        scrollByBlock(dir);
      } else {
        scrollByUnit(dir);
      }
    }
    private int dir;
    private boolean block;
    private JSlider target;
  }

  public class FocusHandler implements java.awt.event.FocusListener {
    public FocusHandler() {
    }
    public void focusGained(java.awt.event.FocusEvent e) {
      slider.repaint();
    }
    public void focusLost(java.awt.event.FocusEvent e) {
      slider.repaint();
    }
  }

  public class ComponentHandler extends java.awt.event.ComponentAdapter {
    public ComponentHandler() {
    }
    public void componentResized(java.awt.event.ComponentEvent e) {
      calculateGeometry();
      slider.repaint();
    }
  }

  public class ChangeHandler implements javax.swing.event.ChangeListener {
    public ChangeHandler() {
    }
    public void stateChanged(ChangeEvent e) {
      if (!isDragging) {
        calculateThumbLocation();
        slider.repaint();
      }
      lastValue = slider.getValue();
    }
  }

  public class PropertyChangeHandler implements java.beans.PropertyChangeListener {
    public PropertyChangeHandler() {
    }
    public void propertyChange(java.beans.PropertyChangeEvent e) {
      String propertyName = e.getPropertyName();
      if ("orientation".equals(propertyName) || "inverted".equals(propertyName) || "labelTable".equals(propertyName)
          || "majorTickSpacing".equals(propertyName) || "minorTickSpacing".equals(propertyName)
          || "paintTicks".equals(propertyName) || "paintTrack".equals(propertyName)
          || "paintLabels".equals(propertyName) || "font".equals(propertyName)
          || "componentOrientation".equals(propertyName)) {
        calculateGeometry();
        slider.repaint();
      } else if ("model".equals(propertyName)) {
        BoundedRangeModel oldModel = (BoundedRangeModel) e.getOldValue();
        BoundedRangeModel newModel = (BoundedRangeModel) e.getNewValue();
        oldModel.removeChangeListener(changeListener);
        newModel.addChangeListener(changeListener);
        calculateThumbLocation();
        slider.repaint();
      }
    }
  }
  private Dimension preferredHorizontalSize;
  private Dimension preferredVerticalSize;
  private Dimension minimumHorizontalSize;
  private Dimension minimumVerticalSize;
  private Color shadowColor;
  private Color highlightColor;
  private Color focusColor;
  private boolean isDragging;
  private int lastValue;

  private static Dimension defaultSize(String key, int w, int h) {
    Dimension d = UIManager.getDimension(key);
    return d == null ? new Dimension(w, h) : d;
  }

  protected int getTickSpacing() {
    int majorTickSpacing = slider.getMajorTickSpacing();
    int minorTickSpacing = slider.getMinorTickSpacing();
    int result;
    if (minorTickSpacing > 0) {
      result = minorTickSpacing;
    } else if (majorTickSpacing > 0) {
      result = majorTickSpacing;
    } else {
      result = 0;
    }
    return result;
  }
}
