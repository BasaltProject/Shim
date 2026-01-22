/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.management;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.management.InstanceNotFoundException;
import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import sun.management.BufferPoolImpl;
import sun.management.ClassLoadingImpl;
import sun.management.CompilationImpl;
import sun.management.GarbageCollectorImpl;
import sun.management.LoggingImpl;
import sun.management.MemoryImpl;
import sun.management.MemoryPoolImpl;
import sun.management.OperatingSystemImpl;
import sun.management.RuntimeImpl;
import sun.management.ThreadImpl;

public class ManagementFactory {
  public static final String CLASS_LOADING_MXBEAN_NAME = "java.lang:type=ClassLoading";
  public static final String COMPILATION_MXBEAN_NAME = "java.lang:type=Compilation";
  public static final String MEMORY_MXBEAN_NAME = "java.lang:type=Memory";
  public static final String OPERATING_SYSTEM_MXBEAN_NAME = "java.lang:type=OperatingSystem";
  public static final String RUNTIME_MXBEAN_NAME = "java.lang:type=Runtime";
  public static final String THREAD_MXBEAN_NAME = "java.lang:type=Threading";
  public static final String GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE = "java.lang:type=GarbageCollector";
  public static final String MEMORY_MANAGER_MXBEAN_DOMAIN_TYPE = "java.lang:type=MemoryManager";
  public static final String MEMORY_POOL_MXBEAN_DOMAIN_TYPE = "java.lang:type=MemoryPool";

  private static ClassLoadingMXBean classLoading;
  private static CompilationMXBean compilation;
  private static MemoryMXBean memory;
  private static OperatingSystemMXBean operatingSystem;
  private static RuntimeMXBean runtime;
  private static ThreadMXBean threads;
  private static PlatformLoggingMXBean logging;
  private static List<GarbageCollectorMXBean> collectors;
  private static List<MemoryPoolMXBean> pools;
  private static List<BufferPoolMXBean> bufferPools;
  private static MBeanServer platformMBeanServer;

  private ManagementFactory() {
  }

  public static synchronized ClassLoadingMXBean getClassLoadingMXBean() {
    if (classLoading == null) {
      classLoading = new ClassLoadingImpl();
    }
    return classLoading;
  }

  public static synchronized MemoryMXBean getMemoryMXBean() {
    if (memory == null) {
      memory = new MemoryImpl();
    }
    return memory;
  }

  public static synchronized ThreadMXBean getThreadMXBean() {
    if (threads == null) {
      threads = new ThreadImpl();
    }
    return threads;
  }

  public static synchronized RuntimeMXBean getRuntimeMXBean() {
    if (runtime == null) {
      runtime = new RuntimeImpl();
    }
    return runtime;
  }

  public static synchronized CompilationMXBean getCompilationMXBean() {
    if (compilation == null) {
      compilation = new CompilationImpl();
    }
    return compilation;
  }

  public static synchronized OperatingSystemMXBean getOperatingSystemMXBean() {
    if (operatingSystem == null) {
      operatingSystem = new OperatingSystemImpl();
    }
    return operatingSystem;
  }

  private static synchronized PlatformLoggingMXBean loggingBean() {
    if (logging == null) {
      logging = new LoggingImpl();
    }
    return logging;
  }

  public static synchronized List<MemoryPoolMXBean> getMemoryPoolMXBeans() {
    if (pools == null) {
      List<MemoryPoolMXBean> list = new ArrayList<MemoryPoolMXBean>();
      list.add(new MemoryPoolImpl());
      pools = Collections.unmodifiableList(list);
    }
    return pools;
  }

  public static List<MemoryManagerMXBean> getMemoryManagerMXBeans() {
    return new ArrayList<MemoryManagerMXBean>(getGarbageCollectorMXBeans());
  }

  public static synchronized List<GarbageCollectorMXBean> getGarbageCollectorMXBeans() {
    if (collectors == null) {
      List<GarbageCollectorMXBean> list = new ArrayList<GarbageCollectorMXBean>();
      list.add(new GarbageCollectorImpl());
      collectors = Collections.unmodifiableList(list);
    }
    return collectors;
  }

  private static synchronized List<BufferPoolMXBean> getBufferPools() {
    if (bufferPools == null) {
      List<BufferPoolMXBean> list = new ArrayList<BufferPoolMXBean>();
      list.add(newBufferPool("mapped"));
      list.add(newBufferPool("direct"));
      bufferPools = Collections.unmodifiableList(list);
    }
    return bufferPools;
  }

  private static BufferPoolMXBean newBufferPool(String name) {
    try {
      java.lang.reflect.Constructor<BufferPoolImpl> c = BufferPoolImpl.class.getDeclaredConstructor(String.class);
      c.setAccessible(true);
      return c.newInstance(name);
    } catch (Exception e) {
      throw new InternalError(e);
    }
  }

  public static synchronized MBeanServer getPlatformMBeanServer() {
    if (platformMBeanServer == null) {
      platformMBeanServer = MBeanServerFactory.createMBeanServer();
      try {
        for (PlatformManagedObject o : allBeans()) {
          platformMBeanServer.registerMBean(o, o.getObjectName());
        }
      } catch (Exception e) {
        throw new InternalError(e);
      }
    }
    return platformMBeanServer;
  }

  private static com.sun.management.HotSpotDiagnosticMXBean hotSpot;

  private static synchronized com.sun.management.HotSpotDiagnosticMXBean hotSpotDiagnostic() {
    if (hotSpot == null) {
      hotSpot = new sun.management.HotSpotDiagnosticImpl();
    }
    return hotSpot;
  }

