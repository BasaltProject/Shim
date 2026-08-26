/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.jcp.xml.dsig.internal.dom;

import java.security.InvalidAlgorithmParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dom.DOMCryptoContext;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.XMLSignature;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public final class DOMDigestMethod implements DigestMethod {
  private final String algorithm;

  public DOMDigestMethod(String algorithm, AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException,
      NoSuchAlgorithmException {
    if (jcaName(algorithm) == null) {
      throw new NoSuchAlgorithmException("unsupported algorithm");
    }
    if (params != null) {
      throw new InvalidAlgorithmParameterException("no parameters should be specified for the " + jcaName(algorithm)
          + " DigestMethod algorithm");
    }
    this.algorithm = algorithm;
  }

  public DOMDigestMethod(Element elem) throws MarshalException {
    algorithm = DOMUtils.getAttributeValue(elem, "Algorithm");
    DOMUtils.checkAlgorithm(algorithm);
    if (algorithm == null || jcaName(algorithm) == null) {
      throw new MarshalException("unsupported DigestMethod algorithm: " + algorithm);
    }
  }

  static String shortName(String alg) {
    if (alg.equals(SHA1)) {
      return "SHA1";
    } else if (alg.equals(SHA224)) {
      return "SHA224";
    } else if (alg.equals(SHA256)) {
      return "SHA256";
    } else if (alg.equals(SHA384)) {
      return "SHA384";
    } else if (alg.equals(SHA512)) {
      return "SHA512";
    }
    return alg;
  }

  static String jcaName(String alg) {
    if (alg.equals(SHA1)) {
      return "SHA-1";
    } else if (alg.equals(SHA224)) {
      return "SHA-224";
    } else if (alg.equals(SHA256)) {
      return "SHA-256";
    } else if (alg.equals(SHA384)) {
      return "SHA-384";
    } else if (alg.equals(SHA512)) {
      return "SHA-512";
    } else if (alg.equals(RIPEMD160)) {
      return "RIPEMD160";
    } else if (alg.equals(SHA3_224)) {
      return "SHA3-224";
    } else if (alg.equals(SHA3_256)) {
      return "SHA3-256";
    } else if (alg.equals(SHA3_384)) {
      return "SHA3-384";
    } else if (alg.equals(SHA3_512)) {
      return "SHA3-512";
    }
    return null;
  }

  public String getAlgorithm() {
    return algorithm;
  }

  public AlgorithmParameterSpec getParameterSpec() {
    return null;
  }

  public boolean isFeatureSupported(String feature) {
    if (feature == null) {
      throw new NullPointerException();
    }
    return false;
  }

  MessageDigest newDigest() throws NoSuchAlgorithmException {
    return MessageDigest.getInstance(jcaName(algorithm));
  }

  void marshal(Node parent, String prefix, DOMCryptoContext context) throws MarshalException {
    Document ownerDoc = DOMUtils.getOwnerDocument(parent);
    Element dmElem = DOMUtils.createElement(ownerDoc, "DigestMethod", XMLSignature.XMLNS, prefix);
    DOMUtils.setAttribute(dmElem, "Algorithm", algorithm);
    parent.appendChild(dmElem);
  }

  public boolean equals(Object o) {
    return o instanceof DigestMethod && algorithm.equals(((DigestMethod) o).getAlgorithm());
  }

  public int hashCode() {
    return 17 * 31 + algorithm.hashCode();
  }
}
