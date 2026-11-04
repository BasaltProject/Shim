/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.crypto.dsig;

public interface SignedInfo extends javax.xml.crypto.XMLStructure {
  javax.xml.crypto.dsig.CanonicalizationMethod getCanonicalizationMethod();
  java.io.InputStream getCanonicalizedData();
  java.lang.String getId();
  java.util.List<javax.xml.crypto.dsig.Reference> getReferences();
  javax.xml.crypto.dsig.SignatureMethod getSignatureMethod();
}
