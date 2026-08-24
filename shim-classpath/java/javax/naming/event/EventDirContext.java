/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming.event;

public interface EventDirContext extends javax.naming.event.EventContext, javax.naming.directory.DirContext {
  void addNamingListener(java.lang.String string, java.lang.String string2, java.lang.Object[] objects, javax.naming.directory.SearchControls searchControls, javax.naming.event.NamingListener namingListener) throws javax.naming.NamingException;
  void addNamingListener(java.lang.String string, java.lang.String string2, javax.naming.directory.SearchControls searchControls, javax.naming.event.NamingListener namingListener) throws javax.naming.NamingException;
  void addNamingListener(javax.naming.Name name, java.lang.String string, java.lang.Object[] objects, javax.naming.directory.SearchControls searchControls, javax.naming.event.NamingListener namingListener) throws javax.naming.NamingException;
  void addNamingListener(javax.naming.Name name, java.lang.String string, javax.naming.directory.SearchControls searchControls, javax.naming.event.NamingListener namingListener) throws javax.naming.NamingException;
}
