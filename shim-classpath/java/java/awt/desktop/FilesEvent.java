/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.desktop;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FilesEvent extends AppEvent {
  private static final long serialVersionUID = 5271763715462312871L;

  final List<File> files;

  FilesEvent(List<File> files) {
    super();
    this.files = new ArrayList<File>(files);
  }

  public List<File> getFiles() { return files; }
}
