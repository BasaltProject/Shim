/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.provider.certpath;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import sun.security.provider.X509Factory;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.x509.X509CertImpl;

public final class X509CertPath extends CertPath {
  private static final long serialVersionUID = 4989800333263052980L;

  private static final String kCertType = "X.509";
  private static final String kPkiPath = "PkiPath";
  private static final String kPkcs7 = "PKCS7";
  private static final List<String> kEncodingList = Collections.unmodifiableList(java.util.Arrays.asList(kPkiPath, kPkcs7));

  private final List<X509Certificate> certs;

  public X509CertPath(List<? extends Certificate> certs) throws CertificateException {
    super(kCertType);
    List<X509Certificate> list = new ArrayList<X509Certificate>(certs.size());
    for (Object cert : certs) {
      if (cert instanceof X509Certificate) {
        list.add((X509Certificate) cert);
      } else {
        throw new CertificateException("List is not all X509Certificates: " + cert.getClass().getName());
      }
    }
    this.certs = Collections.unmodifiableList(list);
  }

  public X509CertPath(InputStream is) throws CertificateException {
    this(is, kPkiPath);
  }

  public X509CertPath(InputStream is, String encoding) throws CertificateException {
    super(kCertType);
    switch (encoding) {
    case kPkiPath:
      this.certs = parsePkiPath(is);
      break;
    case kPkcs7:
      this.certs = parsePkcs7(is);
      break;
    default:
      throw new CertificateException(unsupportedMessage(is));
    }
  }

  private static String unsupportedMessage(InputStream is) {
    try {
      byte[] data = X509Factory.readAll(is);
      return data.length > 0 && data[0] == 0x30 ? "unsupported encoding" : "Empty input";
    } catch (IOException e) {
      return "unsupported encoding";
    }
  }

  private static List<X509Certificate> parsePkiPath(InputStream is) throws CertificateException {
    if (is == null) {
      throw new CertificateException("input stream is null");
    }
    try {
      byte[] data = X509Factory.readAll(is);
      if (data.length == 0) {
        throw new IOException("Empty input");
      }
      DerValue seq = new DerValue(data);
      if (seq.getTag() != DerValue.tag_Sequence) {
        throw new IOException("PkiPath must be a SEQUENCE");
      }
      List<X509Certificate> result = new ArrayList<X509Certificate>();
      DerInputStream in = seq.data();
      while (in.available() > 0) {
        DerValue c = in.getDerValue();
        result.add(0, new X509CertImpl(c.toByteArray()));
      }
      return Collections.unmodifiableList(result);
    } catch (IOException ioe) {
      throw new CertificateException("IOException parsing PkiPath data: " + ioe, ioe);
    }
  }

  private static List<X509Certificate> parsePkcs7(InputStream is) throws CertificateException {
    if (is == null) {
      throw new CertificateException("input stream is null");
    }
    try {
      byte[] data = X509Factory.readAll(is);
      List<X509Certificate> result = new ArrayList<X509Certificate>();
      for (byte[] c : PKCS7Util.certificates(data)) {
        result.add(new X509CertImpl(c));
      }
      return Collections.unmodifiableList(result);
    } catch (IOException ioe) {
      throw new CertificateException("IOException parsing PKCS7 data: " + ioe, ioe);
    }
  }

  @Override
  public byte[] getEncoded() throws CertificateEncodingException {
    return encodePkiPath();
  }

  private byte[] encodePkiPath() throws CertificateEncodingException {
    DerOutputStream body = new DerOutputStream();
    for (int i = certs.size() - 1; i >= 0; i--) {
      X509Certificate cert = certs.get(i);
      body.write(cert.getEncoded());
    }
    DerOutputStream out = new DerOutputStream();
    out.write(DerValue.tag_Sequence, body);
    return out.toByteArray();
  }

  @Override
  public byte[] getEncoded(String encoding) throws CertificateEncodingException {
    switch (encoding) {
    case kPkiPath:
      return encodePkiPath();
    case kPkcs7:
      List<byte[]> list = new ArrayList<byte[]>();
      for (int i = certs.size() - 1; i >= 0; i--) {
        list.add(certs.get(i).getEncoded());
      }
      return PKCS7Util.encode(list);
    default:
      throw new CertificateEncodingException("unsupported encoding");
    }
  }

  public static Iterator<String> getEncodingsStatic() {
    return kEncodingList.iterator();
  }

  @Override
  public Iterator<String> getEncodings() {
    return getEncodingsStatic();
  }

  @Override
  public List<X509Certificate> getCertificates() {
    return certs;
  }
}
