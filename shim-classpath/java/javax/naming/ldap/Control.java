/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming.ldap;

public interface Control extends java.io.Serializable {
  boolean CRITICAL = true;
  boolean NONCRITICAL = false;

  byte[] getEncodedValue();
  java.lang.String getID();
  boolean isCritical();
}
