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

public class Krb5LoginModule implements LoginModule {

  protected Subject subject;
  protected CallbackHandler callbackHandler;
  protected Map<String, ?> sharedState;
  protected Map<String, ?> options;
  protected boolean debug = false;
  protected boolean succeeded = false;
  protected boolean commitSucceeded = false;

  public Krb5LoginModule() {
  }

  public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
      Map<String, ?> options) {
    this.subject = subject;
    this.callbackHandler = callbackHandler;
    this.sharedState = sharedState;
    this.options = options;
    debug = "true".equalsIgnoreCase((String) options.get("debug"));
  }

  private String principalName;

  public boolean login() throws LoginException {
    principalName = (String) options.get("principal");
    if (principalName == null && callbackHandler != null) {
      javax.security.auth.callback.NameCallback nc = new javax.security.auth.callback.NameCallback("Kerberos username: ");
      try {
        callbackHandler.handle(new javax.security.auth.callback.Callback[] {nc});
      } catch (java.io.IOException ioe) {
        throw new LoginException(ioe.toString());
      } catch (javax.security.auth.callback.UnsupportedCallbackException uce) {
        throw new LoginException(uce.getCallback().toString() + " not available to garner authentication information from the user");
      }
      principalName = nc.getName();
    }
    if (principalName == null) {
      throw new LoginException("Unable to obtain Principal Name for authentication");
    }
    throw new javax.security.auth.login.FailedLoginException("Unable to connect to a Kerberos KDC for " + principalName);
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
