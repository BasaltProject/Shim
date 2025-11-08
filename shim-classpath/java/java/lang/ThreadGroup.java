/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang;

import shim.Cell;

public class ThreadGroup implements Thread.UncaughtExceptionHandler {
  private final ThreadGroup parent;
  private final String name;
  private Cell<ThreadGroup> subgroups;
  private int maxPriority;
  private boolean daemon;

  private static final ThreadGroup SYSTEM = new ThreadGroup("system", Thread.MAX_PRIORITY);

  private ThreadGroup(String name, int maxPriority) {
    this.parent = null;
    this.name = name;
    this.maxPriority = maxPriority;
  }

  public ThreadGroup(ThreadGroup parent, String name) {
    if (parent == null) {
      throw new NullPointerException();
    }
    this.parent = parent;
    this.name = name;
    this.maxPriority = parent.getMaxPriority();
    this.daemon = parent.isDaemon();
    parent.addChild(this);
  }

  public ThreadGroup(String name) {
    this(Thread.currentThread().getThreadGroup(), name);
  }

  private boolean isRoot() {
    return parent == null && name == null;
  }

  private static ThreadGroup rootOf(ThreadGroup g) {
    while (g.parent != null) {
      g = g.parent;
    }
    return g;
  }

  private void addChild(ThreadGroup child) {
    synchronized (this) {
      Cell<ThreadGroup> cell = new Cell<ThreadGroup>(child, null);
      if (subgroups == null) {
        subgroups = cell;
      } else {
        Cell<ThreadGroup> last = subgroups;
        while (last.next != null) {
          last = last.next;
        }
        last.next = cell;
      }
    }
  }

  private ThreadGroup[] children() {
    synchronized (this) {
      int n = 0;
      for (Cell<ThreadGroup> c = subgroups; c != null; c = c.next) {
        n++;
      }
      if (this == SYSTEM) {
        ThreadGroup root = rootOf(Thread.currentThread().getThreadGroup());
        return new ThreadGroup[] {root};
      }
      ThreadGroup[] out = new ThreadGroup[n];
      int i = 0;
      for (Cell<ThreadGroup> c = subgroups; c != null; c = c.next) {
        out[i++] = c.value;
      }
      return out;
    }
  }

  public void uncaughtException(Thread t, Throwable e) {
    ThreadGroup p = getParent();
    if (p != null) {
      p.uncaughtException(t, e);
    } else {
      Thread.UncaughtExceptionHandler deh
        = Thread.getDefaultUncaughtExceptionHandler();
      if (deh != null) {
        deh.uncaughtException(t, e);
      } else if (! (e instanceof ThreadDeath)) {
        System.err.print("Exception in thread \"" + t.getName() + "\" ");
        e.printStackTrace(System.err);
      }
    }
  }

  public final int getMaxPriority() {
    return maxPriority == 0 ? Thread.MAX_PRIORITY : maxPriority;
  }

  public final void setMaxPriority(int pri) {
    if (pri >= Thread.MIN_PRIORITY && pri <= Thread.MAX_PRIORITY) {
      ThreadGroup p = getParent();
      maxPriority = p != null ? Math.min(pri, p.getMaxPriority()) : pri;
      for (ThreadGroup child : children()) {
        child.setMaxPriority(pri);
      }
    }
  }

  public final boolean isDaemon() {
    return daemon;
  }

  public final void setDaemon(boolean daemon) {
    this.daemon = daemon;
  }

  public boolean isDestroyed() {
    return false;
  }

  public final void destroy() {
  }

  public final void checkAccess() {
  }

  public final ThreadGroup getParent() {
    return isRoot() ? SYSTEM : parent;
  }

  public final String getName() {
    return isRoot() ? "main" : name;
  }

  @Override
  public String toString() {
    return getClass().getName() + "[name=" + getName() + ",maxpri=" + getMaxPriority() + "]";
  }

  public int activeCount() {
    int allCount = Thread.activeCount();
    Thread[] all = new Thread[allCount];
    allCount = Thread.enumerate(all);

    int count = 0;
    for (int i = 0; i < allCount; ++i) {
      if (parentOf(all[i].getThreadGroup())) {
        ++ count;
      }
    }

    return count;
  }

  public int activeGroupCount() {
    int n = 0;
    for (ThreadGroup g : children()) {
      n += 1 + g.activeGroupCount();
    }
    return n;
  }

  public int enumerate(Thread[] threads) {
    return enumerate(threads, true);
  }

  public int enumerate(Thread[] threads, boolean recurse) {
    int allCount = Thread.activeCount();
    Thread[] all = new Thread[allCount];
    allCount = Thread.enumerate(all);

    int count = 0;
    for (int i = 0; i < allCount && count < threads.length; ++i) {
      Thread t = all[i];
      ThreadGroup g = t.getThreadGroup();
      if (g == this || (recurse && parentOf(g))) {
        threads[count++] = t;
      }
    }

    return count;
  }

  public boolean parentOf(ThreadGroup g) {
    for (; g != null; g = g.getParent()) {
      if (g == this) {
        return true;
      }
    }

    return false;
  }

  public int enumerate(ThreadGroup[] groups) {
    return enumerate(groups, true);
  }

  public int enumerate(ThreadGroup[] groups, boolean recurse) {
    return enumerate(groups, recurse, 0);
  }

  private int enumerate(ThreadGroup[] groups, boolean recurse, int count) {
    for (ThreadGroup g : children()) {
      if (count >= groups.length) {
        break;
      }
      groups[count++] = g;
      if (recurse) {
        count = g.enumerate(groups, true, count);
      }
    }
    return count;
  }

  public final void interrupt() {
    Thread[] all = new Thread[Thread.activeCount() + 8];
    int n = Thread.enumerate(all);
    for (int i = 0; i < n; i++) {
      if (parentOf(all[i].getThreadGroup())) {
        all[i].interrupt();
      }
    }
  }

  public void list() {
    list(System.out, 0);
  }

  private void list(java.io.PrintStream out, int indent) {
    printIndent(out, indent);
    out.println(this);
    indent += 4;
    Thread[] all = new Thread[Thread.activeCount() + 8];
    int n = Thread.enumerate(all);
    for (int i = 0; i < n; i++) {
      if (all[i].getThreadGroup() == this) {
        printIndent(out, indent);
        out.println(all[i]);
      }
    }
    for (ThreadGroup g : children()) {
      g.list(out, indent);
    }
  }

  private static void printIndent(java.io.PrintStream out, int indent) {
    for (int i = 0; i < indent; i++) {
      out.print(' ');
    }
  }
}
