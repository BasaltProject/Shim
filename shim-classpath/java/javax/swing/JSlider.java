/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.Font;
import java.awt.Image;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;
import javax.accessibility.AccessibleValue;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.SliderUI;
import javax.swing.plaf.UIResource;

public class JSlider extends JComponent implements SwingConstants, Accessible {
  protected int majorTickSpacing;
  protected int minorTickSpacing;
  protected boolean snapToTicks = false;
  protected BoundedRangeModel sliderModel;
  protected int orientation;
  protected ChangeEvent changeEvent = null;
  protected ChangeListener changeListener = new ModelListener();

  private final CopyOnWriteArrayList<ChangeListener> changeListeners = new CopyOnWriteArrayList<>();
  private boolean paintTicks = false;
  private boolean paintTrack = true;
  private boolean paintLabels = false;
  private boolean isInverted = false;
  private Dictionary labelTable;
  private AccessibleContext accessibleContext;

  private void checkOrientation(int orientation) {
    switch (orientation) {
      case VERTICAL:
      case HORIZONTAL:
        break;
      default:
        throw new IllegalArgumentException("orientation must be one of: VERTICAL, HORIZONTAL");
    }
  }

  public JSlider() { this(HORIZONTAL, 0, 100, 50); }

  public JSlider(int orientation) { this(orientation, 0, 100, 50); }

  public JSlider(int min, int max) { this(HORIZONTAL, min, max, (min + max) / 2); }

  public JSlider(int min, int max, int value) { this(HORIZONTAL, min, max, value); }

  public JSlider(int orientation, int min, int max, int value) {
    checkOrientation(orientation);
    this.orientation = orientation;
    setModel(new DefaultBoundedRangeModel(value, 0, min, max));
    updateUI();
  }

  public JSlider(BoundedRangeModel brm) {
    this.orientation = JSlider.HORIZONTAL;
    setModel(brm);
    updateUI();
  }

  public SliderUI getUI() { return (SliderUI) ui; }

  public void setUI(SliderUI ui) { super.setUI(ui); }

  public void updateUI() {
    setUI((javax.swing.plaf.ComponentUI) UIManager.getUI(this));
    updateLabelUIs();
  }

  public String getUIClassID() { return "SliderUI"; }

  private class ModelListener implements ChangeListener, java.io.Serializable {
    public void stateChanged(ChangeEvent e) { fireStateChanged(); }
  }

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

  protected ChangeListener createChangeListener() { return new ModelListener(); }

  public BoundedRangeModel getModel() { return sliderModel; }

  public void setModel(BoundedRangeModel newModel) {
    BoundedRangeModel oldModel = getModel();
    if (oldModel != null) {
      oldModel.removeChangeListener(changeListener);
    }
    sliderModel = newModel;
    if (newModel != null) {
      newModel.addChangeListener(changeListener);
    }
    if (accessibleContext != null) {
      accessibleContext.firePropertyChange(AccessibleContext.ACCESSIBLE_VALUE_PROPERTY,
          (oldModel == null ? null : Integer.valueOf(oldModel.getValue())),
          (newModel == null ? null : Integer.valueOf(newModel.getValue())));
    }
    firePropertyChange("model", oldModel, sliderModel);
  }

  public int getValue() { return getModel().getValue(); }

  public void setValue(int n) {
    BoundedRangeModel m = getModel();
    int oldValue = m.getValue();
    if (oldValue == n) {
      return;
    }
    m.setValue(n);
    if (accessibleContext != null) {
      accessibleContext.firePropertyChange(AccessibleContext.ACCESSIBLE_VALUE_PROPERTY, Integer.valueOf(oldValue),
          Integer.valueOf(m.getValue()));
    }
  }

  public int getMinimum() { return getModel().getMinimum(); }

  public void setMinimum(int minimum) {
    int oldMin = getModel().getMinimum();
    getModel().setMinimum(minimum);
    firePropertyChange("minimum", Integer.valueOf(oldMin), Integer.valueOf(minimum));
  }

