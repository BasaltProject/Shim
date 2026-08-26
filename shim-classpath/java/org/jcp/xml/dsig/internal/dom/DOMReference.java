/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.jcp.xml.dsig.internal.dom;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.crypto.Data;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.NodeSetData;
import javax.xml.crypto.OctetStreamData;
import javax.xml.crypto.URIDereferencer;
import javax.xml.crypto.URIReferenceException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.dom.DOMCryptoContext;
import javax.xml.crypto.dom.DOMURIReference;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.TransformException;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLValidateContext;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public final class DOMReference implements Reference, DOMURIReference {
  private final DigestMethod digestMethod;
  private final String id;
  private final List<Transform> transforms;
  private final List<Transform> allTransforms;
  private final Data appliedTransformData;
  private final String uri;
  private final String type;
  private byte[] digestValue;
  private byte[] calcDigestValue;
  private Element refElem;
  private Element digestValueElem;
  private boolean digested;
  private boolean validated;
  private boolean validationStatus;
  private Data derefData;
  private InputStream dis;
  private Node here;

  @SuppressWarnings("rawtypes")
  public DOMReference(String uri, String type, DigestMethod dm, List appliedTransforms, Data result,
      List transforms, String id, byte[] digestValue) {
    if (dm == null) {
      throw new NullPointerException("DigestMethod must be non-null");
    }
    if (appliedTransforms == null) {
      this.allTransforms = new ArrayList<Transform>();
    } else {
      this.allTransforms = new ArrayList<Transform>(appliedTransforms);
      for (int i = 0, size = this.allTransforms.size(); i < size; i++) {
        if (!(this.allTransforms.get(i) instanceof Transform)) {
          throw new ClassCastException("appliedTransforms[" + i + "] is not a valid type");
        }
      }
    }
    if (transforms == null) {
      this.transforms = Collections.emptyList();
    } else {
      List<Transform> copy = new ArrayList<Transform>(transforms);
      for (int i = 0, size = copy.size(); i < size; i++) {
        if (!(copy.get(i) instanceof Transform)) {
          throw new ClassCastException("transforms[" + i + "] is not a valid type");
        }
      }
      this.transforms = copy;
      this.allTransforms.addAll(copy);
    }
    this.digestMethod = dm;
    if (uri != null) {
      this.uri = uri;
      try {
        new java.net.URI(uri);
      } catch (java.net.URISyntaxException e) {
        throw new IllegalArgumentException(e.getMessage());
      }
    } else {
      this.uri = null;
    }
    this.type = type;
    this.id = id;
    if (digestValue != null) {
      this.digestValue = digestValue.clone();
      this.digested = true;
    }
    this.appliedTransformData = result;
  }

  public DOMReference(Element refElem, Document ownerDoc) throws MarshalException {
    this.refElem = refElem;
    Element next = DOMUtils.getFirstChildElement(refElem);
    List<Transform> transforms = new ArrayList<Transform>(5);
    if (next != null && "Transforms".equals(next.getLocalName())) {
      Element transformElem = DOMUtils.getFirstChildElement(next);
      while (transformElem != null) {
        transforms.add(new DOMTransform(transformElem));
        transformElem = DOMUtils.getNextSiblingElement(transformElem);
      }
      if (DOMUtils.secureValidation() && transforms.size() > 5) {
        throw new MarshalException("A maximum of 5 transforms per Reference are allowed when secure validation is "
            + "enabled");
      }
      next = DOMUtils.getNextSiblingElement(next);
    }
    if (next == null || !"DigestMethod".equals(next.getLocalName())) {
      throw new MarshalException("Invalid element name: " + (next == null ? "null" : next.getLocalName())
          + ", expected DigestMethod");
    }
    this.digestMethod = new DOMDigestMethod(next);
    next = DOMUtils.getNextSiblingElement(next);
    if (next == null || !"DigestValue".equals(next.getLocalName())) {
      throw new MarshalException("Invalid element name: " + (next == null ? "null" : next.getLocalName())
          + ", expected DigestValue");
    }
    this.digestValueElem = next;
    try {
      this.digestValue = DOMUtils.decodeBase64(DOMUtils.textOf(next));
    } catch (IllegalArgumentException e) {
      throw new MarshalException(e);
    }
    this.digested = true;
    this.transforms = transforms;
    this.allTransforms = transforms;
    this.appliedTransformData = null;
    this.uri = DOMUtils.getAttributeValue(refElem, "URI");
    this.id = DOMUtils.getAttributeValue(refElem, "Id");
    this.type = DOMUtils.getAttributeValue(refElem, "Type");
    if (id != null) {
      refElem.setIdAttributeNS(null, "Id", true);
    }
    this.here = refElem.getAttributeNodeNS(null, "URI");
    if (this.here == null) {
      this.here = refElem;
    }
  }

  public DigestMethod getDigestMethod() {
    return digestMethod;
  }

  public String getId() {
    return id;
  }

  public String getURI() {
    return uri;
  }

  public String getType() {
    return type;
  }

  public List<Transform> getTransforms() {
    return Collections.unmodifiableList(allTransforms);
  }

  public byte[] getDigestValue() {
    return digestValue == null ? null : digestValue.clone();
  }

  public byte[] getCalculatedDigestValue() {
    return calcDigestValue == null ? null : calcDigestValue.clone();
  }

  public Node getHere() {
    return here;
  }

  public Data getDereferencedData() {
    return derefData;
  }

  public InputStream getDigestInputStream() {
    return dis;
  }

  public boolean isFeatureSupported(String feature) {
    if (feature == null) {
      throw new NullPointerException();
    }
    return false;
  }

  public void marshal(Node parent, String dsPrefix, DOMCryptoContext context) throws MarshalException {
    Document ownerDoc = DOMUtils.getOwnerDocument(parent);
    refElem = DOMUtils.createElement(ownerDoc, "Reference", XMLSignature.XMLNS, dsPrefix);
    DOMUtils.setAttributeID(refElem, "Id", id);
    DOMUtils.setAttribute(refElem, "URI", uri);
    DOMUtils.setAttribute(refElem, "Type", type);
    if (!allTransforms.isEmpty()) {
      Element transformsElem = DOMUtils.createElement(ownerDoc, "Transforms", XMLSignature.XMLNS, dsPrefix);
      refElem.appendChild(transformsElem);
      for (Transform t : allTransforms) {
        ((DOMTransform) t).marshal(transformsElem, dsPrefix, context);
      }
    }
    ((DOMDigestMethod) digestMethod).marshal(refElem, dsPrefix, context);
    digestValueElem = DOMUtils.createElement(ownerDoc, "DigestValue", XMLSignature.XMLNS, dsPrefix);
    if (digestValue != null) {
      digestValueElem.appendChild(ownerDoc.createTextNode(DOMUtils.encodeBase64(digestValue)));
    }
    refElem.appendChild(digestValueElem);
    parent.appendChild(refElem);
    Node attr = refElem.getAttributeNodeNS(null, "URI");
    here = attr != null ? attr : refElem;
  }

  public void digest(XMLCryptoContext signContext) throws XMLSignatureException {
    byte[] calc = transformAndDigest(signContext);
    this.digestValue = calc;
    this.calcDigestValue = calc;
    this.digested = true;
    if (digestValueElem != null) {
      while (digestValueElem.getFirstChild() != null) {
        digestValueElem.removeChild(digestValueElem.getFirstChild());
      }
      digestValueElem.appendChild(DOMUtils.getOwnerDocument(digestValueElem).createTextNode(
          DOMUtils.encodeBase64(calc)));
    }
  }

  public boolean validate(XMLValidateContext validateContext) throws XMLSignatureException {
    if (validateContext == null) {
      throw new NullPointerException("validateContext cannot be null");
    }
    if (validated) {
      return validationStatus;
    }
    byte[] calc = transformAndDigest(validateContext);
    this.calcDigestValue = calc;
    validationStatus = MessageDigest.isEqual(digestValue, calc);
    validated = true;
    return validationStatus;
  }

  private byte[] transformAndDigest(XMLCryptoContext context) throws XMLSignatureException {
    Data data = appliedTransformData;
    try {
      if (data == null) {
        URIDereferencer deref = context.getURIDereferencer();
        if (deref == null) {
          deref = DOMURIDereferencer.INSTANCE;
        }
        data = deref.dereference(this, context);
      }
      boolean cache = Boolean.TRUE.equals(context.getProperty("javax.xml.crypto.dsig.cacheReference"));
      this.derefData = cache ? data : null;
      for (Transform t : transforms) {
        if (t instanceof DOMTransform) {
          if (here != null && ((DOMTransform) t).transformElem == null) {
            ((DOMTransform) t).setHere(refElem);
          }
        }
        data = t.transform(data, context);
      }
      byte[] bytes;
      if (data instanceof NodeSetData) {
        DOMTransform c14n = new DOMTransform(CanonicalizationMethod.INCLUSIVE, null);
        data = c14n.transform(data, context);
      }
      bytes = readAll(((OctetStreamData) data).getOctetStream());
      this.dis = cache ? new ByteArrayInputStream(bytes) : null;
      MessageDigest md = ((DOMDigestMethod) digestMethod).newDigest();
      return md.digest(bytes);
    } catch (URIReferenceException e) {
      throw new XMLSignatureException(e);
    } catch (TransformException e) {
      throw new XMLSignatureException(e);
    } catch (NoSuchAlgorithmException e) {
      throw new XMLSignatureException(e);
    } catch (java.security.InvalidAlgorithmParameterException e) {
      throw new XMLSignatureException(e);
    } catch (IOException e) {
      throw new XMLSignatureException(e);
    }
  }

  private static byte[] readAll(InputStream in) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    byte[] buf = new byte[4096];
    int n;
    while ((n = in.read(buf)) >= 0) {
      bos.write(buf, 0, n);
    }
    return bos.toByteArray();
  }

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Reference)) {
      return false;
    }
    Reference oref = (Reference) o;
    boolean idsEqual = id == null ? oref.getId() == null : id.equals(oref.getId());
    boolean urisEqual = uri == null ? oref.getURI() == null : uri.equals(oref.getURI());
    boolean typesEqual = type == null ? oref.getType() == null : type.equals(oref.getType());
    boolean digestValuesEqual = java.util.Arrays.equals(digestValue, oref.getDigestValue());
    return digestMethod.equals(oref.getDigestMethod()) && idsEqual && urisEqual && typesEqual
        && allTransforms.equals(oref.getTransforms()) && digestValuesEqual;
  }

  public int hashCode() {
    int result = 17;
    if (id != null) {
      result = 31 * result + id.hashCode();
    }
    if (uri != null) {
      result = 31 * result + uri.hashCode();
    }
    if (type != null) {
      result = 31 * result + type.hashCode();
    }
    if (digestValue != null) {
      result = 31 * result + java.util.Arrays.hashCode(digestValue);
    }
    result = 31 * result + digestMethod.hashCode();
    result = 31 * result + allTransforms.hashCode();
    return result;
  }
}
