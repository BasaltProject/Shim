/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.util;

import java.io.Serializable;
import java.util.random.RandomGenerator;

public class Random implements RandomGenerator, Serializable {
  private static final long serialVersionUID = 3905348978240129619L;

  private static final long Mask = 0x5DEECE66DL;

  private static final long InitialSeed = 123456789987654321L;

  private static long nextSeed = InitialSeed;

  private long seed;

  private double nextNextGaussian;
  private boolean haveNextNextGaussian = false;

  public Random(long seed) {
    setSeed(seed);
  }

  public Random() {
    synchronized (Random.class) {
      setSeed(nextSeed ^ System.currentTimeMillis());
      nextSeed *= 123456789987654321L;
      if (nextSeed == 0) {
        nextSeed = InitialSeed;
      }
    }
  }

  public void setSeed(long seed) {
    this.seed = (seed ^ Mask) & ((1L << 48) - 1);
    this.haveNextNextGaussian = false;
  }

  protected int next(int bits) {
    seed = ((seed * Mask) + 0xBL) & ((1L << 48) - 1);
    return (int) (seed >>> (48 - bits));
  }

  public int nextInt(int limit) {
    if (limit <= 0) {
      throw new IllegalArgumentException();
    }
    
    if ((limit & -limit) == limit) {
      // limit is a power of two
      return (int) ((limit * (long) next(31)) >> 31);
    }
    
    int bits;
    int value;
    do {
      bits = next(31);
      value = bits % limit;
    } while (bits - value + (limit - 1) < 0);

    return value;
  }

  public int nextInt() {
    return next(32);
  }

  public void nextBytes(byte[] bytes) {
    final int length = bytes.length;
    for (int i = 0; i < length;) {
      int r = nextInt();
      for (int j = Math.min(length - i, 4); j > 0; --j) {
        bytes[i++] = (byte) r;
        r >>= 8;
      }
    }
  }

  public long nextLong() {
    return ((long) next(32) << 32) + next(32);
  }

  public boolean nextBoolean() {
    return next(1) != 0;
  }

  public float nextFloat() {
    return next(24) / ((float) (1 << 24));
  }

  public double nextDouble() {
    return (((long) next(26) << 27) + next(27)) / (double) (1L << 53);
  }

  public synchronized double nextGaussian() {
    if (haveNextNextGaussian) {
      haveNextNextGaussian = false;
      return nextNextGaussian;
    }
    double v1, v2, s;
    do {
      v1 = 2 * nextDouble() - 1;
      v2 = 2 * nextDouble() - 1;
      s = v1 * v1 + v2 * v2;
    } while (s >= 1 || s == 0);
    double multiplier = Math.sqrt(-2 * Math.log(s) / s);
    nextNextGaussian = v2 * multiplier;
    haveNextNextGaussian = true;
    return v1 * multiplier;
  }

  public static Random from(RandomGenerator generator) {
    Objects.requireNonNull(generator, "generator");
    if (generator instanceof Random) {
      return (Random) generator;
    }
    return new RandomWrapper(generator);
  }

  private static final class RandomWrapper extends Random {
    private final RandomGenerator generator;
    private final boolean initialized;

    private RandomWrapper(RandomGenerator generator) {
      super(0L);
      this.generator = generator;
      this.initialized = true;
    }

    @Override
    public void setSeed(long seed) {
      if (initialized) {
        throw new UnsupportedOperationException("Can't set seed on wrapper");
      }
    }

    @Override
    public boolean isDeprecated() {
      return generator.isDeprecated();
    }

    @Override
    public void nextBytes(byte[] bytes) {
      generator.nextBytes(bytes);
    }

    @Override
    public int nextInt() {
      return generator.nextInt();
    }

    @Override
    public int nextInt(int bound) {
      return generator.nextInt(bound);
    }

    @Override
    public int nextInt(int origin, int bound) {
      return generator.nextInt(origin, bound);
    }

    @Override
    public long nextLong() {
      return generator.nextLong();
    }

    @Override
    public long nextLong(long bound) {
      return generator.nextLong(bound);
    }

    @Override
    public long nextLong(long origin, long bound) {
      return generator.nextLong(origin, bound);
    }

    @Override
    public boolean nextBoolean() {
      return generator.nextBoolean();
    }

    @Override
    public float nextFloat() {
      return generator.nextFloat();
    }

    @Override
    public float nextFloat(float bound) {
      return generator.nextFloat(bound);
    }

    @Override
    public float nextFloat(float origin, float bound) {
      return generator.nextFloat(origin, bound);
    }

    @Override
    public double nextDouble() {
      return generator.nextDouble();
    }

    @Override
    public double nextDouble(double bound) {
      return generator.nextDouble(bound);
    }

    @Override
    public double nextDouble(double origin, double bound) {
      return generator.nextDouble(origin, bound);
    }

    @Override
    public double nextGaussian() {
      return generator.nextGaussian();
    }

    @Override
    public double nextGaussian(double mean, double stddev) {
      return generator.nextGaussian(mean, stddev);
    }

    @Override
    public double nextExponential() {
      return generator.nextExponential();
    }

    @Override
    protected int next(int bits) {
      return (int) (generator.nextLong() >>> (64 - bits));
    }

    @Override
    public java.util.stream.IntStream ints() {
      return generator.ints();
    }

    @Override
    public java.util.stream.IntStream ints(long streamSize) {
      return generator.ints(streamSize);
    }

    @Override
    public java.util.stream.IntStream ints(int origin, int bound) {
      return generator.ints(origin, bound);
    }

    @Override
    public java.util.stream.IntStream ints(long streamSize, int origin, int bound) {
      return generator.ints(origin, bound).limit(streamSize);
    }

    @Override
    public java.util.stream.LongStream longs() {
      return generator.longs();
    }

    @Override
    public java.util.stream.LongStream longs(long streamSize) {
      return generator.longs(streamSize);
    }

    @Override
    public java.util.stream.LongStream longs(long origin, long bound) {
      return generator.longs(origin, bound);
    }

    @Override
    public java.util.stream.LongStream longs(long streamSize, long origin, long bound) {
      return generator.longs(origin, bound).limit(streamSize);
    }

    @Override
    public java.util.stream.DoubleStream doubles() {
      return generator.doubles();
    }

    @Override
    public java.util.stream.DoubleStream doubles(long streamSize) {
      return generator.doubles(streamSize);
    }

    @Override
    public java.util.stream.DoubleStream doubles(double origin, double bound) {
      return generator.doubles(origin, bound);
    }

    @Override
    public java.util.stream.DoubleStream doubles(long streamSize, double origin, double bound) {
      return generator.doubles(origin, bound).limit(streamSize);
    }
  }
}