  public int getMaximum() { return getModel().getMaximum(); }

  public void setMaximum(int maximum) {
    int oldMax = getModel().getMaximum();
    getModel().setMaximum(maximum);
    firePropertyChange("maximum", Integer.valueOf(oldMax), Integer.valueOf(maximum));
  }

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

  public int getExtent() { return getModel().getExtent(); }

  public void setExtent(int extent) { getModel().setExtent(extent); }

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

  public void setFont(Font font) {
    super.setFont(font);
    updateLabelUIs();
  }

  public boolean imageUpdate(Image img, int infoflags, int x, int y, int w, int h) {
    if (!isShowing()) {
      return false;
    }
    Enumeration<?> elements = labelTable == null ? null : labelTable.elements();
    if (elements == null) {
      return false;
    }
    while (elements.hasMoreElements()) {
      Object c = elements.nextElement();
      if (c instanceof JLabel) {
        JLabel label = (JLabel) c;
        if (references(label.getIcon(), img) || references(label.getDisabledIcon(), img)) {
          return super.imageUpdate(img, infoflags, x, y, w, h);
        }
      }
    }
    return false;
  }

  private static boolean references(Icon icon, Image img) {
    return icon instanceof ImageIcon && ((ImageIcon) icon).getImage() == img;
  }

  public Dictionary getLabelTable() { return labelTable; }

  public void setLabelTable(Dictionary labels) {
    Dictionary oldTable = labelTable;
    labelTable = labels;
    updateLabelUIs();
    firePropertyChange("labelTable", oldTable, labelTable);
    if (labels != oldTable) {
      revalidate();
      repaint();
    }
  }

  protected void updateLabelUIs() {
    Dictionary labelTable = getLabelTable();
    if (labelTable == null) {
      return;
    }
    Enumeration<?> labels = labelTable.keys();
    while (labels.hasMoreElements()) {
      Object value = labelTable.get(labels.nextElement());
      if (value instanceof JComponent) {
        JComponent component = (JComponent) value;
        component.updateUI();
        component.setSize(component.getPreferredSize());
      }
    }
  }

  public Hashtable<Integer, JComponent> createStandardLabels(int increment) {
    return createStandardLabels(increment, getMinimum());
  }

  public Hashtable<Integer, JComponent> createStandardLabels(int increment, int start) {
    if (start > getMaximum() || start < getMinimum()) {
      throw new IllegalArgumentException("Slider label start point out of range.");
    }
    if (increment <= 0) {
      throw new IllegalArgumentException("Label incremement must be > 0");
    }
    class SmartHashtable extends Hashtable<Integer, JComponent> {
      private static final long serialVersionUID = 1L;
    }
    SmartHashtable table = new SmartHashtable();
    JLabel label;
    for (int labelIndex = start; labelIndex <= getMaximum(); labelIndex += increment) {
      label = new LabelUIResource("" + labelIndex, JLabel.CENTER);
      table.put(Integer.valueOf(labelIndex), label);
    }
    return table;
  }

  private class LabelUIResource extends JLabel implements UIResource {
    LabelUIResource(String text, int alignment) {
      super(text, alignment);
      setName("Slider.label");
    }

    public Font getFont() {
      Font font = super.getFont();
      if (font != null && !(font instanceof UIResource)) {
        return font;
      }
      return JSlider.this.getFont();
    }

    public java.awt.Color getForeground() {
      java.awt.Color fg = super.getForeground();
      if (fg != null && !(fg instanceof UIResource)) {
        return fg;
      }
      if (!(JSlider.this.getForeground() instanceof UIResource)) {
        return JSlider.this.getForeground();
      }
      return fg;
    }
  }

  public boolean getInverted() { return isInverted; }

  public void setInverted(boolean b) {
    boolean oldValue = isInverted;
    isInverted = b;
    firePropertyChange("inverted", oldValue, isInverted);
    if (b != oldValue) {
      repaint();
    }
  }

