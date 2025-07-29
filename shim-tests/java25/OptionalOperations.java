/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 11

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

public class OptionalOperations {
    public static void main(String[] args) {
        testOptional();
        testOptionalInt();
        testOptionalLong();
        testOptionalDouble();
        System.out.println("ALL OPTIONAL TESTS PASSED");
    }

    private static void testOptional() {
        Optional<String> empty = Optional.empty();
        Optional<String> present = Optional.of("apple");
        Optional<String> nullableNull = Optional.ofNullable(null);
        Optional<String> nullableValue = Optional.ofNullable("banana");

        System.out.println("empty.isPresent: " + empty.isPresent());
        System.out.println("empty.isEmpty: " + empty.isEmpty());
        System.out.println("present.isPresent: " + present.isPresent());
        System.out.println("present.isEmpty: " + present.isEmpty());
        System.out.println("nullableNull.equals(empty): " + nullableNull.equals(empty));
        System.out.println("present.get: " + present.get());
        System.out.println("present.orElse: " + present.orElse("fallback"));
        System.out.println("empty.orElse: " + empty.orElse("fallback"));

        try {
            empty.get();
            throw new AssertionError();
        } catch (NoSuchElementException e) {
            System.out.println("empty.get throws NoSuchElementException: " + e.getMessage());
        }

        try {
            empty.orElseThrow();
            throw new AssertionError();
        } catch (NoSuchElementException e) {
            System.out.println("empty.orElseThrow throws NoSuchElementException: " + e.getMessage());
        }

        try {
            empty.orElseThrow(() -> new IllegalStateException("custom"));
            throw new AssertionError();
        } catch (IllegalStateException e) {
            System.out.println("empty.orElseThrow custom: " + e.getMessage());
        }

        StringBuilder log = new StringBuilder();
        present.ifPresent(v -> log.append("present:").append(v).append(";"));
        empty.ifPresent(v -> log.append("empty:").append(v).append(";"));
        present.ifPresentOrElse(
            v -> log.append("orElse-present:").append(v).append(";"),
            () -> log.append("orElse-empty;")
        );
        empty.ifPresentOrElse(
            v -> log.append("orElse-present:").append(v).append(";"),
            () -> log.append("orElse-empty;")
        );
        System.out.println("ifPresent log: " + log);

        Optional<String> filtered1 = present.filter(s -> s.startsWith("a"));
        Optional<String> filtered2 = present.filter(s -> s.startsWith("z"));
        Optional<String> filtered3 = empty.filter(s -> s.startsWith("a"));
        System.out.println("filtered1: " + filtered1);
        System.out.println("filtered2: " + filtered2);
        System.out.println("filtered3: " + filtered3);

        Optional<Integer> mapped1 = present.map(String::length);
        Optional<Integer> mapped2 = present.map(s -> null);
        Optional<Integer> mapped3 = empty.map(String::length);
        System.out.println("mapped1: " + mapped1);
        System.out.println("mapped2: " + mapped2);
        System.out.println("mapped3: " + mapped3);

        Optional<Integer> flatMapped1 = present.flatMap(s -> Optional.of(s.length() * 10));
        Optional<Integer> flatMapped2 = present.flatMap(s -> Optional.empty());
        Optional<Integer> flatMapped3 = empty.flatMap(s -> Optional.of(99));
        System.out.println("flatMapped1: " + flatMapped1);
        System.out.println("flatMapped2: " + flatMapped2);
        System.out.println("flatMapped3: " + flatMapped3);

        Optional<String> or1 = present.or(() -> Optional.of("alt"));
        Optional<String> or2 = empty.or(() -> Optional.of("alt"));
        System.out.println("or1: " + or1);
        System.out.println("or2: " + or2);

        System.out.println("present.equals(Optional.of(apple)): " + present.equals(Optional.of("apple")));
        System.out.println("present.equals(Optional.of(pear)): " + present.equals(Optional.of("pear")));
        System.out.println("present.equals(empty): " + present.equals(empty));
        System.out.println("empty.equals(Optional.empty()): " + empty.equals(Optional.empty()));
        System.out.println("present.hashCode == apple.hashCode: " + (present.hashCode() == "apple".hashCode()));
        System.out.println("empty.hashCode: " + empty.hashCode());
        System.out.println("present.toString: " + present);
        System.out.println("empty.toString: " + empty);

        try {
            Optional.of(null);
            throw new AssertionError();
        } catch (NullPointerException e) {
            System.out.println("Optional.of(null) throws NPE");
        }
        try {
            present.filter(null);
            throw new AssertionError();
        } catch (NullPointerException e) {
            System.out.println("present.filter(null) throws NPE");
        }
        try {
            present.map(null);
            throw new AssertionError();
        } catch (NullPointerException e) {
            System.out.println("present.map(null) throws NPE");
        }
        try {
            present.flatMap(null);
            throw new AssertionError();
        } catch (NullPointerException e) {
            System.out.println("present.flatMap(null) throws NPE");
        }
        try {
            present.flatMap(s -> null);
            throw new AssertionError();
        } catch (NullPointerException e) {
            System.out.println("present.flatMap(null return) throws NPE");
        }
        try {
            empty.or(null);
            throw new AssertionError();
        } catch (NullPointerException e) {
            System.out.println("empty.or(null) throws NPE");
        }
        try {
            empty.or(() -> null);
            throw new AssertionError();
        } catch (NullPointerException e) {
            System.out.println("empty.or(null return) throws NPE");
        }
    }

