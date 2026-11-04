/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.crypto.dsig;

public interface Reference extends javax.xml.crypto.URIReference, javax.xml.crypto.XMLStructure {
  byte[] getCalculatedDigestValue();
  javax.xml.crypto.Data getDereferencedData();
  java.io.InputStream getDigestInputStream();
  javax.xml.crypto.dsig.DigestMethod getDigestMethod();
  byte[] getDigestValue();
  java.lang.String getId();
  java.util.List<javax.xml.crypto.dsig.Transform> getTransforms();
  boolean validate(javax.xml.crypto.dsig.XMLValidateContext xMLValidateContext) throws javax.xml.crypto.dsig.XMLSignatureException;
}
