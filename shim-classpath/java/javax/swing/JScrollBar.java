/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.Adjustable;
import java.awt.Dimension;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;
import javax.accessibility.AccessibleValue;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ScrollBarUI;

public class JScrollBar extends JComponent implements Adjustable, Accessible {
  private final CopyOnWriteArrayList<AdjustmentListener> adjustmentListeners = new CopyOnWriteArrayList<>();
  private final ChangeListener fwdAdjustmentEvents = new ModelListener();
  private AccessibleContext accessibleContext;

  protected BoundedRangeModel model;
  protected int orientation;
  protected int unitIncrement;
  protected int blockIncrement;

  private void checkOrientation(int orientation) {
    switch (orientation) {
      case VERTICAL:
      case HORIZONTAL:
        break;
      default:
        throw new IllegalArgumentException("orientation must be one of: VERTICAL, HORIZONTAL");
    }
  }

  public JScrollBar(int orientation, int value, int extent, int min, int max) {
    checkOrientation(orientation);
    this.unitIncrement = 1;
    this.blockIncrement = (extent == 0) ? 1 : extent;
    this.orientation = orientation;
    this.model = new DefaultBoundedRangeModel(value, extent, min, max);
    this.model.addChangeListener(fwdAdjustmentEvents);
    setRequestFocusEnabled(false);
    updateUI();
  }

  public JScrollBar(int orientation) { this(orientation, 0, 10, 0, 100); }

  public JScrollBar() { this(VERTICAL); }

  public void setUI(ScrollBarUI ui) { super.setUI(ui); }

  public ScrollBarUI getUI() { return (ScrollBarUI) ui; }

  public void updateUI() { setUI((javax.swing.plaf.ComponentUI) UIManager.getUI(this)); }

  public String getUIClassID() { return "ScrollBarUI"; }

  public int getOrientation() { return orientation; }

  public void setOrientation(int orientation) {
    checkOrientation(orientation);
    int oldValue = this.orientation;
    this.orientation = orientation;
    firePropertyChange("orientation", oldValue, orientation);
    if ((oldValue != orientation) && (accessibleContext != null)) {
      accessibleContext.firePropertyChange(AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
          ((oldValue == VERTICAL) ? AccessibleState.VERTICAL : AccessibleState.HORIZONTAL),
          ((orientation == VERTICAL) ? AccessibleState.VERTICAL : AccessibleState.HORIZONTAL));
    }
    if (orientation != oldValue) {
      revalidate();
    }
  }

  public BoundedRangeModel getModel() { return model; }

  public void setModel(BoundedRangeModel newModel) {
    Integer oldValue = null;
    BoundedRangeModel oldModel = model;
    if (model != null) {
      model.removeChangeListener(fwdAdjustmentEvents);
      oldValue = Integer.valueOf(model.getValue());
    }
    model = newModel;
    if (model != null) {
      model.addChangeListener(fwdAdjustmentEvents);
    }
    firePropertyChange("model", oldModel, model);
    if (accessibleContext != null) {
      accessibleContext.firePropertyChange(AccessibleContext.ACCESSIBLE_VALUE_PROPERTY, oldValue,
          Integer.valueOf(model.getValue()));
    }
  }

  public int getUnitIncrement(int direction) { return unitIncrement; }

  public void setUnitIncrement(int unitIncrement) {
    int oldValue = this.unitIncrement;
    this.unitIncrement = unitIncrement;
    firePropertyChange("unitIncrement", oldValue, unitIncrement);
  }

  public int getBlockIncrement(int direction) { return blockIncrement; }

  public void setBlockIncrement(int blockIncrement) {
    int oldValue = this.blockIncrement;
    this.blockIncrement = blockIncrement;
    firePropertyChange("blockIncrement", oldValue, blockIncrement);
  }

  public int getUnitIncrement() { return unitIncrement; }

  public int getBlockIncrement() { return blockIncrement; }

  public int getValue() { return getModel().getValue(); }

  public void setValue(int value) {
    BoundedRangeModel m = getModel();
    int oldValue = m.getValue();
    m.setValue(value);
    if (accessibleContext != null) {
      accessibleContext.firePropertyChange(AccessibleContext.ACCESSIBLE_VALUE_PROPERTY, Integer.valueOf(oldValue),
          Integer.valueOf(m.getValue()));
    }
  }

  public int getVisibleAmount() { return getModel().getExtent(); }

  public void setVisibleAmount(int extent) { getModel().setExtent(extent); }

  public int getMinimum() { return getModel().getMinimum(); }

  public void setMinimum(int minimum) { getModel().setMinimum(minimum); }

  public int getMaximum() { return getModel().getMaximum(); }

  public void setMaximum(int maximum) { getModel().setMaximum(maximum); }

  public boolean getValueIsAdjusting() { return getModel().getValueIsAdjusting(); }

