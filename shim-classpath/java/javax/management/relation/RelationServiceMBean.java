/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.relation;

public interface RelationServiceMBean {
  void addRelation(javax.management.ObjectName objectName) throws java.lang.IllegalArgumentException, javax.management.relation.RelationServiceNotRegisteredException, java.lang.NoSuchMethodException, javax.management.relation.InvalidRelationIdException, javax.management.InstanceNotFoundException, javax.management.relation.InvalidRelationServiceException, javax.management.relation.RelationTypeNotFoundException, javax.management.relation.RoleNotFoundException, javax.management.relation.InvalidRoleValueException;
  void addRelationType(javax.management.relation.RelationType relationType) throws java.lang.IllegalArgumentException, javax.management.relation.InvalidRelationTypeException;
  java.lang.Integer checkRoleReading(java.lang.String string, java.lang.String string2) throws java.lang.IllegalArgumentException, javax.management.relation.RelationTypeNotFoundException;
  java.lang.Integer checkRoleWriting(javax.management.relation.Role role, java.lang.String string, java.lang.Boolean p2) throws java.lang.IllegalArgumentException, javax.management.relation.RelationTypeNotFoundException;
  void createRelation(java.lang.String string, java.lang.String string2, javax.management.relation.RoleList roleList) throws javax.management.relation.RelationServiceNotRegisteredException, java.lang.IllegalArgumentException, javax.management.relation.RoleNotFoundException, javax.management.relation.InvalidRelationIdException, javax.management.relation.RelationTypeNotFoundException, javax.management.relation.InvalidRoleValueException;
  void createRelationType(java.lang.String string, javax.management.relation.RoleInfo[] roleInfos) throws java.lang.IllegalArgumentException, javax.management.relation.InvalidRelationTypeException;
  java.util.Map<javax.management.ObjectName, java.util.List<java.lang.String>> findAssociatedMBeans(javax.management.ObjectName objectName, java.lang.String string, java.lang.String string2) throws java.lang.IllegalArgumentException;
  java.util.Map<java.lang.String, java.util.List<java.lang.String>> findReferencingRelations(javax.management.ObjectName objectName, java.lang.String string, java.lang.String string2) throws java.lang.IllegalArgumentException;
  java.util.List<java.lang.String> findRelationsOfType(java.lang.String string) throws java.lang.IllegalArgumentException, javax.management.relation.RelationTypeNotFoundException;
  java.util.List<java.lang.String> getAllRelationIds();
  java.util.List<java.lang.String> getAllRelationTypeNames();
  javax.management.relation.RoleResult getAllRoles(java.lang.String string) throws java.lang.IllegalArgumentException, javax.management.relation.RelationNotFoundException, javax.management.relation.RelationServiceNotRegisteredException;
  boolean getPurgeFlag();
  java.util.Map<javax.management.ObjectName, java.util.List<java.lang.String>> getReferencedMBeans(java.lang.String string) throws java.lang.IllegalArgumentException, javax.management.relation.RelationNotFoundException;
  java.lang.String getRelationTypeName(java.lang.String string) throws java.lang.IllegalArgumentException, javax.management.relation.RelationNotFoundException;
  java.util.List<javax.management.ObjectName> getRole(java.lang.String string, java.lang.String string2) throws javax.management.relation.RelationServiceNotRegisteredException, java.lang.IllegalArgumentException, javax.management.relation.RelationNotFoundException, javax.management.relation.RoleNotFoundException;
  java.lang.Integer getRoleCardinality(java.lang.String string, java.lang.String string2) throws java.lang.IllegalArgumentException, javax.management.relation.RelationNotFoundException, javax.management.relation.RoleNotFoundException;
  javax.management.relation.RoleInfo getRoleInfo(java.lang.String string, java.lang.String string2) throws java.lang.IllegalArgumentException, javax.management.relation.RelationTypeNotFoundException, javax.management.relation.RoleInfoNotFoundException;
  java.util.List<javax.management.relation.RoleInfo> getRoleInfos(java.lang.String string) throws java.lang.IllegalArgumentException, javax.management.relation.RelationTypeNotFoundException;
  javax.management.relation.RoleResult getRoles(java.lang.String string, java.lang.String[] strings) throws javax.management.relation.RelationServiceNotRegisteredException, java.lang.IllegalArgumentException, javax.management.relation.RelationNotFoundException;
  java.lang.Boolean hasRelation(java.lang.String string) throws java.lang.IllegalArgumentException;
  void isActive() throws javax.management.relation.RelationServiceNotRegisteredException;
  java.lang.String isRelation(javax.management.ObjectName objectName) throws java.lang.IllegalArgumentException;
  javax.management.ObjectName isRelationMBean(java.lang.String string) throws java.lang.IllegalArgumentException, javax.management.relation.RelationNotFoundException;
  void purgeRelations() throws javax.management.relation.RelationServiceNotRegisteredException;
  void removeRelation(java.lang.String string) throws javax.management.relation.RelationServiceNotRegisteredException, java.lang.IllegalArgumentException, javax.management.relation.RelationNotFoundException;
  void removeRelationType(java.lang.String string) throws javax.management.relation.RelationServiceNotRegisteredException, java.lang.IllegalArgumentException, javax.management.relation.RelationTypeNotFoundException;
  void sendRelationCreationNotification(java.lang.String string) throws java.lang.IllegalArgumentException, javax.management.relation.RelationNotFoundException;
  void sendRelationRemovalNotification(java.lang.String string, java.util.List<javax.management.ObjectName> list) throws java.lang.IllegalArgumentException, javax.management.relation.RelationNotFoundException;
  void sendRoleUpdateNotification(java.lang.String string, javax.management.relation.Role role, java.util.List<javax.management.ObjectName> list) throws java.lang.IllegalArgumentException, javax.management.relation.RelationNotFoundException;
  void setPurgeFlag(boolean flag);
  void setRole(java.lang.String string, javax.management.relation.Role role) throws javax.management.relation.RelationServiceNotRegisteredException, java.lang.IllegalArgumentException, javax.management.relation.RelationNotFoundException, javax.management.relation.RoleNotFoundException, javax.management.relation.InvalidRoleValueException, javax.management.relation.RelationTypeNotFoundException;
  javax.management.relation.RoleResult setRoles(java.lang.String string, javax.management.relation.RoleList roleList) throws javax.management.relation.RelationServiceNotRegisteredException, java.lang.IllegalArgumentException, javax.management.relation.RelationNotFoundException;
  void updateRoleMap(java.lang.String string, javax.management.relation.Role role, java.util.List<javax.management.ObjectName> list) throws java.lang.IllegalArgumentException, javax.management.relation.RelationServiceNotRegisteredException, javax.management.relation.RelationNotFoundException;
}
