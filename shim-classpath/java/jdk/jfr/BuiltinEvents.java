/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class BuiltinEvents {
  private BuiltinEvents() {
  }

  private static ValueDescriptor f(Class<?> t, String name, String label) {
    List<AnnotationElement> a = new ArrayList<AnnotationElement>();
    a.add(new AnnotationElement(Label.class, label));
    return new ValueDescriptor(t, name, a);
  }

  private static EventInfo def(String name, String label, String cat, String period, ValueDescriptor... fields) {
    List<ValueDescriptor> l = new ArrayList<ValueDescriptor>();
    for (ValueDescriptor v : fields) {
      l.add(v);
    }
    return EventInfo.builtin(name, label, cat, period, l);
  }

  static void init() {
    try {
      final EventInfo jvm = def("jdk.JVMInformation", "JVM Information", "Runtime", "beginChunk",
          f(String.class, "jvmName", "JVM Name"), f(String.class, "jvmVersion", "JVM Version"),
          f(String.class, "jvmArguments", "JVM Command Line Arguments"),
          f(String.class, "javaArguments", "Java Application Arguments"),
          f(long.class, "jvmStartTime", "JVM Start Time"), f(long.class, "pid", "Process Identifier"));
      JfrRuntime.addHook(jvm, new Runnable() {
        public void run() {
          DynamicEvent e = new DynamicEvent(jvm);
          e.setValue(0, System.getProperty("java.vm.name"));
          e.setValue(1, System.getProperty("java.vm.version"));
          e.setValue(2, "");
          e.setValue(3, System.getProperty("sun.java.command", ""));
          e.setValue(4, java.lang.management.ManagementFactory.getRuntimeMXBean().getStartTime());
          e.setValue(5, ProcessHandle.current().pid());
          e.commit();
        }
      });
      final EventInfo os = def("jdk.OSInformation", "OS Information", "Operating System", "beginChunk",
          f(String.class, "osVersion", "OS Version"));
      JfrRuntime.addHook(os, new Runnable() {
        public void run() {
          DynamicEvent e = new DynamicEvent(os);
          e.setValue(0, System.getProperty("os.name") + " " + System.getProperty("os.version") + " "
              + System.getProperty("os.arch"));
          e.commit();
        }
      });
      final EventInfo cpu = def("jdk.CPUInformation", "CPU Information", "Operating System", "beginChunk",
          f(String.class, "cpu", "Type"), f(String.class, "description", "Description"),
          f(int.class, "sockets", "Sockets"), f(int.class, "cores", "Cores"), f(int.class, "hwThreads",
              "Hardware Threads"));
      JfrRuntime.addHook(cpu, new Runnable() {
        public void run() {
          DynamicEvent e = new DynamicEvent(cpu);
          int n = Runtime.getRuntime().availableProcessors();
          e.setValue(0, System.getProperty("os.arch"));
          e.setValue(1, System.getProperty("os.arch") + " processor");
          e.setValue(2, 1);
          e.setValue(3, n);
          e.setValue(4, n);
          e.commit();
        }
      });
      final EventInfo prop = def("jdk.InitialSystemProperty", "Initial System Property", "Runtime", "beginChunk",
          f(String.class, "key", "Key"), f(String.class, "value", "Value"));
      JfrRuntime.addHook(prop, new Runnable() {
        public void run() {
          for (Map.Entry<Object, Object> en : System.getProperties().entrySet()) {
            DynamicEvent e = new DynamicEvent(prop);
            e.setValue(0, String.valueOf(en.getKey()));
            e.setValue(1, String.valueOf(en.getValue()));
            e.commit();
          }
        }
      });
      final EventInfo load = def("jdk.CPULoad", "CPU Load", "Operating System", "1 s",
          f(float.class, "jvmUser", "JVM User"), f(float.class, "jvmSystem", "JVM System"),
          f(float.class, "machineTotal", "Machine Total"));
      JfrRuntime.addHook(load, new Runnable() {
        public void run() {
          DynamicEvent e = new DynamicEvent(load);
          double avg = java.lang.management.ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage();
          float v = avg < 0 ? 0f : (float) Math.min(1.0, avg / Runtime.getRuntime().availableProcessors());
          e.setValue(0, v);
          e.setValue(1, 0f);
          e.setValue(2, v);
          e.commit();
        }
      });
      final EventInfo threads = def("jdk.JavaThreadStatistics", "Java Thread Statistics", "Java Application",
          "1 s", f(long.class, "activeCount", "Active Threads"), f(long.class, "daemonCount", "Daemon Threads"),
          f(long.class, "accumulatedCount", "Accumulated Threads"), f(long.class, "peakCount", "Peak Threads"));
      JfrRuntime.addHook(threads, new Runnable() {
        public void run() {
          java.lang.management.ThreadMXBean m = java.lang.management.ManagementFactory.getThreadMXBean();
          DynamicEvent e = new DynamicEvent(threads);
          e.setValue(0, (long) m.getThreadCount());
          e.setValue(1, (long) m.getDaemonThreadCount());
          e.setValue(2, m.getTotalStartedThreadCount());
          e.setValue(3, (long) m.getPeakThreadCount());
          e.commit();
        }
      });
      final EventInfo cl = def("jdk.ClassLoadingStatistics", "Class Loading Statistics", "Java Application", "1 s",
          f(long.class, "loadedClassCount", "Loaded Class Count"), f(long.class, "unloadedClassCount",
              "Unloaded Class Count"));
      JfrRuntime.addHook(cl, new Runnable() {
        public void run() {
          java.lang.management.ClassLoadingMXBean m = java.lang.management.ManagementFactory.getClassLoadingMXBean();
          DynamicEvent e = new DynamicEvent(cl);
          e.setValue(0, (long) m.getLoadedClassCount());
          e.setValue(1, m.getUnloadedClassCount());
          e.commit();
        }
      });
      final EventInfo mem = def("jdk.PhysicalMemory", "Physical Memory", "Operating System", "everyChunk",
          f(long.class, "totalSize", "Total Size"), f(long.class, "usedSize", "Used Size"));
      JfrRuntime.addHook(mem, new Runnable() {
        public void run() {
          Runtime r = Runtime.getRuntime();
          DynamicEvent e = new DynamicEvent(mem);
          e.setValue(0, r.maxMemory());
          e.setValue(1, r.totalMemory() - r.freeMemory());
          e.commit();
        }
      });
    } catch (Throwable t) {
    }
  }

  static List<String> names() {
    List<String> l = new ArrayList<String>();
    for (EventInfo i : JfrRuntime.REGISTERED.values()) {
      if (i.builtin) {
        l.add(i.name);
      }
    }
    java.util.Collections.sort(l);
    return l;
  }
}
