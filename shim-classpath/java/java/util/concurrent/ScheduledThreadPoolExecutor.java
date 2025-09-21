/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util.concurrent;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

public class ScheduledThreadPoolExecutor extends ThreadPoolExecutor implements ScheduledExecutorService {
  private volatile boolean continueExistingPeriodicTasksAfterShutdown;
  private volatile boolean executeExistingDelayedTasksAfterShutdown = true;
  volatile boolean removeOnCancel;

  private static final java.util.concurrent.atomic.AtomicLong sequencer = new java.util.concurrent.atomic.AtomicLong();

  final long now() {
    return System.nanoTime();
  }

  private class ScheduledFutureTask<V> extends FutureTask<V> implements RunnableScheduledFuture<V> {
    private final long sequenceNumber;
    private volatile long time;
    private final long period;
    RunnableScheduledFuture<V> outerTask = this;
    int heapIndex;

    ScheduledFutureTask(Runnable r, V result, long triggerTime, long period, long sequenceNumber) {
      super(r, result);
      this.time = triggerTime;
      this.period = period;
      this.sequenceNumber = sequenceNumber;
    }

    ScheduledFutureTask(Callable<V> callable, long triggerTime, long sequenceNumber) {
      super(callable);
      this.time = triggerTime;
      this.period = 0;
      this.sequenceNumber = sequenceNumber;
    }

    public long getDelay(TimeUnit unit) {
      return unit.convert(time - now(), TimeUnit.NANOSECONDS);
    }

    public int compareTo(Delayed other) {
      if (other == this) {
        return 0;
      }
      if (other instanceof ScheduledFutureTask) {
        ScheduledFutureTask<?> x = (ScheduledFutureTask<?>) other;
        long diff = time - x.time;
        if (diff < 0) {
          return -1;
        } else if (diff > 0) {
          return 1;
        } else if (sequenceNumber < x.sequenceNumber) {
          return -1;
        } else {
          return 1;
        }
      }
      long diff = getDelay(TimeUnit.NANOSECONDS) - other.getDelay(TimeUnit.NANOSECONDS);
      return (diff < 0) ? -1 : (diff > 0) ? 1 : 0;
    }

    public boolean isPeriodic() {
      return period != 0;
    }

    private void setNextRunTime() {
      long p = period;
      if (p > 0) {
        time += p;
      } else {
        time = triggerTime(-p);
      }
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
      boolean cancelled = super.cancel(mayInterruptIfRunning);
      if (cancelled && removeOnCancel && heapIndex >= 0) {
        remove(this);
      }
      return cancelled;
    }

    public void run() {
      if (!canRunInCurrentRunState(this)) {
        cancel(false);
      } else if (!isPeriodic()) {
        super.run();
      } else if (super.runAndReset()) {
        setNextRunTime();
        reExecutePeriodic(outerTask);
      }
    }
  }

  boolean canRunInCurrentRunState(RunnableScheduledFuture<?> task) {
    if (!isShutdown()) {
      return true;
    }
    if (isStopped()) {
      return false;
    }
    return task.isPeriodic() ? continueExistingPeriodicTasksAfterShutdown
                             : (executeExistingDelayedTasksAfterShutdown || task.getDelay(TimeUnit.NANOSECONDS) <= 0);
  }

  private void delayedExecute(RunnableScheduledFuture<?> task) {
    if (isShutdown()) {
      reject(task);
    } else {
      super.getQueue().add(task);
      if (!canRunInCurrentRunState(task) && remove(task)) {
        task.cancel(false);
      } else {
        ensurePrestart();
      }
    }
  }

  void reExecutePeriodic(RunnableScheduledFuture<?> task) {
    if (canRunInCurrentRunState(task)) {
      super.getQueue().add(task);
      if (canRunInCurrentRunState(task) || !remove(task)) {
        ensurePrestart();
        return;
      }
    }
    task.cancel(false);
  }

  void onShutdown() {
    BlockingQueue<Runnable> q = super.getQueue();
    boolean keepDelayed = getExecuteExistingDelayedTasksAfterShutdownPolicy();
    boolean keepPeriodic = getContinueExistingPeriodicTasksAfterShutdownPolicy();
    for (Object e : q.toArray()) {
      if (e instanceof RunnableScheduledFuture) {
        RunnableScheduledFuture<?> t = (RunnableScheduledFuture<?>) e;
        if ((t.isPeriodic() ? !keepPeriodic : (!keepDelayed && t.getDelay(TimeUnit.NANOSECONDS) > 0))
            || t.isCancelled()) {
          if (q.remove(t)) {
            t.cancel(false);
          }
        }
      }
    }
    tryTerminate();
  }

