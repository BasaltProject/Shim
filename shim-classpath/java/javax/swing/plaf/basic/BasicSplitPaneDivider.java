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
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JButton;
import javax.swing.JSplitPane;
import javax.swing.border.Border;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;

public class BasicSplitPaneDivider extends Container implements PropertyChangeListener {
  protected static final int ONE_TOUCH_SIZE = 6;
  protected static final int ONE_TOUCH_OFFSET = 2;
  protected DragController dragger;
  protected BasicSplitPaneUI splitPaneUI;
  protected int dividerSize = 0;
  protected Component hiddenDivider;
  protected JSplitPane splitPane;
  protected MouseHandler mouseHandler;
  protected int orientation;
  protected JButton leftButton;
  protected JButton rightButton;
  private Border border;
  private boolean mouseOver;
  private int oneTouchSize;
  private int oneTouchOffset;
  private boolean centerOneTouchButtons;

  public BasicSplitPaneDivider(BasicSplitPaneUI ui) {
    oneTouchSize = ONE_TOUCH_SIZE;
    oneTouchOffset = ONE_TOUCH_OFFSET;
    centerOneTouchButtons = true;
    setLayout(new DividerLayout());
    setBasicSplitPaneUI(ui);
    orientation = splitPane.getOrientation();
    setCursor((orientation == JSplitPane.HORIZONTAL_SPLIT) ? Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR)
        : Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
    setBackground(javax.swing.UIManager.getColor("SplitPane.background"));
  }

  private void revalidateSplitPane() {
    invalidate();
    if (splitPane != null) {
      splitPane.revalidate();
    }
  }

  public void setBasicSplitPaneUI(BasicSplitPaneUI newUI) {
    if (splitPane != null) {
      splitPane.removePropertyChangeListener(this);
      if (mouseHandler != null) {
        splitPane.removeMouseListener(mouseHandler);
        splitPane.removeMouseMotionListener(mouseHandler);
        removeMouseListener(mouseHandler);
        removeMouseMotionListener(mouseHandler);
        mouseHandler = null;
      }
    }
    splitPaneUI = newUI;
    if (newUI != null) {
      splitPane = newUI.getSplitPane();
      if (splitPane != null) {
        if (mouseHandler == null) {
          mouseHandler = new MouseHandler();
        }
        splitPane.addMouseListener(mouseHandler);
        splitPane.addMouseMotionListener(mouseHandler);
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
        splitPane.addPropertyChangeListener(this);
        if (splitPane.isOneTouchExpandable()) {
          oneTouchExpandableChanged();
        }
      }
    } else {
      splitPane = null;
    }
  }

  public BasicSplitPaneUI getBasicSplitPaneUI() {
    return splitPaneUI;
  }

  public void setDividerSize(int newSize) {
    dividerSize = newSize;
  }

  public int getDividerSize() {
    return dividerSize;
  }

  public void setBorder(Border border) {
    Border oldBorder = this.border;
    this.border = border;
  }

  public Border getBorder() {
    return border;
  }

  public Insets getInsets() {
    Border border = getBorder();
    if (border != null) {
      return border.getBorderInsets(this);
    }
    return super.getInsets();
  }

  protected void setMouseOver(boolean mouseOver) {
    this.mouseOver = mouseOver;
  }

  public boolean isMouseOver() {
    return mouseOver;
  }

  public Dimension getPreferredSize() {
    if (orientation == JSplitPane.HORIZONTAL_SPLIT) {
      return new Dimension(Math.max(getDividerSize(), 1), 1);
    }
    return new Dimension(1, Math.max(getDividerSize(), 1));
  }

  public Dimension getMinimumSize() {
    return getPreferredSize();
  }

  public void propertyChange(PropertyChangeEvent e) {
    if (e.getSource() == splitPane) {
      String name = e.getPropertyName();
      if ("orientation".equals(name)) {
        orientation = splitPane.getOrientation();
        setCursor((orientation == JSplitPane.HORIZONTAL_SPLIT) ? Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR)
            : Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
        revalidateSplitPane();
      } else if ("oneTouchExpandable".equals(name)) {
        oneTouchExpandableChanged();
      }
    }
  }

  public void paint(Graphics g) {
    super.paint(g);
    Border border = getBorder();
    if (border != null) {
      Dimension size = getSize();
      border.paintBorder(this, g, 0, 0, size.width, size.height);
    }
  }

