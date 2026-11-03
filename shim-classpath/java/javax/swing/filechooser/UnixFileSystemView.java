/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.filechooser;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

final class UnixFileSystemView extends FileSystemView {
  private static final String newFolderString = "NewFolder";
  private static final String newFolderNextString = "NewFolder.{0}";

  public File createNewFolder(File containingDir) throws IOException {
    if (containingDir == null) {
      throw new IOException("Containing directory is null:");
    }
    File newFolder = createFileObject(containingDir, newFolderString);
    int i = 1;
    while (newFolder.exists() && i < 100) {
      newFolder = createFileObject(containingDir, MessageFormat.format(newFolderNextString, i));
      i++;
    }
    if (newFolder.exists()) {
      throw new IOException("Directory already exists:" + newFolder.getAbsolutePath());
    } else {
      newFolder.mkdirs();
    }
    return newFolder;
  }

  public boolean isFileSystemRoot(File dir) { return dir != null && dir.getAbsolutePath().equals("/"); }

  public boolean isDrive(File dir) { return isFloppyDrive(dir); }

  public boolean isFloppyDrive(File dir) { return false; }

  public boolean isComputerNode(File dir) {
    if (dir != null) {
      String parent = dir.getParent();
      if (parent != null && parent.equals("/net")) {
        return true;
      }
    }
    return false;
  }

  public String getSystemDisplayName(File f) {
    if (f == null) {
      return null;
    }
    String name = f.getName();
    if (name.length() == 0) {
      return f.getPath();
    }
    return name;
  }
}
