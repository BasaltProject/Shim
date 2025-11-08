/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang;

public class LayerInstantiationException extends RuntimeException {
  private static final long serialVersionUID = -906239691613568347L;

  public LayerInstantiationException() {
  }

  public LayerInstantiationException(String msg) {
    super(msg);
  }

  public LayerInstantiationException(Throwable cause) {
    super(cause);
  }

  public LayerInstantiationException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
