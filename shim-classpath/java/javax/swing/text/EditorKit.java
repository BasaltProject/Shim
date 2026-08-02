/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.text;

public abstract class EditorKit implements java.lang.Cloneable, java.io.Serializable {
  public EditorKit() {
  }
  public java.lang.Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      throw new InternalError(e);
    }
  }
  public abstract void write(java.io.Writer p0, javax.swing.text.Document p1, int p2, int p3) throws java.io.IOException, javax.swing.text.BadLocationException;
  public abstract void write(java.io.OutputStream p0, javax.swing.text.Document p1, int p2, int p3) throws java.io.IOException, javax.swing.text.BadLocationException;
  public abstract void read(java.io.Reader p0, javax.swing.text.Document p1, int p2) throws java.io.IOException, javax.swing.text.BadLocationException;
  public abstract void read(java.io.InputStream p0, javax.swing.text.Document p1, int p2) throws java.io.IOException, javax.swing.text.BadLocationException;
  public void deinstall(javax.swing.JEditorPane c) { }
  public void install(javax.swing.JEditorPane c) { }
  public abstract javax.swing.text.Document createDefaultDocument();
  public abstract javax.swing.text.ViewFactory getViewFactory();
  public abstract javax.swing.text.Caret createCaret();
  public abstract java.lang.String getContentType();
  public abstract javax.swing.Action[] getActions();
}
