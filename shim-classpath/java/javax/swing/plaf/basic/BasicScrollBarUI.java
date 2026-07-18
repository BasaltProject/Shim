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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.LookAndFeel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.ScrollBarUI;
import javax.swing.plaf.UIResource;

public class BasicScrollBarUI extends ScrollBarUI implements java.awt.LayoutManager, SwingConstants {
  private boolean isThumbRollover;
  private boolean supportsAbsolutePositioning;
  protected Dimension minimumThumbSize;
  protected Dimension maximumThumbSize;
  protected Color thumbHighlightColor;
  protected Color thumbLightShadowColor;
  protected Color thumbDarkShadowColor;
  protected Color thumbColor;
  protected Color trackColor;
  protected Color trackHighlightColor;
  protected JScrollBar scrollbar;
  protected JButton incrButton;
  protected JButton decrButton;
  protected boolean isDragging;
  protected Rectangle thumbRect;
  protected Rectangle trackRect;
  protected int trackHighlight;
  protected static final int NO_HIGHLIGHT = 0;
  protected static final int DECREASE_HIGHLIGHT = 1;
  protected static final int INCREASE_HIGHLIGHT = 2;
  protected PropertyChangeListener propertyChangeListener;
  protected Timer scrollTimer;
  protected int scrollBarWidth;
  protected int incrGap;
  protected int decrGap;
  private static final int kScrollSpeedThrottle = 60;
  private ChangeListener modelListener;
  private MouseInputAdapter trackListener;
  private ActionListener scrollListener;
  private int scrollDirection = 1;
  private boolean scrollByBlock;
  private int offset;
  private int currentMouseX;
  private int currentMouseY;

  public BasicScrollBarUI() {
  }

  public static ComponentUI createUI(JComponent c) {
    return new BasicScrollBarUI();
  }

  public void installUI(JComponent c) {
    scrollbar = (JScrollBar) c;
    thumbRect = new Rectangle(0, 0, 0, 0);
    trackRect = new Rectangle(0, 0, 0, 0);
    installDefaults();
    installComponents();
    installListeners();
    installKeyboardActions();
  }

  public void uninstallUI(JComponent c) {
    scrollbar = (JScrollBar) c;
    uninstallListeners();
    uninstallDefaults();
    uninstallComponents();
    uninstallKeyboardActions();
    thumbRect = null;
    scrollbar = null;
    incrButton = null;
    decrButton = null;
  }

  protected void installDefaults() {
    scrollBarWidth = UIManager.getInt("ScrollBar.width");
    if (scrollBarWidth <= 0) {
      scrollBarWidth = 16;
    }
    minimumThumbSize = (Dimension) UIManager.get("ScrollBar.minimumThumbSize");
    maximumThumbSize = (Dimension) UIManager.get("ScrollBar.maximumThumbSize");
    Object abs = UIManager.get("ScrollBar.allowsAbsolutePositioning");
    supportsAbsolutePositioning = abs instanceof Boolean && ((Boolean) abs).booleanValue();
    trackHighlight = NO_HIGHLIGHT;
    if (scrollbar.getLayout() == null || (scrollbar.getLayout() instanceof UIResource)) {
      scrollbar.setLayout(this);
    }
    configureScrollBarColors();
    LookAndFeel.installBorder(scrollbar, "ScrollBar.border");
    LookAndFeel.installProperty(scrollbar, "opaque", Boolean.TRUE);
    scrollbar.setFocusable(true);
    scrollbar.setRequestFocusEnabled(false);
    incrGap = UIManager.getInt("ScrollBar.incrementButtonGap");
    decrGap = UIManager.getInt("ScrollBar.decrementButtonGap");
  }

  protected void configureScrollBarColors() {
    LookAndFeel.installColors(scrollbar, "ScrollBar.background", "ScrollBar.foreground");
    thumbHighlightColor = UIManager.getColor("ScrollBar.thumbHighlight");
    thumbLightShadowColor = UIManager.getColor("ScrollBar.thumbShadow");
    thumbDarkShadowColor = UIManager.getColor("ScrollBar.thumbDarkShadow");
    thumbColor = UIManager.getColor("ScrollBar.thumb");
    trackColor = UIManager.getColor("ScrollBar.track");
    trackHighlightColor = UIManager.getColor("ScrollBar.trackHighlight");
  }

  protected void uninstallDefaults() {
    scrollbar.setLayout(null);
  }

