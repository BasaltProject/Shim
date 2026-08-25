/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming.spi;

public interface Resolver {
  javax.naming.spi.ResolveResult resolveToClass(java.lang.String string, java.lang.Class<? extends javax.naming.Context> p1) throws javax.naming.NamingException;
  javax.naming.spi.ResolveResult resolveToClass(javax.naming.Name name, java.lang.Class<? extends javax.naming.Context> p1) throws javax.naming.NamingException;
}
