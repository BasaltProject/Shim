/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.security.auth;

public interface Destroyable {
  default void destroy() throws DestroyFailedException {
    throw new DestroyFailedException();
  }

  default boolean isDestroyed() {
    return false;
  }
}
