/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.concurrent.locks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public abstract class AbstractQueuedLongSynchronizer extends AbstractOwnableSynchronizer implements Serializable {
  private static final long serialVersionUID = 7373984972572414692L;

  private static final class Node {
    final Thread thread;
    final boolean shared;
    boolean signaled;

    Node(Thread thread, boolean shared) {
      this.thread = thread;
      this.shared = shared;
    }
  }

  private final AtomicLong stateCell = new AtomicLong();
  private final Object queueLock = new Object();
  private final LinkedList<Node> queue = new LinkedList<Node>();
  private volatile boolean contended;

  protected AbstractQueuedLongSynchronizer() { }

  protected final long getState() {
    return stateCell.get();
  }

  protected final void setState(long newState) {
    stateCell.set(newState);
  }

  protected final boolean compareAndSetState(long expect, long update) {
    return stateCell.compareAndSet(expect, update);
  }

  protected boolean tryAcquire(long arg) {
    throw new UnsupportedOperationException();
  }

  protected boolean tryRelease(long arg) {
    throw new UnsupportedOperationException();
  }

  protected long tryAcquireShared(long arg) {
    throw new UnsupportedOperationException();
  }

  protected boolean tryReleaseShared(long arg) {
    throw new UnsupportedOperationException();
  }

  protected boolean isHeldExclusively() {
    throw new UnsupportedOperationException();
  }

  private static final int kAcquired = 0;
  private static final int kTimedOut = 1;

  private boolean isFirst(Node node) {
    synchronized (queueLock) {
      return queue.peekFirst() == node;
    }
  }

  private void unparkFirst() {
    Node first;
    synchronized (queueLock) {
      first = queue.peekFirst();
    }
    if (first != null) {
      LockSupport.unpark(first.thread);
    }
  }

  private void leaveQueue(Node node) {
    boolean wasFirst;
    synchronized (queueLock) {
      wasFirst = queue.peekFirst() == node;
      queue.remove(node);
    }
    if (wasFirst) {
      unparkFirst();
    }
  }

  private int doAcquire(long arg, boolean shared, boolean interruptible, boolean timed, long nanos, boolean first)
      throws InterruptedException {
    Node node = new Node(Thread.currentThread(), shared);
    contended = true;
    synchronized (queueLock) {
      queue.addLast(node);
    }
    long deadline = timed ? System.nanoTime() + nanos : 0L;
    boolean interrupted = false;
    boolean done = false;
    try {
      for (;;) {
        if (isFirst(node)) {
          if (shared) {
            long r = tryAcquireShared(arg);
            if (r >= 0) {
              leaveQueue(node);
              done = true;
              Node next;
              synchronized (queueLock) {
                next = queue.peekFirst();
              }
              if (next != null && next.shared) {
                LockSupport.unpark(next.thread);
              }
              if (interrupted) {
                Thread.currentThread().interrupt();
              }
              return kAcquired;
            }
          } else if (tryAcquire(arg)) {
            synchronized (queueLock) {
              queue.remove(node);
            }
            done = true;
            if (interrupted) {
              Thread.currentThread().interrupt();
            }
            return kAcquired;
          }
        }
        if (timed) {
          long remaining = deadline - System.nanoTime();
          if (remaining <= 0) {
            return kTimedOut;
          }
          LockSupport.parkNanos(this, remaining);
        } else {
          LockSupport.park(this);
        }
        if (Thread.interrupted()) {
          if (interruptible) {
            throw new InterruptedException();
          }
          interrupted = true;
        }
      }
    } finally {
      if (!done) {
        leaveQueue(node);
      }
    }
  }

  private void acquireUninterruptibly(long arg, boolean shared) {
    try {
      doAcquire(arg, shared, false, false, 0L, false);
    } catch (InterruptedException impossible) {
      throw new AssertionError(impossible);
    }
  }

  public final void acquire(long arg) {
    if (!tryAcquire(arg)) {
      acquireUninterruptibly(arg, false);
    }
  }

  public final void acquireInterruptibly(long arg) throws InterruptedException {
    if (Thread.interrupted() || (!tryAcquire(arg) && doAcquire(arg, false, true, false, 0L, false) != kAcquired)) {
      throw new InterruptedException();
    }
  }

  public final boolean tryAcquireNanos(long arg, long nanosTimeout) throws InterruptedException {
    if (!Thread.interrupted()) {
      if (tryAcquire(arg)) {
        return true;
      }
      if (nanosTimeout <= 0L) {
        return false;
      }
      int r = doAcquire(arg, false, true, true, nanosTimeout, false);
      if (r == kAcquired) {
        return true;
      }
      if (r == kTimedOut) {
        return false;
      }
    }
    throw new InterruptedException();
  }

  public final boolean release(long arg) {
    if (tryRelease(arg)) {
      unparkFirst();
      return true;
    }
    return false;
  }

  public final void acquireShared(long arg) {
    if (tryAcquireShared(arg) < 0) {
      acquireUninterruptibly(arg, true);
    }
  }

  public final void acquireSharedInterruptibly(long arg) throws InterruptedException {
    if (Thread.interrupted() || (tryAcquireShared(arg) < 0 && doAcquire(arg, true, true, false, 0L,
        false) != kAcquired)) {
      throw new InterruptedException();
    }
  }

  public final boolean tryAcquireSharedNanos(long arg, long nanosTimeout) throws InterruptedException {
    if (!Thread.interrupted()) {
      if (tryAcquireShared(arg) >= 0) {
        return true;
      }
      if (nanosTimeout <= 0L) {
        return false;
      }
      int r = doAcquire(arg, true, true, true, nanosTimeout, false);
      if (r == kAcquired) {
        return true;
      }
      if (r == kTimedOut) {
        return false;
      }
    }
    throw new InterruptedException();
  }

  public final boolean releaseShared(long arg) {
    if (tryReleaseShared(arg)) {
      unparkFirst();
      return true;
    }
    return false;
  }

  public final boolean hasQueuedThreads() {
    synchronized (queueLock) {
      return !queue.isEmpty();
    }
  }

  public final boolean hasContended() {
    return contended;
  }

  public final Thread getFirstQueuedThread() {
    synchronized (queueLock) {
      Node first = queue.peekFirst();
      return first == null ? null : first.thread;
    }
  }

  public final boolean isQueued(Thread thread) {
    if (thread == null) {
      throw new NullPointerException();
    }
    synchronized (queueLock) {
      for (Node node : queue) {
        if (node.thread == thread) {
          return true;
        }
      }
    }
    return false;
  }

  public final boolean hasQueuedPredecessors() {
    synchronized (queueLock) {
      Node first = queue.peekFirst();
      return first != null && first.thread != Thread.currentThread();
    }
  }

  public final int getQueueLength() {
    synchronized (queueLock) {
      return queue.size();
    }
  }

  private Collection<Thread> queuedThreads(int mode) {
    ArrayList<Thread> list = new ArrayList<Thread>();
    synchronized (queueLock) {
      for (Node node : queue) {
        if (mode == 0 || (mode == 1 && !node.shared) || (mode == 2 && node.shared)) {
          list.add(node.thread);
        }
      }
    }
    return list;
  }

  public final Collection<Thread> getQueuedThreads() {
    return queuedThreads(0);
  }

  public final Collection<Thread> getExclusiveQueuedThreads() {
    return queuedThreads(1);
  }

  public final Collection<Thread> getSharedQueuedThreads() {
    return queuedThreads(2);
  }

  public String toString() {
    return super.toString() + "[State = " + getState() + ", " + (hasQueuedThreads() ? "non" : "") + "empty queue]";
  }

  public final boolean owns(ConditionObject condition) {
    return condition.isOwnedBy(this);
  }

  public final boolean hasWaiters(ConditionObject condition) {
    if (!owns(condition)) {
      throw new IllegalArgumentException("Not owner");
    }
    return condition.hasWaiters();
  }

  public final int getWaitQueueLength(ConditionObject condition) {
    if (!owns(condition)) {
      throw new IllegalArgumentException("Not owner");
    }
    return condition.getWaitQueueLength();
  }

  public final Collection<Thread> getWaitingThreads(ConditionObject condition) {
    if (!owns(condition)) {
      throw new IllegalArgumentException("Not owner");
    }
    return condition.getWaitingThreads();
  }

  public class ConditionObject implements Condition, Serializable {
    private static final long serialVersionUID = 1173984872572414699L;

    private final LinkedList<Node> waiters = new LinkedList<Node>();

    public ConditionObject() { }

    private long fullyRelease() {
      long saved = getState();
      if (release(saved)) {
        return saved;
      }
      throw new IllegalMonitorStateException();
    }

    private Node addWaiter() {
      Node node = new Node(Thread.currentThread(), false);
      synchronized (queueLock) {
        waiters.addLast(node);
      }
      return node;
    }

    private boolean removeWaiter(Node node) {
      synchronized (queueLock) {
        return waiters.remove(node);
      }
    }

    private boolean isSignaled(Node node) {
      synchronized (queueLock) {
        return node.signaled;
      }
    }

    public final void signal() {
      if (!isHeldExclusively()) {
        throw new IllegalMonitorStateException();
      }
      Node node;
      synchronized (queueLock) {
        node = waiters.pollFirst();
        if (node != null) {
          node.signaled = true;
        }
      }
      if (node != null) {
        LockSupport.unpark(node.thread);
      }
    }

    public final void signalAll() {
      if (!isHeldExclusively()) {
        throw new IllegalMonitorStateException();
      }
      ArrayList<Node> all = new ArrayList<Node>();
      synchronized (queueLock) {
        Node node;
        while ((node = waiters.pollFirst()) != null) {
          node.signaled = true;
          all.add(node);
        }
      }
      for (Node node : all) {
        LockSupport.unpark(node.thread);
      }
    }

    public final void awaitUninterruptibly() {
      Node node = addWaiter();
      long saved;
      try {
        saved = fullyRelease();
      } catch (RuntimeException e) {
        removeWaiter(node);
        throw e;
      }
      boolean interrupted = false;
      while (!isSignaled(node)) {
        LockSupport.park(this);
        if (Thread.interrupted()) {
          interrupted = true;
        }
      }
      acquireUninterruptibly(saved, false);
      if (interrupted) {
        Thread.currentThread().interrupt();
      }
    }

    private static final int kNone = 0;
    private static final int kInterruptedBefore = 1;
    private static final int kInterruptedAfter = 2;

    private long await(boolean timed, long nanos, boolean[] timedOut) throws InterruptedException {
      if (Thread.interrupted()) {
        throw new InterruptedException();
      }
      Node node = addWaiter();
      long saved;
      try {
        saved = fullyRelease();
      } catch (RuntimeException e) {
        removeWaiter(node);
        throw e;
      }
      long deadline = timed ? System.nanoTime() + nanos : 0L;
      int mode = kNone;
      while (!isSignaled(node)) {
        if (timed) {
          long remaining = deadline - System.nanoTime();
          if (remaining <= 0) {
            if (removeWaiter(node)) {
              timedOut[0] = true;
            }
            break;
          }
          LockSupport.parkNanos(this, remaining);
        } else {
          LockSupport.park(this);
        }
        if (Thread.interrupted()) {
          mode = removeWaiter(node) ? kInterruptedBefore : kInterruptedAfter;
          break;
        }
      }
      acquireUninterruptibly(saved, false);
      if (mode == kInterruptedBefore) {
        throw new InterruptedException();
      } else if (mode == kInterruptedAfter) {
        Thread.currentThread().interrupt();
      }
      return timed ? deadline - System.nanoTime() : 0L;
    }

    public final void await() throws InterruptedException {
      await(false, 0L, new boolean[1]);
    }

    public final long awaitNanos(long nanosTimeout) throws InterruptedException {
      return await(true, nanosTimeout, new boolean[1]);
    }

    public final boolean awaitUntil(Date deadline) throws InterruptedException {
      long abstime = deadline.getTime();
      boolean[] timedOut = new boolean[1];
      await(true, (abstime - System.currentTimeMillis()) * 1000000L, timedOut);
      return !timedOut[0];
    }

    public final boolean await(long time, TimeUnit unit) throws InterruptedException {
      boolean[] timedOut = new boolean[1];
      await(true, unit.toNanos(time), timedOut);
      return !timedOut[0];
    }

    final boolean isOwnedBy(AbstractQueuedLongSynchronizer sync) {
      return sync == AbstractQueuedLongSynchronizer.this;
    }

    protected final boolean hasWaiters() {
      if (!isHeldExclusively()) {
        throw new IllegalMonitorStateException();
      }
      synchronized (queueLock) {
        return !waiters.isEmpty();
      }
    }

    protected final int getWaitQueueLength() {
      if (!isHeldExclusively()) {
        throw new IllegalMonitorStateException();
      }
      synchronized (queueLock) {
        return waiters.size();
      }
    }

    protected final Collection<Thread> getWaitingThreads() {
      if (!isHeldExclusively()) {
        throw new IllegalMonitorStateException();
      }
      ArrayList<Thread> list = new ArrayList<Thread>();
      synchronized (queueLock) {
        for (Node node : waiters) {
          list.add(node.thread);
        }
      }
      return list;
    }
  }
}
