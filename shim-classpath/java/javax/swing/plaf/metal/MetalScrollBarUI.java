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

  private static void px(Graphics g, boolean horizontal, int l1, int t1, int l2, int t2) {
    if (horizontal) {
      g.fillRect(l1, t1, l2 - l1 + 1, t2 - t1 + 1);
    } else {
      g.fillRect(t1, l1, t2 - t1 + 1, l2 - l1 + 1);
    }
  }

  protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
    g.translate(trackBounds.x, trackBounds.y);
    boolean horizontal = scrollbar.getOrientation() == JScrollBar.HORIZONTAL;
    int len = horizontal ? trackBounds.width : trackBounds.height;
    int thick = horizontal ? trackBounds.height : trackBounds.width;
    int last = thick - 2;
    if (!c.isEnabled()) {
      g.setColor(MetalLookAndFeel.getControlShadow());
      g.fillRect(0, 0, trackBounds.width - 1, trackBounds.height - 1);
    } else {
      g.setColor(trackColor);
      g.fillRect(0, 0, trackBounds.width, trackBounds.height);
      g.setColor(MetalLookAndFeel.getControlDarkShadow());
      px(g, horizontal, 0, 0, len - 1, 0);
      px(g, horizontal, 0, last, len - 1, last);
      px(g, horizontal, 0, 2, 0, last - 1);
      px(g, horizontal, len - 1, 2, len - 1, last - 1);
      g.setColor(MetalLookAndFeel.getControlShadow());
      px(g, horizontal, 1, 1, len - 2, 1);
      px(g, horizontal, 1, 2, 1, last - 1);
      g.setColor(horizontal ? MetalLookAndFeel.getControlShadow() : Color.WHITE);
      px(g, horizontal, 0, last + 1, len - 1, last + 1);
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
    boolean horizontal = scrollbar.getOrientation() == JScrollBar.HORIZONTAL;
    g.translate(thumbBounds.x, thumbBounds.y);
    int len = (horizontal ? thumbBounds.width : thumbBounds.height) + 1;
    int thick = horizontal ? thumbBounds.height : thumbBounds.width;
    int last = thick - 2;
    if (horizontal) {
      MetalPaint.drawGradient(c, g, "ScrollBar.gradient", 2, 2, len - 4, thick - 4, true);
    } else {
      MetalPaint.drawGradient(c, g, "ScrollBar.gradient", 2, 2, thick - 4, len - 4, false);
    }
    g.setColor(UIManager.getColor("ScrollBar.thumbShadow"));
    px(g, horizontal, 0, 0, len - 2, 0);
    px(g, horizontal, 0, last, len - 2, last);
    px(g, horizontal, 0, 1, 0, last - 1);
    px(g, horizontal, len - 2, 1, len - 2, last - 1);
    g.setColor(MetalLookAndFeel.getControlShadow());
    px(g, horizontal, 1, 1, len - 3, 1);
    px(g, horizontal, 1, 2, 1, last - 1);
    px(g, horizontal, len - 1, 1, len - 1, last);
    int mid = len / 2;
    g.setColor(UIManager.getColor("ScrollBar.thumbShadow"));
    for (int k = -1; k <= 1; k++) {
      px(g, horizontal, mid + 2 * k - 1, 4, mid + 2 * k - 1, last - 3);
    }
    g.setColor(Color.WHITE);
    for (int k = -1; k <= 1; k++) {
      px(g, horizontal, mid + 2 * k, 5, mid + 2 * k, last - 2);
    }
    g.translate(-thumbBounds.x, -thumbBounds.y);
  }

  protected Dimension getMinimumThumbSize() {
    return new Dimension(scrollBarWidth, scrollBarWidth);
  }

  protected void setThumbBounds(int x, int y, int width, int height) {
    super.setThumbBounds(x, y, width, height);
  }
}
