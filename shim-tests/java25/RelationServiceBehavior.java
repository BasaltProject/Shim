/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import javax.management.*; import javax.management.relation.*; import java.util.*;
public class RelationServiceBehavior {
  public interface DummyMBean {} public static class Dummy implements DummyMBean {}
  static void t(String l, java.util.concurrent.Callable<Object> c) { try { Object v = c.call(); System.out.println(l + ": " + v); } catch (Throwable e) { System.out.println(l + ": !" + e.getClass().getName() + ": " + e.getMessage()); } }
  static String rr(RoleResult r) { StringBuilder b = new StringBuilder(); for (Role x : r.getRoles().asList()) b.append(x).append(";"); for (RoleUnresolved u : r.getRolesUnresolved().asList()) b.append(u.getRoleName()).append("#").append(u.getProblemType()).append(";"); return b.toString(); }
  public static void main(String[] a) throws Exception {
    MBeanServer mbs = MBeanServerFactory.createMBeanServer();
    ObjectName d1 = new ObjectName("t:n=d1"), d2 = new ObjectName("t:n=d2"), d3 = new ObjectName("t:n=d3"), rs = new ObjectName("t:type=rs");
    for (ObjectName n : new ObjectName[]{d1, d2, d3}) mbs.registerMBean(new Dummy(), n);
    final RelationService svc = new RelationService(true);
    t("inactive", () -> { svc.isActive(); return 1; });
    mbs.registerMBean(svc, rs);
    final List<String> notifs = new ArrayList<>();
    mbs.addNotificationListener(rs, (n, h) -> { RelationNotification r = (RelationNotification) n; notifs.add(r.getType() + " " + r.getRelationId()); }, null, null);
    t("types", () -> svc.getAllRelationTypeNames());
    t("createType", () -> { svc.createRelationType("T", new RoleInfo[]{new RoleInfo("a", Dummy.class.getName(), true, true, 1, 2, "A"), new RoleInfo("b", Dummy.class.getName(), true, false, 0, 1, "B")}); return svc.getAllRelationTypeNames(); });
    t("dupType", () -> { svc.createRelationType("T", new RoleInfo[]{new RoleInfo("a", Dummy.class.getName())}); return 1; });
    t("roleInfos", () -> svc.getRoleInfos("T").size());
    t("noRoleInfo", () -> svc.getRoleInfo("T", "zz"));
    t("noType", () -> svc.getRoleInfos("Q"));
    t("chkRead", () -> svc.checkRoleReading("a", "T") + " " + svc.checkRoleReading("zz", "T"));
    t("chkWrite", () -> svc.checkRoleWriting(new Role("b", Arrays.asList(d1)), "T", false) + " " + svc.checkRoleWriting(new Role("b", Arrays.asList(d1)), "T", true) + " " + svc.checkRoleWriting(new Role("a", new ArrayList<ObjectName>()), "T", false) + " " + svc.checkRoleWriting(new Role("a", Arrays.asList(d1, d2, d3)), "T", false) + " " + svc.checkRoleWriting(new Role("a", Arrays.asList(new ObjectName("t:n=none"))), "T", false) + " " + svc.checkRoleWriting(new Role("a", Arrays.asList(rs)), "T", false));
    final RoleList rl = new RoleList(); rl.add(new Role("a", Arrays.asList(d1, d2)));
    t("create", () -> { svc.createRelation("R1", "T", rl); return svc.getAllRelationIds() + " " + svc.hasRelation("R1") + " " + svc.hasRelation("X"); });
    t("dupRel", () -> { svc.createRelation("R1", "T", rl); return 1; });
    t("badRel", () -> { RoleList bad = new RoleList(); bad.add(new Role("a", new ArrayList<ObjectName>())); svc.createRelation("R2", "T", bad); return 1; });
    t("noTypeRel", () -> { svc.createRelation("R3", "Q", rl); return 1; });
    t("getRole", () -> svc.getRole("R1", "a") + " " + svc.getRole("R1", "b"));
    t("noRole", () -> svc.getRole("R1", "zz"));
    t("noRel", () -> svc.getRole("RX", "a"));
    t("card", () -> svc.getRoleCardinality("R1", "a") + " " + svc.getRoleCardinality("R1", "b"));
    t("allRoles", () -> rr(svc.getAllRoles("R1")));
    t("getRoles", () -> rr(svc.getRoles("R1", new String[]{"a", "zz"})));
    t("typeName", () -> svc.getRelationTypeName("R1"));
    t("referenced", () -> new TreeMap<>(svc.getReferencedMBeans("R1")));
    t("setRole", () -> { svc.setRole("R1", new Role("a", Arrays.asList(d2, d3))); return svc.getRole("R1", "a"); });
    t("setRoleRO", () -> { svc.setRole("R1", new Role("b", Arrays.asList(d1))); return 1; });
    t("setRoleBad", () -> { svc.setRole("R1", new Role("a", Arrays.asList(d1, d2, d3))); return 1; });
    t("setRoles", () -> { RoleList l = new RoleList(); l.add(new Role("a", Arrays.asList(d1))); l.add(new Role("b", Arrays.asList(d3))); l.add(new Role("zz", Arrays.asList(d3))); return rr(svc.setRoles("R1", l)); });
    t("findRef", () -> new TreeMap<>(svc.findReferencingRelations(d1, null, null)) + " " + new TreeMap<>(svc.findReferencingRelations(d3, "T", "a")) + " " + svc.findReferencingRelations(d3, "Q", null));
    t("findAssoc", () -> new TreeMap<>(svc.findAssociatedMBeans(d1, null, null)));
    t("ofType", () -> svc.findRelationsOfType("T"));
    t("isRelation", () -> svc.isRelation(d1) + " " + svc.isRelationMBean("R1"));
    t("support", () -> { RelationSupport rs2 = new RelationSupport("R9", rs, mbs, "T", rl); mbs.registerMBean(rs2, new ObjectName("t:rel=R9")); svc.addRelation(new ObjectName("t:rel=R9")); return svc.getAllRelationIds() + " " + svc.isRelation(new ObjectName("t:rel=R9")) + " " + svc.isRelationMBean("R9") + " " + rs2.isInRelationService(); });
    t("unreg", () -> { mbs.unregisterMBean(d2); return svc.getAllRelationIds() + " " + rr(svc.getAllRoles("R1")); });
    t("removeRel", () -> { svc.removeRelation("R1"); return svc.getAllRelationIds(); });
    t("removeType", () -> { svc.removeRelationType("T"); return svc.getAllRelationTypeNames() + " " + svc.getAllRelationIds(); });
    Thread.sleep(300);
    t("notifs", () -> notifs);
    System.exit(0);
  }
}
