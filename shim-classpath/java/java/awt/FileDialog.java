/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

public class FileDialog extends java.awt.Dialog {
  public static final int LOAD = 0;
  public static final int SAVE = 1;
  public FileDialog(java.awt.Dialog p0, java.lang.String p1, int p2) {
    super(p0, p1);
    throw new java.awt.HeadlessException();
  }
  public FileDialog(java.awt.Dialog p0, java.lang.String p1) {
    super(p0, p1);
    throw new java.awt.HeadlessException();
  }
  public FileDialog(java.awt.Dialog p0) {
    super(p0);
    throw new java.awt.HeadlessException();
  }
  public FileDialog(java.awt.Frame p0) {
    super(p0);
    throw new java.awt.HeadlessException();
  }
  public FileDialog(java.awt.Frame p0, java.lang.String p1) {
    super(p0, p1);
    throw new java.awt.HeadlessException();
  }
  public FileDialog(java.awt.Frame p0, java.lang.String p1, int p2) {
    super(p0, p1);
    throw new java.awt.HeadlessException();
  }
  protected java.lang.String paramString() { throw new UnsupportedOperationException(); }
  public java.lang.String getFile() { throw new UnsupportedOperationException(); }
  public void setMode(int p0) { throw new UnsupportedOperationException(); }
  public void setDirectory(java.lang.String p0) { throw new UnsupportedOperationException(); }
  public void setFile(java.lang.String p0) { throw new UnsupportedOperationException(); }
  public void setFilenameFilter(java.io.FilenameFilter p0) { throw new UnsupportedOperationException(); }
  public int getMode() { throw new UnsupportedOperationException(); }
  public java.lang.String getDirectory() { throw new UnsupportedOperationException(); }
  public java.io.File[] getFiles() { throw new UnsupportedOperationException(); }
  public void setMultipleMode(boolean p0) { throw new UnsupportedOperationException(); }
  public boolean isMultipleMode() { throw new UnsupportedOperationException(); }
  public java.io.FilenameFilter getFilenameFilter() { throw new UnsupportedOperationException(); }
  public void addNotify() { throw new UnsupportedOperationException(); }
  public void setTitle(java.lang.String p0) { throw new UnsupportedOperationException(); }
}
