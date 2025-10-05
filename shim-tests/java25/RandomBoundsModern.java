/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.util.*;
import java.util.random.RandomGenerator;

public class RandomBoundsModern {
  interface Body {
    Object get() throws Exception;
  }

  static void probe(String name, Body body) {
    String out;
    try {
      out = String.valueOf(body.get());
    } catch (Throwable t) {
      out = "!" + t.getClass().getName() + ":" + t.getMessage();
    }
    System.out.println(name + "=" + out);
  }

  public static void main(String[] args) throws Exception {
    probe("ints", () -> {
      Random r = new Random(12345);
      return r.nextInt() + " " + r.nextInt(7) + " " + r.nextInt(8) + " " + r.nextInt(3, 11) + " " + r.nextInt(-5, 5) + " " + r.nextInt(0, 1 << 30)
          + " " + r.nextInt(Integer.MIN_VALUE, Integer.MAX_VALUE) + " " + r.nextInt(-10, 1 << 5) + " " + r.nextInt(100, 117);
    });
    probe("longs", () -> {
      Random r = new Random(777);
      return r.nextLong() + " " + r.nextLong(1000) + " " + r.nextLong(1L << 40) + " " + r.nextLong(5, 500) + " " + r.nextLong(Long.MIN_VALUE, Long.MAX_VALUE)
          + " " + r.nextLong(-100, 100) + " " + r.nextLong(1, 3);
    });
    probe("doubles", () -> {
      Random r = new Random(99);
      return r.nextDouble() + " " + r.nextDouble(10.0) + " " + r.nextDouble(2.0, 3.0) + " " + r.nextFloat() + " " + r.nextFloat(5f) + " " + r.nextFloat(-1f, 1f)
          + " " + r.nextGaussian() + " " + r.nextBoolean() + " " + r.nextBoolean();
    });
    probe("streams", () -> {
      Random r = new Random(5);
      return r.ints(5, 10, 20).boxed().toList() + " " + r.longs(3, 0, 1000).boxed().toList() + " " + r.doubles(3, 1, 2).boxed().toList()
          + " " + r.ints(4).boxed().toList() + " " + r.longs(2).boxed().toList() + " " + r.doubles(2).boxed().toList();
    });
    probe("bytes", () -> {
      Random r = new Random(1);
      byte[] b = new byte[11];
      r.nextBytes(b);
      return Arrays.toString(b);
    });
    probe("generator view", () -> {
      RandomGenerator g = new Random(31);
      return g.nextInt(20) + " " + g.nextInt(5, 15) + " " + g.nextLong(77) + " " + g.nextDouble(3.0);
    });
    String[] bad = {"int0", "int-1", "intRange", "long0", "longRange", "double0", "doubleNaN", "doubleInf", "doubleRange", "doubleRangeInf", "float0", "floatRange", "streamNeg", "streamRange", "streamBound"};
    for (String b : bad) {
      probe("bad " + b, () -> {
        Random r = new Random(1);
        switch (b) {
        case "int0":
          return r.nextInt(0);
        case "int-1":
          return r.nextInt(-1);
        case "intRange":
          return r.nextInt(5, 5);
        case "long0":
          return r.nextLong(0);
        case "longRange":
          return r.nextLong(9, 3);
        case "double0":
          return r.nextDouble(0.0);
        case "doubleNaN":
          return r.nextDouble(Double.NaN);
        case "doubleInf":
          return r.nextDouble(Double.POSITIVE_INFINITY);
        case "doubleRange":
          return r.nextDouble(2.0, 1.0);
        case "doubleRangeInf":
          return r.nextDouble(0.0, Double.POSITIVE_INFINITY);
        case "float0":
          return r.nextFloat(0f);
        case "floatRange":
          return r.nextFloat(1f, 1f);
        case "streamNeg":
          return r.ints(-1);
        case "streamRange":
          return r.ints(3, 5, 5);
        default:
          return r.longs(3, 5, 5);
        }
      });
    }
  }
}