  private static List<PlatformManagedObject> allBeans() {
    List<PlatformManagedObject> all = new ArrayList<PlatformManagedObject>();
    all.add(getClassLoadingMXBean());
    all.add(getCompilationMXBean());
    all.add(getMemoryMXBean());
    all.add(getOperatingSystemMXBean());
    all.add(getRuntimeMXBean());
    all.add(getThreadMXBean());
    all.add(loggingBean());
    all.add(hotSpotDiagnostic());
    all.addAll(getGarbageCollectorMXBeans());
    all.addAll(getMemoryPoolMXBeans());
    all.addAll(getBufferPools());
    return all;
  }

  public static <T> T newPlatformMXBeanProxy(MBeanServerConnection connection, String mxbeanName,
      Class<T> mxbeanInterface) throws IOException {
    ObjectName objName;
    try {
      objName = new ObjectName(mxbeanName);
    } catch (javax.management.MalformedObjectNameException e) {
      throw new IllegalArgumentException(e);
    }
    try {
      if (!connection.isInstanceOf(objName, mxbeanInterface.getName())) {
        throw new IllegalArgumentException(mxbeanName + " is not an instance of " + mxbeanInterface);
      }
    } catch (InstanceNotFoundException e) {
      throw new IllegalArgumentException(mxbeanName + " not found in the connection.");
    }
    return JMX.newMXBeanProxy(connection, objName, mxbeanInterface);
  }

  private static boolean isPlatformInterface(Class<?> c) {
    return c == ClassLoadingMXBean.class || c == CompilationMXBean.class || c == MemoryMXBean.class
        || c == OperatingSystemMXBean.class || c == RuntimeMXBean.class || c == ThreadMXBean.class
        || c == PlatformLoggingMXBean.class || c == GarbageCollectorMXBean.class || c == MemoryManagerMXBean.class
        || c == MemoryPoolMXBean.class || c == BufferPoolMXBean.class
        || c == com.sun.management.HotSpotDiagnosticMXBean.class
        || c == com.sun.management.OperatingSystemMXBean.class || c == com.sun.management.UnixOperatingSystemMXBean.class
        || c == com.sun.management.ThreadMXBean.class || c == com.sun.management.GarbageCollectorMXBean.class;
  }

  private static boolean isSingleton(Class<?> c) {
    return !(c == GarbageCollectorMXBean.class || c == MemoryManagerMXBean.class || c == MemoryPoolMXBean.class
        || c == BufferPoolMXBean.class);
  }

  public static <T extends PlatformManagedObject> T getPlatformMXBean(Class<T> mxbeanInterface) {
    String name = mxbeanInterface.getName();
    if (!isPlatformInterface(mxbeanInterface)) {
      throw new IllegalArgumentException(name + " is not a platform management interface");
    }
    if (!isSingleton(mxbeanInterface)) {
      throw new IllegalArgumentException(name + " can have more than one instance");
    }
    return mxbeanInterface.cast(getPlatformMXBeans(mxbeanInterface).get(0));
  }

  public static <T extends PlatformManagedObject> List<T> getPlatformMXBeans(Class<T> mxbeanInterface) {
    if (!isPlatformInterface(mxbeanInterface)) {
      throw new IllegalArgumentException(mxbeanInterface.getName() + " is not a platform management interface");
    }
    List<T> out = new ArrayList<T>();
    for (PlatformManagedObject o : allBeans()) {
      if (mxbeanInterface.isInstance(o)) {
        out.add(mxbeanInterface.cast(o));
      }
    }
    return Collections.unmodifiableList(out);
  }

  public static <T extends PlatformManagedObject> T getPlatformMXBean(MBeanServerConnection connection,
      Class<T> mxbeanInterface) throws IOException {
    String name = mxbeanInterface.getName();
    if (!isPlatformInterface(mxbeanInterface)) {
      throw new IllegalArgumentException(name + " is not a platform management interface");
    }
    if (!isSingleton(mxbeanInterface)) {
      throw new IllegalArgumentException(name + " can have more than one instance");
    }
    List<T> list = getPlatformMXBeans(connection, mxbeanInterface);
    return list.isEmpty() ? null : list.get(0);
  }

  public static <T extends PlatformManagedObject> List<T> getPlatformMXBeans(MBeanServerConnection connection,
      Class<T> mxbeanInterface) throws IOException {
    if (!isPlatformInterface(mxbeanInterface)) {
      throw new IllegalArgumentException(mxbeanInterface.getName() + " is not a platform management interface");
    }
    List<T> out = new ArrayList<T>();
    for (PlatformManagedObject o : allBeans()) {
      if (mxbeanInterface.isInstance(o)) {
        out.add(newPlatformMXBeanProxy(connection, o.getObjectName().toString(), mxbeanInterface));
      }
    }
    return Collections.unmodifiableList(out);
  }

  public static Set<Class<? extends PlatformManagedObject>> getPlatformManagementInterfaces() {
    Set<Class<? extends PlatformManagedObject>> set = new HashSet<Class<? extends PlatformManagedObject>>();
    set.add(ClassLoadingMXBean.class);
    set.add(CompilationMXBean.class);
    set.add(MemoryMXBean.class);
    set.add(GarbageCollectorMXBean.class);
    set.add(MemoryManagerMXBean.class);
    set.add(MemoryPoolMXBean.class);
    set.add(OperatingSystemMXBean.class);
    set.add(RuntimeMXBean.class);
    set.add(ThreadMXBean.class);
    set.add(PlatformLoggingMXBean.class);
    set.add(BufferPoolMXBean.class);
    return Collections.unmodifiableSet(set);
  }
}
