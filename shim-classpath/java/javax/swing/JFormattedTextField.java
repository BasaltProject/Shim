/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.event.FocusEvent;
import java.awt.event.InputMethodEvent;
import java.text.DateFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.DateFormatter;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.InternationalFormatter;
import javax.swing.text.NavigationFilter;
import javax.swing.text.NumberFormatter;

public class JFormattedTextField extends JTextField {
  public static final int COMMIT = 0;
  public static final int COMMIT_OR_REVERT = 1;
  public static final int REVERT = 2;
  public static final int PERSIST = 3;

  private AbstractFormatterFactory factory;
  private AbstractFormatter format;
  private Object value;
  private boolean editValid = true;
  private int focusLostBehavior = COMMIT_OR_REVERT;
  private boolean edited;
  private DocumentListener editTracker;

  public JFormattedTextField() {
    super();
    setFocusLostBehavior(COMMIT_OR_REVERT);
    installTracker();
  }

  public JFormattedTextField(Object value) {
    this();
    setValue(value);
  }

  public JFormattedTextField(Format format) {
    this();
    setFormatterFactory(getDefaultFormatterFactory(format));
  }

  public JFormattedTextField(AbstractFormatter formatter) {
    this(new DefaultFormatterFactory(formatter));
  }

  public JFormattedTextField(AbstractFormatterFactory factory) {
    this();
    setFormatterFactory(factory);
  }

  public JFormattedTextField(AbstractFormatterFactory factory, Object currentValue) {
    this(currentValue);
    setFormatterFactory(factory);
  }

  private void installTracker() {
    editTracker = new DocumentListener() {
      public void insertUpdate(DocumentEvent e) { textEdited(); }

      public void removeUpdate(DocumentEvent e) {
        if (getDocument().getLength() > 0) {
          textEdited();
        }
      }

      public void changedUpdate(DocumentEvent e) { }
    };
    getDocument().addDocumentListener(editTracker);
  }

  private boolean updating;

  private void textEdited() {
    if (updating) {
      return;
    }
    edited = true;
    AbstractFormatter f = getFormatter();
    if (f != null) {
      boolean ok = true;
      try {
        f.stringToValue(getText());
      } catch (ParseException pe) {
        ok = false;
      }
      setEditValid(ok);
    }
  }

  public void setFocusLostBehavior(int behavior) {
    if (behavior != COMMIT && behavior != COMMIT_OR_REVERT && behavior != PERSIST && behavior != REVERT) {
      throw new IllegalArgumentException("setFocusLostBehavior must be one of: JFormattedTextField.COMMIT, "
          + "JFormattedTextField.COMMIT_OR_REVERT, JFormattedTextField.PERSIST or JFormattedTextField.REVERT");
    }
    focusLostBehavior = behavior;
  }

  public int getFocusLostBehavior() { return focusLostBehavior; }

  public void setFormatterFactory(AbstractFormatterFactory tf) {
    AbstractFormatterFactory oldFactory = factory;
    factory = tf;
    firePropertyChange("formatterFactory", oldFactory, tf);
    setValue(getValue(), true, false);
  }

  public AbstractFormatterFactory getFormatterFactory() { return factory; }

  protected void setFormatter(AbstractFormatter format) {
    AbstractFormatter oldFormat = this.format;
    if (oldFormat != null) {
      oldFormat.uninstall();
    }
    setEditValid(true);
    this.format = format;
    if (format != null) {
      updating = true;
      try {
        format.install(this);
      } finally {
        updating = false;
      }
    }
    setEdited(false);
    firePropertyChange("textFormatter", oldFormat, format);
  }

  public AbstractFormatter getFormatter() { return format; }

  public void setValue(Object value) {
    if (value != null && getFormatterFactory() == null) {
      setFormatterFactory(getDefaultFormatterFactory(value));
    }
    setValue(value, true, true);
  }

  public Object getValue() { return value; }

  public void commitEdit() throws ParseException {
    AbstractFormatter format = getFormatter();
    if (format != null) {
      setValue(format.stringToValue(getText()), false, true);
    }
  }

  private void setEditValid(boolean isValid) {
    if (isValid != editValid) {
      editValid = isValid;
      firePropertyChange("editValid", Boolean.valueOf(!isValid), Boolean.valueOf(isValid));
    }
  }

  public boolean isEditValid() { return editValid; }

  protected void invalidEdit() {
    try {
      java.awt.Toolkit.getDefaultToolkit().beep();
    } catch (RuntimeException ex) {
      return;
    }
  }

  protected void processInputMethodEvent(InputMethodEvent e) { super.processInputMethodEvent(e); }

  protected void processFocusEvent(FocusEvent e) {
    super.processFocusEvent(e);
    if (e.isTemporary()) {
      return;
    }
    if (isEdited() && e.getID() == FocusEvent.FOCUS_LOST) {
      int fb = getFocusLostBehavior();
      if (fb == COMMIT || fb == COMMIT_OR_REVERT) {
        try {
          commitEdit();
          setValue(getValue(), true, true);
        } catch (ParseException pe) {
          if (fb == COMMIT_OR_REVERT) {
            setValue(getValue(), true, true);
          }
        }
      } else if (fb == REVERT) {
        setValue(getValue());
      }
    } else if (e.getID() == FocusEvent.FOCUS_GAINED) {
      AbstractFormatterFactory f = getFormatterFactory();
      if (f != null) {
        setFormatter(f.getFormatter(this));
      }
    }
  }

