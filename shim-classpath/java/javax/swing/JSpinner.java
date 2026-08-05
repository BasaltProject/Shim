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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.SpinnerUI;
import javax.swing.text.DateFormatter;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

public class JSpinner extends JComponent implements javax.accessibility.Accessible {
  private SpinnerModel model;
  private JComponent editor;
  private boolean editorExplicitlySet;
  private ChangeListener modelListener;
  private transient ChangeEvent changeEvent;

  public JSpinner(SpinnerModel model) {
    if (model == null) {
      throw new NullPointerException("model cannot be null");
    }
    this.model = model;
    this.editor = createEditor(model);
    setUIProperty("opaque", true);
    updateUI();
  }

  public JSpinner() { this(new SpinnerNumberModel()); }

  public SpinnerUI getUI() { return (SpinnerUI) ui; }

  public void setUI(SpinnerUI ui) { super.setUI(ui); }

  public String getUIClassID() { return "SpinnerUI"; }

  public void updateUI() { setUI((SpinnerUI) UIManager.getUI(this)); }

  protected JComponent createEditor(SpinnerModel model) {
    if (model instanceof SpinnerDateModel) {
      return new DateEditor(this);
    } else if (model instanceof SpinnerListModel) {
      return new ListEditor(this);
    } else if (model instanceof SpinnerNumberModel) {
      return new NumberEditor(this);
    } else {
      return new DefaultEditor(this);
    }
  }

  public void setModel(SpinnerModel model) {
    if (model == null) {
      throw new IllegalArgumentException("null model");
    }
    if (!model.equals(this.model)) {
      SpinnerModel oldModel = this.model;
      this.model = model;
      if (modelListener != null) {
        oldModel.removeChangeListener(modelListener);
        this.model.addChangeListener(modelListener);
      }
      firePropertyChange("model", oldModel, model);
      if (!editorExplicitlySet) {
        setEditor(createEditor(model));
        editorExplicitlySet = false;
      }
    }
  }

  public SpinnerModel getModel() { return model; }

  public Object getValue() { return getModel().getValue(); }

  public void setValue(Object value) { getModel().setValue(value); }

  public Object getNextValue() { return getModel().getNextValue(); }

  public void addChangeListener(ChangeListener listener) {
    if (modelListener == null) {
      modelListener = new ModelListener();
      getModel().addChangeListener(modelListener);
    }
    listenerList.add(ChangeListener.class, listener);
  }

  public void removeChangeListener(ChangeListener listener) { listenerList.remove(ChangeListener.class, listener); }

  public ChangeListener[] getChangeListeners() { return listenerList.getListeners(ChangeListener.class); }

