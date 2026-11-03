/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.beans.beancontext;

public class BeanContextServiceRevokedEvent extends BeanContextEvent {
  private static final long serialVersionUID = -1295543154724961754L;

  protected Class<?> serviceClass;
  private boolean invalidateRefs;

  public BeanContextServiceRevokedEvent(BeanContextServices bcs, Class<?> sc, boolean invalidate) {
    super((BeanContext) bcs);
    serviceClass = sc;
    invalidateRefs = invalidate;
  }

  public BeanContextServices getSourceAsBeanContextServices() { return (BeanContextServices) getBeanContext(); }

  public Class<?> getServiceClass() { return serviceClass; }

  public boolean isServiceClass(Class<?> service) { return serviceClass.equals(service); }

  public boolean isCurrentServiceInvalidNow() { return invalidateRefs; }
}
