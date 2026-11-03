/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.tree;

import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EventListener;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import javax.swing.DefaultListSelectionModel;
import javax.swing.event.EventListenerList;
import javax.swing.event.SwingPropertyChangeSupport;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

public class DefaultTreeSelectionModel implements Cloneable, Serializable, TreeSelectionModel {
  public static final String SELECTION_MODE_PROPERTY = "selectionMode";

  protected SwingPropertyChangeSupport changeSupport;
  protected TreePath[] selection;
  protected EventListenerList listenerList = new EventListenerList();
  protected transient RowMapper rowMapper;
  protected DefaultListSelectionModel listSelectionModel;
  protected int selectionMode;
  protected TreePath leadPath;
  protected int leadIndex;
  protected int leadRow;

  private Set<TreePath> chosen = new LinkedHashSet<TreePath>();

  public DefaultTreeSelectionModel() {
    listSelectionModel = new DefaultListSelectionModel();
    selectionMode = DISCONTIGUOUS_TREE_SELECTION;
    leadIndex = leadRow = -1;
  }

  public void setRowMapper(RowMapper newMapper) {
    rowMapper = newMapper;
    resetRowSelection();
  }

  public RowMapper getRowMapper() { return rowMapper; }

  public void setSelectionMode(int mode) {
    int oldMode = selectionMode;
    selectionMode = mode;
    if (selectionMode != TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION
        && selectionMode != TreeSelectionModel.CONTIGUOUS_TREE_SELECTION
        && selectionMode != TreeSelectionModel.SINGLE_TREE_SELECTION) {
      selectionMode = TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION;
    }
    if (oldMode != selectionMode && changeSupport != null) {
      changeSupport.firePropertyChange(SELECTION_MODE_PROPERTY, Integer.valueOf(oldMode),
          Integer.valueOf(selectionMode));
    }
  }

  public int getSelectionMode() { return selectionMode; }

  public void setSelectionPath(TreePath path) {
    if (path == null) {
      setSelectionPaths(null);
    } else {
      setSelectionPaths(new TreePath[] {path});
    }
  }

  public void setSelectionPaths(TreePath[] pPaths) {
    TreePath[] paths = pPaths;
    int newCount = paths == null ? 0 : paths.length;
    int oldCount = selection == null ? 0 : selection.length;
    if (newCount + oldCount == 0) {
      return;
    }
    if (selectionMode == SINGLE_TREE_SELECTION) {
      if (newCount > 1) {
        paths = new TreePath[] {pPaths[0]};
        newCount = 1;
      }
    } else if (selectionMode == CONTIGUOUS_TREE_SELECTION) {
      if (newCount > 0 && !arePathsContiguous(paths)) {
        paths = new TreePath[] {pPaths[0]};
        newCount = 1;
      }
    }
    TreePath beginLead = leadPath;
    List<Change> changes = new ArrayList<Change>();
    Set<TreePath> fresh = new LinkedHashSet<TreePath>();
    leadPath = null;
    for (int i = 0; i < newCount; i++) {
      TreePath path = paths[i];
      if (path != null && fresh.add(path)) {
        if (!chosen.contains(path)) {
          changes.add(new Change(path, true));
        }
        leadPath = path;
      }
    }
    for (int i = 0; i < oldCount; i++) {
      if (selection[i] != null && !fresh.contains(selection[i])) {
        changes.add(new Change(selection[i], false));
      }
    }
    chosen = fresh;
    selection = fresh.toArray(new TreePath[fresh.size()]);
    updateLeadIndex();
    resetRowSelection();
    if (!changes.isEmpty()) {
      notifyChanges(changes, beginLead);
    }
  }

  public void addSelectionPath(TreePath path) {
    if (path != null) {
      addSelectionPaths(new TreePath[] {path});
    }
  }

