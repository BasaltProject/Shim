/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security;

import java.io.IOException;
import java.io.ObjectOutputStream;

public class GuardedObject implements java.io.Serializable {
  private static final long serialVersionUID = -5240450096227834308L;

  private final Object object;
  private final Guard guard;

  public GuardedObject(Object object, Guard guard) {
    this.guard = guard;
    this.object = object;
  }

  public Object getObject() throws SecurityException {
    if (guard != null) {
      guard.checkGuard(object);
    }
    return object;
  }

  private void writeObject(ObjectOutputStream oos) throws IOException {
    if (guard != null) {
      guard.checkGuard(object);
    }
    oos.defaultWriteObject();
  }
}
