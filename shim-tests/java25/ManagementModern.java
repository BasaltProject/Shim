/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.lang.management.*;
import java.util.*;
import javax.management.*;
public class ManagementModern {
  static void t(String n, java.util.concurrent.Callable<Object> c) { try { Object o = c.call(); System.out.println(n + ": " + o); } catch (Throwable e) { System.out.println(n + ": !" + e.getClass().getName() + ":" + e.getMessage()); } }
  public static void main(String[] a) throws Exception {
    t("ifaces", () -> { Set<String> l = new TreeSet<>(); for (Class<?> c : ManagementFactory.getPlatformManagementInterfaces()) l.add(c.getSimpleName()); return l.containsAll(List.of("BufferPoolMXBean", "ClassLoadingMXBean", "CompilationMXBean", "GarbageCollectorMXBean", "MemoryMXBean", "MemoryManagerMXBean", "MemoryPoolMXBean", "OperatingSystemMXBean", "PlatformLoggingMXBean", "RuntimeMXBean", "ThreadMXBean")); });
    t("gc", () -> { for (GarbageCollectorMXBean g : ManagementFactory.getGarbageCollectorMXBeans()) if (!g.getObjectName().toString().startsWith("java.lang:type=GarbageCollector,name=") || !g.isValid() || g.getMemoryPoolNames().length == 0) return false; return !ManagementFactory.getGarbageCollectorMXBeans().isEmpty(); });
    t("pools", () -> { for (MemoryPoolMXBean p : ManagementFactory.getMemoryPoolMXBeans()) if (!p.getObjectName().toString().startsWith("java.lang:type=MemoryPool,name=") || p.getType() == null) return false; return !ManagementFactory.getMemoryPoolMXBeans().isEmpty(); });
    t("mgrs", () -> ManagementFactory.getMemoryManagerMXBeans().size() >= ManagementFactory.getGarbageCollectorMXBeans().size());
    t("bufpools", () -> { Set<String> n = new TreeSet<>(); for (BufferPoolMXBean p : ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class)) { n.add(p.getName() + "/" + p.getObjectName()); } return n.contains("direct/java.nio:type=BufferPool,name=direct") && n.contains("mapped/java.nio:type=BufferPool,name=mapped"); });
    t("mbsnames", () -> { Set<ObjectName> n = ManagementFactory.getPlatformMBeanServer().queryNames(new ObjectName("java.lang:*"), null); return n.containsAll(List.of(new ObjectName("java.lang:type=Runtime"), new ObjectName("java.lang:type=Memory"), new ObjectName("java.lang:type=Threading"), new ObjectName("java.lang:type=ClassLoading"), new ObjectName("java.lang:type=OperatingSystem"), new ObjectName("java.lang:type=Compilation"))); });
    t("mbsall", () -> ManagementFactory.getPlatformMBeanServer().isRegistered(MBeanServerDelegate.DELEGATE_NAME));
    t("mbsdomains", () -> { List<String> d = Arrays.asList(ManagementFactory.getPlatformMBeanServer().getDomains()); return d.contains("java.lang") && d.contains("JMImplementation") && d.contains("java.nio") && d.contains("java.util.logging"); });
    t("mbinfoattrs", () -> { List<String> l = new ArrayList<>(); for (MBeanAttributeInfo i : ManagementFactory.getPlatformMBeanServer().getMBeanInfo(new ObjectName("java.lang:type=ClassLoading")).getAttributes()) l.add(i.getName() + ":" + i.getType() + ":" + i.isReadable() + i.isWritable() + i.isIs()); Collections.sort(l); return l; });
    t("mbdescattr", () -> { List<String> l = new ArrayList<>(); for (MBeanAttributeInfo i : ManagementFactory.getPlatformMBeanServer().getMBeanInfo(new ObjectName("java.lang:type=ClassLoading")).getAttributes()) l.add(i.getName() + "=" + i.getDescription() + " " + i.getDescriptor()); Collections.sort(l); return l; });
    t("rt", () -> { RuntimeMXBean r = ManagementFactory.getRuntimeMXBean(); return r.getSpecName() + "|" + r.getSpecVendor() + "|" + r.getSpecVersion() + "|" + r.getManagementSpecVersion() + "|" + r.isBootClassPathSupported() + "|" + (r.getPid() == ProcessHandle.current().pid()) + "|" + r.getName().endsWith("@" + java.net.InetAddress.getLocalHost().getHostName()) + "|" + (r.getUptime() >= 0) + "|" + (r.getStartTime() > 0) + "|" + r.getClassPath().equals(System.getProperty("java.class.path")) + "|" + (r.getSystemProperties().get("java.version") != null) + "|" + (r.getVmName() != null) + "|" + r.getInputArguments().getClass().getSimpleName().isEmpty(); });
    t("thrid", () -> { ThreadMXBean m = ManagementFactory.getThreadMXBean(); ThreadInfo ti = m.getThreadInfo(Thread.currentThread().threadId()); return ti.getThreadName() + " " + (ti.getThreadId() == Thread.currentThread().threadId()) + " " + ti.getThreadState() + " " + ti.getPriority() + " " + ti.isDaemon() + " " + ti.getBlockedCount() + " " + ti.getLockInfo() + " " + ti.getLockOwnerId() + " " + ti.getStackTrace().length; });
    t("thrstr", () -> { ThreadInfo ti = ManagementFactory.getThreadMXBean().getThreadInfo(Thread.currentThread().threadId()); return ti.toString().startsWith("\"" + Thread.currentThread().getName() + "\" prio=" + Thread.currentThread().getPriority() + " Id=" + Thread.currentThread().threadId() + " RUNNABLE\n"); });
    t("memusage", () -> { MemoryUsage u = new MemoryUsage(1024, 2048, 4096, 8192); return u + " " + u.getInit() + " " + u.getUsed() + " " + u.getCommitted() + " " + u.getMax(); });
    t("memusagebad", () -> new MemoryUsage(1, 5, 3, 10));
    t("memusagebad2", () -> new MemoryUsage(-2, 1, 3, 10));
    t("memusagebad3", () -> new MemoryUsage(0, 1, 30, 10));
    t("memtype", () -> MemoryType.HEAP + " " + MemoryType.NON_HEAP + " " + MemoryType.valueOf("HEAP").name());
    t("lockinfo", () -> new LockInfo("a.B", 255) + " " + new MonitorInfo("a.B", 255, 2, new StackTraceElement("C", "m", "C.java", 7)).getLockedStackFrame());
    t("lockinfobad", () -> new MonitorInfo("a.B", 255, 2, null));
    t("mgmtperm", () -> new ManagementPermission("monitor").getName() + " " + new ManagementPermission("control").implies(new ManagementPermission("control")));
    t("mgmtpermbad", () -> new ManagementPermission("x"));
    t("composite", () -> { MemoryUsage u = new MemoryUsage(1, 2, 3, 4); Object cd = ManagementFactory.getPlatformMBeanServer().getAttribute(new ObjectName("java.lang:type=Memory"), "HeapMemoryUsage"); return cd instanceof javax.management.openmbean.CompositeData && MemoryUsage.from((javax.management.openmbean.CompositeData) cd).getMax() >= -1; });
    t("memproxy", () -> { MemoryMXBean p = ManagementFactory.newPlatformMXBeanProxy(ManagementFactory.getPlatformMBeanServer(), "java.lang:type=Memory", MemoryMXBean.class); return p.getHeapMemoryUsage().getUsed() >= 0; });
    t("single", () -> ManagementFactory.getPlatformMXBean(RuntimeMXBean.class).getObjectName());
    t("multi", () -> ManagementFactory.getPlatformMXBean(GarbageCollectorMXBean.class));
    t("multi2", () -> ManagementFactory.getPlatformMXBean(MemoryPoolMXBean.class));
    t("notplat", () -> ManagementFactory.getPlatformMXBean((Class) Runnable.class));
    t("notplat2", () -> ManagementFactory.getPlatformMXBeans((Class) Runnable.class));
    t("plist", () -> ManagementFactory.getPlatformMXBeans(RuntimeMXBean.class).size());
    t("os", () -> ManagementFactory.getOperatingSystemMXBean().getObjectName() + " " + ManagementFactory.getOperatingSystemMXBean().getClass().getInterfaces().length);
    t("names", () -> ManagementFactory.getClassLoadingMXBean().getObjectName() + " " + ManagementFactory.getMemoryMXBean().getObjectName() + " " + ManagementFactory.getThreadMXBean().getObjectName() + " " + (ManagementFactory.getCompilationMXBean() == null ? null : ManagementFactory.getCompilationMXBean().getObjectName()));
    t("mbs", () -> ManagementFactory.getPlatformMBeanServer() == ManagementFactory.getPlatformMBeanServer());
    t("mbsdef", () -> ManagementFactory.getPlatformMBeanServer().getDefaultDomain());
    t("mbscount", () -> ManagementFactory.getPlatformMBeanServer().getMBeanCount() > 5);
    t("attr", () -> ManagementFactory.getPlatformMBeanServer().getAttribute(new ObjectName("java.lang:type=Runtime"), "SpecName"));
    t("attrType", () -> ManagementFactory.getPlatformMBeanServer().getAttribute(new ObjectName("java.lang:type=Memory"), "HeapMemoryUsage").getClass().getName());
    t("attrmissing", () -> ManagementFactory.getPlatformMBeanServer().getAttribute(new ObjectName("java.lang:type=Runtime"), "Nope"));
    t("mbinfo", () -> ManagementFactory.getPlatformMBeanServer().getMBeanInfo(new ObjectName("java.lang:type=ClassLoading")).getClassName());
    t("mbinfoops", () -> { StringBuilder sb = new StringBuilder(); for (MBeanOperationInfo i : ManagementFactory.getPlatformMBeanServer().getMBeanInfo(new ObjectName("java.lang:type=Memory")).getOperations()) sb.append(i.getName()).append(":").append(i.getReturnType()).append(":").append(i.getImpact()).append(";"); return sb; });
    t("mbdesc", () -> ManagementFactory.getPlatformMBeanServer().getMBeanInfo(new ObjectName("java.lang:type=ClassLoading")).getDescription());
    t("mbdescdescr", () -> ManagementFactory.getPlatformMBeanServer().getMBeanInfo(new ObjectName("java.lang:type=ClassLoading")).getDescriptor());
    t("mbdescdescrop", () -> ManagementFactory.getPlatformMBeanServer().getMBeanInfo(new ObjectName("java.lang:type=Memory")).getOperations()[0].getDescriptor());
    t("proxy", () -> ManagementFactory.newPlatformMXBeanProxy(ManagementFactory.getPlatformMBeanServer(), "java.lang:type=Runtime", RuntimeMXBean.class).getSpecName());
    t("proxybad", () -> ManagementFactory.newPlatformMXBeanProxy(ManagementFactory.getPlatformMBeanServer(), "java.lang:type=Runtime", MemoryMXBean.class));
    t("rtbcp", () -> ManagementFactory.getRuntimeMXBean().getBootClassPath());
    t("mem", () -> { MemoryMXBean m = ManagementFactory.getMemoryMXBean(); MemoryUsage h = m.getHeapMemoryUsage(); return (h.getUsed() <= h.getCommitted()) + " " + m.isVerbose() + " " + m.getObjectPendingFinalizationCount(); });
    t("memverbose", () -> { MemoryMXBean m = ManagementFactory.getMemoryMXBean(); m.setVerbose(true); boolean v = m.isVerbose(); m.setVerbose(false); return v; });
    t("os2", () -> { OperatingSystemMXBean o = ManagementFactory.getOperatingSystemMXBean(); return o.getName().equals(System.getProperty("os.name")) + " " + o.getArch().equals(System.getProperty("os.arch")) + " " + o.getVersion().equals(System.getProperty("os.version")) + " " + (o.getAvailableProcessors() == Runtime.getRuntime().availableProcessors()); });
    t("thr2", () -> { ThreadMXBean m = ManagementFactory.getThreadMXBean(); return m.getThreadInfo(0L); });
    t("thr3", () -> { ThreadMXBean m = ManagementFactory.getThreadMXBean(); return m.getThreadInfo(999999L); });
    t("thr4", () -> { ThreadMXBean m = ManagementFactory.getThreadMXBean(); return m.getThreadInfo(new long[] {Thread.currentThread().threadId(), 999999L}).length; });
    t("thr5", () -> { ThreadMXBean m = ManagementFactory.getThreadMXBean(); return m.getThreadInfo(Thread.currentThread().threadId(), -1); });
    t("thr6", () -> { ThreadMXBean m = ManagementFactory.getThreadMXBean(); return m.getThreadInfo(Thread.currentThread().threadId(), 0).getStackTrace().length; });
    t("thr7", () -> { ThreadMXBean m = ManagementFactory.getThreadMXBean(); return m.getThreadInfo(Thread.currentThread().threadId(), Integer.MAX_VALUE).getStackTrace().length > 0; });
    t("thr8", () -> { ThreadMXBean m = ManagementFactory.getThreadMXBean(); return m.getThreadInfo(new long[] {-5L}); });
    t("thr9", () -> { ThreadMXBean m = ManagementFactory.getThreadMXBean(); return m.dumpAllThreads(false, false).length >= 1; });
    t("thr10", () -> { ThreadMXBean m = ManagementFactory.getThreadMXBean(); return m.getCurrentThreadCpuTime() >= 0; });
    t("thrcpu", () -> { ThreadMXBean m = ManagementFactory.getThreadMXBean(); return m.isThreadCpuTimeEnabled() + " " + m.isThreadContentionMonitoringEnabled(); });
    t("clm", () -> { ClassLoadingMXBean c = ManagementFactory.getClassLoadingMXBean(); return (c.getLoadedClassCount() > 0) + " " + (c.getTotalLoadedClassCount() >= c.getLoadedClassCount()) + " " + (c.getUnloadedClassCount() >= 0) + " " + c.isVerbose(); });
    t("comp", () -> { CompilationMXBean c = ManagementFactory.getCompilationMXBean(); return c == null ? "null" : (c.getName() != null) + " " + c.isCompilationTimeMonitoringSupported(); });
    t("gc2", () -> { GarbageCollectorMXBean g = ManagementFactory.getGarbageCollectorMXBeans().get(0); return (g.getCollectionCount() >= -1) + " " + (g.getCollectionTime() >= -1) + " " + (g.getName() != null); });
  }
}
