/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

public interface ListSelectionModel {
  int SINGLE_SELECTION = 0;
  int SINGLE_INTERVAL_SELECTION = 1;
  int MULTIPLE_INTERVAL_SELECTION = 2;
  void clearSelection();
  int getSelectionMode();
  boolean isSelectionEmpty();
  void setSelectionInterval(int p0, int p1);
  void addSelectionInterval(int p0, int p1);
  void removeSelectionInterval(int p0, int p1);
  int getAnchorSelectionIndex();
  int getLeadSelectionIndex();
  void setSelectionMode(int p0);
  void setAnchorSelectionIndex(int p0);
  void setLeadSelectionIndex(int p0);
  int getMinSelectionIndex();
  default int[] getSelectedIndices() {
    int iMin = getMinSelectionIndex();
    int iMax = getMaxSelectionIndex();
    if ((iMin < 0) || (iMax < 0)) {
      return new int[0];
    }
    int[] rvTmp = new int[1 + (iMax - iMin)];
    int n = 0;
    for (int i = iMin; i <= iMax; i++) {
      if (isSelectedIndex(i)) {
        rvTmp[n++] = i;
      }
    }
    int[] rv = new int[n];
    System.arraycopy(rvTmp, 0, rv, 0, n);
    return rv;
  }
  default int getSelectedItemsCount() {
    if (isSelectionEmpty()) {
      return 0;
    }
    int iMin = getMinSelectionIndex();
    int iMax = getMaxSelectionIndex();
    int count = 0;
    for (int i = iMin; i <= iMax; i++) {
      if (isSelectedIndex(i)) {
        count++;
      }
    }
    return count;
  }
  boolean isSelectedIndex(int p0);
  void removeListSelectionListener(javax.swing.event.ListSelectionListener p0);
  void addListSelectionListener(javax.swing.event.ListSelectionListener p0);
  void insertIndexInterval(int p0, int p1, boolean p2);
  void removeIndexInterval(int p0, int p1);
  int getMaxSelectionIndex();
  void setValueIsAdjusting(boolean p0);
  boolean getValueIsAdjusting();
}
