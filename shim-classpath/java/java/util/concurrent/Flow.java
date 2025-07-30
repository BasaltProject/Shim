/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.concurrent;

public final class Flow {
  private Flow() {}

  @FunctionalInterface
  public static interface Publisher<T> {
    public void subscribe(Subscriber<? super T> subscriber);
  }

  public static interface Subscriber<T> {
    public void onSubscribe(Subscription subscription);
    public void onNext(T item);
    public void onError(Throwable throwable);
    public void onComplete();
  }

  public static interface Subscription {
    public void request(long n);
    public void cancel();
  }

  public static interface Processor<T, R> extends Subscriber<T>, Publisher<R> {
  }

  static final int DEFAULT_BUFFER_SIZE = 256;

  public static int defaultBufferSize() {
    return DEFAULT_BUFFER_SIZE;
  }
}
