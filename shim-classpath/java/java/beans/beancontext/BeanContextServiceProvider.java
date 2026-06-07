/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.beans.beancontext;

public interface BeanContextServiceProvider {
  java.util.Iterator<?> getCurrentServiceSelectors(java.beans.beancontext.BeanContextServices p0, java.lang.Class<?> p1);
  java.lang.Object getService(java.beans.beancontext.BeanContextServices p0, java.lang.Object p1, java.lang.Class<?> p2, java.lang.Object p3);
  void releaseService(java.beans.beancontext.BeanContextServices p0, java.lang.Object p1, java.lang.Object p2);
}
