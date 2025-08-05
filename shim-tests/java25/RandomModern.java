/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 17

import java.util.List;
import java.util.Random;
import java.util.SplittableRandom;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import java.util.stream.Collectors;

public class RandomModern {
  public static void main(String[] args) throws Exception {
    testRandomGeneratorFactory();
    testRandomGeneratorDefaults();
    testL32X64MixRandomBitExactness();
    testXoroshiro128PlusPlusBitExactness();
    testSplittableRandomBitExactness();
    testRandomModernization();
    System.out.println("RandomModern OK");
  }

  private static void testRandomGeneratorFactory() {
    RandomGeneratorFactory<RandomGenerator> defFactory = RandomGeneratorFactory.getDefault();
    if (!"L32X64MixRandom".equals(defFactory.name())) {
      throw new AssertionError("default algorithm mismatch: " + defFactory.name());
    }
    if (!"LXM".equals(defFactory.group())) {
      throw new AssertionError("default group mismatch: " + defFactory.group());
    }
    if (defFactory.stateBits() != 96) {
      throw new AssertionError("default stateBits mismatch: " + defFactory.stateBits());
    }
    if (!defFactory.isSplittable() || defFactory.isJumpable()) {
      throw new AssertionError("default flags mismatch");
    }

    List<String> names = RandomGeneratorFactory.all()
        .map(RandomGeneratorFactory::name)
        .collect(Collectors.toList());
    if (!names.contains("L32X64MixRandom") || !names.contains("Xoroshiro128PlusPlus") ||
        !names.contains("SplittableRandom") || !names.contains("Random")) {
      throw new AssertionError("missing factory names: " + names);
    }

    RandomGeneratorFactory<RandomGenerator.JumpableGenerator> xoroFactory =
        RandomGeneratorFactory.of("Xoroshiro128PlusPlus");
    if (!"Xoroshiro".equals(xoroFactory.group()) || xoroFactory.stateBits() != 128) {
      throw new AssertionError("xoro factory mismatch");
    }
    if (!xoroFactory.isJumpable() || !xoroFactory.isLeapable() || xoroFactory.isSplittable()) {
      throw new AssertionError("xoro flags mismatch");
    }

    try {
      RandomGeneratorFactory.of("UnknownNonExistentPRNG");
      throw new AssertionError("expected exception for unknown PRNG");
    } catch (IllegalArgumentException expected) {
      // expected
    }
  }

  private static void testRandomGeneratorDefaults() {
    RandomGenerator rg = RandomGenerator.getDefault();
    int randInt = rg.nextInt(10, 50);
    if (randInt < 10 || randInt >= 50) {
      throw new AssertionError("nextInt bounded out of range: " + randInt);
    }

    long randLong = rg.nextLong(100L, 200L);
    if (randLong < 100L || randLong >= 200L) {
      throw new AssertionError("nextLong bounded out of range: " + randLong);
    }

    float randFloat = rg.nextFloat(1.5f, 3.5f);
    if (randFloat < 1.5f || randFloat >= 3.5f) {
      throw new AssertionError("nextFloat bounded out of range: " + randFloat);
    }

    double randDouble = rg.nextDouble(2.0, 4.0);
    if (randDouble < 2.0 || randDouble >= 4.0) {
      throw new AssertionError("nextDouble bounded out of range: " + randDouble);
    }

    double g = rg.nextGaussian(10.0, 2.0);
    if (Double.isNaN(g) || Double.isInfinite(g)) {
      throw new AssertionError("nextGaussian produced NaN or Infinite: " + g);
    }

    double exp = rg.nextExponential();
    if (exp < 0.0 || Double.isNaN(exp) || Double.isInfinite(exp)) {
      throw new AssertionError("nextExponential out of range: " + exp);
    }

    long countInts = rg.ints(15, 1, 10).count();
    if (countInts != 15) throw new AssertionError("ints count mismatch: " + countInts);

    long countLongs = rg.longs(8).count();
    if (countLongs != 8) throw new AssertionError("longs count mismatch: " + countLongs);

    long countDoubles = rg.doubles(6, 0.0, 1.0).count();
    if (countDoubles != 6) throw new AssertionError("doubles count mismatch: " + countDoubles);
  }

