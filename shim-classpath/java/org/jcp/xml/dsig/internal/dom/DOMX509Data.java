/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.jcp.xml.dsig.internal.dom;

import java.io.ByteArrayInputStream;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMCryptoContext;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.keyinfo.X509IssuerSerial;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public final class DOMX509Data implements X509Data {
  private final List<Object> content;

  @SuppressWarnings("rawtypes")
  public DOMX509Data(List content) {
    if (content == null) {
      throw new NullPointerException("content cannot be null");
    }
    List<Object> contentCopy = new ArrayList<Object>(content);
    if (contentCopy.isEmpty()) {
      throw new IllegalArgumentException("content cannot be empty");
    }
    for (int i = 0, size = contentCopy.size(); i < size; i++) {
      Object x509Type = contentCopy.get(i);
      if (x509Type instanceof String) {
        new javax.security.auth.x500.X500Principal((String) x509Type);
      } else if (!(x509Type instanceof byte[]) && !(x509Type instanceof X509Certificate)
          && !(x509Type instanceof X509CRL) && !(x509Type instanceof XMLStructure)) {
        throw new ClassCastException("content[" + i + "] is not a valid X509Data type");
      }
    }
    this.content = Collections.unmodifiableList(contentCopy);
  }

  public DOMX509Data(Element xdElem) throws MarshalException {
    List<Object> newContent = new ArrayList<Object>();
    try {
      CertificateFactory cf = null;
      for (Element e : DOMUtils.children(xdElem)) {
        String n = e.getLocalName();
        if ("X509Certificate".equals(n)) {
          if (cf == null) {
            cf = CertificateFactory.getInstance("X.509");
          }
          newContent.add(cf.generateCertificate(new ByteArrayInputStream(DOMUtils.decodeBase64(DOMUtils.textOf(e)))));
        } else if ("X509IssuerSerial".equals(n)) {
          newContent.add(new DOMX509IssuerSerial(e));
        } else if ("X509SubjectName".equals(n)) {
          newContent.add(DOMUtils.textOf(e));
        } else if ("X509SKI".equals(n)) {
          newContent.add(DOMUtils.decodeBase64(DOMUtils.textOf(e)));
        } else if ("X509CRL".equals(n)) {
          if (cf == null) {
            cf = CertificateFactory.getInstance("X.509");
          }
          newContent.add(cf.generateCRL(new ByteArrayInputStream(DOMUtils.decodeBase64(DOMUtils.textOf(e)))));
        } else {
          newContent.add(new DOMStructure(e));
        }
      }
    } catch (CertificateException e) {
      throw new MarshalException("unable to create X.509 Certificate", e);
    } catch (CRLException e) {
      throw new MarshalException("unable to create X.509 CRL", e);
    }
    this.content = Collections.unmodifiableList(newContent);
  }

  public List<Object> getContent() {
    return content;
  }

  public boolean isFeatureSupported(String feature) {
    if (feature == null) {
      throw new NullPointerException();
    }
    return false;
  }

  public void marshal(Node parent, String dsPrefix, DOMCryptoContext context) throws MarshalException {
    Document ownerDoc = DOMUtils.getOwnerDocument(parent);
    Element xdElem = DOMUtils.createElement(ownerDoc, "X509Data", XMLSignature.XMLNS, dsPrefix);
    try {
      for (Object o : content) {
        if (o instanceof String) {
          xdElem.appendChild(DOMUtils.textElement(ownerDoc, "X509SubjectName", dsPrefix, (String) o));
        } else if (o instanceof byte[]) {
          xdElem.appendChild(DOMUtils.textElement(ownerDoc, "X509SKI", dsPrefix, DOMUtils.encodeBase64((byte[]) o)));
        } else if (o instanceof X509Certificate) {
          xdElem.appendChild(DOMUtils.textElement(ownerDoc, "X509Certificate", dsPrefix,
              DOMUtils.encodeBase64(((X509Certificate) o).getEncoded())));
        } else if (o instanceof X509CRL) {
          xdElem.appendChild(DOMUtils.textElement(ownerDoc, "X509CRL", dsPrefix,
              DOMUtils.encodeBase64(((X509CRL) o).getEncoded())));
        } else if (o instanceof DOMX509IssuerSerial) {
          ((DOMX509IssuerSerial) o).marshal(xdElem, dsPrefix, context);
        } else if (o instanceof DOMStructure) {
          xdElem.appendChild(ownerDoc.importNode(((DOMStructure) o).getNode(), true));
        } else {
          throw new MarshalException("unsupported X509Data content");
        }
      }
    } catch (CertificateException e) {
      throw new MarshalException("unable to encode X.509 Certificate", e);
    } catch (CRLException e) {
      throw new MarshalException("unable to encode X.509 CRL", e);
    }
    parent.appendChild(xdElem);
  }

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof X509Data)) {
      return false;
    }
    return content.equals(((X509Data) o).getContent());
  }

  public int hashCode() {
    return 17 * 31 + content.hashCode();
  }
}
