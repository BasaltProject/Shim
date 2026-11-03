/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.beans.beancontext;

import java.util.Iterator;

public class BeanContextServiceAvailableEvent extends BeanContextEvent {
  private static final long serialVersionUID = -5333985775656400778L;

  protected Class<?> serviceClass;

  public BeanContextServiceAvailableEvent(BeanContextServices bcs, Class<?> sc) {
    super((BeanContext) bcs);
    serviceClass = sc;
  }

  public BeanContextServices getSourceAsBeanContextServices() { return (BeanContextServices) getBeanContext(); }

  public Class<?> getServiceClass() { return serviceClass; }

  public Iterator<?> getCurrentServiceSelectors() {
    return ((BeanContextServices) getSource()).getCurrentServiceSelectors(serviceClass);
  }
}
