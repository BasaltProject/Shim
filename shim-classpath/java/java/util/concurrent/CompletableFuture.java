/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class CompletableFuture<T> implements Future<T>, CompletionStage<T> {
  static final Executor asyncPool = ForkJoinPool.commonPool();

  static final class AltResult {
    final Throwable ex;

    AltResult(Throwable ex) {
      this.ex = ex;
    }
  }

  static final AltResult NIL = new AltResult(null);

  volatile Object result;
  private final List<Runnable> dependents = new ArrayList<Runnable>();

  public CompletableFuture() {
  }

  static boolean isExceptional(Object r) {
    return (r instanceof AltResult) && (((AltResult) r).ex != null);
  }

  static Throwable getException(Object r) {
    return (r instanceof AltResult) ? ((AltResult) r).ex : null;
  }

  @SuppressWarnings("unchecked")
  static <U> U getNormalValue(Object r) {
    return (r == NIL || r instanceof AltResult) ? null : (U) r;
  }

  static final class Delayer {
    static void delay(Runnable command, long delay, TimeUnit unit) {
      long millis = unit.toMillis(delay);
      Thread t = new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            Thread.sleep(millis);
            command.run();
          } catch (InterruptedException e) {
          }
        }
      });
      t.setDaemon(true);
      t.start();
    }
  }

  boolean internalComplete(Object r) {
    List<Runnable> toRun = null;
    synchronized (this) {
      if (result != null) {
        return false;
      }
      result = (r == null) ? NIL : r;
      toRun = new ArrayList<Runnable>(dependents);
      dependents.clear();
      notifyAll();
    }
    for (Runnable task : toRun) {
      try {
        task.run();
      } catch (Throwable ignore) {
      }
    }
    return true;
  }

  private void addDependent(Runnable task) {
    boolean runNow = false;
    synchronized (this) {
      if (result != null) {
        runNow = true;
      } else {
        dependents.add(task);
      }
    }
    if (runNow) {
      task.run();
    }
  }

  public boolean complete(T value) {
    return internalComplete(value);
  }

  public boolean completeExceptionally(Throwable ex) {
    if (ex == null) throw new NullPointerException();
    return internalComplete(new AltResult(ex));
  }

  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    boolean cancelled = internalComplete(new AltResult(new CancellationException()));
    return cancelled || isCancelled();
  }

  @Override
  public boolean isCancelled() {
    Object r = result;
    return (r instanceof AltResult) && (((AltResult) r).ex instanceof CancellationException);
  }

  @Override
  public boolean isDone() {
    return result != null;
  }

  public boolean isCompletedExceptionally() {
    return isExceptional(result);
  }

  public void obtrudeValue(T value) {
    synchronized (this) {
      result = (value == null) ? NIL : value;
      notifyAll();
    }
  }

  public void obtrudeException(Throwable ex) {
    if (ex == null) throw new NullPointerException();
    synchronized (this) {
      result = new AltResult(ex);
      notifyAll();
    }
  }

  public int getNumberOfDependents() {
    synchronized (this) {
      return dependents.size();
    }
  }

  @SuppressWarnings("unchecked")
  private T decodeValue(Object r) throws ExecutionException {
    if (r instanceof AltResult) {
      Throwable ex = ((AltResult) r).ex;
      if (ex == null) {
        return null;
      }
      if (ex instanceof CancellationException) {
        throw (CancellationException) ex;
      }
      throw new ExecutionException(ex);
    }
    return (T) r;
  }

  @SuppressWarnings("unchecked")
  private T decodeValueJoin(Object r) {
    if (r instanceof AltResult) {
      Throwable ex = ((AltResult) r).ex;
      if (ex == null) {
        return null;
      }
      if (ex instanceof CancellationException) {
        throw (CancellationException) ex;
      }
      if (ex instanceof CompletionException) {
        throw (CompletionException) ex;
      }
      throw new CompletionException(ex);
    }
    return (T) r;
  }

  @Override
  public T get() throws InterruptedException, ExecutionException {
    Object r = result;
    if (r == null) {
      synchronized (this) {
        while ((r = result) == null) {
          wait();
        }
      }
    }
    return decodeValue(r);
  }

  @Override
  public T get(long timeout, TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException {
    long deadline = System.currentTimeMillis() + unit.toMillis(timeout);
    Object r = result;
    if (r == null) {
      synchronized (this) {
        while ((r = result) == null) {
          long remaining = deadline - System.currentTimeMillis();
          if (remaining <= 0) {
            throw new TimeoutException();
          }
          wait(remaining);
        }
      }
    }
    return decodeValue(r);
  }

  public T join() {
    Object r = result;
    if (r == null) {
      synchronized (this) {
        while ((r = result) == null) {
          try {
            wait();
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        }
      }
    }
    return decodeValueJoin(r);
  }

  public T getNow(T valueIfAbsent) {
    Object r = result;
    if (r == null) {
      return valueIfAbsent;
    }
    return decodeValueJoin(r);
  }

  public Executor defaultExecutor() {
    return asyncPool;
  }

  public <U> CompletableFuture<U> newIncompleteFuture() {
    return new CompletableFuture<U>();
  }

  public CompletableFuture<T> copy() {
    CompletableFuture<T> dst = newIncompleteFuture();
    addDependent(new Runnable() {
      @Override
      public void run() {
        dst.internalComplete(result);
      }
    });
    return dst;
  }

  public static <U> CompletableFuture<U> completedFuture(U value) {
    CompletableFuture<U> f = new CompletableFuture<U>();
    f.complete(value);
    return f;
  }

  public static <U> CompletableFuture<U> failedFuture(Throwable ex) {
    if (ex == null) throw new NullPointerException();
    CompletableFuture<U> f = new CompletableFuture<U>();
    f.completeExceptionally(ex);
    return f;
  }

  public static <U> CompletionStage<U> completedStage(U value) {
    return completedFuture(value);
  }

  public static <U> CompletionStage<U> failedStage(Throwable ex) {
    return failedFuture(ex);
  }

  public static CompletableFuture<Void> runAsync(Runnable runnable) {
    return runAsync(runnable, asyncPool);
  }

  public static CompletableFuture<Void> runAsync(Runnable runnable, Executor executor) {
    if (runnable == null || executor == null) throw new NullPointerException();
    final CompletableFuture<Void> f = new CompletableFuture<Void>();
    executor.execute(new Runnable() {
      @Override
      public void run() {
        try {
          runnable.run();
          f.complete(null);
        } catch (Throwable ex) {
          f.completeExceptionally(ex);
        }
      }
    });
    return f;
  }

  public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier) {
    return supplyAsync(supplier, asyncPool);
  }

  public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier, Executor executor) {
    if (supplier == null || executor == null) throw new NullPointerException();
    final CompletableFuture<U> f = new CompletableFuture<U>();
    executor.execute(new Runnable() {
      @Override
      public void run() {
        try {
          f.complete(supplier.get());
        } catch (Throwable ex) {
          f.completeExceptionally(ex);
        }
      }
    });
    return f;
  }

  public CompletableFuture<T> completeAsync(Supplier<? extends T> supplier) {
    return completeAsync(supplier, defaultExecutor());
  }

  public CompletableFuture<T> completeAsync(Supplier<? extends T> supplier, Executor executor) {
    if (supplier == null || executor == null) throw new NullPointerException();
    executor.execute(new Runnable() {
      @Override
      public void run() {
        try {
          complete(supplier.get());
        } catch (Throwable ex) {
          completeExceptionally(ex);
        }
      }
    });
    return this;
  }

  public CompletableFuture<T> orTimeout(long timeout, TimeUnit unit) {
    if (unit == null) throw new NullPointerException();
    Delayer.delay(new Runnable() {
      @Override
      public void run() {
        if (!isDone()) {
          completeExceptionally(new TimeoutException());
        }
      }
    }, timeout, unit);
    return this;
  }

  public CompletableFuture<T> completeOnTimeout(T value, long timeout, TimeUnit unit) {
    if (unit == null) throw new NullPointerException();
    Delayer.delay(new Runnable() {
      @Override
      public void run() {
        if (!isDone()) {
          complete(value);
        }
      }
    }, timeout, unit);
    return this;
  }

  @Override
  public <U> CompletableFuture<U> thenApply(Function<? super T, ? extends U> fn) {
    return thenApplyAsync(fn, null);
  }

  @Override
  public <U> CompletableFuture<U> thenApplyAsync(Function<? super T, ? extends U> fn) {
    return thenApplyAsync(fn, defaultExecutor());
  }

  @Override
  public <U> CompletableFuture<U> thenApplyAsync(
      Function<? super T, ? extends U> fn, Executor executor) {
    if (fn == null) throw new NullPointerException();
    final CompletableFuture<U> dst = newIncompleteFuture();
    addDependent(new Runnable() {
      @Override
      public void run() {
        Runnable action = new Runnable() {
          @Override
          public void run() {
            Object r = result;
            if (isExceptional(r)) {
              dst.internalComplete(r);
            } else {
              try {
                dst.complete(fn.apply(getNormalValue(r)));
              } catch (Throwable ex) {
                dst.completeExceptionally(ex);
              }
            }
          }
        };
        if (executor != null) {
          executor.execute(action);
        } else {
          action.run();
        }
      }
    });
    return dst;
  }

  @Override
  public CompletableFuture<Void> thenAccept(Consumer<? super T> action) {
    return thenAcceptAsync(action, null);
  }

  @Override
  public CompletableFuture<Void> thenAcceptAsync(Consumer<? super T> action) {
    return thenAcceptAsync(action, defaultExecutor());
  }

  @Override
  public CompletableFuture<Void> thenAcceptAsync(
      Consumer<? super T> action, Executor executor) {
    if (action == null) throw new NullPointerException();
    final CompletableFuture<Void> dst = newIncompleteFuture();
    addDependent(new Runnable() {
      @Override
      public void run() {
        Runnable rTask = new Runnable() {
          @Override
          public void run() {
            Object r = result;
            if (isExceptional(r)) {
              dst.internalComplete(r);
            } else {
              try {
                action.accept(getNormalValue(r));
                dst.complete(null);
              } catch (Throwable ex) {
                dst.completeExceptionally(ex);
              }
            }
          }
        };
        if (executor != null) {
          executor.execute(rTask);
        } else {
          rTask.run();
        }
      }
    });
    return dst;
  }

  @Override
  public CompletableFuture<Void> thenRun(Runnable action) {
    return thenRunAsync(action, null);
  }

  @Override
  public CompletableFuture<Void> thenRunAsync(Runnable action) {
    return thenRunAsync(action, defaultExecutor());
  }

  @Override
  public CompletableFuture<Void> thenRunAsync(Runnable action, Executor executor) {
    if (action == null) throw new NullPointerException();
    final CompletableFuture<Void> dst = newIncompleteFuture();
    addDependent(new Runnable() {
      @Override
      public void run() {
        Runnable rTask = new Runnable() {
          @Override
          public void run() {
            Object r = result;
            if (isExceptional(r)) {
              dst.internalComplete(r);
            } else {
              try {
                action.run();
                dst.complete(null);
              } catch (Throwable ex) {
                dst.completeExceptionally(ex);
              }
            }
          }
        };
        if (executor != null) {
          executor.execute(rTask);
        } else {
          rTask.run();
        }
      }
    });
    return dst;
  }

  @Override
  public <U, V> CompletableFuture<V> thenCombine(
      CompletionStage<? extends U> other,
      BiFunction<? super T, ? super U, ? extends V> fn) {
    return thenCombineAsync(other, fn, null);
  }

  @Override
  public <U, V> CompletableFuture<V> thenCombineAsync(
      CompletionStage<? extends U> other,
      BiFunction<? super T, ? super U, ? extends V> fn) {
    return thenCombineAsync(other, fn, defaultExecutor());
  }

  @Override
  public <U, V> CompletableFuture<V> thenCombineAsync(
      CompletionStage<? extends U> other,
      BiFunction<? super T, ? super U, ? extends V> fn,
      Executor executor) {
    if (other == null || fn == null) throw new NullPointerException();
    final CompletableFuture<V> dst = newIncompleteFuture();
    final CompletableFuture<? extends U> o = other.toCompletableFuture();
    Runnable check = new Runnable() {
      @Override
      public void run() {
        if (isDone() && o.isDone()) {
          Runnable rTask = new Runnable() {
            @Override
            public void run() {
              Object r1 = result;
              Object r2 = o.result;
              if (isExceptional(r1)) {
                dst.internalComplete(r1);
              } else if (isExceptional(r2)) {
                dst.internalComplete(r2);
              } else {
                try {
                  dst.complete(fn.apply(getNormalValue(r1), getNormalValue(r2)));
                } catch (Throwable ex) {
                  dst.completeExceptionally(ex);
                }
              }
            }
          };
          if (executor != null) {
            executor.execute(rTask);
          } else {
            rTask.run();
          }
        }
      }
    };
    addDependent(check);
    o.addDependent(check);
    return dst;
  }

  @Override
  public <U> CompletableFuture<Void> thenAcceptBoth(
      CompletionStage<? extends U> other,
      BiConsumer<? super T, ? super U> action) {
    return thenAcceptBothAsync(other, action, null);
  }

  @Override
  public <U> CompletableFuture<Void> thenAcceptBothAsync(
      CompletionStage<? extends U> other,
      BiConsumer<? super T, ? super U> action) {
    return thenAcceptBothAsync(other, action, defaultExecutor());
  }

  @Override
  public <U> CompletableFuture<Void> thenAcceptBothAsync(
      CompletionStage<? extends U> other,
      BiConsumer<? super T, ? super U> action,
      Executor executor) {
    return thenCombineAsync(other, new BiFunction<T, U, Void>() {
      @Override
      public Void apply(T t, U u) {
        action.accept(t, u);
        return null;
      }
    }, executor);
  }

  @Override
  public CompletableFuture<Void> runAfterBoth(
      CompletionStage<?> other, Runnable action) {
    return runAfterBothAsync(other, action, null);
  }

  @Override
  public CompletableFuture<Void> runAfterBothAsync(
      CompletionStage<?> other, Runnable action) {
    return runAfterBothAsync(other, action, defaultExecutor());
  }

  @Override
  public CompletableFuture<Void> runAfterBothAsync(
      CompletionStage<?> other, Runnable action, Executor executor) {
    if (other == null || action == null) throw new NullPointerException();
    return thenCombineAsync(other, new BiFunction<T, Object, Void>() {
      @Override
      public Void apply(T t, Object u) {
        action.run();
        return null;
      }
    }, executor);
  }

  @Override
  public <U> CompletableFuture<U> applyToEither(
      CompletionStage<? extends T> other, Function<? super T, U> fn) {
    return applyToEitherAsync(other, fn, null);
  }

  @Override
  public <U> CompletableFuture<U> applyToEitherAsync(
      CompletionStage<? extends T> other, Function<? super T, U> fn) {
    return applyToEitherAsync(other, fn, defaultExecutor());
  }

  @Override
  public <U> CompletableFuture<U> applyToEitherAsync(
      CompletionStage<? extends T> other,
      Function<? super T, U> fn,
      Executor executor) {
    if (other == null || fn == null) throw new NullPointerException();
    final CompletableFuture<U> dst = newIncompleteFuture();
    final CompletableFuture<? extends T> o = other.toCompletableFuture();
    Runnable check = new Runnable() {
      @Override
      public void run() {
        if (!dst.isDone()) {
          Object r = isDone() ? result : (o.isDone() ? o.result : null);
          if (r != null) {
            Runnable rTask = new Runnable() {
              @Override
              public void run() {
                if (isExceptional(r)) {
                  dst.internalComplete(r);
                } else {
                  try {
                    dst.complete(fn.apply(getNormalValue(r)));
                  } catch (Throwable ex) {
                    dst.completeExceptionally(ex);
                  }
                }
              }
            };
            if (executor != null) {
              executor.execute(rTask);
            } else {
              rTask.run();
            }
          }
        }
      }
    };
    addDependent(check);
    o.addDependent(check);
    return dst;
  }

  @Override
  public CompletableFuture<Void> acceptEither(
      CompletionStage<? extends T> other, Consumer<? super T> action) {
    return acceptEitherAsync(other, action, null);
  }

  @Override
  public CompletableFuture<Void> acceptEitherAsync(
      CompletionStage<? extends T> other, Consumer<? super T> action) {
    return acceptEitherAsync(other, action, defaultExecutor());
  }

  @Override
  public CompletableFuture<Void> acceptEitherAsync(
      CompletionStage<? extends T> other,
      Consumer<? super T> action,
      Executor executor) {
    if (action == null) throw new NullPointerException();
    return applyToEitherAsync(other, new Function<T, Void>() {
      @Override
      public Void apply(T t) {
        action.accept(t);
        return null;
      }
    }, executor);
  }

  @Override
  public CompletableFuture<Void> runAfterEither(
      CompletionStage<?> other, Runnable action) {
    return runAfterEitherAsync(other, action, null);
  }

  @Override
  public CompletableFuture<Void> runAfterEitherAsync(
      CompletionStage<?> other, Runnable action) {
    return runAfterEitherAsync(other, action, defaultExecutor());
  }

  @Override
  public CompletableFuture<Void> runAfterEitherAsync(
      CompletionStage<?> other, Runnable action, Executor executor) {
    if (other == null || action == null) throw new NullPointerException();
    final CompletableFuture<Void> dst = newIncompleteFuture();
    final CompletableFuture<?> o = other.toCompletableFuture();
    Runnable check = new Runnable() {
      @Override
      public void run() {
        if (!dst.isDone()) {
          Object r = isDone() ? result : (o.isDone() ? o.result : null);
          if (r != null) {
            Runnable rTask = new Runnable() {
              @Override
              public void run() {
                if (isExceptional(r)) {
                  dst.internalComplete(r);
                } else {
                  try {
                    action.run();
                    dst.complete(null);
                  } catch (Throwable ex) {
                    dst.completeExceptionally(ex);
                  }
                }
              }
            };
            if (executor != null) {
              executor.execute(rTask);
            } else {
              rTask.run();
            }
          }
        }
      }
    };
    addDependent(check);
    o.addDependent(check);
    return dst;
  }

  @Override
  public <U> CompletableFuture<U> thenCompose(
      Function<? super T, ? extends CompletionStage<U>> fn) {
    return thenComposeAsync(fn, null);
  }

  @Override
  public <U> CompletableFuture<U> thenComposeAsync(
      Function<? super T, ? extends CompletionStage<U>> fn) {
    return thenComposeAsync(fn, defaultExecutor());
  }

  @Override
  public <U> CompletableFuture<U> thenComposeAsync(
      Function<? super T, ? extends CompletionStage<U>> fn, Executor executor) {
    if (fn == null) throw new NullPointerException();
    final CompletableFuture<U> dst = newIncompleteFuture();
    addDependent(new Runnable() {
      @Override
      public void run() {
        Runnable rTask = new Runnable() {
          @Override
          public void run() {
            Object r = result;
            if (isExceptional(r)) {
              dst.internalComplete(r);
            } else {
              try {
                CompletionStage<U> nextStage = fn.apply(getNormalValue(r));
                if (nextStage == null) {
                  throw new NullPointerException();
                }
                CompletableFuture<U> nextFuture = nextStage.toCompletableFuture();
                nextFuture.addDependent(new Runnable() {
                  @Override
                  public void run() {
                    dst.internalComplete(nextFuture.result);
                  }
                });
              } catch (Throwable ex) {
                dst.completeExceptionally(ex);
              }
            }
          }
        };
        if (executor != null) {
          executor.execute(rTask);
        } else {
          rTask.run();
        }
      }
    });
    return dst;
  }

  @Override
  public <U> CompletableFuture<U> handle(
      BiFunction<? super T, Throwable, ? extends U> fn) {
    return handleAsync(fn, null);
  }

  @Override
  public <U> CompletableFuture<U> handleAsync(
      BiFunction<? super T, Throwable, ? extends U> fn) {
    return handleAsync(fn, defaultExecutor());
  }

  @Override
  public <U> CompletableFuture<U> handleAsync(
      BiFunction<? super T, Throwable, ? extends U> fn, Executor executor) {
    if (fn == null) throw new NullPointerException();
    final CompletableFuture<U> dst = newIncompleteFuture();
    addDependent(new Runnable() {
      @Override
      public void run() {
        Runnable rTask = new Runnable() {
          @Override
          public void run() {
            Object r = result;
            T val = getNormalValue(r);
            Throwable ex = getException(r);
            try {
              dst.complete(fn.apply(val, ex));
            } catch (Throwable t) {
              dst.completeExceptionally(t);
            }
          }
        };
        if (executor != null) {
          executor.execute(rTask);
        } else {
          rTask.run();
        }
      }
    });
    return dst;
  }

  @Override
  public CompletableFuture<T> whenComplete(
      BiConsumer<? super T, ? super Throwable> action) {
    return whenCompleteAsync(action, null);
  }

  @Override
  public CompletableFuture<T> whenCompleteAsync(
      BiConsumer<? super T, ? super Throwable> action) {
    return whenCompleteAsync(action, defaultExecutor());
  }

  @Override
  public CompletableFuture<T> whenCompleteAsync(
      BiConsumer<? super T, ? super Throwable> action, Executor executor) {
    if (action == null) throw new NullPointerException();
    final CompletableFuture<T> dst = newIncompleteFuture();
    addDependent(new Runnable() {
      @Override
      public void run() {
        Runnable rTask = new Runnable() {
          @Override
          public void run() {
            Object r = result;
            T val = getNormalValue(r);
            Throwable ex = getException(r);
            Throwable thrown = null;
            try {
              action.accept(val, ex);
            } catch (Throwable t) {
              thrown = t;
            }
            if (ex != null) {
              dst.completeExceptionally(ex);
            } else if (thrown != null) {
              dst.completeExceptionally(thrown);
            } else {
              dst.complete(val);
            }
          }
        };
        if (executor != null) {
          executor.execute(rTask);
        } else {
          rTask.run();
        }
      }
    });
    return dst;
  }

  @Override
  public CompletableFuture<T> exceptionally(Function<Throwable, ? extends T> fn) {
    return exceptionallyAsync(fn, null);
  }

  public CompletableFuture<T> exceptionallyAsync(Function<Throwable, ? extends T> fn) {
    return exceptionallyAsync(fn, defaultExecutor());
  }

  public CompletableFuture<T> exceptionallyAsync(
      Function<Throwable, ? extends T> fn, Executor executor) {
    if (fn == null) throw new NullPointerException();
    final CompletableFuture<T> dst = newIncompleteFuture();
    addDependent(new Runnable() {
      @Override
      public void run() {
        Runnable rTask = new Runnable() {
          @Override
          public void run() {
            Object r = result;
            if (isExceptional(r)) {
              try {
                dst.complete(fn.apply(getException(r)));
              } catch (Throwable ex) {
                dst.completeExceptionally(ex);
              }
            } else {
              dst.internalComplete(r);
            }
          }
        };
        if (executor != null) {
          executor.execute(rTask);
        } else {
          rTask.run();
        }
      }
    });
    return dst;
  }

  public CompletableFuture<T> exceptionallyCompose(
      Function<Throwable, ? extends CompletionStage<T>> fn) {
    return exceptionallyComposeAsync(fn, null);
  }

  public CompletableFuture<T> exceptionallyComposeAsync(
      Function<Throwable, ? extends CompletionStage<T>> fn) {
    return exceptionallyComposeAsync(fn, defaultExecutor());
  }

  public CompletableFuture<T> exceptionallyComposeAsync(
      Function<Throwable, ? extends CompletionStage<T>> fn, Executor executor) {
    if (fn == null) throw new NullPointerException();
    final CompletableFuture<T> dst = newIncompleteFuture();
    addDependent(new Runnable() {
      @Override
      public void run() {
        Runnable rTask = new Runnable() {
          @Override
          public void run() {
            Object r = result;
            if (isExceptional(r)) {
              try {
                CompletionStage<T> stage = fn.apply(getException(r));
                if (stage == null) throw new NullPointerException();
                CompletableFuture<T> f = stage.toCompletableFuture();
                f.addDependent(new Runnable() {
                  @Override
                  public void run() {
                    dst.internalComplete(f.result);
                  }
                });
              } catch (Throwable ex) {
                dst.completeExceptionally(ex);
              }
            } else {
              dst.internalComplete(r);
            }
          }
        };
        if (executor != null) {
          executor.execute(rTask);
        } else {
          rTask.run();
        }
      }
    });
    return dst;
  }

  public static CompletableFuture<Void> allOf(CompletableFuture<?>... cfs) {
    if (cfs == null) throw new NullPointerException();
    final CompletableFuture<Void> dst = new CompletableFuture<Void>();
    if (cfs.length == 0) {
      dst.complete(null);
      return dst;
    }
    final int count = cfs.length;
    final CompletableFuture<?>[] arr = cfs.clone();
    Runnable check = new Runnable() {
      @Override
      public void run() {
        Throwable firstEx = null;
        for (int i = 0; i < count; ++i) {
          CompletableFuture<?> cf = arr[i];
          if (cf == null) throw new NullPointerException();
          if (!cf.isDone()) {
            return;
          }
          Object r = cf.result;
          if (firstEx == null && isExceptional(r)) {
            firstEx = getException(r);
          }
        }
        if (firstEx != null) {
          dst.completeExceptionally(firstEx);
        } else {
          dst.complete(null);
        }
      }
    };
    for (int i = 0; i < count; ++i) {
      CompletableFuture<?> cf = arr[i];
      if (cf == null) throw new NullPointerException();
      cf.addDependent(check);
    }
    check.run();
    return dst;
  }

  public static CompletableFuture<Object> anyOf(CompletableFuture<?>... cfs) {
    if (cfs == null) throw new NullPointerException();
    final CompletableFuture<Object> dst = new CompletableFuture<Object>();
    if (cfs.length == 0) {
      return dst;
    }
    for (int i = 0; i < cfs.length; ++i) {
      CompletableFuture<?> cf = cfs[i];
      if (cf == null) throw new NullPointerException();
      cf.addDependent(new Runnable() {
        @Override
        public void run() {
          Object r = cf.result;
          if (isExceptional(r)) {
            dst.internalComplete(r);
          } else {
            dst.complete(getNormalValue(r));
          }
        }
      });
    }
    return dst;
  }

  @Override
  public CompletableFuture<T> toCompletableFuture() {
    return this;
  }

  @Override
  public String toString() {
    Object r = result;
    int count = getNumberOfDependents();
    if (r == null) {
      return super.toString() + "[Not completed, " + count + " dependents]";
    }
    if (isExceptional(r)) {
      return super.toString() + "[Completed exceptionally: " + getException(r) + "]";
    }
    return super.toString() + "[Completed normally]";
  }
}
