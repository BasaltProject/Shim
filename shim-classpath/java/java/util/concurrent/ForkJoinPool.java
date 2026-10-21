/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.concurrent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ForkJoinPool extends AbstractExecutorService implements ScheduledExecutorService {
  static final int MAX_CAP = 0x7fff;
  private static final AtomicInteger poolIds = new AtomicInteger();

  public static interface ForkJoinWorkerThreadFactory {
    ForkJoinWorkerThread newThread(ForkJoinPool pool);
  }

  public static interface ManagedBlocker {
    boolean block() throws InterruptedException;

    boolean isReleasable();
  }

  private static final class DefaultForkJoinWorkerThreadFactory implements ForkJoinWorkerThreadFactory {
    public final ForkJoinWorkerThread newThread(ForkJoinPool pool) {
      return new ForkJoinWorkerThread(pool);
    }
  }

  public static final ForkJoinWorkerThreadFactory defaultForkJoinWorkerThreadFactory =
      new DefaultForkJoinWorkerThreadFactory();

  static final class WorkQueue {
    final ArrayDeque<ForkJoinTask<?>> deque = new ArrayDeque<ForkJoinTask<?>>();
    int index;
  }

  static final ForkJoinPool common = new ForkJoinPool((byte) 0);

  private final Object lock = new Object();
  private final ArrayDeque<ForkJoinTask<?>> submissions = new ArrayDeque<ForkJoinTask<?>>();
  private final List<WorkQueue> queues = new ArrayList<WorkQueue>();
  private final List<ForkJoinWorkerThread> workers = new ArrayList<ForkJoinWorkerThread>();
  private final ForkJoinWorkerThreadFactory factory;
  private final Thread.UncaughtExceptionHandler ueh;
  private final boolean asyncMode;
  private final String workerNamePrefix;
  private final boolean isCommon;
  private int parallelism;
  private int idle;
  private int active;
  private int blocked;
  private int workerSeq;
  private int nextIndex;
  private long steals;
  private boolean shutdown;
  private boolean stop;
  private boolean terminated;
  private boolean cancelDelayed;
  private final AtomicLong delayedCount = new AtomicLong();
  private ScheduledThreadPoolExecutor delayer;
  private final List<ScheduledForkJoinTask<?>> delayedTasks = new ArrayList<ScheduledForkJoinTask<?>>();

  private ForkJoinPool(byte forCommonPoolOnly) {
    int par = -1;
    try {
      String pp = System.getProperty("java.util.concurrent.ForkJoinPool.common.parallelism");
      if (pp != null) {
        par = Integer.parseInt(pp);
      }
    } catch (Exception ignore) {
      par = -1;
    }
    if (par < 0) {
      par = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
    }
    this.parallelism = Math.min(Math.max(par, 1), MAX_CAP);
    this.factory = defaultForkJoinWorkerThreadFactory;
    this.ueh = null;
    this.asyncMode = false;
    this.workerNamePrefix = "ForkJoinPool.commonPool-worker-";
    this.isCommon = true;
  }

  public ForkJoinPool() {
    this(Math.min(MAX_CAP, Runtime.getRuntime().availableProcessors()), defaultForkJoinWorkerThreadFactory, null,
        false);
  }

  public ForkJoinPool(int parallelism) {
    this(parallelism, defaultForkJoinWorkerThreadFactory, null, false);
  }

  public ForkJoinPool(int parallelism, ForkJoinWorkerThreadFactory factory, Thread.UncaughtExceptionHandler handler,
      boolean asyncMode) {
    this(parallelism, factory, handler, asyncMode, 0, MAX_CAP, 1, null, 60000L, TimeUnit.MILLISECONDS);
  }

  public ForkJoinPool(int parallelism, ForkJoinWorkerThreadFactory factory, Thread.UncaughtExceptionHandler handler,
      boolean asyncMode, int corePoolSize, int maximumPoolSize, int minimumRunnable,
      Predicate<? super ForkJoinPool> saturate, long keepAliveTime, TimeUnit unit) {
    if (factory == null || unit == null) {
      throw new NullPointerException();
    }
    if (parallelism <= 0 || parallelism > MAX_CAP || maximumPoolSize < parallelism || keepAliveTime <= 0L) {
      throw new IllegalArgumentException();
    }
    this.parallelism = parallelism;
    this.factory = factory;
    this.ueh = handler;
    this.asyncMode = asyncMode;
    this.workerNamePrefix = "ForkJoinPool-" + (poolIds.incrementAndGet()) + "-worker-";
    this.isCommon = false;
  }

  public static ForkJoinPool commonPool() {
    return common;
  }

  public static int getCommonPoolParallelism() {
    return common.parallelism;
  }

  String nextWorkerName() {
    synchronized (lock) {
      return workerNamePrefix + (++workerSeq);
    }
  }

  WorkQueue registerWorker(ForkJoinWorkerThread w) {
    synchronized (lock) {
      WorkQueue q = new WorkQueue();
      q.index = nextIndex++;
      queues.add(q);
      workers.add(w);
      return q;
    }
  }

  void deregisterWorker(ForkJoinWorkerThread w, Throwable ex) {
    synchronized (lock) {
      workers.remove(w);
      WorkQueue q = w.workQueue;
      ForkJoinTask<?> t;
      while ((t = q.deque.pollFirst()) != null) {
        submissions.addLast(t);
      }
      queues.remove(q);
      lock.notifyAll();
      tryTerminateLocked();
      if (!terminated && (!submissions.isEmpty() || !workers.isEmpty())) {
        maybeCreateWorkerLocked();
      }
    }
    if (ex != null) {
      Thread.UncaughtExceptionHandler h = ueh;
      if (h != null) {
        try {
          h.uncaughtException(w, ex);
        } catch (Throwable ignore) { }
      }
    }
  }

  private boolean hasOneShotDelayedLocked() {
    for (ScheduledForkJoinTask<?> t : delayedTasks) {
      if (t.period == 0L && !t.isDone()) {
        return true;
      }
    }
    return false;
  }

  private boolean readyToTerminateLocked() {
    return (shutdown || stop) && active == 0 && (stop || (allEmptyLocked() && !hasOneShotDelayedLocked()));
  }

  private void tryTerminateLocked() {
    if (!terminated && !isCommon && workers.isEmpty() && readyToTerminateLocked()) {
      terminated = true;
      lock.notifyAll();
    }
  }

  private boolean allEmptyLocked() {
    if (!submissions.isEmpty()) {
      return false;
    }
    for (WorkQueue q : queues) {
      if (!q.deque.isEmpty()) {
        return false;
      }
    }
    return true;
  }

  private void maybeCreateWorkerLocked() {
    if (stop || (workers.size() >= parallelism) || idle > 0 || terminated) {
      return;
    }
    if (allEmptyLocked()) {
      return;
    }
    try {
      ForkJoinWorkerThread w = factory.newThread(this);
      if (w != null) {
        w.start();
      }
    } catch (Throwable ex) {
      if (ex instanceof Error) {
        throw (Error) ex;
      }
    }
  }

  private ForkJoinTask<?> pollLocked(WorkQueue q) {
    ForkJoinTask<?> t;
    if (q != null) {
      t = asyncMode ? q.deque.pollFirst() : q.deque.pollLast();
      if (t != null) {
        return t;
      }
    }
    t = submissions.pollFirst();
    if (t != null) {
      return t;
    }
    for (WorkQueue other : queues) {
      if (other != q) {
        t = other.deque.pollFirst();
        if (t != null) {
          steals++;
          return t;
        }
      }
    }
    return null;
  }

  void runWorker(ForkJoinWorkerThread w) {
    WorkQueue q = w.workQueue;
    for (;;) {
      ForkJoinTask<?> t;
      synchronized (lock) {
        for (;;) {
          if (stop) {
            return;
          }
          t = pollLocked(q);
          if (t != null) {
            active++;
            break;
          }
          if (!isCommon && readyToTerminateLocked()) {
            lock.notifyAll();
            return;
          }
          idle++;
          try {
            lock.wait(60000L);
          } catch (InterruptedException e) {
            if (stop) {
              return;
            }
          } finally {
            idle--;
          }
        }
      }
      try {
        t.doExec();
      } catch (Throwable ex) {
        Thread.UncaughtExceptionHandler h = ueh;
        if (h == null) {
          h = w.getUncaughtExceptionHandler();
        }
        if (h != null) {
          try {
            h.uncaughtException(w, ex);
          } catch (Throwable ignore) { }
        }
      } finally {
        synchronized (lock) {
          active--;
          if (lockWaiters > 0 || shutdown || stop) {
            lock.notifyAll();
          }
        }
      }
    }
  }

  private int lockWaiters;

  private void wakeLocked() {
    if (lockWaiters > 0) {
      lock.notifyAll();
    } else if (idle > 0) {
      lock.notify();
    }
  }

  void pushLocal(ForkJoinTask<?> task, WorkQueue q) {
    synchronized (lock) {
      if (stop || (shutdown && !isCommon && terminated)) {
        throw new RejectedExecutionException();
      }
      task.pool = this;
      q.deque.addLast(task);
      wakeLocked();
      maybeCreateWorkerLocked();
    }
  }

  void externalPush(ForkJoinTask<?> task) {
    externalPush(task, false);
  }

  void externalPush(ForkJoinTask<?> task, boolean force) {
    synchronized (lock) {
      if (!isCommon && (force ? stop : (shutdown || stop))) {
        throw new RejectedExecutionException();
      }
      task.pool = this;
      task.submitter = Thread.currentThread();
      submissions.addLast(task);
      wakeLocked();
      maybeCreateWorkerLocked();
    }
  }

  boolean tryUnpush(WorkQueue q, ForkJoinTask<?> task) {
    synchronized (lock) {
      ForkJoinTask<?> last = q.deque.peekLast();
      if (last == task) {
        q.deque.pollLast();
        return true;
      }
    }
    return false;
  }

  boolean tryRemoveSubmission(ForkJoinTask<?> task) {
    synchronized (lock) {
      ForkJoinTask<?> first = submissions.peekFirst();
      if (first == task) {
        submissions.pollFirst();
        return true;
      }
      return submissions.remove(task);
    }
  }

  int queueSizeOf(WorkQueue q) {
    return queueSize(q);
  }

  int queueSize(WorkQueue q) {
    synchronized (lock) {
      return q.deque.size();
    }
  }

  int submissionsBy(Thread t) {
    int n = 0;
    synchronized (lock) {
      for (ForkJoinTask<?> task : submissions) {
        if (task.submitter == t) {
          n++;
        }
      }
    }
    return n;
  }

  static void awaitTask(ForkJoinTask<?> task, long deadline, boolean interruptible) throws InterruptedException {
    Thread th = Thread.currentThread();
    ForkJoinWorkerThread w = (th instanceof ForkJoinWorkerThread) ? (ForkJoinWorkerThread) th : null;
    ForkJoinPool wp = w != null ? w.pool : null;
    while (task.status >= 0) {
      if (interruptible && Thread.interrupted()) {
        throw new InterruptedException();
      }
      ForkJoinTask<?> next = null;
      if (wp != null) {
        synchronized (wp.lock) {
          ForkJoinTask<?> last = w.workQueue.deque.peekLast();
          if (last == task) {
            w.workQueue.deque.pollLast();
            next = task;
          } else {
            next = wp.pollLocked(w.workQueue);
          }
          if (next != null) {
            wp.active += (next == task ? 0 : 0);
          }
        }
      } else {
        ForkJoinPool p = task.pool;
        if (p != null && p.isCommon && p.tryRemoveSubmission(task)) {
          next = task;
        }
      }
      if (next != null) {
        next.doExec();
        continue;
      }
      long waitMillis = 10L;
      if (deadline != 0L) {
        long remaining = deadline - System.nanoTime();
        if (remaining <= 0L) {
          return;
        }
        waitMillis = Math.max(1L, Math.min(waitMillis, remaining / 1000000L));
      }
      if (wp != null) {
        synchronized (wp.lock) {
          wp.blocked++;
        }
      }
      try {
        synchronized (task) {
          if (task.status >= 0) {
            task.wait(waitMillis);
          }
        }
      } finally {
        if (wp != null) {
          synchronized (wp.lock) {
            wp.blocked--;
          }
        }
      }
    }
  }

  void helpQuiesce(WorkQueue q) {
    for (;;) {
      ForkJoinTask<?> t;
      synchronized (lock) {
        t = pollLocked(q);
        if (t == null) {
          if (active <= 1 + 0 && allEmptyLocked()) {
            return;
          }
        }
      }
      if (t != null) {
        t.doExec();
      } else {
        Thread.yield();
      }
    }
  }

  void helpComplete(CountedCompleter<?> task, int maxTasks) {
    int count = 0;
    while (count < maxTasks && task.status >= 0) {
      ForkJoinTask<?> found = null;
      synchronized (lock) {
        found = findDescendantLocked(submissions, task);
        if (found == null) {
          for (WorkQueue q : queues) {
            found = findDescendantLocked(q.deque, task);
            if (found != null) {
              break;
            }
          }
        }
      }
      if (found == null) {
        break;
      }
      found.doExec();
      count++;
    }
  }

  private static ForkJoinTask<?> findDescendantLocked(ArrayDeque<ForkJoinTask<?>> dq, CountedCompleter<?> root) {
    Iterator<ForkJoinTask<?>> it = dq.descendingIterator();
    while (it.hasNext()) {
      ForkJoinTask<?> t = it.next();
      if (t instanceof CountedCompleter) {
        CountedCompleter<?> c = (CountedCompleter<?>) t;
        for (CountedCompleter<?> a = c; a != null; a = a.completer) {
          if (a == root) {
            it.remove();
            return t;
          }
        }
      }
    }
    return null;
  }

  public <T> T invoke(ForkJoinTask<T> task) {
    if (task == null) {
      throw new NullPointerException();
    }
    Thread t = Thread.currentThread();
    if (t instanceof ForkJoinWorkerThread && ((ForkJoinWorkerThread) t).pool == this) {
      return task.invoke();
    }
    if (isCommon) {
      return task.invoke();
    }
    externalSubmit(task);
    return task.join();
  }

  public void execute(ForkJoinTask<?> task) {
    externalSubmit(task);
  }

  public <T> ForkJoinTask<T> externalSubmit(ForkJoinTask<T> task) {
    if (task == null) {
      throw new NullPointerException();
    }
    Thread t = Thread.currentThread();
    if (t instanceof ForkJoinWorkerThread && ((ForkJoinWorkerThread) t).pool == this) {
      ForkJoinWorkerThread w = (ForkJoinWorkerThread) t;
      pushLocal(task, w.workQueue);
    } else {
      externalPush(task);
    }
    return task;
  }

  public <T> ForkJoinTask<T> lazySubmit(ForkJoinTask<T> task) {
    return externalSubmit(task);
  }

  public <T> ForkJoinTask<T> submit(ForkJoinTask<T> task) {
    return externalSubmit(task);
  }

  @Override
  public void execute(Runnable task) {
    if (task == null) {
      throw new NullPointerException();
    }
    ForkJoinTask<?> job;
    if (task instanceof ForkJoinTask<?>) {
      job = (ForkJoinTask<?>) task;
    } else {
      job = new ForkJoinTask.RunnableExecuteAction(task);
    }
    externalSubmit(job);
  }

  @Override
  public <T> ForkJoinTask<T> submit(Callable<T> task) {
    return externalSubmit(ForkJoinTask.adaptInterruptible(task));
  }

  @Override
  public <T> ForkJoinTask<T> submit(Runnable task, T result) {
    return externalSubmit(ForkJoinTask.adaptInterruptible(task, result));
  }

  @Override
  @SuppressWarnings("unchecked")
  public ForkJoinTask<?> submit(Runnable task) {
    if (task == null) {
      throw new NullPointerException();
    }
    ForkJoinTask<?> job;
    if (task instanceof ForkJoinTask<?>) {
      job = (ForkJoinTask<Void>) task;
    } else {
      job = ForkJoinTask.adaptInterruptible(task);
    }
    return externalSubmit(job);
  }

  public <T> ForkJoinTask<T> submitWithTimeout(Callable<T> callable, long timeout, TimeUnit unit,
      Consumer<? super ForkJoinTask<T>> timeoutAction) {
    final ScheduledFuture<?>[] handle = new ScheduledFuture<?>[1];
    final ForkJoinTask<T> task = ForkJoinTask.adaptInterruptible(new Callable<T>() {
      public T call() throws Exception {
        try {
          return callable.call();
        } finally {
          ScheduledFuture<?> h = handle[0];
          if (h != null) {
            h.cancel(false);
          }
        }
      }
    });
    externalSubmit(task);
    if (timeout > 0 || timeoutAction != null) {
      handle[0] = delayerExecutor().schedule(new Runnable() {
        public void run() {
          if (!task.isDone()) {
            if (timeoutAction == null) {
              task.cancel(true);
            } else {
              timeoutAction.accept(task);
              if (task instanceof ForkJoinTask.AdaptedInterruptibleCallable) {
                Thread runner = ((ForkJoinTask.AdaptedInterruptibleCallable<?>) task).runner;
                if (runner != null) {
                  runner.interrupt();
                }
              }
            }
          }
        }
      }, timeout, unit);
    }
    return task;
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) {
    ArrayList<Future<T>> futures = new ArrayList<Future<T>>(tasks.size());
    try {
      for (Callable<T> t : tasks) {
        ForkJoinTask<T> f = ForkJoinTask.adaptInterruptible(t);
        futures.add(f);
        externalSubmit(f);
      }
      for (int i = futures.size() - 1; i >= 0; --i) {
        ((ForkJoinTask<?>) futures.get(i)).quietlyJoin();
      }
      return futures;
    } catch (Throwable t) {
      for (Future<T> e : futures) {
        e.cancel(true);
      }
      throw t;
    }
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
      throws InterruptedException {
    long nanos = unit.toNanos(timeout);
    long deadline = System.nanoTime() + nanos;
    ArrayList<Future<T>> futures = new ArrayList<Future<T>>(tasks.size());
    int j = 0;
    try {
      for (Callable<T> t : tasks) {
        ForkJoinTask<T> f = ForkJoinTask.adaptInterruptible(t);
        futures.add(f);
        externalSubmit(f);
      }
      for (int i = futures.size() - 1; i >= 0; --i) {
        ((ForkJoinTask<?>) futures.get(i)).quietlyJoin(Math.max(0L, deadline - System.nanoTime()),
            TimeUnit.NANOSECONDS);
      }
      for (Future<T> f : futures) {
        if (!f.isDone()) {
          f.cancel(true);
        }
      }
      return futures;
    } catch (Throwable t) {
      for (Future<T> e : futures) {
        e.cancel(true);
      }
      throw t;
    }
  }

  public <T> List<Future<T>> invokeAllUninterruptibly(Collection<? extends Callable<T>> tasks) {
    ArrayList<Future<T>> futures = new ArrayList<Future<T>>(tasks.size());
    try {
      for (Callable<T> t : tasks) {
        ForkJoinTask<T> f = ForkJoinTask.adaptInterruptible(t);
        futures.add(f);
        externalSubmit(f);
      }
      for (int i = futures.size() - 1; i >= 0; --i) {
        ((ForkJoinTask<?>) futures.get(i)).quietlyJoin();
      }
      return futures;
    } catch (Throwable t) {
      for (Future<T> e : futures) {
        e.cancel(true);
      }
      throw t;
    }
  }

  public ForkJoinWorkerThreadFactory getFactory() {
    return factory;
  }

  public Thread.UncaughtExceptionHandler getUncaughtExceptionHandler() {
    return ueh;
  }

  public int getParallelism() {
    synchronized (lock) {
      return parallelism;
    }
  }

  public int setParallelism(int size) {
    if (size < 1 || size > MAX_CAP) {
      throw new IllegalArgumentException();
    }
    synchronized (lock) {
      int previous = parallelism;
      parallelism = size;
      lock.notifyAll();
      maybeCreateWorkerLocked();
      return previous;
    }
  }

  public int getPoolSize() {
    synchronized (lock) {
      return workers.size();
    }
  }

  public boolean getAsyncMode() {
    return asyncMode;
  }

  public int getRunningThreadCount() {
    synchronized (lock) {
      return Math.max(0, active - blocked);
    }
  }

  public int getActiveThreadCount() {
    synchronized (lock) {
      return Math.max(0, active);
    }
  }

  public boolean isQuiescent() {
    synchronized (lock) {
      return active == 0 && allEmptyLocked();
    }
  }

  public long getStealCount() {
    synchronized (lock) {
      return steals;
    }
  }

  public long getQueuedTaskCount() {
    long n = 0;
    synchronized (lock) {
      for (WorkQueue q : queues) {
        n += q.deque.size();
      }
    }
    return n;
  }

  public int getQueuedSubmissionCount() {
    synchronized (lock) {
      return submissions.size();
    }
  }

  public boolean hasQueuedSubmissions() {
    synchronized (lock) {
      return !submissions.isEmpty();
    }
  }

  public long getDelayedTaskCount() {
    return delayedCount.get();
  }

  protected ForkJoinTask<?> pollSubmission() {
    synchronized (lock) {
      return submissions.pollFirst();
    }
  }

  protected int drainTasksTo(Collection<? super ForkJoinTask<?>> c) {
    int count = 0;
    synchronized (lock) {
      ForkJoinTask<?> t;
      while ((t = submissions.pollFirst()) != null) {
        c.add(t);
        ++count;
      }
      for (WorkQueue q : queues) {
        while ((t = q.deque.pollFirst()) != null) {
          c.add(t);
          ++count;
        }
      }
    }
    return count;
  }

  @Override
  public String toString() {
    long st;
    long qt;
    int qs;
    int tc;
    int ac;
    int rc;
    int pc;
    String level;
    synchronized (lock) {
      st = steals;
      qt = 0;
      for (WorkQueue q : queues) {
        qt += q.deque.size();
      }
      qs = submissions.size();
      tc = workers.size();
      ac = active;
      rc = Math.max(0, active - blocked);
      pc = parallelism;
      level = terminated ? "Terminated" : stop ? "Terminating" : shutdown ? "Shutting down" : "Running";
    }
    return super.toString() + "[" + level + ", parallelism = " + pc + ", size = " + tc + ", active = " + ac
        + ", running = " + rc + ", steals = " + st + ", tasks = " + qt + ", submissions = " + qs + "]";
  }

  @Override
  public void shutdown() {
    if (isCommon) {
      return;
    }
    synchronized (lock) {
      shutdown = true;
      if (cancelDelayed) {
        cancelDelayedLocked(true);
      } else {
        cancelDelayedLocked(false);
      }
      tryTerminateLocked();
      lock.notifyAll();
    }
  }

  private void cancelDelayedLocked(boolean all) {
    List<ScheduledForkJoinTask<?>> copy = new ArrayList<ScheduledForkJoinTask<?>>(delayedTasks);
    for (ScheduledForkJoinTask<?> t : copy) {
      if (all || t.period != 0L) {
        t.cancel(false);
      }
    }
  }

  public void cancelDelayedTasksOnShutdown() {
    synchronized (lock) {
      cancelDelayed = true;
    }
  }

  @Override
  public List<Runnable> shutdownNow() {
    if (isCommon) {
      return new ArrayList<Runnable>();
    }
    List<ForkJoinTask<?>> drained = new ArrayList<ForkJoinTask<?>>();
    List<ForkJoinWorkerThread> toInterrupt;
    synchronized (lock) {
      shutdown = true;
      stop = true;
      cancelDelayedLocked(true);
      drainTasksTo(drained);
      toInterrupt = new ArrayList<ForkJoinWorkerThread>(workers);
      tryTerminateLocked();
      lock.notifyAll();
    }
    for (ForkJoinTask<?> t : drained) {
      t.cancel(false);
    }
    for (ForkJoinWorkerThread w : toInterrupt) {
      w.interrupt();
    }
    return new ArrayList<Runnable>();
  }

  @Override
  public boolean isShutdown() {
    if (isCommon) {
      return false;
    }
    synchronized (lock) {
      return shutdown;
    }
  }

  @Override
  public boolean isTerminated() {
    if (isCommon) {
      return false;
    }
    synchronized (lock) {
      return terminated;
    }
  }

  public boolean isTerminating() {
    if (isCommon) {
      return false;
    }
    synchronized (lock) {
      return (shutdown || stop) && !terminated;
    }
  }

  @Override
  public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
    if (Thread.interrupted()) {
      throw new InterruptedException();
    }
    if (isCommon) {
      Thread t = Thread.currentThread();
      if (t instanceof ForkJoinWorkerThread) {
        ForkJoinWorkerThread w = (ForkJoinWorkerThread) t;
        w.pool.helpQuiesce(w.workQueue);
      } else {
        awaitQuiescence(timeout, unit);
      }
      return false;
    }
    long deadline = System.nanoTime() + unit.toNanos(timeout);
    synchronized (lock) {
      while (!terminated) {
        long remaining = deadline - System.nanoTime();
        if (remaining <= 0L) {
          return false;
        }
        lockWaiters++;
        try {
          lock.wait(Math.max(1L, remaining / 1000000L));
        } finally {
          lockWaiters--;
        }
      }
      return true;
    }
  }

  public boolean awaitQuiescence(long timeout, TimeUnit unit) {
    long nanos = unit.toNanos(timeout);
    long deadline = System.nanoTime() + nanos;
    if (deadline < 0) {
      deadline = Long.MAX_VALUE;
    }
    Thread t = Thread.currentThread();
    if (t instanceof ForkJoinWorkerThread && ((ForkJoinWorkerThread) t).pool == this) {
      ForkJoinWorkerThread w = (ForkJoinWorkerThread) t;
      helpQuiesce(w.workQueue);
      return true;
    }
    for (;;) {
      ForkJoinTask<?> task;
      synchronized (lock) {
        task = submissions.pollFirst();
        if (task == null) {
          if (active == 0 && allEmptyLocked()) {
            return true;
          }
          long remaining = deadline - System.nanoTime();
          if (remaining <= 0L) {
            return false;
          }
          lockWaiters++;
          try {
            lock.wait(Math.max(1L, Math.min(10L, remaining / 1000000L)));
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
          } finally {
            lockWaiters--;
          }
        }
      }
      if (task != null) {
        task.doExec();
      }
    }
  }

  public static void managedBlock(ManagedBlocker blocker) throws InterruptedException {
    if (blocker == null) {
      throw new NullPointerException();
    }
    while (!blocker.isReleasable() && !blocker.block()) {
      continue;
    }
  }

  @Override
  public void close() {
    if (isCommon) {
      return;
    }
    boolean terminatedNow = isTerminated();
    if (!terminatedNow) {
      shutdown();
      boolean interrupted = false;
      while (!terminatedNow) {
        try {
          terminatedNow = awaitTermination(1L, TimeUnit.DAYS);
        } catch (InterruptedException e) {
          if (!interrupted) {
            shutdownNow();
            interrupted = true;
          }
        }
      }
      if (interrupted) {
        Thread.currentThread().interrupt();
      }
    }
  }

  private ScheduledThreadPoolExecutor delayerExecutor() {
    synchronized (lock) {
      if (delayer == null) {
        ScheduledThreadPoolExecutor ex = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
          public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "ForkJoinPool-delayScheduler");
            t.setDaemon(true);
            return t;
          }
        });
        ex.setRemoveOnCancelPolicy(true);
        delayer = ex;
      }
      return delayer;
    }
  }

  static final class ScheduledForkJoinTask<V> extends ForkJoinTask<V> implements ScheduledFuture<V>, Runnable {
    private static final long serialVersionUID = 1L;

    final ForkJoinPool owner;
    final Callable<? extends V> callable;
    final Runnable runnable;
    final long period;
    volatile long nextTime;
    volatile ScheduledFuture<?> handle;
    V result;

    ScheduledForkJoinTask(ForkJoinPool owner, Callable<? extends V> callable, Runnable runnable, long delayNanos,
        long period) {
      this.owner = owner;
      this.callable = callable;
      this.runnable = runnable;
      this.period = period;
      this.nextTime = System.nanoTime() + delayNanos;
    }

    public V getRawResult() {
      return result;
    }

    protected void setRawResult(V v) {
      result = v;
    }

    void arm() {
      long delay = Math.max(0L, nextTime - System.nanoTime());
      handle = owner.delayerExecutor().schedule(new Runnable() {
        public void run() {
          fire();
        }
      }, delay, TimeUnit.NANOSECONDS);
    }

    void fire() {
      if (isDone()) {
        return;
      }
      try {
        owner.externalPush(this, true);
      } catch (RejectedExecutionException ex) {
        cancel(false);
      }
    }

    protected boolean exec() {
      if (isCancelled()) {
        return true;
      }
      try {
        if (callable != null) {
          result = callable.call();
        } else {
          runnable.run();
        }
      } catch (RuntimeException | Error e) {
        owner.untrack(this);
        throw e;
      } catch (Exception e) {
        owner.untrack(this);
        throw new RuntimeException(e);
      }
      if (period == 0L) {
        setDone();
        owner.untrack(this);
        return true;
      }
      if (period > 0L) {
        nextTime += period;
      } else {
        nextTime = System.nanoTime() - period;
      }
      synchronized (owner.lock) {
        if ((owner.shutdown || owner.stop) && !owner.isCommon) {
          cancel(false);
          return true;
        }
      }
      arm();
      return false;
    }

    public void run() {
      invoke();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
      boolean r = super.cancel(mayInterruptIfRunning);
      ScheduledFuture<?> h = handle;
      if (h != null) {
        h.cancel(false);
      }
      owner.untrack(this);
      return r;
    }

    public long getDelay(TimeUnit unit) {
      return unit.convert(nextTime - System.nanoTime(), TimeUnit.NANOSECONDS);
    }

    public int compareTo(Delayed other) {
      if (other == this) {
        return 0;
      }
      long diff = getDelay(TimeUnit.NANOSECONDS) - other.getDelay(TimeUnit.NANOSECONDS);
      return diff < 0 ? -1 : diff > 0 ? 1 : 0;
    }
  }

  private void track(ScheduledForkJoinTask<?> t) {
    synchronized (lock) {
      delayedTasks.add(t);
    }
    delayedCount.incrementAndGet();
  }

  void untrack(ScheduledForkJoinTask<?> t) {
    boolean removed;
    synchronized (lock) {
      removed = delayedTasks.remove(t);
    }
    if (removed) {
      delayedCount.decrementAndGet();
    }
    synchronized (lock) {
      tryTerminateLocked();
      lock.notifyAll();
    }
  }

  private <V> ScheduledForkJoinTask<V> newScheduled(Callable<? extends V> c, Runnable r, long delay, TimeUnit unit,
      long period) {
    synchronized (lock) {
      if (!isCommon && (shutdown || stop)) {
        throw new RejectedExecutionException();
      }
    }
    ScheduledForkJoinTask<V> task = new ScheduledForkJoinTask<V>(this, c, r, unit.toNanos(Math.max(0L, delay)),
        period);
    track(task);
    task.arm();
    return task;
  }

  @Override
  public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
    if (command == null || unit == null) {
      throw new NullPointerException();
    }
    ScheduledForkJoinTask<Void> t = newScheduled(null, command, delay, unit, 0L);
    return t;
  }

  @Override
  public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
    if (callable == null || unit == null) {
      throw new NullPointerException();
    }
    return newScheduled(callable, null, delay, unit, 0L);
  }

  @Override
  public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
    if (command == null || unit == null) {
      throw new NullPointerException();
    }
    if (period <= 0L) {
      throw new IllegalArgumentException();
    }
    return newScheduled(null, command, initialDelay, unit, unit.toNanos(period));
  }

  @Override
  public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
    if (command == null || unit == null) {
      throw new NullPointerException();
    }
    if (delay <= 0L) {
      throw new IllegalArgumentException();
    }
    return newScheduled(null, command, initialDelay, unit, -unit.toNanos(delay));
  }
}