  public int getMajorTickSpacing() { return majorTickSpacing; }

  public void setMajorTickSpacing(int n) {
    int oldValue = majorTickSpacing;
    majorTickSpacing = n;
    if (labelTable == null && getMajorTickSpacing() > 0 && getPaintLabels()) {
      setLabelTable(createStandardLabels(getMajorTickSpacing()));
    }
    firePropertyChange("majorTickSpacing", oldValue, majorTickSpacing);
    if (majorTickSpacing != oldValue && getPaintTicks()) {
      repaint();
    }
  }

  public int getMinorTickSpacing() { return minorTickSpacing; }

  public void setMinorTickSpacing(int n) {
    int oldValue = minorTickSpacing;
    minorTickSpacing = n;
    firePropertyChange("minorTickSpacing", oldValue, minorTickSpacing);
    if (minorTickSpacing != oldValue && getPaintTicks()) {
      repaint();
    }
  }

  public boolean getSnapToTicks() { return snapToTicks; }

  boolean getSnapToValue() { return true; }

  public void setSnapToTicks(boolean b) {
    boolean oldValue = snapToTicks;
    snapToTicks = b;
    firePropertyChange("snapToTicks", oldValue, snapToTicks);
  }

  public boolean getPaintTicks() { return paintTicks; }

  public void setPaintTicks(boolean b) {
    boolean oldValue = paintTicks;
    paintTicks = b;
    firePropertyChange("paintTicks", oldValue, paintTicks);
    if (paintTicks != oldValue) {
      revalidate();
      repaint();
    }
  }

  public boolean getPaintTrack() { return paintTrack; }

  public void setPaintTrack(boolean b) {
    boolean oldValue = paintTrack;
    paintTrack = b;
    firePropertyChange("paintTrack", oldValue, paintTrack);
    if (paintTrack != oldValue) {
      repaint();
    }
  }

  public boolean getPaintLabels() { return paintLabels; }

  public void setPaintLabels(boolean b) {
    boolean oldValue = paintLabels;
    paintLabels = b;
    if (labelTable == null && getMajorTickSpacing() > 0) {
      setLabelTable(createStandardLabels(getMajorTickSpacing()));
    }
    firePropertyChange("paintLabels", oldValue, paintLabels);
    if (paintLabels != oldValue) {
      revalidate();
      repaint();
    }
  }

  protected String paramString() {
    String paintTicksString = (paintTicks ? "true" : "false");
    String paintTrackString = (paintTrack ? "true" : "false");
    String paintLabelsString = (paintLabels ? "true" : "false");
    String isInvertedString = (isInverted ? "true" : "false");
    String snapToTicksString = (snapToTicks ? "true" : "false");
    String orientationString = (orientation == HORIZONTAL ? "HORIZONTAL" : "VERTICAL");
    return super.paramString() + ",isInverted=" + isInvertedString + ",majorTickSpacing=" + majorTickSpacing
        + ",minorTickSpacing=" + minorTickSpacing + ",orientation=" + orientationString + ",paintLabels="
        + paintLabelsString + ",paintTicks=" + paintTicksString + ",paintTrack=" + paintTrackString
        + ",snapToTicks=" + snapToTicksString + ",snapToValue=" + getSnapToValue();
  }

  public AccessibleContext getAccessibleContext() {
    if (accessibleContext == null) {
      accessibleContext = new AccessibleJSlider();
    }
    return accessibleContext;
  }

  protected class AccessibleJSlider extends AccessibleJComponent implements AccessibleValue {
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

    public AccessibleRole getAccessibleRole() { return AccessibleRole.SLIDER; }

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

    public Number getMaximumAccessibleValue() {
      BoundedRangeModel model = JSlider.this.getModel();
      return Integer.valueOf(model.getMaximum() - model.getExtent());
    }
  }
}
