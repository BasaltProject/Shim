/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.util.function.Function;

public class LambdaWideningModern {
  static int asInt(int v) {
    return v + 1;
  }

  static long asLong(long v) {
    return v * 2;
  }

  static double asDouble(double v) {
    return v / 2;
  }

  static <T, R> R apply(Function<T, R> f, T value) {
    return f.apply(value);
  }

  public static void main(String[] args) {
    Function<Byte, Integer> fromByte = LambdaWideningModern::asInt;
    Function<Short, Integer> fromShort = LambdaWideningModern::asInt;
    Function<Character, Integer> fromChar = LambdaWideningModern::asInt;
    Function<Integer, Long> fromInt = LambdaWideningModern::asLong;
    Function<Byte, Long> byteToLong = LambdaWideningModern::asLong;
    Function<Float, Double> floatToDouble = LambdaWideningModern::asDouble;
    Function<Long, Double> longToDouble = LambdaWideningModern::asDouble;
    System.out.println(apply(fromByte, (byte) 3) + " " + apply(fromShort, (short) 4) + " " + apply(fromChar, 'a'));
    System.out.println(apply(fromInt, 5) + " " + apply(byteToLong, (byte) -6) + " " + apply(floatToDouble, 3f)
        + " " + apply(longToDouble, 9L));
    @SuppressWarnings({"unchecked", "rawtypes"})
    Function<Object, Object> raw = (Function) fromByte;
    try {
      raw.apply(Integer.valueOf(1));
    } catch (ClassCastException e) {
      System.out.println("cce");
    }
  }
}
