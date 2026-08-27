/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.jcp.xml.dsig.internal.dom;

import java.security.InvalidAlgorithmParameterException;
import java.security.spec.AlgorithmParameterSpec;
import java.io.OutputStream;
import javax.xml.crypto.Data;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMCryptoContext;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.TransformException;
import javax.xml.crypto.dsig.TransformService;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public final class DOMTransformService extends TransformService {
  private DOMTransform delegate;
  private final String algorithm;

  public DOMTransformService(String algorithm) {
    this.algorithm = algorithm;
  }

  public void init(TransformParameterSpec params) throws InvalidAlgorithmParameterException {
    delegate = new DOMTransform(algorithm, params);
  }

  public void init(XMLStructure parent, XMLCryptoContext context) throws InvalidAlgorithmParameterException {
    if (parent == null) {
      throw new NullPointerException();
    }
    Node n = ((DOMStructure) parent).getNode();
    try {
      delegate = new DOMTransform((Element) n);
    } catch (MarshalException e) {
      throw new InvalidAlgorithmParameterException(e);
    }
  }

  public void marshalParams(XMLStructure parent, XMLCryptoContext context) throws MarshalException {
    if (delegate == null) {
      try {
        delegate = new DOMTransform(algorithm, null);
      } catch (InvalidAlgorithmParameterException e) {
        throw new MarshalException(e);
      }
    }
    Element elem = (Element) ((DOMStructure) parent).getNode();
    delegate.marshalParams(elem, elem.getOwnerDocument(), DOMUtils.getSignaturePrefix(context));
  }

  public AlgorithmParameterSpec getParameterSpec() {
    return delegate == null ? null : delegate.getParameterSpec();
  }

  public Data transform(Data data, XMLCryptoContext context) throws TransformException {
    return delegate.transform(data, context);
  }

  public Data transform(Data data, XMLCryptoContext context, OutputStream os) throws TransformException {
    return delegate.transform(data, context, os);
  }

  public boolean isFeatureSupported(String feature) {
    if (feature == null) {
      throw new NullPointerException();
    }
    return false;
  }
}
