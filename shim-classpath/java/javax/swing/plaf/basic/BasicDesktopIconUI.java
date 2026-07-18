/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.plaf.basic;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.LookAndFeel;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.DesktopIconUI;

public class BasicDesktopIconUI extends DesktopIconUI {
  protected JInternalFrame.JDesktopIcon desktopIcon;
  protected JInternalFrame frame;
  protected JComponent iconPane;
  private MouseInputListener mouseInputListener;

  public BasicDesktopIconUI() { }

  public static ComponentUI createUI(JComponent c) {
    return new BasicDesktopIconUI();
  }

  public void installUI(JComponent c) {
    desktopIcon = (JInternalFrame.JDesktopIcon) c;
    installDefaults();
    installComponents();
    frame = desktopIcon.getInternalFrame();
    installListeners();
    JLayeredPane.putLayer(desktopIcon, JLayeredPane.getLayer(frame));
  }

  public void uninstallUI(JComponent c) {
    uninstallDefaults();
    uninstallComponents();
    uninstallListeners();
    frame = null;
    desktopIcon = null;
  }

  protected void installComponents() {
    frame = desktopIcon.getInternalFrame();
    iconPane = new BasicInternalFrameTitlePane(frame);
    desktopIcon.setLayout(new BorderLayout());
    desktopIcon.add(iconPane, BorderLayout.CENTER);
  }

  protected void uninstallComponents() {
    desktopIcon.setLayout(null);
    desktopIcon.remove(iconPane);
  }

  protected void installListeners() {
    mouseInputListener = createMouseInputListener();
    desktopIcon.addMouseMotionListener(mouseInputListener);
    desktopIcon.addMouseListener(mouseInputListener);
  }

  protected void uninstallListeners() {
    desktopIcon.removeMouseMotionListener(mouseInputListener);
    desktopIcon.removeMouseListener(mouseInputListener);
    mouseInputListener = null;
  }

  protected void installDefaults() {
    LookAndFeel.installBorder(desktopIcon, "DesktopIcon.border");
    LookAndFeel.installProperty(desktopIcon, "opaque", Boolean.TRUE);
  }

  protected void uninstallDefaults() {
    LookAndFeel.uninstallBorder(desktopIcon);
  }

  protected MouseInputListener createMouseInputListener() {
    return new MouseInputHandler();
  }

  public Dimension getPreferredSize(JComponent c) {
    return desktopIcon.getLayout().preferredLayoutSize(desktopIcon);
  }

  public Dimension getMinimumSize(JComponent c) {
    Dimension dim = new Dimension(iconPane.getMinimumSize());
    Insets i = frame.getInsets();
    dim.width += i.left + i.right;
    dim.height += i.top + i.bottom;
    return dim;
  }

  public Dimension getMaximumSize(JComponent c) {
    return iconPane.getMaximumSize();
  }

  public Insets getInsets(JComponent c) {
    JInternalFrame iframe = desktopIcon.getInternalFrame();
    if (iframe.getBorder() != null) {
      return iframe.getBorder().getBorderInsets(iframe);
    }
    return new Insets(0, 0, 0, 0);
  }

  public void deiconize() {
    try {
      frame.setIcon(false);
    } catch (PropertyVetoException e2) {
      return;
    }
  }

  public class MouseInputHandler extends MouseInputAdapter {
    private int startX;
    private int startY;
    private int startLocationX;
    private int startLocationY;

    public MouseInputHandler() { }

    public void mousePressed(MouseEvent e) {
      startX = e.getXOnScreen();
      startY = e.getYOnScreen();
      startLocationX = desktopIcon.getX();
      startLocationY = desktopIcon.getY();
    }

    public void mouseDragged(MouseEvent e) {
      moveAndRepaint(desktopIcon, startLocationX + e.getXOnScreen() - startX,
          startLocationY + e.getYOnScreen() - startY, desktopIcon.getWidth(), desktopIcon.getHeight());
    }

    public void mouseMoved(MouseEvent e) { }

    public void mouseReleased(MouseEvent e) { }

    public void moveAndRepaint(JComponent f, int newX, int newY, int newWidth, int newHeight) {
      java.awt.Rectangle r = f.getBounds();
      f.setBounds(newX, newY, newWidth, newHeight);
      javax.swing.SwingUtilities.computeUnion(newX, newY, newWidth, newHeight, r);
      if (f.getParent() != null) {
        f.getParent().repaint(r.x, r.y, r.width, r.height);
      }
    }
  }
}
