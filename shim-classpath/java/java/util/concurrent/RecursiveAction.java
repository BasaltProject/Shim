/*
 * Copyright (c) 2026 BasaltMC.
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.concurrent;

public abstract class RecursiveAction extends ForkJoinTask<Void> {
  protected abstract void compute();

  @Override
  public final Void getRawResult() {
    return null;
  }

  @Override
  protected final void setRawResult(Void mustBeNull) {
  }

  @Override
  protected final boolean exec() {
    compute();
    return true;
  }
}
