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

final class CramMD5Client implements SaslClient {
  private boolean completed = false;
  private final String authenticationId;
  private final byte[] pw;


  CramMD5Client(CallbackHandler cbh) throws SaslException {
    if (cbh == null) {
      throw new SaslException("Callback handler to get username/password required");
    }
    try {
      NameCallback nc = new NameCallback("CRAM-MD5 authentication id: ");
      PasswordCallback pcb = new PasswordCallback("CRAM-MD5 password: ", false);
      cbh.handle(new Callback[] {nc, pcb});
      char[] pwChars = pcb.getPassword();
      if (nc.getName() == null || pwChars == null) {
        throw new SaslException("CRAM-MD5: authentication ID and password must be specified");
      }
      authenticationId = nc.getName();
      pw = new String(pwChars).getBytes(StandardCharsets.UTF_8);
      pcb.clearPassword();
    } catch (SaslException e) {
      throw e;
    } catch (UnsupportedCallbackException e) {
      throw new SaslException("CRAM-MD5: Cannot perform callback to acquire authentication ID and password", e);
    } catch (IOException e) {
      throw new SaslException("CRAM-MD5: Error acquiring authentication ID and password", e);
    }
  }

  public String getMechanismName() {
    return "CRAM-MD5";
  }

  public boolean isComplete() {
    return completed;
  }

  public byte[] unwrap(byte[] incoming, int offset, int len) throws SaslException {
    if (completed) {
      throw new IllegalStateException("CRAM-MD5 supports neither integrity nor privacy");
    } else {
      throw new IllegalStateException("CRAM-MD5 authentication not completed");
    }
  }

  public byte[] wrap(byte[] outgoing, int offset, int len) throws SaslException {
    if (completed) {
      throw new IllegalStateException("CRAM-MD5 supports neither integrity nor privacy");
    } else {
      throw new IllegalStateException("CRAM-MD5 authentication not completed");
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
      throw new IllegalStateException("CRAM-MD5 authentication not completed");
    }
  }

  public void dispose() throws SaslException {
  }

  public boolean hasInitialResponse() {
    return false;
  }

  public byte[] evaluateChallenge(byte[] challengeData) throws SaslException {
    if (completed) {
      throw new IllegalStateException("CRAM-MD5 authentication already completed");
    }
    completed = true;
    try {
      String hmac = SaslUtil.hex(SaslUtil.hmacMd5(pw, challengeData));
      byte[] resp = (authenticationId + " " + hmac).getBytes(StandardCharsets.UTF_8);
      java.util.Arrays.fill(pw, (byte) 0);
      return resp;
    } catch (RuntimeException e) {
      throw new SaslException("CRAM-MD5: MD5 digest unavailable", e);
    }
  }

}
