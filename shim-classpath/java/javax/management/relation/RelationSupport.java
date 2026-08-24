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
import java.util.List;
import java.util.Map;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;

public class RelationSupport implements RelationSupportMBean, MBeanRegistration {
  private String myRelId;
  private ObjectName myRelServiceName;
  private MBeanServer myRelServiceMBeanServer;
  private String myRelTypeName;
  private final Map<String, Role> myRoleName2ValueMap = new HashMap<String, Role>();
  private final java.util.concurrent.atomic.AtomicBoolean myInRelServFlag = new java.util.concurrent.atomic.AtomicBoolean();

  public RelationSupport(String relationId, ObjectName relationServiceName, String relationTypeName,
      RoleList list) throws InvalidRoleValueException, IllegalArgumentException {
    init(relationId, relationServiceName, null, relationTypeName, list);
  }

  public RelationSupport(String relationId, ObjectName relationServiceName, MBeanServer relationServiceMBeanServer,
      String relationTypeName, RoleList list) throws InvalidRoleValueException, IllegalArgumentException {
    if (relationServiceMBeanServer == null) {
      throw new IllegalArgumentException("Invalid parameter.");
    }
    init(relationId, relationServiceName, relationServiceMBeanServer, relationTypeName, list);
  }

  private void init(String relationId, ObjectName relationServiceName, MBeanServer server, String relationTypeName,
      RoleList list) throws InvalidRoleValueException {
    if (relationId == null || relationServiceName == null || relationTypeName == null) {
      throw new IllegalArgumentException("Invalid parameter.");
    }
    myRelId = relationId;
    myRelServiceName = relationServiceName;
    myRelServiceMBeanServer = server;
    myRelTypeName = relationTypeName;
    initMembers(list);
  }

  private void initMembers(RoleList list) throws InvalidRoleValueException {
    if (list != null) {
      for (Role role : list.asList()) {
        Integer status = checkWriting(role, true);
        if (status == null) {
          status = Integer.valueOf(0);
        }
        if (status.intValue() != 0) {
          try {
            throwProblem(status.intValue(), role.getRoleName());
          } catch (RoleNotFoundException e) {
            throw new InvalidRoleValueException(e.getMessage());
          }
        }
        myRoleName2ValueMap.put(role.getRoleName(), (Role) role.clone());
      }
    }
    try {
      List<RoleInfo> infos = (List<RoleInfo>) invokeService("getRoleInfos", new Object[] {myRelTypeName},
          new String[] {String.class.getName()});
      for (RoleInfo ri : infos) {
        if (!myRoleName2ValueMap.containsKey(ri.getName())) {
          myRoleName2ValueMap.put(ri.getName(), new Role(ri.getName(), new ArrayList<ObjectName>()));
        }
      }
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
    }
  }

  private Object invokeService(String op, Object[] params, String[] sig) throws Exception {
    if (myRelServiceMBeanServer == null) {
      throw new RelationServiceNotRegisteredException("Relation Service not registered");
    }
    try {
      return myRelServiceMBeanServer.invoke(myRelServiceName, op, params, sig);
    } catch (javax.management.MBeanException e) {
      throw e.getTargetException();
    } catch (javax.management.ReflectionException e) {
      throw e.getTargetException();
    }
  }

  static void throwProblem(int status, String name) throws RoleNotFoundException, InvalidRoleValueException {
    String m;
    switch (status) {
      case RoleStatus.NO_ROLE_WITH_NAME:
        throw new RoleNotFoundException(name + " does not exist in relation.");
      case RoleStatus.ROLE_NOT_READABLE:
        throw new RoleNotFoundException(name + " is not readable.");
      case RoleStatus.ROLE_NOT_WRITABLE:
        throw new RoleNotFoundException(name + " is not writable.");
      case RoleStatus.LESS_THAN_MIN_ROLE_DEGREE:
        m = " has a number of MBean references less than the expected minimum degree.";
        break;
      case RoleStatus.MORE_THAN_MAX_ROLE_DEGREE:
        m = " has a number of MBean references greater than the expected maximum degree.";
        break;
      case RoleStatus.REF_MBEAN_OF_INCORRECT_CLASS:
        m = " has an MBean reference to an MBean not of the expected class of references for that role.";
        break;
      case RoleStatus.REF_MBEAN_NOT_REGISTERED:
        m = " has a reference to null or to an MBean not registered.";
        break;
      default:
        return;
    }
    throw new InvalidRoleValueException(name + m);
  }

