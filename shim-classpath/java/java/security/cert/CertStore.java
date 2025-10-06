/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.cert;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.util.Collection;
import java.util.Objects;
import sun.security.jca.GetInstance;

public class CertStore {
  private static final String CERTSTORE_TYPE = "certstore.type";
  private final CertStoreSpi storeSpi;
  private final Provider provider;
  private final String type;
  private final CertStoreParameters params;

  protected CertStore(CertStoreSpi storeSpi, Provider provider, String type, CertStoreParameters params) {
    this.storeSpi = storeSpi;
    this.provider = provider;
    this.type = type;
    if (params != null) {
      this.params = (CertStoreParameters) params.clone();
    } else {
      this.params = null;
    }
  }

  public final Collection<? extends Certificate> getCertificates(CertSelector selector) throws CertStoreException {
    return storeSpi.engineGetCertificates(selector);
  }

  public final Collection<? extends CRL> getCRLs(CRLSelector selector) throws CertStoreException {
    return storeSpi.engineGetCRLs(selector);
  }

  public static CertStore getInstance(String type, CertStoreParameters params)
      throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
    Objects.requireNonNull(type, "null type name");
    try {
      GetInstance.Instance instance = GetInstance.getInstance("CertStore", CertStoreSpi.class, type, params);
      return new CertStore((CertStoreSpi) instance.impl, instance.provider, type, params);
    } catch (NoSuchAlgorithmException e) {
      handleException(e);
      throw e;
    }
  }

  private static void handleException(NoSuchAlgorithmException e) throws NoSuchAlgorithmException,
      InvalidAlgorithmParameterException {
    Throwable cause = e.getCause();
    if (cause instanceof InvalidAlgorithmParameterException) {
      throw (InvalidAlgorithmParameterException) cause;
    }
    throw e;
  }

  public static CertStore getInstance(String type, CertStoreParameters params, String provider)
      throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
    Objects.requireNonNull(type, "null type name");
    try {
      GetInstance.Instance instance = GetInstance.getInstance("CertStore", CertStoreSpi.class, type, params,
          provider);
      return new CertStore((CertStoreSpi) instance.impl, instance.provider, type, params);
    } catch (NoSuchAlgorithmException e) {
      handleException(e);
      throw e;
    }
  }

  public static CertStore getInstance(String type, CertStoreParameters params, Provider provider)
      throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
    Objects.requireNonNull(type, "null type name");
    try {
      GetInstance.Instance instance = GetInstance.getInstance("CertStore", CertStoreSpi.class, type, params,
          provider);
      return new CertStore((CertStoreSpi) instance.impl, instance.provider, type, params);
    } catch (NoSuchAlgorithmException e) {
      handleException(e);
      throw e;
    }
  }

  public final CertStoreParameters getCertStoreParameters() {
    return (params == null ? null : (CertStoreParameters) params.clone());
  }

  public final String getType() {
    return this.type;
  }

  public final Provider getProvider() {
    return this.provider;
  }

  public static final String getDefaultType() {
    String cstype = Security.getProperty(CERTSTORE_TYPE);
    if (cstype == null) {
      cstype = "LDAP";
    }
    return cstype;
  }
}
