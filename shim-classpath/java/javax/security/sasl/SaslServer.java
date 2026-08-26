/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.security.sasl;

public interface SaslServer {
  void dispose() throws javax.security.sasl.SaslException;
  byte[] evaluateResponse(byte[] bs) throws javax.security.sasl.SaslException;
  java.lang.String getAuthorizationID();
  java.lang.String getMechanismName();
  java.lang.Object getNegotiatedProperty(java.lang.String string);
  boolean isComplete();
  byte[] unwrap(byte[] bs, int i, int i2) throws javax.security.sasl.SaslException;
  byte[] wrap(byte[] bs, int i, int i2) throws javax.security.sasl.SaslException;
}
