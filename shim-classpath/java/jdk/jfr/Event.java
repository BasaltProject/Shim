/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr;

@Enabled
@StackTrace
@Registered
public abstract class Event extends jdk.internal.event.Event {
  long jfrStart;
  long jfrEnd;

  protected Event() {
  }

  public final void commit() {
    JfrRuntime.commit(this);
  }

  public final void begin() {
    jfrStart = System.nanoTime();
  }

  public final void end() {
    jfrEnd = System.nanoTime();
  }

  public final void set(int index, Object value) {
    if (this instanceof DynamicEvent) {
      ((DynamicEvent) this).setValue(index, value);
      return;
    }
    throw new UnsupportedOperationException("Not supported for instrumented events");
  }

  public final boolean isEnabled() {
    return JfrRuntime.isEnabled(this);
  }

  public final boolean shouldCommit() {
    return JfrRuntime.shouldCommit(this);
  }
}
