/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.jcp.xml.dsig.internal.dom;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.util.Arrays;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dom.DOMCryptoContext;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.XMLSignContext;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLValidateContext;
import javax.xml.crypto.dsig.spec.HMACParameterSpec;
import javax.xml.crypto.dsig.spec.RSAPSSParameterSpec;
import javax.xml.crypto.dsig.spec.SignatureMethodParameterSpec;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public final class DOMSignatureMethod implements SignatureMethod {
  private static final int T_RSA = 1;
  private static final int T_DSA = 2;
  private static final int T_EC = 3;
  private static final int T_HMAC = 4;
  private static final int T_PSS = 5;
  private static final int T_EDDSA = 6;

  private final String algorithm;
  private final int type;
  private final String jca;
  private SignatureMethodParameterSpec params;

  public DOMSignatureMethod(String algorithm, AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException,
      NoSuchAlgorithmException {
    this.algorithm = algorithm;
    this.type = typeOf(algorithm);
    this.jca = jcaOf(algorithm);
    if (type == 0) {
      throw new NoSuchAlgorithmException("unsupported algorithm");
    }
    if (params != null && !(params instanceof SignatureMethodParameterSpec)) {
      throw new InvalidAlgorithmParameterException("params must be of type SignatureMethodParameterSpec");
    }
    if (type == T_HMAC) {
      if (params != null && !(params instanceof HMACParameterSpec)) {
        throw new InvalidAlgorithmParameterException("params must be of type HMACParameterSpec");
      }
    } else if (type == T_PSS) {
      if (params != null && !(params instanceof RSAPSSParameterSpec)) {
        throw new InvalidAlgorithmParameterException("params must be of type RSAPSSParameterSpec");
      }
    } else if (params != null) {
      throw new InvalidAlgorithmParameterException("no parameters should be specified for the " + algorithm
          + " SignatureMethod algorithm");
    }
    this.params = (SignatureMethodParameterSpec) params;
  }

  public DOMSignatureMethod(Element elem) throws MarshalException {
    this.algorithm = DOMUtils.getAttributeValue(elem, "Algorithm");
    DOMUtils.checkAlgorithm(algorithm);
    this.type = algorithm == null ? 0 : typeOf(algorithm);
    this.jca = algorithm == null ? null : jcaOf(algorithm);
    if (type == 0) {
      throw new MarshalException("unsupported SignatureMethod algorithm: " + algorithm);
    }
    if (type == T_HMAC) {
      Element child = DOMUtils.getFirstChildElement(elem);
      if (child != null && "HMACOutputLength".equals(child.getLocalName())) {
        params = new HMACParameterSpec(Integer.parseInt(DOMUtils.textOf(child).trim()));
      }
    }
  }

  private String shortName() {
    int i = algorithm.lastIndexOf('#');
    int j = algorithm.lastIndexOf('-');
    String n = algorithm.substring(Math.max(i, j) + 1);
    return n.toUpperCase();
  }

  private static final String kRsaRipemd160 = "http://www.w3.org/2001/04/xmldsig-more#rsa-ripemd160";
  private static final String kHmacRipemd160 = "http://www.w3.org/2001/04/xmldsig-more#hmac-ripemd160";

  private static int typeOf(String alg) {
    if (alg.equals(kRsaRipemd160)) {
      return T_RSA;
    } else if (alg.equals(kHmacRipemd160)) {
      return T_HMAC;
    }
    if (alg.equals(RSA_SHA1) || alg.equals(RSA_SHA224) || alg.equals(RSA_SHA256) || alg.equals(RSA_SHA384)
        || alg.equals(RSA_SHA512)) {
      return T_RSA;
    } else if (alg.equals(DSA_SHA1) || alg.equals(DSA_SHA256)) {
      return T_DSA;
    } else if (alg.startsWith("http://www.w3.org/2001/04/xmldsig-more#ecdsa-")
        || alg.startsWith("http://www.w3.org/2021/04/xmldsig-more#ecdsa-")) {
      return jcaOf(alg) == null ? 0 : T_EC;
    } else if (alg.equals(HMAC_SHA1) || alg.equals(HMAC_SHA224) || alg.equals(HMAC_SHA256) || alg.equals(HMAC_SHA384)
        || alg.equals(HMAC_SHA512)) {
      return T_HMAC;
    } else if (alg.equals(RSA_PSS) || alg.contains("-rsa-MGF1")) {
      return T_PSS;
    } else if (alg.equals(ED25519) || alg.equals(ED448)) {
      return T_EDDSA;
    }
    return 0;
  }

  private static String jcaOf(String alg) {
    if (alg.equals(kRsaRipemd160)) {
      return "RIPEMD160withRSA";
    } else if (alg.equals(kHmacRipemd160)) {
      return "HMACRIPEMD160";
    } else if (alg.equals(RSA_SHA1)) {
      return "SHA1withRSA";
    } else if (alg.equals(RSA_SHA224)) {
      return "SHA224withRSA";
    } else if (alg.equals(RSA_SHA256)) {
      return "SHA256withRSA";
    } else if (alg.equals(RSA_SHA384)) {
      return "SHA384withRSA";
    } else if (alg.equals(RSA_SHA512)) {
      return "SHA512withRSA";
    } else if (alg.equals(DSA_SHA1)) {
      return "SHA1withDSA";
    } else if (alg.equals(DSA_SHA256)) {
      return "SHA256withDSA";
    } else if (alg.equals(ECDSA_SHA1)) {
      return "SHA1withECDSA";
    } else if (alg.equals(ECDSA_SHA224)) {
      return "SHA224withECDSA";
    } else if (alg.equals(ECDSA_SHA256)) {
      return "SHA256withECDSA";
    } else if (alg.equals(ECDSA_SHA384)) {
      return "SHA384withECDSA";
    } else if (alg.equals(ECDSA_SHA512)) {
      return "SHA512withECDSA";
    } else if (alg.equals(ECDSA_SHA3_224)) {
      return "SHA3-224withECDSA";
    } else if (alg.equals(ECDSA_SHA3_256)) {
      return "SHA3-256withECDSA";
    } else if (alg.equals(ECDSA_SHA3_384)) {
      return "SHA3-384withECDSA";
    } else if (alg.equals(ECDSA_SHA3_512)) {
      return "SHA3-512withECDSA";
    } else if (alg.equals(HMAC_SHA1)) {
      return "HmacSHA1";
    } else if (alg.equals(HMAC_SHA224)) {
      return "HmacSHA224";
    } else if (alg.equals(HMAC_SHA256)) {
      return "HmacSHA256";
    } else if (alg.equals(HMAC_SHA384)) {
      return "HmacSHA384";
    } else if (alg.equals(HMAC_SHA512)) {
      return "HmacSHA512";
    } else if (alg.equals(ED25519)) {
      return "Ed25519";
    } else if (alg.equals(ED448)) {
      return "Ed448";
    } else if (alg.equals(RSA_PSS) || alg.contains("-rsa-MGF1")) {
      return "RSASSA-PSS";
    }
    return null;
  }

  public String getAlgorithm() {
    return algorithm;
  }

  public AlgorithmParameterSpec getParameterSpec() {
    return params;
  }

  public boolean isFeatureSupported(String feature) {
    if (feature == null) {
      throw new NullPointerException();
    }
    return false;
  }

  void marshal(Node parent, String prefix, DOMCryptoContext context) throws MarshalException {
    Document ownerDoc = DOMUtils.getOwnerDocument(parent);
    Element smElem = DOMUtils.createElement(ownerDoc, "SignatureMethod", XMLSignature.XMLNS, prefix);
    DOMUtils.setAttribute(smElem, "Algorithm", algorithm);
    if (params instanceof HMACParameterSpec) {
      Element hmacLen = DOMUtils.createElement(ownerDoc, "HMACOutputLength", XMLSignature.XMLNS, prefix);
      hmacLen.appendChild(ownerDoc.createTextNode(String.valueOf(((HMACParameterSpec) params).getOutputLength())));
      smElem.appendChild(hmacLen);
    }
    parent.appendChild(smElem);
  }

  private static byte[] read(InputStream in) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    byte[] buf = new byte[4096];
    int n;
    while ((n = in.read(buf)) >= 0) {
      bos.write(buf, 0, n);
    }
    return bos.toByteArray();
  }

  private Signature newSignature() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
    Signature s = Signature.getInstance(jca);
    if (type == T_PSS) {
      s.setParameter(pssSpec());
    }
    return s;
  }

  private PSSParameterSpec pssSpec() {
    if (params instanceof RSAPSSParameterSpec && ((RSAPSSParameterSpec) params).getPSSParameterSpec() != null) {
      return ((RSAPSSParameterSpec) params).getPSSParameterSpec();
    }
    String digest = "SHA-256";
    int salt = 32;
    if (algorithm.contains("sha1-")) {
      digest = "SHA-1";
      salt = 20;
    } else if (algorithm.contains("sha224-")) {
      digest = "SHA-224";
      salt = 28;
    } else if (algorithm.contains("sha384-")) {
      digest = "SHA-384";
      salt = 48;
    } else if (algorithm.contains("sha512-")) {
      digest = "SHA-512";
      salt = 64;
    }
    return new PSSParameterSpec(digest, "MGF1", new java.security.spec.MGF1ParameterSpec(digest), salt, 1);
  }

  private static void checkHmacLength(int bits, int digestBytes) throws XMLSignatureException {
    if (bits < digestBytes * 8) {
      throw new XMLSignatureException("HMACOutputLength must not be less than " + digestBytes * 8);
    }
  }

  byte[] sign(Key key, SignedInfo si, XMLSignContext context) throws InvalidKeyException, XMLSignatureException {
    if (key == null || si == null) {
      throw new NullPointerException();
    }
    try {
      byte[] data = read(((DOMSignedInfo) si).canonicalized((DOMCryptoContext) context));
      if (type == T_HMAC) {
        if (!(key instanceof SecretKey)) {
          throw new InvalidKeyException("key must be SecretKey");
        }
        Mac mac = Mac.getInstance(jca);
        mac.init(key);
        byte[] full = mac.doFinal(data);
        if (params instanceof HMACParameterSpec) {
          int bits = ((HMACParameterSpec) params).getOutputLength();
          checkHmacLength(bits, full.length);
          return Arrays.copyOf(full, (bits + 7) / 8);
        }
        return full;
      }
      if (!(key instanceof PrivateKey)) {
        throw new InvalidKeyException("key must be PrivateKey");
      }
      Signature s = newSignature();
      s.initSign((PrivateKey) key);
      s.update(data);
      byte[] sig = s.sign();
      if (type == T_EC) {
        return derToConcat(sig, ((java.security.interfaces.ECKey) key).getParams().getOrder().bitLength());
      } else if (type == T_DSA) {
        return derToConcat(sig, ((java.security.interfaces.DSAKey) key).getParams().getQ().bitLength());
      }
      return sig;
    } catch (IOException e) {
      throw new XMLSignatureException(e);
    } catch (NoSuchAlgorithmException e) {
      throw new XMLSignatureException(e);
    } catch (InvalidAlgorithmParameterException e) {
      throw new XMLSignatureException(e);
    } catch (SignatureException e) {
      throw new XMLSignatureException(e);
    }
  }

  boolean verify(Key key, SignedInfo si, byte[] sig, XMLValidateContext context) throws InvalidKeyException,
      SignatureException, XMLSignatureException {
    if (key == null || si == null || sig == null) {
      throw new NullPointerException();
    }
    try {
      byte[] data = read(((DOMSignedInfo) si).canonicalized((DOMCryptoContext) context));
      if (type == T_HMAC) {
        if (!(key instanceof SecretKey)) {
          throw new InvalidKeyException("key must be SecretKey");
        }
        Mac mac = Mac.getInstance(jca);
        mac.init(key);
        byte[] full = mac.doFinal(data);
        int bytes = full.length;
        if (params instanceof HMACParameterSpec) {
          int bits = ((HMACParameterSpec) params).getOutputLength();
          checkHmacLength(bits, full.length);
          if (bits % 8 != 0) {
            throw new SignatureException("unsupported HMAC output length");
          }
          bytes = bits / 8;
        }
        return java.security.MessageDigest.isEqual(Arrays.copyOf(full, bytes), sig);
      }
      if (!(key instanceof PublicKey)) {
        throw new InvalidKeyException("key must be PublicKey");
      }
      if (Boolean.TRUE.equals(((DOMCryptoContext) context).getProperty("org.jcp.xml.dsig.secureValidation"))
          && DOMUtils.restrictKey(key)) {
        throw new XMLSignatureException(new XMLSignatureException(DOMUtils.keyKind(key) + " keys less than "
            + DOMUtils.minKeySize(key) + " bits are forbidden when secure validation is enabled"));
      }
      Signature s = newSignature();
      s.initVerify((PublicKey) key);
      s.update(data);
      byte[] der = sig;
      if (type == T_EC) {
        der = concatToDer(sig);
      } else if (type == T_DSA) {
        der = concatToDer(sig);
      }
      return s.verify(der);
    } catch (IOException e) {
      throw new XMLSignatureException(e);
    } catch (NoSuchAlgorithmException e) {
      throw new XMLSignatureException(e);
    } catch (InvalidAlgorithmParameterException e) {
      throw new XMLSignatureException(e);
    }
  }

  static byte[] derToConcat(byte[] der, int orderBits) throws SignatureException {
    int size = (orderBits + 7) / 8;
    if (der.length < 8 || der[0] != 0x30) {
      throw new SignatureException("Invalid ECDSA signature format");
    }
    int offset;
    if ((der[1] & 0xff) < 0x80) {
      offset = 2;
    } else if ((der[1] & 0xff) == 0x81) {
      offset = 3;
    } else {
      throw new SignatureException("Invalid ECDSA signature format");
    }
    int rLen = der[offset + 1] & 0xff;
    int rOff = offset + 2;
    int sLen = der[rOff + rLen + 1] & 0xff;
    int sOff = rOff + rLen + 2;
    byte[] out = new byte[size * 2];
    copyInt(der, rOff, rLen, out, 0, size);
    copyInt(der, sOff, sLen, out, size, size);
    return out;
  }

  private static void copyInt(byte[] src, int off, int len, byte[] dst, int dstOff, int size) {
    while (len > 0 && src[off] == 0) {
      off++;
      len--;
    }
    System.arraycopy(src, off, dst, dstOff + size - len, len);
  }

  static byte[] concatToDer(byte[] sig) throws SignatureException {
    if (sig.length % 2 != 0 || sig.length == 0) {
      throw new SignatureException("Invalid signature length");
    }
    int half = sig.length / 2;
    byte[] r = derInt(sig, 0, half);
    byte[] s = derInt(sig, half, half);
    int len = r.length + s.length;
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    bos.write(0x30);
    if (len < 0x80) {
      bos.write(len);
    } else {
      bos.write(0x81);
      bos.write(len);
    }
    bos.write(r, 0, r.length);
    bos.write(s, 0, s.length);
    return bos.toByteArray();
  }

  private static byte[] derInt(byte[] src, int off, int len) {
    int start = off;
    int end = off + len;
    while (start < end - 1 && src[start] == 0) {
      start++;
    }
    boolean pad = (src[start] & 0x80) != 0;
    int n = end - start + (pad ? 1 : 0);
    byte[] out = new byte[n + 2];
    out[0] = 0x02;
    out[1] = (byte) n;
    System.arraycopy(src, start, out, 2 + (pad ? 1 : 0), end - start);
    return out;
  }

  public boolean equals(Object o) {
    return o instanceof SignatureMethod && algorithm.equals(((SignatureMethod) o).getAlgorithm());
  }

  public int hashCode() {
    return 17 * 31 + algorithm.hashCode();
  }
}
