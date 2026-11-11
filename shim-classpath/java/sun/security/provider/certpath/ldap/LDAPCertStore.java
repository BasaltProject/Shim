/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.provider.certpath.ldap;

import java.io.ByteArrayInputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.cert.CRL;
import java.security.cert.CRLSelector;
import java.security.cert.CertSelector;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.CertStoreException;
import java.security.cert.CertStoreParameters;
import java.security.cert.CertStoreSpi;
import java.security.cert.LDAPCertStoreParameters;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLSelector;
import java.security.cert.X509CertSelector;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.security.auth.x500.X500Principal;

/** Certificate and CRL repository backed by an LDAP directory (RFC 4523 attributes). */
public final class LDAPCertStore extends CertStoreSpi {
  private static final String kCrossPair = "crossCertificatePair;binary";
  private static final String kCaCert = "cACertificate;binary";
  private static final String kAuthorityRevocation = "authorityRevocationList;binary";
  private static final String kUserCert = "userCertificate;binary";
  private static final String kCrl = "certificateRevocationList;binary";

  private static final class Cached {
    final long expires;
    final Attributes attrs;

    Cached(long expires, Attributes attrs) {
      this.expires = expires;
      this.attrs = attrs;
    }
  }

  private final String url;
  private final Map<String, Cached> cache = new HashMap<String, Cached>();
  private final long lifetimeMs;

  public LDAPCertStore(CertStoreParameters params) throws InvalidAlgorithmParameterException {
    super(params);
    if (!(params instanceof LDAPCertStoreParameters)) {
      throw new InvalidAlgorithmParameterException("parameters must be LDAPCertStoreParameters");
    }
    LDAPCertStoreParameters p = (LDAPCertStoreParameters) params;
    this.url = "ldap://" + p.getServerName() + ":" + p.getPort();
    this.lifetimeMs = Long.getLong("sun.security.certpath.ldap.cache.lifetime", 30L) * 1000L;
  }

  private synchronized Attributes fetch(String dn, String[] ids) throws CertStoreException {
    String key = dn + "|" + String.join(",", ids);
    long now = System.currentTimeMillis();
    Cached c = cache.get(key);
    if (c != null && c.expires > now) {
      return c.attrs;
    }
    Hashtable<String, Object> env = new Hashtable<String, Object>();
    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    env.put(Context.PROVIDER_URL, url);
    env.put("java.naming.ldap.version", "3");
    env.put(Context.REFERRAL, "throw");
    env.put("com.sun.jndi.ldap.connect.timeout", "30000");
    env.put("com.sun.jndi.ldap.read.timeout", "30000");
    DirContext ctx = null;
    try {
      ctx = new InitialDirContext(env);
      Attributes a;
      try {
        a = ctx.getAttributes(dn, ids);
      } catch (NameNotFoundException e) {
        a = new javax.naming.directory.BasicAttributes(true);
      }
      cache.put(key, new Cached(now + lifetimeMs, a));
      return a;
    } catch (NamingException e) {
      throw new CertStoreException("unable to connect to LDAP server: " + e, e);
    } finally {
      if (ctx != null) {
        try {
          ctx.close();
        } catch (NamingException ignore) {
          // closing only
        }
      }
    }
  }

  private static List<byte[]> values(Attributes attrs, String id) throws CertStoreException {
    List<byte[]> out = new ArrayList<byte[]>();
    Attribute a = attrs.get(id);
    if (a == null) {
      return out;
    }
    try {
      for (int i = 0; i < a.size(); i++) {
        Object v = a.get(i);
        if (v instanceof byte[]) {
          out.add((byte[]) v);
        }
      }
    } catch (NamingException e) {
      throw new CertStoreException(e);
    }
    return out;
  }

  private static byte[] derContent(byte[] b, int[] pos, int end) {
    int p = pos[0];
    int len = b[p + 1] & 0xff;
    int off = p + 2;
    if (len >= 0x80) {
      int n = len & 0x7f;
      len = 0;
      for (int i = 0; i < n; i++) {
        len = (len << 8) | (b[off++] & 0xff);
      }
    }
    pos[0] = off + len;
    return java.util.Arrays.copyOfRange(b, off, off + len);
  }

  private static void addPair(byte[] pair, CertificateFactory cf, Collection<Certificate> out) {
    try {
      int[] pos = {0};
      byte[] body = derContent(pair, pos, pair.length);
      int[] p = {0};
      while (p[0] < body.length) {
        int start = p[0];
        derContent(body, p, body.length);
        byte[] inner = java.util.Arrays.copyOfRange(body, start, p[0]);
        int[] q = {0};
        byte[] cert = derContent(inner, q, inner.length);
        out.add(cf.generateCertificate(new ByteArrayInputStream(cert)));
      }
    } catch (Exception e) {
      // a malformed pair is skipped
    }
  }

  @Override
  public Collection<? extends Certificate> engineGetCertificates(CertSelector selector) throws CertStoreException {
    X509CertSelector sel = selector instanceof X509CertSelector ? (X509CertSelector) selector : null;
    X500Principal subject = sel == null ? null : sel.getSubject();
    X500Principal issuer = sel == null ? null : sel.getIssuer();
    if (subject == null && issuer == null) {
      throw new CertStoreException("need subject or issuer to find certs");
    }
    Collection<Certificate> found = new ArrayList<Certificate>();
    try {
      CertificateFactory cf = CertificateFactory.getInstance("X.509");
      String dn = (subject != null ? subject : issuer).getName();
      Attributes attrs = fetch(dn, new String[] {kCrossPair, kCaCert, kAuthorityRevocation, kUserCert});
      List<byte[]> raw = new ArrayList<byte[]>();
      raw.addAll(values(attrs, kCaCert));
      raw.addAll(values(attrs, kUserCert));
      for (byte[] b : raw) {
        try {
          found.add(cf.generateCertificate(new ByteArrayInputStream(b)));
        } catch (Exception e) {
          // unparsable value skipped
        }
      }
      for (byte[] b : values(attrs, kCrossPair)) {
        addPair(b, cf, found);
      }
    } catch (java.security.cert.CertificateException e) {
      throw new CertStoreException(e);
    }
    Collection<Certificate> out = new ArrayList<Certificate>();
    for (Certificate c : found) {
      if (selector == null || selector.match(c)) {
        out.add(c);
      }
    }
    return out;
  }

  @Override
  public Collection<? extends CRL> engineGetCRLs(CRLSelector selector) throws CertStoreException {
    if (!(selector instanceof X509CRLSelector) || ((X509CRLSelector) selector).getIssuers() == null
        || ((X509CRLSelector) selector).getIssuers().isEmpty()) {
      throw new CertStoreException("need issuers or certificate to find CRLs");
    }
    List<CRL> out = new ArrayList<CRL>();
    try {
      CertificateFactory cf = CertificateFactory.getInstance("X.509");
      for (X500Principal issuer : ((X509CRLSelector) selector).getIssuers()) {
        Attributes attrs = fetch(issuer.getName(), new String[] {kCrl});
        for (byte[] b : values(attrs, kCrl)) {
          try {
            X509CRL crl = (X509CRL) cf.generateCRL(new ByteArrayInputStream(b));
            if (selector.match(crl)) {
              out.add(crl);
            }
          } catch (Exception e) {
            // unparsable value skipped
          }
        }
      }
    } catch (java.security.cert.CertificateException e) {
      throw new CertStoreException(e);
    }
    return out;
  }
}
