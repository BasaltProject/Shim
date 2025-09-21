/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 25
 */

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class GatherersModern {
  static void probe(String name, Supplier<Object> body) {
    String out;
    try {
      out = String.valueOf(body.get());
    } catch (Throwable t) {
      out = "!" + t.getClass().getName() + ":" + t.getMessage();
    }
    System.out.println(name + "=" + out);
  }

  public static void main(String[] args) {
    probe("fold", () -> Stream.of(1, 2, 3, 4).gather(Gatherers.fold(() -> "", (a, b) -> a + b)).toList());
    probe("fold-empty", () -> Stream.<Integer>empty().gather(Gatherers.fold(() -> 7, (a, b) -> a + b)).toList());
    probe("scan", () -> Stream.of(1, 2, 3, 4).gather(Gatherers.scan(() -> 0, Integer::sum)).toList());
    probe("window-fixed", () -> Stream.of(1, 2, 3, 4, 5).gather(Gatherers.windowFixed(2)).toList());
    probe("window-fixed-exact", () -> Stream.of(1, 2, 3, 4).gather(Gatherers.windowFixed(2)).toList());
    probe("window-sliding", () -> Stream.of(1, 2, 3, 4, 5).gather(Gatherers.windowSliding(3)).toList());
    probe("window-sliding-short", () -> Stream.of(1, 2).gather(Gatherers.windowSliding(3)).toList());
    probe("window-bad", () -> Gatherers.windowFixed(0));
    probe("window-bad2", () -> Gatherers.windowSliding(-1));
    probe("map-concurrent", () -> IntStream.range(0, 20).boxed().gather(Gatherers.mapConcurrent(4, i -> i * i)).toList());
    probe("map-concurrent-fail", () -> Stream.of(1, 2, 3).gather(Gatherers.mapConcurrent(2, i -> { if (i == 2) throw new IllegalStateException("bad"); return i; })).toList());
    probe("map-concurrent-bad", () -> Gatherers.mapConcurrent(0, x -> x));
    probe("and-then", () -> Stream.of(1, 2, 3, 4, 5, 6).gather(Gatherers.windowFixed(2).andThen(Gatherers.scan(() -> 0, (a, w) -> a + w.size()))).toList());
    probe("and-then-fold", () -> Stream.of(1, 2, 3).gather(Gatherers.scan(() -> 0, Integer::sum).andThen(Gatherers.fold(() -> 0, Integer::sum))).toList());
    probe("chain", () -> Stream.of("a", "b", "c", "d").gather(Gatherers.windowFixed(2)).gather(Gatherers.fold(ArrayList<String>::new, (l, w) -> { l.addAll(w); return l; })).toList());
    probe("limit", () -> Stream.iterate(1, i -> i + 1).gather(Gatherers.scan(() -> 0, Integer::sum)).limit(5).toList());
    probe("sequential", () -> {
      Gatherer<Integer, ?, Integer> g = Gatherer.ofSequential((state, e, ds) -> ds.push(e * 2));
      return Stream.of(1, 2, 3).gather(g).toList();
    });
    probe("stateful", () -> {
      Gatherer<String, int[], String> g = Gatherer.ofSequential(() -> new int[1], (st, e, ds) -> ds.push(e + st[0]++), (st, ds) -> ds.push("end" + st[0]));
      return Stream.of("a", "b").gather(g).toList();
    });
    probe("short-circuit", () -> {
      Gatherer<Integer, int[], Integer> g = Gatherer.ofSequential(() -> new int[1], (st, e, ds) -> { if (++st[0] > 3) return false; return ds.push(e); }, (st, ds) -> ds.push(-st[0]));
      return Stream.of(1, 2, 3, 4, 5, 6).gather(g).toList();
    });
    probe("greedy", () -> Gatherer.Integrator.ofGreedy((Void s, Integer e, Gatherer.Downstream<? super Integer> d) -> d.push(e)) != null);
    probe("parallel-flag", () -> Stream.of(3, 1, 2).parallel().gather(Gatherers.scan(() -> 0, Integer::sum)).toList());
    probe("defaults", () -> { Gatherer<Integer, ?, Integer> g = Gatherer.ofSequential((s, e, d) -> d.push(e)); return g.initializer().get() + " " + (g.finisher() != null) + " " + (g.combiner() != null); });
    probe("null", () -> { try { Stream.of(1).gather(null); return "none"; } catch (NullPointerException e) { return "npe"; } });
    probe("lazy", () -> { int[] c = {0}; var s = Stream.of(1, 2, 3).peek(x -> c[0]++).gather(Gatherers.scan(() -> 0, Integer::sum)); int before = c[0]; var it = s.iterator(); it.next(); return before + " " + c[0]; });
    probe("close", () -> { boolean[] closed = {false}; try (var s = Stream.of(1).onClose(() -> closed[0] = true).gather(Gatherers.scan(() -> 0, Integer::sum))) { s.toList(); } return closed[0]; });
    probe("int-boxed", () -> IntStream.rangeClosed(1, 5).boxed().gather(Gatherers.windowSliding(2)).map(l -> l.get(0) + l.get(1)).toList());

    probe("abstract-spliterator", () -> {
      Spliterator<Integer> sp = new Spliterators.AbstractSpliterator<Integer>(5, Spliterator.ORDERED) {
        int i;
        public boolean tryAdvance(Consumer<? super Integer> a) { if (i >= 5) return false; a.accept(i++); return true; }
      };
      return sp.characteristics() + " " + sp.estimateSize() + " " + StreamSupport.stream(sp, false).toList();
    });
    probe("abstract-spliterator-sized", () -> {
      Spliterator<Integer> sp = new Spliterators.AbstractSpliterator<Integer>(5, Spliterator.SIZED) {
        int i;
        public boolean tryAdvance(Consumer<? super Integer> a) { if (i >= 5) return false; a.accept(i++); return true; }
      };
      var half = sp.trySplit();
      List<Integer> l = new ArrayList<>(); half.forEachRemaining(l::add); sp.forEachRemaining(l::add);
      return sp.characteristics() + " " + l;
    });
    probe("abstract-int", () -> {
      Spliterator.OfInt sp = new Spliterators.AbstractIntSpliterator(4, 0) {
        int i;
        public boolean tryAdvance(java.util.function.IntConsumer a) { if (i >= 4) return false; a.accept(i++); return true; }
      };
      var half = sp.trySplit();
      return half.estimateSize() + " " + sp.estimateSize();
    });
    probe("errors", () -> new UnknownError("u").getMessage() + " " + new ClassCircularityError("c") + " " + (new UnknownError() instanceof VirtualMachineError) + " " + (new ClassCircularityError() instanceof LinkageError));
    probe("enum-not-present", () -> { var e = new EnumConstantNotPresentException(Thread.State.class, "FOO"); return e.getMessage() + " " + e.enumType().getSimpleName() + " " + e.constantName(); });
  }
}
