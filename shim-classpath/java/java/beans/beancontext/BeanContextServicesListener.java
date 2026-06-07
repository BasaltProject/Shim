/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.beans.beancontext;

public interface BeanContextServicesListener extends java.beans.beancontext.BeanContextServiceRevokedListener {
  void serviceAvailable(java.beans.beancontext.BeanContextServiceAvailableEvent p0);
}
