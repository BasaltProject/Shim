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
import java.security.SecureRandom;
import java.util.Map;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.AuthorizeCallback;
import javax.security.sasl.SaslException;
import javax.security.sasl.SaslServer;

final class CramMD5Server implements SaslServer {
  private final String fqdn;
  private final CallbackHandler cbh;
  private byte[] challenge;
  private boolean completed = false;
  private boolean aborted = false;
  private String authzid;

  CramMD5Server(String protocol, String serverFqdn, Map<String, ?> props, CallbackHandler cbh)
      throws SaslException {
    if (serverFqdn == null) {
      throw new SaslException("CRAM-MD5: fully qualified server name must be specified");
    }
    this.fqdn = serverFqdn;
    this.cbh = cbh;
  }

  public String getMechanismName() {
    return "CRAM-MD5";
  }

  public byte[] evaluateResponse(byte[] responseData) throws SaslException {
    if (completed) {
      throw new IllegalStateException("CRAM-MD5 authentication already completed");
    }
    if (aborted) {
      throw new IllegalStateException("CRAM-MD5 authentication previously aborted due to error");
    }
    try {
      if (challenge == null) {
        SecureRandom r = new SecureRandom();
        challenge = ("<" + Long.toString(Math.abs(r.nextLong()), 36) + "." + System.currentTimeMillis() + "@" + fqdn
            + ">").getBytes(StandardCharsets.UTF_8);
        return challenge.clone();
      }
      String resp = new String(responseData, StandardCharsets.UTF_8);
      int sp = resp.lastIndexOf(' ');
      if (sp < 0) {
        aborted = true;
        throw new SaslException("Invalid response");
      }
      String user = resp.substring(0, sp);
      String digest = resp.substring(sp + 1);
      NameCallback ncb = new NameCallback("CRAM-MD5 authentication ID: ", user);
      PasswordCallback pcb = new PasswordCallback("CRAM-MD5 password: ", false);
      cbh.handle(new Callback[] {ncb, pcb});
      char[] pwChars = pcb.getPassword();
      if (pwChars == null) {
        aborted = true;
        throw new SaslException("Invalid response");
      }
      byte[] pw = new String(pwChars).getBytes(StandardCharsets.UTF_8);
      pcb.clearPassword();
      String expected = SaslUtil.hex(SaslUtil.hmacMd5(pw, challenge));
      java.util.Arrays.fill(pw, (byte) 0);
      if (!java.security.MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
          digest.getBytes(StandardCharsets.UTF_8))) {
        aborted = true;
        throw new SaslException("Invalid response");
      }
      AuthorizeCallback acb = new AuthorizeCallback(user, user);
      cbh.handle(new Callback[] {acb});
      if (acb.isAuthorized()) {
        authzid = acb.getAuthorizedID();
      } else {
        aborted = true;
        throw new SaslException("CRAM-MD5: user not authorized: " + user);
      }
      completed = true;
      return null;
    } catch (SaslException e) {
      throw e;
    } catch (UnsupportedCallbackException e) {
      aborted = true;
      throw new SaslException("CRAM-MD5: Cannot perform callback to acquire password", e);
    } catch (IOException e) {
      aborted = true;
      throw new SaslException("CRAM-MD5: Error acquiring password", e);
    }
  }

  public boolean isComplete() {
    return completed;
  }

  public String getAuthorizationID() {
    if (completed) {
      return authzid;
    } else {
      throw new IllegalStateException("CRAM-MD5 authentication not completed");
    }
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
      return propName.equals(javax.security.sasl.Sasl.QOP) ? "auth" : null;
    } else {
      throw new IllegalStateException("CRAM-MD5 authentication not completed");
    }
  }

  public void dispose() throws SaslException {
  }
}
