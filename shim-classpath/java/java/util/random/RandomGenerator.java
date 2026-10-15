/*
 * Copyright (c) 2026 BasaltMC.
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.random;

import java.util.Objects;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public interface RandomGenerator {
  long nextLong();

  default boolean nextBoolean() {
    return nextLong() < 0L;
  }

  default void nextBytes(byte[] bytes) {
    Objects.requireNonNull(bytes, "bytes");
    for (int i = 0; i < bytes.length;) {
      long rnd = nextLong();
      for (int n = Math.min(bytes.length - i, 8); n > 0; --n) {
        bytes[i++] = (byte) rnd;
        rnd >>>= 8;
      }
    }
  }

  default float nextFloat() {
    return (nextInt() >>> 8) * 0x1.0p-24f;
  }

  default float nextFloat(float bound) {
    if (bound <= 0.0f || Float.isInfinite(bound) || Float.isNaN(bound)) {
      throw new IllegalArgumentException("bound must be positive and finite: " + bound);
    }
    float result = nextFloat() * bound;
    return (result >= bound) ? Float.intBitsToFloat(Float.floatToIntBits(bound) - 1) : result;
  }

  default float nextFloat(float origin, float bound) {
    if (origin >= bound || Float.isInfinite(origin) || Float.isInfinite(bound) || Float.isNaN(origin) || Float.isNaN(bound)) {
      throw new IllegalArgumentException("bound must be greater than origin: " + origin + " >= " + bound);
    }
    float result = origin + nextFloat() * (bound - origin);
    return (result >= bound) ? Float.intBitsToFloat(Float.floatToIntBits(bound) - 1) : result;
  }

  default double nextDouble() {
    return (nextLong() >>> 11) * 0x1.0p-53;
  }

  default double nextDouble(double bound) {
    if (bound <= 0.0 || Double.isInfinite(bound) || Double.isNaN(bound)) {
      throw new IllegalArgumentException("bound must be positive and finite: " + bound);
    }
    double result = nextDouble() * bound;
    return (result >= bound) ? Double.longBitsToDouble(Double.doubleToLongBits(bound) - 1L) : result;
  }

  default double nextDouble(double origin, double bound) {
    if (origin >= bound || Double.isInfinite(origin) || Double.isInfinite(bound) || Double.isNaN(origin) || Double.isNaN(bound)) {
      throw new IllegalArgumentException("bound must be greater than origin: " + origin + " >= " + bound);
    }
    double result = origin + nextDouble() * (bound - origin);
    return (result >= bound) ? Double.longBitsToDouble(Double.doubleToLongBits(bound) - 1L) : result;
  }

  default int nextInt() {
    return (int) (nextLong() >>> 32);
  }

  default int nextInt(int bound) {
    if (bound <= 0) {
      throw new IllegalArgumentException("bound must be positive: " + bound);
    }
    int r = nextInt() >>> 1;
    int m = bound - 1;
    if ((bound & m) == 0) {
      return (int) ((bound * (long) r) >> 31);
    }
    for (int u = r; u - (r = u % bound) + m < 0; u = nextInt() >>> 1) {}
    return r;
  }

  default int nextInt(int origin, int bound) {
    if (origin >= bound) {
      throw new IllegalArgumentException("bound must be greater than origin: " + origin + " >= " + bound);
    }
    int n = bound - origin;
    if (n > 0) {
      return origin + nextInt(n);
    }
    int r;
    do {
      r = nextInt();
    } while (r < origin || r >= bound);
    return r;
  }

  default long nextLong(long bound) {
    if (bound <= 0) {
      throw new IllegalArgumentException("bound must be positive: " + bound);
    }
    long r = nextLong() >>> 1;
    long m = bound - 1L;
    if ((bound & m) == 0L) {
      return (nextLong() & m);
    }
    for (long u = r; u - (r = u % bound) + m < 0L; u = nextLong() >>> 1) {}
    return r;
  }

  default long nextLong(long origin, long bound) {
    if (origin >= bound) {
      throw new IllegalArgumentException("bound must be greater than origin: " + origin + " >= " + bound);
    }
    long n = bound - origin;
    long m = n - 1L;
    if (n > 0) {
      if ((n & m) == 0L) {
        return origin + (nextLong() & m);
      }
      long r = nextLong() >>> 1;
      for (long u = r; u - (r = u % n) + m < 0L; u = nextLong() >>> 1) {}
      return origin + r;
    }
    long r;
    do {
      r = nextLong();
    } while (r < origin || r >= bound);
    return r;
  }

  default double nextGaussian() {
    double v1, v2, s;
    do {
      v1 = 2 * nextDouble() - 1;
      v2 = 2 * nextDouble() - 1;
      s = v1 * v1 + v2 * v2;
    } while (s >= 1 || s == 0);
    double multiplier = Math.sqrt(-2 * Math.log(s) / s);
    return v1 * multiplier;
  }

  default double nextGaussian(double mean, double stddev) {
    if (stddev < 0.0) {
      throw new IllegalArgumentException("stddev must be non-negative: " + stddev);
    }
    return mean + stddev * nextGaussian();
  }

  default double nextExponential() {
    return -Math.log(1.0 - nextDouble());
  }

  default IntStream ints() {
    return IntStream.generate(this::nextInt);
  }

  default IntStream ints(long streamSize) {
    if (streamSize < 0L) throw new IllegalArgumentException("size must be non-negative");
    return ints().limit(streamSize);
  }

  default IntStream ints(int origin, int bound) {
    return IntStream.generate(() -> nextInt(origin, bound));
  }

  default IntStream ints(long streamSize, int origin, int bound) {
    if (streamSize < 0L) throw new IllegalArgumentException("size must be non-negative");
    return ints(origin, bound).limit(streamSize);
  }

  default LongStream longs() {
    return LongStream.generate(this::nextLong);
  }

  default LongStream longs(long streamSize) {
    if (streamSize < 0L) throw new IllegalArgumentException("size must be non-negative");
    return longs().limit(streamSize);
  }

  default LongStream longs(long origin, long bound) {
    return LongStream.generate(() -> nextLong(origin, bound));
  }

  default LongStream longs(long streamSize, long origin, long bound) {
    if (streamSize < 0L) throw new IllegalArgumentException("size must be non-negative");
    return longs(origin, bound).limit(streamSize);
  }

  default DoubleStream doubles() {
    return DoubleStream.generate(this::nextDouble);
  }

  default DoubleStream doubles(long streamSize) {
    if (streamSize < 0L) throw new IllegalArgumentException("size must be non-negative");
    return doubles().limit(streamSize);
  }

  default DoubleStream doubles(double origin, double bound) {
    return DoubleStream.generate(() -> nextDouble(origin, bound));
  }

  default DoubleStream doubles(long streamSize, double origin, double bound) {
    if (streamSize < 0L) throw new IllegalArgumentException("size must be non-negative");
    return doubles(origin, bound).limit(streamSize);
  }

  default boolean isDeprecated() {
    return false;
  }

  static RandomGenerator of(String name) {
    return RandomGeneratorFactory.of(name).create();
  }

  static RandomGenerator getDefault() {
    return RandomGeneratorFactory.getDefault().create();
  }

  interface StreamableGenerator extends RandomGenerator {
    static StreamableGenerator of(String name) {
      return RandomGeneratorFactory.<StreamableGenerator>of(name).create();
    }

    Stream<RandomGenerator> rngs();
    Stream<RandomGenerator> rngs(long streamSize);
  }

  interface SplittableGenerator extends StreamableGenerator {
    static SplittableGenerator of(String name) {
      return RandomGeneratorFactory.<SplittableGenerator>of(name).create();
    }

    SplittableGenerator split();
    SplittableGenerator split(SplittableGenerator source);

    default Stream<SplittableGenerator> splits() {
      return Stream.generate(this::split);
    }

    default Stream<SplittableGenerator> splits(long streamSize) {
      if (streamSize < 0L) throw new IllegalArgumentException("size must be non-negative");
      return splits().limit(streamSize);
    }

    default Stream<SplittableGenerator> splits(SplittableGenerator source) {
      Objects.requireNonNull(source, "source");
      return Stream.generate(() -> split(source));
    }

    default Stream<SplittableGenerator> splits(long streamSize, SplittableGenerator source) {
      if (streamSize < 0L) throw new IllegalArgumentException("size must be non-negative");
      return splits(source).limit(streamSize);
    }

    @Override
    default Stream<RandomGenerator> rngs() {
      return splits().map(g -> (RandomGenerator) g);
    }

    @Override
    default Stream<RandomGenerator> rngs(long streamSize) {
      return splits(streamSize).map(g -> (RandomGenerator) g);
    }
  }

  interface JumpableGenerator extends StreamableGenerator {
    static JumpableGenerator of(String name) {
      return RandomGeneratorFactory.<JumpableGenerator>of(name).create();
    }

    JumpableGenerator copy();
    void jump();
    double jumpDistance();

    default RandomGenerator copyAndJump() {
      JumpableGenerator copy = copy();
      jump();
      return copy;
    }

    default Stream<RandomGenerator> jumps() {
      return Stream.generate(this::copyAndJump);
    }

    default Stream<RandomGenerator> jumps(long streamSize) {
      if (streamSize < 0L) throw new IllegalArgumentException("size must be non-negative");
      return jumps().limit(streamSize);
    }

    @Override
    default Stream<RandomGenerator> rngs() {
      return jumps();
    }

    @Override
    default Stream<RandomGenerator> rngs(long streamSize) {
      return jumps(streamSize);
    }
  }

  interface LeapableGenerator extends JumpableGenerator {
    static LeapableGenerator of(String name) {
      return RandomGeneratorFactory.<LeapableGenerator>of(name).create();
    }

    @Override
    LeapableGenerator copy();

    void leap();
    double leapDistance();

    default JumpableGenerator copyAndLeap() {
      JumpableGenerator copy = copy();
      leap();
      return copy;
    }

    default Stream<JumpableGenerator> leaps() {
      return Stream.generate(this::copyAndLeap);
    }

    default Stream<JumpableGenerator> leaps(long streamSize) {
      if (streamSize < 0L) throw new IllegalArgumentException("size must be non-negative");
      return leaps().limit(streamSize);
    }
  }

  interface ArbitrarilyJumpableGenerator extends LeapableGenerator {
    static ArbitrarilyJumpableGenerator of(String name) {
      return RandomGeneratorFactory.<ArbitrarilyJumpableGenerator>of(name).create();
    }

    @Override
    ArbitrarilyJumpableGenerator copy();

    void jumpPowerOfTwo(int logDistance);
    void jump(double distance);

    default void jump() {
      jump(jumpDistance());
    }

    default void leap() {
      jump(leapDistance());
    }

    default ArbitrarilyJumpableGenerator copyAndJump(double distance) {
      ArbitrarilyJumpableGenerator copy = copy();
      jump(distance);
      return copy;
    }

    default Stream<ArbitrarilyJumpableGenerator> jumps(double distance) {
      return Stream.generate(() -> copyAndJump(distance));
    }

    default Stream<ArbitrarilyJumpableGenerator> jumps(long streamSize, double distance) {
      if (streamSize < 0L) throw new IllegalArgumentException("size must be non-negative");
      return jumps(distance).limit(streamSize);
    }
  }
}

