/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

public class TextComponent extends java.awt.Component implements javax.accessibility.Accessible {
  protected java.awt.event.TextListener textListener;
  TextComponent(java.lang.String p0) throws java.awt.HeadlessException {
  }
  TextComponent() {
  }
  protected java.lang.String paramString() { throw new UnsupportedOperationException(); }
  public java.lang.String getText() { throw new UnsupportedOperationException(); }
  public void setText(java.lang.String p0) { throw new UnsupportedOperationException(); }
  public int getSelectionStart() { throw new UnsupportedOperationException(); }
  public int getSelectionEnd() { throw new UnsupportedOperationException(); }
  public void setEditable(boolean p0) { throw new UnsupportedOperationException(); }
  public void setCaretPosition(int p0) { throw new UnsupportedOperationException(); }
  public int getCaretPosition() { throw new UnsupportedOperationException(); }
  protected void processTextEvent(java.awt.event.TextEvent p0) { throw new UnsupportedOperationException(); }
  public void addTextListener(java.awt.event.TextListener p0) { throw new UnsupportedOperationException(); }
  public boolean isEditable() { throw new UnsupportedOperationException(); }
  public void setSelectionStart(int p0) { throw new UnsupportedOperationException(); }
  public void setSelectionEnd(int p0) { throw new UnsupportedOperationException(); }
  public void selectAll() { throw new UnsupportedOperationException(); }
  public void removeTextListener(java.awt.event.TextListener p0) { throw new UnsupportedOperationException(); }
  public java.awt.event.TextListener[] getTextListeners() { throw new UnsupportedOperationException(); }
  public javax.accessibility.AccessibleContext getAccessibleContext() { throw new UnsupportedOperationException(); }
  public void addNotify() { throw new UnsupportedOperationException(); }
  public void removeNotify() { throw new UnsupportedOperationException(); }
  public <T extends java.util.EventListener> T[] getListeners(java.lang.Class<T> p0) { throw new UnsupportedOperationException(); }
  protected void processEvent(java.awt.AWTEvent p0) { throw new UnsupportedOperationException(); }
  public java.awt.Color getBackground() { throw new UnsupportedOperationException(); }
  public void setBackground(java.awt.Color p0) { throw new UnsupportedOperationException(); }
  public void enableInputMethods(boolean p0) { throw new UnsupportedOperationException(); }
  public java.awt.im.InputMethodRequests getInputMethodRequests() { throw new UnsupportedOperationException(); }
  public java.lang.String getSelectedText() { throw new UnsupportedOperationException(); }
  public void select(int p0, int p1) { throw new UnsupportedOperationException(); }
}
