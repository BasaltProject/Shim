/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr;

@jdk.jfr.Enabled() @jdk.jfr.StackTrace() @jdk.jfr.Registered() public abstract class Event extends jdk.internal.event.Event {
  protected Event() { }
  public final void commit() { }
  public final void begin() { }
  public final void end() { }
  public final void set(int arg0, java.lang.Object arg1) { }
  public final boolean isEnabled() { return false; }
  public final boolean shouldCommit() { return false; }
}
