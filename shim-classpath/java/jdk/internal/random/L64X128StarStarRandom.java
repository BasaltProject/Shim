/*
 * Copyright (c) 2026 BasaltMC.
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.internal.random;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.random.RandomGenerator;
import jdk.internal.util.random.RandomSupport;

public final class L64X128StarStarRandom implements RandomGenerator.SplittableGenerator {
  private static final long kGolden = 0x9e3779b97f4a7c15L;
  private static final long kSilver = 0x6A09E667F3BCC909L;
  private static final long kMultiplier = 0xd1342543de82ef95L;
  private static final AtomicLong defaultGen = new AtomicLong(System.currentTimeMillis() ^ (System.nanoTime() << 32));

  private final long a;
  private long s;
  private long x0;
  private long x1;

  public L64X128StarStarRandom(long a, long s, long x0, long x1) {
    this.a = a | 1;
    this.s = s;
    this.x0 = x0;
    this.x1 = x1;
    if ((x0 | x1) == 0) {
      long v = s;
      x0 = mixStafford13(v += kGolden);
      x1 = mixStafford13(v += kGolden);
    }
  }

  public L64X128StarStarRandom(long seed) {
    this(mixMurmur64(seed ^= kSilver), 1, mixStafford13(seed), mixStafford13(seed += kGolden));
  }

  public L64X128StarStarRandom() {
    this(defaultGen.getAndAdd(kGolden));
  }

  public L64X128StarStarRandom(byte[] seed) {
    this(RandomSupport.convertSeedBytesToLongs(java.util.Objects.requireNonNull(seed, "seed"), 4, 2));
  }

  private L64X128StarStarRandom(long[] l) {
    this(l[0], l[1], l[2], l[3]);
  }

  private static long fold(byte[] seed) {
    Objects.requireNonNull(seed, "seed");
    long r = 0L;
    for (int i = 0; i < seed.length; i++) {
      r = (r << 8 | seed[i] & 0xFFL) ^ (i >= 8 ? r >>> 56 : 0L);
    }
    return r;
  }

  private static long mixMurmur64(long z) {
    z = (z ^ (z >>> 33)) * 0xff51afd7ed558ccdL;
    z = (z ^ (z >>> 33)) * 0xc4ceb9fe1a85ec53L;
    return z ^ (z >>> 33);
  }

  private static long mixStafford13(long z) {
    z = (z ^ (z >>> 30)) * 0xbf58476d1ce4e5b9L;
    z = (z ^ (z >>> 27)) * 0x94d049bb133111ebL;
    return z ^ (z >>> 31);
  }

  @Override
  public long nextLong() {
    long z = Long.rotateLeft((s + x0) * 5, 7) * 9;
    s = kMultiplier * s + a;
    long q0 = x0;
    long q1 = x1;
    q1 ^= q0;
    q0 = Long.rotateLeft(q0, 24);
    q0 = q0 ^ q1 ^ (q1 << 16);
    q1 = Long.rotateLeft(q1, 37);
    x0 = q0;
    x1 = q1;
    return z;
  }

  @Override
  public RandomGenerator.SplittableGenerator split() {
    return split(this);
  }

  @Override
  public RandomGenerator.SplittableGenerator split(RandomGenerator.SplittableGenerator source) {
    Objects.requireNonNull(source, "source");
    return new L64X128StarStarRandom(source.nextLong(), source.nextLong(), source.nextLong(), source.nextLong());
  }
}
