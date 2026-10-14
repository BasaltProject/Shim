/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

import java.util.function.*;

public class PrimitiveFunctions {
    public static void main(String[] args) {
        BooleanSupplier booleanSupplier = () -> true;
        System.out.println(booleanSupplier.getAsBoolean());
        {
            IntSupplier supplier = () -> Integer.MIN_VALUE;
            int value = supplier.getAsInt();
            System.out.println(value);
            final int[] calls = {0};
            IntConsumer consumer = v -> { if (v != value) throw new AssertionError(); ++calls[0]; };
            consumer.andThen(v -> { calls[0] += 2; }).accept(value);
            System.out.println(calls[0]);
            IntPredicate positive = v -> v > 0;
            IntPredicate counted = v -> { ++calls[0]; return v == 0; };
            System.out.println(positive.and(counted).test(-1));
            System.out.println(positive.or(counted).test(1));
            System.out.println(calls[0]);
            System.out.println(positive.negate().test(value));
            System.out.println(positive.or(counted).test(0));
            try { consumer.andThen(null); throw new AssertionError(); }
            catch (NullPointerException expected) { System.out.println("int null consumer"); }
            try { positive.or(null); throw new AssertionError(); }
            catch (NullPointerException expected) { System.out.println("int null predicate"); }
            IntConsumer failure = v -> { throw new IllegalStateException(); };
            try { failure.andThen(v -> { calls[0] += 100; }).accept(value); throw new AssertionError(); }
            catch (IllegalStateException expected) { System.out.println(calls[0]); }
        }
        {
            LongSupplier supplier = () -> Long.MIN_VALUE;
            long value = supplier.getAsLong();
            System.out.println(value);
            final int[] calls = {0};
            LongConsumer consumer = v -> { if (v != value) throw new AssertionError(); ++calls[0]; };
            consumer.andThen(v -> { calls[0] += 2; }).accept(value);
            System.out.println(calls[0]);
            LongPredicate positive = v -> v > 0;
            LongPredicate counted = v -> { ++calls[0]; return v == 0; };
            System.out.println(positive.and(counted).test(-1));
            System.out.println(positive.or(counted).test(1));
            System.out.println(calls[0]);
            System.out.println(positive.negate().test(value));
            System.out.println(positive.or(counted).test(0));
            try { consumer.andThen(null); throw new AssertionError(); }
            catch (NullPointerException expected) { System.out.println("long null consumer"); }
            try { positive.or(null); throw new AssertionError(); }
            catch (NullPointerException expected) { System.out.println("long null predicate"); }
            LongConsumer failure = v -> { throw new IllegalStateException(); };
            try { failure.andThen(v -> { calls[0] += 100; }).accept(value); throw new AssertionError(); }
            catch (IllegalStateException expected) { System.out.println(calls[0]); }
        }
        {
            DoubleSupplier supplier = () -> -0.0d;
            double value = supplier.getAsDouble();
            System.out.println(Double.doubleToRawLongBits(value));
            final int[] calls = {0};
            DoubleConsumer consumer = v -> {
                if (Double.doubleToRawLongBits(v) != Double.doubleToRawLongBits(value)) throw new AssertionError();
                ++calls[0];
            };
            consumer.andThen(v -> { calls[0] += 2; }).accept(value);
            System.out.println(calls[0]);
            DoublePredicate positive = v -> v > 0;
            DoublePredicate counted = v -> { ++calls[0]; return v == 0; };
            System.out.println(positive.and(counted).test(-1));
            System.out.println(positive.or(counted).test(1));
            System.out.println(calls[0]);
            System.out.println(positive.negate().test(value));
            System.out.println(positive.or(counted).test(0));
            try { consumer.andThen(null); throw new AssertionError(); }
            catch (NullPointerException expected) { System.out.println("double null consumer"); }
            try { positive.or(null); throw new AssertionError(); }
            catch (NullPointerException expected) { System.out.println("double null predicate"); }
            DoubleConsumer failure = v -> { throw new IllegalStateException(); };
            try { failure.andThen(v -> { calls[0] += 100; }).accept(value); throw new AssertionError(); }
            catch (IllegalStateException expected) { System.out.println(calls[0]); }
        }
        DoubleSupplier nan = () -> Double.NaN;
        DoubleSupplier infinity = () -> Double.POSITIVE_INFINITY;
        System.out.println(Double.isNaN(nan.getAsDouble()));
        System.out.println(infinity.getAsDouble() > Double.MAX_VALUE);
    }
}
