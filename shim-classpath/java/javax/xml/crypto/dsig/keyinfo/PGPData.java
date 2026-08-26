/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.crypto.dsig.keyinfo;

public interface PGPData extends javax.xml.crypto.XMLStructure {
  java.lang.String TYPE = "http://www.w3.org/2000/09/xmldsig#PGPData";

  java.util.List<javax.xml.crypto.XMLStructure> getExternalElements();
  byte[] getKeyId();
  byte[] getKeyPacket();
}
