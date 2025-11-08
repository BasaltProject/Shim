/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.util.*;
import java.util.concurrent.Callable;
public class ThreadGroupModern {
  static void t(String n, Callable<Object> c) { try { System.out.println(n + ": " + c.call()); } catch (Throwable e) { System.out.println(n + ": !" + e.getClass().getName() + ":" + (e instanceof NullPointerException ? null : e.getMessage())); } }
  @SuppressWarnings("deprecation")
  public static void main(String[] a) throws Exception {
    ThreadGroup main = Thread.currentThread().getThreadGroup();
    ThreadGroup g = new ThreadGroup(main, "g1");
    ThreadGroup g2 = new ThreadGroup(g, "g2");
    t("basic", () -> g + " " + g2.getParent().getName() + " " + g.getMaxPriority() + " " + g.isDaemon() + " " + g.isDestroyed() + " " + main.getParent());
    t("nullParent", () -> new ThreadGroup((ThreadGroup) null, "x"));
    t("setMax", () -> { g.setMaxPriority(3); return g.getMaxPriority() + " " + g2.getMaxPriority(); });
    t("setMaxHigh", () -> { g.setMaxPriority(20); return g.getMaxPriority(); });
    t("setMaxLow", () -> { g.setMaxPriority(0); return g.getMaxPriority(); });
    t("setMaxNeg", () -> { g.setMaxPriority(-3); return g.getMaxPriority(); });
    t("daemon", () -> { g.setDaemon(true); return g.isDaemon(); });
    t("childPrio", () -> { g.setMaxPriority(7); ThreadGroup c = new ThreadGroup(g, "c"); return c.getMaxPriority() + " " + c.isDaemon(); });
    t("groupCount", () -> main.activeGroupCount() >= 2 && g.activeGroupCount() == 3);
    t("enumGroups", () -> { ThreadGroup[] arr = new ThreadGroup[10]; int n = g.enumerate(arr); return n + " " + Arrays.stream(arr, 0, n).map(ThreadGroup::getName).sorted().toList(); });
    t("enumGroupsNoRec", () -> { ThreadGroup[] arr = new ThreadGroup[10]; int n = g.enumerate(arr, false); return n + " " + Arrays.stream(arr, 0, n).map(ThreadGroup::getName).sorted().toList(); });
    t("enumGroupsSmall", () -> { ThreadGroup[] arr = new ThreadGroup[1]; return g.enumerate(arr); });
    t("enumNull", () -> g.enumerate((ThreadGroup[]) null));
    Object lock = new Object();
    Thread th = new Thread(g2, () -> { synchronized (lock) { lock.notifyAll(); try { lock.wait(); } catch (InterruptedException e) { System.out.println("interrupted"); } } }, "worker");
    synchronized (lock) { th.start(); lock.wait(); }
    t("activeCount", () -> g.activeCount() + " " + g2.activeCount());
    t("enumThreads", () -> { Thread[] arr = new Thread[5]; int n = g.enumerate(arr); int m = g.enumerate(arr, false); return n + " " + m + " " + arr[0].getName(); });
    t("list", () -> { java.io.PrintStream old = System.out; java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream(); System.setOut(new java.io.PrintStream(bo, true)); try { g.list(); } finally { System.setOut(old); } return bo.toString().replaceAll("#\\d+", "#N"); });
    t("parentOf", () -> g.parentOf(g2) + " " + g2.parentOf(g) + " " + g.parentOf(g) + " " + g.parentOf(null));
    t("interrupt", () -> { g.interrupt(); th.join(2000); return th.isAlive(); });
    t("checkAccess", () -> { g.checkAccess(); return "ok"; });
    t("destroy", () -> { g.destroy(); return g.isDestroyed(); });
    t("uncaught", () -> { String[] got = new String[1]; ThreadGroup ug = new ThreadGroup(g, "ug") { public void uncaughtException(Thread th2, Throwable e) { got[0] = th2.getName() + ":" + e.getMessage(); } }; Thread x = new Thread(ug, () -> { throw new IllegalStateException("boom"); }, "bad"); x.start(); x.join(); return got[0]; });
    t("allStacks", () -> { Map<Thread, StackTraceElement[]> m = Thread.getAllStackTraces(); return m.containsKey(Thread.currentThread()); });
    t("stop", () -> { Thread x = new Thread(() -> {}); x.stop(); return 1; });
    t("checkAccessThread", () -> { Thread.currentThread().checkAccess(); return "ok"; });
  }
}