  public void addSelectionPaths(TreePath[] paths) {
    int newCount = paths == null ? 0 : paths.length;
    if (newCount == 0) {
      return;
    }
    if (selectionMode == SINGLE_TREE_SELECTION) {
      setSelectionPath(paths[0]);
      return;
    }
    if (selectionMode == CONTIGUOUS_TREE_SELECTION && !canPathsBeAdded(paths)) {
      if (arePathsContiguous(paths)) {
        setSelectionPaths(paths);
      } else {
        setSelectionPaths(new TreePath[] {paths[0]});
      }
      return;
    }
    TreePath beginLead = leadPath;
    List<Change> changes = new ArrayList<Change>();
    for (TreePath path : paths) {
      if (path != null && !chosen.contains(path)) {
        changes.add(new Change(path, true));
        chosen.add(path);
      }
    }
    leadPath = paths[newCount - 1];
    if (changes.isEmpty()) {
      return;
    }
    selection = chosen.toArray(new TreePath[chosen.size()]);
    updateLeadIndex();
    resetRowSelection();
    notifyChanges(changes, beginLead);
  }

  public void removeSelectionPath(TreePath path) {
    if (path != null) {
      removeSelectionPaths(new TreePath[] {path});
    }
  }

  public void removeSelectionPaths(TreePath[] paths) {
    if (paths == null || paths.length == 0 || selection == null || selection.length == 0) {
      return;
    }
    if (!canPathsBeRemoved(paths)) {
      List<TreePath> remaining = new ArrayList<TreePath>(chosen);
      for (TreePath p : paths) {
        remaining.remove(p);
      }
      setSelectionPaths(firstContiguousRun(remaining));
      return;
    }
    TreePath beginLead = leadPath;
    List<Change> changes = new ArrayList<Change>();
    for (TreePath p : paths) {
      if (p != null && chosen.remove(p)) {
        changes.add(new Change(p, false));
      }
    }
    if (changes.isEmpty()) {
      return;
    }
    selection = chosen.isEmpty() ? null : chosen.toArray(new TreePath[chosen.size()]);
    if (leadPath != null && !chosen.contains(leadPath)) {
      leadPath = chosen.isEmpty() ? null : selection[selection.length - 1];
    }
    updateLeadIndex();
    resetRowSelection();
    notifyChanges(changes, beginLead);
  }

  private TreePath[] firstContiguousRun(List<TreePath> remaining) {
    if (rowMapper == null || remaining.isEmpty()) {
      return remaining.toArray(new TreePath[0]);
    }
    TreePath[] arr = remaining.toArray(new TreePath[0]);
    int[] rows = rowMapper.getRowsForPaths(arr);
    Integer[] order = new Integer[arr.length];
    for (int i = 0; i < order.length; i++) {
      order[i] = i;
    }
    final int[] r = rows;
    Arrays.sort(order, new Comparator<Integer>() {
      public int compare(Integer a, Integer b) { return r[a] - r[b]; }
    });
    List<TreePath> run = new ArrayList<TreePath>();
    int prev = Integer.MIN_VALUE;
    for (int idx : order) {
      if (!run.isEmpty() && rows[idx] != prev + 1) {
        break;
      }
      run.add(arr[idx]);
      prev = rows[idx];
    }
    return run.toArray(new TreePath[0]);
  }

  public TreePath getSelectionPath() { return selection != null && selection.length > 0 ? selection[0] : null; }

  public TreePath[] getSelectionPaths() {
    if (selection != null) {
      return selection.clone();
    }
    return new TreePath[0];
  }

  public int getSelectionCount() { return (selection == null) ? 0 : selection.length; }

  public boolean isPathSelected(TreePath path) { return (path != null) && chosen.contains(path); }

  public boolean isSelectionEmpty() { return (selection == null || selection.length == 0); }

  public void clearSelection() {
    if (selection != null && selection.length > 0) {
      List<Change> changes = new ArrayList<Change>();
      for (TreePath p : selection) {
        changes.add(new Change(p, false));
      }
      selection = null;
      chosen = new LinkedHashSet<TreePath>();
      TreePath beginLead = leadPath;
      leadPath = null;
      leadIndex = leadRow = -1;
      resetRowSelection();
      notifyChanges(changes, beginLead);
    }
  }

  public TreePath getLeadSelectionPath() { return leadPath; }

  public int getLeadSelectionRow() { return leadRow; }

  public int getMinSelectionRow() { return listSelectionModel.getMinSelectionIndex(); }

  public int getMaxSelectionRow() { return listSelectionModel.getMaxSelectionIndex(); }

  public boolean isRowSelected(int row) { return listSelectionModel.isSelectedIndex(row); }

