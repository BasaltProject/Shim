/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming.directory;

public interface DirContext extends javax.naming.Context {
  int ADD_ATTRIBUTE = 1;
  int REPLACE_ATTRIBUTE = 2;
  int REMOVE_ATTRIBUTE = 3;

  void bind(java.lang.String string, java.lang.Object object, javax.naming.directory.Attributes attributes) throws javax.naming.NamingException;
  void bind(javax.naming.Name name, java.lang.Object object, javax.naming.directory.Attributes attributes) throws javax.naming.NamingException;
  javax.naming.directory.DirContext createSubcontext(java.lang.String string, javax.naming.directory.Attributes attributes) throws javax.naming.NamingException;
  javax.naming.directory.DirContext createSubcontext(javax.naming.Name name, javax.naming.directory.Attributes attributes) throws javax.naming.NamingException;
  javax.naming.directory.Attributes getAttributes(java.lang.String string, java.lang.String[] strings) throws javax.naming.NamingException;
  javax.naming.directory.Attributes getAttributes(java.lang.String string) throws javax.naming.NamingException;
  javax.naming.directory.Attributes getAttributes(javax.naming.Name name, java.lang.String[] strings) throws javax.naming.NamingException;
  javax.naming.directory.Attributes getAttributes(javax.naming.Name name) throws javax.naming.NamingException;
  javax.naming.directory.DirContext getSchema(java.lang.String string) throws javax.naming.NamingException;
  javax.naming.directory.DirContext getSchema(javax.naming.Name name) throws javax.naming.NamingException;
  javax.naming.directory.DirContext getSchemaClassDefinition(java.lang.String string) throws javax.naming.NamingException;
  javax.naming.directory.DirContext getSchemaClassDefinition(javax.naming.Name name) throws javax.naming.NamingException;
  void modifyAttributes(java.lang.String string, javax.naming.directory.ModificationItem[] modificationItems) throws javax.naming.NamingException;
  void modifyAttributes(java.lang.String string, int i, javax.naming.directory.Attributes attributes) throws javax.naming.NamingException;
  void modifyAttributes(javax.naming.Name name, javax.naming.directory.ModificationItem[] modificationItems) throws javax.naming.NamingException;
  void modifyAttributes(javax.naming.Name name, int i, javax.naming.directory.Attributes attributes) throws javax.naming.NamingException;
  void rebind(java.lang.String string, java.lang.Object object, javax.naming.directory.Attributes attributes) throws javax.naming.NamingException;
  void rebind(javax.naming.Name name, java.lang.Object object, javax.naming.directory.Attributes attributes) throws javax.naming.NamingException;
  javax.naming.NamingEnumeration<javax.naming.directory.SearchResult> search(java.lang.String string, java.lang.String string2, java.lang.Object[] objects, javax.naming.directory.SearchControls searchControls) throws javax.naming.NamingException;
  javax.naming.NamingEnumeration<javax.naming.directory.SearchResult> search(java.lang.String string, java.lang.String string2, javax.naming.directory.SearchControls searchControls) throws javax.naming.NamingException;
  javax.naming.NamingEnumeration<javax.naming.directory.SearchResult> search(java.lang.String string, javax.naming.directory.Attributes attributes, java.lang.String[] strings) throws javax.naming.NamingException;
  javax.naming.NamingEnumeration<javax.naming.directory.SearchResult> search(java.lang.String string, javax.naming.directory.Attributes attributes) throws javax.naming.NamingException;
  javax.naming.NamingEnumeration<javax.naming.directory.SearchResult> search(javax.naming.Name name, java.lang.String string, java.lang.Object[] objects, javax.naming.directory.SearchControls searchControls) throws javax.naming.NamingException;
  javax.naming.NamingEnumeration<javax.naming.directory.SearchResult> search(javax.naming.Name name, java.lang.String string, javax.naming.directory.SearchControls searchControls) throws javax.naming.NamingException;
  javax.naming.NamingEnumeration<javax.naming.directory.SearchResult> search(javax.naming.Name name, javax.naming.directory.Attributes attributes, java.lang.String[] strings) throws javax.naming.NamingException;
  javax.naming.NamingEnumeration<javax.naming.directory.SearchResult> search(javax.naming.Name name, javax.naming.directory.Attributes attributes) throws javax.naming.NamingException;
}
