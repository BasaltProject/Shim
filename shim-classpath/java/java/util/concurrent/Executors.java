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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Executors {
  private Executors() {}

  public static <T> Callable<T> callable(final Runnable task, final T result) {
    if (task == null) throw new NullPointerException();
    return new Callable<T>() {
      @Override
      public T call() throws Exception {
        task.run();
        return result;
      }
    };
  }

  public static Callable<Object> callable(final Runnable task) {
    return callable(task, null);
  }

  public static ThreadFactory defaultThreadFactory() {
    return new DefaultThreadFactory();
  }

  private static class DefaultThreadFactory implements ThreadFactory {
    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    DefaultThreadFactory() {
      namePrefix = "pool-" + poolNumber.getAndIncrement() + "-thread-";
    }

    @Override
    public Thread newThread(Runnable r) {
      if (r == null) throw new NullPointerException();
      Thread t = new Thread(r, namePrefix + threadNumber.getAndIncrement());
      if (t.isDaemon()) t.setDaemon(false);
      if (t.getPriority() != Thread.NORM_PRIORITY) t.setPriority(Thread.NORM_PRIORITY);
      return t;
    }
  }

  public static ExecutorService newFixedThreadPool(int nThreads) {
    return new ThreadPoolExecutor(
        nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
  }

  public static ExecutorService newFixedThreadPool(int nThreads, ThreadFactory threadFactory) {
    return new ThreadPoolExecutor(
        nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), threadFactory);
  }

  public static ExecutorService newSingleThreadExecutor() {
    return new ThreadPoolExecutor(
        1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
  }

  public static ExecutorService newSingleThreadExecutor(ThreadFactory threadFactory) {
    return new ThreadPoolExecutor(
        1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), threadFactory);
  }

  public static ExecutorService newCachedThreadPool() {
    return new ThreadPoolExecutor(
        0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
  }

  public static ExecutorService newCachedThreadPool(ThreadFactory threadFactory) {
    return new ThreadPoolExecutor(
        0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), threadFactory);
  }

  public static ExecutorService newVirtualThreadPerTaskExecutor() {
    return newThreadPerTaskExecutor(Thread.ofVirtual().factory());
  }

  public static ExecutorService newThreadPerTaskExecutor(ThreadFactory threadFactory) {
    return new ThreadPerTaskExecutor(threadFactory);
  }

  private static class ThreadPerTaskExecutor extends AbstractExecutorService {
    private final ThreadFactory factory;
    private volatile boolean isShutdown;

    ThreadPerTaskExecutor(ThreadFactory factory) {
      this.factory = (factory != null) ? factory : defaultThreadFactory();
    }

    @Override
    public void execute(Runnable command) {
      if (command == null) throw new NullPointerException();
      if (isShutdown) throw new RejectedExecutionException();
      Thread t = factory.newThread(command);
      if (t == null) throw new RejectedExecutionException();
      t.start();
    }

    @Override
    public void shutdown() {
      isShutdown = true;
    }

    @Override
    public List<Runnable> shutdownNow() {
      isShutdown = true;
      return Collections.emptyList();
    }

    @Override
    public boolean isShutdown() {
      return isShutdown;
    }

    @Override
    public boolean isTerminated() {
      return isShutdown;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) {
      return true;
    }
  }
}
