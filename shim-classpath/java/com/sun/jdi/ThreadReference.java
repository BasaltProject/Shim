/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jdi;

public interface ThreadReference extends com.sun.jdi.ObjectReference {
  int THREAD_STATUS_UNKNOWN = -1;
  int THREAD_STATUS_ZOMBIE = 0;
  int THREAD_STATUS_RUNNING = 1;
  int THREAD_STATUS_SLEEPING = 2;
  int THREAD_STATUS_MONITOR = 3;
  int THREAD_STATUS_WAIT = 4;
  int THREAD_STATUS_NOT_STARTED = 5;

  com.sun.jdi.ObjectReference currentContendedMonitor() throws com.sun.jdi.IncompatibleThreadStateException;
  void forceEarlyReturn(com.sun.jdi.Value value) throws com.sun.jdi.InvalidTypeException, com.sun.jdi.ClassNotLoadedException, com.sun.jdi.IncompatibleThreadStateException;
  com.sun.jdi.StackFrame frame(int i) throws com.sun.jdi.IncompatibleThreadStateException;
  int frameCount() throws com.sun.jdi.IncompatibleThreadStateException;
  java.util.List<com.sun.jdi.StackFrame> frames() throws com.sun.jdi.IncompatibleThreadStateException;
  java.util.List<com.sun.jdi.StackFrame> frames(int i, int i2) throws com.sun.jdi.IncompatibleThreadStateException;
  void interrupt();
  boolean isAtBreakpoint();
  boolean isSuspended();
  default boolean isVirtual() {
    throw new UnsupportedOperationException();
  }
  java.lang.String name();
  java.util.List<com.sun.jdi.ObjectReference> ownedMonitors() throws com.sun.jdi.IncompatibleThreadStateException;
  java.util.List<com.sun.jdi.MonitorInfo> ownedMonitorsAndFrames() throws com.sun.jdi.IncompatibleThreadStateException;
  void popFrames(com.sun.jdi.StackFrame stackFrame) throws com.sun.jdi.IncompatibleThreadStateException;
  void resume();
  int status();
  void stop(com.sun.jdi.ObjectReference objectReference) throws com.sun.jdi.InvalidTypeException;
  void suspend();
  int suspendCount();
  com.sun.jdi.ThreadGroupReference threadGroup();
}
