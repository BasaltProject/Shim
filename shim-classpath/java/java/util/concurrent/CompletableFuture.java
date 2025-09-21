/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.concurrent;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class CompletableFuture<T> implements Future<T>, CompletionStage<T> {
  static final Executor asyncPool = ForkJoinPool.commonPool();

  static final class AltResult {
    final Throwable ex;

    AltResult(Throwable x) {
      this.ex = x;
    }
  }

  static final AltResult NIL = new AltResult(null);

  volatile Object result;
  private final ArrayList<Runnable> stack = new ArrayList<Runnable>();

  public CompletableFuture() { }

  CompletableFuture(Object r) {
    this.result = r;
  }

  static boolean isExceptional(Object r) {
    return r instanceof AltResult && ((AltResult) r).ex != null;
  }

  static Throwable getException(Object r) {
    return (r instanceof AltResult) ? ((AltResult) r).ex : null;
  }

  static Object encodeValue(Object t) {
    return (t == null) ? NIL : t;
  }

  static AltResult encodeThrowable(Throwable x) {
    return new AltResult((x instanceof CompletionException) ? x : new CompletionException(x));
  }

  static Object encodeThrowable(Throwable x, Object r) {
    if (!(x instanceof CompletionException)) {
      x = new CompletionException(x);
    } else if (r instanceof AltResult && x == ((AltResult) r).ex) {
      return r;
    }
    return new AltResult(x);
  }

  static Object encodeRelay(Object r) {
    Throwable x;
    if (r instanceof AltResult && (x = ((AltResult) r).ex) != null && !(x instanceof CompletionException)) {
      r = new AltResult(new CompletionException(x));
    }
    return r;
  }

  final boolean internalComplete(Object r) {
    synchronized (stack) {
      if (result != null) {
        return false;
      }
      result = r;
      stack.notifyAll();
    }
    postComplete();
    return true;
  }

  final void postComplete() {
    for (;;) {
      Runnable h;
      synchronized (stack) {
        if (stack.isEmpty()) {
          return;
        }
        h = stack.remove(stack.size() - 1);
      }
      h.run();
    }
  }

  final void addDependent(Runnable fire) {
    synchronized (stack) {
      if (result == null) {
        stack.add(fire);
        return;
      }
    }
    fire.run();
  }

  final boolean completeValue(Object t) {
    return internalComplete((t == null) ? NIL : t);
  }

  final boolean completeThrowable(Throwable x) {
    return internalComplete(encodeThrowable(x));
  }

  final boolean completeThrowable(Throwable x, Object r) {
    return internalComplete(encodeThrowable(x, r));
  }

  final boolean completeRelay(Object r) {
    return internalComplete(encodeRelay(r));
  }

  final boolean completeNull() {
    return internalComplete(NIL);
  }

  private void exec(Executor e, Runnable body) {
    if (e == null) {
      body.run();
    } else {
      try {
        e.execute(body);
      } catch (Throwable ex) {
        completeThrowable(ex);
      }
    }
  }

  static Executor screenExecutor(Executor e) {
    if (e == null) {
      throw new NullPointerException();
    }
    return e;
  }

  public Executor defaultExecutor() {
    return asyncPool;
  }

  public <U> CompletableFuture<U> newIncompleteFuture() {
    return new CompletableFuture<U>();
  }

  static final class Delayer {
    static ScheduledFuture<?> delay(Runnable command, long delay, TimeUnit unit) {
      return delayer.schedule(command, delay, unit);
    }

    static final class DaemonThreadFactory implements ThreadFactory {
      public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.setName("CompletableFutureDelayScheduler");
        return t;
      }
    }

    static final ScheduledThreadPoolExecutor delayer;

    static {
      (delayer = new ScheduledThreadPoolExecutor(1, new DaemonThreadFactory())).setRemoveOnCancelPolicy(true);
    }
  }

  public static Executor delayedExecutor(long delay, TimeUnit unit, Executor executor) {
    if (unit == null || executor == null) {
      throw new NullPointerException();
    }
    return new DelayedExecutor(delay, unit, executor);
  }

  public static Executor delayedExecutor(long delay, TimeUnit unit) {
    if (unit == null) {
      throw new NullPointerException();
    }
    return new DelayedExecutor(delay, unit, asyncPool);
  }

  static final class DelayedExecutor implements Executor {
    final long delay;
    final TimeUnit unit;
    final Executor executor;

    DelayedExecutor(long delay, TimeUnit unit, Executor executor) {
      this.delay = delay;
      this.unit = unit;
      this.executor = executor;
    }

    public void execute(final Runnable r) {
      Delayer.delay(new Runnable() {
        public void run() {
          executor.execute(r);
        }
      }, delay, unit);
    }
  }

  public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier) {
    return supplyAsync(supplier, asyncPool);
  }

  public static <U> CompletableFuture<U> supplyAsync(final Supplier<U> supplier, Executor executor) {
    if (supplier == null) {
      throw new NullPointerException();
    }
    Executor e = screenExecutor(executor);
    final CompletableFuture<U> d = new CompletableFuture<U>();
    e.execute(new Runnable() {
      public void run() {
        if (d.result == null) {
          try {
            d.completeValue(supplier.get());
          } catch (Throwable ex) {
            d.completeThrowable(ex);
          }
        }
      }
    });
    return d;
  }

  public static CompletableFuture<Void> runAsync(Runnable runnable) {
    return runAsync(runnable, asyncPool);
  }

  public static CompletableFuture<Void> runAsync(final Runnable runnable, Executor executor) {
    if (runnable == null) {
      throw new NullPointerException();
    }
    Executor e = screenExecutor(executor);
    final CompletableFuture<Void> d = new CompletableFuture<Void>();
    e.execute(new Runnable() {
      public void run() {
        if (d.result == null) {
          try {
            runnable.run();
            d.completeNull();
          } catch (Throwable ex) {
            d.completeThrowable(ex);
          }
        }
      }
    });
    return d;
  }

  public static <U> CompletableFuture<U> completedFuture(U value) {
    return new CompletableFuture<U>((value == null) ? NIL : value);
  }

  public static <U> CompletableFuture<U> failedFuture(Throwable ex) {
    if (ex == null) {
      throw new NullPointerException();
    }
    return new CompletableFuture<U>(new AltResult(ex));
  }

  public static <U> CompletionStage<U> completedStage(U value) {
    return new MinimalStage<U>((value == null) ? NIL : value);
  }

  public static <U> CompletionStage<U> failedStage(Throwable ex) {
    if (ex == null) {
      throw new NullPointerException();
    }
    return new MinimalStage<U>(new AltResult(ex));
  }

  public boolean isDone() {
    return result != null;
  }

  public boolean isCancelled() {
    Object r = result;
    return (r instanceof AltResult) && (((AltResult) r).ex instanceof CancellationException);
  }

  public boolean isCompletedExceptionally() {
    Object r = result;
    return (r instanceof AltResult) && r != NIL;
  }

  @SuppressWarnings("unchecked")
  private static <T> T reportGet(Object r) throws InterruptedException, ExecutionException {
    if (r instanceof AltResult) {
      Throwable x, cause;
      if ((x = ((AltResult) r).ex) == null) {
        return null;
      }
      if (x instanceof CancellationException) {
        throw (CancellationException) x;
      }
      if ((x instanceof CompletionException) && (cause = x.getCause()) != null) {
        x = cause;
      }
      throw new ExecutionException(x);
    }
    return (T) r;
  }

  @SuppressWarnings("unchecked")
  private static <T> T reportJoin(Object r) {
    if (r instanceof AltResult) {
      Throwable x;
      if ((x = ((AltResult) r).ex) == null) {
        return null;
      }
      if (x instanceof CancellationException) {
        throw (CancellationException) x;
      }
      if (x instanceof CompletionException) {
        throw (CompletionException) x;
      }
      throw new CompletionException(x);
    }
    return (T) r;
  }

  public T get() throws InterruptedException, ExecutionException {
    Object r;
    synchronized (stack) {
      while ((r = result) == null) {
        stack.wait();
      }
    }
    return reportGet(r);
  }

  public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    long nanos = unit.toNanos(timeout);
    long deadline = System.nanoTime() + nanos;
    Object r;
    synchronized (stack) {
      while ((r = result) == null) {
        if (nanos <= 0L) {
          throw new TimeoutException();
        }
        stack.wait(nanos / 1000000L, (int) (nanos % 1000000L));
        nanos = deadline - System.nanoTime();
      }
    }
    return reportGet(r);
  }

  public T join() {
    Object r;
    boolean interrupted = false;
    synchronized (stack) {
      while ((r = result) == null) {
        try {
          stack.wait();
        } catch (InterruptedException ie) {
          interrupted = true;
        }
      }
    }
    if (interrupted) {
      Thread.currentThread().interrupt();
    }
    return reportJoin(r);
  }

  public T getNow(T valueIfAbsent) {
    Object r;
    return ((r = result) == null) ? valueIfAbsent : CompletableFuture.<T>reportJoin(r);
  }

  @SuppressWarnings("unchecked")
  public T resultNow() {
    Object r = result;
    if (r != null) {
      if (r instanceof AltResult) {
        Throwable x = ((AltResult) r).ex;
        if (x == null) {
          return null;
        }
        if (x instanceof CancellationException) {
          throw new IllegalStateException("Task was cancelled");
        }
        throw new IllegalStateException("Task completed with exception");
      } else {
        return (T) r;
      }
    }
    throw new IllegalStateException("Task has not completed");
  }

  public Throwable exceptionNow() {
    Object r = result;
    if (r != null) {
      Throwable x;
      if (r instanceof AltResult && (x = ((AltResult) r).ex) != null) {
        if (x instanceof CancellationException) {
          throw new IllegalStateException("Task was cancelled");
        }
        if (x instanceof CompletionException) {
          Throwable cause = x.getCause();
          if (cause != null) {
            x = cause;
          }
        }
        return x;
      }
      throw new IllegalStateException("Task completed with a result");
    }
    throw new IllegalStateException("Task has not completed");
  }

  public State state() {
    Object r = result;
    if (r == null) {
      return State.RUNNING;
    }
    if (r != NIL && r instanceof AltResult) {
      return (((AltResult) r).ex instanceof CancellationException) ? State.CANCELLED : State.FAILED;
    }
    return State.SUCCESS;
  }

  public boolean complete(T value) {
    return completeValue(value);
  }

  public boolean completeExceptionally(Throwable ex) {
    if (ex == null) {
      throw new NullPointerException();
    }
    return internalComplete(new AltResult(ex));
  }

  public boolean cancel(boolean mayInterruptIfRunning) {
    boolean cancelled = (result == null) && internalComplete(new AltResult(new CancellationException()));
    return cancelled || isCancelled();
  }

  public void obtrudeValue(T value) {
    synchronized (stack) {
      result = (value == null) ? NIL : value;
      stack.notifyAll();
    }
    postComplete();
  }

  public void obtrudeException(Throwable ex) {
    if (ex == null) {
      throw new NullPointerException();
    }
    synchronized (stack) {
      result = new AltResult(ex);
      stack.notifyAll();
    }
    postComplete();
  }

  public int getNumberOfDependents() {
    synchronized (stack) {
      return stack.size();
    }
  }

  public CompletableFuture<T> completeAsync(Supplier<? extends T> supplier, Executor executor) {
    if (supplier == null || executor == null) {
      throw new NullPointerException();
    }
    final CompletableFuture<T> d = this;
    final Supplier<? extends T> f = supplier;
    executor.execute(new Runnable() {
      public void run() {
        if (d.result == null) {
          try {
            d.completeValue(f.get());
          } catch (Throwable ex) {
            d.completeThrowable(ex);
          }
        }
      }
    });
    return this;
  }

  public CompletableFuture<T> completeAsync(Supplier<? extends T> supplier) {
    return completeAsync(supplier, defaultExecutor());
  }

  public CompletableFuture<T> orTimeout(long timeout, TimeUnit unit) {
    if (unit == null) {
      throw new NullPointerException();
    }
    if (result == null) {
      final CompletableFuture<T> f = this;
      final ScheduledFuture<?> timer = Delayer.delay(new Runnable() {
        public void run() {
          if (!f.isDone()) {
            f.completeExceptionally(new TimeoutException());
          }
        }
      }, timeout, unit);
      whenComplete(new BiConsumer<T, Throwable>() {
        public void accept(T ignore, Throwable ex) {
          if (ex == null && !timer.isDone()) {
            timer.cancel(false);
          }
        }
      });
    }
    return this;
  }

  public CompletableFuture<T> completeOnTimeout(final T value, long timeout, TimeUnit unit) {
    if (unit == null) {
      throw new NullPointerException();
    }
    if (result == null) {
      final CompletableFuture<T> f = this;
      final ScheduledFuture<?> timer = Delayer.delay(new Runnable() {
        public void run() {
          f.complete(value);
        }
      }, timeout, unit);
      whenComplete(new BiConsumer<T, Throwable>() {
        public void accept(T ignore, Throwable ex) {
          if (ex == null && !timer.isDone()) {
            timer.cancel(false);
          }
        }
      });
    }
    return this;
  }

  public CompletableFuture<T> toCompletableFuture() {
    return this;
  }

  public CompletableFuture<T> copy() {
    return uniCopyStage();
  }

  private CompletableFuture<T> uniCopyStage() {
    final CompletableFuture<T> d = newIncompleteFuture();
    final CompletableFuture<T> src = this;
    addDependent(new Runnable() {
      public void run() {
        d.completeRelay(src.result);
      }
    });
    return d;
  }

  public CompletionStage<T> minimalCompletionStage() {
    final MinimalStage<T> d = new MinimalStage<T>();
    final CompletableFuture<T> src = this;
    addDependent(new Runnable() {
      public void run() {
        d.completeRelay(src.result);
      }
    });
    return d;
  }

  static final class MinimalStage<T> extends CompletableFuture<T> {
    MinimalStage() { }

    MinimalStage(Object r) {
      super(r);
    }

    @Override
    public <U> CompletableFuture<U> newIncompleteFuture() {
      return new MinimalStage<U>();
    }

    @Override
    public T get() {
      throw new UnsupportedOperationException();
    }

    @Override
    public T get(long timeout, TimeUnit unit) {
      throw new UnsupportedOperationException();
    }

    @Override
    public T getNow(T valueIfAbsent) {
      throw new UnsupportedOperationException();
    }

    @Override
    public T join() {
      throw new UnsupportedOperationException();
    }

    @Override
    public T resultNow() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Throwable exceptionNow() {
      throw new UnsupportedOperationException();
    }

    @Override
    public State state() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean complete(T value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean completeExceptionally(Throwable ex) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void obtrudeValue(T value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void obtrudeException(Throwable ex) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDone() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCancelled() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCompletedExceptionally() {
      throw new UnsupportedOperationException();
    }

    @Override
    public int getNumberOfDependents() {
      throw new UnsupportedOperationException();
    }

    @Override
    public CompletableFuture<T> completeAsync(Supplier<? extends T> supplier, Executor executor) {
      throw new UnsupportedOperationException();
    }

    @Override
    public CompletableFuture<T> completeAsync(Supplier<? extends T> supplier) {
      throw new UnsupportedOperationException();
    }

    @Override
    public CompletableFuture<T> orTimeout(long timeout, TimeUnit unit) {
      throw new UnsupportedOperationException();
    }

    @Override
    public CompletableFuture<T> completeOnTimeout(T value, long timeout, TimeUnit unit) {
      throw new UnsupportedOperationException();
    }

    @Override
    public CompletableFuture<T> toCompletableFuture() {
      Object r;
      if ((r = result) != null) {
        return new CompletableFuture<T>(encodeRelay(r));
      }
      final CompletableFuture<T> d = new CompletableFuture<T>();
      final CompletableFuture<T> src = this;
      addDependent(new Runnable() {
        public void run() {
          d.completeRelay(src.result);
        }
      });
      return d;
    }
  }

  @SuppressWarnings("unchecked")
  private static <A> A unwrap(Object r) {
    return (r instanceof AltResult) ? null : (A) r;
  }

  private <V> CompletableFuture<V> uniApplyStage(final Executor e, final Function<? super T, ? extends V> f) {
    if (f == null) {
      throw new NullPointerException();
    }
    final CompletableFuture<V> d = newIncompleteFuture();
    final CompletableFuture<T> src = this;
    addDependent(new Runnable() {
      public void run() {
        final Object r = src.result;
        if (isExceptional(r)) {
          d.completeThrowable(((AltResult) r).ex, r);
          return;
        }
        d.exec(e, new Runnable() {
          public void run() {
            try {
              d.completeValue(f.apply(CompletableFuture.<T>unwrap(r)));
            } catch (Throwable ex) {
              d.completeThrowable(ex);
            }
          }
        });
      }
    });
    return d;
  }

  private CompletableFuture<Void> uniAcceptStage(final Executor e, final Consumer<? super T> f) {
    if (f == null) {
      throw new NullPointerException();
    }
    final CompletableFuture<Void> d = newIncompleteFuture();
    final CompletableFuture<T> src = this;
    addDependent(new Runnable() {
      public void run() {
        final Object r = src.result;
        if (isExceptional(r)) {
          d.completeThrowable(((AltResult) r).ex, r);
          return;
        }
        d.exec(e, new Runnable() {
          public void run() {
            try {
              f.accept(CompletableFuture.<T>unwrap(r));
              d.completeNull();
            } catch (Throwable ex) {
              d.completeThrowable(ex);
            }
          }
        });
      }
    });
    return d;
  }

  private CompletableFuture<Void> uniRunStage(final Executor e, final Runnable f) {
    if (f == null) {
      throw new NullPointerException();
    }
    final CompletableFuture<Void> d = newIncompleteFuture();
    final CompletableFuture<T> src = this;
    addDependent(new Runnable() {
      public void run() {
        final Object r = src.result;
        if (isExceptional(r)) {
          d.completeThrowable(((AltResult) r).ex, r);
          return;
        }
        d.exec(e, new Runnable() {
          public void run() {
            try {
              f.run();
              d.completeNull();
            } catch (Throwable ex) {
              d.completeThrowable(ex);
            }
          }
        });
      }
    });
    return d;
  }

  private <V> CompletableFuture<V> uniHandleStage(final Executor e, final BiFunction<? super T, Throwable, ? extends V> f) {
    if (f == null) {
      throw new NullPointerException();
    }
    final CompletableFuture<V> d = newIncompleteFuture();
    final CompletableFuture<T> src = this;
    addDependent(new Runnable() {
      public void run() {
        final Object r = src.result;
        final Throwable x = getException(r);
        d.exec(e, new Runnable() {
          public void run() {
            try {
              d.completeValue(f.apply(CompletableFuture.<T>unwrap(r), x));
            } catch (Throwable ex) {
              d.completeThrowable(ex);
            }
          }
        });
      }
    });
    return d;
  }

  private CompletableFuture<T> uniWhenCompleteStage(final Executor e, final BiConsumer<? super T, ? super Throwable> f) {
    if (f == null) {
      throw new NullPointerException();
    }
    final CompletableFuture<T> d = newIncompleteFuture();
    final CompletableFuture<T> src = this;
    addDependent(new Runnable() {
      public void run() {
        final Object r = src.result;
        d.exec(e, new Runnable() {
          public void run() {
            Throwable x = getException(r);
            try {
              f.accept(CompletableFuture.<T>unwrap(r), x);
              if (x == null) {
                d.internalComplete(r);
                return;
              }
            } catch (Throwable ex) {
              if (x == null) {
                x = ex;
              } else if (x != ex) {
                x.addSuppressed(ex);
              }
            }
            d.completeThrowable(x, r);
          }
        });
      }
    });
    return d;
  }

  private CompletableFuture<T> uniExceptionallyStage(final Executor e, final Function<Throwable, ? extends T> f) {
    if (f == null) {
      throw new NullPointerException();
    }
    final CompletableFuture<T> d = newIncompleteFuture();
    final CompletableFuture<T> src = this;
    addDependent(new Runnable() {
      public void run() {
        final Object r = src.result;
        final Throwable x = isExceptional(r) ? ((AltResult) r).ex : null;
        if (x == null) {
          d.internalComplete(r);
          return;
        }
        d.exec(e, new Runnable() {
          public void run() {
            try {
              d.completeValue(f.apply(x));
            } catch (Throwable ex) {
              d.completeThrowable(ex);
            }
          }
        });
      }
    });
    return d;
  }

  private CompletableFuture<T> uniExceptionallyComposeStage(final Executor e,
      final Function<Throwable, ? extends CompletionStage<T>> f) {
    if (f == null) {
      throw new NullPointerException();
    }
    final CompletableFuture<T> d = newIncompleteFuture();
    final CompletableFuture<T> src = this;
    addDependent(new Runnable() {
      public void run() {
        final Object r = src.result;
        final Throwable x = isExceptional(r) ? ((AltResult) r).ex : null;
        if (x == null) {
          d.internalComplete(r);
          return;
        }
        d.exec(e, new Runnable() {
          public void run() {
            try {
              CompletableFuture<T> g = f.apply(x).toCompletableFuture();
              d.relayFrom(g);
            } catch (Throwable ex) {
              d.completeThrowable(ex);
            }
          }
        });
      }
    });
    return d;
  }

  private <V> CompletableFuture<V> uniComposeStage(final Executor e,
      final Function<? super T, ? extends CompletionStage<V>> f) {
    if (f == null) {
      throw new NullPointerException();
    }
    final CompletableFuture<V> d = newIncompleteFuture();
    final CompletableFuture<T> src = this;
    addDependent(new Runnable() {
      public void run() {
        final Object r = src.result;
        if (isExceptional(r)) {
          d.completeThrowable(((AltResult) r).ex, r);
          return;
        }
        d.exec(e, new Runnable() {
          public void run() {
            try {
              CompletableFuture<V> g = f.apply(CompletableFuture.<T>unwrap(r)).toCompletableFuture();
              d.relayFrom(g);
            } catch (Throwable ex) {
              d.completeThrowable(ex);
            }
          }
        });
      }
    });
    return d;
  }

  private void relayFrom(final CompletableFuture<?> g) {
    Object s = g.result;
    if (s != null) {
      completeRelay(s);
    } else {
      final CompletableFuture<?> target = this;
      g.addDependent(new Runnable() {
        public void run() {
          target.completeRelay(g.result);
        }
      });
    }
  }

  private interface BiBody {
    void apply(Object a, Object b) throws Throwable;
  }

  private <A, B> void biStage(final CompletableFuture<A> a, final CompletableFuture<B> b, final Executor e,
                              final BiBody body) {
    final CompletableFuture<?> d = this;
    final AtomicBoolean claimed = new AtomicBoolean();
    Runnable fire = new Runnable() {
      public void run() {
        final Object r = a.result;
        final Object s = b.result;
        if (r == null || s == null || !claimed.compareAndSet(false, true)) {
          return;
        }
        if (isExceptional(r)) {
          d.completeThrowable(((AltResult) r).ex, r);
          return;
        }
        if (isExceptional(s)) {
          d.completeThrowable(((AltResult) s).ex, s);
          return;
        }
        d.exec(e, new Runnable() {
          public void run() {
            try {
              body.apply(CompletableFuture.unwrap(r), CompletableFuture.unwrap(s));
            } catch (Throwable ex) {
              d.completeThrowable(ex);
            }
          }
        });
      }
    };
    a.addDependent(fire);
    if (b != a) {
      b.addDependent(fire);
    }
  }

  private interface OrBody {
    void apply(Object a) throws Throwable;
  }

  private <A> void orStage(final CompletableFuture<?> a, final CompletableFuture<?> b, final Executor e,
                           final OrBody body) {
    final CompletableFuture<?> d = this;
    final AtomicBoolean claimed = new AtomicBoolean();
    Runnable fire = new Runnable() {
      public void run() {
        Object r = a.result;
        if (r == null) {
          r = b.result;
        }
        if (r == null || !claimed.compareAndSet(false, true)) {
          return;
        }
        if (isExceptional(r)) {
          d.completeThrowable(((AltResult) r).ex, r);
          return;
        }
        final Object value = CompletableFuture.unwrap(r);
        d.exec(e, new Runnable() {
          public void run() {
            try {
              body.apply(value);
            } catch (Throwable ex) {
              d.completeThrowable(ex);
            }
          }
        });
      }
    };
    a.addDependent(fire);
    b.addDependent(fire);
  }

  private <U> CompletableFuture<U> requireFuture(CompletionStage<U> o) {
    CompletableFuture<U> b;
    if (o == null || (b = o.toCompletableFuture()) == null) {
      throw new NullPointerException();
    }
    return b;
  }

  @SuppressWarnings("unchecked")
  private <U, V> CompletableFuture<V> biApplyStage(Executor e, CompletionStage<U> o,
      final BiFunction<? super T, ? super U, ? extends V> f) {
    if (f == null) {
      throw new NullPointerException();
    }
    CompletableFuture<U> b = requireFuture(o);
    final CompletableFuture<V> d = newIncompleteFuture();
    d.biStage(this, b, e, new BiBody() {
      public void apply(Object a, Object bb) {
        d.completeValue(f.apply((T) a, (U) bb));
      }
    });
    return d;
  }

  @SuppressWarnings("unchecked")
  private <U> CompletableFuture<Void> biAcceptStage(Executor e, CompletionStage<U> o,
      final BiConsumer<? super T, ? super U> f) {
    if (f == null) {
      throw new NullPointerException();
    }
    CompletableFuture<U> b = requireFuture(o);
    final CompletableFuture<Void> d = newIncompleteFuture();
    d.biStage(this, b, e, new BiBody() {
      public void apply(Object a, Object bb) {
        f.accept((T) a, (U) bb);
        d.completeNull();
      }
    });
    return d;
  }

  private CompletableFuture<Void> biRunStage(Executor e, CompletionStage<?> o, final Runnable f) {
    if (f == null) {
      throw new NullPointerException();
    }
    CompletableFuture<?> b = requireFuture(o);
    final CompletableFuture<Void> d = newIncompleteFuture();
    d.biStage(this, b, e, new BiBody() {
      public void apply(Object a, Object bb) {
        f.run();
        d.completeNull();
      }
    });
    return d;
  }

  @SuppressWarnings("unchecked")
  private <U extends T, V> CompletableFuture<V> orApplyStage(Executor e, CompletionStage<U> o,
      final Function<? super T, ? extends V> f) {
    if (f == null) {
      throw new NullPointerException();
    }
    CompletableFuture<U> b = requireFuture(o);
    final CompletableFuture<V> d = newIncompleteFuture();
    d.orStage(this, b, e, new OrBody() {
      public void apply(Object a) {
        d.completeValue(f.apply((T) a));
      }
    });
    return d;
  }

  @SuppressWarnings("unchecked")
  private <U extends T> CompletableFuture<Void> orAcceptStage(Executor e, CompletionStage<U> o,
      final Consumer<? super T> f) {
    if (f == null) {
      throw new NullPointerException();
    }
    CompletableFuture<U> b = requireFuture(o);
    final CompletableFuture<Void> d = newIncompleteFuture();
    d.orStage(this, b, e, new OrBody() {
      public void apply(Object a) {
        f.accept((T) a);
        d.completeNull();
      }
    });
    return d;
  }

  private CompletableFuture<Void> orRunStage(Executor e, CompletionStage<?> o, final Runnable f) {
    if (f == null) {
      throw new NullPointerException();
    }
    CompletableFuture<?> b = requireFuture(o);
    final CompletableFuture<Void> d = newIncompleteFuture();
    d.orStage(this, b, e, new OrBody() {
      public void apply(Object a) {
        f.run();
        d.completeNull();
      }
    });
    return d;
  }

  public <U> CompletableFuture<U> thenApply(Function<? super T, ? extends U> fn) {
    return uniApplyStage(null, fn);
  }

  public <U> CompletableFuture<U> thenApplyAsync(Function<? super T, ? extends U> fn) {
    return uniApplyStage(defaultExecutor(), fn);
  }

  public <U> CompletableFuture<U> thenApplyAsync(Function<? super T, ? extends U> fn, Executor executor) {
    return uniApplyStage(screenExecutor(executor), fn);
  }

  public CompletableFuture<Void> thenAccept(Consumer<? super T> action) {
    return uniAcceptStage(null, action);
  }

  public CompletableFuture<Void> thenAcceptAsync(Consumer<? super T> action) {
    return uniAcceptStage(defaultExecutor(), action);
  }

  public CompletableFuture<Void> thenAcceptAsync(Consumer<? super T> action, Executor executor) {
    return uniAcceptStage(screenExecutor(executor), action);
  }

  public CompletableFuture<Void> thenRun(Runnable action) {
    return uniRunStage(null, action);
  }

  public CompletableFuture<Void> thenRunAsync(Runnable action) {
    return uniRunStage(defaultExecutor(), action);
  }

  public CompletableFuture<Void> thenRunAsync(Runnable action, Executor executor) {
    return uniRunStage(screenExecutor(executor), action);
  }

  public <U, V> CompletableFuture<V> thenCombine(CompletionStage<? extends U> other,
                                                  BiFunction<? super T, ? super U, ? extends V> fn) {
    return biApplyStage(null, other, fn);
  }

  public <U, V> CompletableFuture<V> thenCombineAsync(CompletionStage<? extends U> other,
                                                       BiFunction<? super T, ? super U, ? extends V> fn) {
    return biApplyStage(defaultExecutor(), other, fn);
  }

  public <U, V> CompletableFuture<V> thenCombineAsync(CompletionStage<? extends U> other,
                                                       BiFunction<? super T, ? super U, ? extends V> fn,
                                                       Executor executor) {
    return biApplyStage(screenExecutor(executor), other, fn);
  }

  public <U> CompletableFuture<Void> thenAcceptBoth(CompletionStage<? extends U> other,
                                                     BiConsumer<? super T, ? super U> action) {
    return biAcceptStage(null, other, action);
  }

  public <U> CompletableFuture<Void> thenAcceptBothAsync(CompletionStage<? extends U> other,
                                                          BiConsumer<? super T, ? super U> action) {
    return biAcceptStage(defaultExecutor(), other, action);
  }

  public <U> CompletableFuture<Void> thenAcceptBothAsync(CompletionStage<? extends U> other,
                                                          BiConsumer<? super T, ? super U> action,
                                                          Executor executor) {
    return biAcceptStage(screenExecutor(executor), other, action);
  }

  public CompletableFuture<Void> runAfterBoth(CompletionStage<?> other, Runnable action) {
    return biRunStage(null, other, action);
  }

  public CompletableFuture<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action) {
    return biRunStage(defaultExecutor(), other, action);
  }

  public CompletableFuture<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action, Executor executor) {
    return biRunStage(screenExecutor(executor), other, action);
  }

  public <U> CompletableFuture<U> applyToEither(CompletionStage<? extends T> other, Function<? super T, U> fn) {
    return orApplyStage(null, other, fn);
  }

  public <U> CompletableFuture<U> applyToEitherAsync(CompletionStage<? extends T> other, Function<? super T, U> fn) {
    return orApplyStage(defaultExecutor(), other, fn);
  }

  public <U> CompletableFuture<U> applyToEitherAsync(CompletionStage<? extends T> other, Function<? super T, U> fn,
                                                      Executor executor) {
    return orApplyStage(screenExecutor(executor), other, fn);
  }

  public CompletableFuture<Void> acceptEither(CompletionStage<? extends T> other, Consumer<? super T> action) {
    return orAcceptStage(null, other, action);
  }

  public CompletableFuture<Void> acceptEitherAsync(CompletionStage<? extends T> other, Consumer<? super T> action) {
    return orAcceptStage(defaultExecutor(), other, action);
  }

  public CompletableFuture<Void> acceptEitherAsync(CompletionStage<? extends T> other, Consumer<? super T> action,
                                                    Executor executor) {
    return orAcceptStage(screenExecutor(executor), other, action);
  }

  public CompletableFuture<Void> runAfterEither(CompletionStage<?> other, Runnable action) {
    return orRunStage(null, other, action);
  }

  public CompletableFuture<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action) {
    return orRunStage(defaultExecutor(), other, action);
  }

  public CompletableFuture<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action, Executor executor) {
    return orRunStage(screenExecutor(executor), other, action);
  }

  public <U> CompletableFuture<U> thenCompose(Function<? super T, ? extends CompletionStage<U>> fn) {
    return uniComposeStage(null, fn);
  }

  public <U> CompletableFuture<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn) {
    return uniComposeStage(defaultExecutor(), fn);
  }

  public <U> CompletableFuture<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn,
                                                    Executor executor) {
    return uniComposeStage(screenExecutor(executor), fn);
  }

  public <U> CompletableFuture<U> handle(BiFunction<? super T, Throwable, ? extends U> fn) {
    return uniHandleStage(null, fn);
  }

  public <U> CompletableFuture<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn) {
    return uniHandleStage(defaultExecutor(), fn);
  }

  public <U> CompletableFuture<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn, Executor executor) {
    return uniHandleStage(screenExecutor(executor), fn);
  }

  public CompletableFuture<T> whenComplete(BiConsumer<? super T, ? super Throwable> action) {
    return uniWhenCompleteStage(null, action);
  }

  public CompletableFuture<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action) {
    return uniWhenCompleteStage(defaultExecutor(), action);
  }

  public CompletableFuture<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action, Executor executor) {
    return uniWhenCompleteStage(screenExecutor(executor), action);
  }

  public CompletableFuture<T> exceptionally(Function<Throwable, ? extends T> fn) {
    return uniExceptionallyStage(null, fn);
  }

  public CompletableFuture<T> exceptionallyAsync(Function<Throwable, ? extends T> fn) {
    return uniExceptionallyStage(defaultExecutor(), fn);
  }

  public CompletableFuture<T> exceptionallyAsync(Function<Throwable, ? extends T> fn, Executor executor) {
    return uniExceptionallyStage(screenExecutor(executor), fn);
  }

  public CompletableFuture<T> exceptionallyCompose(Function<Throwable, ? extends CompletionStage<T>> fn) {
    return uniExceptionallyComposeStage(null, fn);
  }

  public CompletableFuture<T> exceptionallyComposeAsync(Function<Throwable, ? extends CompletionStage<T>> fn) {
    return uniExceptionallyComposeStage(defaultExecutor(), fn);
  }

  public CompletableFuture<T> exceptionallyComposeAsync(Function<Throwable, ? extends CompletionStage<T>> fn,
                                                         Executor executor) {
    return uniExceptionallyComposeStage(screenExecutor(executor), fn);
  }

  public static CompletableFuture<Void> allOf(CompletableFuture<?>... cfs) {
    return andTree(cfs, 0, cfs.length - 1);
  }

  private static CompletableFuture<Void> andTree(CompletableFuture<?>[] cfs, int lo, int hi) {
    CompletableFuture<Void> d = new CompletableFuture<Void>();
    if (lo > hi) {
      d.result = NIL;
    } else {
      CompletableFuture<?> a, b;
      Object r, s, z;
      Throwable x;
      int mid = (lo + hi) >>> 1;
      if ((a = (lo == mid ? cfs[lo] : andTree(cfs, lo, mid))) == null
          || (b = (lo == hi ? a : (hi == mid + 1) ? cfs[hi] : andTree(cfs, mid + 1, hi))) == null) {
        throw new NullPointerException();
      }
      d.biStage(a, b, null, new BiBody() {
        public void apply(Object x, Object y) { }
      });
      final CompletableFuture<Void> target = d;
      final CompletableFuture<?> fa = a;
      final CompletableFuture<?> fb = b;
      d.stack.clear();
      d.result = null;
      Runnable fire = new Runnable() {
        public void run() {
          Object r = fa.result;
          Object s = fb.result;
          if (r == null || s == null) {
            return;
          }
          Throwable x;
          if (r instanceof AltResult && (x = ((AltResult) r).ex) != null) {
            target.completeThrowable(x, r);
          } else if (s instanceof AltResult && (x = ((AltResult) s).ex) != null) {
            target.completeThrowable(x, s);
          } else {
            target.completeNull();
          }
        }
      };
      a.addDependent(fire);
      if (b != a) {
        b.addDependent(fire);
      }
    }
    return d;
  }

  public static CompletableFuture<Object> anyOf(CompletableFuture<?>... cfs) {
    int n;
    Object r;
    if ((n = cfs.length) <= 1) {
      return (n == 0) ? new CompletableFuture<Object>() : uniCopyAny(cfs[0]);
    }
    for (CompletableFuture<?> cf : cfs) {
      if ((r = cf.result) != null) {
        return new CompletableFuture<Object>(encodeRelay(r));
      }
    }
    final CompletableFuture<Object> d = new CompletableFuture<Object>();
    Runnable relay;
    for (final CompletableFuture<?> cf : cfs) {
      relay = new Runnable() {
        public void run() {
          Object s = cf.result;
          if (s != null) {
            d.completeRelay(s);
          }
        }
      };
      cf.addDependent(relay);
    }
    return d;
  }

  private static CompletableFuture<Object> uniCopyAny(final CompletableFuture<?> src) {
    final CompletableFuture<Object> d = new CompletableFuture<Object>();
    src.addDependent(new Runnable() {
      public void run() {
        d.completeRelay(src.result);
      }
    });
    return d;
  }

  public String toString() {
    Object r = result;
    int count = getNumberOfDependentsForString();
    return super.toString()
        + ((r == null) ? ((count == 0) ? "[Not completed]" : "[Not completed, " + count + " dependents]")
                       : (((r instanceof AltResult) && ((AltResult) r).ex != null)
                              ? "[Completed exceptionally: " + ((AltResult) r).ex + "]"
                              : "[Completed normally]"));
  }

  private int getNumberOfDependentsForString() {
    synchronized (stack) {
      return stack.size();
    }
  }
}
