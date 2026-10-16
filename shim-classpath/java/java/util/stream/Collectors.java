/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util.stream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.IntSummaryStatistics;
import java.util.LongSummaryStatistics;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

public final class Collectors {
  private Collectors() {}

  static class CollectorImpl<T, A, R> implements Collector<T, A, R> {
    private final Supplier<A> supplier;
    private final BiConsumer<A, T> accumulator;
    private final BinaryOperator<A> combiner;
    private final Function<A, R> finisher;
    private final Set<Characteristics> characteristics;

    CollectorImpl(
        Supplier<A> supplier,
        BiConsumer<A, T> accumulator,
        BinaryOperator<A> combiner,
        Function<A, R> finisher,
        Set<Characteristics> characteristics) {
      this.supplier = supplier;
      this.accumulator = accumulator;
      this.combiner = combiner;
      this.finisher = finisher;
      this.characteristics = characteristics;
    }

    @SuppressWarnings("unchecked")
    CollectorImpl(
        Supplier<A> supplier,
        BiConsumer<A, T> accumulator,
        BinaryOperator<A> combiner,
        Set<Characteristics> characteristics) {
      this(supplier, accumulator, combiner, a -> (R) a, characteristics);
    }

    @Override
    public Supplier<A> supplier() {
      return supplier;
    }

    @Override
    public BiConsumer<A, T> accumulator() {
      return accumulator;
    }

    @Override
    public BinaryOperator<A> combiner() {
      return combiner;
    }

    @Override
    public Function<A, R> finisher() {
      return finisher;
    }

    @Override
    public Set<Characteristics> characteristics() {
      return characteristics;
    }
  }

  public static <T> Collector<T, ?, List<T>> toList() {
    return new CollectorImpl<>(
        ArrayList::new,
        List::add,
        (left, right) -> { left.addAll(right); return left; },
        Collections.unmodifiableSet(Collections.singleton(Collector.Characteristics.IDENTITY_FINISH)));
  }

  public static <T> Collector<T, ?, List<T>> toUnmodifiableList() {
    return new CollectorImpl<T, List<T>, List<T>>(
        ArrayList::new,
        List::add,
        (left, right) -> { left.addAll(right); return left; },
        Collections::unmodifiableList,
        Collections.emptySet());
  }

  public static <T> Collector<T, ?, Set<T>> toSet() {
    return new CollectorImpl<>(
        HashSet::new,
        Set::add,
        (left, right) -> { left.addAll(right); return left; },
        Collections.unmodifiableSet(Collections.singleton(Collector.Characteristics.IDENTITY_FINISH)));
  }

  public static <T> Collector<T, ?, Set<T>> toUnmodifiableSet() {
    return new CollectorImpl<T, Set<T>, Set<T>>(
        HashSet::new,
        Set::add,
        (left, right) -> { left.addAll(right); return left; },
        Collections::unmodifiableSet,
        Collections.emptySet());
  }

  public static <T, C extends Collection<T>> Collector<T, ?, C> toCollection(Supplier<C> collectionFactory) {
    Objects.requireNonNull(collectionFactory);
    return new CollectorImpl<>(
        collectionFactory,
        Collection::add,
        (left, right) -> { left.addAll(right); return left; },
        Collections.unmodifiableSet(Collections.singleton(Collector.Characteristics.IDENTITY_FINISH)));
  }

  public static <T, K, U> Collector<T, ?, Map<K, U>> toMap(
      Function<? super T, ? extends K> keyMapper,
      Function<? super T, ? extends U> valueMapper) {
    return toMap(keyMapper, valueMapper, throwingMerger(), HashMap::new);
  }

  public static <T, K, U> Collector<T, ?, Map<K, U>> toMap(
      Function<? super T, ? extends K> keyMapper,
      Function<? super T, ? extends U> valueMapper,
      BinaryOperator<U> mergeFunction) {
    return toMap(keyMapper, valueMapper, mergeFunction, HashMap::new);
  }

