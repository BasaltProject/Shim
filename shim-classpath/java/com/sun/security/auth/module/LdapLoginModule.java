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

public class LdapLoginModule implements LoginModule {

  protected Subject subject;
  protected CallbackHandler callbackHandler;
  protected Map<String, ?> sharedState;
  protected Map<String, ?> options;
  protected boolean debug = false;
  protected boolean succeeded = false;
  protected boolean commitSucceeded = false;

  public LdapLoginModule() {
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
  private com.sun.security.auth.UserPrincipal ldapPrincipal;
  private com.sun.security.auth.LdapPrincipal authzPrincipal;

  public boolean login() throws LoginException {
    String url = (String) options.get("userProvider");
    if (url == null) {
      throw new LoginException("Unable to locate the LDAP directory service: userProvider is not set");
    }
    if (callbackHandler == null) {
      throw new LoginException("No CallbackHandler available to acquire authentication information from the user");
    }
    javax.security.auth.callback.NameCallback nc = new javax.security.auth.callback.NameCallback("User name: ");
    javax.security.auth.callback.PasswordCallback pc =
        new javax.security.auth.callback.PasswordCallback("Password: ", false);
    try {
      callbackHandler.handle(new javax.security.auth.callback.Callback[] {nc, pc});
    } catch (java.io.IOException ioe) {
      throw new LoginException(ioe.toString());
    } catch (javax.security.auth.callback.UnsupportedCallbackException uce) {
      throw new LoginException(uce.getCallback().toString() + " not available");
    }
    username = nc.getName();
    password = pc.getPassword();
    pc.clearPassword();
    if (username == null || username.length() == 0) {
      throw new javax.security.auth.login.FailedLoginException("No user name was provided");
    }
    java.util.Hashtable<String, Object> env = new java.util.Hashtable<String, Object>();
    env.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    env.put(javax.naming.Context.PROVIDER_URL, url);
    String user = (String) options.get("authIdentity");
    if (user == null) {
      user = "{USERNAME}";
    }
    env.put(javax.naming.Context.SECURITY_PRINCIPAL, user.replace("{USERNAME}", username));
    env.put(javax.naming.Context.SECURITY_CREDENTIALS, new String(password));
    try {
      new javax.naming.directory.InitialDirContext(env).close();
    } catch (javax.naming.NamingException e) {
      throw (LoginException) new javax.security.auth.login.FailedLoginException(
          "Cannot bind to LDAP server").initCause(e);
    }
    ldapPrincipal = new com.sun.security.auth.UserPrincipal(username);
    succeeded = true;
    return true;
  }

  public boolean commit() throws LoginException {
    if (!succeeded) {
      return false;
    }
    subject.getPrincipals().add(ldapPrincipal);
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
    return true;
  }

  public boolean logout() throws LoginException {
    if (ldapPrincipal != null) {
      subject.getPrincipals().remove(ldapPrincipal);
    }
    succeeded = false;
    commitSucceeded = false;
    ldapPrincipal = null;
    username = null;
    password = null;
    return true;
  }

}
