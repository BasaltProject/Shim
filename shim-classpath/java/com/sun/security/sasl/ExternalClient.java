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

final class ExternalClient implements SaslClient {
  private boolean completed = false;
  private final byte[] authorizationID;


  ExternalClient(String authorizationID) {
    if (authorizationID != null) {
      this.authorizationID = authorizationID.getBytes(StandardCharsets.UTF_8);
    } else {
      this.authorizationID = new byte[0];
    }
  }

  public String getMechanismName() {
    return "EXTERNAL";
  }

  public boolean isComplete() {
    return completed;
  }

  public byte[] unwrap(byte[] incoming, int offset, int len) throws SaslException {
    if (completed) {
      throw new IllegalStateException("EXTERNAL supports neither integrity nor privacy");
    } else {
      throw new IllegalStateException("EXTERNAL authentication not completed");
    }
  }

  public byte[] wrap(byte[] outgoing, int offset, int len) throws SaslException {
    if (completed) {
      throw new IllegalStateException("EXTERNAL supports neither integrity nor privacy");
    } else {
      throw new IllegalStateException("EXTERNAL authentication not completed");
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
      throw new IllegalStateException("EXTERNAL authentication not completed");
    }
  }

  public void dispose() throws SaslException {
  }

  public boolean hasInitialResponse() {
    return true;
  }

  public byte[] evaluateChallenge(byte[] challenge) throws SaslException {
    if (completed) {
      throw new IllegalStateException("EXTERNAL authentication already completed");
    }
    completed = true;
    return authorizationID;
  }

}