  public static <T, K, U, M extends Map<K, U>> Collector<T, ?, M> toMap(
      Function<? super T, ? extends K> keyMapper,
      Function<? super T, ? extends U> valueMapper,
      BinaryOperator<U> mergeFunction,
      Supplier<M> mapFactory) {
    Objects.requireNonNull(keyMapper);
    Objects.requireNonNull(valueMapper);
    Objects.requireNonNull(mergeFunction);
    Objects.requireNonNull(mapFactory);
    BiConsumer<M, T> accumulator = (map, element) -> {
      K k = keyMapper.apply(element);
      U v = Objects.requireNonNull(valueMapper.apply(element));
      U old = map.get(k);
      if (old == null) {
        map.put(k, v);
      } else {
        map.put(k, mergeFunction.apply(old, v));
      }
    };
    return new CollectorImpl<>(
        mapFactory,
        accumulator,
        (m1, m2) -> {
          for (Map.Entry<K, U> e : m2.entrySet()) {
            U old = m1.get(e.getKey());
            if (old == null) {
              m1.put(e.getKey(), e.getValue());
            } else {
              m1.put(e.getKey(), mergeFunction.apply(old, e.getValue()));
            }
          }
          return m1;
        },
        Collections.unmodifiableSet(Collections.singleton(Collector.Characteristics.IDENTITY_FINISH)));
  }

  public static <T, K, U> Collector<T, ?, Map<K, U>> toUnmodifiableMap(
      Function<? super T, ? extends K> keyMapper,
      Function<? super T, ? extends U> valueMapper) {
    return toUnmodifiableMap(keyMapper, valueMapper, throwingMerger());
  }

  public static <T, K, U> Collector<T, ?, Map<K, U>> toUnmodifiableMap(
      Function<? super T, ? extends K> keyMapper,
      Function<? super T, ? extends U> valueMapper,
      BinaryOperator<U> mergeFunction) {
    Objects.requireNonNull(keyMapper);
    Objects.requireNonNull(valueMapper);
    Objects.requireNonNull(mergeFunction);
    BiConsumer<Map<K, U>, T> accumulator = (map, element) -> {
      K k = keyMapper.apply(element);
      U v = Objects.requireNonNull(valueMapper.apply(element));
      U old = map.get(k);
      if (old == null) {
        map.put(k, v);
      } else {
        map.put(k, mergeFunction.apply(old, v));
      }
    };
    return new CollectorImpl<T, Map<K, U>, Map<K, U>>(
        HashMap::new,
        accumulator,
        (m1, m2) -> {
          for (Map.Entry<K, U> e : m2.entrySet()) {
            U old = m1.get(e.getKey());
            if (old == null) {
              m1.put(e.getKey(), e.getValue());
            } else {
              m1.put(e.getKey(), mergeFunction.apply(old, e.getValue()));
            }
          }
          return m1;
        },
        Collections::unmodifiableMap,
        Collections.emptySet());
  }

  private static <T> BinaryOperator<T> throwingMerger() {
    return (u, v) -> {
      throw new IllegalStateException(String.format("Duplicate key %s", u));
    };
  }

  public static Collector<CharSequence, ?, String> joining() {
    return new CollectorImpl<>(
        StringBuilder::new,
        StringBuilder::append,
        (sb1, sb2) -> { sb1.append(sb2); return sb1; },
        StringBuilder::toString,
        Collections.emptySet());
  }

  public static Collector<CharSequence, ?, String> joining(CharSequence delimiter) {
    return joining(delimiter, "", "");
  }

  public static Collector<CharSequence, ?, String> joining(
      CharSequence delimiter,
      CharSequence prefix,
      CharSequence suffix) {
    Objects.requireNonNull(delimiter);
    Objects.requireNonNull(prefix);
    Objects.requireNonNull(suffix);
    return new CollectorImpl<>(
        () -> new StringJoiner(delimiter, prefix, suffix),
        StringJoiner::add,
        StringJoiner::merge,
        StringJoiner::toString,
        Collections.emptySet());
  }

  public static <T, U, A, R> Collector<T, ?, R> mapping(
      Function<? super T, ? extends U> mapper,
      Collector<? super U, A, R> downstream) {
    Objects.requireNonNull(mapper);
    Objects.requireNonNull(downstream);
    BiConsumer<A, ? super U> downstreamAccumulator = downstream.accumulator();
    return new CollectorImpl<>(
        downstream.supplier(),
        (r, t) -> downstreamAccumulator.accept(r, mapper.apply(t)),
        downstream.combiner(),
        downstream.finisher(),
        downstream.characteristics());
  }

