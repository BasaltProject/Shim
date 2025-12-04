/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.beans;

import java.util.EventListener;

public interface VetoableChangeListener extends EventListener {
  void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException;
}
