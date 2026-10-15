/*
 * Copyright (c) 2026 BasaltMC.
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.internal.random;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.random.RandomGenerator;

public final class L32X64MixRandom implements RandomGenerator.SplittableGenerator {
  private static final AtomicLong defaultGen =
      new AtomicLong(System.currentTimeMillis() ^ (System.nanoTime() << 32));

  private final int a;
  private int s;
  private int x0;
  private int x1;

  public L32X64MixRandom(int a, int s, int x0, int x1) {
    this.a = a | 1;
    this.s = s;
    this.x0 = x0;
    this.x1 = x1;
    if ((x0 | x1) == 0) {
      int v = s;
      this.x0 = mixMurmur32(v - 1640531527);
      this.x1 = mixMurmur32(v - 1640531527 - 1640531527);
    }
  }

  public L32X64MixRandom(long seed) {
    long z = seed ^ 7640891576956012809L;
    int high = (int) (z >>> 32);
    int low = (int) z;
    this.a = mixMurmur32(high) | 1;
    this.s = 1;
    this.x0 = mixLea32(low);
    this.x1 = mixLea32(low - 1640531527);
    if ((this.x0 | this.x1) == 0) {
      int v = this.s;
      this.x0 = mixMurmur32(v - 1640531527);
      this.x1 = mixMurmur32(v - 1640531527 - 1640531527);
    }
  }

  public L32X64MixRandom() {
    this(defaultGen.getAndAdd(-7046029254386353131L));
  }

  public L32X64MixRandom(byte[] seed) {
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
    long z = (s0 ^ s1) ^ 7640891576956012809L;
    int high = (int) (z >>> 32);
    int low = (int) z;
    this.a = mixMurmur32(high) | 1;
    this.s = 1;
    this.x0 = mixLea32(low);
    this.x1 = mixLea32(low - 1640531527);
    if ((this.x0 | this.x1) == 0) {
      int v = this.s;
      this.x0 = mixMurmur32(v - 1640531527);
      this.x1 = mixMurmur32(v - 1640531527 - 1640531527);
    }
  }

  private static int mixMurmur32(int z) {
    z = (z ^ (z >>> 16)) * 0x85ebca6b;
    z = (z ^ (z >>> 13)) * 0xc2b2ae35;
    return z ^ (z >>> 16);
  }

  private static int mixLea32(int z) {
    z = (z ^ (z >>> 16)) * 0xd36d884b;
    z = (z ^ (z >>> 16)) * 0xd36d884b;
    return z ^ (z >>> 16);
  }

  @Override
  public int nextInt() {
    int result = mixLea32(s + x0);
    s = -1380669139 * s + a;
    int q0 = x0;
    int q1 = x1;
    q1 ^= q0;
    q0 = Integer.rotateLeft(q0, 26);
    q0 = q0 ^ q1 ^ (q1 << 9);
    q1 = Integer.rotateLeft(q1, 13);
    x0 = q0;
    x1 = q1;
    return result;
  }

  @Override
  public long nextLong() {
    return (((long) nextInt()) << 32) ^ (long) nextInt();
  }

  @Override
  public RandomGenerator.SplittableGenerator split() {
    return new L32X64MixRandom(nextInt(), nextInt(), nextInt(), nextInt());
  }

  @Override
  public RandomGenerator.SplittableGenerator split(RandomGenerator.SplittableGenerator source) {
    Objects.requireNonNull(source, "source");
    return new L32X64MixRandom(source.nextInt(), source.nextInt(), source.nextInt(), source.nextInt());
  }
}
