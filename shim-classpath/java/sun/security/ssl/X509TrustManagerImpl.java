/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.ssl;

import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.cert.CertPath;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.net.ssl.ExtendedSSLSession;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.X509ExtendedTrustManager;
import sun.security.util.HostnameChecker;

final class X509TrustManagerImpl extends X509ExtendedTrustManager {
  private final boolean pkix;
  private final Collection<X509Certificate> trusted;
  private final PKIXBuilderParameters pkixParams;

  X509TrustManagerImpl(boolean pkix, Collection<X509Certificate> trustedCerts) {
    this.pkix = pkix;
    this.trusted = trustedCerts == null ? new ArrayList<X509Certificate>()
        : new ArrayList<X509Certificate>(trustedCerts);
    this.pkixParams = null;
  }

  X509TrustManagerImpl(PKIXBuilderParameters params) {
    this.pkix = true;
    this.trusted = new ArrayList<X509Certificate>();
    for (TrustAnchor ta : params.getTrustAnchors()) {
      X509Certificate c = ta.getTrustedCert();
      if (c != null) {
        trusted.add(c);
      }
    }
    this.pkixParams = params;
  }

  @Override
  public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
    checkTrusted(chain, authType, (Socket) null, null, false);
  }

  @Override
  public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
    checkTrusted(chain, authType, (Socket) null, null, true);
  }

  @Override
  public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket)
      throws CertificateException {
    checkTrusted(chain, authType, socket, null, false);
  }

  @Override
  public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket)
      throws CertificateException {
    checkTrusted(chain, authType, socket, null, true);
  }

  @Override
  public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine)
      throws CertificateException {
    checkTrusted(chain, authType, null, engine, false);
  }

  @Override
  public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine)
      throws CertificateException {
    checkTrusted(chain, authType, null, engine, true);
  }

  @Override
  public X509Certificate[] getAcceptedIssuers() {
    return trusted.toArray(new X509Certificate[0]);
  }

  private void checkTrusted(X509Certificate[] chain, String authType, Socket socket, SSLEngine engine,
      boolean server) throws CertificateException {
    if (chain == null || chain.length == 0) {
      throw new IllegalArgumentException("null or zero-length certificate chain");
    }
    if (authType == null || authType.isEmpty()) {
      throw new IllegalArgumentException("null or zero-length authentication type");
    }
    SSLSession session = null;
    String identityAlg = null;
    if (socket instanceof SSLSocket) {
      SSLSocket sslSocket = (SSLSocket) socket;
      session = sslSocket.getHandshakeSession();
      if (session == null) {
        throw new CertificateException("No handshake session");
      }
      identityAlg = sslSocket.getSSLParameters().getEndpointIdentificationAlgorithm();
    } else if (engine != null) {
      session = engine.getHandshakeSession();
      if (session == null) {
        throw new CertificateException("No handshake session");
      }
      identityAlg = engine.getSSLParameters().getEndpointIdentificationAlgorithm();
    }
    X509Certificate[] trustedChain = validate(chain, server);
    if (identityAlg != null && !identityAlg.isEmpty() && session != null) {
      checkIdentity(session, trustedChain, identityAlg, !server);
    }
  }

  private static void checkIdentity(SSLSession session, X509Certificate[] chain, String algorithm,
      boolean checkClientTrusted) throws CertificateException {
    String hostname = session.getPeerHost();
    if (session instanceof ExtendedSSLSession) {
      List<SNIServerName> names;
      try {
        names = ((ExtendedSSLSession) session).getRequestedServerNames();
      } catch (UnsupportedOperationException e) {
        names = null;
      }
      if (names != null) {
        for (SNIServerName name : names) {
          if (name instanceof SNIHostName) {
            hostname = ((SNIHostName) name).getAsciiName();
            break;
          }
        }
      }
    }
    if (hostname == null) {
      throw new CertificateException("No hostname to verify");
    }
    String upper = algorithm.toUpperCase(Locale.ENGLISH);
    if (upper.equals("HTTPS")) {
      HostnameChecker.getInstance(HostnameChecker.kTypeTls).match(hostname, chain[0], false);
    } else if (upper.equals("LDAP") || upper.equals("LDAPS")) {
      HostnameChecker.getInstance(HostnameChecker.kTypeLdap).match(hostname, chain[0], false);
    } else {
      throw new CertificateException("Unknown identification algorithm: " + algorithm);
    }
  }

  private X509Certificate[] validate(X509Certificate[] chain, boolean server) throws CertificateException {
    X509Certificate[] ordered = order(chain);
    X509Certificate leaf = ordered[0];
    List<X509Certificate> path = new ArrayList<X509Certificate>();
    X509Certificate anchor = null;
    for (int i = 0; i < ordered.length && anchor == null; i++) {
      if (trusted.contains(ordered[i])) {
        anchor = ordered[i];
        break;
      }
      path.add(ordered[i]);
    }
    if (anchor == null) {
      X509Certificate last = path.get(path.size() - 1);
      for (X509Certificate t : trusted) {
        if (t.getSubjectX500Principal().equals(last.getIssuerX500Principal()) && verifies(last, t)) {
          anchor = t;
          break;
        }
      }
    }
    if (anchor == null) {
      CertPathBuilderException cause = new CertPathBuilderException(
          "unable to find valid certification path to requested target");
      throw new ValidatorException("PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: "
          + "unable to find valid certification path to requested target", cause);
    }
    if (!path.isEmpty()) {
      try {
        CertPath cp = CertificateFactory.getInstance("X.509").generateCertPath(path);
        PKIXParameters params = new PKIXParameters(Collections.singleton(new TrustAnchor(anchor, null)));
        params.setRevocationEnabled(false);
        if (pkixParams != null) {
          params.setDate(pkixParams.getDate());
        }
        CertPathValidator.getInstance("PKIX").validate(cp, params);
      } catch (CertPathValidatorException e) {
        throw new ValidatorException("PKIX path validation failed: " + e, e);
      } catch (GeneralSecurityException e) {
        throw new ValidatorException("PKIX path validation failed: " + e, e);
      }
    } else {
      try {
        leaf.checkValidity();
      } catch (CertificateException e) {
        throw new ValidatorException("PKIX path validation failed: " + e, e);
      }
    }
    checkExtendedKeyUsage(leaf, server);
    X509Certificate[] result = new X509Certificate[path.size() + 1];
    for (int i = 0; i < path.size(); i++) {
      result[i] = path.get(i);
    }
    result[path.size()] = anchor;
    return result;
  }

  private static void checkExtendedKeyUsage(X509Certificate cert, boolean server) throws CertificateException {
    List<String> eku = cert.getExtendedKeyUsage();
    if (eku == null) {
      return;
    }
    String need = server ? "1.3.6.1.5.5.7.3.1" : "1.3.6.1.5.5.7.3.2";
    if (eku.contains(need) || eku.contains("2.5.29.37.0")) {
      return;
    }
    throw new ValidatorException("Extended key usage does not permit use for TLS "
        + (server ? "server" : "client") + " authentication");
  }

  private static boolean verifies(X509Certificate cert, X509Certificate issuer) {
    try {
      cert.verify(issuer.getPublicKey());
      return true;
    } catch (GeneralSecurityException e) {
      return false;
    }
  }

  private static X509Certificate[] order(X509Certificate[] chain) {
    if (chain.length < 2) {
      return chain;
    }
    List<X509Certificate> rest = new ArrayList<X509Certificate>(Arrays.asList(chain));
    List<X509Certificate> out = new ArrayList<X509Certificate>();
    X509Certificate cur = rest.remove(0);
    out.add(cur);
    boolean progress = true;
    while (progress && !rest.isEmpty()) {
      progress = false;
      for (int i = 0; i < rest.size(); i++) {
        X509Certificate cand = rest.get(i);
        if (cand.getSubjectX500Principal().equals(cur.getIssuerX500Principal())) {
          out.add(cand);
          rest.remove(i);
          cur = cand;
          progress = true;
          break;
        }
      }
    }
    return out.toArray(new X509Certificate[0]);
  }

  static final class ValidatorException extends CertificateException {
    private static final long serialVersionUID = -2836879718282292155L;

    ValidatorException(String msg) {
      super(msg);
    }

    ValidatorException(String msg, Throwable cause) {
      super(msg, cause);
    }
  }
}
