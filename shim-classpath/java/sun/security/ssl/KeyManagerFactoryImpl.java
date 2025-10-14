/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.ssl;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactorySpi;
import javax.net.ssl.ManagerFactoryParameters;

abstract class KeyManagerFactoryImpl extends KeyManagerFactorySpi {
  private X509KeyManagerImpl keyManager;
  private boolean isInitialized;

  KeyManagerFactoryImpl() {
  }

  @Override
  protected void engineInit(KeyStore ks, char[] password) throws KeyStoreException, NoSuchAlgorithmException,
      UnrecoverableKeyException {
    keyManager = new X509KeyManagerImpl(ks, password);
    isInitialized = true;
  }

  @Override
  protected void engineInit(ManagerFactoryParameters params) throws InvalidAlgorithmParameterException {
    throw new InvalidAlgorithmParameterException(unsupportedParametersMessage());
  }

  abstract String unsupportedParametersMessage();

  @Override
  protected KeyManager[] engineGetKeyManagers() {
    if (!isInitialized) {
      throw new IllegalStateException("KeyManagerFactoryImpl is not initialized");
    }
    return new KeyManager[] {keyManager};
  }

  public static final class SunX509 extends KeyManagerFactoryImpl {
    public SunX509() {
    }

    @Override
    String unsupportedParametersMessage() {
      return "SunX509KeyManager does not use ManagerFactoryParameters";
    }
  }

  public static final class X509 extends KeyManagerFactoryImpl {
    public X509() {
    }

    @Override
    String unsupportedParametersMessage() {
      return "Parameters must be instance of KeyStoreBuilderParameters";
    }
  }
}