  public void setValueIsAdjusting(boolean b) {
    BoundedRangeModel m = getModel();
    boolean oldValue = m.getValueIsAdjusting();
    m.setValueIsAdjusting(b);
    if ((oldValue != b) && (accessibleContext != null)) {
      accessibleContext.firePropertyChange(AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
          ((oldValue) ? AccessibleState.BUSY : null), ((b) ? AccessibleState.BUSY : null));
    }
  }

  public void setValues(int newValue, int newExtent, int newMin, int newMax) {
    BoundedRangeModel m = getModel();
    int oldValue = m.getValue();
    m.setRangeProperties(newValue, newExtent, newMin, newMax, m.getValueIsAdjusting());
    if (accessibleContext != null) {
      accessibleContext.firePropertyChange(AccessibleContext.ACCESSIBLE_VALUE_PROPERTY, Integer.valueOf(oldValue),
          Integer.valueOf(m.getValue()));
    }
  }

  public void addAdjustmentListener(AdjustmentListener l) { adjustmentListeners.add(l); }

  public void removeAdjustmentListener(AdjustmentListener l) { adjustmentListeners.remove(l); }

  public AdjustmentListener[] getAdjustmentListeners() {
    return adjustmentListeners.toArray(new AdjustmentListener[0]);
  }

  protected void fireAdjustmentValueChanged(int id, int type, int value) {
    fireAdjustmentValueChanged(id, type, value, getValueIsAdjusting());
  }

  private void fireAdjustmentValueChanged(int id, int type, int value, boolean isAdjusting) {
    AdjustmentEvent e = null;
    for (int i = adjustmentListeners.size() - 1; i >= 0; i--) {
      if (e == null) {
        e = new AdjustmentEvent(this, id, type, value, isAdjusting);
      }
      adjustmentListeners.get(i).adjustmentValueChanged(e);
    }
  }

  private class ModelListener implements ChangeListener, java.io.Serializable {
    public void stateChanged(ChangeEvent e) {
      Object obj = e.getSource();
      if (obj instanceof BoundedRangeModel) {
        BoundedRangeModel m = (BoundedRangeModel) obj;
        int id = AdjustmentEvent.ADJUSTMENT_VALUE_CHANGED;
        int type = AdjustmentEvent.TRACK;
        fireAdjustmentValueChanged(id, type, m.getValue(), m.getValueIsAdjusting());
      }
    }
  }

  public Dimension getPreferredSize() {
    if (isPreferredSizeSet()) {
      return super.getPreferredSize();
    }
    boolean embedded = getParent() instanceof JScrollPane;
    int thickness = embedded ? 15 : 17;
    int length = embedded ? 55 : 61;
    return orientation == VERTICAL ? new Dimension(thickness, length) : new Dimension(length, thickness);
  }

  public Dimension getMinimumSize() {
    Dimension pref = getPreferredSize();
    if (orientation == VERTICAL) {
      return new Dimension(pref.width, 5);
    } else {
      return new Dimension(5, pref.height);
    }
  }

  public Dimension getMaximumSize() {
    Dimension pref = getPreferredSize();
    if (getOrientation() == VERTICAL) {
      return new Dimension(pref.width, Short.MAX_VALUE);
    } else {
      return new Dimension(Short.MAX_VALUE, pref.height);
    }
  }

  public void setEnabled(boolean x) {
    super.setEnabled(x);
    java.awt.Component[] children = getComponents();
    for (java.awt.Component child : children) {
      child.setEnabled(x);
    }
  }

  protected String paramString() {
    String orientationString = (orientation == HORIZONTAL ? "HORIZONTAL" : "VERTICAL");
    return super.paramString() + ",blockIncrement=" + blockIncrement + ",orientation=" + orientationString
        + ",unitIncrement=" + unitIncrement;
  }

  public AccessibleContext getAccessibleContext() {
    if (accessibleContext == null) {
      accessibleContext = new AccessibleJScrollBar();
    }
    return accessibleContext;
  }

  protected class AccessibleJScrollBar extends AccessibleJComponent implements AccessibleValue {
    public AccessibleStateSet getAccessibleStateSet() {
      AccessibleStateSet states = super.getAccessibleStateSet();
      if (getValueIsAdjusting()) {
        states.add(AccessibleState.BUSY);
      }
      if (getOrientation() == VERTICAL) {
        states.add(AccessibleState.VERTICAL);
      } else {
        states.add(AccessibleState.HORIZONTAL);
      }
      return states;
    }

    public AccessibleRole getAccessibleRole() { return AccessibleRole.SCROLL_BAR; }

    public AccessibleValue getAccessibleValue() { return this; }

    public Number getCurrentAccessibleValue() { return Integer.valueOf(getValue()); }

    public boolean setCurrentAccessibleValue(Number n) {
      if (n == null) {
        return false;
      }
      setValue(n.intValue());
      return true;
    }

    public Number getMinimumAccessibleValue() { return Integer.valueOf(getMinimum()); }

    public Number getMaximumAccessibleValue() { return Integer.valueOf(model.getMaximum() - model.getExtent()); }
  }
}
