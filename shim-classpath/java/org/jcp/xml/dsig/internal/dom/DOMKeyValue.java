/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.jcp.xml.dsig.internal.dom;

import java.math.BigInteger;
import java.security.KeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.DSAPublicKeySpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMCryptoContext;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public final class DOMKeyValue implements KeyValue {
  static final String DSIG11_NS = "http://www.w3.org/2009/xmldsig11#";

  private final PublicKey publicKey;
  private final Element kvElem;

  public DOMKeyValue(PublicKey key) throws KeyException {
    if (key == null) {
      throw new NullPointerException("key cannot be null");
    }
    if (!(key instanceof RSAPublicKey) && !(key instanceof DSAPublicKey) && !(key instanceof ECPublicKey)) {
      throw new KeyException("unsupported key algorithm: " + key.getAlgorithm());
    }
    this.publicKey = key;
    this.kvElem = null;
  }

  public DOMKeyValue(Element kvElem) throws MarshalException {
    this.kvElem = kvElem;
    Element child = DOMUtils.getFirstChildElement(kvElem);
    if (child == null) {
      throw new MarshalException("KeyValue must contain a key");
    }
    PublicKey k = null;
    try {
      String n = child.getLocalName();
      if ("RSAKeyValue".equals(n)) {
        BigInteger mod = DOMUtils.decodeBigInteger(DOMUtils.textOf(DOMUtils.getFirstChildElement(child)));
        BigInteger exp = DOMUtils.decodeBigInteger(DOMUtils.textOf(DOMUtils.getNextSiblingElement(
            DOMUtils.getFirstChildElement(child))));
        k = KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(mod, exp));
      } else if ("DSAKeyValue".equals(n)) {
        Element p = DOMUtils.getFirstChildElement(child);
        BigInteger[] v = new BigInteger[4];
        int i = 0;
        String[] order = {"P", "Q", "G", "Y"};
        for (Element x : DOMUtils.children(child)) {
          for (int j = 0; j < 4; j++) {
            if (order[j].equals(x.getLocalName())) {
              v[j] = DOMUtils.decodeBigInteger(DOMUtils.textOf(x));
            }
          }
        }
        k = KeyFactory.getInstance("DSA").generatePublic(new DSAPublicKeySpec(v[3], v[0], v[1], v[2]));
      } else if ("ECKeyValue".equals(n)) {
        Element nc = DOMUtils.getFirstChildElement(child);
        Element pk = DOMUtils.getNextSiblingElement(nc);
        String uri = DOMUtils.getAttributeValue(nc, "URI");
        String oid = uri.startsWith("urn:oid:") ? uri.substring(8) : uri;
        java.security.AlgorithmParameters ap = java.security.AlgorithmParameters.getInstance("EC");
        ap.init(new java.security.spec.ECGenParameterSpec(oid));
        ECParameterSpec spec = ap.getParameterSpec(ECParameterSpec.class);
        byte[] point = DOMUtils.decodeBase64(DOMUtils.textOf(pk));
        int len = (point.length - 1) / 2;
        BigInteger x = new BigInteger(1, java.util.Arrays.copyOfRange(point, 1, 1 + len));
        BigInteger y = new BigInteger(1, java.util.Arrays.copyOfRange(point, 1 + len, point.length));
        k = KeyFactory.getInstance("EC").generatePublic(new ECPublicKeySpec(new ECPoint(x, y), spec));
      }
    } catch (Exception e) {
      throw new MarshalException(e);
    }
    this.publicKey = k;
  }

  public PublicKey getPublicKey() throws KeyException {
    if (publicKey == null) {
      throw new KeyException("can't convert KeyValue to PublicKey");
    }
    return publicKey;
  }

  public boolean isFeatureSupported(String feature) {
    if (feature == null) {
      throw new NullPointerException();
    }
    return false;
  }

  public void marshal(Node parent, String dsPrefix, DOMCryptoContext context) throws MarshalException {
    Document ownerDoc = DOMUtils.getOwnerDocument(parent);
    Element kvElem = DOMUtils.createElement(ownerDoc, "KeyValue", XMLSignature.XMLNS, dsPrefix);
    if (publicKey instanceof RSAPublicKey) {
      RSAPublicKey rsa = (RSAPublicKey) publicKey;
      Element rsaElem = DOMUtils.createElement(ownerDoc, "RSAKeyValue", XMLSignature.XMLNS, dsPrefix);
      rsaElem.appendChild(DOMUtils.textElement(ownerDoc, "Modulus", dsPrefix,
          DOMUtils.encodeBigInteger(rsa.getModulus(), -1)));
      rsaElem.appendChild(DOMUtils.textElement(ownerDoc, "Exponent", dsPrefix,
          DOMUtils.encodeBigInteger(rsa.getPublicExponent(), -1)));
      kvElem.appendChild(rsaElem);
    } else if (publicKey instanceof DSAPublicKey) {
      DSAPublicKey dsa = (DSAPublicKey) publicKey;
      Element e = DOMUtils.createElement(ownerDoc, "DSAKeyValue", XMLSignature.XMLNS, dsPrefix);
      e.appendChild(DOMUtils.textElement(ownerDoc, "P", dsPrefix, DOMUtils.encodeBigInteger(dsa.getParams().getP(), -1)));
      e.appendChild(DOMUtils.textElement(ownerDoc, "Q", dsPrefix, DOMUtils.encodeBigInteger(dsa.getParams().getQ(), -1)));
      e.appendChild(DOMUtils.textElement(ownerDoc, "G", dsPrefix, DOMUtils.encodeBigInteger(dsa.getParams().getG(), -1)));
      e.appendChild(DOMUtils.textElement(ownerDoc, "Y", dsPrefix, DOMUtils.encodeBigInteger(dsa.getY(), -1)));
      kvElem.appendChild(e);
    } else if (publicKey instanceof ECPublicKey) {
      ECPublicKey ec = (ECPublicKey) publicKey;
      String oid;
      try {
        java.security.AlgorithmParameters ap = java.security.AlgorithmParameters.getInstance("EC");
        ap.init(ec.getParams());
        oid = ap.getParameterSpec(java.security.spec.ECGenParameterSpec.class).getName();
      } catch (Exception ex) {
        throw new MarshalException(ex);
      }
      Element e = DOMUtils.createElement(ownerDoc, "ECKeyValue", DSIG11_NS, "dsig11");
      e.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:dsig11", DSIG11_NS);
      Element nc = DOMUtils.createElement(ownerDoc, "NamedCurve", DSIG11_NS, "dsig11");
      nc.setAttributeNS(null, "URI", "urn:oid:" + oid);
      e.appendChild(nc);
      int size = (ec.getParams().getOrder().bitLength() + 7) / 8;
      byte[] x = fixed(ec.getW().getAffineX(), size);
      byte[] y = fixed(ec.getW().getAffineY(), size);
      byte[] point = new byte[1 + 2 * size];
      point[0] = 4;
      System.arraycopy(x, 0, point, 1, size);
      System.arraycopy(y, 0, point, 1 + size, size);
      Element pk = DOMUtils.createElement(ownerDoc, "PublicKey", DSIG11_NS, "dsig11");
      pk.appendChild(ownerDoc.createTextNode(DOMUtils.encodeBase64(point)));
      e.appendChild(pk);
      kvElem.appendChild(e);
    }
    parent.appendChild(kvElem);
  }

  private static byte[] fixed(BigInteger v, int size) {
    byte[] b = v.toByteArray();
    byte[] r = new byte[size];
    if (b.length > size) {
      System.arraycopy(b, b.length - size, r, 0, size);
    } else {
      System.arraycopy(b, 0, r, size - b.length, b.length);
    }
    return r;
  }

  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof KeyValue)) {
      return false;
    }
    try {
      return publicKey != null && publicKey.equals(((KeyValue) obj).getPublicKey());
    } catch (KeyException e) {
      return false;
    }
  }

  public int hashCode() {
    return 17 * 31 + (publicKey == null ? 0 : publicKey.hashCode());
  }
}
