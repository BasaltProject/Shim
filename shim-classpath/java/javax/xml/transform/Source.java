/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.transform;

public interface Source {
  void setSystemId(String p0);

  String getSystemId();

  default boolean isEmpty() {
    throw new UnsupportedOperationException("The isEmpty method is not supported.");

  }
}
