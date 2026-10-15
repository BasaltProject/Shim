/*
 * Copyright (c) 2026 BasaltMC.
 *
 * SPDX-License-Identifier: MIT
 */

package java.util;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.random.RandomGenerator;

public final class SplittableRandom implements RandomGenerator, RandomGenerator.SplittableGenerator {
  private static final long GOLDEN_GAMMA = 0x9e3779b97f4a7c15L;

  private static final AtomicLong defaultGen =
      new AtomicLong(System.currentTimeMillis() ^ (System.nanoTime() << 32));

  private long seed;
  private final long gamma;

  public SplittableRandom(long seed) {
    this(seed, GOLDEN_GAMMA);
  }

  public SplittableRandom() {
    this(defaultGen.getAndAdd(-7046029254386353131L));
  }

  private SplittableRandom(long seed, long gamma) {
    this.seed = seed;
    this.gamma = gamma;
  }

  private long nextSeed() {
    return seed += gamma;
  }

  private static long mix64(long z) {
    z = (z ^ (z >>> 30)) * 0xbf58476d1ce4e5b9L;
    z = (z ^ (z >>> 27)) * 0x94d049bb133111ebL;
    return z ^ (z >>> 31);
  }

  private static int mix32(long z) {
    z = (z ^ (z >>> 33)) * 0x62a9d9ed799705f5L;
    return (int) (((z ^ (z >>> 28)) * 0xcb24d0a5c88c35b3L) >>> 32);
  }

  private static long mixGamma(long z) {
    z = (z ^ (z >>> 33)) * 0xff51afd7ed558ccdL;
    z = (z ^ (z >>> 33)) * 0xc4ceb9fe1a85ec53L;
    z = (z ^ (z >>> 33)) | 1L;
    int n = Long.bitCount(z ^ (z >>> 1));
    return (n < 24) ? z ^ 0xaaaaaaaaaaaaaaaaL : z;
  }

  @Override
  public SplittableRandom split() {
    return new SplittableRandom(nextLong(), mixGamma(nextSeed()));
  }

  @Override
  public SplittableRandom split(RandomGenerator.SplittableGenerator source) {
    Objects.requireNonNull(source, "source");
    return new SplittableRandom(source.nextLong(), mixGamma(source.nextLong()));
  }

  @Override
  public int nextInt() {
    return mix32(nextSeed());
  }

  @Override
  public long nextLong() {
    return mix64(nextSeed());
  }

  @Override
  public void nextBytes(byte[] bytes) {
    Objects.requireNonNull(bytes, "bytes");
    for (int i = 0; i < bytes.length;) {
      long rnd = nextLong();
      for (int n = Math.min(bytes.length - i, 8); n > 0; --n) {
        bytes[i++] = (byte) rnd;
        rnd >>>= 8;
      }
    }
  }
}
