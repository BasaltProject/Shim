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

import java.io.IOException;

public class Processes {
  public static void main(String[] args) {
    long start = System.currentTimeMillis();
    try {
      final Process p = Runtime.getRuntime().exec("sleep 10");
      new Thread() {
        public void run() {
          try {
            Thread.sleep(100);
          } catch(InterruptedException e) {
            // ignore
          }
          p.destroy();
        }
      }.start();
      try {
        p.waitFor();
      } catch(InterruptedException e) {
        // ignore
      }
      long stop = System.currentTimeMillis();
      if(stop - start > 5000) {
        throw new RuntimeException("test failed; we didn't kill the process...");
      }
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }
}
