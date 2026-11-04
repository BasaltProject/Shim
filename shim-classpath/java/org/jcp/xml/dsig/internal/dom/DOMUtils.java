/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.jcp.xml.dsig.internal.dom;

import java.util.ArrayList;
import java.util.List;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.dsig.XMLSignature;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public final class DOMUtils {
  private static final ThreadLocal<Boolean> SECURE = new ThreadLocal<Boolean>();

  private static final java.util.Set<String> DISALLOWED = new java.util.HashSet<String>(java.util.Arrays.asList(
      "http://www.w3.org/TR/1999/REC-xslt-19991116", "http://www.w3.org/2001/04/xmldsig-more#rsa-md5",
      "http://www.w3.org/2001/04/xmldsig-more#hmac-md5", "http://www.w3.org/2001/04/xmldsig-more#md5",
      "http://www.w3.org/2000/09/xmldsig#sha1", "http://www.w3.org/2000/09/xmldsig#dsa-sha1",
      "http://www.w3.org/2000/09/xmldsig#rsa-sha1", "http://www.w3.org/2007/05/xmldsig-more#sha1-rsa-MGF1",
      "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha1"));

  private DOMUtils() {
  }

  static boolean secureValidation() {
    return Boolean.TRUE.equals(SECURE.get());
  }

  static void setSecureValidation(boolean secure) {
    if (secure) {
      SECURE.set(Boolean.TRUE);
    } else {
      SECURE.remove();
    }
  }

  static void checkAlgorithm(String alg) throws MarshalException {
    if (secureValidation() && DISALLOWED.contains(alg)) {
      throw new MarshalException("It is forbidden to use algorithm " + alg + " when secure validation is enabled");
    }
  }

  static boolean restrictKey(java.security.Key key) {
    int size;
    String alg = key.getAlgorithm();
    if (key instanceof java.security.interfaces.RSAKey) {
      size = ((java.security.interfaces.RSAKey) key).getModulus().bitLength();
      return size < 1024;
    } else if (key instanceof java.security.interfaces.DSAKey) {
      size = ((java.security.interfaces.DSAKey) key).getParams().getP().bitLength();
      return size < 1024;
    } else if (key instanceof java.security.interfaces.ECKey) {
      size = ((java.security.interfaces.ECKey) key).getParams().getOrder().bitLength();
      return size < 224;
    }
    return false;
  }

  static String keyKind(java.security.Key key) {
    if (key instanceof java.security.interfaces.RSAKey) {
      return "RSA";
    } else if (key instanceof java.security.interfaces.DSAKey) {
      return "DSA";
    }
    return "EC";
  }

  static int minKeySize(java.security.Key key) {
    return key instanceof java.security.interfaces.ECKey && !(key instanceof java.security.interfaces.RSAKey) ? 224 : 1024;
  }

  public static Document getOwnerDocument(Node node) {
    if (node.getNodeType() == Node.DOCUMENT_NODE) {
      return (Document) node;
    }
    return node.getOwnerDocument();
  }

  public static Element createElement(Document doc, String tag, String nsURI, String prefix) {
    String qName = (prefix == null || prefix.length() == 0) ? tag : prefix + ":" + tag;
    return doc.createElementNS(nsURI, qName);
  }

  public static void setAttribute(Element elem, String name, String value) {
    if (value == null) {
      return;
    }
    elem.setAttributeNS(null, name, value);
  }

  public static void setAttributeID(Element elem, String name, String value) {
    if (value == null) {
      return;
    }
    elem.setAttributeNS(null, name, value);
    elem.setIdAttributeNS(null, name, true);
  }

  public static Element getFirstChildElement(Node node) {
    Node child = node.getFirstChild();
    while (child != null && child.getNodeType() != Node.ELEMENT_NODE) {
      child = child.getNextSibling();
    }
    return (Element) child;
  }

  public static Element getLastChildElement(Node node) {
    Node child = node.getLastChild();
    while (child != null && child.getNodeType() != Node.ELEMENT_NODE) {
      child = child.getPreviousSibling();
    }
    return (Element) child;
  }

  public static Element getNextSiblingElement(Node node) {
    Node sibling = node.getNextSibling();
    while (sibling != null && sibling.getNodeType() != Node.ELEMENT_NODE) {
      sibling = sibling.getNextSibling();
    }
    return (Element) sibling;
  }

  public static String getAttributeValue(Element elem, String name) {
    Attr attr = elem.getAttributeNodeNS(null, name);
    return (attr == null) ? null : attr.getValue();
  }

  public static List<Element> children(Node parent) {
    List<Element> l = new ArrayList<Element>();
    for (Node c = parent.getFirstChild(); c != null; c = c.getNextSibling()) {
      if (c.getNodeType() == Node.ELEMENT_NODE) {
        l.add((Element) c);
      }
    }
    return l;
  }

  public static boolean nodeNameEquals(Node node, String name, String ns) {
    return name.equals(node.getLocalName()) && ns.equals(node.getNamespaceURI());
  }

  public static String getSignaturePrefix(XMLCryptoContext context) {
    return context.getNamespacePrefix(XMLSignature.XMLNS, context.getDefaultNamespacePrefix());
  }

  public static String textOf(Element e) {
    StringBuilder b = new StringBuilder();
    for (Node c = e.getFirstChild(); c != null; c = c.getNextSibling()) {
      if (c.getNodeType() == Node.TEXT_NODE || c.getNodeType() == Node.CDATA_SECTION_NODE) {
        b.append(c.getNodeValue());
      }
    }
    return b.toString();
  }

  public static Element textElement(Document doc, String tag, String prefix, String text) {
    Element e = createElement(doc, tag, XMLSignature.XMLNS, prefix);
    e.appendChild(doc.createTextNode(text));
    return e;
  }

  public static byte[] decodeBase64(String s) {
    StringBuilder b = new StringBuilder(s.length());
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (!Character.isWhitespace(c)) {
        b.append(c);
      }
    }
    return java.util.Base64.getDecoder().decode(b.toString());
  }

  public static String encodeBase64(byte[] data) {
    return java.util.Base64.getMimeEncoder().encodeToString(data);
  }

  public static java.math.BigInteger decodeBigInteger(String s) {
    return new java.math.BigInteger(1, decodeBase64(s));
  }

  public static String encodeBigInteger(java.math.BigInteger v, int length) {
    byte[] b = v.toByteArray();
    if (length < 0) {
      if (b.length > 1 && b[0] == 0) {
        b = java.util.Arrays.copyOfRange(b, 1, b.length);
      }
      return encodeBase64(b);
    }
    byte[] r = new byte[length];
    if (b.length > length) {
      System.arraycopy(b, b.length - length, r, 0, length);
    } else {
      System.arraycopy(b, 0, r, length - b.length, b.length);
    }
    return encodeBase64(r);
  }
}
