/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sql.rowset;

public interface Predicate {
  boolean evaluate(java.lang.Object object, java.lang.String string) throws java.sql.SQLException;
  boolean evaluate(java.lang.Object object, int i) throws java.sql.SQLException;
  boolean evaluate(javax.sql.RowSet rowSet);
}
