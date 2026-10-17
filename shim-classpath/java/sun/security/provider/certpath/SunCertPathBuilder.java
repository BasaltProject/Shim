/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.provider.certpath;

import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertPath;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertPathBuilderResult;
import java.security.cert.CertPathBuilderSpi;
import java.security.cert.CertPathChecker;
import java.security.cert.CertPathParameters;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorResult;
import java.security.cert.CertSelector;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public final class SunCertPathBuilder extends CertPathBuilderSpi {
  public SunCertPathBuilder() {
  }

  @Override
  public CertPathChecker engineGetRevocationChecker() {
    throw new UnsupportedOperationException("revocation checking is not supported");
  }

  @Override
  public CertPathBuilderResult engineBuild(CertPathParameters params) throws CertPathBuilderException,
      InvalidAlgorithmParameterException {
    if (!(params instanceof PKIXBuilderParameters)) {
      throw new InvalidAlgorithmParameterException("inappropriate parameter type, must be an instance of "
          + "PKIXBuilderParameters.class");
    }
    PKIXBuilderParameters pp = (PKIXBuilderParameters) params;
    CertSelector selector = pp.getTargetCertConstraints();
    if (!(selector instanceof X509CertSelector)) {
      throw new InvalidAlgorithmParameterException("the targetCertConstraints parameter must be an "
          + "X509CertSelector");
    }
    List<X509Certificate> pool = new ArrayList<X509Certificate>();
    try {
      for (CertStore store : pp.getCertStores()) {
        for (Certificate c : store.getCertificates(null)) {
          if (c instanceof X509Certificate && !pool.contains(c)) {
            pool.add((X509Certificate) c);
          }
        }
      }
    } catch (CertStoreException e) {
      throw new CertPathBuilderException(e);
    }
    List<X509Certificate> targets = new ArrayList<X509Certificate>();
    for (X509Certificate c : pool) {
      if (selector.match(c)) {
        targets.add(c);
      }
    }
    for (TrustAnchor ta : pp.getTrustAnchors()) {
      if (ta.getTrustedCert() != null && selector.match(ta.getTrustedCert())) {
        targets.add(ta.getTrustedCert());
      }
    }
    CertPathValidator validator;
    try {
      validator = CertPathValidator.getInstance("PKIX");
    } catch (NoSuchAlgorithmException e) {
      throw new CertPathBuilderException(e);
    }
    CertPathBuilderException last = null;
    int maxLen = pp.getMaxPathLength();
    for (X509Certificate target : targets) {
      List<X509Certificate> chain = new ArrayList<X509Certificate>();
      chain.add(target);
      Result r = extend(chain, pool, pp, validator, maxLen);
      if (r != null) {
        return r.result;
      }
    }
    throw new CertPathBuilderException("unable to find valid certification path to requested target");
  }

  private static final class Result {
    final PKIXCertPathBuilderResult result;

    Result(PKIXCertPathBuilderResult result) {
      this.result = result;
    }
  }

  private Result extend(List<X509Certificate> chain, List<X509Certificate> pool, PKIXBuilderParameters pp,
      CertPathValidator validator, int maxLen) throws CertPathBuilderException {
    X509Certificate last = chain.get(chain.size() - 1);
    try {
      CertPath path = CertificateFactory.getInstance("X.509").generateCertPath(chain);
      PKIXParameters vp = (PKIXParameters) pp.clone();
      CertPathValidatorResult res = validator.validate(path, vp);
      PKIXCertPathValidatorResult pr = (PKIXCertPathValidatorResult) res;
      return new Result(new PKIXCertPathBuilderResult(path, pr.getTrustAnchor(), pr.getPolicyTree(),
          pr.getPublicKey()));
    } catch (GeneralSecurityException e) {
      // continue extending the chain below
    }
    if (maxLen != -1 && chain.size() > maxLen) {
      return null;
    }
    for (X509Certificate cand : pool) {
      if (chain.contains(cand) || !cand.getSubjectX500Principal().equals(last.getIssuerX500Principal())) {
        continue;
      }
      if (last.getIssuerX500Principal().equals(last.getSubjectX500Principal())) {
        continue;
      }
      try {
        last.verify(cand.getPublicKey());
      } catch (GeneralSecurityException e) {
        continue;
      }
      chain.add(cand);
      Result r = extend(chain, pool, pp, validator, maxLen);
      if (r != null) {
        return r;
      }
      chain.remove(chain.size() - 1);
    }
    return null;
  }
}
