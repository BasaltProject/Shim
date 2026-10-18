/*
 * Copyright (c) 2026 BasaltMC.
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.concurrent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

public class SubmissionPublisher<T> implements Flow.Publisher<T>, AutoCloseable {
  private final Executor executor;
  private final int maxBufferCapacity;
  private final BiConsumer<? super Flow.Subscriber<? super T>, ? super Throwable> handler;
  private final List<SubscriptionImpl> subscriptions = new CopyOnWriteArrayList<SubscriptionImpl>();
  private volatile boolean closed = false;
  private volatile Throwable closedException = null;

  public SubmissionPublisher(Executor executor, int maxBufferCapacity,
                             BiConsumer<? super Flow.Subscriber<? super T>, ? super Throwable> handler) {
    Objects.requireNonNull(executor, "executor");
    if (maxBufferCapacity <= 0) {
      throw new IllegalArgumentException("maxBufferCapacity must be positive");
    }
    this.executor = executor;
    this.maxBufferCapacity = maxBufferCapacity;
    this.handler = handler;
  }

  public Executor getExecutor() {
    return executor;
  }

  public int getMaxBufferCapacity() {
    return maxBufferCapacity;
  }

  public SubmissionPublisher(Executor executor, int maxBufferCapacity) {
    this(executor, maxBufferCapacity, null);
  }

  public SubmissionPublisher() {
    this(ForkJoinPool.commonPool(), Flow.defaultBufferSize(), null);
  }

  @Override
  public void subscribe(Flow.Subscriber<? super T> subscriber) {
    Objects.requireNonNull(subscriber, "subscriber");
    if (closed) {
      subscriber.onSubscribe(new Flow.Subscription() {
        @Override
        public void request(long n) {}
        @Override
        public void cancel() {}
      });
      if (closedException != null) {
        subscriber.onError(closedException);
      } else {
        subscriber.onComplete();
      }
      return;
    }
    SubscriptionImpl sub = new SubscriptionImpl(subscriber);
    subscriptions.add(sub);
    subscriber.onSubscribe(sub);
  }

  public int submit(T item) {
    return offer(item, -1, null, null);
  }

  public int offer(T item, BiPredicate<Flow.Subscriber<? super T>, ? super T> onDrop) {
    return offer(item, 0, TimeUnit.MILLISECONDS, onDrop);
  }

  public int offer(T item, long timeout, TimeUnit unit,
                   BiPredicate<Flow.Subscriber<? super T>, ? super T> onDrop) {
    Objects.requireNonNull(item, "item");
    if (closed) {
      throw new IllegalStateException("Publisher is closed");
    }
    int maxLag = 0;
    for (SubscriptionImpl sub : subscriptions) {
      if (sub.isCancelled()) {
        subscriptions.remove(sub);
        continue;
      }
      if (!sub.offer(item, timeout, unit)) {
        if (onDrop != null && onDrop.test(sub.subscriber, item)) {
          if (!sub.offer(item, timeout, unit)) {
            maxLag = -1;
          }
        } else {
          maxLag = -1;
        }
      }
    }
    return maxLag;
  }

  @Override
  public void close() {
    if (!closed) {
      closed = true;
      for (SubscriptionImpl sub : subscriptions) {
        sub.onComplete();
      }
      subscriptions.clear();
    }
  }

  public void closeExceptionally(Throwable error) {
    Objects.requireNonNull(error, "error");
    if (!closed) {
      closed = true;
      closedException = error;
      for (SubscriptionImpl sub : subscriptions) {
        sub.onError(error);
      }
      subscriptions.clear();
    }
  }

  public boolean isClosed() {
    return closed;
  }

  public Throwable getClosedException() {
    return closedException;
  }

  public boolean hasSubscribers() {
    return !subscriptions.isEmpty();
  }

  public int getNumberOfSubscribers() {
    return subscriptions.size();
  }

  public List<Flow.Subscriber<? super T>> getSubscribers() {
    List<Flow.Subscriber<? super T>> list = new ArrayList<Flow.Subscriber<? super T>>();
    for (SubscriptionImpl sub : subscriptions) {
      list.add(sub.subscriber);
    }
    return Collections.unmodifiableList(list);
  }

  public boolean isSubscribed(Flow.Subscriber<? super T> subscriber) {
    Objects.requireNonNull(subscriber, "subscriber");
    for (SubscriptionImpl sub : subscriptions) {
      if (sub.subscriber.equals(subscriber)) {
        return true;
      }
    }
    return false;
  }

  public long estimateMinimumDemand() {
    long min = Long.MAX_VALUE;
    for (SubscriptionImpl sub : subscriptions) {
      long d = sub.demand.get();
      if (d < min) min = d;
    }
    return min == Long.MAX_VALUE ? 0 : min;
  }

  public int estimateMaximumLag() {
    int max = 0;
    for (SubscriptionImpl sub : subscriptions) {
      int s = sub.queue.size();
      if (s > max) max = s;
    }
    return max;
  }

  public CompletableFuture<Void> consume(Consumer<? super T> consumer) {
    Objects.requireNonNull(consumer, "consumer");
    CompletableFuture<Void> future = new CompletableFuture<Void>();
    subscribe(new Flow.Subscriber<T>() {
      private Flow.Subscription sub;

      @Override
      public void onSubscribe(Flow.Subscription subscription) {
        this.sub = subscription;
        subscription.request(Long.MAX_VALUE);
      }

      @Override
      public void onNext(T item) {
        try {
          consumer.accept(item);
        } catch (Throwable t) {
          if (sub != null) sub.cancel();
          future.completeExceptionally(t);
        }
      }

      @Override
      public void onError(Throwable throwable) {
        future.completeExceptionally(throwable);
      }

      @Override
      public void onComplete() {
        future.complete(null);
      }
    });
    return future;
  }

  private final class SubscriptionImpl implements Flow.Subscription {
    final Flow.Subscriber<? super T> subscriber;
    final BlockingQueue<T> queue = new LinkedBlockingQueue<T>(maxBufferCapacity);
    final AtomicLong demand = new AtomicLong(0);
    final AtomicBoolean cancelled = new AtomicBoolean(false);
    final AtomicBoolean active = new AtomicBoolean(false);
    final AtomicBoolean completed = new AtomicBoolean(false);
    final AtomicBoolean terminated = new AtomicBoolean(false);
    final AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

    SubscriptionImpl(Flow.Subscriber<? super T> subscriber) {
      this.subscriber = subscriber;
    }

    boolean isCancelled() {
      return cancelled.get();
    }

    boolean offer(T item, long timeout, TimeUnit unit) {
      if (cancelled.get()) return false;
      boolean added;
      if (timeout < 0) {
        added = queue.offer(item);
      } else if (timeout == 0) {
        added = queue.offer(item);
      } else {
        try {
          added = queue.offer(item, timeout, unit);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          return false;
        }
      }
      if (added) {
        drain();
      }
      return added;
    }

    @Override
    public void request(long n) {
      if (n <= 0) {
        onError(new IllegalArgumentException("non-positive subscription request: " + n));
        return;
      }
      demand.updateAndGet(current -> {
        long next = current + n;
        return (next < 0) ? Long.MAX_VALUE : next;
      });
      drain();
    }

    @Override
    public void cancel() {
      if (cancelled.compareAndSet(false, true)) {
        queue.clear();
        subscriptions.remove(this);
      }
    }

    void onComplete() {
      completed.set(true);
      drain();
    }

    void onError(Throwable t) {
      error.set(t);
      drain();
    }

    void drain() {
      if (active.compareAndSet(false, true)) {
        executor.execute(new Runnable() {
          @Override
          public void run() {
            try {
              while (!cancelled.get() && demand.get() > 0) {
                T item = queue.poll();
                if (item == null) break;
                demand.decrementAndGet();
                subscriber.onNext(item);
              }
              if (!cancelled.get()) {
                Throwable t = error.get();
                if (t != null) {
                  if (terminated.compareAndSet(false, true)) {
                    subscriber.onError(t);
                  }
                } else if (completed.get() && queue.isEmpty()) {
                  if (terminated.compareAndSet(false, true)) {
                    subscriber.onComplete();
                  }
                }
              }
            } catch (Throwable t) {
              cancel();
              if (handler != null) {
                handler.accept(subscriber, t);
              }
            } finally {
              active.set(false);
              if (!queue.isEmpty() && demand.get() > 0 && !cancelled.get()) {
                drain();
              } else if (!cancelled.get() && (error.get() != null || (completed.get() && queue.isEmpty())) && !terminated.get()) {
                drain();
              }
            }
          }
        });
      }
    }
  }
}
