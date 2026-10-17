/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.provider;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CRL;
import java.security.cert.CRLException;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactorySpi;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import sun.security.provider.certpath.PKCS7Util;
import sun.security.provider.certpath.X509CertPath;
import sun.security.x509.X509CRLImpl;
import sun.security.x509.X509CertImpl;

public class X509Factory extends CertificateFactorySpi {
  public X509Factory() {
  }

  public static byte[] readAll(InputStream is) throws IOException {
    java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
    byte[] buf = new byte[4096];
    int n;
    while ((n = is.read(buf)) > 0) {
      out.write(buf, 0, n);
    }
    return out.toByteArray();
  }

  private static boolean isCertHeader(String label) {
    return label.equals("CERTIFICATE") || label.equals("X509 CERTIFICATE") || label.equals("TRUSTED CERTIFICATE");
  }

  private static byte[] readOneBlock(InputStream is, boolean crl) throws IOException {
    int c = is.read();
    if (c == -1) {
      return null;
    }
    if (c == 0x30) {
      int lenByte = is.read();
      if (lenByte == -1) {
        throw new IOException("Incomplete BER/DER data");
      }
      java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
      out.write(0x30);
      out.write(lenByte);
      long length;
      if ((lenByte & 0x80) == 0) {
        length = lenByte;
      } else {
        int n = lenByte & 0x7f;
        if (n == 0 || n > 4) {
          throw new IOException("Invalid BER/DER data (too huge?)");
        }
        length = 0;
        for (int i = 0; i < n; i++) {
          int b = is.read();
          if (b == -1) {
            throw new IOException("Incomplete BER/DER data");
          }
          out.write(b);
          length = (length << 8) | b;
        }
      }
      byte[] buf = new byte[4096];
      long remaining = length;
      while (remaining > 0) {
        int want = (int) Math.min(buf.length, remaining);
        int r = is.read(buf, 0, want);
        if (r < 0) {
          throw new IOException("Incomplete BER/DER data");
        }
        out.write(buf, 0, r);
        remaining -= r;
      }
      return out.toByteArray();
    }
    StringBuilder line = new StringBuilder();
    StringBuilder body = null;
    String wanted = null;
    boolean eof = false;
    while (true) {
      if (c == -1) {
        if (eof) {
          break;
        }
        eof = true;
        c = '\n';
      }
      if (c == '\n') {
        String raw = line.toString();
        if (raw.endsWith("\r")) {
          raw = raw.substring(0, raw.length() - 1);
        }
        String text = raw.trim();
        line.setLength(0);
        if (body == null) {
          if (text.startsWith("-----BEGIN ") && text.endsWith("-----")) {
            String label = text.substring(11, text.length() - 5);
            if (crl ? label.equals("X509 CRL") : isCertHeader(label)) {
              body = new StringBuilder();
              wanted = "-----END " + label + "-----";
            }
          }
        } else if (text.equals(wanted)) {
          try {
            return Base64.getDecoder().decode(body.toString());
          } catch (IllegalArgumentException e) {
            throw new IOException(e);
          }
        } else {
          body.append(raw);
        }
      } else {
        line.append((char) c);
      }
      c = eof ? -1 : is.read();
    }
    return null;
  }

  private static InputStream markable(InputStream is) {
    return is;
  }

  @Override
  public Certificate engineGenerateCertificate(InputStream is) throws CertificateException {
    if (is == null) {
      throw new CertificateException("Missing input stream");
    }
    try {
      byte[] encoding = readOneBlock(is, false);
      if (encoding == null) {
        throw new IOException("Empty input");
      }
      return new X509CertImpl(encoding);
    } catch (IOException ioe) {
      throw new CertificateException("Could not parse certificate: " + ioe.toString(), ioe);
    }
  }

  @Override
  public Collection<? extends Certificate> engineGenerateCertificates(InputStream is) throws CertificateException {
    if (is == null) {
      throw new CertificateException("Missing input stream");
    }
    try {
      return parseX509orPKCS7Cert(is);
    } catch (IOException ioe) {
      throw new CertificateException(ioe);
    }
  }

  private Collection<? extends Certificate> parseX509orPKCS7Cert(InputStream is) throws CertificateException,
      IOException {
    List<Certificate> certs = new ArrayList<Certificate>();
    byte[] first = readOneBlock(is, false);
    if (first == null) {
      return certs;
    }
    try {
      certs.add(new X509CertImpl(first));
    } catch (CertificateException e) {
      for (byte[] c : PKCS7Util.certificates(first)) {
        certs.add(new X509CertImpl(c));
      }
      return certs;
    }
    while (true) {
      byte[] block = readOneBlock(is, false);
      if (block == null) {
        break;
      }
      certs.add(new X509CertImpl(block));
    }
    return certs;
  }

  @Override
  public CertPath engineGenerateCertPath(InputStream inStream) throws CertificateException {
    return new X509CertPath(inStream);
  }

  @Override
  public CertPath engineGenerateCertPath(InputStream inStream, String encoding) throws CertificateException {
    return new X509CertPath(inStream, encoding);
  }

  @Override
  public CertPath engineGenerateCertPath(List<? extends Certificate> certificates) throws CertificateException {
    return new X509CertPath(certificates);
  }

  @Override
  public Iterator<String> engineGetCertPathEncodings() {
    return X509CertPath.getEncodingsStatic();
  }

  @Override
  public CRL engineGenerateCRL(InputStream is) throws CRLException {
    if (is == null) {
      throw new CRLException("Missing input stream");
    }
    try {
      byte[] encoding = readOneBlock(is, true);
      if (encoding == null) {
        throw new IOException("Empty input");
      }
      return new X509CRLImpl(encoding);
    } catch (IOException ioe) {
      throw new CRLException(ioe.getMessage());
    }
  }

  @Override
  public Collection<? extends CRL> engineGenerateCRLs(InputStream is) throws CRLException {
    if (is == null) {
      throw new CRLException("Missing input stream");
    }
    List<CRL> crls = new ArrayList<CRL>();
    try {
      while (true) {
        byte[] block = readOneBlock(is, true);
        if (block == null) {
          break;
        }
        crls.add(new X509CRLImpl(block));
      }
    } catch (IOException ioe) {
      throw new CRLException(ioe.getMessage());
    }
    return crls;
  }
}
