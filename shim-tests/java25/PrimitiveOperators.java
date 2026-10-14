/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

import java.util.function.*;

public class PrimitiveOperators {
    public static void main(String[] args) {
        {
            IntUnaryOperator increment = value -> value + 1;
            IntUnaryOperator twice = value -> value * 2;
            System.out.println(increment.compose(twice).applyAsInt(3));
            System.out.println(increment.andThen(twice).applyAsInt(3));
            IntBinaryOperator subtract = (first, second) -> first - second;
            System.out.println(subtract.applyAsInt(10, 3));
            int value = IntUnaryOperator.identity().applyAsInt(Integer.MIN_VALUE);
            System.out.println(value);
            try { increment.compose(null); throw new AssertionError(); }
            catch (NullPointerException expected) { System.out.println("Int null compose"); }
            try { increment.andThen(null); throw new AssertionError(); }
            catch (NullPointerException expected) { System.out.println("Int null after"); }
            final int[] calls = {0};
            IntUnaryOperator fail = v -> { throw new IllegalStateException(); };
            IntUnaryOperator counted = v -> { ++calls[0]; return v; };
            try { fail.andThen(counted).applyAsInt(1); throw new AssertionError(); }
            catch (IllegalStateException expected) { System.out.println(calls[0]); }
        }
        {
            LongUnaryOperator increment = value -> value + 1;
            LongUnaryOperator twice = value -> value * 2;
            System.out.println(increment.compose(twice).applyAsLong(3));
            System.out.println(increment.andThen(twice).applyAsLong(3));
            LongBinaryOperator subtract = (first, second) -> first - second;
            System.out.println(subtract.applyAsLong(10, 3));
            long value = LongUnaryOperator.identity().applyAsLong(Long.MIN_VALUE);
            System.out.println(value);
            try { increment.compose(null); throw new AssertionError(); }
            catch (NullPointerException expected) { System.out.println("Long null compose"); }
            try { increment.andThen(null); throw new AssertionError(); }
            catch (NullPointerException expected) { System.out.println("Long null after"); }
            final int[] calls = {0};
            LongUnaryOperator fail = v -> { throw new IllegalStateException(); };
            LongUnaryOperator counted = v -> { ++calls[0]; return v; };
            try { fail.andThen(counted).applyAsLong(1); throw new AssertionError(); }
            catch (IllegalStateException expected) { System.out.println(calls[0]); }
        }
        {
            DoubleUnaryOperator increment = value -> value + 1;
            DoubleUnaryOperator twice = value -> value * 2;
            System.out.println(Double.doubleToRawLongBits(increment.compose(twice).applyAsDouble(3)));
            System.out.println(Double.doubleToRawLongBits(increment.andThen(twice).applyAsDouble(3)));
            DoubleBinaryOperator subtract = (first, second) -> first - second;
            System.out.println(Double.doubleToRawLongBits(subtract.applyAsDouble(10, 3)));
            double value = DoubleUnaryOperator.identity().applyAsDouble(-0.0d);
            System.out.println(Double.doubleToRawLongBits(value));
            try { increment.compose(null); throw new AssertionError(); }
            catch (NullPointerException expected) { System.out.println("Double null compose"); }
            try { increment.andThen(null); throw new AssertionError(); }
            catch (NullPointerException expected) { System.out.println("Double null after"); }
            final int[] calls = {0};
            DoubleUnaryOperator fail = v -> { throw new IllegalStateException(); };
            DoubleUnaryOperator counted = v -> { ++calls[0]; return v; };
            try { fail.andThen(counted).applyAsDouble(1); throw new AssertionError(); }
            catch (IllegalStateException expected) { System.out.println(calls[0]); }
        }
        double payload = Double.longBitsToDouble(0x7ff8000000000042L);
        System.out.println(Double.doubleToRawLongBits(DoubleUnaryOperator.identity().applyAsDouble(payload)));
        DoubleBinaryOperator first = (a, b) -> a;
        System.out.println(Double.doubleToRawLongBits(first.applyAsDouble(payload, 1)));
    }
}
