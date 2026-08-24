/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming.ldap;

public interface ExtendedResponse extends java.io.Serializable {
  byte[] getEncodedValue();
  java.lang.String getID();
}
