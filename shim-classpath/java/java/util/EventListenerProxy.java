/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util;

public abstract class EventListenerProxy<T extends EventListener> implements EventListener {
  private final T listener;

  public EventListenerProxy(T listener) {
    this.listener = listener;
  }

  public T getListener() {
    return listener;
  }
}