  protected void installComponents() {
    switch (scrollbar.getOrientation()) {
      case JScrollBar.VERTICAL:
        incrButton = createIncreaseButton(SOUTH);
        decrButton = createDecreaseButton(NORTH);
        break;
      case JScrollBar.HORIZONTAL:
        if (scrollbar.getComponentOrientation().isLeftToRight()) {
          incrButton = createIncreaseButton(EAST);
          decrButton = createDecreaseButton(WEST);
        } else {
          incrButton = createIncreaseButton(WEST);
          decrButton = createDecreaseButton(EAST);
        }
        break;
      default:
    }
    scrollbar.add(incrButton);
    scrollbar.add(decrButton);
    scrollbar.setEnabled(scrollbar.isEnabled());
  }

  protected void uninstallComponents() {
    scrollbar.remove(incrButton);
    scrollbar.remove(decrButton);
  }

  protected void installListeners() {
    trackListener = new TrackListener();
    scrollListener = new ScrollListener();
    modelListener = new ModelListener();
    propertyChangeListener = createPropertyChangeListener();
    scrollbar.addMouseListener(trackListener);
    scrollbar.addMouseMotionListener(trackListener);
    scrollbar.getModel().addChangeListener(modelListener);
    scrollbar.addPropertyChangeListener(propertyChangeListener);
    if (incrButton != null) {
      incrButton.addMouseListener(new ArrowListener(1));
    }
    if (decrButton != null) {
      decrButton.addMouseListener(new ArrowListener(-1));
    }
    scrollTimer = new Timer(kScrollSpeedThrottle, scrollListener);
    scrollTimer.setInitialDelay(300);
  }

  protected void uninstallListeners() {
    scrollTimer.stop();
    scrollTimer = null;
    scrollbar.getModel().removeChangeListener(modelListener);
    scrollbar.removeMouseListener(trackListener);
    scrollbar.removeMouseMotionListener(trackListener);
    scrollbar.removePropertyChangeListener(propertyChangeListener);
  }

  protected void installKeyboardActions() {
    shim.swing.KeyActions.install(scrollbar, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
        "ScrollBar.ancestorInputMap", shim.swing.KeyActions.scrollBarActions());
  }

  protected void uninstallKeyboardActions() {
    shim.swing.KeyActions.uninstall(scrollbar, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
  }

  protected PropertyChangeListener createPropertyChangeListener() {
    return new PropertyChangeHandler();
  }

  protected JButton createDecreaseButton(int orientation) {
    return new BasicArrowButton(orientation, UIManager.getColor("ScrollBar.thumb"),
        UIManager.getColor("ScrollBar.thumbShadow"), UIManager.getColor("ScrollBar.thumbDarkShadow"),
        UIManager.getColor("ScrollBar.thumbHighlight"));
  }

  protected JButton createIncreaseButton(int orientation) {
    return createDecreaseButton(orientation);
  }

  public boolean isThumbRollover() {
    return isThumbRollover;
  }

  protected void setThumbRollover(boolean active) {
    if (isThumbRollover != active) {
      isThumbRollover = active;
      scrollbar.repaint(getThumbBounds());
    }
  }

  public boolean getSupportsAbsolutePositioning() {
    return supportsAbsolutePositioning;
  }

  protected Rectangle getThumbBounds() {
    return thumbRect;
  }

  protected Rectangle getTrackBounds() {
    return trackRect;
  }

  protected Dimension getMinimumThumbSize() {
    return minimumThumbSize;
  }

  protected Dimension getMaximumThumbSize() {
    return maximumThumbSize;
  }

  public void paint(Graphics g, JComponent c) {
    paintTrack(g, c, getTrackBounds());
    Rectangle thumbBounds = getThumbBounds();
    if (thumbBounds.intersects(g.getClipBounds())) {
      paintThumb(g, c, thumbBounds);
    }
  }

  public Dimension getPreferredSize(JComponent c) {
    return (scrollbar.getOrientation() == JScrollBar.VERTICAL) ? new Dimension(scrollBarWidth, 48)
        : new Dimension(48, scrollBarWidth);
  }

  public Dimension getMaximumSize(JComponent c) {
    return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
  }

  protected void paintDecreaseHighlight(Graphics g) {
    Insets insets = scrollbar.getInsets();
    Rectangle thumbR = getThumbBounds();
    g.setColor(trackHighlightColor);
    if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
      int x = insets.left;
      int y = trackRect.y;
      int w = scrollbar.getWidth() - (insets.left + insets.right);
      int h = thumbR.y - y;
      g.fillRect(x, y, w, h);
    } else {
      int x;
      int w;
      if (scrollbar.getComponentOrientation().isLeftToRight()) {
        x = trackRect.x;
        w = thumbR.x - x;
      } else {
        x = thumbR.x + thumbR.width;
        w = trackRect.x + trackRect.width - x;
      }
      int y = insets.top;
      int h = scrollbar.getHeight() - (insets.top + insets.bottom);
      g.fillRect(x, y, w, h);
    }
  }

