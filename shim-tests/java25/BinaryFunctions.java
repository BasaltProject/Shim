/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

import java.util.function.*;

public class BinaryFunctions {
    public static void main(String[] args) {
        BiFunction<Integer, Integer, Integer> sum = (a, b) -> a + b;
        System.out.println(sum.andThen(value -> value * 2).apply(3, 4));
        BinaryOperator<Integer> product = (a, b) -> a * b;
        System.out.println(product.apply(3, 4));
        System.out.println(product.andThen(value -> value + 1).apply(3, 4));
        Object identity = new Object();
        System.out.println(UnaryOperator.identity().apply(identity) == identity);
        java.util.Comparator<String> length = (a, b) -> a.length() - b.length();
        String first = new String("ab"), tie = new String("cd");
        System.out.println(BinaryOperator.minBy(length).apply(first, "long"));
        System.out.println(BinaryOperator.maxBy(length).apply(first, "long"));
        System.out.println(BinaryOperator.minBy(length).apply(first, tie) == first);
        System.out.println(BinaryOperator.maxBy(length).apply(first, tie) == first);
        final StringBuilder output = new StringBuilder();
        BiConsumer<String, Integer> consumer = (a, b) -> output.append(a).append(b);
        consumer.andThen((a, b) -> output.append(b).append(a)).accept("x", 2);
        System.out.println(output);
        final int[] calls = {0};
        BiPredicate<Integer, Integer> less = (a, b) -> a < b;
        BiPredicate<Integer, Integer> counted = (a, b) -> { ++calls[0]; return a.equals(b); };
        System.out.println(less.and(counted).test(2, 1));
        System.out.println(less.or(counted).test(1, 2));
        System.out.println(calls[0]);
        System.out.println(less.or(counted).test(2, 2));
        System.out.println(less.negate().test(3, 2));
        try { sum.andThen(null); throw new AssertionError(); }
        catch (NullPointerException expected) { System.out.println("null function"); }
        try { consumer.andThen(null); throw new AssertionError(); }
        catch (NullPointerException expected) { System.out.println("null consumer"); }
        try { less.and(null); throw new AssertionError(); }
        catch (NullPointerException expected) { System.out.println("null predicate"); }
        try { BinaryOperator.minBy(null); throw new AssertionError(); }
        catch (NullPointerException expected) { System.out.println("null comparator"); }
        BiConsumer<String, Integer> failing = (a, b) -> { throw new IllegalStateException(); };
        try { failing.andThen((a, b) -> output.append("bad")).accept("x", 2); throw new AssertionError(); }
        catch (IllegalStateException expected) { System.out.println(output); }
    }
}
