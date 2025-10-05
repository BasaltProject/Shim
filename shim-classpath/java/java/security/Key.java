/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security;

public interface Key extends java.io.Serializable {
  @Deprecated
  long serialVersionUID = 6603384152749567654L;

  String getAlgorithm();

  String getFormat();

  byte[] getEncoded();
}
