/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.text;

import java.util.ArrayList;

public class LayoutQueue {
  private static LayoutQueue defaultQueue;
  private final ArrayList<Runnable> tasks = new ArrayList<Runnable>();
  private Thread worker;

  public LayoutQueue() {
  }

  public static synchronized LayoutQueue getDefaultQueue() {
    if (defaultQueue == null) {
      defaultQueue = new LayoutQueue();
    }
    return defaultQueue;
  }

  public static synchronized void setDefaultQueue(LayoutQueue q) { defaultQueue = q; }

  public synchronized void addTask(Runnable task) {
    if (worker == null) {
      worker = new LayoutThread();
      worker.start();
    }
    tasks.add(task);
    notifyAll();
  }

  protected synchronized Runnable waitForWork() {
    while (tasks.size() == 0) {
      try {
        wait();
      } catch (InterruptedException ie) {
        return null;
      }
    }
    return tasks.remove(0);
  }

  class LayoutThread extends Thread {
    LayoutThread() {
      super("text-layout");
      setDaemon(true);
      setPriority(Thread.MIN_PRIORITY);
    }

    public void run() {
      Runnable work;
      do {
        work = waitForWork();
        if (work != null) {
          work.run();
        }
      } while (work != null);
    }
  }
}
