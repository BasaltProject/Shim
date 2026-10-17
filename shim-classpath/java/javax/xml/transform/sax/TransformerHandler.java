/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.transform.sax;

public interface TransformerHandler extends org.xml.sax.ContentHandler, org.xml.sax.ext.LexicalHandler,
    org.xml.sax.DTDHandler {
  void setResult(javax.xml.transform.Result p0) throws IllegalArgumentException;

  void setSystemId(String p0);

  String getSystemId();

  javax.xml.transform.Transformer getTransformer();
}
