/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.lang.reflect.*;
import java.util.*;
import javax.management.*;

public class JmxServerModern {

static class Server {
  public interface HelloMBean {
    String getName();
    void setName(String n);
    int getCount();
    boolean isActive();
    void setActive(boolean b);
    String echo(String s);
    int add(int a, int b);
    void boom() throws Exception;
    void rt();
    void err();
    double getRatio();
    void setWrite(String s);
  }
  public static class Hello extends NotificationBroadcasterSupport implements HelloMBean, MBeanRegistration {
    String name = "n0"; boolean active; String w;
    public Hello() {}
    public Hello(String n) { name = n; }
    public Hello(int a, String n) { name = n + a; }
    public String getName() { return name; }
    public void setName(String n) { name = n; }
    public int getCount() { return 3; }
    public boolean isActive() { return active; }
    public void setActive(boolean b) { active = b; }
    public String echo(String s) { return "e:" + s; }
    public int add(int a, int b) { return a + b; }
    public void boom() throws Exception { throw new Exception("kaboom"); }
    public void rt() { throw new IllegalStateException("rt"); }
    public void err() { throw new AssertionError("err"); }
    public double getRatio() { return 1.5; }
    public void setWrite(String s) { w = s; }
    public ObjectName preRegister(MBeanServer s, ObjectName n) { System.out.println("pre " + n); return n; }
    public void postRegister(Boolean b) { System.out.println("post " + b); }
    public void preDeregister() { System.out.println("preDereg"); }
    public void postDeregister() { System.out.println("postDereg"); }
  }
  public static class Plain { public int getX() { return 1; } }
  public interface NoMBean {}
  public static class NoImpl implements NoMBean {}
  public interface FooMXBean { int getV(); }
  public static class Foo implements FooMXBean { public int getV() { return 7; } }

  static void t(String n, java.util.concurrent.Callable<Object> c) {
    try { Object o = c.call(); System.out.println(n + ": " + (o instanceof Object[] ? Arrays.deepToString((Object[]) o) : o)); }
    catch (Throwable e) { System.out.println(n + ": !" + e.getClass().getName() + ":" + (e instanceof NullPointerException ? null : e.getMessage()) + (e.getCause() != null ? " / " + e.getCause().getClass().getName() + ":" + e.getCause().getMessage() : "")); }
  }

