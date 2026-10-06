/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.security.sasl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslException;

final class PlainClient implements SaslClient {
  private boolean completed = false;
  private byte[] pw;


  PlainClient(String authorizationId, CallbackHandler cbh) throws SaslException {
    if (cbh == null) {
      throw new SaslException("Callback handler to get username/password required");
    }
    String authenticationId;
    byte[] passwd;
    try {
      NameCallback nc = new NameCallback("PLAIN authentication id: ");
      PasswordCallback pcb = new PasswordCallback("PLAIN password: ", false);
      cbh.handle(new Callback[] {nc, pcb});
      authenticationId = nc.getName();
      char[] pwChars = pcb.getPassword();
      passwd = pwChars == null ? null : new String(pwChars).getBytes(StandardCharsets.UTF_8);
      pcb.clearPassword();
    } catch (SaslException e) {
      throw e;
    } catch (UnsupportedCallbackException e) {
      throw new SaslException("PLAIN: Cannot perform callback to acquire authentication ID and password", e);
    } catch (IOException e) {
      throw new SaslException("PLAIN: Error acquiring authentication ID and password", e);
    }
    if (authenticationId == null || passwd == null) {
      throw new SaslException("PLAIN: authentication ID and password must be specified");
    }
    byte[] authz = authorizationId == null ? new byte[0] : authorizationId.getBytes(StandardCharsets.UTF_8);
    byte[] authn = authenticationId.getBytes(StandardCharsets.UTF_8);
    pw = new byte[authz.length + 1 + authn.length + 1 + passwd.length];
    System.arraycopy(authz, 0, pw, 0, authz.length);
    pw[authz.length] = 0;
    System.arraycopy(authn, 0, pw, authz.length + 1, authn.length);
    pw[authz.length + 1 + authn.length] = 0;
    System.arraycopy(passwd, 0, pw, authz.length + authn.length + 2, passwd.length);
    java.util.Arrays.fill(passwd, (byte) 0);
  }

  public String getMechanismName() {
    return "PLAIN";
  }

  public boolean isComplete() {
    return completed;
  }

  public byte[] unwrap(byte[] incoming, int offset, int len) throws SaslException {
    if (completed) {
      throw new IllegalStateException("PLAIN supports neither integrity nor privacy");
    } else {
      throw new IllegalStateException("PLAIN authentication not completed");
    }
  }

  public byte[] wrap(byte[] outgoing, int offset, int len) throws SaslException {
    if (completed) {
      throw new IllegalStateException("PLAIN supports neither integrity nor privacy");
    } else {
      throw new IllegalStateException("PLAIN authentication not completed");
    }
  }

  public Object getNegotiatedProperty(String propName) {
    if (completed) {
      if (propName.equals(javax.security.sasl.Sasl.QOP)) {
        return "auth";
      } else {
        return null;
      }
    } else {
      throw new IllegalStateException("PLAIN authentication not completed");
    }
  }

  public void dispose() throws SaslException {
  }

  public boolean hasInitialResponse() {
    return true;
  }

  public byte[] evaluateChallenge(byte[] challenge) throws SaslException {
    if (completed) {
      throw new IllegalStateException("PLAIN authentication already completed");
    }
    completed = true;
    return pw;
  }

}