  public static <T, A, R> Collector<T, ?, R> filtering(
      Predicate<? super T> predicate,
      Collector<? super T, A, R> downstream) {
    Objects.requireNonNull(predicate);
    Objects.requireNonNull(downstream);
    BiConsumer<A, ? super T> downstreamAccumulator = downstream.accumulator();
    return new CollectorImpl<>(
        downstream.supplier(),
        (r, t) -> {
          if (predicate.test(t)) {
            downstreamAccumulator.accept(r, t);
          }
        },
        downstream.combiner(),
        downstream.finisher(),
        downstream.characteristics());
  }

  public static <T, U, A, R> Collector<T, ?, R> flatMapping(
      Function<? super T, ? extends Stream<? extends U>> mapper,
      Collector<? super U, A, R> downstream) {
    Objects.requireNonNull(mapper);
    Objects.requireNonNull(downstream);
    BiConsumer<A, ? super U> downstreamAccumulator = downstream.accumulator();
    return new CollectorImpl<>(
        downstream.supplier(),
        (r, t) -> {
          try (Stream<? extends U> result = mapper.apply(t)) {
            if (result != null) {
              result.forEach(u -> downstreamAccumulator.accept(r, u));
            }
          }
        },
        downstream.combiner(),
        downstream.finisher(),
        downstream.characteristics());
  }

  public static <T> Collector<T, ?, Long> counting() {
    return reducing(0L, e -> 1L, Long::sum);
  }

  public static <T> Collector<T, ?, java.util.Optional<T>> reducing(BinaryOperator<T> op) {
    Objects.requireNonNull(op);
    class Box {
      T value = null;
      boolean present = false;
    }
    return new CollectorImpl<>(
        Box::new,
        (box, t) -> {
          if (!box.present) {
            box.value = t;
            box.present = true;
          } else {
            box.value = op.apply(box.value, t);
          }
        },
        (b1, b2) -> {
          if (b2.present) {
            if (!b1.present) {
              b1.value = b2.value;
              b1.present = true;
            } else {
              b1.value = op.apply(b1.value, b2.value);
            }
          }
          return b1;
        },
        box -> box.present ? java.util.Optional.of(box.value) : java.util.Optional.empty(),
        Collections.emptySet());
  }

  public static <T> Collector<T, ?, T> reducing(T identity, BinaryOperator<T> op) {
    Objects.requireNonNull(op);
    return new CollectorImpl<>(
        () -> new Object[]{identity},
        (box, t) -> {
          @SuppressWarnings("unchecked")
          T cur = (T) box[0];
          box[0] = op.apply(cur, t);
        },
        (b1, b2) -> {
          @SuppressWarnings("unchecked")
          T v1 = (T) b1[0];
          @SuppressWarnings("unchecked")
          T v2 = (T) b2[0];
          b1[0] = op.apply(v1, v2);
          return b1;
        },
        box -> {
          @SuppressWarnings("unchecked")
          T v = (T) box[0];
          return v;
        },
        Collections.emptySet());
  }

  public static <T, U> Collector<T, ?, U> reducing(
      U identity,
      Function<? super T, ? extends U> mapper,
      BinaryOperator<U> op) {
    Objects.requireNonNull(mapper);
    Objects.requireNonNull(op);
    return new CollectorImpl<>(
        () -> new Object[]{identity},
        (box, t) -> {
          @SuppressWarnings("unchecked")
          U cur = (U) box[0];
          box[0] = op.apply(cur, mapper.apply(t));
        },
        (b1, b2) -> {
          @SuppressWarnings("unchecked")
          U v1 = (U) b1[0];
          @SuppressWarnings("unchecked")
          U v2 = (U) b2[0];
          b1[0] = op.apply(v1, v2);
          return b1;
        },
        box -> {
          @SuppressWarnings("unchecked")
          U v = (U) box[0];
          return v;
        },
        Collections.emptySet());
  }

