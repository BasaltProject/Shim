/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf.basic;

import java.awt.Canvas;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import javax.swing.JSplitPane;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.UIResource;

public class BasicSplitPaneUI extends javax.swing.plaf.SplitPaneUI {
  private javax.swing.plaf.basic.BasicSplitPaneDivider bp_Divider;
  private boolean bp_ContinuousLayout;
  private javax.swing.JSplitPane bp_SplitPane;
  private int bp_LastDragLocation;
  private java.awt.Component bp_NonContinuousLayoutDivider;
  private int bp_Orientation;
  protected static final java.lang.String NON_CONTINUOUS_DIVIDER = null;
  protected static int KEYBOARD_DIVIDER_MOVE_OFFSET;
  protected javax.swing.JSplitPane splitPane;
  protected javax.swing.plaf.basic.BasicSplitPaneUI.BasicHorizontalLayoutManager layoutManager;
  protected javax.swing.plaf.basic.BasicSplitPaneDivider divider;
  protected java.beans.PropertyChangeListener propertyChangeListener;
  protected java.awt.event.FocusListener focusListener;
  protected int dividerSize;
  protected java.awt.Component nonContinuousLayoutDivider;
  protected boolean draggingHW;
  protected int beginDragDividerLocation;
  protected javax.swing.KeyStroke upKey;
  protected javax.swing.KeyStroke downKey;
  protected javax.swing.KeyStroke leftKey;
  protected javax.swing.KeyStroke rightKey;
  protected javax.swing.KeyStroke homeKey;
  protected javax.swing.KeyStroke endKey;
  protected javax.swing.KeyStroke dividerResizeToggleKey;
  protected java.awt.event.ActionListener keyboardUpLeftListener;
  protected java.awt.event.ActionListener keyboardDownRightListener;
  protected java.awt.event.ActionListener keyboardHomeListener;
  protected java.awt.event.ActionListener keyboardEndListener;
  protected java.awt.event.ActionListener keyboardResizeToggleListener;
  public BasicSplitPaneUI() {
  }
  protected java.awt.event.FocusListener createFocusListener() {
    return new FocusHandler();
  }
  public BasicSplitPaneDivider getDivider() {
    return divider;
  }
  public void resetToPreferredSizes(JSplitPane jc) {
    if (layoutManager != null) {
      layoutManager.resetToPreferredSizes();
    }
    splitPane.revalidate();
    splitPane.repaint();
  }
  public void setDividerLocation(JSplitPane jc, int location) {
    if (!ignoreDividerLocationChange) {
      dividerLocationIsSet = true;
      splitPane.revalidate();
      splitPane.repaint();
    }
  }
  public int getMinimumDividerLocation(JSplitPane jc) {
    int minLoc = 0;
    Component leftC = splitPane.getLeftComponent();
    if (leftC != null && leftC.isVisible()) {
      Insets insets = splitPane.getInsets();
      Dimension minSize = leftC.getMinimumSize();
      if (getOrientation() == JSplitPane.VERTICAL_SPLIT) {
        minLoc = minSize.height;
      } else {
        minLoc = minSize.width;
      }
      if (insets != null) {
        if (getOrientation() == JSplitPane.VERTICAL_SPLIT) {
          minLoc += insets.top;
        } else {
          minLoc += insets.left;
        }
      }
    }
    return minLoc;
  }
  public int getMaximumDividerLocation(JSplitPane jc) {
    Dimension splitPaneSize = splitPane.getSize();
    int maxLoc = 0;
    Component rightC = splitPane.getRightComponent();
    if (rightC != null) {
      Insets insets = splitPane.getInsets();
      Dimension minSize = rightC.getMinimumSize();
      if (getOrientation() == JSplitPane.VERTICAL_SPLIT) {
        maxLoc = splitPaneSize.height - minSize.height;
      } else {
        maxLoc = splitPaneSize.width - minSize.width;
      }
      maxLoc -= dividerSize;
      if (insets != null) {
        if (getOrientation() == JSplitPane.VERTICAL_SPLIT) {
          maxLoc -= insets.bottom;
        } else {
          maxLoc -= insets.right;
        }
      }
    }
    return Math.max(0, maxLoc);
  }
  public void finishedPaintingChildren(JSplitPane jc, Graphics g) {
    if (jc == splitPane && getLastDragLocation() != -1 && !isContinuousLayout() && !draggingHW) {
      Dimension size = splitPane.getSize();
      g.setColor(java.awt.Color.darkGray);
      if (getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
        g.fillRect(getLastDragLocation(), 0, dividerSize - 1, size.height - 1);
      } else {
        g.fillRect(0, lastDragLocation, size.width - 1, dividerSize - 1);
      }
    }
  }
  public void setContinuousLayout(boolean b) {
    continuousLayout = b;
  }
  public boolean isContinuousLayout() {
    return continuousLayout;
  }
  public int getDividerLocation(JSplitPane jc) {
    if (getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
      return divider.getLocation().x;
    }
    return divider.getLocation().y;
  }
  public JSplitPane getSplitPane() {
    return splitPane;
  }
  protected void installKeyboardActions() {
    shim.swing.KeyActions.install(splitPane, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
        "SplitPane.ancestorInputMap", shim.swing.KeyActions.splitPaneActions());
  }
  protected void uninstallKeyboardActions() {
    shim.swing.KeyActions.uninstall(splitPane, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
  }
  protected void installDefaults() {
    LookAndFeel.installBorder(splitPane, "SplitPane.border");
    LookAndFeel.installColors(splitPane, "SplitPane.background", "SplitPane.foreground");
    LookAndFeel.installProperty(splitPane, "opaque", Boolean.TRUE);
    if (divider == null) {
      divider = createDefaultDivider();
    }
    divider.setBasicSplitPaneUI(this);
    Border b = divider.getBorder();
    if (b == null || !(b instanceof UIResource)) {
      divider.setBorder(UIManager.getBorder("SplitPaneDivider.border"));
    }
    dividerSize = UIManager.getInt("SplitPane.dividerSize");
    divider.setDividerSize(splitPane.getDividerSize() > 0 ? splitPane.getDividerSize() : dividerSize);
    dividerSize = divider.getDividerSize();
    splitPane.add(divider, JSplitPane.DIVIDER);
    setOrientation(splitPane.getOrientation());
    setContinuousLayout(splitPane.isContinuousLayout());
    resetLayoutManager();
    if (nonContinuousLayoutDivider == null) {
      setNonContinuousLayoutDivider(createDefaultNonContinuousLayoutDivider(), true);
    } else {
      setNonContinuousLayoutDivider(nonContinuousLayoutDivider, true);
    }
  }
  protected void installListeners() {
    if ((propertyChangeListener = createPropertyChangeListener()) != null) {
      splitPane.addPropertyChangeListener(propertyChangeListener);
    }
    if ((focusListener = createFocusListener()) != null) {
      splitPane.addFocusListener(focusListener);
    }
  }
  protected void uninstallListeners() {
    if (propertyChangeListener != null) {
      splitPane.removePropertyChangeListener(propertyChangeListener);
      propertyChangeListener = null;
    }
    if (focusListener != null) {
      splitPane.removeFocusListener(focusListener);
      focusListener = null;
    }
  }
  protected void uninstallDefaults() {
    LookAndFeel.uninstallBorder(splitPane);
    if (divider.getBorder() instanceof UIResource) {
      divider.setBorder(null);
    }
    splitPane.remove(divider);
    divider.setBasicSplitPaneUI(null);
    layoutManager = null;
    divider = null;
    nonContinuousLayoutDivider = null;
    setNonContinuousLayoutDivider(null);
  }
  protected PropertyChangeListener createPropertyChangeListener() {
    return new PropertyHandler();
  }
  protected void startDragging() {
    Component leftC = splitPane.getLeftComponent();
    Component rightC = splitPane.getRightComponent();
    dividerLocationOnDrag = getDividerLocation(splitPane);
    beginDragDividerLocation = dividerLocationOnDrag;
    if (leftC != null) {
      leftC.setMinimumSize(leftC.getMinimumSize());
    }
    draggingHW = true;
    if (!isContinuousLayout()) {
      setLastDragLocation(getDividerLocation(splitPane));
    }
  }
  protected void dragDividerTo(int location) {
    if (isContinuousLayout()) {
      splitPane.setDividerLocation(location);
    } else {
      int old = getLastDragLocation();
      setLastDragLocation(location);
      splitPane.repaint();
    }
  }
  protected void finishDraggingTo(int location) {
    dragDividerTo(location);
    setLastDragLocation(-1);
    splitPane.setDividerLocation(location);
    draggingHW = false;
  }
  public void setLastDragLocation(int l) {
    lastDragLocation = l;
  }
  public BasicSplitPaneDivider createDefaultDivider() {
    return new BasicSplitPaneDivider(this);
  }
  protected void resetLayoutManager() {
    if (getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
      layoutManager = new BasicHorizontalLayoutManager(0);
    } else {
      layoutManager = new BasicVerticalLayoutManager();
    }
    splitPane.setLayout(layoutManager);
    layoutManager.updateComponents();
    splitPane.revalidate();
    splitPane.repaint();
  }
  protected Component createDefaultNonContinuousLayoutDivider() {
    return new Canvas() {
      public void paint(Graphics g) {
        if (!isContinuousLayout() && getLastDragLocation() != -1) {
          Dimension size = splitPane.getSize();
          g.setColor(java.awt.Color.darkGray);
          if (getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
            g.fillRect(0, 0, dividerSize - 1, size.height - 1);
          } else {
            g.fillRect(0, 0, size.width - 1, dividerSize - 1);
          }
        }
      }
    };
  }
  protected void setNonContinuousLayoutDivider(Component newDivider) {
    setNonContinuousLayoutDivider(newDivider, true);
  }
  protected void setNonContinuousLayoutDivider(Component newDivider, boolean rememberSizes) {
    nonContinuousLayoutDivider = newDivider;
  }
  public int getLastDragLocation() {
    return lastDragLocation;
  }
  protected java.awt.event.ActionListener createKeyboardUpLeftListener() {
    return new KeyboardUpLeftHandler();
  }
  protected java.awt.event.ActionListener createKeyboardDownRightListener() {
    return new KeyboardDownRightHandler();
  }
  protected java.awt.event.ActionListener createKeyboardHomeListener() {
    return new KeyboardHomeHandler();
  }
  protected java.awt.event.ActionListener createKeyboardEndListener() {
    return new KeyboardEndHandler();
  }
  protected java.awt.event.ActionListener createKeyboardResizeToggleListener() {
    return new KeyboardResizeToggleHandler();
  }
  public Component getNonContinuousLayoutDivider() {
    return nonContinuousLayoutDivider;
  }
  protected int getDividerBorderSize() {
    return 0;
  }
  public static javax.swing.plaf.ComponentUI createUI(JComponent x) {
    return new BasicSplitPaneUI();
  }
  public void setOrientation(int orientation) {
    this.orientationValue = orientation;
  }
  public Insets getInsets(JComponent jc) {
    return null;
  }
  public Dimension getMaximumSize(JComponent jc) {
    if (splitPane != null) {
      return layoutManager.maximumLayoutSize(splitPane);
    }
    return new Dimension(0, 0);
  }
  public void paint(Graphics g, JComponent jc) {
    if (!painted && splitPane.getDividerLocation() < 0) {
      ignoreDividerLocationChange = true;
      splitPane.setDividerLocation(getDividerLocation(splitPane));
      ignoreDividerLocationChange = false;
    }
    painted = true;
  }
  public Dimension getPreferredSize(JComponent jc) {
    if (splitPane != null) {
      return layoutManager.preferredLayoutSize(splitPane);
    }
    return new Dimension(0, 0);
  }
  public Dimension getMinimumSize(JComponent jc) {
    if (splitPane != null) {
      return layoutManager.minimumLayoutSize(splitPane);
    }
    return new Dimension(0, 0);
  }
  public int getOrientation() {
    return orientationValue;
  }
  public void installUI(JComponent c) {
    splitPane = (JSplitPane) c;
    dividerLocationIsSet = false;
    installDefaults();
    installListeners();
    installKeyboardActions();
    setLastDragLocation(-1);
  }
  public void uninstallUI(JComponent c) {
    uninstallKeyboardActions();
    uninstallListeners();
    uninstallDefaults();
    dividerLocationIsSet = false;
    splitPane = null;
  }

  public class BasicHorizontalLayoutManager implements java.awt.LayoutManager2 {
    protected int[] sizes;
    protected java.awt.Component[] components;
    BasicHorizontalLayoutManager() {
      this(0);
    }
    public void resetToPreferredSizes() {
      updateComponents();
      sizes[0] = components[0] != null && components[0].isVisible() ? getPreferredSizeOfComponent(components[0]) : 0;
      sizes[1] = components[1] != null ? getPreferredSizeOfComponent(components[1]) : 0;
      sizes[2] = components[2] != null && components[2].isVisible() ? getPreferredSizeOfComponent(components[2]) : 0;
    }
    protected void setSizes(int[] newSizes) {
      System.arraycopy(newSizes, 0, sizes, 0, 3);
    }
    protected int[] getSizes() {
      int[] retSizes = new int[3];
      System.arraycopy(sizes, 0, retSizes, 0, 3);
      return retSizes;
    }
    protected void updateComponents() {
      Component comp = splitPane.getLeftComponent();
      if (components[0] != comp) {
        components[0] = comp;
      }
      comp = splitPane.getRightComponent();
      if (components[2] != comp) {
        components[2] = comp;
      }
      components[1] = divider;
    }
    protected int getAvailableSize(Dimension containerSize, Insets insets) {
      if (insets == null) {
        return axis == 0 ? containerSize.width : containerSize.height;
      }
      if (axis == 0) {
        return containerSize.width - (insets.left + insets.right);
      }
      return containerSize.height - (insets.top + insets.bottom);
    }
    protected int getInitialLocation(Insets insets) {
      if (insets != null) {
        return axis == 0 ? insets.left : insets.top;
      }
      return 0;
    }
    protected void setComponentToSize(Component c, int size, int location, Insets insets, Dimension containerSize) {
      if (c == null) {
        return;
      }
      if (insets != null) {
        if (axis == 0) {
          c.setBounds(location, insets.top, size, containerSize.height - (insets.top + insets.bottom));
        } else {
          c.setBounds(insets.left, location, containerSize.width - (insets.left + insets.right), size);
        }
      } else if (axis == 0) {
        c.setBounds(location, 0, size, containerSize.height);
      } else {
        c.setBounds(0, location, containerSize.width, size);
      }
    }
    protected int getPreferredSizeOfComponent(Component c) {
      Dimension d = c.getPreferredSize();
      return axis == 0 ? d.width : d.height;
    }
    protected void resetSizeAt(int index) {
      sizes[index] = components[index] != null ? getPreferredSizeOfComponent(components[index]) : 0;
    }
    protected int getSizeOfComponent(Component c) {
      Dimension d = c.getSize();
      return axis == 0 ? d.width : d.height;
    }
    public void removeLayoutComponent(Component component) {
      for (int counter = 0; counter < 3; counter++) {
        if (components[counter] == component) {
          components[counter] = null;
          sizes[counter] = 0;
        }
      }
    }
    public void addLayoutComponent(String place, Component component) {
      boolean isValid = true;
      if (place != null) {
        if (place.equals(JSplitPane.DIVIDER)) {
          components[1] = component;
          sizes[1] = getSizeOfComponent(component);
        } else if (place.equals(JSplitPane.LEFT) || place.equals(JSplitPane.TOP)) {
          components[0] = component;
          sizes[0] = 0;
        } else if (place.equals(JSplitPane.RIGHT) || place.equals(JSplitPane.BOTTOM)) {
          components[2] = component;
          sizes[2] = 0;
        } else if (!place.equals("nonContinuousDivider")) {
          isValid = false;
        }
      } else {
        isValid = false;
      }
      if (!isValid) {
        throw new IllegalArgumentException("cannot add to layout: unknown constraint: " + place);
      }
    }
    public void addLayoutComponent(Component comp, Object constraints) {
      addLayoutComponent((String) constraints, comp);
    }
    public void layoutContainer(Container container) {
      Dimension containerSize = container.getSize();
      if (containerSize.height <= 0 || containerSize.width <= 0) {
        resetToPreferredSizes();
        return;
      }
      updateComponents();
      Insets insets = splitPane.getInsets();
      int available = getAvailableSize(containerSize, insets);
      int dividerSz = components[1] != null ? getPreferredSizeOfComponent(components[1]) : 0;
      int total = axis == 0 ? containerSize.width : containerSize.height;
      int insetStart = axis == 0 ? insets.left : insets.top;
      int location = splitPane.getDividerLocation();
      int leftSize;
      if (location < 0) {
        leftSize = components[0] != null && components[0].isVisible() ? getPreferredSizeOfComponent(components[0]) : 0;
        if (lastSize > 0 && lastSize != total && sizes[0] > 0 && splitPane.getResizeWeight() > 0) {
          leftSize = (int) (sizes[0] + splitPane.getResizeWeight() * (total - lastSize));
        }
      } else {
        leftSize = location - insetStart;
        if (lastSize > 0 && lastSize != total) {
          leftSize += (int) (splitPane.getResizeWeight() * (total - lastSize));
        }
      }
      int minLeft = components[0] != null && components[0].isVisible() ? (axis == 0
          ? components[0].getMinimumSize().width : components[0].getMinimumSize().height) : 0;
      int minRight = components[2] != null && components[2].isVisible() ? (axis == 0
          ? components[2].getMinimumSize().width : components[2].getMinimumSize().height) : 0;
      if (components[0] == null || !components[0].isVisible()) {
        leftSize = 0;
      }
      leftSize = Math.max(leftSize, minLeft);
      leftSize = Math.min(leftSize, Math.max(0, available - dividerSz - minRight));
      if (components[2] == null || !components[2].isVisible()) {
        leftSize = available - dividerSz;
      }
      sizes[0] = leftSize;
      sizes[1] = dividerSz;
      sizes[2] = Math.max(0, available - leftSize - dividerSz);
      lastSize = total;
      int pos = getInitialLocation(insets);
      for (int i = 0; i < 3; i++) {
        if (components[i] != null && components[i].isVisible() || i == 1) {
          setComponentToSize(components[i], sizes[i], pos, insets, containerSize);
          pos += sizes[i];
        }
      }
    }
    public void invalidateLayout(Container c) {
    }
    public Dimension preferredLayoutSize(Container container) {
      int prefPrimary = 0;
      int prefSecondary = 0;
      Insets insets = splitPane.getInsets();
      for (int counter = 0; counter < 3; counter++) {
        if (components[counter] != null) {
          Dimension prefSize = components[counter].getPreferredSize();
          int primary = axis == 0 ? prefSize.width : prefSize.height;
          int secondary = axis == 0 ? prefSize.height : prefSize.width;
          prefPrimary += primary;
          prefSecondary = Math.max(secondary, prefSecondary);
        }
      }
      if (insets != null) {
        prefPrimary += axis == 0 ? insets.left + insets.right : insets.top + insets.bottom;
        prefSecondary += axis == 0 ? insets.top + insets.bottom : insets.left + insets.right;
      }
      return axis == 0 ? new Dimension(prefPrimary, prefSecondary) : new Dimension(prefSecondary, prefPrimary);
    }
    public Dimension minimumLayoutSize(Container container) {
      int minPrimary = 0;
      int minSecondary = 0;
      Insets insets = splitPane.getInsets();
      for (int counter = 0; counter < 3; counter++) {
        if (components[counter] != null) {
          Dimension minSize = components[counter].getMinimumSize();
          int primary = axis == 0 ? minSize.width : minSize.height;
          int secondary = axis == 0 ? minSize.height : minSize.width;
          minPrimary += primary;
          minSecondary = Math.max(secondary, minSecondary);
        }
      }
      if (insets != null) {
        minPrimary += axis == 0 ? insets.left + insets.right : insets.top + insets.bottom;
        minSecondary += axis == 0 ? insets.top + insets.bottom : insets.left + insets.right;
      }
      return axis == 0 ? new Dimension(minPrimary, minSecondary) : new Dimension(minSecondary, minPrimary);
    }
    public Dimension maximumLayoutSize(Container target) {
      return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }
    public float getLayoutAlignmentX(Container target) {
      return 0.0f;
    }
    public float getLayoutAlignmentY(Container target) {
      return 0.0f;
    }
    private int axis;
    private int lastSize = -1;

    BasicHorizontalLayoutManager(int axis) {
      this.axis = axis;
      components = new Component[3];
      sizes = new int[3];
    }

    protected int getMinimumSizeOfComponent(Component c) {
      Dimension d = c.getMinimumSize();
      return axis == 0 ? d.width : d.height;
    }
  }

  public class KeyboardUpLeftHandler implements java.awt.event.ActionListener {
    public KeyboardUpLeftHandler() {
    }
    public void actionPerformed(java.awt.event.ActionEvent ev) {
      if (dividerKeyboardResize) {
        splitPane.setDividerLocation(Math.max(0, getDividerLocation(splitPane) - KEYBOARD_DIVIDER_MOVE_OFFSET));
      }
    }
  }

  public class KeyboardDownRightHandler implements java.awt.event.ActionListener {
    public KeyboardDownRightHandler() {
    }
    public void actionPerformed(java.awt.event.ActionEvent ev) {
      if (dividerKeyboardResize) {
        splitPane.setDividerLocation(getDividerLocation(splitPane) + KEYBOARD_DIVIDER_MOVE_OFFSET);
      }
    }
  }

  public class KeyboardHomeHandler implements java.awt.event.ActionListener {
    public KeyboardHomeHandler() {
    }
    public void actionPerformed(java.awt.event.ActionEvent ev) {
      if (dividerKeyboardResize) {
        splitPane.setDividerLocation(0);
      }
    }
  }

  public class KeyboardEndHandler implements java.awt.event.ActionListener {
    public KeyboardEndHandler() {
    }
    public void actionPerformed(java.awt.event.ActionEvent ev) {
      if (dividerKeyboardResize) {
        Insets insets = splitPane.getInsets();
        int bottomI = (insets != null) ? insets.bottom : 0;
        int rightI = (insets != null) ? insets.right : 0;
        if (orientationValue == JSplitPane.VERTICAL_SPLIT) {
          splitPane.setDividerLocation(splitPane.getHeight() - bottomI);
        } else {
          splitPane.setDividerLocation(splitPane.getWidth() - rightI);
        }
      }
    }
  }

  public class KeyboardResizeToggleHandler implements java.awt.event.ActionListener {
    public KeyboardResizeToggleHandler() {
    }
    public void actionPerformed(java.awt.event.ActionEvent ev) {
      if (!dividerKeyboardResize) {
        splitPane.requestFocus();
      }
    }
  }

  public class BasicVerticalLayoutManager extends javax.swing.plaf.basic.BasicSplitPaneUI.BasicHorizontalLayoutManager {
    public BasicVerticalLayoutManager() {
      super(1);
    }
  }

  public class FocusHandler extends java.awt.event.FocusAdapter {
    public FocusHandler() {
    }
    public void focusGained(java.awt.event.FocusEvent ev) {
      dividerKeyboardResize = true;
      splitPane.repaint();
    }
    public void focusLost(java.awt.event.FocusEvent ev) {
      dividerKeyboardResize = false;
      splitPane.repaint();
    }
  }

  public class PropertyHandler implements java.beans.PropertyChangeListener {
    public PropertyHandler() {
    }
    public void propertyChange(PropertyChangeEvent e) {
      if (e.getSource() == splitPane) {
        String changeName = e.getPropertyName();
        if ("orientation".equals(changeName)) {
          orientationValue = splitPane.getOrientation();
          resetLayoutManager();
        } else if ("continuousLayout".equals(changeName)) {
          setContinuousLayout(splitPane.isContinuousLayout());
          if (!isContinuousLayout()) {
            if (nonContinuousLayoutDivider == null) {
              setNonContinuousLayoutDivider(createDefaultNonContinuousLayoutDivider(), true);
            }
          }
        } else if ("dividerSize".equals(changeName)) {
          divider.setDividerSize(splitPane.getDividerSize());
          dividerSize = divider.getDividerSize();
          splitPane.revalidate();
          splitPane.repaint();
        } else if ("dividerLocation".equals(changeName)) {
          setDividerLocation(splitPane, splitPane.getDividerLocation());
        } else if ("leftComponent".equals(changeName) || "rightComponent".equals(changeName) || "topComponent".equals(changeName)
            || "bottomComponent".equals(changeName)) {
          layoutManager.updateComponents();
          splitPane.revalidate();
          splitPane.repaint();
        }
      }
    }
  }
  private boolean continuousLayout = true;
  private boolean dividerKeyboardResize;
  private int lastDragLocation;
  private int orientationValue;
  private boolean dividerLocationIsSet;
  private int lastSplitPaneSize;
  private boolean ignoreDividerLocationChange;
  private int dividerLocationOnDrag = -1;
  private boolean painted;
}
