/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package shim.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.ActionMapUIResource;
import javax.swing.plaf.InputMapUIResource;

public final class KeyActions {
  private KeyActions() { }

  public static void install(JComponent c, int condition, String mapKey, ActionMap actions) {
    InputMap im = (InputMap) UIManager.get(mapKey);
    if (im != null && !c.getComponentOrientation().isLeftToRight()) {
      InputMap rtl = (InputMap) UIManager.get(mapKey + ".RightToLeft");
      if (rtl != null) {
        InputMap merged = new InputMapUIResource();
        for (javax.swing.KeyStroke ks : rtl.allKeys()) {
          merged.put(ks, rtl.get(ks));
        }
        merged.setParent(im);
        im = merged;
      }
    }
    SwingUtilities.replaceUIInputMap(c, condition, im);
    SwingUtilities.replaceUIActionMap(c, actions);
  }

  public static void uninstall(JComponent c, int condition) {
    SwingUtilities.replaceUIInputMap(c, condition, null);
    SwingUtilities.replaceUIActionMap(c, null);
  }

  public static ActionMap map(Action... actions) {
    ActionMap map = new ActionMapUIResource();
    for (Action a : actions) {
      map.put(a.getValue(Action.NAME), a);
    }
    return map;
  }

  public static ActionMap sliderActions() {
    return map(new Act("positiveUnitIncrement"), new Act("negativeUnitIncrement"), new Act("positiveBlockIncrement"),
        new Act("negativeBlockIncrement"), new Act("minScroll"), new Act("maxScroll"));
  }

  public static ActionMap scrollBarActions() {
    return sliderActions();
  }

  public static ActionMap spinnerActions() {
    return map(new Act("increment"), new Act("decrement"));
  }

  public static ActionMap scrollPaneActions() {
    return map(new Act("scrollUp"), new Act("scrollDown"), new Act("scrollHome"), new Act("scrollEnd"),
        new Act("scrollLeft"), new Act("scrollRight"), new Act("unitScrollUp"), new Act("unitScrollDown"),
        new Act("unitScrollLeft"), new Act("unitScrollRight"));
  }

  public static ActionMap splitPaneActions() {
    return map(new Act("negativeIncrement"), new Act("positiveIncrement"), new Act("selectMin"),
        new Act("selectMax"), new Act("startResize"), new Act("toggleFocus"), new Act("focusOutForward"),
        new Act("focusOutBackward"));
  }

  private static final class Act extends AbstractAction {
    private final String name;

    Act(String name) {
      super(name);
      this.name = name;
    }

    public boolean isEnabled() {
      return true;
    }

    public void actionPerformed(ActionEvent e) {
      Object src = e.getSource();
      if (src instanceof JSlider) {
        slider((JSlider) src);
      } else if (src instanceof JScrollBar) {
        scrollBar((JScrollBar) src);
      } else if (src instanceof JSpinner) {
        spinner((JSpinner) src);
      } else if (src instanceof JScrollPane) {
        scrollPane((JScrollPane) src);
      } else if (src instanceof JSplitPane) {
        splitPane((JSplitPane) src);
      }
    }

    private void slider(JSlider s) {
      if (!s.isEnabled()) {
        return;
      }
      int dir = 0;
      boolean block = false;
      switch (name) {
        case "positiveUnitIncrement":
          dir = 1;
          break;
        case "negativeUnitIncrement":
          dir = -1;
          break;
        case "positiveBlockIncrement":
          dir = 1;
          block = true;
          break;
        case "negativeBlockIncrement":
          dir = -1;
          block = true;
          break;
        case "minScroll":
          s.setValue(s.getInverted() ? s.getMaximum() : s.getMinimum());
          return;
        case "maxScroll":
          s.setValue(s.getInverted() ? s.getMinimum() : s.getMaximum());
          return;
        default:
          return;
      }
      if (s.getInverted()) {
        dir = -dir;
      }
      int delta = 1;
      if (block) {
        delta = (s.getMaximum() - s.getMinimum()) / 10;
        if (delta <= 0 && s.getMaximum() > s.getMinimum()) {
          delta = 1;
        }
      }
      s.setValue(s.getValue() + delta * dir);
    }

