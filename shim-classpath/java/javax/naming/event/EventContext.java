/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming.event;

public interface EventContext extends javax.naming.Context {
  int OBJECT_SCOPE = 0;
  int ONELEVEL_SCOPE = 1;
  int SUBTREE_SCOPE = 2;

  void addNamingListener(java.lang.String string, int i, javax.naming.event.NamingListener namingListener) throws javax.naming.NamingException;
  void addNamingListener(javax.naming.Name name, int i, javax.naming.event.NamingListener namingListener) throws javax.naming.NamingException;
  void removeNamingListener(javax.naming.event.NamingListener namingListener) throws javax.naming.NamingException;
  boolean targetMustExist() throws javax.naming.NamingException;
}
