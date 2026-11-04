/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 25

import java.util.*;
import java.util.function.*;

public class CompilerInferenceGapsModern {
    interface Defaults { default List<String> parts() { return List.of("d"); } }
    static class Over implements Defaults {
        List<String> divided;
        public List<String> parts() { return Objects.requireNonNullElseGet(divided, Defaults.super::parts); }
    }

    interface Entry<T> { }
    static class ByteEntry implements Entry<Byte> { }
    static class Meta { <T> T get(Entry<T> e, T value) { return value; } }
    static int widen(int i) { return i; }
    static long widenLong(long v) { return v; }

    interface Codec<T> { T read(); void write(T v); }
    static <T> Codec<T> codec(Codec<T> c, T fallback) { return c; }
    static final Codec<Integer> OPT = codec(new Codec<>() {
        public Integer read() { return 7; }
        public void write(Integer v) { }
    }, null);
    static String submit(Supplier<String> s) { return s.get(); }
    static String submit(Supplier<String> s, int k) { return s.get() + k; }

    static class Entity { }
    static class Player extends Entity { }
    interface Target<E extends Entity> { }
    static final class Holder<T extends Entity> {
        final Target<T> target;
        Holder(Target<T> target) { this.target = target; }
    }

    interface IntFn extends Function<Integer, Integer>, IntUnaryOperator {
        default Integer apply(Integer v) { return applyAsInt(v); }
    }
    static int twice(IntFn f, int v) { return f.applyAsInt(v) * 2; }

    record Pair(List<String> items) {
        Pair(String... values) {
            this(List.of(values));
        }
        Pair(List<String> items) {
            if (items.isEmpty()) throw new IllegalArgumentException("empty");
            List<String> copy = new ArrayList<>(items);
            Collections.sort(copy);
            this.items = copy;
        }
    }

    static class Base {
        final int value;
        Base(int value) { this.value = value; }
    }
    static class Flexible extends Base {
        Flexible(String text) {
            int parsed = Integer.parseInt(text.trim());
            if (parsed < 0) throw new IllegalArgumentException();
            super(parsed * 2);
        }
    }

    sealed interface Shape permits Circle, Square { }
    record Circle(int r) implements Shape { }
    record Square(int s) implements Shape { }
    static String nested(Shape a, Shape b) {
        return switch (a) {
            case Circle c -> switch (b) {
                case Circle d -> "cc" + (c.r() + d.r());
                case Square s -> "cs" + (c.r() + s.s());
            };
            case Square q -> switch (b) {
                case Circle d -> "sc" + (q.s() + d.r());
                case Square s -> "ss" + (q.s() + s.s());
            };
        };
    }

    public static void main(String[] args) {
        System.out.println(new Over().parts());
        Meta meta = new Meta();
        Entry<Byte> e = new ByteEntry();
        System.out.println(widen(meta.get(e, (byte) 5)) + " " + widenLong(meta.get(e, (byte) 6)));
        System.out.println(OPT.read());
        String d = "x";
        System.out.println(submit(new Supplier<>() { public String get() { return d; } }, 1));
        List<Target<? extends Entity>> targets = List.of(new Target<Entity>() { }, new Target<Player>() { });
        @SuppressWarnings("unchecked")
        Holder<Entity>[] holders = targets.stream().map((Function<Target<?>, Holder<?>>) Holder::new)
            .toArray(Holder[]::new);
        System.out.println(holders.length);
        IntFn f = v -> v + 1;
        System.out.println(twice(f, 4));
        IntFn g = v -> v + 1;
        Consumer<Integer> boxedUser = v -> System.out.println("boxed " + (v + 1));
        boxedUser.accept(g.apply(1));
        System.out.println(new Pair("b", "a").items());
        System.out.println(new Flexible(" 21 ").value);
        System.out.println(nested(new Circle(1), new Square(2)) + nested(new Square(3), new Square(4)));
        Runnable inner = () -> {
            int x = switch ((Object) 3) {
                case Integer i when i > 2 -> i;
                default -> 0;
            };
            System.out.println(x);
        };
        inner.run();
    }
}
