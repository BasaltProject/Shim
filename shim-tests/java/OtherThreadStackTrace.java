/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

public class OtherThreadStackTrace {

  private static volatile boolean stop;
  private static volatile long sink;

  private static void expect(boolean v) {
    if (! v) throw new RuntimeException("Expectation failed");
  }

  private static void spin() {
    long n = 0;
    while (! stop) {
      n += 1;
    }
    sink = n;
  }

  private static void outer() {
    spin();
  }

  private static boolean has(StackTraceElement[] trace, String method) {
    for (StackTraceElement e: trace) {
      if (e.getMethodName().equals(method)) return true;
    }
    return false;
  }

  public static void main(String[] args) throws Exception {
    Thread busy = new Thread(new Runnable() {
        public void run() { outer(); }
      }, "busy");
    busy.start();

    Thread idle = new Thread(new Runnable() {
        public void run() {
          try {
            Thread.sleep(60000);
          } catch (InterruptedException e) { }
        }
      }, "idle");
    idle.setDaemon(true);
    idle.start();

    Thread.sleep(200);

    for (int i = 0; i < 50; ++i) {
      StackTraceElement[] trace = busy.getStackTrace();
      expect(trace.length >= 3);
      expect(has(trace, "spin"));
      expect(has(trace, "outer"));
    }

    expect(has(idle.getStackTrace(), "sleep"));

    stop = true;
    busy.join();
    expect(busy.getStackTrace().length == 0);
  }
}
