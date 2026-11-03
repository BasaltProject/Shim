/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.relation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.MBeanServerDelegate;
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

public class RelationService extends NotificationBroadcasterSupport
    implements RelationServiceMBean, MBeanRegistration, NotificationListener {
  private final Map<String, Object> myRelId2ObjMap = new HashMap<String, Object>();
  private final Map<String, String> myRelId2RelTypeMap = new HashMap<String, String>();
  private final Map<String, ObjectName> myRelMBeanObjName2RelIdMap = new HashMap<String, ObjectName>();
  private final Map<String, RelationType> myRelType2ObjMap = new LinkedHashMap<String, RelationType>();
  private final Map<String, List<String>> myRelType2RelIdsMap = new HashMap<String, List<String>>();
  private final Map<ObjectName, Map<String, List<String>>> myRefedMBeanObjName2RelIdsMap =
      new HashMap<ObjectName, Map<String, List<String>>>();
  private boolean myPurgeFlag = true;
  private final AtomicLong atomicSeqNo = new AtomicLong();
  private ObjectName myObjectName;
  private MBeanServer myMBeanServer;
  private boolean listening;

  public RelationService(boolean immediatePurgeFlag) {
    myPurgeFlag = immediatePurgeFlag;
  }

  public void isActive() throws RelationServiceNotRegisteredException {
    if (myMBeanServer == null) {
      throw new RelationServiceNotRegisteredException("Relation Service not registered in the MBean Server.");
    }
  }

  public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception {
    myMBeanServer = server;
    myObjectName = name;
    return name;
  }

  public void postRegister(Boolean registrationDone) {
    if (registrationDone != null && registrationDone.booleanValue() && myMBeanServer != null && !listening) {
      try {
        myMBeanServer.addNotificationListener(MBeanServerDelegate.DELEGATE_NAME, this, new NotificationFilter() {
          private static final long serialVersionUID = 1L;

          public boolean isNotificationEnabled(Notification n) {
            return n.getType().equals(MBeanServerNotification.UNREGISTRATION_NOTIFICATION);
          }
        }, null);
        listening = true;
      } catch (InstanceNotFoundException e) {
      }
    }
  }

  public void preDeregister() throws Exception {
  }

  public void postDeregister() {
    if (myMBeanServer != null && listening) {
      try {
        myMBeanServer.removeNotificationListener(MBeanServerDelegate.DELEGATE_NAME, this);
      } catch (Exception e) {
      }
    }
    listening = false;
  }

  public boolean getPurgeFlag() {
    return myPurgeFlag;
  }

  public void setPurgeFlag(boolean purgeFlag) {
    myPurgeFlag = purgeFlag;
  }

  public void createRelationType(String relationTypeName, RoleInfo[] roleInfoArray)
      throws IllegalArgumentException, InvalidRelationTypeException {
    if (relationTypeName == null || roleInfoArray == null) {
      throw new IllegalArgumentException("Invalid parameter.");
    }
    addRelationTypeInt(new RelationTypeSupport(relationTypeName, roleInfoArray));
  }

  public void addRelationType(RelationType relationTypeObj) throws IllegalArgumentException,
      InvalidRelationTypeException {
    if (relationTypeObj == null) {
      throw new IllegalArgumentException("Invalid parameter.");
    }
    List<RoleInfo> infos = relationTypeObj.getRoleInfos();
    if (infos == null || infos.isEmpty()) {
      throw new InvalidRelationTypeException("No role info provided.");
    }
    java.util.HashSet<String> seen = new java.util.HashSet<String>();
    for (RoleInfo ri : infos) {
      if (ri == null) {
        throw new InvalidRelationTypeException("Null role info provided.");
      }
      if (!seen.add(ri.getName())) {
        throw new InvalidRelationTypeException("Two role infos provided for role " + ri.getName());
      }
    }
    addRelationTypeInt(relationTypeObj);
  }

  private void addRelationTypeInt(RelationType type) throws InvalidRelationTypeException {
    String name = type.getRelationTypeName();
    synchronized (myRelType2ObjMap) {
      if (myRelType2ObjMap.containsKey(name)) {
        throw new InvalidRelationTypeException("There is already a relation type in the Relation Service with name "
            + name);
      }
      myRelType2ObjMap.put(name, type);
    }
  }

  public List<String> getAllRelationTypeNames() {
    synchronized (myRelType2ObjMap) {
      return new ArrayList<String>(myRelType2ObjMap.keySet());
    }
  }

  private RelationType type(String name) throws RelationTypeNotFoundException {
    RelationType t;
    synchronized (myRelType2ObjMap) {
      t = myRelType2ObjMap.get(name);
    }
    if (t == null) {
      throw new RelationTypeNotFoundException("No relation type created in the Relation Service with the name " + name);
    }
    return t;
  }

  public List<RoleInfo> getRoleInfos(String relationTypeName) throws IllegalArgumentException,
      RelationTypeNotFoundException {
    if (relationTypeName == null) {
      throw new IllegalArgumentException("Invalid parameter.");
    }
    return new ArrayList<RoleInfo>(type(relationTypeName).getRoleInfos());
  }

  public RoleInfo getRoleInfo(String relationTypeName, String roleInfoName) throws IllegalArgumentException,
      RelationTypeNotFoundException, RoleInfoNotFoundException {
    if (relationTypeName == null || roleInfoName == null) {
      throw new IllegalArgumentException("Invalid parameter.");
    }
    return type(relationTypeName).getRoleInfo(roleInfoName);
  }

  public void removeRelationType(String relationTypeName) throws RelationServiceNotRegisteredException,
      IllegalArgumentException, RelationTypeNotFoundException {
    isActive();
    if (relationTypeName == null) {
      throw new IllegalArgumentException("Invalid parameter.");
    }
    type(relationTypeName);
    List<String> ids;
    synchronized (myRelType2RelIdsMap) {
      List<String> l = myRelType2RelIdsMap.remove(relationTypeName);
      ids = l == null ? new ArrayList<String>() : new ArrayList<String>(l);
    }
    for (String id : ids) {
      try {
        removeRelation(id);
      } catch (RelationNotFoundException e) {
      }
    }
    synchronized (myRelType2ObjMap) {
      myRelType2ObjMap.remove(relationTypeName);
    }
  }

  private int checkRoleInt(int roleType, String roleName, List<ObjectName> values, RelationType type,
      boolean initFlag) {
    RoleInfo info;
    try {
      info = type.getRoleInfo(roleName);
    } catch (RoleInfoNotFoundException e) {
      return RoleStatus.NO_ROLE_WITH_NAME;
    }
    if (roleType == 1 && !initFlag && !info.isWritable()) {
      return RoleStatus.ROLE_NOT_WRITABLE;
    }
    if (roleType == 2 && !info.isReadable()) {
      return RoleStatus.ROLE_NOT_READABLE;
    }
    if (roleType == 2) {
      return 0;
    }
    int n = values == null ? 0 : values.size();
    if (!info.checkMinDegree(n)) {
      return RoleStatus.LESS_THAN_MIN_ROLE_DEGREE;
    }
    if (!info.checkMaxDegree(n)) {
      return RoleStatus.MORE_THAN_MAX_ROLE_DEGREE;
    }
    if (values != null) {
      for (ObjectName on : values) {
        if (on == null || myMBeanServer == null || !myMBeanServer.isRegistered(on)) {
          return RoleStatus.REF_MBEAN_NOT_REGISTERED;
        }
        try {
          if (!myMBeanServer.isInstanceOf(on, info.getRefMBeanClassName())) {
            return RoleStatus.REF_MBEAN_OF_INCORRECT_CLASS;
          }
        } catch (InstanceNotFoundException e) {
          return RoleStatus.REF_MBEAN_NOT_REGISTERED;
        }
      }
    }
    return 0;
  }

  public Integer checkRoleReading(String roleName, String relationTypeName) throws IllegalArgumentException,
      RelationTypeNotFoundException {
    if (roleName == null || relationTypeName == null) {
      throw new IllegalArgumentException("Invalid parameter.");
    }
    return Integer.valueOf(checkRoleInt(2, roleName, null, type(relationTypeName), false));
  }

  public Integer checkRoleWriting(Role role, String relationTypeName, Boolean initFlag)
      throws IllegalArgumentException, RelationTypeNotFoundException {
    if (role == null || relationTypeName == null || initFlag == null) {
      throw new IllegalArgumentException("Invalid parameter.");
    }
    return Integer.valueOf(checkRoleInt(1, role.getRoleName(), role.getRoleValue(), type(relationTypeName),
        initFlag.booleanValue()));
  }

  public void createRelation(String relationId, String relationTypeName, RoleList roleList)
      throws RelationServiceNotRegisteredException, IllegalArgumentException, RoleNotFoundException,
      InvalidRelationIdException, RelationTypeNotFoundException, InvalidRoleValueException {
    isActive();
    if (relationId == null || relationTypeName == null) {
      throw new IllegalArgumentException("Invalid parameter.");
    }
    checkNewId(relationId);
    type(relationTypeName);
    RelationSupport rel = new RelationSupport(relationId, myObjectName, myMBeanServer, relationTypeName, roleList);
    addRelationInt(true, rel, null, relationId, relationTypeName, rel.retrieveAllRoles());
    rel.setRelationServiceManagementFlag(Boolean.TRUE);
  }

  private void checkNewId(String relationId) throws InvalidRelationIdException {
    synchronized (myRelId2ObjMap) {
      if (myRelId2ObjMap.containsKey(relationId)) {
        throw new InvalidRelationIdException("There is already a relation with id " + relationId);
      }
    }
  }

  private void addRelationInt(boolean internal, Object rel, ObjectName mbean, String id, String typeName,
      RoleList roles) {
    synchronized (myRelId2ObjMap) {
      myRelId2ObjMap.put(id, rel);
      myRelId2RelTypeMap.put(id, typeName);
      if (!internal) {
        myRelMBeanObjName2RelIdMap.put(mbean.getCanonicalName(), mbean);
        myRelMBeanObjName2RelIdMap.put("id:" + mbean.getCanonicalName(), mbean);
      }
    }
    synchronized (myRelType2RelIdsMap) {
      List<String> l = myRelType2RelIdsMap.get(typeName);
      if (l == null) {
        l = new ArrayList<String>();
        myRelType2RelIdsMap.put(typeName, l);
      }
      l.add(id);
    }
    for (Role r : roles.asList()) {
      updateRoleMapInt(id, r, new ArrayList<ObjectName>());
    }
    try {
      sendRelationCreationNotification(id);
    } catch (RelationNotFoundException e) {
    }
  }

  private final Map<String, String> externalIds = new HashMap<String, String>();

  public void addRelation(ObjectName relationObjectName) throws IllegalArgumentException,
      RelationServiceNotRegisteredException, NoSuchMethodException, InvalidRelationIdException,
      InstanceNotFoundException, InvalidRelationServiceException, RelationTypeNotFoundException,
      RoleNotFoundException, InvalidRoleValueException {
    if (relationObjectName == null) {
      throw new IllegalArgumentException("Invalid parameter.");
    }
    isActive();
    if (!myMBeanServer.isRegistered(relationObjectName)) {
      throw new InstanceNotFoundException(relationObjectName.toString());
    }
    if (!myMBeanServer.isInstanceOf(relationObjectName, Relation.class.getName())) {
      throw new NoSuchMethodException("This MBean is not a Relation: " + relationObjectName);
    }
    try {
      String id = (String) myMBeanServer.getAttribute(relationObjectName, "RelationId");
      ObjectName svc = (ObjectName) myMBeanServer.getAttribute(relationObjectName, "RelationServiceName");
      String typeName = (String) myMBeanServer.getAttribute(relationObjectName, "RelationTypeName");
      if (!myObjectName.equals(svc)) {
        throw new InvalidRelationServiceException("The Relation Service referenced in the MBean is not the current one.");
      }
      checkNewId(id);
      type(typeName);
      RoleList roles = (RoleList) myMBeanServer.invoke(relationObjectName, "retrieveAllRoles", null, null);
      RelationType t = type(typeName);
      for (Role r : roles.asList()) {
        int st = checkRoleInt(1, r.getRoleName(), r.getRoleValue(), t, true);
        if (st != 0) {
          throw new InvalidRoleValueException("Role " + r.getRoleName() + " invalid (status " + st + ")");
        }
      }
      addRelationInt(false, relationObjectName, relationObjectName, id, typeName, roles);
      externalIds.put(relationObjectName.getCanonicalName(), id);
      try {
        myMBeanServer.setAttribute(relationObjectName,
            new javax.management.Attribute("RelationServiceManagementFlag", Boolean.TRUE));
      } catch (Exception e) {
        myMBeanServer.invoke(relationObjectName, "setRelationServiceManagementFlag", new Object[] {Boolean.TRUE},
            new String[] {Boolean.class.getName()});
      }
    } catch (InstanceNotFoundException e) {
      throw e;
    } catch (InvalidRelationServiceException e) {
      throw e;
    } catch (InvalidRelationIdException e) {
      throw e;
    } catch (RelationTypeNotFoundException e) {
      throw e;
    } catch (InvalidRoleValueException e) {
      throw e;
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new IllegalArgumentException(e.toString());
    }
  }

  public ObjectName isRelationMBean(String relationId) throws IllegalArgumentException, RelationNotFoundException {
    if (relationId == null) {
      throw new IllegalArgumentException("Invalid parameter.");
    }
    Object o = rel(relationId);
    if (o instanceof ObjectName) {
      return (ObjectName) o;
    }
    return null;
  }

  public String isRelation(ObjectName objectName) throws IllegalArgumentException {
    if (objectName == null) {
      throw new IllegalArgumentException("Invalid parameter.");
    }
    return externalIds.get(objectName.getCanonicalName());
  }

  public Boolean hasRelation(String relationId) throws IllegalArgumentException {
    if (relationId == null) {
      throw new IllegalArgumentException("Invalid parameter.");
    }
    synchronized (myRelId2ObjMap) {
      return Boolean.valueOf(myRelId2ObjMap.containsKey(relationId));
    }
  }

  public List<String> getAllRelationIds() {
    synchronized (myRelId2ObjMap) {
      return new ArrayList<String>(myRelId2ObjMap.keySet());
    }
  }

  private Object rel(String relationId) throws RelationNotFoundException {
    Object o;
    synchronized (myRelId2ObjMap) {
      o = myRelId2ObjMap.get(relationId);
    }
    if (o == null) {
      throw new RelationNotFoundException("No relation associated to relation id " + relationId);
    }
    return o;
  }

  private Object callRel(Object rel, String op, Object[] args, Class<?>[] types) throws Exception {
    if (rel instanceof ObjectName) {
      String[] sig = new String[types.length];
      for (int i = 0; i < sig.length; i++) {
        sig[i] = types[i].getName();
      }
      try {
        return myMBeanServer.invoke((ObjectName) rel, op, args, sig);
      } catch (javax.management.MBeanException e) {
        throw e.getTargetException();
      }
    }
    try {
      return Relation.class.getMethod(op, types).invoke(rel, args);
    } catch (java.lang.reflect.InvocationTargetException e) {
      Throwable t = e.getCause();
      if (t instanceof Exception) {
        throw (Exception) t;
      }
      throw e;
    }
  }

  public Integer getRoleCardinality(String relationId, String roleName) throws IllegalArgumentException,
      RelationNotFoundException, RoleNotFoundException {
    if (relationId == null || roleName == null) {
      throw new IllegalArgumentException("Invalid parameter.");
    }
    Object r = rel(relationId);
    try {
      return (Integer) callRel(r, "getRoleCardinality", new Object[] {roleName}, new Class<?>[] {String.class});
    } catch (RoleNotFoundException e) {
      throw e;
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new IllegalArgumentException(e.toString());
    }
  }

  @SuppressWarnings("unchecked")
  public List<ObjectName> getRole(String relationId, String roleName) throws RelationServiceNotRegisteredException,
      IllegalArgumentException, RelationNotFoundException, RoleNotFoundException {
    if (relationId == null || roleName == null) {
      throw new IllegalArgumentException("Invalid parameter.");
    }
    isActive();
    Object r = rel(relationId);
    try {
      return (List<ObjectName>) callRel(r, "getRole", new Object[] {roleName}, new Class<?>[] {String.class});
    } catch (RoleNotFoundException e) {
      throw e;
    } catch (RelationServiceNotRegisteredException e) {
      throw e;
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new IllegalArgumentException(e.toString());
    }
  }

  public RoleResult getRoles(String relationId, String[] roleNameArray) throws RelationServiceNotRegisteredException,
      IllegalArgumentException, RelationNotFoundException {
    if (relationId == null || roleNameArray == null) {
      throw new IllegalArgumentException("Invalid parameter.");
    }
    isActive();
    Object r = rel(relationId);
    try {
      return (RoleResult) callRel(r, "getRoles", new Object[] {roleNameArray}, new Class<?>[] {String[].class});
    } catch (RelationServiceNotRegisteredException e) {
      throw e;
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new IllegalArgumentException(e.toString());
    }
  }

  public RoleResult getAllRoles(String relationId) throws IllegalArgumentException, RelationNotFoundException,
      RelationServiceNotRegisteredException {
    if (relationId == null) {
      throw new IllegalArgumentException("Invalid parameter.");
    }
    isActive();
    Object r = rel(relationId);
    try {
      return (RoleResult) callRel(r, "getAllRoles", new Object[0], new Class<?>[0]);
    } catch (RelationServiceNotRegisteredException e) {
      throw e;
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new IllegalArgumentException(e.toString());
    }
  }

  @SuppressWarnings("unchecked")
  public Map<ObjectName, List<String>> getReferencedMBeans(String relationId) throws IllegalArgumentException,
      RelationNotFoundException {
    if (relationId == null) {
      throw new IllegalArgumentException("Invalid parameter.");
    }
    Object r = rel(relationId);
    try {
      return (Map<ObjectName, List<String>>) callRel(r, "getReferencedMBeans", new Object[0], new Class<?>[0]);
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new IllegalArgumentException(e.toString());
    }
  }

  public String getRelationTypeName(String relationId) throws IllegalArgumentException, RelationNotFoundException {
    if (relationId == null) {
      throw new IllegalArgumentException("Invalid parameter.");
    }
    rel(relationId);
    synchronized (myRelId2ObjMap) {
      return myRelId2RelTypeMap.get(relationId);
    }
  }

  public void setRole(String relationId, Role role) throws RelationServiceNotRegisteredException,
      IllegalArgumentException, RelationNotFoundException, RoleNotFoundException, InvalidRoleValueException {
    if (relationId == null || role == null) {
      throw new IllegalArgumentException("Invalid parameter.");
    }
    isActive();
    Object r = rel(relationId);
    try {
      callRel(r, "setRole", new Object[] {role}, new Class<?>[] {Role.class});
    } catch (RoleNotFoundException e) {
      throw e;
    } catch (InvalidRoleValueException e) {
      throw e;
    } catch (RelationServiceNotRegisteredException e) {
      throw e;
    } catch (RelationNotFoundException e) {
      throw e;
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new IllegalArgumentException(e.toString());
    }
  }

  public RoleResult setRoles(String relationId, RoleList roleList) throws RelationServiceNotRegisteredException,
      IllegalArgumentException, RelationNotFoundException {
    if (relationId == null || roleList == null) {
      throw new IllegalArgumentException("Invalid parameter.");
    }
    isActive();
    Object r = rel(relationId);
    try {
      return (RoleResult) callRel(r, "setRoles", new Object[] {roleList}, new Class<?>[] {RoleList.class});
    } catch (RelationServiceNotRegisteredException e) {
      throw e;
    } catch (RelationNotFoundException e) {
      throw e;
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new IllegalArgumentException(e.toString());
    }
  }

  public void removeRelation(String relationId) throws RelationServiceNotRegisteredException,
      IllegalArgumentException, RelationNotFoundException {
    isActive();
    if (relationId == null) {
      throw new IllegalArgumentException("Invalid parameter.");
    }
    Object r = rel(relationId);
    List<ObjectName> unreg = new ArrayList<ObjectName>();
    sendRelationRemovalNotification(relationId, unreg);
    String typeName;
    synchronized (myRelId2ObjMap) {
      myRelId2ObjMap.remove(relationId);
      typeName = myRelId2RelTypeMap.remove(relationId);
    }
    if (r instanceof ObjectName) {
      externalIds.remove(((ObjectName) r).getCanonicalName());
    } else {
      ((RelationSupport) r).setRelationServiceManagementFlag(Boolean.FALSE);
    }
    synchronized (myRelType2RelIdsMap) {
      List<String> l = myRelType2RelIdsMap.get(typeName);
      if (l != null) {
        l.remove(relationId);
      }
    }
    synchronized (myRefedMBeanObjName2RelIdsMap) {
      for (java.util.Iterator<Map.Entry<ObjectName, Map<String, List<String>>>> it =
          myRefedMBeanObjName2RelIdsMap.entrySet().iterator(); it.hasNext();) {
        Map<String, List<String>> m = it.next().getValue();
        m.remove(relationId);
        if (m.isEmpty()) {
          it.remove();
        }
      }
    }
  }

  public void purgeRelations() throws RelationServiceNotRegisteredException {
    isActive();
    List<ObjectName> gone = new ArrayList<ObjectName>();
    synchronized (myRefedMBeanObjName2RelIdsMap) {
      for (ObjectName on : myRefedMBeanObjName2RelIdsMap.keySet()) {
        if (!myMBeanServer.isRegistered(on)) {
          gone.add(on);
        }
      }
    }
    for (ObjectName on : gone) {
      handleUnregistered(on);
    }
  }

  private void handleUnregistered(ObjectName on) {
    Map<String, List<String>> m;
    synchronized (myRefedMBeanObjName2RelIdsMap) {
      m = myRefedMBeanObjName2RelIdsMap.get(on);
      if (m == null) {
        return;
      }
      m = new HashMap<String, List<String>>(m);
    }
    List<String> toRemove = new ArrayList<String>();
    for (Map.Entry<String, List<String>> e : m.entrySet()) {
      Object r;
      synchronized (myRelId2ObjMap) {
        r = myRelId2ObjMap.get(e.getKey());
      }
      if (r == null) {
        continue;
      }
      for (String roleName : new ArrayList<String>(e.getValue())) {
        try {
          callRel(r, "handleMBeanUnregistration", new Object[] {on, roleName},
              new Class<?>[] {ObjectName.class, String.class});
        } catch (InvalidRoleValueException ex) {
          toRemove.add(e.getKey());
        } catch (Exception ex) {
        }
      }
    }
    for (String id : toRemove) {
      try {
        removeRelation(id);
      } catch (Exception ex) {
      }
    }
    synchronized (myRefedMBeanObjName2RelIdsMap) {
      myRefedMBeanObjName2RelIdsMap.remove(on);
    }
  }

  public void handleNotification(Notification notif, Object handback) {
    if (!(notif instanceof MBeanServerNotification)) {
      return;
    }
    MBeanServerNotification n = (MBeanServerNotification) notif;
    if (!n.getType().equals(MBeanServerNotification.UNREGISTRATION_NOTIFICATION)) {
      return;
    }
    ObjectName on = n.getMBeanName();
    String externalId = externalIds.get(on.getCanonicalName());
    if (externalId != null) {
      try {
        removeRelation(externalId);
      } catch (Exception e) {
      }
    }
    if (myPurgeFlag) {
      handleUnregistered(on);
    }
  }

  public Map<String, List<String>> findReferencingRelations(ObjectName mbeanName, String relationTypeName,
      String roleName) throws IllegalArgumentException {
    if (mbeanName == null) {
      throw new IllegalArgumentException("Invalid parameter.");
    }
    Map<String, List<String>> result = new HashMap<String, List<String>>();
    Map<String, List<String>> m;
    synchronized (myRefedMBeanObjName2RelIdsMap) {
      m = myRefedMBeanObjName2RelIdsMap.get(mbeanName);
      if (m == null) {
        return result;
      }
      m = new HashMap<String, List<String>>(m);
    }
    for (Map.Entry<String, List<String>> e : m.entrySet()) {
      if (relationTypeName != null) {
        String t;
        synchronized (myRelId2ObjMap) {
          t = myRelId2RelTypeMap.get(e.getKey());
        }
        if (!relationTypeName.equals(t)) {
          continue;
        }
      }
      List<String> roles = new ArrayList<String>();
      for (String rn : e.getValue()) {
        if (roleName == null || roleName.equals(rn)) {
          roles.add(rn);
        }
      }
      if (!roles.isEmpty()) {
        result.put(e.getKey(), roles);
      }
    }
    return result;
  }

  public Map<ObjectName, List<String>> findAssociatedMBeans(ObjectName mbeanName, String relationTypeName,
      String roleName) throws IllegalArgumentException {
    if (mbeanName == null) {
      throw new IllegalArgumentException("Invalid parameter.");
    }
    Map<ObjectName, List<String>> result = new HashMap<ObjectName, List<String>>();
    Map<String, List<String>> refs = findReferencingRelations(mbeanName, relationTypeName, roleName);
    for (String id : refs.keySet()) {
      try {
        for (Map.Entry<ObjectName, List<String>> e : getReferencedMBeans(id).entrySet()) {
          if (e.getKey().equals(mbeanName)) {
            continue;
          }
          List<String> ids = result.get(e.getKey());
          if (ids == null) {
            ids = new ArrayList<String>();
            result.put(e.getKey(), ids);
          }
          ids.add(id);
        }
      } catch (RelationNotFoundException e) {
      }
    }
    return result;
  }

  public List<String> findRelationsOfType(String relationTypeName) throws IllegalArgumentException,
      RelationTypeNotFoundException {
    if (relationTypeName == null) {
      throw new IllegalArgumentException("Invalid parameter.");
    }
    type(relationTypeName);
    synchronized (myRelType2RelIdsMap) {
      List<String> l = myRelType2RelIdsMap.get(relationTypeName);
      return l == null ? new ArrayList<String>() : new ArrayList<String>(l);
    }
  }

  public void updateRoleMap(String relationId, Role newRole, List<ObjectName> oldRoleValue)
      throws IllegalArgumentException, RelationServiceNotRegisteredException, RelationNotFoundException {
    if (relationId == null || newRole == null || oldRoleValue == null) {
      throw new IllegalArgumentException("Invalid parameter.");
    }
    isActive();
    rel(relationId);
    updateRoleMapInt(relationId, newRole, oldRoleValue);
  }

  private void updateRoleMapInt(String relationId, Role newRole, List<ObjectName> oldValue) {
    String roleName = newRole.getRoleName();
    synchronized (myRefedMBeanObjName2RelIdsMap) {
      for (ObjectName on : oldValue) {
        if (newRole.getRoleValue().contains(on)) {
          continue;
        }
        Map<String, List<String>> m = myRefedMBeanObjName2RelIdsMap.get(on);
        if (m == null) {
          continue;
        }
        List<String> roles = m.get(relationId);
        if (roles != null) {
          roles.remove(roleName);
          if (roles.isEmpty()) {
            m.remove(relationId);
          }
        }
        if (m.isEmpty()) {
          myRefedMBeanObjName2RelIdsMap.remove(on);
        }
      }
      for (ObjectName on : newRole.getRoleValue()) {
        if (oldValue.contains(on)) {
          continue;
        }
        Map<String, List<String>> m = myRefedMBeanObjName2RelIdsMap.get(on);
        if (m == null) {
          m = new HashMap<String, List<String>>();
          myRefedMBeanObjName2RelIdsMap.put(on, m);
        }
        List<String> roles = m.get(relationId);
        if (roles == null) {
          roles = new ArrayList<String>();
          m.put(relationId, roles);
        }
        if (!roles.contains(roleName)) {
          roles.add(roleName);
        }
      }
    }
  }

  public void sendRelationCreationNotification(String relationId) throws IllegalArgumentException,
      RelationNotFoundException {
    if (relationId == null) {
      throw new IllegalArgumentException("Invalid parameter.");
    }
    Object r = rel(relationId);
    String type;
    synchronized (myRelId2ObjMap) {
      type = myRelId2RelTypeMap.get(relationId);
    }
    String t = r instanceof ObjectName ? RelationNotification.RELATION_MBEAN_CREATION
        : RelationNotification.RELATION_BASIC_CREATION;
    sendNotification(new RelationNotification(t, myObjectName != null ? myObjectName : (Object) this,
        atomicSeqNo.incrementAndGet(), System.currentTimeMillis(), "Creation of relation " + relationId, relationId,
        type, r instanceof ObjectName ? (ObjectName) r : null, null));
  }

  public void sendRelationRemovalNotification(String relationId, List<ObjectName> unregMBeanList)
      throws IllegalArgumentException, RelationNotFoundException {
    if (relationId == null) {
      throw new IllegalArgumentException("Invalid parameter.");
    }
    Object r = rel(relationId);
    String type;
    synchronized (myRelId2ObjMap) {
      type = myRelId2RelTypeMap.get(relationId);
    }
    String t = r instanceof ObjectName ? RelationNotification.RELATION_MBEAN_REMOVAL
        : RelationNotification.RELATION_BASIC_REMOVAL;
    sendNotification(new RelationNotification(t, myObjectName != null ? myObjectName : (Object) this,
        atomicSeqNo.incrementAndGet(), System.currentTimeMillis(), "Removal of relation " + relationId, relationId,
        type, r instanceof ObjectName ? (ObjectName) r : null, unregMBeanList));
  }

  public void sendRoleUpdateNotification(String relationId, Role newRole, List<ObjectName> oldValue)
      throws IllegalArgumentException, RelationNotFoundException {
    if (relationId == null || newRole == null || oldValue == null) {
      throw new IllegalArgumentException("Invalid parameter.");
    }
    Object r = rel(relationId);
    String type;
    synchronized (myRelId2ObjMap) {
      type = myRelId2RelTypeMap.get(relationId);
    }
    String t = r instanceof ObjectName ? RelationNotification.RELATION_MBEAN_UPDATE
        : RelationNotification.RELATION_BASIC_UPDATE;
    String msg = "Value of role " + newRole.getRoleName() + " has changed\nOld value:\n"
        + describe(oldValue) + "\nNew value:\n" + describe(newRole.getRoleValue());
    sendNotification(new RelationNotification(t, myObjectName != null ? myObjectName : (Object) this,
        atomicSeqNo.incrementAndGet(), System.currentTimeMillis(), msg, relationId, type,
        r instanceof ObjectName ? (ObjectName) r : null, newRole.getRoleName(), newRole.getRoleValue(), oldValue));
  }

  private static String describe(List<ObjectName> l) {
    if (l.isEmpty()) {
      return "";
    }
    StringBuilder b = new StringBuilder();
    for (ObjectName on : l) {
      b.append("\t").append(on).append("\n");
    }
    b.setLength(b.length() - 1);
    return b.toString();
  }

  public MBeanNotificationInfo[] getNotificationInfo() {
    String[] types = {RelationNotification.RELATION_BASIC_CREATION, RelationNotification.RELATION_MBEAN_CREATION,
        RelationNotification.RELATION_BASIC_UPDATE, RelationNotification.RELATION_MBEAN_UPDATE,
        RelationNotification.RELATION_BASIC_REMOVAL, RelationNotification.RELATION_MBEAN_REMOVAL};
    return new MBeanNotificationInfo[] {new MBeanNotificationInfo(types, RelationNotification.class.getName(),
        "Sent when a relation is created, updated or removed from the Relation Service")};
  }
}
