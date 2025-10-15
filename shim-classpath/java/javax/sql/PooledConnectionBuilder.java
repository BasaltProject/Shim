/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sql;

public interface PooledConnectionBuilder {
  PooledConnectionBuilder user(String p0);

  PooledConnectionBuilder password(String p0);

  PooledConnectionBuilder shardingKey(java.sql.ShardingKey p0);

  PooledConnectionBuilder superShardingKey(java.sql.ShardingKey p0);

  PooledConnection build() throws java.sql.SQLException;
}
