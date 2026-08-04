/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.LayoutManager2;
import java.io.Serializable;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.plaf.RootPaneUI;

public class JRootPane extends JComponent implements Accessible {
  public static final int NONE = 0;
  public static final int FRAME = 1;
  public static final int PLAIN_DIALOG = 2;
  public static final int INFORMATION_DIALOG = 3;
  public static final int ERROR_DIALOG = 4;
  public static final int COLOR_CHOOSER_DIALOG = 5;
  public static final int FILE_CHOOSER_DIALOG = 6;
  public static final int QUESTION_DIALOG = 7;
  public static final int WARNING_DIALOG = 8;

  private int windowDecorationStyle;
  protected JMenuBar menuBar;
  protected Container contentPane;
  protected JLayeredPane layeredPane;
  protected Component glassPane;
  protected JButton defaultButton;

  public JRootPane() {
    setGlassPane(createGlassPane());
    setLayeredPane(createLayeredPane());
    setContentPane(createContentPane());
    setLayout(createRootLayout());
    setDoubleBuffered(true);
    updateUI();
  }

  public void setDoubleBuffered(boolean aFlag) {
    if (isDoubleBuffered() != aFlag) {
      super.setDoubleBuffered(aFlag);
    }
  }

  public int getWindowDecorationStyle() { return windowDecorationStyle; }

  public void setWindowDecorationStyle(int windowDecorationStyle) {
    if (windowDecorationStyle < 0 || windowDecorationStyle > WARNING_DIALOG) {
      throw new IllegalArgumentException("Invalid decoration style");
    }
    int oldWindowDecorationStyle = getWindowDecorationStyle();
    this.windowDecorationStyle = windowDecorationStyle;
    firePropertyChange("windowDecorationStyle", oldWindowDecorationStyle, windowDecorationStyle);
  }

  public RootPaneUI getUI() { return (RootPaneUI) ui; }

  public void setUI(RootPaneUI ui) { super.setUI(ui); }

  public void updateUI() { setUI((RootPaneUI) UIManager.getUI(this)); }

  public String getUIClassID() { return "RootPaneUI"; }

  protected JLayeredPane createLayeredPane() {
    JLayeredPane p = new JLayeredPane();
    p.setName(this.getName() + ".layeredPane");
    return p;
  }

  protected Container createContentPane() {
    JComponent c = new JPanel();
    c.setName(this.getName() + ".contentPane");
    c.setLayout(new BorderLayout() {
      public void addLayoutComponent(Component comp, Object constraints) {
        if (constraints == null) {
          constraints = BorderLayout.CENTER;
        }
        super.addLayoutComponent(comp, constraints);
      }
    });
    return c;
  }

  protected Component createGlassPane() {
    JComponent c = new JPanel();
    c.setName(this.getName() + ".glassPane");
    c.setVisible(false);
    ((JPanel) c).setOpaque(false);
    return c;
  }

  protected LayoutManager createRootLayout() { return new RootLayout(); }

  public void setJMenuBar(JMenuBar menu) {
    if (menuBar != null && menuBar.getParent() == layeredPane) {
      layeredPane.remove(menuBar);
    }
    menuBar = menu;
    if (menuBar != null) {
      layeredPane.add(menuBar, JLayeredPane.FRAME_CONTENT_LAYER);
    }
  }

  public void setMenuBar(JMenuBar menu) {
    if (menuBar != null && menuBar.getParent() == layeredPane) {
      layeredPane.remove(menuBar);
    }
    menuBar = menu;
    if (menuBar != null) {
      layeredPane.add(menuBar, JLayeredPane.FRAME_CONTENT_LAYER);
    }
  }

  public JMenuBar getJMenuBar() { return menuBar; }

  public JMenuBar getMenuBar() { return menuBar; }

  public void setContentPane(Container content) {
    if (content == null) {
      throw new java.awt.IllegalComponentStateException("contentPane cannot be set to null.");
    }
    if (contentPane != null && contentPane.getParent() == layeredPane) {
      layeredPane.remove(contentPane);
    }
    contentPane = content;
    layeredPane.add(contentPane, JLayeredPane.FRAME_CONTENT_LAYER);
  }

  public Container getContentPane() { return contentPane; }

  public void setLayeredPane(JLayeredPane layered) {
    if (layered == null) {
      throw new java.awt.IllegalComponentStateException("layeredPane cannot be set to null.");
    }
    if (layeredPane != null && layeredPane.getParent() == this) {
      this.remove(layeredPane);
    }
    layeredPane = layered;
    this.add(layeredPane, -1);
  }

  public JLayeredPane getLayeredPane() { return layeredPane; }

  public void setGlassPane(Component glass) {
    if (glass == null) {
      throw new NullPointerException("glassPane cannot be set to null.");
    }
    boolean visible = false;
    if (glassPane != null && glassPane.getParent() == this) {
      this.remove(glassPane);
      visible = glassPane.isVisible();
    }
    glass.setVisible(visible);
    glassPane = glass;
    this.add(glassPane, 0);
    if (visible) {
      repaint();
    }
  }

