/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.crypto.dsig;

public interface Transform extends javax.xml.crypto.XMLStructure, javax.xml.crypto.AlgorithmMethod {
  java.lang.String BASE64 = "http://www.w3.org/2000/09/xmldsig#base64";
  java.lang.String ENVELOPED = "http://www.w3.org/2000/09/xmldsig#enveloped-signature";
  java.lang.String XPATH = "http://www.w3.org/TR/1999/REC-xpath-19991116";
  java.lang.String XPATH2 = "http://www.w3.org/2002/06/xmldsig-filter2";
  java.lang.String XSLT = "http://www.w3.org/TR/1999/REC-xslt-19991116";

  java.security.spec.AlgorithmParameterSpec getParameterSpec();
  javax.xml.crypto.Data transform(javax.xml.crypto.Data data, javax.xml.crypto.XMLCryptoContext xMLCryptoContext, java.io.OutputStream outputStream) throws javax.xml.crypto.dsig.TransformException;
  javax.xml.crypto.Data transform(javax.xml.crypto.Data data, javax.xml.crypto.XMLCryptoContext xMLCryptoContext) throws javax.xml.crypto.dsig.TransformException;
}