    private static void testOptionalInt() {
        OptionalInt empty = OptionalInt.empty();
        OptionalInt present = OptionalInt.of(42);

        System.out.println("int empty.isPresent: " + empty.isPresent());
        System.out.println("int empty.isEmpty: " + empty.isEmpty());
        System.out.println("int present.isPresent: " + present.isPresent());
        System.out.println("int present.isEmpty: " + present.isEmpty());
        System.out.println("int present.getAsInt: " + present.getAsInt());
        System.out.println("int present.orElse: " + present.orElse(99));
        System.out.println("int empty.orElse: " + empty.orElse(99));
        System.out.println("int present.orElseGet: " + present.orElseGet(() -> 100));
        System.out.println("int empty.orElseGet: " + empty.orElseGet(() -> 100));

        try {
            empty.getAsInt();
            throw new AssertionError();
        } catch (NoSuchElementException e) {
            System.out.println("int empty.getAsInt throws NoSuchElementException: " + e.getMessage());
        }

        try {
            empty.orElseThrow();
            throw new AssertionError();
        } catch (NoSuchElementException e) {
            System.out.println("int empty.orElseThrow throws NoSuchElementException: " + e.getMessage());
        }

        try {
            empty.orElseThrow(() -> new IllegalArgumentException("int-custom"));
            throw new AssertionError();
        } catch (IllegalArgumentException e) {
            System.out.println("int empty.orElseThrow custom: " + e.getMessage());
        }

        StringBuilder log = new StringBuilder();
        present.ifPresent(v -> log.append("int-present:").append(v).append(";"));
        empty.ifPresent(v -> log.append("int-empty:").append(v).append(";"));
        present.ifPresentOrElse(
            v -> log.append("int-orElse-present:").append(v).append(";"),
            () -> log.append("int-orElse-empty;")
        );
        empty.ifPresentOrElse(
            v -> log.append("int-orElse-present:").append(v).append(";"),
            () -> log.append("int-orElse-empty;")
        );
        System.out.println("int ifPresent log: " + log);

        System.out.println("int present.equals(OptionalInt.of(42)): " + present.equals(OptionalInt.of(42)));
        System.out.println("int present.equals(OptionalInt.of(43)): " + present.equals(OptionalInt.of(43)));
        System.out.println("int present.equals(empty): " + present.equals(empty));
        System.out.println("int empty.equals(OptionalInt.empty()): " + empty.equals(OptionalInt.empty()));
        System.out.println("int present.hashCode: " + present.hashCode());
        System.out.println("int empty.hashCode: " + empty.hashCode());
        System.out.println("int present.toString: " + present);
        System.out.println("int empty.toString: " + empty);
    }

