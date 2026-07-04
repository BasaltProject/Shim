/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

import java.awt.event.InvocationEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.EmptyStackException;
import java.util.LinkedList;

public class EventQueue {
  private static final EventQueue kSystem = new EventQueue();
  private static AWTEvent currentEvent;
  private static long mostRecentEventTime = System.currentTimeMillis();

  private final LinkedList<AWTEvent> queue = new LinkedList<>();
  private EventQueue previous;
  private EventQueue next;
  private Thread dispatchThread;

  public EventQueue() { }

  public void postEvent(AWTEvent theEvent) {
    synchronized (this) {
      if (next != null) {
        next.postEvent(theEvent);
        return;
      }
      queue.addLast(theEvent);
      notifyAll();
      if (dispatchThread == null) {
        startDispatchThread();
      }
    }
  }

  private void startDispatchThread() {
    final EventQueue self = this;
    Thread t = new Thread(new Runnable() {
      public void run() {
        self.dispatchLoop();
      }
    }, "AWT-EventQueue-0");
    t.setDaemon(false);
    dispatchThread = t;
    t.start();
  }

  private void dispatchLoop() {
    while (true) {
      AWTEvent e;
      synchronized (this) {
        if (queue.isEmpty()) {
          try {
            wait(1000);
          } catch (InterruptedException ie) {
            // ignore
          }
          if (queue.isEmpty()) {
            dispatchThread = null;
            return;
          }
        }
        e = queue.removeFirst();
      }
      try {
        currentEvent = e;
        mostRecentEventTime = System.currentTimeMillis();
        dispatchEvent(e);
      } catch (Throwable t) {
        Thread th = Thread.currentThread();
        th.getUncaughtExceptionHandler().uncaughtException(th, t);
      } finally {
        currentEvent = null;
      }
    }
  }

  protected void dispatchEvent(AWTEvent event) {
    Object src = event.getSource();
    if (event instanceof ActiveEvent) {
      ((ActiveEvent) event).dispatch();
    } else if (src instanceof Component) {
      ((Component) src).dispatchEvent(event);
    } else if (src instanceof MenuComponent) {
      ((MenuComponent) src).dispatchEvent(event);
    }
  }

  public synchronized AWTEvent getNextEvent() throws InterruptedException {
    while (queue.isEmpty()) {
      wait();
    }
    return queue.removeFirst();
  }

  public synchronized AWTEvent peekEvent() {
    return queue.isEmpty() ? null : queue.getFirst();
  }

  public synchronized AWTEvent peekEvent(int id) {
    for (AWTEvent e : queue) {
      if (e.getID() == id) {
        return e;
      }
    }
    return null;
  }

  public synchronized void push(EventQueue newEventQueue) {
    if (next != null) {
      next.push(newEventQueue);
      return;
    }
    while (!queue.isEmpty()) {
      newEventQueue.postEvent(queue.removeFirst());
    }
    newEventQueue.previous = this;
    next = newEventQueue;
  }

  protected void pop() throws EmptyStackException {
    synchronized (this) {
      if (next != null) {
        next.pop();
        return;
      }
      if (previous == null) {
        throw new EmptyStackException();
      }
      EventQueue prev = previous;
      synchronized (prev) {
        while (!queue.isEmpty()) {
          prev.queue.addLast(queue.removeFirst());
        }
        prev.next = null;
      }
      previous = null;
    }
  }

  public SecondaryLoop createSecondaryLoop() {
    return new SecondaryLoop() {
      private boolean running;

      public synchronized boolean enter() {
        running = true;
        return true;
      }

      public synchronized boolean exit() {
        boolean was = running;
        running = false;
        return was;
      }
    };
  }

  public static void invokeLater(Runnable runnable) {
    Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new InvocationEvent(Toolkit.getDefaultToolkit(), runnable));
  }

  public static void invokeAndWait(Runnable runnable) throws InterruptedException, InvocationTargetException {
    if (isDispatchThread()) {
      throw new Error("Cannot call invokeAndWait from the event dispatcher thread");
    }
    final class AWTInvocationLock { }
    Object lock = new AWTInvocationLock();
    InvocationEvent event = new InvocationEvent(Toolkit.getDefaultToolkit(), runnable, lock, true);
    synchronized (lock) {
      Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(event);
      while (!event.isDispatched()) {
        lock.wait();
      }
    }
    Throwable eventThrowable = event.getThrowable();
    if (eventThrowable != null) {
      throw new InvocationTargetException(eventThrowable);
    }
  }

  public static boolean isDispatchThread() {
    EventQueue eq = Toolkit.getDefaultToolkit().getSystemEventQueue();
    return Thread.currentThread() == eq.dispatchThread;
  }

  public static AWTEvent getCurrentEvent() {
    return currentEvent;
  }

  public static long getMostRecentEventTime() {
    return mostRecentEventTime;
  }
}
