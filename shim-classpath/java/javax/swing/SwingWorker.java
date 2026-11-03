/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class SwingWorker<T, V> implements RunnableFuture<T> {
  public enum StateValue {
    PENDING, STARTED, DONE
  }

  private static final int kMaxWorkerThreads = 10;
  private static final AtomicInteger kPoolNumber = new AtomicInteger();
  private static ThreadPoolExecutor executor;

  private volatile int progress;
  private volatile StateValue state = StateValue.PENDING;
  private final FutureTask<T> future;
  private final PropertyChangeSupport propertyChangeSupport;
  private final List<V> pending = new ArrayList<V>();
  private boolean flushScheduled;

  public SwingWorker() {
    Callable<T> callable = new Callable<T>() {
      public T call() throws Exception {
        setState(StateValue.STARTED);
        return doInBackground();
      }
    };
    future = new FutureTask<T>(callable) {
      protected void done() {
        doneEDT();
        setState(StateValue.DONE);
      }
    };
    propertyChangeSupport = new EdtSupport(this);
  }

  private static final class EdtSupport extends PropertyChangeSupport {
    EdtSupport(Object source) { super(source); }

    public void firePropertyChange(final PropertyChangeEvent evt) {
      if (SwingUtilities.isEventDispatchThread()) {
        super.firePropertyChange(evt);
      } else {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() { EdtSupport.super.firePropertyChange(evt); }
        });
      }
    }
  }

  protected abstract T doInBackground() throws Exception;

  public final void run() { future.run(); }

  @SafeVarargs
  protected final void publish(V... chunks) {
    synchronized (pending) {
      for (V c : chunks) {
        pending.add(c);
      }
      if (!flushScheduled) {
        flushScheduled = true;
        later(new Runnable() {
          public void run() { flush(); }
        });
      }
    }
  }

  private void flush() {
    List<V> batch;
    synchronized (pending) {
      batch = new ArrayList<V>(pending);
      pending.clear();
      flushScheduled = false;
    }
    if (!batch.isEmpty()) {
      process(batch);
    }
  }

  protected void process(List<V> chunks) { }

  protected void done() { }

  protected final void setProgress(int progress) {
    if (progress < 0 || progress > 100) {
      throw new IllegalArgumentException("the value should be from 0 to 100");
    }
    if (this.progress == progress) {
      return;
    }
    int oldProgress = this.progress;
    this.progress = progress;
    if (!getPropertyChangeSupport().hasListeners("progress")) {
      return;
    }
    synchronized (pending) {
      if (!progressScheduled) {
        progressScheduled = true;
        progressFrom = oldProgress;
        later(new Runnable() {
          public void run() {
            int from;
            synchronized (pending) {
              from = progressFrom;
              progressScheduled = false;
            }
            getPropertyChangeSupport().firePropertyChange("progress", Integer.valueOf(from),
                Integer.valueOf(getProgress()));
          }
        });
      }
    }
  }

  private int progressFrom;
  private boolean progressScheduled;
  private static java.util.Timer timer;

  private static synchronized void later(final Runnable r) {
    if (timer == null) {
      timer = new java.util.Timer("SwingWorker-timer", true);
    }
    timer.schedule(new java.util.TimerTask() {
      public void run() { SwingUtilities.invokeLater(r); }
    }, 1000 / 30);
  }

  public final int getProgress() { return progress; }

  public final void execute() { getWorkersExecutorService().execute(this); }

  public final boolean cancel(boolean mayInterruptIfRunning) { return future.cancel(mayInterruptIfRunning); }

  public final boolean isCancelled() { return future.isCancelled(); }

  public final boolean isDone() { return future.isDone(); }

  public final T get() throws InterruptedException, ExecutionException { return future.get(); }

  public final T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    return future.get(timeout, unit);
  }

  public final void addPropertyChangeListener(PropertyChangeListener listener) {
    getPropertyChangeSupport().addPropertyChangeListener(listener);
  }

  public final void removePropertyChangeListener(PropertyChangeListener listener) {
    getPropertyChangeSupport().removePropertyChangeListener(listener);
  }

  public final void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
    getPropertyChangeSupport().firePropertyChange(propertyName, oldValue, newValue);
  }

  public final PropertyChangeSupport getPropertyChangeSupport() { return propertyChangeSupport; }

  public final StateValue getState() {
    if (isDone()) {
      return StateValue.DONE;
    }
    return state;
  }

  private void setState(StateValue state) {
    StateValue old = this.state;
    this.state = state;
    firePropertyChange("state", old, state);
  }

  private void doneEDT() {
    Runnable doDone = new Runnable() {
      public void run() {
        flush();
        done();
      }
    };
    if (SwingUtilities.isEventDispatchThread()) {
      doDone.run();
    } else {
      SwingUtilities.invokeLater(doDone);
    }
  }

  private static synchronized ThreadPoolExecutor getWorkersExecutorService() {
    if (executor == null) {
      final int poolNumber = kPoolNumber.incrementAndGet();
      final AtomicInteger threadNumber = new AtomicInteger();
      ThreadFactory threadFactory = new ThreadFactory() {
        public Thread newThread(Runnable r) {
          Thread t = new Thread(r, "SwingWorker-pool-" + poolNumber + "-thread-" + threadNumber.incrementAndGet());
          if (t.isDaemon()) {
            t.setDaemon(false);
          }
          t.setDaemon(true);
          t.setPriority(Thread.NORM_PRIORITY);
          return t;
        }
      };
      executor = new ThreadPoolExecutor(kMaxWorkerThreads, kMaxWorkerThreads, 10L, TimeUnit.MINUTES,
          new LinkedBlockingQueue<Runnable>(), threadFactory);
      executor.allowCoreThreadTimeOut(true);
    }
    return executor;
  }
}