  public static void run() throws Exception {
    MBeanServer s = MBeanServerFactory.newMBeanServer();
    ObjectName n = new ObjectName("d:type=Hello");
    t("count0", () -> s.getMBeanCount());
    t("dom", () -> s.getDefaultDomain());
    t("domains", () -> { String[] d = s.getDomains(); Arrays.sort(d); return d; });
    t("reg", () -> s.registerMBean(new Hello(), n));
    t("regdup", () -> s.registerMBean(new Hello(), n));
    t("regnull", () -> s.registerMBean(null, new ObjectName("d:x=y")));
    t("regpattern", () -> s.registerMBean(new Hello(), new ObjectName("d:*")));
    t("regnoname", () -> s.registerMBean(new Hello(), null));
    t("regplain", () -> s.registerMBean(new Plain(), new ObjectName("d:type=plain")));
    t("regnoimpl", () -> s.registerMBean(new NoImpl(), new ObjectName("d:type=noimpl")));
    t("regmx", () -> s.registerMBean(new Foo(), new ObjectName("d:type=foo")));
    t("regjmi", () -> s.registerMBean(new Hello(), new ObjectName("JMImplementation:type=X")));
    t("regdefdom", () -> s.registerMBean(new Hello(), new ObjectName(":type=nodomain")));
    t("count", () -> s.getMBeanCount());
    t("isreg", () -> s.isRegistered(n) + " " + s.isRegistered(new ObjectName("d:type=zip")));
    t("isregnull", () -> s.isRegistered(null));
    t("inst", () -> s.getObjectInstance(n));
    t("instmissing", () -> s.getObjectInstance(new ObjectName("d:type=zip")));
    t("getattr", () -> s.getAttribute(n, "Name") + " " + s.getAttribute(n, "Count") + " " + s.getAttribute(n, "Active") + " " + s.getAttribute(n, "Ratio"));
    t("getattrmissing", () -> s.getAttribute(n, "Nope"));
    t("getattrwriteonly", () -> s.getAttribute(n, "Write"));
    t("getattrnullname", () -> s.getAttribute(null, "Name"));
    t("getattrnullattr", () -> s.getAttribute(n, null));
    t("getattrnomb", () -> s.getAttribute(new ObjectName("d:type=zip"), "Name"));
    t("setattr", () -> { s.setAttribute(n, new Attribute("Name", "newname")); return s.getAttribute(n, "Name"); });
    t("setattrbad", () -> { s.setAttribute(n, new Attribute("Name", 5)); return "ok"; });
    t("setattrro", () -> { s.setAttribute(n, new Attribute("Count", 5)); return "ok"; });
    t("setattrmissing", () -> { s.setAttribute(n, new Attribute("Nope", 5)); return "ok"; });
    t("setattrprim", () -> { s.setAttribute(n, new Attribute("Active", true)); return s.getAttribute(n, "Active"); });
    t("setattrprimbad", () -> { s.setAttribute(n, new Attribute("Active", null)); return "ok"; });
    t("getattrs", () -> s.getAttributes(n, new String[] {"Name", "Count", "Nope"}));
    t("setattrs", () -> { AttributeList l = new AttributeList(); l.add(new Attribute("Name", "x")); l.add(new Attribute("Nope", 1)); return s.setAttributes(n, l); });
    t("invoke", () -> s.invoke(n, "echo", new Object[] {"hi"}, new String[] {"java.lang.String"}));
    t("invoke2", () -> s.invoke(n, "add", new Object[] {2, 3}, new String[] {"int", "int"}));
    t("invokemissing", () -> s.invoke(n, "nope", new Object[0], new String[0]));
    t("invokebadsig", () -> s.invoke(n, "echo", new Object[] {"hi"}, new String[] {"java.lang.Integer"}));
    t("invokeboom", () -> s.invoke(n, "boom", null, null));
    t("invokert", () -> s.invoke(n, "rt", null, null));
    t("invokeerr", () -> s.invoke(n, "err", null, null));
    t("invokebadargs", () -> s.invoke(n, "echo", new Object[] {5}, new String[] {"java.lang.String"}));
    t("invokenoargs", () -> s.invoke(n, "echo", null, new String[] {"java.lang.String"}));
    t("info", () -> { MBeanInfo i = s.getMBeanInfo(n); return i.getClassName() + " " + i.getDescription() + " " + i.getDescriptor(); });
    t("infoattrs", () -> { StringBuilder sb = new StringBuilder(); List<String> l = new ArrayList<>(); for (MBeanAttributeInfo x : s.getMBeanInfo(n).getAttributes()) l.add(x.getName() + ":" + x.getType() + ":" + x.isReadable() + x.isWritable() + x.isIs() + ":" + x.getDescription() + ":" + x.getDescriptor()); Collections.sort(l); return l; });
    t("infoops", () -> { List<String> l = new ArrayList<>(); for (MBeanOperationInfo x : s.getMBeanInfo(n).getOperations()) l.add(x.getName() + ":" + x.getReturnType() + ":" + x.getImpact() + ":" + x.getDescription() + ":" + Arrays.toString(x.getSignature()) + ":" + x.getDescriptor()); Collections.sort(l); return l; });
    t("infoctors", () -> { List<String> l = new ArrayList<>(); for (MBeanConstructorInfo x : s.getMBeanInfo(n).getConstructors()) l.add(x.getName() + ":" + x.getDescription() + ":" + Arrays.toString(x.getSignature())); Collections.sort(l); return l; });
    t("infonotif", () -> Arrays.toString(s.getMBeanInfo(n).getNotifications()));
    t("infomx", () -> { MBeanInfo i = s.getMBeanInfo(new ObjectName("d:type=foo")); return i.getClassName() + " " + i.getDescriptor() + " " + Arrays.toString(i.getAttributes()) + " " + i.getConstructors().length; });
    t("infomissing", () -> s.getMBeanInfo(new ObjectName("d:type=zip")));
    t("delegate", () -> s.getAttribute(MBeanServerDelegate.DELEGATE_NAME, "SpecificationName") + "|" + s.getAttribute(MBeanServerDelegate.DELEGATE_NAME, "SpecificationVersion") + "|" + s.getAttribute(MBeanServerDelegate.DELEGATE_NAME, "ImplementationName") + "|" + s.getAttribute(MBeanServerDelegate.DELEGATE_NAME, "SpecificationVendor") + "|" + ((String) s.getAttribute(MBeanServerDelegate.DELEGATE_NAME, "MBeanServerId")).contains("_"));
    t("delegateinfo", () -> s.getMBeanInfo(MBeanServerDelegate.DELEGATE_NAME).getClassName() + " " + Arrays.toString(s.getMBeanInfo(MBeanServerDelegate.DELEGATE_NAME).getNotifications()));
    t("instanceof", () -> s.isInstanceOf(n, "JmxServerModern$Server$Hello") + " " + s.isInstanceOf(n, "JmxServerModern$Server$HelloMBean") + " " + s.isInstanceOf(n, "java.lang.String") + " " + s.isInstanceOf(n, "javax.management.NotificationBroadcasterSupport") + " " + s.isInstanceOf(n, "nope.Missing"));
    t("queryNames", () -> new TreeSet<>(s.queryNames(null, null)));
    t("queryNames2", () -> new TreeSet<>(s.queryNames(new ObjectName("d:*"), null)));
    t("queryNames3", () -> new TreeSet<>(s.queryNames(new ObjectName("*:type=Hello"), null)));
    t("queryNames4", () -> new TreeSet<>(s.queryNames(n, null)));
    t("queryNames5", () -> new TreeSet<>(s.queryNames(new ObjectName("zz:*"), null)));
    t("queryExp", () -> new TreeSet<>(s.queryNames(new ObjectName("d:*"), Query.eq(Query.attr("Count"), Query.value(3)))));
    t("queryExp2", () -> new TreeSet<>(s.queryNames(new ObjectName("d:*"), Query.gt(Query.attr("Count"), Query.value(3)))));
    t("queryExp3", () -> new TreeSet<>(s.queryNames(new ObjectName("d:*"), Query.match(Query.attr("Name"), Query.value("x*")))));
    t("queryExp4", () -> new TreeSet<>(s.queryNames(new ObjectName("d:*"), Query.and(Query.isInstanceOf(Query.value("JmxServerModern$Server$Hello")), Query.eq(Query.attr("Active"), Query.value(true))))));
    t("queryExp5", () -> new TreeSet<>(s.queryNames(new ObjectName("d:*"), Query.between(Query.attr("Ratio"), Query.value(1.0), Query.value(2.0)))));
    t("queryExp6", () -> new TreeSet<>(s.queryNames(new ObjectName("d:*"), Query.in(Query.attr("Count"), new ValueExp[] {Query.value(1), Query.value(3)}))));
    t("queryExp7", () -> new TreeSet<>(s.queryNames(new ObjectName("d:*"), Query.not(Query.eq(Query.attr("Count"), Query.value(3))))));
    t("queryExp8", () -> new TreeSet<>(s.queryNames(new ObjectName("d:*"), Query.eq(Query.plus(Query.attr("Count"), Query.value(1)), Query.value(4)))));
    t("queryExp9", () -> new TreeSet<>(s.queryNames(new ObjectName("d:*"), Query.initialSubString(Query.attr("Name"), Query.value("x")))));
    t("queryExp10", () -> new TreeSet<>(s.queryNames(new ObjectName("d:*"), Query.eq(Query.classattr(), Query.value("JmxServerModern$Server$Hello")))));
    t("queryStr", () -> Query.and(Query.eq(Query.attr("a"), Query.value(1)), Query.or(Query.gt(Query.attr("b"), Query.value(2.5)), Query.not(Query.match(Query.attr("c"), Query.value("x?"))))));
    t("queryStr2", () -> Query.between(Query.attr("C", "a"), Query.plus(Query.value(1), Query.value(2)), Query.times(Query.value(3), Query.minus(Query.value(4), Query.value(5)))));
    t("queryStr3", () -> Query.in(Query.attr("a"), new ValueExp[] {Query.value("p"), Query.value("q'r")}) + " | " + Query.isInstanceOf(Query.value("x.Y")) + " | " + Query.initialSubString(Query.attr("a"), Query.value("b*")) + " | " + Query.anySubString(Query.attr("a"), Query.value("b")) + " | " + Query.finalSubString(Query.attr("a"), Query.value("b")) + " | " + Query.classattr() + " | " + Query.div(Query.value(1), Query.value(2.5)) + " | " + Query.value(true) + " | " + Query.value(1.0f));
    t("queryMB", () -> s.queryMBeans(new ObjectName("d:type=Hello"), null));
    t("notif", () -> {
      List<String> got = new ArrayList<>();
      NotificationListener l = (nn, hb) -> got.add(nn.getType() + ":" + ((MBeanServerNotification) nn).getMBeanName() + ":" + hb + ":" + nn.getSource() + ":" + nn.getSequenceNumber());
      s.addNotificationListener(MBeanServerDelegate.DELEGATE_NAME, l, null, "hb");
      ObjectName q = new ObjectName("d:type=Q");
      s.registerMBean(new Hello(), q);
      s.unregisterMBean(q);
      s.removeNotificationListener(MBeanServerDelegate.DELEGATE_NAME, l);
      return got;
    });
    t("notiflistener", () -> {
      List<String> got = new ArrayList<>();
      NotificationListener l = (nn, hb) -> got.add(nn.getType() + ":" + nn.getMessage() + ":" + hb);
      s.addNotificationListener(n, l, null, "hb2");
      s.invoke(n, "echo", new Object[] {"x"}, new String[] {"java.lang.String"});
      Object o = s.getClassLoaderFor(n);
      s.removeNotificationListener(n, l);
      return got;
    });
    t("notifnotbroadcaster", () -> { s.addNotificationListener(new ObjectName("d:type=foo"), (nn, hb) -> {}, null, null); return "ok"; });
    t("notifremove", () -> { s.removeNotificationListener(n, (nn, hb) -> {}); return "ok"; });
    t("notifnull", () -> { s.addNotificationListener(n, (NotificationListener) null, null, null); return "ok"; });
    t("unreg", () -> { s.unregisterMBean(new ObjectName("d:type=foo")); return s.isRegistered(new ObjectName("d:type=foo")); });
    t("unregmissing", () -> { s.unregisterMBean(new ObjectName("d:type=foo")); return "ok"; });
    t("unregnull", () -> { s.unregisterMBean(null); return "ok"; });
    t("unregdelegate", () -> { s.unregisterMBean(MBeanServerDelegate.DELEGATE_NAME); return "ok"; });
    t("create", () -> s.createMBean("JmxServerModern$Server$Hello", new ObjectName("d:type=created")));
    t("createargs", () -> { ObjectInstance i = s.createMBean("JmxServerModern$Server$Hello", new ObjectName("d:type=created2"), new Object[] {"zz"}, new String[] {"java.lang.String"}); return i + " " + s.getAttribute(new ObjectName("d:type=created2"), "Name"); });
    t("createargs2", () -> { s.createMBean("JmxServerModern$Server$Hello", new ObjectName("d:type=created3"), new Object[] {4, "p"}, new String[] {"int", "java.lang.String"}); return s.getAttribute(new ObjectName("d:type=created3"), "Name"); });
    t("createmissing", () -> s.createMBean("no.Such", new ObjectName("d:type=cm")));
    t("createbadsig", () -> s.createMBean("JmxServerModern$Server$Hello", new ObjectName("d:type=cm"), new Object[] {1}, new String[] {"java.lang.Integer"}));
    t("createnull", () -> s.createMBean(null, new ObjectName("d:type=cm")));
    t("instantiate", () -> s.instantiate("JmxServerModern$Server$Hello").getClass().getName());
    t("fac", () -> { MBeanServer m1 = MBeanServerFactory.createMBeanServer(); MBeanServer m2 = MBeanServerFactory.createMBeanServer("zzz"); String id = (String) m1.getAttribute(MBeanServerDelegate.DELEGATE_NAME, "MBeanServerId"); int sz = MBeanServerFactory.findMBeanServer(null).size(); int one = MBeanServerFactory.findMBeanServer(id).size(); MBeanServerFactory.releaseMBeanServer(m1); MBeanServerFactory.releaseMBeanServer(m2); return sz + " " + one + " " + m2.getDefaultDomain() + " " + MBeanServerFactory.findMBeanServer(null).size(); });
    t("facrel", () -> { MBeanServerFactory.releaseMBeanServer(s); return "ok"; });
    t("proxy", () -> { HelloMBean p = JMX.newMBeanProxy(s, n, HelloMBean.class); p.setName("viaproxy"); return p.getName() + " " + p.echo("q") + " " + p.add(1, 2) + " " + p.isActive() + " " + p.toString().startsWith("MBeanProxy(") + " " + (p.hashCode() == JMX.newMBeanProxy(s, n, HelloMBean.class).hashCode()) + " " + p.equals(JMX.newMBeanProxy(s, n, HelloMBean.class)); });
    t("proxyboom", () -> { HelloMBean p = JMX.newMBeanProxy(s, n, HelloMBean.class); p.boom(); return "ok"; });
    t("proxyrt", () -> { HelloMBean p = JMX.newMBeanProxy(s, n, HelloMBean.class); p.rt(); return "ok"; });
    t("proxyemit", () -> { HelloMBean p = JMX.newMBeanProxy(s, n, HelloMBean.class, true); return p instanceof NotificationEmitter; });
    t("proxymx", () -> { FooMXBean p = JMX.newMXBeanProxy(s, new ObjectName("d:type=foo2"), FooMXBean.class); return p.toString().startsWith("MXBeanProxy("); });
    t("ismx", () -> JMX.isMXBeanInterface(FooMXBean.class) + " " + JMX.isMXBeanInterface(HelloMBean.class) + " " + JMX.isMXBeanInterface(Foo.class));
    t("stdmbean", () -> { StandardMBean m = new StandardMBean(new Hello("sm"), HelloMBean.class); ObjectName on = new ObjectName("d:type=sm"); s.registerMBean(m, on); return s.getAttribute(on, "Name") + " " + s.getMBeanInfo(on).getClassName() + " " + m.getMBeanInterface().getName() + " " + m.getImplementationClass().getName(); });
    t("stdmbeannull", () -> new StandardMBean(null, HelloMBean.class));
    t("stdmbeanbad", () -> new StandardMBean((Object) new Plain(), (Class) HelloMBean.class));
    t("stdmbeannoiface", () -> new StandardMBean(new Plain(), Plain.class));
    t("stdmbeanmx", () -> { StandardMBean m = new StandardMBean(new Foo(), FooMXBean.class, true); return m.getMBeanInfo().getDescriptor(); });
    t("stdinfo", () -> { StandardMBean m = new StandardMBean(new Hello("sm"), HelloMBean.class); MBeanInfo i = m.getMBeanInfo(); return i.getClassName() + " " + i.getDescription() + " " + i.getConstructors().length + " " + (m.getMBeanInfo() == i); });
    t("stdhooks", () -> { StandardMBean m = new StandardMBean(new Hello("sm"), HelloMBean.class) { protected String getDescription(MBeanAttributeInfo i) { return "A-" + i.getName(); } protected String getDescription(MBeanOperationInfo i) { return "O-" + i.getName(); } protected String getDescription(MBeanInfo i) { return "I"; } protected int getImpact(MBeanOperationInfo i) { return MBeanOperationInfo.ACTION; } protected String getParameterName(MBeanOperationInfo o, MBeanParameterInfo p, int n) { return "arg" + n; } }; MBeanInfo i = m.getMBeanInfo(); List<String> l = new ArrayList<>(); for (MBeanOperationInfo o : i.getOperations()) l.add(o.getName() + ":" + o.getDescription() + ":" + o.getImpact() + ":" + Arrays.toString(o.getSignature())); Collections.sort(l); return i.getDescription() + " " + l; });
  }
}

static class Notifications {
  static void t(String n, java.util.concurrent.Callable<Object> c) { try { System.out.println(n + ": " + c.call()); } catch (Throwable e) { System.out.println(n + ": !" + e.getClass().getName() + ":" + (e instanceof NullPointerException ? null : e.getMessage())); } }
  public static void run() throws Exception {
    NotificationBroadcasterSupport b = new NotificationBroadcasterSupport();
    List<String> got = new ArrayList<>();
    NotificationListener l = (n, hb) -> got.add(n.getType() + "/" + hb);
    NotificationFilterSupport f = new NotificationFilterSupport(); f.enableType("a.");
    t("remove0", () -> { b.removeNotificationListener(l); return "ok"; });
    t("remove3", () -> { b.removeNotificationListener(l, null, null); return "ok"; });
    t("addnull", () -> { b.addNotificationListener(null, null, null); return "ok"; });
    b.addNotificationListener(l, f, "h1");
    b.addNotificationListener(l, null, "h2");
    b.sendNotification(new Notification("a.b", "src", 1));
    b.sendNotification(new Notification("c.d", "src", 2));
    t("got", () -> got);
    t("remove3b", () -> { b.removeNotificationListener(l, f, "wrong"); return "ok"; });
    t("remove3c", () -> { b.removeNotificationListener(l, f, "h1"); return "ok"; });
    t("remove0b", () -> { b.removeNotificationListener(l); return "ok"; });
    t("remove0c", () -> { b.removeNotificationListener(l); return "ok"; });
    t("send null", () -> { b.sendNotification(null); return "ok"; });
    t("info", () -> b.getNotificationInfo().length);
    t("info2", () -> new NotificationBroadcasterSupport(new MBeanNotificationInfo(new String[] {"x"}, "N", "d")).getNotificationInfo().length);
    t("exec", () -> { List<String> g = new ArrayList<>(); NotificationBroadcasterSupport e = new NotificationBroadcasterSupport(r -> { g.add("queued"); r.run(); }); e.addNotificationListener((n, hb) -> g.add("handled"), null, null); e.sendNotification(new Notification("x", "s", 1)); return g; });
    t("listenerthrows", () -> { NotificationBroadcasterSupport e = new NotificationBroadcasterSupport(); e.addNotificationListener((n, hb) -> { throw new IllegalStateException("x"); }, null, null); e.sendNotification(new Notification("x", "s", 1)); return "ok"; });
    t("filterthrows", () -> { NotificationBroadcasterSupport e = new NotificationBroadcasterSupport(); List<String> g = new ArrayList<>(); e.addNotificationListener((n, hb) -> g.add("h"), n -> { throw new IllegalStateException("f"); }, null); e.sendNotification(new Notification("x", "s", 1)); return g; });
    Notification n = new Notification("t.y", "src", 5, 1234L, "msg");
    t("notif", () -> n.toString() + "|" + n.getTimeStamp() + "|" + n.getSequenceNumber() + "|" + n.getUserData() + "|" + n.getMessage());
    t("notif2", () -> new Notification("t", "src", 1).getMessage() + "|" + new Notification("t", "src", 1, "m").getMessage());
    t("notifsrc", () -> { n.setSource("other"); return n.getSource() + " " + n; });
    t("notifnullsrc", () -> new Notification("t", null, 1));
    t("acn", () -> new AttributeChangeNotification("src", 1, 2L, "m", "Att", "int", 1, 2).toString() + "|" + new AttributeChangeNotification("src", 1, 2L, "m", "Att", "int", 1, 2).getType());
    AttributeChangeNotificationFilter af = new AttributeChangeNotificationFilter();
    t("af", () -> { af.enableAttribute("Att"); return af.isNotificationEnabled(new AttributeChangeNotification("src", 1, 2L, "m", "Att", "int", 1, 2)) + " " + af.isNotificationEnabled(new AttributeChangeNotification("src", 1, 2L, "m", "Other", "int", 1, 2)) + " " + af.isNotificationEnabled(new Notification("jmx.attribute.change", "s", 1)) + " " + af.getEnabledAttributes(); });
    t("afnull", () -> { af.enableAttribute(null); return "ok"; });
    t("nfs", () -> { NotificationFilterSupport s = new NotificationFilterSupport(); s.enableType("a"); s.enableType("a"); s.enableType("b.c"); return s.getEnabledTypes() + " " + s.isNotificationEnabled(new Notification("ab", "s", 1)) + " " + s.isNotificationEnabled(new Notification("c", "s", 1)) + " " + s.isNotificationEnabled(new Notification(null, "s", 1)); });
    t("nfsnull", () -> { new NotificationFilterSupport().enableType(null); return "ok"; });
    t("attr", () -> new Attribute("a", 1) + " " + new Attribute("a", null) + " " + new Attribute("a", 1).equals(new Attribute("a", 1)) + " " + new Attribute("a", 1).hashCode() + " " + new Attribute("a", null).hashCode());
    t("attrnull", () -> new Attribute(null, 1));
    t("al", () -> { AttributeList l2 = new AttributeList(); l2.add(new Attribute("a", 1)); l2.add((Object) new Attribute("b", 2)); return l2 + " " + l2.asList().size(); });
    t("al2", () -> { AttributeList l2 = new AttributeList(); l2.add("junk"); return l2.asList(); });
    t("al3", () -> { AttributeList l2 = new AttributeList(new ArrayList<Attribute>(List.of(new Attribute("a", 1)))); l2.add("junk"); return l2; });
    t("al4", () -> { AttributeList l2 = new AttributeList(); l2.add(0, new Attribute("a", 1)); l2.add(5, new Attribute("a", 1)); return l2; });
    t("al5", () -> { AttributeList l2 = new AttributeList(); l2.set(0, new Attribute("a", 1)); return l2; });
    t("oi", () -> new ObjectInstance("d:a=b", "C") + " " + new ObjectInstance("d:a=b", null) + " " + new ObjectInstance("d:a=b", "C").equals(new ObjectInstance("d:a=b", "C")) + " " + new ObjectInstance("d:a=b", "C").hashCode());
    t("oipat", () -> new ObjectInstance("d:*", "C"));
    t("oibad", () -> new ObjectInstance("bad", "C"));
  }
}

static class Descriptors {
  static void t(String n, java.util.concurrent.Callable<Object> c) { try { Object o = c.call(); System.out.println(n + ": " + (o instanceof Object[] ? Arrays.deepToString((Object[]) o) : o)); } catch (Throwable e) { System.out.println(n + ": !" + e.getClass().getName() + ":" + (e instanceof NullPointerException ? null : e.getMessage()) + (e.getCause() != null ? " / " + e.getCause() : "")); } }
  public static void run() throws Exception {
    ImmutableDescriptor d = new ImmutableDescriptor("b=2", "A=1", "c=x");
    t("fields", () -> d.getFields());
    t("names", () -> d.getFieldNames());
    t("toString", () -> d);
    t("empty", () -> ImmutableDescriptor.EMPTY_DESCRIPTOR);
    t("val", () -> d.getFieldValue("a"));
    t("valmissing", () -> d.getFieldValue("zz"));
    t("valnull", () -> d.getFieldValue(null));
    t("valsmulti", () -> d.getFieldValues("a", "zz", "C"));
    t("valsall", () -> d.getFieldValues((String[]) null));
    t("badfield", () -> new ImmutableDescriptor("noequals"));
    t("emptyname", () -> new ImmutableDescriptor("=x"));
    t("nullstr", () -> new ImmutableDescriptor((String) null));
    t("dup", () -> new ImmutableDescriptor("a=1", "A=2"));
    t("arrs", () -> new ImmutableDescriptor(new String[] {"x", "y"}, new Object[] {1}));
    t("arrs2", () -> new ImmutableDescriptor(new String[] {"x", ""}, new Object[] {1, 2}));
    t("arrs3", () -> new ImmutableDescriptor(new String[] {"x", null}, new Object[] {1, 2}));
    t("arrs4", () -> new ImmutableDescriptor(new String[] {"x", "y"}, null));
    t("map", () -> new ImmutableDescriptor(Map.of("Q", 1)));
    t("map2", () -> { Map<String,Object> m = new HashMap<>(); m.put("a", 1); m.put("A", 2); return new ImmutableDescriptor(m); });
    t("map3", () -> { Map<String,Object> m = new HashMap<>(); m.put(null, 1); return new ImmutableDescriptor(m); });
    t("map4", () -> { Map<String,Object> m = new HashMap<>(); m.put("", 1); return new ImmutableDescriptor(m); });
    t("mapnull", () -> new ImmutableDescriptor((Map<String,?>) null));
    t("eq", () -> new ImmutableDescriptor("A=1", "B=2", "C=x").equals(d));
    t("eqh", () -> new ImmutableDescriptor("A=1", "B=2", "C=x").hashCode() == d.hashCode());
    t("hash", () -> d.hashCode());
    t("hashE", () -> ImmutableDescriptor.EMPTY_DESCRIPTOR.hashCode());
    t("set", () -> { d.setField("a", 1); return "ok-same"; });
    t("set2", () -> { d.setField("a", 5); return "ok"; });
    t("set3", () -> { d.setField("z", 5); return "ok"; });
    t("setN", () -> { d.setField(null, 5); return "ok"; });
    t("setE", () -> { d.setField("", 5); return "ok"; });
    t("sets", () -> { d.setFields(new String[] {"a"}, new Object[] {1}); return "ok"; });
    t("sets2", () -> { d.setFields(new String[] {"a"}, new Object[] {1, 2}); return "ok"; });
    t("sets3", () -> { d.setFields(null, new Object[] {1, 2}); return "ok"; });
    t("sets4", () -> { d.setFields(new String[] {"a"}, null); return "ok"; });
    t("rem", () -> { d.removeField("zz"); return "ok"; });
    t("rem2", () -> { d.removeField("a"); return "ok"; });
    t("remN", () -> { d.removeField(null); return "ok"; });
    t("clone", () -> d.clone() == d);
    t("valid", () -> d.isValid());
    t("union", () -> ImmutableDescriptor.union(d, new ImmutableDescriptor("d=4", "A=9")));
    t("union0", () -> ImmutableDescriptor.union());
    t("union1", () -> ImmutableDescriptor.union(d) == d);
    t("union2", () -> ImmutableDescriptor.union((Descriptor) null, d) == d);
    t("union3", () -> ImmutableDescriptor.union(ImmutableDescriptor.EMPTY_DESCRIPTOR) == ImmutableDescriptor.EMPTY_DESCRIPTOR);
    t("arrval", () -> new ImmutableDescriptor(new String[] {"x", "y"}, new Object[] {new int[] {1, 2}, new String[] {"s"}}));
    t("arrval2", () -> new ImmutableDescriptor(new String[] {"x"}, new Object[] {null}));
    t("arrvalEq", () -> new ImmutableDescriptor(new String[] {"x"}, new Object[] {new int[] {1}}).equals(new ImmutableDescriptor(new String[] {"x"}, new Object[] {new int[] {1}})));
    t("getfieldvals", () -> new ImmutableDescriptor(new String[] {"x"}, new Object[] {new int[] {1}}).getFieldValue("x").getClass().getSimpleName());
    t("cloneArr", () -> { int[] orig = {1}; ImmutableDescriptor q = new ImmutableDescriptor(new String[] {"x"}, new Object[] {orig}); orig[0] = 9; return ((int[]) q.getFieldValue("x"))[0]; });
    t("nulldescr", () -> new ImmutableDescriptor(new String[0], new Object[0]) == ImmutableDescriptor.EMPTY_DESCRIPTOR);
    t("numvals", () -> new ImmutableDescriptor("n=5").getFieldValue("n").getClass().getSimpleName());
    t("eqdesc", () -> d.equals(null) + " " + d.equals("x"));
  }
}

static class Infos {
  static void t(String n, java.util.concurrent.Callable<Object> c) { try { Object o = c.call(); System.out.println(n + ": " + (o instanceof Object[] ? Arrays.deepToString((Object[]) o) : o)); } catch (Throwable e) { System.out.println(n + ": !" + e.getClass().getName() + ":" + (e instanceof NullPointerException ? null : e.getMessage()) + (e.getCause() != null ? " / " + e.getCause() : "")); } }
  public interface Foo { int getA(); void setA(int a); boolean isB(); String getC(); void setD(String d); void doIt(String x, int y); int calc(); }
  public static void run() throws Exception {
    Descriptor desc = new ImmutableDescriptor("k=v");
    t("attr1", () -> new MBeanAttributeInfo("n", "java.lang.String", "dsc", true, false, false));
    t("attr2", () -> new MBeanAttributeInfo("n", "boolean", "dsc", true, true, true));
    t("attr3", () -> new MBeanAttributeInfo("n", "int", null, false, true, false, desc));
    t("attr4", () -> new MBeanAttributeInfo("n", "int", null, false, false, false));
    t("attr5", () -> new MBeanAttributeInfo("n", "int", null, true, true, true));
    t("attr6", () -> new MBeanAttributeInfo("n", "int", null, true, false, true));
    t("attrNullName", () -> new MBeanAttributeInfo(null, "int", null, true, false, false));
    t("attrNullType", () -> new MBeanAttributeInfo("n", null, null, true, false, false));
    t("attrM", () -> new MBeanAttributeInfo("a", "d", Foo.class.getMethod("getA"), Foo.class.getMethod("setA", int.class)));
    t("attrM2", () -> new MBeanAttributeInfo("b", "d", Foo.class.getMethod("isB"), null));
    t("attrM3", () -> new MBeanAttributeInfo("d", "d", null, Foo.class.getMethod("setD", String.class)));
    t("attrM4", () -> new MBeanAttributeInfo("a", "d", Foo.class.getMethod("calc"), Foo.class.getMethod("setD", String.class)));
    t("attrM5", () -> new MBeanAttributeInfo("a", "d", Foo.class.getMethod("doIt", String.class, int.class), null));
    t("attrM6", () -> new MBeanAttributeInfo("a", "d", null, null));
    t("attrM7", () -> new MBeanAttributeInfo("a", "d", Foo.class.getMethod("getC"), Foo.class.getMethod("setA", int.class)));
    t("attrM8", () -> new MBeanAttributeInfo("a", "d", Foo.class.getMethod("setA", int.class), null));
    t("attrFlags", () -> { MBeanAttributeInfo i = new MBeanAttributeInfo("b", "d", Foo.class.getMethod("isB"), null); return i.isReadable() + " " + i.isWritable() + " " + i.isIs() + " " + i.getType(); });
    t("attrEq", () -> new MBeanAttributeInfo("n", "int", "x", true, false, false).equals(new MBeanAttributeInfo("n", "int", "x", true, false, false)));
    t("attrEq2", () -> new MBeanAttributeInfo("n", "int", "x", true, false, false).equals(new MBeanAttributeInfo("n", "int", "y", true, false, false)));
    t("attrHash", () -> new MBeanAttributeInfo("n", "int", "x", true, false, false).hashCode());
    t("attrHashNullDesc", () -> new MBeanAttributeInfo("n", "int", null, true, false, false).hashCode());
    t("attrClone", () -> { MBeanAttributeInfo i = new MBeanAttributeInfo("n", "int", "x", true, false, false); Object c = i.clone(); return c != i && c.equals(i); });
    t("attrDesc", () -> new MBeanAttributeInfo("n", "int", "x", true, false, false).getDescriptor());
    t("attrDesc2", () -> new MBeanAttributeInfo("n", "int", "x", true, false, false, desc).getDescriptor());
    t("opM", () -> new MBeanOperationInfo("d", Foo.class.getMethod("doIt", String.class, int.class)));
    t("opM2", () -> new MBeanOperationInfo("d", Foo.class.getMethod("calc")));
    t("op1", () -> new MBeanOperationInfo("o", "dsc", new MBeanParameterInfo[] {new MBeanParameterInfo("p", "int", "pd")}, "void", MBeanOperationInfo.ACTION));
    t("op2", () -> new MBeanOperationInfo("o", "dsc", null, "void", MBeanOperationInfo.INFO));
    t("op3", () -> new MBeanOperationInfo("o", "dsc", new MBeanParameterInfo[0], "void", 7));
    t("op4", () -> new MBeanOperationInfo("o", "dsc", new MBeanParameterInfo[0], "void", MBeanOperationInfo.UNKNOWN, desc));
    t("op5", () -> new MBeanOperationInfo("o", "dsc", new MBeanParameterInfo[0], "void", MBeanOperationInfo.ACTION_INFO));
    t("opSig", () -> new MBeanOperationInfo("o", "dsc", new MBeanParameterInfo[] {new MBeanParameterInfo("p", "int", "pd")}, "void", 1).getSignature().length);
    t("opEq", () -> new MBeanOperationInfo("o", "dsc", null, "void", 1).equals(new MBeanOperationInfo("o", "dsc", new MBeanParameterInfo[0], "void", 1)));
    t("opHash", () -> new MBeanOperationInfo("o", "dsc", null, "void", 1).hashCode());
    t("opImpact", () -> new MBeanOperationInfo("o", "dsc", null, "void", 1).getImpact() + " " + new MBeanOperationInfo("o", "dsc", null, "void", 1).getReturnType());
    t("ctorI", () -> new MBeanConstructorInfo("c", "dsc", new MBeanParameterInfo[] {new MBeanParameterInfo("p", "int", "pd")}));
    t("ctorI2", () -> new MBeanConstructorInfo("c", "dsc", null, desc));
    t("ctorM", () -> new MBeanConstructorInfo("d", ArrayList.class.getConstructor(int.class)));
    t("ctorM2", () -> new MBeanConstructorInfo("d", ArrayList.class.getConstructor()));
    t("ctorEq", () -> new MBeanConstructorInfo("c", "dsc", null).equals(new MBeanConstructorInfo("c", "dsc", new MBeanParameterInfo[0])));
    t("ctorHash", () -> new MBeanConstructorInfo("c", "dsc", null).hashCode());
    t("param", () -> new MBeanParameterInfo("p", "int", "pd"));
    t("param2", () -> new MBeanParameterInfo("p", "int", null, desc));
    t("paramEq", () -> new MBeanParameterInfo("p", "int", "pd").equals(new MBeanParameterInfo("p", "int", "pd")));
    t("paramHash", () -> new MBeanParameterInfo("p", "int", "pd").hashCode());
    t("notif", () -> new MBeanNotificationInfo(new String[] {"a.b", "c"}, "N", "ndesc"));
    t("notif2", () -> new MBeanNotificationInfo(null, "N", "ndesc", desc));
    t("notifTypes", () -> new MBeanNotificationInfo(new String[] {"a.b", "c"}, "N", "ndesc").getNotifTypes());
    t("notifTypes2", () -> new MBeanNotificationInfo(null, "N", "ndesc").getNotifTypes());
    t("notifEq", () -> new MBeanNotificationInfo(new String[] {"a"}, "N", "ndesc").equals(new MBeanNotificationInfo(new String[] {"a"}, "N", "ndesc")));
    t("notifHash", () -> new MBeanNotificationInfo(new String[] {"a"}, "N", "ndesc").hashCode());
    MBeanAttributeInfo[] as = {new MBeanAttributeInfo("n", "int", "x", true, false, false)};
    MBeanOperationInfo[] os = {new MBeanOperationInfo("o", "dsc", null, "void", 1)};
    MBeanConstructorInfo[] cs = {new MBeanConstructorInfo("c", "dsc", null)};
    MBeanNotificationInfo[] ns = {new MBeanNotificationInfo(new String[] {"a"}, "N", "ndesc")};
    t("info", () -> new MBeanInfo("cls", "dd", as, cs, os, ns));
    t("info2", () -> new MBeanInfo("cls", null, null, null, null, null));
    t("info3", () -> new MBeanInfo("cls", "dd", as, cs, os, ns, desc));
    t("infoDesc", () -> new MBeanInfo("cls", "dd", as, cs, os, ns).getDescriptor());
    t("infoEq", () -> new MBeanInfo("cls", "dd", as, cs, os, ns).equals(new MBeanInfo("cls", "dd", as, cs, os, ns)));
    t("infoHash", () -> new MBeanInfo("cls", "dd", as, cs, os, ns).hashCode());
    t("infoHash2", () -> new MBeanInfo("cls", null, null, null, null, null).hashCode());
    t("infoArr", () -> new MBeanInfo("cls", null, null, null, null, null).getAttributes().length);
    t("infoClone", () -> { MBeanInfo i = new MBeanInfo("cls", "dd", as, cs, os, ns); Object c = i.clone(); return c != i && c.equals(i); });
    t("infoArrNullElem", () -> new MBeanInfo("cls", "dd", new MBeanAttributeInfo[] {null}, cs, os, ns));
    t("featDesc", () -> new MBeanFeatureInfo("a", "b") {}.getDescriptor());
    t("featEq", () -> new MBeanFeatureInfo("a", "b") {}.equals(new MBeanFeatureInfo("a", "b") {}));
  }
}

  public static void main(String[] args) throws Exception {
    java.util.Locale.setDefault(java.util.Locale.ENGLISH);
    System.out.println("##### Server");
    Server.run();
    System.out.println("##### Notifications");
    Notifications.run();
    System.out.println("##### Descriptors");
    Descriptors.run();
    System.out.println("##### Infos");
    Infos.run();
  }
}
