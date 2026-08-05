/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Graphics;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleStateSet;
import javax.swing.plaf.SplitPaneUI;

public class JSplitPane extends JComponent implements Accessible {
  public static final int VERTICAL_SPLIT = 0;
  public static final int HORIZONTAL_SPLIT = 1;
  public static final String LEFT = "left";
  public static final String RIGHT = "right";
  public static final String TOP = "top";
  public static final String BOTTOM = "bottom";
  public static final String DIVIDER = "divider";
  public static final String ORIENTATION_PROPERTY = "orientation";
  public static final String CONTINUOUS_LAYOUT_PROPERTY = "continuousLayout";
  public static final String DIVIDER_SIZE_PROPERTY = "dividerSize";
  public static final String ONE_TOUCH_EXPANDABLE_PROPERTY = "oneTouchExpandable";
  public static final String LAST_DIVIDER_LOCATION_PROPERTY = "lastDividerLocation";
  public static final String DIVIDER_LOCATION_PROPERTY = "dividerLocation";
  public static final String RESIZE_WEIGHT_PROPERTY = "resizeWeight";

  protected int orientation;
  protected boolean continuousLayout;
  protected Component leftComponent;
  protected Component rightComponent;
  protected int dividerSize;
  private boolean dividerSizeSet = false;
  protected boolean oneTouchExpandable;
  private boolean oneTouchExpandableSet;
  protected int lastDividerLocation;
  private double resizeWeight;
  private int dividerLocation = -1;

  public JSplitPane() {
    this(JSplitPane.HORIZONTAL_SPLIT, false, new JButton(UIManager.getString("SplitPane.leftButtonText")),
        new JButton(UIManager.getString("SplitPane.rightButtonText")));
  }

  public JSplitPane(int newOrientation) { this(newOrientation, false); }

  public JSplitPane(int newOrientation, boolean newContinuousLayout) {
    this(newOrientation, newContinuousLayout, null, null);
  }

  public JSplitPane(int newOrientation, Component newLeftComponent, Component newRightComponent) {
    this(newOrientation, false, newLeftComponent, newRightComponent);
  }

  public JSplitPane(int newOrientation, boolean newContinuousLayout, Component newLeftComponent,
      Component newRightComponent) {
    super();
    dividerSize = 0;
    setLayout(null);
    continuousLayout = newContinuousLayout;
    if (newOrientation != HORIZONTAL_SPLIT && newOrientation != VERTICAL_SPLIT) {
      throw new IllegalArgumentException("cannot create JSplitPane, orientation must be one of "
          + "JSplitPane.HORIZONTAL_SPLIT or JSplitPane.VERTICAL_SPLIT");
    }
    orientation = newOrientation;
    if (newLeftComponent != null) {
      setLeftComponent(newLeftComponent);
    }
    if (newRightComponent != null) {
      setRightComponent(newRightComponent);
    }
    updateUI();
  }

  public void setComponentOrientation(ComponentOrientation o) { super.setComponentOrientation(o); }

  public void setUI(SplitPaneUI ui) {
    if ((SplitPaneUI) this.ui != ui) {
      super.setUI(ui);
      revalidate();
    }
  }

  public SplitPaneUI getUI() { return (SplitPaneUI) ui; }

  public void updateUI() {
    setUI((SplitPaneUI) UIManager.getUI(this));
    if (!dividerSizeSet) {
      dividerSize = UIManager.getInt("SplitPane.dividerSize");
    }
    revalidate();
  }

  public String getUIClassID() { return "SplitPaneUI"; }

  public void setDividerSize(int newSize) {
    int oldSize = dividerSize;
    dividerSizeSet = true;
    if (oldSize != newSize) {
      dividerSize = newSize;
      firePropertyChange(DIVIDER_SIZE_PROPERTY, oldSize, newSize);
    }
  }

  public int getDividerSize() { return dividerSize; }

  public void setLeftComponent(Component comp) {
    if (comp == null) {
      if (leftComponent != null) {
        remove(leftComponent);
        leftComponent = null;
      }
    } else {
      add(comp, JSplitPane.LEFT);
    }
  }