  private Integer checkWriting(Role role, boolean init) {
    try {
      Object r = invokeService("checkRoleWriting", new Object[] {role, myRelTypeName, Boolean.valueOf(init)},
          new String[] {Role.class.getName(), String.class.getName(), Boolean.class.getName()});
      return (Integer) r;
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      return Integer.valueOf(0);
    }
  }

  private int checkReading(String roleName) throws RelationServiceNotRegisteredException {
    try {
      return ((Integer) invokeService("checkRoleReading", new Object[] {roleName, myRelTypeName},
          new String[] {String.class.getName(), String.class.getName()})).intValue();
    } catch (RelationServiceNotRegisteredException e) {
      throw e;
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      return 0;
    }
  }

  public List<ObjectName> getRole(String roleName) throws IllegalArgumentException, RoleNotFoundException,
      RelationServiceNotRegisteredException {
    if (roleName == null) {
      throw new IllegalArgumentException("Invalid parameter.");
    }
    int status = checkReading(roleName);
    if (status != 0) {
      try {
        throwProblem(status, roleName);
      } catch (InvalidRoleValueException e) {
        throw new RoleNotFoundException(e.getMessage());
      }
    }
    Role r = myRoleName2ValueMap.get(roleName);
    return new ArrayList<ObjectName>(r.getRoleValue());
  }

  public RoleResult getRoles(String[] roleNameArray) throws IllegalArgumentException,
      RelationServiceNotRegisteredException {
    if (roleNameArray == null) {
      throw new IllegalArgumentException("Invalid parameter.");
    }
    RoleList ok = new RoleList();
    RoleUnresolvedList bad = new RoleUnresolvedList();
    for (String name : roleNameArray) {
      int status = checkReading(name);
      if (status == 0) {
        ok.add((Role) myRoleName2ValueMap.get(name).clone());
      } else {
        bad.add(new RoleUnresolved(name, null, status));
      }
    }
    return new RoleResult(ok, bad);
  }

  public RoleResult getAllRoles() throws RelationServiceNotRegisteredException {
    return getRoles(myRoleName2ValueMap.keySet().toArray(new String[0]));
  }

  public RoleList retrieveAllRoles() {
    RoleList l = new RoleList();
    synchronized (myRoleName2ValueMap) {
      for (Role r : myRoleName2ValueMap.values()) {
        l.add((Role) r.clone());
      }
    }
    return l;
  }

  public Integer getRoleCardinality(String roleName) throws IllegalArgumentException, RoleNotFoundException {
    if (roleName == null) {
      throw new IllegalArgumentException("Invalid parameter.");
    }
    Role r = myRoleName2ValueMap.get(roleName);
    if (r == null) {
      throw new RoleNotFoundException("No role with name " + roleName);
    }
    return Integer.valueOf(r.getRoleValue().size());
  }

  public void setRole(Role role) throws IllegalArgumentException, RoleNotFoundException,
      RelationTypeNotFoundException, InvalidRoleValueException, RelationServiceNotRegisteredException,
      RelationNotFoundException {
    if (role == null) {
      throw new IllegalArgumentException("Invalid parameter.");
    }
    Object result = setRoleInt(role, true);
    if (result != null) {
      if (result instanceof RuntimeException) {
        throw (RuntimeException) result;
      }
    }
  }

