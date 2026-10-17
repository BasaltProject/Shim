/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util.stream;

import java.util.ArrayDeque;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Supplier;

final class GatherImpl<T, A, R> implements Gatherer<T, A, R> {
  private final Supplier<A> initializer;
  private final Gatherer.Integrator<A, T, R> integrator;
  private final BinaryOperator<A> combiner;
  private final BiConsumer<A, Gatherer.Downstream<? super R>> finisher;

  GatherImpl(Supplier<A> initializer, Gatherer.Integrator<A, T, R> integrator, BinaryOperator<A> combiner,
      BiConsumer<A, Gatherer.Downstream<? super R>> finisher) {
    this.initializer = initializer;
    this.integrator = integrator;
    this.combiner = combiner;
    this.finisher = finisher;
  }

  @SuppressWarnings("unchecked")
  static <A> Supplier<A> defaultInitializer() {
    return (Supplier<A>) kNullInitializer;
  }

  @SuppressWarnings("unchecked")
  static <A> BinaryOperator<A> failingCombiner() {
    return (BinaryOperator<A>) kFailingCombiner;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  static <A, R> BiConsumer<A, Gatherer.Downstream<? super R>> defaultFinisher() {
    return (BiConsumer<A, Gatherer.Downstream<? super R>>) (BiConsumer) kNoFinisher;
  }

  private static final Supplier<Object> kNullInitializer = new Supplier<Object>() {
    public Object get() {
      return null;
    }
  };

  private static final BinaryOperator<Object> kFailingCombiner = new BinaryOperator<Object>() {
    public Object apply(Object a, Object b) {
      throw new UnsupportedOperationException("combiner");
    }
  };

  private static final BiConsumer<Object, Gatherer.Downstream<? super Object>> kNoFinisher =
      new BiConsumer<Object, Gatherer.Downstream<? super Object>>() {
        public void accept(Object state, Gatherer.Downstream<? super Object> downstream) { }
      };

  @Override
  public Supplier<A> initializer() {
    return initializer != null ? initializer : GatherImpl.<A>defaultInitializer();
  }

  @Override
  public Gatherer.Integrator<A, T, R> integrator() {
    return integrator;
  }

  @Override
  public BinaryOperator<A> combiner() {
    return combiner != null ? combiner : GatherImpl.<A>failingCombiner();
  }

  @Override
  public BiConsumer<A, Gatherer.Downstream<? super R>> finisher() {
    return finisher != null ? finisher : GatherImpl.<A, R>defaultFinisher();
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  static <T, R, RR> Gatherer<T, ?, RR> compose(final Gatherer<T, ?, R> first, final Gatherer<? super R, ?,
      ? extends RR> second) {
    final Gatherer<T, Object, R> g1 = (Gatherer) first;
    final Gatherer<R, Object, RR> g2 = (Gatherer) second;
    Supplier<Object[]> init = new Supplier<Object[]>() {
      public Object[] get() {
        return new Object[] {g1.initializer().get(), g2.initializer().get()};
      }
    };
    Gatherer.Integrator<Object[], T, RR> integ = new Gatherer.Integrator<Object[], T, RR>() {
      public boolean integrate(final Object[] state, T element, final Gatherer.Downstream<? super RR> downstream) {
        final boolean[] secondOpen = {true};
        boolean more = g1.integrator().integrate(state[0], element, new Gatherer.Downstream<R>() {
          public boolean push(R r) {
            if (!g2.integrator().integrate(state[1], r, downstream)) {
              secondOpen[0] = false;
            }
            return secondOpen[0];
          }

          public boolean isRejecting() {
            return !secondOpen[0] || downstream.isRejecting();
          }
        });
        return more && secondOpen[0];
      }
    };
    BinaryOperator<Object[]> comb = new BinaryOperator<Object[]>() {
      public Object[] apply(Object[] a, Object[] b) {
        return new Object[] {g1.combiner().apply(a[0], b[0]), g2.combiner().apply(a[1], b[1])};
      }
    };
    BiConsumer<Object[], Gatherer.Downstream<? super RR>> fin = new BiConsumer<Object[],
        Gatherer.Downstream<? super RR>>() {
      public void accept(final Object[] state, final Gatherer.Downstream<? super RR> downstream) {
        g1.finisher().accept(state[0], new Gatherer.Downstream<R>() {
          public boolean push(R r) {
            return g2.integrator().integrate(state[1], r, downstream);
          }

          public boolean isRejecting() {
            return downstream.isRejecting();
          }
        });
        g2.finisher().accept(state[1], downstream);
      }
    };
    return new GatherImpl<T, Object[], RR>(init, integ, comb, fin);
  }

  static <T, A, R> Spliterator<R> spliterator(
      final Spliterator<T> source, final Gatherer<? super T, A, R> gatherer) {
    return new GatherSpliterator<T, A, R>(source, gatherer);
  }

  static final class GatherSpliterator<T, A, R> implements Spliterator<R>, Gatherer.Downstream<R> {
    private final Spliterator<T> source;
    private final Gatherer<? super T, A, R> gatherer;
    private final ArrayDeque<R> buffer = new ArrayDeque<R>();
    private A state;
    private boolean started;
    private boolean stopped;
    private boolean finished;
    private Gatherer.Integrator<A, ? super T, R> integrator;

    GatherSpliterator(Spliterator<T> source, Gatherer<? super T, A, R> gatherer) {
      this.source = source;
      this.gatherer = gatherer;
    }

    public boolean push(R element) {
      buffer.add(element);
      return true;
    }

    public boolean isRejecting() {
      return false;
    }

    public boolean tryAdvance(Consumer<? super R> action) {
      if (action == null) {
        throw new NullPointerException();
      }
      for (;;) {
        if (!buffer.isEmpty()) {
          action.accept(buffer.poll());
          return true;
        }
        if (finished) {
          return false;
        }
        if (!started) {
          started = true;
          state = gatherer.initializer().get();
          integrator = gatherer.integrator();
        }
        if (stopped) {
          finished = true;
          gatherer.finisher().accept(state, this);
          continue;
        }
        boolean advanced = source.tryAdvance(new Consumer<T>() {
          public void accept(T t) {
            if (!integrator.integrate(state, t, GatherSpliterator.this)) {
              stopped = true;
            }
          }
        });
        if (!advanced) {
          stopped = true;
        }
      }
    }

    public Spliterator<R> trySplit() {
      return null;
    }

    public long estimateSize() {
      return Long.MAX_VALUE;
    }

    public int characteristics() {
      return source.characteristics() & Spliterator.ORDERED;
    }
  }
}
