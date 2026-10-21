/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr.consumer;

public final class RecordedEvent extends jdk.jfr.consumer.RecordedObject {
  public jdk.jfr.consumer.RecordedStackTrace getStackTrace() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public java.util.List<jdk.jfr.ValueDescriptor> getFields() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public java.time.Duration getDuration() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public jdk.jfr.EventType getEventType() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public java.time.Instant getStartTime() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public java.time.Instant getEndTime() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public jdk.jfr.consumer.RecordedThread getThread() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
}
