/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr;

final class EventRecord {
  final EventInfo info;
  final long start;
  final long duration;
  final ThreadData thread;
  final StackTraceElement[] stack;
  final Object[] values;

  EventRecord(EventInfo info, long start, long duration, ThreadData thread, StackTraceElement[] stack,
      Object[] values) {
    this.info = info;
    this.start = start;
    this.duration = duration;
    this.thread = thread;
    this.stack = stack;
    this.values = values;
  }
}
