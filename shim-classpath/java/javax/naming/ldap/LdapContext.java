/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming.ldap;

public interface LdapContext extends javax.naming.directory.DirContext {
  java.lang.String CONTROL_FACTORIES = "java.naming.factory.control";

  javax.naming.ldap.ExtendedResponse extendedOperation(javax.naming.ldap.ExtendedRequest extendedRequest) throws javax.naming.NamingException;
  javax.naming.ldap.Control[] getConnectControls() throws javax.naming.NamingException;
  javax.naming.ldap.Control[] getRequestControls() throws javax.naming.NamingException;
  javax.naming.ldap.Control[] getResponseControls() throws javax.naming.NamingException;
  javax.naming.ldap.LdapContext newInstance(javax.naming.ldap.Control[] controls) throws javax.naming.NamingException;
  void reconnect(javax.naming.ldap.Control[] controls) throws javax.naming.NamingException;
  void setRequestControls(javax.naming.ldap.Control[] controls) throws javax.naming.NamingException;
}
