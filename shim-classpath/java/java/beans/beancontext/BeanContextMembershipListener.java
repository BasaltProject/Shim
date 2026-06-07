/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.beans.beancontext;

public interface BeanContextMembershipListener extends java.util.EventListener {
  void childrenAdded(java.beans.beancontext.BeanContextMembershipEvent p0);
  void childrenRemoved(java.beans.beancontext.BeanContextMembershipEvent p0);
}
