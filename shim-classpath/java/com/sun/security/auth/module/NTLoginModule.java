/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.security.auth.module;

import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

public class NTLoginModule implements LoginModule {

  protected Subject subject;
  protected CallbackHandler callbackHandler;
  protected Map<String, ?> sharedState;
  protected Map<String, ?> options;
  protected boolean debug = false;
  protected boolean succeeded = false;
  protected boolean commitSucceeded = false;

  public NTLoginModule() {
  }

  public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
      Map<String, ?> options) {
    this.subject = subject;
    this.callbackHandler = callbackHandler;
    this.sharedState = sharedState;
    this.options = options;
    debug = "true".equalsIgnoreCase((String) options.get("debug"));
  }

  public boolean login() throws LoginException {
    throw new UnsatisfiedLinkError("no jaas in java.library.path");
  }

  public boolean commit() throws LoginException {
    if (!succeeded) {
      return false;
    }
    commitSucceeded = true;
    return true;
  }

  public boolean abort() throws LoginException {
    succeeded = false;
    commitSucceeded = false;
    return true;
  }

  public boolean logout() throws LoginException {
    succeeded = false;
    commitSucceeded = false;
    return true;
  }

}
