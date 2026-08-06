/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.filechooser;

public abstract class FileFilter {
  protected FileFilter() {
  }
  public abstract boolean accept(java.io.File p0);
  public abstract java.lang.String getDescription();
}
