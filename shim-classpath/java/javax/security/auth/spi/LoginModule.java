/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.security.auth.spi;

import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;

public interface LoginModule {
  void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
      Map<String, ?> options);

  boolean login() throws LoginException;

  boolean commit() throws LoginException;

  boolean abort() throws LoginException;

  boolean logout() throws LoginException;
}
