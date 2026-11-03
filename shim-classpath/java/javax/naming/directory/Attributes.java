/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming.directory;

public interface Attributes extends java.lang.Cloneable, java.io.Serializable {
  java.lang.Object clone();
  javax.naming.directory.Attribute get(java.lang.String string);
  javax.naming.NamingEnumeration<? extends javax.naming.directory.Attribute> getAll();
  javax.naming.NamingEnumeration<java.lang.String> getIDs();
  boolean isCaseIgnored();
  javax.naming.directory.Attribute put(java.lang.String string, java.lang.Object object);
  javax.naming.directory.Attribute put(javax.naming.directory.Attribute attribute);
  javax.naming.directory.Attribute remove(java.lang.String string);
  int size();
}
