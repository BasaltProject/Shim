/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 9
import java.util.Objects;
import java.util.function.Supplier;

public class ObjectDefaults {
    public static void main(String[] args) {
        System.out.print((String) null);
        System.out.println((String) null);
        Object value = new Object();
        Object fallback = new Object();
        final int[] calls = {0};
        Supplier<Object> supplier = () -> { ++calls[0]; return fallback; };
        System.out.println(Objects.requireNonNullElse(value, null) == value);
        System.out.println(Objects.requireNonNullElse(null, fallback) == fallback);
        System.out.println(Objects.requireNonNullElseGet(value, null) == value);
        System.out.println(Objects.requireNonNullElseGet(value, supplier) == value);
        System.out.println(calls[0]);
        System.out.println(Objects.requireNonNullElseGet(null, supplier) == fallback);
        System.out.println(calls[0]);
        Supplier<String> message = () -> { ++calls[0]; return "missing value"; };
        System.out.println(Objects.requireNonNull(value, message) == value);
        System.out.println(Objects.requireNonNull(value, (Supplier<String>) null) == value);
        System.out.println(calls[0]);
        try { Objects.requireNonNull(null, message); throw new AssertionError(); }
        catch (NullPointerException expected) { System.out.println(expected.getMessage()); }
        System.out.println(calls[0]);
        try { Objects.requireNonNull(null, (Supplier<String>) null); throw new AssertionError(); }
        catch (NullPointerException expected) { System.out.println(expected.getMessage()); }
        try { Objects.requireNonNullElse(null, null); throw new AssertionError(); }
        catch (NullPointerException expected) { System.out.println(expected.getMessage()); }
        try { Objects.requireNonNullElseGet(null, null); throw new AssertionError(); }
        catch (NullPointerException expected) { System.out.println(expected.getMessage()); }
        try { Objects.requireNonNullElseGet(null, () -> null); throw new AssertionError(); }
        catch (NullPointerException expected) { System.out.println(expected.getMessage()); }
        try { Objects.requireNonNullElseGet(null, () -> { throw new IllegalStateException("supplier failed"); });
              throw new AssertionError(); }
        catch (IllegalStateException expected) { System.out.println(expected.getMessage()); }
        java.util.function.Predicate<Object> isNull = Objects::isNull;
        java.util.function.Predicate<Object> nonNull = Objects::nonNull;
        System.out.println(isNull.test(null));
        System.out.println(isNull.test(value));
        System.out.println(nonNull.test(null));
        System.out.println(nonNull.test(value));
    }
}
