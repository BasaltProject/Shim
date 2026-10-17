/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.net.ssl;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;

public abstract class TrustManagerFactorySpi {
  public TrustManagerFactorySpi() {
  }

  protected abstract void engineInit(KeyStore ks) throws KeyStoreException;

  protected abstract void engineInit(ManagerFactoryParameters spec) throws InvalidAlgorithmParameterException;

  protected abstract TrustManager[] engineGetTrustManagers();
}
