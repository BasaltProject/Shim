/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.beans;

import java.util.EventListenerProxy;

public class PropertyChangeListenerProxy extends EventListenerProxy<PropertyChangeListener>
    implements PropertyChangeListener {
  private final String propertyName;

  public PropertyChangeListenerProxy(String propertyName, PropertyChangeListener listener) {
    super(listener);
    this.propertyName = propertyName;
  }

  @Override
  public void propertyChange(PropertyChangeEvent event) {
    getListener().propertyChange(event);
  }

  public String getPropertyName() {
    return propertyName;
  }
}