  protected void oneTouchExpandableChanged() {
    if (!splitPane.isOneTouchExpandable()) {
      if (leftButton != null) {
        remove(leftButton);
        leftButton = null;
      }
      if (rightButton != null) {
        remove(rightButton);
        rightButton = null;
      }
    } else {
      leftButton = createLeftOneTouchButton();
      rightButton = createRightOneTouchButton();
      if (leftButton != null) {
        add(leftButton);
      }
      if (rightButton != null) {
        add(rightButton);
      }
    }
    revalidateSplitPane();
  }

  protected JButton createLeftOneTouchButton() {
    return new OneTouchButton(true);
  }

  protected JButton createRightOneTouchButton() {
    return new OneTouchButton(false);
  }

  private class OneTouchButton extends JButton {
    private final boolean left;

    OneTouchButton(boolean left) {
      this.left = left;
      setMinimumSize(new Dimension(oneTouchSize, oneTouchSize));
      setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      setFocusPainted(false);
      setBorderPainted(false);
      setRequestFocusEnabled(false);
      addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          if (splitPaneUI == null) {
            return;
          }
          int current = splitPaneUI.getDividerLocation(splitPane);
          if (OneTouchButton.this.left) {
            if (current >= splitPaneUI.getMaximumDividerLocation(splitPane)) {
              splitPane.setDividerLocation(splitPane.getLastDividerLocation());
            } else {
              splitPane.setDividerLocation(splitPaneUI.getMinimumDividerLocation(splitPane));
            }
          } else {
            if (current <= splitPaneUI.getMinimumDividerLocation(splitPane)) {
              splitPane.setDividerLocation(splitPane.getLastDividerLocation());
            } else {
              splitPane.setDividerLocation(splitPaneUI.getMaximumDividerLocation(splitPane));
            }
          }
        }
      });
    }

    public Dimension getPreferredSize() {
      return new Dimension(oneTouchSize, oneTouchSize);
    }
  }

  protected void prepareForDragging() {
    splitPaneUI.startDragging();
  }

  protected void dragDividerTo(int location) {
    splitPaneUI.dragDividerTo(location);
  }

  protected void finishDraggingTo(int location) {
    splitPaneUI.finishDraggingTo(location);
  }

  protected class MouseHandler extends MouseInputAdapter {
    public void mousePressed(MouseEvent e) {
      if ((e.getSource() == BasicSplitPaneDivider.this || e.getSource() == splitPane) && dragger == null
          && splitPane.isContinuousLayout()) {
        dragger = new DragController(e);
      } else if (e.getSource() == BasicSplitPaneDivider.this && dragger == null) {
        dragger = new DragController(e);
      }
      if (dragger != null) {
        dragger.isValid();
        prepareForDragging();
        dragger.continueDrag(e);
        e.consume();
      }
    }

    public void mouseReleased(MouseEvent e) {
      if (dragger != null) {
        if (e.getSource() == splitPane) {
          dragger.completeDrag(e.getX(), e.getY());
        } else if (e.getSource() == BasicSplitPaneDivider.this) {
          java.awt.Point ourLoc = getLocation();
          dragger.completeDrag(e.getX() + ourLoc.x, e.getY() + ourLoc.y);
        }
        e.consume();
        dragger = null;
      }
    }

    public void mouseDragged(MouseEvent e) {
      if (dragger != null) {
        if (e.getSource() == splitPane) {
          dragger.continueDrag(e.getX(), e.getY());
        } else if (e.getSource() == BasicSplitPaneDivider.this) {
          java.awt.Point ourLoc = getLocation();
          dragger.continueDrag(e.getX() + ourLoc.x, e.getY() + ourLoc.y);
        }
        e.consume();
      }
    }

    public void mouseMoved(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
      if (e.getSource() == BasicSplitPaneDivider.this) {
        setMouseOver(true);
      }
    }

    public void mouseExited(MouseEvent e) {
      if (e.getSource() == BasicSplitPaneDivider.this) {
        setMouseOver(false);
      }
    }
  }

  protected class DragController {
    int initialX;
    int maxX;
    int minX;
    int offset;

    protected DragController(MouseEvent e) {
      JSplitPane splitPane = splitPaneUI.getSplitPane();
      Component leftC = splitPane.getLeftComponent();
      Component rightC = splitPane.getRightComponent();
      initialX = getLocation().x;
      if (e.getSource() == BasicSplitPaneDivider.this) {
        offset = e.getX();
      } else {
        offset = e.getX() - initialX;
      }
      if (leftC == null || rightC == null || offset < -1 || offset >= getSize().width) {
        maxX = -1;
      } else {
        Insets insets = splitPane.getInsets();
        if (leftC.isVisible()) {
          minX = leftC.getMinimumSize().width;
          if (insets != null) {
            minX += insets.left;
          }
        } else {
          minX = 0;
        }
        if (rightC.isVisible()) {
          int right = (insets != null) ? insets.right : 0;
          maxX = Math.max(0, splitPane.getSize().width - (getSize().width + right) - rightC.getMinimumSize().width);
        } else {
          int right = (insets != null) ? insets.right : 0;
          maxX = Math.max(0, splitPane.getSize().width - (getSize().width + right));
        }
        if (maxX < minX) {
          minX = maxX = 0;
        }
      }
    }

    protected boolean isValid() {
      return maxX > 0;
    }

    protected int positionForMouseEvent(MouseEvent e) {
      int newX = (e.getSource() == BasicSplitPaneDivider.this) ? (e.getX() + getLocation().x) : e.getX();
      newX = Math.min(maxX, Math.max(minX, newX - offset));
      return newX;
    }

    protected int getNeededLocation(int x, int y) {
      return Math.min(maxX, Math.max(minX, x - offset));
    }

    protected void continueDrag(int newX, int newY) {
      dragDividerTo(getNeededLocation(newX, newY));
    }

    protected void continueDrag(MouseEvent e) {
      dragDividerTo(positionForMouseEvent(e));
    }

    protected void completeDrag(int x, int y) {
      finishDraggingTo(getNeededLocation(x, y));
    }

    protected void completeDrag(MouseEvent e) {
      finishDraggingTo(positionForMouseEvent(e));
    }
  }

  protected class VerticalDragController extends DragController {
    protected VerticalDragController(MouseEvent e) {
      super(e);
    }
  }

  protected class DividerLayout implements java.awt.LayoutManager {
    public void layoutContainer(Container c) {
      if (leftButton != null && rightButton != null && c == BasicSplitPaneDivider.this) {
        if (splitPane.isOneTouchExpandable()) {
          Insets insets = getInsets();
          if (orientation == JSplitPane.VERTICAL_SPLIT) {
            int extraX = (insets != null) ? insets.left : 0;
            int blockSize = getHeight();
            if (insets != null) {
              blockSize -= (insets.top + insets.bottom);
              blockSize = Math.max(blockSize, 0);
            }
            blockSize = Math.min(blockSize, oneTouchSize);
            int y = (c.getSize().height - blockSize) / 2;
            if (!centerOneTouchButtons) {
              y = (insets != null) ? insets.top : 0;
              extraX = 0;
            }
            leftButton.setBounds(extraX + oneTouchOffset, y, blockSize * 2, blockSize);
            rightButton.setBounds(extraX + oneTouchOffset + oneTouchSize * 2, y, blockSize * 2, blockSize);
          } else {
            int extraY = (insets != null) ? insets.top : 0;
            int blockSize = getWidth();
            if (insets != null) {
              blockSize -= (insets.left + insets.right);
              blockSize = Math.max(blockSize, 0);
            }
            blockSize = Math.min(blockSize, oneTouchSize);
            int x = (c.getSize().width - blockSize) / 2;
            if (!centerOneTouchButtons) {
              x = (insets != null) ? insets.left : 0;
              extraY = 0;
            }
            leftButton.setBounds(x, extraY + oneTouchOffset, blockSize, blockSize * 2);
            rightButton.setBounds(x, extraY + oneTouchOffset + oneTouchSize * 2, blockSize, blockSize * 2);
          }
        } else {
          leftButton.setBounds(-5, -5, 1, 1);
          rightButton.setBounds(-5, -5, 1, 1);
        }
      }
    }

    public Dimension minimumLayoutSize(Container c) {
      return new Dimension(0, 0);
    }

    public Dimension preferredLayoutSize(Container c) {
      return new Dimension(0, 0);
    }

    public void removeLayoutComponent(Component c) {
    }

    public void addLayoutComponent(String string, Component c) {
    }
  }
}
