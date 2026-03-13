/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr;

public final class EventFactory {
  public void register() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public static jdk.jfr.EventFactory create(java.util.List<jdk.jfr.AnnotationElement> arg0, java.util.List<jdk.jfr.ValueDescriptor> arg1) { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public void unregister() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public jdk.jfr.EventType getEventType() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public jdk.jfr.Event newEvent() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
}
