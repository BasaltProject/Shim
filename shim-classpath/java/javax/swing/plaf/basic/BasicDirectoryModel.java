/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf.basic;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.AbstractListModel;
import javax.swing.JFileChooser;
import javax.swing.event.ListDataEvent;

public class BasicDirectoryModel extends AbstractListModel<Object> implements PropertyChangeListener {
  private final JFileChooser filechooser;
  private final Vector<File> directories = new Vector<File>();
  private final Vector<File> files = new Vector<File>();
  private final CopyOnWriteArrayList<PropertyChangeListener> propertyListeners =
      new CopyOnWriteArrayList<PropertyChangeListener>();

  public BasicDirectoryModel(JFileChooser filechooser) {
    this.filechooser = filechooser;
    validateFileCache();
  }

  public void propertyChange(PropertyChangeEvent e) {
    String prop = e.getPropertyName();
    if (JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(prop) || JFileChooser.FILE_VIEW_CHANGED_PROPERTY.equals(prop)
        || JFileChooser.FILE_FILTER_CHANGED_PROPERTY.equals(prop)
        || JFileChooser.FILE_HIDING_CHANGED_PROPERTY.equals(prop)
        || JFileChooser.FILE_SELECTION_MODE_CHANGED_PROPERTY.equals(prop)
        || JFileChooser.MULTI_SELECTION_ENABLED_CHANGED_PROPERTY.equals(prop)
        || JFileChooser.FILE_SYSTEM_VIEW_CHANGED_PROPERTY.equals(prop)) {
      validateFileCache();
    }
  }

  public int getSize() {
    return directories.size() + files.size();
  }

  public boolean contains(Object o) {
    return directories.contains(o) || files.contains(o);
  }

  public int indexOf(Object o) {
    int index = files.indexOf(o);
    if (index >= 0) {
      return index + directories.size();
    }
    return directories.indexOf(o);
  }

  public Object getElementAt(int index) {
    if (index < directories.size()) {
      return directories.get(index);
    }
    return files.get(index - directories.size());
  }

  public void intervalAdded(ListDataEvent e) { }

  public void intervalRemoved(ListDataEvent e) { }

  protected void sort(Vector<? extends File> v) {
    Collections.sort(v, new Comparator<File>() {
      public int compare(File a, File b) {
        return a == b ? 0 : lt(a, b) ? -1 : lt(b, a) ? 1 : 0;
      }
    });
  }

  protected boolean lt(File a, File b) {
    int diff = a.getName().compareToIgnoreCase(b.getName());
    if (diff != 0) {
      return diff < 0;
    }
    return a.getName().compareTo(b.getName()) < 0;
  }

  public Vector<File> getDirectories() {
    return directories;
  }

  public Vector<File> getFiles() {
    return files;
  }

  public void fireContentsChanged() {
    fireContentsChanged(this, 0, getSize() - 1);
  }

  public void invalidateFileCache() { }

  public void validateFileCache() {
    File currentDirectory = filechooser.getCurrentDirectory();
    if (currentDirectory == null) {
      return;
    }
    int oldSize = getSize();
    directories.clear();
    files.clear();
    boolean fileSelectionEnabled = filechooser.isFileSelectionEnabled();
    File[] list = filechooser.getFileSystemView().getFiles(currentDirectory, filechooser.isFileHidingEnabled());
    for (File f : list) {
      if (!filechooser.accept(f)) {
        continue;
      }
      if (filechooser.isTraversable(f)) {
        directories.add(f);
      } else if (fileSelectionEnabled) {
        files.add(f);
      }
    }
    sort(directories);
    sort(files);
    if (oldSize > 0) {
      fireIntervalRemoved(this, 0, oldSize - 1);
    }
    if (getSize() > 0) {
      fireIntervalAdded(this, 0, getSize() - 1);
    }
    firePropertyChange("fileCount", Integer.valueOf(oldSize), Integer.valueOf(getSize()));
  }

  public boolean renameFile(File oldFile, File newFile) {
    synchronized (this) {
      if (oldFile.renameTo(newFile)) {
        validateFileCache();
        return true;
      }
      return false;
    }
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    if (listener != null) {
      propertyListeners.add(listener);
    }
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    propertyListeners.remove(listener);
  }

  public PropertyChangeListener[] getPropertyChangeListeners() {
    return propertyListeners.toArray(new PropertyChangeListener[0]);
  }

  protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
    if (oldValue != null && oldValue.equals(newValue)) {
      return;
    }
    PropertyChangeEvent event = new PropertyChangeEvent(this, propertyName, oldValue, newValue);
    for (PropertyChangeListener l : propertyListeners) {
      l.propertyChange(event);
    }
  }
}
