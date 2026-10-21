/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr;

public final class EventType {
  public java.lang.String getName() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public java.util.List<jdk.jfr.ValueDescriptor> getFields() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public jdk.jfr.ValueDescriptor getField(java.lang.String arg0) { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public <A extends java.lang.annotation.Annotation> A getAnnotation(java.lang.Class<A> arg0) { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public boolean isEnabled() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public long getId() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public java.lang.String getLabel() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public java.util.List<jdk.jfr.AnnotationElement> getAnnotationElements() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public java.lang.String getDescription() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public static jdk.jfr.EventType getEventType(java.lang.Class<? extends jdk.jfr.Event> arg0) { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public java.util.List<jdk.jfr.SettingDescriptor> getSettingDescriptors() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public java.util.List<java.lang.String> getCategoryNames() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
}