  public static <T, A, R, RR> Collector<T, A, RR> collectingAndThen(
      Collector<T, A, R> downstream,
      Function<R, RR> finisher) {
    Objects.requireNonNull(downstream);
    Objects.requireNonNull(finisher);
    Set<Collector.Characteristics> cs = downstream.characteristics();
    if (cs.contains(Collector.Characteristics.IDENTITY_FINISH)) {
      if (cs.size() == 1) {
        cs = Collections.emptySet();
      } else {
        cs = new HashSet<>(cs);
        cs.remove(Collector.Characteristics.IDENTITY_FINISH);
        cs = Collections.unmodifiableSet(cs);
      }
    }
    return new CollectorImpl<>(
        downstream.supplier(),
        downstream.accumulator(),
        downstream.combiner(),
        downstream.finisher().andThen(finisher),
        cs);
  }

  public static <T, K> Collector<T, ?, Map<K, List<T>>> groupingBy(
      Function<? super T, ? extends K> classifier) {
    return groupingBy(classifier, HashMap::new, toList());
  }

  public static <T, K, A, D> Collector<T, ?, Map<K, D>> groupingBy(
      Function<? super T, ? extends K> classifier,
      Collector<? super T, A, D> downstream) {
    return groupingBy(classifier, HashMap::new, downstream);
  }

  public static <T, K, D, A, M extends Map<K, D>> Collector<T, ?, M> groupingBy(
      Function<? super T, ? extends K> classifier,
      Supplier<M> mapFactory,
      Collector<? super T, A, D> downstream) {
    Objects.requireNonNull(classifier);
    Objects.requireNonNull(mapFactory);
    Objects.requireNonNull(downstream);
    Supplier<A> downstreamSupplier = downstream.supplier();
    BiConsumer<A, ? super T> downstreamAccumulator = downstream.accumulator();
    BiConsumer<Map<K, A>, T> accumulator = (m, t) -> {
      K key = Objects.requireNonNull(classifier.apply(t));
      A container = m.computeIfAbsent(key, k -> downstreamSupplier.get());
      downstreamAccumulator.accept(container, t);
    };
    return new CollectorImpl<>(
        () -> new HashMap<K, A>(),
        accumulator,
        (m1, m2) -> {
          for (Map.Entry<K, A> entry : m2.entrySet()) {
            A existing = m1.get(entry.getKey());
            if (existing == null) {
              m1.put(entry.getKey(), entry.getValue());
            } else {
              m1.put(entry.getKey(), downstream.combiner().apply(existing, entry.getValue()));
            }
          }
          return m1;
        },
        intermediate -> {
          M result = mapFactory.get();
          Function<A, D> finisher = downstream.finisher();
          for (Map.Entry<K, A> entry : intermediate.entrySet()) {
            result.put(entry.getKey(), finisher.apply(entry.getValue()));
          }
          return result;
        },
        Collections.emptySet());
  }

  public static <T> Collector<T, ?, Map<Boolean, List<T>>> partitioningBy(
      Predicate<? super T> predicate) {
    return partitioningBy(predicate, toList());
  }

  public static <T, D, A> Collector<T, ?, Map<Boolean, D>> partitioningBy(
      Predicate<? super T> predicate,
      Collector<? super T, A, D> downstream) {
    Objects.requireNonNull(predicate);
    Objects.requireNonNull(downstream);
    BiConsumer<A, ? super T> downstreamAccumulator = downstream.accumulator();
    class Partition {
      A forTrue = downstream.supplier().get();
      A forFalse = downstream.supplier().get();
    }
    return new CollectorImpl<>(
        Partition::new,
        (partition, t) -> {
          if (predicate.test(t)) {
            downstreamAccumulator.accept(partition.forTrue, t);
          } else {
            downstreamAccumulator.accept(partition.forFalse, t);
          }
        },
        (p1, p2) -> {
          p1.forTrue = downstream.combiner().apply(p1.forTrue, p2.forTrue);
          p1.forFalse = downstream.combiner().apply(p1.forFalse, p2.forFalse);
          return p1;
        },
        partition -> {
          Map<Boolean, D> result = new HashMap<>(2);
          result.put(Boolean.TRUE, downstream.finisher().apply(partition.forTrue));
          result.put(Boolean.FALSE, downstream.finisher().apply(partition.forFalse));
          return result;
        },
        Collections.emptySet());
  }

