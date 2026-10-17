/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sql;

public interface XAConnectionBuilder {
  XAConnectionBuilder user(String p0);

  XAConnectionBuilder password(String p0);

  XAConnectionBuilder shardingKey(java.sql.ShardingKey p0);

  XAConnectionBuilder superShardingKey(java.sql.ShardingKey p0);

  XAConnection build() throws java.sql.SQLException;
}
