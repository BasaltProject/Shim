/*
 * Copyright (c) 2026 BasaltMC.
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.ref;

import java.util.Objects;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

public final class Cleaner {
  final ReferenceQueue<Object> queue = new ReferenceQueue<Object>();
  final PhantomCleanable<?> sentinel = new DummyCleanable();

  public interface Cleanable {
    void clean();
  }

  private static class DummyCleanable extends PhantomCleanable<Object> {
    DummyCleanable() {
      super();
    }

    @Override
    protected void performCleanup() {
    }
  }

  private Cleaner() {
  }

  public static Cleaner create() {
    return create(new ThreadFactory() {
      @Override
      public Thread newThread(Runnable r) {
        Thread t = new Thread(r, "Cleaner");
        t.setDaemon(true);
        return t;
      }
    });
  }

  public static Cleaner create(ThreadFactory threadFactory) {
    Objects.requireNonNull(threadFactory, "threadFactory");
    Cleaner cleaner = new Cleaner();
    Thread t = threadFactory.newThread(new CleanerRunnable(cleaner));
    t.start();
    return cleaner;
  }

  public Cleanable register(Object obj, Runnable action) {
    Objects.requireNonNull(obj, "obj");
    Objects.requireNonNull(action, "action");
    return new ActionCleanable(obj, this, action);
  }

  private static class CleanerRunnable implements Runnable {
    private final Cleaner cleaner;

    CleanerRunnable(Cleaner cleaner) {
      this.cleaner = cleaner;
    }

    @Override
    public void run() {
      while (true) {
        try {
          Reference<?> ref = cleaner.queue.remove();
          if (ref instanceof Cleanable) {
            ((Cleanable) ref).clean();
          }
        } catch (InterruptedException e) {
          // ignore and continue
        } catch (Throwable t) {
          // ignore per specification
        }
      }
    }
  }

  static abstract class PhantomCleanable<T> extends PhantomReference<T> implements Cleanable {
    private final AtomicBoolean cleaned = new AtomicBoolean(false);
    private PhantomCleanable<?> prev = this;
    private PhantomCleanable<?> next = this;
    private Cleaner cleaner;

    PhantomCleanable() {
      super(null, null);
    }

    PhantomCleanable(T referent, Cleaner cleaner) {
      super(referent, cleaner.queue);
      this.cleaner = cleaner;
      insert(cleaner.sentinel);
    }

    private void insert(PhantomCleanable<?> sentinel) {
      synchronized (sentinel) {
        this.prev = sentinel;
        this.next = sentinel.next;
        this.next.prev = this;
        sentinel.next = this;
      }
    }

    private void remove() {
      if (cleaner != null) {
        synchronized (cleaner.sentinel) {
          prev.next = next;
          next.prev = prev;
          prev = this;
          next = this;
        }
      }
    }

    @Override
    public final void clean() {
      if (cleaned.compareAndSet(false, true)) {
        remove();
        super.clear();
        performCleanup();
      }
    }

    protected abstract void performCleanup();
  }

  private static final class ActionCleanable extends PhantomCleanable<Object> {
    private final Runnable action;

    ActionCleanable(Object referent, Cleaner cleaner, Runnable action) {
      super(referent, cleaner);
      this.action = action;
    }

    @Override
    protected void performCleanup() {
      action.run();
    }
  }
}