  public static <T> Collector<T, ?, java.util.Optional<T>> minBy(Comparator<? super T> comparator) {
    return reducing(BinaryOperator.minBy(comparator));
  }

  public static <T> Collector<T, ?, java.util.Optional<T>> maxBy(Comparator<? super T> comparator) {
    return reducing(BinaryOperator.maxBy(comparator));
  }

  public static <T> Collector<T, ?, Integer> summingInt(ToIntFunction<? super T> mapper) {
    Objects.requireNonNull(mapper);
    return new CollectorImpl<T, int[], Integer>(
        () -> new int[1],
        (a, t) -> { a[0] += mapper.applyAsInt(t); },
        (a, b) -> { a[0] += b[0]; return a; },
        a -> a[0],
        Collections.emptySet());
  }

  public static <T> Collector<T, ?, Long> summingLong(ToLongFunction<? super T> mapper) {
    Objects.requireNonNull(mapper);
    return new CollectorImpl<T, long[], Long>(
        () -> new long[1],
        (a, t) -> { a[0] += mapper.applyAsLong(t); },
        (a, b) -> { a[0] += b[0]; return a; },
        a -> a[0],
        Collections.emptySet());
  }

  public static <T> Collector<T, ?, Double> summingDouble(ToDoubleFunction<? super T> mapper) {
    Objects.requireNonNull(mapper);
    return new CollectorImpl<T, double[], Double>(
        () -> new double[3],
        (a, t) -> {
          double val = mapper.applyAsDouble(t);
          sumWithCompensation(a, val);
          a[2] += val;
        },
        (a, b) -> {
          sumWithCompensation(a, b[0]);
          a[2] += b[2];
          return sumWithCompensation(a, -b[1]);
        },
        a -> computeFinalSum(a),
        Collections.emptySet());
  }

  static double[] sumWithCompensation(double[] intermediateSum, double value) {
    double tmp = value - intermediateSum[1];
    double sum = intermediateSum[0];
    double velvel = sum + tmp;
    intermediateSum[1] = (velvel - sum) - tmp;
    intermediateSum[0] = velvel;
    return intermediateSum;
  }

  static double computeFinalSum(double[] summands) {
    double tmp = summands[0] - summands[1];
    double simpleSum = summands[summands.length - 1];
    if (Double.isNaN(tmp) && Double.isInfinite(simpleSum)) {
      return simpleSum;
    } else {
      return tmp;
    }
  }

  public static <T> Collector<T, ?, Double> averagingInt(ToIntFunction<? super T> mapper) {
    Objects.requireNonNull(mapper);
    return new CollectorImpl<T, long[], Double>(
        () -> new long[2],
        (a, t) -> { a[0] += mapper.applyAsInt(t); a[1]++; },
        (a, b) -> { a[0] += b[0]; a[1] += b[1]; return a; },
        a -> (a[1] == 0) ? 0.0d : (double) a[0] / a[1],
        Collections.emptySet());
  }

  public static <T> Collector<T, ?, Double> averagingLong(ToLongFunction<? super T> mapper) {
    Objects.requireNonNull(mapper);
    return new CollectorImpl<T, long[], Double>(
        () -> new long[2],
        (a, t) -> { a[0] += mapper.applyAsLong(t); a[1]++; },
        (a, b) -> { a[0] += b[0]; a[1] += b[1]; return a; },
        a -> (a[1] == 0) ? 0.0d : (double) a[0] / a[1],
        Collections.emptySet());
  }

  public static <T> Collector<T, ?, Double> averagingDouble(ToDoubleFunction<? super T> mapper) {
    Objects.requireNonNull(mapper);
    return new CollectorImpl<T, double[], Double>(
        () -> new double[4],
        (a, t) -> {
          double val = mapper.applyAsDouble(t);
          sumWithCompensation(a, val);
          a[2]++;
          a[3] += val;
        },
        (a, b) -> {
          sumWithCompensation(a, b[0]);
          sumWithCompensation(a, -b[1]);
          a[2] += b[2];
          a[3] += b[3];
          return a;
        },
        a -> (a[2] == 0) ? 0.0d : (computeFinalSum(a) / a[2]),
        Collections.emptySet());
  }

