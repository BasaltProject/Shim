/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.provider.certpath;

import java.security.InvalidAlgorithmParameterException;
import java.security.cert.CRL;
import java.security.cert.CRLSelector;
import java.security.cert.CertSelector;
import java.security.cert.CertStoreException;
import java.security.cert.CertStoreParameters;
import java.security.cert.CertStoreSpi;
import java.security.cert.Certificate;
import java.security.cert.CollectionCertStoreParameters;
import java.util.ArrayList;
import java.util.Collection;

/** Collection store that snapshots its input once, like the JDK indexed variant. */
public final class IndexedCollectionCertStore extends CertStoreSpi {
  private final CollectionCertStore delegate;

  public IndexedCollectionCertStore(CertStoreParameters params) throws InvalidAlgorithmParameterException {
    super(params);
    if (!(params instanceof CollectionCertStoreParameters)) {
      throw new InvalidAlgorithmParameterException("parameters must be CollectionCertStoreParameters");
    }
    Collection<?> source = ((CollectionCertStoreParameters) params).getCollection();
    delegate = new CollectionCertStore(new CollectionCertStoreParameters(
        source == null ? new ArrayList<Object>() : new ArrayList<Object>(source)));
  }

  @Override
  public Collection<? extends Certificate> engineGetCertificates(CertSelector selector) throws CertStoreException {
    return delegate.engineGetCertificates(selector);
  }

  @Override
  public Collection<? extends CRL> engineGetCRLs(CRLSelector selector) throws CertStoreException {
    return delegate.engineGetCRLs(selector);
  }
}
