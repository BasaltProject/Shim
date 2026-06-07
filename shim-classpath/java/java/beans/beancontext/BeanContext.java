/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.beans.beancontext;

public interface BeanContext extends java.beans.beancontext.BeanContextChild, java.util.Collection, java.beans.DesignMode, java.beans.Visibility {
  java.lang.Object globalHierarchyLock = new java.lang.Object();
  java.io.InputStream getResourceAsStream(java.lang.String p0, java.beans.beancontext.BeanContextChild p1) throws java.lang.IllegalArgumentException;
  java.net.URL getResource(java.lang.String p0, java.beans.beancontext.BeanContextChild p1) throws java.lang.IllegalArgumentException;
  java.lang.Object instantiateChild(java.lang.String p0) throws java.io.IOException, java.lang.ClassNotFoundException;
  void addBeanContextMembershipListener(java.beans.beancontext.BeanContextMembershipListener p0);
  void removeBeanContextMembershipListener(java.beans.beancontext.BeanContextMembershipListener p0);
}
