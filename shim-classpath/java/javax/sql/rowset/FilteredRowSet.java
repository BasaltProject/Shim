/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sql.rowset;

public interface FilteredRowSet extends javax.sql.rowset.WebRowSet {
  javax.sql.rowset.Predicate getFilter();
  void setFilter(javax.sql.rowset.Predicate predicate) throws java.sql.SQLException;
}
