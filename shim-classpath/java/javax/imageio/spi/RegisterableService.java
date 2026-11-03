/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.imageio.spi;

public interface RegisterableService {
  void onRegistration(javax.imageio.spi.ServiceRegistry p0, java.lang.Class<?> p1);
  void onDeregistration(javax.imageio.spi.ServiceRegistry p0, java.lang.Class<?> p1);
}
