/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.accessibility;

public interface AccessibleTable {
  javax.accessibility.Accessible getAccessibleCaption();
  void setAccessibleCaption(javax.accessibility.Accessible p0);
  javax.accessibility.Accessible getAccessibleSummary();
  void setAccessibleSummary(javax.accessibility.Accessible p0);
  int getAccessibleRowCount();
  int getAccessibleColumnCount();
  int getAccessibleRowExtentAt(int p0, int p1);
  int getAccessibleColumnExtentAt(int p0, int p1);
  javax.accessibility.AccessibleTable getAccessibleRowHeader();
  void setAccessibleRowHeader(javax.accessibility.AccessibleTable p0);
  javax.accessibility.AccessibleTable getAccessibleColumnHeader();
  void setAccessibleColumnHeader(javax.accessibility.AccessibleTable p0);
  javax.accessibility.Accessible getAccessibleRowDescription(int p0);
  void setAccessibleRowDescription(int p0, javax.accessibility.Accessible p1);
  javax.accessibility.Accessible getAccessibleColumnDescription(int p0);
  void setAccessibleColumnDescription(int p0, javax.accessibility.Accessible p1);
  boolean isAccessibleSelected(int p0, int p1);
  boolean isAccessibleRowSelected(int p0);
  boolean isAccessibleColumnSelected(int p0);
  int[] getSelectedAccessibleRows();
  int[] getSelectedAccessibleColumns();
  javax.accessibility.Accessible getAccessibleAt(int p0, int p1);
}
