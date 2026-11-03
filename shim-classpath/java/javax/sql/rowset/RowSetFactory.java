/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sql.rowset;

public interface RowSetFactory {
  javax.sql.rowset.CachedRowSet createCachedRowSet() throws java.sql.SQLException;
  javax.sql.rowset.FilteredRowSet createFilteredRowSet() throws java.sql.SQLException;
  javax.sql.rowset.JdbcRowSet createJdbcRowSet() throws java.sql.SQLException;
  javax.sql.rowset.JoinRowSet createJoinRowSet() throws java.sql.SQLException;
  javax.sql.rowset.WebRowSet createWebRowSet() throws java.sql.SQLException;
}
