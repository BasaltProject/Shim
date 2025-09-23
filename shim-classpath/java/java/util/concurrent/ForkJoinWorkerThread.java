/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.concurrent;

public class ForkJoinWorkerThread extends Thread {
  final ForkJoinPool pool;
  final ForkJoinPool.WorkQueue workQueue;

  protected ForkJoinWorkerThread(ForkJoinPool pool) {
    super(pool.nextWorkerName());
    this.pool = pool;
    this.workQueue = pool.registerWorker(this);
    setDaemon(true);
  }

  protected ForkJoinWorkerThread(ThreadGroup group, ForkJoinPool pool, boolean preserveThreadLocals) {
    super(group, null, pool.nextWorkerName());
    this.pool = pool;
    this.workQueue = pool.registerWorker(this);
    setDaemon(true);
  }

  public ForkJoinPool getPool() {
    return pool;
  }

  public int getPoolIndex() {
    return workQueue.index;
  }

  protected void onStart() { }

  protected void onTermination(Throwable exception) { }

  @Override
  public void run() {
    Throwable exception = null;
    try {
      onStart();
      pool.runWorker(this);
    } catch (Throwable ex) {
      exception = ex;
    } finally {
      try {
        onTermination(exception);
      } catch (Throwable ex) {
        if (exception == null) {
          exception = ex;
        }
      } finally {
        pool.deregisterWorker(this, exception);
      }
    }
  }
}