  public static <T> Collector<T, ?, IntSummaryStatistics> summarizingInt(ToIntFunction<? super T> mapper) {
    Objects.requireNonNull(mapper);
    return new CollectorImpl<T, IntSummaryStatistics, IntSummaryStatistics>(
        IntSummaryStatistics::new,
        (r, t) -> r.accept(mapper.applyAsInt(t)),
        (l, r) -> { l.combine(r); return l; },
        Collections.unmodifiableSet(Collections.singleton(Collector.Characteristics.IDENTITY_FINISH)));
  }

  public static <T> Collector<T, ?, LongSummaryStatistics> summarizingLong(ToLongFunction<? super T> mapper) {
    Objects.requireNonNull(mapper);
    return new CollectorImpl<T, LongSummaryStatistics, LongSummaryStatistics>(
        LongSummaryStatistics::new,
        (r, t) -> r.accept(mapper.applyAsLong(t)),
        (l, r) -> { l.combine(r); return l; },
        Collections.unmodifiableSet(Collections.singleton(Collector.Characteristics.IDENTITY_FINISH)));
  }

  public static <T> Collector<T, ?, DoubleSummaryStatistics> summarizingDouble(ToDoubleFunction<? super T> mapper) {
    Objects.requireNonNull(mapper);
    return new CollectorImpl<T, DoubleSummaryStatistics, DoubleSummaryStatistics>(
        DoubleSummaryStatistics::new,
        (r, t) -> r.accept(mapper.applyAsDouble(t)),
        (l, r) -> { l.combine(r); return l; },
        Collections.unmodifiableSet(Collections.singleton(Collector.Characteristics.IDENTITY_FINISH)));
  }

  public static <T, R1, R2, R> Collector<T, ?, R> teeing(
      Collector<? super T, ?, R1> downstream1,
      Collector<? super T, ?, R2> downstream2,
      BiFunction<? super R1, ? super R2, R> merger) {
    return teeing0(downstream1, downstream2, merger);
  }

  private static <T, A1, A2, R1, R2, R> Collector<T, ?, R> teeing0(
      Collector<? super T, A1, R1> downstream1,
      Collector<? super T, A2, R2> downstream2,
      BiFunction<? super R1, ? super R2, R> merger) {
    Objects.requireNonNull(downstream1, "downstream1");
    Objects.requireNonNull(downstream2, "downstream2");
    Objects.requireNonNull(merger, "merger");
    Supplier<A1> c1Supplier = Objects.requireNonNull(downstream1.supplier(), "downstream1 supplier");
    Supplier<A2> c2Supplier = Objects.requireNonNull(downstream2.supplier(), "downstream2 supplier");
    BiConsumer<A1, ? super T> c1Accumulator = Objects.requireNonNull(downstream1.accumulator(), "downstream1 accumulator");
    BiConsumer<A2, ? super T> c2Accumulator = Objects.requireNonNull(downstream2.accumulator(), "downstream2 accumulator");
    BinaryOperator<A1> c1Combiner = Objects.requireNonNull(downstream1.combiner(), "downstream1 combiner");
    BinaryOperator<A2> c2Combiner = Objects.requireNonNull(downstream2.combiner(), "downstream2 combiner");
    Function<A1, R1> c1Finisher = Objects.requireNonNull(downstream1.finisher(), "downstream1 finisher");
    Function<A2, R2> c2Finisher = Objects.requireNonNull(downstream2.finisher(), "downstream2 finisher");
    final class PairBox {
      A1 left = c1Supplier.get();
      A2 right = c2Supplier.get();

      void add(T t) {
        c1Accumulator.accept(left, t);
        c2Accumulator.accept(right, t);
      }

      PairBox combine(PairBox other) {
        left = c1Combiner.apply(left, other.left);
        right = c2Combiner.apply(right, other.right);
        return this;
      }

      R get() {
        R1 r1 = c1Finisher.apply(left);
        R2 r2 = c2Finisher.apply(right);
        return merger.apply(r1, r2);
      }
    }
    return new CollectorImpl<T, PairBox, R>(PairBox::new, PairBox::add, PairBox::combine, PairBox::get,
                                            Collections.emptySet());
  }

