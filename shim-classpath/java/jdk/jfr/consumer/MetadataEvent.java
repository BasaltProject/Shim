/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr.consumer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jdk.jfr.Configuration;
import jdk.jfr.EventType;

public final class MetadataEvent {
  private final List<EventType> current;
  private final List<EventType> added;
  private final List<EventType> removed;
  private final List<Configuration> configurations;

  MetadataEvent(List<EventType> current, List<EventType> added, List<EventType> removed,
      List<Configuration> configurations) {
    this.current = current;
    this.added = added;
    this.removed = removed;
    this.configurations = configurations;
  }

  public final List<EventType> getEventTypes() {
    return Collections.unmodifiableList(new ArrayList<EventType>(current));
  }

  public final List<EventType> getAddedEventTypes() {
    return Collections.unmodifiableList(new ArrayList<EventType>(added));
  }

  public final List<EventType> getRemovedEventTypes() {
    return Collections.unmodifiableList(new ArrayList<EventType>(removed));
  }

  public List<Configuration> getConfigurations() {
    return Collections.unmodifiableList(new ArrayList<Configuration>(configurations));
  }
}
