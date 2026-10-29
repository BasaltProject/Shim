/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

/** Run separately from output-comparison tests: times vary, checksums must not.
 * Extracting a fixed-size slice must not grow with the original backing length.
 */
public class StringSubstringBench {
  public static void main(String[] args) {
    int iterations = args.length == 0 ? 1000 : Integer.parseInt(args[0]);
    for (int size : new int[] {16 * 1024, 32 * 1024, 64 * 1024, 128 * 1024}) {
      for (boolean unicode : new boolean[] {false, true}) {
        char[] chars = new char[size];
        java.util.Arrays.fill(chars, 'a');
        if (unicode) chars[size - 1] = '\u00f1';
        String text = new String(chars);
        long started = System.nanoTime();
        long checksum = 0;
        for (int at = 0; at < iterations; ++at) {
          int start = at % (size - 8);
          checksum += text.substring(start, start + 4).charAt(0);
        }
        if (checksum != 97L * iterations) throw new AssertionError(checksum);
        System.out.println("size=" + size + " unicode=" + unicode + " slice_ms="
            + (System.nanoTime() - started) / 1000000.0 + " checksum=" + checksum);
      }
    }
  }
}
