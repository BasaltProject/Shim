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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import shim.swing.MetalPaint;

public class MetalScrollBarUI extends BasicScrollBarUI {
  protected MetalScrollButton increaseButton;
  protected MetalScrollButton decreaseButton;
  protected int scrollBarWidth;
  public static final String FREE_STANDING_PROP = "JScrollBar.isFreeStanding";
  protected boolean isFreeStanding = true;

  public MetalScrollBarUI() {
  }

  public static ComponentUI createUI(JComponent c) {
    return new MetalScrollBarUI();
  }

  protected void installDefaults() {
    scrollBarWidth = UIManager.getInt("ScrollBar.width");
    super.installDefaults();
    Object prop = scrollbar.getClientProperty(FREE_STANDING_PROP);
    isFreeStanding = prop == null ? true : ((Boolean) prop).booleanValue();
    scrollBarWidth = UIManager.getInt("ScrollBar.width");
    scrollbar.setBorder(null);
  }

  protected void installListeners() {
    super.installListeners();
    scrollbar.addPropertyChangeListener(propertyChangeListener);
  }

  protected PropertyChangeListener createPropertyChangeListener() {
    return new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
        if (FREE_STANDING_PROP.equals(e.getPropertyName())) {
          isFreeStanding = e.getNewValue() == null || ((Boolean) e.getNewValue()).booleanValue();
          if (increaseButton != null) {
            increaseButton.setFreeStanding(isFreeStanding);
          }
          if (decreaseButton != null) {
            decreaseButton.setFreeStanding(isFreeStanding);
          }
        }
      }
    };
  }

  protected void configureScrollBarColors() {
    super.configureScrollBarColors();
  }

  public Dimension getPreferredSize(JComponent c) {
    if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
      return new Dimension(scrollBarWidth, scrollBarWidth * 3 + 10);
    }
    return new Dimension(scrollBarWidth * 3 + 10, scrollBarWidth);
  }

  protected JButton createDecreaseButton(int orientation) {
    decreaseButton = new MetalScrollButton(orientation, scrollBarWidth, isFreeStanding);
    return decreaseButton;
  }

  protected JButton createIncreaseButton(int orientation) {
    increaseButton = new MetalScrollButton(orientation, scrollBarWidth, isFreeStanding);
    return increaseButton;
  }

  protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
    g.translate(trackBounds.x, trackBounds.y);
    boolean vertical = scrollbar.getOrientation() == JScrollBar.VERTICAL;
    if (!c.isEnabled()) {
      g.setColor(MetalLookAndFeel.getControlShadow());
      g.fillRect(0, 0, trackBounds.width - 1, trackBounds.height - 1);
    } else {
      g.setColor(trackColor);
      g.fillRect(0, 0, trackBounds.width, trackBounds.height);
      g.setColor(MetalLookAndFeel.getControlDarkShadow());
      Color line = MetalPaint.lastColor("ScrollBar.gradient", trackColor);
      if (vertical) {
        g.drawLine(0, 0, 0, trackBounds.height - 1);
        g.setColor(line);
        g.drawLine(1, 0, 1, trackBounds.height - 1);
      } else {
        g.drawLine(0, 0, trackBounds.width - 1, 0);
        g.setColor(line);
        g.drawLine(0, 1, trackBounds.width - 1, 1);
      }
      if (trackHighlight == DECREASE_HIGHLIGHT) {
        paintDecreaseHighlight(g);
      } else if (trackHighlight == INCREASE_HIGHLIGHT) {
        paintIncreaseHighlight(g);
      }
    }
    g.translate(-trackBounds.x, -trackBounds.y);
  }

  protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
    if (!c.isEnabled()) {
      return;
    }
    boolean vertical = scrollbar.getOrientation() == JScrollBar.VERTICAL;
    g.translate(thumbBounds.x, thumbBounds.y);
    int w = thumbBounds.width;
    int h = thumbBounds.height;
    Color inner = MetalPaint.lastColor("ScrollBar.gradient", thumbColor);
    g.setColor(inner);
    g.fillRect(1, 1, w - 2, h - 2);
    MetalPaint.drawGradient(c, g, "ScrollBar.gradient", 2, 2, w - 4, h - 4, !vertical);
    g.setColor(UIManager.getColor("ScrollBar.thumbShadow"));
    g.drawRect(0, 0, w - 1, h - 1);
    g.translate(-thumbBounds.x, -thumbBounds.y);
  }

  protected Dimension getMinimumThumbSize() {
    return new Dimension(scrollBarWidth, scrollBarWidth);
  }

  protected void setThumbBounds(int x, int y, int width, int height) {
    super.setThumbBounds(x, y, width, height);
  }
}
