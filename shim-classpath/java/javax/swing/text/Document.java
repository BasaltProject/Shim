/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.text;

public interface Document {
  java.lang.String StreamDescriptionProperty = "stream";
  java.lang.String TitleProperty = "title";
  void remove(int p0, int p1) throws javax.swing.text.BadLocationException;
  java.lang.Object getProperty(java.lang.Object p0);
  int getLength();
  java.lang.String getText(int p0, int p1) throws javax.swing.text.BadLocationException;
  void getText(int p0, int p1, javax.swing.text.Segment p2) throws javax.swing.text.BadLocationException;
  void putProperty(java.lang.Object p0, java.lang.Object p1);
  void removeDocumentListener(javax.swing.event.DocumentListener p0);
  void addDocumentListener(javax.swing.event.DocumentListener p0);
  void insertString(int p0, java.lang.String p1, javax.swing.text.AttributeSet p2) throws javax.swing.text.BadLocationException;
  javax.swing.text.Position createPosition(int p0) throws javax.swing.text.BadLocationException;
  javax.swing.text.Element[] getRootElements();
  javax.swing.text.Element getDefaultRootElement();
  void render(java.lang.Runnable p0);
  void addUndoableEditListener(javax.swing.event.UndoableEditListener p0);
  void removeUndoableEditListener(javax.swing.event.UndoableEditListener p0);
  javax.swing.text.Position getStartPosition();
  javax.swing.text.Position getEndPosition();
}
