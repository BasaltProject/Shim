/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.beans.beancontext;

public interface BeanContextServices extends java.beans.beancontext.BeanContext, java.beans.beancontext.BeanContextServicesListener {
  java.util.Iterator<?> getCurrentServiceSelectors(java.lang.Class<?> p0);
  boolean addService(java.lang.Class<?> p0, java.beans.beancontext.BeanContextServiceProvider p1);
  void revokeService(java.lang.Class<?> p0, java.beans.beancontext.BeanContextServiceProvider p1, boolean p2);
  boolean hasService(java.lang.Class<?> p0);
  java.lang.Object getService(java.beans.beancontext.BeanContextChild p0, java.lang.Object p1, java.lang.Class<?> p2, java.lang.Object p3, java.beans.beancontext.BeanContextServiceRevokedListener p4) throws java.util.TooManyListenersException;
  void releaseService(java.beans.beancontext.BeanContextChild p0, java.lang.Object p1, java.lang.Object p2);
  java.util.Iterator<?> getCurrentServiceClasses();
  void addBeanContextServicesListener(java.beans.beancontext.BeanContextServicesListener p0);
  void removeBeanContextServicesListener(java.beans.beancontext.BeanContextServicesListener p0);
}
