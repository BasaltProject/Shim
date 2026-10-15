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
import java.util.function.Supplier;

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
}
