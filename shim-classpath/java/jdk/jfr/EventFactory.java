/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr;

import java.util.ArrayList;
import java.util.List;

public final class EventFactory {
  private final EventInfo info;

  private EventFactory(EventInfo info) {
    this.info = info;
  }

  public static EventFactory create(List<AnnotationElement> annotationElements, List<ValueDescriptor> fields) {
    java.util.Objects.requireNonNull(annotationElements);
    java.util.Objects.requireNonNull(fields);
    List<AnnotationElement> anns = new ArrayList<AnnotationElement>(annotationElements);
    List<ValueDescriptor> fs = new ArrayList<ValueDescriptor>(fields);
    for (ValueDescriptor v : fs) {
      String n = v.getName();
      if (n.equals("startTime") || n.equals("duration") || n.equals("eventThread") || n.equals("stackTrace")) {
        throw new IllegalArgumentException("Field name '" + n + "' is reserved");
      }
    }
    boolean named = false;
    for (AnnotationElement a : anns) {
      if (a.getTypeName().equals(Name.class.getName())) {
        named = true;
      }
    }
    if (!named) {
      throw new IllegalArgumentException("Annotation element " + Name.class.getName() + " is required");
    }
    return new EventFactory(EventInfo.dynamic(anns, fs));
  }

  public Event newEvent() {
    return new DynamicEvent(info);
  }

  public EventType getEventType() {
    return info.type;
  }

  public void register() {
    JfrRuntime.register(info);
  }

  public void unregister() {
    JfrRuntime.unregister(info);
  }
}
