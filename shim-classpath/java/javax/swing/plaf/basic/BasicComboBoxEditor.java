/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf.basic;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.ComboBoxEditor;
import javax.swing.JTextField;
import javax.swing.border.Border;

public class BasicComboBoxEditor implements ComboBoxEditor, FocusListener {
  protected JTextField editor;
  private Object oldValue;

  public BasicComboBoxEditor() {
    editor = createEditorComponent();
  }

  public Component getEditorComponent() {
    return editor;
  }

  protected JTextField createEditorComponent() {
    JTextField editor = new BorderlessTextField("", 9);
    editor.setBorder(null);
    return editor;
  }

  public void setItem(Object anObject) {
    String text;
    if (anObject != null) {
      text = anObject.toString();
      if (text == null) {
        text = "";
      }
      oldValue = anObject;
    } else {
      text = "";
    }
    if (!text.equals(editor.getText())) {
      editor.setText(text);
    }
  }

  public Object getItem() {
    Object newValue = editor.getText();
    if (oldValue != null && !(oldValue instanceof String)) {
      if (newValue.equals(oldValue.toString())) {
        return oldValue;
      }
      Class<?> cls = oldValue.getClass();
      try {
        java.lang.reflect.Method method = cls.getMethod("valueOf", new Class<?>[] {String.class});
        newValue = method.invoke(oldValue, new Object[] {editor.getText()});
      } catch (Exception ex) {
        // keep the text when the item type cannot parse it
      }
    }
    return newValue;
  }

  public void selectAll() {
    editor.selectAll();
    editor.requestFocus();
  }

  public void focusGained(FocusEvent e) {
  }

  public void focusLost(FocusEvent e) {
  }

  public void addActionListener(ActionListener l) {
    editor.addActionListener(l);
  }

  public void removeActionListener(ActionListener l) {
    editor.removeActionListener(l);
  }

  static class BorderlessTextField extends JTextField {
    public BorderlessTextField(String value, int n) {
      super(value, n);
    }

    public void setText(String s) {
      if (getText().equals(s)) {
        return;
      }
      super.setText(s);
    }

    public void setBorder(Border b) {
      if (!(b instanceof javax.swing.plaf.UIResource)) {
        super.setBorder(b);
      }
    }
  }

  public static class UIResource extends BasicComboBoxEditor implements javax.swing.plaf.UIResource {
    public UIResource() {
    }
  }
}