  protected <V> RunnableScheduledFuture<V> decorateTask(Runnable runnable, RunnableScheduledFuture<V> task) {
    return task;
  }

  protected <V> RunnableScheduledFuture<V> decorateTask(Callable<V> callable, RunnableScheduledFuture<V> task) {
    return task;
  }

  private static final long DEFAULT_KEEPALIVE_MILLIS = 10L;

  public ScheduledThreadPoolExecutor(int corePoolSize) {
    super(corePoolSize, Integer.MAX_VALUE, DEFAULT_KEEPALIVE_MILLIS, TimeUnit.MILLISECONDS, new DelayedWorkQueue());
  }

  public ScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory) {
    super(corePoolSize, Integer.MAX_VALUE, DEFAULT_KEEPALIVE_MILLIS, TimeUnit.MILLISECONDS, new DelayedWorkQueue(),
          threadFactory);
  }

  public ScheduledThreadPoolExecutor(int corePoolSize, RejectedExecutionHandler handler) {
    super(corePoolSize, Integer.MAX_VALUE, DEFAULT_KEEPALIVE_MILLIS, TimeUnit.MILLISECONDS, new DelayedWorkQueue(),
          handler);
  }

  public ScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
    super(corePoolSize, Integer.MAX_VALUE, DEFAULT_KEEPALIVE_MILLIS, TimeUnit.MILLISECONDS, new DelayedWorkQueue(),
          threadFactory, handler);
  }

  private long triggerTime(long delay, TimeUnit unit) {
    return triggerTime(unit.toNanos((delay < 0) ? 0 : delay));
  }

  long triggerTime(long delay) {
    return now() + ((delay < (Long.MAX_VALUE >> 1)) ? delay : overflowFree(delay));
  }

  private long overflowFree(long delay) {
    Delayed head = (Delayed) super.getQueue().peek();
    if (head != null) {
      long headDelay = head.getDelay(TimeUnit.NANOSECONDS);
      if (headDelay < 0 && (delay - headDelay < 0)) {
        delay = Long.MAX_VALUE + headDelay;
      }
    }
    return delay;
  }

  public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
    if (command == null || unit == null) {
      throw new NullPointerException();
    }
    RunnableScheduledFuture<Void> t = decorateTask(command, new ScheduledFutureTask<Void>(
        command, null, triggerTime(delay, unit), 0, sequencer.getAndIncrement()));
    delayedExecute(t);
    return t;
  }

  public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
    if (callable == null || unit == null) {
      throw new NullPointerException();
    }
    RunnableScheduledFuture<V> t = decorateTask(callable, new ScheduledFutureTask<V>(
        callable, triggerTime(delay, unit), sequencer.getAndIncrement()));
    delayedExecute(t);
    return t;
  }

  public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
    if (command == null || unit == null) {
      throw new NullPointerException();
    }
    if (period <= 0L) {
      throw new IllegalArgumentException();
    }
    ScheduledFutureTask<Void> sft = new ScheduledFutureTask<Void>(
        command, null, triggerTime(initialDelay, unit), unit.toNanos(period), sequencer.getAndIncrement());
    RunnableScheduledFuture<Void> t = decorateTask(command, sft);
    sft.outerTask = t;
    delayedExecute(t);
    return t;
  }

  public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
    if (command == null || unit == null) {
      throw new NullPointerException();
    }
    if (delay <= 0L) {
      throw new IllegalArgumentException();
    }
    ScheduledFutureTask<Void> sft = new ScheduledFutureTask<Void>(
        command, null, triggerTime(initialDelay, unit), -unit.toNanos(delay), sequencer.getAndIncrement());
    RunnableScheduledFuture<Void> t = decorateTask(command, sft);
    sft.outerTask = t;
    delayedExecute(t);
    return t;
  }

  public void execute(Runnable command) {
    schedule(command, 0, TimeUnit.NANOSECONDS);
  }

  public Future<?> submit(Runnable task) {
    return schedule(task, 0, TimeUnit.NANOSECONDS);
  }

  public <T> Future<T> submit(Runnable task, T result) {
    return schedule(Executors.callable(task, result), 0, TimeUnit.NANOSECONDS);
  }

  public <T> Future<T> submit(Callable<T> task) {
    return schedule(task, 0, TimeUnit.NANOSECONDS);
  }

  public void setContinueExistingPeriodicTasksAfterShutdownPolicy(boolean value) {
    continueExistingPeriodicTasksAfterShutdown = value;
    if (!value && isShutdown()) {
      onShutdown();
    }
  }

  public boolean getContinueExistingPeriodicTasksAfterShutdownPolicy() {
    return continueExistingPeriodicTasksAfterShutdown;
  }

  public void setExecuteExistingDelayedTasksAfterShutdownPolicy(boolean value) {
    executeExistingDelayedTasksAfterShutdown = value;
    if (!value && isShutdown()) {
      onShutdown();
    }
  }

  public boolean getExecuteExistingDelayedTasksAfterShutdownPolicy() {
    return executeExistingDelayedTasksAfterShutdown;
  }

  public void setRemoveOnCancelPolicy(boolean value) {
    removeOnCancel = value;
  }

  public boolean getRemoveOnCancelPolicy() {
    return removeOnCancel;
  }

  public void shutdown() {
    super.shutdown();
  }

  public List<Runnable> shutdownNow() {
    return super.shutdownNow();
  }

  public BlockingQueue<Runnable> getQueue() {
    return super.getQueue();
  }

  static class DelayedWorkQueue extends AbstractQueue<Runnable> implements BlockingQueue<Runnable> {
    private static final int INITIAL_CAPACITY = 16;
    private RunnableScheduledFuture<?>[] queue = new RunnableScheduledFuture<?>[INITIAL_CAPACITY];
    private final Object lock = new Object();
    private int size;

    private void setIndex(RunnableScheduledFuture<?> f, int idx) {
      if (f instanceof ScheduledFutureTask) {
        ((ScheduledFutureTask<?>) f).heapIndex = idx;
      }
    }

    private void siftUp(int k, RunnableScheduledFuture<?> key) {
      while (k > 0) {
        int parent = (k - 1) >>> 1;
        RunnableScheduledFuture<?> e = queue[parent];
        if (key.compareTo(e) >= 0) {
          break;
        }
        queue[k] = e;
        setIndex(e, k);
        k = parent;
      }
      queue[k] = key;
      setIndex(key, k);
    }

    private void siftDown(int k, RunnableScheduledFuture<?> key) {
      int half = size >>> 1;
      while (k < half) {
        int child = (k << 1) + 1;
        RunnableScheduledFuture<?> c = queue[child];
        int right = child + 1;
        if (right < size && c.compareTo(queue[right]) > 0) {
          c = queue[child = right];
        }
        if (key.compareTo(c) <= 0) {
          break;
        }
        queue[k] = c;
        setIndex(c, k);
        k = child;
      }
      queue[k] = key;
      setIndex(key, k);
    }

    private void grow() {
      int oldCapacity = queue.length;
      int newCapacity = oldCapacity + (oldCapacity >> 1);
      if (newCapacity < 0) {
        newCapacity = Integer.MAX_VALUE;
      }
      queue = Arrays.copyOf(queue, newCapacity);
    }

    private int indexOf(Object x) {
      if (x != null) {
        if (x instanceof ScheduledFutureTask) {
          int i = ((ScheduledFutureTask<?>) x).heapIndex;
          if (i >= 0 && i < size && queue[i] == x) {
            return i;
          }
        } else {
          for (int i = 0; i < size; i++) {
            if (x.equals(queue[i])) {
              return i;
            }
          }
        }
      }
      return -1;
    }

    public boolean contains(Object x) {
      synchronized (lock) {
        return indexOf(x) != -1;
      }
    }

    public boolean remove(Object x) {
      synchronized (lock) {
        int i = indexOf(x);
        if (i < 0) {
          return false;
        }
        setIndex(queue[i], -1);
        int s = --size;
        RunnableScheduledFuture<?> replacement = queue[s];
        queue[s] = null;
        if (s != i) {
          siftDown(i, replacement);
          if (queue[i] == replacement) {
            siftUp(i, replacement);
          }
        }
        return true;
      }
    }

    public int size() {
      synchronized (lock) {
        return size;
      }
    }

    public boolean isEmpty() {
      return size() == 0;
    }

    public int remainingCapacity() {
      return Integer.MAX_VALUE;
    }

    public RunnableScheduledFuture<?> peek() {
      synchronized (lock) {
        return queue[0];
      }
    }

    public boolean offer(Runnable x) {
      if (x == null) {
        throw new NullPointerException();
      }
      RunnableScheduledFuture<?> e = (RunnableScheduledFuture<?>) x;
      synchronized (lock) {
        int i = size;
        if (i >= queue.length) {
          grow();
        }
        size = i + 1;
        if (i == 0) {
          queue[0] = e;
          setIndex(e, 0);
        } else {
          siftUp(i, e);
        }
        if (queue[0] == e) {
          lock.notifyAll();
        }
      }
      return true;
    }

    public void put(Runnable e) {
      offer(e);
    }

    public boolean add(Runnable e) {
      return offer(e);
    }

    public boolean offer(Runnable e, long timeout, TimeUnit unit) {
      return offer(e);
    }

    private RunnableScheduledFuture<?> finishPoll(RunnableScheduledFuture<?> f) {
      int s = --size;
      RunnableScheduledFuture<?> x = queue[s];
      queue[s] = null;
      if (s != 0) {
        siftDown(0, x);
      }
      setIndex(f, -1);
      return f;
    }

    public RunnableScheduledFuture<?> poll() {
      synchronized (lock) {
        RunnableScheduledFuture<?> first = queue[0];
        return (first == null || first.getDelay(TimeUnit.NANOSECONDS) > 0) ? null : finishPoll(first);
      }
    }

    public RunnableScheduledFuture<?> take() throws InterruptedException {
      synchronized (lock) {
        for (;;) {
          RunnableScheduledFuture<?> first = queue[0];
          if (first == null) {
            lock.wait();
          } else {
            long delay = first.getDelay(TimeUnit.NANOSECONDS);
            if (delay <= 0L) {
              return finishPoll(first);
            }
            lock.wait(delay / 1000000L, (int) (delay % 1000000L));
          }
        }
      }
    }

    public RunnableScheduledFuture<?> poll(long timeout, TimeUnit unit) throws InterruptedException {
      long nanos = unit.toNanos(timeout);
      long deadline = System.nanoTime() + nanos;
      synchronized (lock) {
        for (;;) {
          RunnableScheduledFuture<?> first = queue[0];
          if (first == null) {
            if (nanos <= 0L) {
              return null;
            }
            lock.wait(nanos / 1000000L, (int) (nanos % 1000000L));
          } else {
            long delay = first.getDelay(TimeUnit.NANOSECONDS);
            if (delay <= 0L) {
              return finishPoll(first);
            }
            if (nanos <= 0L) {
              return null;
            }
            long wait = Math.min(delay, nanos);
            lock.wait(wait / 1000000L, (int) (wait % 1000000L));
          }
          nanos = deadline - System.nanoTime();
        }
      }
    }

    public void clear() {
      synchronized (lock) {
        for (int i = 0; i < size; i++) {
          RunnableScheduledFuture<?> t = queue[i];
          if (t != null) {
            queue[i] = null;
            setIndex(t, -1);
          }
        }
        size = 0;
      }
    }

    public int drainTo(Collection<? super Runnable> c) {
      return drainTo(c, Integer.MAX_VALUE);
    }

    public int drainTo(Collection<? super Runnable> c, int maxElements) {
      Objects.requireNonNull(c);
      if (c == this) {
        throw new IllegalArgumentException();
      }
      if (maxElements <= 0) {
        return 0;
      }
      synchronized (lock) {
        int n = 0;
        for (RunnableScheduledFuture<?> first;
             n < maxElements && (first = queue[0]) != null && first.getDelay(TimeUnit.NANOSECONDS) <= 0;) {
          c.add(first);
          finishPoll(first);
          ++n;
        }
        return n;
      }
    }

    public Object[] toArray() {
      synchronized (lock) {
        return Arrays.copyOf(queue, size, Object[].class);
      }
    }

    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
      synchronized (lock) {
        if (a.length < size) {
          return (T[]) Arrays.copyOf(queue, size, a.getClass());
        }
        System.arraycopy(queue, 0, a, 0, size);
        if (a.length > size) {
          a[size] = null;
        }
        return a;
      }
    }

    public Iterator<Runnable> iterator() {
      synchronized (lock) {
        return new Itr(Arrays.copyOf(queue, size));
      }
    }

    private class Itr implements Iterator<Runnable> {
      final RunnableScheduledFuture<?>[] array;
      int cursor;
      int lastRet = -1;

      Itr(RunnableScheduledFuture<?>[] array) {
        this.array = array;
      }

      public boolean hasNext() {
        return cursor < array.length;
      }

      public Runnable next() {
        if (cursor >= array.length) {
          throw new NoSuchElementException();
        }
        return array[lastRet = cursor++];
      }

      public void remove() {
        if (lastRet < 0) {
          throw new IllegalStateException();
        }
        DelayedWorkQueue.this.remove(array[lastRet]);
        lastRet = -1;
      }
    }
  }
}
