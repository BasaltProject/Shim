/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.relation;

public interface RelationType extends java.io.Serializable {
  java.lang.String getRelationTypeName();
  javax.management.relation.RoleInfo getRoleInfo(java.lang.String string) throws java.lang.IllegalArgumentException, javax.management.relation.RoleInfoNotFoundException;
  java.util.List<javax.management.relation.RoleInfo> getRoleInfos();
}
