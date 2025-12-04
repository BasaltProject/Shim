/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.beans;

import java.util.EventListenerProxy;

public class VetoableChangeListenerProxy extends EventListenerProxy<VetoableChangeListener>
    implements VetoableChangeListener {
  private final String propertyName;

  public VetoableChangeListenerProxy(String propertyName, VetoableChangeListener listener) {
    super(listener);
    this.propertyName = propertyName;
  }

  @Override
  public void vetoableChange(PropertyChangeEvent event) throws PropertyVetoException {
    getListener().vetoableChange(event);
  }

  public String getPropertyName() {
    return propertyName;
  }
}
