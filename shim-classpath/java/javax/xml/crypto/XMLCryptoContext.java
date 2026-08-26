/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.crypto;

public interface XMLCryptoContext {
  java.lang.Object get(java.lang.Object object);
  java.lang.String getBaseURI();
  java.lang.String getDefaultNamespacePrefix();
  javax.xml.crypto.KeySelector getKeySelector();
  java.lang.String getNamespacePrefix(java.lang.String string, java.lang.String string2);
  java.lang.Object getProperty(java.lang.String string);
  javax.xml.crypto.URIDereferencer getURIDereferencer();
  java.lang.Object put(java.lang.Object object, java.lang.Object object2);
  java.lang.String putNamespacePrefix(java.lang.String string, java.lang.String string2);
  void setBaseURI(java.lang.String string);
  void setDefaultNamespacePrefix(java.lang.String string);
  void setKeySelector(javax.xml.crypto.KeySelector keySelector);
  java.lang.Object setProperty(java.lang.String string, java.lang.Object object);
  void setURIDereferencer(javax.xml.crypto.URIDereferencer uRIDereferencer);
}
