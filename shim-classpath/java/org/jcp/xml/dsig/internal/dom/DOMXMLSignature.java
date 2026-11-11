/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.jcp.xml.dsig.internal.dom;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.KeySelectorException;
import javax.xml.crypto.KeySelectorResult;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dom.DOMCryptoContext;
import javax.xml.crypto.dsig.Manifest;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.XMLObject;
import javax.xml.crypto.dsig.XMLSignContext;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLValidateContext;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public final class DOMXMLSignature implements XMLSignature {
  private final String id;
  private final SignatureValue sv;
  private final KeyInfo ki;
  private final List<XMLObject> objects;
  private final SignedInfo si;
  private boolean validated;
  private boolean validationStatus;
  private KeySelectorResult ksr;
  private Element sigElem;
  private DOMSignatureValue domSv;

  @SuppressWarnings("rawtypes")
  public DOMXMLSignature(SignedInfo si, KeyInfo ki, List objs, String id, String signatureValueId) {
    if (si == null) {
      throw new NullPointerException("signedInfo cannot be null");
    }
    this.si = si;
    this.id = id;
    this.sv = new DOMSignatureValue(signatureValueId);
    this.domSv = (DOMSignatureValue) sv;
    if (objs == null) {
      this.objects = Collections.emptyList();
    } else {
      List<XMLObject> objectsCopy = new ArrayList<XMLObject>(objs);
      for (int i = 0, size = objectsCopy.size(); i < size; i++) {
        if (!(objectsCopy.get(i) instanceof XMLObject)) {
          throw new ClassCastException("objs[" + i + "] is not an XMLObject");
        }
      }
      this.objects = Collections.unmodifiableList(objectsCopy);
    }
    this.ki = ki;
  }

  public DOMXMLSignature(Element sigElem) throws MarshalException {
    this.sigElem = sigElem;
    this.id = DOMUtils.getAttributeValue(sigElem, "Id");
    if (id != null) {
      sigElem.setIdAttributeNS(null, "Id", true);
    }
    Element siElem = DOMUtils.getFirstChildElement(sigElem);
    if (siElem == null || !"SignedInfo".equals(siElem.getLocalName())) {
      throw new MarshalException("Missing SignedInfo element");
    }
    this.si = new DOMSignedInfo(siElem, sigElem.getOwnerDocument());
    Element sigValElem = DOMUtils.getNextSiblingElement(siElem);
    if (sigValElem == null || !"SignatureValue".equals(sigValElem.getLocalName())) {
      throw new MarshalException("Invalid element name: " + (sigValElem == null ? "null" : sigValElem.getLocalName())
          + ", expected SignatureValue");
    }
    this.domSv = new DOMSignatureValue(sigValElem);
    this.sv = domSv;
    Element next = DOMUtils.getNextSiblingElement(sigValElem);
    KeyInfo newKi = null;
    if (next != null && "KeyInfo".equals(next.getLocalName())) {
      newKi = new DOMKeyInfo(next);
      next = DOMUtils.getNextSiblingElement(next);
    }
    this.ki = newKi;
    List<XMLObject> newObjects = new ArrayList<XMLObject>();
    while (next != null) {
      if (!"Object".equals(next.getLocalName())) {
        throw new MarshalException("Invalid element name: " + next.getLocalName() + ", expected Object");
      }
      newObjects.add(new DOMXMLObject(next));
      next = DOMUtils.getNextSiblingElement(next);
    }
    this.objects = Collections.unmodifiableList(newObjects);
  }

  public String getId() {
    return id;
  }

  public KeyInfo getKeyInfo() {
    return ki;
  }

  public SignedInfo getSignedInfo() {
    return si;
  }

  public List<XMLObject> getObjects() {
    return objects;
  }

  public SignatureValue getSignatureValue() {
    return sv;
  }

  public KeySelectorResult getKeySelectorResult() {
    return ksr;
  }

  public boolean isFeatureSupported(String feature) {
    if (feature == null) {
      throw new NullPointerException();
    }
    return false;
  }

  public void marshal(Node parent, Node nextSibling, String dsPrefix, DOMCryptoContext context)
      throws MarshalException {
    Document ownerDoc = DOMUtils.getOwnerDocument(parent);
    sigElem = DOMUtils.createElement(ownerDoc, "Signature", XMLSignature.XMLNS, dsPrefix);
    if (dsPrefix == null || dsPrefix.length() == 0) {
      sigElem.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", XMLSignature.XMLNS);
    } else {
      sigElem.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:" + dsPrefix, XMLSignature.XMLNS);
    }
    DOMUtils.setAttributeID(sigElem, "Id", id);
    parent.insertBefore(sigElem, nextSibling);
    ((DOMSignedInfo) si).marshal(sigElem, dsPrefix, context);
    domSv.marshal(sigElem, dsPrefix, context);
    if (ki != null) {
      ((DOMKeyInfo) ki).marshal(sigElem, null, dsPrefix, context);
    }
    for (XMLObject obj : objects) {
      ((DOMXMLObject) obj).marshal(sigElem, dsPrefix, context);
    }
  }

  public void sign(XMLSignContext signContext) throws MarshalException, XMLSignatureException {
    if (signContext == null) {
      throw new NullPointerException("signContext cannot be null");
    }
    DOMSignContext context = (DOMSignContext) signContext;
    marshal(context.getParent(), context.getNextSibling(), DOMUtils.getSignaturePrefix(context), context);
    List<Reference> allReferences = new ArrayList<Reference>(si.getReferences());
    List<Manifest> manifests = new ArrayList<Manifest>();
    java.util.Map<String, List<Manifest>> byObjectId = new java.util.HashMap<String, List<Manifest>>();
    for (XMLObject obj : objects) {
      for (XMLStructure_ s : XMLStructure_.of(obj)) {
        if (s.manifest != null) {
          manifests.add(s.manifest);
          allReferences.addAll(s.manifest.getReferences());
          if (obj.getId() != null) {
            List<Manifest> l = byObjectId.get(obj.getId());
            if (l == null) {
              l = new ArrayList<Manifest>();
              byObjectId.put(obj.getId(), l);
            }
            l.add(s.manifest);
          }
        }
      }
    }
    java.util.Set<Reference> digested = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<Reference, Boolean>());
    for (Reference ref : allReferences) {
      digestReference(ref, context, manifests, byObjectId, digested);
    }
    for (Reference ref : allReferences) {
      if (!digested.contains(ref)) {
        ((DOMReference) ref).digest(context);
      }
    }
    KeySelectorResult ksResult;
    try {
      ksResult = context.getKeySelector().select(ki, KeySelector.Purpose.SIGN, si.getSignatureMethod(), context);
    } catch (KeySelectorException kse) {
      throw new XMLSignatureException("cannot find key", kse);
    }
    this.ksr = ksResult;
    Key signingKey = ksResult.getKey();
    if (signingKey == null) {
      throw new XMLSignatureException("the keySelector did not find a signing key");
    }
    byte[] val;
    try {
      val = ((DOMSignatureMethod) si.getSignatureMethod()).sign(signingKey, si, context);
    } catch (InvalidKeyException ike) {
      throw new XMLSignatureException(ike);
    }
    domSv.setValue(val);
    validated = false;
  }

  public boolean validate(XMLValidateContext validateContext) throws XMLSignatureException {
    if (validateContext == null) {
      throw new NullPointerException("validateContext is null");
    }
    if (!(validateContext instanceof DOMValidateContext)) {
      throw new ClassCastException("validateContext must be of type DOMValidateContext");
    }
    if (validated) {
      return validationStatus;
    }
    boolean sigValidity = domSv.validate(validateContext);
    if (!sigValidity) {
      validationStatus = false;
      validated = true;
      return validationStatus;
    }
    List<Reference> refs = new ArrayList<Reference>(si.getReferences());
    boolean validateRefs = true;
    boolean refValidity = true;
    for (Reference ref : refs) {
      boolean refValid = ref.validate(validateContext);
      if (!refValid) {
        refValidity = false;
      }
    }
    if (Boolean.TRUE.equals(validateContext.getProperty("org.jcp.xml.dsig.validateManifests"))) {
      for (XMLObject obj : objects) {
        for (XMLStructure_ s : XMLStructure_.of(obj)) {
          if (s.manifest != null) {
            for (Reference ref : s.manifest.getReferences()) {
              ref.validate(validateContext);
            }
          }
        }
      }
    }
    validationStatus = refValidity;
    validated = true;
    return validationStatus;
  }

  private void digestReference(Reference ref, DOMSignContext context, List<Manifest> manifests,
      java.util.Map<String, List<Manifest>> byObjectId, java.util.Set<Reference> digested)
      throws XMLSignatureException {
    if (digested.contains(ref)) {
      return;
    }
    digested.add(ref);
    String uri = ref.getURI();
    if (uri != null && uri.startsWith("#")) {
      String id = uri.substring(1);
      List<Manifest> deps = new ArrayList<Manifest>();
      if (byObjectId.containsKey(id)) {
        deps.addAll(byObjectId.get(id));
      }
      for (Manifest m : manifests) {
        if (id.equals(m.getId())) {
          deps.add(m);
        }
      }
      for (Manifest m : deps) {
        for (Reference r : m.getReferences()) {
          digestReference(r, context, manifests, byObjectId, digested);
        }
      }
    }
    ((DOMReference) ref).digest(context);
  }

  private static final class XMLStructure_ {
    final Manifest manifest;

    XMLStructure_(Manifest m) {
      this.manifest = m;
    }

    static List<XMLStructure_> of(XMLObject obj) {
      List<XMLStructure_> l = new ArrayList<XMLStructure_>();
      for (javax.xml.crypto.XMLStructure s : obj.getContent()) {
        if (s instanceof Manifest) {
          l.add(new XMLStructure_((Manifest) s));
        }
      }
      return l;
    }
  }

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof XMLSignature)) {
      return false;
    }
    XMLSignature osig = (XMLSignature) o;
    boolean idEqual = id == null ? osig.getId() == null : id.equals(osig.getId());
    boolean keyInfoEqual = ki == null ? osig.getKeyInfo() == null : ki.equals(osig.getKeyInfo());
    return idEqual && keyInfoEqual && sv.equals(osig.getSignatureValue()) && si.equals(osig.getSignedInfo())
        && objects.equals(osig.getObjects());
  }

  public int hashCode() {
    int result = 17;
    if (id != null) {
      result = 31 * result + id.hashCode();
    }
    if (ki != null) {
      result = 31 * result + ki.hashCode();
    }
    result = 31 * result + sv.hashCode();
    result = 31 * result + si.hashCode();
    result = 31 * result + objects.hashCode();
    return result;
  }

  public final class DOMSignatureValue implements SignatureValue {
    private final String svId;
    private byte[] value;
    private String valueBase64;
    private Element sigValueElem;
    private boolean svValidated;
    private boolean svStatus;

    DOMSignatureValue(String id) {
      this.svId = id;
    }

    DOMSignatureValue(Element sigValueElem) throws MarshalException {
      try {
        this.value = DOMUtils.decodeBase64(DOMUtils.textOf(sigValueElem));
      } catch (IllegalArgumentException e) {
        throw new MarshalException(e);
      }
      this.svId = DOMUtils.getAttributeValue(sigValueElem, "Id");
      if (svId != null) {
        sigValueElem.setIdAttributeNS(null, "Id", true);
      }
      this.sigValueElem = sigValueElem;
    }

    public String getId() {
      return svId;
    }

    public byte[] getValue() {
      return value == null ? null : value.clone();
    }

    void setValue(byte[] v) {
      this.value = v;
      if (sigValueElem != null) {
        while (sigValueElem.getFirstChild() != null) {
          sigValueElem.removeChild(sigValueElem.getFirstChild());
        }
        sigValueElem.appendChild(DOMUtils.getOwnerDocument(sigValueElem).createTextNode(DOMUtils.encodeBase64(v)));
      }
    }

    public boolean validate(XMLValidateContext validateContext) throws XMLSignatureException {
      if (validateContext == null) {
        throw new NullPointerException("validateContext cannot be null");
      }
      if (svValidated) {
        return svStatus;
      }
      KeySelectorResult ksResult;
      try {
        ksResult = validateContext.getKeySelector().select(ki, KeySelector.Purpose.VERIFY, si.getSignatureMethod(),
            validateContext);
      } catch (KeySelectorException kse) {
        throw new XMLSignatureException("cannot find validation key", kse);
      }
      Key validationKey = null;
      if (ksResult != null) {
        validationKey = ksResult.getKey();
      }
      if (validationKey == null) {
        throw new XMLSignatureException("the keyselector did not find a validation key");
      }
      DOMXMLSignature.this.ksr = ksResult;
      try {
        svStatus = ((DOMSignatureMethod) si.getSignatureMethod()).verify(validationKey, si, value, validateContext);
      } catch (InvalidKeyException ike) {
        throw new XMLSignatureException(ike);
      } catch (SignatureException se) {
        throw new XMLSignatureException(se);
      }
      svValidated = true;
      return svStatus;
    }

    public boolean isFeatureSupported(String feature) {
      if (feature == null) {
        throw new NullPointerException();
      }
      return false;
    }

    void marshal(Node parent, String dsPrefix, DOMCryptoContext context) throws MarshalException {
      Document ownerDoc = DOMUtils.getOwnerDocument(parent);
      sigValueElem = DOMUtils.createElement(ownerDoc, "SignatureValue", XMLSignature.XMLNS, dsPrefix);
      if (value != null) {
        sigValueElem.appendChild(ownerDoc.createTextNode(DOMUtils.encodeBase64(value)));
      }
      DOMUtils.setAttributeID(sigValueElem, "Id", svId);
      parent.appendChild(sigValueElem);
    }

    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof SignatureValue)) {
        return false;
      }
      SignatureValue osv = (SignatureValue) o;
      boolean idEqual = svId == null ? osv.getId() == null : svId.equals(osv.getId());
      return idEqual;
    }

    public int hashCode() {
      int result = 17;
      if (svId != null) {
        result = 31 * result + svId.hashCode();
      }
      return result;
    }
  }
}
