/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.*;
import javax.swing.text.*;
import javax.accessibility.*;

public class JPasswordField extends javax.swing.JTextField {
  private boolean echoCharSet;
  private AccessibleContext accessibleContext;
  private void provideErrorFeedback() {
    UIManager.getLookAndFeel().provideErrorFeedback(this);
  }
  private java.lang.String bp_Text;
  private char[] bp_Password;
  private char bp_EchoChar;
  private javax.accessibility.AccessibleContext bp_AccessibleContext;
  public JPasswordField(javax.swing.text.Document p0, java.lang.String p1, int p2) {
    super(p0, p1, p2);
  }
  public JPasswordField(java.lang.String p0, int p1) {
    super(p0, p1);
  }
  public JPasswordField(int p0) {
    super(p0);
  }
  public JPasswordField(java.lang.String p0) {
    super(p0);
  }
  public JPasswordField() {
  }
  public void copy() {
    provideErrorFeedback();
  }
  protected java.lang.String paramString() {
    return super.paramString() + ",echoChar=" + bp_EchoChar;
  }
  public java.lang.String getText() {
    return new String(getPassword());
  }
  public java.lang.String getText(int p0, int p1) throws javax.swing.text.BadLocationException {
    return super.getText(p0, p1);
  }
  public char[] getPassword() {
    Document doc = getDocument();
    Segment txt = new Segment();
    try {
      doc.getText(0, doc.getLength(), txt);
    } catch (BadLocationException e) {
      return null;
    }
    char[] retValue = new char[txt.count];
    System.arraycopy(txt.array, txt.offset, retValue, 0, txt.count);
    return retValue;
  }
  public void setEchoChar(char p0) {
    bp_EchoChar = p0;
    echoCharSet = true;
    repaint();
    revalidate();
  }
  public char getEchoChar() { return bp_EchoChar; }
  public boolean echoCharIsSet() {
    return bp_EchoChar != 0;
  }
  public void cut() {
    provideErrorFeedback();
  }
  public void setText(java.lang.String p0) {
    super.setText(p0);
  }
  public javax.accessibility.AccessibleContext getAccessibleContext() {
    if (accessibleContext == null) {
      accessibleContext = new AccessibleJPasswordField();
    }
    return accessibleContext;
  }
  public void updateUI() {
    if (!echoCharSet) {
      bp_EchoChar = '*';
    }
    super.updateUI();
  }
  protected void setUIProperty(String propertyName, Object value) {
    if ("echoChar".equals(propertyName)) {
      if (!echoCharSet) {
        setEchoChar(((Character) value).charValue());
        echoCharSet = false;
      }
    } else {
      super.setUIProperty(propertyName, value);
    }
  }
  public java.lang.String getUIClassID() { return "PasswordFieldUI"; }

  protected class AccessibleJPasswordField extends AccessibleJTextField {
    public AccessibleRole getAccessibleRole() { return AccessibleRole.PASSWORD_TEXT; }
  }

}
