/*
 * Copyright (c) 2026 BasaltMC.
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.concurrent;

public abstract class RecursiveTask<V> extends ForkJoinTask<V> {
  private V result;

  protected abstract V compute();

  @Override
  public final V getRawResult() {
    return result;
  }

  @Override
  protected final void setRawResult(V value) {
    this.result = value;
  }

  @Override
  protected final boolean exec() {
    result = compute();
    return true;
  }
}
