/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.net.ssl;

import java.security.Principal;

public abstract class X509ExtendedKeyManager implements X509KeyManager {
  protected X509ExtendedKeyManager() {
  }

  public String chooseEngineClientAlias(String[] keyType, Principal[] issuers, SSLEngine engine) {
    return null;
  }

  public String chooseEngineServerAlias(String keyType, Principal[] issuers, SSLEngine engine) {
    return null;
  }
}
