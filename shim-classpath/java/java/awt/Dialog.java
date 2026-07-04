/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

public class Dialog extends java.awt.Window {
  public static final java.awt.Dialog.ModalityType DEFAULT_MODALITY_TYPE = null;
  public Dialog(java.awt.Window p0, java.lang.String p1) {
    super(p0);
    throw new java.awt.HeadlessException();
  }
  public Dialog(java.awt.Window p0) {
    super(p0);
    throw new java.awt.HeadlessException();
  }
  public Dialog(java.awt.Dialog p0, java.lang.String p1, boolean p2, java.awt.GraphicsConfiguration p3) {
    super(p0, p3);
    throw new java.awt.HeadlessException();
  }
  public Dialog(java.awt.Dialog p0, java.lang.String p1, boolean p2) {
    super(p0);
    throw new java.awt.HeadlessException();
  }
  public Dialog(java.awt.Window p0, java.awt.Dialog.ModalityType p1) {
    super(p0);
    throw new java.awt.HeadlessException();
  }
  public Dialog(java.awt.Window p0, java.lang.String p1, java.awt.Dialog.ModalityType p2) {
    super(p0);
    throw new java.awt.HeadlessException();
  }
  public Dialog(java.awt.Window p0, java.lang.String p1, java.awt.Dialog.ModalityType p2, java.awt.GraphicsConfiguration p3) {
    super(p0, p3);
    throw new java.awt.HeadlessException();
  }
  public Dialog(java.awt.Frame p0, java.lang.String p1, boolean p2) {
    super(p0);
    throw new java.awt.HeadlessException();
  }
  public Dialog(java.awt.Frame p0, java.lang.String p1) {
    super(p0);
    throw new java.awt.HeadlessException();
  }
  public Dialog(java.awt.Frame p0, boolean p1) {
    super(p0);
    throw new java.awt.HeadlessException();
  }
  public Dialog(java.awt.Frame p0) {
    super(p0);
    throw new java.awt.HeadlessException();
  }
  public Dialog(java.awt.Dialog p0, java.lang.String p1) {
    super(p0);
    throw new java.awt.HeadlessException();
  }
  public Dialog(java.awt.Dialog p0) {
    super(p0);
    throw new java.awt.HeadlessException();
  }
  public Dialog(java.awt.Frame p0, java.lang.String p1, boolean p2, java.awt.GraphicsConfiguration p3) {
    super(p0, p3);
    throw new java.awt.HeadlessException();
  }
  protected java.lang.String paramString() { throw new UnsupportedOperationException(); }
  public javax.accessibility.AccessibleContext getAccessibleContext() { throw new UnsupportedOperationException(); }
  public void addNotify() { throw new UnsupportedOperationException(); }
  public void setVisible(boolean p0) { throw new UnsupportedOperationException(); }
  public void show() { throw new UnsupportedOperationException(); }
  public void hide() { throw new UnsupportedOperationException(); }
  public void setBackground(java.awt.Color p0) { throw new UnsupportedOperationException(); }
  public void toBack() { throw new UnsupportedOperationException(); }
  public void setOpacity(float p0) { throw new UnsupportedOperationException(); }
  public void setShape(java.awt.Shape p0) { throw new UnsupportedOperationException(); }
  public void setModalityType(java.awt.Dialog.ModalityType p0) { throw new UnsupportedOperationException(); }
  public void setTitle(java.lang.String p0) { throw new UnsupportedOperationException(); }
  public boolean isModal() { throw new UnsupportedOperationException(); }
  public void setResizable(boolean p0) { throw new UnsupportedOperationException(); }
  public boolean isUndecorated() { throw new UnsupportedOperationException(); }
  public java.awt.Dialog.ModalityType getModalityType() { throw new UnsupportedOperationException(); }
  public void setModal(boolean p0) { throw new UnsupportedOperationException(); }
  public java.lang.String getTitle() { throw new UnsupportedOperationException(); }
  public boolean isResizable() { throw new UnsupportedOperationException(); }
  public void setUndecorated(boolean p0) { throw new UnsupportedOperationException(); }

  public enum ModalityType {
    MODELESS, DOCUMENT_MODAL, APPLICATION_MODAL, TOOLKIT_MODAL;
  }

  public enum ModalExclusionType {
    NO_EXCLUDE, APPLICATION_EXCLUDE, TOOLKIT_EXCLUDE;
  }
}
