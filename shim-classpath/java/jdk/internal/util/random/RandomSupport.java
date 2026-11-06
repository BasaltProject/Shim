/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.internal.util.random;

import java.util.random.RandomGenerator;

public final class RandomSupport {
  private static final double kScale = 9.223372036854776E18d;
  private static final double kExponentialStep = 7.569274694148063d;
  private static final double kExponentialCap = 6.981423655327773E19d;

  private RandomSupport() {
  }

  public static long initialSeed() {
    return mixStafford13(System.currentTimeMillis()) ^ mixStafford13(System.nanoTime());
  }

  public static long mixMurmur64(long z) {
    z = (z ^ (z >>> 33)) * 0xff51afd7ed558ccdL;
    z = (z ^ (z >>> 33)) * 0xc4ceb9fe1a85ec53L;
    return z ^ (z >>> 33);
  }

  public static long mixStafford13(long z) {
    z = (z ^ (z >>> 30)) * 0xbf58476d1ce4e5b9L;
    z = (z ^ (z >>> 27)) * 0x94d049bb133111ebL;
    return z ^ (z >>> 31);
  }

  public static long mixLea64(long z) {
    z = (z ^ (z >>> 32)) * 0xdaba0b6eb09322e3L;
    z = (z ^ (z >>> 32)) * 0xdaba0b6eb09322e3L;
    return z ^ (z >>> 32);
  }

  public static int mixMurmur32(int z) {
    z = (z ^ (z >>> 16)) * 0x85ebca6b;
    z = (z ^ (z >>> 13)) * 0xc2b2ae35;
    return z ^ (z >>> 16);
  }

  public static int[] convertSeedBytesToInts(byte[] seed, int n, int z) {
    final int[] result = new int[n];
    final int m = Math.min(seed.length, n << 2);
    for (int j = 0; j < m; j++) {
      result[j >> 2] = (result[j >> 2] << 8) | (seed[j] & 0xFF);
    }
    int s = result[0];
    for (int j = (m + 3) >> 2; j < n; j++) {
      result[j] = mixMurmur32(s += 1779033703);
    }
    for (int j = n - z; j < n; j++) {
      if (result[j] != 0) {
        return result;
      }
    }
    int s2 = result[0] & -2;
    for (int j = n - z; j < n; j++) {
      result[j] = mixMurmur32(s2 += 1779033703);
    }
    return result;
  }

  public static long[] convertSeedBytesToLongs(byte[] seed, int n, int z) {
    final long[] result = new long[n];
    final int m = Math.min(seed.length, n << 3);
    for (int j = 0; j < m; j++) {
      result[j >> 3] = (result[j >> 3] << 8) | (seed[j] & 0xFFL);
    }
    long s = result[0];
    for (int j = (m + 7) >> 3; j < n; j++) {
      result[j] = mixMurmur64(s += 7640891576956012809L);
    }
    for (int j = n - z; j < n; j++) {
      if (result[j] != 0) {
        return result;
      }
    }
    long s2 = result[0] & -2L;
    for (int j = n - z; j < n; j++) {
      result[j] = mixMurmur64(s2 += 7640891576956012809L);
    }
    return result;
  }

  public static double computeNextExponential(RandomGenerator rng) {
    return computeNextExponentialSoftCapped(rng, kExponentialCap);
  }

