/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 16
 */

import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.*;

public class UtilGapsModern {
  interface Action { Object run() throws Throwable; }

  static void probe(String label, Action action) {
    try {
      System.out.println(label + ":" + action.run());
    } catch (Throwable error) {
      System.out.println(label + ":" + error.getClass().getName() + ":" + error.getMessage());
    }
  }

  public static void main(String[] args) throws Exception {
    Locale.setDefault(Locale.ROOT);
    List<String> words = List.of("alpha", "beta", "gamma", "delta", "epsilon");
    probe("int-stats", () -> IntStream.of(3, 1, 4, 1, 5).summaryStatistics());
    probe("long-stats", () -> LongStream.of(10L, 20L).summaryStatistics());
    probe("double-stats", () -> DoubleStream.of(0.1, 0.2, 0.3).summaryStatistics());
    probe("empty-int-stats", () -> new IntSummaryStatistics());
    probe("empty-double-stats", () -> new DoubleSummaryStatistics());
    probe("stats-ctor", () -> new IntSummaryStatistics(2, 1, 5, 6));
    probe("stats-ctor-bad", () -> new IntSummaryStatistics(-1, 1, 5, 6));
    probe("stats-combine", () -> {
      IntSummaryStatistics a = IntStream.of(1, 2).summaryStatistics();
      a.combine(IntStream.of(10).summaryStatistics());
      return a;
    });
    probe("summing-int", () -> words.stream().collect(Collectors.summingInt(String::length)));
    probe("summing-long", () -> words.stream().collect(Collectors.summingLong(String::length)));
    probe("summing-double", () -> words.stream().collect(Collectors.summingDouble(s -> s.length() / 10.0)));
    probe("averaging-int", () -> words.stream().collect(Collectors.averagingInt(String::length)));
    probe("averaging-double", () -> words.stream().collect(Collectors.averagingDouble(s -> s.length() / 3.0)));
    probe("summarizing-int", () -> words.stream().collect(Collectors.summarizingInt(String::length)));
    probe("summarizing-double", () -> words.stream().collect(Collectors.summarizingDouble(String::length)));
    probe("min-by", () -> words.stream().collect(Collectors.minBy(Comparator.naturalOrder())));
    probe("max-by", () -> words.stream().collect(Collectors.maxBy(Comparator.comparing(String::length))));
    probe("teeing", () -> words.stream().collect(Collectors.teeing(Collectors.counting(),
        Collectors.joining(","), (n, s) -> n + ":" + s)));
    probe("to-concurrent-map", () -> new TreeMap<>(words.stream().collect(
        Collectors.toConcurrentMap(s -> s.charAt(0), String::length))));
    probe("grouping-concurrent", () -> new TreeMap<>(words.stream().collect(
        Collectors.groupingByConcurrent(String::length, Collectors.counting()))));
    probe("map-multi", () -> Stream.of(1, 2, 3).<Integer>mapMulti((n, sink) -> { sink.accept(n); sink.accept(n * 10); })
        .collect(Collectors.toList()));
    probe("map-multi-int", () -> Stream.of(1, 2).mapMultiToInt((n, sink) -> { sink.accept(n); sink.accept(-n); })
        .sum());
    probe("int-builder", () -> IntStream.builder().add(1).add(2).add(3).build().sum());
    probe("int-map-multi", () -> IntStream.of(1, 2).mapMulti((n, sink) -> { sink.accept(n); sink.accept(n); }).sum());
    probe("collections-list", () -> Collections.list(Collections.enumeration(words)));
    probe("format-code-point", () -> new IllegalFormatCodePointException(0x110000).getMessage());
    probe("format-conversion", () -> new IllegalFormatConversionException('d', String.class).getMessage());
    probe("format-flags", () -> new IllegalFormatFlagsException("-#").getMessage());
    probe("format-precision", () -> new IllegalFormatPrecisionException(7).getPrecision());
    probe("format-mismatch", () -> new FormatFlagsConversionMismatchException("#", 'd').getMessage());
    probe("format-duplicate", () -> new DuplicateFormatFlagsException("--").getMessage());
    probe("format-missing", () -> new MissingFormatArgumentException("%s").getMessage());
    probe("format-unknown", () -> new UnknownFormatConversionException("q").getMessage());
    probe("format-unknown-flags", () -> new UnknownFormatFlagsException("z").getMessage());
    probe("illformed", () -> new IllformedLocaleException("bad", 3).getMessage());
    probe("properties-format", () -> new InvalidPropertiesFormatException(new RuntimeException("x")).getMessage());
    probe("formattable-flags", () -> FormattableFlags.LEFT_JUSTIFY + "," + FormattableFlags.UPPERCASE + ","
        + FormattableFlags.ALTERNATE);

    Timer timer = new Timer("probe", true);
    int[] fired = new int[1];
    Object lock = new Object();
    timer.schedule(new TimerTask() {
      public void run() {
        synchronized (lock) {
          if (++fired[0] == 3) {
            cancel();
            lock.notifyAll();
          }
        }
      }
    }, 10, 10);
    synchronized (lock) {
      long deadline = System.currentTimeMillis() + 5000;
      while (fired[0] < 3 && System.currentTimeMillis() < deadline) lock.wait(100);
    }
    System.out.println("timer-fired:" + fired[0]);
    probe("timer-negative", () -> { timer.schedule(new TimerTask() { public void run() { } }, -1); return 0; });
    probe("timer-period", () -> { timer.schedule(new TimerTask() { public void run() { } }, 1, 0); return 0; });
    TimerTask reused = new TimerTask() { public void run() { } };
    timer.schedule(reused, 60000);
    probe("timer-twice", () -> { timer.schedule(reused, 1); return 0; });
    System.out.println("timer-cancel-task:" + reused.cancel());
    System.out.println("timer-purge:" + timer.purge());
    timer.cancel();
    probe("timer-cancelled", () -> { timer.schedule(new TimerTask() { public void run() { } }, 1); return 0; });
  }
}