    private static void testOptionalLong() {
        OptionalLong empty = OptionalLong.empty();
        OptionalLong present = OptionalLong.of(9876543210123L);

        System.out.println("long empty.isPresent: " + empty.isPresent());
        System.out.println("long empty.isEmpty: " + empty.isEmpty());
        System.out.println("long present.isPresent: " + present.isPresent());
        System.out.println("long present.isEmpty: " + present.isEmpty());
        System.out.println("long present.getAsLong: " + present.getAsLong());
        System.out.println("long present.orElse: " + present.orElse(99L));
        System.out.println("long empty.orElse: " + empty.orElse(99L));
        System.out.println("long present.orElseGet: " + present.orElseGet(() -> 100L));
        System.out.println("long empty.orElseGet: " + empty.orElseGet(() -> 100L));

        try {
            empty.getAsLong();
            throw new AssertionError();
        } catch (NoSuchElementException e) {
            System.out.println("long empty.getAsLong throws NoSuchElementException: " + e.getMessage());
        }

        try {
            empty.orElseThrow();
            throw new AssertionError();
        } catch (NoSuchElementException e) {
            System.out.println("long empty.orElseThrow throws NoSuchElementException: " + e.getMessage());
        }

        try {
            empty.orElseThrow(() -> new IllegalArgumentException("long-custom"));
            throw new AssertionError();
        } catch (IllegalArgumentException e) {
            System.out.println("long empty.orElseThrow custom: " + e.getMessage());
        }

        StringBuilder log = new StringBuilder();
        present.ifPresent(v -> log.append("long-present:").append(v).append(";"));
        empty.ifPresent(v -> log.append("long-empty:").append(v).append(";"));
        present.ifPresentOrElse(
            v -> log.append("long-orElse-present:").append(v).append(";"),
            () -> log.append("long-orElse-empty;")
        );
        empty.ifPresentOrElse(
            v -> log.append("long-orElse-present:").append(v).append(";"),
            () -> log.append("long-orElse-empty;")
        );
        System.out.println("long ifPresent log: " + log);

        System.out.println("long present.equals(OptionalLong.of(9876543210123L)): " + present.equals(OptionalLong.of(9876543210123L)));
        System.out.println("long present.equals(OptionalLong.of(1L)): " + present.equals(OptionalLong.of(1L)));
        System.out.println("long present.equals(empty): " + present.equals(empty));
        System.out.println("long empty.equals(OptionalLong.empty()): " + empty.equals(OptionalLong.empty()));
        System.out.println("long present.hashCode: " + present.hashCode());
        System.out.println("long empty.hashCode: " + empty.hashCode());
        System.out.println("long present.toString: " + present);
        System.out.println("long empty.toString: " + empty);
    }

    private static void testOptionalDouble() {
        OptionalDouble empty = OptionalDouble.empty();
        OptionalDouble present = OptionalDouble.of(3.14);
        OptionalDouble posZero = OptionalDouble.of(+0.0);
        OptionalDouble negZero = OptionalDouble.of(-0.0);
        OptionalDouble nan1 = OptionalDouble.of(Double.NaN);
        OptionalDouble nan2 = OptionalDouble.of(Double.longBitsToDouble(0x7ff8000000000001L));

        System.out.println("double empty.isPresent: " + empty.isPresent());
        System.out.println("double empty.isEmpty: " + empty.isEmpty());
        System.out.println("double present.isPresent: " + present.isPresent());
        System.out.println("double present.isEmpty: " + present.isEmpty());
        System.out.println("double present.getAsDouble: " + present.getAsDouble());
        System.out.println("double present.orElse: " + present.orElse(9.9));
        System.out.println("double empty.orElse: " + empty.orElse(9.9));
        System.out.println("double present.orElseGet: " + present.orElseGet(() -> 1.0));
        System.out.println("double empty.orElseGet: " + empty.orElseGet(() -> 1.0));

        try {
            empty.getAsDouble();
            throw new AssertionError();
        } catch (NoSuchElementException e) {
            System.out.println("double empty.getAsDouble throws NoSuchElementException: " + e.getMessage());
        }

        try {
            empty.orElseThrow();
            throw new AssertionError();
        } catch (NoSuchElementException e) {
            System.out.println("double empty.orElseThrow throws NoSuchElementException: " + e.getMessage());
        }

        try {
            empty.orElseThrow(() -> new IllegalArgumentException("double-custom"));
            throw new AssertionError();
        } catch (IllegalArgumentException e) {
            System.out.println("double empty.orElseThrow custom: " + e.getMessage());
        }

        StringBuilder log = new StringBuilder();
        present.ifPresent(v -> log.append("double-present:").append(v).append(";"));
        empty.ifPresent(v -> log.append("double-empty:").append(v).append(";"));
        present.ifPresentOrElse(
            v -> log.append("double-orElse-present:").append(v).append(";"),
            () -> log.append("double-orElse-empty;")
        );
        empty.ifPresentOrElse(
            v -> log.append("double-orElse-present:").append(v).append(";"),
            () -> log.append("double-orElse-empty;")
        );
        System.out.println("double ifPresent log: " + log);

        System.out.println("double present.equals(OptionalDouble.of(3.14)): " + present.equals(OptionalDouble.of(3.14)));
        System.out.println("double present.equals(OptionalDouble.of(2.0)): " + present.equals(OptionalDouble.of(2.0)));
        System.out.println("double posZero.equals(negZero): " + posZero.equals(negZero));
        System.out.println("double nan1.equals(nan2): " + nan1.equals(nan2));
        System.out.println("double present.equals(empty): " + present.equals(empty));
        System.out.println("double empty.equals(OptionalDouble.empty()): " + empty.equals(OptionalDouble.empty()));
        System.out.println("double present.hashCode: " + present.hashCode());
        System.out.println("double empty.hashCode: " + empty.hashCode());
        System.out.println("double present.toString: " + present);
        System.out.println("double empty.toString: " + empty);
    }
}
