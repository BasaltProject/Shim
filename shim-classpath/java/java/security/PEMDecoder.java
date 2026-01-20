/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Objects;
import javax.crypto.EncryptedPrivateKeyInfo;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.x509.AlgorithmId;

public final class PEMDecoder {
  private static final PEMDecoder kDefault = new PEMDecoder(null, null);
  private static final String kNoAlgorithm = "No recognized algorithm detected in encoding";

  private final Provider factory;
  private final char[] password;

  private PEMDecoder(Provider factory, char[] password) {
    this.factory = factory;
    this.password = password;
  }

  public static PEMDecoder of() {
    return kDefault;
  }

  public PEMDecoder withFactory(Provider provider) {
    Objects.requireNonNull(provider);
    return new PEMDecoder(provider, password);
  }

  public PEMDecoder withDecryption(char[] password) {
    Objects.requireNonNull(password);
    return new PEMDecoder(factory, password.clone());
  }

  public DEREncodable decode(String str) {
    Objects.requireNonNull(str);
    try {
      return decode(new ByteArrayInputStream(str.getBytes(StandardCharsets.ISO_8859_1)));
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }

  public <S extends DEREncodable> S decode(String str, Class<S> tClass) {
    Objects.requireNonNull(str);
    Objects.requireNonNull(tClass);
    try {
      return decode(new ByteArrayInputStream(str.getBytes(StandardCharsets.ISO_8859_1)), tClass);
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }

  public DEREncodable decode(InputStream is) throws IOException {
    PEMRecord record = read(is);
    return convert(record);
  }

  public <S extends DEREncodable> S decode(InputStream is, Class<S> tClass) throws IOException {
    Objects.requireNonNull(tClass);
    PEMRecord record = read(is);
    if (tClass == PEMRecord.class) {
      return tClass.cast(record);
    }
    if (tClass == PKCS8EncodedKeySpec.class && record.type().equals("PRIVATE KEY")) {
      return tClass.cast(new PKCS8EncodedKeySpec(derOf(record)));
    }
    if (tClass == X509EncodedKeySpec.class && record.type().equals("PUBLIC KEY")) {
      return tClass.cast(new X509EncodedKeySpec(derOf(record)));
    }
    DEREncodable result = convert(record);
    if (result instanceof KeyPair && !tClass.isInstance(result)) {
      KeyPair pair = (KeyPair) result;
      if (tClass.isInstance(pair.getPrivate())) {
        return tClass.cast(pair.getPrivate());
      }
      if (tClass.isInstance(pair.getPublic())) {
        return tClass.cast(pair.getPublic());
      }
    }
    return tClass.cast(result);
  }

  private static int next(InputStream is) throws IOException {
    return is.read();
  }

  private static String line(InputStream is, StringBuilder first) throws IOException {
    StringBuilder sb = new StringBuilder(first);
    int c;
    while ((c = next(is)) != -1 && c != '\n') {
      sb.append((char) c);
    }
    if (c == -1 && sb.length() == 0) {
      return null;
    }
    int n = sb.length();
    if (n > 0 && sb.charAt(n - 1) == '\r') {
      sb.setLength(n - 1);
    }
    return sb.toString();
  }

  private static PEMRecord read(InputStream is) throws IOException {
    Objects.requireNonNull(is);
    ByteArrayOutputStream leading = new ByteArrayOutputStream();
    int c = next(is);
    if (c == -1) {
      throw new EOFException("No data available");
    }
    int dashes = 0;
    StringBuilder tail = new StringBuilder();
    while (c != -1) {
      if (c == '-') {
        dashes++;
        if (dashes == 5) {
          break;
        }
      } else {
        for (int i = 0; i < dashes; i++) {
          leading.write('-');
        }
        dashes = 0;
        leading.write(c);
      }
      c = next(is);
    }
    if (c == -1) {
      throw new EOFException("No PEM data found");
    }
    String header = line(is, new StringBuilder("-----"));
    if (header == null || !header.startsWith("-----BEGIN ") || !header.endsWith("-----")
        || header.length() < 17) {
      throw new IOException("Illegal header: " + header);
    }
    String type = header.substring(11, header.length() - 5);
    StringBuilder body = new StringBuilder();
    String footer = null;
    for (;;) {
      String text = line(is, tail);
      if (text == null) {
        throw new EOFException("Incomplete header");
      }
      String trimmed = text.trim();
      if (trimmed.startsWith("-----")) {
        footer = trimmed;
        break;
      }
      body.append(trimmed);
    }
    if (!footer.startsWith("-----END ") || !footer.endsWith("-----")) {
      throw new IOException("Illegal footer: " + footer);
    }
    String footerType = footer.substring(9, footer.length() - 5);
    if (!footerType.equals(type)) {
      throw new IOException("Header and footer do not match: " + type + " " + footerType);
    }
    byte[] leadingData = leading.size() == 0 ? null : leading.toByteArray();
    return new PEMRecord(type, body.toString(), leadingData);
  }

  private static byte[] derOf(PEMRecord record) throws IOException {
    try {
      return Base64.getMimeDecoder().decode(record.content());
    } catch (IllegalArgumentException e) {
      throw new IOException(kNoAlgorithm);
    }
  }

  private DEREncodable convert(PEMRecord record) throws IOException {
    String type = record.type();
    switch (type) {
    case "PUBLIC KEY":
      return publicKey(derOf(record));
    case "PRIVATE KEY":
      return privateKey(derOf(record));
    case "RSA PRIVATE KEY":
      return privateKey(wrapPkcs1(derOf(record)));
    case "CERTIFICATE":
      return certificate(derOf(record));
    case "X509 CRL":
      return crl(derOf(record));
    case "ENCRYPTED PRIVATE KEY":
      return encrypted(derOf(record));
    default:
      return record;
    }
  }

  private static byte[] wrapPkcs1(byte[] pkcs1) throws IOException {
    DerOutputStream algorithm = new DerOutputStream();
    algorithm.putOID(sun.security.util.ObjectIdentifier.of("1.2.840.113549.1.1.1"));
    algorithm.putNull();
    DerOutputStream body = new DerOutputStream();
    body.putInteger(0);
    body.write(DerValue.tag_Sequence, algorithm);
    body.putOctetString(pkcs1);
    DerOutputStream out = new DerOutputStream();
    out.write(DerValue.tag_Sequence, body);
    return out.toByteArray();
  }

  private KeyFactory keyFactory(String algorithm) throws IOException {
    try {
      return factory == null ? KeyFactory.getInstance(algorithm) : KeyFactory.getInstance(algorithm, factory);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalArgumentException(e);
    }
  }

  private static String algorithmOf(DerValue algorithmId) throws IOException {
    String oid = algorithmId.data().getOID().toString();
    String name = AlgorithmId.keyAlgorithm(oid);
    if (name.equals(oid)) {
      throw new IOException(kNoAlgorithm);
    }
    return name;
  }

  private DEREncodable publicKey(byte[] der) throws IOException {
    try {
      DerInputStream in = new DerValue(der).data();
      String algorithm = algorithmOf(in.getDerValue());
      return keyFactory(algorithm).generatePublic(new X509EncodedKeySpec(der));
    } catch (IOException e) {
      if (kNoAlgorithm.equals(e.getMessage()) || e.getCause() != null) {
        throw e;
      }
      throw new IOException(kNoAlgorithm);
    } catch (java.security.spec.InvalidKeySpecException e) {
      throw new IllegalArgumentException(e);
    }
  }

  private DEREncodable privateKey(byte[] der) throws IOException {
    DerValue algorithmId;
    byte[] publicBits = null;
    try {
      DerInputStream in = new DerValue(der).data();
      in.getInteger();
      algorithmId = in.getDerValue();
      byte[] inner = in.getOctetString();
      while (in.available() > 0) {
        DerValue extra = in.getDerValue();
        if (extra.getTag() == (byte) 0x81) {
          publicBits = extra.getDataBytes();
        }
      }
      String algorithm = algorithmOf(algorithmId);
      if (publicBits == null && algorithm.equals("EC")) {
        publicBits = embeddedEcPublic(inner);
      }
      PrivateKey key = keyFactory(algorithm).generatePrivate(new PKCS8EncodedKeySpec(der));
      if (publicBits == null) {
        return key;
      }
      DerOutputStream bits = new DerOutputStream();
      bits.write(DerValue.tag_BitString, publicBits);
      DerOutputStream body = new DerOutputStream();
      body.putDerValue(algorithmId);
      body.write(bits.toByteArray());
      DerOutputStream spki = new DerOutputStream();
      spki.write(DerValue.tag_Sequence, body);
      PublicKey pub = keyFactory(algorithm).generatePublic(new X509EncodedKeySpec(spki.toByteArray()));
      return new KeyPair(pub, key);
    } catch (IOException e) {
      if (kNoAlgorithm.equals(e.getMessage()) || e.getCause() != null) {
        throw e;
      }
      throw new IOException(kNoAlgorithm);
    } catch (java.security.spec.InvalidKeySpecException e) {
      throw new IllegalArgumentException(e);
    }
  }

  private static byte[] embeddedEcPublic(byte[] inner) throws IOException {
    DerInputStream in = new DerValue(inner).data();
    in.getInteger();
    in.getOctetString();
    while (in.available() > 0) {
      DerValue field = in.getDerValue();
      if (field.isContextSpecific((byte) 1)) {
        return new DerValue(field.getDataBytes()).getDataBytes();
      }
    }
    return null;
  }

  private DEREncodable certificate(byte[] der) throws IOException {
    try {
      return (DEREncodable) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(der));
    } catch (java.security.cert.CertificateException | ClassCastException e) {
      throw new IllegalArgumentException(e);
    }
  }

  private DEREncodable crl(byte[] der) throws IOException {
    try {
      return (DEREncodable) CertificateFactory.getInstance("X.509").generateCRL(new ByteArrayInputStream(der));
    } catch (java.security.cert.CertificateException | java.security.cert.CRLException e) {
      throw new IllegalArgumentException(e);
    }
  }

  private DEREncodable encrypted(byte[] der) throws IOException {
    EncryptedPrivateKeyInfo info = new EncryptedPrivateKeyInfo(der);
    if (password == null) {
      return info;
    }
    try {
      PrivateKey key = info.getKey(password);
      return privateKey(key.getEncoded());
    } catch (GeneralSecurityException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
