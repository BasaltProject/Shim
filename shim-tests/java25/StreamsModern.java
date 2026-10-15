/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 17

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class StreamsModern {
    public static void main(String[] args) {
        testBasicStream();
        testStreamToList();
        testFactoriesAndPredicates();
        testTakeWhileDropWhile();
        testReductionsAndMatching();
        testFlatMap();
        testCollectors();
        testPrimitiveStreams();
        testIntegrationWithExistingTypes();
        testSpliterators();
        System.out.println("ALL STREAMS MODERN TESTS PASSED");
    }

    private static void testBasicStream() {
        List<String> list = Arrays.asList("apple", "banana", "cherry", "avocado", "apricot");
        List<String> aWords = list.stream()
                .filter(s -> s.startsWith("a"))
                .map(String::toUpperCase)
                .sorted()
                .collect(Collectors.toList());
        System.out.println("aWords: " + aWords);

        long count = list.stream().filter(s -> s.length() > 5).count();
        System.out.println("length > 5 count: " + count);

        List<Integer> numbers = Arrays.asList(5, 3, 1, 4, 2, 3, 5);
        List<Integer> distinctSorted = numbers.stream().distinct().sorted().collect(Collectors.toList());
        System.out.println("distinctSorted: " + distinctSorted);

        List<Integer> skippedLimited = numbers.stream().skip(2).limit(3).collect(Collectors.toList());
        System.out.println("skippedLimited: " + skippedLimited);
    }

    private static void testStreamToList() {
        List<String> items = Stream.of("x", "y", "z").toList();
        System.out.println("items: " + items);
        try {
            items.add("w");
            throw new AssertionError();
        } catch (UnsupportedOperationException expected) {
            System.out.println("toList immutable: true");
        }
    }

    private static void testFactoriesAndPredicates() {
        System.out.println("empty count: " + Stream.empty().count());
        System.out.println("of count: " + Stream.of("one").count());
        System.out.println("ofNullable null count: " + Stream.ofNullable(null).count());
        System.out.println("ofNullable nonnull count: " + Stream.ofNullable("val").count());

        List<Integer> iterated = Stream.iterate(1, n -> n + 2).limit(4).toList();
        System.out.println("iterate unary: " + iterated);

        List<Integer> iteratedPredicate = Stream.iterate(1, n -> n <= 7, n -> n + 2).toList();
        System.out.println("iterate predicate: " + iteratedPredicate);

        List<String> generated = Stream.generate(() -> "gen").limit(3).toList();
        System.out.println("generated: " + generated);

        Stream<String> s1 = Stream.of("a", "b");
        Stream<String> s2 = Stream.of("c", "d");
        List<String> concatted = Stream.concat(s1, s2).toList();
        System.out.println("concat: " + concatted);

        Stream.Builder<String> builder = Stream.builder();
        builder.accept("first");
        builder.add("second").add("third");
        List<String> built = builder.build().toList();
        System.out.println("built: " + built);
    }

    private static void testTakeWhileDropWhile() {
        List<Integer> nums = List.of(2, 4, 6, 7, 8, 10);
        List<Integer> taken = nums.stream().takeWhile(n -> n % 2 == 0).toList();
        System.out.println("takeWhile even: " + taken);

        List<Integer> dropped = nums.stream().dropWhile(n -> n % 2 == 0).toList();
        System.out.println("dropWhile even: " + dropped);
    }

    private static void testReductionsAndMatching() {
        List<Integer> nums = List.of(1, 2, 3, 4, 5);
        int sum = nums.stream().reduce(0, Integer::sum);
        System.out.println("sum: " + sum);

        Optional<Integer> optSum = nums.stream().reduce(Integer::sum);
        System.out.println("optSum: " + optSum.orElse(-1));

        Optional<Integer> emptySum = Stream.<Integer>empty().reduce(Integer::sum);
        System.out.println("emptySum isPresent: " + emptySum.isPresent());

        System.out.println("anyMatch > 4: " + nums.stream().anyMatch(n -> n > 4));
        System.out.println("anyMatch > 10: " + nums.stream().anyMatch(n -> n > 10));
        System.out.println("allMatch > 0: " + nums.stream().allMatch(n -> n > 0));
        System.out.println("allMatch > 2: " + nums.stream().allMatch(n -> n > 2));
        System.out.println("noneMatch < 0: " + nums.stream().noneMatch(n -> n < 0));
        System.out.println("noneMatch == 3: " + nums.stream().noneMatch(n -> n == 3));

        System.out.println("findFirst: " + nums.stream().findFirst().orElse(-1));
        System.out.println("findAny: " + nums.stream().findAny().orElse(-1));
        System.out.println("min: " + nums.stream().min(Comparator.naturalOrder()).orElse(-1));
        System.out.println("max: " + nums.stream().max(Comparator.naturalOrder()).orElse(-1));
    }

    private static void testFlatMap() {
        List<List<String>> nested = List.of(List.of("a", "b"), List.of("c"), List.of("d", "e"));
        List<String> flat = nested.stream().flatMap(Collection::stream).toList();
        System.out.println("flat: " + flat);
    }

    private static void testCollectors() {
        List<String> list = List.of("apple", "banana", "apricot", "cherry", "banana");
        Set<String> set = list.stream().collect(Collectors.toSet());
        System.out.println("set size: " + set.size());
        System.out.println("set contains banana: " + set.contains("banana"));

        String joined = list.stream().distinct().sorted().collect(Collectors.joining(", ", "[", "]"));
        System.out.println("joined: " + joined);

        Map<Integer, List<String>> byLength = list.stream().collect(Collectors.groupingBy(String::length));
        List<Integer> lengths = new ArrayList<>(byLength.keySet());
        Collections.sort(lengths);
        for (Integer len : lengths) {
            System.out.println("len " + len + ": " + byLength.get(len));
        }

        Map<Boolean, List<String>> partitioned = list.stream().collect(Collectors.partitioningBy(s -> s.startsWith("a")));
        System.out.println("partition true: " + partitioned.get(true));
        System.out.println("partition false: " + partitioned.get(false));

        long count = list.stream().collect(Collectors.counting());
        System.out.println("collector counting: " + count);

        List<Integer> mapped = list.stream().collect(Collectors.mapping(String::length, Collectors.toList()));
        System.out.println("collector mapping: " + mapped);

        List<String> filtered = list.stream().collect(Collectors.filtering(s -> s.startsWith("b"), Collectors.toList()));
        System.out.println("collector filtering: " + filtered);

        int unmodifiableSize = list.stream().collect(Collectors.toUnmodifiableList()).size();
        System.out.println("toUnmodifiableList size: " + unmodifiableSize);
    }

    private static void testPrimitiveStreams() {
        int sum = IntStream.range(1, 5).sum(); // 1+2+3+4 = 10
        System.out.println("IntStream.range sum: " + sum);

        int closedSum = IntStream.rangeClosed(1, 5).sum(); // 1+2+3+4+5 = 15
        System.out.println("IntStream.rangeClosed sum: " + closedSum);

        List<Integer> boxed = IntStream.of(3, 1, 4, 1, 5).boxed().toList();
        System.out.println("IntStream.boxed: " + boxed);

        int max = IntStream.of(10, 40, 20).max().orElse(-1);
        int min = IntStream.of(10, 40, 20).min().orElse(-1);
        System.out.println("IntStream max: " + max + " min: " + min);

        long lSum = LongStream.rangeClosed(10L, 12L).sum(); // 10+11+12 = 33
        System.out.println("LongStream sum: " + lSum);

        double dSum = DoubleStream.of(1.5, 2.5, 3.0).sum();
        System.out.println("DoubleStream sum: " + dSum);

        double avg = IntStream.of(10, 20, 30).average().orElse(0.0);
        System.out.println("IntStream avg: " + avg);
    }

    private static void testIntegrationWithExistingTypes() {
        String multiline = "line1\nline2\r\nline3\rline4";
        List<String> lines = multiline.lines().toList();
        System.out.println("multiline lines: " + lines);

        String text = "abc";
        List<Integer> chars = text.chars().boxed().toList();
        System.out.println("chars: " + chars);

        List<Integer> codePoints = text.codePoints().boxed().toList();
        System.out.println("codePoints: " + codePoints);

        Optional<String> opt = Optional.of("present");
        System.out.println("opt stream: " + opt.stream().toList());

        Optional<String> emptyOpt = Optional.empty();
        System.out.println("empty opt stream: " + emptyOpt.stream().toList());

        OptionalInt optInt = OptionalInt.of(42);
        System.out.println("optInt stream: " + optInt.stream().boxed().toList());

        List<String> modifiable = new ArrayList<>(List.of("alpha", "beta", "gamma"));
        modifiable.removeIf(s -> s.startsWith("b"));
        System.out.println("after removeIf: " + modifiable);

        String[] arr = {"first", "second"};
        System.out.println("Arrays.stream: " + Arrays.stream(arr).toList());

        int[] intArr = {7, 8, 9};
        System.out.println("Arrays.stream(int[]): " + Arrays.stream(intArr).sum());
    }

    private static void testSpliterators() {
        List<String> list = List.of("one", "two", "three");
        Spliterator<String> spliterator = list.spliterator();
        System.out.println("estimateSize: " + spliterator.estimateSize());
        List<String> collected = new ArrayList<>();
        spliterator.forEachRemaining(collected::add);
        System.out.println("spliterator collected: " + collected);

        Spliterator.OfInt intSpliterator = Spliterators.spliterator(new int[]{1, 2, 3}, 0);
        List<Integer> intCollected = new ArrayList<>();
        intSpliterator.forEachRemaining((IntConsumer) intCollected::add);
        System.out.println("intSpliterator collected: " + intCollected);
    }
}
