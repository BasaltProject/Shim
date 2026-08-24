/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming.ldap;

public interface ExtendedRequest extends java.io.Serializable {
  javax.naming.ldap.ExtendedResponse createExtendedResponse(java.lang.String string, byte[] bs, int i, int i2) throws javax.naming.NamingException;
  byte[] getEncodedValue();
  java.lang.String getID();
}
