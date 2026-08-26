/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.crypto.dsig.keyinfo;

public interface X509Data extends javax.xml.crypto.XMLStructure {
  java.lang.String TYPE = "http://www.w3.org/2000/09/xmldsig#X509Data";
  java.lang.String RAW_X509_CERTIFICATE_TYPE = "http://www.w3.org/2000/09/xmldsig#rawX509Certificate";

  java.util.List<?> getContent();
}
