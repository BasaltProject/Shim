/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.text.html;

import java.io.Serializable;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;

class OptionListModel<E> extends DefaultListModel<E> implements ListSelectionModel, Serializable {
  private final DefaultListSelectionModel selection = new DefaultListSelectionModel();
  private int selectionMode = SINGLE_SELECTION;

  public int getMinSelectionIndex() { return selection.getMinSelectionIndex(); }

  public int getMaxSelectionIndex() { return selection.getMaxSelectionIndex(); }

  public boolean isSelectedIndex(int index) { return selection.isSelectedIndex(index); }

  public boolean isSelectionEmpty() { return selection.isSelectionEmpty(); }

  public void setSelectionInterval(int index0, int index1) { selection.setSelectionInterval(index0, index1); }

  public void addSelectionInterval(int index0, int index1) { selection.addSelectionInterval(index0, index1); }

  public void removeSelectionInterval(int index0, int index1) { selection.removeSelectionInterval(index0, index1); }

  public void clearSelection() { selection.clearSelection(); }

  public void insertIndexInterval(int index, int length, boolean before) {
    selection.insertIndexInterval(index, length, before);
  }

  public void removeIndexInterval(int index0, int index1) { selection.removeIndexInterval(index0, index1); }

  public int getAnchorSelectionIndex() { return selection.getAnchorSelectionIndex(); }

  public void setAnchorSelectionIndex(int index) { selection.setAnchorSelectionIndex(index); }

  public int getLeadSelectionIndex() { return selection.getLeadSelectionIndex(); }

  public void setLeadSelectionIndex(int index) { selection.setLeadSelectionIndex(index); }

  public void setValueIsAdjusting(boolean valueIsAdjusting) { selection.setValueIsAdjusting(valueIsAdjusting); }

  public boolean getValueIsAdjusting() { return selection.getValueIsAdjusting(); }

  public int getSelectionMode() { return selectionMode; }

  public void setSelectionMode(int selectionMode) {
    this.selectionMode = selectionMode;
    selection.setSelectionMode(selectionMode);
  }

  public void addListSelectionListener(ListSelectionListener l) { selection.addListSelectionListener(l); }

  public void removeListSelectionListener(ListSelectionListener l) { selection.removeListSelectionListener(l); }
}
