/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sql.rowset.spi;

public interface TransactionalWriter extends javax.sql.RowSetWriter {
  void commit() throws java.sql.SQLException;
  void rollback() throws java.sql.SQLException;
  void rollback(java.sql.Savepoint savepoint) throws java.sql.SQLException;
}
