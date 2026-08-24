/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.relation;

public interface RelationSupportMBean extends javax.management.relation.Relation {
  java.lang.Boolean isInRelationService();
  void setRelationServiceManagementFlag(java.lang.Boolean p0) throws java.lang.IllegalArgumentException;
}