  private Object setRoleInt(Role role, boolean notify) throws RoleNotFoundException, InvalidRoleValueException,
      RelationServiceNotRegisteredException, RelationTypeNotFoundException, RelationNotFoundException {
    String name = role.getRoleName();
    int status = checkWriting(role, false).intValue();
    Role old = myRoleName2ValueMap.get(name);
    if (status != 0) {
      throwProblem(status, name);
    }
    List<ObjectName> oldValue = old == null ? new ArrayList<ObjectName>() : new ArrayList<ObjectName>(old.getRoleValue());
    myRoleName2ValueMap.put(name, (Role) role.clone());
    if (notify && myInRelServFlag.get()) {
      try {
        invokeService("updateRoleMap", new Object[] {myRelId, role, oldValue},
            new String[] {String.class.getName(), Role.class.getName(), List.class.getName()});
        invokeService("sendRoleUpdateNotification", new Object[] {myRelId, role, oldValue},
            new String[] {String.class.getName(), Role.class.getName(), List.class.getName()});
      } catch (RelationServiceNotRegisteredException e) {
        throw e;
      } catch (RelationNotFoundException e) {
        throw e;
      } catch (RuntimeException e) {
        throw e;
      } catch (Exception e) {
      }
    }
    return null;
  }

  public RoleResult setRoles(RoleList list) throws IllegalArgumentException, RelationServiceNotRegisteredException,
      RelationTypeNotFoundException, RelationNotFoundException {
    if (list == null) {
      throw new IllegalArgumentException("Invalid parameter.");
    }
    RoleList ok = new RoleList();
    RoleUnresolvedList bad = new RoleUnresolvedList();
    for (Role role : list.asList()) {
      try {
        setRoleInt(role, true);
        ok.add(role);
      } catch (RoleNotFoundException e) {
        bad.add(new RoleUnresolved(role.getRoleName(), role.getRoleValue(),
            checkWriting(role, false).intValue()));
      } catch (InvalidRoleValueException e) {
        bad.add(new RoleUnresolved(role.getRoleName(), role.getRoleValue(),
            checkWriting(role, false).intValue()));
      }
    }
    return new RoleResult(ok, bad);
  }

  public void handleMBeanUnregistration(ObjectName objectName, String roleName) throws IllegalArgumentException,
      RoleNotFoundException, InvalidRoleValueException, RelationServiceNotRegisteredException,
      RelationTypeNotFoundException, RelationNotFoundException {
    if (objectName == null || roleName == null) {
      throw new IllegalArgumentException("Invalid parameter.");
    }
    Role role = myRoleName2ValueMap.get(roleName);
    if (role == null) {
      throw new RoleNotFoundException("No role with name " + roleName);
    }
    List<ObjectName> newValue = new ArrayList<ObjectName>(role.getRoleValue());
    newValue.remove(objectName);
    Role newRole = new Role(roleName, newValue);
    int status = checkWriting(newRole, true).intValue();
    if (status != 0) {
      throwProblem(status, roleName);
    }
    setRoleInt(newRole, true);
  }

  public Map<ObjectName, List<String>> getReferencedMBeans() {
    Map<ObjectName, List<String>> map = new HashMap<ObjectName, List<String>>();
    synchronized (myRoleName2ValueMap) {
      for (Role r : myRoleName2ValueMap.values()) {
        for (ObjectName on : r.getRoleValue()) {
          List<String> names = map.get(on);
          if (names == null) {
            names = new ArrayList<String>();
            map.put(on, names);
          }
          names.add(r.getRoleName());
        }
      }
    }
    return map;
  }

  public String getRelationTypeName() {
    return myRelTypeName;
  }

  public ObjectName getRelationServiceName() {
    return myRelServiceName;
  }

  public String getRelationId() {
    return myRelId;
  }

  public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception {
    myRelServiceMBeanServer = server;
    return name;
  }

  public void postRegister(Boolean registrationDone) {
  }

  public void preDeregister() throws Exception {
  }

  public void postDeregister() {
  }

  public Boolean isInRelationService() {
    return Boolean.valueOf(myInRelServFlag.get());
  }

  public void setRelationServiceManagementFlag(Boolean flag) throws IllegalArgumentException {
    if (flag == null) {
      throw new IllegalArgumentException("Invalid parameter.");
    }
    myInRelServFlag.set(flag.booleanValue());
  }
}
