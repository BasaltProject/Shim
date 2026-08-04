/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.Graphics;
import java.text.NumberFormat;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;
import javax.accessibility.AccessibleValue;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ProgressBarUI;

public class JProgressBar extends JComponent implements SwingConstants, Accessible {
  protected int orientation;
  protected boolean paintBorder;
  protected BoundedRangeModel model;
  protected String progressString;
  protected boolean paintString;
  protected transient ChangeEvent changeEvent = null;
  protected ChangeListener changeListener = null;

  private final CopyOnWriteArrayList<ChangeListener> changeListeners = new CopyOnWriteArrayList<>();
  private transient boolean indeterminate;
  private AccessibleContext accessibleContext;

  public JProgressBar() { this(HORIZONTAL); }

  public JProgressBar(int orient) { this(orient, 0, 100); }

  public JProgressBar(int min, int max) { this(HORIZONTAL, min, max); }

  public JProgressBar(int orient, int min, int max) {
    setModel(new DefaultBoundedRangeModel(min, 0, min, max));
    updateUI();
    setOrientation(orient);
    setBorderPainted(true);
    setStringPainted(false);
    setString(null);
    setIndeterminate(false);
  }

  public JProgressBar(BoundedRangeModel newModel) {
    setModel(newModel);
    updateUI();
    setOrientation(HORIZONTAL);
    setBorderPainted(true);
    setStringPainted(false);
    setString(null);
    setIndeterminate(false);
  }

  public int getOrientation() { return orientation; }

  public void setOrientation(int newOrientation) {
    if (orientation != newOrientation) {
      switch (newOrientation) {
        case VERTICAL:
        case HORIZONTAL:
          int oldOrientation = orientation;
          orientation = newOrientation;
          firePropertyChange("orientation", oldOrientation, newOrientation);
          if (accessibleContext != null) {
            accessibleContext.firePropertyChange(AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                ((oldOrientation == VERTICAL) ? AccessibleState.VERTICAL : AccessibleState.HORIZONTAL),
                ((orientation == VERTICAL) ? AccessibleState.VERTICAL : AccessibleState.HORIZONTAL));
          }
          break;
        default:
          throw new IllegalArgumentException(newOrientation + " is not a legal orientation");
      }
      revalidate();
    }
  }

  public boolean isStringPainted() { return paintString; }

  public void setStringPainted(boolean b) {
    boolean oldValue = paintString;
    paintString = b;
    firePropertyChange("stringPainted", oldValue, paintString);
    if (paintString != oldValue) {
      revalidate();
      repaint();
    }
  }

  public String getString() {
    if (progressString != null) {
      return progressString;
    } else {
      NumberFormat format = NumberFormat.getPercentInstance();
      return format.format(Double.valueOf(getPercentComplete()));
    }
  }

  public void setString(String s) {
    String oldValue = progressString;
    progressString = s;
    firePropertyChange("string", oldValue, progressString);
    if (progressString == null || oldValue == null || !progressString.equals(oldValue)) {
      repaint();
    }
  }

  public double getPercentComplete() {
    long span = model.getMaximum() - model.getMinimum();
    double currentValue = model.getValue();
    double pc = (currentValue - model.getMinimum()) / span;
    return pc;
  }

  public boolean isBorderPainted() { return paintBorder; }

  public void setBorderPainted(boolean b) {
    boolean oldValue = paintBorder;
    paintBorder = b;
    firePropertyChange("borderPainted", oldValue, paintBorder);
    if (paintBorder != oldValue) {
      repaint();
    }
  }

  protected void paintBorder(Graphics g) {
    if (isBorderPainted()) {
      super.paintBorder(g);
    }
  }

  public ProgressBarUI getUI() { return (ProgressBarUI) ui; }

  public void setUI(ProgressBarUI ui) { super.setUI(ui); }

  public void updateUI() { setUI((javax.swing.plaf.ComponentUI) UIManager.getUI(this)); }

  public String getUIClassID() { return "ProgressBarUI"; }

