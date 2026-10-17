/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;

public abstract class AuthProvider extends Provider {
  private static final long serialVersionUID = 4197859053084546461L;

  protected AuthProvider(String name, double version, String info) {
    super(name, version, info);
  }

  protected AuthProvider(String name, String versionStr, String info) {
    super(name, versionStr, info);
  }

  public abstract void login(Subject subject, CallbackHandler handler) throws LoginException;

  public abstract void logout() throws LoginException;

  public abstract void setCallbackHandler(CallbackHandler handler);
}
