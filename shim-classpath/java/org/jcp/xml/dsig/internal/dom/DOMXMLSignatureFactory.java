/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.jcp.xml.dsig.internal.dom;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.List;
import javax.xml.crypto.Data;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.URIDereferencer;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Manifest;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignatureProperties;
import javax.xml.crypto.dsig.SignatureProperty;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLObject;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.XMLValidateContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.DigestMethodParameterSpec;
import javax.xml.crypto.dsig.spec.SignatureMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DOMXMLSignatureFactory extends XMLSignatureFactory {
  public DOMXMLSignatureFactory() {
  }

  public XMLSignature newXMLSignature(SignedInfo si, KeyInfo ki) {
    return new DOMXMLSignature(si, ki, null, null, null);
  }

  @SuppressWarnings("rawtypes")
  public XMLSignature newXMLSignature(SignedInfo si, KeyInfo ki, List objects, String id, String signatureValueId) {
    return new DOMXMLSignature(si, ki, objects, id, signatureValueId);
  }

  public Reference newReference(String uri, DigestMethod dm) {
    return newReference(uri, dm, null, null, null);
  }

  @SuppressWarnings("rawtypes")
  public Reference newReference(String uri, DigestMethod dm, List transforms, String type, String id) {
    return new DOMReference(uri, type, dm, null, null, transforms, id, null);
  }

  @SuppressWarnings("rawtypes")
  public Reference newReference(String uri, DigestMethod dm, List transforms, String type, String id,
      byte[] digestValue) {
    if (digestValue == null) {
      throw new NullPointerException("digestValue cannot be null");
    }
    return new DOMReference(uri, type, dm, null, null, transforms, id, digestValue);
  }

  @SuppressWarnings("rawtypes")
  public Reference newReference(String uri, DigestMethod dm, List appliedTransforms, Data result, List transforms,
      String type, String id) {
    if (appliedTransforms == null) {
      throw new NullPointerException("appliedTransforms cannot be null");
    }
    if (appliedTransforms.isEmpty()) {
      throw new NullPointerException("appliedTransforms cannot be empty");
    }
    if (result == null) {
      throw new NullPointerException("result cannot be null");
    }
    return new DOMReference(uri, type, dm, appliedTransforms, result, transforms, id, null);
  }

  @SuppressWarnings("rawtypes")
  public SignedInfo newSignedInfo(CanonicalizationMethod cm, SignatureMethod sm, List references) {
    return newSignedInfo(cm, sm, references, null);
  }

  @SuppressWarnings("rawtypes")
  public SignedInfo newSignedInfo(CanonicalizationMethod cm, SignatureMethod sm, List references, String id) {
    return new DOMSignedInfo(cm, sm, references, id);
  }

  @SuppressWarnings("rawtypes")
  public XMLObject newXMLObject(List content, String id, String mimeType, String encoding) {
    return new DOMXMLObject(content, id, mimeType, encoding);
  }

  @SuppressWarnings("rawtypes")
  public Manifest newManifest(List references) {
    return newManifest(references, null);
  }

  @SuppressWarnings("rawtypes")
  public Manifest newManifest(List references, String id) {
    return new DOMManifest(references, id);
  }

  @SuppressWarnings("rawtypes")
  public SignatureProperties newSignatureProperties(List props, String id) {
    return new DOMSignatureProperties(props, id);
  }

  @SuppressWarnings("rawtypes")
  public SignatureProperty newSignatureProperty(List content, String target, String id) {
    return new DOMSignatureProperty(content, target, id);
  }

  public XMLSignature unmarshalXMLSignature(XMLValidateContext context) throws MarshalException {
    if (context == null) {
      throw new NullPointerException("context cannot be null");
    }
    boolean secure = Boolean.TRUE.equals(context.getProperty("org.jcp.xml.dsig.secureValidation"));
    DOMUtils.setSecureValidation(secure);
    try {
      return unmarshal(((DOMValidateContext) context).getNode());
    } finally {
      DOMUtils.setSecureValidation(false);
    }
  }

  public XMLSignature unmarshalXMLSignature(XMLStructure xmlStructure) throws MarshalException {
    if (xmlStructure == null) {
      throw new NullPointerException("xmlStructure cannot be null");
    }
    if (!(xmlStructure instanceof DOMStructure)) {
      throw new ClassCastException("xmlStructure must be of type DOMStructure");
    }
    return unmarshal(((DOMStructure) xmlStructure).getNode());
  }

  private XMLSignature unmarshal(Node node) throws MarshalException {
    node.normalize();
    Element element = null;
    if (node.getNodeType() == Node.DOCUMENT_NODE) {
      element = ((org.w3c.dom.Document) node).getDocumentElement();
    } else if (node.getNodeType() == Node.ELEMENT_NODE) {
      element = (Element) node;
    } else {
      throw new MarshalException("Signature element is not a proper Node");
    }
    String tag = element.getLocalName();
    String namespace = element.getNamespaceURI();
    if (tag == null || namespace == null) {
      throw new MarshalException("Document implementation must support DOM Level 2 and be namespace aware");
    }
    if (tag.equals("Signature") && XMLSignature.XMLNS.equals(namespace)) {
      return new DOMXMLSignature(element);
    }
    throw new MarshalException("Invalid Signature tag: " + namespace + ":" + tag);
  }

  public boolean isFeatureSupported(String feature) {
    if (feature == null) {
      throw new NullPointerException();
    }
    return false;
  }

  public DigestMethod newDigestMethod(String algorithm, DigestMethodParameterSpec params)
      throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
    return new DOMDigestMethod(algorithm, params);
  }

  public SignatureMethod newSignatureMethod(String algorithm, SignatureMethodParameterSpec params)
      throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
    return new DOMSignatureMethod(algorithm, params);
  }

  public Transform newTransform(String algorithm, TransformParameterSpec params) throws NoSuchAlgorithmException,
      InvalidAlgorithmParameterException {
    if (!DOMTransform.isKnown(algorithm) || isC14N(algorithm)) {
      throw new NoSuchAlgorithmException(algorithm + " algorithm and DOM mechanism not available");
    }
    return new DOMTransform(algorithm, params);
  }

  public Transform newTransform(String algorithm, XMLStructure params) throws NoSuchAlgorithmException,
      InvalidAlgorithmParameterException {
    if (!DOMTransform.isKnown(algorithm) || isC14N(algorithm)) {
      throw new NoSuchAlgorithmException(algorithm + " algorithm and DOM mechanism not available");
    }
    try {
      return new DOMTransform(elementWith("Transform", algorithm, params));
    } catch (MarshalException e) {
      throw new InvalidAlgorithmParameterException(e);
    }
  }

  private static boolean isC14N(String alg) {
    return alg.equals(CanonicalizationMethod.INCLUSIVE) && false;
  }

  static Element elementWith(String name, String algorithm, XMLStructure params) {
    javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
    f.setNamespaceAware(true);
    try {
      org.w3c.dom.Document d = f.newDocumentBuilder().newDocument();
      Element e = d.createElementNS(XMLSignature.XMLNS, name);
      e.setAttributeNS(null, "Algorithm", algorithm);
      d.appendChild(e);
      if (params != null) {
        if (!(params instanceof DOMStructure)) {
          throw new ClassCastException("params must be of type DOMStructure");
        }
        e.appendChild(d.importNode(((DOMStructure) params).getNode(), true));
      }
      return e;
    } catch (javax.xml.parsers.ParserConfigurationException ex) {
      throw new IllegalStateException(ex);
    }
  }

  public CanonicalizationMethod newCanonicalizationMethod(String algorithm, C14NMethodParameterSpec params)
      throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
    return new DOMCanonicalizationMethod(algorithm, params);
  }

  public CanonicalizationMethod newCanonicalizationMethod(String algorithm, XMLStructure params)
      throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
    try {
      return new DOMCanonicalizationMethod(elementWith("CanonicalizationMethod", algorithm, params));
    } catch (MarshalException e) {
      throw new InvalidAlgorithmParameterException(e);
    }
  }

  public URIDereferencer getURIDereferencer() {
    return DOMURIDereferencer.INSTANCE;
  }
}
