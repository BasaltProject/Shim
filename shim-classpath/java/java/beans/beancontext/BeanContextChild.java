/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.beans.beancontext;

public interface BeanContextChild {
  void setBeanContext(java.beans.beancontext.BeanContext p0) throws java.beans.PropertyVetoException;
  java.beans.beancontext.BeanContext getBeanContext();
  void addPropertyChangeListener(java.lang.String p0, java.beans.PropertyChangeListener p1);
  void removePropertyChangeListener(java.lang.String p0, java.beans.PropertyChangeListener p1);
  void addVetoableChangeListener(java.lang.String p0, java.beans.VetoableChangeListener p1);
  void removeVetoableChangeListener(java.lang.String p0, java.beans.VetoableChangeListener p1);
}
