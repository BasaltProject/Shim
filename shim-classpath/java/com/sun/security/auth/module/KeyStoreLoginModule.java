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

public class KeyStoreLoginModule implements LoginModule {

  protected Subject subject;
  protected CallbackHandler callbackHandler;
  protected Map<String, ?> sharedState;
  protected Map<String, ?> options;
  protected boolean debug = false;
  protected boolean succeeded = false;
  protected boolean commitSucceeded = false;

  public KeyStoreLoginModule() {
  }

  public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
      Map<String, ?> options) {
    this.subject = subject;
    this.callbackHandler = callbackHandler;
    this.sharedState = sharedState;
    this.options = options;
    debug = "true".equalsIgnoreCase((String) options.get("debug"));
  }

  private java.security.KeyStore keyStore;
  private String alias;
  private javax.security.auth.x500.X500Principal principal;
  private java.security.cert.Certificate[] fromKeyStore;
  private java.security.cert.CertPath certP;
  private javax.security.auth.x500.X500PrivateCredential privateCredential;

  public boolean login() throws LoginException {
    String url = (String) options.get("keyStoreURL");
    String type = (String) options.get("keyStoreType");
    if (type == null) {
      type = java.security.KeyStore.getDefaultType();
    }
    alias = (String) options.get("keyStoreAlias");
    boolean prot = "true".equalsIgnoreCase((String) options.get("protected"));
    if (url == null) {
      url = "file:" + System.getProperty("user.home") + java.io.File.separator + ".keystore";
    }
    char[] storePass = null;
    char[] keyPass = null;
    if (!prot) {
      if (callbackHandler == null) {
        throw new LoginException("Error: no CallbackHandler available to garner authentication information from the user");
      }
      javax.security.auth.callback.PasswordCallback pc =
          new javax.security.auth.callback.PasswordCallback("Keystore password: ", false);
      javax.security.auth.callback.NameCallback nc = new javax.security.auth.callback.NameCallback("Keystore alias: ");
      try {
        if (alias == null) {
          callbackHandler.handle(new javax.security.auth.callback.Callback[] {nc, pc});
          alias = nc.getName();
        } else {
          callbackHandler.handle(new javax.security.auth.callback.Callback[] {pc});
        }
      } catch (java.io.IOException ioe) {
        throw new LoginException(ioe.toString());
      } catch (javax.security.auth.callback.UnsupportedCallbackException uce) {
        throw new LoginException("Error: " + uce.getCallback().toString() + " not available to garner authentication information from the user");
      }
      storePass = pc.getPassword();
      keyPass = storePass;
    }
    try {
      keyStore = java.security.KeyStore.getInstance(type);
      java.io.InputStream in = new java.net.URL(url).openStream();
      try {
        keyStore.load(in, storePass);
      } finally {
        in.close();
      }
      if (alias == null) {
        throw new javax.security.auth.login.FailedLoginException("Please specify the alias of the key to use");
      }
      fromKeyStore = keyStore.getCertificateChain(alias);
      if (fromKeyStore == null || fromKeyStore.length == 0
          || !(fromKeyStore[0] instanceof java.security.cert.X509Certificate)) {
        throw new javax.security.auth.login.FailedLoginException("Unable to find X.509 certificate chain in keystore");
      }
      java.security.cert.X509Certificate cert0 = (java.security.cert.X509Certificate) fromKeyStore[0];
      principal = cert0.getSubjectX500Principal();
      java.security.cert.CertificateFactory cf = java.security.cert.CertificateFactory.getInstance("X.509");
      certP = cf.generateCertPath(java.util.Arrays.asList(fromKeyStore));
      java.security.Key privKey = keyStore.getKey(alias, keyPass);
      if (privKey == null || !(privKey instanceof java.security.PrivateKey)) {
        throw new javax.security.auth.login.FailedLoginException("Unable to recover key from keystore");
      }
      privateCredential = new javax.security.auth.x500.X500PrivateCredential(cert0, (java.security.PrivateKey) privKey, alias);
    } catch (LoginException e) {
      throw e;
    } catch (Exception e) {
      throw (LoginException) new javax.security.auth.login.FailedLoginException(e.toString()).initCause(e);
    }
    succeeded = true;
    return true;
  }

  public boolean commit() throws LoginException {
    if (!succeeded) {
      return false;
    }
    if (subject.isReadOnly()) {
      throw new LoginException("Subject is read-only");
    }
    subject.getPrincipals().add(principal);
    subject.getPublicCredentials().add(certP);
    subject.getPrivateCredentials().add(privateCredential);
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
    if (subject.isReadOnly()) {
      throw new LoginException("Subject is read-only");
    }
    subject.getPrincipals().remove(principal);
    subject.getPublicCredentials().remove(certP);
    subject.getPrivateCredentials().remove(privateCredential);
    succeeded = false;
    commitSucceeded = false;
    return true;
  }

}
