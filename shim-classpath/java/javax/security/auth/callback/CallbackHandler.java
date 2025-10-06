/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.security.auth.callback;

public interface CallbackHandler {
  void handle(Callback[] callbacks) throws java.io.IOException, UnsupportedCallbackException;
}