  public Component getGlassPane() { return glassPane; }

  public boolean isValidateRoot() { return true; }

  public boolean isOptimizedDrawingEnabled() { return !glassPane.isVisible(); }

  public void addNotify() { super.addNotify(); }

  public void removeNotify() { super.removeNotify(); }

  public void setDefaultButton(JButton defaultButton) {
    JButton oldDefault = this.defaultButton;
    if (oldDefault != defaultButton) {
      this.defaultButton = defaultButton;
      if (oldDefault != null) {
        oldDefault.repaint();
      }
      if (defaultButton != null) {
        defaultButton.repaint();
      }
    }
    firePropertyChange("defaultButton", oldDefault, defaultButton);
  }

  public JButton getDefaultButton() { return defaultButton; }

  protected void addImpl(Component comp, Object constraints, int index) {
    super.addImpl(comp, constraints, index);
    if (glassPane != null && glassPane.getParent() == this && getComponent(0) != glassPane) {
      add(glassPane, 0);
    }
  }

  protected String paramString() { return super.paramString(); }

  public AccessibleContext getAccessibleContext() {
    if (accessibleContext == null) {
      accessibleContext = new AccessibleJRootPane();
    }
    return accessibleContext;
  }

  protected class AccessibleJRootPane extends AccessibleJComponent {
    protected AccessibleJRootPane() { }

    public AccessibleRole getAccessibleRole() { return AccessibleRole.ROOT_PANE; }

    public int getAccessibleChildrenCount() { return super.getAccessibleChildrenCount(); }

    public Accessible getAccessibleChild(int i) { return super.getAccessibleChild(i); }
  }

  protected class RootLayout implements LayoutManager2, Serializable {
    public Dimension preferredLayoutSize(Container parent) {
      Dimension rd;
      Dimension mbd;
      Insets i = getInsets();
      if (contentPane != null) {
        rd = contentPane.getPreferredSize();
      } else {
        rd = parent.getSize();
      }
      if (menuBar != null && menuBar.isVisible()) {
        mbd = menuBar.getPreferredSize();
      } else {
        mbd = new Dimension(0, 0);
      }
      return new Dimension(Math.max(rd.width, mbd.width) + i.left + i.right, rd.height + mbd.height + i.top + i.bottom);
    }

    public Dimension minimumLayoutSize(Container parent) {
      Dimension rd;
      Dimension mbd;
      Insets i = getInsets();
      if (contentPane != null) {
        rd = contentPane.getMinimumSize();
      } else {
        rd = parent.getSize();
      }
      if (menuBar != null && menuBar.isVisible()) {
        mbd = menuBar.getMinimumSize();
      } else {
        mbd = new Dimension(0, 0);
      }
      return new Dimension(Math.max(rd.width, mbd.width) + i.left + i.right, rd.height + mbd.height + i.top + i.bottom);
    }

    public Dimension maximumLayoutSize(Container target) {
      Dimension rd;
      Dimension mbd;
      Insets i = getInsets();
      if (menuBar != null && menuBar.isVisible()) {
        mbd = menuBar.getMaximumSize();
      } else {
        mbd = new Dimension(Integer.MAX_VALUE, 0);
      }
      if (contentPane != null) {
        rd = contentPane.getMaximumSize();
      } else {
        rd = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE - i.top - i.bottom - mbd.height - 1);
      }
      return new Dimension(Math.min(rd.width, mbd.width) + i.left + i.right,
          rd.height + mbd.height + i.top + i.bottom);
    }

    public void layoutContainer(Container parent) {
      Rectangle_ b = new Rectangle_(parent.getBounds());
      Insets i = getInsets();
      int contentY = 0;
      int w = b.width - i.right - i.left;
      int h = b.height - i.top - i.bottom;
      if (layeredPane != null) {
        layeredPane.setBounds(i.left, i.top, w, h);
      }
      if (glassPane != null) {
        glassPane.setBounds(i.left, i.top, w, h);
      }
      if (menuBar != null && menuBar.isVisible()) {
        Dimension mbd = menuBar.getPreferredSize();
        menuBar.setBounds(0, 0, w, mbd.height);
        contentY += mbd.height;
      }
      if (contentPane != null) {
        contentPane.setBounds(0, contentY, w, h - contentY);
      }
    }

    public void addLayoutComponent(String name, Component comp) { }

    public void removeLayoutComponent(Component comp) { }

    public void addLayoutComponent(Component comp, Object constraints) { }

    public float getLayoutAlignmentX(Container target) { return 0.0f; }

    public float getLayoutAlignmentY(Container target) { return 0.0f; }

    public void invalidateLayout(Container target) { }
  }

  private static final class Rectangle_ {
    final int width;
    final int height;

    Rectangle_(java.awt.Rectangle r) {
      width = r.width;
      height = r.height;
    }
  }
}
