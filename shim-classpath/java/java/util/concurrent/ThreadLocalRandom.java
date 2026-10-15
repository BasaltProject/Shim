/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.concurrent;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class ThreadLocalRandom extends Random {
  private static final long serialVersionUID = -5851777807851030925L;

  private static final long GAMMA = 0x9e3779b97f4a7c15L;
  private static final AtomicLong seeder = new AtomicLong(System.nanoTime());

  private static final ThreadLocal<ThreadLocalRandom> localRandom =
      ThreadLocal.withInitial(ThreadLocalRandom::new);

  private long rnd;
  private boolean initialized;

  private ThreadLocalRandom() {
    super();
    this.rnd = mix64(seeder.getAndAdd(GAMMA));
    this.initialized = true;
  }

  public static ThreadLocalRandom current() {
    return localRandom.get();
  }

  private static long mix64(long z) {
    z = (z ^ (z >>> 30)) * 0xbf58476d1ce4e5b9L;
    z = (z ^ (z >>> 27)) * 0x94d049bb133111ebL;
    return z ^ (z >>> 31);
  }

  @Override
  public void setSeed(long seed) {
    if (initialized) {
      throw new UnsupportedOperationException();
    }
    super.setSeed(seed);
  }

  @Override
  protected int next(int bits) {
    return (int) (mix64(rnd += GAMMA) >>> (64 - bits));
  }

  @Override
  public int nextInt() {
    return (int) mix64(rnd += GAMMA);
  }

  @Override
  public int nextInt(int bound) {
    if (bound <= 0) {
      throw new IllegalArgumentException("bound must be positive: " + bound);
    }
    int r = (int) mix64(rnd += GAMMA);
    int m = bound - 1;
    if ((bound & m) == 0) {
      r = (int) ((bound * (long) (r >>> 1)) >> 31);
    } else {
      for (int u = r >>> 1; u + m - (r = u % bound) < 0; u = (int) mix64(rnd += GAMMA) >>> 1) {
      }
    }
    return r;
  }

  @Override
  public int nextInt(int origin, int bound) {
    if (origin >= bound) {
      throw new IllegalArgumentException("bound must be greater than origin");
    }
    return nextInt(bound - origin) + origin;
  }

  @Override
  public long nextLong() {
    return mix64(rnd += GAMMA);
  }

  public long nextLong(long bound) {
    if (bound <= 0) {
      throw new IllegalArgumentException("bound must be positive: " + bound);
    }
    long r = mix64(rnd += GAMMA) >>> 1;
    long m = bound - 1;
    if ((bound & m) == 0L) {
      return (bound * r) >>> 63;
    }
    for (long u = r; u - (r = u % bound) + m < 0L; u = mix64(rnd += GAMMA) >>> 1) {
    }
    return r;
  }

  public long nextLong(long origin, long bound) {
    if (origin >= bound) {
      throw new IllegalArgumentException("bound must be greater than origin");
    }
    long n = bound - origin;
    long m = n - 1L;
    long r;
    if ((n & m) == 0L) {
      r = (mix64(rnd += GAMMA) & m) + origin;
    } else if (n > 0L) {
      for (long u = mix64(rnd += GAMMA) >>> 1; u + m - (r = u % n) < 0L; u = mix64(rnd += GAMMA) >>> 1) {
      }
      r += origin;
    } else {
      do {
        r = mix64(rnd += GAMMA);
      } while (r < origin || r >= bound);
    }
    return r;
  }

  @Override
  public double nextDouble() {
    return (mix64(rnd += GAMMA) >>> 11) * 0x1.0p-53;
  }

  @Override
  public double nextDouble(double bound) {
    if (!(bound > 0.0)) {
      throw new IllegalArgumentException("bound must be positive: " + bound);
    }
    double result = (mix64(rnd += GAMMA) >>> 11) * 0x1.0p-53 * bound;
    return result < bound ? result : Double.longBitsToDouble(Double.doubleToLongBits(bound) - 1);
  }

  @Override
  public double nextDouble(double origin, double bound) {
    if (!(origin < bound)) {
      throw new IllegalArgumentException("bound must be greater than origin");
    }
    double result = (mix64(rnd += GAMMA) >>> 11) * 0x1.0p-53 * (bound - origin) + origin;
    return result < bound ? result : Double.longBitsToDouble(Double.doubleToLongBits(bound) - 1);
  }

  @Override
  public float nextFloat() {
    return (float) ((mix64(rnd += GAMMA) >>> 40) * 0x1.0p-24);
  }

  @Override
  public boolean nextBoolean() {
    return mix64(rnd += GAMMA) < 0;
  }
}
