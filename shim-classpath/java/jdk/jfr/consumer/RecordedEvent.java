/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr.consumer;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import jdk.jfr.EventType;
import jdk.jfr.ValueDescriptor;

public final class RecordedEvent extends RecordedObject {
  RecordedEvent(Chunk.TypeDef td, Object[] vals, Chunk chunk) {
    super(td, vals, chunk);
  }

  public RecordedStackTrace getStackTrace() {
    return (RecordedStackTrace) getValue("stackTrace");
  }

  public RecordedThread getThread() {
    return (RecordedThread) getValue("eventThread");
  }

  public EventType getEventType() {
    return chunk.eventType(td);
  }

  public List<ValueDescriptor> getFields() {
    return getEventType().getFields();
  }

  public Instant getStartTime() {
    return chunk.instantOf(getLong("startTime"));
  }

  public Instant getEndTime() {
    return getStartTime().plus(getDuration());
  }

  public Duration getDuration() {
    return Duration.ofNanos((long) (getLong("duration") * (1e9 / chunk.ticksPerSecond)));
  }
}
