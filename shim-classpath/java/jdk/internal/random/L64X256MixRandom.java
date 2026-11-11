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

public final class L64X256MixRandom implements RandomGenerator.SplittableGenerator {
  private static final long kGolden = 0x9e3779b97f4a7c15L;
  private static final long kSilver = 0x6A09E667F3BCC909L;
  private static final long kMultiplier = 0xd1342543de82ef95L;
  private static final AtomicLong defaultGen = new AtomicLong(System.currentTimeMillis() ^ (System.nanoTime() << 32));

  private final long a;
  private long s;
  private long x0;
  private long x1;
  private long x2;
  private long x3;

  public L64X256MixRandom(long a, long s, long x0, long x1, long x2, long x3) {
    this.a = a | 1;
    this.s = s;
    this.x0 = x0;
    this.x1 = x1;
    this.x2 = x2;
    this.x3 = x3;
    if ((x0 | x1 | x2 | x3) == 0) {
      long v = s;
      x0 = mixStafford13(v += kGolden);
      x1 = mixStafford13(v += kGolden);
      x2 = mixStafford13(v += kGolden);
      x3 = mixStafford13(v += kGolden);
    }
  }

  public L64X256MixRandom(long seed) {
    this(mixMurmur64(seed ^= kSilver), 1, mixStafford13(seed), mixStafford13(seed += kGolden), mixStafford13(seed += kGolden), mixStafford13(seed += kGolden));
  }

  public L64X256MixRandom() {
    this(defaultGen.getAndAdd(kGolden));
  }

  public L64X256MixRandom(byte[] seed) {
    this(RandomSupport.convertSeedBytesToLongs(java.util.Objects.requireNonNull(seed, "seed"), 6, 4));
  }

  private L64X256MixRandom(long[] l) {
    this(l[0], l[1], l[2], l[3], l[4], l[5]);
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
    long z = s + x0;
    z = (z ^ (z >>> 32)) * 0xdaba0b6eb09322e3L;
    z = (z ^ (z >>> 32)) * 0xdaba0b6eb09322e3L;
    z = z ^ (z >>> 32);
    s = kMultiplier * s + a;
    long q0 = x0;
    long q1 = x1;
    long q2 = x2;
    long q3 = x3;
    long t = q1 << 17;
    q2 ^= q0;
    q3 ^= q1;
    q1 ^= q2;
    q0 ^= q3;
    q2 ^= t;
    q3 = Long.rotateLeft(q3, 45);
    x0 = q0;
    x1 = q1;
    x2 = q2;
    x3 = q3;
    return z;
  }

  @Override
  public RandomGenerator.SplittableGenerator split() {
    return split(this);
  }

  @Override
  public RandomGenerator.SplittableGenerator split(RandomGenerator.SplittableGenerator source) {
    Objects.requireNonNull(source, "source");
    return new L64X256MixRandom(source.nextLong(), source.nextLong(), source.nextLong(), source.nextLong(), source.nextLong(), source.nextLong());
  }

  @Override
  public java.util.stream.Stream<java.util.random.RandomGenerator.SplittableGenerator> splits(long streamSize) {
    return jdk.internal.random.SplitStreams.splits(this, streamSize);
  }

  @Override
  public java.util.stream.Stream<java.util.random.RandomGenerator.SplittableGenerator> splits(
      java.util.random.RandomGenerator.SplittableGenerator source) {
    return jdk.internal.random.SplitStreams.splits(this, source);
  }

  @Override
  public java.util.stream.Stream<java.util.random.RandomGenerator.SplittableGenerator> splits(long streamSize,
      java.util.random.RandomGenerator.SplittableGenerator source) {
    return jdk.internal.random.SplitStreams.splits(this, streamSize, source);
  }
}
