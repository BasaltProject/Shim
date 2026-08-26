/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.crypto.dsig;

public interface XMLSignature extends javax.xml.crypto.XMLStructure {
  java.lang.String XMLNS = "http://www.w3.org/2000/09/xmldsig#";

  java.lang.String getId();
  javax.xml.crypto.dsig.keyinfo.KeyInfo getKeyInfo();
  javax.xml.crypto.KeySelectorResult getKeySelectorResult();
  java.util.List<javax.xml.crypto.dsig.XMLObject> getObjects();
  javax.xml.crypto.dsig.XMLSignature.SignatureValue getSignatureValue();
  javax.xml.crypto.dsig.SignedInfo getSignedInfo();
  void sign(javax.xml.crypto.dsig.XMLSignContext xMLSignContext) throws javax.xml.crypto.MarshalException, javax.xml.crypto.dsig.XMLSignatureException;
  boolean validate(javax.xml.crypto.dsig.XMLValidateContext xMLValidateContext) throws javax.xml.crypto.dsig.XMLSignatureException;

  public interface SignatureValue extends javax.xml.crypto.XMLStructure {
    java.lang.String getId();
    byte[] getValue();
    boolean validate(javax.xml.crypto.dsig.XMLValidateContext xMLValidateContext) throws javax.xml.crypto.dsig.XMLSignatureException;
  }
}
