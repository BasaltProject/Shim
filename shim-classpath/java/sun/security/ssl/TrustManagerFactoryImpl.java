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
import java.security.cert.CertPathParameters;
import java.security.cert.PKIXBuilderParameters;
import javax.net.ssl.CertPathTrustManagerParameters;
import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactorySpi;

abstract class TrustManagerFactoryImpl extends TrustManagerFactorySpi {
  private X509TrustManagerImpl trustManager;
  private boolean isInitialized;

  TrustManagerFactoryImpl() {
  }

  abstract boolean isPkix();

  @Override
  protected void engineInit(KeyStore ks) throws KeyStoreException {
    if (ks == null) {
      try {
        trustManager = new X509TrustManagerImpl(isPkix(), TrustStoreManager.getTrustedCerts());
      } catch (SecurityException se) {
        trustManager = new X509TrustManagerImpl(isPkix(), null);
      } catch (Exception e) {
        throw new KeyStoreException("problem accessing trust store", e);
      }
    } else {
      trustManager = new X509TrustManagerImpl(isPkix(), TrustStoreManager.trustedCerts(ks));
    }
    isInitialized = true;
  }

  @Override
  protected void engineInit(ManagerFactoryParameters spec) throws InvalidAlgorithmParameterException {
    if (spec instanceof CertPathTrustManagerParameters) {
      CertPathParameters params = ((CertPathTrustManagerParameters) spec).getParameters();
      if (!(params instanceof PKIXBuilderParameters)) {
        throw new InvalidAlgorithmParameterException("Encountered unsupported parameters: " + params.getClass()
            + "; PKIXBuilderParameters expected");
      }
      trustManager = new X509TrustManagerImpl((PKIXBuilderParameters) params);
      isInitialized = true;
    } else {
      throw new InvalidAlgorithmParameterException("Parameters must be CertPathTrustManagerParameters");
    }
  }

  @Override
  protected TrustManager[] engineGetTrustManagers() {
    if (!isInitialized) {
      throw new IllegalStateException("TrustManagerFactoryImpl is not initialized");
    }
    return new TrustManager[] {trustManager};
  }

  public static final class SimpleFactory extends TrustManagerFactoryImpl {
    public SimpleFactory() {
    }

    @Override
    boolean isPkix() {
      return false;
    }
  }

  public static final class PKIXFactory extends TrustManagerFactoryImpl {
    public PKIXFactory() {
    }

    @Override
    boolean isPkix() {
      return true;
    }
  }
}