  public Component getLeftComponent() { return leftComponent; }

  public void setTopComponent(Component comp) { setLeftComponent(comp); }

  public Component getTopComponent() { return leftComponent; }

  public void setRightComponent(Component comp) {
    if (comp == null) {
      if (rightComponent != null) {
        remove(rightComponent);
        rightComponent = null;
      }
    } else {
      add(comp, JSplitPane.RIGHT);
    }
  }

  public Component getRightComponent() { return rightComponent; }

  public void setBottomComponent(Component comp) { setRightComponent(comp); }

  public Component getBottomComponent() { return rightComponent; }

  public void setOneTouchExpandable(boolean newValue) {
    boolean oldValue = oneTouchExpandable;
    oneTouchExpandable = newValue;
    oneTouchExpandableSet = true;
    firePropertyChange(ONE_TOUCH_EXPANDABLE_PROPERTY, oldValue, newValue);
    repaint();
  }

  public boolean isOneTouchExpandable() { return oneTouchExpandable; }

  public void setLastDividerLocation(int newLastLocation) {
    int oldLocation = lastDividerLocation;
    lastDividerLocation = newLastLocation;
    firePropertyChange(LAST_DIVIDER_LOCATION_PROPERTY, oldLocation, newLastLocation);
  }

  public int getLastDividerLocation() { return lastDividerLocation; }

  public void setOrientation(int orientation) {
    if ((orientation != VERTICAL_SPLIT) && (orientation != HORIZONTAL_SPLIT)) {
      throw new IllegalArgumentException("JSplitPane: orientation must be one of "
          + "JSplitPane.VERTICAL_SPLIT or JSplitPane.HORIZONTAL_SPLIT");
    }
    int oldOrientation = this.orientation;
    this.orientation = orientation;
    firePropertyChange(ORIENTATION_PROPERTY, oldOrientation, orientation);
  }

  public int getOrientation() { return orientation; }

  public void setContinuousLayout(boolean newContinuousLayout) {
    boolean oldCD = continuousLayout;
    continuousLayout = newContinuousLayout;
    firePropertyChange(CONTINUOUS_LAYOUT_PROPERTY, oldCD, newContinuousLayout);
  }

  public boolean isContinuousLayout() { return continuousLayout; }

  public void setResizeWeight(double value) {
    if (value < 0 || value > 1) {
      throw new IllegalArgumentException("JSplitPane weight must be between 0 and 1");
    }
    double oldWeight = resizeWeight;
    resizeWeight = value;
    firePropertyChange(RESIZE_WEIGHT_PROPERTY, oldWeight, value);
  }

  public double getResizeWeight() { return resizeWeight; }

  public void resetToPreferredSizes() {
    SplitPaneUI ui = getUI();
    if (ui != null) {
      ui.resetToPreferredSizes(this);
    }
  }

  public void setDividerLocation(double proportionalLocation) {
    if (proportionalLocation < 0.0 || proportionalLocation > 1.0) {
      throw new IllegalArgumentException("proportional location must be between 0.0 and 1.0.");
    }
    if (getOrientation() == VERTICAL_SPLIT) {
      setDividerLocation((int) ((double) (getHeight() - getDividerSize()) * proportionalLocation));
    } else {
      setDividerLocation((int) ((double) (getWidth() - getDividerSize()) * proportionalLocation));
    }
  }

  public void setDividerLocation(int location) {
    int oldValue = dividerLocation;
    dividerLocation = location;
    SplitPaneUI ui = getUI();
    if (ui != null) {
      ui.setDividerLocation(this, location);
    }
    firePropertyChange(DIVIDER_LOCATION_PROPERTY, oldValue, location);
    setLastDividerLocation(oldValue);
  }

  public int getDividerLocation() { return dividerLocation; }

  public int getMinimumDividerLocation() {
    SplitPaneUI ui = getUI();
    if (ui != null) {
      return ui.getMinimumDividerLocation(this);
    }
    return -1;
  }

