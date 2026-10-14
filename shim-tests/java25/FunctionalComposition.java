/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

import java.util.function.*;

public class FunctionalComposition {
    public static void main(String[] args) throws Exception {
        Supplier<String> supplier = () -> "value";
        System.out.println(supplier.get());
        Function<Integer, Integer> increment = value -> value + 1;
        Function<Integer, Integer> doubleValue = value -> value * 2;
        System.out.println(increment.compose(doubleValue).apply(3));
        System.out.println(increment.andThen(doubleValue).apply(3));
        Object identity = new Object();
        System.out.println(Function.identity().apply(identity) == identity);
        final StringBuilder output = new StringBuilder();
        Consumer<String> first = value -> output.append("first:" + value);
        first.andThen(value -> output.append("second:" + value)).accept("x");
        System.out.println(output);
        final int[] calls = {0};
        Predicate<Integer> positive = value -> value > 0;
        Predicate<Integer> counted = value -> { ++calls[0]; return value % 2 == 0; };
        System.out.println(positive.and(counted).test(-2));
        System.out.println(positive.or(counted).test(2));
        System.out.println(calls[0]);
        System.out.println(positive.and(counted).test(2));
        System.out.println(positive.negate().test(0));
        Predicate<Integer> negated = (Predicate<Integer>) Predicate.class.getMethod("not", Predicate.class).invoke(null, positive);
        System.out.println(negated.test(0));
        try { positive.and(null); throw new AssertionError(); }
        catch (NullPointerException expected) { System.out.println("null predicate"); }
        System.out.println(Predicate.isEqual(null).test(null));
        System.out.println(Predicate.isEqual("x").test(new String("x")));
        try { increment.compose(null); throw new AssertionError(); }
        catch (NullPointerException expected) { System.out.println("null compose"); }
        try { first.andThen(null); throw new AssertionError(); }
        catch (NullPointerException expected) { System.out.println("null consumer"); }
        Consumer<String> failing = value -> { throw new IllegalStateException(); };
        try { failing.andThen(value -> output.append("unexpected")).accept("x"); throw new AssertionError(); }
        catch (IllegalStateException expected) { System.out.println(output); }
    }
}