  public static <T, K, U> Collector<T, ?, ConcurrentMap<K, U>> toConcurrentMap(
      Function<? super T, ? extends K> keyMapper,
      Function<? super T, ? extends U> valueMapper) {
    return toConcurrentMap(keyMapper, valueMapper, throwingMerger(), ConcurrentHashMap::new);
  }

  public static <T, K, U> Collector<T, ?, ConcurrentMap<K, U>> toConcurrentMap(
      Function<? super T, ? extends K> keyMapper,
      Function<? super T, ? extends U> valueMapper,
      BinaryOperator<U> mergeFunction) {
    return toConcurrentMap(keyMapper, valueMapper, mergeFunction, ConcurrentHashMap::new);
  }

  public static <T, K, U, M extends ConcurrentMap<K, U>> Collector<T, ?, M> toConcurrentMap(
      Function<? super T, ? extends K> keyMapper,
      Function<? super T, ? extends U> valueMapper,
      BinaryOperator<U> mergeFunction,
      Supplier<M> mapFactory) {
    Objects.requireNonNull(keyMapper);
    Objects.requireNonNull(valueMapper);
    Objects.requireNonNull(mergeFunction);
    Objects.requireNonNull(mapFactory);
    BiConsumer<M, T> accumulator =
        (map, element) -> map.merge(keyMapper.apply(element), valueMapper.apply(element), mergeFunction);
    return new CollectorImpl<T, M, M>(mapFactory, accumulator, (left, right) -> {
      for (Map.Entry<K, U> e : right.entrySet()) {
        left.merge(e.getKey(), e.getValue(), mergeFunction);
      }
      return left;
    }, Collections.unmodifiableSet(Collections.singleton(Collector.Characteristics.IDENTITY_FINISH)));
  }

  public static <T, K> Collector<T, ?, ConcurrentMap<K, List<T>>> groupingByConcurrent(
      Function<? super T, ? extends K> classifier) {
    return groupingByConcurrent(classifier, ConcurrentHashMap::new, toList());
  }

  public static <T, K, A, D> Collector<T, ?, ConcurrentMap<K, D>> groupingByConcurrent(
      Function<? super T, ? extends K> classifier, Collector<? super T, A, D> downstream) {
    return groupingByConcurrent(classifier, ConcurrentHashMap::new, downstream);
  }

  @SuppressWarnings("unchecked")
  public static <T, K, A, D, M extends ConcurrentMap<K, D>> Collector<T, ?, M> groupingByConcurrent(
      Function<? super T, ? extends K> classifier, Supplier<M> mapFactory, Collector<? super T, A, D> downstream) {
    Objects.requireNonNull(classifier);
    Objects.requireNonNull(mapFactory);
    Objects.requireNonNull(downstream);
    Supplier<A> downstreamSupplier = downstream.supplier();
    BiConsumer<A, ? super T> downstreamAccumulator = downstream.accumulator();
    BinaryOperator<ConcurrentMap<K, A>> merger = (m1, m2) -> {
      for (Map.Entry<K, A> e : m2.entrySet()) {
        m1.merge(e.getKey(), e.getValue(), downstream.combiner());
      }
      return m1;
    };
    Supplier<ConcurrentMap<K, A>> mangledFactory = (Supplier<ConcurrentMap<K, A>>) mapFactory;
    BiConsumer<ConcurrentMap<K, A>, T> accumulator = (m, t) -> {
      K key = Objects.requireNonNull(classifier.apply(t), "element cannot be mapped to a null key");
      A resultContainer = m.computeIfAbsent(key, k -> downstreamSupplier.get());
      synchronized (resultContainer) {
        downstreamAccumulator.accept(resultContainer, t);
      }
    };
    return new CollectorImpl<T, ConcurrentMap<K, A>, M>(mangledFactory, accumulator, merger,
        intermediate -> {
          intermediate.replaceAll((k, v) -> ((Function<A, A>) downstream.finisher()).apply(v));
          return (M) intermediate;
        }, Collections.emptySet());
  }
}
