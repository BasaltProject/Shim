/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sql.rowset.spi;

public interface SyncResolver extends javax.sql.RowSet {
  int UPDATE_ROW_CONFLICT = 0;
  int DELETE_ROW_CONFLICT = 1;
  int INSERT_ROW_CONFLICT = 2;
  int NO_ROW_CONFLICT = 3;

  java.lang.Object getConflictValue(java.lang.String string) throws java.sql.SQLException;
  java.lang.Object getConflictValue(int i) throws java.sql.SQLException;
  int getStatus();
  boolean nextConflict() throws java.sql.SQLException;
  boolean previousConflict() throws java.sql.SQLException;
  void setResolvedValue(java.lang.String string, java.lang.Object object) throws java.sql.SQLException;
  void setResolvedValue(int i, java.lang.Object object) throws java.sql.SQLException;
}
