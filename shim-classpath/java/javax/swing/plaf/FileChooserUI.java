/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf;

public abstract class FileChooserUI extends javax.swing.plaf.ComponentUI {
  protected FileChooserUI() {
  }
  public abstract javax.swing.filechooser.FileFilter getAcceptAllFileFilter(javax.swing.JFileChooser p0);
  public abstract void ensureFileIsVisible(javax.swing.JFileChooser p0, java.io.File p1);
  public abstract void rescanCurrentDirectory(javax.swing.JFileChooser p0);
  public abstract java.lang.String getDialogTitle(javax.swing.JFileChooser p0);
  public abstract javax.swing.filechooser.FileView getFileView(javax.swing.JFileChooser p0);
  public abstract java.lang.String getApproveButtonText(javax.swing.JFileChooser p0);
  public javax.swing.JButton getDefaultButton(javax.swing.JFileChooser p0) { throw new UnsupportedOperationException(); }
}
