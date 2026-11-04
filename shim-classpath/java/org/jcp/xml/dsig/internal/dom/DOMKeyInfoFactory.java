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
import java.security.PublicKey;
import java.util.List;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.URIDereferencer;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyName;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.keyinfo.PGPData;
import javax.xml.crypto.dsig.keyinfo.RetrievalMethod;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.keyinfo.X509IssuerSerial;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public final class DOMKeyInfoFactory extends KeyInfoFactory {
  public DOMKeyInfoFactory() {
  }

  @SuppressWarnings("rawtypes")
  public KeyInfo newKeyInfo(List content) {
    return newKeyInfo(content, null);
  }

  @SuppressWarnings("rawtypes")
  public KeyInfo newKeyInfo(List content, String id) {
    return new DOMKeyInfo(content, id);
  }

  public KeyName newKeyName(String name) {
    return new DOMKeyName(name);
  }

  public KeyValue newKeyValue(PublicKey key) throws KeyException {
    return new DOMKeyValue(key);
  }

  @SuppressWarnings("rawtypes")
  public PGPData newPGPData(byte[] keyId) {
    return newPGPData(keyId, null, null);
  }

  @SuppressWarnings("rawtypes")
  public PGPData newPGPData(byte[] keyId, byte[] keyPacket, List other) {
    return new DOMPGPData(keyId, keyPacket, other);
  }

  @SuppressWarnings("rawtypes")
  public PGPData newPGPData(byte[] keyPacket, List other) {
    return new DOMPGPData(keyPacket, other);
  }

  @SuppressWarnings("rawtypes")
  public RetrievalMethod newRetrievalMethod(String uri) {
    return newRetrievalMethod(uri, null, null);
  }

  @SuppressWarnings("rawtypes")
  public RetrievalMethod newRetrievalMethod(String uri, String type, List transforms) {
    if (uri == null) {
      throw new NullPointerException("uri must not be null");
    }
    return new DOMRetrievalMethod(uri, type, transforms);
  }

  @SuppressWarnings("rawtypes")
  public X509Data newX509Data(List content) {
    return new DOMX509Data(content);
  }

  public X509IssuerSerial newX509IssuerSerial(String issuerName, BigInteger serialNumber) {
    return new DOMX509IssuerSerial(issuerName, serialNumber);
  }

  public boolean isFeatureSupported(String feature) {
    if (feature == null) {
      throw new NullPointerException();
    }
    return false;
  }

  public URIDereferencer getURIDereferencer() {
    return DOMURIDereferencer.INSTANCE;
  }

  public KeyInfo unmarshalKeyInfo(XMLStructure xmlStructure) throws MarshalException {
    if (xmlStructure == null) {
      throw new NullPointerException("xmlStructure cannot be null");
    }
    if (!(xmlStructure instanceof DOMStructure)) {
      throw new ClassCastException("xmlStructure must be of type DOMStructure");
    }
    Node node = ((DOMStructure) xmlStructure).getNode();
    node.normalize();
    Element element = null;
    if (node.getNodeType() == Node.DOCUMENT_NODE) {
      element = ((org.w3c.dom.Document) node).getDocumentElement();
    } else if (node.getNodeType() == Node.ELEMENT_NODE) {
      element = (Element) node;
    } else {
      throw new MarshalException("xmlStructure does not contain a proper Node");
    }
    String tag = element.getLocalName();
    String namespace = element.getNamespaceURI();
    if (tag == null || namespace == null) {
      throw new MarshalException("Document implementation must support DOM Level 2 and be namespace aware");
    }
    if (tag.equals("KeyInfo") && javax.xml.crypto.dsig.XMLSignature.XMLNS.equals(namespace)) {
      return new DOMKeyInfo(element);
    }
    throw new MarshalException("invalid KeyInfo tag: " + namespace + ":" + tag);
  }
}
