/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming.directory;

public interface Attribute extends java.lang.Cloneable, java.io.Serializable {
  long serialVersionUID = 8707690322213556804L;

  boolean add(java.lang.Object object);
  void add(int i, java.lang.Object object);
  void clear();
  java.lang.Object clone();
  boolean contains(java.lang.Object object);
  java.lang.Object get() throws javax.naming.NamingException;
  java.lang.Object get(int i) throws javax.naming.NamingException;
  javax.naming.NamingEnumeration<?> getAll() throws javax.naming.NamingException;
  javax.naming.directory.DirContext getAttributeDefinition() throws javax.naming.NamingException;
  javax.naming.directory.DirContext getAttributeSyntaxDefinition() throws javax.naming.NamingException;
  java.lang.String getID();
  boolean isOrdered();
  boolean remove(java.lang.Object object);
  java.lang.Object remove(int i);
  java.lang.Object set(int i, java.lang.Object object);
  int size();
}