  public int[] getSelectionRows() {
    if (rowMapper != null && selection != null && selection.length != 0) {
      int[] rows = rowMapper.getRowsForPaths(selection);
      if (rows != null) {
        int invisCount = 0;
        for (int counter = rows.length - 1; counter >= 0; counter--) {
          if (rows[counter] == -1) {
            invisCount++;
          }
        }
        if (invisCount > 0) {
          if (invisCount == rows.length) {
            rows = null;
          } else {
            int[] tempRows = new int[rows.length - invisCount];
            for (int counter = 0, visCounter = 0; counter < rows.length; counter++) {
              if (rows[counter] != -1) {
                tempRows[visCounter++] = rows[counter];
              }
            }
            rows = tempRows;
          }
        }
      }
      return rows;
    }
    return new int[0];
  }

  public void resetRowSelection() {
    listSelectionModel.clearSelection();
    if (selection != null && rowMapper != null) {
      int[] rows = rowMapper.getRowsForPaths(selection);
      for (int row : rows) {
        if (row != -1) {
          listSelectionModel.addSelectionInterval(row, row);
        }
      }
      if (leadIndex != -1 && rows != null && leadIndex < rows.length) {
        leadRow = rows[leadIndex];
      } else if (leadPath != null) {
        TreePath[] tempPaths = new TreePath[] {leadPath};
        int[] tempRows = rowMapper.getRowsForPaths(tempPaths);
        leadRow = tempRows != null ? tempRows[0] : -1;
      } else {
        leadRow = -1;
      }
      listSelectionModel.moveLeadSelectionIndex(leadRow);
    } else {
      leadRow = -1;
    }
  }

  public void addTreeSelectionListener(TreeSelectionListener x) { listenerList.add(TreeSelectionListener.class, x); }

  public void removeTreeSelectionListener(TreeSelectionListener x) {
    listenerList.remove(TreeSelectionListener.class, x);
  }

  public TreeSelectionListener[] getTreeSelectionListeners() {
    return listenerList.getListeners(TreeSelectionListener.class);
  }

