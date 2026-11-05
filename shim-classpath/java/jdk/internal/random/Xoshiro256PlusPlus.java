/*
 * Copyright (c) 2026 BasaltMC.
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.internal.random;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.random.RandomGenerator;

public final class Xoshiro256PlusPlus implements RandomGenerator.LeapableGenerator {
  private static final long kGolden = 0x9e3779b97f4a7c15L;
  private static final long kSilver = 0x6A09E667F3BCC909L;
  private static final AtomicLong defaultGen = new AtomicLong(System.currentTimeMillis() ^ (System.nanoTime() << 32));
  private static final long[] kJump = {0x180ec6d33cfd0abaL, 0xd5a61266f0c9392cL, 0xa9582618e03fc9aaL,
      0x39abdc4529b1661cL};
  private static final long[] kLeap = {0x76e15d3efefdcbbfL, 0xc5004e441c522fb3L, 0x77710069854ee241L,
      0x39109bb02acbe635L};

  private long x0;
  private long x1;
  private long x2;
  private long x3;

  public Xoshiro256PlusPlus(long x0, long x1, long x2, long x3) {
    this.x0 = x0;
    this.x1 = x1;
    this.x2 = x2;
    this.x3 = x3;
    if ((x0 | x1 | x2 | x3) == 0L) {
      long v = 0L;
      this.x0 = mixStafford13(v += kGolden);
      this.x1 = mixStafford13(v += kGolden);
      this.x2 = mixStafford13(v += kGolden);
      this.x3 = mixStafford13(v += kGolden);
    }
  }

  public Xoshiro256PlusPlus(long seed) {
    this(mixStafford13(seed ^= kSilver), mixStafford13(seed += kGolden), mixStafford13(seed += kGolden),
        mixStafford13(seed += kGolden));
  }

  public Xoshiro256PlusPlus() {
    this(defaultGen.getAndAdd(kGolden));
  }

  public Xoshiro256PlusPlus(byte[] seed) {
    Objects.requireNonNull(seed, "seed");
    long[] w = new long[4];
    for (int i = 0; i < seed.length; i++) {
      w[(i >> 3) & 3] = w[(i >> 3) & 3] << 8 | seed[i] & 0xFFL;
    }
    this.x0 = w[0];
    this.x1 = w[1];
    this.x2 = w[2];
    this.x3 = w[3];
    if ((x0 | x1 | x2 | x3) == 0L) {
      this.x0 = mixStafford13(kGolden);
    }
  }

  private static long mixStafford13(long z) {
    z = (z ^ (z >>> 30)) * 0xbf58476d1ce4e5b9L;
    z = (z ^ (z >>> 27)) * 0x94d049bb133111ebL;
    return z ^ (z >>> 31);
  }

  @Override
  public long nextLong() {
    long result = Long.rotateLeft(x0 + x3, 23) + x0;
    long t = x1 << 17;
    x2 ^= x0;
    x3 ^= x1;
    x1 ^= x2;
    x0 ^= x3;
    x2 ^= t;
    x3 = Long.rotateLeft(x3, 45);
    return result;
  }

  @Override
  public Xoshiro256PlusPlus copy() {
    return new Xoshiro256PlusPlus(x0, x1, x2, x3);
  }

  @Override
  public double jumpDistance() {
    return 0x1.0p128;
  }

  @Override
  public double leapDistance() {
    return 0x1.0p192;
  }

  @Override
  public void jump() {
    jumpAlgorithm(kJump);
  }

  @Override
  public void leap() {
    jumpAlgorithm(kLeap);
  }

  private void jumpAlgorithm(long[] table) {
    long s0 = 0L;
    long s1 = 0L;
    long s2 = 0L;
    long s3 = 0L;
    for (long word : table) {
      for (int b = 0; b < 64; b++) {
        if ((word & (1L << b)) != 0L) {
          s0 ^= x0;
          s1 ^= x1;
          s2 ^= x2;
          s3 ^= x3;
        }
        nextLong();
      }
    }
    x0 = s0;
    x1 = s1;
    x2 = s2;
    x3 = s3;
  }
}
