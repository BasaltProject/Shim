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
import java.security.PublicKey;
import java.security.cert.CertPath;
import java.security.cert.CertPathChecker;
import java.security.cert.CertPathParameters;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertPathValidatorException.BasicReason;
import java.security.cert.CertPathValidatorResult;
import java.security.cert.CertPathValidatorSpi;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.PKIXParameters;
import java.security.cert.PKIXReason;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.security.auth.x500.X500Principal;

public class PKIXCertPathValidator extends CertPathValidatorSpi {
  public PKIXCertPathValidator() {
  }

  @Override
  public CertPathChecker engineGetRevocationChecker() {
    throw new UnsupportedOperationException("revocation checking is not supported");
  }

  @Override
  public CertPathValidatorResult engineValidate(CertPath cp, CertPathParameters params)
      throws CertPathValidatorException, InvalidAlgorithmParameterException {
    if (!(params instanceof PKIXParameters)) {
      throw new InvalidAlgorithmParameterException("inappropriate parameters, must be an instance of "
          + "PKIXParameters");
    }
    if (!cp.getType().equals("X.509") && !cp.getType().equals("X509")) {
      throw new InvalidAlgorithmParameterException("inappropriate certification path type specified, must be X.509");
    }
    PKIXParameters pkixParams = (PKIXParameters) params;
    Set<TrustAnchor> anchors = pkixParams.getTrustAnchors();
    if (anchors == null || anchors.isEmpty()) {
      throw new InvalidAlgorithmParameterException("Trust anchors must be non-empty");
    }
    List<? extends Certificate> certList = cp.getCertificates();
    List<X509Certificate> certs = new ArrayList<X509Certificate>(certList.size());
    for (Certificate c : certList) {
      if (!(c instanceof X509Certificate)) {
        throw new CertPathValidatorException("Unsupported certificate type", null, cp, certs.size());
      }
      certs.add((X509Certificate) c);
    }
    if (certs.isEmpty()) {
      throw new CertPathValidatorException("Path does not chain with any of the trust anchors", null, null, -1,
          PKIXReason.NO_TRUST_ANCHOR);
    }
    CertPathValidatorException lastException = null;
    X500Principal topIssuer = certs.get(certs.size() - 1).getIssuerX500Principal();
    for (TrustAnchor anchor : anchors) {
      X500Principal caName = anchor.getCA();
      if (!caName.equals(topIssuer)) {
        X509Certificate trusted = anchor.getTrustedCert();
        if (trusted == null || !trusted.equals(certs.get(certs.size() - 1))) {
          continue;
        }
      }
      try {
        return validate(anchor, cp, certs, pkixParams);
      } catch (CertPathValidatorException e) {
        lastException = e;
      }
    }
    if (lastException != null) {
      throw lastException;
    }
    throw new CertPathValidatorException("Path does not chain with any of the trust anchors", null, null, -1,
        PKIXReason.NO_TRUST_ANCHOR);
  }

  private static final String[] kDisabledAlgs = {"MD2", "MD5"};

  private static boolean disabled(String sigAlg) {
    String upper = sigAlg.toUpperCase(java.util.Locale.ROOT);
    for (String d : kDisabledAlgs) {
      if (upper.startsWith(d + "WITH")) {
        return true;
      }
    }
    return false;
  }

  private PKIXCertPathValidatorResult validate(TrustAnchor anchor, CertPath cp, List<X509Certificate> certs,
      PKIXParameters params) throws CertPathValidatorException {
    PublicKey workingKey = anchor.getCAPublicKey();
    X500Principal workingIssuer = anchor.getCA();
    Date date = params.getDate();
    if (date == null) {
      date = new Date();
    }
    List<PKIXCertPathChecker> checkers = new ArrayList<PKIXCertPathChecker>(params.getCertPathCheckers());
    for (PKIXCertPathChecker ck : checkers) {
      ck.init(false);
    }
    Set<String> supportedExts = new HashSet<String>(java.util.Arrays.asList("2.5.29.15", "2.5.29.19", "2.5.29.17",
        "2.5.29.18", "2.5.29.37", "2.5.29.14", "2.5.29.35", "2.5.29.30", "2.5.29.31", "2.5.29.32", "2.5.29.33",
        "2.5.29.36", "2.5.29.54", "2.5.29.16", "1.3.6.1.5.5.7.1.1", "1.3.6.1.5.5.7.1.11",
        "2.16.840.1.113730.1.1"));
    for (PKIXCertPathChecker ck : checkers) {
      Set<String> s = ck.getSupportedExtensions();
      if (s != null) {
        supportedExts.addAll(s);
      }
    }
    int n = certs.size();
    int maxPathLength = n;
    for (int i = n - 1; i >= 0; i--) {
      X509Certificate cert = certs.get(i);
      int depth = n - 1 - i;
      try {
        cert.checkValidity(date);
      } catch (CertificateExpiredException e) {
        throw new CertPathValidatorException("validity check failed", e, cp, i, BasicReason.EXPIRED);
      } catch (CertificateNotYetValidException e) {
        throw new CertPathValidatorException("validity check failed", e, cp, i, BasicReason.NOT_YET_VALID);
      }
      if (!cert.getIssuerX500Principal().equals(workingIssuer)) {
        throw new CertPathValidatorException("subject/issuer name chaining check failed", null, cp, i,
            PKIXReason.NAME_CHAINING);
      }
      if (disabled(cert.getSigAlgName()) && i == 0 || disabled(cert.getSigAlgName())) {
        throw new CertPathValidatorException("Algorithm constraints check failed on signature algorithm: "
            + cert.getSigAlgName(), null, cp, i, BasicReason.ALGORITHM_CONSTRAINED);
      }
      try {
        cert.verify(workingKey, params.getSigProvider() == null ? "" : params.getSigProvider());
      } catch (GeneralSecurityException e) {
        throw new CertPathValidatorException("signature check failed", e, cp, i, BasicReason.INVALID_SIGNATURE);
      }
      Set<String> unresolved = new HashSet<String>();
      Set<String> crit = cert.getCriticalExtensionOIDs();
      if (crit != null) {
        unresolved.addAll(crit);
      }
      for (PKIXCertPathChecker ck : checkers) {
        ck.check(cert, unresolved);
      }
      unresolved.removeAll(supportedExts);
      if (!unresolved.isEmpty()) {
        throw new CertPathValidatorException("unrecognized critical extension(s)", null, cp, i,
            PKIXReason.UNRECOGNIZED_CRIT_EXT);
      }
      if (i > 0) {
        int bc = cert.getBasicConstraints();
        if (bc < 0) {
          throw new CertPathValidatorException("basic constraints check failed: this is not a CA certificate", null,
              cp, i, PKIXReason.NOT_CA_CERT);
        }
        boolean[] ku = cert.getKeyUsage();
        if (ku != null && !ku[5]) {
          throw new CertPathValidatorException("key usage check failed: keyCertSign bit is not set", null, cp, i,
              PKIXReason.INVALID_KEY_USAGE);
        }
        if (bc < maxPathLength - 1 - depth + depth) {
          // pathLenConstraint counts remaining non-self-issued intermediates below this CA
        }
        int below = i - 1;
        if (bc != Integer.MAX_VALUE && below > bc) {
          throw new CertPathValidatorException("basic constraints check failed: pathLenConstraint violated, this "
              + "cert must be the last certificate in the certification path", null, cp, i, PKIXReason.PATH_TOO_LONG);
        }
      }
      workingKey = cert.getPublicKey();
      workingIssuer = cert.getSubjectX500Principal();
    }
    return new PKIXCertPathValidatorResult(anchor, null, workingKey);
  }
}
