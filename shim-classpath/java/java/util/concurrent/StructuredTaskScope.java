/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util.concurrent;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface StructuredTaskScope<T, R> extends AutoCloseable {

  static <T, R> StructuredTaskScope<T, R> open(
      Joiner<? super T, ? extends R> joiner,
      Function<Configuration, Configuration> config) {
    Objects.requireNonNull(joiner, "joiner");
    Configuration conf = new ConfigurationImpl();
    if (config != null) {
      conf = config.apply(conf);
    }
    return new DefaultStructuredTaskScope<T, R>(joiner, (ConfigurationImpl) conf);
  }

  static <T, R> StructuredTaskScope<T, R> open(Joiner<? super T, ? extends R> joiner) {
    return open(joiner, null);
  }

  static <T> StructuredTaskScope<T, Void> open() {
    return open(Joiner.<T>awaitAll(), null);
  }

  <U extends T> Subtask<U> fork(Callable<? extends U> task);

  default <U extends T> Subtask<U> fork(Runnable task) {
    Objects.requireNonNull(task, "task");
    return fork(() -> {
      task.run();
      return null;
    });
  }

  R join() throws InterruptedException;

  boolean isCancelled();

  @Override
  void close();

  interface Subtask<T> extends Supplier<T> {
    enum State {
      UNAVAILABLE,
      SUCCESS,
      FAILED
    }

    State state();

    @Override
    T get();

    Throwable exception();
  }

  interface Joiner<T, R> {
    default boolean onFork(Subtask<? extends T> subtask) {
      return false;
    }

    default boolean onComplete(Subtask<? extends T> subtask) {
      return false;
    }

    R result() throws Throwable;

    static <T> Joiner<T, Stream<Subtask<T>>> allSuccessfulOrThrow() {
      return new AllSuccessfulOrThrowJoiner<T>();
    }

    static <T> Joiner<T, T> anySuccessfulResultOrThrow() {
      return new AnySuccessfulResultOrThrowJoiner<T>();
    }

    static <T> Joiner<T, Void> awaitAllSuccessfulOrThrow() {
      return new AwaitAllSuccessfulOrThrowJoiner<T>();
    }

    static <T> Joiner<T, Void> awaitAll() {
      return new AwaitAllJoiner<T>();
    }

    static <T> Joiner<T, Stream<Subtask<T>>> allUntil(
        Predicate<Subtask<? extends T>> predicate) {
      return new AllUntilJoiner<T>(predicate);
    }
  }

  interface Configuration {
    Configuration withThreadFactory(ThreadFactory threadFactory);

    Configuration withName(String name);

    Configuration withTimeout(Duration timeout);
  }

  final class FailedException extends RuntimeException {
    public FailedException(Throwable cause) {
      super(cause);
    }
  }

  class ShutdownOnSuccess<T> implements AutoCloseable {
    private final StructuredTaskScope<T, T> scope;

    public ShutdownOnSuccess() {
      this.scope = StructuredTaskScope.open(Joiner.<T>anySuccessfulResultOrThrow());
    }

    public <U extends T> Subtask<U> fork(Callable<? extends U> task) {
      return scope.fork(task);
    }

    public ShutdownOnSuccess<T> join() throws InterruptedException {
      scope.join();
      return this;
    }

    public T result() throws ExecutionException {
      try {
        return scope.join();
      } catch (Throwable t) {
        throw new ExecutionException(t);
      }
    }

    @Override
    public void close() {
      scope.close();
    }
  }

  class ShutdownOnFailure implements AutoCloseable {
    private final StructuredTaskScope<Object, Void> scope;
    private volatile Throwable firstException;

    public ShutdownOnFailure() {
      this.scope = StructuredTaskScope.open(new Joiner<Object, Void>() {
        @Override
        public boolean onComplete(Subtask<?> subtask) {
          if (subtask.state() == Subtask.State.FAILED) {
            if (firstException == null) {
              firstException = subtask.exception();
            }
            return true;
          }
          return false;
        }

        @Override
        public Void result() {
          return null;
        }
      });
    }

    public <U> Subtask<U> fork(Callable<? extends U> task) {
      return scope.fork(task);
    }

    public ShutdownOnFailure join() throws InterruptedException {
      scope.join();
      return this;
    }

    public void throwIfFailed() throws ExecutionException {
      if (firstException != null) {
        throw new ExecutionException(firstException);
      }
    }

    @Override
    public void close() {
      scope.close();
    }
  }

  final class ConfigurationImpl implements Configuration {
    ThreadFactory threadFactory = Executors.defaultThreadFactory();
    String name;
    Duration timeout;

    @Override
    public Configuration withThreadFactory(ThreadFactory threadFactory) {
      this.threadFactory = Objects.requireNonNull(threadFactory, "threadFactory");
      return this;
    }

    @Override
    public Configuration withName(String name) {
      this.name = Objects.requireNonNull(name, "name");
      return this;
    }

    @Override
    public Configuration withTimeout(Duration timeout) {
      this.timeout = Objects.requireNonNull(timeout, "timeout");
      return this;
    }
  }

  final class SubtaskImpl<T> implements Subtask<T> {
    private final Callable<? extends T> callable;
    private volatile State state = State.UNAVAILABLE;
    private volatile T result;
    private volatile Throwable exception;

    SubtaskImpl(Callable<? extends T> callable) {
      this.callable = callable;
    }

    @Override
    public State state() {
      return state;
    }

    @Override
    public T get() {
      if (state == State.UNAVAILABLE) {
        throw new IllegalStateException("Subtask result is not available");
      }
      if (state == State.FAILED) {
        throw new IllegalStateException("Subtask failed", exception);
      }
      return result;
    }

    @Override
    public Throwable exception() {
      if (state == State.UNAVAILABLE) {
        throw new IllegalStateException("Subtask result is not available");
      }
      if (state == State.SUCCESS) {
        throw new IllegalStateException("Subtask completed successfully");
      }
      return exception;
    }

    void execute() {
      try {
        result = callable.call();
        state = State.SUCCESS;
      } catch (Throwable t) {
        exception = t;
        state = State.FAILED;
      }
    }
  }

  final class DefaultStructuredTaskScope<T, R> implements StructuredTaskScope<T, R> {
    private final Joiner<? super T, ? extends R> joiner;
    private final Thread owner = Thread.currentThread();
    private final ThreadFactory threadFactory;
    private final Duration timeout;
    private final List<SubtaskImpl<? extends T>> subtasks =
        Collections.synchronizedList(new ArrayList<SubtaskImpl<? extends T>>());
    private final CountDownLatch latch = new CountDownLatch(1);
    private final AtomicInteger pending = new AtomicInteger(0);
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private volatile boolean joined;

    DefaultStructuredTaskScope(
        Joiner<? super T, ? extends R> joiner,
        ConfigurationImpl config) {
      this.joiner = joiner;
      this.threadFactory = config.threadFactory;
      this.timeout = config.timeout;
    }

    private void checkOwner() {
      if (Thread.currentThread() != owner) {
        throw new IllegalStateException("Current thread is not scope owner");
      }
    }

    @Override
    public <U extends T> Subtask<U> fork(Callable<? extends U> task) {
      checkOwner();
      if (closed.get()) {
        throw new IllegalStateException("Scope is closed");
      }
      Objects.requireNonNull(task, "task");
      SubtaskImpl<U> subtask = new SubtaskImpl<U>(task);
      subtasks.add(subtask);
      pending.incrementAndGet();

      boolean earlyFinish = joiner.onFork(subtask);
      if (earlyFinish) {
        cancelled.set(true);
        latch.countDown();
      }

      threadFactory.newThread(() -> {
        try {
          if (!cancelled.get()) {
            subtask.execute();
          }
        } finally {
          boolean done = joiner.onComplete(subtask);
          int left = pending.decrementAndGet();
          if (done || left == 0) {
            if (done) {
              cancelled.set(true);
            }
            latch.countDown();
          }
        }
      }).start();

      return subtask;
    }

    @Override
    public R join() throws InterruptedException {
      checkOwner();
      if (closed.get()) {
        throw new IllegalStateException("Scope is closed");
      }
      if (pending.get() > 0) {
        if (timeout != null) {
          boolean ok = latch.await(timeout.toMillis(), TimeUnit.MILLISECONDS);
          if (!ok) {
            cancelled.set(true);
            throw new RuntimeException("Scope timeout");
          }
        } else {
          latch.await();
        }
      }
      joined = true;
      try {
        return joiner.result();
      } catch (RuntimeException | Error e) {
        throw e;
      } catch (Throwable t) {
        throw new FailedException(t);
      }
    }

    @Override
    public boolean isCancelled() {
      return cancelled.get();
    }

    @Override
    public void close() {
      checkOwner();
      if (closed.compareAndSet(false, true)) {
        cancelled.set(true);
        latch.countDown();
      }
    }
  }

  final class AwaitAllJoiner<T> implements Joiner<T, Void> {
    @Override
    public Void result() {
      return null;
    }
  }

  final class AwaitAllSuccessfulOrThrowJoiner<T> implements Joiner<T, Void> {
    private final AtomicReference<Throwable> firstException = new AtomicReference<Throwable>();

    @Override
    public boolean onComplete(Subtask<? extends T> subtask) {
      if (subtask.state() == Subtask.State.FAILED) {
        firstException.compareAndSet(null, subtask.exception());
        return true;
      }
      return false;
    }

    @Override
    public Void result() throws Throwable {
      Throwable t = firstException.get();
      if (t != null) {
        throw t;
      }
      return null;
    }
  }

  final class AnySuccessfulResultOrThrowJoiner<T> implements Joiner<T, T> {
    private final AtomicReference<T> winner = new AtomicReference<T>();
    private final AtomicReference<Throwable> lastException = new AtomicReference<Throwable>();
    private final AtomicInteger successCount = new AtomicInteger(0);

    @Override
    public boolean onComplete(Subtask<? extends T> subtask) {
      if (subtask.state() == Subtask.State.SUCCESS) {
        winner.compareAndSet(null, subtask.get());
        successCount.incrementAndGet();
        return true;
      }
      if (subtask.state() == Subtask.State.FAILED) {
        lastException.compareAndSet(null, subtask.exception());
      }
      return false;
    }

    @Override
    public T result() throws Throwable {
      if (successCount.get() > 0 || winner.get() != null) {
        return winner.get();
      }
      Throwable t = lastException.get();
      if (t != null) {
        throw t;
      }
      throw new NoSuchElementException("No task completed successfully");
    }
  }

  final class AllSuccessfulOrThrowJoiner<T> implements Joiner<T, Stream<Subtask<T>>> {
    private final List<Subtask<T>> results =
        Collections.synchronizedList(new ArrayList<Subtask<T>>());
    private final AtomicReference<Throwable> firstException = new AtomicReference<Throwable>();

    @Override
    @SuppressWarnings("unchecked")
    public boolean onComplete(Subtask<? extends T> subtask) {
      if (subtask.state() == Subtask.State.FAILED) {
        firstException.compareAndSet(null, subtask.exception());
        return true;
      }
      results.add((Subtask<T>) subtask);
      return false;
    }

    @Override
    public Stream<Subtask<T>> result() throws Throwable {
      Throwable t = firstException.get();
      if (t != null) {
        throw t;
      }
      return results.stream();
    }
  }

  final class AllUntilJoiner<T> implements Joiner<T, Stream<Subtask<T>>> {
    private final Predicate<Subtask<? extends T>> predicate;
    private final List<Subtask<T>> results =
        Collections.synchronizedList(new ArrayList<Subtask<T>>());

    AllUntilJoiner(Predicate<Subtask<? extends T>> predicate) {
      this.predicate = Objects.requireNonNull(predicate, "predicate");
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean onComplete(Subtask<? extends T> subtask) {
      results.add((Subtask<T>) subtask);
      return predicate.test(subtask);
    }

    @Override
    public Stream<Subtask<T>> result() {
      return results.stream();
    }
  }
}
