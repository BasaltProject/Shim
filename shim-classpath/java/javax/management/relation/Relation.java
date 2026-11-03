/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.relation;

public interface Relation {
  javax.management.relation.RoleResult getAllRoles() throws javax.management.relation.RelationServiceNotRegisteredException;
  java.util.Map<javax.management.ObjectName, java.util.List<java.lang.String>> getReferencedMBeans();
  java.lang.String getRelationId();
  javax.management.ObjectName getRelationServiceName();
  java.lang.String getRelationTypeName();
  java.util.List<javax.management.ObjectName> getRole(java.lang.String string) throws java.lang.IllegalArgumentException, javax.management.relation.RoleNotFoundException, javax.management.relation.RelationServiceNotRegisteredException;
  java.lang.Integer getRoleCardinality(java.lang.String string) throws java.lang.IllegalArgumentException, javax.management.relation.RoleNotFoundException;
  javax.management.relation.RoleResult getRoles(java.lang.String[] strings) throws java.lang.IllegalArgumentException, javax.management.relation.RelationServiceNotRegisteredException;
  void handleMBeanUnregistration(javax.management.ObjectName objectName, java.lang.String string) throws java.lang.IllegalArgumentException, javax.management.relation.RoleNotFoundException, javax.management.relation.InvalidRoleValueException, javax.management.relation.RelationServiceNotRegisteredException, javax.management.relation.RelationTypeNotFoundException, javax.management.relation.RelationNotFoundException;
  javax.management.relation.RoleList retrieveAllRoles();
  void setRole(javax.management.relation.Role role) throws java.lang.IllegalArgumentException, javax.management.relation.RoleNotFoundException, javax.management.relation.RelationTypeNotFoundException, javax.management.relation.InvalidRoleValueException, javax.management.relation.RelationServiceNotRegisteredException, javax.management.relation.RelationNotFoundException;
  javax.management.relation.RoleResult setRoles(javax.management.relation.RoleList roleList) throws java.lang.IllegalArgumentException, javax.management.relation.RelationServiceNotRegisteredException, javax.management.relation.RelationTypeNotFoundException, javax.management.relation.RelationNotFoundException;
}
