/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.desktop;

public final class PrintFilesEvent extends java.awt.desktop.FilesEvent {
  public PrintFilesEvent(java.util.List<java.io.File> files) {
    super(files);
  }
}