  protected void fireStateChanged() {
    Object[] listeners = listenerList.getListenerList();
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == ChangeListener.class) {
        if (changeEvent == null) {
          changeEvent = new ChangeEvent(this);
        }
        ((ChangeListener) listeners[i + 1]).stateChanged(changeEvent);
      }
    }
  }

  public Object getPreviousValue() { return getModel().getPreviousValue(); }

  public void setEditor(JComponent editor) {
    if (editor == null) {
      throw new IllegalArgumentException("null editor");
    }
    editorExplicitlySet = true;
    if (!editor.equals(this.editor)) {
      JComponent oldEditor = this.editor;
      this.editor = editor;
      if (oldEditor instanceof DefaultEditor) {
        ((DefaultEditor) oldEditor).dismiss(this);
      }
      firePropertyChange("editor", oldEditor, editor);
      revalidate();
      repaint();
    }
  }

  public JComponent getEditor() { return editor; }

  public void commitEdit() throws ParseException {
    JComponent editor = getEditor();
    if (editor instanceof DefaultEditor) {
      ((DefaultEditor) editor).commitEdit();
    }
  }

  public AccessibleContext getAccessibleContext() {
    if (accessibleContext == null) {
      accessibleContext = new AccessibleJSpinner();
    }
    return accessibleContext;
  }

  protected class AccessibleJSpinner extends AccessibleJComponent {
    protected AccessibleJSpinner() { }

    public AccessibleRole getAccessibleRole() { return AccessibleRole.SPIN_BOX; }
  }

  private class ModelListener implements ChangeListener, Serializable {
    public void stateChanged(ChangeEvent e) { fireStateChanged(); }
  }

  public static class DefaultEditor extends JPanel implements ChangeListener, PropertyChangeListener, LayoutManager {
    private final JSpinner owner;

    public DefaultEditor(JSpinner spinner) {
      super(null);
      owner = spinner;
      JFormattedTextField ftf = new JFormattedTextField();
      ftf.setName("Spinner.formattedTextField");
      ftf.setValue(spinner.getValue());
      ftf.addPropertyChangeListener(this);
      ftf.setEditable(false);
      ftf.setInheritsPopupMenu(true);
      String toolTipText = spinner.getToolTipText();
      if (toolTipText != null) {
        ftf.setToolTipText(toolTipText);
      }
      add(ftf);
      setLayout(this);
      spinner.addChangeListener(this);
    }

    public void dismiss(JSpinner spinner) { spinner.removeChangeListener(this); }

    public JSpinner getSpinner() {
      for (Component c = this; c != null; c = c.getParent()) {
        if (c instanceof JSpinner) {
          return (JSpinner) c;
        }
      }
      return owner;
    }

    public JFormattedTextField getTextField() { return (JFormattedTextField) getComponent(0); }

    public void stateChanged(ChangeEvent e) {
      JSpinner spinner = (JSpinner) (e.getSource());
      getTextField().setValue(spinner.getValue());
    }

    public void propertyChange(PropertyChangeEvent e) {
      JSpinner spinner = getSpinner();
      if (spinner == null) {
        return;
      }
      Object source = e.getSource();
      String name = e.getPropertyName();
      if ((source instanceof JFormattedTextField) && "value".equals(name)) {
        Object lastValue = spinner.getValue();
        try {
          spinner.setValue(getTextField().getValue());
        } catch (IllegalArgumentException iae) {
          try {
            ((JFormattedTextField) source).setValue(lastValue);
          } catch (IllegalArgumentException iae2) {
            return;
          }
        }
      }
    }

    public void addLayoutComponent(String name, Component child) { }

    public void removeLayoutComponent(Component child) { }

    private Dimension insetSize(Container parent) {
      Insets insets = parent.getInsets();
      return new Dimension(insets.left + insets.right, insets.top + insets.bottom);
    }

    public Dimension preferredLayoutSize(Container parent) {
      Dimension preferredSize = insetSize(parent);
      if (parent.getComponentCount() > 0) {
        Dimension childSize = getComponent(0).getPreferredSize();
        preferredSize.width += childSize.width;
        preferredSize.height += childSize.height;
      }
      return preferredSize;
    }

    public Dimension minimumLayoutSize(Container parent) {
      Dimension minimumSize = insetSize(parent);
      if (parent.getComponentCount() > 0) {
        Dimension childSize = getComponent(0).getMinimumSize();
        minimumSize.width += childSize.width;
        minimumSize.height += childSize.height;
      }
      return minimumSize;
    }

    public void layoutContainer(Container parent) {
      if (parent.getComponentCount() > 0) {
        Insets insets = parent.getInsets();
        int w = parent.getWidth() - (insets.left + insets.right);
        int h = parent.getHeight() - (insets.top + insets.bottom);
        getComponent(0).setBounds(insets.left, insets.top, w, h);
      }
    }

    public void commitEdit() throws ParseException { getTextField().commitEdit(); }

    public int getBaseline(int width, int height) {
      super.getBaseline(width, height);
      Insets insets = getInsets();
      width = width - insets.left - insets.right;
      height = height - insets.top - insets.bottom;
      int baseline = getComponent(0).getBaseline(width, height);
      if (baseline >= 0) {
        return baseline + insets.top;
      }
      return -1;
    }

    public BaselineResizeBehavior getBaselineResizeBehavior() { return getComponent(0).getBaselineResizeBehavior(); }
  }

  private static class NumberEditorFormatter extends NumberFormatter {
    private final SpinnerNumberModel model;

    NumberEditorFormatter(SpinnerNumberModel model, NumberFormat format) {
      super(format);
      this.model = model;
      setValueClass(model.getValue().getClass());
    }

    public void setMinimum(Comparable<?> minimum) { model.setMinimum(minimum); }

    public Comparable<?> getMinimum() { return model.getMinimum(); }

    public void setMaximum(Comparable<?> maximum) { model.setMaximum(maximum); }

    public Comparable<?> getMaximum() { return model.getMaximum(); }
  }

  public static class NumberEditor extends DefaultEditor {
    private static String getDefaultPattern(Locale locale) {
      NumberFormat nf = NumberFormat.getInstance(locale);
      if (nf instanceof DecimalFormat) {
        return ((DecimalFormat) nf).toPattern();
      }
      return "#,##0.###";
    }

    public NumberEditor(JSpinner spinner) { this(spinner, getDefaultPattern(spinner.getLocale())); }

    public NumberEditor(JSpinner spinner, String decimalFormatPattern) {
      this(spinner, new DecimalFormat(decimalFormatPattern));
    }

    private NumberEditor(JSpinner spinner, DecimalFormat format) {
      super(spinner);
      if (!(spinner.getModel() instanceof SpinnerNumberModel)) {
        throw new IllegalArgumentException("model not a SpinnerNumberModel");
      }
      SpinnerNumberModel model = (SpinnerNumberModel) spinner.getModel();
      NumberFormatter formatter = new NumberEditorFormatter(model, format);
      DefaultFormatterFactory factory = new DefaultFormatterFactory(formatter);
      JFormattedTextField ftf = getTextField();
      ftf.setEditable(true);
      ftf.setFormatterFactory(factory);
      try {
        String maxString = formatter.valueToString(model.getMinimum());
        String minString = formatter.valueToString(model.getMaximum());
        ftf.setColumns(Math.max(maxString.length(), minString.length()));
      } catch (ParseException e) {
        return;
      } catch (RuntimeException e) {
        return;
      }
    }

    public DecimalFormat getFormat() {
      return (DecimalFormat) ((NumberFormatter) (getTextField().getFormatter())).getFormat();
    }

    public SpinnerNumberModel getModel() { return (SpinnerNumberModel) (getSpinner().getModel()); }

    public void setComponentOrientation(ComponentOrientation o) {
      super.setComponentOrientation(o);
      getTextField().setHorizontalAlignment(o.isLeftToRight() ? JTextField.RIGHT : JTextField.LEFT);
    }
  }

  private static class DateEditorFormatter extends DateFormatter {
    private final SpinnerDateModel model;

    @SuppressWarnings("unchecked")
    private static Comparable<Date> cast(Comparable<?> c) { return (Comparable<Date>) c; }

    DateEditorFormatter(SpinnerDateModel model, DateFormat format) {
      super(format);
      this.model = model;
    }

    public void setMinimum(Comparable<?> minimum) { model.setStart(cast(minimum)); }

    public Comparable<?> getMinimum() { return model.getStart(); }

    public void setMaximum(Comparable<?> maximum) { model.setEnd(cast(maximum)); }

    public Comparable<?> getMaximum() { return model.getEnd(); }
  }

  public static class DateEditor extends DefaultEditor {
    private static String getDefaultPattern(Locale loc) {
      DateFormat f = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, loc);
      if (f instanceof SimpleDateFormat) {
        return ((SimpleDateFormat) f).toPattern();
      }
      return "M/d/yy";
    }

    public DateEditor(JSpinner spinner) { this(spinner, getDefaultPattern(spinner.getLocale())); }

    public DateEditor(JSpinner spinner, String dateFormatPattern) {
      this(spinner, new SimpleDateFormat(dateFormatPattern, spinner.getLocale()));
    }

    private DateEditor(JSpinner spinner, SimpleDateFormat format) {
      super(spinner);
      if (!(spinner.getModel() instanceof SpinnerDateModel)) {
        throw new IllegalArgumentException("model not a SpinnerDateModel");
      }
      SpinnerDateModel model = (SpinnerDateModel) spinner.getModel();
      DateFormatter formatter = new DateEditorFormatter(model, format);
      DefaultFormatterFactory factory = new DefaultFormatterFactory(formatter);
      JFormattedTextField ftf = getTextField();
      ftf.setEditable(true);
      ftf.setFormatterFactory(factory);
      try {
        String maxString = formatter.valueToString(model.getStart());
        String minString = formatter.valueToString(model.getEnd());
        ftf.setColumns(Math.max(maxString.length(), minString.length()));
      } catch (ParseException e) {
        return;
      } catch (RuntimeException e) {
        return;
      }
    }

    public SimpleDateFormat getFormat() {
      return (SimpleDateFormat) ((DateFormatter) (getTextField().getFormatter())).getFormat();
    }

    public SpinnerDateModel getModel() { return (SpinnerDateModel) (getSpinner().getModel()); }
  }

  public static class ListEditor extends DefaultEditor {
    public ListEditor(JSpinner spinner) {
      super(spinner);
      if (!(spinner.getModel() instanceof SpinnerListModel)) {
        throw new IllegalArgumentException("model not a SpinnerListModel");
      }
      getTextField().setEditable(true);
      getTextField().setFormatterFactory(new DefaultFormatterFactory(new ListFormatter()));
    }

    public SpinnerListModel getModel() { return (SpinnerListModel) (getSpinner().getModel()); }

    private class ListFormatter extends DefaultFormatter {
      public String valueToString(Object value) throws ParseException {
        if (value == null) {
          return "";
        }
        return value.toString();
      }

      public Object stringToValue(String string) throws ParseException {
        return getModel().findNextMatch(string) == null ? super.stringToValue(string) : getModel().findNextMatch(string);
      }
    }
  }
}
