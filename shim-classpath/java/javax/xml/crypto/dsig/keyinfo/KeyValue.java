/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.crypto.dsig.keyinfo;

public interface KeyValue extends javax.xml.crypto.XMLStructure {
  java.lang.String DSA_TYPE = "http://www.w3.org/2000/09/xmldsig#DSAKeyValue";
  java.lang.String RSA_TYPE = "http://www.w3.org/2000/09/xmldsig#RSAKeyValue";
  java.lang.String EC_TYPE = "http://www.w3.org/2009/xmldsig11#ECKeyValue";

  java.security.PublicKey getPublicKey() throws java.security.KeyException;
}
