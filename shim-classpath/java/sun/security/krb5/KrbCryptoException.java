/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package sun.security.krb5;

public class KrbCryptoException extends KrbException {
  private static final long serialVersionUID = 4668713551704977156L;

  public KrbCryptoException(String s) {
    super(s);
  }

  public KrbCryptoException(String s, Throwable cause) {
    super(s);
    initCause(cause);
  }
}
