/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.crypto.dsig.keyinfo;

public interface X509IssuerSerial extends javax.xml.crypto.XMLStructure {
  java.lang.String getIssuerName();
  java.math.BigInteger getSerialNumber();
}