  private static final Action[] defaultActions = {new CancelAction()};

  public Action[] getActions() {
    return javax.swing.text.TextAction.augmentList(super.getActions(), defaultActions);
  }

  static class CancelAction extends javax.swing.text.TextAction {
    CancelAction() { super("reset-field-edit"); }

    public void actionPerformed(java.awt.event.ActionEvent e) {
      javax.swing.text.JTextComponent target = getFocusedComponent();
      if (target instanceof JFormattedTextField) {
        JFormattedTextField ftf = (JFormattedTextField) target;
        ftf.setValue(ftf.getValue());
      }
    }

    public boolean isEnabled() {
      javax.swing.text.JTextComponent target = getFocusedComponent();
      if (target instanceof JFormattedTextField) {
        return ((JFormattedTextField) target).isEdited();
      }
      return super.isEnabled();
    }
  }

  public String getUIClassID() { return "FormattedTextFieldUI"; }

  public void setDocument(Document doc) {
    Document old = getDocument();
    if (old != null && editTracker != null) {
      old.removeDocumentListener(editTracker);
    }
    super.setDocument(doc);
    if (editTracker != null && doc != null) {
      doc.addDocumentListener(editTracker);
    }
  }

  private void setEdited(boolean edited) { this.edited = edited; }

  private boolean isEdited() { return edited; }

  private void setValue(Object value, boolean createFormat, boolean firePC) {
    Object oldValue = this.value;
    this.value = value;
    if (createFormat) {
      AbstractFormatterFactory f = getFormatterFactory();
      AbstractFormatter atf = f != null ? f.getFormatter(this) : null;
      setFormatter(atf);
    } else {
      setEditValid(true);
    }
    setEdited(false);
    if (firePC) {
      firePropertyChange("value", oldValue, value);
    }
  }

  private AbstractFormatterFactory getDefaultFormatterFactory(Object type) {
    if (type instanceof DateFormat) {
      return new DefaultFormatterFactory(new DateFormatter((DateFormat) type));
    }
    if (type instanceof NumberFormat) {
      return new DefaultFormatterFactory(new NumberFormatter((NumberFormat) type));
    }
    if (type instanceof Format) {
      return new DefaultFormatterFactory(new InternationalFormatter((Format) type));
    }
    if (type instanceof java.util.Date) {
      return new DefaultFormatterFactory(new DateFormatter());
    }
    if (type instanceof Number) {
      AbstractFormatter displayFormatter = new NumberFormatter();
      ((NumberFormatter) displayFormatter).setValueClass(type.getClass());
      AbstractFormatter editFormatter = new NumberFormatter(new java.text.DecimalFormat("#.#"));
      ((NumberFormatter) editFormatter).setValueClass(type.getClass());
      return new DefaultFormatterFactory(displayFormatter, displayFormatter, editFormatter);
    }
    return new DefaultFormatterFactory(new DefaultFormatter());
  }

  public abstract static class AbstractFormatterFactory {
    protected AbstractFormatterFactory() { }

    public abstract AbstractFormatter getFormatter(JFormattedTextField tf);
  }

  public abstract static class AbstractFormatter implements java.io.Serializable {
    private JFormattedTextField ftf;

    protected AbstractFormatter() { }

    public void install(JFormattedTextField ftf) {
      if (this.ftf != null) {
        uninstall();
      }
      this.ftf = ftf;
      if (ftf != null) {
        try {
          ftf.setText(valueToString(ftf.getValue()));
        } catch (ParseException pe) {
          setEditValid(false);
          invalidEdit();
        }
        Document doc = ftf.getDocument();
        DocumentFilter df = getDocumentFilter();
        if (doc instanceof AbstractDocument && df != null) {
          ((AbstractDocument) doc).setDocumentFilter(df);
        }
        NavigationFilter nf = getNavigationFilter();
        if (nf != null) {
          ftf.setNavigationFilter(nf);
        }
      }
    }

    public void uninstall() {
      if (ftf != null) {
        Document doc = ftf.getDocument();
        if (doc instanceof AbstractDocument && getDocumentFilter() != null) {
          ((AbstractDocument) doc).setDocumentFilter(null);
        }
        if (getNavigationFilter() != null) {
          ftf.setNavigationFilter(null);
        }
      }
      this.ftf = null;
    }

    public abstract Object stringToValue(String text) throws ParseException;

    public abstract String valueToString(Object value) throws ParseException;

    protected JFormattedTextField getFormattedTextField() { return ftf; }

    protected void invalidEdit() {
      JFormattedTextField ftf = getFormattedTextField();
      if (ftf != null) {
        ftf.invalidEdit();
      }
    }

    protected void setEditValid(boolean valid) {
      JFormattedTextField ftf = getFormattedTextField();
      if (ftf != null) {
        ftf.setEditValid(valid);
      }
    }

    protected Action[] getActions() { return null; }

    protected DocumentFilter getDocumentFilter() { return null; }

    protected NavigationFilter getNavigationFilter() { return null; }

    protected Object clone() throws CloneNotSupportedException {
      AbstractFormatter format = (AbstractFormatter) super.clone();
      format.ftf = null;
      return format;
    }
  }
}
