/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.util.concurrent;

import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class Executors {
  private Executors() { }

  public static ExecutorService newFixedThreadPool(int nThreads) {
    return new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
  }

  public static ExecutorService newWorkStealingPool(int parallelism) {
    return new ForkJoinPool(parallelism);
  }

  public static ExecutorService newWorkStealingPool() {
    return new ForkJoinPool();
  }

  public static ExecutorService newFixedThreadPool(int nThreads, ThreadFactory threadFactory) {
    return new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(),
                                  threadFactory);
  }

  public static ExecutorService newSingleThreadExecutor() {
    return new DelegatedExecutorService(new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                                                               new LinkedBlockingQueue<Runnable>()));
  }

  public static ExecutorService newSingleThreadExecutor(ThreadFactory threadFactory) {
    return new DelegatedExecutorService(new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                                                               new LinkedBlockingQueue<Runnable>(), threadFactory));
  }

  public static ExecutorService newCachedThreadPool() {
    return new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
  }

  public static ExecutorService newCachedThreadPool(ThreadFactory threadFactory) {
    return new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(),
                                  threadFactory);
  }

  public static ExecutorService newThreadPerTaskExecutor(ThreadFactory threadFactory) {
    return new ThreadPerTaskExecutor(Objects.requireNonNull(threadFactory));
  }

  public static ExecutorService newVirtualThreadPerTaskExecutor() {
    return newThreadPerTaskExecutor(Thread.ofVirtual().factory());
  }

  public static ScheduledExecutorService newSingleThreadScheduledExecutor() {
    return new DelegatedScheduledExecutorService(new ScheduledThreadPoolExecutor(1));
  }

  public static ScheduledExecutorService newSingleThreadScheduledExecutor(ThreadFactory threadFactory) {
    return new DelegatedScheduledExecutorService(new ScheduledThreadPoolExecutor(1, threadFactory));
  }

  public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize) {
    return new ScheduledThreadPoolExecutor(corePoolSize);
  }

  public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize, ThreadFactory threadFactory) {
    return new ScheduledThreadPoolExecutor(corePoolSize, threadFactory);
  }

  public static ExecutorService unconfigurableExecutorService(ExecutorService executor) {
    if (executor == null) {
      throw new NullPointerException();
    }
    return new DelegatedExecutorService(executor);
  }

  public static ScheduledExecutorService unconfigurableScheduledExecutorService(ScheduledExecutorService executor) {
    if (executor == null) {
      throw new NullPointerException();
    }
    return new DelegatedScheduledExecutorService(executor);
  }

  public static ThreadFactory defaultThreadFactory() {
    return new DefaultThreadFactory();
  }

  public static ThreadFactory privilegedThreadFactory() {
    return new DefaultThreadFactory();
  }

  public static <T> Callable<T> callable(Runnable task, T result) {
    if (task == null) {
      throw new NullPointerException();
    }
    return new RunnableAdapter<T>(task, result);
  }

  public static Callable<Object> callable(Runnable task) {
    if (task == null) {
      throw new NullPointerException();
    }
    return new RunnableAdapter<Object>(task, null);
  }

  public static Callable<Object> callable(final PrivilegedAction<?> action) {
    if (action == null) {
      throw new NullPointerException();
    }
    return new Callable<Object>() {
      public Object call() {
        return action.run();
      }
    };
  }

  public static <T> Callable<T> privilegedCallable(Callable<T> callable) {
    if (callable == null) {
      throw new NullPointerException();
    }
    return callable;
  }

  private static final class RunnableAdapter<T> implements Callable<T> {
    private final Runnable task;
    private final T result;

    RunnableAdapter(Runnable task, T result) {
      this.task = task;
      this.result = result;
    }

    public T call() {
      task.run();
      return result;
    }

    public String toString() {
      return super.toString() + "[Wrapped task = " + task + "]";
    }
  }

  private static class DefaultThreadFactory implements ThreadFactory {
    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    DefaultThreadFactory() {
      group = Thread.currentThread().getThreadGroup();
      namePrefix = "pool-" + poolNumber.getAndIncrement() + "-thread-";
    }

    public Thread newThread(Runnable r) {
      Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
      if (t.isDaemon()) {
        t.setDaemon(false);
      }
      if (t.getPriority() != Thread.NORM_PRIORITY) {
        t.setPriority(Thread.NORM_PRIORITY);
      }
      return t;
    }
  }

  private static class DelegatedExecutorService implements ExecutorService {
    private final ExecutorService e;

    DelegatedExecutorService(ExecutorService executor) {
      e = executor;
    }

    public void execute(Runnable command) {
      e.execute(command);
    }

    public void shutdown() {
      e.shutdown();
    }

    public List<Runnable> shutdownNow() {
      return e.shutdownNow();
    }

    public boolean isShutdown() {
      return e.isShutdown();
    }

    public boolean isTerminated() {
      return e.isTerminated();
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
      return e.awaitTermination(timeout, unit);
    }

    public Future<?> submit(Runnable task) {
      return e.submit(task);
    }

    public <T> Future<T> submit(Callable<T> task) {
      return e.submit(task);
    }

    public <T> Future<T> submit(Runnable task, T result) {
      return e.submit(task, result);
    }

    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
      return e.invokeAll(tasks);
    }

    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
        throws InterruptedException {
      return e.invokeAll(tasks, timeout, unit);
    }

    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
      return e.invokeAny(tasks);
    }

    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
      return e.invokeAny(tasks, timeout, unit);
    }
  }

  private static class DelegatedScheduledExecutorService extends DelegatedExecutorService
      implements ScheduledExecutorService {
    private final ScheduledExecutorService e;

    DelegatedScheduledExecutorService(ScheduledExecutorService executor) {
      super(executor);
      e = executor;
    }

    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
      return e.schedule(command, delay, unit);
    }

    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
      return e.schedule(callable, delay, unit);
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
      return e.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
      return e.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }
  }

  private static class ThreadPerTaskExecutor extends AbstractExecutorService {
    private final ThreadFactory factory;
    private final Set<Thread> threads = new HashSet<Thread>();
    private final Object lock = new Object();
    private boolean shutdown;
    private boolean terminated;

    ThreadPerTaskExecutor(ThreadFactory factory) {
      this.factory = factory;
    }

    public void execute(Runnable task) {
      Objects.requireNonNull(task);
      final Thread thread;
      synchronized (lock) {
        if (shutdown) {
          throw new RejectedExecutionException();
        }
        final Runnable wrapped = task;
        thread = factory.newThread(new Runnable() {
          public void run() {
            try {
              wrapped.run();
            } finally {
              taskComplete(Thread.currentThread());
            }
          }
        });
        if (thread == null) {
          throw new RejectedExecutionException();
        }
        threads.add(thread);
      }
      boolean started = false;
      try {
        thread.start();
        started = true;
      } finally {
        if (!started) {
          taskComplete(thread);
        }
      }
    }

    private void taskComplete(Thread thread) {
      synchronized (lock) {
        threads.remove(thread);
        tryTerminate();
      }
    }

    private void tryTerminate() {
      if (shutdown && threads.isEmpty() && !terminated) {
        terminated = true;
        lock.notifyAll();
      }
    }

    public void shutdown() {
      synchronized (lock) {
        if (!shutdown) {
          shutdown = true;
          tryTerminate();
        }
      }
    }

    public List<Runnable> shutdownNow() {
      List<Thread> snapshot;
      synchronized (lock) {
        shutdown = true;
        snapshot = new ArrayList<Thread>(threads);
        tryTerminate();
      }
      for (Thread t : snapshot) {
        t.interrupt();
      }
      return new ArrayList<Runnable>();
    }

    public boolean isShutdown() {
      synchronized (lock) {
        return shutdown;
      }
    }

    public boolean isTerminated() {
      synchronized (lock) {
        return terminated;
      }
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
      long nanos = unit.toNanos(timeout);
      long deadline = System.nanoTime() + nanos;
      synchronized (lock) {
        while (!terminated) {
          if (nanos <= 0L) {
            return false;
          }
          lock.wait(nanos / 1000000L, (int) (nanos % 1000000L));
          nanos = deadline - System.nanoTime();
        }
        return true;
      }
    }
  }

  public static Callable<Object> callable(final java.security.PrivilegedExceptionAction<?> action) {
    if (action == null) {
      throw new NullPointerException();
    }
    return new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        return action.run();
      }
    };
  }

  public static <T> Callable<T> privilegedCallableUsingCurrentClassLoader(Callable<T> callable) {
    if (callable == null) {
      throw new NullPointerException();
    }
    return callable;
  }
}
