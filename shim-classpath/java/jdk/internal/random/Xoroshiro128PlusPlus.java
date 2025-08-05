/*
 * Copyright (c) 2026 BasaltMC.
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.internal.random;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.random.RandomGenerator;

public final class Xoroshiro128PlusPlus implements RandomGenerator.LeapableGenerator {
  private static final AtomicLong defaultGen =
      new AtomicLong(System.currentTimeMillis() ^ (System.nanoTime() << 32));

  private static final long[] JUMP_TABLE = new long[] { 3159176899437800924L, 689838746718161413L };
  private static final long[] LEAP_TABLE = new long[] { 3895567441539718553L, -7174682294107748637L };

  private long x0;
  private long x1;

  public Xoroshiro128PlusPlus(long x0, long x1) {
    this.x0 = x0;
    this.x1 = x1;
    if ((x0 | x1) == 0L) {
      this.x0 = -7046029254386353131L;
      this.x1 = 7640891576956012809L;
    }
  }

  public Xoroshiro128PlusPlus(long seed) {
    long s = seed ^ 7640891576956012809L;
    this.x0 = mixStafford13(s);
    this.x1 = mixStafford13(s + -7046029254386353131L);
    if ((this.x0 | this.x1) == 0L) {
      this.x0 = -7046029254386353131L;
      this.x1 = 7640891576956012809L;
    }
  }

  public Xoroshiro128PlusPlus() {
    this(defaultGen.getAndAdd(-7046029254386353131L));
  }

  public Xoroshiro128PlusPlus(byte[] seed) {
    Objects.requireNonNull(seed, "seed");
    long s0 = 0L;
    long s1 = 0L;
    for (int i = 0; i < seed.length; i++) {
      if (i < 8) {
        s0 = (s0 << 8) | (seed[i] & 0xFFL);
      } else {
        s1 = (s1 << 8) | (seed[i] & 0xFFL);
      }
    }
    this.x0 = s0;
    this.x1 = s1;
    if ((this.x0 | this.x1) == 0L) {
      this.x0 = -7046029254386353131L;
      this.x1 = 7640891576956012809L;
    }
  }

  private static long mixStafford13(long z) {
    z = (z ^ (z >>> 30)) * 0xbf58476d1ce4e5b9L;
    z = (z ^ (z >>> 27)) * 0x94d049bb133111ebL;
    return z ^ (z >>> 31);
  }

  @Override
  public long nextLong() {
    long s0 = x0;
    long s1 = x1;
    long result = Long.rotateLeft(s0 + s1, 17) + s0;
    s1 ^= s0;
    x0 = Long.rotateLeft(s0, 49) ^ s1 ^ (s1 << 21);
    x1 = Long.rotateLeft(s1, 28);
    return result;
  }

  @Override
  public Xoroshiro128PlusPlus copy() {
    return new Xoroshiro128PlusPlus(x0, x1);
  }

  @Override
  public double jumpDistance() {
    return 1.8446744073709552E19d;
  }

  @Override
  public double leapDistance() {
    return 7.922816251426434E28d;
  }

  @Override
  public void jump() {
    jumpAlgorithm(JUMP_TABLE);
  }

  @Override
  public void leap() {
    jumpAlgorithm(LEAP_TABLE);
  }

  private void jumpAlgorithm(long[] table) {
    long s0 = 0L;
    long s1 = 0L;
    for (int i = 0; i < table.length; i++) {
      for (int b = 0; b < 64; b++) {
        if ((table[i] & (1L << b)) != 0L) {
          s0 ^= x0;
          s1 ^= x1;
        }
        nextLong();
      }
    }
    x0 = s0;
    x1 = s1;
  }
}
