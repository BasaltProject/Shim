/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.crypto.dsig.keyinfo;

public interface RetrievalMethod extends javax.xml.crypto.URIReference, javax.xml.crypto.XMLStructure {
  javax.xml.crypto.Data dereference(javax.xml.crypto.XMLCryptoContext xMLCryptoContext) throws javax.xml.crypto.URIReferenceException;
  java.util.List<javax.xml.crypto.dsig.Transform> getTransforms();
  java.lang.String getURI();
}
