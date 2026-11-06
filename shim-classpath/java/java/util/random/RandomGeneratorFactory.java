/*
 * Copyright (c) 2026 BasaltMC.
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.random;

import java.math.BigInteger;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.SplittableRandom;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import jdk.internal.random.L128X1024MixRandom;
import jdk.internal.random.L128X128MixRandom;
import jdk.internal.random.L128X256MixRandom;
import jdk.internal.random.L32X64MixRandom;
import jdk.internal.random.L64X1024MixRandom;
import jdk.internal.random.L64X128MixRandom;
import jdk.internal.random.L64X128StarStarRandom;
import jdk.internal.random.L64X256MixRandom;
import jdk.internal.random.Xoshiro256PlusPlus;
import jdk.internal.random.Xoroshiro128PlusPlus;

public final class RandomGeneratorFactory<T extends RandomGenerator> {
  private static final Map<String, RandomGeneratorFactory<? extends RandomGenerator>> FACTORIES =
      new LinkedHashMap<>();

  private final String name;
  private final String group;
  private final int stateBits;
  private final int equidistribution;
  private final BigInteger period;
  private final boolean statistical;
  private final boolean stochastic;
  private final boolean hardware;
  private final boolean arbitrarilyJumpable;
  private final boolean jumpable;
  private final boolean leapable;
  private final boolean splittable;
  private final boolean streamable;
  private final boolean deprecated;
  private final Class<T> generatorClass;
  private final Supplier<T> defaultCreator;
  private final Function<Long, T> seedCreator;
  private final Function<byte[], T> bytesCreator;

  private RandomGeneratorFactory(
      String name,
      String group,
      int stateBits,
      int equidistribution,
      BigInteger period,
      boolean statistical,
      boolean stochastic,
      boolean hardware,
      boolean arbitrarilyJumpable,
      boolean jumpable,
      boolean leapable,
      boolean splittable,
      boolean streamable,
      boolean deprecated,
      Class<T> generatorClass,
      Supplier<T> defaultCreator,
      Function<Long, T> seedCreator,
      Function<byte[], T> bytesCreator) {
    this.name = name;
    this.group = group;
    this.stateBits = stateBits;
    this.equidistribution = equidistribution;
    this.period = period;
    this.statistical = statistical;
    this.stochastic = stochastic;
    this.hardware = hardware;
    this.arbitrarilyJumpable = arbitrarilyJumpable;
    this.jumpable = jumpable;
    this.leapable = leapable;
    this.splittable = splittable;
    this.streamable = streamable;
    this.deprecated = deprecated;
    this.generatorClass = generatorClass;
    this.defaultCreator = defaultCreator;
    this.seedCreator = seedCreator;
    this.bytesCreator = bytesCreator;
  }

  static {
    register(new RandomGeneratorFactory<>(
        "L32X64MixRandom",
        "LXM",
        96,
        1,
        BigInteger.ONE.shiftLeft(64).subtract(BigInteger.ONE).shiftLeft(32),
        true,
        false,
        false,
        false,
        false,
        false,
        true,
        true,
        false,
        L32X64MixRandom.class,
        L32X64MixRandom::new,
        L32X64MixRandom::new,
        L32X64MixRandom::new));

    register(new RandomGeneratorFactory<>(
        "L64X128MixRandom",
        "LXM",
        192,
        2,
        BigInteger.ONE.shiftLeft(128).subtract(BigInteger.ONE).shiftLeft(64),
        true,
        false,
        false,
        false,
        false,
        false,
        true,
        true,
        false,
        L64X128MixRandom.class,
        L64X128MixRandom::new,
        L64X128MixRandom::new,
        L64X128MixRandom::new));

    register(new RandomGeneratorFactory<>(
        "L64X128StarStarRandom",
        "LXM",
        192,
        2,
        BigInteger.ONE.shiftLeft(128).subtract(BigInteger.ONE).shiftLeft(64),
        true,
        false,
        false,
        false,
        false,
        false,
        true,
        true,
        false,
        L64X128StarStarRandom.class,
        L64X128StarStarRandom::new,
        L64X128StarStarRandom::new,
        L64X128StarStarRandom::new));

    register(new RandomGeneratorFactory<>(
        "L64X256MixRandom",
        "LXM",
        320,
        4,
        BigInteger.ONE.shiftLeft(256).subtract(BigInteger.ONE).shiftLeft(64),
        true,
        false,
        false,
        false,
        false,
        false,
        true,
        true,
        false,
        L64X256MixRandom.class,
        L64X256MixRandom::new,
        L64X256MixRandom::new,
        L64X256MixRandom::new));

    register(new RandomGeneratorFactory<>(
        "L64X1024MixRandom",
        "LXM",
        1088,
        16,
        BigInteger.ONE.shiftLeft(1024).subtract(BigInteger.ONE).shiftLeft(64),
        true,
        false,
        false,
        false,
        false,
        false,
        true,
        true,
        false,
        L64X1024MixRandom.class,
        L64X1024MixRandom::new,
        L64X1024MixRandom::new,
        L64X1024MixRandom::new));

    register(new RandomGeneratorFactory<>(
        "L128X128MixRandom",
        "LXM",
        256,
        1,
        BigInteger.ONE.shiftLeft(128).subtract(BigInteger.ONE).shiftLeft(128),
        true,
        false,
        false,
        false,
        false,
        false,
        true,
        true,
        false,
        L128X128MixRandom.class,
        L128X128MixRandom::new,
        L128X128MixRandom::new,
        L128X128MixRandom::new));

    register(new RandomGeneratorFactory<>(
        "L128X256MixRandom",
        "LXM",
        384,
        1,
        BigInteger.ONE.shiftLeft(256).subtract(BigInteger.ONE).shiftLeft(128),
        true,
        false,
        false,
        false,
        false,
        false,
        true,
        true,
        false,
        L128X256MixRandom.class,
        L128X256MixRandom::new,
        L128X256MixRandom::new,
        L128X256MixRandom::new));

    register(new RandomGeneratorFactory<>(
        "L128X1024MixRandom",
        "LXM",
        1152,
        1,
        BigInteger.ONE.shiftLeft(1024).subtract(BigInteger.ONE).shiftLeft(128),
        true,
        false,
        false,
        false,
        false,
        false,
        true,
        true,
        false,
        L128X1024MixRandom.class,
        L128X1024MixRandom::new,
        L128X1024MixRandom::new,
        L128X1024MixRandom::new));

    register(new RandomGeneratorFactory<>(
        "Xoshiro256PlusPlus",
        "Xoshiro",
        256,
        3,
        BigInteger.ONE.shiftLeft(256).subtract(BigInteger.ONE),
        true,
        false,
        false,
        false,
        true,
        true,
        false,
        true,
        false,
        Xoshiro256PlusPlus.class,
        Xoshiro256PlusPlus::new,
        Xoshiro256PlusPlus::new,
        Xoshiro256PlusPlus::new));

    register(new RandomGeneratorFactory<>(
        "Xoroshiro128PlusPlus",
        "Xoroshiro",
        128,
        1,
        BigInteger.ONE.shiftLeft(128).subtract(BigInteger.ONE),
        true,
        false,
        false,
        false,
        true,
        true,
        false,
        true,
        false,
        Xoroshiro128PlusPlus.class,
        Xoroshiro128PlusPlus::new,
        Xoroshiro128PlusPlus::new,
        Xoroshiro128PlusPlus::new));

    register(new RandomGeneratorFactory<>(
        "SplittableRandom",
        "Legacy",
        64,
        1,
        BigInteger.ONE.shiftLeft(64),
        true,
        false,
        false,
        false,
        false,
        false,
        true,
        true,
        false,
        SplittableRandom.class,
        SplittableRandom::new,
        SplittableRandom::new,
        bytes -> {
          throw new UnsupportedOperationException();
        }));

    register(new RandomGeneratorFactory<>(
        "Random",
        "Legacy",
        48,
        0,
        BigInteger.ONE.shiftLeft(48),
        true,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        Random.class,
        Random::new,
        Random::new,
        bytes -> {
          throw new UnsupportedOperationException();
        }));

    register(new RandomGeneratorFactory<>(
        "SecureRandom",
        "Legacy",
        Integer.MAX_VALUE,
        Integer.MAX_VALUE,
        BigInteger.ZERO,
        false,
        true,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        java.security.SecureRandom.class,
        java.security.SecureRandom::new,
        seed -> {
          java.security.SecureRandom r = new java.security.SecureRandom();
          r.setSeed(seed);
          return r;
        },
        bytes -> new java.security.SecureRandom(bytes)));
  }

  private static void register(RandomGeneratorFactory<? extends RandomGenerator> factory) {
    FACTORIES.put(factory.name(), factory);
  }

  private static long bytesToLong(byte[] bytes) {
    Objects.requireNonNull(bytes, "bytes");
    long result = 0L;
    for (int i = 0; i < Math.min(bytes.length, 8); i++) {
      result = (result << 8) | (bytes[i] & 0xFFL);
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  public static <T extends RandomGenerator> RandomGeneratorFactory<T> of(String name) {
    Objects.requireNonNull(name, "name");
    RandomGeneratorFactory<? extends RandomGenerator> factory = FACTORIES.get(name);
    if (factory == null) {
      throw new IllegalArgumentException("No implementation of the random number generator algorithm \"" + name
          + "\" is available");
    }
    return (RandomGeneratorFactory<T>) factory;
  }

  @SuppressWarnings("unchecked")
  static <T extends RandomGenerator> RandomGeneratorFactory<T> of(String name, Class<T> category) {
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(category, "category");
    RandomGeneratorFactory<? extends RandomGenerator> factory = FACTORIES.get(name);
    if (factory == null) {
      throw new IllegalArgumentException("No implementation of the random number generator algorithm \"" + name
          + "\" is available");
    }
    if (!category.isAssignableFrom(factory.generatorClass)) {
      throw new IllegalArgumentException(name + " does not implement " + category.getName());
    }
    return (RandomGeneratorFactory<T>) factory;
  }

  public static RandomGeneratorFactory<RandomGenerator> getDefault() {
    return of("L32X64MixRandom");
  }

  @SuppressWarnings("unchecked")
  public static Stream<RandomGeneratorFactory<RandomGenerator>> all() {
    return FACTORIES.values().stream().map(f -> (RandomGeneratorFactory<RandomGenerator>) f);
  }

  public String name() {
    return name;
  }

  public String group() {
    return group;
  }

  public int stateBits() {
    return stateBits;
  }

  public int equidistribution() {
    return equidistribution;
  }

  public BigInteger period() {
    return period;
  }

  public boolean isStatistical() {
    return statistical;
  }

  public boolean isStochastic() {
    return stochastic;
  }

  public boolean isHardware() {
    return hardware;
  }

  public boolean isArbitrarilyJumpable() {
    return arbitrarilyJumpable;
  }

  public boolean isJumpable() {
    return jumpable;
  }

  public boolean isLeapable() {
    return leapable;
  }

  public boolean isSplittable() {
    return splittable;
  }

  public boolean isStreamable() {
    return streamable;
  }

  public boolean isDeprecated() {
    return deprecated;
  }

  public T create() {
    return defaultCreator.get();
  }

  public T create(long seed) {
    return seedCreator.apply(seed);
  }

  public T create(byte[] seed) {
    return bytesCreator.apply(seed);
  }
}

