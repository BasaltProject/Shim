/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.internal.random;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.random.RandomGenerator;
import jdk.internal.util.random.RandomSupport;

public final class L128X256MixRandom implements RandomGenerator.SplittableGenerator {
  private static final long kGolden = 0x9e3779b97f4a7c15L;
  private static final long kMultiplier = 0xd605bbb58c8abbfdL;
  private static final AtomicLong defaultGen = new AtomicLong(RandomSupport.initialSeed());

  private final long ah;
  private final long al;
  private long sh;
  private long sl;
  private long x0;
  private long x1;
  private long x2;
  private long x3;

  public L128X256MixRandom(long ah, long al, long sh, long sl, long x0, long x1, long x2, long x3) {
    this.ah = ah;
    this.al = al | 1;
    this.sh = sh;
    this.sl = sl;
    this.x0 = x0;
    this.x1 = x1;
    this.x2 = x2;
    this.x3 = x3;
    if ((x0 | x1 | x2 | x3) == 0) {
      long v = sh;
      this.x0 = RandomSupport.mixStafford13(v += kGolden);
      this.x1 = RandomSupport.mixStafford13(v += kGolden);
      this.x2 = RandomSupport.mixStafford13(v += kGolden);
      this.x3 = RandomSupport.mixStafford13(v += kGolden);
    }
  }

  public L128X256MixRandom(long seed) {
    this(RandomSupport.mixMurmur64(seed ^= 7640891576956012809L), RandomSupport.mixMurmur64(seed += kGolden), 0, 1,
        RandomSupport.mixStafford13(seed), RandomSupport.mixStafford13(seed += kGolden), RandomSupport.mixStafford13(seed += kGolden), RandomSupport.mixStafford13(seed + kGolden));
  }

  public L128X256MixRandom() {
    this(defaultGen.getAndAdd(kGolden));
  }

  public L128X256MixRandom(byte[] seed) {
    Objects.requireNonNull(seed, "seed");
    long[] l = RandomSupport.convertSeedBytesToLongs(seed, 8, 4);
    this.ah = l[0];
    this.al = l[1] | 1;
    this.sh = l[2];
    this.sl = l[3];
    this.x0 = l[4];
    this.x1 = l[5];
    this.x2 = l[6];
    this.x3 = l[7];
  }

  @Override
  public long nextLong() {
    long z = RandomSupport.mixLea64(sh + x0);
    long t = kMultiplier * sl;
    sh = kMultiplier * sh + Math.unsignedMultiplyHigh(kMultiplier, sl) + sl + ah;
    sl = t + al;
    if (Long.compareUnsigned(sl, t) < 0) {
      sh++;
    }
    long q0 = x0;
    long q1 = x1;
    long q2 = x2;
    long q3 = x3;
    long t17 = q1 << 17;
    q2 ^= q0;
    q3 ^= q1;
    q1 ^= q2;
    q0 ^= q3;
    q2 ^= t17;
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
    long brine = source.nextLong();
    return new L128X256MixRandom(source.nextLong(), brine << 1, source.nextLong(), source.nextLong(), source.nextLong(), source.nextLong(), source.nextLong(), source.nextLong());
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
