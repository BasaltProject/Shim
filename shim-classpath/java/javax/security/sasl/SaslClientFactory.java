/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.security.sasl;

public interface SaslClientFactory {
  javax.security.sasl.SaslClient createSaslClient(java.lang.String[] strings, java.lang.String string, java.lang.String string2, java.lang.String string3, java.util.Map<java.lang.String, ?> map, javax.security.auth.callback.CallbackHandler callbackHandler) throws javax.security.sasl.SaslException;
  java.lang.String[] getMechanismNames(java.util.Map<java.lang.String, ?> map);
}
