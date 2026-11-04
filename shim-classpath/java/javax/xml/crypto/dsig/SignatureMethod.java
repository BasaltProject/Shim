/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.crypto.dsig;

public interface SignatureMethod extends javax.xml.crypto.XMLStructure, javax.xml.crypto.AlgorithmMethod {
  java.lang.String DSA_SHA1 = "http://www.w3.org/2000/09/xmldsig#dsa-sha1";
  java.lang.String DSA_SHA256 = "http://www.w3.org/2009/xmldsig11#dsa-sha256";
  java.lang.String RSA_SHA1 = "http://www.w3.org/2000/09/xmldsig#rsa-sha1";
  java.lang.String RSA_SHA224 = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha224";
  java.lang.String RSA_SHA256 = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256";
  java.lang.String RSA_SHA384 = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha384";
  java.lang.String RSA_SHA512 = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha512";
  java.lang.String SHA1_RSA_MGF1 = "http://www.w3.org/2007/05/xmldsig-more#sha1-rsa-MGF1";
  java.lang.String SHA224_RSA_MGF1 = "http://www.w3.org/2007/05/xmldsig-more#sha224-rsa-MGF1";
  java.lang.String SHA256_RSA_MGF1 = "http://www.w3.org/2007/05/xmldsig-more#sha256-rsa-MGF1";
  java.lang.String SHA384_RSA_MGF1 = "http://www.w3.org/2007/05/xmldsig-more#sha384-rsa-MGF1";
  java.lang.String SHA512_RSA_MGF1 = "http://www.w3.org/2007/05/xmldsig-more#sha512-rsa-MGF1";
  java.lang.String ECDSA_SHA1 = "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha1";
  java.lang.String ECDSA_SHA224 = "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha224";
  java.lang.String ECDSA_SHA256 = "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256";
  java.lang.String ECDSA_SHA384 = "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha384";
  java.lang.String ECDSA_SHA512 = "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha512";
  java.lang.String HMAC_SHA1 = "http://www.w3.org/2000/09/xmldsig#hmac-sha1";
  java.lang.String HMAC_SHA224 = "http://www.w3.org/2001/04/xmldsig-more#hmac-sha224";
  java.lang.String HMAC_SHA256 = "http://www.w3.org/2001/04/xmldsig-more#hmac-sha256";
  java.lang.String HMAC_SHA384 = "http://www.w3.org/2001/04/xmldsig-more#hmac-sha384";
  java.lang.String HMAC_SHA512 = "http://www.w3.org/2001/04/xmldsig-more#hmac-sha512";
  java.lang.String RSA_PSS = "http://www.w3.org/2007/05/xmldsig-more#rsa-pss";
  java.lang.String ED25519 = "http://www.w3.org/2021/04/xmldsig-more#eddsa-ed25519";
  java.lang.String ED448 = "http://www.w3.org/2021/04/xmldsig-more#eddsa-ed448";
  java.lang.String SHA3_224_RSA_MGF1 = "http://www.w3.org/2007/05/xmldsig-more#sha3-224-rsa-MGF1";
  java.lang.String SHA3_256_RSA_MGF1 = "http://www.w3.org/2007/05/xmldsig-more#sha3-256-rsa-MGF1";
  java.lang.String SHA3_384_RSA_MGF1 = "http://www.w3.org/2007/05/xmldsig-more#sha3-384-rsa-MGF1";
  java.lang.String SHA3_512_RSA_MGF1 = "http://www.w3.org/2007/05/xmldsig-more#sha3-512-rsa-MGF1";
  java.lang.String ECDSA_SHA3_224 = "http://www.w3.org/2021/04/xmldsig-more#ecdsa-sha3-224";
  java.lang.String ECDSA_SHA3_256 = "http://www.w3.org/2021/04/xmldsig-more#ecdsa-sha3-256";
  java.lang.String ECDSA_SHA3_384 = "http://www.w3.org/2021/04/xmldsig-more#ecdsa-sha3-384";
  java.lang.String ECDSA_SHA3_512 = "http://www.w3.org/2021/04/xmldsig-more#ecdsa-sha3-512";

  java.security.spec.AlgorithmParameterSpec getParameterSpec();
}
