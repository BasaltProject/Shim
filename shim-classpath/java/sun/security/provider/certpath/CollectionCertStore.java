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
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashSet;

public class CollectionCertStore extends CertStoreSpi {
  private final Collection<?> coll;

  public CollectionCertStore(CertStoreParameters params) throws InvalidAlgorithmParameterException {
    super(params);
    if (!(params instanceof CollectionCertStoreParameters)) {
      throw new InvalidAlgorithmParameterException("parameters must be CollectionCertStoreParameters");
    }
    coll = ((CollectionCertStoreParameters) params).getCollection();
  }

  @Override
  public Collection<Certificate> engineGetCertificates(CertSelector selector) throws CertStoreException {
    if (coll == null) {
      return Collections.<Certificate>emptySet();
    }
    HashSet<Certificate> result = new HashSet<Certificate>();
    try {
      for (Object o : coll) {
        if (o instanceof Certificate && (selector == null || selector.match((Certificate) o))) {
          result.add((Certificate) o);
        }
      }
    } catch (ConcurrentModificationException e) {
      throw new CertStoreException(e);
    }
    return result;
  }

  @Override
  public Collection<CRL> engineGetCRLs(CRLSelector selector) throws CertStoreException {
    if (coll == null) {
      return Collections.<CRL>emptySet();
    }
    HashSet<CRL> result = new HashSet<CRL>();
    try {
      for (Object o : coll) {
        if (o instanceof CRL && (selector == null || selector.match((CRL) o))) {
          result.add((CRL) o);
        }
      }
    } catch (ConcurrentModificationException e) {
      throw new CertStoreException(e);
    }
    return result;
  }
}