  public int getMaximumDividerLocation() {
    SplitPaneUI ui = getUI();
    if (ui != null) {
      return ui.getMaximumDividerLocation(this);
    }
    return -1;
  }

  public void remove(Component component) {
    if (component == leftComponent) {
      leftComponent = null;
    } else if (component == rightComponent) {
      rightComponent = null;
    }
    super.remove(component);
    revalidate();
    repaint();
  }

  public void remove(int index) {
    Component comp = getComponent(index);
    if (comp == leftComponent) {
      leftComponent = null;
    } else if (comp == rightComponent) {
      rightComponent = null;
    }
    super.remove(index);
    revalidate();
    repaint();
  }

  public void removeAll() {
    leftComponent = rightComponent = null;
    super.removeAll();
    revalidate();
    repaint();
  }

  public boolean isValidateRoot() { return true; }

  protected void addImpl(Component comp, Object constraints, int index) {
    if ((constraints != null) && !(constraints instanceof String)) {
      throw new IllegalArgumentException("cannot add to layout: constraint must be a string (or null)");
    }
    if (constraints == null) {
      if (getLeftComponent() == null) {
        constraints = JSplitPane.LEFT;
      } else if (getRightComponent() == null) {
        constraints = JSplitPane.RIGHT;
      }
    }
    if (constraints != null && (constraints.equals(JSplitPane.LEFT) || constraints.equals(JSplitPane.TOP))) {
      Component toRemove = getLeftComponent();
      if (toRemove != null) {
        remove(toRemove);
      }
      leftComponent = comp;
      index = -1;
    } else if (constraints != null && (constraints.equals(JSplitPane.RIGHT) || constraints.equals(JSplitPane.BOTTOM))) {
      Component toRemove = getRightComponent();
      if (toRemove != null) {
        remove(toRemove);
      }
      rightComponent = comp;
      index = -1;
    } else if (constraints != null && constraints.equals(JSplitPane.DIVIDER)) {
      index = -1;
    }
    super.addImpl(comp, constraints, index);
    revalidate();
    repaint();
  }

  protected void paintChildren(Graphics g) { super.paintChildren(g); }

  protected String paramString() {
    String orientationString = (orientation == HORIZONTAL_SPLIT ? "HORIZONTAL_SPLIT" : "VERTICAL_SPLIT");
    String continuousLayoutString = (continuousLayout ? "true" : "false");
    String oneTouchExpandableString = (oneTouchExpandable ? "true" : "false");
    return super.paramString() + ",continuousLayout=" + continuousLayoutString + ",dividerSize=" + dividerSize
        + ",lastDividerLocation=" + lastDividerLocation + ",oneTouchExpandable=" + oneTouchExpandableString
        + ",orientation=" + orientationString;
  }

  public AccessibleContext getAccessibleContext() {
    if (accessibleContext == null) {
      accessibleContext = new AccessibleJSplitPane();
    }
    return accessibleContext;
  }

  protected class AccessibleJSplitPane extends AccessibleJComponent implements javax.accessibility.AccessibleValue {
    public AccessibleStateSet getAccessibleStateSet() {
      AccessibleStateSet states = super.getAccessibleStateSet();
      if (getOrientation() == VERTICAL_SPLIT) {
        states.add(javax.accessibility.AccessibleState.VERTICAL);
      } else {
        states.add(javax.accessibility.AccessibleState.HORIZONTAL);
      }
      return states;
    }

    public javax.accessibility.AccessibleValue getAccessibleValue() { return this; }

    public Number getCurrentAccessibleValue() { return Integer.valueOf(getDividerLocation()); }

    public boolean setCurrentAccessibleValue(Number n) {
      if (n == null) {
        return false;
      }
      setDividerLocation(n.intValue());
      return true;
    }

    public Number getMinimumAccessibleValue() { return Integer.valueOf(getUI().getMinimumDividerLocation(JSplitPane.this)); }

    public Number getMaximumAccessibleValue() { return Integer.valueOf(getUI().getMaximumDividerLocation(JSplitPane.this)); }

    public AccessibleRole getAccessibleRole() { return AccessibleRole.SPLIT_PANE; }
  }
}
