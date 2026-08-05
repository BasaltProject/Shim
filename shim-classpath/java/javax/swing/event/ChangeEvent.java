/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.event;

import java.util.EventObject;

public class ChangeEvent extends EventObject {
  public ChangeEvent(Object source) {
    super(source);
  }
}
