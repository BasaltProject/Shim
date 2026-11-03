/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.plaf.metal;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicScrollPaneUI;

public class MetalScrollPaneUI extends BasicScrollPaneUI {
  private PropertyChangeListener scrollBarSwapListener;

  public MetalScrollPaneUI() {
  }

  public static ComponentUI createUI(JComponent x) {
    return new MetalScrollPaneUI();
  }

  public void installUI(JComponent c) {
    super.installUI(c);
    JScrollPane sp = (JScrollPane) c;
    updateScrollbarsFreeStanding();
  }

  public void uninstallUI(JComponent c) {
    super.uninstallUI(c);
  }

  public void installListeners(JScrollPane scrollPane) {
    super.installListeners(scrollPane);
    scrollBarSwapListener = createScrollBarSwapListener();
    scrollPane.addPropertyChangeListener(scrollBarSwapListener);
  }

  public void uninstallListeners(JScrollPane scrollPane) {
    scrollPane.removePropertyChangeListener(scrollBarSwapListener);
    super.uninstallListeners(scrollPane);
  }

  protected void uninstallListeners(JComponent c) {
    uninstallListeners((JScrollPane) c);
  }

  private void updateScrollbarsFreeStanding() {
    if (scrollpane == null) {
      return;
    }
    Object value = Boolean.FALSE;
    if (scrollpane.getBorder() == null) {
      value = null;
    }
    JScrollBar vsb = scrollpane.getVerticalScrollBar();
    if (vsb != null) {
      vsb.putClientProperty(MetalScrollBarUI.FREE_STANDING_PROP, value);
    }
    JScrollBar hsb = scrollpane.getHorizontalScrollBar();
    if (hsb != null) {
      hsb.putClientProperty(MetalScrollBarUI.FREE_STANDING_PROP, value);
    }
  }

  protected PropertyChangeListener createScrollBarSwapListener() {
    return new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
        String propertyName = e.getPropertyName();
        if (propertyName.equals("verticalScrollBar") || propertyName.equals("horizontalScrollBar")) {
          JScrollBar oldSB = (JScrollBar) e.getOldValue();
          if (oldSB != null) {
            oldSB.putClientProperty(MetalScrollBarUI.FREE_STANDING_PROP, null);
          }
          JScrollBar newSB = (JScrollBar) e.getNewValue();
          if (newSB != null) {
            newSB.putClientProperty(MetalScrollBarUI.FREE_STANDING_PROP, Boolean.FALSE);
          }
        }
      }
    };
  }
}