    private void scrollBar(JScrollBar b) {
      switch (name) {
        case "positiveUnitIncrement":
          b.setValue(b.getValue() + b.getUnitIncrement(1));
          break;
        case "negativeUnitIncrement":
          b.setValue(b.getValue() - b.getUnitIncrement(-1));
          break;
        case "positiveBlockIncrement":
          b.setValue(b.getValue() + b.getBlockIncrement(1));
          break;
        case "negativeBlockIncrement":
          b.setValue(b.getValue() - b.getBlockIncrement(-1));
          break;
        case "minScroll":
          b.setValue(b.getMinimum());
          break;
        case "maxScroll":
          b.setValue(b.getMaximum());
          break;
        default:
          break;
      }
    }

    private void spinner(JSpinner sp) {
      Object value = name.equals("increment") ? sp.getNextValue() : sp.getPreviousValue();
      if (value != null) {
        sp.setValue(value);
      }
    }

    private void scrollPane(JScrollPane sp) {
      JViewport vp = sp.getViewport();
      if (vp == null || vp.getView() == null) {
        return;
      }
      Component view = vp.getView();
      Rectangle vis = vp.getViewRect();
      Dimension size = view.getSize();
      int horizontalDir = sp.getComponentOrientation().isLeftToRight() ? 1 : -1;
      switch (name) {
        case "unitScrollUp":
          scroll(vp, SwingConstants.VERTICAL, -1, false);
          break;
        case "unitScrollDown":
          scroll(vp, SwingConstants.VERTICAL, 1, false);
          break;
        case "unitScrollLeft":
          scroll(vp, SwingConstants.HORIZONTAL, -1, false);
          break;
        case "unitScrollRight":
          scroll(vp, SwingConstants.HORIZONTAL, 1, false);
          break;
        case "scrollUp":
          scroll(vp, SwingConstants.VERTICAL, -1, true);
          break;
        case "scrollDown":
          scroll(vp, SwingConstants.VERTICAL, 1, true);
          break;
        case "scrollLeft":
          scroll(vp, SwingConstants.HORIZONTAL, -horizontalDir, true);
          break;
        case "scrollRight":
          scroll(vp, SwingConstants.HORIZONTAL, horizontalDir, true);
          break;
        case "scrollHome":
          vp.setViewPosition(new Point(sp.getComponentOrientation().isLeftToRight() ? 0
              : Math.max(0, size.width - vis.width), 0));
          break;
        case "scrollEnd":
          vp.setViewPosition(new Point(sp.getComponentOrientation().isLeftToRight()
              ? Math.max(0, size.width - vis.width) : 0, Math.max(0, size.height - vis.height)));
          break;
        default:
          break;
      }
    }

    private static void scroll(JViewport vp, int orientation, int direction, boolean block) {
      Component view = vp.getView();
      Rectangle vis = vp.getViewRect();
      Dimension size = view.getSize();
      int amount;
      if (view instanceof Scrollable) {
        Scrollable s = (Scrollable) view;
        amount = block ? s.getScrollableBlockIncrement(vis, orientation, direction)
            : s.getScrollableUnitIncrement(vis, orientation, direction);
      } else if (block) {
        amount = orientation == SwingConstants.VERTICAL ? vis.height : vis.width;
      } else {
        amount = 10;
      }
      if (orientation == SwingConstants.VERTICAL) {
        vis.y += amount * direction;
        vis.y = Math.max(0, Math.min(vis.y, size.height - vis.height));
      } else {
        vis.x += amount * direction;
        vis.x = Math.max(0, Math.min(vis.x, size.width - vis.width));
      }
      vp.setViewPosition(vis.getLocation());
    }

    private void splitPane(JSplitPane sp) {
      boolean resizing = Boolean.TRUE.equals(sp.getClientProperty("shim.dividerKeyboardResize"));
      if (name.equals("startResize")) {
        sp.putClientProperty("shim.dividerKeyboardResize", Boolean.TRUE);
        return;
      }
      if (!resizing) {
        return;
      }
      switch (name) {
        case "negativeIncrement":
          sp.setDividerLocation(Math.max(0, sp.getDividerLocation() - 3));
          break;
        case "positiveIncrement":
          sp.setDividerLocation(sp.getDividerLocation() + 3);
          break;
        case "selectMin":
          sp.setDividerLocation(0);
          break;
        case "selectMax":
          sp.setDividerLocation(sp.getMaximumDividerLocation());
          break;
        default:
          break;
      }
    }
  }
}
