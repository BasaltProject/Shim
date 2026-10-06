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

public class JndiLoginModule implements LoginModule {
  public final String USER_PROVIDER = "user.provider.url";
  public final String GROUP_PROVIDER = "group.provider.url";

  protected Subject subject;
  protected CallbackHandler callbackHandler;
  protected Map<String, ?> sharedState;
  protected Map<String, ?> options;
  protected boolean debug = false;
  protected boolean succeeded = false;
  protected boolean commitSucceeded = false;

  public JndiLoginModule() {
  }

  public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
      Map<String, ?> options) {
    this.subject = subject;
    this.callbackHandler = callbackHandler;
    this.sharedState = sharedState;
    this.options = options;
    debug = "true".equalsIgnoreCase((String) options.get("debug"));
  }

  private String username;
  private char[] password;
  private javax.naming.directory.Attributes userAttrs;
  private com.sun.security.auth.UnixPrincipal userPrincipal;
  private com.sun.security.auth.UnixNumericUserPrincipal UIDPrincipal;
  private com.sun.security.auth.UnixNumericGroupPrincipal GIDPrincipal;
  private java.util.List<com.sun.security.auth.UnixNumericGroupPrincipal> groups =
      new java.util.ArrayList<com.sun.security.auth.UnixNumericGroupPrincipal>();

  private String userProvider() {
    return (String) options.get("user.provider.url");
  }

  public boolean login() throws LoginException {
    String up = userProvider();
    if (up == null) {
      throw new LoginException("Error: Unable to locate JNDI user provider");
    }
    if (callbackHandler == null) {
      throw new LoginException("Error: no CallbackHandler available to garner authentication information from the user");
    }
    javax.security.auth.callback.NameCallback nc = new javax.security.auth.callback.NameCallback("User name: ");
    javax.security.auth.callback.PasswordCallback pc =
        new javax.security.auth.callback.PasswordCallback("Password: ", false);
    try {
      callbackHandler.handle(new javax.security.auth.callback.Callback[] {nc, pc});
    } catch (java.io.IOException ioe) {
      throw new LoginException(ioe.toString());
    } catch (javax.security.auth.callback.UnsupportedCallbackException uce) {
      throw new LoginException("Error: " + uce.getCallback().toString()
          + " not available to garner authentication information from the user");
    }
    username = nc.getName();
    password = pc.getPassword();
    pc.clearPassword();
    java.util.Hashtable<String, String> env = new java.util.Hashtable<String, String>();
    for (Map.Entry<String, ?> e : options.entrySet()) {
      if (e.getKey().startsWith("java.naming.") && e.getValue() instanceof String) {
        env.put(e.getKey(), (String) e.getValue());
      }
    }
    env.put(javax.naming.Context.PROVIDER_URL, up);
    try {
      javax.naming.directory.DirContext ctx = new javax.naming.directory.InitialDirContext(env);
      userAttrs = ctx.getAttributes(username);
      javax.naming.directory.Attribute pw = userAttrs.get("userPassword");
      if (pw == null || !java.util.Arrays.equals(password, new String((byte[]) pw.get()).toCharArray())) {
        throw new javax.security.auth.login.FailedLoginException("Password Incorrect/Invalid for " + username);
      }
    } catch (javax.naming.NamingException ne) {
      throw (LoginException) new javax.security.auth.login.FailedLoginException(
          "Error: User not found/invalid for " + username).initCause(ne);
    }
    succeeded = true;
    return true;
  }

  public boolean commit() throws LoginException {
    if (!succeeded) {
      return false;
    }
    userPrincipal = new com.sun.security.auth.UnixPrincipal(username);
    subject.getPrincipals().add(userPrincipal);
    commitSucceeded = true;
    return true;
  }

  public boolean abort() throws LoginException {
    if (!succeeded) {
      return false;
    }
    if (commitSucceeded) {
      logout();
    }
    succeeded = false;
    username = null;
    password = null;
    return true;
  }

  public boolean logout() throws LoginException {
    if (userPrincipal != null) {
      subject.getPrincipals().remove(userPrincipal);
    }
    succeeded = false;
    commitSucceeded = false;
    username = null;
    password = null;
    userPrincipal = null;
    return true;
  }

}
