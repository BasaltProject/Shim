/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.concurrent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPoolExecutor extends AbstractExecutorService {
  private volatile int corePoolSize;
  private volatile int maximumPoolSize;
  private volatile long keepAliveTime;
  private volatile TimeUnit unit;
  private final BlockingQueue<Runnable> workQueue;
  private volatile ThreadFactory threadFactory;
  private volatile RejectedExecutionHandler handler;
  private volatile boolean isShutdown;
  private final List<Thread> workers = new ArrayList<Thread>();
  private final Set<Thread> idleWorkers = new HashSet<Thread>();
  private final AtomicInteger activeCount = new AtomicInteger();

  public static class AbortPolicy implements RejectedExecutionHandler {
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
      throw new RejectedExecutionException("Task " + r + " rejected from " + e);
    }
  }

  public static class CallerRunsPolicy implements RejectedExecutionHandler {
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
      if (!e.isShutdown()) {
        r.run();
      }
    }
  }

  public static class DiscardPolicy implements RejectedExecutionHandler {
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
    }
  }

  public static class DiscardOldestPolicy implements RejectedExecutionHandler {
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
      if (!e.isShutdown()) {
        e.getQueue().poll();
        e.execute(r);
      }
    }
  }

  private static final RejectedExecutionHandler defaultHandler = new AbortPolicy();

  public ThreadPoolExecutor(
      int corePoolSize,
      int maximumPoolSize,
      long keepAliveTime,
      TimeUnit unit,
      BlockingQueue<Runnable> workQueue) {
    this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
         Executors.defaultThreadFactory(), defaultHandler);
  }

  public ThreadPoolExecutor(
      int corePoolSize,
      int maximumPoolSize,
      long keepAliveTime,
      TimeUnit unit,
      BlockingQueue<Runnable> workQueue,
      ThreadFactory threadFactory) {
    this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
         threadFactory, defaultHandler);
  }

  public ThreadPoolExecutor(
      int corePoolSize,
      int maximumPoolSize,
      long keepAliveTime,
      TimeUnit unit,
      BlockingQueue<Runnable> workQueue,
      RejectedExecutionHandler handler) {
    this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
         Executors.defaultThreadFactory(), handler);
  }

  public ThreadPoolExecutor(
      int corePoolSize,
      int maximumPoolSize,
      long keepAliveTime,
      TimeUnit unit,
      BlockingQueue<Runnable> workQueue,
      ThreadFactory threadFactory,
      RejectedExecutionHandler handler) {
    if (corePoolSize < 0 || maximumPoolSize <= 0 || maximumPoolSize < corePoolSize || keepAliveTime < 0) {
      throw new IllegalArgumentException();
    }
    if (workQueue == null || threadFactory == null || handler == null) {
      throw new NullPointerException();
    }
    this.corePoolSize = corePoolSize;
    this.maximumPoolSize = maximumPoolSize;
    this.keepAliveTime = keepAliveTime;
    this.unit = unit;
    this.workQueue = workQueue;
    this.threadFactory = threadFactory;
    this.handler = handler;
  }

  public BlockingQueue<Runnable> getQueue() {
    return workQueue;
  }

  public int getCorePoolSize() {
    return corePoolSize;
  }

  public int getMaximumPoolSize() {
    return maximumPoolSize;
  }

  public int getPoolSize() {
    synchronized (workers) {
      return workers.size();
    }
  }

  public int getActiveCount() {
    return activeCount.get();
  }

  private void addWorker(Runnable firstTask) {
    synchronized (workers) {
      if (workers.size() >= maximumPoolSize) {
        return;
      }
      Thread t = threadFactory.newThread(new Runnable() {
        @Override
        public void run() {
          Runnable task = firstTask;
          while (!isShutdown || !workQueue.isEmpty()) {
            if (task == null) {
              synchronized (idleWorkers) {
                idleWorkers.add(Thread.currentThread());
              }
              try {
                task = workQueue.poll(keepAliveTime > 0 ? keepAliveTime : 100,
                                      unit != null ? unit : TimeUnit.MILLISECONDS);
              } catch (InterruptedException e) {
                if (isShutdown) break;
              } finally {
                synchronized (idleWorkers) {
                  idleWorkers.remove(Thread.currentThread());
                }
              }
            }
            if (task != null) {
              activeCount.incrementAndGet();
              try {
                task.run();
              } catch (Throwable ignore) {
              } finally {
                activeCount.decrementAndGet();
                task = null;
              }
            } else if (workers.size() > corePoolSize) {
              break;
            }
          }
          synchronized (workers) {
            workers.remove(Thread.currentThread());
          }
          synchronized (idleWorkers) {
            idleWorkers.remove(Thread.currentThread());
          }
        }
      });
      if (t != null) {
        workers.add(t);
        t.start();
      }
    }
  }

  @Override
  public void execute(Runnable command) {
    if (command == null) throw new NullPointerException();
    if (isShutdown) {
      handler.rejectedExecution(command, this);
      return;
    }
    synchronized (workers) {
      if (workers.size() < corePoolSize) {
        addWorker(command);
        return;
      }
    }
    if (workQueue.offer(command)) {
      synchronized (workers) {
        if (workers.isEmpty()) {
          addWorker(null);
        }
      }
    } else {
      synchronized (workers) {
        if (workers.size() < maximumPoolSize) {
          addWorker(command);
          return;
        }
      }
      handler.rejectedExecution(command, this);
    }
  }

  @Override
  public void shutdown() {
    isShutdown = true;
    synchronized (idleWorkers) {
      for (Thread w : idleWorkers) {
        w.interrupt();
      }
    }
  }

  @Override
  public List<Runnable> shutdownNow() {
    isShutdown = true;
    List<Runnable> tasks = new ArrayList<Runnable>();
    workQueue.drainTo(tasks);
    synchronized (workers) {
      for (Thread w : workers) {
        w.interrupt();
      }
    }
    return tasks;
  }

  @Override
  public boolean isShutdown() {
    return isShutdown;
  }

  @Override
  public boolean isTerminated() {
    synchronized (workers) {
      return isShutdown && workQueue.isEmpty() && workers.isEmpty();
    }
  }

  @Override
  public boolean awaitTermination(long timeout, TimeUnit unit)
      throws InterruptedException {
    long deadline = System.currentTimeMillis() + unit.toMillis(timeout);
    while (!isTerminated()) {
      long rem = deadline - System.currentTimeMillis();
      if (rem <= 0) return false;
      Thread.sleep(Math.min(rem, 50));
    }
    return true;
  }
}
