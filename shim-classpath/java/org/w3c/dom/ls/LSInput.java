/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.ls;

public interface LSInput {
  java.io.Reader getCharacterStream();

  void setCharacterStream(java.io.Reader p0);

  java.io.InputStream getByteStream();

  void setByteStream(java.io.InputStream p0);

  String getStringData();

  void setStringData(String p0);

  String getSystemId();

  void setSystemId(String p0);

  String getPublicId();

  void setPublicId(String p0);

  String getBaseURI();

  void setBaseURI(String p0);

  String getEncoding();

  void setEncoding(String p0);

  boolean getCertifiedText();

  void setCertifiedText(boolean p0);
}
