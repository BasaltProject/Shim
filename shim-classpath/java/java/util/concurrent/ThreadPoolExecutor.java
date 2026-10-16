/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.concurrent;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class ThreadPoolExecutor extends AbstractExecutorService {
  private static final int RUNNING = 0;
  private static final int SHUTDOWN = 1;
  private static final int STOP = 2;
  private static final int TIDYING = 3;
  private static final int TERMINATED = 4;

  private static final int MAX_CAPACITY = (1 << 29) - 1;

  private final Object mainLock = new Object();
  private int runState = RUNNING;
  private int workerCount;
  private final HashSet<Worker> workers = new HashSet<>();
  private int largestPoolSize;
  private long completedTaskCount;

  private final BlockingQueue<Runnable> workQueue;
  private volatile ThreadFactory threadFactory;
  private volatile RejectedExecutionHandler handler;
  private volatile long keepAliveTime;
  private volatile boolean allowCoreThreadTimeOut;
  private volatile int corePoolSize;
  private volatile int maximumPoolSize;

  private static final RejectedExecutionHandler defaultHandler = new AbortPolicy();

  private final class Worker implements Runnable {
    final Thread thread;
    Runnable firstTask;
    volatile boolean running;
    long completedTasks;

    Worker(Runnable firstTask) {
      this.firstTask = firstTask;
      this.thread = getThreadFactory().newThread(this);
    }

    public void run() {
      runWorker(this);
    }
  }

  private boolean isRunning() {
    synchronized (mainLock) {
      return runState == RUNNING;
    }
  }

  private void advanceRunState(int targetState) {
    synchronized (mainLock) {
      if (runState < targetState) {
        runState = targetState;
      }
    }
  }

  final void tryTerminate() {
    synchronized (mainLock) {
      if (runState == RUNNING || runState >= TIDYING || (runState == SHUTDOWN && !workQueue.isEmpty())) {
        return;
      }
      if (workerCount != 0) {
        interruptIdleWorkers(true);
        return;
      }
      runState = TIDYING;
      try {
        terminated();
      } finally {
        runState = TERMINATED;
        mainLock.notifyAll();
      }
    }
  }

  private void interruptWorkers() {
    synchronized (mainLock) {
      for (Worker w : workers) {
        try {
          w.thread.interrupt();
        } catch (SecurityException ignore) {
        }
      }
    }
  }

  private void interruptIdleWorkers(boolean onlyOne) {
    synchronized (mainLock) {
      for (Worker w : workers) {
        Thread t = w.thread;
        if (!t.isInterrupted() && !w.running) {
          try {
            t.interrupt();
          } catch (SecurityException ignore) {
          }
        }
        if (onlyOne) {
          break;
        }
      }
    }
  }

  private void interruptIdleWorkers() {
    interruptIdleWorkers(false);
  }

  final void reject(Runnable command) {
    handler.rejectedExecution(command, this);
  }

  void onShutdown() { }

  final boolean isRunningOrShutdown(boolean shutdownOK) {
    synchronized (mainLock) {
      return runState == RUNNING || (runState == SHUTDOWN && shutdownOK);
    }
  }

  private List<Runnable> drainQueue() {
    BlockingQueue<Runnable> q = workQueue;
    ArrayList<Runnable> taskList = new ArrayList<>();
    q.drainTo(taskList);
    if (!q.isEmpty()) {
      for (Runnable r : q.toArray(new Runnable[0])) {
        if (q.remove(r)) {
          taskList.add(r);
        }
      }
    }
    return taskList;
  }

  private boolean addWorker(Runnable firstTask, boolean core) {
    Worker w;
    synchronized (mainLock) {
      int rs = runState;
      if (rs >= SHUTDOWN && !(rs == SHUTDOWN && firstTask == null && !workQueue.isEmpty())) {
        return false;
      }
      if (workerCount >= MAX_CAPACITY || workerCount >= (core ? corePoolSize : maximumPoolSize)) {
        return false;
      }
      w = new Worker(firstTask);
      if (w.thread == null) {
        return false;
      }
      workers.add(w);
      workerCount++;
      int s = workers.size();
      if (s > largestPoolSize) {
        largestPoolSize = s;
      }
    }
    boolean started = false;
    try {
      w.thread.start();
      started = true;
    } finally {
      if (!started) {
        synchronized (mainLock) {
          workers.remove(w);
          workerCount--;
        }
        tryTerminate();
      }
    }
    return true;
  }

  private void processWorkerExit(Worker w, boolean completedAbruptly) {
    synchronized (mainLock) {
      completedTaskCount += w.completedTasks;
      workers.remove(w);
      if (completedAbruptly) {
        workerCount--;
      }
    }
    tryTerminate();
    int rs;
    synchronized (mainLock) {
      rs = runState;
    }
    if (rs < STOP) {
      if (!completedAbruptly) {
        int min = allowCoreThreadTimeOut ? 0 : corePoolSize;
        if (min == 0 && !workQueue.isEmpty()) {
          min = 1;
        }
        int wc;
        synchronized (mainLock) {
          wc = workerCount;
        }
        if (wc >= min) {
          return;
        }
      }
      addWorker(null, false);
    }
  }

  private Runnable getTask() {
    boolean timedOut = false;
    for (;;) {
      int rs;
      int wc;
      boolean decrement = false;
      synchronized (mainLock) {
        rs = runState;
        if (rs >= SHUTDOWN && (rs >= STOP || workQueue.isEmpty())) {
          workerCount--;
          return null;
        }
        wc = workerCount;
        boolean timed = allowCoreThreadTimeOut || wc > corePoolSize;
        if ((wc > maximumPoolSize || (timed && timedOut)) && (wc > 1 || workQueue.isEmpty())) {
          workerCount--;
          return null;
        }
      }
      boolean timed = allowCoreThreadTimeOut || wc > corePoolSize;
      try {
        Runnable r = timed ? workQueue.poll(keepAliveTime, TimeUnit.NANOSECONDS) : workQueue.take();
        if (r != null) {
          return r;
        }
        timedOut = true;
      } catch (InterruptedException retry) {
        timedOut = false;
      }
    }
  }

  final void runWorker(Worker w) {
    Thread wt = Thread.currentThread();
    Runnable task = w.firstTask;
    w.firstTask = null;
    boolean completedAbruptly = true;
    try {
      while (task != null || (task = getTask()) != null) {
        w.running = true;
        boolean stopping;
        synchronized (mainLock) {
          stopping = runState >= STOP;
        }
        if (stopping) {
          if (!wt.isInterrupted()) {
            wt.interrupt();
          }
        } else {
          Thread.interrupted();
        }
        try {
          beforeExecute(wt, task);
          Throwable thrown = null;
          try {
            task.run();
          } catch (RuntimeException x) {
            thrown = x;
            throw x;
          } catch (Error x) {
            thrown = x;
            throw x;
          } catch (Throwable x) {
            thrown = x;
            throw new Error(x);
          } finally {
            afterExecute(task, thrown);
          }
        } finally {
          task = null;
          w.completedTasks++;
          w.running = false;
        }
      }
      completedAbruptly = false;
    } finally {
      processWorkerExit(w, completedAbruptly);
    }
  }

  public ThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                            BlockingQueue<Runnable> workQueue) {
    this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, Executors.defaultThreadFactory(),
         defaultHandler);
  }

  public ThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                            BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
    this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, defaultHandler);
  }

  public ThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                            BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
    this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, Executors.defaultThreadFactory(), handler);
  }

  public ThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                            BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory,
                            RejectedExecutionHandler handler) {
    if (corePoolSize < 0 || maximumPoolSize <= 0 || maximumPoolSize < corePoolSize || keepAliveTime < 0) {
      throw new IllegalArgumentException();
    }
    if (workQueue == null || threadFactory == null || handler == null) {
      throw new NullPointerException();
    }
    this.corePoolSize = corePoolSize;
    this.maximumPoolSize = maximumPoolSize;
    this.workQueue = workQueue;
    this.keepAliveTime = unit.toNanos(keepAliveTime);
    this.threadFactory = threadFactory;
    this.handler = handler;
  }

  public void execute(Runnable command) {
    if (command == null) {
      throw new NullPointerException();
    }
    int wc;
    synchronized (mainLock) {
      wc = workerCount;
    }
    if (wc < corePoolSize) {
      if (addWorker(command, true)) {
        return;
      }
    }
    if (isRunning() && workQueue.offer(command)) {
      boolean running = isRunning();
      if (!running && remove(command)) {
        reject(command);
      } else {
        synchronized (mainLock) {
          wc = workerCount;
        }
        if (wc == 0) {
          addWorker(null, false);
        }
      }
    } else if (!addWorker(command, false)) {
      reject(command);
    }
  }

  public void shutdown() {
    synchronized (mainLock) {
      advanceRunState(SHUTDOWN);
      interruptIdleWorkers();
      onShutdown();
    }
    tryTerminate();
  }

  public List<Runnable> shutdownNow() {
    List<Runnable> tasks;
    synchronized (mainLock) {
      advanceRunState(STOP);
      interruptWorkers();
      tasks = drainQueue();
    }
    tryTerminate();
    return tasks;
  }

  public boolean isShutdown() {
    synchronized (mainLock) {
      return runState >= SHUTDOWN;
    }
  }

  boolean isStopped() {
    synchronized (mainLock) {
      return runState >= STOP;
    }
  }

  public boolean isTerminating() {
    synchronized (mainLock) {
      return runState >= SHUTDOWN && runState < TERMINATED;
    }
  }

  public boolean isTerminated() {
    synchronized (mainLock) {
      return runState >= TERMINATED;
    }
  }

  public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
    long nanos = unit.toNanos(timeout);
    long deadline = System.nanoTime() + nanos;
    synchronized (mainLock) {
      while (runState < TERMINATED) {
        if (nanos <= 0L) {
          return false;
        }
        mainLock.wait(nanos / 1000000L, (int) (nanos % 1000000L));
        nanos = deadline - System.nanoTime();
      }
      return true;
    }
  }

  public void setThreadFactory(ThreadFactory threadFactory) {
    if (threadFactory == null) {
      throw new NullPointerException();
    }
    this.threadFactory = threadFactory;
  }

  public ThreadFactory getThreadFactory() {
    return threadFactory;
  }

  public void setRejectedExecutionHandler(RejectedExecutionHandler handler) {
    if (handler == null) {
      throw new NullPointerException();
    }
    this.handler = handler;
  }

  public RejectedExecutionHandler getRejectedExecutionHandler() {
    return handler;
  }

  public void setCorePoolSize(int corePoolSize) {
    if (corePoolSize < 0 || maximumPoolSize < corePoolSize) {
      throw new IllegalArgumentException();
    }
    int delta = corePoolSize - this.corePoolSize;
    this.corePoolSize = corePoolSize;
    int wc;
    synchronized (mainLock) {
      wc = workerCount;
    }
    if (wc > corePoolSize) {
      interruptIdleWorkers();
    } else if (delta > 0) {
      int k = Math.min(delta, workQueue.size());
      while (k-- > 0 && addWorker(null, true)) {
        if (workQueue.isEmpty()) {
          break;
        }
      }
    }
  }

  public int getCorePoolSize() {
    return corePoolSize;
  }

  public boolean prestartCoreThread() {
    int wc;
    synchronized (mainLock) {
      wc = workerCount;
    }
    return wc < corePoolSize && addWorker(null, true);
  }

  void ensurePrestart() {
    int wc;
    synchronized (mainLock) {
      wc = workerCount;
    }
    if (wc < corePoolSize) {
      addWorker(null, true);
    } else if (wc == 0) {
      addWorker(null, false);
    }
  }

  public int prestartAllCoreThreads() {
    int n = 0;
    while (addWorker(null, true)) {
      ++n;
    }
    return n;
  }

  public boolean allowsCoreThreadTimeOut() {
    return allowCoreThreadTimeOut;
  }

  public void allowCoreThreadTimeOut(boolean value) {
    if (value && keepAliveTime <= 0) {
      throw new IllegalArgumentException("Core threads must have nonzero keep alive times");
    }
    if (value != allowCoreThreadTimeOut) {
      allowCoreThreadTimeOut = value;
      if (value) {
        interruptIdleWorkers();
      }
    }
  }

  public void setMaximumPoolSize(int maximumPoolSize) {
    if (maximumPoolSize <= 0 || maximumPoolSize < corePoolSize) {
      throw new IllegalArgumentException();
    }
    this.maximumPoolSize = maximumPoolSize;
    int wc;
    synchronized (mainLock) {
      wc = workerCount;
    }
    if (wc > maximumPoolSize) {
      interruptIdleWorkers();
    }
  }

  public int getMaximumPoolSize() {
    return maximumPoolSize;
  }

  public void setKeepAliveTime(long time, TimeUnit unit) {
    if (time < 0) {
      throw new IllegalArgumentException();
    }
    if (time == 0 && allowsCoreThreadTimeOut()) {
      throw new IllegalArgumentException("Core threads must have nonzero keep alive times");
    }
    long keepAliveTime = unit.toNanos(time);
    long delta = keepAliveTime - this.keepAliveTime;
    this.keepAliveTime = keepAliveTime;
    if (delta < 0) {
      interruptIdleWorkers();
    }
  }

  public long getKeepAliveTime(TimeUnit unit) {
    return unit.convert(keepAliveTime, TimeUnit.NANOSECONDS);
  }

  public BlockingQueue<Runnable> getQueue() {
    return workQueue;
  }

  public boolean remove(Runnable task) {
    boolean removed = workQueue.remove(task);
    tryTerminate();
    return removed;
  }

  public void purge() {
    final BlockingQueue<Runnable> q = workQueue;
    try {
      Iterator<Runnable> it = q.iterator();
      while (it.hasNext()) {
        Runnable r = it.next();
        if (r instanceof Future<?> && ((Future<?>) r).isCancelled()) {
          it.remove();
        }
      }
    } catch (ConcurrentModificationException fallThrough) {
      for (Object r : q.toArray()) {
        if (r instanceof Future<?> && ((Future<?>) r).isCancelled()) {
          q.remove(r);
        }
      }
    }
    tryTerminate();
  }

  public int getPoolSize() {
    synchronized (mainLock) {
      return runState >= TIDYING ? 0 : workers.size();
    }
  }

  public int getActiveCount() {
    synchronized (mainLock) {
      int n = 0;
      for (Worker w : workers) {
        if (w.running) {
          ++n;
        }
      }
      return n;
    }
  }

  public int getLargestPoolSize() {
    synchronized (mainLock) {
      return largestPoolSize;
    }
  }

  public long getTaskCount() {
    synchronized (mainLock) {
      long n = completedTaskCount;
      for (Worker w : workers) {
        n += w.completedTasks;
        if (w.running) {
          ++n;
        }
      }
      return n + workQueue.size();
    }
  }

  public long getCompletedTaskCount() {
    synchronized (mainLock) {
      long n = completedTaskCount;
      for (Worker w : workers) {
        n += w.completedTasks;
      }
      return n;
    }
  }

  public String toString() {
    long ncompleted;
    int nworkers, nactive;
    synchronized (mainLock) {
      ncompleted = completedTaskCount;
      nactive = 0;
      nworkers = workers.size();
      for (Worker w : workers) {
        ncompleted += w.completedTasks;
        if (w.running) {
          ++nactive;
        }
      }
    }
    String runState;
    synchronized (mainLock) {
      int c = this.runState;
      runState = c == RUNNING ? "Running" : (c >= TERMINATED ? "Terminated" : "Shutting down");
    }
    return super.toString() + "[" + runState + ", pool size = " + nworkers + ", active threads = " + nactive
        + ", queued tasks = " + workQueue.size() + ", completed tasks = " + ncompleted + "]";
  }

  protected void beforeExecute(Thread t, Runnable r) { }

  protected void afterExecute(Runnable r, Throwable t) { }

  protected void terminated() { }

  public static class CallerRunsPolicy implements RejectedExecutionHandler {
    public CallerRunsPolicy() { }

    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
      if (!e.isShutdown()) {
        r.run();
      }
    }
  }

  public static class AbortPolicy implements RejectedExecutionHandler {
    public AbortPolicy() { }

    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
      throw new RejectedExecutionException("Task " + r.toString() + " rejected from " + e.toString());
    }
  }

  public static class DiscardPolicy implements RejectedExecutionHandler {
    public DiscardPolicy() { }

    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) { }
  }

  public static class DiscardOldestPolicy implements RejectedExecutionHandler {
    public DiscardOldestPolicy() { }

    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
      if (!e.isShutdown()) {
        e.getQueue().poll();
        e.execute(r);
      }
    }
  }
}
