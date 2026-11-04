/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.crypto.dsig.keyinfo;

public interface KeyInfo extends javax.xml.crypto.XMLStructure {
  java.util.List<javax.xml.crypto.XMLStructure> getContent();
  java.lang.String getId();
  void marshal(javax.xml.crypto.XMLStructure xMLStructure, javax.xml.crypto.XMLCryptoContext xMLCryptoContext) throws javax.xml.crypto.MarshalException;
}