  private class ModelListener implements ChangeListener, java.io.Serializable {
    public void stateChanged(ChangeEvent e) { fireStateChanged(); }
  }

  protected ChangeListener createChangeListener() { return new ModelListener(); }

  public void addChangeListener(ChangeListener l) { changeListeners.add(l); }

  public void removeChangeListener(ChangeListener l) { changeListeners.remove(l); }

  public ChangeListener[] getChangeListeners() { return changeListeners.toArray(new ChangeListener[0]); }

  protected void fireStateChanged() {
    for (int i = changeListeners.size() - 1; i >= 0; i--) {
      if (changeEvent == null) {
        changeEvent = new ChangeEvent(this);
      }
      changeListeners.get(i).stateChanged(changeEvent);
    }
  }

  public BoundedRangeModel getModel() { return model; }

  public void setModel(BoundedRangeModel newModel) {
    BoundedRangeModel oldModel = getModel();
    if (newModel != oldModel) {
      if (oldModel != null) {
        oldModel.removeChangeListener(changeListener);
        changeListener = null;
      }
      model = newModel;
      if (newModel != null) {
        changeListener = createChangeListener();
        newModel.addChangeListener(changeListener);
      }
      if (accessibleContext != null) {
        accessibleContext.firePropertyChange(AccessibleContext.ACCESSIBLE_VALUE_PROPERTY,
            (oldModel == null ? null : Integer.valueOf(oldModel.getValue())),
            (newModel == null ? null : Integer.valueOf(newModel.getValue())));
      }
      if (model != null) {
        model.setExtent(0);
      }
      repaint();
    }
  }

  public int getValue() { return getModel().getValue(); }

  public int getMinimum() { return getModel().getMinimum(); }

  public int getMaximum() { return getModel().getMaximum(); }

  public void setValue(int n) {
    BoundedRangeModel brm = getModel();
    int oldValue = brm.getValue();
    brm.setValue(n);
    if (accessibleContext != null) {
      accessibleContext.firePropertyChange(AccessibleContext.ACCESSIBLE_VALUE_PROPERTY, Integer.valueOf(oldValue),
          Integer.valueOf(brm.getValue()));
    }
  }

  public void setMinimum(int n) { getModel().setMinimum(n); }

  public void setMaximum(int n) { getModel().setMaximum(n); }

  public void setIndeterminate(boolean newValue) {
    boolean oldValue = indeterminate;
    indeterminate = newValue;
    firePropertyChange("indeterminate", oldValue, indeterminate);
  }

  public boolean isIndeterminate() { return indeterminate; }

  protected String paramString() {
    String orientationString = (orientation == HORIZONTAL ? "HORIZONTAL" : "VERTICAL");
    String paintBorderString = (paintBorder ? "true" : "false");
    String progressStringString = (progressString != null ? progressString : "");
    String paintStringString = (paintString ? "true" : "false");
    String indeterminateString = (indeterminate ? "true" : "false");
    return super.paramString() + ",orientation=" + orientationString + ",paintBorder=" + paintBorderString
        + ",paintString=" + paintStringString + ",progressString=" + progressStringString + ",indeterminateString="
        + indeterminateString;
  }

  public AccessibleContext getAccessibleContext() {
    if (accessibleContext == null) {
      accessibleContext = new AccessibleJProgressBar();
    }
    return accessibleContext;
  }

  protected class AccessibleJProgressBar extends AccessibleJComponent implements AccessibleValue {
    public AccessibleStateSet getAccessibleStateSet() {
      AccessibleStateSet states = super.getAccessibleStateSet();
      if (getModel().getValueIsAdjusting()) {
        states.add(AccessibleState.BUSY);
      }
      if (getOrientation() == VERTICAL) {
        states.add(AccessibleState.VERTICAL);
      } else {
        states.add(AccessibleState.HORIZONTAL);
      }
      return states;
    }

    public AccessibleRole getAccessibleRole() { return AccessibleRole.PROGRESS_BAR; }

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
