/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.beans.beancontext;

import java.util.EventObject;

public abstract class BeanContextEvent extends EventObject {
  private static final long serialVersionUID = 7267998073569045052L;

  protected BeanContext propagatedFrom;

  protected BeanContextEvent(BeanContext bc) { super(bc); }

  public BeanContext getBeanContext() { return (BeanContext) getSource(); }

  public synchronized void setPropagatedFrom(BeanContext bc) { propagatedFrom = bc; }

  public synchronized BeanContext getPropagatedFrom() { return propagatedFrom; }

  public synchronized boolean isPropagated() { return propagatedFrom != null; }
}
