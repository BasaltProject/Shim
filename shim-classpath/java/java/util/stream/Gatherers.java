/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util.stream;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public final class Gatherers {
  private Gatherers() { }

  public static <T, R> Gatherer<T, ?, R> fold(
      final Supplier<R> initial, final BiFunction<? super R, ? super T, ? extends R> folder) {
    Objects.requireNonNull(initial);
    Objects.requireNonNull(folder);
    Supplier<Object[]> init = new Supplier<Object[]>() {
      public Object[] get() {
        return new Object[] {initial.get()};
      }
    };
    Gatherer.Integrator<Object[], T, R> integ = Gatherer.Integrator.ofGreedy(
        new Gatherer.Integrator.Greedy<Object[], T, R>() {
          @SuppressWarnings("unchecked")
          public boolean integrate(Object[] state, T element, Gatherer.Downstream<? super R> downstream) {
            state[0] = folder.apply((R) state[0], element);
            return true;
          }
        });
    BiConsumer<Object[], Gatherer.Downstream<? super R>> fin = new BiConsumer<Object[], Gatherer.Downstream<? super R>>() {
      @SuppressWarnings("unchecked")
      public void accept(Object[] state, Gatherer.Downstream<? super R> downstream) {
        downstream.push((R) state[0]);
      }
    };
    return Gatherer.ofSequential(init, integ, fin);
  }

  public static <T, R> Gatherer<T, ?, R> scan(
      final Supplier<R> initial, final BiFunction<? super R, ? super T, ? extends R> scanner) {
    Objects.requireNonNull(initial);
    Objects.requireNonNull(scanner);
    Supplier<Object[]> init = new Supplier<Object[]>() {
      public Object[] get() {
        return new Object[] {initial.get()};
      }
    };
    Gatherer.Integrator<Object[], T, R> integ = new Gatherer.Integrator<Object[], T, R>() {
      @SuppressWarnings("unchecked")
      public boolean integrate(Object[] state, T element, Gatherer.Downstream<? super R> downstream) {
        R next = scanner.apply((R) state[0], element);
        state[0] = next;
        return downstream.push(next);
      }
    };
    return Gatherer.ofSequential(init, integ);
  }

  public static <TR> Gatherer<TR, ?, List<TR>> windowFixed(final int windowSize) {
    if (windowSize < 1) {
      throw new IllegalArgumentException("'windowSize' must be greater than zero");
    }
    Supplier<ArrayList<TR>> init = new Supplier<ArrayList<TR>>() {
      public ArrayList<TR> get() {
        return new ArrayList<TR>(windowSize);
      }
    };
    Gatherer.Integrator<ArrayList<TR>, TR, List<TR>> integ = new Gatherer.Integrator<ArrayList<TR>, TR, List<TR>>() {
      public boolean integrate(ArrayList<TR> window, TR element, Gatherer.Downstream<? super List<TR>> downstream) {
        window.add(element);
        if (window.size() < windowSize) {
          return true;
        }
        List<TR> full = List.copyOf(window);
        window.clear();
        return downstream.push(full);
      }
    };
    BiConsumer<ArrayList<TR>, Gatherer.Downstream<? super List<TR>>> fin =
        new BiConsumer<ArrayList<TR>, Gatherer.Downstream<? super List<TR>>>() {
          public void accept(ArrayList<TR> window, Gatherer.Downstream<? super List<TR>> downstream) {
            if (!window.isEmpty()) {
              downstream.push(List.copyOf(window));
            }
          }
        };
    return Gatherer.ofSequential(init, integ, fin);
  }

  public static <TR> Gatherer<TR, ?, List<TR>> windowSliding(final int windowSize) {
    if (windowSize < 1) {
      throw new IllegalArgumentException("'windowSize' must be greater than zero");
    }
    Supplier<Object[]> init = new Supplier<Object[]>() {
      public Object[] get() {
        return new Object[] {new ArrayDeque<Object>(windowSize), Boolean.FALSE};
      }
    };
    Gatherer.Integrator<Object[], TR, List<TR>> integ = new Gatherer.Integrator<Object[], TR, List<TR>>() {
      @SuppressWarnings("unchecked")
      public boolean integrate(Object[] state, TR element, Gatherer.Downstream<? super List<TR>> downstream) {
        ArrayDeque<TR> window = (ArrayDeque<TR>) state[0];
        window.addLast(element);
        if (window.size() < windowSize) {
          return true;
        }
        List<TR> full = List.copyOf(window);
        window.removeFirst();
        state[1] = Boolean.TRUE;
        return downstream.push(full);
      }
    };
    BiConsumer<Object[], Gatherer.Downstream<? super List<TR>>> fin =
        new BiConsumer<Object[], Gatherer.Downstream<? super List<TR>>>() {
          @SuppressWarnings("unchecked")
          public void accept(Object[] state, Gatherer.Downstream<? super List<TR>> downstream) {
            ArrayDeque<TR> window = (ArrayDeque<TR>) state[0];
            if (state[1] == Boolean.FALSE && !window.isEmpty()) {
              downstream.push(List.copyOf(window));
            }
          }
        };
    return Gatherer.ofSequential(init, integ, fin);
  }

  public static <T, R> Gatherer<T, ?, R> mapConcurrent(
      final int maxConcurrency, final Function<? super T, ? extends R> mapper) {
    if (maxConcurrency < 1) {
      throw new IllegalArgumentException("'maxConcurrency' must be greater than 0");
    }
    Objects.requireNonNull(mapper);
    Supplier<Object[]> init = new Supplier<Object[]>() {
      public Object[] get() {
        return new Object[] {Executors.newVirtualThreadPerTaskExecutor(), new ArrayDeque<Future<R>>()};
      }
    };
    Gatherer.Integrator<Object[], T, R> integ = new Gatherer.Integrator<Object[], T, R>() {
      @SuppressWarnings("unchecked")
      public boolean integrate(Object[] state, final T element, Gatherer.Downstream<? super R> downstream) {
        ExecutorService executor = (ExecutorService) state[0];
        ArrayDeque<Future<R>> pending = (ArrayDeque<Future<R>>) state[1];
        try {
          pending.add(executor.submit(new java.util.concurrent.Callable<R>() {
            public R call() {
              return mapper.apply(element);
            }
          }));
          while (pending.size() >= maxConcurrency) {
            if (!downstream.push(await(pending.poll()))) {
              cancelAll(pending);
              return false;
            }
          }
          return true;
        } catch (RuntimeException | Error e) {
          cancelAll(pending);
          executor.shutdownNow();
          throw e;
        }
      }
    };
    BiConsumer<Object[], Gatherer.Downstream<? super R>> fin = new BiConsumer<Object[], Gatherer.Downstream<? super R>>() {
      @SuppressWarnings("unchecked")
      public void accept(Object[] state, Gatherer.Downstream<? super R> downstream) {
        ArrayDeque<Future<R>> pending = (ArrayDeque<Future<R>>) state[1];
        try {
          while (!pending.isEmpty()) {
            if (!downstream.push(await(pending.poll()))) {
              cancelAll(pending);
              break;
            }
          }
        } finally {
          ((ExecutorService) state[0]).shutdownNow();
        }
      }
    };
    return Gatherer.ofSequential(init, integ, fin);
  }

  private static <R> void cancelAll(ArrayDeque<Future<R>> pending) {
    for (Future<R> f : pending) {
      f.cancel(true);
    }
    pending.clear();
  }

  private static <R> R await(Future<R> future) {
    try {
      return future.get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    } catch (ExecutionException e) {
      Throwable cause = e.getCause();
      if (cause instanceof RuntimeException) {
        throw (RuntimeException) cause;
      }
      if (cause instanceof Error) {
        throw (Error) cause;
      }
      throw new RuntimeException(cause);
    }
  }
}
