/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sql;

public interface XAConnection extends PooledConnection {
  javax.transaction.xa.XAResource getXAResource() throws java.sql.SQLException;
}