  private static void testL32X64MixRandomBitExactness() {
    RandomGenerator rg = RandomGeneratorFactory.of("L32X64MixRandom").create(42L);
    int firstInt = rg.nextInt();
    if (firstInt != 706096086) {
      throw new AssertionError("L32X64MixRandom seed 42 first int mismatch: " + firstInt);
    }

    RandomGenerator.SplittableGenerator splittable = RandomGenerator.SplittableGenerator.of("L32X64MixRandom");
    RandomGenerator.SplittableGenerator child = splittable.split();
    if (child == null || child == splittable) {
      throw new AssertionError("split() returned invalid generator");
    }

    List<RandomGenerator.SplittableGenerator> splits = splittable.splits(3).collect(Collectors.toList());
    if (splits.size() != 3) {
      throw new AssertionError("splits count mismatch: " + splits.size());
    }
  }

  private static void testXoroshiro128PlusPlusBitExactness() {
    RandomGenerator.LeapableGenerator xoro =
        RandomGenerator.LeapableGenerator.of("Xoroshiro128PlusPlus");
    RandomGenerator.LeapableGenerator seeded =
        (RandomGenerator.LeapableGenerator) RandomGeneratorFactory.of("Xoroshiro128PlusPlus").create(42L);

    long val1 = seeded.nextLong();
    if (val1 != -4695948378737616609L) {
      throw new AssertionError("Xoroshiro128PlusPlus val1 mismatch: " + val1);
    }

    long val2 = seeded.nextLong();
    if (val2 != 7341713790291473579L) {
      throw new AssertionError("Xoroshiro128PlusPlus val2 mismatch: " + val2);
    }

    seeded.jump();
    long valJump = seeded.nextLong();
    if (valJump != -5406123444715477853L) {
      throw new AssertionError("Xoroshiro128PlusPlus post-jump mismatch: " + valJump);
    }

    RandomGenerator.JumpableGenerator copy = seeded.copy();
    if (copy.nextLong() != seeded.nextLong()) {
      throw new AssertionError("copy did not reproduce same sequence");
    }
  }

  private static void testSplittableRandomBitExactness() {
    SplittableRandom sr = new SplittableRandom(42L);
    int firstInt = sr.nextInt();
    if (firstInt != -491277234) {
      throw new AssertionError("SplittableRandom first int mismatch: " + firstInt);
    }

    long firstLong = sr.nextLong();
    if (firstLong != 2949826092126892291L) {
      throw new AssertionError("SplittableRandom first long mismatch: " + firstLong);
    }

    SplittableRandom child = sr.split();
    if (child == null || child == sr) {
      throw new AssertionError("split returned null or self");
    }

    byte[] bytes = new byte[16];
    sr.nextBytes(bytes);
    boolean hasNonZero = false;
    for (byte b : bytes) {
      if (b != 0) hasNonZero = true;
    }
    if (!hasNonZero) {
      throw new AssertionError("nextBytes filled with all zeros");
    }
  }

  private static void testRandomModernization() throws Exception {
    Random r = new Random(12345L);
    if (!(r instanceof RandomGenerator)) {
      throw new AssertionError("Random must implement RandomGenerator");
    }

    boolean b = r.nextBoolean();
    float f = r.nextFloat();
    double d = r.nextDouble();
    double g = r.nextGaussian();
    int bounded = r.nextInt(10, 20);
    if (bounded < 10 || bounded >= 20) {
      throw new AssertionError("Random bounded nextInt out of range: " + bounded);
    }

    long streamCount = r.ints(5).count();
    if (streamCount != 5) {
      throw new AssertionError("Random ints stream count mismatch: " + streamCount);
    }

    RandomGenerator l32 = RandomGenerator.of("L32X64MixRandom");
    try {
      java.lang.reflect.Method fromMethod = Random.class.getMethod("from", RandomGenerator.class);
      Random wrapped = (Random) fromMethod.invoke(null, l32);
      if (!(wrapped instanceof Random)) {
        throw new AssertionError("from() must return Random instance");
      }

      int wrappedInt = wrapped.nextInt();
      long wrappedLong = wrapped.nextLong();

      try {
        wrapped.setSeed(42L);
        throw new AssertionError("expected UnsupportedOperationException from setSeed on wrapper");
      } catch (UnsupportedOperationException expected) {
        // expected
      }

      Random same = (Random) fromMethod.invoke(null, r);
      if (same != r) {
        throw new AssertionError("Random.from(Random) must return same instance");
      }
    } catch (NoSuchMethodException ignored) {
    }
  }
}

