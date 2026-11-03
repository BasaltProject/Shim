/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.desktop;

import java.io.File;
import java.util.List;

public final class OpenFilesEvent extends FilesEvent {
  private static final long serialVersionUID = -3982871005867718956L;

  final String searchTerm;

  public OpenFilesEvent(List<File> files, String searchTerm) {
    super(files);
    this.searchTerm = searchTerm;
  }

  public String getSearchTerm() { return searchTerm; }
}
