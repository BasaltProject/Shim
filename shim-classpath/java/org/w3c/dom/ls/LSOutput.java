/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.ls;

public interface LSOutput {
  java.io.Writer getCharacterStream();

  void setCharacterStream(java.io.Writer p0);

  java.io.OutputStream getByteStream();

  void setByteStream(java.io.OutputStream p0);

  String getSystemId();

  void setSystemId(String p0);

  String getEncoding();

  void setEncoding(String p0);
}
