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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JComponent;
import javax.swing.JProgressBar;
import javax.swing.LookAndFeel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.UIResource;

public class BasicProgressBarUI extends javax.swing.plaf.ProgressBarUI {
  protected javax.swing.JProgressBar progressBar;
  protected javax.swing.event.ChangeListener changeListener;
  protected java.awt.Rectangle boxRect;
  public BasicProgressBarUI() {
  }
  protected void paintString(Graphics g, int x, int y, int width, int height, int amountFull, Insets b) {
    if (progressBar.getOrientation() == JProgressBar.HORIZONTAL) {
      if (progressBar.getComponentOrientation().isLeftToRight()) {
        if (progressBar.isIndeterminate()) {
          boxRect = getBox(boxRect);
          paintString(g, x, y, width, height, boxRect == null ? 0 : boxRect.x, boxRect == null ? 0 : boxRect.width, b);
        } else {
          paintString(g, x, y, width, height, x, amountFull, b);
        }
      } else {
        paintString(g, x, y, width, height, x + width - amountFull, amountFull, b);
      }
    } else {
      if (progressBar.isIndeterminate()) {
        boxRect = getBox(boxRect);
        paintString(g, x, y, width, height, boxRect == null ? 0 : boxRect.y, boxRect == null ? 0 : boxRect.height, b);
      } else {
        paintString(g, x, y, width, height, y + height - amountFull, amountFull, b);
      }
    }
  }
  protected void installDefaults() {
    LookAndFeel.installProperty(progressBar, "opaque", Boolean.TRUE);
    LookAndFeel.installColorsAndFont(progressBar, "ProgressBar.background", "ProgressBar.foreground",
        "ProgressBar.font");
    LookAndFeel.installBorder(progressBar, "ProgressBar.border");
    horizontalSize = UIManager.getDimension("ProgressBar.horizontalSize");
    if (horizontalSize == null) {
      horizontalSize = new Dimension(146, 12);
    }
    verticalSize = UIManager.getDimension("ProgressBar.verticalSize");
    if (verticalSize == null) {
      verticalSize = new Dimension(12, 146);
    }
    selectionForeground = UIManager.getColor("ProgressBar.selectionForeground");
    selectionBackground = UIManager.getColor("ProgressBar.selectionBackground");
    Object o = UIManager.get("ProgressBar.cellLength");
    cellLength = o instanceof Integer ? ((Integer) o).intValue() : 1;
    o = UIManager.get("ProgressBar.cellSpacing");
    cellSpacing = o instanceof Integer ? ((Integer) o).intValue() : 0;
    o = UIManager.get("ProgressBar.repaintInterval");
    repaintInterval = o instanceof Integer ? ((Integer) o).intValue() : 50;
    o = UIManager.get("ProgressBar.cycleTime");
    cycleTime = o instanceof Integer ? ((Integer) o).intValue() : 3000;
    numFrames = Math.max(2, 2 * (cycleTime / Math.max(1, repaintInterval) / 2));
  }
  protected void installListeners() {
    changeListener = new ChangeHandler();
    progressBar.addChangeListener(changeListener);
    progressBar.addHierarchyListener(new java.awt.event.HierarchyListener() {
      public void hierarchyChanged(java.awt.event.HierarchyEvent e) {
        if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.DISPLAYABILITY_CHANGED) != 0 && progressBar != null) {
          if (progressBar.isIndeterminate() && progressBar.isDisplayable()) {
            startAnimationTimer();
          } else {
            stopAnimationTimer();
          }
        }
      }
    });
    progressBar.addHierarchyListener(new java.awt.event.HierarchyListener() {
      public void hierarchyChanged(java.awt.event.HierarchyEvent e) {
        if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.DISPLAYABILITY_CHANGED) != 0 && progressBar != null) {
          if (progressBar.isIndeterminate() && progressBar.isDisplayable()) {
            startAnimationTimer();
          } else {
            stopAnimationTimer();
          }
        }
      }
    });
    progressBar.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
      public void propertyChange(java.beans.PropertyChangeEvent e) {
        String prop = e.getPropertyName();
        if ("indeterminate".equals(prop)) {
          if (progressBar.isIndeterminate() && progressBar.isDisplayable()) {
            startAnimationTimer();
          } else {
            stopAnimationTimer();
          }
        }
        progressBar.revalidate();
        progressBar.repaint();
      }
    });
  }
  protected void uninstallListeners() {
    progressBar.removeChangeListener(changeListener);
  }
  protected void uninstallDefaults() {
    LookAndFeel.uninstallBorder(progressBar);
  }
  protected void paintIndeterminate(Graphics g, JComponent c) {
    Insets b = progressBar.getInsets();
    int barRectWidth = progressBar.getWidth() - (b.right + b.left);
    int barRectHeight = progressBar.getHeight() - (b.top + b.bottom);
    if (barRectWidth <= 0 || barRectHeight <= 0) {
      return;
    }
    Graphics2D g2 = (Graphics2D) g;
    boxRect = getBox(boxRect);
    if (boxRect != null) {
      g2.setColor(progressBar.getForeground());
      g2.fillRect(boxRect.x, boxRect.y, boxRect.width, boxRect.height);
    }
    if (progressBar.isStringPainted()) {
      if (progressBar.getOrientation() == JProgressBar.HORIZONTAL) {
        paintString(g2, b.left, b.top, barRectWidth, barRectHeight, boxRect == null ? 0 : boxRect.x, b);
      } else {
        paintString(g2, b.left, b.top, barRectWidth, barRectHeight, boxRect == null ? 0 : boxRect.y, b);
      }
    }
  }
  protected void paintDeterminate(Graphics g, JComponent c) {
    Insets b = progressBar.getInsets();
    int barRectWidth = progressBar.getWidth() - (b.right + b.left);
    int barRectHeight = progressBar.getHeight() - (b.top + b.bottom);
    if (barRectWidth <= 0 || barRectHeight <= 0) {
      return;
    }
    int cellLength = getCellLength();
    int cellSpacing = getCellSpacing();
    int amountFull = getAmountFull(b, barRectWidth, barRectHeight);
    g.setColor(progressBar.getForeground());
    if (progressBar.getOrientation() == JProgressBar.HORIZONTAL) {
      if (progressBar.getComponentOrientation().isLeftToRight()) {
        if (cellSpacing == 0 && amountFull > 0) {
          g.fillRect(b.left, b.top, amountFull, barRectHeight);
        } else {
          int x = b.left;
          for (int i = 0; i < amountFull / (cellLength + cellSpacing); i++) {
            g.fillRect(x, b.top, cellLength, barRectHeight);
            x += cellLength + cellSpacing;
          }
        }
      } else {
        g.fillRect(b.left + barRectWidth - amountFull, b.top, amountFull, barRectHeight);
      }
    } else {
      if (cellSpacing == 0 && amountFull > 0) {
        g.fillRect(b.left, b.top + barRectHeight - amountFull, barRectWidth, amountFull);
      } else {
        int y = b.top + barRectHeight;
        for (int i = 0; i < amountFull / (cellLength + cellSpacing); i++) {
          y -= cellLength;
          g.fillRect(b.left, y, barRectWidth, cellLength);
          y -= cellSpacing;
        }
      }
    }
    if (progressBar.isStringPainted()) {
      paintString(g, b.left, b.top, barRectWidth, barRectHeight, amountFull, b);
    }
  }
  protected int getAnimationIndex() {
    return animationIndex;
  }
  protected int getBoxLength(int availableLength, int otherDimension) {
    return (int) Math.round(availableLength / 6.0);
  }
  protected Rectangle getBox(Rectangle r) {
    int currentFrame = getAnimationIndex();
    int middleFrame = numFrames / 2;
    if (sizeChanged() || delta == 0.0 || maxPosition == 0.0) {
      Insets b = progressBar.getInsets();
      int w = progressBar.getWidth();
      int h = progressBar.getHeight();
      if (progressBar.getOrientation() == JProgressBar.HORIZONTAL) {
        int bl = getBoxLength(w - b.left - b.right, h - b.top - b.bottom);
        maxPosition = w - b.left - b.right - bl;
      } else {
        int bl = getBoxLength(h - b.top - b.bottom, w - b.left - b.right);
        maxPosition = h - b.top - b.bottom - bl;
      }
      delta = 2.0 * (double) maxPosition / (double) numFrames;
    }
    if (r == null) {
      r = new Rectangle();
    }
    Insets b = progressBar.getInsets();
    int w = progressBar.getWidth() - b.left - b.right;
    int h = progressBar.getHeight() - b.top - b.bottom;
    if (w <= 0 || h <= 0) {
      return null;
    }
    boolean horizontal = progressBar.getOrientation() == JProgressBar.HORIZONTAL;
    int len = horizontal ? getBoxLength(w, h) : getBoxLength(h, w);
    int pos;
    if (currentFrame <= middleFrame) {
      pos = (int) Math.round(delta * currentFrame);
    } else {
      pos = maxPosition - (int) Math.round(delta * (currentFrame - middleFrame));
    }
    if (horizontal) {
      r.setBounds(b.left + (progressBar.getComponentOrientation().isLeftToRight() ? pos : maxPosition - pos), b.top, len, h);
    } else {
      r.setBounds(b.left, b.top + pos, w, len);
    }
    return r;
  }
  protected int getCellLength() {
    return progressBar.isStringPainted() ? 1 : cellLength;
  }
  protected int getCellSpacing() {
    return progressBar.isStringPainted() ? 0 : cellSpacing;
  }
  protected int getAmountFull(Insets b, int width, int height) {
    int amountFull = 0;
    javax.swing.BoundedRangeModel model = progressBar.getModel();
    if ((model.getMaximum() - model.getMinimum()) != 0) {
      if (progressBar.getOrientation() == JProgressBar.HORIZONTAL) {
        amountFull = (int) Math.round(width * progressBar.getPercentComplete());
      } else {
        amountFull = (int) Math.round(height * progressBar.getPercentComplete());
      }
    }
    return amountFull;
  }
  protected Point getStringPlacement(Graphics g, String progressString, int x, int y, int width, int height) {
    FontMetrics fontSizer = progressBar.getFontMetrics(progressBar.getFont());
    int stringWidth = fontSizer.stringWidth(progressString);
    if (progressBar.getOrientation() == JProgressBar.HORIZONTAL) {
      return new Point(x + Math.round(width / 2 - stringWidth / 2), y + ((height + fontSizer.getAscent()
          - fontSizer.getLeading() - fontSizer.getDescent()) / 2) - 1);
    }
    return new Point(x + Math.round(width / 2 - fontSizer.getHeight() / 2 + fontSizer.getAscent()),
        y + Math.round(height / 2 - stringWidth / 2));
  }
  protected Dimension getPreferredInnerHorizontal() {
    return new Dimension(horizontalSize);
  }
  protected Dimension getPreferredInnerVertical() {
    return new Dimension(verticalSize);
  }
  protected void setAnimationIndex(int newValue) {
    if (animationIndex != newValue) {
      if (sizeChanged()) {
        animationIndex = newValue;
        maxPosition = 0;
        delta = 0.0;
        progressBar.repaint();
        return;
      }
      nextPaintRect = getBox(nextPaintRect);
      animationIndex = newValue;
      if (nextPaintRect != null) {
        boxRect = getBox(boxRect);
        if (boxRect != null) {
          nextPaintRect.add(boxRect);
        }
      }
    } else {
      return;
    }
    if (nextPaintRect != null) {
      progressBar.repaint(nextPaintRect);
    } else {
      progressBar.repaint();
    }
  }
  protected void startAnimationTimer() {
    if (timer == null) {
      timer = new Timer(repaintInterval, new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          incrementAnimationIndex();
          if (progressBar != null) {
            progressBar.repaint();
          }
        }
      });
      timer.setRepeats(true);
    }
    timer.start();
  }
  protected void stopAnimationTimer() {
    if (timer != null) {
      timer.stop();
      timer = null;
    }
    setAnimationIndex(0);
  }
  protected void setCellLength(int cellLen) {
    this.cellLength = cellLen;
  }
  protected void setCellSpacing(int cellSpace) {
    this.cellSpacing = cellSpace;
  }
  protected final int getFrameCount() {
    return numFrames;
  }
  protected void incrementAnimationIndex() {
    int newValue = animationIndex + 1;
    if (newValue < numFrames) {
      setAnimationIndex(newValue);
    } else {
      setAnimationIndex(0);
    }
  }
  protected Color getSelectionForeground() {
    return selectionForeground;
  }
  protected Color getSelectionBackground() {
    return selectionBackground;
  }
  public static javax.swing.plaf.ComponentUI createUI(JComponent x) {
    return new BasicProgressBarUI();
  }
  public Dimension getMaximumSize(JComponent c) {
    Dimension pref = getPreferredSize(progressBar);
    if (progressBar.getOrientation() == JProgressBar.HORIZONTAL) {
      pref.width = Short.MAX_VALUE;
    } else {
      pref.height = Short.MAX_VALUE;
    }
    return pref;
  }
  public void paint(Graphics g, JComponent c) {
    if (progressBar.isIndeterminate()) {
      paintIndeterminate(g, c);
    } else {
      paintDeterminate(g, c);
    }
  }
  public Dimension getPreferredSize(JComponent c) {
    Dimension size;
    Insets border = progressBar.getInsets();
    FontMetrics fontSizer = progressBar.getFontMetrics(progressBar.getFont());
    if (progressBar.getOrientation() == JProgressBar.HORIZONTAL) {
      size = new Dimension(getPreferredInnerHorizontal());
      if (progressBar.isStringPainted()) {
        String progString = progressBar.getString();
        int stringWidth = fontSizer.stringWidth(progString);
        if (stringWidth > size.width) {
          size.width = stringWidth;
        }
        int stringHeight = fontSizer.getHeight() + fontSizer.getDescent();
        if (stringHeight > size.height) {
          size.height = stringHeight;
        }
      }
    } else {
      size = new Dimension(getPreferredInnerVertical());
      if (progressBar.isStringPainted()) {
        String progString = progressBar.getString();
        int stringHeight = fontSizer.getHeight() + fontSizer.getDescent();
        if (stringHeight > size.width) {
          size.width = stringHeight;
        }
        int stringWidth = fontSizer.stringWidth(progString);
        if (stringWidth > size.height) {
          size.height = stringWidth;
        }
      }
    }
    size.width += border.left + border.right;
    size.height += border.top + border.bottom;
    return size;
  }
  public Dimension getMinimumSize(JComponent c) {
    Dimension pref = getPreferredSize(progressBar);
    if (progressBar.getOrientation() == JProgressBar.HORIZONTAL) {
      pref.width = 10;
    } else {
      pref.height = 10;
    }
    return pref;
  }
  public int getBaseline(JComponent c, int width, int height) {
    super.getBaseline(c, width, height);
    if (progressBar.isStringPainted() && progressBar.getOrientation() == JProgressBar.HORIZONTAL) {
      FontMetrics metrics = progressBar.getFontMetrics(progressBar.getFont());
      Insets insets = progressBar.getInsets();
      int y = insets.top;
      height = height - insets.top - insets.bottom;
      return y + (height + metrics.getAscent() - metrics.getLeading() - metrics.getDescent()) / 2;
    }
    return -1;
  }
  public Component.BaselineResizeBehavior getBaselineResizeBehavior(JComponent c) {
    super.getBaselineResizeBehavior(c);
    if (progressBar.isStringPainted() && progressBar.getOrientation() == JProgressBar.HORIZONTAL) {
      return Component.BaselineResizeBehavior.CENTER_OFFSET;
    }
    return Component.BaselineResizeBehavior.OTHER;
  }
  public void installUI(JComponent c) {
    progressBar = (JProgressBar) c;
    installDefaults();
    installListeners();
    if (progressBar.isIndeterminate() && progressBar.isDisplayable()) {
      startAnimationTimer();
    }
  }
  public void uninstallUI(JComponent c) {
    if (progressBar.isIndeterminate()) {
      stopAnimationTimer();
    }
    uninstallDefaults();
    uninstallListeners();
    progressBar = null;
  }

  public class ChangeHandler implements javax.swing.event.ChangeListener {
    public ChangeHandler() {
    }
    public void stateChanged(ChangeEvent e) {
      if (progressBar.isIndeterminate()) {
        return;
      }
      progressBar.repaint();
    }
  }
  private Dimension horizontalSize;
  private Dimension verticalSize;
  private int cellLength;
  private int cellSpacing;
  private int animationIndex = 0;
  private int numFrames;
  private Timer timer;
  private Color selectionForeground;
  private Color selectionBackground;
  private int repaintInterval;
  private int cycleTime;
  private Rectangle nextPaintRect;
  private int maxPosition;
  private double delta;
  private int oldWidth;
  private int oldHeight;

  private boolean sizeChanged() {
    if (oldWidth != progressBar.getWidth() || oldHeight != progressBar.getHeight()) {
      oldWidth = progressBar.getWidth();
      oldHeight = progressBar.getHeight();
      return true;
    }
    return false;
  }

  private void paintString(Graphics g, int x, int y, int width, int height, int fillStart, int amountFull, Insets b) {
    if (!(g instanceof Graphics2D)) {
      return;
    }
    Graphics2D g2 = (Graphics2D) g;
    String progressString = progressBar.getString();
    g2.setFont(progressBar.getFont());
    Point renderLocation = getStringPlacement(g2, progressString, x, y, width, height);
    Rectangle oldClip = g2.getClipBounds();
    if (progressBar.getOrientation() == JProgressBar.HORIZONTAL) {
      g2.setColor(getSelectionBackground());
      g2.drawString(progressString, renderLocation.x, renderLocation.y);
      g2.setColor(getSelectionForeground());
      g2.clipRect(fillStart, y, amountFull, height);
      g2.drawString(progressString, renderLocation.x, renderLocation.y);
    } else {
      g2.setColor(getSelectionBackground());
      java.awt.geom.AffineTransform rotate = java.awt.geom.AffineTransform.getRotateInstance(Math.PI / 2);
      g2.setFont(progressBar.getFont().deriveFont(rotate));
      renderLocation = getStringPlacement(g2, progressString, x, y, width, height);
      g2.drawString(progressString, renderLocation.x, renderLocation.y);
      g2.setColor(getSelectionForeground());
      g2.clipRect(x, fillStart, width, amountFull);
      g2.drawString(progressString, renderLocation.x, renderLocation.y);
    }
    g2.setClip(oldClip);
  }
}
