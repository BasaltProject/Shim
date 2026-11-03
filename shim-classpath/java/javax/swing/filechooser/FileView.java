/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.filechooser;

import java.io.File;
import javax.swing.Icon;

public abstract class FileView {
  protected FileView() { }

  public String getName(File f) { return null; }

  public String getDescription(File f) { return null; }

  public String getTypeDescription(File f) { return null; }

  public Icon getIcon(File f) { return null; }

  public Boolean isTraversable(File f) { return null; }
}
