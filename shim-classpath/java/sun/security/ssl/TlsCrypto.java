/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.ssl;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;

final class TlsCrypto {
  private TlsCrypto() {
  }

  static byte[] certificateVerifyContent(boolean serverSigns, byte[] transcriptHash) {
    ByteWriter w = new ByteWriter();
    for (int i = 0; i < 64; i++) {
      w.u8(0x20);
    }
    w.bytes((serverSigns ? "TLS 1.3, server CertificateVerify" : "TLS 1.3, client CertificateVerify")
        .getBytes(StandardCharsets.US_ASCII));
    w.u8(0);
    w.bytes(transcriptHash);
    return w.toByteArray();
  }

  static boolean verify(SignatureScheme scheme, PublicKey key, byte[] content, byte[] signature)
      throws GeneralSecurityException {
    Signature sig = scheme.newSignature();
    sig.initVerify(key);
    sig.update(content);
    return sig.verify(signature);
  }

  static byte[] sign(SignatureScheme scheme, PrivateKey key, byte[] content, SecureRandom random)
      throws GeneralSecurityException {
    Signature sig = scheme.newSignature();
    sig.initSign(key, random);
    sig.update(content);
    return sig.sign();
  }

  static X509Certificate[] parseChain(List<byte[]> encodedCerts) throws CertificateException {
    CertificateFactory cf = CertificateFactory.getInstance("X.509");
    X509Certificate[] chain = new X509Certificate[encodedCerts.size()];
    for (int i = 0; i < chain.length; i++) {
      chain[i] = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(encodedCerts.get(i)));
    }
    return chain;
  }

  static SignatureScheme findScheme(int id, List<SignatureScheme> allowed) {
    for (SignatureScheme s : allowed) {
      if (s.id == id) {
        return s;
      }
    }
    return null;
  }

  static String keyTypeFor(SignatureScheme scheme) {
    return scheme.kind == SignatureScheme.kEcdsa ? "EC" : scheme.kind == SignatureScheme.kRsaPss ? "RSA" : "RSA";
  }
}
