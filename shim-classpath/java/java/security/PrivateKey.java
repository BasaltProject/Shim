/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security;

public interface PrivateKey extends AsymmetricKey, javax.security.auth.Destroyable {
  @Deprecated
  long serialVersionUID = 6034044314589513430L;
}
