/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.sql;

public interface ConnectionBuilder {
  ConnectionBuilder user(String p0);

  ConnectionBuilder password(String p0);

  ConnectionBuilder shardingKey(ShardingKey p0);

  ConnectionBuilder superShardingKey(ShardingKey p0);

  Connection build() throws SQLException;
}