  protected void fireValueChanged(TreeSelectionEvent e) {
    Object[] listeners = listenerList.getListenerList();
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == TreeSelectionListener.class) {
        ((TreeSelectionListener) listeners[i + 1]).valueChanged(e);
      }
    }
  }

  public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
    return listenerList.getListeners(listenerType);
  }

  public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
    if (changeSupport == null) {
      changeSupport = new SwingPropertyChangeSupport(this);
    }
    changeSupport.addPropertyChangeListener(listener);
  }

  public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
    if (changeSupport == null) {
      return;
    }
    changeSupport.removePropertyChangeListener(listener);
  }

  public PropertyChangeListener[] getPropertyChangeListeners() {
    if (changeSupport == null) {
      return new PropertyChangeListener[0];
    }
    return changeSupport.getPropertyChangeListeners();
  }

  protected boolean arePathsContiguous(TreePath[] paths) {
    if (rowMapper == null || paths.length < 2) {
      return true;
    }
    int[] rows = rowMapper.getRowsForPaths(paths);
    if (rows == null) {
      return true;
    }
    int[] sorted = rows.clone();
    Arrays.sort(sorted);
    if (sorted[0] == -1) {
      return false;
    }
    for (int i = 1; i < sorted.length; i++) {
      if (sorted[i] != sorted[i - 1] + 1) {
        return false;
      }
    }
    return true;
  }

  protected boolean canPathsBeAdded(TreePath[] paths) {
    if (paths == null || paths.length == 0 || rowMapper == null || selection == null
        || selectionMode == DISCONTIGUOUS_TREE_SELECTION) {
      return true;
    }
    TreePath[] combined = new TreePath[paths.length + selection.length];
    System.arraycopy(paths, 0, combined, 0, paths.length);
    System.arraycopy(selection, 0, combined, paths.length, selection.length);
    return arePathsContiguous(combined);
  }

  protected boolean canPathsBeRemoved(TreePath[] paths) {
    if (rowMapper == null || selection == null || selectionMode == DISCONTIGUOUS_TREE_SELECTION) {
      return true;
    }
    List<TreePath> remaining = new ArrayList<TreePath>(chosen);
    for (TreePath p : paths) {
      remaining.remove(p);
    }
    return arePathsContiguous(remaining.toArray(new TreePath[0]));
  }

  protected void notifyPathChange(Vector<?> changedPaths, TreePath oldLeadSelection) {
    int cPathCount = changedPaths.size();
    boolean[] newness = new boolean[cPathCount];
    TreePath[] paths = new TreePath[cPathCount];
    for (int counter = 0; counter < cPathCount; counter++) {
      PathPlaceHolder placeholder = (PathPlaceHolder) changedPaths.elementAt(counter);
      newness[counter] = placeholder.isNew;
      paths[counter] = placeholder.path;
    }
    fireValueChanged(new TreeSelectionEvent(this, paths, newness, oldLeadSelection, leadPath));
  }

  private void notifyChanges(List<Change> changes, TreePath oldLead) {
    Vector<PathPlaceHolder> v = new Vector<PathPlaceHolder>(changes.size());
    for (Change c : changes) {
      v.add(new PathPlaceHolder(c.path, c.isNew));
    }
    notifyPathChange(v, oldLead);
  }

  protected void updateLeadIndex() {
    if (leadPath != null) {
      if (selection == null) {
        leadPath = null;
        leadIndex = leadRow = -1;
      } else {
        leadRow = leadIndex = -1;
        for (int counter = selection.length - 1; counter >= 0; counter--) {
          if (selection[counter].equals(leadPath)) {
            leadIndex = counter;
            break;
          }
        }
      }
    } else {
      leadIndex = -1;
    }
  }

  protected void insureRowContinuity() {
    if (selectionMode == CONTIGUOUS_TREE_SELECTION && selection != null && rowMapper != null) {
      DefaultListSelectionModel lModel = listSelectionModel;
      int min = lModel.getMinSelectionIndex();
      if (min != -1) {
        for (int counter = min, maxCounter = lModel.getMaxSelectionIndex(); counter <= maxCounter; counter++) {
          if (!lModel.isSelectedIndex(counter)) {
            if (counter == min) {
              clearSelection();
            } else {
              TreePath[] newSel = new TreePath[counter - min];
              int[] selectionIndex = rowMapper.getRowsForPaths(selection);
              for (int selCounter = 0; selCounter < selectionIndex.length; selCounter++) {
                if (selectionIndex[selCounter] < counter && selectionIndex[selCounter] >= min) {
                  newSel[selectionIndex[selCounter] - min] = selection[selCounter];
                }
              }
              setSelectionPaths(newSel);
              break;
            }
          }
        }
      }
    } else if (selectionMode == SINGLE_TREE_SELECTION && selection != null && selection.length > 1) {
      setSelectionPath(selection[0]);
    }
  }

  protected void insureUniqueness() { }

  public String toString() {
    int selCount = getSelectionCount();
    StringBuilder retBuffer = new StringBuilder();
    int[] rows;
    if (rowMapper != null) {
      rows = rowMapper.getRowsForPaths(selection);
    } else {
      rows = null;
    }
    retBuffer.append(getClass().getName()).append(" ").append(Integer.toString(hashCode())).append(" [ ");
    for (int counter = 0; counter < selCount; counter++) {
      if (rows != null) {
        retBuffer.append(selection[counter].toString()).append("@").append(Integer.toString(rows[counter])).append(" ");
      } else {
        retBuffer.append(selection[counter].toString()).append(" ");
      }
    }
    retBuffer.append("]");
    return retBuffer.toString();
  }

  public Object clone() throws CloneNotSupportedException {
    DefaultTreeSelectionModel clone = (DefaultTreeSelectionModel) super.clone();
    clone.changeSupport = null;
    if (selection != null) {
      clone.selection = selection.clone();
    }
    clone.chosen = new LinkedHashSet<TreePath>(chosen);
    clone.listenerList = new EventListenerList();
    clone.listSelectionModel = (DefaultListSelectionModel) listSelectionModel.clone();
    return clone;
  }

  private static final class Change {
    final TreePath path;
    final boolean isNew;

    Change(TreePath path, boolean isNew) {
      this.path = path;
      this.isNew = isNew;
    }
  }

  private static final class PathPlaceHolder {
    final TreePath path;
    final boolean isNew;

    PathPlaceHolder(TreePath path, boolean isNew) {
      this.path = path;
      this.isNew = isNew;
    }
  }
}
