/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.crypto.dsig;

public interface DigestMethod extends javax.xml.crypto.XMLStructure, javax.xml.crypto.AlgorithmMethod {
  java.lang.String SHA1 = "http://www.w3.org/2000/09/xmldsig#sha1";
  java.lang.String SHA224 = "http://www.w3.org/2001/04/xmldsig-more#sha224";
  java.lang.String SHA256 = "http://www.w3.org/2001/04/xmlenc#sha256";
  java.lang.String SHA384 = "http://www.w3.org/2001/04/xmldsig-more#sha384";
  java.lang.String SHA512 = "http://www.w3.org/2001/04/xmlenc#sha512";
  java.lang.String RIPEMD160 = "http://www.w3.org/2001/04/xmlenc#ripemd160";
  java.lang.String SHA3_224 = "http://www.w3.org/2007/05/xmldsig-more#sha3-224";
  java.lang.String SHA3_256 = "http://www.w3.org/2007/05/xmldsig-more#sha3-256";
  java.lang.String SHA3_384 = "http://www.w3.org/2007/05/xmldsig-more#sha3-384";
  java.lang.String SHA3_512 = "http://www.w3.org/2007/05/xmldsig-more#sha3-512";

  java.security.spec.AlgorithmParameterSpec getParameterSpec();
}
