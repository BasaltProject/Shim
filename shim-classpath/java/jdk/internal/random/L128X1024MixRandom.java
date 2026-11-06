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

public final class L128X1024MixRandom implements RandomGenerator.SplittableGenerator {
  private static final long kGolden = 0x9e3779b97f4a7c15L;
  private static final long kMultiplier = 0xd605bbb58c8abbfdL;
  private static final AtomicLong defaultGen = new AtomicLong(RandomSupport.initialSeed());

  private final long ah;
  private final long al;
  private long sh;
  private long sl;
  private final long[] x = new long[16];
  private int p = 15;

  public L128X1024MixRandom(long ah, long al, long sh, long sl, long x0, long x1, long x2, long x3, long x4, long x5, long x6, long x7, long x8, long x9, long x10, long x11, long x12, long x13, long x14, long x15) {
    this.ah = ah;
    this.al = al | 1;
    this.sh = sh;
    this.sl = sl;
    x[0] = x0;
    x[1] = x1;
    x[2] = x2;
    x[3] = x3;
    x[4] = x4;
    x[5] = x5;
    x[6] = x6;
    x[7] = x7;
    x[8] = x8;
    x[9] = x9;
    x[10] = x10;
    x[11] = x11;
    x[12] = x12;
    x[13] = x13;
    x[14] = x14;
    x[15] = x15;
    if ((x0 | x1 | x2 | x3 | x4 | x5 | x6 | x7 | x8 | x9 | x10 | x11 | x12 | x13 | x14 | x15) == 0) {
      long v = sh;
      for (int i = 0; i < 16; i++) {
        x[i] = RandomSupport.mixStafford13(v += kGolden);
      }
    }
  }

  public L128X1024MixRandom(long seed) {
    this(RandomSupport.mixMurmur64(seed ^= 7640891576956012809L), RandomSupport.mixMurmur64(seed += kGolden), 0, 1,
        RandomSupport.mixStafford13(seed), RandomSupport.mixStafford13(seed += kGolden), RandomSupport.mixStafford13(seed += kGolden), RandomSupport.mixStafford13(seed += kGolden), RandomSupport.mixStafford13(seed += kGolden), RandomSupport.mixStafford13(seed += kGolden), RandomSupport.mixStafford13(seed += kGolden), RandomSupport.mixStafford13(seed += kGolden), RandomSupport.mixStafford13(seed += kGolden), RandomSupport.mixStafford13(seed += kGolden), RandomSupport.mixStafford13(seed += kGolden), RandomSupport.mixStafford13(seed += kGolden), RandomSupport.mixStafford13(seed += kGolden), RandomSupport.mixStafford13(seed += kGolden), RandomSupport.mixStafford13(seed += kGolden), RandomSupport.mixStafford13(seed + kGolden));
  }

  public L128X1024MixRandom() {
    this(defaultGen.getAndAdd(kGolden));
  }

  public L128X1024MixRandom(byte[] seed) {
    Objects.requireNonNull(seed, "seed");
    long[] l = RandomSupport.convertSeedBytesToLongs(seed, 20, 16);
    this.ah = l[0];
    this.al = l[1] | 1;
    this.sh = l[2];
    this.sl = l[3];
    for (int i = 0; i < 16; i++) {
      x[i] = l[4 + i];
    }
  }

  @Override
  public long nextLong() {
    int q = p;
    long s0 = x[p = (p + 1) & 15];
    long s15 = x[q];
    long z = RandomSupport.mixLea64(sh + s0);
    long t = kMultiplier * sl;
    sh = kMultiplier * sh + Math.unsignedMultiplyHigh(kMultiplier, sl) + sl + ah;
    sl = t + al;
    if (Long.compareUnsigned(sl, t) < 0) {
      sh++;
    }
    s15 ^= s0;
    x[q] = Long.rotateLeft(s0, 25) ^ s15 ^ (s15 << 27);
    x[p] = Long.rotateLeft(s15, 36);
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
    return new L128X1024MixRandom(source.nextLong(), brine << 1, source.nextLong(), source.nextLong(), source.nextLong(), source.nextLong(), source.nextLong(), source.nextLong(), source.nextLong(), source.nextLong(), source.nextLong(), source.nextLong(), source.nextLong(), source.nextLong(), source.nextLong(), source.nextLong(), source.nextLong(), source.nextLong(), source.nextLong(), source.nextLong());
  }
}