  public static double computeNextExponentialSoftCapped(RandomGenerator rng, double max) {
    long u1 = rng.nextLong();
    long i = u1 & 255L;
    if (i < 252L) {
      return DoubleZigguratTables.exponentialX[(int) i] * (double) (u1 >>> 1);
    }
    if (max <= 0.0d) {
      return 0.0d;
    }
    long maxJ = max >= kExponentialCap ? Long.MAX_VALUE : (long) (max / kExponentialStep);
    long j = 0L;
    while (true) {
      long ra = rng.nextLong();
      int ia = (int) ra & 255;
      if (ra >= DoubleZigguratTables.exponentialAliasThreshold[ia]) {
        ia = DoubleZigguratTables.exponentialAliasMap[ia] & 255;
      }
      if (ia > 0) {
        double[] x = DoubleZigguratTables.exponentialX;
        double[] y = DoubleZigguratTables.exponentialY;
        u1 = u1 >>> 1;
        while (true) {
          long ua = rng.nextLong() >>> 1;
          long ud = ua - u1;
          if (ud < 0) {
            ud = -ud;
            ua = u1;
            u1 = u1 - ud;
          }
          double x1 = x[ia] * kScale + (x[ia - 1] - x[ia]) * (double) u1;
          if (ud >= 853965788476313645L) {
            return Math.fma((double) j, kExponentialStep, x1);
          }
          double y1 = y[ia] * kScale + (y[ia - 1] - y[ia]) * (double) ua;
          if (y1 <= Math.exp(-x1)) {
            return Math.fma((double) j, kExponentialStep, x1);
          }
          u1 = rng.nextLong() >>> 1;
        }
      }
      if (j == maxJ) {
        return max;
      }
      j++;
      u1 = rng.nextLong();
      i = u1 & 255L;
      if (i < 252L) {
        return Math.fma((double) j, kExponentialStep, DoubleZigguratTables.exponentialX[(int) i] * (double) (u1 >>> 1));
      }
    }
  }

  public static double computeNextGaussian(RandomGenerator rng) {
    long u1 = rng.nextLong();
    long i = u1 & 255L;
    if (i < 253L) {
      return DoubleZigguratTables.normalX[(int) i] * (double) u1;
    }
    double signBit = u1 >= 0 ? 1.0d : -1.0d;
    u1 = (u1 << 1) >>> 1;
    long ra = rng.nextLong();
    int ia = (int) ra & 255;
    if (ra >= DoubleZigguratTables.normalAliasThreshold[ia]) {
      ia = DoubleZigguratTables.normalAliasMap[ia] & 255;
    }
    double[] x = DoubleZigguratTables.normalX;
    double[] y = DoubleZigguratTables.normalY;
    double x1;
    if (ia > 204) {
      while (true) {
        long u2 = rng.nextLong() >>> 1;
        x1 = x[ia] * kScale + (x[ia - 1] - x[ia]) * (double) u1;
        long ud = u2 - u1;
        if (ud >= 0) {
          break;
        }
        if (ud > -2269182951627976012L) {
          double y1 = y[ia] * kScale + (y[ia - 1] - y[ia]) * (double) u2;
          if (y1 <= Math.exp(-0.5d * x1 * x1)) {
            break;
          }
        }
        u1 = rng.nextLong() >>> 1;
      }
    } else if (ia == 0) {
      double e;
      do {
        x1 = 0.27502700159745347d * computeNextExponential(rng);
        e = 0.5d * x1 * x1;
      } while (computeNextExponentialSoftCapped(rng, e) < e);
      x1 = x1 + 3.6360066255009458d;
    } else if (ia < 204) {
      while (true) {
        long u2 = rng.nextLong() >>> 1;
        long ud = u2 - u1;
        if (ud < 0) {
          ud = -ud;
          u2 = u1;
          u1 = u1 - ud;
        }
        x1 = x[ia] * kScale + (x[ia - 1] - x[ia]) * (double) u1;
        if (ud >= 760463704284035183L) {
          break;
        }
        double y1 = y[ia] * kScale + (y[ia - 1] - y[ia]) * (double) u2;
        if (y1 <= Math.exp(-0.5d * x1 * x1)) {
          break;
        }
        u1 = rng.nextLong() >>> 1;
      }
    } else {
      while (true) {
        long u2 = rng.nextLong() >>> 1;
        x1 = Math.fma(x[ia - 1] - x[ia], (double) u1, x[ia] * kScale);
        long ud = u2 - u1;
        if (ud >= 760463704284035183L) {
          break;
        }
        if (ud > -2269182951627976012L) {
          double y1 = Math.fma(y[ia - 1] - y[ia], (double) u2, y[ia] * kScale);
          if (y1 <= Math.exp(-0.5d * x1 * x1)) {
            break;
          }
        }
        u1 = rng.nextLong() >>> 1;
      }
    }
    return signBit * x1;
  }
}