  protected void paintIncreaseHighlight(Graphics g) {
    Insets insets = scrollbar.getInsets();
    Rectangle thumbR = getThumbBounds();
    g.setColor(trackHighlightColor);
    if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
      int x = insets.left;
      int y = thumbR.y + thumbR.height;
      int w = scrollbar.getWidth() - (insets.left + insets.right);
      int h = trackRect.y + trackRect.height - y;
      g.fillRect(x, y, w, h);
    } else {
      int x;
      int w;
      if (scrollbar.getComponentOrientation().isLeftToRight()) {
        x = thumbR.x + thumbR.width;
        w = trackRect.x + trackRect.width - x;
      } else {
        x = trackRect.x;
        w = thumbR.x - x;
      }
      int y = insets.top;
      int h = scrollbar.getHeight() - (insets.top + insets.bottom);
      g.fillRect(x, y, w, h);
    }
  }

  protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
    g.setColor(trackColor);
    g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
    if (trackHighlight == DECREASE_HIGHLIGHT) {
      paintDecreaseHighlight(g);
    } else if (trackHighlight == INCREASE_HIGHLIGHT) {
      paintIncreaseHighlight(g);
    }
  }

  protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
    if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
      return;
    }
    int w = thumbBounds.width;
    int h = thumbBounds.height;
    g.translate(thumbBounds.x, thumbBounds.y);
    g.setColor(thumbDarkShadowColor);
    g.drawRect(0, 0, w - 1, h - 1);
    g.setColor(thumbColor);
    g.fillRect(0, 0, w - 1, h - 1);
    g.setColor(thumbHighlightColor);
    g.drawLine(1, 1, 1, h - 2);
    g.drawLine(2, 1, w - 3, 1);
    g.setColor(thumbLightShadowColor);
    g.drawLine(2, h - 2, w - 2, h - 2);
    g.drawLine(w - 2, 1, w - 2, h - 3);
    g.translate(-thumbBounds.x, -thumbBounds.y);
  }

  protected void setThumbBounds(int x, int y, int width, int height) {
    if ((thumbRect.x == x) && (thumbRect.y == y) && (thumbRect.width == width) && (thumbRect.height == height)) {
      return;
    }
    int minX = Math.min(x, thumbRect.x);
    int minY = Math.min(y, thumbRect.y);
    int maxX = Math.max(x + width, thumbRect.x + thumbRect.width);
    int maxY = Math.max(y + height, thumbRect.y + thumbRect.height);
    thumbRect.setBounds(x, y, width, height);
    scrollbar.repaint(minX, minY, maxX - minX, maxY - minY);
    setThumbRollover(false);
  }

  public void layoutContainer(Container scrollbarContainer) {
    if (isDragging) {
      return;
    }
    JScrollBar scrollbar = (JScrollBar) scrollbarContainer;
    switch (scrollbar.getOrientation()) {
      case JScrollBar.VERTICAL:
        layoutVScrollbar(scrollbar);
        break;
      case JScrollBar.HORIZONTAL:
        layoutHScrollbar(scrollbar);
        break;
      default:
    }
  }

  protected void layoutVScrollbar(JScrollBar sb) {
    Dimension sbSize = sb.getSize();
    Insets sbInsets = sb.getInsets();
    int itemW = sbSize.width - (sbInsets.left + sbInsets.right);
    int itemX = sbInsets.left;
    boolean squareButtons = Boolean.TRUE.equals(UIManager.get("ScrollBar.squareButtons"));
    int decrButtonH = squareButtons ? itemW : decrButton.getPreferredSize().height;
    int decrButtonY = sbInsets.top;
    int incrButtonH = squareButtons ? itemW : incrButton.getPreferredSize().height;
    int incrButtonY = sbSize.height - (sbInsets.bottom + incrButtonH);
    int sbInsetsH = sbInsets.top + sbInsets.bottom;
    int sbButtonsH = decrButtonH + incrButtonH;
    float trackH = sbSize.height - (sbInsetsH + sbButtonsH);
    float min = sb.getMinimum();
    float extent = sb.getVisibleAmount();
    float range = sb.getMaximum() - min;
    float value = sb.getValue();
    int thumbH = (range <= 0) ? getMaximumThumbSize().height : (int) (trackH * (extent / range));
    thumbH = Math.max(thumbH, getMinimumThumbSize().height);
    thumbH = Math.min(thumbH, getMaximumThumbSize().height);
    int thumbY = incrButtonY - incrGap - thumbH;
    if (value < (sb.getMaximum() - sb.getVisibleAmount())) {
      float thumbRange = trackH - thumbH;
      thumbY = (int) (0.5f + (thumbRange * ((value - min) / (range - extent))));
      thumbY += decrButtonY + decrButtonH + decrGap;
    }
    int sbAvailButtonH = (sbSize.height - sbInsetsH);
    if (sbAvailButtonH < sbButtonsH) {
      incrButtonH = decrButtonH = sbAvailButtonH / 2;
      incrButtonY = sbSize.height - (sbInsets.bottom + incrButtonH);
    }
    decrButton.setBounds(itemX, decrButtonY, itemW, decrButtonH);
    incrButton.setBounds(itemX, incrButtonY, itemW, incrButtonH);
    trackRect.setBounds(itemX, decrButtonY + decrButtonH + decrGap, itemW,
        incrButtonY - incrGap - (decrButtonY + decrButtonH + decrGap));
    if (thumbH >= (int) trackH) {
      setThumbBounds(0, 0, 0, 0);
    } else {
      if ((thumbY + thumbH) > incrButtonY - incrGap) {
        thumbY = incrButtonY - incrGap - thumbH;
      }
      if (thumbY < (decrButtonY + decrButtonH + decrGap)) {
        thumbY = decrButtonY + decrButtonH + decrGap + 1;
      }
      setThumbBounds(itemX, thumbY, itemW, thumbH);
    }
  }

  protected void layoutHScrollbar(JScrollBar sb) {
    Dimension sbSize = sb.getSize();
    Insets sbInsets = sb.getInsets();
    int itemH = sbSize.height - (sbInsets.top + sbInsets.bottom);
    int itemY = sbInsets.top;
    boolean ltr = sb.getComponentOrientation().isLeftToRight();
    boolean squareButtons = Boolean.TRUE.equals(UIManager.get("ScrollBar.squareButtons"));
    int leftButtonW = squareButtons ? itemH : decrButton.getPreferredSize().width;
    int rightButtonW = squareButtons ? itemH : incrButton.getPreferredSize().width;
    if (!ltr) {
      int t = leftButtonW;
      leftButtonW = rightButtonW;
      rightButtonW = t;
    }
    int leftButtonX = sbInsets.left;
    int rightButtonX = sbSize.width - (sbInsets.right + rightButtonW);
    int leftGap = ltr ? decrGap : incrGap;
    int rightGap = ltr ? incrGap : decrGap;
    int sbInsetsW = sbInsets.left + sbInsets.right;
    int sbButtonsW = leftButtonW + rightButtonW;
    float trackW = sbSize.width - (sbInsetsW + sbButtonsW);
    float min = sb.getMinimum();
    float max = sb.getMaximum();
    float extent = sb.getVisibleAmount();
    float range = max - min;
    float value = sb.getValue();
    int thumbW = (range <= 0) ? getMaximumThumbSize().width : (int) (trackW * (extent / range));
    thumbW = Math.max(thumbW, getMinimumThumbSize().width);
    thumbW = Math.min(thumbW, getMaximumThumbSize().width);
    int thumbX = ltr ? rightButtonX - rightGap - thumbW : leftButtonX + leftButtonW + leftGap;
    if (value < (max - sb.getVisibleAmount())) {
      float thumbRange = trackW - thumbW;
      if (ltr) {
        thumbX = (int) (0.5f + (thumbRange * ((value - min) / (range - extent))));
      } else {
        thumbX = (int) (0.5f + (thumbRange * ((max - extent - value) / (range - extent))));
      }
      thumbX += leftButtonX + leftButtonW + leftGap;
    }
    int sbAvailButtonW = (sbSize.width - sbInsetsW);
    if (sbAvailButtonW < sbButtonsW) {
      rightButtonW = leftButtonW = sbAvailButtonW / 2;
      rightButtonX = sbSize.width - (sbInsets.right + rightButtonW + rightGap);
    }
    (ltr ? decrButton : incrButton).setBounds(leftButtonX, itemY, leftButtonW, itemH);
    (ltr ? incrButton : decrButton).setBounds(rightButtonX, itemY, rightButtonW, itemH);
    int trackX = leftButtonX + leftButtonW + leftGap;
    trackRect.setBounds(trackX, itemY, rightButtonX - rightGap - trackX, itemH);
    if (thumbW >= (int) trackW) {
      setThumbBounds(0, 0, 0, 0);
    } else {
      if (thumbX + thumbW > rightButtonX - rightGap) {
        thumbX = rightButtonX - rightGap - thumbW;
      }
      if (thumbX < leftButtonX + leftButtonW + leftGap) {
        thumbX = leftButtonX + leftButtonW + leftGap + 1;
      }
      setThumbBounds(thumbX, itemY, thumbW, itemH);
    }
  }

  public void addLayoutComponent(String name, Component child) {
  }

  public void removeLayoutComponent(Component child) {
  }

  public Dimension preferredLayoutSize(Container scrollbarContainer) {
    return getPreferredSize((JComponent) scrollbarContainer);
  }

  public Dimension minimumLayoutSize(Container scrollbarContainer) {
    return getMinimumSize((JComponent) scrollbarContainer);
  }

  protected void scrollByBlock(int direction) {
    int oldValue = scrollbar.getValue();
    int blockIncrement = scrollbar.getBlockIncrement(direction);
    int delta = blockIncrement * ((direction > 0) ? +1 : -1);
    int newValue = oldValue + delta;
    if (delta > 0 && newValue < oldValue) {
      newValue = scrollbar.getMaximum();
    } else if (delta < 0 && newValue > oldValue) {
      newValue = scrollbar.getMinimum();
    }
    scrollbar.setValue(newValue);
  }

  protected void scrollByUnit(int direction) {
    int delta = scrollbar.getUnitIncrement(direction);
    if (direction <= 0) {
      delta = -delta;
    }
    int oldValue = scrollbar.getValue();
    int newValue = oldValue + delta;
    if (delta > 0 && newValue < oldValue) {
      newValue = scrollbar.getMaximum();
    } else if (delta < 0 && newValue > oldValue) {
      newValue = scrollbar.getMinimum();
    }
    scrollbar.setValue(newValue);
  }

  private int valueForPosition(int pos) {
    int value;
    int min = scrollbar.getMinimum();
    int max = scrollbar.getMaximum();
    int extent = scrollbar.getVisibleAmount();
    if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
      int thumbMin = trackRect.y;
      int thumbMax = trackRect.y + trackRect.height - thumbRect.height;
      value = pos <= thumbMin ? min : pos >= thumbMax ? max - extent
          : min + (int) ((long) (max - extent - min) * (pos - thumbMin) / Math.max(1, thumbMax - thumbMin));
    } else {
      int thumbMin = trackRect.x;
      int thumbMax = trackRect.x + trackRect.width - thumbRect.width;
      value = pos <= thumbMin ? min : pos >= thumbMax ? max - extent
          : min + (int) ((long) (max - extent - min) * (pos - thumbMin) / Math.max(1, thumbMax - thumbMin));
      if (!scrollbar.getComponentOrientation().isLeftToRight()) {
        value = max - extent - (value - min);
      }
    }
    return value;
  }

  private class TrackListener extends MouseInputAdapter {
    public void mousePressed(MouseEvent e) {
      if (!SwingUtilities.isLeftMouseButton(e) || !scrollbar.isEnabled()) {
        return;
      }
      if (!scrollbar.hasFocus() && scrollbar.isRequestFocusEnabled()) {
        scrollbar.requestFocus();
      }
      scrollbar.setValueIsAdjusting(true);
      currentMouseX = e.getX();
      currentMouseY = e.getY();
      if (getThumbBounds().contains(currentMouseX, currentMouseY)) {
        if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
          offset = currentMouseY - getThumbBounds().y;
        } else {
          offset = currentMouseX - getThumbBounds().x;
        }
        isDragging = true;
        return;
      }
      isDragging = false;
      Dimension sbSize = scrollbar.getSize();
      int direction = +1;
      if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
        direction = currentMouseY < getThumbBounds().y ? -1 : +1;
      } else {
        boolean ltr = scrollbar.getComponentOrientation().isLeftToRight();
        direction = (currentMouseX < getThumbBounds().x) == ltr ? -1 : +1;
      }
      scrollByBlock = true;
      scrollDirection = direction;
      trackHighlight = direction > 0 ? INCREASE_HIGHLIGHT : DECREASE_HIGHLIGHT;
      scrollByBlock(direction);
      scrollTimer.stop();
      scrollTimer.start();
      scrollbar.repaint();
    }

    public void mouseReleased(MouseEvent e) {
      if (!SwingUtilities.isLeftMouseButton(e)) {
        return;
      }
      if (isDragging) {
        isDragging = false;
      }
      scrollTimer.stop();
      trackHighlight = NO_HIGHLIGHT;
      scrollbar.setValueIsAdjusting(false);
      scrollbar.repaint();
    }

    public void mouseDragged(MouseEvent e) {
      if (!SwingUtilities.isLeftMouseButton(e) || !scrollbar.isEnabled()) {
        return;
      }
      currentMouseX = e.getX();
      currentMouseY = e.getY();
      if (isDragging) {
        int pos = scrollbar.getOrientation() == JScrollBar.VERTICAL ? currentMouseY - offset
            : currentMouseX - offset;
        scrollbar.setValue(valueForPosition(pos));
      }
    }

    public void mouseMoved(MouseEvent e) {
      if (!isDragging) {
        setThumbRollover(getThumbBounds().contains(e.getX(), e.getY()));
      }
    }

    public void mouseExited(MouseEvent e) {
      if (!isDragging) {
        setThumbRollover(false);
      }
    }
  }

  private class ArrowListener extends MouseInputAdapter {
    private final int direction;

    ArrowListener(int direction) {
      this.direction = direction;
    }

    public void mousePressed(MouseEvent e) {
      if (!SwingUtilities.isLeftMouseButton(e) || !scrollbar.isEnabled()) {
        return;
      }
      scrollByBlock = false;
      scrollDirection = direction;
      scrollbar.setValueIsAdjusting(true);
      scrollByUnit(direction);
      scrollTimer.stop();
      scrollTimer.start();
    }

    public void mouseReleased(MouseEvent e) {
      scrollTimer.stop();
      scrollbar.setValueIsAdjusting(false);
    }
  }

  private class ScrollListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      if (scrollByBlock) {
        Rectangle t = getThumbBounds();
        boolean vertical = scrollbar.getOrientation() == JScrollBar.VERTICAL;
        int pos = vertical ? currentMouseY : currentMouseX;
        int start = vertical ? t.y : t.x;
        int end = vertical ? t.y + t.height : t.x + t.width;
        boolean ltr = vertical || scrollbar.getComponentOrientation().isLeftToRight();
        if ((scrollDirection > 0 && ltr && pos < end) || (scrollDirection < 0 && ltr && pos >= start)) {
          scrollTimer.stop();
          trackHighlight = NO_HIGHLIGHT;
          scrollbar.repaint();
          return;
        }
        scrollByBlock(scrollDirection);
      } else {
        scrollByUnit(scrollDirection);
      }
      if (scrollDirection > 0 && scrollbar.getValue() + scrollbar.getVisibleAmount() >= scrollbar.getMaximum()) {
        scrollTimer.stop();
      } else if (scrollDirection < 0 && scrollbar.getValue() <= scrollbar.getMinimum()) {
        scrollTimer.stop();
      }
    }
  }

  private class ModelListener implements ChangeListener {
    public void stateChanged(ChangeEvent e) {
      if (!isDragging) {
        layoutContainer(scrollbar);
      }
      scrollbar.repaint();
    }
  }

  public class PropertyChangeHandler implements PropertyChangeListener {
    public PropertyChangeHandler() {
    }

    public void propertyChange(PropertyChangeEvent e) {
      String propertyName = e.getPropertyName();
      if ("model".equals(propertyName)) {
        BoundedRangeModel oldModel = (BoundedRangeModel) e.getOldValue();
        BoundedRangeModel newModel = (BoundedRangeModel) e.getNewValue();
        oldModel.removeChangeListener(modelListener);
        newModel.addChangeListener(modelListener);
        scrollbar.repaint();
        scrollbar.revalidate();
      } else if ("orientation".equals(propertyName)) {
        scrollbar.remove(incrButton);
        scrollbar.remove(decrButton);
        installComponents();
        scrollbar.revalidate();
        scrollbar.repaint();
      } else if ("enabled".equals(propertyName)) {
        Boolean b = (Boolean) e.getNewValue();
        if (incrButton != null) {
          incrButton.setEnabled(b.booleanValue());
        }
        if (decrButton != null) {
          decrButton.setEnabled(b.booleanValue());
        }
      } else if ("componentOrientation".equals(propertyName)) {
        scrollbar.revalidate();
      }
    }
  }
}
