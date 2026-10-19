/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.cert.CRLException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Objects;
import javax.crypto.EncryptedPrivateKeyInfo;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public final class PEMEncoder {
  private static final PEMEncoder kDefault = new PEMEncoder(null);
  private static final String kLineSeparator = "\r\n";

  private final char[] password;

  private PEMEncoder(char[] password) {
    this.password = password;
  }

  public static PEMEncoder of() {
    return kDefault;
  }

  public PEMEncoder withEncryption(char[] password) {
    Objects.requireNonNull(password, "password cannot be null.");
    return new PEMEncoder(password.clone());
  }

  public byte[] encode(DEREncodable de) {
    return encodeToString(de).getBytes(StandardCharsets.ISO_8859_1);
  }

  public String encodeToString(DEREncodable de) {
    Objects.requireNonNull(de);
    if (password != null) {
      return encrypted(de);
    }
    try {
      if (de instanceof PublicKey) {
        return wrap("PUBLIC KEY", ((PublicKey) de).getEncoded());
      }
      if (de instanceof PrivateKey) {
        return wrap("PRIVATE KEY", ((PrivateKey) de).getEncoded());
      }
      if (de instanceof KeyPair) {
        KeyPair pair = (KeyPair) de;
        return wrap("PRIVATE KEY", withPublicKey(pair.getPrivate().getEncoded(), pair.getPublic().getEncoded()));
      }
      if (de instanceof X509Certificate) {
        return wrap("CERTIFICATE", ((X509Certificate) de).getEncoded());
      }
      if (de instanceof X509CRL) {
        return wrap("X509 CRL", ((X509CRL) de).getEncoded());
      }
      if (de instanceof PKCS8EncodedKeySpec) {
        return wrap("PRIVATE KEY", ((PKCS8EncodedKeySpec) de).getEncoded());
      }
      if (de instanceof X509EncodedKeySpec) {
        return wrap("PUBLIC KEY", ((X509EncodedKeySpec) de).getEncoded());
      }
      if (de instanceof EncryptedPrivateKeyInfo) {
        return wrap("ENCRYPTED PRIVATE KEY", ((EncryptedPrivateKeyInfo) de).getEncoded());
      }
      if (de instanceof PEMRecord) {
        PEMRecord record = (PEMRecord) de;
        return format(record.type(), record.content());
      }
    } catch (IOException | CertificateEncodingException | CRLException e) {
      throw new IllegalArgumentException(e);
    }
    throw new IllegalArgumentException("PEM does not support " + de.getClass().getCanonicalName());
  }

  private String encrypted(DEREncodable de) {
    if (de instanceof PrivateKey) {
      return wrap("ENCRYPTED PRIVATE KEY", EncryptedPrivateKeyInfo.encryptKey((PrivateKey) de, password).getEncoded());
    }
    if (de instanceof EncryptedPrivateKeyInfo) {
      return wrap("ENCRYPTED PRIVATE KEY", ((EncryptedPrivateKeyInfo) de).getEncoded());
    }
    if (de instanceof X509Certificate || de instanceof X509CRL) {
      throw new IllegalArgumentException("Certificates cannot be encrypted");
    }
    if (de instanceof PEMRecord) {
      throw new IllegalArgumentException("PEMRecord cannot be encrypted");
    }
    throw new IllegalArgumentException("Can only encrypt a PrivateKey.");
  }

  private static byte[] withPublicKey(byte[] pkcs8, byte[] spki) throws IOException {
    DerInputStream in = new DerValue(pkcs8).data();
    in.getInteger();
    DerValue algorithm = in.getDerValue();
    byte[] privateKey = in.getOctetString();
    DerInputStream pub = new DerValue(spki).data();
    pub.getDerValue();
    DerValue bits = pub.getDerValue();
    DerOutputStream body = new DerOutputStream();
    body.putInteger(1);
    body.putDerValue(algorithm);
    body.putOctetString(privateKey);
    body.write((byte) 0x81, bits.getDataBytes());
    DerOutputStream out = new DerOutputStream();
    out.write(DerValue.tag_Sequence, body);
    return out.toByteArray();
  }

  private static String wrap(String type, byte[] der) {
    return format(type, Base64.getEncoder().encodeToString(der));
  }

  static String format(String type, String content) {
    StringBuilder sb = new StringBuilder();
    sb.append("-----BEGIN ").append(type).append("-----").append(kLineSeparator);
    if (content.isEmpty()) {
      sb.append(kLineSeparator);
    }
    for (int i = 0; i < content.length(); i += 64) {
      String chunk = content.substring(i, Math.min(content.length(), i + 64));
      sb.append(chunk);
      if (!chunk.endsWith("\n")) {
        sb.append(kLineSeparator);
      }
    }
    sb.append("-----END ").append(type).append("-----").append(kLineSeparator);
    return sb.toString();
  }
}
