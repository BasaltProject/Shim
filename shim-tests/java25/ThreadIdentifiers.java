/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */


import java.util.HashSet;
import java.util.Set;

public class ThreadIdentifiers {
  private static void add(Set<Long> ids, Thread thread) {
    long id = thread.threadId();
    if (id <= 0 || id != thread.getId() || !ids.add(id)) {
      throw new AssertionError("duplicate or invalid thread identifier");
    }
  }

  public static void main(String[] args) throws Exception {
    Set<Long> ids = new HashSet<Long>();
    add(ids, Thread.currentThread());
    for (int i = 0; i < 128; ++i) {
      add(ids, new Thread());
      add(ids, Thread.ofVirtual().unstarted(() -> { }));
    }
    long[][] generated = new long[4][128];
    Thread[] workers = new Thread[4];
    for (int index = 0; index < workers.length; ++index) {
      final long[] row = generated[index];
      workers[index] = new Thread(() -> {
        for (int i = 0; i < row.length; ++i) row[i] = new Thread().threadId();
      });
      add(ids, workers[index]);
      workers[index].start();
    }
    for (Thread worker : workers) {
      worker.join(5000);
      if (worker.isAlive()) throw new AssertionError("worker did not terminate");
    }
    for (long[] row : generated) {
      for (long id : row) {
        if (id <= 0 || !ids.add(id)) throw new AssertionError("concurrent duplicate identifier");
      }
    }
    System.out.println("unique thread identifiers: " + ids.size());
  }
}
