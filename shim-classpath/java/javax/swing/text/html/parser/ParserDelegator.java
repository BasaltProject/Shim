/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.text.html.parser;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import javax.swing.text.html.HTMLEditorKit;

public class ParserDelegator extends HTMLEditorKit.Parser implements Serializable {
  private static DTD dtd = null;

  protected static synchronized void setDefaultDTD() {
    if (dtd == null) {
      try {
        dtd = DTD.getDTD("html32");
      } catch (IOException e) {
        throw new Error("could not build the HTML DTD");
      }
    }
  }

  protected static DTD createDTD(DTD dtd, String name) {
    try {
      return DTD.getDTD(name);
    } catch (IOException e) {
      return dtd;
    }
  }

  public ParserDelegator() {
    setDefaultDTD();
  }

  public void parse(Reader r, HTMLEditorKit.ParserCallback cb, boolean ignoreCharSet) throws IOException {
    new DocumentParser(dtd).parse(r, cb, ignoreCharSet);
  }
}
