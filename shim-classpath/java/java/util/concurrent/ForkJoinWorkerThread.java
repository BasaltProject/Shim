/*
 * Copyright (c) 2026 BasaltMC.
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.concurrent;

public class ForkJoinWorkerThread extends Thread {
  final ForkJoinPool pool;

  protected ForkJoinWorkerThread(ForkJoinPool pool) {
    super("ForkJoinPool-worker");
    this.pool = pool;
    setDaemon(true);
  }

  public ForkJoinPool getPool() {
    return pool;
  }

  @Override
  public void run() {
    pool.runWorker();
  }
}
