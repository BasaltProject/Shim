/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.text;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.text.ParseException;
import javax.swing.JFormattedTextField;

public class DefaultFormatter extends JFormattedTextField.AbstractFormatter implements Cloneable, Serializable {
  private boolean allowsInvalid;
  private boolean overwriteMode;
  private boolean commitOnEdit;
  private Class<?> valueClass;

  public DefaultFormatter() {
    overwriteMode = true;
    allowsInvalid = true;
  }

  public void install(JFormattedTextField ftf) {
    super.install(ftf);
  }

  public void setCommitsOnValidEdit(boolean commit) { commitOnEdit = commit; }

  public boolean getCommitsOnValidEdit() { return commitOnEdit; }

  public void setOverwriteMode(boolean overwriteMode) { this.overwriteMode = overwriteMode; }

  public boolean getOverwriteMode() { return overwriteMode; }

  public void setAllowsInvalid(boolean allowsInvalid) { this.allowsInvalid = allowsInvalid; }

  public boolean getAllowsInvalid() { return allowsInvalid; }

  public void setValueClass(Class<?> valueClass) { this.valueClass = valueClass; }

  public Class<?> getValueClass() { return valueClass; }

  public Object stringToValue(String string) throws ParseException {
    Class<?> vc = getValueClass();
    JFormattedTextField ftf = getFormattedTextField();
    if (vc == null && ftf != null) {
      Object value = ftf.getValue();
      if (value != null) {
        vc = value.getClass();
      }
    }
    if (vc != null) {
      Constructor<?> cons;
      try {
        cons = vc.getConstructor(String.class);
      } catch (NoSuchMethodException nsme) {
        cons = null;
      }
      if (cons != null) {
        try {
          return cons.newInstance(string);
        } catch (Throwable ex) {
          throw new ParseException("Error creating instance", 0);
        }
      }
    }
    return string;
  }

  public String valueToString(Object value) throws ParseException {
    return value == null ? "" : value.toString();
  }

  protected DocumentFilter getDocumentFilter() { return null; }

  protected NavigationFilter getNavigationFilter() { return null; }

  public Object clone() throws CloneNotSupportedException { return super.clone(); }
}
