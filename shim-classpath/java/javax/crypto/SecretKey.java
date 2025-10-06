/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.crypto;

public interface SecretKey extends java.security.Key, javax.security.auth.Destroyable {
  @Deprecated
  long serialVersionUID = -4795878709595146952L;
}
