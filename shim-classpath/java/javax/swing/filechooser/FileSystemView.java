/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.filechooser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.UIManager;

public abstract class FileSystemView {
  private static FileSystemView unixFileSystemView;

  public static FileSystemView getFileSystemView() {
    synchronized (FileSystemView.class) {
      if (unixFileSystemView == null) {
        unixFileSystemView = new UnixFileSystemView();
      }
      return unixFileSystemView;
    }
  }

  public FileSystemView() { }

  public boolean isRoot(File f) {
    if (f == null || !f.isAbsolute()) {
      return false;
    }
    File[] roots = getRoots();
    for (File root : roots) {
      if (root.equals(f)) {
        return true;
      }
    }
    return false;
  }

  public Boolean isTraversable(File f) { return Boolean.valueOf(f.isDirectory()); }

  public String getSystemDisplayName(File f) {
    if (f == null) {
      return null;
    }
    String name = f.getName();
    if (!name.equals("..") && !name.equals(".") && f.exists() && name.length() == 0) {
      name = f.getPath();
    }
    return name;
  }

  public String getSystemTypeDescription(File f) { return null; }

  public Icon getSystemIcon(File f) {
    if (f == null) {
      return null;
    }
    return UIManager.getIcon(f.isDirectory() ? "FileView.directoryIcon" : "FileView.fileIcon");
  }

  public Icon getSystemIcon(File f, int width, int height) { return getSystemIcon(f); }

  public boolean isParent(File folder, File file) {
    if (folder == null || file == null) {
      return false;
    }
    return folder.equals(file.getParentFile());
  }

  public File getChild(File parent, String fileName) { return createFileObject(parent, fileName); }

  public boolean isFileSystem(File f) { return true; }

  public abstract File createNewFolder(File containingDir) throws IOException;

  public boolean isHiddenFile(File f) { return f.isHidden(); }

  public boolean isFileSystemRoot(File dir) { return isRoot(dir); }

  public boolean isDrive(File dir) { return false; }

  public boolean isFloppyDrive(File dir) { return false; }

  public boolean isComputerNode(File dir) { return false; }

  public File[] getRoots() { return new File[] {createFileSystemRoot(new File("/"))}; }

  public File getHomeDirectory() { return createFileObject(System.getProperty("user.home")); }

  public File getDefaultDirectory() { return createFileObject(System.getProperty("user.home")); }

  public File createFileObject(File dir, String filename) {
    if (dir == null) {
      return new File(filename);
    } else {
      return new File(dir, filename);
    }
  }

  public File createFileObject(String path) {
    File f = new File(path);
    if (isFileSystemRoot(f)) {
      f = createFileSystemRoot(f);
    }
    return f;
  }

  public File[] getFiles(File dir, boolean useFileHiding) {
    List<File> files = new ArrayList<File>();
    File[] names = dir.listFiles();
    if (names == null) {
      return new File[0];
    }
    for (File f : names) {
      if (Thread.currentThread().isInterrupted()) {
        break;
      }
      if (!useFileHiding || !isHiddenFile(f)) {
        files.add(f);
      }
    }
    return files.toArray(new File[files.size()]);
  }

  public File getParentDirectory(File dir) {
    if (dir == null || !dir.exists()) {
      return null;
    }
    File psf = dir.getParentFile();
    if (psf == null) {
      return null;
    }
    if (isFileSystem(psf)) {
      File f = psf;
      if (!f.exists()) {
        File ppsf = psf.getParentFile();
        if (ppsf == null || !isFileSystem(ppsf)) {
          f = createFileSystemRoot(f);
        }
      }
      return f;
    }
    return psf;
  }

  public File[] getChooserComboBoxFiles() { return getRoots(); }

  public final File[] getChooserShortcutPanelFiles() { return new File[] {getHomeDirectory()}; }

  public boolean isLink(File file) { return false; }

  public File getLinkLocation(File file) throws FileNotFoundException { return null; }

  protected File createFileSystemRoot(File f) { return new FileSystemRoot(f); }

  static class FileSystemRoot extends File {
    public FileSystemRoot(File f) { super(f, ""); }

    public FileSystemRoot(String s) { super(s); }

    public boolean isDirectory() { return true; }

    public String getName() { return getPath(); }
  }
}
